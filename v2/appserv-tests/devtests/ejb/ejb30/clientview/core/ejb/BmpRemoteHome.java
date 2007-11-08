/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.core;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface BmpRemoteHome extends BmpRemoteHomeSuper
{
    BmpRemote findByPrimaryKey(String s) throws RemoteException, CreateException;    
}
