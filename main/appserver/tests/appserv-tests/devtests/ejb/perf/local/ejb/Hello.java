/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.perf.local;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface Hello extends EJBObject
{
    void warmup(int type, boolean local) throws RemoteException;

    float notSupported(int type, boolean tx) throws RemoteException;
    float required(int type, boolean tx) throws RemoteException;
    float requiresNew(int type, boolean tx) throws RemoteException;
    float mandatory(int type, boolean tx) throws RemoteException;
    float never(int type, boolean tx) throws RemoteException;
    float supports(int type, boolean tx) throws RemoteException;

    float notSupportedRemote(int type, boolean tx) throws RemoteException;
    float requiredRemote(int type, boolean tx) throws RemoteException;
    float requiresNewRemote(int type, boolean tx) throws RemoteException;
    float mandatoryRemote(int type, boolean tx) throws RemoteException;
    float neverRemote(int type, boolean tx) throws RemoteException;
    float supportsRemote(int type, boolean tx) throws RemoteException;
}
