/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * TransactionImpl.java
 *
 * Create on December 1, 2000
 */

package com.sun.org.apache.jdo.impl.pm;

import javax.transaction.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.ejb.EJBImplHelper;
import com.sun.org.apache.jdo.store.Connector;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.*;

/**
 *
 * The Transaction interface allows operations to be performed against
 * the transaction in the target Transaction object. A Transaction 
 * object is created corresponding to each PersistentManagerImpl creation.
 * The Transaction object can be used for synchronization registration, 
 * transaction completion and status query operations.
 *
 * This implementation is StoreManager independent.
 *
 * @author Marina Vatkina
 */
class TransactionImpl implements com.sun.persistence.support.Transaction {

    /**
     * Transaction status (from javax.transaction.Status).
     */
    private int            status;

    /**
     * The commit process has already begun (even though the status is still
     * STATUS_ACTIVE).  This is the first thing set during commit or rollback.
     */
    private boolean        startedCommit;

    /**
     * Registered Synchronization object.
     */
    private Object    synchronization;

    /**
     * Synchronisation object associated with this transaction instance
     */
    private Object txSync = null;
    
    /**
     * PersistenceManagerFactory associated with this transaction 
     */
    private PersistenceManagerFactoryImpl pmFactory = null;
    
    /**
     * PersistenceManager associated with this transaction (1-1)
     */
    private PersistenceManagerImpl    persistenceManager     = null;

    /**
     * Connector associated with this transaction 
     */
    private Connector    connector     = null;

    /**
     * javax.transaction.Transaction instance associated with the current
     * thread or null if there is none.
     */
    private javax.transaction.Transaction jta = null;

    /**
     * Flag that indicates how to handle objects after commit.
     * If true, at commit instances retain their values and the instances
     */
    private boolean retainValues = true;

    /**
     * Flag that indicates how to handle objects after rollback.
     * If true, at rollback instances have their values restored.
     */
    private boolean restoreValues = true;

    /**
     * Flag that indicates type of the transaction.
     * Optimistic transactions do not hold data store locks until commit time.
     */
    private boolean optimistic = true;

    /**
     * Flag that indicates if queries and navigation are allowed
     * without an active transaction
     */
    private boolean nontransactionalRead = true;    

    /**
     * Flag that indicates if write access is allowed
     * without an active transaction
     */
    private boolean nontransactionalWrite = true;

    /** values for the datasource user and user password to access
     * security connections
     */
    private String         username     = null;
    private String         password     = null;

    /**
     * Possible values of txType
     */
    protected static final int NON_MGD = 0;
    protected static final int CMT = 1;
    protected static final int BMT_UT = 2;
    protected static final int BMT_JDO = 3;

    /**
     * Flag to indicate usage mode (non-managed versus managed, and so on).
     */
    private int txType = -1;

    /**
     * Logger instance
     */
    private static final Log logger = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.impl.pm"); // NOI18N

    /**
     * I18N message handler
     */
     private final static I18NHelper msg = 
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.pm.Bundle"); // NOI18N
    
    /**
     * Constructors new instance of TransactionImpl for the corresponding
     * PersistenceManagerImpl. Username and password are provided for future
     * validation and Connector request.
     *
     * @param pm calling instance of PersistenceManagerImpl
     * @param pmf PersistenceManagerFactoryImpl associated with the instance of
     * PersistenceManagerImpl
     * @param username user name for Connector request
     * @param password user password for Connector request
     */
    TransactionImpl(PersistenceManagerImpl pm, PersistenceManagerFactoryImpl pmf,
        String username, String password) {

        status = Status.STATUS_NO_TRANSACTION;
        startedCommit = false;
        persistenceManager = pm;

        pmFactory = pmf;
        optimistic = pmFactory.getOptimistic();
        retainValues = pmFactory.getRetainValues();
        restoreValues = pmFactory.getRestoreValues();
        nontransactionalRead = pmFactory.getNontransactionalRead();
        nontransactionalWrite = pmFactory.getNontransactionalWrite();

        this.username = username;
        this.password = password;

    }

