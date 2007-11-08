/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.transaction;

import java.util.*;
import javax.naming.*;
import oracle.toplink.essentials.sessions.ExternalTransactionController;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.exceptions.TransactionException;
import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Abstract implementation of an ExternalTransactionController.
 * <p>
 * <b>Description</b>: This class implements the ExternalTransactionController
 * interface. Concrete implementations of this class are responsible for performing
 * the coordination with the external transaction manager through whatever means
 * available to that manager. Different transaction services may do this in slightly
 * different ways, but most common ones (JTA and JTS/OTS) will use a synchronization
 * interface.
 * <p>
 * @see AbstractSynchronizationListener
 * @see oracle.toplink.essentials.sessions.ExternalTransactionController
 */
public abstract class AbstractTransactionController implements ExternalTransactionController {

    /** Table of external transaction object keys and unit of work values */
    protected Hashtable unitsOfWork;

    /** The session this controller is responsible for controlling */
    protected AbstractSession session;

    /** Generates listener instances for synchronization */
    protected SynchronizationListenerFactory listenerFactory;

    /** PERF: Cache the active uow in a thread local. */
    protected ThreadLocal activeUnitOfWorkThreadLocal;

    /**
     * INTERNAL:
     * Return a new controller.
     */
    public AbstractTransactionController() {
        this.unitsOfWork = new Hashtable();
        this.activeUnitOfWorkThreadLocal = new ThreadLocal();
    }

    /**
     * INTERNAL:
     * Associate the given unit of work and TopLink session with the active external
     * transaction. This may be done by whatever means supported by the transaction
     * manager (i.e. through a synchronization protocol).
     *
     * @param unitOfWork The unit of work that is to be bound to the active transaction
     * @param session The session ancestor of the unit of work
     */
    public void bindToCurrentTransaction(UnitOfWorkImpl unitOfWork, AbstractSession session) {
        Object status = getTransactionStatus();
        logTxStateTrace(unitOfWork, "TX_bind", status);
        try {
            Object txn = getTransaction();
            if (txn == null) {
                // If no external transaction is active then start one through the uow
                unitOfWork.beginTransaction();
                txn = getTransaction();
            }

            // If there still is no active txn then something is wrong
            if (txn == null) {
                throw TransactionException.externalTransactionNotActive();
            }

            // Create and register the synchronization listener
            AbstractSynchronizationListener listener = getListenerFactory().newSynchronizationListener(unitOfWork, session, txn, this);

            registerSynchronization_impl(listener, txn);
            unitOfWork.setSynchronized(true);

        } catch (Exception exception) {
            throw TransactionException.errorBindingToExternalTransaction(exception);
        }
    }

    /**
     * INTERNAL:
     * Begin an external transaction.
     *
     * @param session The session for which the transaction is being begun.
     */
    public void beginTransaction(AbstractSession session) {
        try {
            Object status = getTransactionStatus();
            logTxStateTrace(session, "TX_begin", status);

            // Make sure that we are in a state that we can actually start 
            // a transaction (e.g. ensure one is not already in progress)
            if (canBeginTransaction_impl(status)) {
                logTxTrace(session, "TX_beginningTxn", null);
                beginTransaction_impl();
                session.setWasJTSTransactionInternallyStarted(true);
            }
        } catch (Exception exception) {
            throw TransactionException.errorBeginningExternalTransaction(exception);
        }
    }

    /**
     * INTERNAL:
     * Commit the external transaction.
     *
     * @param session The session for which the transaction is being committed.
     */
    public void commitTransaction(AbstractSession session) {
        try {
            Object status = getTransactionStatus();
            logTxStateTrace(session, "TX_commit", status);

            if (canCommitTransaction_impl(status)) {
                logTxTrace(session, "TX_committingTxn", null);

                session.setWasJTSTransactionInternallyStarted(false);
                commitTransaction_impl();
            }
        } catch (Exception exception) {
            throw TransactionException.errorCommittingExternalTransaction(exception);
        }
    }

