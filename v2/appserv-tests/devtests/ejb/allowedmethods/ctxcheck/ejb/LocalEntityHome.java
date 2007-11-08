/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import javax.ejb.EJBLocalHome;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface LocalEntityHome
    extends EJBLocalHome
{

    /**
     * Gets a reference to the remote interface to the StudentBean bean.
     * @exception throws CreateException and RemoteException.
     *
     */
    public LocalEntity create(String studentId, String name)
        throws CreateException;

    /**
     * Gets a reference to the remote interface to the StudentBean object by Primary Key.
     * @exception throws FinderException and RemoteException.
     *
     */
 
    public LocalEntity findByPrimaryKey(String studentId) 
        throws FinderException;
}
