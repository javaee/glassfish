package corba;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

public class RemoteTestImpl extends PortableRemoteObject implements RemoteTest {

    public RemoteTestImpl() throws RemoteException {
        super();
    }

    public void ping() {
    }

}