package com.sun.s1asdev.connector.failallconnections.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSession extends EJBObject {
    public boolean test1() throws RemoteException;
    public boolean test2() throws RemoteException;
}
