package com.sun.s1asdev.ejb.stubs.stubcount;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Foo7 extends EJBObject {
    void sayHello() throws RemoteException;
}
