package com.sun.s1asdev.ejb.ejbc.redef;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface FooSuper extends Remote {

    void callHello() throws RemoteException;
    String sayHello() throws RemoteException;
}
