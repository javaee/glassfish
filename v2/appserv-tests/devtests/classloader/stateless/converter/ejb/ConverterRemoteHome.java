/**
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.ejb.stateless.converter.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface ConverterRemoteHome extends EJBHome {
    ConverterRemote create() throws RemoteException, CreateException;
}
