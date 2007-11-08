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

package com.sun.persistence.runtime.em.impl;

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.runtime.em.EntityManagerInternal;
import com.sun.persistence.support.Transaction;

import javax.persistence.EntityTransaction;


/**
 * The <code>EntityTransaction</code> implementation.
 *
 * @see javax.persistence.EntityTransaction
 * @author Martin Zaun
 */
public class EntityTransactionImpl implements EntityTransaction {

    /**
     * I18N message handler
     */
    static protected final I18NHelper msg
        = I18NHelper.getInstance("com.sun.persistence.runtime.Bundle"); //NOI18N

    /** The EntityManager who created this <code>EntityTransaction</code>. */
    protected final EntityManagerInternal em;

    /** The JDO transaction delegate. */
    protected final Transaction tx;

    /**
     * Creates a new <code>EntityTransactionImpl</code>.
     * @param em the entity manager creating this instance
     * @param tx the JDO transaction delegate
     */
    public EntityTransactionImpl(EntityManagerInternal em,
                                 Transaction tx) {
        assert (em != null);
        assert (tx != null);
        this.em = em;
        this.tx = tx;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return tx.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof EntityTransactionImpl) {
            return tx.equals(((EntityTransactionImpl)obj).tx);
        }
        return false;
    }

    /**
     * Start a resource transaction.
     * @see javax.persistence.EntityTransaction#begin
     */
    public void begin() {
        ensureIsNotActive();
        tx.begin();
    }

    /**
     * Commit the current transaction, writing any unflushed
     * changes to the database.
     * @see javax.persistence.EntityTransaction#commit
     */
    public void commit() {
        ensureIsActive();
        tx.commit();
    }

    /**
     * Roll back the current transaction.
     * @see javax.persistence.EntityTransaction#rollback
     */
    public void rollback() {
        ensureIsActive();
        tx.rollback();
    }

    /**
     * Check to see if a transaction is in progress.
     * @see javax.persistence.EntityTransaction#isActive
     */
    public boolean isActive() {
        return tx.isActive();
    }

    // ----------------------------------------------------------------------

    /**
     * Checks that this transaction is in an active state.
     * @throws IllegalStateException if this is transaction is not active
     */
    protected final void ensureIsActive() {
        if (!isActive()) {
            throw new IllegalStateException(
                msg.msg("runtime.entitytransaction.notactive"));//NOI18N
        }
    }

    /**
     * Checks that this transaction is not in an active state.
     * @throws IllegalStateException if this is transaction is active
     */
    protected final void ensureIsNotActive() {
        if (isActive()) {
            throw new IllegalStateException(
                msg.msg("runtime.entitytransaction.isactive"));//NOI18N
        }
    }
}
