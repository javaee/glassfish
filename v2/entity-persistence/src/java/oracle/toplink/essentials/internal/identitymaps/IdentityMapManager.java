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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.localization.*;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedGetConstructorFor;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.essentials.internal.security.PrivilegedInvokeConstructor;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>: Maintain identity maps for domain classes mapped with TopLink.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Build new identity maps lazily using info from the descriptor
 *    <li> Insert objects into appropriate identity map
 *    <li> Get object from appropriate identity map using object or primary key with class
 *    <li> Get and Set write lock values for cached objects
 * </ul>
 *    @since TOPLink/Java 1.0
 */
public class IdentityMapManager implements Serializable, Cloneable {

    /** A table of identity maps with the key being the domain Class. */
    protected Hashtable identityMaps;

    /** A table of identity maps with the key being the query */
    protected Map queryResults;

    /** A reference to the session owning this manager. */
    protected AbstractSession session;

    /** Ensure mutual exclusion depending on the cache isolation.*/
    protected transient ConcurrencyManager cacheMutex;

    /** Optimize the object retrival from the identity map. */
    protected IdentityMap lastAccessedIdentityMap = null;
    protected Class lastAccessedIdentityMapClass = null;

    /** Used to store the write lock manager used for merging. */
    protected transient WriteLockManager writeLockManager;

    /** PERF: Used to avoid readLock and profiler checks to improve performance. */
    protected Boolean isCacheAccessPreCheckRequired;

    public IdentityMapManager(AbstractSession session) {
        this.session = session;
        this.cacheMutex = new ConcurrencyManager();
        this.identityMaps = new Hashtable();
        this.queryResults = JavaPlatform.getQueryCacheMap();
    }

    /**
     * Provides access for setting a deferred lock on an object in the IdentityMap.
     */
    public CacheKey acquireDeferredLock(Vector primaryKey, Class domainClass, ClassDescriptor descriptor) {
        CacheKey cacheKey = null;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                cacheKey = getIdentityMap(descriptor).acquireDeferredLock(primaryKey);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            cacheKey = getIdentityMap(descriptor).acquireDeferredLock(primaryKey);
        }

