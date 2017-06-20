/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txglobal.ejb.beanA;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface TxRemoteA extends EJBObject{
    public boolean txCommit() throws RemoteException;
    public boolean txRollback() throws RemoteException;
}
