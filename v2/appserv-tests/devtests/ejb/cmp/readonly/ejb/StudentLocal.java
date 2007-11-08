/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1asdev.ejb.cmp.readonly.ejb;

import java.util.ArrayList;
import javax.ejb.EJBLocalObject;

public interface StudentLocal extends EJBLocalObject {
 
  /**
   * Returns the Name of a student.
   */
   public String getName();


  /**
   * Sets the Name of a student.
   */
   public void setName(String name, boolean notify);
}
