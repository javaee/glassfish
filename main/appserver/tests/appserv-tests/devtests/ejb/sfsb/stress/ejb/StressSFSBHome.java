package com.sun.s1asdev.ejb.sfsb.stress.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

public interface StressSFSBHome
    extends EJBHome
{
    public StressSFSB create(String sfsbName)
	throws CreateException, RemoteException;
}
