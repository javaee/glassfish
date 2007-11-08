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

/*
 * PersistenceManagerImpl.java
 *
 * Created on December 1, 2000
 */

package com.sun.org.apache.jdo.impl.pm;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.lang.reflect.Constructor;
import javax.transaction.Status;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.pm.PersistenceManagerFactoryInternal;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.sco.SCOCollection;
import com.sun.org.apache.jdo.sco.SCOMap;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.util.JDORIVersion;
import com.sun.persistence.support.*;
import com.sun.persistence.support.spi.*;

/** 
 * This is the StoreManager independent implemetation of the 
 * com.sun.org.apache.jdo.pm.PersistenceManagerInternal interface. Delegates most of 
 * the method execution to the corresponding instance of the CacheManagerImpl.
 * <p>
 * Please note, this class is changed wrt. the JDO apache version:
 * The query factory methods throw UnsupportedOperationException.
 * 
 * @author Marina Vatkina
 */ 
public class PersistenceManagerImpl implements PersistenceManagerInternal {
    private final boolean DEBUG = true;
    
    /**
     * True if this PersistenceManager is closed
     */
    private boolean _isClosed = true;

    /**
     * Associated Transaction
     */
    private TransactionImpl _transaction = null;

    /**
     *  Reference to the associated JTA Transaction if any
     */
    private Object _jta = null;

    /**
     * Current PersistenceManagerWrapper as PersistenceManagerInternal
     */
    private PersistenceManagerWrapper current = null;

    /**
     * PersistenceManagerFactory that created (and could be pooling)
     * this PersistenceManager
     */
    private PersistenceManagerFactoryImpl pmf = null;

    /**
     * Reference to the CacheManager
     */
    private CacheManagerImpl _txCache = null;

    /**
     * Reference to the StoreManager
     */
    private StoreManager _storeManager = null;

    /**
     * Flag for Query. 
     * Constructor defaults it to the PMF setting.
     */
    private boolean _ignoreCache;

    /**
     * Flag for active transaction
     */
    private boolean _activeTransaction = false;

    /**
     * Flag for optimistic transaction
     */
    private boolean optimistic = true;
    
    /**
     * Flag for multithreaded support.
     */
    private boolean multithreaded = true;

    /**
     * User Object
     */
    private Object _userObject = null;

    /**
     * Flag for flushing process
     */
    private boolean _flushing = false;

    /**
     * This is the ClassLoader that was the
     *     Thread.currentThread().getContextClassLoader()
     * at the time this PersistenceManagerImpl was created.
     */
    private final ClassLoader myClassLoader;

    /**
     * Constructor signatures 
     */
    private static final Class[] sigSCO_Short = new Class []{
        java.lang.Object.class,
        java.lang.String.class};

        /** The signature for SCO  Collections.
         */        
    private static final Class[] sigSCO_Collection = new Class []{
        java.lang.Class.class,
        boolean.class,
        int.class};

        /** The signature for SCO  Maps.
         */        
    private static final Class[] sigSCO_Map = new Class []{
        java.lang.Class.class,
        java.lang.Class.class,
        boolean.class,
        int.class};

        /** The signature for SCO HashSet.
         */        
    private static final Class[] sigSCO_HashSet = new Class []{
        java.lang.Class.class,
        boolean.class,
        int.class,
        float.class};

        /** The signature for SCO TreeSet.
         */        
    private static final Class[] sigSCO_TreeSet = new Class []{
        java.lang.Class.class,
        boolean.class,
        java.util.Comparator.class};

        /** The signature for SCO HashMap.
         */        
    private static final Class[] sigSCO_HashMap = new Class []{
        java.lang.Class.class,
        java.lang.Class.class,
        boolean.class,
        int.class,
        float.class};

        /** The signature for SCO TreeMap.
         */        
    private static final Class[] sigSCO_TreeMap = new Class []{
        java.lang.Class.class,
        java.lang.Class.class,
        boolean.class,
        java.util.Comparator.class};

        /** Default initialCapacity values.
         */        
    private static final Integer int11 = new Integer(11);
    private static final Integer int10 = new Integer(10);

    /** RuntimeJavaModelFactory. */
    private static final RuntimeJavaModelFactory javaModelFactory =
        (RuntimeJavaModelFactory) AccessController.doPrivileged(
            new PrivilegedAction () {
                public Object run () {
                    return RuntimeJavaModelFactory.getInstance();
                }
            }
        );
 
    /**
     * Logger support
     */
    private static final Log logger = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.impl.pm"); // NOI18N

    /**
     * I18N message handler
     */
    private final static I18NHelper msg = 
        I18NHelper.getInstance(PersistenceManagerImpl.class);

    /**
     * Helper objects to identify StateManager associated with the given 
     * PersistenceCapable instance
     */
    private StateManagerInternal lookingFor = null;
    private PersistenceCapable pc = null;

