package com.sun.s1asdev.ejb.stubs.stubcount;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;


public interface FooHome10 extends EJBHome {
    Foo10 create () throws RemoteException, CreateException;
}
