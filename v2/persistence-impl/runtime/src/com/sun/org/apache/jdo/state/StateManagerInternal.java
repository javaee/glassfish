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

package com.sun.org.apache.jdo.state;


import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.persistence.support.spi.PersistenceCapable;
import com.sun.persistence.support.spi.StateManager;


/**
 * Extends the StateManager interface for JDO-internal use.  Provides
 * additional information about the state of particular fields.  Provides a
 * means to give a field's value to an object that can cause that value to be
 * stored.
 *
 * @author Dave Bristor
 */
public interface StateManagerInternal extends StateManager {

    /**
    * Return values for flush operations
    */
    public static final int FLUSHED_PARTIAL  = -1;
    public static final int FLUSHED_NONE     = 0;
    public static final int FLUSHED_COMPLETE = 1;

    /**
    * Provides the object managed by this state manager.
    * @return The object managed by this state manager.
    */
    public PersistenceCapable getObject();


    /**
    * Returns internal representation of the object id associated with this statemanager.
    * @return internal representation of the object id associated with this statemanager.
    */
    public Object getInternalObjectId();

    /**
    * Returns external representation of the object id associated with this statemanager.
    * @return external representation of the object id associated with this statemanager.
    */
    public Object getExternalObjectId();

    /**
    * Allows a client to change this state manager's object Id.  For example,
    * with datastore identity, allows one object id to be used before the
    * object has been stored (i.e. a "provisional" id), and another once the
    * object has been put into the datbase.
    */
    public void setObjectId(Object objectId);

    /**
    * Causes the state manager to send itself to the store manager for
    * insert, update, and so on as per its own state.  It should flush itself
    * only if it has no dependencies on other state manager.
    * @param srm The StoreManager to which the instance should send itself.
    * @return true if the state manager could flush itself, false if it has
    * dependencies on other state managers and could not flush itself.
    */
    public boolean flush(StoreManager srm);

    /**
     * Causes the values of the field indicated by the specified field number
     * be given to the FieldManager.
     * @param fieldNumber Indicates which field should be provided to the
     * fieldManager.
     * @param fieldManager FieldManager to which the field should be given.
     * @param identifying If true, provides values from the before or flushed
     * image, as determined by this StateManager's state; if false provides
     * values from the current image.
     */
    public void provideField(int fieldNumber, FieldManager fieldManager,
                             boolean identifying);

    /**
     * Causes the values of the fields  indicated by the specified fields to
     * be given to the FieldManager.
     * @param fields Indicates which fields should be provided to the
     * fieldManager.
     * @param fieldManager FieldManager to which the field should be given.
     * @param identifying If true, provides values from the before or flushed
     * image, as determined by this StateManager's state; if false provides
     * values from the current image.
     */
    public void provideFields(int fields[], FieldManager fieldManager,
                              boolean identifying);

    /**
    * For replacing field values in a PC with the ones that is provided by
    * the FieldManager.
    * @param fields Indicates which fields should be replaced in the PC.
    * @param fieldManager FieldManager from which the field values should
    * be obtained.
    */
    public void replaceFields(int fields[], FieldManager fieldManager);

    /** 
     * Fetch or refresh object from the data store.
     */  
    public void reload();

    /** 
     * Retrieve an instance from the store.
     */  
    public void retrieve();
    
    /**
     * Transition the lifecycle state as if the instance is retrieved from the 
     * datastore, but use the specified field values instead of loading them 
     * from the datastore.
     * @param fields Indicates which fields should be replaced in the PC.
     * @param fieldManager FieldManager from which the field values should
     * be obtained.
     */
    public void replace(int fields[], FieldManager fieldManager);

    /**
     * Transitions lifecycle state in afterCompletion callback
     * @param abort true if transaction has been rolled back
     * @param retainValues true if values need to be preserved on commit.
     * @param restoreValues true if values need to be restored on rollback.
     */
    public void afterCompletion(boolean abort, boolean retainValues,
        boolean restoreValues);

   /**
     * Transitions lifecycle state in to PERSISTENT_NEW
     */
    public void makePersistent();

   /**
     * Transitions lifecycle state in to transactional
     */
    public void makeTransactional();

   /**
     * Transitions lifecycle state in to nontransactional
     */
    public void makeNontransactional();

   /**
     * Transitions lifecycle state in to TRANSIENT
     */
    public void makeTransient();

   /**
     * Transitions lifecycle state in to PERSISTENT_DELETED
     */
    public void deletePersistent();

   /**
     * Transitions lifecycle state to P_CLEAN or P_NON_TX
     */
    public void refreshInstance();

   /**
     * Transitions lifecycle state to HOLLOW
     */
    public void evictInstance();

   /**
     * Calls preStore on the associated object if necessary.
     */
    public void preStore();

   /**
     * Replaces field values that are regular SCO instances with tracked SCOs.
     * Called internally during the afterCompletion processing when instance 
     * transions to P-nontransactional (if the retainValues flag is set to true).
     * May be called by the StoreManager during the flush process to store tracked
     * instances in the data store.
     */
    public void replaceSCOFields();

   /** Processes relationships for reachability algorithm
     * and define the dependencies  
     * @param flag is true if method is called inside the flush, false otherwise
     */  
    public void handleReachability(boolean flag);

    /**
     * Returns true if the instance exists in a datastore. Returns false
     * for transient instances, PersistentNew, PersistentNewDeleted, and 
     * PersistentDeletedFlushed
     */
    public boolean isStored();

    /**
     * Returns true if the instance has been flushed to the datastore. 
     */
    public boolean isFlushed();

    /**
     * Sets dependency object containing dependency information specific to this 
     * instance of the StateManager
     * @param dependency new dependency object
     */  
    public Object setDependency(Object dependency);

    /**
     * Returns dependency object that contains dependency information specific to 
     * this instance of the StateManager
     */  
    public Object getDependency();
    
    /** 
     * Returns PersistenceManager associated with this StateManager instance
     * @return the PersistenceManager
     */
    public PersistenceManagerInternal getPersistenceManager();

    /** Mark the associated PersistenceCapable field dirty.
     * <P> The StateManager will make a copy of the field
     * so it can be restored if needed later, and then mark
     * the field as modified in the current transaction.
     * @param fieldNumber the number of the field
     */  
    public void makeDirty (int fieldNumber); 

    /**
     * Processes changes to the Tracked SCO instance owned by this
     * StateManager.
     * @param fieldNumber the number of the field
     * @param sco Tracked SCO instance.
     */
    public void trackUpdates(int fieldNumber, SCO sco);

    /**
     * Returns field name for the field number. Used for debugging.
     * @param fieldNumber the number of the field
     * @return field name as String
     */  
    public String getFieldName(int fieldNumber);

    /**
     * Allows StateManager to set the actual PC Class if it was not available
     * at the constructor time and create a hollow instnce of that type.
     * @param pcClass the Class type of the instance.
     */
    public void setPCClass(Class pcClass);

    /**
     * Returns PC Class known to this StateManager. Can be a candidate Class.
     * @return the Class type of the PC instance.
     */  
    public Class getPCClass();

    /** Tests whether this StateManager represents a instance made persistent
     * object.
     *
     * @return <code>true</code> if this StateManager represents an
     * instance made persistent in the current transaction.
     */
    public boolean isNew();

    /**
     * Returns <code>true</code>, if a before image must be created. The
     * decision is based on the current lifecycle state plus other conditions
     * e.g. transaction type, restore values flag, etc.
     * @return <code>true</code> if a before image must be created.
     */
    public boolean isBeforeImageRequired();

}
