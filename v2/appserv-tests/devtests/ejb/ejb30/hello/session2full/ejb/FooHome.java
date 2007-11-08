package com.sun.s1asdev.ejb.ejb30.hello.session2full;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;


public interface FooHome extends EJBHome {
    Foo create () throws RemoteException, CreateException;
}