    /**
     * INTERNAL:
     * Roll back the external transaction.
     *
     * @param session The session for which the transaction is being rolled back.
     */
    public void rollbackTransaction(AbstractSession session) {
        try {
            Object status = getTransactionStatus();
            logTxStateTrace(session, "TX_rollback", status);

            session.setWasJTSTransactionInternallyStarted(false);

            // Only roll back if there is a transaction to roll back
            if ((canRollbackTransaction_impl(status)) && (getTransaction() != null)) {
                logTxTrace(session, "TX_rollingBackTxn", null);
                rollbackTransaction_impl();
            }
        } catch (Exception exception) {
            throw TransactionException.errorRollingBackExternalTransaction(exception);
        }
    }

    /**
     * INTERNAL:
     * Mark the external transaction for rollback.
     */
    public void markTransactionForRollback() {
        try {
            markTransactionForRollback_impl();
        } catch (Exception exception) {
            throw TransactionException.errorMarkingTransactionForRollback(exception);
        }
    }

    /**
     * INTERNAL:
     * Return the active external transaction object, or null if
     * none is active. This may be in whatever form the transaction system uses.
     */
    public Object getTransaction() {
        try {
            return getTransaction_impl();
        } catch (Exception exception) {
            throw TransactionException.errorGettingExternalTransaction(exception);
        }
    }

    /**
     * INTERNAL:
     * Return a key for the specified external transaction object.
     * The key is just something that can be inserted into a hashtable (must support
     * hashCode() and equals() methods).
     */
    public Object getTransactionKey(Object transaction) {
        try {
            return getTransactionKey_impl(transaction);
        } catch (Exception exception) {
            throw TransactionException.errorGettingExternalTransaction(exception);
        }
    }

    /**
     * INTERNAL:
     * Return the transaction status. This may be any type of status or value,
     * depending upon the transaction system.
     */
    public Object getTransactionStatus() {
        try {
            return getTransactionStatus_impl();
        } catch (Exception exception) {
            throw TransactionException.errorGettingExternalTransactionStatus(exception);
        }
    }

    /**
    * INTERNAL:
    * Used the EJB 3.0 to determine if a transaction is in a state where an EntityManager can
    * be closed
    */
    public boolean noTransactionOrRolledBackOrCommited() {
        try {
            Object status = getTransactionStatus();
            return canBeginTransaction_impl(status) || canMergeUnitOfWork_impl(status) || isRolledBack_impl(status);
        } catch (Exception exception) {
            throw TransactionException.errorGettingExternalTransactionStatus(exception);
        }
    }

    /**
     * INTERNAL:
     * Return true if the transaction is in the roleld back state.
     */
    public abstract boolean isRolledBack_impl(Object status);

    /**
     * INTERNAL:
     * Return true if there is a unit of work associated with the active external
     * transaction. Return false if no transaction is current, or if no uow has
     * been associated with the active transaction yet.
     */
    public boolean hasActiveUnitOfWork() {
        return this.lookupActiveUnitOfWork() != null;
    }

    /**
     * INTERNAL:
     * Return the active unit of work for the current external transaction.
     * If no transaction is active then return null. If a transaction is active
     * but no unit of work has been bound to it then create and return one.
     */
    public UnitOfWorkImpl getActiveUnitOfWork() {
        Object transaction = getTransaction();
        if (transaction == null) {
            return null;
        }
        
        UnitOfWorkImpl activeUnitOfWork = lookupActiveUnitOfWork(transaction);
        if (activeUnitOfWork == null) {
            // Txn is active but no UoW has been associated with it yet, so create one.
            activeUnitOfWork = (UnitOfWorkImpl) getSession().acquireUnitOfWork();
            Object transactionKey = getTransactionKey(transaction);
            addUnitOfWork(transactionKey, activeUnitOfWork);
            activeUnitOfWork.setTransaction(transaction);
            this.activeUnitOfWorkThreadLocal.set(activeUnitOfWork);
        }
        return activeUnitOfWork;
    }

    /**
     * INTERNAL:
     * Return the unit of work associated with the active external transaction.
     * Return null if no transaction is active, or if no uow has been associated with
     * the active transaction yet.
     */
    public UnitOfWorkImpl lookupActiveUnitOfWork() {
        return lookupActiveUnitOfWork(getTransaction());
    }

