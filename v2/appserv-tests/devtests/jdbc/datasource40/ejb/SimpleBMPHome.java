package com.sun.s1asdev.jdbc.datasource40.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMPHome
        extends EJBHome {
    SimpleBMP create()
            throws RemoteException, CreateException;

}