        return cacheKey;
    }

    /**
     * Provides access for setting a concurrency lock on an object in the IdentityMap.
     * called with true from the merge process, if true then the refresh will not refresh the object
     *    @see IdentityMap#aquire
     */
    public CacheKey acquireLock(Vector primaryKey, Class domainClass, boolean forMerge, ClassDescriptor descriptor) {
        CacheKey cacheKey = null;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                cacheKey = getIdentityMap(descriptor).acquireLock(primaryKey, forMerge);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            cacheKey = getIdentityMap(descriptor).acquireLock(primaryKey, forMerge);
        }

        return cacheKey;
    }

    /**
     * Provides access for setting a concurrency lock on an object in the IdentityMap.
     * called with true from the merge process, if true then the refresh will not refresh the object
     *    @see IdentityMap#aquire
     */
    public CacheKey acquireLockNoWait(Vector primaryKey, Class domainClass, boolean forMerge, ClassDescriptor descriptor) {
        CacheKey cacheKey = null;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                cacheKey = getIdentityMap(descriptor).acquireLockNoWait(primaryKey, forMerge);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            cacheKey = getIdentityMap(descriptor).acquireLockNoWait(primaryKey, forMerge);
        }

        return cacheKey;
    }

    /**
     * PERF: Used to micro optimize cache access.
     * Avoid the readLock and profile checks if not required.
     */
    protected boolean isCacheAccessPreCheckRequired() {
        if (this.isCacheAccessPreCheckRequired == null) {
            if ((getSession().getProfiler() != null) || getSession().getDatasourceLogin().shouldSynchronizedReadOnWrite()) {
                this.isCacheAccessPreCheckRequired = Boolean.TRUE;
            } else {
                this.isCacheAccessPreCheckRequired = Boolean.FALSE;
            }
        }
        return this.isCacheAccessPreCheckRequired.booleanValue();
    }

    /**
     * Clear the cache access pre-check flag, used from session when profiler .
     */
    public void clearCacheAccessPreCheck() {
        this.isCacheAccessPreCheckRequired = null;
    }

    /**
     * Provides access for setting a concurrency lock on an IdentityMap.
     * @see IdentityMap#aquire
     */
    public void acquireReadLock() {
        getSession().startOperationProfile(SessionProfiler.CACHE);

        if (getSession().getDatasourceLogin().shouldSynchronizedReadOnWrite()) {
            getCacheMutex().acquireReadLock();
        }

        getSession().endOperationProfile(SessionProfiler.CACHE);
    }

    /**
     * INTERNAL:
     * Find the cachekey for the provided primary key and place a readlock on it.
     * This will allow multiple users to read the same object but prevent writes to
     * the object while the read lock is held.
     */
    public CacheKey acquireReadLockOnCacheKey(Vector primaryKey, Class domainClass, ClassDescriptor descriptor) {
        CacheKey cacheKey = null;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                cacheKey = getIdentityMap(descriptor).acquireReadLockOnCacheKey(primaryKey);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            cacheKey = getIdentityMap(descriptor).acquireReadLockOnCacheKey(primaryKey);
        }

        return cacheKey;
    }

    /**
     * INTERNAL:
     * Find the cachekey for the provided primary key and place a readlock on it.
     * This will allow multiple users to read the same object but prevent writes to
     * the object while the read lock is held.
     * If no readlock can be acquired then do not wait but return null.
     */
    public CacheKey acquireReadLockOnCacheKeyNoWait(Vector primaryKey, Class domainClass, ClassDescriptor descriptor) {
        CacheKey cacheKey = null;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                cacheKey = getIdentityMap(descriptor).acquireReadLockOnCacheKeyNoWait(primaryKey);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            cacheKey = getIdentityMap(descriptor).acquireReadLockOnCacheKeyNoWait(primaryKey);
        }

        return cacheKey;
    }

    /**
     * Lock the entire cache if the cache isolation requires.
     * By default concurrent reads and writes are allowed.
     * By write, unit of work merge is meant.
     */
    public boolean acquireWriteLock() {
        if (getSession().getDatasourceLogin().shouldSynchronizedReadOnWrite() || getSession().getDatasourceLogin().shouldSynchronizeWrites()) {
            getCacheMutex().acquire();
            return true;
        }
        return false;
    }

    /**
     * INTERNAL: (Public to allow testing to access)
     * Return a new empty identity map to cache instances of the class.
     */
    public IdentityMap buildNewIdentityMap(ClassDescriptor descriptor) throws ValidationException, DescriptorException {
        if (getSession().isUnitOfWork()) {
            return new FullIdentityMap(100);
        }

        try {
            Constructor constructor = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    constructor = (Constructor)AccessController.doPrivileged(new PrivilegedGetConstructorFor(descriptor.getIdentityMapClass(), new Class[] { ClassConstants.PINT }, false));
                    return (IdentityMap)AccessController.doPrivileged(new PrivilegedInvokeConstructor(constructor, new Object[] { new Integer(descriptor.getIdentityMapSize())}));
                } catch (PrivilegedActionException exception) {
                    throw DescriptorException.invalidIdentityMap(descriptor, exception.getException());
                }
            } else {
                constructor = PrivilegedAccessHelper.getConstructorFor(descriptor.getIdentityMapClass(), new Class[] { ClassConstants.PINT }, false);
                return (IdentityMap)PrivilegedAccessHelper.invokeConstructor(constructor, new Object[] { new Integer(descriptor.getIdentityMapSize())});

            }
        } catch (Exception exception) {
            throw DescriptorException.invalidIdentityMap(descriptor, exception);
        }
    }

    /**
     * INTERNAL:
     * Clear the the lastAccessedIdentityMap and the lastAccessedIdentityMapClass
     */
    public void clearLastAccessedIdentityMap() {
        lastAccessedIdentityMap = null;
        lastAccessedIdentityMapClass = null;
    }

    /**
     * INTERNAL:
     * Clones itself, used for uow commit and resume on failure.
     */
    public Object clone() {
        IdentityMapManager manager = null;

        try {
            manager = (IdentityMapManager)super.clone();
            manager.setIdentityMaps(new Hashtable());
            for (Enumeration identityMapEnum = getIdentityMaps().keys();
                     identityMapEnum.hasMoreElements();) {
                Class theClass = (Class)identityMapEnum.nextElement();
                manager.getIdentityMaps().put(theClass, ((IdentityMap)getIdentityMaps().get(theClass)).clone());
            }
        } catch (Exception e) {
            ;
        }

        return manager;
    }

    /**
     * Clear all the query caches
     */
    public void clearQueryCache() {
        this.queryResults = JavaPlatform.getQueryCacheMap();
    }

    /**
     * Remove the cache key related to a query.
     * Note this method is not synchronized and care should be taken to ensure
     * there are no other threads accessing the cache key.  This is used to clean up
     * cached clones of queries
     */
    public void clearQueryCache(ReadQuery query) {
        if (query != null) {
            queryResults.remove(query);
        }
    }

    public boolean containsKey(Vector key, Class theClass, ClassDescriptor descriptor) {
        // Check for null, contains causes null pointer.
        for (int index = 0; index < key.size(); index++) {
            if (key.elementAt(index) == null) {
                return false;
            }
        }

        IdentityMap map = getIdentityMap(descriptor);
        boolean contains;

        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                contains = map.containsKey(key);
            } finally {
                releaseReadLock();
                getSession().endOperationProfile(SessionProfiler.CACHE);
            }
        } else {
            contains = map.containsKey(key);
        }

        return contains;
    }

    /**
     * Query the cache in-memory.
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean shouldReturnInvalidatedObjects) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        getSession().startOperationProfile(SessionProfiler.CACHE);
        Vector objects = null;
        try {
            Expression selectionCriteriaClone = selectionCriteria;

            // Only clone if required.
            if ((selectionCriteria != null) && (selectionCriteriaClone.getBuilder().getSession() == null)) {
                selectionCriteriaClone = (Expression)selectionCriteria.clone();
                selectionCriteriaClone.getBuilder().setSession(getSession().getRootSession(null));
                selectionCriteriaClone.getBuilder().setQueryClass(theClass);
            }
            objects = new Vector();
            IdentityMap map = getIdentityMap(descriptor);

            // cache the current time to avoid calculating it every time through the loop
            long currentTimeInMillis = System.currentTimeMillis();
            for (Enumeration cacheEnum = map.keys(); cacheEnum.hasMoreElements();) {
                CacheKey key = (CacheKey)cacheEnum.nextElement();
                if ((key.getObject() == null) || (!shouldReturnInvalidatedObjects && getSession().getDescriptor(theClass).getCacheInvalidationPolicy().isInvalidated(key, currentTimeInMillis))) {
                    continue;
                }
                Object object = key.getObject();

                // Bug # 3216337 - key.getObject() should check for null; object may be GC'd (MWN)
                if (object == null) {
                    continue;
                }

                // Must check for inheritance.
                if ((object.getClass() == theClass) || (theClass.isInstance(object))) {
                    if (selectionCriteriaClone == null) {
                        objects.addElement(object);
                        getSession().incrementProfile(SessionProfiler.CacheHits);
                    } else {
                        try {
                            if (selectionCriteriaClone.doesConform(object, getSession(), (AbstractRecord)translationRow, valueHolderPolicy)) {
                                objects.addElement(object);
                                getSession().incrementProfile(SessionProfiler.CacheHits);
                            }
                        } catch (QueryException queryException) {
                            if (queryException.getErrorCode() == QueryException.MUST_INSTANTIATE_VALUEHOLDERS) {
                                if (valueHolderPolicy.shouldIgnoreIndirectionExceptionReturnConformed()) {
                                    objects.addElement(object);
                                    getSession().incrementProfile(SessionProfiler.CacheHits);
                                } else if (valueHolderPolicy.shouldThrowIndirectionException()) {
                                    throw queryException;
                                }
                            } else {
                                throw queryException;
                            }
                        }
                    }
                }
            }
        } finally {
            getSession().endOperationProfile(SessionProfiler.CACHE);
        }
        return objects;
    }

    /**
     * INTERNAL:
     * Retrieve the cache key for the given identity information
     * @param Vector the primary key of the cache key to be retrieved
     * @param Class the class of the cache key to be retrieved
     * @return CacheKey
     */
    public CacheKey getCacheKeyForObject(Vector primaryKey, Class myClass, ClassDescriptor descriptor) {
        IdentityMap map = getIdentityMap(descriptor);
        CacheKey cacheKey = null;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                cacheKey = map.getCacheKey(primaryKey);
            } finally {
                releaseReadLock();
                getSession().endOperationProfile(SessionProfiler.CACHE);
            }
        } else {
            cacheKey = map.getCacheKey(primaryKey);
        }
        return cacheKey;
    }

    /**
     * Return the cache mutex.
     * This allows for the entire cache to be locked.
     * This is done for transaction isolations on merges, although never locked by default.
     */
    public ConcurrencyManager getCacheMutex() {
        return cacheMutex;
    }

    /**
     * INTERNAL:
     *        This method is used to get a list of those classes with IdentityMaps in the Session.
     */
    public Vector getClassesRegistered() {
        Enumeration classes = getIdentityMaps().keys();
        Vector results = new Vector(getIdentityMaps().size());
        while (classes.hasMoreElements()) {
            results.add(((Class)classes.nextElement()).getName());
        }
        return results;
    }

    /**
     * Get the object from the identity map which has the same identity information
     * as the given object.
     */
    public Object getFromIdentityMap(Object object) {
        ClassDescriptor descriptor = getSession().getDescriptor(object);
        Vector primaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(object, getSession());
        return getFromIdentityMap(primaryKey, object.getClass(), descriptor);
    }

    /**
     * Get the object from the identity map which has the given primary key and class
     */
    public Object getFromIdentityMap(Vector key, Class theClass, ClassDescriptor descriptor) {
        return getFromIdentityMap(key, theClass, true, descriptor);
    }

    /**
     * Get the object from the identity map which has the given primary key and class
     * Only return the object if it has not Invalidated
     */
    public Object getFromIdentityMap(Vector key, Class theClass, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        if (key == null) {
            return null;
        }

        // Check for null, contains causes null pointer.
        for (int index = 0; index < key.size(); index++) {
            if (key.elementAt(index) == null) {
                return null;
            }
        }

        CacheKey cacheKey;
        IdentityMap map = getIdentityMap(descriptor);
        Object domainObject = null;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                cacheKey = map.getCacheKey(key);
            } finally {
                releaseReadLock();
            }
        } else {
            cacheKey = map.getCacheKey(key);
        }

        if ((cacheKey != null) && (shouldReturnInvalidatedObjects || !getSession().getDescriptor(theClass).getCacheInvalidationPolicy().isInvalidated(cacheKey, System.currentTimeMillis()))) {
            //bug 4772232- acquire readlock on cachekey then release to ensure object is fully built before being returned
            try {
                cacheKey.acquireReadLock();
            } finally {
                cacheKey.releaseReadLock();
            }
            //bug 4772232- acquire readlock on cachekey then release to ensure object is fully built before being returned
            try {
                cacheKey.acquireReadLock();
                domainObject = cacheKey.getObject();
            } finally {
                cacheKey.releaseReadLock();
            }
            //reslove the inheritance issues
            domainObject = checkForInheritance(domainObject, theClass);
        }

        if (isCacheAccessPreCheckRequired()) {
            getSession().endOperationProfile(SessionProfiler.CACHE);
            if (domainObject == null) {
                getSession().incrementProfile(SessionProfiler.CacheMisses);
            } else {
                getSession().incrementProfile(SessionProfiler.CacheHits);
            }
        }

        return domainObject;
    }

    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean conforming, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        UnitOfWorkImpl unitOfWork = (conforming) ? (UnitOfWorkImpl)getSession() : null;
        getSession().startOperationProfile(SessionProfiler.CACHE);
        try {
            Expression selectionCriteriaClone = selectionCriteria;

            // Only clone if required.
            if ((selectionCriteria != null) && (selectionCriteriaClone.getBuilder().getSession() == null)) {
                selectionCriteriaClone = (Expression)selectionCriteria.clone();
                selectionCriteriaClone.getBuilder().setSession(getSession().getRootSession(null));
                selectionCriteriaClone.getBuilder().setQueryClass(theClass);
            }
            IdentityMap map = getIdentityMap(descriptor);

            // cache the current time to avoid calculating it every time through the loop
            long currentTimeInMillis = System.currentTimeMillis();
            for (Enumeration cacheEnum = map.keys(); cacheEnum.hasMoreElements();) {
                CacheKey key = (CacheKey)cacheEnum.nextElement();
                if (!shouldReturnInvalidatedObjects && descriptor.getCacheInvalidationPolicy().isInvalidated(key, currentTimeInMillis)) {
                    continue;
                }
                Object object = key.getObject();

                // Bug # 3216337 - key.getObject() should check for null; object may be GC'd (MWN)
                if (object == null) {
                    continue;
                }

                // Must check for inheritance.
                if ((object.getClass() == theClass) || (theClass.isInstance(object))) {
                    if (selectionCriteriaClone == null) {
                        // bug 2782991: if first found was deleted nothing returned. 
                        if (!(conforming && unitOfWork.isObjectDeleted(object))) {
                            getSession().incrementProfile(SessionProfiler.CacheHits);
                            return object;
                        }
                    }

                    //CR 3677 integration of a ValueHolderPolicy
                    try {
                        if (selectionCriteriaClone.doesConform(object, getSession(), (AbstractRecord)translationRow, valueHolderPolicy)) {
                            // bug 2782991: if first found was deleted nothing returned. 
                            if (!(conforming && unitOfWork.isObjectDeleted(object))) {
                                getSession().incrementProfile(SessionProfiler.CacheHits);
                                return object;
                            }
                        }
                    } catch (QueryException queryException) {
                        if (queryException.getErrorCode() == QueryException.MUST_INSTANTIATE_VALUEHOLDERS) {
                            if (valueHolderPolicy.shouldIgnoreIndirectionExceptionReturnConformed()) {
                                // bug 2782991: if first found was deleted nothing returned. 
                                if (!(conforming && unitOfWork.isObjectDeleted(object))) {
                                    getSession().incrementProfile(SessionProfiler.CacheHits);
                                    return object;
                                }
                            } else if (valueHolderPolicy.shouldIgnoreIndirectionExceptionReturnNotConformed()) {
                                // For bug 2667870 just skip this item, but do not abort.
                            } else {
                                throw queryException;
                            }
                        } else {
                            throw queryException;
                        }
                    }
                }
            }
        } finally {
            getSession().endOperationProfile(SessionProfiler.CACHE);
        }
        return null;
    }

    /**
     * Get the object from the cache with the given primary key and class
     * do not return the object if it was invalidated
     */
    public Object getFromIdentityMapWithDeferredLock(Vector key, Class theClass, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        if (key == null) {
            getSession().incrementProfile(SessionProfiler.CacheMisses);
            return null;
        }

        // Check for null, contains causes null pointer.
        for (int index = 0; index < key.size(); index++) {
            if (key.elementAt(index) == null) {
                getSession().incrementProfile(SessionProfiler.CacheMisses);
                return null;
            }
        }

        IdentityMap map = getIdentityMap(descriptor);
        CacheKey cacheKey;
        Object domainObject = null;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                cacheKey = map.getCacheKey(key);
            } finally {
                releaseReadLock();
            }
        } else {
            cacheKey = map.getCacheKey(key);
        }

        if ((cacheKey != null) && (shouldReturnInvalidatedObjects || !descriptor.getCacheInvalidationPolicy().isInvalidated(cacheKey, System.currentTimeMillis()))) {
            cacheKey.acquireDeferredLock();
            domainObject = cacheKey.getObject();
            cacheKey.releaseDeferredLock();
        }

        //reslove the inheritance issues
        domainObject = checkForInheritance(domainObject, theClass);

        if (isCacheAccessPreCheckRequired()) {
            getSession().endOperationProfile(SessionProfiler.CACHE);
            if (domainObject == null) {
                getSession().incrementProfile(SessionProfiler.CacheMisses);
            } else {
                getSession().incrementProfile(SessionProfiler.CacheHits);
            }
        }

        return domainObject;
    }

    /**
     *    INTERNAL: (public to allow test cases to check)
     * Return the identity map for the class, if missing create a new one.
     */
    public IdentityMap getIdentityMap(ClassDescriptor descriptor) {
        IdentityMap identityMap;

        // Enusre that an im is only used for the root descriptor for inheritence.
        // This is required to obtain proper cache hits.
        if (descriptor.hasInheritance()) {
            descriptor = descriptor.getInheritancePolicy().getRootParentDescriptor();
        }
        Class descriptorClass = descriptor.getJavaClass();

        // PERF: Synchronize around caching of last accessed map and lookup/build,
        // note that get is synchronized anyway so this is not adding any additional synching.
        synchronized (this) {
            // Optimazition for object retrival.
            IdentityMap tempMap = this.lastAccessedIdentityMap;
            if ((tempMap != null) && (this.lastAccessedIdentityMapClass == descriptorClass)) {
                return tempMap;
            }

            // PERF: Only synch around creation.
            identityMap = (IdentityMap)getIdentityMaps().get(descriptorClass);
            if (identityMap == null) {
                identityMap = buildNewIdentityMap(descriptor);
                getIdentityMaps().put(descriptorClass, identityMap);
            }
            this.lastAccessedIdentityMap = identityMap;
            this.lastAccessedIdentityMapClass = descriptorClass;
        }
        return identityMap;
    }

    protected Hashtable getIdentityMaps() {
        return identityMaps;
    }
    
    /**
     * INTERNAL:
     * 
     * @return an enumeration of the classes in the identity map. 
     */
    public Enumeration getIdentityMapClasses() {
        return identityMaps.keys();
    }

    protected Vector getKey(Object domainObject) {
        return getSession().keyFromObject(domainObject);
    }

    protected AbstractSession getSession() {
        return session;
    }

    /**
     * Get the wrapper object from the cache key associated with the given primary key,
     * this is used for EJB.
     */
    public Object getWrapper(Vector primaryKey, Class theClass) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        IdentityMap map = getIdentityMap(descriptor);
        Object wrapper;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                wrapper = map.getWrapper(primaryKey);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            wrapper = map.getWrapper(primaryKey);
        }
        return wrapper;
    }

    /**
     * INTERNAL:
     * Returns the single write Lock manager for this session
     */
    public WriteLockManager getWriteLockManager() {
        // With Isolated Sessions not all Identity maps need a WriteLockManager so
        //lazy initialize
        synchronized (this) {
            if (this.writeLockManager == null) {
                this.writeLockManager = new WriteLockManager();
            }
        }
        return this.writeLockManager;
    }

    /**
     * Retrieve the write lock value of the cache key associated with the given primary key,
     */
    public Object getWriteLockValue(Vector primaryKey, Class domainClass, ClassDescriptor descriptor) {
        IdentityMap map = getIdentityMap(descriptor);
        Object value;
        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            acquireReadLock();
            try {
                value = map.getWriteLockValue(primaryKey);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            value = map.getWriteLockValue(primaryKey);
        }
        return value;
    }

    /**
     *    Reset the identity map for only the instances of the class.
     * For inheritence the user must make sure that they only use the root class.
     */
    public void initializeIdentityMap(Class theClass) throws TopLinkException {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);

        if (descriptor == null) {
            throw ValidationException.missingDescriptor(String.valueOf(theClass));
        }
        if (descriptor.isChildDescriptor()) {
            throw ValidationException.childDescriptorsDoNotHaveIdentityMap();
        }

        // Bug 3736313 - look up identity map by descriptor's java class
        Class javaClass = descriptor.getJavaClass();

        // bug 3745484 - clear the cached identity map if we are clearing the associated identity map
        if (javaClass == lastAccessedIdentityMapClass) {
            clearLastAccessedIdentityMap();
        }
        IdentityMap identityMap = buildNewIdentityMap(descriptor);
        getIdentityMaps().put(javaClass, identityMap);
    }

    public void initializeIdentityMaps() {
        clearLastAccessedIdentityMap();
        setIdentityMaps(new Hashtable());
        clearQueryCache();
    }

    /**
     * INTERNAL:
     * Used to print all the objects in the identity map of the passed in class.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     */
    public void printIdentityMap(Class businessClass) {
        String cr = Helper.cr();
        ClassDescriptor descriptor = getSession().getDescriptor(businessClass);
        int cacheCounter = 0;
        StringWriter writer = new StringWriter();
        if (descriptor.isAggregateDescriptor()) {
            return;//do nothing if descriptor is aggregate
        }

        IdentityMap map = getIdentityMap(descriptor);
        writer.write(LoggingLocalization.buildMessage("identitymap_for", new Object[] { cr, Helper.getShortClassName(map.getClass()), Helper.getShortClassName(businessClass) }));
        if (descriptor.hasInheritance()) {
            if (descriptor.getInheritancePolicy().isRootParentDescriptor()) {
                writer.write(LoggingLocalization.buildMessage("includes"));
                Vector childDescriptors;
                childDescriptors = descriptor.getInheritancePolicy().getChildDescriptors();
                if ((childDescriptors != null) && (childDescriptors.size() != 0)) {//Bug#2675242
                    Enumeration enum2 = childDescriptors.elements();
                    writer.write(Helper.getShortClassName((Class)((ClassDescriptor)enum2.nextElement()).getJavaClass()));
                    while (enum2.hasMoreElements()) {
                        writer.write(", " + Helper.getShortClassName((Class)((ClassDescriptor)enum2.nextElement()).getJavaClass()));
                    }
                }
                writer.write(")");
            }
        }

        for (Enumeration enumtr = map.keys(); enumtr.hasMoreElements();) {
            oracle.toplink.essentials.internal.identitymaps.CacheKey cacheKey = (oracle.toplink.essentials.internal.identitymaps.CacheKey)enumtr.nextElement();
            Object object = cacheKey.getObject();
            if (businessClass.isInstance(object)) {
                cacheCounter++;
                if (object == null) {
                    writer.write(LoggingLocalization.buildMessage("key_object_null", new Object[] { cr, cacheKey.getKey(), "\t" }));
                } else {
                    writer.write(LoggingLocalization.buildMessage("key_identity_hash_code_object", new Object[] { cr, cacheKey.getKey(), "\t", String.valueOf(System.identityHashCode(object)), object }));
                }
            }
        }
        writer.write(LoggingLocalization.buildMessage("elements", new Object[] { cr, String.valueOf(cacheCounter) }));
        getSession().log(SessionLog.SEVERE, SessionLog.CACHE, writer.toString(), null, null, false);
    }

    /**
     * INTERNAL:
     * Used to print all the objects in every identity map in this session.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     */
    public void printIdentityMaps() {
        for (Iterator iterator = getSession().getDescriptors().keySet().iterator();
                 iterator.hasNext();) {
            Class businessClass = (Class)iterator.next();
            ClassDescriptor descriptor = getSession().getDescriptor(businessClass);
            if (descriptor.hasInheritance()) {
                if (descriptor.getInheritancePolicy().isRootParentDescriptor()) {
                    printIdentityMap(businessClass);
                }
            } else {
                printIdentityMap(businessClass);
            }
        }
    }

    /**
     * INTERNAL:
     * Used to print all the Locks in every identity map in this session.
     * The output of this method will be logged to this session's SessionLog at FINEST level.
     */
    public void printLocks() {
        StringWriter writer = new StringWriter();
        HashMap threadCollection = new HashMap();
        writer.write(TraceLocalization.buildMessage("lock_writer_header", (Object[])null) + Helper.cr());
        Iterator idenityMapsIterator = this.session.getIdentityMapAccessorInstance().getIdentityMapManager().getIdentityMaps().values().iterator();
        while (idenityMapsIterator.hasNext()) {
            IdentityMap idenityMap = (IdentityMap)idenityMapsIterator.next();
            idenityMap.collectLocks(threadCollection);
        }
        Object[] parameters = new Object[1];
        for (Iterator threads = threadCollection.keySet().iterator(); threads.hasNext();) {
            Thread activeThread = (Thread)threads.next();
            parameters[0] = activeThread.getName();
            writer.write(TraceLocalization.buildMessage("active_thread", parameters) + Helper.cr());
            for (Iterator cacheKeys = ((HashSet)threadCollection.get(activeThread)).iterator();
                     cacheKeys.hasNext();) {
                CacheKey cacheKey = (CacheKey)cacheKeys.next();
                parameters[0] = cacheKey.getObject();
                writer.write(TraceLocalization.buildMessage("locked_object", parameters) + Helper.cr());
                parameters[0] = new Integer(cacheKey.getMutex().getDepth());
                writer.write(TraceLocalization.buildMessage("depth", parameters) + Helper.cr());
            }
            DeferredLockManager deferredLockManager = ConcurrencyManager.getDeferredLockManager(activeThread);
            if (deferredLockManager != null) {
                for (Iterator deferredLocks = deferredLockManager.getDeferredLocks().iterator();
                         deferredLocks.hasNext();) {
                    ConcurrencyManager lock = (ConcurrencyManager)deferredLocks.next();
                    parameters[0] = lock.getOwnerCacheKey().getObject();
                    writer.write(TraceLocalization.buildMessage("deferred_locks", parameters) + Helper.cr());
                }
            }
        }
        writer.write(Helper.cr() + TraceLocalization.buildMessage("lock_writer_footer", (Object[])null) + Helper.cr());
        getSession().log(SessionLog.FINEST, SessionLog.CACHE, writer.toString(), null, null, false);
    }

    /**
     * INTERNAL:
     * Used to print all the Locks in the specified identity map in this session.
     * The output of this method will be logged to this session's SessionLog at FINEST level.
     */
    public void printLocks(Class theClass) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        StringWriter writer = new StringWriter();
        HashMap threadCollection = new HashMap();
        writer.write(TraceLocalization.buildMessage("lock_writer_header", (Object[])null) + Helper.cr());
        IdentityMap identityMap = getIdentityMap(descriptor);
        identityMap.collectLocks(threadCollection);

        Object[] parameters = new Object[1];
        for (Iterator threads = threadCollection.keySet().iterator(); threads.hasNext();) {
            Thread activeThread = (Thread)threads.next();
            parameters[0] = activeThread.getName();
            writer.write(TraceLocalization.buildMessage("active_thread", parameters) + Helper.cr());
            for (Iterator cacheKeys = ((HashSet)threadCollection.get(activeThread)).iterator();
                     cacheKeys.hasNext();) {
                CacheKey cacheKey = (CacheKey)cacheKeys.next();
                parameters[0] = cacheKey.getObject();
                writer.write(TraceLocalization.buildMessage("locked_object", parameters) + Helper.cr());
                parameters[0] = new Integer(cacheKey.getMutex().getDepth());
                writer.write(TraceLocalization.buildMessage("depth", parameters) + Helper.cr());
            }
            DeferredLockManager deferredLockManager = ConcurrencyManager.getDeferredLockManager(activeThread);
            if (deferredLockManager != null) {
                for (Iterator deferredLocks = deferredLockManager.getDeferredLocks().iterator();
                         deferredLocks.hasNext();) {
                    ConcurrencyManager lock = (ConcurrencyManager)deferredLocks.next();
                    parameters[0] = lock.getOwnerCacheKey().getObject();
                    writer.write(TraceLocalization.buildMessage("deferred_locks", parameters) + Helper.cr());
                }
            }
        }
        writer.write(Helper.cr() + TraceLocalization.buildMessage("lock_writer_footer", (Object[])null) + Helper.cr());
        getSession().log(SessionLog.FINEST, SessionLog.CACHE, writer.toString(), null, null, false);
    }

    /**
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * The readTime may also be included in the cache key as it is constructed
     */
    public CacheKey putInIdentityMap(Object domainObject, Vector keys, Object writeLockValue, long readTime, ClassDescriptor descriptor) {
        ObjectBuilder builder = descriptor.getObjectBuilder();
        Object implementation = builder.unwrapObject(domainObject, getSession());

        IdentityMap map = getIdentityMap(descriptor);
        CacheKey cacheKey;

        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            // This is atomic so considered a read lock.
            acquireReadLock();
            try {
                cacheKey = map.put(keys, implementation, writeLockValue, readTime);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            cacheKey = map.put(keys, implementation, writeLockValue, readTime);
        }
        return cacheKey;
    }

    /**
     * Read-release the local-map and the entire cache.
     */
    protected void releaseReadLock() {
        if (getSession().getDatasourceLogin().shouldSynchronizedReadOnWrite()) {
            getCacheMutex().releaseReadLock();
        }
    }

    /**
     * Lock the entire cache if the cache isolation requires.
     * By default concurrent reads and writes are allowed.
     * By write, unit of work merge is meant.
     */
    public void releaseWriteLock() {
       if (getSession().getDatasourceLogin().shouldSynchronizedReadOnWrite() || getSession().getDatasourceLogin().shouldSynchronizeWrites()) {
             getCacheMutex().release();
        }
    }

    /**
     * Remove the object from the object cache.
     */
    public Object removeFromIdentityMap(Vector key, Class domainClass, ClassDescriptor descriptor) {
        IdentityMap map = getIdentityMap(descriptor);
        Object value;

        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            // This is atomic so considered a read lock.
            acquireReadLock();
            try {
                value = map.remove(key);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            value = map.remove(key);
        }
        return value;
    }

    /**
     * Set the cache mutex.
     * This allows for the entire cache to be locked.
     * This is done for transaction isolations on merges, although never locked by default.
     */
    protected void setCacheMutex(ConcurrencyManager cacheMutex) {
        this.cacheMutex = cacheMutex;
    }

    public void setIdentityMaps(Hashtable identityMaps) {
        clearLastAccessedIdentityMap();
        this.identityMaps = identityMaps;
    }

    protected void setSession(AbstractSession session) {
        this.session = session;
    }

    /**
     * Update the wrapper object the cache key associated with the given primary key,
     * this is used for EJB.
     */
    public void setWrapper(Vector primaryKey, Class theClass, Object wrapper) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        IdentityMap map = getIdentityMap(descriptor);

        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            // This is atomic so considered a read lock.
            acquireReadLock();
            try {
                map.setWrapper(primaryKey, wrapper);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            map.setWrapper(primaryKey, wrapper);
        }
    }

    /**
     * Update the write lock value of the cache key associated with the given primary key,
     */
    public void setWriteLockValue(Vector primaryKey, Class theClass, Object writeLockValue) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        IdentityMap map = getIdentityMap(descriptor);

        if (isCacheAccessPreCheckRequired()) {
            getSession().startOperationProfile(SessionProfiler.CACHE);
            // This is atomic so considered a read lock.
            acquireReadLock();
            try {
                map.setWriteLockValue(primaryKey, writeLockValue);
            } finally {
                releaseReadLock();
            }
            getSession().endOperationProfile(SessionProfiler.CACHE);
        } else {
            map.setWriteLockValue(primaryKey, writeLockValue);
        }
    }

    /**
     * This method is used to resolve the inheritance issues arisen when conforming from the identity map
     * 1. Avoid reading the unintended subclass during in-memory queyr(e.g. when querying on large project, do not want
     *    to check small project,  both are inheritanced from the project, and stored in the same identity map).
     * 2. EJB container-generated classes broke the inheritance hirearchy. Need to use associated descriptor to track
     *    the relationship. CR4005-2612426, King-Sept-18-2002
     */
    private Object checkForInheritance(Object domainObject, Class superClass) {
        if ((domainObject != null) && ((domainObject.getClass() != superClass) && (!superClass.isInstance(domainObject)))) {
            //before returning null, check if we are using EJB inheritance.
            ClassDescriptor descriptor = getSession().getDescriptor(superClass);

            if (descriptor.hasInheritance() && descriptor.getInheritancePolicy().getUseDescriptorsToValidateInheritedObjects()) {
                //EJB inheritance on the descriptors, not the container-generated classes/objects. We need to check the
                //identity map for the bean instance through the descriptor.
                if (descriptor.getInheritancePolicy().getSubclassDescriptor(domainObject.getClass()) == null) {
                    return null;
                }

                //else
                return domainObject;
            }

            //else
            return null;
        }

        //else
        return domainObject;
    }
}
