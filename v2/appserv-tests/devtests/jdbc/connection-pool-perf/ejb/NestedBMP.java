package com.sun.s1asdev.jdbc.statementwrapper.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface NestedBMP extends EJBObject {
    public boolean test1() throws RemoteException;
}
