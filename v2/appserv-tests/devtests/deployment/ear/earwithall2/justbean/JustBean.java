/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package justbean;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

public interface JustBean extends EJBObject {

    public void 
    log(String message) 
        throws RemoteException;

    public String[]
    findAllMarbles() 
        throws RemoteException;
}
