/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 *
 */

package com.sun.s1peqe.ejb.cmp.preselect.ejb;

import java.util.*;
import javax.ejb.*;
import java.rmi.RemoteException;

public interface CustomerHome extends EJBHome {
    
    public CustomerRemote create (String id, String name) throws RemoteException, CreateException;
    
    public CustomerRemote findByPrimaryKey (String id)
        throws FinderException, RemoteException;     
}
