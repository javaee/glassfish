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

package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.ComponentContext;
import com.sun.enterprise.container.common.spi.JavaEETransaction;
import com.sun.appserv.connectors.spi.ResourceHandle;

import javax.transaction.*;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.XATerminator;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationException;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

/**
 * Dummy implementation of JavaEE TransactionManager
 */
@Service
public class JavaEETransactionManagerProxy implements JavaEETransactionManager {

    @Inject
    private JavaEETransaction tx;
    /**
     * register a synchronization object with the transaction
     * associated with the current thread
     *
     * @param sync the synchronization object
     * @throws IllegalStateException Thrown if the transaction in the
     *                               target object is in prepared state or the transaction is inactive.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition
     */
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
        // Do nothing
    }

    /**
     * Enlist the resource specified with the transaction
     *
     * @param tran The transaction object
     * @param h    The resource handle object
     * @return <i>true</i> if the resource was enlisted successfully; otherwise     *    false.
     * @throws javax.transaction.RollbackException
     *                               Thrown to indicate that
     *                               the transaction has been marked for rollback only.
     * @throws IllegalStateException Thrown if the transaction in the
     *                               target object is in prepared state or the transaction is inactive.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition
     */
    public boolean enlistResource(Transaction tran, ResourceHandle h) throws RollbackException, IllegalStateException, SystemException {
        return true;
    }

    /**
     * Delist the resource specified from the transaction
     *
     * @param tran The transaction object
     * @param h    The resource handle object
     * @param flag One of the values of TMSUCCESS, TMSUSPEND, or TMFAIL.
     * @throws IllegalStateException Thrown if the transaction in the
     *                               target object is inactive.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition
     */
    public boolean delistResource(Transaction tran, ResourceHandle h, int flag) throws IllegalStateException, SystemException {
        return true;
    }

    /**
     * This is called by the Container to ask the Transaction
     * Manager to enlist all resources held by a component and
     * to associate the current Transaction with the current
     * Invocation
     * The TM finds the component through the InvocationManager
     */
    public void enlistComponentResources() throws RemoteException {
        // Do nothing
    }

    /**
     * This is called by the Container to ask the Transaction
     * Manager to delist all resources held by a component
     * <p/>
     * The TM finds the component through the InvocationManager
     *
     * @param suspend true if the resources should be delisted
     *                with TMSUSPEND flag; false otherwise
     */
    public void delistComponentResources(boolean suspend) throws RemoteException {
        // Do nothing
    }

    /**
     * This is called by Container to indicate that a component
     * is being destroyed. All resources registered in the context
     * should be released
     *
     * @param instance The component instance
     */
    public void componentDestroyed(Object instance) {
        // Do nothing
    }

    public void ejbDestroyed(ComponentContext context) {
        // Do nothing
    }

    /**
     * Called by InvocationManager
     */

    public void preInvoke(ComponentInvocation prev) throws InvocationException {
        // Do nothing
    }

    /**
     * Called by InvocationManager
     */

    public void postInvoke(ComponentInvocation curr, ComponentInvocation prev) throws InvocationException {
        // Do nothing
    }

    public void setDefaultTransactionTimeout(int seconds) {
        // Do nothing
    }

    public void cleanTxnTimeout() // clean up thread specific timeout
    {
        // Do nothing
    }

    /**
     * Returns a list of resource handles held by the component
     */

    public List getExistingResourceList(Object instance, ComponentInvocation inv) {
        return new ArrayList();
    }

    public void registerComponentResource(ResourceHandle h) {
        // Do nothing
    }

    public void unregisterComponentResource(ResourceHandle h) {
        // Do nothing
    }

    public void recover(XAResource[] resourceList) {
        // Do nothing
    }

    public void begin(int timeout) throws NotSupportedException, SystemException {
        // Do nothing
    }

    /**
     * Return true if a "null transaction context" was received
     * from the client or if the server's transaction.interoperability
     * flag is false.
     * A null tx context indicates that the client had an active
     * tx but the client container did not support tx interop.
     * See EJB2.0 spec section 18.5.2.1.
     */
    public boolean isNullTransaction() {
        return false;  // Do nothing
    }

    /**
     * Perform checks during export of a transaction on a remote call.
     */
    public void checkTransactionExport(boolean isLocal) {
        // Do nothing
    }

    /**
     * Perform checks during import of a transaction on a remote call.
     * This is called from the reply interceptors after a remote call completes.
     */
    public void checkTransactionImport() {
        // Do nothing
    }

    /**
     * Utility for the ejb container to check if the transaction is marked for
     * rollback because of timeout. This is applicable only for local transactions
     * as jts transaction will rollback instead of setting the txn for rollback
     */
    public boolean isTimedOut() {
        return false;  // Do nothing
    }

    public ArrayList getActiveTransactions() {
        return new ArrayList();
    }/*
    * Called by Admin Framework. Forces the given transaction to be rolled back
    */
    public void forceRollback(Transaction tran) throws IllegalStateException, SystemException {
        // Do nothing
    }/*
    * Called by Admin Framework. Returnes number of transactions commited till now.
    */
    public int getNumberOfTransactionsCommitted() {
        return 0;  // Do nothing
    }/*
    * Called by Admin Framework. Returnes number of transactions rolledback till now.
    */
    public int getNumberOfTransactionsRolledBack() {
        return 0;  // Do nothing
    }/*
    * Called by Admin Framework. Returnes number of Active transactions.
    */
    public int getNumberOfActiveTransactions() {
        return 0;  // Do nothing
    }/*
    * Called by Admin Framework.
    */
    public void setMonitoringEnabled(boolean enabled) {
        // Do nothing
    }/*
    * Called by Admin Framework.
    */
    public void freeze() {
        // Do nothing
    }/*
    * Called by Admin Framework.
    */
    public void unfreeze() {
        // Do nothing
    }/*
    * Called by Admin Framework
    */
    public boolean isFrozen() {
        return false;  // Do nothing
    }

    /**
     * recreate a transaction based on the Xid. This call causes the calling
      * thread to be associated with the specified transaction. <p>
      * This is used by importing transactions via the Connector contract.
      *
      * @param xid the Xid object representing a transaction.
      */
     public void recreate(Xid xid, long timeout) throws WorkException {
        // Do nothing
    }

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void release(Xid xid) throws WorkException {
        // Do nothing
    }

    /**
     * Provides a handle to a <code>XATerminator</code> instance. The
     * <code>XATerminator</code> instance could be used by a resource adapter
     * to flow-in transaction completion and crash recovery calls from an EIS.
     * <p/>
     * This is used by importing transactions via the Connector contract.
     *
     * @return a <code>XATerminator</code> instance.
     */
    public XATerminator getXATerminator() {
        return null;  // Do nothing
    }

    /**
     * Create a new transaction and associate it with the current thread.
     *
     * @throws javax.transaction.NotSupportedException
     *          Thrown if the thread is already
     *          associated with a transaction and the Transaction Manager
     *          implementation does not support nested transactions.
     * @throws javax.transaction.SystemException
     *          Thrown if the transaction manager
     *          encounters an unexpected error condition.
     */
    public void begin() throws NotSupportedException, SystemException {
        // Do nothing
    }

    /**
     * Complete the transaction associated with the current thread. When this
     * method completes, the thread is no longer associated with a transaction.
     *
     * @throws javax.transaction.RollbackException
     *                               Thrown to indicate that
     *                               the transaction has been rolled back rather than committed.
     * @throws javax.transaction.HeuristicMixedException
     *                               Thrown to indicate that a heuristic
     *                               decision was made and that some relevant updates have been committed
     *                               while others have been rolled back.
     * @throws javax.transaction.HeuristicRollbackException
     *                               Thrown to indicate that a
     *                               heuristic decision was made and that all relevant updates have been
     *                               rolled back.
     * @throws SecurityException     Thrown to indicate that the thread is
     *                               not allowed to commit the transaction.
     * @throws IllegalStateException Thrown if the current thread is
     *                               not associated with a transaction.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        // Do nothing
    }

    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @return The transaction status. If no transaction is associated with
     *         the current thread, this method returns the Status.NoTransaction
     *         value.
     * @throws javax.transaction.SystemException
     *          Thrown if the transaction manager
     *          encounters an unexpected error condition.
     */
    public int getStatus() throws SystemException {
        return 0;  // Do nothing
    }

    /**
     * Get the transaction object that represents the transaction
     * context of the calling thread.
     *
     * @return the <code>Transaction</code> object representing the
     *         transaction associated with the calling thread.
     * @throws javax.transaction.SystemException
     *          Thrown if the transaction manager
     *          encounters an unexpected error condition.
     */
    public Transaction getTransaction() throws SystemException {
        return tx;
    }

    /**
     * Resume the transaction context association of the calling thread
     * with the transaction represented by the supplied Transaction object.
     * When this method returns, the calling thread is associated with the
     * transaction context specified.
     *
     * @param tobj The <code>Transaction</code> object that represents the
     *             transaction to be resumed.
     * @throws javax.transaction.InvalidTransactionException
     *                               Thrown if the parameter
     *                               transaction object contains an invalid transaction.
     * @throws IllegalStateException Thrown if the thread is already
     *                               associated with another transaction.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
        // Do nothing
    }

    /**
     * Roll back the transaction associated with the current thread. When this
     * method completes, the thread is no longer associated with a
     * transaction.
     *
     * @throws SecurityException     Thrown to indicate that the thread is
     *                               not allowed to roll back the transaction.
     * @throws IllegalStateException Thrown if the current thread is
     *                               not associated with a transaction.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        // Do nothing
    }

    /**
     * Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     *
     * @throws IllegalStateException Thrown if the current thread is
     *                               not associated with a transaction.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        // Do nothing
    }

    /**
     * Modify the timeout value that is associated with transactions started
     * by the current thread with the begin method.
     * <p/>
     * <p> If an application has not called this method, the transaction
     * service uses some default value for the transaction timeout.
     *
     * @param seconds The value of the timeout in seconds. If the value is zero,
     *                the transaction service restores the default value. If the value
     *                is negative a SystemException is thrown.
     * @throws javax.transaction.SystemException
     *          Thrown if the transaction manager
     *          encounters an unexpected error condition.
     */
    public void setTransactionTimeout(int seconds) throws SystemException {
        // Do nothing
    }

    /**
     * Suspend the transaction currently associated with the calling
     * thread and return a Transaction object that represents the
     * transaction context being suspended. If the calling thread is
     * not associated with a transaction, the method returns a null
     * object reference. When this method returns, the calling thread
     * is not associated with a transaction.
     *
     * @return Transaction object representing the suspended transaction.
     * @throws javax.transaction.SystemException
     *          Thrown if the transaction manager
     *          encounters an unexpected error condition.
     */
    public Transaction suspend() throws SystemException {
        return null;  // Do nothing
    }
}
