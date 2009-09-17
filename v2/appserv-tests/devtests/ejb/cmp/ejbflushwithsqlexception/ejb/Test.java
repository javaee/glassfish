package com.sun.s1asdev.ejb.ejbflush;

import javax.ejb.*;
import java.rmi.*;

public interface Test extends EJBObject {
 
    public void testA1() throws RemoteException;
        
    public void testA1WithFlush() throws RemoteException;
        
    public void testA2() throws RemoteException;
        
    public void testA2WithFlush() throws RemoteException;
        
}
