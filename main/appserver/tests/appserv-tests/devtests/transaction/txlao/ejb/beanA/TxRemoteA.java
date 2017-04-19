/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txlao.ejb.beanA;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface TxRemoteA extends EJBObject{
    public boolean firstXAJDBCSecondNonXAJDBC() throws RemoteException ;
    public boolean firstNonXAJDBCSecondXAJDBC() throws RemoteException ;
    public boolean firstXAJDBCSecondXAJDBC() throws RemoteException ;
    public boolean firstNonXAJDBCSecondNonXAJDBC() throws RemoteException ;
    public boolean firstXAJMSSecondNonXAJDBC() throws RemoteException ;
    public boolean firstNonXAJDBCOnly() throws RemoteException ;
    public void cleanup() throws RemoteException;
    public boolean rollbackXAJDBCNonXAJDBC()throws RemoteException;
    public boolean rollbackNonXAJDBCXAJDBC() throws RemoteException;
    public boolean txCommit() throws RemoteException;
    public boolean txRollback() throws RemoteException;
}
