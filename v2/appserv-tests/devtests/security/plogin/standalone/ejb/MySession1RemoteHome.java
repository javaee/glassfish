import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;


/**
 * This is the home interface for MySession1 enterprise bean.
 */
public interface MySession1RemoteHome extends EJBHome {
    
    MySession1Remote create()  throws CreateException, RemoteException;
    
    
}