    /**
     * Constructs new instance of PersistenceManagerImpl for this
     * PersistenceManagerFactoryInternal and particular combination of 
     * username and password.
     *
     * @param pmf calling PersistenceManagerFactory as PersistenceManagerFactoryInternal
     * @param username user name used for accessing Connector or null if none 
     *      is provided.
     * @param password user password used for accessing Connector or null if 
     *      none is provided.
     */
    public PersistenceManagerImpl(PersistenceManagerFactoryInternal pmf,
        String username, String password) {

        if (debugging())
            debug("constructor"); // NOI18N

        if (javaModelFactory == null)
            throw new JDOUserException(
                msg.msg("EXC_CannotGetRuntimeJavaModelFactory")); // NOI18N

        this.pmf = (PersistenceManagerFactoryImpl)pmf;
        _transaction = new TransactionImpl(this, this.pmf, username, password);
        _txCache = new CacheManagerImpl(this);

        optimistic = pmf.getOptimistic();
        _ignoreCache = pmf.getIgnoreCache();

        _isClosed = false;

        myClassLoader = getContextClassLoaderPrivileged();
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#setIgnoreCache(boolean flag)
     * @param flag the ignoreCache value
     */
    public void setIgnoreCache(boolean flag) {
        assertIsOpen();
        _ignoreCache = flag;
    }

    /** Get the value of the ignoreCache flag.
     * @see com.sun.persistence.support.PersistenceManager#getIgnoreCache()
     * @return the IgnoreCache flag
     */
    public boolean getIgnoreCache() {
        assertIsOpen();
        return _ignoreCache;
    }

    /** Set the Multithreaded flag for this PersistenceManager.  Applications
     * that use multiple threads to invoke methods or access fields from 
     * instances managed by this PersistenceManager must set this flag to true.
     * Instances managed by this PersistenceManager include persistent or
     * transactional instances of PersistenceCapable classes, as well as 
     * helper instances such as Query, Transaction, or Extent.
     *
     * @param flag the Multithreaded setting.
     */
    public void setMultithreaded(boolean flag) {
        assertIsOpen();
        multithreaded = flag;
    }
    
    /** Get the current Multithreaded flag for this PersistenceManager.  
     * @see #setMultithreaded
     * @return the Multithreaded setting.
     */
    public boolean getMultithreaded() {
        assertIsOpen();
        return multithreaded;
    }

    /** Return whether this PersistenceManager is closed.
     * @see com.sun.persistence.support.PersistenceManager#isClosed()
     */
    public boolean isClosed() {
        return _isClosed;
    }

    /**
     * Close this persistence manager
     * @see com.sun.persistence.support.PersistenceManager#close()
     */
    public synchronized void close() {
        if (debugging())
            debug("close"); // NOI18N

        assertIsOpen();

        if (_activeTransaction) {
            throw new JDOUserException(msg.msg(
                "EXC_ActiveTransaction"));// NOI18N
        }

        if (current == null){
            forceClose();
        }

    }

    /**
     * Returns transaction associated with this persistence manager
     * @return transaction    current transaction
     */
    public Transaction currentTransaction() {
        assertIsOpen();
        return (Transaction) _transaction;
    }

    //
    // -------- ObjectId related methods --------
    //

    /** This method locates a persistent instance in the cache of instances
    * managed by this PersistenceManager.
    *   
    * <P>If the validate flag is true: This method verifies that there
    * is an instance in the data store with the same oid, constructs an
    * instance, and returns it.  If there is no transaction active, then
    * a hollow instance or persistent non-transactional instance is returned.
    * If there is a transaction active, then
    * a persistent clean instance is returned.
    * <P>If the validate flag is false: If there is not already an instance
    * in the cache with the same oid, then an instance is constructed and
    * returned.  If the instance does not exist
    * in the data store, then this method will
    * not fail.  However, a request to access fields of the instance will
    * throw an exception.
    * @return the PersistenceCapable instance with the specified
    * ObjectId
    * @param oid an ObjectId
    * @param validate if the existence of the instance is to be validated
    */
    public Object getObjectById (Object oid, boolean validate) {
        return getObjectById(null, oid, validate);
    }

    /**
     * @see #getObjectById(Object, boolean)
     * @see com.sun.org.apache.jdo.pm.PersistenceManagerInternal#getObjectById(java.lang.Class, java.lang.Object, boolean)
     */
    public Object getObjectById(Class candidateClass, Object oid, boolean validate) {
        if (debugging())
            debug("getObjectById " + oid); // NOI18N

        assertIsOpen();
        if (oid == null)
            return null;

        return _txCache.getObjectById(candidateClass, oid, validate);
    }

    /** The ObjectId returned by this method represents the JDO identity of
    * the instance.  The ObjectId is a copy (clone) of the internal state
    * of the instance, and changing it does not affect the JDO identity of
    * the instance.
    * Delegates actual execution to the internal method.
    * @param pc the PersistenceCapable instance
    * @return the corresponding ObjectId of the instance. Returns null
    * if pc is null, not persistence-capable, or not persistent.
    */
    public Object getObjectId (Object pc) {
        if (debugging())
            debug("getObjectId"); // NOI18N

        return getExternalObjectId(pc, false);
    }


    /**
    * @see com.sun.org.apache.jdo.pm.PersistenceManagerInternal#loadClass
    */
    public Class loadClass(String name, ClassLoader given)
        throws ClassNotFoundException {

        Class rc = null;

        if (null != given) {
            try {
                rc = Class.forName(name, true, given);
            } catch (ClassNotFoundException ex) {
            }
        }

        if (null == rc) {
            try {
                rc = Class.forName(
                    name, true,
                    getContextClassLoaderPrivileged());
            } catch (ClassNotFoundException ex) {
            }
        }

        if (null == rc) {
            rc = Class.forName(name, true, myClassLoader);
        }

        return rc;            
    }

    /**
     * @see com.sun.org.apache.jdo.pm.PersistenceManagerInternal#loadPCClassForObjectIdClass
     */
    public Class loadPCClassForObjectIdClass(Class objectIdClass) 
        throws ClassNotFoundException
    {
        if (debugging())
            debug("loadPCClassForObjectIdClass: " + objectIdClass.getName()); //NOI18N

        JavaModel javaModel = null;
        JDOClass jdoClass = null;
        JavaType objectIdJavaType = javaModelFactory.getJavaType(objectIdClass);
        // try Model of objectId's class loader
        ClassLoader classLoader = 
            javaModelFactory.getClassLoaderPrivileged(objectIdClass);
        javaModel = javaModelFactory.getJavaModel(classLoader);
        jdoClass = javaModel.getJDOModel().
            getJDOClassForObjectIdClass(objectIdJavaType);
        
        if (null == jdoClass) {
            // try Model of thread context class loader
            classLoader = getContextClassLoaderPrivileged();
            javaModel = javaModelFactory.getJavaModel(classLoader);
            jdoClass = javaModel.getJDOModel().
                getJDOClassForObjectIdClass(objectIdJavaType);
        }

        if (null == jdoClass) {
            // try Model of the pm's thead context class loader
            javaModel = javaModelFactory.getJavaModel(myClassLoader);
            jdoClass = javaModel.getJDOModel().
                getJDOClassForObjectIdClass(objectIdJavaType);
        }

        if (jdoClass == null) 
            // persistence-capable class not found => ClassNotFoundException
            throw new ClassNotFoundException(msg.msg(
                "EXC_CannotFindPCClassForObjectIdClass", // NOI18N
                objectIdClass.getName())); //NOI18N
        return javaModelFactory.getJavaClass(jdoClass.getJavaType());
    }

    /**
     * Gets the internal object id for this instance. Returns null
     * if it is not a PersistenceCapable instance.
     * @param pc
     * @return the internal object id
     */
    public Object getInternalObjectId(Object pc) {
        if (debugging())
            debug("getInternalObjectId for " + pc.getClass().getName()); // NOI18N

        assertIsOpen();

        Object rc = null;
        if ( pc instanceof PersistenceCapable ) {
            PersistenceCapable p = (PersistenceCapable)pc;
            if( !(p.jdoIsPersistent()) ) {
                throw new JDOFatalInternalException(msg.msg(
                    "EXC_TransientInstance", pc.getClass().getName()));// NOI18N
            }

            StateManagerInternal sm = findStateManager(p);
            if (sm == null) {
                throw new JDOFatalInternalException(
                    msg.msg("EXC_CannotFindSM", pc.getClass().getName())); // NOI18N
            }
            rc = sm.getInternalObjectId();
        }
        return rc;
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#getTransactionalObjectId
     */
    public Object getTransactionalObjectId (Object pc) {
        if (debugging())
            debug("getTransactionalObjectId"); // NOI18N

        return getExternalObjectId(pc, true);
    }

    /** 
     * This method returns an object id instance corresponding to the Class 
     * and String arguments. The String argument might have been the 
     * result of executing toString on an object id instance. 
     * @param pcClass the Class of the persistence-capable instance
     * @param str the String form of the object id
     * @return an instance of the object identity class
     */
    public Object newObjectIdInstance (Class pcClass, String str) {
        if (debugging())
            debug("newObjectIdInstance for: " + pcClass + ", and " + str); // NOI18N

        assertIsOpen();
        return this.getStoreManager().newObjectIdInstance (pcClass, str);
    }
    
    /** Return the Class that implements the JDO Identity for the
    * specified PersistenceCapable Class.  The application can use the
    * returned Class to construct a JDO Identity instance for
    * application identity PersistenceCapable classes.  This JDO Identity
    * instance can then be used to get an instance of the
    * PersistenceCapable class for use in the application.
    *   
    * <P>In order for the application to construct an instance of the ObjectId class
    * it needs to know the class being used by the JDO implementation.
    * @param cls the PersistenceCapable Class
    * @return the Class of the ObjectId of the parameter
    * @see #getObjectById
    */  
    public Class getObjectIdClass(Class cls) {
        if (debugging())
            debug("getObjectIdClass for: " + cls); // NOI18N

        assertIsOpen();
        return pmf.getObjectIdClass(cls);
    }
    
    //
    // -------- Query and Extent methods --------
    //

    /** Create a new Query with no elements.
    * @return a new Query instance with no elements.
    */  
    public Query newQuery() {
        throw new UnsupportedOperationException();
    }

    /** Create a new Query using elements from another Query.  The other Query
    * must have been created by the same JDO implementation.  It might be active
    * in a different PersistenceManager or might have been serialized and
    * restored.
    * @return the new Query
    * @param compiled another Query from the same JDO implementation
    */  
    public Query newQuery (Object compiled) {
        throw new UnsupportedOperationException();
    }

    /** Create a new Query using the specified language.
     * @param language the language of the query parameter
     * @param query the query, which is of a form determined by the language
     * @return the new Query
     */    
    public Query newQuery (String language, Object query) {
        throw new UnsupportedOperationException();
    }
    
    /** Create a new Query specifying the Class of the results.
    * @param cls the Class of the results
    * @return the new Query
    */
    public Query newQuery (Class cls) {
        throw new UnsupportedOperationException();
    }

    /** Create a new Query with the candidate Extent; the class is taken
     * from the Extent.
     * specified.
     * @return the new Query
     * @param cln the Extent of candidate instances */  
    public Query newQuery(Extent cln) {
          throw new UnsupportedOperationException();
    }

    /** Create a new Query with the Class of the results and candidate Collection.
    * specified.
    * @param cls the Class of results
    * @param cln the Collection of candidate instances
    * @return the new Query
    */
    public Query newQuery (Class cls, Collection cln) {
        throw new UnsupportedOperationException();
    }

    /** Create a new Query with the Class of the results and Filter.
    * specified.
    * @param cls the Class of results
    * @param filter the Filter for candidate instances
    * @return the new Query
    */
    public Query newQuery (Class cls, String filter) {
        throw new UnsupportedOperationException();
    }

    /** Create a new Query with the Class of the results, candidate Collection,
    * and Filter.
    * @param cls the Class of results
    * @param cln the Collection of candidate instances
    * @param filter the Filter for candidate instances
    * @return the new Query
    */
    public Query newQuery (Class cls, Collection cln, String filter) {
        throw new UnsupportedOperationException();
    }

    /** Create a new Query with the candidate Extent and Filter.
     * The class is taken from the Extent.
     * @return the new Query
     * @param cln the Extent of candidate instances
     * @param filter the Filter for candidate instances */  
    public Query newQuery(Extent cln, String filter) {
          throw new UnsupportedOperationException();
    }

    /** The PersistenceManager may manage a collection of instances in the data
     * store based on the class of the instances.  This method returns an
     * Extent of instances in the data store that might be iterated or
     * given to a Query as the Extent of candidate instances.
     * @param persistenceCapableClass Class of instances
     * @param subclasses whether to include instances of subclasses
     * @return an Extent of instances.
     * @see StoreManager#getExtent
     * @see com.sun.persistence.support.Query
     */
    public Extent getExtent (Class persistenceCapableClass, boolean subclasses) {
        if (debugging())
            debug("getExtent for: " + persistenceCapableClass + // NOI18N
            ", subclasses: " + subclasses); // NOI18N

        assertIsOpen();
        Extent rc = null;
        if (PersistenceCapable.class.isAssignableFrom(persistenceCapableClass)) {
            rc = this.getStoreManager().getExtent(persistenceCapableClass, subclasses, this);
        } else {
            throw new JDOUserException(msg.msg(
                "EXC_ClassNotPersistenceCapable", persistenceCapableClass.getName())); // NOI18N
        }
        if (null == rc) {
            // Protect against bogus store managers that return null.
            rc = new EmptyExtent(persistenceCapableClass, subclasses);
        }
        return rc;
    }

    class EmptyExtent implements Extent {
        private final Class cls;
        private final boolean subclasses;
        EmptyExtent(Class cls, boolean subclasses) {
            this.cls = cls;
            this.subclasses = subclasses;
        }
        public Iterator iterator() {
            return new Iterator() {
                public boolean hasNext() { return false; }
                public Object next() { return null; }
                public void remove() { }
            };}
        public boolean hasSubclasses() { return false; }
        public Class getCandidateClass() { return cls; }
        public boolean subclasses() { return subclasses; }
        public PersistenceManager getPersistenceManager() {
            return PersistenceManagerImpl.this.current;
        }
        public void closeAll() { }
        public void close(Iterator it) { }
    }

    //
    // -------- State transition methods --------
    //

    /** Make the transient instance persistent in this PersistenceManager.
    * This method must be called in an active transaction.
    * The PersistenceManager assigns an ObjectId to the instance and
    * transitions it to persistent-new.
    * The instance will be managed in the Extent associated with its Class.
    * The instance will be put into the data store at commit.
    * @param pc a transient instance of a Class that implements
    * PersistenceCapable
    */
    public void makePersistent (Object pc) {
        if (debugging())
            debug("makePersistent"); // NOI18N

        assertIsOpen();
        assertActiveTransaction(false);

        makePersistentInternal(pc);
    }

    /** Make an array of instances persistent.
    * @param pcs an array of transient instances
    * @see #makePersistent(Object pc)
    */
    public void makePersistentAll(Object[] pcs) {
        if (debugging())
            debug("makePersistentAll"); // NOI18N

        assertIsOpen();
        assertIsOpen();
        assertActiveTransaction(false);
        makePersistentAllInternal(pcs);

    }

    /** Make an collection of instances persistent.
    * @param pcs an collection of transient instances
    * @see #makePersistent(Object pc)
    */
    public void makePersistentAll(Collection pcs) {
        if (debugging())
            debug("makePersistentAll"); // NOI18N

        assertIsOpen();
        assertActiveTransaction(false);
        makePersistentAllInternal(pcs.toArray());
    }

    /** Delete the persistent instance from the data store.
     * This method must be called in an active transaction.
     * The data store object will be removed at commit.
     * Unlike makePersistent, which makes the closure of the instance persistent,
     * the closure of the instance is not deleted from the data store.
     * This method has no effect if the instance is already deleted in the
     * current transaction.
     * This method throws an exception if the instance is transient or is managed by another
     * PersistenceManager.
     *   
     * @param pc a persistent instance
     */ 
    public void deletePersistent (Object pc) {
        if (debugging())
            debug("deletePersistent"); // NOI18N

        assertIsOpen();
        assertActiveTransaction(false);
        deletePersistentInternal(pc);
    }

    /** Delete an array of instances from the data store.
     * @param pcs a Collection of persistent instances
     * @see #deletePersistent(Object pc)
     */
    public void deletePersistentAll(Object[] pcs) {
        if (debugging())
            debug("deletePersistentAll"); // NOI18N

        assertIsOpen();
        assertActiveTransaction(false);
        deletePersistentAllInternal(pcs);

    }

    /** Delete a Collection of instances from the data store.
     * @param pcs a Collection of persistent instances
     * @see #deletePersistent(Object pc)
     */  
    public void deletePersistentAll(Collection pcs) {
        if (debugging())
            debug("deletePersistentAll"); // NOI18N

        assertIsOpen();
        assertActiveTransaction(false);
        deletePersistentAllInternal(pcs.toArray());
    }

    /** Make an instance transient, removing it from management by this
    * PersistenceManager.
    *   
    * <P>The instance loses its JDO identity and it is no longer associated
    * with any PersistenceManager.  The state of fields is preserved unchanged.
    * @param pc the instance to make transient.
    */  
    public void makeTransient(Object pc) {
        if (debugging())
            debug("makeTransient"); // NOI18N

        assertIsOpen();
        makeTransientInternal(pc);
    }
    
    /** Make an array of instances transient, removing them from management by this
    * PersistenceManager.
    *   
    * <P>The instances lose their JDO identity and they are no longer associated
    * with any PersistenceManager.  The state of fields is preserved unchanged.
    * @param pcs the instances to make transient.
    */  
    public void makeTransientAll(Object[] pcs) {
        if (debugging())
            debug("makeTransientAll"); // NOI18N

        assertIsOpen();
        makeTransientAllInternal(pcs);
    }
    
    /** Make a Collection of instances transient, removing them from management by this
    * PersistenceManager.
    *   
    * <P>The instances lose their JDO identity and they are no longer associated
    * with any PersistenceManager.  The state of fields is preserved unchanged.
    * @param pcs the instances to make transient.
    */  
    public void makeTransientAll(Collection pcs) {
        if (debugging())
            debug("makeTransientAll"); // NOI18N

        assertIsOpen();
        makeTransientAll(pcs.toArray());
    }

    /** Make an instance subject to transactional boundaries.
    *   
    * <P>Transient instances normally do not observe transaction boundaries.
    * This method makes transient instances sensitive to transaction completion.
    * If an instance is modified in a transaction, and the transaction rolls back,
    * the state of the instance is restored to the state before the first change
    * in the transaction.
    *   
    * <P>For persistent instances read in optimistic transactions, this method
    * allows the application to make the state of the instance part of the
    * transactional state.  At transaction commit, the state of the instance in
    * cache is compared to the state of the instance in the data store.  If they
    * are not the same, then an exception is thrown.
    * @param pc the instance to make transactional.
    */  
    public void makeTransactional(Object pc) {
        if (debugging())
            debug("makeTransactional"); // NOI18N

        assertIsOpen();
        makeTransactionalInternal(pc);
    }
      
    /** Make an array of instances subject to transactional boundaries.
    * @param pcs the array of instances to make transactional.
    * @see #makeTransactional(Object pc)
    */
    public void makeTransactionalAll(Object[] pcs) {
        if (debugging())
            debug("makeTransactionalAll"); // NOI18N

        assertIsOpen();
        makeTransactionalAllInternal(pcs);
    }
    
    /** Make a Collection of instances subject to transactional boundaries.
    * @param pcs the Collection of instances to make transactional.
    * @see #makeTransactional(Object pc)
    */
    public void makeTransactionalAll(Collection pcs) {
        if (debugging())
            debug("makeTransactionalAll"); // NOI18N

        assertIsOpen();
        makeTransactionalAllInternal(pcs.toArray());
    }

    /** Make an instance non-transactional after commit.
    *
    * <P>Normally, at transaction completion, instances are evicted from the
    * cache.  This method allows an application to identify an instance as
    * not being evicted from the cache at transaction completion.  Instead,
    * the instance remains in the cache with nontransactional state.
    *
    * @param pc the instance to make nontransactional.
    */
    public void makeNontransactional(Object pc) {
        if (debugging())
            debug("makeNontransactional"); // NOI18N

        assertIsOpen();
        makeNontransactionalInternal(pc);
    }
    
    /** Make an array of instances non-transactional after commit.
    *
    * @param pcs the array of instances to make nontransactional.
    * @see #makeNontransactional(Object pc)
    */
    public void makeNontransactionalAll(Object[] pcs){
        if (debugging())
            debug("makeNontransactionalAll"); // NOI18N

        assertIsOpen();
        makeNontransactionalAllInternal(pcs);
    }

    /** Make a Collection of instances non-transactional after commit.
    *
    * @param pcs the Collection of instances to make nontransactional.
    * @see #makeNontransactional(Object pc)
    */
    public void makeNontransactionalAll(Collection pcs) {
        if (debugging())
            debug("makeNontransactionalAll"); // NOI18N

        assertIsOpen();
        makeNontransactionalAllInternal(pcs.toArray());
    }

    /** Mark an instance as no longer needed in the cache.
    * Eviction is normally done automatically by the PersistenceManager
    * at transaction completion.  This method allows the application to
    * explicitly provide a hint to the PersistenceManager that the instance
    * is no longer needed in the cache.
    * @param pc the instance to evict from the cache.
    */  
    public  void evict(Object pc) {
        if (debugging())
            debug("evict"); // NOI18N

        assertIsOpen();
        evictInternal(pc);
    }

   /** Mark an array of instances as no longer needed in the cache.
    * @see #evict(Object pc)
    * @param pcs the array of instances to evict from the cache.
    */  
    public void evictAll(Object[] pcs) {
        if (debugging())
            debug("evictAll"); // NOI18N

        assertIsOpen();
        evictAllInternal(pcs);
    }

    /** Mark a Collection of instances as no longer needed in the cache.
    * @param pcs the Collection of instance to evict from the cache.
    */  
    public void evictAll(Collection pcs) {
        if (debugging())
            debug("evictAll"); // NOI18N

        assertIsOpen();
        evictAllInternal(pcs.toArray());
    }

    /** Mark all persistent-nontransactional instances as no longer needed
    * in the cache.  It transitions
    * all persistent-nontransactional instances to hollow.  Transactional
    * instances are subject to eviction based on the RetainValues setting.
    * @see #evict(Object pc)
    */  
    public void evictAll() {
        if (debugging())
            debug("evictAll()"); // NOI18N

        assertIsOpen();
        _txCache.evictAll();
    }


    /** Refresh the state of the instance from the data store.
    *   
    * <P>In an optimistic transaction, the state of instances in the cache
    * might not match the state in the data store.  This method is used to
    * reload the state of the instance from the data store so that a subsequent
    * commit is more likely to succeed.
    * <P>Outside a transaction, this method will refresh nontransactional state.
    * @param pc the instance to refresh.
    */  
    public void refresh(Object pc) { 
        if (debugging())
            debug("refresh"); // NOI18N

        assertIsOpen();
        refreshInternal(pc);
    }
     
    /** Refresh the state of an array of instances from the data store.
    *   
    * @see #refresh(Object pc)
    * @param pcs the array of instances to refresh.
    */  
    public void refreshAll(Object[] pcs) {
        if (debugging())
            debug("refreshAll"); // NOI18N

        assertIsOpen();
        refreshAllInternal(pcs);
    }
         
    /** Refresh the state of a Collection of instances from the data store.
    *
    * @see #refresh(Object pc)
    * @param pcs the Collection of instances to refresh.
    */
    public void refreshAll(Collection pcs) {
        if (debugging())
            debug("refreshAll"); // NOI18N

        assertIsOpen();
        refreshAllInternal(pcs.toArray());
    }
         
    /** Refresh the state of all applicable instances from the data store.
    * <P>If called with an active transaction, all transactional instances
    * will be refreshed.  If called outside an active transaction, all
    * nontransactional instances will be refreshed.
    * @see #refresh(Object pc)
    */
    public void refreshAll() {
        if (debugging())
            debug("refreshAll()"); // NOI18N

        assertIsOpen();
        if (_activeTransaction) {
            _txCache.refreshAllTransactional(); 
        } else {
            _txCache.refreshAllNontransactional(); 
        }
    }

    /** Retrieve field values of an instance from the store.  This tells
     * the <code>PersistenceManager</code> that the application intends to use the
     * instance, and its field values must be retrieved.
     * <P>The <code>PersistenceManager</code> might use policy information about the
     * class to retrieve associated instances.
     * @param pc the instance
     */
    public void retrieve(Object pc) {
        if (debugging())
            debug("retrieve"); // NOI18N

        assertIsOpen();
        retrieveInternal(pc, false);
    }
    
    /** Retrieve field values of instances from the store.  This tells
     * the <code>PersistenceManager</code> that the application intends to use the
     * instances, and all field values must be retrieved.
     * <P>The <code>PersistenceManager</code> might use policy information about the
     * class to retrieve associated instances.
     * @param pcs the instances
     */
    public void retrieveAll(Object[] pcs) {
        retrieveAll(pcs, false);
    }
    
    /** Retrieve field values of instances from the store.  This tells
     * the <code>PersistenceManager</code> that the application intends to use the
     * instances, and their field values should be retrieved.  The fields
     * in the default fetch group must be retrieved, and the implementation
     * might retrieve more fields than the default fetch group.
     * <P>The <code>PersistenceManager</code> might use policy information about the
     * class to retrieve associated instances.
     * @param pcs the instances
     * @param DFGOnly whether to retrieve only the default fetch group fields
     * @since 1.0.1
     */
    public void retrieveAll (Object[] pcs, boolean DFGOnly) {
        if (debugging())
            debug("retrieveAll, DFGOnly: " + DFGOnly); // NOI18N

        assertIsOpen();
        retrieveAllInternal(pcs, DFGOnly);
    }

           
    /** Retrieve field values of instances from the store.  This tells
     * the <code>PersistenceManager</code> that the application intends to use the
     * instances, and all field values must be retrieved.
     * <P>The <code>PersistenceManager</code> might use policy information about the
     * class to retrieve associated instances.
     * @param pcs the instances
     */
    public void retrieveAll(Collection pcs) {
        retrieveAll(pcs, false);
    }
    
    /** Retrieve field values of instances from the store.  This tells
     * the <code>PersistenceManager</code> that the application intends to use the
     * instances, and their field values should be retrieved.  The fields
     * in the default fetch group must be retrieved, and the implementation
     * might retrieve more fields than the default fetch group.
     * <P>The <code>PersistenceManager</code> might use policy information about the
     * class to retrieve associated instances.
     * @param pcs the instances
     * @param DFGOnly whether to retrieve only the default fetch group fields
     * @since 1.0.1
     */
    public void retrieveAll (Collection pcs, boolean DFGOnly) {
        if (debugging())
            debug("retrieveAll, DFGOnly: " + DFGOnly); // NOI18N

        assertIsOpen();
        retrieveAllInternal(pcs.toArray(), DFGOnly);
    }

    //
    // -------- Other public methods --------
    //

    /** This method returns the PersistenceManagerFactory used to create
    * this PersistenceManager.  It returns null if this instance was
    * created via a constructor.
    * @return the PersistenceManagerFactory that created
    * this PersistenceManager
    */  
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        assertIsOpen();
        return (PersistenceManagerFactory)pmf;
    }
   
    /** The application can manage the PersistenceManager instances
    * more easily by having an application object associated with each
    * PersistenceManager instance.
    * @param o the user instance to be remembered by the PersistenceManager
    * @see #getUserObject
    */  
    public void setUserObject(Object o) {
        assertIsOpen();
        this._userObject = o;
    }
     
    /** The application can manage the PersistenceManager instances
    * more easily by having an application object associated with each
    * PersistenceManager instance.
    * @return the user object associated with this PersistenceManager
    * @see #setUserObject
    */  
    public Object getUserObject() {
        assertIsOpen();
        return _userObject;
    }
     
    /** The JDO vendor might store certain non-operational properties and
    * make those properties available to applications (for troubleshooting).
    *   
    * <P>Standard properties include:
    * <li>VendorName</li>
    * <li>VersionNumber</li>
    * @return the Properties of this PersistenceManager
    */  
    public Properties getProperties() {
        assertIsOpen();
        return JDORIVersion.getVendorProperties();
    }

    //
    // Implement PersistenceManagerInternal methods
    //

    /**
     * assert this PM instance is open
     */
    public void assertIsOpen() {
        if (_isClosed) {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
        // this is synchronized on pmf.closeLock
        pmf.assertNotClosed();
    }

    /**
    * @see com.sun.org.apache.jdo.pm.PersistenceManagerInternal#getStoreManager()
    */
    public StoreManager getStoreManager() {
        return _storeManager;        
    }

    /**
    * @see com.sun.org.apache.jdo.pm.PersistenceManagerInternal#setStoreManager(StoreManager)
    */
    public void setStoreManager(StoreManager storeManager) {
        _storeManager = storeManager;
    }

    //
    // -------- SCO Instance creation methods --------
    //

    /**
     * Called by internally by the runtime to create a new tracked instance.
     * Will not result in marking field as dirty
     *
     * @see PersistenceManagerInternal#newSCOInstanceInternal (Class type)
     */  
    public Object newSCOInstanceInternal (Class type) {
        if (debugging())
            debug("newSCOInstanceInternal for: " + type); // NOI18N

        Class newType = pmf.getTrackedClass(type);
        if (newType == null) {
            // Not found - means not supported. Return null here for
            // the internal calls. 
            return null;
        }

        Object obj = null;

        try {
            Constructor constr = newType.getConstructor((Class[]) null);

            if (constr != null) {
                obj = constr.newInstance((Object []) null);
            }
        } catch (Exception e) {
            throw new JDOUserException(msg.msg(
                "EXC_CannotConstructSCO",// NOI18N
                newType.getName()), e);
        }

        return obj;
    }

    /**
     * Called by internally by the runtime to create a new tracked instance.
     * Will not result in marking field as dirty
     *
     * @see PersistenceManagerInternal#newCollectionInstanceInternal(Class type,
        Class elementType, boolean allowNulls, Integer initialSize,
        Float loadFactor, Collection initialContents, Comparator comparator)
     */
    public Collection newCollectionInstanceInternal(
        Class type,
        Class elementType, boolean allowNulls, Integer initialSize,
        Float loadFactor, Collection initialContents, Comparator comparator) {

        if (debugging())
            debug("newCollectionInstanceInternal for: " + type); // NOI18N

        Class newType = pmf.getTrackedClass(type);
        if (newType == null) {
            // Not found - means not supported. Return null here for
            // the internal calls. 
            return null;
        }

        Object obj = null;
        SCOCollection rc = null;


        try {
            if (com.sun.org.apache.jdo.impl.sco.TreeSet.class == newType) {
                // Comparator can be null.
                Constructor constr = newType.getConstructor(sigSCO_TreeSet);

                if (constr != null) {
                    obj = constr.newInstance(
                        new Object[] {
                            elementType,
                            new Boolean(allowNulls), comparator
                        });
                }
            } else if (null == loadFactor) {        
                // If loadFactor is null, then it might be a HashSet.  If loadFactor
                // is not null, it must be a HashSet, or we'll throw an exception below.

                Constructor constr = newType.getConstructor(sigSCO_Collection);

                if (constr != null) {
                    if (initialSize == null) {
                        initialSize = getInitialSize(newType);
                    }
                    obj = constr.newInstance(
                        new Object[] {
                            elementType,
                            new Boolean(allowNulls), initialSize
                        });
                }
            } else if (com.sun.org.apache.jdo.impl.sco.HashSet.class == newType) {
                Constructor constr = newType.getConstructor(sigSCO_HashSet);
                
                if (constr != null) {
                    if (initialSize == null) {
                        initialSize = getInitialSize(newType);
                    }
                    obj = constr.newInstance(
                        new Object[] {
                            elementType,
                            new Boolean(allowNulls), initialSize, loadFactor
                        });
                    // We don't have a good way of testing this, because
                    // HashSet is opaque w.r.t. loadFactor.  Uncommenting this
                    // is all I can suggest!
                    /*
                    System.out.println(
                        "newCollection: loadFactor " + loadFactor); // NOI18N
                    */
                }
            } else {
                throw new IllegalArgumentException(msg.msg(
                    "EXC_IllegalArguments",// NOI18N
                    type.getName()));
            }
            rc = (SCOCollection)obj;
            if (initialContents != null && initialContents.size() > 0) {
                // Do not use addAllInternal to verify element types of
                // initialContents.
                rc.addAll(initialContents);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new JDOUserException(msg.msg(
                "EXC_CannotConstructSCO",// NOI18N
                newType.getName()), e);
        }
        return rc;
    }

    /**
     * Called by internally by the runtime to create a new tracked instance.
     * Will not result in marking field as dirty.
     *
     * @see PersistenceManagerInternal#newMapInstanceInternal(Class type,
        Class keyType, Class valueType, boolean allowNulls, Integer initialSize,
        Float loadFactor, Map initialContents, Comparator comparator)
     */
    public Map newMapInstanceInternal(
        Class type,
        Class keyType, Class valueType, boolean allowNulls, Integer initialSize,
        Float loadFactor, Map initialContents, Comparator comparator) {

        if (debugging())
            debug("newMapInstanceInternal for: " + type); // NOI18N

        Class newType = pmf.getTrackedClass(type);
        if (newType == null) {
            // Not found - means not supported. Return null here for
            // the internal calls. 
            return null;
        }

        Object obj = null;
        SCOMap rc = null;


        try {
            if (com.sun.org.apache.jdo.impl.sco.TreeMap.class == newType) {
                // Comparator can be null.
                Constructor constr = newType.getConstructor(sigSCO_TreeMap);

                if (constr != null) {
                    obj = constr.newInstance(
                        new Object[] {
                            keyType,
                            valueType,
                            new Boolean(allowNulls), comparator
                        });
                }
            } else if (null == loadFactor) {        
                // It is not TreeMap, so it should be either HashMap or Hashtable
                // and they can be constructed with or without loadFactor.
                Constructor constr = newType.getConstructor(sigSCO_Map);

                if (constr != null) {
                    if (initialSize == null) {
                        initialSize = getInitialSize(newType);
                    }
                    obj = constr.newInstance(
                        new Object[] {
                            keyType,
                            valueType,
                            new Boolean(allowNulls), initialSize
                        });
                }
            } else if (com.sun.org.apache.jdo.impl.sco.HashMap.class == newType ||
                com.sun.org.apache.jdo.impl.sco.Hashtable.class == newType) {
                Constructor constr = newType.getConstructor(sigSCO_HashMap);
                
                if (constr != null) {
                    if (initialSize == null) {
                        initialSize = getInitialSize(newType);
                    }
                    obj = constr.newInstance(
                        new Object[] {
                            keyType,
                            valueType,
                            new Boolean(allowNulls), initialSize, loadFactor
                        });
                    // We don't have a good way of testing this, because
                    // HashMap is opaque w.r.t. loadFactor.  Uncommenting this
                    // is all I can suggest!
                    /*
                    System.out.println(
                        "newMap: loadFactor " + loadFactor); // NOI18N
                    */
                }
            } else {
                throw new IllegalArgumentException(msg.msg(
                    "EXC_IllegalArguments",// NOI18N
                    type.getName()));
            }
            rc = (SCOMap)obj;
            if (initialContents != null && initialContents.size() > 0) {
                // Do not use putAllInternal to verify key/value types of
                // initialContents.
                rc.putAll(initialContents);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new JDOUserException(msg.msg(
                "EXC_CannotConstructSCO",// NOI18N
                newType.getName()), e);
        }
        return rc;
    }

    //
    // ----------- StateManger interaction methods -----------------
    //

    /**
     *
     * @see PersistenceManagerInternal#isSupportedSCOType (Class type)
     */  
    public boolean isSupportedSCOType (Class type) {
        return (pmf.getTrackedClass(type) != null);
    }

    /**
     * @see PersistenceManagerInternal#register(StateManagerInternal sm, Object oid, 
     * boolean transactional, boolean throwDuplicateException)
     */
    public void register(StateManagerInternal sm, Object oid, boolean transactional, 
        boolean throwDuplicateException) {
        if (debugging())
            debug("register" + oid); // NOI18N

        _txCache.register(sm, oid, transactional, throwDuplicateException);
    }

    /**
     * @see PersistenceManagerInternal#registerTransient(StateManagerInternal sm)
     */
    public void registerTransient(StateManagerInternal sm) {
        if (debugging())
            debug("registerTransient"); // NOI18N

        _txCache.registerTransient(sm);
    }

    /**
     * @see PersistenceManagerInternal#deregister(Object oid)
     */
    public void deregister(Object oid) {
        if (debugging())
            debug("deregister" + oid); // NOI18N

        _txCache.deregister(oid);
    }

    /**
     * @see PersistenceManagerInternal#deregisterTransient(StateManagerInternal sm)
     */
    public void deregisterTransient(StateManagerInternal sm) {
        if (debugging())
            debug("deregisterTransient"); // NOI18N

        _txCache.deregisterTransient(sm);
    }

    /**
     * @see PersistenceManagerInternal#replaceObjectId(Object oldId, Object newId)
     */
    public void replaceObjectId(Object oldId, Object newId) {
        if (debugging())
            debug("replaceObjectId old: " + oldId + " new: " + newId); // NOI18N

        if (!newId.equals(oldId)) {
            _txCache.replaceObjectId(oldId, newId);
        }
    }

    /**
     * @see PersistenceManagerInternal#markAsFlushed(StateManagerInternal sm)
     */
    public void markAsFlushed(StateManagerInternal sm) {
        if (debugging())
            debug("markAsFlushed"); // NOI18N

        if (!_flushing) {
            _txCache.markAsFlushed(sm);
        }
    }

    /**
     * @see PersistenceManagerInternal#insideCommit()
     */
    public boolean insideCommit() {
        return _transaction.startedCommit();
    }

    /** A helper method called from the StateManager inside getPersistenceManager()
     * to identify StateManager associated with this PC instance
     * @param pc PC instance 
     * @param sm StateManager to save
     */
    public synchronized void hereIsStateManager(StateManagerInternal sm, 
        PersistenceCapable pc) {
        if (debugging())
            debug("hereIsStateManager"); // NOI18N

        if (this.pc == pc) {
            lookingFor = sm;
        }
    }

    /**
    * @see PersistenceManagerInternal#getStateManager
    */
    public StateManagerInternal getStateManager(Object oid, Class pcClass) {
        if (debugging())
            debug("getStateManager"); // NOI18N

        return _txCache.getStateManager(oid, pcClass);
    }

    /** A helper method to find the StateManager associated with this PC instance
     * @param pc PC instance
     * @return StateManager as StateManagerInternal
     */
    public synchronized StateManagerInternal findStateManager(PersistenceCapable pc) {
        if (debugging())
            debug("findStateManager"); // NOI18N

        lookingFor = null;
        this.pc = pc;
        // pc.jdoGetPersistenceManager() returns the wrapper. It should compare equals
        // to this PersistenceManager:
        PersistenceManager pm = pc.jdoGetPersistenceManager();
        if (debugging())
            debug("findStateManager " + pm); // NOI18N

        if (pm != null && !this.equals(pm)) {
            throw new JDOUserException(msg.msg(
                 "EXC_AnotherPersistenceManager"));// NOI18N
        }
        return lookingFor;
    }    

    /**
     * Returns current wrapper
     */
    public PersistenceManager getCurrentWrapper() {
        return current;
    }

    /**
     * Returns a Collection of instances that has been made persistent
     * or become persistent through persistence-by-reachability
     * algorithm in this transaction. Called by the Extent.iterator.
     * @see PersistenceManagerInternal#getInsertedInstances
     * @return Collection of Persistent-New instances.
     */
    public Collection getInsertedInstances() {
        if (debugging())
            debug("getInsertedInstances"); // NOI18N

        return _txCache.getInsertedInstances();
    }

    /**
     * Returns a hash code value for this PersistenceManager.
     * @return  a hash code value for this PersistenceManager.
     */  
    public int hashCode() {
        return super.hashCode();
    }
 
    /**  
     * Indicates whether some other object is "equal to" this one.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */  
    public boolean equals(Object obj) {
        if (obj instanceof PersistenceManagerWrapper) {
            return (((PersistenceManagerWrapper)obj).getPersistenceManager() == this); 
 
        } else if (obj instanceof PersistenceManagerImpl) {
            return (((PersistenceManagerImpl)obj) == this); 
        } 
        return false; 
    } 

    //
    // ----------- Protected methods -----------------
    //

    /**
     * Remember the current wrapper
     */
    protected void pushCurrentWrapper(PersistenceManagerWrapper pmw) {
        if (debugging())
            debug("pushCurrentWrapper"); // NOI18N

        current = pmw;
    }

    /**
     * Replace current wrapper with the previous
     */
    protected void popCurrentWrapper(PersistenceManagerWrapper prev) {
        if (debugging())
            debug("popCurrentWrapper"); // NOI18N

        current = prev;
        if (_jta == null && current == null) {
            this.close();
        }
    }

    /**
     * with the current thread in the managed environment
     */
    protected void setJTATransaction(Object t) {
        if (debugging())
            debug("setJTATransaction"); // NOI18N

        if (this._jta != null) {
            throw new JDOFatalInternalException(
                msg.msg("EXC_NotNullJTATransaction")); //NOI18N
        }
        this._jta = t;
    }

    /**
     * Disassociate this PersistenceManager with the current JTA
     * transaction.
     */
    protected void deregisterJTA() {
        if (debugging())
            debug("deregisterJTA"); // NOI18N

        pmf.deregisterPersistenceManager(this, _jta);
        _jta = null;
    }

    /** 
     * Close this persistence manager even if there are open wrappers
     * or an uncomplete transaction.
     * Called by transaction completion in case of an inconsistent state
     * or as a part of a normal close process.
     */
    protected void forceClose() {
        if (debugging())
            debug("forceClose"); // NOI18N

        _txCache.close();

        // Remove association with JTA Transaction if any and return
        // to the pool.
        pmf.releasePersistenceManager(this, _jta);

        // Reset all references:
        pmf = null;
        _jta = null;
        _isClosed = true;
        _transaction = null;
        _txCache = null;
        _userObject = null;
    }

    /**
     * Verify that cached instance of a PersistenceManager was initialy
     * requested with the same values for username and password
     */
    protected boolean verify(String username, String password) {
        if (debugging())
            debug("verify"); // NOI18N

        return _transaction.verify(username, password);
    }

    /**
     * Called by Transaction commit()
     * Loops through transactional cache and calls PersistentStore.updatePersistent()
     * on each instance
     */
    protected void flushInstances() {
        if (debugging())
            debug("flushInstances"); // NOI18N

        assertIsOpen();
        assertActiveTransaction(false);
        _flushing = true;

        try {
            _txCache.flushInstances();
        } finally {
            _flushing = false;
        }
    }

    /**
     * Called by Transaction commit() or rollback()
     * cleans up transactional cache
     * @param    status        javax.transaction.Status
     */
    protected void afterCompletion(int status) {
        if (debugging())
            debug("afterCompletion"); // NOI18N

        assertIsOpen();
        _flushing = true;
        boolean abort = ((status == Status.STATUS_ROLLEDBACK) ||
             (status == Status.STATUS_ROLLING_BACK) ||
             (status == Status.STATUS_MARKED_ROLLBACK));
        _txCache.afterCompletion(abort);
        _flushing = false;
    }

    /**
     * Called by Query or Extent to flush updates to the database
     * in a datastore transaction. Delegates to TransactionImpl to do
     * the processing that will call internaly flushInstances() to do actual
     * flush.
     * @see #flushInstances()
     */
    public synchronized void flush() {
        if (debugging())
            debug("flush"); // NOI18N

        _transaction.internalFlush();
    }

    /**
     * For Transaction to notify PersistenceManager that
     * status is changed
     */
    protected synchronized void notifyStatusChange(boolean isActive) {
        _activeTransaction = isActive;
    }

    /**
     * For Transaction to notify PersistenceManager that
     * optimistic flag is changed
     */
    protected synchronized void notifyOptimistic(boolean optimistic) {
        this.optimistic = optimistic;
    }

    /** --------------Private Methods--------------  */

    /** Returns external representation of the current or transactional
     * objectId depending on the parameter
     * @param pc the PersistenceCapable instance
     * @param transactional true if the transactional Id is requested
     * @return the corresponding ObjectId of the instance. Returns null
     * if pc is null, not persistence-capable, or not persistent.
     */
    private Object getExternalObjectId(Object pc, boolean transactional) {
        if (debugging())
            debug("getExternalObjectId"); // NOI18N

        assertIsOpen();
        if (pc != null && pc instanceof PersistenceCapable) {
            PersistenceCapable p = (PersistenceCapable)pc;
            if (p.jdoIsPersistent()) {
                return _txCache.getExternalObjectId(p, transactional);
            }
        }
        // All other cases if no exception thrown
        return null;
    }

    /**
     * assert that the associated Transaction is active 
     */
    private void assertActiveTransaction(boolean insideQuery) {
        if (_flushing || (insideQuery && _transaction.getNontransactionalRead())) {
             return;
        }

        if (!_activeTransaction) {
            throw new JDOUserException(msg.msg(
                "EXC_TransactionNotActive"));// NOI18N
        }

    }

    /**
     * assert Object is PersistenceCapable
     */
    private void assertPersistenceCapable(Object pc, String caller) {
        if ( !(pc instanceof PersistenceCapable) ) {
            String message = null;
            if (pc instanceof Collection) {
                message = msg.msg("EXC_CollectionType", caller); // NOI18N
            } else if (pc.getClass().isArray()) {
                message = msg.msg("EXC_ArrayType", caller);
            } else {
                message = msg.msg("EXC_NotPersistenceCapable", pc.getClass().getName()); // NOI18N
            }
            throw new JDOUserException(message, pc);
        }

    }

    /**
     * assert PersistenceCapable is associated with this instance of PersistenceManager
     */  
    private void assertPersistenceManager(PersistenceCapable pc) {
        // pc.jdoGetPersistenceManager() returns the wrapper. It should compare equals
        // to this PersistenceManager:
        if (!this.equals(pc.jdoGetPersistenceManager())) {
            throw new JDOUserException(msg.msg(
                 "EXC_AnotherPersistenceManager"));// NOI18N
        }
    }

    /**
     * Helper method to validate errors on processing arrays of objects.
     * @param l actual size of the array
     * @param err array of Throwable to validate
     * @throws JDOUserException if <code>l</code> is greater than 0.
     */  
    private void validateResult(int l, Throwable[] err) {
        if (l > 0) {
            Throwable[] t = new Throwable[l];
            System.arraycopy(err, 0, t, 0, l);
            throw new JDOUserException(msg.msg(
                "EXC_FailedToProcessAll"), t); //(Throwable[])err.toArray());// NOI18N
        } 
    }
    /**
     * Helper method to calculate initialSize of the Collection or Map if
     * null is specifield for the new tracked instance.
     *
     * @param type Class type for which the request is processed.
     * @return default initialSize as Integer.
     */  
    private Integer getInitialSize(Class type) {
        if (type.equals(com.sun.org.apache.jdo.impl.sco.HashSet.class) ||
            type.equals(com.sun.org.apache.jdo.impl.sco.HashMap.class) ||
            type.equals(com.sun.org.apache.jdo.impl.sco.Hashtable.class)) {
            return int11;
        } else {
            return int10;
        }
    }
    
    //
    // Internal methods to process state transitions requests.
    //

    /**
     * Internal method for processing makePersistent request.
     * @see #makePersistent(Object pc)
     */
    private void makePersistentInternal(Object pc) {
        if (pc == null) {
            if (debugging())
                debug("makePersistentInternal null"); // NOI18N
            return;        // ignore
        }
        if (debugging())
            debug("makePersistentInternal for " + pc.getClass().getName()); // NOI18N

        assertPersistenceCapable(pc, "makePersistent");
        if (debugging())
            debug("makePersistentInternal is pc"); // NOI18N

        PersistenceCapable p = (PersistenceCapable)pc;
        _txCache.makePersistent(p);
    }

    /**
     * Internal method for processing makePersistentAll request.
     * @see #makePersistentAll(Object[] pcs)
     */
    private void makePersistentAllInternal(Object[] pcs) {
        Throwable[] err = new Throwable[pcs.length];
        int l = 0;

        if (debugging())
            debug("makePersistentAllInternal " + pcs.length); // NOI18N
        for(int i = 0; i < pcs.length; i++) {
            try {
                makePersistentInternal(pcs[i]);
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        validateResult(l, err);
    }

    /**
     * Internal method to process deletePersistent call.
     * @see #deletePersistent(Object pc)
     */
    private void deletePersistentInternal(Object pc) {
        if (pc == null) {
            return;        // ignore
        }
        assertPersistenceCapable(pc, "deletePersistent");
        
        PersistenceCapable p = (PersistenceCapable)pc;

        if( !(p.jdoIsPersistent()) ) {
            throw new JDOUserException(msg.msg(
                "EXC_TransientInstance", pc.getClass().getName()));// NOI18N
        }

        StateManagerInternal sm = findStateManager(p);
        sm.deletePersistent();
    }
    
    /**
     * Internal method to process deletePersistentAll call.
     * @see #deletePersistentAll(Object[] pcs)
     */
    private void deletePersistentAllInternal(Object[] pcs) {
        Throwable[] err = new Throwable[pcs.length];
        int l = 0;

        for(int i = 0; i < pcs.length; i++) { 
            try {
                deletePersistentInternal(pcs[i]); 
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        validateResult(l, err);
    }

    /**
     * Internal method to process makeTransient call.
     * @see #makeTransient(Object pc)
     */  
    private void makeTransientInternal(Object pc) {
        if (pc == null) {
            return;        // ignore
        }
        assertPersistenceCapable(pc, "makeTransient");

        PersistenceCapable p = (PersistenceCapable)pc;
        _txCache.makeTransient(p);
    }

    /**
     * Internal method to process makeTransientAll call.
     * @see #makeTransientAll(Object[] pcs)
     */  
    private void makeTransientAllInternal(Object[] pcs) {
        Throwable[] err = new Throwable[pcs.length];
        int l = 0;

        for(int i = 0; i < pcs.length; i++) { 
            try {
                makeTransientInternal(pcs[i]);
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        validateResult(l, err);
    }

    /**
     * Internal method to process makeTransactional call.
     * @see #makeTransactional(Object pc)
     */  
    private void makeTransactionalInternal(Object pc) {
        if (pc == null) {
            return;        // ignore
        }
        assertPersistenceCapable(pc, "makeTransactional");
        
        PersistenceCapable p = (PersistenceCapable)pc;
        if (p.jdoIsPersistent()) {
            assertActiveTransaction(false);
        }

        _txCache.makeTransactional(p);
    }

    /**
     * Internal method to process makeTransactionalAll call.
     * @see #makeTransactionalAll(Object[] pcs)
     */  
    private void makeTransactionalAllInternal(Object[] pcs) {
        Throwable[] err = new Throwable[pcs.length];
        int l = 0;

        for(int i = 0; i < pcs.length; i++) {
            try {
                makeTransactionalInternal(pcs[i]);
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        validateResult(l, err);
    }

    /**
     * Internal method to process makeNontransactional  call.
     * @see #makeNontransactional(Object pc)
     */  
    private void makeNontransactionalInternal(Object pc) {
        if (pc == null) {
            return;        // ignore
        }
        assertPersistenceCapable(pc, "makeNontransactional");
        
        PersistenceCapable p = (PersistenceCapable)pc;
        _txCache.makeNontransactional(p);
    }

    /**
     * Internal method to process makeNontransactionalAll  call.
     * @see #makeNontransactionalAll(Object[] pcs)
     */  
    private void makeNontransactionalAllInternal(Object[] pcs) {
        Throwable[] err = new Throwable[pcs.length];
        int l = 0;

        for(int i = 0; i < pcs.length; i++) {
            try {
                makeNontransactionalInternal(pcs[i]);
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        validateResult(l, err);
    }

    /**
     * Internal method to process evict call.
     * @see #evict(Object pc)
     */  
    private void evictInternal(Object pc) {
        if (pc == null) {
            return;        // ignore
        }
        assertPersistenceCapable(pc, "evict");

        PersistenceCapable p = (PersistenceCapable)pc;
        _txCache.evict(p);
    }

    /**
     * Internal method to process evictAll call.
     * @see #evictAll(Object[] pcs)
     */  
    private void evictAllInternal(Object[] pcs) {
        Throwable[] err = new Throwable[pcs.length];
        int l = 0;

        for(int i = 0; i < pcs.length; i++) {
            try {
                evict(pcs[i]);
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        validateResult(l, err);
    }

    /**
     * Internal method to process refresh call.
     * @see #refresh(Object pc)
     */  
    private void refreshInternal(Object pc) {
        if (pc == null) {
            return;        // ignore
        }
        assertPersistenceCapable(pc, "refresh");

        PersistenceCapable p = (PersistenceCapable)pc;
        _txCache.refresh(p);
    }

    /**
     * Internal method to process refreshAll call.
     * @see #refreshAll(Object[] pcs)
     */  
    private void refreshAllInternal(Object[] pcs) {
        Throwable[] err = new Throwable[pcs.length];
        int l = 0;

        for(int i = 0; i < pcs.length; i++) {
            try {
                refresh(pcs[i]);
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        validateResult(l, err);
    }

    /**
     * Internal method to process retrieve call.
     * @see #retrieve(Object pc)
     */  
    private void retrieveInternal(Object pc, boolean DFGOnly) {
        // XXX the DFGOnly flag is ignored here.
        if (pc == null) {
            return;        // ignore
        }
        assertPersistenceCapable(pc, "retrieve");

        PersistenceCapable p = (PersistenceCapable)pc;
        _txCache.retrieve(p);
    }

    /**
     * Internal method to process retrieveAll call.
     * @see #retrieveAll(Object[] pcs)
     */  
    private void retrieveAllInternal(Object[] pcs, boolean DFGOnly) {
        Throwable[] err = new Throwable[pcs.length];
        int l = 0;

        for(int i = 0; i < pcs.length; i++) {
            try {
                retrieveInternal(pcs[i], DFGOnly);
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        validateResult(l, err);
    }

    /**
     * Tracing method
     * @param msg String to display
     */
    private void debug(String msg) {
        logger.debug("In PersistenceManagerImpl " + msg); // NOI18N
    }

    /**
     * Verifies if debugging is enabled.
     * @return true if debugging is enabled.
     */
    private boolean debugging() {
        return logger.isDebugEnabled();
    }

    /**
     * Calls getContextClassLoader for the current Thread in a
     * doPrivileged block. 
     * @return the context class loader of the current Thread
     * @exception SecurityException thrown by getContextClassLoader.
     */
    public ClassLoader getContextClassLoaderPrivileged()
        throws SecurityException
    {
        return (ClassLoader) AccessController.doPrivileged (
            new PrivilegedAction () {
                public Object run () {
                    return Thread.currentThread().getContextClassLoader();
                }
            }
            );
    }
    
    /**
     * Assert the NontransactionalRead flag is true or a transaction is active.
     */
    public void assertReadAllowed() {
        assertIsOpen();
        _transaction.assertReadAllowed();
    }
}
