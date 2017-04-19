package com.sun.s1asdev.ejb.ee.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface BMTSession
    extends EJBObject
{

    public String getName()
	throws RemoteException;

    public int getActivateCount()
	throws RemoteException;

    public int getPassivateCount()
	throws RemoteException;

    public void startTx()
	throws RemoteException;

    public void commitTx()
	throws RemoteException;

    public void rollbackTx()
	throws RemoteException;

    public void incrementCount()
	throws RemoteException;

    public void accessCMTBean()
	throws RemoteException;

    public CMTSession getCMTSession()
	throws RemoteException;
}
