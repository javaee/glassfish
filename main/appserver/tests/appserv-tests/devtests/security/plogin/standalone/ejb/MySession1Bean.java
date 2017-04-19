import javax.ejb.*;

/**
 * This is the bean class for the MySession1Bean enterprise bean.
 */
public class MySession1Bean implements SessionBean, MySession1RemoteBusiness {
    private SessionContext context;
    
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    // </editor-fold>
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO implement ejbCreate if necessary, acquire resources
        // This method has access to the JNDI context so resource aquisition
        // spanning all methods can be performed here such as home interfaces
        // and data sources.
    }
    
    
    
    public String businessMethod(String name) {
        return "hello " + name;
    }

    public String businessMethod2(String name) {
        return "hey " + name;
    }

    public String businessMethod3(String name) {
        return "howdy "+name;
    }
    
    
    
}
