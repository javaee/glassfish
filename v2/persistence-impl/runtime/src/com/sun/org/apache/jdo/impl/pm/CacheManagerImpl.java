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
 * CacheManagerImpl.java
 *
 * Created on December 1, 2000
 */

package com.sun.org.apache.jdo.impl.pm;

import java.util.*;
import java.lang.ref.WeakReference;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.impl.model.java.BaseReflectionJavaType;
import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.impl.state.StateManagerFactory;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaModelFactory;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.util.WeakValueHashMap;
import com.sun.persistence.support.*;
import com.sun.persistence.support.spi.*;

/*
 * This is the cache manager that is responsible for operation of
 * all types of caches (weak, transactional, flushed, transient)
 * associated with the referenced instance of a PersistenceManager.
 *
 * @author Marina Vatkina
 */
class CacheManagerImpl {

    // Reference to the corresponding PersistenceManagerImpl.
    PersistenceManagerImpl pm = null;

    /**
     * Collection of Persistent instances created and/or updated
     * in this Transaction
     */
    private Collection _txCache = Collections.synchronizedSet(new HashSet());

    /**
     * Collection of Transient-transactional instances registered
     * with this Transaction
     */
    private Collection _transientCache = new Vector();

    /**
     * Collection of Persistent instances that will require state
     * change at the transaction completion
     */
    private ArrayList _flushedCache = new ArrayList();

    /**
     * Collection of StateManager instances that represent Persistent
     * instances that had been made newly persistent in the current
     * transaction.
     */
    private ArrayList _newInstances = new ArrayList();

    /** 
     * Weak Hashtable of Persistent instances accessed by this PersistenceManager
     */
    private WeakValueHashMap _weakCache = new WeakValueHashMap();

    /**
     * Logger instance
     */
    private static final Log logger = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.impl.pm"); // NOI18N

    /**
     * I18N message handler
     */
    private final static I18NHelper msg = 
        I18NHelper.getInstance(CacheManagerImpl.class);

    /**
     * Constructs new instnstance of CacheManagerImpl
     * 
     * @param pm calling instance of PersistenceManagerImpl
     */
    CacheManagerImpl(PersistenceManagerImpl pm) {
        this.pm = pm;
    }

    /**
     * close the CacheManagerImpl
     */
    protected void close() {
        // RELEASE THE CACHE...
        // Nothing should be in _txCache and/or _flushedCache because
        // PersistenceManager verified that transaction is not
        // active. _transientCache can have transient transactional
        // instances, but it is OK to clear them. 
       _weakCache.clear();
       _txCache.clear();
       _flushedCache.clear();
       _transientCache.clear();
    }

    

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
    protected Object getObjectById (Class candidateClassType, Object oid, boolean validate) {
        if (debugging())
            debug ("getObjectById"); // NOI18N

        StateManagerInternal sm = this.getStateManager(candidateClassType, oid, validate);
        return ((StateManagerInternal)sm).getObject();
    }

    /**
     * Returns StateManager instance associated with this instance of ObjectId
     * Creates a Hollow instance of a PersistenceCapable object, if it cannot be 
     * found in the cache
     * @param oid an ObjectId
     * @param pcClass Class of a Hollow instance to be created.
     * @return the StateManagerInternal 
     */
    protected StateManagerInternal getStateManager (Object oid, Class pcClass) {
        if (debugging())
            debug ("getStateManager " + oid + " for: " + pcClass.getName()); // NOI18N

        StateManagerInternal sm = null;
        // Check weak cache to find SM:
        synchronized (_weakCache) {
            // Need to keep a reference to the value in the cache as it is a weak
            // cache and the value might be removed otherwise.
            Object o = _weakCache.get(oid);
            if (o == null) {
                // Nothing found
                sm  = createNewSM(null, oid, pcClass);
            } else {
                // Prepare Hollow instance if its class type was not
                // known before. 
                sm = (StateManagerInternal)o;
                sm.setPCClass(pcClass);
            }
        }

        if (debugging())
            debug ("return from getStateManager: " + sm); // NOI18N

        return sm;
    }

