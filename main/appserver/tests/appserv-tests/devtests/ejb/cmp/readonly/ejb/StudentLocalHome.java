
/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.ejb.cmp.readonly.ejb;

import javax.ejb.EJBLocalHome;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface StudentLocalHome extends EJBLocalHome {

    /**
     * Gets a reference to the remote interface to the StudentBean bean.
     * @exception throws CreateException
     *
     */
    public StudentLocal create(String studentId, String name)
        throws CreateException;

    /**
     * Gets a reference to the remote interface to the StudentBean object by Primary Key.
     * @exception throws FinderException.
     *
     */
 
    public StudentLocal findByPrimaryKey(String studentId) 
        throws FinderException;


    public StudentLocal findByLocalStudent(StudentLocal student) throws FinderException;
    
}
