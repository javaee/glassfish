/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.stubs.proxy;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface SfulRemoteHome extends EJBHome
{
    SfulRemote create() throws CreateException, RemoteException;
}
