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


package com.sun.org.apache.jdo.pm;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import com.sun.persistence.support.Extent;
import com.sun.persistence.support.PersistenceManager;
import com.sun.persistence.support.PersistenceManagerFactory;
import com.sun.persistence.support.Query;
import com.sun.persistence.support.Transaction;
import com.sun.persistence.support.spi.PersistenceCapable;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;

/**
 * Empty implementation; does nothing.
 */
public class MockPersistenceManagerInternal implements PersistenceManagerInternal {

    public void assertIsOpen() {
    }

    public void assertReadAllowed() {
    }

    public StoreManager getStoreManager() {
        return null;
    }

    public void setStoreManager(StoreManager storeManager) {
    }
    
    public StateManagerInternal getStateManager(Object oid, Class pcClass) {
        return null;
    }

    public StateManagerInternal findStateManager(PersistenceCapable pc) {
        return null;
    }

    public Class loadClass(String name, ClassLoader given)
            throws ClassNotFoundException {
        return null;
    }

    public Class loadPCClassForObjectIdClass(Class objectIdClass)
            throws ClassNotFoundException {
        return null;
    }

    public Object getInternalObjectId(Object pc) {
        return null;
    }

    public void register(StateManagerInternal sm, Object oid,
            boolean transactional, boolean throwDuplicateException) {
    }

    public void registerTransient(StateManagerInternal sm) {
    }

    public void deregister(Object oid) {
    }

    public void deregisterTransient(StateManagerInternal sm) {
    }

    public void replaceObjectId(Object oldId, Object newId) {
    }

    public void hereIsStateManager(StateManagerInternal sm,
            PersistenceCapable pc) {
    }

    public void markAsFlushed(StateManagerInternal sm) {
    }

    public boolean insideCommit() {
        return false;
    }

    public Object newSCOInstanceInternal(Class type) {
        return null;
    }

    public Collection newCollectionInstanceInternal(Class type,
            Class elementType, boolean allowNulls, Integer initialSize,
            Float loadFactor, Collection initialContents, Comparator comparator) {
        return null;
    }

    public Map newMapInstanceInternal(Class type, Class keyType,
            Class valueType, boolean allowNulls, Integer initialSize,
            Float loadFactor, Map initialContents, Comparator comparator) {
        return null;
    }

    public boolean isSupportedSCOType(Class type) {
        return false;
    }

    public void flush() {
    }

    public PersistenceManager getCurrentWrapper() {
        return null;
    }

    public Collection getInsertedInstances() {
        return null;
    }

    public boolean isClosed() {
        return false;
    }

    public void close() {
    }

    public Transaction currentTransaction() {
        return null;
    }

    public void evict(Object pc) {
    }

    public void evictAll(Object[] pcs) {
    }

    public void evictAll(Collection pcs) {
    }

    public void evictAll() {
    }

    public void refresh(Object pc) {
    }

    public void refreshAll(Object[] pcs) {
    }

    public void refreshAll(Collection pcs) {
    }

    public void refreshAll() {
    }

    public Query newQuery() {
        return null;
    }

    public Query newQuery(Object compiled) {
        return null;
    }

    public Query newQuery(String language, Object query) {
        return null;
    }

    public Query newQuery(Class cls) {
        return null;
    }

    public Query newQuery(Extent cln) {
        return null;
    }

    public Query newQuery(Class cls, Collection cln) {
        return null;
    }

    public Query newQuery(Class cls, String filter) {
        return null;
    }

    public Query newQuery(Class cls, Collection cln, String filter) {
        return null;
    }

    public Query newQuery(Extent cln, String filter) {
        return null;
    }

    public Extent getExtent(Class persistenceCapableClass, boolean subclasses) {
        return null;
    }

    public Object getObjectById(Object oid, boolean validate) {
        return null;
    }

    public Object getObjectId(Object pc) {
        return null;
    }

    public Object getTransactionalObjectId(Object pc) {
        return null;
    }

    public Object newObjectIdInstance(Class pcClass, String str) {
        return null;
    }

    public void makePersistent(Object pc) {
    }

    public void makePersistentAll(Object[] pcs) {
    }

    public void makePersistentAll(Collection pcs) {
    }

    public void deletePersistent(Object pc) {
    }

    public void deletePersistentAll(Object[] pcs) {
    }

    public void deletePersistentAll(Collection pcs) {
    }

    public void makeTransient(Object pc) {
    }

    public void makeTransientAll(Object[] pcs) {
    }

    public void makeTransientAll(Collection pcs) {
    }

    public void makeTransactional(Object pc) {
    }

    public void makeTransactionalAll(Object[] pcs) {
    }

    public void makeTransactionalAll(Collection pcs) {
    }

    public void makeNontransactional(Object pc) {
    }

    public void makeNontransactionalAll(Object[] pcs) {
    }

    public void makeNontransactionalAll(Collection pcs) {
    }

    public void retrieve(Object pc) {
    }

    public void retrieveAll(Collection pcs) {
    }

    public void retrieveAll(Collection pcs, boolean DFGOnly) {
    }

    public void retrieveAll(Object[] pcs) {
    }

    public void retrieveAll(Object[] pcs, boolean DFGOnly) {
    }

    public void setUserObject(Object o) {
    }

    public Object getUserObject() {
        return null;
    }

    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return null;
    }

    public Class getObjectIdClass(Class cls) {
        return null;
    }

    public void setMultithreaded(boolean flag) {
    }

    public boolean getMultithreaded() {
        return false;
    }

    public void setIgnoreCache(boolean flag) {
    }

    public boolean getIgnoreCache() {
        return false;
    }

    public Object getObjectById(Class candidateClass, Object oid, boolean validate) {
        return null;
    }
}
