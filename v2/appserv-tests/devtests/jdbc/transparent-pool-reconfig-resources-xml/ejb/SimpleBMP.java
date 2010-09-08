package com.sun.s1asdev.jdbc.transparent_pool_reconfig.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP extends EJBObject {

    public boolean acquireConnectionsTest(boolean expectFailure, long sleep) throws RemoteException;

    public  void setProperty(String property, String value) throws RemoteException ;

    public  void setAttribute(String property, String value) throws RemoteException ;
}
