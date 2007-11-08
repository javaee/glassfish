package com.sun.s1asdev.admin.ee.synchronization.api.client;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

public interface SynchronizationHome
    extends EJBHome
{
	public Synchronization create()
        throws CreateException, RemoteException;
}