    /**
     * INTERNAL:
     * Return the unit of work associated with the active external transaction.
     * Return null if no transaction is active, or if no uow has been associated with
     * the active transaction yet.
     */
    public UnitOfWorkImpl lookupActiveUnitOfWork(Object transaction) {
        if (transaction == null) {
            return null;
        }
        Object transactionKey = getTransactionKey(transaction);

        // PERF: Cache the active unit of work in a thread local.
        // This is just a heuristic, so uses == and no tx-key and direct access as extreamly high throughput.
        UnitOfWorkImpl activeUnitOfWork = (UnitOfWorkImpl)this.activeUnitOfWorkThreadLocal.get();
        if (activeUnitOfWork != null) {
            if (transaction == activeUnitOfWork.getTransaction()) {
                return activeUnitOfWork;
            }
        }
        activeUnitOfWork = (UnitOfWorkImpl)getUnitsOfWork().get(transactionKey);
        if (activeUnitOfWork != null) {
            activeUnitOfWork.setTransaction(transaction);
        }
        this.activeUnitOfWorkThreadLocal.set(activeUnitOfWork);
        return activeUnitOfWork;
    }

    /**
     * INTERNAL:
     * Add a UnitOfWork object to the Hashtable keyed on the external transaction object.
     */
    public void addUnitOfWork(Object transaction, UnitOfWorkImpl activeUnitOfWork) {
        this.activeUnitOfWorkThreadLocal.set(null);
        getUnitsOfWork().put(transaction, activeUnitOfWork);
    }

    /**
     * INTERNAL:
     * Remove the unit of work associated with the transaction passed in.
     */
    public void removeUnitOfWork(Object transaction) {
        if (transaction != null) {
            getUnitsOfWork().remove(transaction);
        }
        this.activeUnitOfWorkThreadLocal.set(null);
    }

    /**
     * INTERNAL:
     * Return the manager's session.
     */
    public AbstractSession getSession() {
        return session;
    }

    /**
     * INTERNAL:
     * Set the manager's session.
     */
    public void setSession(AbstractSession session) {
        this.session = session;
    }

    /**
     * INTERNAL:
     * Return the hashtable keyed on the external transaction objectss with values
     * that are the associated units of work.
     */
    public Hashtable getUnitsOfWork() {
        return unitsOfWork;
    }

    /**
     * INTERNAL:
     * Set the table of transactions to units of work.
     */
    protected void setUnitsOfWork(Hashtable unitsOfWork) {
        this.unitsOfWork = unitsOfWork;
    }

    /**
     * INTERNAL:
     * Get the factory used to generate synchronization listeners.
     */
    public SynchronizationListenerFactory getListenerFactory() {
        return listenerFactory;
    }

    /**
     * INTERNAL:
     * Set the factory used to generate synchronization listeners. This should be
     * set if a listener other than the default one is being used.
     */
    public void setListenerFactory(SynchronizationListenerFactory factory) {
        listenerFactory = factory;
    }

    /**
     * INTERNAL:
     * Associate the given unit of work and TopLink session with the current external
     * transaction. This method is offered only for backwards compatibility.
     */
    public void registerSynchronizationListener(UnitOfWorkImpl uow, AbstractSession session) throws DatabaseException {
        this.bindToCurrentTransaction(uow, session);
    }

    /**
     * PUBLIC:
     * Look up a given name in JNDI. This can be used by a subclass or even an
     * application to look up transaction artifacts that are required for the
     * implementation.
     * <p>
     * The lookup assumes that it is taking place on the server side, and that the
     * InitialContext can be used without requiring any special properties.
     *
     * @param jndiName The name to look up
     * @return The object bound in JNDI to the specified name
     * @exception TransactionException Thrown in case of lookup failure
     */
    public Object jndiLookup(String jndiName) {
        Context context = null;
        Object jndiObject = null;
        try {
            context = new InitialContext();
            jndiObject = context.lookup(jndiName);
        } catch (NamingException ex) {
            throw TransactionException.jndiLookupException(jndiName, ex);
        } finally {
            try {
                context.close();
            } catch (Exception ex2) {/* ignore */
            }
        }
        return jndiObject;
    }

    /*
     * INTERNAL:
     * Helper method to log trace statements to the transaction channel.
     */
    public void logTxTrace(AbstractSession session, String msgInd, Object[] args) {
        session.log(SessionLog.FINER, SessionLog.TRANSACTION, msgInd, args);
    }

    /*
     * INTERNAL:
     * Helper method to log transaction state to the transaction channel.
     */
    public void logTxStateTrace(AbstractSession session, String msgInd, Object status) {
        if (session.shouldLog(SessionLog.FINER, SessionLog.TRANSACTION)) {
            String statusString = statusToString_impl(status);
            Object[] args = { statusString };
            session.log(SessionLog.FINER, SessionLog.TRANSACTION, msgInd, args);
        }
    }

