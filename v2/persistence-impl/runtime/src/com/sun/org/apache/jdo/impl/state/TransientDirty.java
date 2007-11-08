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
 *  TransientDirty.java    August 10, 2001
 */

package com.sun.org.apache.jdo.impl.state;

import com.sun.persistence.support.Transaction;

/**
 * This class represents TransientDirty state specific state transitions as
 * requested by StateManagerImpl. This state is the result of a wrie operation
 * on a TransientClean instance
 *
 * @author Marina Vatkina
 */
class TransientDirty extends LifeCycleState {

    TransientDirty() {
        // these flags are set only in the constructor 
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass 
        // but their values are specific to subclasses)
        isPersistent = false;
        isTransactional = true;
        isDirty = true;
        isNew = false;
        isDeleted = false;

        isNavigable = true;
        isRefreshable = true;
        isBeforeImageUpdatable = true;
        isFlushed = false;
        
        stateType =  T_DIRTY;
    }

   /**
    * @see LifeCycleState#transitionMakeTransient(StateManagerImpl sm,
    * Transaction tx)
    */
    protected LifeCycleState transitionMakeTransient(StateManagerImpl sm,
        Transaction tx) { 
        return this;
    } 
 
   /**
    * @see LifeCycleState#transitionMakePersistent(StateManagerImpl sm)
    */
    protected LifeCycleState transitionMakePersistent(StateManagerImpl sm) {    

        sm.registerTransactional();
        return changeState(P_NEW);
    }

   /**
    * @see LifeCycleState#transitionToAutoPersistent(StateManagerImpl sm)
    */
    protected LifeCycleState transitionToAutoPersistent(StateManagerImpl sm) {    
        sm.registerTransactional();
        return changeState(AP_NEW);
    }

   /**
    * @see LifeCycleState#transitionCommit(boolean retainValues, StateManagerImpl sm)
    */
    protected LifeCycleState transitionCommit(boolean retainValues, StateManagerImpl sm) {     
        sm.reset();
        return changeState(T_CLEAN);
    } 
 
   /**
    * @see LifeCycleState#transitionRollback(boolean restoreValues, StateManagerImpl sm)
    */
    protected LifeCycleState transitionRollback(boolean restoreValues, StateManagerImpl sm)  {      
        sm.restoreFields();
        sm.reset();
        return changeState(T_CLEAN); 
    }  
  
}
