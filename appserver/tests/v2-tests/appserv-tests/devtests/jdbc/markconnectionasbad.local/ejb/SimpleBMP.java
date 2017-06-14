package com.sun.s1asdev.jdbc.markconnectionasbad.local.ejb;

import javax.ejb.*;
import java.rmi.*;
import java.sql.Connection;

public interface SimpleBMP extends EJBObject {

    public String test1() throws RemoteException;

    public String test2() throws RemoteException;

    public boolean test3() throws RemoteException;

    public boolean test4() throws RemoteException;
    
    public boolean test5(int count, boolean expectSuccess) throws RemoteException;
}
