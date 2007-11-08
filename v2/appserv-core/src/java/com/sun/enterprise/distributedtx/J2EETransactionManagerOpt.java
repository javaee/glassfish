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

import java.util.*;
import javax.transaction.*;
import javax.transaction.xa.*;

import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.resource.*;
import com.sun.enterprise.util.i18n.StringManager;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742



/**
 * A wrapper over J2EETransactionManagerImpl that provides optimized local
 * transaction support when a transaction uses zero/one non-XA resource,
 * and delegates to J2EETransactionManagerImpl (i.e. JTS) otherwise.
 *
 * @author Tony Ng
 */
public final class J2EETransactionManagerOpt
	     extends J2EETransactionManagerImpl
{

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.JTA_LOGGER);
    // END OF IASRI 4660742
	// Sting Manager for Localization
	private static StringManager sm = StringManager.getManager(J2EETransactionManagerOpt.class);
    // Note: this is not inheritable because we dont want transactions
    // to be inherited by child threads.
    private ThreadLocal transactions;
    private ThreadLocal localCallCounter;
    private Hashtable globalTransactions;
    // START IASRI 4662745
    private static com.sun.jts.CosTransactions.RWLock freezeLock = new com.sun.jts.CosTransactions.RWLock();
    // END IASRI 4662745

    public J2EETransactionManagerOpt() {
        super();
	transactions = new ThreadLocal();
	localCallCounter = new ThreadLocal();
	globalTransactions = new Hashtable();
    }

    public void clearThreadTx() {
	transactions.set(null);
    }

