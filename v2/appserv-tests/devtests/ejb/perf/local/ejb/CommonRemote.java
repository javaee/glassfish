/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.perf.local;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface CommonRemote extends EJBObject
{
    public static final int STATELESS = 0;
    public static final int STATEFUL = 1;
    public static final int BMP = 2;
    public static final int CMP = 3;

    void notSupported() throws RemoteException;
    void required() throws RemoteException;
    void requiresNew() throws RemoteException;
    void mandatory() throws RemoteException;
    void never() throws RemoteException;
    void supports() throws RemoteException;

}
