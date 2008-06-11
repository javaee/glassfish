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

package com.sun.enterprise.transaction.spi;

import javax.transaction.*;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface JavaEETransactionManagerDelegate {

    /**
     * Returns <code>true</code> if this implementation supports 
     * last agent optimization.
     */
    public boolean useLAO();

    /**
     * Reset LAO value.
     *
     * @param b the boolean value for the LAO flag.
     */
    public void setUseLAO(boolean b);

    /** 
     * Commit distributed transaction if there is any.
     */
    public void commitDistributedTransaction() throws RollbackException,
            HeuristicMixedException, HeuristicRollbackException, 
            SecurityException,
            IllegalStateException, SystemException;

    /** 
     * Rollback distributed transaction if there is any.
     */
    public void rollbackDistributedTransaction() throws IllegalStateException, 
            SecurityException, SystemException;

    /**
     * Get implementation specific status of the transaction associated with 
     * the current thread.
     *
     * @return the status value as an int.
     */
    public int getStatus() throws SystemException;

    /**
     * Get implementation specific transaction object that represents the transaction 
     * context of the calling thread.
     *
     * @return the transaction object.
     */
    public Transaction getTransaction() throws SystemException;

    /**
     * Perform implementation specific steps to enlist a non-XA resource
     * with a distribute transaction.
     *
     * @param tran the Transaction object to be used to enlist the resource.
     * @param h the TransactionalResource object to be enlisted.
     * @return <code>true</code> if the resource was enlisted successfully.
     */
    public boolean enlistDistributedNonXAResource(
            Transaction tran, TransactionalResource h)
            throws RollbackException, IllegalStateException, SystemException;

    /**
     * Perform implementation specific steps to enlist resource as a LAO.
     *
     * @param tran the Transaction object to be used to enlist the resource.
     * @param h the TransactionalResource object to be enlisted.
     * @return <code>true</code> if the resource was enlisted successfully.
     */
    public boolean enlistLAOResource(Transaction tran, TransactionalResource h)
        throws RollbackException, IllegalStateException, SystemException;

    /**
     * Perform implementation specific steps to set setRollbackOnly status
     * for distributed transaction if there is any.
     */
    public void setRollbackOnlyDistributedTransaction() 
            throws IllegalStateException, SystemException;

    /**
     * Perform implementation specific steps to suspend a JavaEETransaction.
     *
     * @param tran the JavaEETransaction object to be suspend.
     * @return Transaction object representing the suspended transaction.
     */
    public Transaction suspend(JavaEETransaction tx) throws SystemException;

    /**
     * Perform implementation specific steps to resume a Transaction.
     *
     * @param tx the Transaction object that represents the transaction to be resumed.
     */
    public void resume(Transaction tx)
            throws InvalidTransactionException, IllegalStateException,
            SystemException;

    /**
     * Remove the Transaction object from the cache.
     *
     * @param tx the Transaction object to be removed.
     */
    public void removeTransaction(Transaction tx);

    /**
     * The delegate with the largest order will be used.
     *
     * @returns the order in which this delegate should be used.
     */
    public int getOrder();

    /**
     * Set the JavaEETransactionManager reference.
     *
     * @param tm the JavaEETransactionManager object.
     */
    public void setTransactionManager(JavaEETransactionManager tm);
}
