package com.sun.s1asdev.jdbc.multipleusercredentials.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSessionHome extends EJBHome {
    SimpleSession create()
            throws RemoteException, CreateException;

}
