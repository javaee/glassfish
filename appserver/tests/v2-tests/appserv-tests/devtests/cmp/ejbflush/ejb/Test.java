package test;

import javax.ejb.*;
import java.rmi.*;

public interface Test extends EJBObject {
 
    public void testA1() throws CreateException, RemoteException;
        
    public void testA2() throws CreateException, RemoteException;
        
    public void testA1WithFlush() throws CreateException, 
            FlushException, RemoteException;
        
    public void testA2WithFlush() throws CreateException, 
            FlushException, RemoteException;
        
}