    //
    // ----- Methods from com.sun.persistence.support.Transaction interface ------
    //

    /**
     * Returns PersistenceManager associated with this transaction
     * @see com.sun.persistence.support.Transaction#getPersistenceManager()
     */
    public com.sun.persistence.support.PersistenceManager getPersistenceManager() {
        persistenceManager.assertIsOpen();
        return (com.sun.persistence.support.PersistenceManager) persistenceManager.getCurrentWrapper();
    }

    /**
     * @see com.sun.persistence.support.Transaction#isActive()
     */
    public boolean isActive() {
        persistenceManager.assertIsOpen();
        return !isTerminated();
    }

    /**
     * @see com.sun.persistence.support.Transaction#setRetainValues(boolean flag)
     */
    public void setRetainValues(boolean flag) {
        persistenceManager.assertIsOpen();

        // Cannot change flag value when transaction commit is in progress.
        if (startedCommit)
            throw new JDOUserException(msg.msg(
                "EXC_CannotSetFlag")); // NOI18N

        retainValues = flag;
    }

    /**
     * @see com.sun.persistence.support.Transaction#getRetainValues()
     */
    public boolean getRetainValues() {
        persistenceManager.assertIsOpen();
        return retainValues;
    }

    /**
     * @see com.sun.persistence.support.Transaction#setRestoreValues(boolean flag)
     */
    public void setRestoreValues(boolean flag) {
        persistenceManager.assertIsOpen();
        // Cannot change flag if transaction is active.
        if (isActive()) {
            throw new JDOUserException(msg.msg(
                "EXC_CannotSetFlagIfActive")); // NOI18N
        }
        restoreValues = flag;
    }

    /**
     * @see com.sun.persistence.support.Transaction#getRestoreValues()
     */
    public boolean getRestoreValues() {
        persistenceManager.assertIsOpen();
        return restoreValues;
    }

    /**
     * @see com.sun.persistence.support.Transaction#setNontransactionalRead(boolean flag)
     */
    public synchronized void setNontransactionalRead (boolean flag) {
        persistenceManager.assertIsOpen();

        // Cannot change flag value when transaction commit is in progress.
        if (startedCommit)
            throw new JDOUserException(msg.msg(
                "EXC_CannotSetFlag")); // NOI18N

        nontransactionalRead = flag;

    }

    /**
     * @see com.sun.persistence.support.Transaction#getNontransactionalRead()
     */
    public boolean getNontransactionalRead() {
        persistenceManager.assertIsOpen();
        return nontransactionalRead;
    }

    /**
     * @see com.sun.persistence.support.Transaction#setNontransactionalWrite(boolean flag)
     */
    public synchronized void setNontransactionalWrite (boolean flag) {
        persistenceManager.assertIsOpen();

        // Cannot change flag value when transaction commit is in progress.
        if (startedCommit)
            throw new JDOUserException(msg.msg(
                "EXC_CannotSetFlag")); // NOI18N

        nontransactionalWrite = flag;
    }

    /**
     * @see com.sun.persistence.support.Transaction#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite() {
        persistenceManager.assertIsOpen();
        return nontransactionalWrite;
    }

    /**
     * @see com.sun.persistence.support.Transaction#setOptimistic(boolean flag)
     */
    public synchronized void setOptimistic(boolean flag) {
        persistenceManager.assertIsOpen();

        if (isTerminated()) {
            optimistic = flag;
        } else {
            // Cannot change flag value inside an active transaction.
            throw new JDOUserException(msg.msg(
                "EXC_CannotChangeType")); // NOI18N
        }

        // Notify PM about Tx type change
        persistenceManager.notifyOptimistic(flag);
    }

