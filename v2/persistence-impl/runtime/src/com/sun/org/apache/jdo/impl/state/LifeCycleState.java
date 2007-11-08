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
 *  LifeCycleState.java    March 10, 2001
 */

package com.sun.org.apache.jdo.impl.state;

import java.util.BitSet;


import com.sun.org.apache.jdo.state.FieldManager;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.JDOUserException;
import com.sun.persistence.support.Transaction;


/**
 * This is an abstract LifeCycleState that provides the most common implementation
 * for the state transitions and most common values for the state flags. Each 
 * specific state overrides only necessary methods. All states are represented 
 * by the static array of state instances. States can only call back StateManagerImpl 
 * if it is passed as a parameter to a method call. States do not hold references 
 * to any instances and are StateManger independent.
 * 
 * @author Marina Vatkina
 */
abstract class LifeCycleState {

    /**
      * I18N message handler
      */
    protected final static I18NHelper msg = 
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.state.Bundle"); // NOI18N
    
    // These are state flags that are set to required valuers in specific state 
    protected boolean isPersistent;
    protected boolean isTransactional;
    protected boolean isDirty;
    protected boolean isNew;
    protected boolean isDeleted;

    // These are internal flags.
    protected boolean isNavigable;
    protected boolean isRefreshable;
    protected boolean isBeforeImageUpdatable;
    protected boolean isFlushed;
    protected boolean isAutoPersistent = false;

    // This flag is set to true if the state corresponds to an instance
    // that exists in a datastore.
    protected boolean isStored = true;

    // The following flag states that merge is needed
    protected boolean needMerge = true;

    protected int     stateType;

    /**
     * Constants to specify the life cycle state type
     */
    final static protected int
        TRANSIENT             = 0,
        HOLLOW                = 1,
        P_NON_TX              = 2,

        P_CLEAN               = 3,
        P_CLEAN_TX            = 4,
 
        P_DIRTY               = 5,
        P_DIRTY_FLUSHED       = 6,

        P_NEW                 = 7,
        P_NEW_FLUSHED         = 8,
        P_NEW_FLUSHED_DELETED = 9,
        P_NEW_FLUSHED_DIRTY   = 10,
        P_NEW_DELETED         = 11,

        P_DELETED             = 12,
        P_DELETED_FLUSHED     = 13,

        T_CLEAN               = 14,
        T_DIRTY               = 15,

        AP_NEW                = 16,
        AP_NEW_FLUSHED        = 17,
        AP_NEW_FLUSHED_DIRTY  = 18,
        AP_PENDING            = 19,

        TOTAL                 = 20;
    
    private static LifeCycleState stateTypes[];

    
    // ******************************************************************
    // Initialisation stuff
    // ******************************************************************

    /**
     * Static initialiser.
     * Initialises the life cycle.
     */
    static {
        initLifeCycleState();
    }   
    
    /**
     * Initialises the objects. This class implements the "state pattern".
     */
    
    // This method is called (through the static initializer) 
    // when the LifeCycleState class or any of its subclasses is loaded.
    
    // It is extremely important that this method is called before any of isNew etc is called,
    // and before stateType() is called !!!
    
    protected static void initLifeCycleState() {
        stateTypes = new LifeCycleState[TOTAL];

        stateTypes[TRANSIENT] = null;
        stateTypes[T_CLEAN] = new TransientClean();
        stateTypes[T_DIRTY] = new TransientDirty();

        stateTypes[P_CLEAN] = new PersistentClean();
        stateTypes[P_CLEAN_TX] = new PersistentCleanTransactional();

        stateTypes[P_DIRTY] = new PersistentDirty();
        stateTypes[P_DIRTY_FLUSHED] = new PersistentDirtyFlushed();

        stateTypes[P_NEW] = new PersistentNew();
        stateTypes[P_NEW_FLUSHED] = new PersistentNewFlushed();
        stateTypes[P_NEW_DELETED] = new PersistentNewDeleted();
        stateTypes[P_NEW_FLUSHED_DELETED] = new PersistentNewFlushedDeleted();
        stateTypes[P_NEW_FLUSHED_DIRTY] = new PersistentNewFlushedDirty();

        stateTypes[AP_NEW] = new AutoPersistentNew();
        stateTypes[AP_NEW_FLUSHED] = new AutoPersistentNewFlushed();
        stateTypes[AP_NEW_FLUSHED_DIRTY] = new AutoPersistentNewFlushedDirty();
        stateTypes[AP_PENDING] = new AutoPersistentPending();

        stateTypes[P_DELETED] = new PersistentDeleted();
        stateTypes[P_DELETED_FLUSHED] = new PersistentDeletedFlushed();              

        stateTypes[HOLLOW] = new Hollow();
        stateTypes[P_NON_TX] = new PersistentNonTransactional();
    }

