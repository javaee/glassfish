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
package oracle.toplink.essentials.internal.sessions;

import java.util.*;
import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.helper.WriteLockManager;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;

/**
 * INTERNAL:
 * Internal subclass that provides access to identity maps through the session.
 * Implements the IdentityMapAccessor interface which provides all publicly available
 * identity map functionality to users.
 * This is the main class that should be used to access identity maps.  In general, any
 * function that accesses the identity map manager should go through this class
 * Any session specific functionality appears in subclasses
 */
public class IsolatedClientSessionIdentityMapAccessor extends oracle.toplink.essentials.internal.sessions.IdentityMapAccessor {

    /**
     * INTERNAL:
     * An IdentityMapAccessor sits between the session and the identityMapManager
     * It needs references in both directions
     */
    public IsolatedClientSessionIdentityMapAccessor(AbstractSession session, IdentityMapManager identityMapManager) {
        super(session, identityMapManager);
    }

    /**
     * INTERNAL:
     * Deferred lock the identity map for the object, this is used for avoiding deadlock
     * The return cacheKey should be used to release the deferred lock
     */
    public CacheKey acquireDeferredLock(Vector primaryKey, Class javaClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().acquireDeferredLock(primaryKey, javaClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().acquireDeferredLock(primaryKey, javaClass, descriptor);
        }
    }

    /**
     * INTERNAL:
     * Provides access for setting a concurrency lock on an object in the IdentityMap.
     * called with true from the merge process, if true then the refresh will not refresh the object
     *    @see IdentityMap#aquire
     */
    public CacheKey acquireLock(Vector primaryKey, Class domainClass, boolean forMerge, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().acquireLock(primaryKey, domainClass, forMerge, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().acquireLock(primaryKey, domainClass, forMerge, descriptor);
        }
    }

    /**
    * INTERNAL:
    * Provides access for setting a concurrency lock on an object in the IdentityMap.
    * called with true from the merge process, if true then the refresh will not refresh the object
    *    @see IdentityMap#aquire
     */
    public CacheKey acquireLockNoWait(Vector primaryKey, Class domainClass, boolean forMerge, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().acquireLockNoWait(primaryKey, domainClass, forMerge, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().acquireLockNoWait(primaryKey, domainClass, forMerge, descriptor);
        }
    }

    /**
     * INTERNAL:
     * Find the cachekey for the provided primary key and place a readlock on it.
     * This will allow multiple users to read the same object but prevent writes to
     * the object while the read lock is held.
     */
    public CacheKey acquireReadLockOnCacheKey(Vector primaryKey, Class domainClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().acquireReadLockOnCacheKey(primaryKey, domainClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().acquireReadLockOnCacheKey(primaryKey, domainClass, descriptor);
        }
    }

    /**
     * INTERNAL:
     * Find the cachekey for the provided primary key and place a readlock on it.
     * This will allow multiple users to read the same object but prevent writes to
     * the object while the read lock is held.
     * If no readlock can be acquired then do not wait but return null.
     */
    public CacheKey acquireReadLockOnCacheKeyNoWait(Vector primaryKey, Class domainClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().acquireReadLockOnCacheKeyNoWait(primaryKey, domainClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().acquireReadLockOnCacheKeyNoWait(primaryKey, domainClass, descriptor);
        }
    }

    /**
    * INTERNAL:
    * Lock the entire cache if the cache isolation requires.
    * By default concurrent reads and writes are allowed.
    * By write, unit of work merge is meant.
    */
    public boolean acquireWriteLock() {
        getIdentityMapManager().acquireWriteLock();
        // must lock the parents cache as well.
        return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().acquireWriteLock();
    }

    /**
     * INTERNAL:
     * Return whether the identity maps contain an item of the given class and key
     */
    public boolean containsKey(Vector key, Class theClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().containsKey(key, theClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().containsKey(key, theClass, descriptor);
        }
    }

    /**
     * ADVANCED:
     * Return if their is an object for the primary key.
     */
    public boolean containsObjectInIdentityMap(Vector primaryKey, Class theClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().containsKey(primaryKey, theClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().containsObjectInIdentityMap(primaryKey, theClass, descriptor);
        }
    }

    /**
     * INTERNAL:
     * This method is used to get a list of those classes with IdentityMaps in the Session.
     */
    public Vector getClassesRegistered() {
        Vector results = getIdentityMapManager().getClassesRegistered();
        results.addAll(((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getClassesRegistered());
        return results;
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     * Only return objects that are invalid in the cache if specified.
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean shouldReturnInvalidatedObjects) throws QueryException {
        if (session.getDescriptor(theClass).isIsolated()) {
            return getIdentityMapManager().getAllFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, shouldReturnInvalidatedObjects);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getAllFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, shouldReturnInvalidatedObjects);
        }
    }

