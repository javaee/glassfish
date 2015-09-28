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
package org.glassfish.hk2.utilities.general.internal;

import java.lang.ref.ReferenceQueue;
import java.util.LinkedList;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.hk2.utilities.cache.CacheKeyFilter;
import org.glassfish.hk2.utilities.general.WeakHashLRU;

/**
 * An implementation of the WeakHashLRU as needed by the CAR algorithm
 * 
 * @author jwells
 *
 */
public class WeakHashLRUImpl<K> implements WeakHashLRU<K> {
    private final static Object VALUE = new Object();
    
    private final boolean isWeak;
    private final WeakHashMap<K, DoubleNode<K, Object>> byKey;
    private final ConcurrentHashMap<K, DoubleNode<K, Object>> byKeyNotWeak;
    
    private final ReferenceQueue<? super K> myQueue = new ReferenceQueue<K>();
    
    private DoubleNode<K, Object> mru;
    private DoubleNode<K, Object> lru;
    
    public WeakHashLRUImpl(boolean isWeak) {
        this.isWeak = isWeak;
        if (isWeak) {
            byKey = new WeakHashMap<K, DoubleNode<K, Object>>();
            byKeyNotWeak = null;
        }
        else {
            byKey = null;
            byKeyNotWeak = new ConcurrentHashMap<K, DoubleNode<K, Object>>();
        }
    }
    
    private DoubleNode<K,Object> addToHead(K key) {
        DoubleNode<K, Object> added = new DoubleNode<K,Object>(key, VALUE, myQueue);
        
        if (mru == null) {
            mru = added;
            lru = added;
            return added;
        }
        
        added.setNext(mru);
        
        mru.setPrevious(added);
        mru = added;
        
        return added;
    }
    
    private K remove(DoubleNode<K, Object> removeMe) {
        K retVal = removeMe.getWeakKey().get();
        
        if (removeMe.getNext() != null) {
            removeMe.getNext().setPrevious(removeMe.getPrevious());
        }
        if (removeMe.getPrevious() != null) {
            removeMe.getPrevious().setNext(removeMe.getNext());
        }
        
        if (removeMe == mru) {
            mru = removeMe.getNext();
        }
        if (removeMe == lru) {
            lru = removeMe.getPrevious();
        }
        
        removeMe.setNext(null);
        removeMe.setPrevious(null);
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#add(java.lang.Object)
     */
    @Override
    public synchronized void add(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        
        DoubleNode<K, Object> existing;
        if (isWeak) {
            clearStale();
            
            existing = byKey.get(key);
        }
        else {
            existing = byKeyNotWeak.get(key);
        }
        
        if (existing != null) {
            remove(existing);
        }
        
        DoubleNode<K, Object> added = addToHead(key);
        
        if (isWeak) {
            byKey.put(key, added);
        }
        else {
            byKeyNotWeak.put(key, added);
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#contains(java.lang.Object)
     */
    @Override
    public boolean contains(K key) {
        if (isWeak) {
            synchronized (this) {
                clearStale();
        
                return byKey.containsKey(key);
            }
        }
        
        return byKeyNotWeak.containsKey(key);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#remove(java.lang.Object)
     */
    @Override
    public synchronized boolean remove(K key) {
        if (isWeak) {
            clearStale();
        }
        
        return removeNoClear(key);
    }
    
    private boolean removeNoClear(K key) {
        if (key == null) return false;
        
        DoubleNode<K, Object> removeMe;
        if (isWeak) {
            removeMe = byKey.remove(key);
        }
        else {
            removeMe = byKeyNotWeak.remove(key);
        }
        if (removeMe == null) return false;
        
        remove(removeMe);
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#size()
     */
    @Override
    public int size() {
        if (isWeak) {
            synchronized (this) {
                clearStale();
        
                return byKey.size();
            }
        }
        
        return byKeyNotWeak.size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#remove()
     */
    @Override
    public synchronized K remove() {
        try {
            if (lru == null) return null;
        
            DoubleNode<K, Object> current = lru;
            while (current != null) {
                DoubleNode<K, Object> previous = current.getPrevious();
            
                K retVal = current.getWeakKey().get();
            
                if (retVal != null) {
                    removeNoClear(retVal);
                
                    return retVal;
                }
                else {
                    remove(current);
                }
            
                current = previous;
            }
        
            return null;
        }
        finally {
            clearStale();
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#releaseMatching(org.glassfish.hk2.utilities.cache.CacheKeyFilter)
     */
    @Override
    public synchronized void releaseMatching(CacheKeyFilter<K> filter) {
        if (filter == null) return;
        if (isWeak) {
            clearStale();
        }
        
        LinkedList<K> removeMe = new LinkedList<K>();
        DoubleNode<K, Object> current = mru;
        while (current != null) {
            K key = current.getWeakKey().get();
            if (key != null && filter.matches(key)) {
                removeMe.add(key);
            }
            
            current = current.getNext();
        }
        
        for (K removeKey : removeMe) {
            removeNoClear(removeKey);
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#clear()
     */
    @Override
    public synchronized void clear() {
        if (isWeak) {
            clearStale();
            
            byKey.clear();
        }
        else {
            byKeyNotWeak.clear();
        }
        
        mru = null;
        lru = null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#clearStaleReferences()
     */
    @Override
    public synchronized void clearStaleReferences() {
        clearStale();
    }
    
    private void clearStale() {
        boolean goOn = false;
        while (myQueue.poll() != null) {
            goOn = true;
        }
        
        if (!goOn) return;
        
        DoubleNode<K, Object> current;
        
        current = mru;
        while (current != null) {
            DoubleNode<K, Object> next = current.getNext();
            
            if (current.getWeakKey().get() == null) {
                remove(current);
            }
            
            current = next;
        }
    }
    
    @Override
    public synchronized String toString() {
        StringBuffer sb = new StringBuffer("WeakHashLRUImpl({");
        
        boolean first = true;
        DoubleNode<K,Object> current = mru;
        while (current != null) {
            K key = current.getWeakKey().get();
            String keyString = (key == null) ? "null" : key.toString();
            
            if (first) {
                first = false;
                
                sb.append(keyString);
            }
            else {
                sb.append("," + keyString);
            }
            
            current = current.getNext();
        }
        
        sb.append("}," + System.identityHashCode(this) + ")");
              
        return sb.toString();
    }
}
