/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.osgi.ee.resources;

import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;
import org.osgi.framework.BundleContext;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service to export resources in GlassFish to OSGi's service-registry.<br>
 * OSGi applications can use <i>ServiceReference</i> to get access to these resources.
 * OSGi applications can do lookup of appropriate type of<br>
 * <i>ServiceReference</i> with the filter <i>"jndi-name"</i> <br><br>
 * For JDBC Resources, additional filter <i>"osgi.jdbc.driver.class"</i> that indicates the<br>
 * driver-class-name/datasource-class-name will work.
 * <p/>
 * JDBC Resources, JMS Connection Factories, JMS Destinations are exported with following <i>ServiceReference</i> names<br>
 * For JDBC Resources : <i>javax.sql.DataSource</i>  <br>
 * For JMS Resources : <i>javax.jms.ConnectionFactory / javax.jms.QueueConnectionFactory / javax.jms.TopicConnectionFactory</i> <br>
 * For JMS Destinations : <i>javax.jms.Queue / javax.jms.Topic</i> <br>
 *
 * @author Jagadish Ramu
 */
public class ResourceProviderService implements ConfigListener {

    private Habitat habitat;

    private Resources resources;
    private Servers servers;

    //config-bean proxy objects so as to listen to changes to these configuration.
    private ObservableBean serverConfigBean;
    private ObservableBean resourcesConfigBean;

    private BundleContext bundleContext;
    private ResourceHelper resourceHelper;

    private Collection<ResourceManager> resourceManagers;

    private static final Logger logger = Logger.getLogger(
            ResourceProviderService.class.getPackage().getName());

    public ResourceProviderService(Habitat habitat, BundleContext bundleContext) {
        this.habitat = habitat;
        this.bundleContext = bundleContext;
        servers = habitat.getComponent(Servers.class);
        resources = habitat.getComponent(Domain.class).getResources();
        resourceHelper = new ResourceHelper(habitat);
        resourceManagers = new ArrayList<ResourceManager>();
        initializeResourceManagers();
        postConstruct();
    }

    private void initializeResourceManagers() {
        Habitat habitat = getHabitat();
        resourceManagers.add(new JDBCResourceManager(habitat));
        if(runtimeSupportsJMS()){
            registerJMSResources(resourceManagers, habitat);
        }
    }

    private Habitat getHabitat() {
        return habitat;
    }

    public void registerResources() {
        Collection<ResourceManager> resourceManagers = getAllResourceManagers();
        for (ResourceManager rm : resourceManagers) {
            rm.registerResources(bundleContext);
        }
    }

    public void unRegisterResources() {
        Collection<ResourceManager> resourceManagers = getAllResourceManagers();
        for (ResourceManager rm : resourceManagers) {
            rm.unRegisterResources(bundleContext);
        }
        preDestroy();
    }

    /**
     * un-register config bean proxy change listeners
     */
    public void preDestroy() {
        if (serverConfigBean != null) {
            serverConfigBean.removeListener(this);
        }
        if (resourcesConfigBean != null) {
            resourcesConfigBean.removeListener(this);
        }
    }

