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
package com.sun.enterprise.transaction.jts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.transaction.*;
import javax.transaction.xa.*;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkException;

import com.sun.jts.jta.TransactionManagerImpl;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionalResource;

import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;
import com.sun.enterprise.transaction.JavaEETransactionImpl;

import com.sun.enterprise.util.i18n.StringManager;

import org.jvnet.hk2.annotations.Service;

/**
 ** Implementation of JavaEETransactionManagerDelegate that supports XA
 * transactions with JTS.
 *
 * @author Marina Vatkina
 */
@Service
public class JavaEETransactionManagerJTSDelegate 
            implements JavaEETransactionManagerDelegate {

    // an implementation of the JavaEETransactionManager that calls
    // this object.
    private JavaEETransactionManagerSimplified javaEETM;

    // an implementation of the JTA TransactionManager provided by JTS.
    private TransactionManager tm;

    private Hashtable globalTransactions;

    private Logger _logger;

    // Sting Manager for Localization
    private static StringManager sm
           = StringManager.getManager(JavaEETransactionManagerSimplified.class);

    private boolean lao = true;

    public JavaEETransactionManagerJTSDelegate() {
        globalTransactions = new Hashtable();
    }

    public boolean useLAO() {
         return lao;
    }

    public void setUseLAO(boolean b) {
        lao = b;
    }

    /** XXX Throw an exception if called ??? XXX
     *  it might be a JTS imported global tx or an error
     */
    public void commitDistributedTransaction() throws 
            RollbackException, HeuristicMixedException, 
            HeuristicRollbackException, SecurityException, 
            IllegalStateException, SystemException {

        if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE,"TM: commit");
        validateTransactionManager();
        Object obj = tm.getTransaction(); // monitoring object
        
        if (javaEETM.isInvocationStackEmpty()) {
            try{
                tm.commit();
                javaEETM.monitorTxCompleted0(obj, true);
            }catch(RollbackException e){
                javaEETM.monitorTxCompleted0(obj, false);
                throw e;
            }catch(HeuristicRollbackException e){
                javaEETM.monitorTxCompleted0(obj, false);
                throw e;
            }catch(HeuristicMixedException e){
                javaEETM.monitorTxCompleted0(obj, true);
                throw e;
            }
        } else {
            try {
                javaEETM.setTransactionCompeting(true);
                tm.commit();
                javaEETM.monitorTxCompleted0(obj, true);
/**
            } catch (InvocationException ex) {
                assert false;
**/
            }catch(RollbackException e){
                javaEETM.monitorTxCompleted0(obj, false);
                throw e;
            }catch(HeuristicRollbackException e){
                javaEETM.monitorTxCompleted0(obj, false);
                throw e;
            }catch(HeuristicMixedException e){
                javaEETM.monitorTxCompleted0(obj, true);
                throw e;
            } finally {
                javaEETM.setTransactionCompeting(false);
            }
        }
    }

    /** XXX Throw an exception if called ??? XXX
     *  it might be a JTS imported global tx or an error
     */
    public void rollbackDistributedTransaction() throws IllegalStateException, 
            SecurityException, SystemException {

        if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE,"TM: rollback");
        validateTransactionManager();

        Object obj = tm.getTransaction(); // monitoring object
        
        if (javaEETM.isInvocationStackEmpty()) {
            tm.rollback();
        } else {
            try {
                javaEETM.setTransactionCompeting(true);
                tm.rollback();
/**
            } catch (InvocationException ex) {
                assert false;
**/
            } finally {
                javaEETM.setTransactionCompeting(false);
            }
        }

        javaEETM.monitorTxCompleted0(obj, false);
    }

    public int getStatus() throws SystemException {
        JavaEETransaction tx = javaEETM.getCurrentTransaction();
        if ( tx != null && tx.isLocalTx())
            return tx.getStatus();
        else if (tm != null) 
            return tm.getStatus();
        else
            return javax.transaction.Status.STATUS_NO_TRANSACTION;
    }

    public Transaction getTransaction() 
            throws SystemException {
        JavaEETransaction tx = javaEETM.getCurrentTransaction();
        if ( tx != null )
            return tx;

        // Check for a JTS imported tx
        Transaction jtsTx = null;
        if (tm != null) {
            jtsTx = tm.getTransaction();
        }

        if ( jtsTx == null )
            return null;
        else {
            // check if this JTS Transaction was previously active
            // in this JVM (possible for distributed loopbacks).
            tx = (JavaEETransaction)globalTransactions.get(jtsTx);
            if ( tx == null ) {
                tx = javaEETM.createImportedTransaction(jtsTx);
                globalTransactions.put(jtsTx, tx);
            }
            javaEETM.setCurrentTransaction(tx); // associate tx with thread
            return tx;
        }
    }

    public boolean enlistDistributedNonXAResource(Transaction tx, TransactionalResource h)
           throws RollbackException, IllegalStateException, SystemException {
        if(useLAO()) {
            if (javaEETM.resourceEnlistable(h)) {
                XAResource res = h.getXAResource();
                boolean result = tx.enlistResource(res);
                if (!h.isEnlisted())
                    h.enlistedInTransaction(tx);
                    return result;
                } else {
                    return true;
            }
        } else {
            throw new IllegalStateException(
                    sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
        }
    }

    public boolean enlistLAOResource(Transaction tran, TransactionalResource h)
           throws RollbackException, IllegalStateException, SystemException {

        JavaEETransactionImpl tx = (JavaEETransactionImpl)tran;
        startJTSTx(tx);

        //If transaction conatains a NonXA and no LAO, convert the existing
        //Non XA to LAO
        if(useLAO()) {
            if(h != null && (tx.getLAOResource() == null) ) {
                tx.setLAOResource(h);
                if (h.isTransactional()) {
                    XAResource res = h.getXAResource();
                    return tran.enlistResource(res);
                }
            }
        }
        return true;

    }

    public void setRollbackOnlyDistributedTransaction()
            throws IllegalStateException, SystemException {
        if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE,"TM: setRollbackOnly");

        validateTransactionManager();
        tm.setRollbackOnly();
    }

    public Transaction suspend(JavaEETransaction tx) throws SystemException {
        if ( tx != null ) {
            if ( !tx.isLocalTx() )
                suspendInternal();

            javaEETM.setCurrentTransaction(null);
            return tx;
        }
        else {
            return suspendInternal(); // probably a JTS imported tx
        }
    }

    public void resume(Transaction tx)
        throws InvalidTransactionException, IllegalStateException,
        SystemException {
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"TM: resume");

        tm.resume(tx);
    }

    public void removeTransaction(Transaction tx) {
        globalTransactions.remove(tx);
    }

    public int getOrder() {
        return 3;
    }

    public void setTransactionManager(JavaEETransactionManager tm) {
        javaEETM = (JavaEETransactionManagerSimplified)tm;
        _logger = javaEETM.getLogger();
    }

    public void startJTSTx(JavaEETransaction tx) 
            throws RollbackException, IllegalStateException, SystemException {
        if (tm == null)
            tm = TransactionManagerImpl.getTransactionManagerImpl();

        javaEETM.startJTSTx(tx);
        globalTransactions.put(tm.getTransaction(), tx);
    }

    public void recover(XAResource[] resourceList) {
        ((TransactionManagerImpl)tm).recover(
                Collections.enumeration(Arrays.asList(resourceList)));
    }

    public void release(Xid xid) throws WorkException {
        ((TransactionManagerImpl) tm).release(xid);
    }

    public void recreate(Xid xid, long timeout) throws WorkException {
        ((TransactionManagerImpl) tm).recreate(xid, timeout);
    }

    public XATerminator getXATerminator() {
        return ((TransactionManagerImpl) tm).getXATerminator();
    }

    private Transaction suspendInternal() throws SystemException {
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"TM: suspend");
        validateTransactionManager();
        return tm.suspend();
    }

    private void validateTransactionManager() throws IllegalStateException {
        if (tm == null) {
            throw new IllegalStateException
            (sm.getString("enterprise_distributedtx.transaction_notactive"));
        }
    }

    public boolean supportsRecovery() {
        return true;
    }
}
