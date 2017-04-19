package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface ThereRemote
    extends EJBObject
{
    public void doSomethingHere()
        throws RemoteException;

    public void accessEJBObject()
        throws RemoteException;

    public void accessEJBLocalObject()
        throws RemoteException;

    public void accessEJBHome()
        throws RemoteException;

    public void accessEJBLocalHome()
        throws RemoteException;
}
