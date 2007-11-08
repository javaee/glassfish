package com.sun.s1asdev.ejb.ee.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

public interface CMTSessionHome
    extends EJBHome
{
    public CMTSession create(String sfsbName)
	throws CreateException, RemoteException;

}
