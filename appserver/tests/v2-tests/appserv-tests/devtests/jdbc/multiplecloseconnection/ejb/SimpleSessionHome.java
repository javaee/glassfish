package com.sun.s1asdev.jdbc.multiplecloseconnection.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

public interface SimpleSessionHome extends EJBHome {
    SimpleSession create()
            throws RemoteException, CreateException;

}
