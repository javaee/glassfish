package com.sun.s1asdev.ejb.ejbc.equals;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface FooSuper extends java.rmi.Remote {
    void louie() throws RemoteException;
}