    /**
     * Returns the LifeCycleState for the state constant.
     *
     * @param state the state type as integer
     * @return the type as LifeCycleState object
     */
    protected static LifeCycleState getLifeCycleState(int state) {
        return stateTypes[state];
    }
    
    /**
     * Returns the type of the life cycle state as an int.
     *
     * @return the type of this life cycle state as an int.
     *
     */
    protected int stateType() {
        return stateType;
    }
    
    /**
     * Transitions LifeCycleState on call to makeTransient.
     * 
     * @see com.sun.persistence.support.PersistenceManager#makeTransient(Object pc)
     * @param sm StateManagerImpl requested the transition.
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState.
     */
    protected LifeCycleState transitionMakeTransient(StateManagerImpl sm, 
        Transaction tx) {
        throw new JDOUserException(msg.msg(
                    "EXC_DirtyInstance"), sm.getObject()); // NOI18N
    }

    /**
     * Transitions LifeCycleState on call to makeTransactional
     *  
     * @see com.sun.persistence.support.PersistenceManager#makeTransactional(Object pc) 
     * @param sm StateManagerImpl requested the transition.
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState.
     */ 
    protected LifeCycleState transitionMakeTransactional(StateManagerImpl sm, 
        Transaction tx) {
        return this;
    }

    /**
     * Transitions LifeCycleState on call to makeNontransactional 
     *   
     * @see com.sun.persistence.support.PersistenceManager#makeNontransactional(Object pc) 
     * @param sm StateManagerImpl requested the transition. 
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState.
     */ 
    protected LifeCycleState transitionMakeNontransactional(StateManagerImpl sm, 
        Transaction tx) {
        throw new JDOUserException(msg.msg(
                    "EXC_DirtyInstance"), sm.getObject()); // NOI18N
    }


    /** 
     * Transitions LifeCycleState on call to makePersistent  
     *   
     * @see com.sun.persistence.support.PersistenceManager#makePersistent(Object pc)  
     * @param sm StateManagerImpl requested the transition.  
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionMakePersistent(StateManagerImpl sm) {
        return this;
    }

    /** 
     * Transitions LifeCycleState as a result of call to makePersistent  
     * of a referencing instance (persistence-by-reachability)
     *   
     * @see com.sun.persistence.support.PersistenceManager#makePersistent(Object pc)  
     * @param sm StateManagerImpl requested the transition.  
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionToAutoPersistent(StateManagerImpl sm) {
        return this;
    }

    /** 
     * Transitions LifeCycleState to transient for AutoPersistent instance
     * that is not referenced anymore (persistence-by-reachability)
     *   
     * @see com.sun.persistence.support.PersistenceManager#makePersistent(Object pc)  
     * @param sm StateManagerImpl requested the transition.  
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionFromAutoPersistent(StateManagerImpl sm) {
        return this;
    }

    /** 
     * Transitions LifeCycleState on call to deletePersistent  
     *   
     * @see com.sun.persistence.support.PersistenceManager#deletePersistent(Object pc)  
     * @param sm StateManagerImpl requested the transition.  
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionDeletePersistent(StateManagerImpl sm) {
        return this;
    }

    /** 
     * Transitions LifeCycleState on call to evict an instance.
     *   
     * @see com.sun.persistence.support.PersistenceManager#evict(Object pc)  
     * @param sm StateManagerImpl requested the transition.  
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionEvict(StateManagerImpl sm, Transaction tx) {
        return this;
    }

    /** 
     * Transitions LifeCycleState on call to refresh an instance.
     *   
     * @see com.sun.persistence.support.PersistenceManager#refresh(Object pc)  
     * @param sm StateManagerImpl requested the transition.  
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionRefresh(StateManagerImpl sm, Transaction tx) {
        return this;
    }

    /** 
     * Transitions LifeCycleState on call to retrieve an instance.
     *   
     * @see com.sun.persistence.support.PersistenceManager#retrieve(Object pc)  
     * @param sm StateManagerImpl requested the transition.  
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionRetrieve(StateManagerImpl sm, Transaction tx) {
        return this;
    }
    
    /** 
     * Transitions the lifecycle state as if the instance is retrieved from
     * the datastore, but use the specified field values instead of loading
     * them from the datastore.
     * @param sm StateManagerImpl requested the transition.  
     * @param tx Transaction associated with the hashing PersistenceManager
     * @param fields Indicates which fields should be replaced in the PC.
     * @param fieldManager FieldManager from which the field's value should be
     * @return new LifeCycleState. 
     */
    protected LifeCycleState transitionReplace(StateManagerImpl sm, 
        Transaction tx, int[] fields, FieldManager fieldManager) {

        sm.replaceUnloadedFields(fields, fieldManager);
        return this;
    }


