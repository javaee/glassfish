/*
 * SimpleServer.java
 *
 * Created on September 13, 2004, 11:23 AM
 */

package stubprops;

import java.rmi.RemoteException; 
import java.rmi.Remote; 

/**
 * Simple WebServices Interface
 *
 * @author Jerome Dochez
 */
public interface SimpleServer extends Remote {
    
    public String sayHello(String who) throws RemoteException;
}
