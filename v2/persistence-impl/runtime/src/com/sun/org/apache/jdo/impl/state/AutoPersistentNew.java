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
 *  AutoPersistentNew.java    September 4, 2001
 */

package com.sun.org.apache.jdo.impl.state;

import java.util.BitSet;


import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.persistence.support.Transaction;


/**
 * This class represents AutoPersistentNew state specific state transitions as 
 * requested by StateManagerImpl. This state is a result of a call to makePersistent 
 * on a transient instance that references this instance (persistence-by-reachability).
 *
 * @author Marina Vatkina
 */
class AutoPersistentNew extends PersistentNew {

    AutoPersistentNew() {
        super();

        isAutoPersistent = true;
        stateType = AP_NEW;
    }

   /**
    * @see LifeCycleState#transitionMakePersistent(StateManagerImpl sm)
    */
    protected LifeCycleState transitionMakePersistent(StateManagerImpl sm) {
        return changeState(P_NEW);
    }

   /**
    * @see LifeCycleState#flush(BitSet loadedFields, BitSet dirtyFields,
    *   StoreManager srm, StateManagerImpl sm)
    */
    protected LifeCycleState flush(BitSet loadedFields, BitSet dirtyFields,
        StoreManager srm, StateManagerImpl sm) { 
        if (sm.getPersistenceManager().insideCommit()) {
            // Nothing to do - state is unreachable at commit.
            sm.markAsFlushed();
            return changeState(AP_PENDING);
        }

        int result = srm.insert(loadedFields, dirtyFields, sm); 

        switch (result) {
          case StateManagerInternal.FLUSHED_COMPLETE:
            // Do NOT mark as flushed - it needs to be available for possible delete
            // at commit.
            return changeState(AP_NEW_FLUSHED);

          case StateManagerInternal.FLUSHED_PARTIAL:
            return changeState(AP_NEW_FLUSHED_DIRTY);

          case StateManagerInternal.FLUSHED_NONE:
          default:
            return this;

        }
    }

   /**
    * @see LifeCycleState#transitionFromAutoPersistent(StateManagerImpl sm)
    */
    protected LifeCycleState transitionFromAutoPersistent(StateManagerImpl sm) {
        sm.disconnect();
        return changeState(TRANSIENT);
    }

   /**
    * Differs from the generic implementation in LifeCycleState that state transitions
    * to Transient and keeps all fields. Called only if state becomes not reachable.
    * @see LifeCycleState#transitionCommit(boolean retainValues, StateManagerImpl sm)
    */
    protected LifeCycleState transitionCommit(boolean retainValues, StateManagerImpl sm) {
        sm.disconnect();
        return changeState(TRANSIENT);
    }

   /**
    * Differs from the generic implementation in LifeCycleState that state transitions
    * to Transient and restores all fields. Called only if state becomes not reachable.
    * @see LifeCycleState#transitionRollback(boolean restoreValues, StateManagerImpl sm)
    */
    protected LifeCycleState transitionRollback(boolean restoreValues, StateManagerImpl sm) {
        if (restoreValues) {
            sm.restoreFields();
        } else {
            sm.unsetSCOFields();
        }
        sm.disconnect();
        return changeState(TRANSIENT);
    }

}

