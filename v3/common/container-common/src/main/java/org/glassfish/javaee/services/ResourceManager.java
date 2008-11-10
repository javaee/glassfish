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
import org.glassfish.api.admin.config.Property;

import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import org.jvnet.hk2.config.ObservableBean;



@Service
/**
 * Resource manager to bind various resources during startup, create/update/delete of resource/pool
 * @author Jagadish Ramu
 */
public class ResourceManager implements NamingObjectsProvider, PostConstruct, PreDestroy, ConfigListener {


    @Inject
    private Logger logger;

    @Inject
    private ResourceAdaptersBinder resourcesBinder;

    @Inject
    private Habitat connectorRuntimeHabitat;

    @Inject
    private Resources allResources; // Needed so as to listen to config changes.

    @Inject
    private Habitat deployerHabitat;
    
    private ConnectorRuntime runtime;
    
    public void postConstruct() {
        bindResources(allResources);
        addListenerToResources();
    }

    public void bindResources(Resources allResources){
        for(Resource resource : allResources.getResources()){
            if(resource instanceof BindableResource){
                BindableResource bindableResource = (BindableResource)resource;
                resourcesBinder.deployResource(bindableResource.getJndiName(), resource);
            }else{
                //TODO V3 handle other resources (ra-config) later
            }
        }
    }


    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {

        //TODO V3 : Admin need to make sure that poolnames are unique. As of V2 they are not unique.
        Collection resources = ConnectorsUtil.getAllSystemRAResourcesAndPools(allResources);
        
        //TODO V3 : even in case when there is no resource used by an application (no RAR was started),
        //TODO V3 this seems to be called ??
        getConnectorRuntime().shutdownAllActiveResourceAdapters(resources);
        removeListenerFromResources();
    }

