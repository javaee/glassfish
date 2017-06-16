/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.devtest.admin.notification.lookup.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.math.*;

/**
 *
 */
public interface LookupRemote extends EJBObject {

    /**
     */
    public BigDecimal dollarToYen(BigDecimal dollars) throws RemoteException;

    /**
     */
    public BigDecimal yenToEuro(BigDecimal yen) throws RemoteException;

    public boolean lookupResource(String jndiName) throws RemoteException;
}
