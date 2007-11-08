package com.sun.s1asdev.ejb.ejbc.redef;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.*;


public interface FooHome extends EJBHome, FooHomeSuper {
    Foo create () throws RemoteException, CreateException;

 // erroneously override reserved EJBHome methods
    void remove(Object o) throws RemoteException, RemoveException;
    void remove(Handle h) throws RemoteException, RemoveException;
    EJBMetaData getEJBMetaData() throws RemoteException;
    HomeHandle getHomeHandle() throws RemoteException;


}
