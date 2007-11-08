/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1peqe.security.authoriz.methodperms;

import javax.ejb.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface MethodPermRemote extends EJBObject {

    public String authorizedMethod() throws RemoteException;
    public String authorizedMethod(String s, int i) throws RemoteException;
    public String authorizedMethod(int j) throws RemoteException;
    public void unauthorizedMethod() throws RemoteException;
    public String sayGoodbye() throws RemoteException;
    
}
