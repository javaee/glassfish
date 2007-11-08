import javax.ejb.*;
import java.rmi.*;

public interface TestHome extends EJBHome {
    Test create(int i) throws RemoteException, CreateException;
    Test findByPrimaryKey(int i) throws RemoteException, FinderException;
}
