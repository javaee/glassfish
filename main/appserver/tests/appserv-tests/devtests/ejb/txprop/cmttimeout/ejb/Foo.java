package com.sun.s1asdev.ejb.txprop.cmttimeout;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Foo extends EJBObject {
    boolean invokeMethod(int timeout) throws RemoteException;
}
