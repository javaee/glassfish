/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.distributedtx;

import java.rmi.RemoteException;
import java.io.Serializable;
import javax.transaction.*;
import java.util.Properties;

import com.sun.ejb.*;
import com.sun.enterprise.*;
import com.sun.enterprise.log.Log;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.iiop.*;
import com.sun.enterprise.util.InvocationManagerImpl;

import java.util.logging.*;
import com.sun.logging.*;

/** 
 * This class implements javax.transaction.UserTransaction .
 * Its methods are called from TX_BEAN_MANAGED EJB code.
 * Most of its methods just delegate to the TransactionManager
 * after doing some EJB Container-related steps.
 *
 * Note: EJB1.1 Section 6.4.1 requires that the Container must be able to 
 * preserve an object reference of the UserTransaction interface across 
 * passivation, so we make this Serializable.
 *
 * @author Tony Ng
 */

public class UserTransactionImpl implements UserTransaction, Serializable
{

    static Logger _logger=LogDomains.getLogger(LogDomains.JTA_LOGGER);
	// Sting Manager for Localization
	private static StringManager sm = StringManager.getManager(UserTransactionImpl.class);
    private static final boolean debug = false;
    private transient J2EETransactionManager transactionManager;
    private transient InvocationManager invocationManager;
    private transient boolean initialized;

    // for non-J2EE clients usage
    private transient UserTransaction userTx;

    // private int transactionTimeout;

    // true if ejb access checks should be performed.  Default is
    // true.  All instances of UserTransaction exposed to applications
    // will have checking turned on.   
    private boolean checkEjbAccess;

    /**
     * Default constructor.
     */
    public UserTransactionImpl()
    {
        this(true);
    }

    /**
     * Alternate version of constructor that allows control over whether
     * ejb access checks are performed.  
     */
    public UserTransactionImpl(boolean doEjbAccessChecks)
    {
        init();
        checkEjbAccess = doEjbAccessChecks;
    }


    /**
     * Could be called after passivation and reactivation
     */
    private void init()
    {
        initialized = true;
        Switch theSwitch = Switch.getSwitch();
        transactionManager = theSwitch.getTransactionManager();
        invocationManager = theSwitch.getInvocationManager();
	if (invocationManager == null) 
	    invocationManager = new InvocationManagerImpl();
        if (transactionManager == null) {
	    PEORBConfigurator.initTransactionService(null, new Properties());
	    transactionManager = 
		    J2EETransactionManagerImpl.createTransactionManager();
	    theSwitch.setTransactionManager(transactionManager);

            // non J2EE client, set up UserTransaction from JTS
           //  userTx = new com.sun.jts.jta.UserTransactionImpl();
        }
    }

    private void checkUserTransactionMethodAccess(ComponentInvocation inv) 
        throws IllegalStateException, SystemException
    {
        if ( (inv.getInvocationType() == ComponentInvocation.EJB_INVOCATION)
             && checkEjbAccess ) {
            Container ejbContainer = (Container) inv.container;
            if( !ejbContainer.userTransactionMethodsAllowed(inv) ) {
                throw new IllegalStateException(sm.getString("enterprise_distributedtx.operation_not_allowed"));
            }
        }
    }

    public void begin() throws NotSupportedException, SystemException
    {
        if (!initialized) init();

        if (userTx != null) {
            userTx.begin();
            return;
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
	    if (inv != null) {
            checkUserTransactionMethodAccess(inv);
	    }

	    transactionManager.begin();
            /**
	    if ( transactionTimeout > 0 ) 
	        transactionManager.begin(transactionTimeout);
	    else
	        transactionManager.begin();
            **/

	    try {
                if (inv != null) {
	            if ( inv.getInvocationType() == ComponentInvocation.EJB_INVOCATION )
		        ((Container)inv.container).doAfterBegin(inv);

                    inv.setTransaction(transactionManager.getTransaction());
                    transactionManager.enlistComponentResources();
                }
        } catch ( RemoteException ex ) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_utx_begin", ex);
            SystemException sysEx = new SystemException(ex.getMessage());
            sysEx.initCause(ex);
            throw sysEx;
        }
    }

    public void commit() throws RollbackException,
        HeuristicMixedException, HeuristicRollbackException, SecurityException,
        IllegalStateException, SystemException
    {
        if (!initialized) init();

        if (userTx != null) {
            userTx.commit();
            return;
        }

	    ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
            checkUserTransactionMethodAccess(inv);
        }


        try {
            transactionManager.delistComponentResources(false);  // TMSUCCESS
            transactionManager.commit();
        } catch ( RemoteException ex ) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_utx_commit", ex);
            throw new SystemException();
        } finally {
            if (inv != null)
                inv.setTransaction(null);
        }
    }

    public void rollback() throws IllegalStateException, SecurityException,
        SystemException
    {
        if (!initialized) init();

        if (userTx != null) {
            userTx.rollback();
            return;
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
                checkUserTransactionMethodAccess(inv);
        }


        try {
            transactionManager.delistComponentResources(false); // TMSUCCESS
            transactionManager.rollback();
        } catch ( RemoteException ex ) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_utx_rollback", ex);
            throw new SystemException();
        } finally {
            if (inv !=  null)
                inv.setTransaction(null);
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        if (!initialized) init();

        if (userTx != null) {
            userTx.setRollbackOnly();
            return;
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
                checkUserTransactionMethodAccess(inv);
        }

        transactionManager.setRollbackOnly();
    }

    public int getStatus() throws SystemException
    {
        if (!initialized) init();

        if (userTx != null) {
            return userTx.getStatus();
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
                checkUserTransactionMethodAccess(inv);
        }

        return transactionManager.getStatus();
    }

    public void setTransactionTimeout(int seconds) throws SystemException
    {
        if (!initialized) init();

        if (userTx != null) {
            userTx.setTransactionTimeout(seconds);
            return;
        }

	ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
                checkUserTransactionMethodAccess(inv);
        }
        
        if (seconds < 0) seconds = 0;
        // transactionTimeout = seconds;
        transactionManager.setTransactionTimeout(seconds);
    }
}
