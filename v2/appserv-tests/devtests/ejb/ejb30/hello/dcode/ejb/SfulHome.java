/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.hello.dcode;

public interface SfulHome extends javax.ejb.EJBHome
{
    public Sful create() throws javax.ejb.CreateException, java.rmi.RemoteException;
}
