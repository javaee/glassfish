/**
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.devtest.security.plogin.converter.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.math.*;

/**
 * Remote interface for the <code>ConverterBean</code>. The remote interface, </code>Converter</code>
 * defines all possible business methods for the bean. These are methods, going tobe invoked
 * remotely by clients, once they have a reference to the remote interface.
 *
 * Clients generally take the help of JNDI to lookup the bean's home interface and
 * then use the home interface to obtain references to the bean's remote interface.
 *
 * @see ConverterHome
 * @see ConverterBean
 */
public interface ConverterRemote extends EJBObject {

    /**
     * Returns the yen value for a given dollar amount.
     * @param dollars dollar amount to be converted to yen.
     */
    public BigDecimal dollarToYen(BigDecimal dollars) throws RemoteException;

    /**
     * Returns the euro value for a given yen amount.
     * @param yen yen amount to be converted to euro.
     */
    public BigDecimal yenToEuro(BigDecimal yen) throws RemoteException;
    
    public String myCallerPrincipal() throws RemoteException;
    
}
