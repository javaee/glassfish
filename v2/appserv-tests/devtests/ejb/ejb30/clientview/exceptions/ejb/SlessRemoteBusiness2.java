/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

// Remote business interface

import javax.ejb.Remote;
import java.rmi.RemoteException;

@Remote
public interface SlessRemoteBusiness2 extends java.rmi.Remote
{
    void forceTransactionRequiredException() throws RemoteException;

    void forceTransactionRolledbackException() throws RemoteException;

    void throwRuntimeAppException() throws RemoteException, RuntimeAppException;
    void throwRollbackAppException() throws RemoteException, RollbackAppException;

    void denied() throws RemoteException;
}