    private ConnectorRuntime getConnectorRuntime() {
        //TODO V3 not synchronized
        if(runtime == null){
            runtime = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class, null);
        }
        return runtime;
    }

    /**
     * deploys the resources (pool/resources) of a particular resource-adapter
     * @param moduleName resource-adapter name
     */
    public void deployResourcesForModule(String moduleName){
        Collection<ConnectorConnectionPool> connectionPools = ConnectorsUtil.getAllPoolsOfModule(moduleName, allResources);
        Collection<String> poolNames = ConnectorsUtil.getAllPoolNames(connectionPools);
        Collection<ConnectorResource> connectorResources = ConnectorsUtil.getAllResources(poolNames, allResources);
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
        Collection<ConnectorConnectionPool> connectionPools = ConnectorsUtil.getAllPoolsOfModule(moduleName, allResources);
        Collection<String> poolNames = ConnectorsUtil.getAllPoolNames(connectionPools);
        Collection<ConnectorResource> connectorResources = ConnectorsUtil.getAllResources(poolNames, allResources);
        List resources = new ArrayList();
        resources.addAll(connectionPools);
        resources.addAll(connectorResources);
        destroyResourcesAndPools(resources);
    }

    /**
     * destroys a list of resource and pools (jdbc/connector)
     * @param Collection of resources and pools to be destroyed
     */
    private void destroyResourcesAndPools(Collection resources) {
        getConnectorRuntime().destroyResourcesAndPools(resources);
    }

    /**
     * Notification that @Configured objects that were injected have changed
     *
     * @param events list of changes
     */
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        // I am not so interested with the list of events, just sort who got added or removed for me.
        final UnprocessedChangeEvents unprocessed = ConfigSupport.sortAndDispatch(events, new Changed() {
            /**
             * Notification of a change on a configuration object
             *
             * @param type            type of change : ADD mean the changedInstance was added to the parent
             *                        REMOVE means the changedInstance was removed from the parent, CHANGE means the
             *                        changedInstance has mutated.
             * @param changedType     type of the configuration object
             * @param changedInstance changed instance.
             */
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
                NotProcessed np = null;
                switch (type) {
                    case ADD:
                        logger.fine("A new " + changedType.getName() + " was added : " + changedInstance);
                        np = handleAddEvent(changedInstance);
                        break;

                    case CHANGE:
                        logger.fine("A " + changedType.getName() + " was changed : " + changedInstance);
                        np = handleChangeEvent(changedInstance);
                        break;

                    case REMOVE:
                        logger.fine("A " + changedType.getName() + " was removed : " + changedInstance);
                        np = handleRemoveEvent(changedInstance);
                        break;

                    default:
                        np = new NotProcessed("Unrecognized type of change: " + type);
                        break;
                }
                return np;
            }

            private <T extends ConfigBeanProxy> NotProcessed handleChangeEvent(T instance) {
                NotProcessed np = null;
                try {
                    if (ConnectorsUtil.isValidEventType(instance)) {
                        String resourceType = ConnectorsUtil.getResourceType((Resource)instance);
                        ResourceDeployer deployer = deployerHabitat.getComponent(ResourceDeployer.class, resourceType);
                        if(deployer != null){
                            deployer.redeployResource(instance);
                        }else{
                            logger.log(Level.WARNING,"no deployer found for resource type [ "+ resourceType + "]");
                            //TODO V3 log throw Exception
                        }
                    } else if (ConnectorsUtil.isValidEventType(instance.getParent())) {
                        //Added in case of a property change
                        //check for validity of the property's parent and redeploy
                        String resourceType = ConnectorsUtil.getResourceType((Resource)instance);
                        ResourceDeployer deployer = deployerHabitat.getComponent(ResourceDeployer.class, resourceType);
                        if(deployer != null){
                            deployer.redeployResource(instance.getParent());
                        }else{
                            System.out.println("no deployer found for resource type [ "+ resourceType + "]");
                            //TODO V3 log throw Exception
                        }
                    }
                } catch (Exception ex) {
                    final String msg = ResourceManager.class.getName() + " : Error while handling change Event";
                    logger.severe(msg);
                    np = new NotProcessed(msg);
                }
                return np;
            }

            private <T extends ConfigBeanProxy> NotProcessed handleAddEvent(T instance) {
                NotProcessed np = null;
                //Add listener to the changed instance object
                ResourceManager.this.addListenerToResource(instance);

                if(instance instanceof BindableResource){
                    resourcesBinder.deployResource(((BindableResource)instance).getJndiName(), (Resource)instance);
                }else if(instance instanceof ConnectorConnectionPool || instance instanceof JdbcConnectionPool) {
                    //TODO V3 handle - ccp, jdbc-cp, ra-config
                } else if (instance instanceof Property) {
                    final Property prop = (Property) instance;
                    np = new NotProcessed("ResourceManager: a property was added: " + prop.getName() + "=" + prop.getValue());
                } else {
                    np = new NotProcessed("ResourceManager: configuration " +
                            Dom.unwrap(instance).getProxyType().getName() + " was added");
                }
                return np;
            }

            private <T extends ConfigBeanProxy> NotProcessed handleRemoveEvent(final T instance) {
                ArrayList instancesToDestroy = new ArrayList();
                NotProcessed np = null;
                try {
                    //this check ensures that a valid resource is handled
                    if (ConnectorsUtil.isValidEventType(instance)) {
                        instancesToDestroy.add(instance);
                        //Remove listener from the removed instance
                        ResourceManager.this.removeListenerFromResource(instance);

                        //get appropriate deployer and deploy resource
                        String resourceType = ConnectorsUtil.getResourceType((Resource)instance);
                        ResourceDeployer deployer = deployerHabitat.getComponent(ResourceDeployer.class, resourceType);
                        if(deployer != null){
                            deployer.deployResource(instance);
                        }else{
                            System.out.println("no deployer found for resource type [ "+ resourceType + "]");
                            //TODO V3 log throw Exception
                        }

                    } else if (ConnectorsUtil.isValidEventType(instance.getParent())) {
                        //Added in case of a property remove
                        //check for validity of the property's parent and redeploy
                        String resourceType = ConnectorsUtil.getResourceType((Resource)instance);
                        ResourceDeployer deployer = deployerHabitat.getComponent(ResourceDeployer.class, resourceType);
                        if(deployer != null){
                            deployer.redeployResource(instance.getParent());
                        }else{
                            System.out.println("no deployer found for resource type [ "+ resourceType + "]");
                            //TODO V3 log throw Exception
                        }
                    }
                } catch (Exception ex) {
                    final String msg = ResourceManager.class.getName() + " : Error while handling remove Event";
                    logger.severe(msg);
                    np = new NotProcessed(msg);
                }
                return np;
            }
        }, logger);
        return unprocessed;
    }


    /**
     * Add listener to all resources (JDBC Connection Pool/JDBC Resource/
     * Connector Connection Pool/Connector Resource.
     * Invoked from postConstruct()
     */
    private void addListenerToResources() {
        for (Resource configuredResource : allResources.getResources()) {
            addListenerToResource(configuredResource);
        }
    }

    /**
     * Add listener to a generic resource (JDBC Connection Pool/Connector 
     * Connection Pool/JDBC resource/Connector resource)
     * Used in the case of create asadmin command when listeners have to 
     * be added to the specific pool/resource
     * @param instance
     */
    private void addListenerToResource(Object instance) {
        ObservableBean bean = null;

        //add listener to all types of Resource
        if(instance instanceof Resource){
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy)instance);
            bean.addListener(this);
        }
    }


    /**
     * Remove listener from a generic resource (JDBC Connection Pool/Connector 
     * Connection Pool/JDBC resource/Connector resource)
     * Used in the case of delete asadmin command
     * @param instance
     */
    private void removeListenerFromResource(Object instance) {
        ObservableBean bean = null;

        if(instance instanceof Resource){
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy)instance);
            bean.removeListener(this);
        }
    }

    /**
     * Remove listener from all resources - JDBC Connection Pools/JDBC Resources/
     * Connector Connection Pools/ Connector Resources.
     * Invoked from preDestroy()
     */
    private void removeListenerFromResources() {
        for (Resource configuredResource : allResources.getResources()) {
            removeListenerFromResource(configuredResource);
        }
    }
}
