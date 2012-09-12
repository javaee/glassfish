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

import java.util.HashMap;

import org.glassfish.hk2.utilities.cache.LRUCache;

/**
 * The implementation of the LRUCache
 * 
 * @author jwells
 *
 */
public class LRUCacheImpl<K,V> extends LRUCache<K, V> {
    private final int maxCacheSize;
    private final HashMap<K, CacheEntry<K, V>> entries =
            new HashMap<K, CacheEntry<K, V>>();
    
    private CacheEntry<K, V> first;
    private CacheEntry<K, V> last;
    
    public LRUCacheImpl(int maxCacheSize) {
        if (maxCacheSize <= 2) throw new IllegalArgumentException();
        
        this.maxCacheSize = maxCacheSize;
    }
    
    private void removeEntry(CacheEntry<K, V> removeMe) {
        CacheEntry<K, V> previous = removeMe.getPrevious();
        CacheEntry<K, V> next = removeMe.getNext();
        
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
    }
    
    private void addToFront(CacheEntry<K, V> addMe) {
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
        
        CacheEntry<K, V> entry = entries.get(key);
        if (entry == null) return null;
        
        removeEntry(entry);
        addToFront(entry);
        
        return entry.getValue();
    }

    @Override
    public synchronized void put(K key, V value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        
        CacheEntry<K, V> addMe = new CacheEntry<K, V>(value);
        addMe.setKey(key);   // For debugging
        
        entries.put(key, addMe);
        
        addToFront(addMe);
        if (entries.size() > maxCacheSize) {
            K removeMe = last.getKey();
            entries.remove(removeMe);
            
            removeEntry(last);
        }
    }

    @Override
    public synchronized void releaseCache() {
        entries.clear();
        
        first = null;
        last = null;
    }

    @Override
    public int getMaxCacheSize() {
        return maxCacheSize;
    }
    
    private static class CacheEntry<K, V> {
        private K key;
        private final V value;
        
        private CacheEntry<K, V> next;
        private CacheEntry<K, V> previous;
        
        private CacheEntry(V value) {
            this.value = value;
        }
        
        private V getValue() { return value; }
        
        private CacheEntry<K, V> getNext() { return next; }
        private CacheEntry<K, V> getPrevious() { return previous; }
        
        private void setNext(CacheEntry<K, V> next) {
            this.next = next;
        }
        
        private void setPrevious(CacheEntry<K, V> previous) {
            this.previous = previous;
        }
        
        private void setKey(K key) {
            this.key = key;
        }
        
        private K getKey() {
            return key;
        }
        
        public String toString() {
            return "CacheEntry(" + key + "=" + value + "," + System.identityHashCode(this) + ")";
        }
    }
    
    public String toString() {
        return "LRUCacheImpl(maxCacheSize=" + maxCacheSize + "," + System.identityHashCode(this) + ")";
    }

}
