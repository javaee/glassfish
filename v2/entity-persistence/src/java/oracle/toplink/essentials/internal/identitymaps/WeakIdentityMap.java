/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package oracle.toplink.essentials.internal.identitymaps;

import java.util.*;

/**
 * <p><b>Purpose</b>: A WeakIdentityMap holds all objects referenced by the application only.
 * The weak identity map is similar to the full identity map except for the fact that it allows
 * full garbage collection.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Guarantees identity
 * <li> Allows garbage collection
 * </ul>
 *    @since TOPLink/Java 1.0
 */
public class WeakIdentityMap extends FullIdentityMap {

    /** Keep track of a counter to amortize cleanup of dead cache keys */
    protected int cleanupCount;

    /** PERF: Keep track of a cleanup size to avoid cleanup bottleneck for large caches. */
    protected int cleanupSize;

    public WeakIdentityMap(int size) {
        super(size);
        this.cleanupCount = 0;
        this.cleanupSize = size;
    }

    /**
     * Search for any cache keys that have been garbage collected and remove them.
     * This must be done because allthough the objects held by the cache keys will garbage collect,
     * the keys themselves will not and must be cleaned up.  This is a linear opperation so
     * is amortized through the cleanupCount to occur only once per cycle avergaing to make
     * the total time still constant.
     */
    protected void cleanupDeadCacheKeys() {
        for (Enumeration keysEnum = getCacheKeys().elements(); keysEnum.hasMoreElements();) {
            CacheKey key = (CacheKey)keysEnum.nextElement();
            if (key.getObject() == null) {
                // Check lock first.
                //Change for CR 2317
                if (key.acquireNoWait()) {
                    try {
                        if (key.getObject() == null) {
                            getCacheKeys().remove(key);
                        }
                    } finally {
                        key.release();
                    }
                }

                //change complete CR 2317  
            }
        }
    }

    public CacheKey createCacheKey(Vector primaryKey, Object object, Object writeLockValue, long readTime) {
        return new WeakCacheKey(primaryKey, object, writeLockValue, readTime);
    }

    /**
     * Used to amortized the cleanup of dead cache keys.
     */
    protected int getCleanupCount() {
        return cleanupCount;
    }

    protected void setCleanupCount(int cleanupCount) {
        this.cleanupCount = cleanupCount;
    }

    /**
     * Used to amortized the cleanup of dead cache keys.
     */
    protected int getCleanupSize() {
        return cleanupSize;
    }

    protected void setCleanupSize(int cleanupSize) {
        this.cleanupSize = cleanupSize;
    }

    /**
     * Store the object in the cache with the cache key.
     */
    protected void put(CacheKey cacheKey) {
        //CR3712  Add the method back.
        synchronized (this) {
            if (getCleanupCount() > getCleanupSize()) {
                cleanupDeadCacheKeys();
                setCleanupCount(0);
                // PERF: Avoid cleanup bottleneck for large cache sizes, increase next cleanup.
                if (getSize() > getCleanupSize()) {
                    setCleanupSize(getSize());
                }
            }
            setCleanupCount(getCleanupCount() + 1);
        }
        super.put(cacheKey);
    }
}
