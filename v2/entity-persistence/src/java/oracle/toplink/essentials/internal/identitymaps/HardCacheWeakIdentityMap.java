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
import oracle.toplink.essentials.internal.helper.linkedlist.*;

/**
 * <p><b>Purpose</b>: A weak cache is identical to the weak identity map, however the weak
 * can be a performance problem for some types of apps because it can cause too much garbage collection
 * of objects read causing them to be re-read and re-built (this defeats the purpose of the cache).
 * The weak cache solves this through also holding a fixed number of objects is memory to improve caching.
 * This makes used of an exposed node linked list to maintain the objects by storing the link nodes in the cache key.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Guarantees identity
 * <li> Allows garbage collection
 * <li> Increases performance through maintaining a fixed size cache of MRU objects when memory is available
 * <li> The default size of the reference cache is half the max size
 * </ul>
 * @since TOPLink/Java 1.2
 */
public class HardCacheWeakIdentityMap extends WeakIdentityMap {
    protected ExposedNodeLinkedList referenceCache;

    public HardCacheWeakIdentityMap(int size) {
        super(size);
        this.referenceCache = new ExposedNodeLinkedList();
    }

    /**
     * Use a ReferenceCacheKey that also stores the linked list node to manage
     * the LRU sub-cache of references.
     */
    public CacheKey createCacheKey(Vector primaryKey, Object object, Object writeLockValue, long readTime) {
        return new ReferenceCacheKey(primaryKey, object, writeLockValue, readTime);
    }

    /**
     * Return the linked reference cache.
     */
    public ExposedNodeLinkedList getReferenceCache() {
        return referenceCache;
    }

    /**
     * Creates a Soft reference if Required
     * @param object is the domain object to cache.
     */
    public Object buildReference(Object object) {
        return object;
    }

    /**
     * Checks if the object is null, or reference's object is null.
     * @param the object for hard or the reference for soft.
     */
    public boolean hasReference(Object reference) {
        return reference != null;
    }

    /**
     * Store the object in the cache with the cache key.
     * Also store the linked list node in the cache key.
     */
    protected void put(CacheKey cacheKey) {
        ReferenceCacheKey referenceCacheKey = (ReferenceCacheKey)cacheKey;
        LinkedNode node = getReferenceCache().addFirst(buildReference(referenceCacheKey.getObject()));
        referenceCacheKey.setReferenceCacheNode(node);
        super.put(cacheKey);
    }

    /**
     * Remove the cache key from the map and the sub-cache list.
     */
    public Object remove(CacheKey cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        ReferenceCacheKey referenceCacheKey = (ReferenceCacheKey)cacheKey;
        synchronized (this){
            getReferenceCache().remove(referenceCacheKey.getReferenceCacheNode());
        }
        return super.remove(cacheKey);
    }

    /**
     * This method will be used to update the max cache size.
     */
    public synchronized void updateMaxSize(int maxSize) {
        setMaxSize(maxSize);
        // Remove the LRU items if max size exceeded.
        while (getReferenceCache().size() > getMaxSize()) {
            getReferenceCache().removeLast();
        }
    }

    /**
     * Inner class to define the specialized weak cache key.
     * Keeps track of the linked list node to allow quick repositioning.
     */
    public class ReferenceCacheKey extends WeakCacheKey {
        protected LinkedNode referenceNode;

        public ReferenceCacheKey(Vector primaryKey, Object object, Object writeLockValue, long readTime) {
            super(primaryKey, object, writeLockValue, readTime);
        }

        public LinkedNode getReferenceCacheNode() {
            return referenceNode;
        }

        public void setReferenceCacheNode(LinkedNode referenceNode) {
            this.referenceNode = referenceNode;
        }

        public ExposedNodeLinkedList getReferenceCache() {
            return referenceCache;
        }

        /**
         * Notifies that cache key that it has been accessed.
         * Allows the LRU sub-cache to be maintained,
         * the cache node must be moved to the front of the list.
         */
        public void updateAccess() {
            // CR#3573797 must be synchronized on the map, not the cache key.
            synchronized (HardCacheWeakIdentityMap.this) {
                // Check if the node's contents is null (was removed),
                // also the object is null on initial put of acquired cache key,
                // or ref value may have garbage collected so reset it.
                if (!hasReference(getReferenceCacheNode().getContents())) {
                    getReferenceCacheNode().setContents(buildReference(getObject()));
                }

                // This is a fast constant time operations because of the linked list usage.
                getReferenceCache().moveFirst(getReferenceCacheNode());
                // Remove the old LRU items if max size exceeded (if was removed).
                while (getReferenceCache().size() > getMaxSize()) {
                    getReferenceCache().removeLast();
                }
            }
        }
    }
}