    /**
     * INTERNAL:
     * Retrieve the cache key for the given identity information
     * @param Vector the primary key of the cache key to be retrieved
     * @param Class the class of the cache key to be retrieved
     * @return CacheKey
     */
    public CacheKey getCacheKeyForObject(Vector primaryKey, Class myClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().getCacheKeyForObject(primaryKey, myClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getCacheKeyForObject(primaryKey, myClass, descriptor);
        }
    }

    /**
     * ADVANCED:
     * Return the object from the identity with the primary and class.
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().getFromIdentityMap(primaryKey, theClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getFromIdentityMap(primaryKey, theClass, descriptor);
        }
    }

    /**
     * INTERNAL:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean conforming, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().getFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, conforming, shouldReturnInvalidatedObjects, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, conforming, shouldReturnInvalidatedObjects, descriptor);
        }
    }

    /**
     * INTERNAL:
     * Return the object from the identity with the primary and class.
     * Only return invalidated objects if requested
     */
    public Object getFromIdentityMapWithDeferredLock(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().getFromIdentityMapWithDeferredLock(primaryKey, theClass, shouldReturnInvalidatedObjects, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getFromIdentityMapWithDeferredLock(primaryKey, theClass, shouldReturnInvalidatedObjects, descriptor);
        }
    }

    /**
     * INTERNAL:
     * Get the IdentityMapManager for this IdentityMapAccessor
     * This method should be used for all IdentityMapManager access since it may
     * be overridden in sub classes.
     */
    public IdentityMapManager getIdentityMapManager() {
        return identityMapManager;
    }

    /**
     * INTERNAL:
     * Get the identity map for the given class from the IdentityMapManager
     */
    public IdentityMap getIdentityMap(ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().getIdentityMap(descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getIdentityMap(descriptor);
        }
    }

    /**
     * ADVANCED:
     * Return the remaining life of this object.  This method is associated with use of
     * TopLink's cache invalidation feature and returns the difference between the next expiry
     * time of the object and its read time.  The method will return 0 for invalidated objects.
     */
    public long getRemainingValidTime(Object object) {
        ClassDescriptor descriptor = getSession().getDescriptor(object);
        CacheKey key = this.getCacheKeyForObject(object, descriptor);
        if (key == null) {
            throw QueryException.objectDoesNotExistInCache(object);
        }
        return descriptor.getCacheInvalidationPolicy().getRemainingValidTime(key);
    }

    /**
     * INTERNAL:
     * get the session associated with this IdentityMapAccessor
     */
    public AbstractSession getSession() {
        return session;
    }

    /**
     * INTERNAL:
     * Get the wrapper object from the cache key associated with the given primary key,
     * this is used for EJB.
     */
    public Object getWrapper(Vector primaryKey, Class theClass) {
        if (session.getDescriptor(theClass).isIsolated()) {
            return getIdentityMapManager().getWrapper(primaryKey, theClass);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getWrapper(primaryKey, theClass);
        }
    }

    /**
     * INTERNAL:
     * Returns the single write Lock manager for this session
     */
    public WriteLockManager getWriteLockManager() {
        // As there should only be one write lock manager per server session
        // get the one from the parent.
        return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getWriteLockManager();
    }

    /**
     * ADVANCED:
     * Extract the write lock value from the identity map.
     */
    public Object getWriteLockValue(Vector primaryKey, Class theClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().getWriteLockValue(primaryKey, theClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().getWriteLockValue(primaryKey, theClass, descriptor);
        }
    }

    /**
     * PUBLIC:
     * Reset the entire object cache.
     * <p> NOTE: be careful using this method. This method blows away both this session's and its parents caches,
     * this includes the server cache or any other cache. This throws away any objects that have been read in.
     * Extream caution should be used before doing this because object identity will no longer
     * be maintained for any objects currently read in.  This should only be called
     * if the application knows that it no longer has references to object held in the cache.
     */
    public void initializeAllIdentityMaps() {
        getSession().log(SessionLog.FINER, SessionLog.CACHE, "initialize_all_local_identitymaps");
        getIdentityMapManager().initializeIdentityMaps();
        ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().initializeIdentityMaps();
    }

