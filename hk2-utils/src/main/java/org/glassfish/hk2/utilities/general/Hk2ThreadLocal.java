/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.utilities.general;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * This is a poor mans version of a {@link java.lang.ThreadLocal} with
 * the one major upside of a {@link #removeAll()} method that
 * can be used to remove ALL instances of all thread locals on
 * ALL threads from any other thread.
 *
 * @author jwells
 *
 */
public class Hk2ThreadLocal<T> {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final WriteLock wLock = readWriteLock.writeLock();
    private final ReadLock rLock = readWriteLock.readLock();
    
    private final HashMap<Long, T> locals = new HashMap<Long, T>();
    
    /**
     * Returns the current thread's "initial value" for this
     * thread-local variable.  This method will be invoked the first
     * time a thread accesses the variable with the {@link #get}
     * method, unless the thread previously invoked the {@link #set}
     * method, in which case the <tt>initialValue</tt> method will not
     * be invoked for the thread.  Normally, this method is invoked at
     * most once per thread, but it may be invoked again in case of
     * subsequent invocations of {@link #remove} followed by {@link #get}.
     *
     * <p>This implementation simply returns <tt>null</tt>; if the
     * programmer desires thread-local variables to have an initial
     * value other than <tt>null</tt>, <tt>ThreadLocal</tt> must be
     * subclassed, and this method overridden.  Typically, an
     * anonymous inner class will be used.
     *
     * @return the initial value for this thread-local
     */
    protected T initialValue() {
        return null;
    }
    
    /**
     * Returns the value in the current thread's copy of this
     * thread-local variable.  If the variable has no value for the
     * current thread, it is first initialized to the value returned
     * by an invocation of the {@link #initialValue} method.
     *
     * @return the current thread's value of this thread-local
     */
    public T get() {
        long id = Thread.currentThread().getId();
        
        rLock.lock();
        try {
            if (locals.containsKey(id)) {
                return locals.get(id);
            }
        }
        finally {
            rLock.unlock();
        }
        
        // Did not previously get a value, so get it now
        // under write lock
        wLock.lock();
        try {
            if (locals.containsKey(id)) {
                return locals.get(id);
            }
            
            T initialValue = initialValue();
            locals.put(id, initialValue);
            
            return initialValue;
        }
        finally {
            wLock.unlock();
        }
        
    }
    
    /**
     * Sets the current thread's copy of this thread-local variable
     * to the specified value.  Most subclasses will have no need to
     * override this method, relying solely on the {@link #initialValue}
     * method to set the values of thread-locals.
     *
     * @param value the value to be stored in the current thread's copy of
     *        this thread-local.
     */
    public void set(T value) {
        long id = Thread.currentThread().getId();
        
        wLock.lock();
        try {
            locals.put(id, value);
        }
        finally {
            wLock.unlock();
        }
        
    }
    
    /**
     * Removes the current thread's value for this thread-local
     * variable.  If this thread-local variable is subsequently
     * {@linkplain #get read} by the current thread, its value will be
     * reinitialized by invoking its {@link #initialValue} method,
     * unless its value is {@linkplain #set set} by the current thread
     * in the interim.  This may result in multiple invocations of the
     * <tt>initialValue</tt> method in the current thread.
     */
     public void remove() {
         long id = Thread.currentThread().getId();
         
         wLock.lock();
         try {
             locals.remove(id);
         }
         finally {
             wLock.unlock();
         }
         
     }
     
     /**
      * Removes all threads current thread's value for this thread-local
      * variable.  If this thread-local variable is subsequently
      * {@linkplain #get read} by the current thread, its value will be
      * reinitialized by invoking its {@link #initialValue} method,
      * unless its value is {@linkplain #set set} by the current thread
      * in the interim.  This may result in multiple invocations of the
      * <tt>initialValue</tt> method in the current thread.
      */
      public void removeAll() {
          wLock.lock();
          try {
              locals.clear();
          }
          finally {
              wLock.unlock();
          }
          
      }
}
