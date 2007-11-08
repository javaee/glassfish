package com.sun.s1asdev.ejb.stubs.ejbclient;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;


public interface FooHome extends EJBHome {
    Foo create () throws RemoteException, CreateException;
}
