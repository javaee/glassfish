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
import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.CosTransactions.DefaultTransactionService;

import org.omg.CORBA.CompletionStatus;
import com.sun.corba.ee.spi.costransactions.TransactionService;
import com.sun.corba.ee.impl.logging.POASystemException;

import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.api.XAResourceWrapper;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionalResource;

import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;
import com.sun.enterprise.transaction.JavaEETransactionImpl;

import com.sun.enterprise.util.i18n.StringManager;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.annotations.Inject;

/**
 ** Implementation of JavaEETransactionManagerDelegate that supports XA
 * transactions with JTS.
 *
 * @author Marina Vatkina
 */
@Service
public class JavaEETransactionManagerJTSDelegate 
            implements JavaEETransactionManagerDelegate, PostConstruct {

    // an implementation of the JavaEETransactionManager that calls
    // this object.
    // @Inject 
    private JavaEETransactionManager javaEETM;

    @Inject 
    private com.sun.enterprise.config.serverbeans.TransactionService txnService;

    // an implementation of the JTA TransactionManager provided by JTS.
    private TransactionManager tm;

    private Hashtable globalTransactions;
    private Hashtable<String, XAResourceWrapper> xaresourcewrappers =
            new Hashtable<String, XAResourceWrapper>();

    private Logger _logger;

    // Sting Manager for Localization
    private static StringManager sm
           = StringManager.getManager(JavaEETransactionManagerSimplified.class);

    private boolean lao = true;

    public JavaEETransactionManagerJTSDelegate() {
        globalTransactions = new Hashtable();
    }

    public void postConstruct() {
        if (javaEETM != null) {
            // JavaEETransactionManager has been already initialized
            javaEETM.setDelegate(this);
        }

        initTransactionProperties();
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

        JavaEETransactionManagerSimplified javaEETMS = 
                (JavaEETransactionManagerSimplified)javaEETM;
        
        if (javaEETMS.isInvocationStackEmpty()) {
            try{
                tm.commit();
                javaEETMS.monitorTxCompleted0(obj, true);
            }catch(RollbackException e){
                javaEETMS.monitorTxCompleted0(obj, false);
                throw e;
            }catch(HeuristicRollbackException e){
                javaEETMS.monitorTxCompleted0(obj, false);
                throw e;
            }catch(HeuristicMixedException e){
                javaEETMS.monitorTxCompleted0(obj, true);
                throw e;
            }
        } else {
            try {
                javaEETMS.setTransactionCompeting(true);
                tm.commit();
                javaEETMS.monitorTxCompleted0(obj, true);
/**
            } catch (InvocationException ex) {
                assert false;
**/
            }catch(RollbackException e){
                javaEETMS.monitorTxCompleted0(obj, false);
                throw e;
            }catch(HeuristicRollbackException e){
                javaEETMS.monitorTxCompleted0(obj, false);
                throw e;
            }catch(HeuristicMixedException e){
                javaEETMS.monitorTxCompleted0(obj, true);
                throw e;
            } finally {
                javaEETMS.setTransactionCompeting(false);
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
        
        JavaEETransactionManagerSimplified javaEETMS = 
                (JavaEETransactionManagerSimplified)javaEETM;
        
        if (javaEETMS.isInvocationStackEmpty()) {
            tm.rollback();
        } else {
            try {
                javaEETMS.setTransactionCompeting(true);
                tm.rollback();
/**
            } catch (InvocationException ex) {
                assert false;
**/
            } finally {
                javaEETMS.setTransactionCompeting(false);
            }
        }

        javaEETMS.monitorTxCompleted0(obj, false);
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
                tx = ((JavaEETransactionManagerSimplified)javaEETM).createImportedTransaction(jtsTx);
                globalTransactions.put(jtsTx, tx);
            }
            javaEETM.setCurrentTransaction(tx); // associate tx with thread
            return tx;
        }
    }

    public boolean enlistDistributedNonXAResource(Transaction tx, TransactionalResource h)
           throws RollbackException, IllegalStateException, SystemException {
        if(useLAO()) {
            if (((JavaEETransactionManagerSimplified)javaEETM).resourceEnlistable(h)) {
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
        javaEETM = tm;
        _logger = ((JavaEETransactionManagerSimplified)javaEETM).getLogger();
    }

    public void startJTSTx(JavaEETransaction tx) 
            throws RollbackException, IllegalStateException, SystemException {
        if (tm == null)
            tm = TransactionManagerImpl.getTransactionManagerImpl();

        ((JavaEETransactionManagerSimplified)javaEETM).startJTSTx(tx);
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

    public XAResourceWrapper getXAResourceWrapper(String clName) {
        XAResourceWrapper rc = xaresourcewrappers.get(clName);

        if (rc != null)
            return rc.getInstance();

        return null;
    }

    public void handlePropertyUpdate(String name, Object value) {
        if (name.equals(ServerTags.KEYPOINT_INTERVAL)) {
            Configuration.setKeypointTrigger(Integer.parseInt((String)value,10));

        } else if (name.equals(ServerTags.RETRY_TIMEOUT_IN_SECONDS)) {
            Configuration.setCommitRetryVar((String)value);

        }
    }

    public void initTransactionProperties() {
        if (txnService != null) {
            String value = txnService.getPropertyValue("use-last-agent-optimization");
            if (value != null && "false".equals(value)) {
                setUseLAO(false);
                if (_logger.isLoggable(Level.FINE))
                    _logger.log(Level.FINE,"TM: LAO is disabled");
            }
    
            value = txnService.getPropertyValue("oracle-xa-recovery-workaround");
            if (value == null || "true".equals(value)) {
                xaresourcewrappers.put(
                    "oracle.jdbc.xa.client.OracleXADataSource",
                    new OracleXAResource());
            }
    
            value = txnService.getPropertyValue("sybase-xa-recovery-workaround");
            if (value != null && "true".equals(value)) {
                xaresourcewrappers.put(
                    "com.sybase.jdbc2.jdbc.SybXADataSource",
                    new SybaseXAResource());
            }

            // XXX ??? Properties from EjbServiceGroup.initJTSProperties ??? XXX
        }

        // XXX MOVE TO A GENERIC LOCATION? XXX

        try {
            TransactionService jts = new DefaultTransactionService();
/** XXX ???
            jts.identify_ORB(theORB, tsIdent, jtsProperties ) ;
            jtsInterceptor.setTSIdentification(tsIdent);
            org.omg.CosTransactions.Current transactionCurrent =
                  jts.get_current();
    
            theORB.getLocalResolver().register(
                   ORBConstants.TRANSACTION_CURRENT_NAME,
                   new Constant(transactionCurrent));
    
            // the JTS PI use this to call the proprietary hooks
            theORB.getLocalResolver().register(
                   "TSIdentification", new Constant(tsIdent));
                    txServiceInitialized = true;
** XXX ??? **/
        } catch (Exception ex) {
            throw new org.omg.CORBA.INITIALIZE(
                   "JTS Exception: "+ex, POASystemException.JTS_INIT_ERROR, 
                   CompletionStatus.COMPLETED_MAYBE);
        }

    }
}
