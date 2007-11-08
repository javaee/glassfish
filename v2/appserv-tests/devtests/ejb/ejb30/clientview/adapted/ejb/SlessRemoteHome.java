/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import javax.ejb.*;


public interface SlessRemoteHome extends EJBHome
{
    SlessRemote create() throws CreateException, java.rmi.RemoteException;
}
