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
 *  PersistentNewDeleted.java    March 10, 2001
 */

package com.sun.org.apache.jdo.impl.state;

import com.sun.persistence.support.JDOUserException;
import com.sun.persistence.support.Transaction;

/**
 * This class represents PersistentNewDeleted state specific state
 * transitions as requested by StateManagerImpl. This state differs from
 * PersistentNew state as it is result of a call to deletePersistent
 * for the same instance.
 *
 * @author Marina Vatkina
 */
class PersistentNewDeleted extends LifeCycleState {

    PersistentNewDeleted() {
        // these flags are set only in the constructor 
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass 
        // but their values are specific to subclasses)
        isPersistent = true;
        isTransactional = true;
        isDirty = true;
        isNew = true;
        isDeleted = true;

        isNavigable = false;
        isRefreshable = false;
        isBeforeImageUpdatable = false;
        isFlushed = true;

        isStored = false;

        // The following flag allows merge 
        needMerge = false;

        stateType = P_NEW_DELETED;
    }

   /**
    * @see LifeCycleState#transitionReadField(StateManagerImpl sm, Transaction tx)
    * @throws JDOUserException on read access.
    */
    protected LifeCycleState transitionReadField(StateManagerImpl sm, 
        Transaction tx) {
        // Cannot read a deleted object
        throw new JDOUserException(msg.msg(
                "EXC_AccessDeletedField")); // NOI18N
    }

   /**
    * @see LifeCycleState#transitionWriteField(StateManagerImpl sm, Transaction tx)
    * @throws JDOUserException on write access.
    */
    protected LifeCycleState transitionWriteField(StateManagerImpl sm,
        Transaction tx) {
        // Cannot update a deleted object
        throw new JDOUserException(msg.msg(
                "EXC_AccessDeletedField")); // NOI18N
    }

   /**
    * @see LifeCycleState#transitionCommit(boolean retainValues, StateManagerImpl sm)
    */
    protected LifeCycleState transitionCommit(boolean retainValues, StateManagerImpl sm) {
        sm.clearFields();
        sm.disconnect();
        return changeState(TRANSIENT);
    }
   /**
    * Differs from the generic implementation in LifeCycleState that state transitions
    * to Transient.
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
