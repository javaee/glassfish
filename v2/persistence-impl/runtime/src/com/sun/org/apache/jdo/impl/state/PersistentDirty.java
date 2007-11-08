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
 *  PersistentDirty.java    March 10, 2001
 */

package com.sun.org.apache.jdo.impl.state;

import java.util.BitSet;


import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.Transaction;


/**
 * This class represents PersistentDirty state specific state transitions as requested
 * by StateManagerImpl. This state is a result of a write operation on a persistent
 * instance.
 *
 * @author Marina Vatkina
 */
class PersistentDirty extends LifeCycleState {

    PersistentDirty() {
        // these flags are set only in the constructor 
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass 
        // but their values are specific to subclasses)
        isPersistent = true;
        isTransactional = true;
        isDirty = true;
        isNew = false;
        isDeleted = false;

        isNavigable = true;
        isRefreshable = true;
        isBeforeImageUpdatable = true;
        isFlushed = false;
        
        stateType =  P_DIRTY;
    }

   /**
    * @see LifeCycleState#transitionDeletePersistent(StateManagerImpl sm)
    */
    protected LifeCycleState transitionDeletePersistent(StateManagerImpl sm) {    
        sm.preDelete();
        return changeState(P_DELETED);
    }
    
   /**
    * @see LifeCycleState#transitionRefresh(StateManagerImpl sm, Transaction tx)
    */
    protected LifeCycleState transitionRefresh(StateManagerImpl sm,
        Transaction tx) {
        sm.unsetBeforeImage();
        sm.refresh();

        if (tx.isActive() && !tx.getOptimistic()) {
            return changeState(P_CLEAN);
        } else {
            sm.registerNonTransactional();
        }
        return changeState(P_NON_TX);
    }

   /**
    * @see LifeCycleState#transitionRetrieve(StateManagerImpl sm, Transaction tx)
    */
    protected LifeCycleState transitionRetrieve(StateManagerImpl sm,
        Transaction tx) {

        sm.loadUnloaded();
        return this;
    }

   /**
    * @see LifeCycleState#flush(BitSet loadedFields, BitSet dirtyFields,
    *   StoreManager srm, StateManagerImpl sm) 
    */ 
    protected LifeCycleState flush(BitSet loadedFields, BitSet dirtyFields,
        StoreManager srm, StateManagerImpl sm) {

        if (srm.update(loadedFields, dirtyFields, sm) ==
            StateManagerInternal.FLUSHED_COMPLETE) {

            sm.markAsFlushed();
            return changeState(P_DIRTY_FLUSHED);
        }

        return this;
    }
}
