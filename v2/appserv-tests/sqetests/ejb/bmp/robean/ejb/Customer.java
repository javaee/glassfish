/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.bmp.robean.ejb;

import java.rmi.RemoteException;

public interface Customer extends javax.ejb.EJBObject {
    public double getBalance() throws RemoteException;

    public void doCredit(double amount) throws RemoteException;

    public void doDebit(double amount) throws RemoteException;
}
