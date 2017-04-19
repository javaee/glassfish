package com.sun.s1asdev.ejb.ejbc.sameimpl;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Hello extends EJBObject {
    void callHello() throws RemoteException;
    String sayHello() throws RemoteException;
}