    /**
     * @see com.sun.persistence.support.Transaction#getOptimistic()
     */
    public boolean getOptimistic() {
        persistenceManager.assertIsOpen();
        return optimistic;
    }

    /**
     * @see com.sun.persistence.support.Transaction#setSynchronization(Synchronization sync)
     */
    public void setSynchronization(Synchronization sync) {
        persistenceManager.assertIsOpen();
        if (sync == synchronization) {
            return; // the same Synchronization.

        } else if (sync != null && synchronization != null) {
            throw new JDOUserException(msg.msg(
                "EXC_SynchronizationNotNull")); // NOI18N
        }

        registerSynchronization(sync);
    }

    /**
     * @see com.sun.persistence.support.Transaction#getRetainValues()
     */
    public Synchronization getSynchronization() {
        persistenceManager.assertIsOpen();
        return (Synchronization)synchronization;
    }

    public void assertReadAllowed() {
        if ((!isActive()) && (!getNontransactionalRead())) {
            throw new JDOUserException(msg.msg(
                "EXC_NontransactionalRead"));// NOI18N
        }
    }
    
    /**
     * Begin a transaction.
     * @see com.sun.persistence.support.Transaction#begin()
     */
    public void begin() {
        // Check and set status...
        beginInternal();

       // BMT with JDO Transaction
       if (EJBImplHelper.isManaged()) {
           txType = BMT_JDO;
           try {
                TransactionManager tm = EJBImplHelper.getLocalTransactionManager();

                tm.begin();
                jta = tm.getTransaction();
                if (txSync == null)
                    txSync = new TransactionSynchronization(this);

                jta.registerSynchronization((Synchronization)txSync);
                pmFactory.registerPersistenceManager(persistenceManager, jta);

            } catch (JDOException e) {
                throw e;     // re-throw it.
            } catch (Exception e) {
                throw new JDOFatalInternalException(
                    "EXC_ErrorRegister", e); // NOI18N
            }
        } else {
            // Mark as non-managed transaction.
            txType = NON_MGD;
        }
        
    }

    /**
     * Commit the transaction represented by this Transaction object
     * @see com.sun.persistence.support.Transaction#commit()
     */
    public void commit() {
        persistenceManager.assertIsOpen();

        if (txType == CMT || txType == BMT_UT) {
            // Error - should not be called
            throw new JDOUserException(msg.msg(
                 "EXC_WrongMgdOperation", "commit")); //NOI18N
        } else if (txType == BMT_JDO) {
            // Send request to the container:
            try {
                EJBImplHelper.getLocalTransactionManager().commit();
                return;
            } catch (Exception e) { 
                throw new JDOException("", e); // NOI18N
            }
        }
 
        // Proceede with non-managed environment call.
        synchronized (this) {
            //
            // Disallow parallel transaction completion calls:
            //
            if (startedCommit) {
                throw new JDOUserException(msg.msg(
                    "EXC_TransactionCommitting")); // NOI18N
            }
        
            // This flag prevents user from making any changes to the transaction object.
            startedCommit = true;
        }

        if (debugging())
            traceCall("commit"); // NOI18N

        try {
            prepareFlush(true); // do actual beforeComplition.
            commitPrepare(); // check internal status.
            commitComplete(); // commitConnector and set status to success.

        } catch (Throwable e) {
            try {
                internalRollback();
            } catch (Exception re) {
                // Do not rethrow the rollback exception - just log it.
                if (debugging())
                    logger.debug("Exception during rollback after failed commit: " + re);
            }

            if (e instanceof JDOException) {
                throw (JDOException)e;
            }
            throw new JDOException("", e); // NOI18N
        } finally {
            internalAfterCompletion(); // do afterCompletion and cleanup.
        }
    }

