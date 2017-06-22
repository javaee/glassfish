package com.sun.s1asdev.jdbc.notxconn.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface SimpleSession extends EJBObject {
    public boolean test1() throws RemoteException;
}
