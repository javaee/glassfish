/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities.cache.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.hk2.utilities.cache.CacheEntry;
import org.glassfish.hk2.utilities.cache.CacheKeyFilter;
import org.glassfish.hk2.utilities.cache.LRUCache;

/**
 * LRU Cache implementation that relies on entries that keep
 * last hit (get/put) timestamp in order to be able to remove least recently
 * accessed items when running out of cache capacity.
 * Item order is not being maintained during regular cache usage (mainly reads).
 * This makes pruning operation expensive in exchange
 * for making reads quite cheap in a multi-threaded environment.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @param <K> The key of the cache
 * @param <V> The values in the cache
 */
public class LRUCacheCheapRead<K,V> extends LRUCache<K,V> {

    final Object prunningLock = new Object();

    final int maxCacheSize;
    Map<K,CacheEntryImpl<K, V>> cache = new ConcurrentHashMap<K, CacheEntryImpl<K,V>>();

    /**
     * Create new cache with given maximum capacity.
     *
     * @param maxCacheSize Maximum number of items to keep.
     */
    public LRUCacheCheapRead(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public V get(K key) {
        final CacheEntryImpl<K, V> entry = cache.get(key);
        return entry != null ? entry.hit().value : null;
    }

    @Override
    public CacheEntry put(K key, V value) {
        CacheEntryImpl<K, V> entry = new CacheEntryImpl<K, V>(key, value, this);
        synchronized (prunningLock) {
            if (cache.size() + 1 > maxCacheSize) {
                removeLRUItem();
            }
            cache.put(key, entry);
            return entry;
        }
    }

    @Override
    public void releaseCache() {
        cache.clear();
    }

    @Override
    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    @Override
    public void releaseMatching(CacheKeyFilter<K> filter) {
        if (filter == null) return;

        for (Map.Entry<K, CacheEntryImpl<K,V>> entry : (new HashMap<K, CacheEntryImpl<K,V>>(cache)).entrySet()) {
            if (filter.matches(entry.getKey())) {
                entry.getValue().removeFromCache();
            }
        }

    }

    /**
     * Remove least recently used item form the cache.
     * No checks are done here. The method just tries to remove the least recently used
     * cache item. An exception will be thrown if the cache is empty.
     */
    private void removeLRUItem() {
        final Collection<CacheEntryImpl<K, V>> values = cache.values();
        Collections.min(values, COMPARATOR).removeFromCache();
    }

    private static final CacheEntryImplComparator COMPARATOR = new CacheEntryImplComparator();

    private static class CacheEntryImplComparator implements Comparator<CacheEntryImpl<?,?>> {

        @Override
        public int compare(CacheEntryImpl<?,?> first, CacheEntryImpl<?,?> second) {
            final long diff = first.lastHit - second.lastHit;
            return diff > 0 ? 1 : diff == 0 ? 0 : -1;
        }

    }

    private static class CacheEntryImpl<K,V> implements CacheEntry {

        final K key;
        final V value;
        final LRUCacheCheapRead<K, V> parent;
        long lastHit;

        public CacheEntryImpl(K k, V v, LRUCacheCheapRead<K,V> cache) {
            this.parent = cache;
            this.key = k;
            this.value = v;
            this.lastHit = System.nanoTime();
        }

        @Override
        public void removeFromCache() {
            parent.cache.remove(key);
        }

        public CacheEntryImpl<K,V> hit() {
            this.lastHit = System.nanoTime();
            return this;
        }
    }


}
