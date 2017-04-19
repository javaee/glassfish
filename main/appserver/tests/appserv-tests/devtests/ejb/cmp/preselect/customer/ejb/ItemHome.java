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

public interface ItemHome extends EJBHome {
    
    public ItemRemote create (String id, String name, double price) throws CreateException, RemoteException;
    
    public ItemRemote findByPrimaryKey (String id)
        throws FinderException, RemoteException;     
}
