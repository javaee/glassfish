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
 * PersistenceManagerWrapper.java
 *
 * Created on January 16, 2001
 */
 
package com.sun.org.apache.jdo.impl.pm;

import java.util.*;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.*;
import com.sun.persistence.support.spi.PersistenceCapable;


/**  
 * This is a thin wrapper for the current implemetation of com.sun.persistence.support.PersistenceManager
 * interface. Delegates most of method execution to the corresponding instance of 
 * the PersistenceManagerImpl. Becomes invalid after PersistenceManager is closed.
 *  
 * @author Marina Vatkina 
 */  
public class PersistenceManagerWrapper implements PersistenceManagerInternal {

    // Previous  PersistenceManagerWrapper
    private PersistenceManagerWrapper prev = null;

    // Actual  PersistenceManager
    private PersistenceManagerImpl pm = null;

    // Boolean flag that allows to use this wrapper
    private boolean isValid = false;

    /**
     * I18N message handler
     */
     private final static I18NHelper msg = 
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.pm.Bundle"); // NOI18N

    // Constructed by  PersistenceManagerFactoryImpl
    PersistenceManagerWrapper(PersistenceManagerImpl pm) {
        this.pm = pm;
        prev = (PersistenceManagerWrapper)pm.getCurrentWrapper();
        pm.pushCurrentWrapper(this);
        isValid = true;
    }

    //----------------------------------------------------------------------
    // PersistenceManager Methods
    //----------------------------------------------------------------------

    /** 
     * @see com.sun.persistence.support.PersistenceManager#isClosed()
     */
    public boolean isClosed() {
        if (isValid) {
            return pm.isClosed();
        } else {
            return true;
        }
    }


