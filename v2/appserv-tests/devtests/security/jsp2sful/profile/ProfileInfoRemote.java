package profile;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * ProfileInfo Stateful Session Bean. Test JSR 115 authorization.
 * @author  swchan2
 */
public interface ProfileInfoRemote extends EJBObject {
    public String getCallerInfo() throws RemoteException;
    public String getSecretInfo() throws RemoteException;
}
