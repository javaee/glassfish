/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

import javax.ejb.Remote;
import java.rmi.RemoteException;

@Remote
public interface SfulRemoteBusiness2 extends java.rmi.Remote
{

    void foo();

    void forceTransactionRequiredException() throws RemoteException;

    void remove() throws RemoteException;

    void forceTransactionRolledbackException() throws RemoteException;

    void throwRuntimeAppException() throws RemoteException, RuntimeAppException;

    void throwRollbackAppException() throws RemoteException, RollbackAppException;

    void sleepFor(int sec) throws RemoteException;

    void pingRemote() throws RemoteException;

    void denied() throws RemoteException;
}
