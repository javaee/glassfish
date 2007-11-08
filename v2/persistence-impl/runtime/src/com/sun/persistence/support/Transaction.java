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
 * Transaction.java
 *
 * Created on February 25, 2000
 */
 
package com.sun.persistence.support;
import javax.transaction.Synchronization;

/** The JDO <code>Transaction</code> interface provides for initiation and completion 
 * of transactions under user control.
 * It is a sub-interface of the {@link PersistenceManager}
 * that deals with options and transaction demarcation. 
 * <P>Transaction options include whether optimistic concurrency
 * control should be used for the current transaction, whether instances
 * may hold values in the cache outside transactions, and whether
 * values should be retained in the cache after transaction completion.  These
 * options are valid for both managed and non-managed transactions.
 *
 * <P>Transaction completion methods have the same semantics as
 * <code>javax.transaction.UserTransaction</code>, and are valid only in the
 * non-managed, non-distributed transaction environment.
 * <P>For operation in the distributed environment, <code>Transaction</code> is declared
 * to implement <code>javax.transaction.Synchronization</code>.  This allows for
 * flushing the cache to the data store during externally managed
 * transaction completion.
 * @author Craig Russell
 * @version 1.0
 */

public interface Transaction
{
    /** Begin a transaction.  The type of transaction is determined by the
     * setting of the Optimistic flag.
     * @see #setOptimistic
     * @see #getOptimistic
     * @throws JDOUserException if transactions are managed by a container
     * in the managed environment, or if the transaction is already active.
     */
    void begin();
    
    /** Commit the current transaction.
     * @throws JDOUserException if transactions are managed by a container
     * in the managed environment, or if the transaction is not active.
     */
    void commit();
    
    /** Roll back the current transaction.
     * @throws JDOUserException if transactions are managed by a container
     * in the managed environment, or if the transaction is not active.
     */
    void rollback();

    /** Returns whether there is a transaction currently active.
     * @return <code>true</code> if the transaction is active.
     */
    boolean isActive();
    
    /** If <code>true</code>, allow persistent instances to be read without
     * a transaction active.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param nontransactionalRead the value of the nontransactionalRead property
     */
    void setNontransactionalRead (boolean nontransactionalRead);
    
    /** If <code>true</code>, allows persistent instances to be read without
     * a transaction active.
     * @return the value of the nontransactionalRead property
     */
    boolean getNontransactionalRead ();
    
    /** If <code>true</code>, allow persistent instances to be written without
     * a transaction active.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param nontransactionalWrite the value of the nontransactionalRead property
     */
    void setNontransactionalWrite (boolean nontransactionalWrite);
    
    /** If <code>true</code>, allows persistent instances to be written without
     * a transaction active.
     * @return the value of the nontransactionalWrite property
     */
    boolean getNontransactionalWrite ();
    
    /** If <code>true</code>, at commit instances retain their values and the instances
     * transition to persistent-nontransactional.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param retainValues the value of the retainValues property
     */
    void setRetainValues(boolean retainValues);
    
    /** If <code>true</code>, at commit time instances retain their field values.
     * @return the value of the retainValues property
     */
    boolean getRetainValues();
    
    /** If <code>true</code>, at rollback, fields of newly persistent instances 
     * are restored to 
     * their values as of the beginning of the transaction, and the instances
     * revert to transient.  Additionally, fields of modified
     * instances of primitive types and immutable reference types
     * are restored to their values as of the beginning of the 
     * transaction.
     * <P>If <code>false</code>, at rollback, the values of fields of 
     * newly persistent instances are unchanged and the instances revert to
     * transient.  Additionally, dirty instances transition to hollow.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param restoreValues the value of the restoreValues property
     */
    void setRestoreValues(boolean restoreValues);
    
    /** Return the current value of the restoreValues property.
     * @return the value of the restoreValues property
     */
    boolean getRestoreValues();
    
    /** Optimistic transactions do not hold data store locks until commit time.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param optimistic the value of the Optimistic flag.
     */
    void setOptimistic(boolean optimistic);
    
    /** Optimistic transactions do not hold data store locks until commit time.
     * @return the value of the Optimistic property.
     */
    boolean getOptimistic();
    
    /** The user can specify a <code>Synchronization</code> instance to be notified on
     * transaction completions.  The <code>beforeCompletion</code> method is called prior
     * to flushing instances to the data store.
     *
     * <P>The <code>afterCompletion</code> method is called after performing state
     * transitions of persistent and transactional instances, following 
     * the data store commit or rollback operation.
     * <P>Only one <code>Synchronization</code> instance can be registered with the 
     * <code>Transaction</code>. If the application requires more than one instance to 
     * receive synchronization callbacks, then the single application instance 
     * is responsible for managing them, and forwarding callbacks to them.
     * @param sync the <code>Synchronization</code> instance to be notified; <code>null</code> for none
     */
    void setSynchronization(Synchronization sync);
    
    /** The user-specified <code>Synchronization</code> instance for this <code>Transaction</code> instance.    
     * @return the user-specified <code>Synchronization</code> instance.
     */
    Synchronization getSynchronization();

    /** The <code>Transaction</code> instance is always associated with exactly one
     * <code>PersistenceManager</code>.
     *
     * @return the <code>PersistenceManager</code> for this <code>Transaction</code> instance
     */
    PersistenceManager getPersistenceManager();
}
