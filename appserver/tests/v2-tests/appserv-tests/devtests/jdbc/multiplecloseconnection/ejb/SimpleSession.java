package com.sun.s1asdev.jdbc.multiplecloseconnection.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface SimpleSession extends EJBObject {
    public boolean test1() throws RemoteException;

    public boolean test2() throws RemoteException;
}
