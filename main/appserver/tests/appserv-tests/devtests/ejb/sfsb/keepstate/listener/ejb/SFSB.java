package com.sun.s1asdev.ejb.ejb30.sfsb.lifecycle.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface SFSB
    extends EJBObject
{

    public String getName()
	throws RemoteException;

    public int getActivationCount()
	throws RemoteException;

    public int getPassivationCount()
	throws RemoteException;

    public void makeStateNonSerializable()
	throws RemoteException;

    public void sleepForSeconds(int sec)
	throws RemoteException;

    public boolean isOK(String name)
	throws RemoteException;

    public void unusedMethod()
	throws RemoteException;
}
