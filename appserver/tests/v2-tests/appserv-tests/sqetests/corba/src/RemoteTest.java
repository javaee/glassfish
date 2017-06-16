package corba;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTest extends Remote {

    public void ping() throws RemoteException;

}