    /**
     * PUBLIC:
     * Reset the identity map for only the instances of the class.
     * For inheritance the user must make sure that they only use the root class.
     * Caution must be used in doing this to ensure that the objects within the identity map
     * are not referenced from other objects of other classes or from the application.
     */
    public void initializeIdentityMap(Class theClass) {
        getSession().log(SessionLog.FINER, SessionLog.CACHE, "initialize_identitymap", theClass);
        if (session.getDescriptor(theClass).isIsolated()) {
            getIdentityMapManager().initializeIdentityMap(theClass);
        } else {
            ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().initializeIdentityMap(theClass);
        }
    }

    /**
     * PUBLIC:
     * Reset the entire local object cache.
     * This throws away any objects that have been read in.
     * Extream caution should be used before doing this because object identity will no longer
     * be maintained for any objects currently read in.  This should only be called
     * if the application knows that it no longer has references to object held in the cache.
     */
    public void initializeIdentityMaps() {
        getSession().log(SessionLog.FINER, SessionLog.CACHE, "initialize_identitymaps");
        getIdentityMapManager().initializeIdentityMaps();
        getSession().getCommitManager().reinitialize();
    }

    /**
     * PUBLIC:
     * Used to print all the objects in the identity map of the passed in class.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     */
    public void printIdentityMap(Class businessClass) {
        if (getSession().shouldLog(SessionLog.SEVERE, SessionLog.CACHE)) {
            if (session.getDescriptor(businessClass).isIsolated()) {
                getIdentityMapManager().printIdentityMap(businessClass);
            } else {
                ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().printIdentityMap(businessClass);
            }
        }
    }

    /**
     * PUBLIC:
     * Used to print all the objects in every identity map in this session.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     */
    public void printIdentityMaps() {
        if (getSession().shouldLog(SessionLog.SEVERE, SessionLog.CACHE)) {
            getIdentityMapManager().printIdentityMaps();
            ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().printIdentityMaps();
        }
    }

    /**
     * PUBLIC:
     * Used to print all the locks in every identity map in this session.
     * The output of this method will be logged to this session's SessionLog at FINEST level.
     */
    public void printIdentityMapLocks() {
        if (getSession().shouldLog(SessionLog.FINEST, SessionLog.CACHE)) {
            getIdentityMapManager().printLocks();
            ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().printIdentityMapLocks();
        }
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * The readTime may also be included in the cache key as it is constructed.
     */
    public CacheKey internalPutInIdentityMap(Object domainObject, Vector key, Object writeLockValue, long readTime, ClassDescriptor descriptor) {
        //no need to unwrap as the put will unwrap later anyway
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().putInIdentityMap(domainObject, key, writeLockValue, readTime, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().internalPutInIdentityMap(domainObject, key, writeLockValue, readTime, descriptor);
        }
    }

    /**
    * INTERNAL:
    * Lock the entire cache if the cache isolation requires.
    * By default concurrent reads and writes are allowed.
    * By write, unit of work merge is meant.
    */
    public void releaseWriteLock() {
        //release in the oposite order of the acquire
        ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().releaseWriteLock();
        getIdentityMapManager().releaseWriteLock();
    }

    /**
     * ADVANCED:
     * Remove the object from the object cache.
     */
    public Object removeFromIdentityMap(Vector key, Class theClass, ClassDescriptor descriptor) {
        if (descriptor.isIsolated()) {
            return getIdentityMapManager().removeFromIdentityMap(key, theClass, descriptor);
        } else {
            return ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().removeFromIdentityMap(key, theClass, descriptor);
        }
    }

    /**
     * INTERNAL:
     * Set the IdentityMapManager for this IdentityMapAccessor
     */
    public void setIdentityMapManager(IdentityMapManager identityMapManager) {
        this.identityMapManager = identityMapManager;
    }

    /**
     * INTERNAL:
     * Update the wrapper object the cache key associated with the given primary key,
     * this is used for EJB.
     */
    public void setWrapper(Vector primaryKey, Class theClass, Object wrapper) {
        if (getSession().getDescriptor(theClass).isIsolated()) {
            getIdentityMapManager().setWrapper(primaryKey, theClass, wrapper);
        } else {
            ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().setWrapper(primaryKey, theClass, wrapper);
        }
    }

    /**
     * ADVANCED:
     * Update the write lock value in the identity map.
     */
    public void updateWriteLockValue(Vector primaryKey, Class theClass, Object writeLockValue) {
        if (getSession().getDescriptor(theClass).isIsolated()) {
            getIdentityMapManager().setWriteLockValue(primaryKey, theClass, writeLockValue);
        } else {
            ((IsolatedClientSession)session).getParent().getIdentityMapAccessorInstance().updateWriteLockValue(primaryKey, theClass, writeLockValue);
        }
    }
}