    /**
    * The ObjectId returned by this method represents the JDO identity of
    * the instance.  The ObjectId is a copy (clone) of the internal state
    * of the instance, and changing it does not affect the JDO identity of
    * the instance.
    * Delegates actual execution to the internal method.
    * @param pc the PersistenceCapable instance
    * @param transactional true if transactional Id is requested
    * @return the ObjectId of the instance
    */
    protected Object getExternalObjectId (PersistenceCapable pc,
                                          boolean transactional) {
        StateManagerInternal sm = pm.findStateManager(pc);

        Object oid = null;
        if (_weakCache.containsValue(sm)) {
            if (transactional)
                oid = sm.getTransactionalObjectId(pc);
            else
                oid = sm.getExternalObjectId();
        }

        return oid;
    }

    /** Make the transient instance persistent in this PersistenceManager.
     * This method must be called in an active transaction.
     * The PersistenceManager assigns an ObjectId to the instance and
     * transitions it to persistent-new.
     * The instance will be managed in the Extent associated with its Class.
     * The instance will be put into the data store at commit.
     * @param pc a transient instance of a Class that implements
     * PersistenceCapable
     */
    protected void makePersistent (PersistenceCapable pc) {

        StateManagerInternal sm = pm.findStateManager(pc);
        if (sm == null) {
            sm = StateManagerFactory.newInstance(pc, pm);
        }

        sm.makePersistent();
    }

    /** Make the transient or persistent instance transactional in
     * this PersistenceManager.
     * @see com.sun.persistence.support.PersistenceManager#makeTransactional(Object pc)
     */
    protected void makeTransactional(PersistenceCapable pc) {
        StateManagerInternal sm = pm.findStateManager(pc);
        if (sm == null) {
            sm = StateManagerFactory.newInstance(pc, pm);
        }
        sm.makeTransactional();
    }


    /** Make the transient or persistent instance transactional in
     * this PersistenceManager.
     * @see com.sun.persistence.support.PersistenceManager#makeNontransactional(Object pc)
     */
    protected void makeNontransactional(PersistenceCapable pc) {
        StateManagerInternal sm = pm.findStateManager(pc);
        if (sm == null) {
            throw new JDOUserException(msg.msg(
                    "EXC_NonTransactional")); // NOI18N
        }
        sm.makeNontransactional();
    }


    /** Make the persistent instance transient in this PersistenceManager.
     * @see com.sun.persistence.support.PersistenceManager#makeTransient(Object pc)
     */
    protected void makeTransient(PersistenceCapable pc) {
        StateManagerInternal sm = pm.findStateManager(pc);
        if (sm != null) {
            sm.makeTransient();
        }
    }

    /** Make persistent instance hollow in this PersistenceManager.
     * @see com.sun.persistence.support.PersistenceManager#evict(Object pc)
     */
    protected void evict(PersistenceCapable pc) {
        StateManagerInternal sm = pm.findStateManager(pc);
        if (sm != null) {
            sm.evictInstance();
        }
    }

    /** Make all non-dirty persistent instances in the cache hollow in 
     * this PersistenceManager.
     * @see com.sun.persistence.support.PersistenceManager#evictAll()
     */
    protected void evictAll() {
        StateManagerInternal sm = null;

        Iterator it = _weakCache.entrySet().iterator();
        while (it.hasNext()) {
            sm = (StateManagerInternal) ((Map.Entry)it.next()).getValue();
            sm.evictInstance();
        }
    }

    /** Retrieve Hollow persistent instance in this PersistenceManager.
     * @see com.sun.persistence.support.PersistenceManager#retrieve(Object pc)
     */
    protected void retrieve(PersistenceCapable pc) {
        StateManagerInternal sm = pm.findStateManager(pc);
        if (sm != null) {
            sm.retrieve();
        }
    }