    /** 
     * Transitions LifeCycleState on call to reload an instance. This is the
     * result of a request to validate non-transactional instance.
     *   
     * @see com.sun.persistence.support.PersistenceManager#getObjectById(Object oid, boolean validate)  
     * @param sm StateManagerImpl requested the transition.  
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionReload(StateManagerImpl sm, Transaction tx) {
        return this;
    }

    /** 
     * Transitions LifeCycleState on commit. Called by TransactionImpl.afterCompletion.
     *   
     * @param retainValues the value of the flag in the Transaction instance 
     * associated with the hashing PersistenceManager
     * @param sm StateManagerImpl requested the transition.  
     * @return new LifeCycleState. 
     */  
     protected LifeCycleState transitionCommit(boolean retainValues, StateManagerImpl sm) {
         throw new JDOFatalInternalException(msg.msg(
            "EXC_InconsistentState", // NOI18N
            "commit", this.toString())); // NOI18N
     }

    /** 
     * Transitions LifeCycleState on rollback. Called by TransactionImpl.afterCompletion.
     *   
     * @param restoreValues the value of the flag in the Transaction instance 
     * associated with the hashing PersistenceManager
     * @param sm StateManagerImpl requested the transition.  
     * @return new LifeCycleState. 
     */  
     protected LifeCycleState transitionRollback(boolean restoreValues, StateManagerImpl sm) {
         int newstate;

         if (restoreValues) {
              sm.restoreFields();
              newstate = P_NON_TX;
         } else {
              sm.clearFields();
              newstate = HOLLOW;
         }

         sm.reset();
         return changeState(newstate);

    }

    /** 
     * Performs state specific flush operation and transitions LifeCycleState depending
     * on the result.
     *   
     * @param loadedFields BitSet of fields loaded from the database.
     * @param dirtyFields BitSet of changed fields that are to be flushed and/or
     * verified against those in the database, if this <code>flush</code> is within the
     * context of an optimistic transaction.  After return, bits will remain set
     * for fields that were not flushed, and in such case the return will be
     * <code>StateManagerInternal.FLUSHED_PARTIAL</code>. If the <code>flush</code>
     * was not performed because of the dependency or other restrictions, the
     * return will be <code>StateManagerInternal.FLUSHED_NONE</code>. If operation
     * was successful, the return will be <code>StateManagerInternal.FLUSHED_COMPLETE</code>.
     * @param sm StateManagerImpl requested the transition.  
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState flush(BitSet loadedFields, BitSet dirtyFields,
        StoreManager srm, StateManagerImpl sm) {
        return this;
    }

    /** 
     * Transitions LifeCycleState on call to read a field.
     *   
     * @param sm StateManagerImpl requested the transition.  
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionReadField(StateManagerImpl sm, Transaction tx) {
        assertTransaction(tx.isActive());
        return this;
    }

    /** 
     * Transitions LifeCycleState on call to write a field.
     *   
     * @param sm StateManagerImpl requested the transition.  
     * @param tx Transaction associated with the hashing PersistenceManager
     * @return new LifeCycleState. 
     */  
    protected LifeCycleState transitionWriteField(StateManagerImpl sm, Transaction tx) {
        assertTransaction(tx.isActive());
        return this;
    }

