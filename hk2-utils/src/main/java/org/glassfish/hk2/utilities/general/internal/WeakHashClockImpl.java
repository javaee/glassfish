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
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.glassfish.hk2.utilities.general.WeakHashClock;

/**
 * Implementation of WeakHashClock as needed by the CAR algorithm
 * 
 * @author jwells
 *
 */
public class WeakHashClockImpl<K,V> implements WeakHashClock<K,V> {
    private final WeakHashMap<K, DoubleNode<K, V>> byKey = new WeakHashMap<K, DoubleNode<K, V>>();
    
    private final ReferenceQueue<? super K> myQueue = new ReferenceQueue<K>();
    
    private DoubleNode<K, V> head;
    private DoubleNode<K, V> tail;
    private DoubleNode<K, V> dot;
    
    private DoubleNode<K,V> addBeforeDot(K key, V value) {
        DoubleNode<K, V> toAdd = new DoubleNode<K,V>(key,value, myQueue);
        
        if (dot == null) {
            // List is empty
            head = toAdd;
            tail = toAdd;
            dot = toAdd;
            return toAdd;
        }
        
        if (dot.previous == null) {
            // Dot is currently at the head
            dot.previous = toAdd;
            
            toAdd.next = dot;
            head = toAdd;
            return toAdd;
        }
        
        // Otherwise just add it.  Note it will NEVER be added as
        // the tail because it is always being added before something
        toAdd.next = dot;
        toAdd.previous = dot.previous;
        
        dot.previous.next = toAdd;
        dot.previous = toAdd;
        
        return toAdd;
    }
    
    private void removeFromDLL(DoubleNode<K,V> removeMe) {
        if (removeMe.previous != null) {
            removeMe.previous.next = removeMe.next;
        }
        if (removeMe.next != null) {
            removeMe.next.previous = removeMe.previous;
        }
        
        if (removeMe == head) {
            head = removeMe.next;
        }
        if (removeMe == tail) {
            tail = removeMe.previous;
        }
        
        if (removeMe == dot) {
            dot = removeMe.next;
        }
        
        removeMe.next = null;
        removeMe.previous = null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashClock#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public synchronized void put(final K key, final V value) {
        removeStale();
        
        if (key == null || value == null) throw new IllegalArgumentException("key " + key + " or value " + value + " is null");
        
        DoubleNode<K,V> addMe = addBeforeDot(key, value);
        
        byKey.put(key, addMe);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashClock#get(java.lang.Object)
     */
    @Override
    public synchronized V get(K key) {
        removeStale();
        
        if (key == null) return null;
        
        DoubleNode<K,V> node = byKey.get(key);
        if (node == null) return null;
        
        return node.value;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashClock#remove(java.lang.Object)
     */
    @Override
    public synchronized V remove(K key) {
        removeStale();
        
        if (key == null) return null;
        
        DoubleNode<K,V> node = byKey.remove(key);
        if (node == null) return null;
        
        removeFromDLL(node);
        
        return node.value;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashClock#size()
     */
    @Override
    public synchronized int size() {
        removeStale();
        
        return byKey.size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashClock#next()
     */
    @Override
    public synchronized Entry<K, V> next() {
        DoubleNode<K,V> returnSource = dot;
        if (dot == null) return null;
        
        dot = returnSource.next;
        if (dot == null) dot = head;
        
        final K key = returnSource.weakKey.get();
        if (key == null) {
            // it went from underneath us
            removeStale();
            
            return next();
        }
        final V value = returnSource.value;
        
        return new Map.Entry<K,V>() {

            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V value) {
                throw new AssertionError("not implemented");
            }
            
        };
        
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.general.WeakHashClock#clearStaleReferences()
     */
    @Override
    public synchronized void clearStaleReferences() {
        removeStale();
    }
    
    private void removeStale() {
        boolean goOn = false;
        while (myQueue.poll() != null) {
            goOn = true;
        }
        
        if (!goOn) return;
        
        DoubleNode<K,V> current = head;
        while (current != null) {
            DoubleNode<K,V> next = current.next;
            
            if (current.weakKey.get() == null) {
                removeFromDLL(current);
            }
            
            current = next;
        }
    }
    
    private final static class DoubleNode<K, V> {
        private final WeakReference<K> weakKey;
        private final V value;
        private DoubleNode<K, V> previous;
        private DoubleNode<K, V> next;
        
        private DoubleNode(K key, V value, ReferenceQueue<? super K> queue) {
            weakKey = new WeakReference<K>(key, queue);
            this.value = value;
        }
    }

    
}