    /**
     * Rollback the transaction represented by this transaction object.
     * @see com.sun.persistence.support.Transaction#rollback()
     */
    public void rollback() {
        persistenceManager.assertIsOpen();
        if (txType == CMT || txType == BMT_UT) {
            // Error - should not be called
            throw new JDOUserException(msg.msg(
                 "EXC_WrongMgdOperation", "rollback")); //NOI18N
        }

        if (debugging())
            traceCall("rollback"); // NOI18N

        synchronized (this) {
            //
            // Disallow parallel transaction completion calls:
            //
            if (startedCommit) {
                throw new JDOUserException(msg.msg(
                    "EXC_TransactionCommitting")); // NOI18N

            } else if ((status != Status.STATUS_ACTIVE) &&    
                (status != Status.STATUS_MARKED_ROLLBACK)) {

                throw new JDOUserException(msg.msg(
                    "EXC_TransactionNotActive")); // NOI18N
            }
        
            // This flag prevents user from making any changes to the transaction object.
            startedCommit = true;
        }

        try {
            internalRollback();

            if (txType == BMT_JDO) {
                // Send request to the container:
                EJBImplHelper.getLocalTransactionManager().rollback();
            }
        } catch (JDOException e) { 
            throw e;

        } catch (Exception e) { 
            throw new JDOException("", e); // NOI18N

        } finally {
            if (txType == NON_MGD) {
                // afterCompletion and cleanup in case of the managed env. had been
                // done already.
                internalAfterCompletion(); 
            }
        }
    }

    //
    // ----- Other public methods ------
    //

    /**
     * Modify the transaction object such that the only possible outcome of
     * the transaction is to roll back.
     */
    public void setRollbackOnly() {
        if (debugging())
            traceCall("setRollbackOnly"); // NOI18N

        if ((status == Status.STATUS_ROLLING_BACK)
                ||    (status == Status.STATUS_ROLLEDBACK)
                ||     (status == Status.STATUS_MARKED_ROLLBACK)) {
            //
            // Already rolled back, rollback in progress or already marked.
            //
            return;
        }

        if (txType == NON_MGD) {
            setStatus(Status.STATUS_MARKED_ROLLBACK);
        } else {
            try {
                jta.setRollbackOnly();
            } catch (Exception e) { 
                throw new JDOException("", e); // NOI18N
            }
        }

    }

    /**
     * Obtain the status of this transaction object.
     * 
     * @return The transaction status. 
     */
    public int getStatus() {
        synchronized (this) {
            return status;
        }
    }

    /**
     * Translates a javax.transaction.Status value into a string. 
     *
     * @param   status   Status object to translate.
     * @return  Printable String for a Status object.
     */
    public static String statusString(int status) {
        switch (status) {
            case Status.STATUS_ACTIVE:            return "STATUS_ACTIVE"; // NOI18N
            case Status.STATUS_MARKED_ROLLBACK:    return "STATUS_MARKED_ROLLBACK"; // NOI18N
            case Status.STATUS_PREPARED:        return "STATUS_PREPARED"; // NOI18N
            case Status.STATUS_COMMITTED:        return "STATUS_COMMITTED"; // NOI18N
            case Status.STATUS_ROLLEDBACK:        return "STATUS_ROLLEDBACK"; // NOI18N
            case Status.STATUS_UNKNOWN:            return "STATUS_UNKNOWN"; // NOI18N
            case Status.STATUS_NO_TRANSACTION:    return "STATUS_NO_TRANSACTION"; // NOI18N
            case Status.STATUS_PREPARING:        return "STATUS_PREPARING"; // NOI18N
            case Status.STATUS_COMMITTING:        return "STATUS_COMMITTING"; // NOI18N
            case Status.STATUS_ROLLING_BACK:    return "STATUS_ROLLING_BACK"; // NOI18N
            default:                            break;
        }
        return "STATUS_Invalid[" + status + "]"; // NOI18N
    }