    /** Refresh dirty persistent instance in this PersistenceManager.
     * @see com.sun.persistence.support.PersistenceManager#refresh(Object pc)
     */
    protected void refresh(PersistenceCapable pc) {
        StateManagerInternal sm = pm.findStateManager(pc);
        if (sm != null) {
            sm.refreshInstance();
        }
    }

    /** Refresh dirty persistent instances in the transactional cache 
     * of this PersistenceManager. Called in an active transaction.
     * @see com.sun.persistence.support.PersistenceManager#refreshAll()
     */
    protected void refreshAllTransactional() {
        StateManagerInternal sm = null;

        Iterator it = _txCache.iterator();
        while(it.hasNext()) {
            sm = (StateManagerInternal)it.next();
            sm.refreshInstance();
        }
    }

    /** Refresh  nontransactional instances in the weak cache
     * of this PersistenceManager. Called outside an active transaction.
     * @see com.sun.persistence.support.PersistenceManager#refreshAll()
     */
    protected void refreshAllNontransactional() {
        StateManagerInternal sm = null;

        Iterator it = _weakCache.entrySet().iterator();
        while (it.hasNext()) {
            sm = (StateManagerInternal) ((Map.Entry)it.next()).getValue();
            sm.refreshInstance();
        }
    }

    /**
     * Register transient instance in the transient cache
     */
    protected void registerTransient(StateManagerInternal sm) {
        Iterator it = _transientCache.iterator();
        while(it.hasNext()) {
            Object o = ((WeakReference)it.next()).get();
            if ((StateManagerInternal)o == sm) {
                // The same SM is found - nothing to do.
                return;
            }
        }
        _transientCache.add(new WeakReference(sm));
    }

    /**
     * Register persistent instance in the transactional cache
     */
    protected void register(StateManagerInternal sm, Object oid, 
            boolean transactional, boolean throwDuplicateException) {
        if (oid == null) {
            oid = sm.getInternalObjectId();
        }

        //register in both caches for transactional instances only

        if (! _weakCache.containsKey(oid)) {
            deregisterTransient(sm);
            _weakCache.put(oid, sm);  

        } else if (throwDuplicateException) {
            throw new JDOUserException(msg.msg(
                "EXC_ObjectExistsInCache")); // NOI18N
        }

        if (pm.currentTransaction().isActive() && transactional) {
            // Register in both caches for convenience.
            if (! _flushedCache.contains(sm)) {
                _flushedCache.add(sm); 
            }
            if (! _txCache.contains(sm)) {
                _txCache.add(sm); 
                if (sm.isNew()) 
                    _newInstances.add(sm.getObject());
            }
        }

        if (!transactional) {
            // Remove from transactional caches if instance became
            // nontransactional
            _txCache.remove(sm); 
            _flushedCache.remove(sm);
        }
    }

    /**
     * Remove transient instance from the transient cache
     */
    protected void deregisterTransient(Object sm) {
        Iterator it = _transientCache.iterator();
        while(it.hasNext()) {
            WeakReference wr = (WeakReference)it.next();
            if ((StateManagerInternal)wr.get() == sm) {
                _transientCache.remove(wr);
                break;
            }
        }
    }

    /**
     * Remove persistent instance from all caches
     */
    protected void deregister(Object oid) {
        if (oid != null) {
            //deregister the instance from all the caches
            Object o = _weakCache.remove(oid);

            // No need to do anything outside an active transaction.
            if (pm.currentTransaction().isActive()) {
                _txCache.remove(o);
                _flushedCache.remove(o);
            }
        }
    }

    /**  
     * @see com.sun.org.apache.jdo.pm.PersistenceManagerInternal#replaceObjectId(Object oldId,
     * Object newId)
     */  
    protected void replaceObjectId(Object oldId, Object newId) {
        if (debugging())
            debug ("replaceObjectId"); // NOI18N

        synchronized(_weakCache) {
            if (_weakCache.containsKey(newId)) {
                throw new JDOFatalInternalException(msg.msg(
                    "EXC_ObjectIdExistsInCache", newId)); // NOI18N
            }
            Object o = _weakCache.remove(oldId);
            if (o == null) {
                throw new JDOFatalInternalException(msg.msg(
                    "EXC_ObjectIdNotExistsInCache", newId)); // NOI18N
            }
            _weakCache.put(newId, o);
        }
    }

