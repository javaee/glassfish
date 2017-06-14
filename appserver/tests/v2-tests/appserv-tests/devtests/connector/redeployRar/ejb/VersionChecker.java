/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface VersionChecker extends EJBObject {
    int getVersion() throws RemoteException;
}
