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
package oracle.toplink.essentials.sessions;

import java.util.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;

/**
 * PUBLIC:
 * IdentityMapAccessor provides the public interface into all functionality associated with
 * TopLink identity maps. An appropriate IdentityMapAccessor can be obtained from a session 
 * with its getIdentityMapAccessor() method.
 * Methods that used to be called on the Session to access identity maps can now be called
 * through the IdentityMapAccessor.
 * <p>
 * For instance, to initialize identity maps the code used to be: <code>
 * session.initializeIdentityIdentityMaps()                       <br></code>
 * With this class, the code now is:                              <code>
 * session.getIdentityMapAccessor().initializeIdentityMaps()      </code>
 * @see oracle.toplink.sessions.Session
 */
public interface IdentityMapAccessor {

    /**
	 * ADVANCED:
	 * Returns true if the identity map contains an Object with the same primary 
	 * key and Class type of the given domainObject.
     * @param domainObject Object
     * @return boolean
     */
    public boolean containsObjectInIdentityMap(Object domainObject);

    /**
     * ADVANCED:
     * Returns true if the identity map contains an Object with the same
     * primary key and Class type as those specified.
     * @param primaryKey Vector
     * @param theClass Class
     * @return boolean
     */
    public boolean containsObjectInIdentityMap(Vector primaryKey, Class theClass);

    /**
     * ADVANCED:
     * Returns true if the identity map contains an Object with the same primary key 
     * of the specified row (ie. the database record) and Class type.
     * @param rowContainingPrimaryKey Record
     * @param theClass Class type to be found 
     * @return boolean - true if Object in indentity map
     */
    public boolean containsObjectInIdentityMap(Record rowContainingPrimaryKey, Class theClass);

    /**
     * ADVANCED:
     * Queries the cache in-memory with the passed in criteria and returns matching Objects.
     * If the expression is too complex an exception will be thrown.
     * Only returns Objects that are invalid from the map if specified with the 
     * boolean shouldReturnInvalidatedObjects.
     * @param selectionCriteria Expression selecting the Objects to be returned
     * @param theClass Class to be considered
     * @param translationRow Record
     * @param valueHolderPolicy see 
     * {@link oracle.toplink.queryframework.InMemoryQueryIndirectionPolicy InMemoryQueryIndirectionPolicy}
     * @param shouldReturnInvalidatedObjects boolean - true if only invalid Objects should be returned
     * @return Vector of Objects
     * @throws QueryException
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, 
    		Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, 
    		boolean shouldReturnInvalidatedObjects) 
    throws QueryException;

    /**
     * ADVANCED:
     * Queries the cache in-memory with the passed in criteria and returns matching Objects.
     * If the expression is too complex an exception will be thrown.
     * @param selectionCriteria Expression selecting the Objects to be returned
     * @param theClass Class to be considered
     * @param translationRow Record
     * @param valueHolderPolicy  see 
     * {@link oracle.toplink.queryframework.InMemoryQueryIndirectionPolicy InMemoryQueryIndirectionPolicy}
     * @return Vector of Objects with type theClass and matching the selectionCriteria
     * @throws QueryException
     */
    public Vector getAllFromIdentityMap(Expression selectionCriteria, Class theClass, 
    		Record translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy) 
    throws QueryException;

    /**
     * ADVANCED:
     * Returns the Object from the identity map with the same primary key 
     * and Class type of the given domainObject. 
     * @param domainObject Object
     * @return Object from identity map, may be null.
     */
    public Object getFromIdentityMap(Object domainObject);

    /**
     * ADVANCED:
     * Returns the Object from the identity map with the same primary key 
     * and Class type as those specified.
     * @param primaryKey Vector
     * @param theClass Class
     * @return Object from identity map, may be null.
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass);

    /**
     * ADVANCED:
     * Returns the Object from the identity map with the same primary key 
     * of the specified row (ie. the database record) and Class type.
     * @param rowContainingPrimaryKey Record
     * @param theClass Class
     * @return Object from identity map, may be null.
     */
    public Object getFromIdentityMap(Record rowContainingPrimaryKey, Class theClass);

