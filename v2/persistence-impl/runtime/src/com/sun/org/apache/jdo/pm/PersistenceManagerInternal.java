/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.pm;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;


import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.persistence.support.PersistenceManager;
import com.sun.persistence.support.spi.PersistenceCapable;


/**
 * Extends the PersistenceManager interface for JDO-internal use.  Provides
 * additional information and helper methods for StateManagerInternal
 * interaction with the cache.
 *
 * @author Marina Vatkina
 */
public interface PersistenceManagerInternal extends PersistenceManager {
    /**
     * assert this PM instance is open
     */
    public void assertIsOpen();

    /**
     * assert that the NontransactionalRead flag is true or a transaction is active.
     */
    public void assertReadAllowed();
    
    /**
    * Provides a StoreManager that is ready to accept operations on it.
    * @return A StoreManager.
    */
    public StoreManager getStoreManager();


    /**
    * Sets a StoreManager that is ready to accept operations on it such
    * as insert, etc.
    * @param storeManager the StoreManager to be used by this PersistenceManager
    */
    public void setStoreManager(StoreManager storeManager);

    /**
    * Provides a StateManagerInternal for the given Object Id.
    * @param oid the given Object Id.
    * @param pcClass Class of a PersistenceCapable instance to be created
    * if this Object Id was not registered with this PersistenceManager.
    * @return A StateManagerInternal.
    */
    public StateManagerInternal getStateManager(Object oid, Class pcClass);

    /**
    * Finds a StateManagerInternal for the given PersistenceCapable object.
    * Validates PersistenceManager associated with this PersistenceCapable object.
    * @param pc the given PersistenceCapable object.
    * @return A StateManagerInternal.
    */
    public StateManagerInternal findStateManager(PersistenceCapable pc);


    /**
     * Provides a Class of the given name.  This method will use one of as
     * many as three ClassLoaders in attempting to load the named class. The
     * ClassLoaders are:
     * <ul>
     *   <li>The given Class Loader</li>
     *   <li>The current Thread's context Class Loader</li>
     *   <li>The class loader that was the context Class Loader of the thread
     *     which created this PersistenceManagerInternal</li> 
     * </ul>
     * For each such non-null Class Loader, it is used as a parameter of
     * Class.forName. If the result is not null, then the given Comparator's
     * compare method is invoked with that Class and the given Object o. If
     * that returns zero, that Class is returned. If either the Class.forName
     * returns null or the comparator returns non-zero, the next non-null
     * ClassLoader in the above list is tried in the same manner.
     * <p>
     * If after the above has been tried on all the ClassLoaders, an
     * appropriate Class has not been found, throws JDOUserException.
     * @param name Fully qualified name of the Class to load.
     * @param given ClassLoader which is the first to be tried
     * in loading the named Class. 
     * @throws ClassNotFoundException - if an appropriate Class can not
     * be loaded.
     */

     /* XXX At one point, we discussed also using a Comparator to validate
     * the class loaded by the loader.  Pending resolution, we are omitting
     * this, instead following the proposal Craig made to the JDO Experts.
     *
     * @param c Comparator used to determine if a
     * Class loaded from a ClassLoader  is in fact
     * that which the caller wants. Invoked with a loaded Class
     * as the first argument, and the given object o as the second
     * argument. If it returns zero, the comparison is deemed to have succeed,
     * and that Class will be returned by this method.
     * @param o Object passed as second argument to given
     * Comparator's compare method.
     */

     public Class loadClass(String name,
                            ClassLoader given) throws ClassNotFoundException;
     //                     Comparator c,
     //                     Object o) throws ClassNotFoundException;


    /**
     * Provides the Class object of the persistence-capable class that defines 
     * the specified class as its ObjectId class. This method will use one of as
     * many as three ClassLoaders in attempting to find the persistence-capable 
     * class. The ClassLoaders are the same as in {@link #loadClass}:
     * <ul>
     * <li>The given Class Loader, here the given class loader is the class 
     * loader of the ObjectId class</li>
     * <li>The current Thread's context Class Loader</li>
     * <li>The class loader that was the context Class Loader of the thread
     *     which created this PersistenceManagerInternal</li> 
     * </ul>
     * The method returns the top most persistence-capable class in the case of 
     * an inheritance hierachy.
     */
    public Class loadPCClassForObjectIdClass(Class objectIdClass) 
        throws ClassNotFoundException;
    
