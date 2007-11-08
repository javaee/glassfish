/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package statelesshello;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface Statelesshello extends EJBObject {

    public String sayStatelesshello() throws RemoteException;
    public String getUserDefinedException() throws RemoteException, StatelesshelloException;
}
