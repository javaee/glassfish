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
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.logging.LogDomains;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.Startup;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

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
public class GmsAdapterService implements Startup, PostConstruct {

    final static Logger logger = LogDomains.getLogger(
        GmsAdapterService.class, LogDomains.CORE_LOGGER);

    @Inject(name=ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    ServerEnvironment env;

    @Inject
    Habitat habitat;

    /* TODO:
     * For M2, there is just the one cluster so we'll have only
     * one GMSAdapter. Going forward, we want one adapter created
     * for each cluster (in the DAS).
     */
    GMSAdapter gmsAdapter;

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
     *
     * Look at the list of existing clusters. Stop after finding the
     * first gms-enabled cluster.
     *
     * todo: in instance, only check *my* cluster (if I'm part of
     * a cluster)
     */
    @Override
    public void postConstruct() {
        Domain domain = habitat.getComponent(Domain.class);
        Clusters clusters = domain.getClusters();
        if (clusters != null) {
            if (server.isDas()) {
                checkAllClusters(clusters);
            } else {
                checkCurrentCluster(clusters);
            }
        }
    }

    @Override
    public String toString() {
        return "GMS Loader";
    }

    /* TODO:
     * In instance case, there is only the one adapter. In the DAS,
     * do we want multiple instances of GMSAdapter and each one has
     * only the single getModule() method?
     */
    public GMSAdapter getGMSAdapter() {
        return gmsAdapter;
    }

    /* TODO:
     */
    public GMSAdapter getGMSAdapterByName(String clusterName) {
        return gmsAdapter;
    }

    /* TODO:
     * Create a GMSAdapter for each cluster that has gms enabled.
     */
    private void checkAllClusters(Clusters clusters) {
        logger.fine("In DAS. Checking all clusters.");
        for (Cluster cluster : clusters.getCluster()) {
            String gmsEnString = cluster.getGmsEnabled();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format(
                    "cluster %s found with gms-enabled='%s'",
                    cluster.getName(), gmsEnString));
            }
            if (gmsEnString != null && Boolean.parseBoolean(gmsEnString)) {
                loadModule(cluster);
            }
        }
    }

    /* TODO:
     * Need to find only the cluster to which this instance belongs
     * and check to see if gms is enabled.
     */
    private void checkCurrentCluster(Clusters clusters) {
        logger.fine("In instance. Looking for current cluster.");
        for (Cluster cluster : clusters.getCluster()) {
            String gmsEnString = cluster.getGmsEnabled();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format(
                    "cluster %s found with gms-enabled='%s'",
                    cluster.getName(), gmsEnString));
            }
            if (gmsEnString != null && Boolean.parseBoolean(gmsEnString)) {
                loadModule(cluster);
                break;
            }
        }
    }

    /*
     * TODO: in case of multiple clusters, we want an instance
     * of GMSAdapter for each group. The cluster param is more
     * of a placeholder. May only need a name to use for group here.
     */
    private void loadModule(Cluster cluster) {
        // getting the service from the habitat is enough to load it
        gmsAdapter = habitat.getByContract(GMSAdapter.class);
    }

}
