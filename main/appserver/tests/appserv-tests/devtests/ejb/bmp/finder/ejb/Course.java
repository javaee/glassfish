/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1asdev.ejb.bmp.finder.ejb;

import java.util.ArrayList;
import javax.ejb.EJBObject;
import java.rmi.RemoteException;


public interface Course extends EJBObject {
 
    /**
     * Returns an arraylist of StudentIds taking the course.
     * @exception RemoteException 
     */
    public ArrayList getStudentIds() throws RemoteException;

    /**
     * Returns the name of the course.
     * @exception RemoteException 
     * 
     */
    public String getName() throws RemoteException;

    /**
     * Sets the name of the course.
     * @exception RemoteException 
     * 
     */
    public void setName(String name) throws RemoteException;

}
