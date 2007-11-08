/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.stubs.proxy;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface Hello extends EJBObject
{
    void warmup(int type) throws RemoteException;
    void shutdown() throws RemoteException;

    float notSupported(int type, boolean tx) throws RemoteException;
    float required(int type, boolean tx) throws RemoteException;
    float requiresNew(int type, boolean tx) throws RemoteException;
    float mandatory(int type, boolean tx) throws RemoteException;
    float never(int type, boolean tx) throws RemoteException;
    float supports(int type, boolean tx) throws RemoteException;

    void throwException() throws RemoteException, Exception;
    void throwAppException1() throws RemoteException, FinderException;
    void throwAppException2() throws RemoteException, FinderException;

    Object testPassByRef() throws RemoteException;
}
