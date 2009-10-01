package com.sun.s1asdev.jdbc.connectionleaktracing.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface SimpleBMP extends EJBObject {
    public boolean test1() throws RemoteException;
}
