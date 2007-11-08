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

package com.sun.org.apache.jdo.store;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;


import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.persistence.support.Extent;
import com.sun.persistence.support.spi.PersistenceCapable;
import com.sun.persistence.support.spi.StateManager;


/**
 * StoreManager represents the datastore to the rest of the JDO components.
 * It provides the means to write and read instances, to get the extent of
 * classes, and to get the object id for a persistence capable object.
 * <p>
 * Please note, this class is changed wrt. the JDO apache version:
 * Method newQueryResult is in comments.
 */
public interface StoreManager {
    /**
     * Returns a Connector suitable for committing or rolling back operations
     * on this store.
     */
    public Connector getConnector();

    /**
     * Returns a Connector suitable for committing or rolling back operations
     * on this store for a specific userid.
     * @param userid the userid for the connection
     * @param password the password for the connection
     */
    public Connector getConnector(String userid, String password);

    //
    // Methods which represent individual requests on the store
    //

    /**
     * Causes the object managed by the given state manager's object to be
     * inserted into the database.
     * @param loadedFields BitSet of fields to be inserted in the database (may be
     * ignored).
     * @param dirtyFields BitSet of all fields as all fields are marked as dirty for
     * <code>insert</code>. After return, bits will remain as set for fields that 
     * were <em>not</em> inserted. If any bits are set, the return will be 
     * <code>FLUSHED_PARTIAL</code>. 
     * @param sm The state manager whose object is to be inserted.
     * @return one of <code>StateManagerInternal.FLUSHED_{COMPLETE, PARTIAL,
     * NONE}</code>,
     * depending on the success of the operation in inserting specified fields
     * into the database.
     */
    public int insert(BitSet loadedFields, BitSet dirtyFields,
                      StateManagerInternal sm);

    /**
     * Causes the object managed by the given state manager to be updated
     * in the database.
     * @param loadedFields BitSet of fields loaded from the database.
     * @param dirtyFields BitSet of changed fields that are to be flushed to the
     * database. It is the StoreManager policy which fields are to be verified 
     * against those in the database, if this <code>update</code> is within the
     * context of an optimistic transaction.  After return, bits will remain set
     * for fields that were not flushed, and in such case the return will be
     * <code>FLUSHED_PARTIAL</code>. 
     * @param sm The state manager whose object is to be updated.
     * @return one of <code>StateManagerInternal.FLUSHED_{COMPLETE, PARTIAL,
     * NONE}</code>, depending on the success of the operation in updating
     * specified fields into the database.
     */
    public int update(BitSet loadedFields, BitSet dirtyFields,
                      StateManagerInternal sm);

    /**
     * Causes the object managed by the given state manager to be verified
     * in the database.
     * @param loadedFields BitSet of fields to be verified against those in the
     * database.
     * @param dirtyFields Unused as there are no changed fields in this transaction.
     * @param sm The state manager whose object is to be verified.
     * @return StateManagerInternal.FLUSHED_COMPLETE.
     * @throws JDODataStoreException if data in memory does not match that in
     * the database.
     */
    public int verifyFields(BitSet loadedFields, BitSet dirtyFields,
                             StateManagerInternal sm);

    // RESOLVE: Marina, do we need this? @see com.sun.org.apache.jdo.impl.fostore.FOStoreStoreManager#verifyExistence.
    /**
     * Causes the database to check if the object managed by the given state
     * manager exists in the database.
     * @param sm The state manager whose object is to be verified.
     * @return true if object exists in the database.
     */
//    public boolean verifyExistence(StateManagerInternal sm);

    /**
     * Causes the object managed by the given state manager to be deleted
     * in the database.
     * @param loadedFields BitSet of fields loaded from the database.
     * @param dirtyFields BitSet of changed fields. It is the StoreManager policy 
     * which fields are to be verified against those in the database, if this 
     * <code>delete</code> is within the context of an optimistic transaction. After 
     * return, bits will remain set for the fields that were not flushed, if the 
     * <code>update</code> was performed to resolve dependencies. In such case the 
     * return will be <code>StateManagerInternal.FLUSHED_PARTIAL</code>.
     * @param sm The state manager whose object is to be deleted.
     * @return one of <code>StateManagerInternal.FLUSHED_{COMPLETE, 
     * NONE}</code>, depending on the success of the delete operation.
     */
    public int delete(BitSet loadedFields, BitSet dirtyFields,
                          StateManagerInternal sm);

    //
    // The following methods allow fields of an object to be read from the
    // store to the client.  There are not any corresponding methods for
    // update, nor delete,as those are handled via prepare and commit.  I.e.,
    // when the prepare method of an implementation of this interface is
    // invoked, it should examine the given sm to see if it is dirty or
    // deleted, and update or remove it in the store accordingly.
    //
    
