package com.sun.s1asdev.ejb.ejbc.redef;


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.*;


public interface FooHomeSuper extends Remote {
   
    Foo create() throws RemoteException, CreateException;
}
