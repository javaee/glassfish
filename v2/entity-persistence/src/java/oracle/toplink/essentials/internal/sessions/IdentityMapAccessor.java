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
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
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
public class IdentityMapAccessor implements oracle.toplink.essentials.sessions.IdentityMapAccessor, java.io.Serializable {

    /** This is the identity map manager for this accessor.  It should only be accessed through the getter **/
    protected IdentityMapManager identityMapManager = null;
    protected AbstractSession session = null;

    /**
     * INTERNAL:
     * An IdentityMapAccessor sits between the session and the identityMapManager
     * It needs references in both directions
     */
    public IdentityMapAccessor(AbstractSession session, IdentityMapManager identityMapManager) {
        this.session = session;
        this.identityMapManager = identityMapManager;
    }

    /**
     * INTERNAL:
     * Deferred lock the identity map for the object, this is used for avoiding deadlock
     * The return cacheKey should be used to release the deferred lock
     */
    public CacheKey acquireDeferredLock(Vector primarKey, Class javaClass, ClassDescriptor descriptor) {
        return getIdentityMapManager().acquireDeferredLock(primarKey, javaClass, descriptor);
    }

    /**
     * INTERNAL:
     * Lock the identity map for the object, this must be done when building objects.
     * The return cacheKey should be used to release the lock
     */
    public CacheKey acquireLock(Vector primarKey, Class javaClass, ClassDescriptor descriptor) {
        return acquireLock(primarKey, javaClass, false, descriptor);
    }

    /**
     * INTERNAL:
     * Provides access for setting a concurrency lock on an object in the IdentityMap.
     * called with true from the merge process, if true then the refresh will not refresh the object
     *    @see IdentityMap#aquire
     */
    public CacheKey acquireLock(Vector primaryKey, Class domainClass, boolean forMerge, ClassDescriptor descriptor) {
        return getIdentityMapManager().acquireLock(primaryKey, domainClass, forMerge, descriptor);
    }

    /**
     * INTERNAL:
     * Provides access for setting a concurrency lock on an object in the IdentityMap.
     * called with true from the merge process, if true then the refresh will not refresh the object
     *    @see IdentityMap#aquire
     */
    public CacheKey acquireLockNoWait(Vector primaryKey, Class domainClass, boolean forMerge, ClassDescriptor descriptor) {
        return getIdentityMapManager().acquireLockNoWait(primaryKey, domainClass, forMerge, descriptor);
    }

    /**
     * INTERNAL:
     * Find the cachekey for the provided primary key and place a readlock on it.
     * This will allow multiple users to read the same object but prevent writes to
     * the object while the read lock is held.
     */
    public CacheKey acquireReadLockOnCacheKey(Vector primaryKey, Class domainClass, ClassDescriptor descriptor) {
        return getIdentityMapManager().acquireReadLockOnCacheKey(primaryKey, domainClass, descriptor);
    }

    /**
     * INTERNAL:
     * Find the cachekey for the provided primary key and place a readlock on it.
     * This will allow multiple users to read the same object but prevent writes to
     * the object while the read lock is held.
     * If no readlock can be acquired then do not wait but return null.
     */
    public CacheKey acquireReadLockOnCacheKeyNoWait(Vector primaryKey, Class domainClass, ClassDescriptor descriptor) {
        return getIdentityMapManager().acquireReadLockOnCacheKeyNoWait(primaryKey, domainClass, descriptor);
    }

    /**
     * INTERNAL:
     * Lock the entire cache if the cache isolation requires.
     * By default concurrent reads and writes are allowed.
     * By write, unit of work merge is meant.
     */
    public boolean acquireWriteLock() {
        return getIdentityMapManager().acquireWriteLock();
    }

    /**
     * ADVANCED:
     * Clear all the query caches
     */
    public void clearQueryCache() {
        getIdentityMapManager().clearQueryCache();
    }

    /**
     * ADVANCED:
     * Clear the query class associated with the passed-in read query
     */
    public void clearQueryCache(ReadQuery query) {
        getIdentityMapManager().clearQueryCache(query);
    }

