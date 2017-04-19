/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.perf.local;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface BmpRemoteHome extends EJBHome
{
    BmpRemote create(String s) throws RemoteException, CreateException;
    BmpRemote findByPrimaryKey(String s) throws RemoteException, CreateException;    
}
