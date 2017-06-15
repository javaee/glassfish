/*
 * SimpleServerImpl.java
 *
 * Created on September 13, 2004, 11:24 AM
 */

package stubprops;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import java.rmi.RemoteException; 

/**
 *
 * @author dochez
 */
public class SimpleServerImpl implements SessionBean {
    
    SessionContext sc;
    
    /** Creates a new instance of SimpleServerImpl */
    public SimpleServerImpl() {
    }
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In GoogleEJB::ejbCreate !!");
}    
    
    public String sayHello(String who) throws RemoteException {
        return "hello" + who;
    }    
    
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}
