/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.s1asdev.ejb.bmp.handle.mix.ejb;

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

   public String testEnrollerHomeHandle()
       throws RemoteException;

   public String testEnrollerHandle()
       throws RemoteException;

   public String testStudentHomeHandle()
       throws RemoteException;

   public String testStudentHandle(String studentID)
       throws RemoteException;

}
