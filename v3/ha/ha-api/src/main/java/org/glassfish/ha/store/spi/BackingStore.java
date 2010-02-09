/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.ha.store.spi;

import org.glassfish.ha.store.criteria.Criteria;
import org.jvnet.hk2.annotations.Contract;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * An object that stores a given Metadata against an id. This class defines the
 * set of operations that a container could perform on a store.
 * <p/>
 * <p/>
 * An instance of BackingStore is created by calling
 * <code>BackingStoreFactory.createSimpleStore()</code> method.
 * <p/>
 * <p/>
 * The BackingStore instance is created and used for storing data that belongs
 * to a single application or container.
 * <p/>
 * <p/>
 * The store implementation must be thread safe.
 * <p/>
 * <p/>
 *
 * @author Mahesh.Kannan@Sun.Com
 * @author Larry.White@Sun.Com
 */
public abstract class BackingStore<K, V> {

    private String storeName;

    private Properties props;

    private Class<V> vClazz;

    protected Properties getProperties() {
        return props;
    }

    protected String getStoreName() {
        return this.storeName;
    }

    protected void initialize(String storeName, Class<K> keyClazz, Class<V> vClazz, Properties props) {
        this.storeName = storeName;
        this.vClazz = vClazz;
        this.props = props;
    }

    /**
     * Creates a new BackingStore for the given storeType.
     * @param storeType The store type
     * @param storeName The store name
     * @param keyClazz  The key class
     * @param entryClazz The entry class
     * @param env  Properties that holds implementation specific data
     * @param <S>  The type of Key
     * @param <T>  The type of Value
     *
     * @return An instance of BackingStore whose keys are of type KeyClazz and Values are entryClazz
     *
     * @throws BackingStoreException If any exception during initialization
     */
    public static <S, T> BackingStore<S, T> createBackingStore(
            String storeType, String storeName, Class<S> keyClazz, Class<T> entryClazz,
            Properties  env)
            throws BackingStoreException {
        BackingStore<S, T> store = null;
        BackingStoreFactory factory = null;
        try {
            factory = BackingStoreRegistry.getInstance().getFactoryInstance(storeType);
        } catch (BackingStoreException bse) {
            throw bse;
        } catch (Exception ex) {
            throw new BackingStoreException(ex.getMessage(), ex);
        }
        if (factory != null) {
            try {
                store = factory.createBackingStore(storeName, keyClazz, entryClazz, env);
            } catch (Throwable th) {
                throw new BackingStoreException("Exception during createBackingStore", th);
            } 
        }
        return store;
    }

    /**
     * Create a new instance of Storable. T should be a StoreEntry.
     *
     * @return V a new instance
     * @throws IllegalArgumentException if entryClazz
     *                                  is not a StoreEntry
     */
    public final V createStorable() {
        V v = null;
        try {
            v = vClazz.newInstance();
            String subClassName = vClazz.getName() + "__Sub__";
            Class<V> subClazz = (Class<V>) Class.forName(subClassName);
            v = subClazz.newInstance();
        } catch (Exception ex) {
            //FIXME throw Exception
            System.err.println(getClass().getName() +
                    " Error while creating storable " + ex.getMessage());
        }
        return v;
    }


    /**
     * Load and return the data for the given id. The store is expected to
     * return the largest ever version that was saved in the stored using the
     * <code>save()</code> method.
     *
     * @param key the key whose value must be returned
     * @return the value if this store contains it or null. The implementation
     *         must return the exact same type as that was passed to it in the
     *         save method.
     * @throws NullPointerException  if the id is null
     * @throws BackingStoreException if the underlying store implementation encounters any
     *                               exception
     */
    public abstract V load(K key, String version) throws BackingStoreException;

    /**
     * Save the value whose key is id. The store is NOT expected to throw an exception if
     * isNew is false but the entry doesn't exist in the store. (This is possible in
     * some implementations (like in-memory) where packets could be lost.)
     *
     * @param key   the id
     * @param value The Metadata to be stored
     * @throws BackingStoreException if the underlying store implementation encounters any
     *                               exception
     * @pram isNew
     * A flag indicating if the entry is new or not.
     */
    public abstract void save(K key, V value, boolean isNew) throws BackingStoreException;

    /**
     * Remove the association for the id.
     * <p/>
     * After this call, any call to <code>load(id)</code> <b>must</b> return
     * null. In addition, any association between <code>id</code> and
     * container extra params must also be removed.
     *
     * @param key the id of the Metadata
     * @throws BackingStoreException if the underlying store implementation encounters any
     *                               exception
     */
    public abstract void remove(K key) throws BackingStoreException;

    /**
     * Remove expired entries
     */
    public abstract int removeExpired()
             throws BackingStoreException;

    /**
     * Remove instances that meet the criteria.
     *
     * @throws BackingStoreException if the underlying store implementation encounters any
     *                               exception
     */
    public void removeByCriteria(Criteria<V> criteria,
              StoreEntryEvaluator<K, V> eval) throws BackingStoreException {

    }

    /**
     * Get the current size of the store
     *
     * @return the number of entries in the store
     * @throws BackingStoreException if the underlying store implementation encounters any
     *                               exception
     */
    public abstract int size() throws BackingStoreException;

    /**
     * Called when the store is no longer needed. Must clean up and close any
     * opened resources. The store must not be used after this call.
     */
    public abstract void destroy() throws BackingStoreException;

    /**
     * Find entries that satisfy the given Criteria. If criteria is null, then
     * all entries in this store match the criteria.
     * The store must do the following:
     * 1. Execute the criteria and for each value that satisfy the criteria, must call
     * 2. evaluator._getExtraParamCollectionFromManager(key, value) and if evaluator._getExtraParamCollectionFromManager() return true, then ;
     * 3. if eagerFetch is true then all attributes of value are populated. Else only
     * those whose loadLazily is set to false will be returned.
     *
     * @param criteria The criteria that must be satisfied. Can be null (in which case
     *                 every value in this store is assumed to match the criteria)
     * @param eval     The StoreEntryEvaluator The evaluator whose _getExtraParamCollectionFromManager method must be invoked to further
     *                 narrow the result.
     */
    public Collection findByCriteria(Criteria<V> criteria, StoreEntryEvaluator<K, V> eval) {
        return (Collection) Collections.EMPTY_LIST;
    }

    //public <T> Collection<T> findByCriteria(T t) {return (Collection<T>) Collections.EMPTY_LIST;}


    /**
     * Cache the keys for the entries that satisfy the given Criteria. If criteria is null, then
     * all entries in this store match the criteria.
     * The store must do the following:
     * 1. Execute the criteria and for each value that satisfy the criteria, must call
     * 2. evaluator._getExtraParamCollectionFromManager(key, value) and if evaluator._getExtraParamCollectionFromManager() return true, then ;
     * 3. if eagerFetch is true then all attributes of value are populated. Else only
     * those whose loadLazily is set to false will be returned.
     *
     * @param criteria The criteria that must be satisfied. Can be null (in which case
     *                 every value in this store is assumed to match the criteria)
     * @param eval     The StoreEntryEvaluator The evaluator whose _getExtraParamCollectionFromManager method must be invoked to further
     *                 narrow the result.
     */
    public Collection synchronizeKeys(Criteria<V> criteria, StoreEntryEvaluator<K, V> eval, boolean eagerFetch) {
        return Collections.EMPTY_LIST;
    }

}