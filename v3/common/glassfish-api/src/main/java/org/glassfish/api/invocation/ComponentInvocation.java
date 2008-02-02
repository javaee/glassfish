package org.glassfish.api.invocation;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

@Scoped(PerLookup.class)
@Service
@Contract
public class ComponentInvocation {

    public enum ComponentInvocationType {
        SERVLET_INVOCATION, EJB_INVOCATION,
        APP_CLIENT_INVOCATION, UN_INITIALIZED,
        SERVICE_STARTUP
    }

    private ComponentInvocationType invocationType
            = ComponentInvocationType.UN_INITIALIZED;


    // the component instance, type Servlet, Filter or EnterpriseBean
    public Object instance;

    // ServletContext for servlet, Container for EJB
    public Object container;

    public Object jndiEnvironment;


    public String componentId;

    public Object transaction;

    public ComponentInvocation() {
        
    }

    public ComponentInvocation(String componentId,
            ComponentInvocationType invocationType,
            Object container) {
        this.componentId = componentId;
        this.invocationType = invocationType;
        this.container = container;
    }


    public ComponentInvocation(String componentId,
            ComponentInvocationType invocationType,
            Object instance, Object container,
            Object transaction) {
        this.componentId = componentId;
        this.invocationType = invocationType;
        this.instance = instance;
        this.container = container;
        this.transaction = transaction;
    }

    public ComponentInvocationType getInvocationType() {
        return invocationType;
    }

    public void setComponentInvocationType(ComponentInvocationType t) {
        this.invocationType = t;
    }

    public Object getInstance() {
        return instance;
    }

    public String getComponentId() {
        return this.componentId;
    }

    public Object getContainer() {
        return container;
    }

    public Object getTransaction() {
        return transaction;
    }

    public void setTransaction(Object t) {
        this.transaction = t;
    }

}
