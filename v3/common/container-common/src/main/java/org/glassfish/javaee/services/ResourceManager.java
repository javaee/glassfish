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
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.Startup;

import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.config.ObservableBean;



@Service(name="ResourceManager") // this name is used in ApplicationLoaderService
/**
 * Resource manager to bind various resources during startup, create/update/delete of resource/pool
 * @author Jagadish Ramu
 */
public class ResourceManager implements Startup, PostConstruct, PreDestroy, ConfigListener {

    private static final Logger logger = LogDomains.getLogger(ResourceManager.class,LogDomains.RESOURCE_BUNDLE);

    @Inject
    private ResourcesBinder resourcesBinder;

    @Inject
    private Habitat connectorRuntimeHabitat;

    @Inject
    private Resources allResources; // Needed so as to listen to config changes.

    @Inject
    private Habitat deployerHabitat;
    
    private ConnectorRuntime runtime;
    
    //Listen to config changes of resource refs.
    @Inject
    private ResourceRef[] resourceRefs;
    
    public void postConstruct() {
        deployResources(allResources.getResources());
        addListenerToResourceRefs();
    }

    /**
     * deploy resources
     * @param resources list
     */
    public void deployResources(Collection<Resource> resources){
        for(Resource resource : resources){
            if(resource instanceof BindableResource){
                BindableResource bindableResource = (BindableResource)resource;
                resourcesBinder.deployResource(bindableResource.getJndiName(), resource);
            } else if (resource instanceof ResourcePool) {
                //TODO V3 handle other resources later (con-pools)
            } else{
                // only other resource left is RAC
                try{
                    getResourceDeployer(resource).deployResource(resource);
                }catch(Exception e){
                    Object[] params = {ConnectorsUtil.getResourceName(resource), e};
                    logger.log(Level.WARNING, "resources.resource-manager.deploy-resource-failed", params);
                }
            }
        }
        //TODO V3 will there be a chance of double listener registrationf or a resource ?
        //TODO V3 eg: allresources added during startup, resources of a particular
        //TODO V3 connector during connector startup / redeploy ?
        addListenerToResources(resources);
    }

    public Resources getAllResources(){
        return allResources;
    }


    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {
        //TODO V3 : Admin need to make sure that poolnames are unique. As of V2 they are not unique.
        Collection<Resource> resources = ConnectorsUtil.getAllSystemRAResourcesAndPools(allResources);
        
        //TODO V3 : even in case when there is no resource used by an application (no RAR was started),
        //TODO V3 not undeploying resources other than jdbc, connector pool/resource ?
        undeployResources(resources);
        ConnectorRuntime cr = getConnectorRuntime();
        if (cr!=null) {
            cr.shutdownAllActiveResourceAdapters();
        }
        removeListenerFromResources();
        removeListenerFromResourceRefs();
    }


