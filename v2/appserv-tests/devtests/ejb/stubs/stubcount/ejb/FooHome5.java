package com.sun.s1asdev.ejb.stubs.stubcount;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;


public interface FooHome5 extends EJBHome {
    Foo5 create () throws RemoteException, CreateException;
}
