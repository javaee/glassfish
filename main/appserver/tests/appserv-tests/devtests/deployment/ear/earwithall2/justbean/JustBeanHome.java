/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package justbean;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;
 
public interface JustBeanHome extends EJBHome {
 
    public JustBean 
    create() 
        throws RemoteException, CreateException;
    
}
