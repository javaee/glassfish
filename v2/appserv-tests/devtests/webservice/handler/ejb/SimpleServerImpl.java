package ejb;

import java.rmi.*;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class SimpleServerImpl implements SessionBean {
    SessionContext sc;
    
    public String sayHello(String hello) throws RemoteException {
        return "salut " + hello;
    }
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In SimpleServer:::ejbCreate !!");
    }
    
     public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}   
}