    /**
     * ADVANCED:
     * Returns the Object from the identity map with the same primary key and Class type
     * as specified. May return null and will only return an Object that is invalidated 
     * if specified with the boolean shouldReturnInvalidatedObjects.
     * @param primaryKey Vector
     * @param theClass Class
     * @param shouldReturnInvalidatedObjects InMemoryQueryIndirectionPolicy
     * @return Object from identity map, may be null.
     */
    public Object getFromIdentityMap(Vector primaryKey, Class theClass, boolean shouldReturnInvalidatedObjects);

    /**
     * ADVANCED:
     * Returns the Object from the identity map with the same primary key of the specified 
     * row and Class type. May return null and will only Only return an Object that is 
     * invalidated if specified with the boolean shouldReturnInvalidatedObjects.
     * @param rowContainingPrimaryKey Record
     * @param theClass Class
     * @param shouldReturnInvalidatedObjects boolean
     * @return Object from identity map, may be null.
     */
    public Object getFromIdentityMap(Record rowContainingPrimaryKey, Class theClass, 
    		boolean shouldReturnInvalidatedObjects);

    /**
     * ADVANCED:
     * Queries the cache in-memory and returns an Object from this identity map.
     * If the Object is not found with the passed in Class type, Row and selectionCriteria, 
     * null is returned. If the expression is too complex an exception will be thrown.
     * @param selectionCriteria Expression
     * @param theClass Class
     * @param translationRow Record
     * @return Object from identity map, may be null
     * @throws QueryException
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow) 
    throws QueryException;

    /**
     * ADVANCED:
     * Queries the cache in-memory and returns an Object from this identity map.
     * If the Object is not found with the passed in Class type, Row and selectionCriteria, 
     * null is returned. This method allows for control of un-instantiated indirection access 
     * with valueHolderPolicy. If the expression is too complex an exception will be thrown.
     * @param selectionCriteria Expression
     * @param theClass Class
     * @param translationRow Record
     * @param valueHolderPolicy 
     * see {@link oracle.toplink.queryframework.InMemoryQueryIndirectionPolicy InMemoryQueryIndirectionPolicy}
     * @return Object from identity map, may be null
     * @throws QueryException
     */
    public Object getFromIdentityMap(Expression selectionCriteria, Class theClass, Record translationRow, 
    		InMemoryQueryIndirectionPolicy valueHolderPolicy) throws QueryException;

    /**
     * ADVANCED:
     * Returns the remaining life of the given Object.  This method is associated with use of
     * TopLink's cache invalidation feature and returns the difference between the next expiry
     * time of the Object and its read time.  The method will return 0 for invalidated Objects.
     * @param object Object under consideration
     * @return long time in milliseconds
     */
    public long getRemainingValidTime(Object object);

    /**
     * ADVANCED:
     * Extracts and returns the write lock value from the identity map through the given Object.
     * Write lock values are used when optimistic locking is stored in the cache instead of the object.
     * @param domainObject Object
     * @return Object for versioning
     */
    public Object getWriteLockValue(Object domainObject);

    /**
     * ADVANCED:
     * Extracts the write lock value from the identity map through the passed in primaryKey and Class type.
     * Write lock values are used when optimistic locking is stored in the cache instead of the object.
     * @param primaryKey Vector
     * @param theClass Class
     * @return Object for versioning
     */
    public Object getWriteLockValue(Vector primaryKey, Class theClass);

    /**
     * PUBLIC:
     * Resets the entire Object cache.
     * <p> NOTE: Be careful using this method. This method blows away both this session's and its parent's caches.
     * This includes the server cache or any other cache. This throws away any Objects that have been read in.
     * Extreme caution should be used before doing this because Object identity will no longer
     * be maintained for any Objects currently read in.  This should only be called
     * if the application knows that it no longer has references to Objects held in the cache.
     */
    public void initializeAllIdentityMaps();

