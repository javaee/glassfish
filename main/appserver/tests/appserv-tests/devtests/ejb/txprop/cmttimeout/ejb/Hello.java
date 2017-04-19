package com.sun.s1asdev.ejb.txprop.cmttimeout;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Hello extends EJBObject {
    boolean compute(int timeout) throws RemoteException;
}