    /**
     * Returns an instance of the given class with the given identity.
     * @see com.sun.persistence.support.PersistenceManager#getObjectById(Object, boolean)
     * @param candidateClass Class of instance to be returned.
     * @param oid object id of instance to be returned.
     * @param validate see description of PersistenceManager.getObjectById
     * @return an object of the given class with the given identity.
     */
    public Object getObjectById(Class candidateClass, Object oid, boolean validate);

    /**
    * Provides an object id for the given PersistenceCapable.  The object id
    * must not be given to user/client code.
    */     
    public Object getInternalObjectId(Object pc);

    /**
    * Adds persistent object to the cache.
    * @param sm instance of StateManagerInternal to be added
    * @param oid ObjectId of the corresponding persistence-capable instance
    * @param transactional true if the corresponding lifecycle state is transactional
    * @param throwDuplicateException true if the exception should be thrown in case the same ObjectId
    * has been already registered.
    */
    public void register(StateManagerInternal sm, Object oid, boolean transactional, 
        boolean throwDuplicateException);

    /**
    * Adds transient object to the transient cache.
    * @param sm instance of StateManagerInternal to be added
    */
    public void registerTransient(StateManagerInternal sm);

    /**
    * Removes the object from the cache.
    * @param oid ObjectId of the instance to be removed.
    */
    public void deregister(Object oid);

    /**
    * Removes transient object from the transient cache.
    * @param sm instance of StateManagerInternal to be removed
    */
    public void deregisterTransient(StateManagerInternal sm);

    /**
    * Replaces the objectId key value in the cache.
    * @param oldId previous value of ObjectId.
    * @param newId new value of ObjectId.
    */
    public void replaceObjectId(Object oldId, Object newId);

   /** A helper method called from the StateManager inside getPersistenceManager()
     * to identify StateManager associated with this PC instance
     * @param pc PC instance
     * @param sm StateManager to save
     */  
    public void hereIsStateManager(StateManagerInternal sm, PersistenceCapable pc);

   /**
    * Called by StateManagerInternal#markAsFlushed() to adjust transactional cache(s)
    * if necessary after successful flush to the data store.
    * @param sm StateManagerInternal instance that has been flushed
    */
    public void markAsFlushed(StateManagerInternal sm);

   /**
    * Returns true if the call initiated as a result of the commit process,
    * versus flush for query in a datastore transaction.
    * @return true if commit has started
    */
    public boolean insideCommit();

    /**
     * Called internally by the runtime to create a new tracked instance.
     * Will not result in marking field as dirty
     *
     * Returns a new Second Class Object instance of the type java.uti.Date,
     * or supported subclass.
     * @param type Class of the new SCO instance
     * @return the object of the class type
     */
    public Object newSCOInstanceInternal (Class type);

    /**
     * Called internally by the runtime to create a new tracked instance of type
     * Collection.
     * Will not result in marking field as dirty
     *   
     */  
    public Collection newCollectionInstanceInternal(Class type,
        Class elementType, boolean allowNulls, Integer initialSize,
        Float loadFactor, Collection initialContents, Comparator comparator);

    /**
     * Called internally by the runtime to create a new tracked instance of type Map.
     * Will not result in marking field as dirty
     *   
     */  
    public Map newMapInstanceInternal(Class type, Class keyType, Class valueType, 
        boolean allowNulls, Integer initialSize, Float loadFactor, 
        Map initialContents, Comparator comparator);

    /**
     * Called by StateManager to verify field type.
     * @param type Class type of the field.
     * @return true if this type is a supported SCO type.
     */
    public boolean isSupportedSCOType(Class type);

    /**
     * Called by Query or Extent to flush updates to the database
     * in a datastore transaction. It is up to the StoreManager to decide
     * at what point of the processing to call this method. No validation
     * of the transaction type is performed.
     * @throws JDOUserException if current transaction is not active.
     */
    public void flush();

    /**
     * Returns current instance of PersistenceManager wrapper
     */  
    public PersistenceManager getCurrentWrapper();

    /**
     * Returns a Collection of instances that has been made persistent
     * or become persistent through persistence-by-reachability
     * algorithm in this transaction. Called by the Extent.iterator.
     * @return Collection of Persistent-New instances.
     */
    public Collection getInsertedInstances();
}
