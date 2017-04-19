/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txlao.ejb.beanB;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface TxRemoteB extends EJBObject{
    public void firstXAJDBCSecondNonXAJDBC(String acc, float bal) throws RemoteException;
    public void firstNonXAJDBCSecondXAJDBC(String acc, float bal) throws RemoteException ;

    public void firstXAJDBCSecondXAJDBC(String acc, float bal) throws RemoteException ;

    public void firstNonXAJDBCSecondNonXAJDBC(String acc, float bal) throws RemoteException ;

    public void firstXAJMSSecondNonXAJDBC(String msg, String acc, float bal) throws RemoteException ;

    public void firstNonXAJDBCOnly(String acc, float bal) throws RemoteException ;
    public void rollbackXAJDBCNonXAJDBC(String acc, float bal)throws RemoteException;
    public void rollbackNonXAJDBCXAJDBC(String acc, float bal) throws RemoteException;
    public void delete(String dbURL,String account) throws RemoteException;
    public void delete(String account)  throws RemoteException;
    public void insert(String dbURL,String account, float balance) throws RemoteException;
    public void sendJMSMessage(String msg) throws RemoteException;
    public boolean verifyResults(String account, String resource, String resType)
    throws RemoteException;
}
