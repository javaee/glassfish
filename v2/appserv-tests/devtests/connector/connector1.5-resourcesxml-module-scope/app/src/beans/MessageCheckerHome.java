/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

public interface MessageCheckerHome extends EJBHome {
    MessageChecker create() throws RemoteException, CreateException;
    boolean done() throws RemoteException, CreateException;
    int expectedResults() throws RemoteException, CreateException;
    void notifyAndWait() throws RemoteException, CreateException;
}