    /**
     * Returns a string representation of this transaction object.
     *
     * @return  String describing contents of this Transaction object.
     */
    public String toString() {
        StringBuffer    s = new StringBuffer();

        s.append("  Transaction: \n   status        = " + statusString(status)+ "\n"); // NOI18N
        if (startedCommit)
            s.append("   startedCommit = true\n"); // NOI18N

        if (synchronization != null) 
            s.append("   sync          = " + synchronization.getClass().getName() + "\n"); // NOI18N
        
        return s.toString();
    }

    //
    // ----- protected methods ------
    //

    /**
     * Returns current transaction type
     * @return current transaction type as int.
     */
    protected int getTransactionType() {
        return txType;
    }

    /** Verify that username and password are equal to ones stored before
     *
     * @param username as String
     * @param password as String
     * @return true if they are equal
     */
    protected boolean verify(String username, String password) {
        if ((this.username != null && !this.username.equals(username)) ||
            (this.username == null && username != null) ||
            (this.password != null && !this.password.equals(password)) ||
            (this.password  == null && password != null)) {
            return false;
        }
        return true;
    }

    /** Returns true if commit has started
     * @return true if commit has started
     */
    protected boolean startedCommit() {
        return startedCommit;
    } 

    /**
     * Flush changes to the datastore. Performed in an active datastore 
     * transaction only.
     */
    protected void internalFlush() {
        if (status != Status.STATUS_ACTIVE) { 
            throw new JDOUserException(msg.msg(
                "EXC_TransactionNotActive")); // NOI18N
        }

        prepareFlush(false); // prepare the flush.
    }

    /**
     * Begin a transaction in a managed environment. Called by 
     * PersistenceManagerFactoryImpl when JTA Transaction associated with 
     * the current thread is active.
     *
     * @param t JTA Transaction associated with the current thread
     */
    protected void begin(javax.transaction.Transaction t) {

        beginInternal();
        try {
            jta = t;
            if (txSync == null) 
                txSync = new TransactionSynchronization(this); 

            jta.registerSynchronization((Synchronization)txSync);
        } catch (Exception e) {
            throw new JDOFatalInternalException(msg.msg(
                "EXC_ErrorRegister")); //NOI18N
        }

        // Set transaction type.
        txType = CMT;
    }

    /** 
     * Called in the managed environment only for transaction completion
     * by TransactionSynchronization#beforeCompletion().
     */
    protected void beforeCompletion() {
    
        if (txType == NON_MGD) {
            // Error - should not be called
            throw new JDOUserException(msg.msg(
                "EXC_WrongNonMgdOperation", "beforeCompletion")); //NOI18N
        }      
 
        Object o = null;
 
        // This flag prevents user from making any changes to the transaction object.
        startedCommit = true;

        try {
            o = EJBImplHelper.enlistBeforeCompletion(
                new Object[] {this, persistenceManager, jta});
            prepareFlush(true); // do actual beforeComplition.
            commitPrepare(); // check internal status.

            // do not do commitConnector() in the managed environment:
            setStatus(Status.STATUS_COMMITTED); 

        } finally {
            flushConnector(); // close
            EJBImplHelper.delistBeforeCompletion(o);
        }
    }
 
    /** 
     * Called in the managed environment only for transaction completion
     * by TransactionSynchronization#afterCompletion(int st).
     */
    protected void afterCompletion(int st) {
        if (txType == NON_MGD) {
            // Error - should not be called
            throw new JDOUserException(msg.msg(
                "EXC_WrongNonMgdOperation", "afterCompletion")); //NOI18N
        }
        st = EJBImplHelper.translateStatus(st); // translate Status

        if (debugging()) {
            traceCall("afterCompletion", st); // NOI18N
        }

        if (st == Status.STATUS_ROLLEDBACK) {
            internalRollback();
        } 

        if (st != status) {
            // Status mismatch - should not happen.
            try {
                if (synchronization != null)
                    // Allow user to do any cleanup.
                    ((Synchronization)synchronization).afterCompletion(st);
            } catch (Exception ex) {
                logger.warn(msg.msg(
                 "EXC_AfterCompletion"), ex); // NOI18N
            }

            // Force to close the persistence manager.
            persistenceManager.forceClose();

            throw new JDOUserException(msg.msg(
                 "EXC_InvalidStatus", // NOI18N
                 "afterCompletion", statusString(status),  // NOI18N
                 statusString(st)));
         }

         internalAfterCompletion();

    }

