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

import java.util.Map;

/**
 * This is a clock (if non-empty the next verb will always return a new value
 * in a cycle) that can also get values in O(1) complexity.  This HashClock
 * also has Weak key references, so if the key becomes unavailable it
 * will not be retrievable from the get operation and the next operation
 * will remove it from the clock
 * 
 * @author jwells
 *
 */
public interface WeakHashClock<K,V> {
    /**
     * Adds the given pair to the clock.  It will
     * be placed at the current tail of the clock
     * 
     * @param key Must not be null
     * @param value May not be null
     */
    public void put(K key, V value);
    
    /**
     * Gets the given key, returning null
     * if not found
     * 
     * @param key The key to search for, may not be null
     * @return The value found, or null if not found
     */
    public V get(K key);
    
    /**
     * Removes the given key from the clock, if found
     * 
     * @param key The key to remove, may not be null
     * @return The value removed if found, or null if not found
     */
    public V remove(K key);
    
    /**
     * Returns the number of elements currently
     * in the clock.  This size will contain the elements
     * of the clock, including any weak references that
     * have gone out of scope but which have not yet
     * been removed via the next operation
     * 
     * @return The number of entries currently
     * in the clock
     */
    public int size();
    
    /**
     * Returns the next key/value pair in the clock,
     * or null if the clock has no members.  This
     * will advance the head and tail of the clock
     * to the next element.  If the WeakReference
     * for the returned element is null then this
     * element will also have been removed from
     * the clock by this operation
     * 
     * @return The next key/value pair in the 
     */
    public Map.Entry<K, V> next();
    
    /**
     * Causes stale references to be cleared from the data
     * structures.  Since this is a weak clock the references
     * can go away at any time, which happens whenever
     * any operation has been performed.  However, it may be
     * the case that no operation will be performed for a while
     * and so this method is provided to have a no-op operation
     * to call in order to clear out any stale references
     */
    public void clearStaleReferences();
}
