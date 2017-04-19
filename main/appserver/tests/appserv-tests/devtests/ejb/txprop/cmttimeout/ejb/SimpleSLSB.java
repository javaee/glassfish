package com.sun.s1asdev.ejb.slsb;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SimpleSLSB
    extends EJBObject
{

    public boolean doSomething(int timeout)
        throws RemoteException;

    public boolean doSomethingAndRollback()
        throws RemoteException;

}
