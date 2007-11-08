/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.ejb.bmp.readonly.ejb;

import java.util.ArrayList;
import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface Enroller extends EJBObject {
 
 /**
  * Enrolls a Student in a course
  * @param studentId primary key of the student object
  * @param courseId primary key of the course object
  * @exception RemoteException
  */
   public void enroll(String studentId, String courseId)
      throws RemoteException;
 /**
  * Un-Enrolls a Student in a course
  * @param studentId primary key of the student object
  * @param courseId primary key of the course object
  * @exception RemoteException
  */

   public void unEnroll(String studentId, String courseId)
      throws RemoteException;
 /**
  * Deletes a Student 
  * @param studentId primary key of the student object
  * @exception RemoteException
  */

   public void deleteStudent(String studentId)
      throws RemoteException;

 /**
  * Deletes a Course 
  * @param courseId primary key of the course object
  * @exception RemoteException
  */
   public void deleteCourse(String courseId)
      throws RemoteException;
 /**
  * Returns an Arraylist of StudentsIds enrolled in a course
  * @param courseId primary key of the course object
  * @exception RemoteException
  */

   public ArrayList getStudentIds(String courseId)
      throws RemoteException;
 /**
  * Return an ArrayList of CourseIds that student is enroller in
  * @param studentId primary key of the student object
  * @exception RemoteException
  */

   public ArrayList getCourseIds(String studentId)
      throws RemoteException;

   public boolean canGetReadOnlyBeanNotifier(boolean testNewNotifier)
       throws RemoteException;

    public boolean canGetReadOnlyBeanLocalNotifier(boolean testNewNotifier)
       throws RemoteException;

   public boolean testReadOnlyBeanStudentRefresh(String studentId, boolean testNewNotifier)
       throws RemoteException;

    public boolean testReadOnlyBeanLocalStudentRefresh(String studentId, boolean testNewNotifier)
       throws RemoteException;

    public boolean testReadOnlyBeanLocalCreate(String studentId, String name)
        throws RemoteException;
}
