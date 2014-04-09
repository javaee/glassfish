/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.hk2.utilities.cache.internal.LRUCacheCheapRead;

/**
 * A cache that contains a certain number of entries, and whose oldest accessed
 * entries are removed when removal is necessary.
 *
 * @author jwells
 * @param <K> The key type for this cache
 * @param <V> The value type for this cache
 *
 */
public abstract class LRUCache<K,V> {

    /**
     * Creates a cache with the given maximum cache size
     *
     * @param maxCacheSize The maximum number of entries in the cache, must be greater than 2
     * @return An LRUCache that can be used to quickly retrieve objects
     */
    public static <K,V> LRUCache<K,V> createCache(int maxCacheSize) {
        return new LRUCacheCheapRead<K,V>(maxCacheSize);
    }

    /**
     * Returns the value associated with the given key.  If there is no
     * value, returns null
     *
     * @param key Must be a non-null key, appropriate for use as the key to a hash map
     * @return The value associated with the key, or null if there is no such value
     */
    public abstract V get(K key);

    /**
     * Adds the given key and value pair into the cache
     *
     * @param key Must be a non-null key, appropriate for use as the key to a hash map
     * @param value Must be a non-null value
     * @return A cache entry that can be used to remove this entry from the cache.  Will not return null
     */
    public abstract CacheEntry put(K key, V value);

    /**
     * Clears all entries in the cache, for use when a known event makes the cache incorrect
     */
    public abstract void releaseCache();

    /**
     * Returns the maximum number of entries that will be stored in this cache
     *
     * @return The maximum number of entries that will be stored in this cache
     */
    public abstract int getMaxCacheSize();
    
    /**
     * This method will remove all cache entries for which this filter
     * matches
     * 
     * @param filter Entries in the cache that match this filter will
     * be removed from the cache.  If filter is null nothing
     * will be removed from the cache
     */
    public abstract void releaseMatching(CacheKeyFilter<K> filter);
}
