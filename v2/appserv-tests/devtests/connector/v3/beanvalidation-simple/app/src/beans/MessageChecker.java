/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface MessageChecker extends EJBObject {
    int getMessageCount() throws RemoteException;
    boolean done() throws RemoteException;
    int expectedResults() throws RemoteException;
    void notifyAndWait() throws RemoteException;
    boolean testAdminObject(String jndiName, boolean expectLookupSuccess) throws RemoteException;
    boolean testRA(int intValue) throws RemoteException;
}