   /**
    * @see com.sun.persistence.support.PersistenceManager#close()
    */
    public void close() {
        if (isValid) { 
            pm.popCurrentWrapper(prev);
            isValid = false;
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
    }

     /** 
      * @see com.sun.persistence.support.PersistenceManager#currentTransaction()
      */
    public Transaction currentTransaction() {
        if (isValid) { 
            return pm.currentTransaction();
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }

    /**
     * @see com.sun.persistence.support.PersistenceManager#setIgnoreCache(boolean flag)
     */
    public void setIgnoreCache(boolean flag) {
        if (isValid) {
            pm.setIgnoreCache(flag);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        if (isValid) {
            return pm.getIgnoreCache();
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }

    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#getMultithreaded()
     */
    public boolean getMultithreaded() {
        if (isValid) {
            return pm.getMultithreaded();
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#setMultithreaded(boolean flag)
     */
    public void setMultithreaded(boolean flag) {
        if (isValid) {
            pm.setMultithreaded(flag);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#evict(Object pc)
     */
    public  void evict(Object pc) {
        if (isValid) {
            pm.evict(pc);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#evictAll(Object[] pcs)
     */
    public  void evictAll(Object[] pcs) {
        if (isValid) {
            pm.evictAll(pcs);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#evictAll(Collection pcs)
     */
    public  void evictAll(Collection pcs) {
        if (isValid) {
            pm.evictAll(pcs);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#evictAll()
     */
    public  void evictAll() {
        if (isValid) {
            pm.evictAll();
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#refresh(Object pc)
     */
    public  void refresh(Object pc) {
        if (isValid) {
            pm.refresh(pc);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#refreshAll(Object[] pcs)
     */
    public  void refreshAll(Object[] pcs) {
        if (isValid) {
            pm.refreshAll(pcs);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#refreshAll(Collection pcs)
     */
    public  void refreshAll(Collection pcs) {
        if (isValid) {
            pm.refreshAll(pcs);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see com.sun.persistence.support.PersistenceManager#refreshAll()
     */
    public  void refreshAll() {
        if (isValid) {
            pm.refreshAll();
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery()
     */
    public Query newQuery(){
        if (isValid) { 
            return pm.newQuery();
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery(Object compiled)
     */
    public Query newQuery(Object compiled){
        if (isValid) { 
            return pm.newQuery(compiled);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery(Class cls)
     */
    public Query newQuery(Class cls){
        if (isValid) { 
            return pm.newQuery(cls);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery(Extent cln)
     */
    public Query newQuery(Extent cln){
        if (isValid) { 
            return pm.newQuery(cln);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery(Class cls,Collection cln)
     */
    public Query newQuery(Class cls,Collection cln){
        if (isValid) { 
            return pm.newQuery(cls, cln);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery(String language, Object query)
     */
    public Query newQuery (String language, Object query){
        if (isValid) { 
            return pm.newQuery(language, query);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery(Class cls, String filter)
     */
    public Query newQuery (Class cls, String filter){
        if (isValid) { 
            return pm.newQuery(cls, filter);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery(Class cls, Collection cln, String filter)
     */
    public Query newQuery (Class cls, Collection cln, String filter){
        if (isValid) { 
            return pm.newQuery(cls, cln, filter);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newQuery(Extent cln, String filter)
     */
    public Query newQuery (Extent cln, String filter){
        if (isValid) { 
            return pm.newQuery(cln, filter);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#getExtent(Class persistenceCapableClass,
     * boolean subclasses)
     */
    public Extent getExtent(Class persistenceCapableClass,boolean subclasses){
        if (isValid) { 
            return pm.getExtent(persistenceCapableClass, subclasses);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }

    /** 
     * @see com.sun.persistence.support.PersistenceManager#getObjectById(Object oid, boolean validate)
     */
    public Object getObjectById(Object oid, boolean validate){
        if (isValid) { 
            return pm.getObjectById(oid, validate);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#getObjectId(Object pc)
     */
    public Object getObjectId(Object pc){
        if (isValid) { 
            return pm.getObjectId(pc);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#getTransactionalObjectId (Object pc)
     */
    public Object getTransactionalObjectId (Object pc) {
        if (isValid) { 
            return pm.getTransactionalObjectId(pc);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#newObjectIdInstance (Class pcClass, String str)
     */
    public Object newObjectIdInstance (Class pcClass, String str) {
        if (isValid) { 
            return pm.newObjectIdInstance (pcClass, str);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makePersistent(Object pc)
     */
    public void makePersistent(Object pc){
        if (isValid) { 
            pm.makePersistent(pc);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makePersistentAll(Object[] pc)
     */
    public void makePersistentAll(Object[] pcs){
        if (isValid) { 
            pm.makePersistentAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makePersistentAll(Collection pcs)
     */
    public void makePersistentAll (Collection pcs){
        if (isValid) { 
            pm.makePersistentAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#deletePersistent(Object pc)
     */
    public void deletePersistent(Object pc){
        if (isValid) { 
            pm.deletePersistent(pc);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#deletePersistentAll(Object[] pc)
     */
    public void deletePersistentAll (Object[] pcs){
        if (isValid) { 
            pm.deletePersistentAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#deletePersistentAll(Collection pc)
     */
    public void deletePersistentAll (Collection pcs){
        if (isValid) { 
            pm.deletePersistentAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makeTransient(Object pc)
     */
    public void makeTransient(Object pc){
        if (isValid) { 
            pm.makeTransient(pc);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makeTransientAll(Object[] pc)
     */
    public void makeTransientAll(Object[] pcs){
        if (isValid) { 
            pm.makeTransientAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makeTransientAll(Collection pcs)
     */
    public void makeTransientAll (Collection pcs){
        if (isValid) { 
            pm.makeTransientAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makeTransactional(Object pc)
     */
    public void makeTransactional(Object pc){
        if (isValid) { 
            pm.makeTransactional(pc);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makeTransactionalAll(Object[] pc)
     */
    public void makeTransactionalAll(Object[] pcs){
        if (isValid) { 
            pm.makeTransactionalAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makeTransactionalAll(Collection pcs)
     */
    public void makeTransactionalAll (Collection pcs){
        if (isValid) { 
            pm.makeTransactionalAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /*
     * @see com.sun.persistence.support.PersistenceManager#makeNontransactional(Object pc)
     */
    public void makeNontransactional(Object pc){
        if (isValid) { 
            pm.makeNontransactional(pc);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makeNontransactionalAll(Object[] pc)
     */
    public void makeNontransactionalAll(Object[] pcs){
        if (isValid) { 
            pm.makeNontransactionalAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#makeNontransactionalAll(Collection pcs)
     */
    public void makeNontransactionalAll (Collection pcs){
        if (isValid) { 
            pm.makeNontransactionalAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** Retrieve an instance from the store.  This is only a hint to
     * the PersistenceManager that the application intends to use the
     * instance, and its field values should be retrieved.
     * <P>The PersistenceManager might use policy information about the
     * class to retrieve associated instances.
     */
    public void retrieve(Object pc) {
        if (isValid) { 
            pm.retrieve(pc);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
    }
    
    /** Retrieve field values of instances from the store.  This tells
     * the <code>PersistenceManager</code> that the application intends to use the
     * instances, and all field values must be retrieved.
     * <P>The <code>PersistenceManager</code> might use policy information about the
     * class to retrieve associated instances.
     * @param pcs the instances
     */
    public void retrieveAll(Object[] pcs) {
        if (isValid) { 
            pm.retrieveAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
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
        if (isValid) { 
            pm.retrieveAll(pcs, DFGOnly);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
    }
           
    /** Retrieve field values of instances from the store.  This tells
     * the <code>PersistenceManager</code> that the application intends to use the
     * instances, and all field values must be retrieved.
     * <P>The <code>PersistenceManager</code> might use policy information about the
     * class to retrieve associated instances.
     * @param pcs the instances
     */
    public void retrieveAll(Collection pcs) {
        if (isValid) { 
            pm.retrieveAll(pcs);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
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
        if (isValid) {
            pm.retrieveAll(pcs, DFGOnly);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
    }
            
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#getPersistenceManagerFactory()
     */
    public PersistenceManagerFactory getPersistenceManagerFactory(){
        if (isValid) { 
            return pm.getPersistenceManagerFactory();
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
   }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#setUserObject(Object o)
     */
    public void setUserObject(Object o){
        if (isValid) { 
            pm.setUserObject(o);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
    }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#getUserObject()
     */
    public Object getUserObject(){
        if (isValid) { 
            return pm.getUserObject();
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
    }
    
    /** 
     * @see com.sun.persistence.support.PersistenceManager#getObjectIdClass(Class cls)
     */
    public Class getObjectIdClass(Class cls){
        if (isValid) { 
            return pm.getObjectIdClass(cls);
        } else { 
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        } 
    }

    /**
     * Returns PersistenceManagerInternal associated with this wrapper.
     * This method should be accessed by the PersistenceManagerInternal
     * only.
     * @return PersistenceManagerInternal.
     */
    protected PersistenceManagerInternal getPersistenceManager() {
        return (PersistenceManagerInternal)pm;
    }

    /** 
     * Returns a hash code value for this PersistenceManagerWrapper.
     * @return  a hash code value for this PersistenceManagerWrapper.
     */
    public int hashCode() {
        return pm.hashCode();
    }

    /**  
     * Indicates whether some other object is "equal to" this one.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */  
    public boolean equals(Object obj) {
        if (obj instanceof PersistenceManagerWrapper) {
            return (((PersistenceManagerWrapper)obj).pm == this.pm);

        } else if (obj instanceof PersistenceManagerImpl) {
            return (((PersistenceManagerImpl)obj) == this.pm);
        }
        return false;
    }

    //----------------------------------------------------------------------
    // PersistenceManagerInternal Methods
    //----------------------------------------------------------------------

    /**
     * @see PersistenceManagerInternal#assertIsOpen
     */
    public void assertIsOpen() {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.assertIsOpen();
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#assertReadAllowed
     */
    public void assertReadAllowed() {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.assertReadAllowed();
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#getStoreManager
     */
    public StoreManager getStoreManager() {
        if (isValid) {
            return pm.getStoreManager();
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see PersistenceManagerInternal#getStoreManager
     */
    public void setStoreManager(StoreManager storeManager) {
        if (isValid) {
            pm.setStoreManager(storeManager);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see PersistenceManagerInternal#getStateManager
     */
    public StateManagerInternal getStateManager(Object oid,
                                                Class pcClass) {
        if (isValid) {
            return pm.getStateManager(oid,
                                      pcClass);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see PersistenceManagerInternal#findStateManager
     */
    public StateManagerInternal findStateManager(PersistenceCapable pc) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.findStateManager(pc);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#loadClass
     */
    public Class loadClass(String name,
                           ClassLoader given)
        throws ClassNotFoundException {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.loadClass(name,
        //                        given);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#loadPCClassForObjectIdClass
     */
    public Class loadPCClassForObjectIdClass(Class objectIdClass)
        throws ClassNotFoundException {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.loadPCClassForObjectIdClass(objectIdClass);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#getObjectById
     */
    public Object getObjectById(Class candidateClass,
                                Object oid,
                                boolean validate) {
        if (isValid) {
            return pm.getObjectById(candidateClass,
                                    oid,
                                    validate);
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see PersistenceManagerInternal#getInternalObjectId
     */
    public Object getInternalObjectId(Object pc) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.getInternalObjectId(pc);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#register
     */
    public void register(StateManagerInternal sm,
                         Object oid,
                         boolean transactional,
                         boolean throwDuplicateException) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.register(sm,
        //                oid,
        //                transactional,
        //                throwDuplicateException);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#registerTransient
     */
    public void registerTransient(StateManagerInternal sm) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.registerTransient(sm);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#deregister
     */
    public void deregister(Object oid) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.deregister(oid);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#deregisterTransient
     */
    public void deregisterTransient(StateManagerInternal sm) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.deregisterTransient(sm);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#replaceObjectId
     */
    public void replaceObjectId(Object oldId,
                                Object newId) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.replaceObjectId(oldId,
        //                       newId);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#hereIsStateManager
     */
    public void hereIsStateManager(StateManagerInternal sm,
                                   PersistenceCapable pc) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.hereIsStateManager(sm,
        //                          pc);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#markAsFlushed
     */
    public void markAsFlushed(StateManagerInternal sm) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    pm.markAsFlushed(sm);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#insideCommit
     */
    public boolean insideCommit() {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.insideCommit();
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#newSCOInstanceInternal
     */
    public Object newSCOInstanceInternal(Class type) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.newSCOInstanceInternal(type);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#newCollectionInstanceInternal
     */
    public Collection newCollectionInstanceInternal(Class type,
                                                    Class elementType,
                                                    boolean allowNulls,
                                                    Integer initialSize,
                                                    Float loadFactor,
                                                    Collection initialContents,
                                                    Comparator comparator) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.newCollectionInstanceInternal(type,
        //                                            elementType,
        //                                            allowNulls,
        //                                            initialSize,
        //                                            loadFactor,
        //                                            initialContents,
        //                                            comparator);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#newMapInstanceInternal
     */
    public Map newMapInstanceInternal(Class type,
                                      Class keyType,
                                      Class valueType,
                                      boolean allowNulls,
                                      Integer initialSize,
                                      Float loadFactor,
                                      Map initialContents,
                                      Comparator comparator) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.newMapInstanceInternal(type,
        //                                     keyType,
        //                                     valueType,
        //                                     allowNulls,
        //                                     initialSize,
        //                                     loadFactor,
        //                                     initialContents,
        //                                     comparator);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#isSupportedSCOType
     */
    public boolean isSupportedSCOType(Class type) {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.isSupportedSCOType(type);
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#flush
     */
    public void flush() {
        if (isValid) {
            pm.flush();
        } else {
            throw new JDOFatalUserException(msg.msg(
                "EXC_PersistenceManagerClosed"));// NOI18N
        }
    }

    /**
     * @see PersistenceManagerInternal#getCurrentWrapper
     */
    public PersistenceManager getCurrentWrapper() {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.getCurrentWrapper();
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }

    /**
     * @see PersistenceManagerInternal#getInsertedInstances
     */
    public Collection getInsertedInstances() {
        throw new UnsupportedOperationException("not delegated yet.");
        //if (isValid) {
        //    return pm.getInsertedInstances();
        //} else {
        //    throw new JDOFatalUserException(msg.msg(
        //        "EXC_PersistenceManagerClosed"));// NOI18N
        //}
    }
}
