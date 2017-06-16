/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.bmp.robean.ejb;

import java.rmi.RemoteException;
import javax.ejb.FinderException;

public interface AddressHome extends javax.ejb.EJBHome {
    public Address findByPrimaryKey(String SSN) throws FinderException, RemoteException; 
}

