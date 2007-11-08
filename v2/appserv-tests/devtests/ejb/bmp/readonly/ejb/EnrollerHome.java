/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1asdev.ejb.bmp.readonly.ejb;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface EnrollerHome extends EJBHome {

    /**
     * Gets a reference to the remote interface to the EnrollerBean bean.
     * @exception throws CreateException and RemoteException.
     *
     */
    Enroller create() throws RemoteException, CreateException;
}
