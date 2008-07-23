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
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;
import org.glassfish.api.naming.NamingObjectsProvider;

import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import java.util.*;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;


@Service
/**
 * Resource manager to bind various allResources during startup, create/update/delete of resource/pool
 * @author Jagadish Ramu
 */
public class ResourceManager implements NamingObjectsProvider, PostConstruct, PreDestroy, ConfigListener {

    @Inject
    private JdbcResource[] jdbcResources;

    @Inject
    private JdbcConnectionPool[] jdbcPools;

    @Inject
    private Logger logger;

    @Inject
    private ResourceAdaptersBinder resourcesBinder;

    @Inject
    private Habitat connectorRuntimeHabitat;

    @Inject
    private Resources allResources; // Needed so as to listen to config changes.

    private ConnectorRuntime runtime;

    public void postConstruct() {
        //TODO V3 need to get jdbc pools/resources from Resources
        resourcesBinder.deployAllJdbcResourcesAndPools(jdbcResources, jdbcPools);
        //TODO V3 handle connector system resources, pools later
        //resourcesBinder.deployAllConnectorResourcesAndPools(connectorResources, connectorPools);
    }

    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {

        //TODO V3 : Admin need to make sure that poolnames are unique. As of V2 they are not unique.
        Collection pools = getAllSystemRAPools();
        Collection resources = getAllSystemRAResources();

        //TODO V3 : even in case when there is no resource used by an application (no RAR was started),
        //TODO V3 this seems to be called ??
        getConnectorRuntime().shutdownAllActiveResourceAdapters(pools, resources);
    }

