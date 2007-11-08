package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Driver extends EJBObject {

    public void localSlsbGetEJBObject()
        throws RemoteException;

    public void localSlsbGetEJBLocalObject()
        throws RemoteException;

    public void localSlsbGetEJBHome()
        throws RemoteException;

    public void localSlsbGetEJBLocalHome()
        throws RemoteException;

    public void localEntityGetEJBObject()
        throws RemoteException;

    public void localEntityGetEJBLocalObject()
        throws RemoteException;

    public void localEntityGetEJBHome()
        throws RemoteException;

    public void localEntityGetEJBLocalHome()
        throws RemoteException;

}