    /**  
     * @see com.sun.org.apache.jdo.pm.PersistenceManagerInternal#markAsFlushed(StateManagerInternal sm)
     */  
    protected void markAsFlushed(StateManagerInternal sm) { 
        _txCache.remove(sm);
    }

    /**
     * Called by Transaction#commit(), Transaction#beforeCompletion(), or
     * Transaction#internalFlush().
     * Processes instances for the reachability algorithm, then calls
     * StoreManager to iterate over transactional cache and to call flush() 
     * for each StateManager in it.
     */
    protected void flushInstances() {
        StateManagerInternal sm = null;

        Object[] e = _txCache.toArray();
        boolean commit = pm.insideCommit();

        for (int i = 0; i < e.length; i++) {
            sm = (StateManagerInternal)e[i];

            //
            // NOTE: handleRelationships has the side-effect of adding
            // more objects to the transaction cache.
            //
            sm.handleReachability(commit);
        }

        StoreManager srm = pm.getStoreManager();
        Iterator it = _txCache.iterator();

        srm.flush(it, pm);

        _txCache.clear();
    }

    /**
     * Called by Transaction commit() or rollback()
     * cleans up transactional cache
     * @param    abort 
     */
    protected void afterCompletion(boolean abort) {
        boolean retainValues = pm.currentTransaction().getRetainValues();
        boolean restoreValues = pm.currentTransaction().getRestoreValues();

        // Need to process transient instances also
        Iterator it = _transientCache.iterator();
        while(it.hasNext()) {
            Object o = ((WeakReference)it.next()).get();

            if (o == null) { 
                // It has been GC'd and should be removed from _transientCache. 
                it.remove(); 
            } else {
                _flushedCache.add(o);
            }
        }

        int len = _flushedCache.size();
        for ( int i = 0; i < len; i++) {
            StateManagerInternal sm = (StateManagerInternal)_flushedCache.get(i);
            sm.afterCompletion(abort, retainValues, restoreValues);
        }

        // Now clean the flushed cache
        _flushedCache.clear();
        _newInstances.clear();

        // Just in case beforeCompletion failed or it was a rollback
        _txCache.clear();
    }

    /**
     * Returns a Collection of instances that has been made persistent
     * or become persistent through persistence-by-reachability
     * algorithm in this transaction. Called by the Extent.iterator.
     * @see com.sun.org.apache.jdo.pm.PersistenceManagerInternal#getInsertedInstances
     * @return Collection of Persistent-New instances.
     */
    protected Collection getInsertedInstances() {
        if (debugging())
            debug("getInsertedInstances"); // NOI18N

        return _newInstances;
    }

    /** --------------Private Methods--------------  */

