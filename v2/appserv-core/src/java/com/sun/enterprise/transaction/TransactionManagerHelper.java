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
package com.sun.enterprise.transaction;

import javax.transaction.*;
import javax.resource.spi.XATerminator;
import com.sun.enterprise.*;
import javax.transaction.xa.Xid;
import com.sun.enterprise.distributedtx.J2EETransactionManagerOpt;
import com.sun.enterprise.distributedtx.J2EETransaction;
import com.sun.enterprise.distributedtx.UserTransactionImpl;


/**
* This class is wrapper for the actual transaction manager implementation.
* JNDI lookup name "java:appserver/TransactionManager"
* see the com/sun/enterprise/naming/java/javaURLContext.java
**/

public class TransactionManagerHelper implements TransactionManager, TransactionImport {

    public void begin() throws NotSupportedException, SystemException {
	getTransactionManagerImpl().begin();
    }

    
    public void commit() throws RollbackException,
	HeuristicMixedException, HeuristicRollbackException, SecurityException,
	IllegalStateException, SystemException {
	getTransactionManagerImpl().commit();
    }

    public int getStatus() throws SystemException {
	return getTransactionManagerImpl().getStatus();
    }

    public Transaction getTransaction() throws SystemException {
	return getTransactionManagerImpl().getTransaction();
    }

    
    public void resume(Transaction tobj)
            throws InvalidTransactionException, IllegalStateException,
            SystemException {
	getTransactionManagerImpl().resume(tobj);
    }

    
    public void rollback() throws IllegalStateException, SecurityException,
                            SystemException {
	getTransactionManagerImpl().rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
	getTransactionManagerImpl().setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
	getTransactionManagerImpl().setTransactionTimeout(seconds);
    }

    public Transaction suspend() throws SystemException {
	return getTransactionManagerImpl().suspend();
    }

    public static TransactionManager getTransactionManager() {
	return tmHelper;
    }
    
    public void recreate(Xid xid, long timeout) {
        final J2EETransactionManager tm = getTransactionManagerImpl();
        
        try {
            tm.recreate(xid, timeout);
        } catch (javax.resource.spi.work.WorkException ex) {
            throw new IllegalStateException(ex);
        }
        servletPreInvokeTx();
    }

    public void release(Xid xid) {
        IllegalStateException rethrow = null;
        final J2EETransactionManager tm = getTransactionManagerImpl();
     
        servletPostInvokeTx(false);
        try {
            tm.release(xid);    
        } catch (javax.resource.spi.work.WorkException ex) {
            throw new IllegalStateException(ex);
        }  finally { 
            if (tm instanceof J2EETransactionManagerOpt) {
                ((J2EETransactionManagerOpt) tm).clearThreadTx();
            }
            if (rethrow != null) {
                throw rethrow;
            }
        } 
    }
    
    public XATerminator getXATerminator() {
        return getTransactionManagerImpl().getXATerminator();
    }



    
    /**
     * PreInvoke Transaction configuration for Servlet Container.
     * BaseContainer.preInvokeTx() handles all this for CMT EJB.
     *
     * Compensate that J2EEInstanceListener.handleBeforeEvent(
     * BEFORE_SERVICE_EVENT)
     * gets called before WSIT WSTX Service pipe associates a JTA txn with 
     * incoming thread.
     *
     * Precondition: assumes JTA transaction already associated with current 
     * thread.
     */
    public void servletPreInvokeTx() {
        final ComponentInvocation inv = 
		Switch.getSwitch().getInvocationManager().getCurrentInvocation();
        if (inv != null && 
            inv.getInvocationType() == ComponentInvocation.SERVLET_INVOCATION){
            try { 
                // Required side effect: note that 
                // enlistComponentResources calls
                // ComponentInvocation.setTransaction(currentJTATxn).
                // If this is not correctly set, managed XAResource connections
                // are not auto enlisted when they are created.
                getTransactionManagerImpl().enlistComponentResources();
            } catch (java.rmi.RemoteException re) {
                throw new IllegalStateException(re);
            }
        }
    }    
    
    /**
     * PostInvoke Transaction configuration for Servlet Container.
     * BaseContainer.preInvokeTx() handles all this for CMT EJB.
     *
     * Precondition: assumed called prior to current transcation being 
     * suspended or released.
     * 
     * @param suspend indicate whether the delisting is due to suspension or 
     * transaction completion(commmit/rollback)
     */
    public void servletPostInvokeTx(boolean suspend) {
        final ComponentInvocation inv = 
	    Switch.getSwitch().getInvocationManager().getCurrentInvocation();
        if (inv != null && inv.getInvocationType() == 
			ComponentInvocation.SERVLET_INVOCATION) {
            try {
                getTransactionManagerImpl().delistComponentResources(suspend);
            } catch (java.rmi.RemoteException re) {
                throw new IllegalStateException(re);
            } finally {   
		inv.setTransaction(null);
	    }
        }
    }
    
     /**
     * Return duration before current transaction would timeout.
     *
     * @return Returns the duration in seconds before current transaction would
     *         timeout.
     *         Returns zero if transaction has no timeout set and returns 
     *         negative value if transaction already timed out.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     */
    public int getTransactionRemainingTimeout() throws SystemException {
        int timeout = 0;
        Transaction txn = getTransaction(); 
        if (txn == null) {
            throw new IllegalStateException("no current transaction");
        } else if (txn instanceof J2EETransaction) {
            timeout = ((J2EETransaction)txn).getRemainingTimeout();
        }
        return timeout;
    }

    private J2EETransactionManager getTransactionManagerImpl() {
        J2EETransactionManager tm = Switch.getSwitch().getTransactionManager();
	if (tm != null)
            return tm;
	new UserTransactionImpl(); // Hack to make clients work using this class
	tm = Switch.getSwitch().getTransactionManager();
	return tm;
    }

    private static TransactionManagerHelper tmHelper = new TransactionManagerHelper();

}


