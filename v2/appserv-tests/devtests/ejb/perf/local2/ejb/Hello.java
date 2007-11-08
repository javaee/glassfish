/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.perf.local2;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface Hello extends EJBObject
{
    void warmup(int type) throws RemoteException;

    float createAccessRemove(int type, boolean tx) throws RemoteException;

    float notSupported(int type, boolean tx) throws RemoteException;
    float required(int type, boolean tx) throws RemoteException;
    float requiresNew(int type, boolean tx) throws RemoteException;
    float mandatory(int type, boolean tx) throws RemoteException;
    float never(int type, boolean tx) throws RemoteException;
    float supports(int type, boolean tx) throws RemoteException;

}
