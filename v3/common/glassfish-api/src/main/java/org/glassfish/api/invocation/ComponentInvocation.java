package org.glassfish.api.invocation;

import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

@Scoped(PerLookup.class)
@Service
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

    // true if transaction commit or rollback is
    // happening for this invocation context
    private boolean transactionCompleting = false;

    //  security context coming in a call
    // security context changes on a runas call - on a run as call
    // the old logged in security context is stored in here.
    public Object oldSecurityContext;
    
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

    public Object getContainerContext() {
        return container;
    }

    public <T> T getTransaction() {
        return (T)transaction;
    }

    public <T> void setTransaction(T t) {
        this.transaction = t;
    }
    
    /** 
     * Sets the security context of the call coming in
     */
    public void setOldSecurityContext (Object sc){
	this.oldSecurityContext = sc;
    }
    /**
     * gets the security context of the call that came in
     * before a new context for runas is made
     */
    public Object getOldSecurityContext (){
	return oldSecurityContext;
    }

    public boolean isTransactionCompleting() {
        return transactionCompleting;
    }

    public void setTransactionCompeting(boolean value) {
        transactionCompleting = value;
    }
}
