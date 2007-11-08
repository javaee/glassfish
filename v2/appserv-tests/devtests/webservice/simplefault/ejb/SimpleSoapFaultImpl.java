/*
 * SimpleServerImpl.java
 *
 * Created on September 13, 2004, 11:24 AM
 */

package soapfault.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import java.rmi.RemoteException; 

/**
 *
 * @author dochez
 */
public class SimpleSoapFaultImpl implements SessionBean {
    
    SessionContext sc;
    
    /** Creates a new instance of SimpleServerImpl */
    public SimpleSoapFaultImpl() {
    }
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In GoogleEJB::ejbCreate !!");
}    
    
    public String simpleMethod()
        throws SimpleSoapException, RemoteException {
        
        throw new SimpleSoapException("I only raise exceptions !");
    }
    
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}