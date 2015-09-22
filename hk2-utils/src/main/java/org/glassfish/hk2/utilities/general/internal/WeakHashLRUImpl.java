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
import java.util.WeakHashMap;

import org.glassfish.hk2.utilities.general.WeakHashLRU;

/**
 * An implementation of the WeakHashLRU as needed by the CAR algorithm
 * 
 * @author jwells
 *
 */
public class WeakHashLRUImpl<K> implements WeakHashLRU<K> {
    private final static Object VALUE = new Object();
    
    private final WeakHashMap<K, DoubleNode<K, Object>> byKey = new WeakHashMap<K, DoubleNode<K, Object>>();
    
    private final ReferenceQueue<? super K> myQueue = new ReferenceQueue<K>();
    
    private DoubleNode<K, Object> mru;
    private DoubleNode<K, Object> lru;
    
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
        clearStale();
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        
        DoubleNode<K, Object> existing = byKey.get(key);
        if (existing != null) {
            remove(existing);
        }
        
        DoubleNode<K, Object> added = addToHead(key);
        
        byKey.put(key, added);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#contains(java.lang.Object)
     */
    @Override
    public synchronized boolean contains(K key) {
        clearStale();
        
        return byKey.containsKey(key);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#remove(java.lang.Object)
     */
    @Override
    public synchronized boolean remove(K key) {
        clearStale();
        return removeNoClear(key);
    }
    
    public boolean removeNoClear(K key) {
        if (key == null) return false;
        
        DoubleNode<K, Object> removeMe = byKey.remove(key);
        if (removeMe == null) return false;
        
        remove(removeMe);
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#size()
     */
    @Override
    public synchronized int size() {
        clearStale();
        
        return byKey.size();
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
     * @see org.glassfish.hk2.utilities.general.WeakHashLRU#clearStaleReferences()
     */
    @Override
    public synchronized void clearStaleReferences() {
        clearStale();
    }
    
    private void clearStale() {
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

}
