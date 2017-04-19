/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txlao.ejb.beanB;

import javax.ejb.EJBHome;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public interface TxRemoteHomeB extends EJBHome {
    public TxRemoteB create() throws RemoteException, CreateException;
}

