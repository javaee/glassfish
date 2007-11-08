/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1asdev.ejb.bmp.handle.mix.ejb;

import java.util.ArrayList;
import javax.ejb.EJBObject;
import java.rmi.RemoteException;


public interface Student extends EJBObject {
 
  /**
   * Returns the CourseIds that a student is enrolled in. 
   * @param studentId primary key of the student object
   * @param courseId primary key of the course object
   * @exception RemoteException
   */
   public ArrayList getCourseIds() throws RemoteException;

  /**
   * Returns the Name of a student.
   * exception RemoteException
   */
   public String getName() throws RemoteException;


  /**
   * Sets the Name of a student.
   * exception RemoteException
   */
   public void setName(String name) throws RemoteException;
}
