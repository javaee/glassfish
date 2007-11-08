/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.ejb.bmp.readonly.ejb;

import javax.ejb.EJBHome;
import java.rmi.RemoteException;
import javax.ejb.FinderException;
import javax.ejb.CreateException;

public interface CourseHome extends EJBHome {

    /**
     * Gets a reference to the remote interface to the CourseBean bean.
     * @exception throws CreateException and RemoteException.
     *
     */
    public Course create(String courseId, String name)
        throws RemoteException, CreateException;
    
    /**
     * Gets a reference to the remote interface to the CourseBean object by Primary Key.
     * @exception throws FinderException and RemoteException.
     *
     */
    public Course findByPrimaryKey(String courseId) 
        throws FinderException, RemoteException;
}
