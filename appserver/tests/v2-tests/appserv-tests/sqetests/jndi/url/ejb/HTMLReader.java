/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1peqe.jndi.url.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface HTMLReader extends EJBObject {
 
   public StringBuffer getContents() 
      throws RemoteException, HTTPResponseException;

}
