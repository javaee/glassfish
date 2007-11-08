/**
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.devtest.security.plogin.converter.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * Home interface for the <code>ConverterBean</code>. Clients generally use home interface
 * to obtain references to the bean's remote interface, <code>Converter</code>.
 *
 * @see Converter
 * @see ConverterBean
 */
public interface ConverterRemoteHome extends EJBHome {
    /**
     * Gets a reference to the remote interface of the <code>ConverterBean</code>.
     * @exception throws CreateException and RemoteException.
     *
     */
    ConverterRemote create() throws RemoteException, CreateException;
}
