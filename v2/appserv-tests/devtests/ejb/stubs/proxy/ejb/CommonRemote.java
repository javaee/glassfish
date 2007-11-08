/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.stubs.proxy;

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

    // test proxy for behavior when interface method is not defined on
    // bean class.
    void notImplemented() throws RemoteException;

    void testException1() throws Exception, RemoteException;

    // will throw ejb exception
    void testException2() throws RemoteException;

    // throws some checked exception
    void testException3() throws javax.ejb.FinderException, RemoteException;

    void testException4() throws javax.ejb.FinderException, RemoteException;


    void testPassByRef1(int a) throws RemoteException;
    void testPassByRef2(Helper1 helper1) throws RemoteException;
    void testPassByRef3(Helper2 helper2) throws RemoteException;
    void testPassByRef4(CommonRemote cr) throws RemoteException;
    Helper1 testPassByRef5() throws RemoteException;
    Helper2 testPassByRef6() throws RemoteException;
    CommonRemote testPassByRef7() throws RemoteException;
    int testPassByRef8() throws RemoteException;

}
