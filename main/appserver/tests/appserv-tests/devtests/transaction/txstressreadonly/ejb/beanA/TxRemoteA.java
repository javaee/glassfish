/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txstressreadonly.ejb.beanA;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface TxRemoteA extends EJBObject{
    public boolean txCommit(int identity) throws RemoteException;
}
