/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.osgijdbc;

import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service to export jdbc resources in GlassFish to OSGi's service-registy.<br>
 * OSGi applications can do lookup of <code>javax.sql.DataSource</code> type of
 * service with any of the filters "osgi.jdbc.driver.class" or "jndi-name"<br>
 *
 * @author Jagadish Ramu 
 */
@Service
@Scoped(Singleton.class)
public class GlassFishResourceProviderService implements ConfigListener {

    private Habitat habitat ;

    private Resources resources;
    private Servers servers;

    //config-bean proxy objects so as to listen to changes to these configuration.
    private ObservableBean serverConfigBean ;
    private ObservableBean resourcesConfigBean ;

    private BundleContext bundleContext;

    private List<ServiceRegistration> services = new ArrayList<ServiceRegistration>();

    private static final Logger logger = Logger.getLogger(
            GlassFishResourceProviderService.class.getPackage().getName());

    public GlassFishResourceProviderService(Habitat habitat, BundleContext bundleContext){
        this.habitat = habitat;
        this.bundleContext = bundleContext;
        servers = habitat.getComponent(Servers.class);
        resources = habitat.getComponent(Resources.class);
        postConstruct();
    }

    private Habitat getHabitat(){
        return habitat;
    }

    /**
     * Iterates through all of the configured jdbc-resources <br>
     * Exposes them as OSGi service by contract "javax.sql.DataSource"
     */
    public void registerJdbcResources() {
        Resources resources = getHabitat().getComponent(Resources.class);
        Collection<JdbcResource> jdbcResources = resources.getResources(JdbcResource.class);
        for(JdbcResource resource : jdbcResources){
            ResourceRef resRef = getResourceRef(resource.getJndiName());
            registerJdbcResource(resource, resRef);
        }
    }

    /**
     * Retrieves driver-class-name information so as to register
     * the service with parameter <i>osgi.jdbc.driver.class</i><br>
     * @param resource jdbc-resource
     * @param resRef resource-ref
     */

    private void registerJdbcResource(JdbcResource resource, ResourceRef resRef) {
        if(resource.getEnabled().equalsIgnoreCase("true")){
            if(resRef != null && resRef.getEnabled().equalsIgnoreCase("true")){
                String poolName = resource.getPoolName();
                JdbcConnectionPool pool = (JdbcConnectionPool)resources.getResourceByName(JdbcConnectionPool.class,
                        poolName);
                String className;
                if(pool.getResType().equalsIgnoreCase(Constants.DRIVER)){
                    className = pool.getDriverClassname();
                }else{
                    className = pool.getDatasourceClassname();
                }
                DataSourceProxy proxy = new DataSourceProxy(resource.getJndiName());
                Dictionary properties = new Hashtable();
                properties.put(DataSourceFactory.JDBC_DRIVER_CLASS, className);
                properties.put(Constants.JNDI_NAME, resource.getJndiName());
                ServiceRegistration service = bundleContext.registerService(javax.sql.DataSource.class.getName(),
                        proxy, properties);
                debug("registering resource ["+resource.getJndiName()+"]");
                services.add(service);
            }
        }
    }

    /**
     * un-register the resource from OSGi service registry
     */
    public void unRegisterJdbcResources(){
        for(ServiceRegistration serviceRegistration : services){
            unRegisterJdbcResource(serviceRegistration);
        }
        preDestroy();
    }

    /**
     * un-register the jdbc-resource, invalidates proxy data-source object.
     * @param serviceRegistration ServiceRegistration
     */
    private void unRegisterJdbcResource(ServiceRegistration serviceRegistration) {
        debug("unregistering resource ["+serviceRegistration.getReference().getProperty(Constants.JNDI_NAME)+"]");
        DataSourceProxy dsProxy = (DataSourceProxy)serviceRegistration.getReference().getBundle().
                getBundleContext().getService(serviceRegistration.getReference());
        dsProxy.invalidate();
        serviceRegistration.unregister();
    }

    /**
     * retrieves <i>resource-ref</i> from configuration
     * @param resourceName jdbc-resource-name
     * @return resource-ref
     */
    private ResourceRef getResourceRef(String resourceName) {
        ServerContext context = getHabitat().getComponent(ServerContext.class);
        String instanceName = context.getInstanceName();

        Servers servers = getHabitat().getComponent(Servers.class);
        for(Server server : servers.getServer()){
            if(server.getName().equals(instanceName)){
                List<ResourceRef> resourceRefs = server.getResourceRef();
                for(ResourceRef resourceRef : resourceRefs){
                    if(resourceRef.getRef().equals(resourceName)){
                        return resourceRef;
                    }
                }
            }
        }
        return null;
    }

    private void debug(String s) {
        if(logger.isLoggable(Level.FINEST)){
            logger.finest("[osgi-jdbc] : " + s);
        }
    }

