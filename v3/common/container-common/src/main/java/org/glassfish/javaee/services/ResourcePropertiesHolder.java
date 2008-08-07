/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.javaee.services;

import com.sun.enterprise.config.serverbeans.Resource;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.naming.NamingObjectsProvider;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.Property;

/**
 * ResourcePropertiesHolder to bind the resources with its properties to the 
 * ConfigListener
 * @author Shalini M
 */
@Service
public class ResourcePropertiesHolder implements NamingObjectsProvider,
        ConfigListener {

    @Inject
    private Logger logger;
    @Inject
    private Habitat connectorRuntimeHabitat;
    protected Resource resource;
    protected ObservableBean propertyBean;
    private ConnectorRuntime runtime;

    /**
     * Get the value of resource
     *
     * @return the value of resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Set the value of resource
     *
     * @param resource new value of resource
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Get the value of propertyBean
     *
     * @return the value of propertyBean
     */
    public ObservableBean getPropertyBean() {
        return propertyBean;
    }

    /**
     * Set the value of propertyBean
     *
     * @param propertyBean new value of propertyBean
     */
    public void setPropertyBean(ObservableBean propertyBean) {
        this.propertyBean = propertyBean;
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {

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
                switch (type) {
                    case ADD: //Does not happen here. When a new property is added, 
                        //RM.ADD is got for Property ADD and RM.CHANGE is got
                        //for Resource change event. No need to handle here.
                        logger.fine("A new " + changedType.getName() + " was added : " + changedInstance);
                        break;

                    case CHANGE:
                        logger.fine("A " + changedType.getName() + " was changed : " + changedInstance);
                        handleChangeEvent(changedInstance);
                        break;

                    case REMOVE:
                        logger.fine("A " + changedType.getName() + " was removed : " + changedInstance);
                        handleRemoveEvent(changedInstance);
                        break;
                }
            }

            /**
             * Handle the change event of a property of a resource. 
             * Redeploys the pool using the resource object stored in 
             * ResourcePropertiesHolder.
             */
            private <T extends ConfigBeanProxy> void handleChangeEvent(T instance) {
                try {
                    Resource resource = ResourcePropertiesHolder.this.resource;
                    //Redeploy resource
                    if (resource instanceof JdbcConnectionPool) {
                        JdbcConnectionPool pool = (JdbcConnectionPool) resource;
                        getConnectorRuntime().redeployResource(pool);
                    } else if (resource instanceof ConnectorConnectionPool) {
                        ConnectorConnectionPool pool = (ConnectorConnectionPool) resource;
                        getConnectorRuntime().redeployResource(pool);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ResourcePropertiesHolder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //when is this called? from GUI???
            private <T extends ConfigBeanProxy> void handleRemoveEvent(final T instance) {
                try {
                    //Remove listener from this property
                    if(instance instanceof Property) {
                        Property prop = (Property) instance;
                        ObservableBean propertyBean = (ObservableBean) ConfigSupport.getImpl(prop);
                        ResourcePropertiesHolder.this.setPropertyBean(propertyBean);
                        ResourcePropertiesHolder.this.removeListener();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ResourcePropertiesHolder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, logger);
        return null;
    }

    /**
     * Add listener to the property bean
     */
    void addListener() {
        this.propertyBean.addListener(this);
    }

    /**
     * Remove listener from the property bean
     */
    void removeListener() {
        this.propertyBean.removeListener(this);
    }

    private ConnectorRuntime getConnectorRuntime() {
        //TODO V3 not synchronized
        if (runtime == null) {
            runtime = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class, null);
        }
        return runtime;
    }
}
