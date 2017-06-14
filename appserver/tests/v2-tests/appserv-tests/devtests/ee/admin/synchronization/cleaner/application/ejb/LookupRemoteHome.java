/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.devtest.admin.notification.lookup.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 */
public interface LookupRemoteHome extends EJBHome {
    /**
     */
    LookupRemote create() throws RemoteException, CreateException;
}
