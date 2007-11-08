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
 * StateManagerFactory.java
 *
 * Created on August 3, 2001
 */

package com.sun.org.apache.jdo.impl.state;


import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.persistence.support.spi.PersistenceCapable;

/**
 * This class is responsible for creation of new instances of a StateManagerInternal.
 * Called by CacheManagerImpl on call to makePersistent a Transient istance, or as a
 * result of a StoreManager request to process query or navigation.
 *
 * @author  mvatkina
 * @version 1.0
 */
public class StateManagerFactory {

   /**
    * Returns a new instance of a StateManagerInternal
    * @param pc instance of PersistenceCapable 
    * @param pm instance of PersistenceManagerInternal associated with this request
    */
    public static StateManagerInternal newInstance(PersistenceCapable pc, 
        PersistenceManagerInternal pm) {
        StateManagerImpl sm = new StateManagerImpl(pc, pm);
        return (StateManagerInternal)sm;
    }


   /** 
    * Returns a new instance of a StateManagerInternal. Called by the StoreManager
    * to process query results. 
    * @param userOid User provided Object Id 
    * @param internalOid Object Id that can be used internally
    * @param pm instance of PersistenceManagerInternal associated with this request 
    * @param clazz Class type for the PersistenceCapable to be created 
    */ 
    public static StateManagerInternal newInstance(Object userOid, Object internalOid,
        PersistenceManagerInternal pm, Class clazz) {
        StateManagerImpl sm = new StateManagerImpl(userOid, internalOid, pm, clazz);
        return (StateManagerInternal)sm;
    }

}
