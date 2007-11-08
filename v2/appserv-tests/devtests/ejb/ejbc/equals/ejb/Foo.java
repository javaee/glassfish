package com.sun.s1asdev.ejb.ejbc.equals;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Foo extends EJBObject, FooSuper, java.rmi.Remote {
    void callHello() throws RemoteException;
    String sayHello() throws RemoteException;
    
    public boolean assertValidRemoteObject(String msg)
	throws RemoteException;
}
