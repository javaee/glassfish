package com.sun.s1asdev.jdbc.datasource40.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
        extends EJBObject {
    public boolean test1() throws RemoteException;

    public boolean test2() throws RemoteException;

    public boolean test3() throws RemoteException;
}
