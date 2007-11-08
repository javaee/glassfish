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
 * <p><b>Purpose</b>: A FullIdentityMap holds all objects stored within it for the life of the application
 * <p><b>Responsibilities</b>:<ul>
 *    <li> Guarantees identity
 * <li> Holds all cached objects indefinetly.
 * </ul>
 * @since TOPLink/Java 1.0
 */
public class FullIdentityMap extends IdentityMap {

    /** Hashtable of CacheKeys stored using their key */
    protected Hashtable cacheKeys;

    public FullIdentityMap(int size) {
        super(size);
        cacheKeys = new Hashtable(size);
    }

    /**
     * INTERNAL:
     * Clones itself.
     */
    public Object clone() {
        FullIdentityMap clone = (FullIdentityMap)super.clone();
        clone.setCacheKeys(new Hashtable(getCacheKeys().size()));

        for (Enumeration cacheKeysEnum = getCacheKeys().elements();
                 cacheKeysEnum.hasMoreElements();) {
            CacheKey key = (CacheKey)((CacheKey)cacheKeysEnum.nextElement()).clone();
            clone.getCacheKeys().put(key, key);
        }

        return clone;
    }

    /**
     * INTERNAL:
     * Used to print all the Locks in every identity map in this session.
     * The output of this method will go to log passed in as a parameter.
     */
    public void collectLocks(HashMap threadList) {
        Iterator cacheKeyIterator = this.cacheKeys.values().iterator();
        while (cacheKeyIterator.hasNext()) {
            CacheKey cacheKey = (CacheKey)cacheKeyIterator.next();
            if (cacheKey.isAcquired()) {
                Thread activeThread = cacheKey.getMutex().getActiveThread();
                Set set = (Set)threadList.get(activeThread);
                if (set == null) {
                    set = new HashSet();
                    threadList.put(activeThread, set);
                }
                set.add(cacheKey);
            }
        }
    }

    /**
     * Allow for the cache to be iterated on.
     */
    public Enumeration elements() {
        return new IdentityMapEnumeration(this);
    }

    /**
     *    Return the object indexed in the recevier at the cache key.
     *    If now object for the key exists, return null.
     *    @return a CacheKey for the primary key or null
     */
    protected synchronized CacheKey getCacheKey(CacheKey searchKey) {
        return (CacheKey)getCacheKeys().get(searchKey);
    }

    public Hashtable getCacheKeys() {
        return cacheKeys;
    }

    /**
     * Return the number of objects in the IdentityMap.
     */
    public int getSize() {
        return cacheKeys.size();
    }

    /**
     * Return the number of actual objects of type myClass in the IdentityMap.
     * Recurse = true will include subclasses of myClass in the count.
       */
    public int getSize(Class myClass, boolean recurse) {
        int i = 0;
        Enumeration keys = getCacheKeys().keys();

        while (keys.hasMoreElements()) {
            CacheKey key = (CacheKey)keys.nextElement();
            Object obj = key.getObject();

            if (obj != null) {
                if (recurse && myClass.isInstance(obj)) {
                    i++;
                } else if (obj.getClass().equals(myClass)) {
                    i++;
                }
            }
        }

        return i;
    }

    /**
     * Allow for the cache keys to be iterated on.
     */
    public Enumeration keys() {
        return new IdentityMapKeyEnumeration(this);
    }

    /**
     * Store the object in the cache at its primary key.
     * @param primaryKey is the primary key for the object.
     * @param object is the domain object to cache.
     * @param writeLockValue is the current write lock value of object, if null the version is ignored.
     */
    public CacheKey put(Vector primaryKey, Object object, Object writeLockValue, long readTime) {
        CacheKey cacheKey = getCacheKey(primaryKey);

        // Find the cache key in the hashtable, reset it.
        if (cacheKey != null) {
            // The cache key has to be locked during the re-setting, keep other threads from accessing the object.
            resetCacheKey(cacheKey, object, writeLockValue);

            // Still must put the object to ensure the LRU caching.
            put(cacheKey);
        } else {
            // Cache key not found, create a new one and put it into the hashtable.
            cacheKey = createCacheKey(primaryKey, object, writeLockValue, readTime);

            put(cacheKey);
        }

        return cacheKey;
    }

    /**
     * Store the object in the cache with the cache key.
     */
    protected void put(CacheKey cacheKey) {
        //synchronized because subclasses may not sync around call
        synchronized(this){
            getCacheKeys().put(cacheKey, cacheKey);
        }
        cacheKey.setOwningMap(this);
    }

    /**
     * Removes the CacheKey from the Hashtable.
     * @return The object held within the CacheKey or null if no object cached for given primaryKey
     */
    public Object remove(CacheKey cacheKey) {
        if (cacheKey != null) {
            //Cache key needs to be locked when removing from the Hashtable
            cacheKey.acquire();

            //remove opration from the hashtable has to be synchronized
            synchronized (this) {
                getCacheKeys().remove(cacheKey);
            }

            //Cache key needs to be released after removing from the Hashtable
            cacheKey.release();
        } else {
            return null;
        }

        return cacheKey.getObject();
    }

    public void resetCacheKey(CacheKey key, Object object, Object writeLockValue) {
        resetCacheKey(key, object, writeLockValue, 0);
    }

    public void resetCacheKey(CacheKey key, Object object, Object writeLockValue, long readTime) {
        key.acquire();
        key.setObject(object);
        key.setWriteLockValue(writeLockValue);
        key.setReadTime(readTime);
        key.release();
    }

    protected void setCacheKeys(Hashtable cacheKeys) {
        this.cacheKeys = cacheKeys;
    }
}
