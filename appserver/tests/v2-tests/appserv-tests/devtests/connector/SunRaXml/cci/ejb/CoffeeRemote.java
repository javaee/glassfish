/*
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1peqe.connector.cci;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface CoffeeRemote extends EJBObject {
    public void insertCoffee(String name, int quantity) throws RemoteException;
    public int getCoffeeCount() throws RemoteException;
}
