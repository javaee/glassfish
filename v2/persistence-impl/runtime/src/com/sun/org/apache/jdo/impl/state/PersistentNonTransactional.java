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
 *  PersistentNonTransactional.java    March 10, 2001
 */

package com.sun.org.apache.jdo.impl.state;


import com.sun.org.apache.jdo.state.FieldManager;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.Transaction;

/**
 * This class represents PersistentNonTransactional state specific state 
 * transitions as requested by StateManagerImpl. This state is the result 
 * of the following operations:
 *     - commit or rollback of a persistent instance when retainValues flag 
 * on the Transaction is set to true;
 *     -  non-transactional access of a Hollow instance;
 *     -  makeNontransactional call of a PersistentClean instance;
 *     -  refresh of a PersistentDirty instance in an optimistic transaction
 * or outside of an active transaction.
 *
 * @author Marina Vatkina
 */
class PersistentNonTransactional extends LifeCycleState {

    PersistentNonTransactional() {
        // these flags are set only in the constructor 
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass 
        // but their values are specific to subclasses)
        isPersistent = true;
        isTransactional = false;
        isDirty = false;
        isNew = false;
        isDeleted = false;

        isNavigable = true;
        isRefreshable = false;
        isBeforeImageUpdatable = false;
        isFlushed = true;

        stateType = P_NON_TX;
    }
    
   /**
    * @see LifeCycleState#transitionDeletePersistent(StateManagerImpl sm)
    */
    protected LifeCycleState transitionDeletePersistent(StateManagerImpl sm) {
        sm.createBeforeImage();
        sm.refresh();
        sm.registerTransactional();
        sm.preDelete();
        return changeState(P_DELETED);
    }

   /**
    * @see LifeCycleState#transitionMakeTransactional(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionMakeTransactional(StateManagerImpl sm,
        Transaction tx) { 
        if (tx.isActive()) {
            sm.registerTransactional();

            if (tx.getOptimistic()) {
                sm.createBeforeImage();
                return changeState(P_CLEAN_TX);
            } else { // pessimistic

                sm.refresh();
                // Need to remove from non-flushed cache
                sm.markAsFlushed();

                return changeState(P_CLEAN);
            }
        } else { // transaction not active
            return this;
        }
    }

   /**
    * @see LifeCycleState#transitionMakeNontransactional(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionMakeNontransactional(StateManagerImpl sm,
        Transaction tx) {
        return this;
    }  
 
   /**
    * @see LifeCycleState#transitionMakeTransient(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionMakeTransient(StateManagerImpl sm,
        Transaction tx) {
        sm.disconnect();
        return changeState(TRANSIENT);
    }

   /** 
    * @see LifeCycleState#transitionEvict(StateManagerImpl sm, Transaction tx) 
    */
    protected LifeCycleState transitionEvict(StateManagerImpl sm,
                                             Transaction tx) {
        sm.clearFields();
        sm.reset();
        return changeState(HOLLOW);
    }

  /**
    * @see LifeCycleState#transitionRefresh(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionRefresh(StateManagerImpl sm,
        Transaction tx) {
        //Do NOT create BeforeImage here as the call is intended to
        // synchronize state with the datastore.
        sm.refresh();
        return this;
    }

   /**
    * @see LifeCycleState#transitionReadField(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionReadField(StateManagerImpl sm, 
        Transaction tx) {

        if (!tx.getNontransactionalRead()) {
            assertTransaction(tx.isActive());
        }

        return transitionRead(sm, tx, false);
    }

    /**
    * Transtions the state on read or retrieve.
    * @see LifeCycleState#transitionReadField(StateManagerImpl sm,
    * Transaction tx)
    * @see LifeCycleState#transitionRetrieve(StateManagerImpl sm,
    * Transaction tx) 
    */
    private LifeCycleState transitionRead(StateManagerImpl sm,
                                Transaction tx, boolean retrieve) {

        if (tx.isActive() && !tx.getOptimistic()) {
            // This is a datastore transaction. 
            if (retrieve) {
                sm.loadUnloaded();
            } else {
                // If refresh, save current image for rollback.
                sm.createBeforeImage();
                sm.refresh();
            }
            sm.registerTransactional();

            // Need to remove from non-flushed cache
            sm.markAsFlushed();

            return changeState(P_CLEAN);
        } else if (retrieve) {
            // Always load all the fields on retrieve.
            sm.loadUnloaded();
        }
        return this;
    }


   /**
    * @see LifeCycleState#transitionWriteField(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionWriteField(StateManagerImpl sm, 
        Transaction tx) {

        if (tx.isActive()) {
            // This is the first change in the current transaction. Save image 
            // for rollback:
            sm.createBeforeImage();
            sm.registerTransactional();

            return changeState(P_DIRTY);

        } else if (!tx.getNontransactionalWrite()) {
            // This actually throws an exception
            assertTransaction(false);
        }

        return this;
    }


   /** 
    * @see LifeCycleState#transitionReload(StateManagerImpl sm, Transaction tx)
    */
    protected LifeCycleState transitionReload(StateManagerImpl sm,
        Transaction tx) {

        boolean transactional = (tx.isActive() && !tx.getOptimistic());

        // This transition will refresh the fields.. Save current image 
        // for rollback:
        if (transactional) {
            sm.createBeforeImage();
        }

        sm.refresh();

        if (transactional) {
            sm.registerTransactional();
            // Need to remove from non-flushed cache
            sm.markAsFlushed();

            return changeState(P_CLEAN);
        }
        return this;
    }

   /**   
    * @see LifeCycleState#transitionRetrieve(StateManagerImpl sm, Transaction tx)
    */   
    protected LifeCycleState transitionRetrieve(StateManagerImpl sm,
        Transaction tx) {

        return transitionRead(sm, tx, true);
    }

    /**
     * @see LifeCycleState#transitionReplace(StateManagerImpl sm, 
     * Transaction tx, int[] fields, FieldManager fieldManager)
     */
    protected LifeCycleState transitionReplace(StateManagerImpl sm, 
        Transaction tx, int[] fields, FieldManager fieldManager) {

        if (tx.isActive() && !tx.getOptimistic()) {
            sm.replaceFields(fields, fieldManager);
            sm.registerTransactional();

            // Need to remove from non-flushed cache
            sm.markAsFlushed();

            return changeState(P_CLEAN);
        } else {
            sm.replaceUnloadedFields(fields, fieldManager);
        }
        return this;
    }

   /**   
    * @see LifeCycleState#transitionRollback(boolean restoreValues,
    * StateManagerImpl sm)
    * @throws JDOFatalInternalException if called as this state
    * transition is not valid.
    */
    protected LifeCycleState transitionRollback(boolean restoreValues,
                                                StateManagerImpl sm) {
        throw new JDOFatalInternalException(msg.msg(
            "EXC_InconsistentState", // NOI18N
            "rollback", this.toString())); // NOI18N
    }

}



