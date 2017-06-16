/*
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1peqe.connector.cci;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface CoffeeRemoteHome extends EJBLocalHome {
    CoffeeRemote create() throws CreateException;
}
