package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;


public interface ThereRemoteHome
    extends EJBHome
{
    ThereRemote create()
        throws CreateException, RemoteException;
}