    /**
     * PUBLIC:
     * Resets the identity map for only the instances of the given Class type.
     * For inheritance the user must make sure that they only use the root class.
     * <p> NOTE: Caution must be used in doing this to ensure that the Objects within the identity map
     * are not referenced from other Objects of other classes or from the application.
     * @param theClass Class
     */
    public void initializeIdentityMap(Class theClass);

    /**
     * PUBLIC:
     * Resets the entire local Object cache.
     * <p> NOTE: This throws away any Objects that have been read in.
     * Extreme caution should be used before doing this because Object identity will no longer
     * be maintained for any Objects currently read in.  This should only be called
     * if the application knows that it no longer has references to Objects held in the cache.
     */
    public void initializeIdentityMaps();

    /**
     * ADVANCED:
     * Sets an Object to be invalid in the TopLink identity maps.
     * If this Object does not exist in the cache, this method will return
     * without any action.
     * @param object Object
     */
    public void invalidateObject(Object object);

    /**
     * ADVANCED:
     * Sets an Object with the specified primary key and Class type to be invalid in 
     * the TopLink identity maps. If the Object does not exist in the cache, 
     * this method will return without any action.
     * @param primaryKey Vector
     * @param theClass Class
     */
    public void invalidateObject(Vector primaryKey, Class theClass);

    /**
     * ADVANCED:
     * Sets an Object with the specified primary key of the passed in Row and Class type to 
     * be invalid in the TopLink identity maps. If the Object does not exist in the cache, 
     * this method will return without any action.
     * @param rowContainingPrimaryKey Record
     * @param theClass Class
     */
    public void invalidateObject(Record rowContainingPrimaryKey, Class theClass);

    /**
     * ADVANCED:
     * Sets all of the Objects in the given collection to be invalid in the TopLink identity maps.
     * This method will take no action for any Objects in the collection that do not exist in the cache.
     * @param collection Vector of Objects to be invalidated
     */
    public void invalidateObjects(Vector collection);

    /**
     * ADVANCED:
     * Sets all of the Objects matching the given Expression to be invalid in the TopLink identity maps.
     * <p>
     * <b>Example</b> - Invalidating Employee Objects with non-null first names: 
     * <p> 
     * <code>
     *  ExpressionBuilder eb = new ExpressionBuilder(Employee.class);    <br>
     *  Expression exp = eb.get("firstName").notNull();                  <br>
     *  session.getIdentityMapAccessor().invalidateObjects(exp);         <br> 
     * </code>
     * @param selectionCriteria Expression
     */
    public void invalidateObjects(Expression selectionCriteria);

    /**
     * ADVANCED:
     * Sets all of the Objects for all classes to be invalid in TopLink's identity maps. 
     * It will recurse on inheritance.
     */
    public void invalidateAll();
    
    /**
     * ADVANCED:
     * Sets all of the Objects of the specified Class type to be invalid in TopLink's identity maps
     * Will set the recurse on inheritance to true.
     * @param myClass Class
     */
    public void invalidateClass(Class myClass);

    /**
     * ADVANCED:
     * Sets all of the Objects of the specified Class type to be invalid in TopLink's identity maps.
     * User can set the recurse flag to false if they do not want to invalidate
     * all the same Class types within an inheritance tree.
     * @param myClass Class
     * @param recurse boolean
     */
    public void invalidateClass(Class myClass, boolean recurse);

    /**
     * ADVANCED:
     * Returns true if an Object with the same primary key and Class type of the
     * the given Object is valid in TopLink's identity maps.
     * @param object Object
     * @return boolean
     */
    public boolean isValid(Object object);

    /**
     * ADVANCED:
     * Returns true if the Object described by the given primary key and Class type is valid 
     * in TopLink's identity maps.
     * @param primaryKey Vector
     * @param theClass Class
     * @return boolean
     */
    public boolean isValid(Vector primaryKey, Class theClass);

