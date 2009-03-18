/*
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1peqe.connector.cci;

import javax.ejb.EJBLocalObject;

public interface CoffeeRemote extends EJBLocalObject {
    public void insertCoffee(String name, int quantity);

    public int getCoffeeCount();
}
