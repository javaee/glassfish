/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import java.util.ArrayList;
import javax.ejb.EJBLocalObject;
import java.rmi.RemoteException;


public interface LocalStudent
    extends EJBLocalObject
{
 
  /**
   * Returns the Name of a student.
   * exception RemoteException
   */
   public String getName();


  /**
   * Sets the Name of a student.
   * exception RemoteException
   */
   public void setName(String name);
}