    //---------------------------------------------------------------------------------
    // The following methods must be implemented by subclass implementations.
    //---------------------------------------------------------------------------------

    /**
     * INTERNAL:
     * Register the specified synchronization listener with the given active
     * transaction.
     *
     * @param listener The synchronization listener created for this transaction
     * @param txn The active transaction for which notification is being requested
     */
    protected abstract void registerSynchronization_impl(AbstractSynchronizationListener listener, Object txn) throws Exception;

    /**
     * INTERNAL:
     * Return the active external transaction for the calling thread, or null if
     * none is currently active for this thread.
     *
     * @return The active transaction object, or null if no transaction is active
     */
    protected abstract Object getTransaction_impl() throws Exception;

    /**
     * INTERNAL:
     * Return a key for the specified external transaction object.
     * The key is just something that can be inserted into a hashtable (must support
     * hashCode() and equals() methods).
     *
     * @param transaction The transaction to which the returned key applies (may be null)
     * @return A key for the passed in transaction, or null if no transaction specified
     */
    protected abstract Object getTransactionKey_impl(Object transaction) throws Exception;

    /**
     * INTERNAL:
     * Return the transaction status. This may be any type of status or value,
     * depending upon the transaction system.
     *
     * @return The current transaction status
     */
    protected abstract Object getTransactionStatus_impl() throws Exception;

    /**
     * INTERNAL:
     * Begin an external transaction. Do this in a way appropriate to the
     * transaction subsystem.
     */
    protected abstract void beginTransaction_impl() throws Exception;

    /**
     * INTERNAL:
     * Commit the external transaction. Do this in a way appropriate to the
     * transaction subsystem.
     */
    protected abstract void commitTransaction_impl() throws Exception;

    /**
     * INTERNAL:
     * Roll back the external transaction. Do this in a way appropriate to the
     * transaction subsystem.
     */
    protected abstract void rollbackTransaction_impl() throws Exception;

    /**
     * INTERNAL:
     * Mark the external transaction for rollback. Do this in a way appropriate to the
     * transaction subsystem.
     */
    protected abstract void markTransactionForRollback_impl() throws Exception;

    /**
     * INTERNAL:
     * Return true if the status indicates that a transaction can be started. This
     * would normally mean that no transaction is currently active.
     * The status is interpreted by the transaction subsystem.
     *
     * @param status The current transaction status
     * @return true if the current state allows for a transaction to be started
     */
    protected abstract boolean canBeginTransaction_impl(Object status);

    /**
     * INTERNAL:
     * Return true if the status indicates that a transaction can be committed. This
     * would normally mean that a transaction is currently active.
     * The status is interpreted by the transaction subsystem.
     *
     * @param status The current transaction status
     * @return true if the current state allows for a transaction to be committed
     */
    protected abstract boolean canCommitTransaction_impl(Object status);

    /**
     * INTERNAL:
     * Return true if the status indicates that a transaction can be rolled back. This
     * would normally mean that a transaction is currently active.
     * The status is interpreted by the transaction subsystem.
     *
     * @param status The current transaction status
     * @return true if the current state allows for a transaction to be rolled back
     */
    protected abstract boolean canRollbackTransaction_impl(Object status);

    /**
     * INTERNAL:
     * Return true if the status indicates that the SQL should be issued to the db.
     * This would normally mean that a transaction was active and not being rolled
     * back or marked for rollback.
     * The status is interpreted by the transaction subsystem.
     *
     * @param status The current transaction status
     * @return true if the current state allows for the SQL to be sent to the database
     */
    protected abstract boolean canIssueSQLToDatabase_impl(Object status);

    /**
     * INTERNAL:
     * Return true if the status indicates that the unit of work should be merged
     * into the shared cache. This would normally mean that the transaction was
     * committed successfully.
     * The status is interpreted by the transaction subsystem.
     *
     * @param status The current transaction status
     * @return true if the current state dictates that the unit of work should be merged
     */
    protected abstract boolean canMergeUnitOfWork_impl(Object status);

    /**
     * INTERNAL:
     * Convert the status to a string for tracing.
     */
    protected abstract String statusToString_impl(Object status);
}
