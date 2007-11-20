package org.glassfish.api.invocation;

public class ComponentInvocation {

    public enum ComponentInvocationType {
        SERVLET_INVOCATION, EJB_INVOCATION,
        APP_CLIENT_INVOCATION, UN_INITIALIZED,
        SERVICE_STARTUP
    }

    private ComponentInvocationType invocationType
            = ComponentInvocationType.UN_INITIALIZED;


    // the component instance, type Servlet, Filter or EnterpriseBean
    private Object instance;

    // ServletContext for servlet, Container for EJB
    private Object container;

    private String componentId;

    private Object transaction;

    public ComponentInvocation(ComponentInvocationType invocationType,
                               Object instance, Object container,
                               String componentId) {
        this.invocationType = invocationType;
        this.instance = instance;
        this.container = container;
        this.componentId = componentId;
    }

    public ComponentInvocationType getInvocationType() {
        return invocationType;
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

    public <T> T getTransaction() {
        return (T) transaction;
    }

    public <T> void setTransaction(T t) {
        this.transaction = t;
    }

}