    /**
     * Returns StateManager instance associated with this instance of ObjectId.
     * If the candidateClassType is not null, then this method checks that the
     * given oid is for an instance of that class, else this method determines
     * the class of the instance from the oid.
     * @param candidateClassType Class of the instance indicated by the oid.
     * @param oid an ObjectId.
     * @param validate if the existence of the instance is to be validated.
     * @return StateManager for the oid.
     */
    private StateManagerInternal getStateManager(
            Class candidateClassType, Object oid, boolean validate) {
        Object o = null;
        StoreManager srm = pm.getStoreManager();
        
        if (candidateClassType != null) {
/*
            // getJDOClass will throw IllegalArgumentException if
            // c is not an Entity.
            JDOClass jdoClass = getJDOClass(candidateClassType);
            JavaType jdoPKClass = jdoClass.getObjectIdClass();
            
            Class pkClass = oid.getClass();
            
            // Verify that Class c's PK class is pk.getClass()
            BaseReflectionJavaType t = (BaseReflectionJavaType) jdoPKClass;
            Class x = t.getJavaClass();
            if (x != pkClass) {
                throw new JDOUserException(
                    msg.msg("EXC_OIDNotCompatible"), // NOI18N
                    oid);
            }
*/
            
        } else {
            candidateClassType = srm.getPCClassForOid(oid, pm);
            if (candidateClassType == null) {
                // not found, report an error
                throw new JDOUserException(
                    msg.msg("EXC_NotOID"), // NOI18N
                    oid);
            }
        }

        Object internalOid = srm.getInternalObjectId(oid, pm, candidateClassType);
        if (debugging())
            debug ("getStateManager internal oid: " + internalOid); // NOI18N
        StateManagerInternal sm = null;
        // Check weak cache to find SM:
        synchronized (_weakCache) {
            if((o = _weakCache.get(internalOid)) == null) {
                // Nothing found
                if (debugging())
                    debug ("getStateManager oid not found."); // NOI18N

                sm  = createNewSM(oid, internalOid, candidateClassType);
                // Always reload from the DB to resolve actual classType
                if (validate || !srm.hasActualPCClass(internalOid))
                    sm.reload();
                return sm;

            } else  if (validate && !_flushedCache.contains(o)) {
                // Found but NOT in the transactional cache. Reload.
                if (debugging())
                    debug ("getStateManager oid found - reload."); // NOI18N

                sm = (StateManagerInternal)o;
                sm.reload();
                return sm;
            }
        }
        return (StateManagerInternal)o;
    }

    /**
     * @param c class for which a JDOClass is needed
     * @return JDOClass for given Class c
     */
    private JDOClass getJDOClass(Class c) {
        JDOClass rc = null;
        JavaModelFactory javaModelFactory = RuntimeJavaModelFactory.getInstance();
        JavaModel javaModel = javaModelFactory.getJavaModel(c.getClassLoader());
        if (javaModel == null) {
            // No model -> given Class is not an Entity
            throw new IllegalArgumentException(); // XXX FIXME
        }
        JDOModel jdoModel = javaModel.getJDOModel();
        if (jdoModel == null) {
            // No model -> given Class is not an Entity
            throw new IllegalArgumentException(); // XXX FIXME
        }
        rc = jdoModel.getJDOClass(c.getName());
        if (rc == null) {
            throw new IllegalArgumentException(); // XXX FIXME
        }
        return rc;
    }
    
    /**
     * Creates new StateManager instance associated with this instance
     * of ObjectId.
     * @see #getObjectById(Class, Object , boolean)
     * @param UserOid a user provided ObjectId
     * @param internalOid an internal ObjectId
     * @param candidateClassType super class of a Hollow instance to be created.
     */
    private StateManagerInternal createNewSM(Object UserOid, Object internalOid,
                                                      Class candidateClassType) {
        try {
            return StateManagerFactory.newInstance(UserOid, internalOid, 
                pm, candidateClassType);

        } catch (JDOUserException e) {
            throw e;
            // XXX Possible jikes bug
            //
            // The following catch phrase causes jikes to complain (Caution:
            // This try block cannot throw a "checked exception" (JLS section
            // 14.7) that can be caught here. You may have intended to catch
            // a RuntimeException instead of an Exception.)  But this try
            // block is *not* here throwing any checked exceptions!  That's
            // why I think it's a jikes bug (Sun's javac does not complain.)
        } catch (Exception e) {
            throw new JDOUserException(msg.msg("EXC_NotOID"), e, UserOid); // NOI18N
        }
    }

    /**
     * Tracing method
     * @param msg String to display
     */  
    private void debug(String msg) {
        logger.debug("In CacheManagerImpl " + msg); // NOI18N
    }
    /**
     * Verifies if debugging is enabled.
     * @return true if debugging is enabled.
     */
    private boolean debugging() {
        return logger.isDebugEnabled();
    }

}
