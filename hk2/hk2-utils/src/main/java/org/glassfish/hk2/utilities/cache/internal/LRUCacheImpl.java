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
package org.glassfish.hk2.utilities.cache.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

import org.glassfish.hk2.utilities.cache.CacheEntry;
import org.glassfish.hk2.utilities.cache.LRUCache;
import org.glassfish.hk2.utilities.reflection.Logger;

/**
 * The implementation of the LRUCache
 *
 * @author jwells
 *
 */
public class LRUCacheImpl<K,V> extends LRUCache<K, V> {
    private final static String CACHING_PROPERTY = "org.jvnet.hk2.properties.caching";
    private final static String REPORT_INTERVAL_PROPERTY = "org.jvnet.hk2.properties.caching.reportInterval";
    private final static boolean CACHING;
    static {
        CACHING = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return Boolean.parseBoolean(System.getProperty(CACHING_PROPERTY, "true"));
            }

        });

        if (!CACHING) {
            Logger.getLogger().debug("HK2 Caching has been disabled");
        }
    }

    private final int maxCacheSize;
    private final HashMap<K, CacheEntryImpl> entries =
            new HashMap<K, CacheEntryImpl>();

    private CacheEntryImpl first;
    private CacheEntryImpl last;

    private final long cacheHitRateReportingInterval;
    private long hits;
    private long queries;

    private LRUCacheImpl(int maxCacheSize, long userInterval) {
        if (maxCacheSize <= 2) throw new IllegalArgumentException();

        this.maxCacheSize = maxCacheSize;

        cacheHitRateReportingInterval = (userInterval < 0) ? 0 : userInterval;
    }

    public LRUCacheImpl(int maxCacheSize) {
        this(maxCacheSize, AccessController.doPrivileged(new PrivilegedAction<Long>() {

            @Override
            public Long run() {
                return Long.parseLong(System.getProperty(REPORT_INTERVAL_PROPERTY, "0"));
            }

        }));
    }

    private synchronized void removeEntryFromRemove(CacheEntryImpl removeMe) {
        if (removeEntry(removeMe, true)) {
            entries.remove(removeMe.getKey());
        }
    }

    /**
     * Needs to be synchronized because it could be entered from
     * {@link CacheEntry#removeFromCache()}
     *
     * @param removeMe
     */
    private synchronized boolean removeEntry(CacheEntryImpl removeMe, boolean trulyRemove) {
        if (removeMe.isRemoved()) return false;
        if (trulyRemove) {
            removeMe.remove();
        }

        CacheEntryImpl previous = removeMe.getPrevious();
        CacheEntryImpl next = removeMe.getNext();

        if (previous == null) {
            // I am first
            first = next;  // even if next is null!
        }
        else {
            previous.setNext(next);
        }

        if (next == null) {
            // I am last
            last = previous;  // even if previous is null!
        }
        else {
            next.setPrevious(previous);
        }

        removeMe.setNext(null);
        removeMe.setPrevious(null);

        return true;
    }

    private void addToFront(CacheEntryImpl addMe) {
        addMe.setNext(first);

        if (first != null) {
            first.setPrevious(addMe);
        }

        first = addMe;

        if (last == null) {
            last = addMe;
        }
    }

    @Override
    public synchronized V get(K key) {
        if (key == null) throw new IllegalArgumentException();
        if ((cacheHitRateReportingInterval > 0L) && (queries > 0L) &&
                (queries % cacheHitRateReportingInterval) == 0) {
            double rate = (((double) hits) / ((double) queries));

            Logger.getLogger().debug("LRUCacheHitRate is Rate=" + rate + " hits=" + hits + " queries=" + queries + " entries=" +
                entries.size());
        }

        queries++;
        CacheEntryImpl entry = entries.get(key);
        if (entry == null) return null;
        hits++;

        removeEntry(entry, false);
        addToFront(entry);

        return entry.getValue();
    }


    @Override
    public synchronized CacheEntry put(K key, V value) {
        if (key == null || value == null) throw new IllegalArgumentException();

        if (!CACHING) return new CacheEntry() {
            public void removeFromCache() {
            }

        };

        CacheEntryImpl addMe = new CacheEntryImpl(value);
        addMe.setKey(key);   // For debugging

        CacheEntryImpl cei = entries.put(key, addMe);
        if (cei != null) {
            removeEntry(cei, true);
        }

        addToFront(addMe);
        if (entries.size() > maxCacheSize) {
            K removeMe = last.getKey();
            entries.remove(removeMe);

            removeEntry(last, true);
        }

        return addMe;
    }

    @Override
    public synchronized void releaseCache() {
        entries.clear();

        first = null;
        last = null;

        hits = 0L;
        queries = 0L;
    }

    @Override
    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    private class CacheEntryImpl implements CacheEntry {
        private K key;
        private final V value;
        private boolean removed = false;

        private CacheEntryImpl next;
        private CacheEntryImpl previous;

        private CacheEntryImpl(V value) {
            this.value = value;
        }

        private V getValue() { return value; }

        private boolean isRemoved() { return removed; }

        private void remove() { removed = true; }

        private CacheEntryImpl getNext() { return next; }
        private CacheEntryImpl getPrevious() { return previous; }

        private void setNext(CacheEntryImpl next) {
            this.next = next;
        }

        private void setPrevious(CacheEntryImpl previous) {
            this.previous = previous;
        }

        private void setKey(K key) {
            this.key = key;
        }

        private K getKey() {
            return key;
        }

        @Override
        public void removeFromCache() {
            removeEntryFromRemove(this);
        }

        public String toString() {
            return "CacheEntry(" + key + "=" + value + "," + removed + "," + System.identityHashCode(this) + ")";
        }
    }

    public String toString() {
        return "LRUCacheImpl(maxCacheSize=" + maxCacheSize + "," + System.identityHashCode(this) + ")";
    }

}
