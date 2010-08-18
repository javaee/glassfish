/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.gms.bootstrap;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.logging.LogDomains;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.Startup;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.*;

/**
 * This service is responsible for loading the group management
 * service. In the DAS, GMS will only be enabled if there is at least
 * one cluster present with the gms-enabled attribute set to true.
 * In an instance, GMS will be enabled if the cluster containing this
 * instance has gms-enabled set to true.
 *
 * Components can inject this service in order to obtain a reference
 * to a GMSAdapter object. From this, the appropriate GroupManagementService
 * object can be retrieved.
 */
@Service()
public class GMSAdapterService implements Startup, PostConstruct, ConfigListener {

    final static Logger logger = LogDomains.getLogger(
        GMSAdapterService.class, LogDomains.CORE_LOGGER);

    @Inject
    Clusters clusters;

    @Inject(name=ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    ServerEnvironment env;

    @Inject
    Habitat habitat;

    static private Object lock = new Object();

    List<GMSAdapter> gmsAdapters = new LinkedList<GMSAdapter>();

    final static private Level TRACE_LEVEL = Level.FINE;

    /**
     * Returns the lifecyle of the service. This service may not be needed
     * after startup -- we still need to determine how to load GMS when
     * a gms-enabled cluster is first created during runtime.
     * TODO: determine SERVER v START
     */
    @Override
    public Startup.Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }

    /**
     * Starts the application loader service.
     */
    @Override
    public void postConstruct() {
        if (clusters != null) {
            if (server.isDas()) {
                checkAllClusters(clusters);
            } else {
                Cluster cluster = server.getCluster();
                if (cluster != null) {
                    checkCluster(cluster);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "GMS Loader";
    }

    /*
     */
    public GMSAdapter getGMSAdapter() {
        synchronized(lock) {
            if (gmsAdapters.size() > 1) {
                throw new IllegalStateException("use getGMSAdapterByName method when there are multiple clusters");
            } else if (gmsAdapters.size() == 1) {
                return gmsAdapters.get(0);
            } else {
                return null;
            }
        }
    }

    public boolean isGmsEnabled() {
        return gmsAdapters.size() > 0;
    }

    public GMSAdapter getGMSAdapterByName(String clusterName) {
        synchronized(lock) {
            return habitat.getComponent(GMSAdapter.class, clusterName);
        }
    }

    /**
     * Create a GMSAdapter for each cluster that has gms enabled.
     */
    private void checkAllClusters(Clusters clusters) {
        if (logger.isLoggable(TRACE_LEVEL)) {
            logger.log(TRACE_LEVEL, "In DAS. Checking all clusters.");
        }
        for (Cluster cluster : clusters.getCluster()) {
            checkCluster(cluster);
        }
    }

    private GMSAdapter checkCluster(Cluster cluster) {
        GMSAdapter result = null;
        String gmsEnString = cluster.getGmsEnabled();
        if (logger.isLoggable(TRACE_LEVEL)) {
            logger.log(TRACE_LEVEL, String.format("cluster %s found with gms-enabled='%s'",
                        cluster.getName(), gmsEnString));
        }
        if (gmsEnString != null && Boolean.parseBoolean(gmsEnString)) {
            result = loadModule(cluster);
        }
        return result;
    }

    /*
     * initial support for multiple clusters in DAS. a clustered instance can only belong to one cluster.
     */
    private GMSAdapter loadModule(Cluster cluster) {
        GMSAdapter result = null;
        synchronized(lock) {
            result = getGMSAdapterByName(cluster.getName());
            if (logger.isLoggable(TRACE_LEVEL)) {
                logger.log(TRACE_LEVEL, "lookup GMSAdapter by clusterName=" + cluster.getName() + " returned " + result);
            }
            if (result == null) {
                if (logger.isLoggable(TRACE_LEVEL)) {
                    logger.log(TRACE_LEVEL, "creating gms-adapter for clustername " + cluster.getName() + " since no gms adapter found for clustername " + cluster.getName());
                }
                result = habitat.getByContract(GMSAdapter.class);

                // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=12850
                if (result == null) {
                    logger.log(Level.WARNING, "gmsadapter.not.available");
                    return null;
                }
                boolean initResult = result.initialize(cluster.getName());
                habitat.addIndex(new ExistingSingletonInhabitant<GMSAdapter>(result), GMSAdapter.class.getName(), cluster.getName());
                if (logger.isLoggable(TRACE_LEVEL)) {
                    logger.log(TRACE_LEVEL, "loadModule: registered created gmsadapter for cluster " + cluster.getName() + " initialized result=" + initResult);
                }
                gmsAdapters.add(result);
            }
        }
        return result;
    }

    /*
     * On create-cluster event, DAS joins a gms-enabled cluster.
     * On delete-cluster event, DAS leaves a gms-enabled cluster.
     */

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        if (env.isDas()) {
            return ConfigSupport.sortAndDispatch(events, new Changed() {
                @Override
                public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
                    if (changedType == Cluster.class && type == TYPE.ADD) {  //create-cluster
                        Cluster cluster = (Cluster) changedInstance;
                        if (logger.isLoggable(TRACE_LEVEL)) {
                            logger.log(TRACE_LEVEL, "ClusterChangeEvent add cluster " + cluster.getName());
                        }
                        GMSAdapter localGmsAdapter = checkCluster(cluster);
                        if (localGmsAdapter != null) {
                            localGmsAdapter.getModule().reportJoinedAndReadyState(cluster.getName());
                        }

                        // todo:  when supporting multiple clusters, ensure that newly added cluster has a different gms-multicast-address than all existing clusters.
                        //        currently, generating a unique multicast address depending on random so this check is necessary.
                    }
                    if (changedType == Cluster.class && type == TYPE.REMOVE) {  //remove-cluster
                        Cluster cluster = (Cluster) changedInstance;
                        if (logger.isLoggable(TRACE_LEVEL)) {
                            logger.log(TRACE_LEVEL, "ClusterChangeEvent remove cluster " + cluster.getName());
                        }
                        synchronized(lock) {
                            GMSAdapter localGmsAdapter = getGMSAdapterByName(cluster.getName());
                            if (localGmsAdapter != null) {
                                gmsAdapters.remove(localGmsAdapter);
                                localGmsAdapter.getModule().shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                                boolean result = habitat.removeIndex(GMSAdapter.class.getName(), localGmsAdapter);
                                if (logger.isLoggable(TRACE_LEVEL)) {
                                    logger.log(TRACE_LEVEL, "removeIndex(" + GMSAdapter.class.getName() + ") returned result of " + result);
                                }

                                // remove GMS module for deleted cluster.  Must do this or will fail if the cluster is recreated before DAS is stopped.
                                localGmsAdapter.complete();
                            }
                        }
                    }
                    return null;
                }
            }, logger);
        }
        return null;
    }
}
