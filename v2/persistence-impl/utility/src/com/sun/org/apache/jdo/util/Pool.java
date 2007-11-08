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

package com.sun.org.apache.jdo.util;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
* A general purpose pooling class.
*
* @author Dave Bristor
*/
public class Pool {
    // Objects in the pool
    private final Stack stack = new Stack();

    // Size of the pool
    private final int size;

    // Number of elements release by Pool for client use
    private int count = 0;
    
    /** Number of millis to wait for a free entry
     * Currently fixed; might be made configurable in future.
     */
    private int waitMillis = 1000;
    
    /** Number of times to wait for a free entry
     * Currently fixed; might be made configurable in future.
     */
    private int waitNumber = 5;

    /** I18N */
    private final static I18NHelper msg = I18NHelper.getInstance("com.sun.org.apache.jdo.util.Bundle"); // NOI18N

    // For debugging TBD!!!
    static final Log test = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.util"); // NOI18N

    /**
     * Constructs a pool that will limit the number of objects which it can
     * contain.
     * @param size The maximum number of items that can be put into the pool.
     */
    public Pool(int size) {
        this.size = size;
    }

    /**
     * Puts the given object into the pool, if there the pool has fewer than
     * the number of elements specifed when created.  If the pool is full,
     * blocks until an element is removed. 
     * @param o Object to be put in the pool.
     * @throws InterruptedException
     */
    public synchronized void put(Object o) throws InterruptedException {
       boolean debug = test.isDebugEnabled();
        
       if (debug) {
           test.debug("Pool.put: " + o); // NOI18N
       }
       
       if (count > size || count < 0) {
           if (debug) {
               test.debug("Pool: count " + count + // NOI18N
                            " out of range 0-" + size); // NOI18N
           }
           throw new RuntimeException(
               msg.msg(
                   "EXC_CountOutOfRange", // NOI18N
                   new Integer(count).toString(),
                   new Integer(size).toString()));
       }
       
       if (stack.contains(o)) {
           if (debug) {
               test.debug("Pool: duplicate object"); // NOI18N
           }
           throw new RuntimeException(
               msg.msg(
                   "EXC_DuplicateObject", o)); // NOI18N
       }

       while (count == size) {
           if (debug) {
               test.debug("Pool.put: block"); // NOI18N
           }
           wait();
       }
       stack.push(o);
       ++count;
       notify();
    }

    /**
     * Gets an object from the pool, if one is available.  If an object is not
     * available, waits until one is.  The waiting is governed by two
     * variables, which are currently fixed: waitMillis and waitNumber.
     * If no object is available from the pool within (waitNumber) times
     * (waitMillis) milliseconds, then a RuntimeException is thrown.
     * In future, the waitMillis and waitNumber should be configurable.
     * @return An object from the pool.
     */
    public synchronized Object get() throws InterruptedException {
        boolean debug = test.isDebugEnabled();
        Object rc = null;

        if (count > size || count < 0) {
            if (debug) {
                test.debug("Pool: count " + count + // NOI18N
                           " out of range 0-" + size); // NOI18N
            }
            throw new RuntimeException(
                msg.msg(
                    "EXC_CountOutOfRange", // NOI18N
                    new Integer(count).toString(),
                    new Integer(size).toString()));
        }

        int timeouts = 0;
        while (count == 0 && timeouts++ < waitNumber) {
            if (debug) {
                test.debug("Pool.get: block " + timeouts); // NOI18N
            }
            wait(waitMillis);
        }
        if (timeouts >= waitNumber) {
            throw new RuntimeException(
                msg.msg("EXC_PoolGetTimeout")); // NOI18N
        }
        rc = stack.pop();
        --count;
        notify();
        if (debug) {
            test.debug("Pool.get: " + rc); // NOI18N
        }
        return rc;
    }
}