    //
    // ----- private methods ------
    //

    /** 
     * Status change and validation. Called by begin methods.
     */
    private void beginInternal() {
        persistenceManager.assertIsOpen();

        if (debugging())
            traceCall("begin");  // NOI18N

        if (isActive()) {
            throw new JDOUserException(msg.msg(
                "EXC_ErrorBegin"));  // NOI18N

        }
        setStatus(Status.STATUS_ACTIVE);

        connector = getConnector();
        connector.begin(optimistic);
    }

    /**
     * Lower-level before-commit method - phase 1.
     *
     * This is called to flush changes to the store.
     * State transition:
     *        STATUS_ACTIVE        starting state
     *        internalBeforeCompletion()    called while still active
     *        STATUS_PREPARING    no longer active, about to "really" commit
     *
     * @param _commit true if called during the commit processing
     * For exceptions see commit() method.
     */
    private void prepareFlush(boolean _commit) {
        boolean        rollbackOnly = false; //marked for rollback

        if (debugging())
            traceCall("prepareFlush"); // NOI18N
        //
        // Prepare connection
        //
        connector = getConnector();

        //
        // Validate transaction state before we commit
        //

        if ((status == Status.STATUS_ROLLING_BACK)
            ||    (status == Status.STATUS_ROLLEDBACK)) {
            throw new JDOUserException(msg.msg(
                "EXC_TransactionRolledback")); // NOI18N
        }

        if (connector.getRollbackOnly() ||
            status == Status.STATUS_MARKED_ROLLBACK) {
            rollbackOnly = true;

        } else if (status != Status.STATUS_ACTIVE) {
            throw new JDOUserException(msg.msg(
                "EXC_TransactionNotActive")); // NOI18N
        }

        //
        // User notifications done outside of lock - check for concurrent
        // rollback or setRollbackOnly during notification.
        //
        if (!rollbackOnly) {
            flushInstances(_commit);

            if (status == Status.STATUS_ACTIVE) {        // All ok
                if (startedCommit) { // inside commit - change status.
                    setStatus(Status.STATUS_PREPARING);
                }

            } else if (status == Status.STATUS_MARKED_ROLLBACK) {
                // This could happen only if setRollbackOnly() was called 
                // during flushInstances() without throwing an
                // exception.
                rollbackOnly = true;

            } else {    // concurrently rolled back - should not happen.
                throw new JDOUserException(msg.msg(
                    "EXC_TransactionRolledback")); // NOI18N
            }
        }
        if (rollbackOnly) {
            // Do not rollback here, but throw the exception and the rollback
            // will happen in the 'catch' block. Usually happens if the
            // connector was set rollback-only before the commit.
            setRollbackOnly();

            throw new JDOUserException(msg.msg(
               "EXC_MarkedRolledback")); // NOI18N

        }
    }

    /**
     * Lower-level prepare-commit method - phase 2. 
     *
     * This is called when flush is finished but before connectorCommit.
     * Will allow to support 2-phase commit.
     * State transition:
     *        STATUS_PREPARING    starting state
     *        STATUS_PREPARED
     *
     * For exceptions see commit() method.
     */
    private void commitPrepare() {
        if (debugging())
            traceCall("commitPrepare"); // NOI18N
        //
        // Once we've reached the Status.STATUS_PREPARING state we do not need
        // to check for concurrent state changes.  All user-level methods
        // (rollback, setRollbackOnly, register, enlist, etc) are no longer
        // allowed.
        //

        //
        // Validate initial state
        //
        if (status != Status.STATUS_PREPARING) {
            throw new JDOUserException(msg.msg(
               "EXC_WrongStateCommit")); // NOI18N
        }

        setStatus(Status.STATUS_PREPARED);
    }

