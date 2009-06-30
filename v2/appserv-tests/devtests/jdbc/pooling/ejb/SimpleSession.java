package com.sun.s1asdev.jdbc.pooling.ejb;

import javax.ejb.*;
import java.rmi.*;
import javax.sql.DataSource;

public interface SimpleSession extends EJBObject {
    public boolean test1(boolean isXa, boolean rollback) throws RemoteException;
    public boolean test2(boolean isXa, boolean rollback) throws RemoteException;
    public boolean test3(boolean rollback) throws RemoteException;
    public boolean openMaxConnections(int count) throws RemoteException;
    public boolean openAndCloseConnection(int count) throws RemoteException;
}
