/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txglobal.ejb.beanB;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface TxRemoteB extends EJBObject{
    public void test1() throws RemoteException;
    public void test2() throws RemoteException;
    public int getCommitStatus() throws RemoteException;
}
