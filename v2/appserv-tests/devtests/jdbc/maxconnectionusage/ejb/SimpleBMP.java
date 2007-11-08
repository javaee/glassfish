package com.sun.s1asdev.jdbc.maxconnectionusage.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
        extends EJBObject {
    public boolean test1(boolean useXA) throws RemoteException;

    public String test2(boolean useXA, int value) throws RemoteException;

    public String test3(int count, boolean useXA, int value) throws RemoteException;
}
