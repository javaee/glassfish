package com.sun.s1asdev.ejb.sfsb.keepstate.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface SFSB
    extends EJBObject
{

    public String getName()
	throws RemoteException;

    public void createSFSBChild() 
	throws RemoteException;

    public String getMessage()
	throws RemoteException;

    public boolean checkSessionContext()
        throws RemoteException;       

    public boolean checkInitialContext()
        throws RemoteException;       

    public boolean checkEntityHome()
        throws RemoteException;       

    public boolean checkEntityLocalHome()
        throws RemoteException;       

    public boolean checkEntityRemoteRef()
        throws RemoteException;       

    public boolean checkEntityLocalRef()
        throws RemoteException;       

    public boolean checkHomeHandle()
        throws RemoteException;       

    public boolean checkHandle()
        throws RemoteException;       

    public boolean checkUserTransaction()
        throws RemoteException;

    public boolean isOK(String name)
        throws RemoteException;

    public int getActivationCount()
	throws RemoteException;

    public int getPassivationCount()
	throws RemoteException;

    public void makeStateNonSerializable()
	throws RemoteException;

    public void sleepForSeconds(int sec)
	throws RemoteException;

    public void unusedMethod()
	throws RemoteException;
}
