package com.sun.s1asdev.ejb.sfsb.stress.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface StressSFSB
    extends EJBObject
{

    public String getName()
	throws RemoteException;

    public boolean hasSameName(String name)
        throws RemoteException;

    public void ping()
        throws RemoteException;

    public boolean doWork(long millis)
        throws RemoteException;

}
