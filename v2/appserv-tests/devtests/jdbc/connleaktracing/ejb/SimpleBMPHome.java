package com.sun.s1asdev.jdbc.connectionleaktracing.ejb;

import javax.ejb.CreateException;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;

public interface SimpleBMPHome
        extends EJBHome {
    SimpleBMP create()
            throws RemoteException, CreateException;

}