    /**
     * register config bean proxy change listeners
     */
    public void postConstruct() {
        List<Server> serversList = servers.getServer();

        ServerContext context = getHabitat().getComponent(ServerContext.class);
        String instanceName = context.getInstanceName();

        for (Server server : serversList) {
            if (server.getName().equals(instanceName)) {
                serverConfigBean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy) server);
                serverConfigBean.addListener(this);
            }
        }

        resourcesConfigBean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy) resources);
        resourcesConfigBean.addListener(this);

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

        public <T extends ConfigBeanProxy> NotProcessed changed(Changed.TYPE type, Class<T> changedType, T changedInstance) {

            NotProcessed np = null;
            try {
                switch (type) {
                    case ADD:
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("A new " + changedType.getName() + " was added : " + changedInstance);
                        }
                        np = handleAddEvent(changedInstance);
                        break;

                    case CHANGE:
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("A " + changedType.getName() + " was changed : " + changedInstance);
                        }
                        np = handleChangeEvent(changedInstance);
                        break;

                    case REMOVE:
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("A " + changedType.getName() + " was removed : " + changedInstance);
                        }
                        np = handleRemoveEvent(changedInstance);
                        break;

                    default:
                        np = new NotProcessed("Unrecognized type of change: " + type);
                        break;
                }
                return np;
            } finally {
            }

        }

        private <T extends ConfigBeanProxy> NotProcessed handleRemoveEvent(T removedInstance) {
            if (removedInstance instanceof ResourceRef) {
                ResourceRef resourceRef = (ResourceRef) removedInstance;
                String resourceName = resourceRef.getRef();
                BindableResource resource = (BindableResource)
                        resources.getResourceByName(BindableResource.class, resourceName);
                unRegisterResource(resource);
            } else if (removedInstance instanceof BindableResource) {
                //since delete resource-ref event will not work (resource related configuration
                //information won't be available during resource-ref deletion event), handling
                //un-register of service here also.
                unRegisterResource((BindableResource) removedInstance);
            }
            return null;
        }

        private <T extends ConfigBeanProxy> NotProcessed handleChangeEvent(T changedInstance) {
            //TODO Handle other attribute changes (jndi-name)

            if (changedInstance instanceof ResourceRef) {
                ResourceRef resourceRef = (ResourceRef) changedInstance;
                String refName = resourceRef.getRef();

                for (PropertyChangeEvent event : events) {
                    String propertyName = event.getPropertyName();
                    if ("enabled".equalsIgnoreCase(propertyName)) {
                        boolean newValue = Boolean.parseBoolean(event.getNewValue().toString());
                        boolean oldValue = Boolean.parseBoolean(event.getOldValue().toString());
                        //make sure that there is state change
                        if (!(newValue && oldValue)) {
                            BindableResource bindableResource =
                                    (BindableResource) resources.getResourceByName(BindableResource.class, refName);
                            if (newValue) {
                                registerResource(bindableResource, resourceRef);
                            } else {
                                unRegisterResource(bindableResource);
                            }
                        }
                    }
                }
            } else if (changedInstance instanceof BindableResource) {
                BindableResource bindableResource = (BindableResource) changedInstance;
                for (PropertyChangeEvent event : events) {
                    String propertyName = event.getPropertyName();
                    if ("enabled".equalsIgnoreCase(propertyName)) {
                        boolean newValue = Boolean.parseBoolean(event.getNewValue().toString());
                        boolean oldValue = Boolean.parseBoolean(event.getOldValue().toString());
                        //make sure that there is state change
                        if (!(newValue && oldValue)) {
                            if (newValue) {
                                registerResource(bindableResource);
                            } else {
                                unRegisterResource(bindableResource);
                            }
                        }
                    }
                }
            }
            return null;
        }

        private void unRegisterResource(BindableResource bindableResource) {
            Collection<ResourceManager> resourceManagers = getResourceManagers(bindableResource);
            for (ResourceManager rm : resourceManagers) {
                ResourceRef ref = getResourceHelper().getResourceRef(bindableResource.getJndiName());
                rm.unRegisterResource(bindableResource, ref, bundleContext);
            }
        }

        private void registerResource(BindableResource bindableResource, ResourceRef ref) {
            Collection<ResourceManager> resourceManagers = getResourceManagers(bindableResource);
            for (ResourceManager rm : resourceManagers) {
                rm.registerResource(bindableResource, ref, bundleContext);
            }
        }

        private void registerResource(BindableResource bindableResource) {
            ResourceRef ref = getResourceHelper().getResourceRef(bindableResource.getJndiName());
            registerResource(bindableResource, ref);
        }

        private <T extends ConfigBeanProxy> NotProcessed handleAddEvent(T addedInstance) {
            if (addedInstance instanceof ResourceRef) {
                ResourceRef resourceRef = (ResourceRef) addedInstance;
                String resourceName = resourceRef.getRef();
                BindableResource resource = (BindableResource)
                        resources.getResourceByName(BindableResource.class, resourceName);
                if (resource != null) {
                    registerResource(resource, resourceRef);
                }
            }
            return null;
        }
    }

    /**
     * get the list of resource-managers that can handle the resource
     * @param resource resource
     * @return list of resource-managers
     */
    private Collection<ResourceManager> getResourceManagers(BindableResource resource) {
        Collection<ResourceManager> resourceManagers = new ArrayList<ResourceManager>();
        for (ResourceManager rm : getAllResourceManagers()) {
            if (rm.handlesResource(resource)) {
                resourceManagers.add(rm);
            }
        }
        return resourceManagers;
    }

    /**
     * get the list of all resource-managers in the system
     * @return list of resource-managers
     */
    private Collection<ResourceManager> getAllResourceManagers() {
        //resourceManagers = getHabitat().getAllByContract(ResourceManager.class);
        return resourceManagers;
    }

    private boolean runtimeSupportsJMS() {
        boolean supports = false;
        try{
            Class.forName("javax.jms.QueueConnectionFactory");
            supports = true;
        }catch(Throwable e){
            logger.finest("Exception while loading JMS API " + e);
        }
        return supports;
    }

    private void registerJMSResources(Collection<ResourceManager> resourceManagers, Habitat habitat) {
        resourceManagers.add(new JMSResourceManager(habitat));
        resourceManagers.add(new JMSDestinationResourceManager(habitat));
    }

    private ResourceHelper getResourceHelper() {
        return resourceHelper;
        //return habitat.getComponent(ResourceHelper.class);
    }


    private void debug(String s) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[osgi-ee-resources] : " + s);
        }
    }
}
