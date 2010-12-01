package com.sun.s1asdev.jdbc.statementwrapper.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface NestedBMPHome
        extends EJBHome {
    NestedBMP create()
            throws RemoteException, CreateException;
}