    /**
     * Lower-level commit method - phase 3. Called only in a non-
     * managed environment.
     *
     * State transition:
     *        STATUS_PREPARED        starting state
     *        STATUS_COMMITTING    starting to do final phase
     *        commitConnector()        commit the flush.
     *        STATUS_COMMITTED
     *
     */
    private void commitComplete() {
        if (debugging())
            traceCall("commitComplete"); // NOI18N

        //
        // Validate initial state
        //
        if (status == Status.STATUS_ROLLING_BACK) {
            setStatus(Status.STATUS_ROLLING_BACK); 

            setStatus(Status.STATUS_ROLLEDBACK);

        } else if (status == Status.STATUS_PREPARED) {
            setStatus(Status.STATUS_COMMITTING);
            commitConnector();
            setStatus(Status.STATUS_COMMITTED);

        } else {
            throw new JDOUserException(msg.msg(
                "EXC_WrongStateCommit")); // NOI18N
        }
    }

    /**
     * Lower-level internal rollback method. This is to avoid concurrent rollbacks.
     *
     */
    private void internalRollback() {
        if (debugging())
            traceCall("internalRollback"); // NOI18N

        setStatus(Status.STATUS_ROLLING_BACK);
        try {
            if (txType == NON_MGD) {
                rollbackConnector();
            }
        } catch (JDOException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new JDOException("", ex);

        } finally {
            setStatus(Status.STATUS_ROLLEDBACK);
        }
    }

    /**
     *
     * Force rollback.  This is called when something goes wrong during
     * a late state check (i.e. some failure occurred during the prepare
     * stage).  Unless we're not already rolling back (or rolled back) this
     * will blindly change the state of the transaction and complete the
     * latter stage of rollback.
     *
     * @return the final status of the transaction.
     *
     * See internalRollback() for exceptions
     */
    private int forceRollback() {
        if (debugging())
            traceCall("forceRollback"); // NOI18N

        if ((status == Status.STATUS_ROLLING_BACK)        // Already
            ||    (status == Status.STATUS_ROLLEDBACK)        // Done
            ||    (status == Status.STATUS_COMMITTED)        // Too late
            ||    (status == Status.STATUS_NO_TRANSACTION)    // Never was
           ) {
            return status;
        }
        try {
            internalRollback();
        } finally {
            internalAfterCompletion();
        }

        return status;
    }

    /** 
     * Register a Synchronization object for this transaction object.
     * The transction manager invokes the beforeCompletion method prior to
     * starting the transaction commit process. After the transaction is
     * completed (or aborted), the transaction manager invokes the
     * afterCompletion method.
     *
     * @param sync The Synchronization object for the transaction.
     */
    private void registerSynchronization(Synchronization sync) {
        if (debugging())
            traceCall("registerSynchronization"); // NOI18N
        synchronized (this) {
            //
            // Disallow registration of new synchronization objects during
            // beforeCompletion or afterCompletion processing.  Synchronizations
            // are themselves involved in the process.
            //
            if (startedCommit) {
                throw new JDOUserException(msg.msg(
                    "EXC_TransactionCommitting")); // NOI18N
            }

            synchronization = sync;

            if (debugging()) {
                traceCall("registerSynchronization"); // NOI18N
            }
        }
    }


    /**
     * Confirm that transaction is terminated.
     * 
     * @return True if transaction is completed or not started.
     */
    private boolean isTerminated() {
        synchronized (this) {
            return (    (status == Status.STATUS_COMMITTED)
                    ||    (status == Status.STATUS_ROLLEDBACK)
                    ||    (status == Status.STATUS_NO_TRANSACTION));
        }
    }

