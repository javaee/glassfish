/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.javaee.services;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.glassfish.api.Startup;
import org.glassfish.api.Async;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.config.Changed;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.appserv.connectors.spi.ConnectorConstants;

import javax.naming.NamingException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.beans.PropertyChangeEvent;

/**
 * Binds proxy objects in the jndi namespace for all the JdbcResources defined in the
 * configuration. Those objects will delay the instantiation of the associated
 * resource adapeters until code looks them up in the naming manager.
 *
 * @author Jerome Dochez
 */
@Service
@Async
public class ResourceAdaptersBinder implements Startup, PostConstruct, PreDestroy, ConfigListener {

    @Inject
    JdbcResource[] jdbcResources;

    @Inject
    JdbcConnectionPool[] pools;

    @Inject
    Resources resources;

    @Inject
    GlassfishNamingManager manager;

    @Inject
    Logger logger;

    @Inject
    Habitat raProxyHabitat;

    @Inject
    Habitat connectorRuntimeHabitat;

    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {

        deployAllJdbcResourcesAndPools();
    }

    private void deployAllJdbcResourcesAndPools() {
        for (JdbcResource resource : jdbcResources) {
            try {
                JdbcConnectionPool pool = getAssociatedPool(resource.getPoolName());
                if (pool == null) {
                    logger.log(Level.SEVERE, "Could not get the pool [ " + resource.getPoolName() + " ] of resource [ " +
                            resource.getJndiName() + " ]");
                    continue;
                }
                bindResource(resource, pool, resource.getJndiName(), ConnectorConstants.RES_TYPE_JDBC);
            } catch (NamingException e) {
                logger.log(Level.SEVERE, "Cannot bind " + resource.getPoolName() + " to naming manager", e);
            }
        }
    }

    public void bindResource(Object resource, Object pool, String resourceName, String resourceType) throws NamingException {
        ResourceAdapterProxy raProxy = constructResourceProxy(resource, pool, resourceType, resourceName);
        manager.publishObject(resourceName, raProxy, true);
    }

    private ResourceAdapterProxy constructResourceProxy(Object resource, Object pool, String resourceType,
                                                        String resourceName) {
        ResourceAdapterProxy raProxy = raProxyHabitat.getComponent(ResourceAdapterProxy.class);
        raProxy.setResource(resource);
        raProxy.setConnectionPool(pool);
        raProxy.setResourceType(resourceType);
        raProxy.setResourceName(resourceName);
        return raProxy;
    }

    /**
     * get the associated pool's name for the jdbc-resource
     * @param  poolName Pool Name
     * @return JdbcConnectionPool
     */
    private JdbcConnectionPool getAssociatedPool(String poolName) {
        JdbcConnectionPool result = null;
        for (JdbcConnectionPool pool : pools) {
            if (pool.getName().equalsIgnoreCase(poolName)) {
                result = pool;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {
        raProxyHabitat.getInhabitant(com.sun.appserv.connectors.spi.ConnectorRuntime.class, null).release();
        
        /*ConnectorRuntime runtime = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class, null);

        List<String> poolNames = getAllPoolNames(pools);
        List<String> resourceNames = getAllResourceNames(resources);

        runtime.shutdownAllActiveResourceAdapters(poolNames, resourceNames);
        */
    }

    private List<String> getAllPoolNames(JdbcConnectionPool[] pools) {
        List<String> poolNames = new ArrayList<String>();
        for(JdbcConnectionPool pool : pools){
            poolNames.add(pool.getName());
        }
        return poolNames;
    }

    private List<String> getAllResourceNames(JdbcResource[] resources) {
        List<String> resourceNames = new ArrayList<String>();
        for(JdbcResource resource : resources){
            resourceNames.add(resource.getJndiName());
        }
        return resourceNames;           
    }

    /**
     * Notification that @Configured objects that were injected have changed
     *
     * @param events list of changes
     */
    public void changed(PropertyChangeEvent[] events) {
        // I am not so interested with the list of events, just sort who got added or removed for me.
        ConfigSupport.sortAndDispatch(events, new Changed() {
            /**
             * Notification of a change on a configuration object
             *
             * @param type            type of change : ADD mean the changedInstance was added to the parent
             *                        REMOVE means the changedInstance was removed from the parent, CHANGE means the
             *                        changedInstance has mutated.
             * @param changedType     type of the configuration object
             * @param changedInstance changed instance.
             */
            public <T extends ConfigBeanProxy> void changed(TYPE type, Class<T> changedType, T changedInstance) {
                switch(type) {
                    case ADD : logger.info("A new " + changedType.getName() + " was added : " + changedInstance);
                        break;

                    case CHANGE : logger.info("A " + changedType.getName() + " was changed : " + changedInstance);
                        break;

                    case REMOVE : logger.info("A " + changedType.getName() + " was removed : " + changedInstance);
                        break;
                }
            }
        }, logger);

    }
}
