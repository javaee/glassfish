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

/**
 * @author jwells
 *
 */
public interface WeakHashLRU<K> {
    /**
     * Adds the given key to the LRU.  It will
     * be placed at the MRU of the LRU.  If this
     * key already exists in the LRU it will
     * be moved to the MRU
     * 
     * @param key Must not be null
     */
    public void add(K key);
    
    /**
     * Tells if the given key is in the LRU
     * 
     * @param key The key to search for, may not be null
     * @return true if found, false otherwise
     */
    public boolean contains(K key);
    
    /**
     * Removes the given key from the LRU, if found
     * 
     * @param key The key to remove, may not be null
     * @return true if removed, false otherwise
     */
    public boolean remove(K key);
    
    /**
     * Returns the number of elements currently
     * in the clock.  References that have gone
     * away because they were weakly referenced
     * will not be counted in the size
     * 
     * @return The number of entries currently
     * in the LRU
     */
    public int size();
    
    /**
     * Removes the key that was Least
     * Recently Used
     * 
     * @return The key that was removed, or
     * null if the list is empty
     */
    public K remove();
    
    /**
     * Removes all entries from this LRU
     */
    public void clear();
    
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