    private ConnectorRuntime getConnectorRuntime() {
        //TODO V3 not synchronized
        if(runtime == null){
            runtime = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class, null);
        }
        return runtime;
    }

    /**
     * undeploy the given set of resources<br>
     * <b>care has to be taken for the case of dependent resources<br>
     * eg : all resources need to be undeployed <br>
     * before undeploying the pool that they refer to</b>
     * @param resources list of resources
     */
    public void undeployResources(Collection<Resource> resources){
        for(Resource resource : resources){
            try{
                //TODO V3 no need to check bindableResource ? (similar to deployResources())
                //TODO V3 handle Resource-Adapter-Config ?
                getResourceDeployer(resource).undeployResource(resource);
            }catch(Exception e){
                //TODO V3 can't Resource (config bean) have name ?
                Object[] params = {ConnectorsUtil.getResourceName(resource), e};
                logger.log(Level.WARNING, "resources.resource-manager.undeploy-resource-failed", params);
            }finally{
                removeListenerFromResource(resource);
            }
        }
    }

    /**
     * Notification that @Configured objects that were injected have changed
     *
     * @param events list of changes
     */
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new PropertyChangeHandler(events), logger);
    }
    
    class PropertyChangeHandler implements Changed {
        
        PropertyChangeEvent[] events;

        private PropertyChangeHandler(PropertyChangeEvent[] events) {
            this.events = events;
        }

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
            //TODO V3 handle enabled / disabled / resource-ref / redeploy ?
            try {
                if (ConnectorsUtil.isValidEventType(instance)) {
                    getResourceDeployer(instance).redeployResource(instance);
                } else if (ConnectorsUtil.isValidEventType(instance.getParent())) {
                    //Added in case of a property change
                    //check for validity of the property's parent and redeploy
                    getResourceDeployer(instance.getParent()).redeployResource(instance.getParent());
                } else if (instance instanceof ResourceRef) {
                    ResourceRef ref = (ResourceRef) instance;
                    ResourceDeployer deployer = null;
                    String refName = ref.getRef();
                    BindableResource bindableResource = null;

                    for(PropertyChangeEvent event : events) {
                        String propertyName = event.getPropertyName();
                        //Depending on the type of event (disable/enable, invoke the 
                        //method on deployer.
                        if ("enabled".equalsIgnoreCase(propertyName)) {
                            boolean newValue = ConnectorsUtil.parseBoolean(event.getNewValue().toString());
                            boolean oldValue = ConnectorsUtil.parseBoolean(event.getOldValue().toString());
                            for (Resource resource : allResources.getResources()) {
                                if (resource instanceof BindableResource) {
                                    bindableResource = (BindableResource) resource;
                                    if (refName != null) {
                                        if (refName.equals(bindableResource.getJndiName())) {
                                            deployer = getResourceDeployer(bindableResource);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (deployer != null) {
                                //both cannot be true or false
                                if (!(newValue && oldValue)) {
                                    if (newValue) {
                                        deployer.enableResource(bindableResource);
                                    } else {
                                        deployer.disableResource(bindableResource);
                                    }
                                }
                            }
                        }
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

            if (instance instanceof BindableResource) {
                resourcesBinder.deployResource(((BindableResource) instance).getJndiName(), (Resource) instance);
            } else if (instance instanceof ResourcePool) {
                //TODO V3 handle - ccp, jdbc-cp
            } else if (instance instanceof Resource) {
                //only resource type left is RAC
                try {
                    getResourceDeployer(instance).deployResource(instance);
                } catch (Exception e) {
                    String resourceName = ConnectorsUtil.getResourceName((Resource) instance);
                    Object params[] = {resourceName, e};
                    logger.log(Level.WARNING, "resources.resource-manager.deploy-resource-failed", params);
                }
            } else if (instance instanceof Property) {
                //Property is not handled here. It is handled as part of the
                //Change event of a jdbc-connection-pool. 
            } else if (instance instanceof ResourceRef) {
                //TODO V3 : is this for asadmin create-resource-ref? 
            } else {
                //For any other type of instance, dont do anything.
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
                    //get appropriate deployer and unddeploy resource
                    getResourceDeployer(instance).undeployResource(instance);
                } else if (ConnectorsUtil.isValidEventType(instance.getParent())) {
                    //Added in case of a property remove
                    //check for validity of the property's parent and redeploy
                    getResourceDeployer(instance).redeployResource(instance.getParent());
                } else if (instance instanceof ResourceRef) {
                    //TODO V3: asadmin delete-resource-ref
                }
            } catch (Exception ex) {
                final String msg = ResourceManager.class.getName() + " : Error while handling remove Event";
                logger.severe(msg);
                np = new NotProcessed(msg);
            }
            return np;
        }
    }


    /**
     * Add listener to all resources (JDBC Connection Pool/JDBC Resource/
     * Connector Connection Pool/Connector Resource.
     * Invoked from postConstruct()
     * @param resources list of resources for which listeners will be registered.
     */
    private void addListenerToResources(Collection<Resource> resources) {
        for (Resource configuredResource : resources) {
            addListenerToResource(configuredResource);
        }
    }

    private void addListenerToResourceRefs() {
        for (ResourceRef ref : resourceRefs) {
            addListenerToResource(ref);
        }
    }
    
    /**
     * Add listener to a generic resource
     * Used in the case of create asadmin command when listeners have to
     * be added to the specific resource
     * @param instance instance to which listener will be registered
     */
    private void addListenerToResource(Object instance) {
        ObservableBean bean = null;

        //add listener to all types of Resource
        if(instance instanceof Resource){
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy)instance);
            bean.addListener(this);
        } else if(instance instanceof ResourceRef) {
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy)instance);
            bean.addListener(this);
        }
    }


    /**
     * Remove listener from a generic resource (JDBC Connection Pool/Connector 
     * Connection Pool/JDBC resource/Connector resource)
     * Used in the case of delete asadmin command
     * @param instance remove the resource from listening to resource events
     */
    private void removeListenerFromResource(Object instance) {
        ObservableBean bean = null;

        if(instance instanceof Resource){
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy)instance);
            bean.removeListener(this);
        } else if(instance instanceof ResourceRef) {
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy) instance);
            bean.removeListener(this);
        }
    }

    /**
     * Remove listener from all resource refs.
     * Invoked from preDestroy().
     */
    private void removeListenerFromResourceRefs() {
        for (ResourceRef ref : resourceRefs) {
            removeListenerFromResource(ref);
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

    /**
     * Given a <i>resource</i> instance, appropriate deployer will be provided
     *
     * @param resource resource instance
     * @return ResourceDeployer
     */
    private ResourceDeployer getResourceDeployer(Object resource){
        Collection<ResourceDeployer> deployers = deployerHabitat.getAllByContract(ResourceDeployer.class);

        for(ResourceDeployer deployer : deployers){
            if(deployer.handles(resource)){
                return deployer;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }
}
