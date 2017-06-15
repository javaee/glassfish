package com.sun.s1asdev.jdbc.statementwrapper.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
    extends EJBObject {
    public boolean statementTest() throws RemoteException;
    public boolean preparedStatementTest() throws RemoteException;
    public boolean preparedStatementTest1() throws RemoteException;
    public boolean callableStatementTest() throws RemoteException;
    public boolean metaDataTest() throws RemoteException;
    public boolean resultSetTest() throws RemoteException;
    public boolean test1() throws RemoteException;
}
