/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txstressreadonly.ejb.beanA;

import javax.ejb.EJBHome;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public interface TxRemoteHomeA extends EJBHome {
    public TxRemoteA create() throws RemoteException, CreateException;
}
