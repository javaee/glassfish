/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package j2eeguide.product;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.*;

public interface ProductHome extends EJBHome {

    public Product create(String productId, String description, 
        double balance) throws RemoteException, CreateException;
    
    public Product findByPrimaryKey(String productId) 
        throws FinderException, RemoteException;
    
    public Collection findByDescription(String description)
        throws FinderException, RemoteException;

    public Collection findInRange(double low, double high)
        throws FinderException, RemoteException;
}
