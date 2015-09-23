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
    
    private final WeakHashClock<K,V> t1 = GeneralUtilities.getWeakHashClock();
    private final WeakHashClock<K,V> t2 = GeneralUtilities.getWeakHashClock();
    private final WeakHashLRU<K> b1 = GeneralUtilities.getWeakHashLRU();
    private final WeakHashLRU<K> b2 = GeneralUtilities.getWeakHashLRU();
    
    public WeakCARCacheImpl(Computable<K,V> computable, int maxSize) {
        this.computable = computable;
        this.maxSize = maxSize;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.utilities.cache.WeakCARCache#compute(java.lang.Object)
     */
    @Override
    public V compute(K key) {
        throw new AssertionError("not yet implemented");
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
        throw new AssertionError("not implemented yet");
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

}
