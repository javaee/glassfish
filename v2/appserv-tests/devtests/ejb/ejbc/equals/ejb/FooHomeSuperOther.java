package com.sun.s1asdev.ejb.ejbc.equals;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FooHomeSuperOther extends java.rmi.Remote {
    Foo create() throws RemoteException, javax.ejb.CreateException;
}
