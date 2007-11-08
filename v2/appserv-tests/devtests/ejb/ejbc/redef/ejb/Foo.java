package com.sun.s1asdev.ejb.ejbc.redef;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Foo extends EJBObject, FooSuper {

    // erroneously override EJBObject methods.
    EJBHome getEJBHome() throws RemoteException;
    Object getPrimaryKey() throws RemoteException;
    void remove() throws RemoteException,RemoveException;
    Handle getHandle() throws RemoteException;
    boolean isIdentical(EJBObject o) throws RemoteException;

    void callHello() throws RemoteException;
    String sayHello() throws RemoteException;
}
