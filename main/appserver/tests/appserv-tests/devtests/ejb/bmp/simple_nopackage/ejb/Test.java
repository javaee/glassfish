import javax.ejb.*;
import java.rmi.*;

public interface Test extends EJBObject {
    public void foo() throws RemoteException;
}
