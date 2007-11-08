package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;


public interface DriverHome extends EJBHome {
    Driver create () throws RemoteException, CreateException;
}
