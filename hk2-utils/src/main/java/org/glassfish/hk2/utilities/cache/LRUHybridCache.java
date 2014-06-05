/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Hybrid cache that allows explicit removals of included entries as well
 * as implicit removal of entries that have been least recently accessed.
 * The implicit removal happens only in case the internal cache runs out of space.
 * Maximum number of items that can be kept in the cache is invariant for a single cache instance,
 * and can be set in the cache constructor. The cache deals with {@link HybridCacheEntry}
 * items than could even instruct the cache to drop them as they get computed.
 * The cache will still make sure such items get computed just once in given time (based on computation length)
 * for given key.
 *
 * Desired value will only be computed once and computed value stored in the cache.
 * The implementation is based on an example from the "Java Concurrency in Practice" book
 * authored by Brian Goetz and company.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @param <K> The type for the keys in the cache
 * @param <V> The type for the values in the cache
 */
public class LRUHybridCache<K,V> implements Computable<K, HybridCacheEntry<V>> {

    /**
     * Should a cycle be detected during computation of a value
     * for given key, this interface allows client code to register
     * a callback that would get invoked in such a case.
     *
     * The cycle is defined as follows. If any thread starts computation
     * for given key and code from the very same thread requests the computed value
     * before the computation ends, a cycle is detected.
     * Registered cycle handler is then given a chance to handle the cycle and
     * throw a runtime exception if appropriate.
     *
     * @param <K> Key type.
     */
    public interface CycleHandler<K> {

        /**
         * Handle cycle that was detected while computing a cache value
         * for given key. This method would typically just throw a runtime exception.
         *
         * @param key instance that caused the cycle.
         */
        public void handleCycle(K key);
    }

    private final LRUHybridCache.CycleHandler<K> cycleHandler;

    /**
     * Helper class, that remembers the future task origin thread, so that cycles could be detected.
     */
    private class OriginThreadAwareFuture implements Future<HybridCacheEntry<V>> {
        private final K key;
        private final FutureTask<HybridCacheEntry<V>> future;
        private volatile long threadId;
        private volatile long lastHit;

        OriginThreadAwareFuture(LRUHybridCache<K, HybridCacheEntry<V>> cache, final K key) {
            this.key = key;
            this.threadId = Thread.currentThread().getId();
            Callable<HybridCacheEntry<V>> eval = new Callable<HybridCacheEntry<V>>() {
                @Override
                public HybridCacheEntry<V> call() throws Exception {
                    try {
                        final HybridCacheEntry<V> result = computable.compute(key);
                        return result;
                    } finally {
                        threadId = -1;
                    }
                }
            };
            this.future = new FutureTask<HybridCacheEntry<V>>(eval);
            this.lastHit = System.nanoTime();
        }

        @Override
        public int hashCode() {
            return future.hashCode();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LRUHybridCache<K,V>.OriginThreadAwareFuture other = (LRUHybridCache.OriginThreadAwareFuture) obj;
            if (this.future != other.future && (this.future == null || !this.future.equals(other.future))) {
                return false;
            }
            return true;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public HybridCacheEntry<V> get() throws InterruptedException, ExecutionException {
            return future.get();
        }

        @Override
        public HybridCacheEntry<V> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(timeout, unit);
        }

        public void run() {
            future.run();
        }
    }
    
    private static final LRUHybridCache.CycleHandler<Object> EMPTY_CYCLE_HANDLER = new LRUHybridCache.CycleHandler<Object>() {
        @Override
        public void handleCycle(Object key) {
        }
    };

    private final ConcurrentHashMap<K, LRUHybridCache<K,V>.OriginThreadAwareFuture> cache = new ConcurrentHashMap<K, LRUHybridCache<K,V>.OriginThreadAwareFuture>();
    private final Computable<K, HybridCacheEntry<V>> computable;

    private final Object prunningLock = new Object();
    private final int maxCacheSize;

    /**
     * Create new cache with given computable to compute values.
     * @param maxCacheSize The maximum number of entries in the cache
     * @param computable The thing that can create the entry
     */
    @SuppressWarnings("unchecked")
    public LRUHybridCache(int maxCacheSize, Computable<K, HybridCacheEntry<V>> computable) {
        this(maxCacheSize, computable, (LRUHybridCache.CycleHandler<K>) EMPTY_CYCLE_HANDLER);
    }

