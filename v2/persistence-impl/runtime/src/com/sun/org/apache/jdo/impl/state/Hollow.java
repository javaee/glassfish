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
 *  Hollow.java    March 10, 2001
 */

package com.sun.org.apache.jdo.impl.state;


import com.sun.org.apache.jdo.state.FieldManager;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.Transaction;

/**
 * This class represents Hollow state specific state transitions as requested
 * by StateManagerImpl. This state is the result of commit or rollback of a
 * persistent instance when retainValues flag on the Transaction is set to 
 * false.
 *
 * @author Marina Vatkina
 */
class Hollow extends LifeCycleState {

    Hollow() {
        // these flags are set only in the constructor 
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass 
        // but their values are specific to subclasses)
        isPersistent = true;
        isTransactional = false;
        isDirty = false;
        isNew = false;
        isDeleted = false;

        isBeforeImageUpdatable = false;
        isRefreshable = false;
        isFlushed = true;
        
        stateType = HOLLOW;
    }

   /**
    * @see LifeCycleState#transitionDeletePersistent(StateManagerImpl sm)
    */
    protected LifeCycleState transitionDeletePersistent(StateManagerImpl sm) {

        sm.refresh();
        sm.registerTransactional();
        sm.preDelete();
        return changeState(P_DELETED);
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
      Transaction tx)
    */
    protected LifeCycleState transitionMakeTransient(StateManagerImpl sm, 
        Transaction tx) { 
        sm.disconnect();
        return changeState(TRANSIENT);
    }

   /**
    * @see LifeCycleState#transitionMakeTransactional(
    * StateManagerImpl sm, Transaction tx)
    */
    protected LifeCycleState transitionMakeTransactional(StateManagerImpl sm, 
        Transaction tx) {

        if (tx.isActive()) {
            sm.registerTransactional();
            sm.refresh();
            sm.postLoad();
        
            if (tx.getOptimistic()) {
                sm.createBeforeImage();
                return changeState(P_CLEAN_TX);
            } else { // pessimistic transaction
                sm.markAsFlushed();
                return changeState(P_CLEAN);
            }
        } else { // transaction not active
            return this;
        }
    }

   /**
    * @see LifeCycleState#transitionReload(StateManagerImpl sm, Transaction tx)
    */
    protected LifeCycleState transitionReload(StateManagerImpl sm, 
        Transaction tx) {

        sm.refresh();
        return this.transitionLoad(sm, tx);
    }

   /**
    * @see LifeCycleState#transitionRetrieve(StateManagerImpl sm, Transaction tx)
    */
    protected LifeCycleState transitionRetrieve(StateManagerImpl sm, 
        Transaction tx) {

        sm.loadUnloaded();
        // This state behaves the same on retrieve request and reload:
        return this.transitionLoad(sm, tx);
    }
    
    /**
     * @see LifeCycleState#transitionReplace(StateManagerImpl sm, 
     * Transaction tx, int[] fields, FieldManager fieldManager)
     */
    protected LifeCycleState transitionReplace(StateManagerImpl sm, 
        Transaction tx, int[] fields, FieldManager fieldManager) {

        sm.replaceFields(fields, fieldManager);
        return this.transitionLoad(sm, tx);
    }
    
   /**
    * @see LifeCycleState#transitionReadField(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionReadField(StateManagerImpl sm, 
        Transaction tx) {

        boolean transactionActive = tx.isActive();
        if (!tx.getNontransactionalRead()) {
            assertTransaction(transactionActive);
        }

        sm.postLoad();
        if (!tx.getOptimistic() && transactionActive) {
            sm.registerTransactional();
            // Need to remove from non-flushed cache
            sm.markAsFlushed();
            return changeState(P_CLEAN);
        }

        sm.registerNonTransactional();
        return changeState(P_NON_TX);
    }

   /**
    * @see LifeCycleState#transitionWriteField(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionWriteField(StateManagerImpl sm, 
        Transaction tx) {

        boolean transactionActive = tx.isActive();
        if (!tx.getNontransactionalWrite()) {
            assertTransaction(transactionActive);
        }

        sm.postLoad();
        if(transactionActive) {
            //sm.createBeforeImage();
            sm.registerTransactional();
            return changeState(P_DIRTY);
        } else {
            sm.registerNonTransactional();
            return changeState(P_NON_TX);
        }
            
    }

   /**
    * This state transition is invalid for the rollback.
    * @see LifeCycleState#transitionRollback(boolean restoreValues,
    * StateManagerImpl sm)
    */
    protected LifeCycleState transitionRollback(boolean restoreValues,
                                                StateManagerImpl sm) {
        throw new JDOFatalInternalException(msg.msg(
            "EXC_InconsistentState", // NOI18N
            "rollback", this.toString())); // NOI18N
    }

   /**
    * Transitions LifeCycle state on reload or retrieve call.
    */
   private LifeCycleState transitionLoad(StateManagerImpl sm, Transaction tx) {
        sm.postLoad();
        if (tx.isActive() && !tx.getOptimistic()) {
            sm.registerTransactional();
            // Need to remove from non-flushed cache
            sm.markAsFlushed();

            return changeState(P_CLEAN);
        } else {
            sm.registerNonTransactional();
        }
        return changeState(P_NON_TX);
    }
}

