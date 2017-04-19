/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import javax.ejb.*;


public interface SfulRemoteHome extends EJBHome
{
    SfulRemote create() throws CreateException, java.rmi.RemoteException;

    SfulRemote create(int ignore) throws CreateException, java.rmi.RemoteException;

    SfulRemote createFoo(int state) throws CreateException, java.rmi.RemoteException;

}
