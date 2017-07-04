/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

public interface WorkTestHome extends EJBHome {
    WorkTest create() throws RemoteException, CreateException;
    int executeTest() throws RemoteException, CreateException;
}
