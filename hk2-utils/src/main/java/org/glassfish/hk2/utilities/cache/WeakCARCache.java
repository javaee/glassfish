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
package org.glassfish.hk2.utilities.cache;

/**
 * A cache that uses the CAR algorithm to remove entries.
 * <p>
 * As a quick review, the CAR algorithm maintains four lists:<OL>
 * <LI>t1 - A clock of recently used keys with values</LI>
 * <LI>t2 - A clock of frequently used keys with values</LI>
 * <LI>b1 - A LRU of keys removed from t1 with no values</LI>
 * <LI>b2 - A LRU of keys removed from t2 with no values</LI>
 * </OL>
 * The sum of entries in t1 and t2 will never be higher than maxSize (c).
 * The sum of entries in all four lists will never be higher than 2c.
 * There is an adaptive parameter p which is the target size of the
 * t1 list which gets modified when a key is found on either the b1 or
 * b2 list.  This adaptive parameter essentially allows the algorithm
 * to adapt to the pattern of keys, whether there be more frequently used
 * keys or there be a pattern of keys used quickly but not again.
 * 
 * 
 * @author jwells
 *
 */
public interface WeakCARCache<K,V> {
    /**
     * The method used to get or add values to this cache
     * 
     * @param key The key to add to the cache.  If the value
     * is not found, then the computable will be called to
     * get the value.  May not be null
     * 
     * @return The calculated return value.  May not be null
     */
    public V compute(K key);
    
    /**
     * Returns the current number of keys in the cache.  Note
     * that the number of keys can be up to 2x the maximum size
     * of the cache
     * 
     * @return The current number of key entries in the cache
     */
    public int getKeySize();
    
    /**
     * Returns the current number of values in the cache.  Note
     * that the number of values can be up the maximum size
     * of the cache
     * 
     * @return The current number of value entries in the cache
     */
    public int getValueSize();
    
    /**
     * Returns the number of items in the T1 clock
     * 
     * @return The current number of items in the T1 clock
     */
    public int getT1Size();
    
    /**
     * Returns the number of items in the T2 clock
     * 
     * @return The current number of items in the T2 clock
     */
    public int getT2Size();
    
    /**
     * Returns the number of items in the B1 LRU
     * 
     * @return The current number of items in the B1 LRU
     */
    public int getB1Size();
    
    /**
     * Returns the number of items in the B2 LRU
     * 
     * @return The current number of items in the B2 LRU
     */
    public int getB2Size();
    
    
    /**
     * Clears the current cache, making the current size zero
     */
    public void clear();
    
    /**
     * Gets the current maximum size of the cache (the maximum
     * number of values that will be kept by the cache).  Note
     * that the number of keys kept will be 2x, where x is the
     * maximum size of the cache (see CAR algorithm which keeps
     * a key history)
     * 
     * @return The maximum size of the cache
     */
    public int getMaxSize();
    
    /**
     * The computable associated with this cache
     * 
     * @return The computable associated with this cache
     */
    public Computable<K,V> getComputable();
    
    /**
     * Used to remove a single key and value from the cache (if
     * the value is available)
     * @param key The key to remove. May not be null
     * @return true if a key was found and removed
     */
    public boolean remove(K key);
    
    /**
     * Releases all key/value pairs that match the filter
     * 
     * @param filter A non-null filter that can be used
     * to delete every key/value pair that matches the filter
     */
    public void releaseMatching(CacheKeyFilter<K> filter);
    
    /**
     * Causes stale references to be cleared from the data
     * structures.  Since this is a weak cache the references
     * can go away at any time, which happens whenever
     * any operation has been performed.  However, it may be
     * the case that no operation will be performed for a while
     * and so this method is provided to have a no-op operation
     * to call in order to clear out any stale references
     */
    public void clearStaleReferences();
    
    /**
     * Returns the value of p from the CAR algorithm, which
     * is the target size of the t1 clock
     * 
     * @return The current value of P
     */
    public int getP();
    
    /**
     * Returns a string that will contain all the elements of the four lists
     * 
     * @return A String containing the values of T1, T2, B1 and B2
     */
    public String dumpAllLists();

}
