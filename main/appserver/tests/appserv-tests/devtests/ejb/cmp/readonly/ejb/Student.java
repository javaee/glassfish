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
import javax.ejb.EJBObject;
import java.rmi.RemoteException;


public interface Student extends EJBObject {

  /**
   * Returns the Name of a student.
   * exception RemoteException
   */
   public String getName() throws RemoteException;

    /**
     * Same as getName(), except done within a tx
     */
    public String getNameTx() throws RemoteException;


  /**
   * Sets the Name of a student.
   * exception RemoteException
   */
   public void setName(String name, boolean notify) throws RemoteException;
}