    /**
     * Flush dirty persistent instances to the datastore.
     * If called during the commit processing, notifies registered 
     * Synchronization interfaces with beforeCompletion().
     */
    private void flushInstances(boolean commit) {
        if (commit) {
            getConnector().beforeCompletion();
            if (synchronization != null) {
                ((Synchronization)synchronization).beforeCompletion();
            }
        }

        persistenceManager.flushInstances();
    }

    /**
     * Notify Connector, PersistenceManager, and registered Synchronization 
     * instances about afterCompletion(). 
     * All status changes occured before executing this method.
     */
    private void internalAfterCompletion() {
        //
        // This will execute w/o an active transaction context
        //
        persistenceManager.afterCompletion(status);

        try {
            if (synchronization != null)
                ((Synchronization)synchronization).afterCompletion(status);
        } catch (Exception ex) {
            //
            // Exceptions ignored
            //
        }
        
        finish();
    }

    /**
     * Set status under lock (may be a nested lock which is ok)
     */
    private void setStatus(int status) {
        if (debugging()) {
            logger.debug(
                "Tran[" + toString() + "].setStatus: " + // NOI18N
                statusString(this.status) + " => " + // NOI18N
                statusString(status));
        }

        synchronized(this) {
            this.status = status;
            persistenceManager.notifyStatusChange(!isTerminated());
        }
    }

    /**
     * Finish this transaction
     */
    private void finish() {
        if (debugging())
            traceCall("finish"); // NOI18N

        //
        // Do not clear:
        //
        //    .status            -- users can still check status
        //
        startedCommit = false;

        if (txType == CMT || txType == BMT_UT) {
            persistenceManager.forceClose();
        } else if (txType == BMT_JDO) {
            persistenceManager.deregisterJTA();
        }

        jta = null;
        txType = NON_MGD;       // Restore the flag
    }

    //
    // ----- Connector utilities -----
    //

    /**
     * Get a connector 
     */
    private Connector getConnector() {
        StoreManager srm = persistenceManager.getStoreManager();

        if (username != null) {
            return srm.getConnector(username, password);
        }
        return srm.getConnector();
    }

    /**
     * Close a connector does flush of the changes and close
     */
    private void flushConnector() {
            connector = getConnector();
            connector.flush();
    }

    /** 
     * Rollback a connector does rollback and close
     */
    private void rollbackConnector() {
        connector = getConnector();
        connector.rollback();
    }

    /** 
     * Commit a connector does flush if necessary, commit and close
     */
    private void commitConnector() {
        connector = getConnector();
        connector.commit();
    }
 
    //
    // ----- Debugging utilities -----
    //

    /**
     * Verifies if debugging is enabled.
     * @return true if debugging is enabled.
     */
    private boolean debugging() {
        return logger.isDebugEnabled();
    }


    /**
     * Trace method call.
     */
    private void traceCall(String call) {
        logger.debug(
            "Tran[" + toString() + "]." + call + // NOI18N
            ": status = " + statusString(status) + // NOI18N
            ", txType: " + txTypeString()); // NOI18N
    }

    /**
     * Trace method call with a provided status.
     */
    private void traceCall(String call, int st) {
        logger.debug(
            "Tran[" + toString() + "]." + call + // NOI18N
            ": status = " + statusString(st) + // NOI18N
            ", txType: " + txTypeString()); // NOI18N
    }

    /**
     * Translates a txType value into a string.
     * 
     * @return  Printable String for a txType value
     */
    private String txTypeString() {
        switch (txType) {
            case NON_MGD:                   return "NON_MGD"; // NOI18N
            case CMT:                       return "CMT"; // NOI18N
            case BMT_UT:                    return "BMT_UT"; // NOI18N
            case BMT_JDO:                   return "BMT_JDO"; // NOI18N
            default:                        break;
        }
        return "UNKNOWN"; // NOI18N
    }      
}



