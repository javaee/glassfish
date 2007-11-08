package com.sun.s1asdev.jdbc.connsharing.xa.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSession extends EJBObject {
    public boolean test1() throws RemoteException;
    public boolean test2() throws RemoteException;
    public boolean test3() throws RemoteException;
    public boolean test4() throws RemoteException;
    public boolean query() throws RemoteException;
    public boolean query2() throws RemoteException;
}