    /**
     * ADVANCED:
     * Returns true if this Object with the given primary key of the Row and Class type 
     * given is valid in TopLink's identity maps.
     * @param rowContainingPrimaryKey AbstractRecord
     * @param theClass Class
     * @return boolean
     */
    public boolean isValid(AbstractRecord rowContainingPrimaryKey, Class theClass);

    /**
     * PUBLIC:
     * Used to print all the Objects in the identity map of the given Class type.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     * @param businessClass Class
     */
    public void printIdentityMap(Class businessClass);

    /**
     * PUBLIC:
     * Used to print all the Objects in every identity map in this session.
     * The output of this method will be logged to this session's SessionLog at SEVERE level.
     */
    public void printIdentityMaps();

    /**
     * PUBLIC:
     * Used to print all the locks in every identity map in this session.
     * The output of this method will be logged to this session's SessionLog at FINEST level.
     */
    public void printIdentityMapLocks();

    /**
     * ADVANCED:
     * Registers the given Object with the identity map.
     * The Object must always be registered with its version number if optimistic locking is used.
     * @param domainObject Object
     * @return Object
     */
    public Object putInIdentityMap(Object domainObject);

    /**
     * ADVANCED:
     * Registers the Object and given key with the identity map.
     * The Object must always be registered with its version number if optimistic locking is used.
     * @param domainObject Object
     * @param key Vector
     * @return Object
     */
    public Object putInIdentityMap(Object domainObject, Vector key);

    /**
     * ADVANCED:
     * Registers the Object and given key with the identity map.
     * The Object must always be registered with its version number if optimistic locking is used.
     * @param domainObject Object
     * @param key Vector
     * @param writeLockValue Object for versioning
     * @return Object
     */
    public Object putInIdentityMap(Object domainObject, Vector key, Object writeLockValue);

    /**
     * ADVANCED:
     * Registers the given Object with the identity map.
     * The Object must always be registered with its version number if optimistic locking is used.
     * The readTime may also be included in the cache key as it is constructed.
     * @param domainObject Object
     * @param key Vector
     * @param writeLockValue Object for versioning
     * @param readTime long, time in milliseconds
     * @return Object the Object put into the identity map
     */
    public Object putInIdentityMap(Object domainObject, Vector key, Object writeLockValue, long readTime);

    /**
     * ADVANCED:
     * Removes the Object from the Object cache.
     * <p> NOTE: Caution should be used when calling to avoid violating Object identity.
     * The application should only call this if its known that no references to the Object exist.
     * @param domainObject Object
     * @return Object the Object removed from the identity map
     */
    public Object removeFromIdentityMap(Object domainObject);

    /**
     * ADVANCED:
     * Removes the Object with given primary key and Class from the Object cache.
     * <p> NOTE: Caution should be used when calling to avoid violating Object identity.
     * The application should only call this if its known that no references to the Object exist.
     * @param key Vector
     * @param theClass Class
     * @return Object the Object removed from the identity map
     */
    public Object removeFromIdentityMap(Vector key, Class theClass);

    /**
     * ADVANCED:
     * Updates the write lock value in the identity map for cache key of the primary key
     * the given Object.
     * @param domainObject Object
     * @param writeLockValue Object for versioning
     */
    public void updateWriteLockValue(Object domainObject, Object writeLockValue);

    /**
     * ADVANCED:
     * Updates the write lock value in the cache for the Object with same primary key as the given Object.
     * The write lock values is used when optimistic locking is stored in the cache instead of in the object.
     * @param primaryKey Vector
     * @param theClass Class
     * @param writeLockValue Object for versioning
     */
    public void updateWriteLockValue(Vector primaryKey, Class theClass, Object writeLockValue);

    /**
     * ADVANCED:
     * This can be used to help debugging an Object identity problem.
     * An Object identity problem is when an Object in the cache references an 
     * Object that is not in the cache. This method will validate that all cached 
     * Objects are in a correct state.
     */
    public void validateCache();
}
