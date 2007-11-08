package com.sun.s1asdev.ejb.stubs.ejbapp;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Hello extends EJBObject {
    void sayHello() throws RemoteException;
}
