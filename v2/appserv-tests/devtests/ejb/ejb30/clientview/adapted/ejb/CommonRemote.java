/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import java.rmi.RemoteException;

public interface CommonRemote extends java.rmi.Remote
{
    public static final int STATELESS = 0;
    public static final int STATEFUL = 1;

    void notSupported() throws RemoteException;
    void required() throws RemoteException;
    void requiresNew() throws RemoteException;
    void mandatory() throws RemoteException;
    void never() throws RemoteException;
    void supports() throws RemoteException;

}