    /**
     * ADVANCED:
     * Clear the query cache associated with the named query on the session
     */
    public void clearQueryCache(String sessionQueryName) {
        getIdentityMapManager().clearQueryCache((ReadQuery)session.getQuery(sessionQueryName));
    }

    /**
     * ADVANCED:
     * Clear the query cache associated with the named query on the descriptor for the given class
     */
    public void clearQueryCache(String descriptorQueryName, Class queryClass) {
        getIdentityMapManager().clearQueryCache((ReadQuery)session.getDescriptor(queryClass).getQueryManager().getQuery(descriptorQueryName));
    }

    /**
     * INTERNAL:
     * Return whether the identity maps contain an item of the given class and key
     */
    public boolean containsKey(Vector key, Class theClass, ClassDescriptor descriptor) {
        return getIdentityMapManager().containsKey(key, theClass, descriptor);
    }

    /**
     * ADVANCED:
     * Return if their is an object for the primary key.
     */
    public boolean containsObjectInIdentityMap(Object object) {
        return containsObjectInIdentityMap(getSession().keyFromObject(object), object.getClass());
    }

    /**
     * ADVANCED:
     * Return if their is an object for the primary key.
     */
    public boolean containsObjectInIdentityMap(Vector primaryKey, Class theClass) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        return containsObjectInIdentityMap(primaryKey, theClass, descriptor);
    }
    
    /**
     * INTERNAL:
     * Return if their is an object for the primary key.
     */
    public boolean containsObjectInIdentityMap(Vector primaryKey, Class theClass, ClassDescriptor descriptor) {
        return getIdentityMapManager().containsKey(primaryKey, theClass, descriptor);
    }

    /**
     * ADVANCED:
     * Return if their is an object for the row containing primary key and the class.
     */
    public boolean containsObjectInIdentityMap(Record rowContainingPrimaryKey, Class theClass) {
        return containsObjectInIdentityMap(extractPrimaryKeyFromRow(rowContainingPrimaryKey, theClass), theClass);
    }

    /**
     * INTERNAL:
     * Extract primary key from a row
     * @param DatabaseRow to extract primary key from
     * @param Class
     * @return Vector primary key
     */
    protected Vector extractPrimaryKeyFromRow(Record rowContainingPrimaryKey, Class theClass) {
        return this.session.getDescriptor(theClass).getObjectBuilder().extractPrimaryKeyFromRow((AbstractRecord)rowContainingPrimaryKey, this.session);
    }

    /**
    * INTERNAL:
    * Retrieve the cache key for the given object from the identity maps
    * @param Object object the object to get the cache key for
    * @return CacheKey
    */
    public CacheKey getCacheKeyForObject(Object object, ClassDescriptor descriptor) {
        return getCacheKeyForObject(getSession().keyFromObject(object, descriptor), object.getClass(), descriptor);
    }

    /**
     * INTERNAL:
     * This method is used to get a list of those classes with IdentityMaps in the Session.
     */
    public Vector getClassesRegistered() {
        return getIdentityMapManager().getClassesRegistered();
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow) throws QueryException {
        return getAllFromIdentityMap(selectionCriteria, theClass, translationRow, new InMemoryQueryIndirectionPolicy());
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     * Only return objects that are invalid in the cache if specified.
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean shouldReturnInvalidatedObjects) throws QueryException {
        return getIdentityMapManager().getAllFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, shouldReturnInvalidatedObjects);
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the expression is too complex an exception will be thrown.
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) throws QueryException {
        return getAllFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, true);
    }

    /**
     * ADVANCED:
     * Return the object from the identity with primary and class of the given object.
     */
    public Object getFromIdentityMap(Object object) {
        return getFromIdentityMap(getSession().keyFromObject(object), object.getClass());
    }

    /**
     * INTERNAL:
     * Retrieve the cache key for the given identity information
     * @param Vector the primary key of the cache key to be retrieved
     * @param Class the class of the cache key to be retrieved
     * @return CacheKey
     */
    public CacheKey getCacheKeyForObject(Vector primaryKey, Class myClass, ClassDescriptor descriptor) {
        return getIdentityMapManager().getCacheKeyForObject(primaryKey, myClass, descriptor);
    }

    /**
     * ADVANCED:
     * Return the object from the identity with the primary and class.
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass) {
        return getFromIdentityMap(primaryKey, theClass, true);
    }
    
    /**
     * INTERNAL:
     * Return the object from the identity with the primary and class.
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass, ClassDescriptor descriptor) {
        return getFromIdentityMap(primaryKey, theClass, true, descriptor);
    }

    /**
     * ADVANCED:
     * Return the object from the identity with the primary and class.
     * Only return invalidated objects if requested.
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects) {
        return getFromIdentityMap(primaryKey, theClass, shouldReturnInvalidatedObjects, getSession().getDescriptor(theClass));
    }
    
    /**
     * INTERNAL:
     * Return the object from the identity with the primary and class.
     * Only return invalidated objects if requested.
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        return getIdentityMapManager().getFromIdentityMap(primaryKey, theClass, shouldReturnInvalidatedObjects, descriptor);
    }

    /**
     * ADVANCED:
     * Return the object from the identity with the primary and class.
     */
    public Object getFromIdentityMap(Record rowContainingPrimaryKey, Class theClass) {
        return getFromIdentityMap(extractPrimaryKeyFromRow((AbstractRecord)rowContainingPrimaryKey, theClass), theClass);
    }

    /**
     * ADVANCED:
     * Return the object from the identity with the primary and class.
     * Only return invalidated objects if requested.
     */
    public Object getFromIdentityMap(Record rowContainingPrimaryKey, Class theClass, boolean shouldReturnInvalidatedObjects) {
        return getFromIdentityMap(extractPrimaryKeyFromRow((AbstractRecord)rowContainingPrimaryKey, theClass), theClass, shouldReturnInvalidatedObjects);
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow) throws QueryException {
        return getFromIdentityMap(selectionCriteria, theClass, translationRow, new InMemoryQueryIndirectionPolicy());
    }

    /**
     * ADVANCED:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) throws QueryException {
        return getFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, false);
    }

    /**
     * INTERNAL:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean conforming) {
        return getFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, conforming, true, getSession().getDescriptor(theClass));
    }

    /**
     * INTERNAL:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean conforming, boolean shouldReturnInvalidatedObjects) {
        return getFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, conforming, shouldReturnInvalidatedObjects, getSession().getDescriptor(theClass));
    }
    
    /**
     * INTERNAL:
     * Query the cache in-memory.
     * If the object is not found null is returned.
     * If the expression is too complex an exception will be thrown.
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean conforming, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        return getIdentityMapManager().getFromIdentityMap(selectionCriteria, theClass, translationRow, valueHolderPolicy, conforming, shouldReturnInvalidatedObjects, descriptor);
    }

    /**
     * INTERNAL:
     * Return the object from the identity with the primary and class.
     */
    public Object getFromIdentityMapWithDeferredLock(Vector primaryKey, Class theClass, ClassDescriptor descriptor) {
        return getFromIdentityMapWithDeferredLock(primaryKey, theClass, true, descriptor);
    }

    /**
     * INTERNAL:
     * Return the object from the identity with the primary and class.
     * Only return invalidated objects if requested
     */
    public Object getFromIdentityMapWithDeferredLock(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects, ClassDescriptor descriptor) {
        return getIdentityMapManager().getFromIdentityMapWithDeferredLock(primaryKey, theClass, shouldReturnInvalidatedObjects, descriptor);
    }

    /**
     * INTERNAL:
     * Get the IdentityMapManager for this IdentityMapAccessor
     * This method should be used for all IdentityMapManager access since it may
     * be overridden in sub classes.
     */
    public IdentityMapManager getIdentityMapManager() {
        if (session.hasBroker()) {
            return getSession().getBroker().getIdentityMapAccessorInstance().getIdentityMapManager();
        }
        return identityMapManager;
    }

    /**
     * INTERNAL:
     * Get the identity map for the given class from the IdentityMapManager
     */
    public IdentityMap getIdentityMap(Class theClass) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        if (descriptor == null) {
            throw ValidationException.missingDescriptor(theClass.toString());
        }
        return this.getIdentityMap(descriptor);
    }

    /**
     * INTERNAL:
     * Get the identity map for the given class from the IdentityMapManager
     */
    public IdentityMap getIdentityMap(ClassDescriptor descriptor) {
        return getIdentityMapManager().getIdentityMap(descriptor);
    }

    /**
     * ADVANCED:
     * Return the remaining life of this object.  This method is associated with use of
     * TopLink's cache invalidation feature and returns the difference between the next expiry
     * time of the object and its read time.  The method will return 0 for invalidated objects.
     */
    public long getRemainingValidTime(Object object) {
        Vector primaryKey = getSession().keyFromObject(object);
        ClassDescriptor descriptor = getSession().getDescriptor(object);
        CacheKey key = getCacheKeyForObject(primaryKey, object.getClass(), descriptor);
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
        return getIdentityMapManager().getWrapper(primaryKey, theClass);
    }

    /**
     * INTERNAL:
     * Returns the single write Lock manager for this session
     */
    public WriteLockManager getWriteLockManager() {
        return getIdentityMapManager().getWriteLockManager();
    }

    /**
    * ADVANCED:
    * Extract the write lock value from the identity map.
    */
    public Object getWriteLockValue(Object object) {
        return getWriteLockValue(getSession().keyFromObject(object), object.getClass());
    }

    /**
     * ADVANCED:
     * Extract the write lock value from the identity map.
     */
    public Object getWriteLockValue(Vector primaryKey, Class theClass) {
        return getWriteLockValue(primaryKey, theClass, getSession().getDescriptor(theClass));
    }
        
    /**
     * ADVANCED:
     * Extract the write lock value from the identity map.
     */
    public Object getWriteLockValue(Vector primaryKey, Class theClass, ClassDescriptor descriptor) {
        return getIdentityMapManager().getWriteLockValue(primaryKey, theClass, descriptor);
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
        getSession().log(SessionLog.FINER, SessionLog.CACHE, "initialize_all_identitymaps");
        getIdentityMapManager().initializeIdentityMaps();
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
        getIdentityMapManager().initializeIdentityMap(theClass);
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
     * ADVANCED:
     * Set an object to be invalid in the TopLink identity maps.
     * If the object does not exist in the cache, thiss method will return
     * without any action
     */
    public void invalidateObject(Object object) {
        invalidateObject(getSession().keyFromObject(object), object.getClass());
    }

    /**
     * ADVANCED:
     * Set an object to be invalid in the TopLink identity maps.
     * If the object does not exist in the cache, this method will return
     * without any action
     */
    public void invalidateObject(Vector primaryKey, Class theClass) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        //forward the call to getCacheKeyForObject locally in case subclasses overload
        CacheKey key = this.getCacheKeyForObject(primaryKey, theClass, descriptor);
        if (key != null) {
            key.setInvalidationState(CacheKey.CACHE_KEY_INVALID);
        }
    }

    /**
     * ADVANCED:
     * Set an object to be invalid in the TopLink identity maps.
     * If the object does not exist in the cache, this method will return
     * without any action
     */
    public void invalidateObject(Record rowContainingPrimaryKey, Class theClass) {
        invalidateObject(extractPrimaryKeyFromRow(rowContainingPrimaryKey, theClass), theClass);
    }

    /**
     * ADVANCED:
     * Set all of the objects from the given Expression to be invalid in the TopLink Identity Maps
     */
    public void invalidateObjects(Expression selectionCriteria) {
        invalidateObjects(getAllFromIdentityMap(selectionCriteria, selectionCriteria.getBuilder().getQueryClass(), new DatabaseRecord(1)));
    }

    /**
     * ADVANCED:
     * Set all of the objects in the given collection to be invalid in the TopLink Identity Maps
     * This method will take no action for any objects in the collection that do not exist in the cache.
     */
    public void invalidateObjects(Vector collection) {
        Enumeration enumtr = collection.elements();
        while (enumtr.hasMoreElements()) {
            invalidateObject(enumtr.nextElement());
        }
    }

    /**
     * ADVANCED:
     * Set all of the objects of a specific class to be invalid in TopLink's identity maps
     * Will set the recurse on inheritance to true.
     */
    public void invalidateClass(Class myClass) {
        invalidateClass(myClass, true);
    }

    /**
     * ADVANCED:
     * Set all of the objects of a specific class to be invalid in TopLink's identity maps.
     * User can set the recurse flag to false if they do not want to invalidate
     * all the classes within an inheritance tree.
     */
    public void invalidateClass(Class myClass, boolean recurse) {
        //forward the call to getIdentityMap locally in case subclasses overload
        IdentityMap identityMap = this.getIdentityMap(myClass);
        synchronized (identityMap) {
            Enumeration keys = identityMap.keys();

            while (keys.hasMoreElements()) {
                CacheKey key = (CacheKey)keys.nextElement();
                Object obj = key.getObject();

                if (recurse || ((obj != null) && obj.getClass().equals(myClass))) {
                    key.setInvalidationState(CacheKey.CACHE_KEY_INVALID);
                }
            }
        }
    }

    /**
     * ADVANCED:
     * Set all of the objects from all identity maps to be invalid in TopLink's 
     * identity maps.
     */
    public void invalidateAll() {
        Enumeration identiyMapClasses = getIdentityMapManager().getIdentityMapClasses();
        
        while (identiyMapClasses.hasMoreElements()) {
            invalidateClass((Class) identiyMapClasses.nextElement());
        }
    }
    
    /**
     * ADVANCED:
     * Return true if this object is valid in TopLink's identity maps
     * return false otherwise
     */
    public boolean isValid(Object object) {
        return isValid(getSession().keyFromObject(object), object.getClass());
    }

    /**
     * ADVANCED:
     * Return true if this object is valid in TopLink's identity maps
     * return false otherwise
     */
    public boolean isValid(Vector primaryKey, Class theClass) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        //forward the call to getCacheKeyForObject locally in case subclasses overload
        CacheKey key = this.getCacheKeyForObject(primaryKey, theClass, descriptor);
        if (key == null) {
            throw QueryException.classPkDoesNotExistInCache(theClass, primaryKey);
        }
        return !descriptor.getCacheInvalidationPolicy().isInvalidated(key, System.currentTimeMillis());
    }

    /**
     * ADVANCED:
     * Return true if this object is valid in TopLink's identity maps
     * return false otherwise
     */
    public boolean isValid(AbstractRecord rowContainingPrimaryKey, Class theClass) {
        return isValid(extractPrimaryKeyFromRow(rowContainingPrimaryKey, theClass), theClass);
    }

    /**
     * PUBLIC:
     * Used to print all the objects in the identity map of the passed in class.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     */
    public void printIdentityMap(Class businessClass) {
        if (getSession().shouldLog(SessionLog.SEVERE, SessionLog.CACHE)) {
            getIdentityMapManager().printIdentityMap(businessClass);
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
        }
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     */
    public Object putInIdentityMap(Object object) {
        return putInIdentityMap(object, getSession().keyFromObject(object));
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     */
    public Object putInIdentityMap(Object object, Vector key) {
        return putInIdentityMap(object, key, null);
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     */
    public Object putInIdentityMap(Object object, Vector key, Object writeLockValue) {
        return putInIdentityMap(object, key, writeLockValue, 0);
    }

    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * The readTime may also be included in the cache key as it is constructed
     */
    public Object putInIdentityMap(Object object, Vector key, Object writeLockValue, long readTime) {
        ClassDescriptor descriptor = getSession().getDescriptor(object);
        return putInIdentityMap(object, key, writeLockValue, readTime, descriptor);
    }
    
    /**
     * ADVANCED:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * The readTime may also be included in the cache key as it is constructed
     */
    public Object putInIdentityMap(Object object, Vector key, Object writeLockValue, long readTime, ClassDescriptor descriptor) {
        CacheKey cacheKey = internalPutInIdentityMap(object, key, writeLockValue, readTime, descriptor);
        if (cacheKey == null) {
            return null;
        }
        return cacheKey.getObject();
    }

    /**
     * INTERNAL:
     * Register the object with the identity map.
     * The object must always be registered with its version number if optimistic locking is used.
     * The readTime may also be included in the cache key as it is constructed.
     * Return the cache-key.
     */
    public CacheKey internalPutInIdentityMap(Object object, Vector key, Object writeLockValue, long readTime, ClassDescriptor descriptor) {
        return getIdentityMapManager().putInIdentityMap(object, key, writeLockValue, readTime, descriptor);
    }

    /**
     * INTERNAL:
     * Lock the entire cache if the cache isolation requires.
     * By default concurrent reads and writes are allowed.
     * By write, unit of work merge is meant.
     */
    public void releaseWriteLock() {
        getIdentityMapManager().releaseWriteLock();
    }

    /**
     * ADVANCED:
     * Remove the object from the object cache.
     * Caution should be used when calling to avoid violating object identity.
     * The application should only call this is it knows that no references to the object exist.
     */
    public Object removeFromIdentityMap(Object object) {
        return removeFromIdentityMap(getSession().keyFromObject(object), object.getClass());
    }

    /**
     * ADVANCED:
     * Remove the object from the object cache.
     */
    public Object removeFromIdentityMap(Vector key, Class theClass) {
        ClassDescriptor descriptor = getSession().getDescriptor(theClass);
        return removeFromIdentityMap(key, theClass, descriptor);
    }
    
    /**
     * INTERNAL:
     * Remove the object from the object cache.
     */
    public Object removeFromIdentityMap(Vector key, Class theClass, ClassDescriptor descriptor) {
        return getIdentityMapManager().removeFromIdentityMap(key, theClass, descriptor);
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
        getIdentityMapManager().setWrapper(primaryKey, theClass, wrapper);
    }

    /**
     * ADVANCED:
     * Update the write lock value in the identity map.
     */
    public void updateWriteLockValue(Object object, Object writeLockValue) {
        updateWriteLockValue(getSession().keyFromObject(object), object.getClass(), writeLockValue);
    }

    /**
     * ADVANCED:
     * Update the write lock value in the identity map.
     */
    public void updateWriteLockValue(Vector primaryKey, Class theClass, Object writeLockValue) {
        getIdentityMapManager().setWriteLockValue(primaryKey, theClass, writeLockValue);
    }

    /**
     * INTERNAL:
     * This can be used to help debugging an object identity problem.
     * An object identity problem is when an object in the cache references an object not in the cache.
     * This method will validate that all cached objects are in a correct state.
     */
    public void validateCache() {
        //pass certain calls to this in order to allow subclasses to implement own behaviour
        getSession().log(SessionLog.FINER, SessionLog.CACHE, "validate_cache");
        // This define an inner class for process the itteration operation, don't be scared, its just an inner class.
        DescriptorIterator iterator = new DescriptorIterator() {
            public void iterate(Object object) {
                if (!containsObjectInIdentityMap(IdentityMapAccessor.this.session.getDescriptor(object.getClass()).getObjectBuilder().extractPrimaryKeyFromObject(object, IdentityMapAccessor.this.getSession()), object.getClass())) {
                    IdentityMapAccessor.this.session.log(SessionLog.FINEST, SessionLog.CACHE, "stack_of_visited_objects_that_refer_to_the_corrupt_object", getVisitedStack());
                    IdentityMapAccessor.this.session.log(SessionLog.FINER, SessionLog.CACHE, "corrupt_object_referenced_through_mapping", getCurrentMapping());
                    IdentityMapAccessor.this.session.log(SessionLog.FINER, SessionLog.CACHE, "corrupt_object", object);
                }
            }
        };

        iterator.setSession(getSession());
        Iterator descriptors = getSession().getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptors.next();
            for (Enumeration mapEnum = getIdentityMap(descriptor).elements();
                     mapEnum.hasMoreElements();) {
                iterator.startIterationOn(mapEnum.nextElement());
            }
        }
    }
}
