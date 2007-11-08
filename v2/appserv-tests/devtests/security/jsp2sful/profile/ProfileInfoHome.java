package profile;
import javax.ejb.EJBHome;
/**
 *
 * @author  swchan2
 */
public interface ProfileInfoHome extends EJBHome{
    
    public ProfileInfoRemote create(String name) 
        throws java.rmi.RemoteException, javax.ejb.CreateException;
    
}
