/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package samples.ejb.bmp.robean.ejb;

import java.rmi.RemoteException;
import javax.ejb.FinderException;

public interface CustomerTransactionalHome extends javax.ejb.EJBHome {
    public CustomerTransactional findByPrimaryKey(String SSN) throws FinderException, RemoteException; 
}

