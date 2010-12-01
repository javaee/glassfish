package com.sun.s1asdev.jdbc.statementwrapper.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
        extends EJBObject {
    public boolean test1(int number) throws RemoteException;

    public boolean test2() throws RemoteException;

    public void test3() throws RemoteException;
}
