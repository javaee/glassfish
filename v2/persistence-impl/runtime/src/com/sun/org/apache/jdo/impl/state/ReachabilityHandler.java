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
 * ReachabilityHandler.java
 *
 * Created on September 19, 2001, 9:29 AM
 */

package com.sun.org.apache.jdo.impl.state;

import java.util.*;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.*;
import com.sun.persistence.support.spi.PersistenceCapable;

/**
 * This is the helper class to process persistence-by-reachability requests
 * from the StateManager.
 *
 * @author  Marina Vatkina
 * @version 1.0
 */
class ReachabilityHandler {

    /** The singleton ReachabilityHandler instance. */    
    private static final ReachabilityHandler singleton = new ReachabilityHandler();

    /**
     * Map of possible processors.
     */
    private final HashMap processors = new HashMap();

    /**
     * I18N message handler
     */
    private final static I18NHelper msg = 
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.state.Bundle"); // NOI18N

    /**
     * Logger instance
     */  
    private static final Log logger = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.impl.state"); // NOI18N

    /** Constructs a new <code>ReachabilityHandler</code> without specific
     * parameters.
     */
    private ReachabilityHandler() { 
        /**  
         * Processors instances.
         */  
        MakePersistentProcessor _makePersistentProcessor = 
            new MakePersistentProcessor();
        AutoPersistentProcessor _autoPersistentProcessor = 
            new AutoPersistentProcessor();
        
        processors.put(Boolean.TRUE, _makePersistentProcessor);
        processors.put(Boolean.FALSE, _autoPersistentProcessor);
    }

    /** 
     * Get the ReachabilityHandler singleton instance.
     * @return an instance of ReachabilityHandler
     */    
    public static ReachabilityHandler getInstance()
    {
        return singleton;
    }

    /**
     * Process recursevely requests for persistence-by-reachability.
     * @param o Object to process.
     * @param pm the PersistenceManagerInternal instance associated with the 
     * caller.
     * @param type true if the request comes during the commit operation.
     */
    void process (Object o, 
        PersistenceManagerInternal pm, 
        boolean type) {

        Processor p = (Processor) processors.get(getBoolean(type));

        if (debugging())
            debug("process for " + o.getClass() + " with " + p.getClass().getName()); // NOI18N

        p.process(o, pm);
    }

    /**
     * Translates boolean value into Boolean.
     * @param type as boolean.
     * @return corresponding Boolean object.
     */
    private Boolean getBoolean(boolean type) {
        return ((type)? Boolean.TRUE :Boolean.FALSE);
    }

    /**
     * Tracing method
     * @param msg String to display
     */  
    private void debug(String msg) {
        logger.debug("In ReachabilityHandler " + msg); // NOI18N
    }

    /**
     * Verifies if debugging is enabled.
     * @return true if debugging is enabled.
     */
    private boolean debugging() {
        return logger.isDebugEnabled();
    }



    /** An abstract class that knows how process reachability requests.
     */
    abstract class Processor {
        /**
         * Processes reachability requests.
         * @param o Object to process.
         * @param pm the PersistenceManagerInternal instance associated with 
         * the caller.
         */
        abstract void process(Object o, PersistenceManagerInternal pm);
    }

    /** Processor for MakePersistent request.
     */
    class MakePersistentProcessor extends Processor {

        /** Transition Object or elements of a Collection or values and
         * keys of a Map to Persistent at commit (persistence-by-reachability)
         * @param o Object to process.
         * @param pm the PersistenceManagerInternal instance associated with 
         * the caller.
         */  
        void process(Object o, PersistenceManagerInternal pm) {
            try {
                if (java.util.Collection.class.isInstance(o)) {
                    pm.makePersistentAll(((Collection)o).toArray());

                } else if (java.util.Map.class.isInstance(o)) {
                    Map m = (Map)o;
                    try {
                        pm.makePersistentAll(m.keySet().toArray());
                    } catch (JDOException e) {
                        // Ignore all problems - some of the elements could be
                        // not PersistenceCapable
                    }
                    // Now process the values.
                    pm.makePersistentAll(m.values().toArray());

                } else if (com.sun.persistence.support.spi.PersistenceCapable.class.isInstance(o)) {
                    pm.makePersistent(o);

                } else {
                    Class c = o.getClass();
                    if (c.isArray() &&
                        PersistenceCapable.class.isAssignableFrom(
                        c.getComponentType())) {

                        pm.makePersistentAll((Object[])o);
                    } // else do nothing

                }
            } catch (JDOException e) {
                // Ignore all problems - some of the elements could be
                // not PersistenceCapable
            }
        }    
    }    

    /** Processor for MakeAutoPersistent request. 
     */ 
    class AutoPersistentProcessor extends Processor {
 
        /** Transition Object or elements of a Collection or values and
         * keys of a Map to auto-persistent inside an active transaction
         * (persistence-by-reachability)
         * @param o Object to process.
         * @param pm the PersistenceManagerInternal instance associated with 
         * the caller.
         */  
        void process(Object o, PersistenceManagerInternal pm) {
            StateManagerImpl sm = null;

            if (com.sun.persistence.support.spi.PersistenceCapable.class.isInstance(o)) {
                PersistenceCapable pc = (PersistenceCapable) o;
                sm = (StateManagerImpl) pm.findStateManager(pc);
                if (sm == null) {
                    sm = (StateManagerImpl) 
                        StateManagerFactory.newInstance(pc, pm);
                }
                sm.makeAutoPersistent();

            } else if (java.util.Collection.class.isInstance(o)) {
                processArray(((Collection)o).toArray(), pm);
 
            } else if (java.util.Map.class.isInstance(o)) {
                Map m = (Map)o;
                processArray(m.keySet().toArray(), pm);
                processArray(m.values().toArray(), pm);
 
            } else { // check for Arrays.
                Class c = o.getClass();
                if (c.isArray() && PersistenceCapable.class.isAssignableFrom(
                    c.getComponentType())) {
                    processArray((Object[])o, pm);
                } // else do nothing
 
            } // else do nothing
        }
 
        /** Processes Array of referenced objects for possible auto-persistence
         * (persistence-by-reachability).
         * @param o Array of referenced objects
         * @param pm PersistenceManagerInternal instance associated with the 
         * request.
         */
        private void processArray(Object[] o, 
            PersistenceManagerInternal pm) {
            if (o == null) {
                return;
            }
            for (int i = 0; i < o.length; i++) {
                if (o[i] != null) {
                    process(o[i], pm);
                } // else nothing to do.
            }
        }

    }
}