/****************************************************************************/
/** Implementations of J2EETransactionManager APIs **************************/
/****************************************************************************/

    public boolean enlistResource(Transaction tran, ResourceHandle h)
        throws RollbackException, IllegalStateException, SystemException
    {
        if ( !h.isTransactional() )
	    return true;

        //If LazyEnlistment is suspended, do not enlist resource.
        if(h.isEnlistmentSuspended()){
            return false;
        }

	if ( !(tran instanceof J2EETransaction) )
	    return super.enlistResource(tran, h);

	J2EETransaction tx = (J2EETransaction)tran;

      if(_logger.isLoggable(Level.FINE)) {
	       _logger.log(Level.FINE,"\n\nIn J2EETransactionManagerOpt.enlistResource, h=" +h+" h.xares="+h.getXAResource()+" h.alloc="
			   +h.getResourceAllocator()+" tx="+tx);
      }

    if ( (tx.getNonXAResource()!=null) && (!useLAO || (useLAO && !h.supportsXA()))) {
	    boolean isSameRM=false;
	   	try {
		    isSameRM = h.getXAResource().isSameRM(tx.getNonXAResource().getXAResource());
	   	} catch ( XAException xex ) {
		    throw new SystemException(sm.getString("enterprise_distributedtx.samerm_excep",xex));
	   	} catch ( Exception ex ) {
		    throw new SystemException(sm.getString("enterprise_distributedtx.samerm_excep",ex));
	   	}
	   	if ( !isSameRM ) {
			throw new IllegalStateException(sm.getString("enterprise_distributedtx.already_has_nonxa"));
	   	}
	}

    if (monitoringEnabled) {
        tx.addResourceName(h.getResourceSpec().getResourceId());
    }

	if ( h.supportsXA() ) {
	    if ( tx.isLocalTx() ) {
		startJTSTx(tx);
		//If transaction conatains a NonXA and no LAO, convert the existing
        //Non XA to LAO
        if(useLAO) {
		    if(tx.getNonXAResource()!=null && (tx.getLAOResource()==null) ) {
			    tx.setLAOResource(tx.getNonXAResource());
			    super.enlistLAOResource(tx, tx.getNonXAResource());
		    }
        }
	    }
	    return super.enlistResource(tx, h);
	}
	else { // non-XA resource
            if (tx.isImportedTransaction())
                throw new IllegalStateException(sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
            if (tx.getNonXAResource() == null) {
	        tx.setNonXAResource(h);
            }
	    if ( tx.isLocalTx() ) {
		// notify resource that it is being used for tx,
		// e.g. this allows the correct physical connection to be
		// swapped in for the logical connection.
		// The flags parameter can be 0 because the flags are not
		// used by the XAResource implementation for non-XA resources.
		try {
		    h.getXAResource().start(tx.getLocalXid(), 0);
		} catch ( XAException ex ) {
		    throw new RuntimeException(sm.getString("enterprise_distributedtx.xaresource_start_excep"),ex);
		}

   		poolmgr.resourceEnlisted(tx, h);
		return true;
	    }
	    else {
        if(useLAO) {
		    return super.enlistResource(tx, h);
        }
        else {
            throw new IllegalStateException(sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
        }
	    }
	}
    }

    void startJTSTx(J2EETransaction tx)
        throws RollbackException, IllegalStateException, SystemException
    {
	J2EETransactionManagerImpl.createJTSTransactionManager();
        try {
            if (tx.isAssociatedTimeout()) {
                // calculate the timeout for the transaction, this is required as the local tx 
                // is getting converted to a global transaction
                int timeout = tx.cancelTimerTask();
                int newtimeout = (int) ((System.currentTimeMillis() - tx.getStartTime()) / 1000);
                newtimeout = (timeout -   newtimeout);
                super.begin(newtimeout);
            }
            else {
                super.begin();
            }
            // START IASRI 4662745
            // The local Transaction was promoted to global Transaction
            if (tx!=null && monitoringEnabled){
                if(activeTransactions.remove(tx)){
                    m_transInFlight--;
                }
            }
            // END IASRI 4662745
	} catch ( NotSupportedException ex ) {
	    throw new RuntimeException(sm.getString("enterprise_distributedtx.lazy_transaction_notstarted"),ex);
	}
	Transaction jtsTx = super.getTransaction();
	tx.setJTSTx(jtsTx);
	jtsTx.registerSynchronization(new JTSSynchronization(jtsTx, this));
	globalTransactions.put(jtsTx, tx);
    }

    public boolean delistResource(Transaction tran, ResourceHandle h, int flag)
        throws IllegalStateException, SystemException
    {
        if (!h.isTransactional()) return true;
	if ( !(tran instanceof J2EETransaction) )
	    return super.delistResource(tran, h, flag);

	J2EETransaction tx = (J2EETransaction)tran;
	if ( tx.isLocalTx() ) {
	    // dissociate resource from tx
	    try {
		h.getXAResource().end(tx.getLocalXid(), flag);
	    } catch ( XAException ex ) {
		throw new RuntimeException(sm.getString("enterprise_distributedtx.xaresource_end_excep", ex));
	    }
	    return true;
	}
	else
	    return super.delistResource(tx, h, flag);
    }


    /**
     * Called from the CORBA Interceptors on the server-side when
     * the server is replying to the client (local + remote client).
     * Check if there is an active transaction and remove it from TLS.
     */
    public void checkTransactionImport() {
	// First check if this is a local call
	int[] count = (int[])localCallCounter.get();
	if ( count != null && count[0] > 0 ) {
	    count[0]--;
	    return;
	}
	else {
	    // A remote call, clear TLS so that if this thread is reused
	    // later, the current tx doesnt hang around.
	    clearThreadTx();
	}
    }

    /**
     * Called from the CORBA Interceptors on the client-side when
     * a client makes a call to a remote object (not in the same JVM).
     * Check if there is an active, exportable transaction.
     * @exception RuntimeException if the transaction is not exportable
     */
    public void checkTransactionExport(boolean isLocal) {

	if ( isLocal ) {
	    // Put a counter in TLS indicating this is a local call.
	    // Use int[1] as a mutable java.lang.Integer!
	    int[] count = (int[])localCallCounter.get();
	    if ( count == null ) {
		count = new int[1];
		localCallCounter.set(count);
	    }
	    count[0]++;
	    return;
	}

    	J2EETransaction tx = (J2EETransaction)transactions.get();
	if ( tx == null )
	    return;

	if ( !tx.isLocalTx() ) // a JTS tx, can be exported
	    return;

	// Check if a local tx with non-XA resource is being exported.
	// XXX what if this is a call on a non-transactional remote object ?
	if ( tx.getNonXAResource() != null )
	    throw new RuntimeException(sm.getString("enterprise_distributedtx.cannot_export_transaction_having_nonxa"));

	// If we came here, it means we have a local tx with no registered
	// resources, so start a JTS tx which can be exported.
	try {
	    startJTSTx(tx);
	} catch ( RollbackException rlex ) {
	    throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"),rlex);
	} catch ( IllegalStateException isex ) {
	    throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"),isex);
	} catch ( SystemException ex ) {
	    throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"),ex);
	} catch ( Exception excep ) {
	    throw new RuntimeException(sm.getString("enterprise_distributedtx.unable_tostart_JTSTransaction"),excep);
	}
    }


/****************************************************************************/
/** Implementations of JTA TransactionManager APIs **************************/
/****************************************************************************/

   // Implementation of begin() is moved to begin(int timeout) 
   public void begin() throws NotSupportedException, SystemException {
	begin(getEffectiveTimeout());
   }

   /**
    * This method is introduced as part of implementing the local transaction timeout 
    * capability. Implementation of begin() moved here. Previpusly there is no timeout
    * infrastructure for local txns, so when ever a timeout required for local txn, it
    * uses the globaltxn timeout infrastructure by doing an XA simulation.   
    **/
   public void begin(int timeout) throws NotSupportedException, SystemException {
	// Check if tx already exists
	if ( transactions.get() != null )
	    throw new NotSupportedException(sm.getString("enterprise_distributedtx.notsupported_nested_transaction"));

	// Check if JTS tx exists, without starting JTS tx.
	// This is needed in case the JTS tx was imported from a client.
	if ( super.getStatus() != Status.STATUS_NO_TRANSACTION )
	    throw new NotSupportedException(sm.getString("enterprise_distributedtx.notsupported_nested_transaction"));
            // START IASRI 4662745
        boolean acquiredlock = false;
        if(monitoringEnabled){
             freezeLock.acquireReadLock();
             acquiredlock = true;
        }
        try{
            J2EETransaction tx = null;
            if (timeout > 0)
                tx = new J2EETransaction(timeout);
            else
                tx = new J2EETransaction();
            transactions.set(tx);
            if (monitoringEnabled) {
            	activeTransactions.addElement(tx);
                m_transInFlight++;
                ComponentInvocation inv = invMgr.getCurrentInvocation();
                if (inv != null && inv.getInstance() != null) {
                    tx.setComponentName(inv.getInstance().getClass().getName());
                }
            }
        }finally{
        	if(acquiredlock){
            	freezeLock.releaseReadLock();
            }
        }
            // START IASRI 4662745
    }

    public void commit() throws RollbackException,
	HeuristicMixedException, HeuristicRollbackException, SecurityException,
	IllegalStateException, SystemException {

	try {
            J2EETransaction tx = (J2EETransaction)transactions.get();
	    if ( tx != null && tx.isLocalTx()) {
                // START IASRI 4662745
                Object obj = null;
                boolean acquiredlock = false;
                if(monitoringEnabled){
                    obj = tx;
                }
                try{
                    if(monitoringEnabled){
                        freezeLock.acquireReadLock();
                        acquiredlock = true;
                    }
                    tx.commit(); // commit local tx
                    if (monitoringEnabled){
                        monitorTxCompleted(obj, true);
                    }
                }catch(RollbackException e){
                    if (monitoringEnabled){
                        monitorTxCompleted(obj, false);
                    }
                    throw e;
                }catch(HeuristicRollbackException e){
                    if (monitoringEnabled){
                        monitorTxCompleted(obj, false);
                    }
                    throw e;
                }catch(HeuristicMixedException e){
                    if (monitoringEnabled){
                        monitorTxCompleted(obj, true);
                    }
                    throw e;
                }finally{
                    if(acquiredlock){
                        freezeLock.releaseReadLock();
                    }
                }
            }
	    else  {
		super.commit(); // it might be a JTS imported global tx or an error
            }
	} finally {
	    transactions.set(null); // clear current thread's tx
	}
        // END IASRI 4662745
    }

    public void rollback() throws IllegalStateException, SecurityException,
                SystemException {
        // START IASRI 4662745
        boolean acquiredlock=false;
	try {
	    J2EETransaction tx = (J2EETransaction)transactions.get();
	    if ( tx != null && tx.isLocalTx()) {
                Object obj = null;
                if(monitoringEnabled){
                    obj = tx;
                }
                if(monitoringEnabled){
                    freezeLock.acquireReadLock();
                    acquiredlock = true;
                }
		tx.rollback(); // rollback local tx
                if(monitoringEnabled){
                    monitorTxCompleted(obj, false);
                }
            }
	    else  {
		super.rollback(); // a JTS imported global tx or an error
            }
	} finally {
	    transactions.set(null); // clear current thread's tx
            if(acquiredlock){
                freezeLock.releaseReadLock();
            }
	}
        // END IASRI 4662745
    }


    public int getStatus() throws SystemException {
	J2EETransaction tx = (J2EETransaction)transactions.get();
	if ( tx != null && tx.isLocalTx())
	    return tx.getStatus();
	else
	    return super.getStatus();
    }

    public Transaction getTransaction() throws SystemException {
	J2EETransaction tx = (J2EETransaction)transactions.get();
	if ( tx != null )
	    return tx;
	else { // maybe a JTS imported tx
	    Transaction jtsTx = super.getTransaction();
	    if ( jtsTx == null )
		return null;
	    else {
		// check if this JTS Transaction was previously active
		// in this JVM (possible for distributed loopbacks).
		tx = (J2EETransaction)globalTransactions.get(jtsTx);
		if ( tx == null ) {
		    tx = new J2EETransaction(jtsTx);
                    tx.setImportedTransaction();
		    try {
			jtsTx.registerSynchronization(
					new JTSSynchronization(jtsTx, this));
		    } catch ( RollbackException rlex ) {
				throw new SystemException(rlex.toString());
		    } catch ( IllegalStateException isex ) {
				throw new SystemException(isex.toString());
		    } catch ( Exception ex ) {
				throw new SystemException(ex.toString());
		    } 

		    globalTransactions.put(jtsTx, tx);
		}
		transactions.set(tx); // associate tx with thread
		return tx;
	    }
	}
    }

    public void setRollbackOnly()
        throws IllegalStateException, SystemException {

	J2EETransaction tx = (J2EETransaction)transactions.get();
        // START IASRI 4662745
	if ( tx != null && tx.isLocalTx()){
            boolean acquiredlock=false;
            if(monitoringEnabled){
                freezeLock.acquireReadLock();
                acquiredlock = true;
            }
            try{
	        tx.setRollbackOnly();
            }finally{
                if(acquiredlock){
                    freezeLock.releaseReadLock();
                }
            }
        }
	else
	    super.setRollbackOnly(); // probably a JTS imported tx
        // END IASRI 4662745
    }


    public Transaction suspend() throws SystemException {
	J2EETransaction tx = (J2EETransaction)transactions.get();
	if ( tx != null ) {
	    if ( !tx.isLocalTx() )
		super.suspend();
	    transactions.set(null);
	    return tx;
	}
	else {
	    return super.suspend(); // probably a JTS imported tx
	}
    }

    public void resume(Transaction tobj)
        throws InvalidTransactionException, IllegalStateException,
            SystemException {

	J2EETransaction tx = (J2EETransaction)transactions.get();
	if ( tx != null )
	    throw new IllegalStateException(sm.getString("enterprise_distributedtx.transaction_exist_on_currentThread"));
	if ( tobj instanceof J2EETransaction ) {
	    J2EETransaction j2eeTx = (J2EETransaction)tobj;
	    if ( !j2eeTx.isLocalTx() )
		super.resume(j2eeTx.getJTSTx());

	    transactions.set(tobj);
	}
	else {
	    super.resume(tobj); // probably a JTS imported tx
	}
    }

    public boolean isTimedOut() {
        J2EETransaction tx = (J2EETransaction)transactions.get();
        if ( tx != null)
	    return tx.isTimedout();
	else
	    return false;
    }
    // START IASRI 4662745
   /*
    * Called by Admin Framework to freeze the transactions.
    */
    public synchronized void freeze(){
        super.freeze();
        if(freezeLock.isWriteLocked()){
            //multiple freezes will hang this thread, therefore just return
            return;
        }
        freezeLock.acquireWriteLock();
    }
    /*
     * Called by Admin Framework to freeze the transactions. These undoes the work done by the freeze.
     */
    public synchronized void unfreeze(){
        super.unfreeze();
        if(freezeLock.isWriteLocked()){
            freezeLock.releaseWriteLock();
        }
    }

    // END IASRI 4662745

    private class JTSSynchronization implements Synchronization {
	private Transaction jtsTx;
	private J2EETransactionManagerOpt j2eeTM;

	JTSSynchronization(Transaction jtsTx, J2EETransactionManagerOpt j2eeTM){
	    this.jtsTx = jtsTx;
	    this.j2eeTM = j2eeTM;
	}

	public void beforeCompletion() {}

	public void afterCompletion(int status) {
	    j2eeTM.globalTransactions.remove(jtsTx);
	}
    }
}