    /**
     * Asserts that current transaction is active.
     * 
     * @throws JDOUserException if transaction is not active
     * @param transactionActive true if the current transaction is active
     */
    protected void assertTransaction(boolean transactionActive) {
        if (!transactionActive) {
            throw new JDOUserException(msg.msg(
                "EXC_TransactionNotActive")); // NOI18N
        }
    }
                                       
    /***************************************************************/
    /************** Methods that return values for flags ***********/
    /***************************************************************/
    
    /**
     * Return whether the object state is persistent.
     */  
    protected boolean isPersistent() {
        return isPersistent;
    }
    
    /**
     * Return whether the object state is transactional.
     */    
    protected boolean isTransactional() {
        return isTransactional;
    }
        
    /**
     * Return whether the object state is dirty, that is, the object has been 
     * changed (created, updated, deleted) in this Tx.
     */
    protected boolean isDirty() {
        return isDirty;
    }
    
    /**
     * Return whether the state represents a newly created object. 
     */
    protected boolean isNew() {
        return isNew;
    }    
    
    /**
     * Return whether the state represents a deleted object. 
     */
    protected boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Return whether the object can dynamically navigate to fields that are
     * not present.
     */
    protected boolean isNavigable() {
        return isNavigable;
    }

    /**
     * Return whether the object can be refreshed from the datastore.
     */
    protected boolean isRefreshable() {
        return isRefreshable;
    }

    /**
     * Return whether the beforeImage can be updated.
     */
    protected boolean isBeforeImageUpdatable() {
        return isBeforeImageUpdatable;
    }

    /**
     * Return whether the object has been flushed to the datastore.
     */
    protected boolean isFlushed() {
        return isFlushed;
    }

    /**
     * Return whether the object state is persistent by reachabilty only.
     */  
    protected boolean isAutoPersistent() {
        return isAutoPersistent;
    }
    
    /**
     * Return whether the object is stored in the datastore.
     */
    protected boolean isStored() {
        return isStored;
    }

    /**
     * Return whether the merge is needed.
     */
    protected boolean needMerge() {
        return needMerge;
    }

    /*************************************************************/
    /********************* Helper methods ************************/
    /*************************************************************/
    
    /** 
     * Changes Life Cycle State.
     *
     * @return new LifeCycleState. 
     */
    protected LifeCycleState changeState(int newStateType) {
        LifeCycleState lc = stateTypes[newStateType];
        return lc;
    }

    public String toString() {
        switch (stateType) {
            case HOLLOW: return "HOLLOW"; // NOI18N
            case P_NON_TX: return "P_NON_TX"; // NOI18N

            case T_CLEAN: return "T_CLEAN"; // NOI18N
            case T_DIRTY: return "T_DIRTY"; // NOI18N

            case P_CLEAN: return "P_CLEAN"; // NOI18N
            case P_CLEAN_TX: return "P_CLEAN_TX"; // NOI18N

            case P_DIRTY: return "P_DIRTY"; // NOI18N
            case P_DIRTY_FLUSHED: return "P_DIRTY_FLUSHED"; // NOI18N

            case P_NEW: return "P_NEW"; // NOI18N
            case P_NEW_FLUSHED: return "P_NEW_FLUSHED"; // NOI18N
            case P_NEW_FLUSHED_DELETED: return "P_NEW_FLUSHED_DELETED"; // NOI18N
            case P_NEW_FLUSHED_DIRTY: return "P_NEW_FLUSHED_DIRTY"; // NOI18N
            case P_NEW_DELETED: return "P_NEW_DELETED"; // NOI18N

            case AP_NEW: return "AP_NEW"; // NOI18N
            case AP_NEW_FLUSHED: return "AP_NEW_FLUSHED"; // NOI18N
            case AP_NEW_FLUSHED_DIRTY: return "AP_NEW_FLUSHED_DIRTY"; // NOI18N
            case AP_PENDING: return "AP_PENDING"; // NOI18N

            case P_DELETED: return "P_DELETED"; // NOI18N
            case P_DELETED_FLUSHED: return "P_DELETED_FLUSHED"; // NOI18N
        }

        return null;
    }

}