    /**
     * Causes values for fields required by the state manager's object to
     * be retrieved from the store and supplied to the state manager.
     * @param sm The state manager whose fields are to be read.
     * @param fieldNums The fields which are to be read.
     */
    public void fetch(StateManagerInternal sm, int fieldNums[]);

    /**
     * Provides the means to get all instances of a particular class, or of
     * that class and its subclasses.  If there are no instances of the given
     * class (nor its subclass) in the store, returns null.
     * @param pcClass Indicates the class of the instances that are in the
     * returned Extent.
     * @param subclasses If true, then instances subclasses of pcClass are
     * included in the resulting Extent.  If false, then only instances of
     * pcClass are  included.
     * @param pm PersistenceManagerInternal making the request.
     * @return An Extent from which instances of pcClass (and subclasses if
     * appropriate) can be obtained.  Does not return null if there are no
     * instances; in that case it returns an empty Extent.
     */
    public Extent getExtent(Class pcClass, boolean subclasses,
                            PersistenceManagerInternal pm);

    //
    // The following methods provide mappings between object ids, state
    // managers, and persistence capables.
    //
    
    /**
     * Creates a new Object Id for the given StateManagerInternal.  The
     * resulting Object Id should not be given to user/client code.
     * @param sm StateManagerInternal for which an Object Id is needed.
     * @param pm PersistenceManagerInternal in which the sm's object is
     * created.
     * @return Object Id corresponding to the given StateManagerInternal
     */
    public Object createObjectId(StateManagerInternal sm,
                                 PersistenceManagerInternal pm);

    /**
     * Returns an Object Id that can be given to user/client code and which is
     * a <em>copy or external representation</em> of the given objectId. 
     * @param objectId Object Id for which an external Object Id is needed.
     * @param pc PersistenceCapable instance associated with this Object Id.
     * @return An Object Id that can be given to user/client code.
     */
    public Object getExternalObjectId(Object objectId, PersistenceCapable pc);

    /**
     * Returns an Object Id that can be used by the runtime code and which is
     * a <em>an internal representation</em> of the given objectId. 
     * @param objectId Object Id for which an internal Object Id is needed.
     * @param pm PersistenceManagerInternal which requested the Object Id.
     * @param pcClass the PersistenceCapable class.
     * @return An Object Id that can be given to user/client code.
     */
    public Object getInternalObjectId(Object objectId, 
                                 PersistenceManagerInternal pm, Class pcClass);

    /**
    * Returns the Class of the PersistenceCapable instance identified by the
    * given oid.
    * @param oid object id whose java.lang.Class is wanted.
    * @param pm PersistenceManagerInternal to use in loading the oid's
    * Class.
    * @return java.lang.Class of the PersistenceCapable instance identified with
    * this oid.
    */
    public Class getPCClassForOid(Object oid, PersistenceManagerInternal pm);

    /**
     * Indicates whether mediation from store manager is required to copy oid
     * @return true if mediation is required else false.
     */
    boolean isMediationRequiredToCopyOid();

    /**
    * Returns true if actual Class for a PersistenceCapable instance can be
    * resolved only in the database.
    * @param objectId Object Id whose java.lang.Class needs to be resolved.
    * @return true if the request needs to be resolved in the back end.
    */  
    public boolean hasActualPCClass(Object objectId);

    /** 
     * This method returns an object id instance corresponding to the Class 
     * and String arguments. The String argument might have been the 
     * result of executing toString on an object id instance. 
     * @param pcClass the Class of the persistence-capable instance
     * @param str the String form of the object id
     * @return an instance of the object identity class
     */
    public Object newObjectIdInstance (Class pcClass, String str);

    /**
     * This method copies PK field values from internal Object Id into the
     * Hollow instance.
     * @param sm StateManagerInternal for which an operation is needed.
     * @param pcClass the Class of the persistence-capable instance
     */  
    public void copyKeyFieldsFromObjectId(StateManagerInternal sm, Class pcClass);

    //
    // Assorted other methods.
    //

    /**
    * Flushes all elements in the given iterator.
    * @param it Iterator of StateManagerInternal instances to be flushed.
    * @param pm PersistenceManagerInternal on whose behalf instances are being
    * flushed.
    * @throws JDOFatalDataStoreException if instances could not all be flushed
    * as determined by <code>sm.isFlushed()</code>.
    */
    public void flush(Iterator it, PersistenceManagerInternal pm);

    /**
     * Returns a QueryResult instance which is then returned as the result of 
     * Query.execute(...). This method allows support for datastore specific 
     * query execution strategies, since each StoreManager can have its own
     * implementation of the QueryResult interface.
     * @param qrh the helper providing the query tree, the candidates 
     * and the actual parameters.
     * @return a datastore specific query result instance
     */
    // MBO: method newQueryResult is in comments, because QueryResult
    // and QueryResultHelper are defined in the jdoql package.
    //public QueryResult newQueryResult(QueryResultHelper qrh);

}
