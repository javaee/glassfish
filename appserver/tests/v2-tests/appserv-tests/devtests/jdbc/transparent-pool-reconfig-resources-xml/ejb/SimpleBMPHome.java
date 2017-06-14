package com.sun.s1asdev.jdbc.transparent_pool_reconfig.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMPHome
        extends EJBHome {
    SimpleBMP create() throws RemoteException, CreateException;
}
