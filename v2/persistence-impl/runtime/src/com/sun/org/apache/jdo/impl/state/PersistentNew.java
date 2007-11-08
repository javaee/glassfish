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
 *  PersistentNew.java    March 10, 2001
 */

package com.sun.org.apache.jdo.impl.state;

import java.util.BitSet;

import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;


/**
 * This class represents PersistentNew state specific state transitions as requested
 * by StateManagerImpl. This state is a result of a call to makePersistent on a
 * transient instance.
 *
 * @author Marina Vatkina
 */
class PersistentNew extends LifeCycleState {

    PersistentNew() {
        // these flags are set only in the constructor 
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass 
        // but their values are specific to subclasses)
        isPersistent = true;
        isTransactional = true;
        isDirty = true;
        isNew = true;
        isDeleted = false;

        isNavigable = false;
        isRefreshable = false;
        isBeforeImageUpdatable = false;
        isFlushed = false;

        isStored = false;

        stateType = P_NEW;
    }

   /**
    * @see LifeCycleState#transitionDeletePersistent(StateManagerImpl sm)
    */
    protected LifeCycleState transitionDeletePersistent(StateManagerImpl sm) {
        // Remove from non-flushed chache.
        sm.getPersistenceManager().markAsFlushed(sm);
        sm.preDelete();
        return changeState(P_NEW_DELETED);
    }

   /**
    * This implementation differs from a generic version from the LifeCycleState as
    * the state transitions to transient.
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

   /**
    * @see LifeCycleState#flush(BitSet loadedFields, BitSet dirtyFields,
    *   StoreManager srm, StateManagerImpl sm)
    */
    protected LifeCycleState flush(BitSet loadedFields, BitSet dirtyFields,
        StoreManager srm, StateManagerImpl sm) { 

        int result = srm.insert(loadedFields, dirtyFields, sm); 

        switch(result) {
          case StateManagerInternal.FLUSHED_COMPLETE:
            sm.markAsFlushed();
            return changeState(P_NEW_FLUSHED);

          case StateManagerInternal.FLUSHED_PARTIAL:
            return changeState(P_NEW_FLUSHED_DIRTY);

          case StateManagerInternal.FLUSHED_NONE:
          default:
            return this;

        }
    }
}

