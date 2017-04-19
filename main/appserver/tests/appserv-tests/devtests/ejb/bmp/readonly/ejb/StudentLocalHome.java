/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.ejb.bmp.readonly.ejb;

import javax.ejb.EJBLocalHome;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface StudentLocalHome extends EJBLocalHome {

    /**
     * Gets a reference to the remote interface to the StudentBean bean.
     * @exception throws CreateException
     *
     */
    public Student create(String studentId, String name)
        throws CreateException;

    /**
     * Gets a reference to the remote interface to the StudentBean object by Primary Key.
     * @exception throws FinderException.
     *
     */
 
    public Student findByPrimaryKey(String studentId) 
        throws FinderException;
}
