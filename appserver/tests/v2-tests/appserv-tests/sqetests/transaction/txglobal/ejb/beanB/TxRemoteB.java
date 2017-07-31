/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txglobal.ejb.beanB;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface TxRemoteB extends EJBObject{
    public void delete(String account) throws RemoteException;
    public void insert(String account, float balance) throws RemoteException;
    public void sendJMSMessage(String msg) throws RemoteException;
    public boolean verifyResults(String account, String resource) 
    throws RemoteException;
}
