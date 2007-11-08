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
 *  PersistentCleanTransactional.java    August 13, 2001
 */

package com.sun.org.apache.jdo.impl.state;

import java.util.BitSet;


import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.persistence.support.Transaction;


/**
 * This class represents PersistentCleanTransactional state specific state 
 * transitions as requested by StateManagerImpl. This state differs from 
 * PersistentClean in that it is the result of call to makeTransactional
 * of a PersistentNonTransactional instance in an active optimistic transaction.
 * This state verifies itself at flush.
 *
 * @author Marina Vatkina
 */
class PersistentCleanTransactional extends PersistentClean {

    PersistentCleanTransactional() {
        isFlushed = false;
        stateType = P_CLEAN_TX;
    }

   /**
    * @see LifeCycleState#transitionWriteField(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionWriteField(StateManagerImpl sm,
        Transaction tx) {
        return changeState(P_DIRTY);
    }

   /** 
    * @see LifeCycleState#flush(BitSet loadedFields, BitSet dirtyFields,
    *   StoreManager srm, StateManagerImpl sm)
    */
    protected LifeCycleState flush(BitSet loadedFields, BitSet dirtyFields,
        StoreManager srm, StateManagerImpl sm) {
        if (srm.verifyFields(loadedFields, dirtyFields, sm) ==
            StateManagerInternal.FLUSHED_COMPLETE) {
            sm.markAsFlushed();
            return changeState(P_CLEAN);
        }
        return this;
    }

}
