/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.bmp.robean.ejb;

import java.rmi.RemoteException;

public interface Address extends javax.ejb.EJBObject {
    public String getName() throws RemoteException;

    public String getAddress() throws RemoteException;

    public String getSSN() throws RemoteException;
}
