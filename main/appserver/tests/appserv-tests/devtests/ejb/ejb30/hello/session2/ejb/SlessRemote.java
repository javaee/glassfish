/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.hello.session2;

public interface SlessRemote extends javax.ejb.EJBObject
{
    public String hello() throws java.rmi.RemoteException;
}
