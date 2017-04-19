/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

// Remote business interface
import java.rmi.RemoteException;

public interface SfulRemoteBusiness extends CommonRemote
{

    public void removeRetainIfException(boolean throwException) 
        throws Exception;

    // The remote business interface has no relationship to EJBObject
    // so it's not a problem to define a method that happens to have the
    // same signature as one of EJBObject's methods.  remove() is
    // a likely name for a method that has @Remove behavior so it needs
    // to work.  
    public void remove() throws RemoteException;

}
