/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1peqe.ejb.bmp.enroller.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface Enroller extends EJBObject {
 
   public String doTest(String threadId)
      throws RemoteException;

   public int verifyTest()
      throws RemoteException;
}