    /**
     * Create new cache with given computable and cycleHandler.
     *
     * @param maxCacheSize The maximum number of entries in the cache
     * @param computable The thing that can create the entry
     * @param cycleHandler What to do if a cycle is detected
     */
    public LRUHybridCache(int maxCacheSize, Computable<K,HybridCacheEntry<V>> computable, LRUHybridCache.CycleHandler<K> cycleHandler) {
        this.maxCacheSize = maxCacheSize;
        this.computable = computable;
        this.cycleHandler = cycleHandler;
    }

    private final class HybridCacheEntryImpl<V1> implements HybridCacheEntry<V1> {

        private final K key;
        private final V1 value;
        private final boolean dropMe;

        public HybridCacheEntryImpl(K key, V1 value, boolean dropMe) {
            this.key = key;
            this.value = value;
            this.dropMe = dropMe;
        }

        @Override
        public V1 getValue() {
            return value;
        }

        @Override
        public boolean dropMe() {
            return dropMe;
        }

        @Override
        public void removeFromCache() {
            LRUHybridCache.this.remove(key);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + (this.key != null ? this.key.hashCode() : 0);
            return hash;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HybridCacheEntryImpl<V1> other = (HybridCacheEntryImpl<V1>) obj;
            if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
                return false;
            }
            return true;
        }
    }

    /**
     * Create cache entry for given values.
     *
     * @param k cache entry key.
     * @param v cache entry value.
     * @param dropMe should this entry be kept in the cache this must be set to false.
     * @return an instance of cache entry.
     */
    public HybridCacheEntry<V> createCacheEntry(final K k, final V v, final boolean dropMe) {
        return new HybridCacheEntryImpl<V>(k,v,dropMe);
    }

    @Override
    public HybridCacheEntry<V> compute(final K key) {
        while (true) {
            LRUHybridCache<K,V>.OriginThreadAwareFuture f = cache.get(key);

            if (f == null) {
                LRUHybridCache<K,V>.OriginThreadAwareFuture ft =
                        new LRUHybridCache.OriginThreadAwareFuture(this, key);

                synchronized (prunningLock) {
                    if (cache.size() + 1 > maxCacheSize) {
                        removeLRUItem();
                    }
                    f = cache.putIfAbsent(key, ft);
                }
                if (f == null) {
                    f = ft;
                    ft.run();
                }
            } else {
                long tid = f.threadId;
                
                if ((tid != -1) && (Thread.currentThread().getId() == f.threadId)) {
                    cycleHandler.handleCycle(key);
                }
                f.lastHit = System.nanoTime();
            }
            try {
                final HybridCacheEntry result = f.get();
                if (result.dropMe()) {
                    cache.remove(key);
                }
                return result;
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                cache.remove(key);  // otherwise the exception would be remembered
                if (ex.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)ex.getCause();
                } else {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * Empty the cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Returns true if the key has already been cached.
     *
     * @param key
     * @return true if given key is present in the cache.
     */
    public boolean containsKey(final K key) {
        return cache.containsKey(key);
    }

    /**
     * Remove given item from the cache.
     *
     * @param key item key.
     */
    public void remove(final K key) {
        cache.remove(key);
    }

    /**
     * Remove least recently used item form the cache.
     * No checks are done here. The method just tries to remove the least recently used
     * cache item. An exception will be thrown if the cache is empty.
     */
    private void removeLRUItem() {
        final Collection<LRUHybridCache<K,V>.OriginThreadAwareFuture> values = cache.values();
        cache.remove((K)Collections.min(values, COMPARATOR).key);
    }

    private static final Comparator<LRUHybridCache.OriginThreadAwareFuture> COMPARATOR = new CacheEntryImplComparator();

    private static class CacheEntryImplComparator<K,V> implements Comparator<LRUHybridCache<K,V>.OriginThreadAwareFuture> {

        @Override
        public int compare(LRUHybridCache<K,V>.OriginThreadAwareFuture first, LRUHybridCache<K,V>.OriginThreadAwareFuture second) {
            final long diff = first.lastHit - second.lastHit;
            return diff > 0 ? 1 : diff == 0 ? 0 : -1;
        }
    }

    /**
     * This method will remove all cache entries for which this filter
     * matches
     *
     * @param filter Entries in the cache that match this filter will
     * be removed from the cache.  If filter is null nothing
     * will be removed from the cache
     */
    public void releaseMatching(CacheKeyFilter<K> filter) {
        if (filter == null) return;
        for (K key : cache.keySet()) {
            if (filter.matches(key)) {
                cache.remove(key);
            }
        }
    }
}
