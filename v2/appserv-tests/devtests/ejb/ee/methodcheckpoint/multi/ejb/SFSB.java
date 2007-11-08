package com.sun.s1asdev.ejb.ee.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface SFSB
    extends EJBObject
{

    public String getAccountHolderName()
	throws RemoteException;

    public int getBalance()
	throws RemoteException;

    public void incrementBalance(int val)
	throws RemoteException;

    public int getCheckpointedBalance()
	throws RemoteException;	     




    public void nonTxNonCheckpointedMethod()
	throws RemoteException;

    public void nonTxCheckpointedMethod()
	throws RemoteException;

    public void txNonCheckpointedMethod()
	throws RemoteException;

    public void txCheckpointedMethod()
	throws RemoteException;

}
