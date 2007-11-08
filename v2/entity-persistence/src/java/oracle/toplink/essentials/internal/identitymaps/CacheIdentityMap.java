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

import oracle.toplink.essentials.exceptions.*;

/**
 * <p><b>Purpose</b>: A fixed size LRU cache<p>
 *    Using a linked list as well as the hashtable from the superclass a LRU cache is maintained.
 * When a get is executed the LRU list is updated and when a new object is inserted the object
 *    at the start of the list is deleted (provided the maxSize has been reached).
 * <p><b>Responsibilities</b>:<ul>
  *    <li> Guarantees identity through primary key values
 * <li> Keeps the LRU linked list updated.
 * </ul>
 * @since TOPLink/Java 1.0
 */
public class CacheIdentityMap extends FullIdentityMap {

    /** Provide handles on the linked list */
    protected LinkedCacheKey first;

    /** Provide handles on the linked list */
    protected LinkedCacheKey last;

    /**
     * Initialize newly instantiated CacheIdentityMap.
     * @param size int The size of the Cache
     */
    public CacheIdentityMap(int size) {
        super(size);
        this.first = new LinkedCacheKey(new Vector(2), null, null, 0);
        this.last = new LinkedCacheKey(new Vector(2), null, null, 0);
        this.first.setNext(this.last);
        this.last.setPrevious(this.first);
    }

    public CacheKey createCacheKey(Vector primaryKey, Object object, Object writeLockValue, long readTime) {
        return new LinkedCacheKey(primaryKey, object, writeLockValue, readTime);
    }

    /**
     * Reduces the size of the receiver down to the maxSize removing objects from the
     * start of the linked list.
     */
    protected void ensureFixedSize() {
        // protect the case where someone attempts to break the cache by
        // setting max size to 0.
        synchronized(this.first) {
        while (getMaxSize() > 0 && getSize() > getMaxSize()) {
            remove(last.getPrevious());
            }
        }
    }

    /**
     *    Access the object within the table for the given primaryKey.
     *    Move the accessed key to the top of the order keys linked list to maintain LRU.
     *    @param aVector is the primary key for the object to search for.
     *    @return The LinkedCacheKey or null if none found for primaryKey
     */
    protected CacheKey getCacheKey(Vector primaryKeys) {
        LinkedCacheKey cacheKey = (LinkedCacheKey)super.getCacheKey(primaryKeys);

        if (cacheKey != null) {
            synchronized (this.first) {
                removeLink(cacheKey);
                insertLink(cacheKey);
            }
        }

        return cacheKey;
    }

    /**
     *    Insert a new element into the linked list of LinkedCacheKeys.
     *    New elements (Recently Used) are added at the end (last).
     *    @return The added LinkedCacheKey
     */
    protected LinkedCacheKey insertLink(LinkedCacheKey key) {
        if (key == null){
            return key;
        }
        // no sence on locking the entire cache, just lock on the list
        synchronized (this.first){
            this.first.getNext().setPrevious(key);
            key.setNext(this.first.getNext());
            key.setPrevious(this.first);
            this.first.setNext(key);
        }
        return key;
    }

    /**
     *  Store the object in the identity map with the linked cache key
     */
    protected void put(CacheKey cacheKey) {
        super.put(cacheKey);
        insertLink((LinkedCacheKey)cacheKey);
        ensureFixedSize();
    }

    /**
     * Remove the LinkedCacheKey from the cache as well as from the linked list.
     * @return The LinkedCacheKey to be removed
     */
    public Object remove(CacheKey key) {
        super.remove(key);
        // CR2408
        if (key == null) {
            Class cacheItemClass = null;

            // Get the class of the CacheKey which we could not remove
            // (if possible) for client debugging purposes.
            // We can't get the descriptor, because we don't know the session.
            if (!getCacheKeys().isEmpty()) {
                CacheKey aKey = (CacheKey)getCacheKeys().keys().nextElement();
                if ((aKey != null) && (aKey.getObject() != null)) {
                    cacheItemClass = aKey.getObject().getClass();
                }
            }
            throw ValidationException.nullCacheKeyFoundOnRemoval(this, cacheItemClass);
        }
        return removeLink((LinkedCacheKey)key).getObject();
    }

    /**
     * Remove the LinkedCacheKey from the linked list.
     * @return The removed LinkedCacheKey
     */
    protected LinkedCacheKey removeLink(LinkedCacheKey key) {
        if (key == null){
            return key;
        }
        synchronized (this.first) {
            if (key.getPrevious() == null || key.getNext() == null){
                //already removed by a competing thread, just return
                return key;
            }
            key.getPrevious().setNext(key.getNext());
            key.getNext().setPrevious(key.getPrevious());
            key.setNext(null);
            key.setPrevious(null);
        }
        return key;
    }

    /**
     * INTERNAL:
     *        This method will be used to update the max cache size, any objects exceeding the max cache size will
     * be remove from the cache. Please note that this does not remove the object from the identityMap, except in
     * the case of the CacheIdentityMap.
     */
    public synchronized void updateMaxSize(int maxSize) {
        setMaxSize(maxSize);
        ensureFixedSize();
    }
}
