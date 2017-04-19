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
import javax.ejb.EJBLocalObject;

public interface StudentLocal extends EJBLocalObject {
 
  /**
   * Returns the CourseIds that a student is enrolled in. 
   * @param studentId primary key of the student object
   * @param courseId primary key of the course object
   * 
   */
   public ArrayList getCourseIds();

  /**
   * Returns the Name of a student.
   */
   public String getName();


  /**
   * Sets the Name of a student.
   */
   public void setName(String name, boolean notify);
}
