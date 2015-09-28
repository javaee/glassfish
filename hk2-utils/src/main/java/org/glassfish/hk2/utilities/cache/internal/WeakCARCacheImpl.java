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
package org.glassfish.hk2.utilities.cache.internal;

import java.util.Map;

import org.glassfish.hk2.utilities.cache.Computable;
import org.glassfish.hk2.utilities.cache.WeakCARCache;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.general.WeakHashClock;
import org.glassfish.hk2.utilities.general.WeakHashLRU;

/**
 * @author jwells
 *
 */
public class WeakCARCacheImpl<K,V> implements WeakCARCache<K, V> {
    private final Computable<K,V> computable;
    private final int maxSize;  // TODO, make this dynamic
    
    private final WeakHashClock<K,CarValue<V>> t1;
    private final WeakHashClock<K,CarValue<V>> t2;
    private final WeakHashLRU<K> b1;
    private final WeakHashLRU<K> b2;
    
    // The target size of t1, adaptive
    private int p = 0;
    
    public WeakCARCacheImpl(Computable<K,V> computable, int maxSize, boolean isWeak) {
        this.computable = computable;
        this.maxSize = maxSize;
        
        t1 = GeneralUtilities.getWeakHashClock(isWeak);
        t2 = GeneralUtilities.getWeakHashClock(isWeak);
        b1 = GeneralUtilities.getWeakHashLRU(isWeak);
        b2 = GeneralUtilities.getWeakHashLRU(isWeak);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#compute(java.lang.Object)
     */
    @Override
    public V compute(K key) {
        CarValue<V> cValue = t1.get(key);
        if (cValue != null) {
            // So fast
            cValue.referenceBit = true;
            return cValue.value;
        }
        
        cValue = t2.get(key);
        if (cValue != null) {
            // So fast
            cValue.referenceBit = true;
            return cValue.value;
        }
        
        // Cache Miss.  First, get the value.  Any failures
        // will bubble up prior to us messing with any data structures
        V value = computable.compute(key);
        
        synchronized (this) {
            cValue = t1.get(key);
            if (cValue != null) {
                // So fast
                cValue.referenceBit = true;
                return cValue.value;
            }
            
            cValue = t2.get(key);
            if (cValue != null) {
                // So fast
                cValue.referenceBit = true;
                return cValue.value;
            }
        
            int cacheSize = getValueSize();
            if (cacheSize >= maxSize) {
                replace();
            
                boolean inB1 = b1.contains(key);
                boolean inB2 = b2.contains(key);
                if (!inB1 && !inB2) {
                    if ((t1.size() + b1.size()) >= maxSize) {
                        b1.remove();
                    }
                    else if ((t1.size() + t2.size() + b1.size() + b2.size()) >= (2 * maxSize)) {
                        b2.remove();
                    }
                }
            }
        
            boolean inB1 = b1.contains(key);
            boolean inB2 = b2.contains(key);
        
            if (!inB1 && !inB2) {
                t1.put(key, new CarValue<V>(value));
            }
            else if (inB1) {
                int b1size = b1.size();
                if (b1size == 0) b1size = 1;  // Can happen in a weak situation, we fake the one
            
                int b2size = b2.size();
            
                int ratio = b2size / b1size;  // integer division
                if (ratio <= 0) ratio = 1;
            
                p = p + ratio;
                if (p > maxSize) p = maxSize;
            
                b1.remove(key);
                t2.put(key, new CarValue<V>(value));
            }
            else {
                // Must be in B2
                int b2size = b2.size();
                if (b2size == 0) b2size = 1;  // Can happen in a weak situation, we fake the one
            
                int b1size = b1.size();
            
                int ratio = b1size / b2size;
                if (ratio <= 0) ratio = 1;
            
                p = p - ratio;
                if (p < 0) p = 0;
            
                b2.remove(key);
                t2.put(key, new CarValue<V>(value));
            }
        }
        
        return value;
    }
    
    private void replace() {
        boolean found = false;
        while (!found) {
            int trySize = p;
            if (trySize < 1) trySize = 1;
            
            if (t1.size() >= trySize) {
                Map.Entry<K, CarValue<V>> entry = t1.next();
                
                if (entry.getValue().referenceBit == false) {
                    found = true;
                    
                    t1.remove(entry.getKey());
                    b1.add(entry.getKey());
                }
                else {
                    CarValue<V> entryValue = entry.getValue();
                    entryValue.referenceBit = false;
                    
                    t1.remove(entry.getKey());
                    t2.put(entry.getKey(), entryValue);
                }
            }
            else {
                Map.Entry<K, CarValue<V>> entry = t2.next();
                
                if (entry.getValue().referenceBit == false) {
                    found = true;
                    
                    t2.remove(entry.getKey());
                    b2.add(entry.getKey());
                }
                else {
                    CarValue<V> entryValue = entry.getValue();
                    entryValue.referenceBit = false;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getKeySize()
     */
    @Override
    public synchronized int getKeySize() {
        return t1.size() + t2.size() + b1.size() + b2.size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getValueSize()
     */
    @Override
    public synchronized int getValueSize() {
        return t1.size() + t2.size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#clear()
     */
    @Override
    public void clear() {
        throw new AssertionError("not yet implemented");
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#setMaxSize(int)
     */
    @Override
    public int setMaxSize(int newMax) {
        throw new AssertionError("not yet implemented");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getMaxSize()
     */
    @Override
    public int getMaxSize() {
        return maxSize;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getComputable()
     */
    @Override
    public Computable<K, V> getComputable() {
        return computable;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#remove(java.lang.Object)
     */
    @Override
    public boolean remove(K key) {
        if (t1.remove(key) == null) {
            if (t2.remove(key) == null) {
                if (!b1.remove(key)) {
                    return b2.remove(key);
                }
                
                return true;
            }
            
            return true;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#clearStaleReferences()
     */
    @Override
    public synchronized void clearStaleReferences() {
        t1.clearStaleReferences();
        t2.clearStaleReferences();
        b1.clearStaleReferences();
        b2.clearStaleReferences();
    }
    
    private static class CarValue<V> {
        private final V value;
        private volatile boolean referenceBit = false;
        
        private CarValue(V value) {
            this.value = value;
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getT1Size()
     */
    @Override
    public int getT1Size() {
        return t1.size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getT2Size()
     */
    @Override
    public int getT2Size() {
        return t2.size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getB1Size()
     */
    @Override
    public int getB1Size() {
        return b1.size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getB2Size()
     */
    @Override
    public int getB2Size() {
        return b2.size();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#getP()
     */
    @Override
    public int getP() {
        return p;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#dumpAllLists()
     */
    @Override
    public String dumpAllLists() {
        StringBuffer sb = new StringBuffer("p=" + p + "\nT1: " + t1.toString() + "\n");
        sb.append("T2: " + t2.toString() + "\n");
        sb.append("B1: " + b1.toString() + "\n");
        sb.append("B2: " + b2.toString() + "\n");
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "WeakCARCacheImpl(t1size=" + t1.size() + ",t2Size=" + t2.size() +
                ",b1Size=" + b1.size() + ",b2Size=" + b2.size() + ",p=" + p + "," +
                System.identityHashCode(this) + ")";
    }

}
