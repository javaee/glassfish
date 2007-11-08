package ejb;

import java.rmi.*;

public interface SimpleServer extends Remote {
    
    public String sayHello(String source) throws RemoteException;
    
}