    private ConnectorRuntime getConnectorRuntime() {
        //TODO V3 not synchronized
        if(runtime == null){
            runtime = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class, null);
        }
        return runtime;
    }

    /**
     * get all System RAR pools
     * @return Collection of system RAR pools
     */
    private Collection getAllSystemRAPools(){
        List pools = new ArrayList();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof JdbcConnectionPool ){
                pools.add(resource);
            }else if( resource instanceof ConnectorConnectionPool){
                String raName = ((ConnectorConnectionPool)resource).getResourceAdapterName();
                if( ConnectorsUtil.belongsToSystemRA(raName) ){
                    pools.add(resource);
                }
            }
        }
        return pools;
    }

    /**
     * get all System RAR resources
     * @return Collection of system RAR resources
     */
    private Collection getAllSystemRAResources(){
        List systemRAResources = new ArrayList();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof JdbcResource){
                systemRAResources.add(resource);
            }else if( resource instanceof ConnectorResource){
                String poolName = ((ConnectorResource)resource).getPoolName();
                String raName = getResourceAdapterNameOfPool(poolName);
                if( ConnectorsUtil.belongsToSystemRA(raName) ){
                    systemRAResources.add(resource);
                }
            }
        }
        return systemRAResources;
    }

    /**
     * Given the poolname, retrieve the resourceadapter name
     * @param poolName
     * @return resource-adaapter name
     */
    private String getResourceAdapterNameOfPool(String poolName) {
        String raName = ""; //TODO V3 this need not be initialized to ""
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool ccp = (ConnectorConnectionPool)resource;
                String name = ccp.getName();
                if(name.equalsIgnoreCase(poolName)){
                    raName = ccp.getResourceAdapterName();
                }
            }
        }
        return raName;
    }

    /**
     * get all the connection pool (jdbc/connector) names
     * @return Collection of pool names
     */
    public Collection<String> getAllPoolNames(){
        //TODO V3 unused ?
        List<String> poolNames = new ArrayList<String>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof JdbcConnectionPool ){
                poolNames.add(((JdbcConnectionPool)resource).getName());
            }else if( resource instanceof ConnectorConnectionPool){
                poolNames.add(((ConnectorConnectionPool)resource).getName());
            }
        }
        return poolNames;
    }

    /**
     * get all resource (jdbc/connector) names
     * @return Collection of resource names
     */
    public Collection<String> getAllResourceNames(){
        //TODO V3 unused ?
        List<String> resourceNames = new ArrayList<String>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof JdbcResource){
                resourceNames.add(((JdbcResource)resource).getJndiName());
            }else if(resource instanceof ConnectorResource){
                resourceNames.add(((ConnectorResource)resource).getJndiName());
            }
        }
        return resourceNames;
    }

    /**
     * deploys the resources (pool/resources) of a particular resource-adapter
     * @param moduleName resource-adapter name
     */
    public void deployResourcesForModule(String moduleName){
        Collection<ConnectorConnectionPool> connectionPools = getAllPoolsOfModule(moduleName);
        Collection<String> poolNames = getAllPoolNames(connectionPools);
        Collection<ConnectorResource> connectorResources = getAllResources(poolNames);
        ConnectorConnectionPool[] pools = new ConnectorConnectionPool[connectionPools.size()];
        ConnectorResource[] resources = new ConnectorResource[connectorResources.size()];

        resourcesBinder.deployAllConnectorResourcesAndPools(connectorResources.toArray(resources),
                connectionPools.toArray(pools));
    }

    /**
     * undeploys the resources (pool/resources) of a particular resource-adapter
     * @param moduleName resource-adapter name
     */
    public void undeployResourcesForModule(String moduleName){
        Collection<ConnectorConnectionPool> connectionPools = getAllPoolsOfModule(moduleName);
        Collection<String> poolNames = getAllPoolNames(connectionPools);
        Collection<ConnectorResource> connectorResources = getAllResources(poolNames);

        destroyResourcesAndPools(connectorResources, connectionPools);
    }

    /**
     * destroys a list of resource and pools (jdbc/connector)
     * @param resources to be destroyed
     * @param pools to be destroyed
     */
    private void destroyResourcesAndPools(Collection resources, Collection pools) {
        getConnectorRuntime().destroyResourcesAndPools(resources, pools);
    }

    private Collection<ConnectorResource> getAllResources(Collection<String> poolNames) {
        List<ConnectorResource> connectorResources = new ArrayList<ConnectorResource>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorResource){
                ConnectorResource connectorResource = (ConnectorResource)resource;
                if(poolNames.contains(connectorResource.getPoolName())){
                    connectorResources.add(connectorResource);
                }
            }
        }
        return connectorResources;
    }

    /**
     * get the list of pool names
     * @param connectionPools list of pools
     * @return list of pol names
     */
    private Collection<String> getAllPoolNames(Collection<ConnectorConnectionPool> connectionPools) {
        Set<String> poolNames = new HashSet<String>();
        for(ConnectorConnectionPool pool : connectionPools){
            poolNames.add(pool.getName());
        }
        return poolNames;
    }

    /**
     * get the pools for a particular resource-adapter
     * @param moduleName resource-adapter name
     * @return collection of connectorConnectionPool
     */
    private Collection<ConnectorConnectionPool> getAllPoolsOfModule(String moduleName) {
        List<ConnectorConnectionPool> connectorConnectionPools = new ArrayList<ConnectorConnectionPool>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool)resource;
                if(connectorConnectionPool.getResourceAdapterName().equals(moduleName)){
                    connectorConnectionPools.add(connectorConnectionPool);
                }
            }
        }
        return connectorConnectionPools;
    }

    /**
     * Notification that @Configured objects that were injected have changed
     *
     * @param events list of changes
     */
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
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
                    case ADD : logger.fine("A new " + changedType.getName() + " was added : " + changedInstance);
                        handleAddEvent(changedInstance);
                        break;

                    case CHANGE : logger.fine("A " + changedType.getName() + " was changed : " + changedInstance);
                        break;

                    case REMOVE : logger.fine("A " + changedType.getName() + " was removed : " + changedInstance);
                        handleRemoveEvent(changedInstance);
                        break;
                }
            }

            private <T extends ConfigBeanProxy> void handleAddEvent( T instance) {
                if(instance instanceof JdbcConnectionPool){
                    resourcesBinder.deployAllJdbcResourcesAndPools(new JdbcResource[]{},
                            new JdbcConnectionPool[]{((JdbcConnectionPool)instance)});
                }else if (instance instanceof JdbcResource){
                    JdbcResource resource = (JdbcResource)instance;
                    JdbcConnectionPool pool = getAssociatedJdbcConnectionPool(resource);
                    resourcesBinder.deployAllJdbcResourcesAndPools(new JdbcResource[]{resource},
                            new JdbcConnectionPool[]{pool});
                }else if (instance instanceof ConnectorConnectionPool){
                    resourcesBinder.deployAllConnectorResourcesAndPools(new ConnectorResource[]{},
                            new ConnectorConnectionPool[]{((ConnectorConnectionPool)instance)});
                }else if (instance instanceof ConnectorResource){
                    ConnectorResource resource = (ConnectorResource)instance;
                    ConnectorConnectionPool pool = getAssociatedConnectorConnectionPool(resource);
                    resourcesBinder.deployAllConnectorResourcesAndPools(new ConnectorResource[]{resource},
                            new ConnectorConnectionPool[]{pool});
                }
            }

            private <T extends ConfigBeanProxy> void handleRemoveEvent(final T instance) {
                ArrayList instancesToDestroy = new ArrayList();
                ArrayList dummy = new ArrayList();
                instancesToDestroy.add(instance);
                if(instance instanceof JdbcConnectionPool){
                    destroyResourcesAndPools(dummy, instancesToDestroy);
                }else if (instance instanceof JdbcResource){
                    destroyResourcesAndPools(instancesToDestroy, dummy);
                }else if (instance instanceof ConnectorConnectionPool){
                    destroyResourcesAndPools(dummy, instancesToDestroy);
                }else if (instance instanceof ConnectorResource){
                    destroyResourcesAndPools(instancesToDestroy, dummy);
                }
            }
        }, logger);
        return null;
    }

    /**
     * given a jdbc-resource, get associated jdbc-connection-pool
     * @param resource jdbc-resource
     * @return jdbc-connection-pool
     */
    private  JdbcConnectionPool getAssociatedJdbcConnectionPool(JdbcResource resource) {
        //TODO V3 need to find a generic way (instead of separate methods for jdbc/connector)
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof JdbcConnectionPool){
                JdbcConnectionPool pool = (JdbcConnectionPool)configuredResource;
                if(resource.getPoolName().equalsIgnoreCase(pool.getName())){
                    return pool;
                }
            }
        }
        return null;  //TODO V3 cannot happen ?
    }

    /**
     * given a connector-resource, get associated connector-connection-pool
     * @param resource connector-resource
     * @return connector-connection-pool
     */
    private  ConnectorConnectionPool getAssociatedConnectorConnectionPool(ConnectorResource resource) {
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool pool = (ConnectorConnectionPool)configuredResource;
                if(resource.getPoolName().equalsIgnoreCase(pool.getName())){
                    return pool;
                }
            }
        }
        return null;  //TODO V3 cannot happen ?
    }

    public JdbcConnectionPool getJdbcConnectionPoolConfig(String poolName){
        //TODO V3 need to find a generic way (instead of separate methods for jdbc/connector)
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof JdbcConnectionPool){
                JdbcConnectionPool pool = (JdbcConnectionPool)configuredResource;
                if(pool.getName().equalsIgnoreCase(poolName)){
                    return pool;
                }
            }
        }
        return null; //TODO V3 cannot happen ?
    }
}