    /**
     * un-register config bean proxy change listeners
     */
    public void preDestroy() {
        if(serverConfigBean != null){
            serverConfigBean.removeListener(this);            
        }
        if(resourcesConfigBean != null){
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

        for(Server server : serversList){
            if(server.getName().equals(instanceName)){
                serverConfigBean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy)server);
                serverConfigBean.addListener(this);
            }
        }

        resourcesConfigBean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy)resources);
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

        public <T extends ConfigBeanProxy> NotProcessed changed(Changed.TYPE type, Class<T> changedType, T changedInstance){

            NotProcessed np = null;
            try {
                switch (type) {
                    case ADD:
                        if(logger.isLoggable(Level.FINEST)){
                            logger.finest("A new " + changedType.getName() + " was added : " + changedInstance);
                        }
                        np = handleAddEvent(changedInstance);
                        break;

                    case CHANGE:
                        if(logger.isLoggable(Level.FINEST)){
                            logger.finest("A " + changedType.getName() + " was changed : " + changedInstance);
                        }
                        np = handleChangeEvent(changedInstance);
                        break;

                    case REMOVE:
                        if(logger.isLoggable(Level.FINEST)){
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
            if(removedInstance instanceof ResourceRef){
                ResourceRef resourceRef = (ResourceRef)removedInstance;
                String resourceName = resourceRef.getRef();
                BindableResource resource = (BindableResource)
                        resources.getResourceByName(BindableResource.class, resourceName);
                if(resource instanceof JdbcResource){
                    unRegisterJdbcResourceService(resourceName);
                }
            }else if(removedInstance instanceof JdbcResource){
                //since delete resource-ref event will not work (jdbc-resource related configuration
                //information won't be available during resource-ref deletion event), handling
                //un-register of service here also.
                JdbcResource resource = (JdbcResource)removedInstance;
                unRegisterJdbcResourceService(resource.getJndiName());
            }
            return null;
        }

        private void unRegisterJdbcResourceService(String resourceName) {
            ServiceRegistration toRemove = null;
            for(ServiceRegistration serviceRegistration : services){
                if(serviceRegistration.getReference().getProperty(Constants.JNDI_NAME).equals(resourceName)){
                    unRegisterJdbcResource(serviceRegistration);
                    toRemove = serviceRegistration;
                    break;
                }
            }
            if(toRemove != null){
                services.remove(toRemove);
            }
        }

        private <T extends ConfigBeanProxy> NotProcessed handleChangeEvent(T changedInstance) {
            //TODO Handle other attribute changes (jndi-name)
            
            if(changedInstance instanceof ResourceRef){
                ResourceRef resourceRef = (ResourceRef)changedInstance;
                    String refName = resourceRef.getRef();

                    for(PropertyChangeEvent event : events) {
                        String propertyName = event.getPropertyName();
                        if ("enabled".equalsIgnoreCase(propertyName)) {
                            boolean newValue = Boolean.parseBoolean(event.getNewValue().toString());
                            boolean oldValue = Boolean.parseBoolean(event.getOldValue().toString());
                            //make sure that there is state change
                            if (!(newValue && oldValue)) {
                                if (newValue) {
                                    JdbcResource jdbcResource =
                                            (JdbcResource) resources.getResourceByName(
                                                    BindableResource.class, refName );
                                    registerJdbcResource(jdbcResource, resourceRef);
                                } else {
                                    unRegisterJdbcResourceService(resourceRef.getRef());
                                }
                            }
                        }
                    }
            }else if(changedInstance instanceof JdbcResource){
                JdbcResource jdbcResource = (JdbcResource)changedInstance;
                String jndiName = jdbcResource.getJndiName();
                for(PropertyChangeEvent event : events) {
                    String propertyName = event.getPropertyName();
                    if ("enabled".equalsIgnoreCase(propertyName)) {
                        boolean newValue = Boolean.parseBoolean(event.getNewValue().toString());
                        boolean oldValue = Boolean.parseBoolean(event.getOldValue().toString());
                        //make sure that there is state change
                        if (!(newValue && oldValue)) {
                            if (newValue) {
                                registerJdbcResource(jdbcResource, getResourceRef(jndiName));
                            } else {
                                unRegisterJdbcResourceService(jndiName);
                            }
                        }
                    }
                }
            }
            return null;
        }

        private <T extends ConfigBeanProxy> NotProcessed handleAddEvent(T addedInstance) {
            if(addedInstance instanceof ResourceRef){
                ResourceRef resourceRef = (ResourceRef)addedInstance;
                String resourceName = resourceRef.getRef();
                BindableResource resource = (BindableResource)
                        resources.getResourceByName(BindableResource.class, resourceName);
                if(resource != null && resource instanceof JdbcResource){
                    registerJdbcResource((JdbcResource)resource, resourceRef);
                }
            }
            return null;
        }
    }
}
