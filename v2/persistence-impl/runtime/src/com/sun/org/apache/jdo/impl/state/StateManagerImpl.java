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
 * StateManagerImpl.java
 *
 * Created on September 1, 2000, 2:29 PM
 * @version 1.0.1
 */

package com.sun.org.apache.jdo.impl.state;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.sco.SCOCollection;
import com.sun.org.apache.jdo.sco.SCOMap;
import com.sun.org.apache.jdo.state.FieldManager;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.*;
import com.sun.persistence.support.spi.JDOImplHelper;
import com.sun.persistence.support.spi.PersistenceCapable;
import com.sun.persistence.support.spi.StateManager;

/**
 * This is the StoreManager independent implemetation of
 * com.sun.persistence.support.spi.StateManager interface. Delegates state transition
 * requests to LifeCycleState.
 *
 * @author  Marina Vatkina
 * @version 1.0.1
 */
class StateManagerImpl implements StateManagerInternal {

    // Reference to the associated PersistenceManager as
    // PersistenceManagerInternal
    private PersistenceManagerInternal myPM = null;

    // Current Transaction
    private Transaction tx = null;

    // Associated PersistenceCapable object
    private PersistenceCapable myPC = null;

    // LifeCycle state to handle state transition requests.
    private LifeCycleState myLC = null;

    private byte jdoFlags = 0;

    /** beforeImage represents state of an instance before
     * any change or as of the call to makePersistent/deletePersistent
     */
    private PersistenceCapable beforeImage = null;

    /** flushedImage represents state of an instance as of the last flush
     * to the datastore.
     */
    private PersistenceCapable flushedImage = null;

    /** Helper StateFieldManager instance for resetting fields in a Hollow
     * instance at commit/rollback.
     */
    private final static StateFieldManager hollowFieldManager =
        new StateFieldManager();
    
    /** Helper StateFieldManager instance for fetching Object fields values for
     * reachability and SCO processing.
     */
    private StateFieldManager objectFieldManager = new StateFieldManager();
    
    /** Reference to JDO Model.
     */
    private Object metaData = null;
    private Class myPCClass = null;
    
    // Flag that indicates processing inside Transaction.afterCompletion when 
    // fields are being reset to Java default values
    private boolean inAfterCompletion = false;

    // Flag that indicates state transition to Transient inside
    // disconnect() method to allow setting jdoStateManager to null
    // from call-back replacingStateManager()
    private boolean transitionTransient = false;

    // Flag that indicates that PersistenceManagerFactory supports option 
    // com.sun.persistence.support.option.ChangeApplicationIdentity.
    private boolean allowedChangeApplicationIdentity = false;

    // Representation of the available ("get") fields
    private BitSet loadedFields = null;

    // Representation of the changed ("set") fields
    private BitSet dirtyFields = null;

    // Representation of the fieldspresent in the beforeImage
    private BitSet biFields = null;

    // Helper array to keep a single field number.
    private int[] fieldArr = new int[1];

    // The objectId of the object associated with this StateManager.
    private Object objectId;

    // The transactional objectId of the object associated with this
    // StateManager.
    private Object txObjectId;

    // Object contains dependency information specific to this
    // instance of the StateManager
    private Object dependency = null;

    // Assists in moving data from this StateManager's object to/from the
    // store.  See provideFields(), replaceFields(), and the getXXXField()
    // and providedXXXField() methods.
    private FieldManager fieldManager;

    // Reference to the instance requested to provide fields via fieldManager.
    private Object expectedProvider;

    // Class model information
    private JDOClass jdoClass;

    // Number of persistent fields
    private int numFields = 0;

    // JDOImplHelper instance
    static final JDOImplHelper jdoImplHelper = 
        (JDOImplHelper) AccessController.doPrivileged (
        // Need to have privileges to perform JDOImplHelper.getInstance().
            new PrivilegedAction () {
                public Object run () {
                    try {
                        return JDOImplHelper.getInstance();
                    }    
                    catch (SecurityException e) {
                        throw new JDOFatalUserException (msg.msg(
                            "EXC_CannotGetJDOImplHelper"), e); // NOI18N
                    }
                }
            }    
        );

    /** RuntimeJavaModelFactory. */
    private static final RuntimeJavaModelFactory javaModelFactory =
        (RuntimeJavaModelFactory) AccessController.doPrivileged(
            new PrivilegedAction () {
                public Object run () {
                    return RuntimeJavaModelFactory.getInstance();
                }
            }
        );

    // ReachabilityHandler instance
    private final static ReachabilityHandler reachabilityHandler = 
        ReachabilityHandler.getInstance();

    // SCOProcessor instance
    private final static SCOProcessor scoProcessor = 
        SCOProcessor.getInstance();

    // Helper string
    private final static String ChangeApplicationIdentityOption = 
        "com.sun.persistence.support.option.ChangeApplicationIdentity"; // NOI18N

    /**
     * I18N message handler
     */
    private final static I18NHelper msg = 
    	I18NHelper.getInstance("com.sun.org.apache.jdo.impl.state.Bundle");  // NOI18N

    /**
     * Logger instance
     */  
    private static final Log logger = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.impl.state"); // NOI18N

    /** Register this class with the JDOImplHelper as an authorized class
     * for replaceStateManager.
     */
    static {
        AccessController.doPrivileged(
            new PrivilegedAction () {
                public Object run () {
                    JDOImplHelper.registerAuthorizedStateManagerClass(StateManagerImpl.class);
                    return null;
                }
            }
        );
    }
    
    /** Constructs a new <code>StateManagerImpl</code> to process
     * future makePersistent request.
     * @param pc the reference to the associated PersistenceCapable instance
     * @param pm the reference to the associated
     * PersistenceManagerInternal instance
     */  
    StateManagerImpl(PersistenceCapable pc, PersistenceManagerInternal pm) {
        if (debugging())
            debug("constructor with PC: " + pc); // NOI18N

        myPC = pc;
        myPCClass = pc.getClass();
        initializePM(pm);
        initializePCInfo();
    }

    /** Constructs a new <code>StateManagerImpl</code> when requested
     * from query processing.
     * @param uoid the reference to the user object ID
     * @param ioid the reference to the internal object ID
     * @param pm the reference to the associated
     * PersistenceManagerInternal instance
     * @param clazz Class of the PersistenceCapable instance
     */ 
    StateManagerImpl(Object uoid, Object ioid, PersistenceManagerInternal pm,
                     Class clazz) {
        this.objectId = ioid; 
        this.txObjectId = this.objectId;
        if (debugging())
            debug("constructor with user OID: " + uoid); // NOI18N

        initializePM(pm);
        StoreManager srm = myPM.getStoreManager();
        myPCClass = clazz; 

        if (uoid == null) { // Requested by the store.
            initializePC(srm);

        } else if (srm.hasActualPCClass(ioid)){
            initializePCInfo();
            myPC = jdoImplHelper.newInstance (myPCClass, this, uoid);
            markPKFieldsAsLoaded();

        } // else do nothing.

        if (tx.isActive() && (tx.getOptimistic() == false)) { 
            myLC = LifeCycleState.getLifeCycleState(LifeCycleState.HOLLOW);
        } else {
            myLC = LifeCycleState.getLifeCycleState(LifeCycleState.P_NON_TX);
        }

        if (debugging())
            debug("constructor " + myLC + " for: " + myPCClass); // NOI18N

        registerNonTransactional();
    }

    /** Initialize PC Class information.
     */
    private void initializePCInfo() {
        if (debugging())
            debug("initializePCInfo"); // NOI18N

        jdoClass = javaModelFactory.getJavaType(myPCClass).getJDOClass();

        // RESOLVE: remember JDOFields of FieldDescriptors.
        numFields = jdoClass.getManagedFields().length;

        loadedFields = new BitSet(numFields);

        dirtyFields = new BitSet(numFields);
        biFields = new BitSet(numFields);
    }

    /** Initialize PersistenceManager related information.
     * @param pm the reference to the associated
     * PersistenceManagerInternal instance
     */
    private void initializePM(PersistenceManagerInternal pm) {
        if (debugging())
            debug("initializePM: " + pm); // NOI18N

        myPM = pm;
        tx = myPM.currentTransaction();
        PersistenceManagerFactory pmf = myPM.getPersistenceManagerFactory();
        allowedChangeApplicationIdentity = pmf.supportedOptions().contains(
            ChangeApplicationIdentityOption);

    }

    /** Mark PK fields as loaded:
     */
    private void markPKFieldsAsLoaded() {
        int[] pkfields = jdoClass.getPrimaryKeyFieldNumbers();
        for (int i = 0; i < pkfields.length; i++) {
            if (debugging())
                debug("markPKFieldsAsLoaded " + myPCClass.getName() + "." + // NOI18N
                    getFieldName(pkfields[i])); 

            loadedFields.set(pkfields[i]);
        }
    }

    /** Clears the loaded field bits and marks only the key fields, if
     * any, as loaded.  Note: The key fields must always be marked as
     * loaded for Application Identity.
     */
    private void clearLoadedFields() {
        loadedFields.andNot(loadedFields);
        markPKFieldsAsLoaded();
    }

    //
    // ------------ State transition methods ------------
    //

    /** Transitions LifeCycleState on afterCompletion.
     * @param abort true if rollback
     * @param retainValues the flag that indicates how to proceed on commit.
     * @param restoreValues the flag that indicates how to proceed on rollback.
     */
    public void afterCompletion(boolean abort, boolean retainValues, 
            boolean restoreValues) {
        inAfterCompletion = true;

        if (debugging())
            debug("afterCompletion " + myLC); // NOI18N

        if (abort)
            myLC = myLC.transitionRollback(restoreValues, this);
        else
            myLC = myLC.transitionCommit(retainValues, this);

        if (debugging())
            debug("afterCompletion " + myLC); // NOI18N

        inAfterCompletion = false;
    }

    /** Transition to Persistent-New
     */
    public void makePersistent() {
        LifeCycleState st = myLC;

        if (debugging())
            debug("makePersistent " + myLC); // NOI18N

        if (myLC == null) {
            // New request.
            initializeSM(LifeCycleState.P_NEW);
        } else {
            // LifeCycle is already asigned.
            myLC = myLC.transitionMakePersistent(this);
        }
        if (st != myLC) {
            // It was not a no-op...
            createAllBeforeImage();

            // Replace java.util SCO instances with tracked SCOs.
            replaceSCOFields();
            processReachability(false);
            setSCOOwner(true);
        }
        if (debugging())
            debug("makePersistent " + myLC); // NOI18N
    }

    /** delete persistencecapable
     */
    public void deletePersistent() {
        if (debugging())
            debug("deletePersistent " + myLC); // NOI18N

        myLC = myLC.transitionDeletePersistent(this);
        //deleteRelationships();
        if (debugging())
            debug("deletePersistent " + myLC); // NOI18N
    }

    /** Transition to Transactional
     */  
    public void makeTransactional() {
        if (debugging())
            debug("makeTransactional " + myLC); // NOI18N

        if (myLC == null) {
            // New request.
            initializeSM(LifeCycleState.T_CLEAN);
            markAllDirty();
            createBeforeImage();
        } else if (tx.isActive()); {
            // LifeCycle is already asigned.
            myLC = myLC.transitionMakeTransactional(this, tx);
        }
        if (debugging())
            debug("makeTransactional " + myLC); // NOI18N
    }

    /** Transition to Nontransactional
     */  
    public void makeNontransactional() {
        if (debugging())
            debug("makeNontransactional " + myLC); // NOI18N

        myLC = myLC.transitionMakeNontransactional(this, tx);
        if (debugging())
            debug("makeNontransactional " + myLC); // NOI18N
    }

    /** Transition to Transient
     */  
    public void makeTransient() {
        if (debugging())
            debug("makeTransient " + myLC); // NOI18N

        myLC = myLC.transitionMakeTransient(this, tx);
        if (debugging())
            debug("makeTransient " + myLC); // NOI18N
    }

    /** Transition to Hollow
     */  
    public void evictInstance() {
        if (debugging())
            debug("evictInstance " + myLC); // NOI18N

        myLC = myLC.transitionEvict(this, tx);
        if (debugging())
            debug("evictInstance " + myLC); // NOI18N
    }

    /** Transition to Clean
     */  
    public void refreshInstance() {
        if (debugging())
            debug("refreshInstance " + myLC); // NOI18N

        myLC = myLC.transitionRefresh(this, tx);
        if (debugging())
            debug("refreshInstance " + myLC); // NOI18N
    }

    /** Transition on retrieve request. This fetches Hollow instance
     * and transitions to the appropriate LifeCycle state.
     */  
    public void retrieve() {
        if (debugging())
            debug("retrieve " + myLC); // NOI18N

        myLC = myLC.transitionRetrieve(this, tx);
        if (debugging())
            debug("retrieve " + myLC); // NOI18N
    }
    
    /**
     * Transition the lifecycle state as if the instance is retrieved from the 
     * datastore, but use the specified field values instead of loading them 
     * from the datastore.
     * @param fields Indicates which fields should be replaced in the PC.
     * @param fieldManager FieldManager from which the field's value should be
     * obtained.
     */
    // Synchronized to avoid conflicts w.r.t. fieldManager.
    public synchronized void replace(int[] fields, FieldManager fieldManager) {
        if (debugging())
            debug("replace " + myLC); // NOI18N
        
        myLC = myLC.transitionReplace(this, tx, fields, fieldManager);
         
        if (debugging())
            debug("replace " + myLC); // NOI18N
    }
     
    /** Fetches or refreshes pc instance. Called by
     * PersistenceManager.getObjectById with validate flag set to true.
     */  
    public void reload() {
        if (debugging())
            debug("reload " + myLC); // NOI18N

        try {
            myLC = myLC.transitionReload(this, tx);
        } catch (JDOException e) {
            // RESOLVE - disconnect or just deregister?
            disconnect();
            throw e;
        }
        if (debugging())
            debug("reload " + myLC); // NOI18N
    }

    /**
    * @see com.sun.org.apache.jdo.state.StateManagerInternal#flush(StoreManager srm)
    */
    public boolean flush(StoreManager srm) {
        LifeCycleState st = myLC;
        try {
            myLC = myLC.flush(loadedFields, dirtyFields, srm, this);
        } catch (JDOException e) {
            myLC = st;
            throw e;
        }
        return true;
    }

    /** 
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#handleReachability(
     * boolean flag)
     */
    public void handleReachability(boolean commit) { 
        if (myLC.isDeleted) {
            // Don't do reachability for deleted instances.
            return;
        } else {
            processReachability(commit);
            if (!commit && myLC.isPersistent() && ! myLC.isAutoPersistent()) {
                // Set owner only inside an active transaction and
                // only for persistent instances.
                setSCOOwner(true); 
            }
        }
    }

    /** 
     * Replaces field values that are regular SCO instances with tracked SCOs.
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#replaceSCOFields()
     */
    public void replaceSCOFields() {
        SCO sco = null;
        Object o = null;
        Class cls = null;
        JDOField jdoField = null;
        int[] nonpkfields = jdoClass.getPersistentNonPrimaryKeyFieldNumbers();

        for (int i = 0; i < nonpkfields.length; i++) {
            int field = nonpkfields[i];
            jdoField = jdoClass.getField(field);
            if (isSCOType(javaModelFactory.getJavaClass(jdoField.getType()))) {
                o = fetchObjectField(field);
                sco = scoProcessor.getSCOField(o, jdoField, myPM);
                replaceSCO(sco, field);

            } // else primitive - do nothing.
        }
    }

    /** 
     * Unsets owner of tracked SCO field values and marks fields as not loaded.
     */
    protected void unsetSCOFields() {
        Object o = null;
        JDOField jdoField = null;
        int[] nonpkfields = jdoClass.getPersistentNonPrimaryKeyFieldNumbers();
        int tmp[] = new int[nonpkfields.length];
        int l = 0;

        for (int i = 0; i < nonpkfields.length; i++) {
            int field = nonpkfields[i];
            jdoField = jdoClass.getField(field);
            if (isSCOType(javaModelFactory.getJavaClass(jdoField.getType()))) {
                o = fetchObjectField(field);
                if (com.sun.org.apache.jdo.sco.SCO.class.isInstance(o)) {
                    resetOwner((SCO)o, field, false);
                    loadedFields.clear(field);
                    tmp[l] = field;
                    l++;
                }

            } // else primitive - do nothing.
        }
/* DO NOT SET TO NULL
        // Now set those references to null.
        int[] fields = new int[l];
        System.arraycopy(tmp, 0, fields, 0, l);

        this.fieldManager = hollowFieldManager; // Save for callback in giveXXXField

        expectedProvider = myPC; // Save for verification.
        myPC.jdoReplaceFields(fields);
        expectedProvider = null; // No expected request.
*/

    }

    /** 
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#makeDirty (int field)
     */  
    public void makeDirty (int field) {
        if (debugging())
            debug("makeDirty " + field); // NOI18N

        // Reload field if necessary:
        if(myLC.isPersistent()) {
            loadField(field);
        }

        if (debugging())
            debug("makeDirty " + myLC); // NOI18N

        myLC = myLC.transitionWriteField(this, tx);

        if (debugging())
            debug("makeDirty " + myLC); // NOI18N

        updateBeforeImage(getFields(field));
        dirtyFields.set(field);
    }

    /** 
     * Makes newly added instances to an SCO Collection or SCO Map
     * auto-persistent.
     *
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#trackUpdates (int field, 
     * SCO sco)
     */  
    public void trackUpdates (int field, SCO sco) {
        if (debugging())
            debug("trackUpdates " + field); // NOI18N

        scoProcessor.trackUpdates(this, field, sco);
    }

    /** 
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#getFieldName (int field)
     */  
    public String getFieldName (int field) {
        return jdoClass.getField(field).getName();
    }

    /** 
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#getPCClass ()
     */  
    public Class getPCClass () {
        return myPCClass;
    }

    /** 
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#setPCClass (Class pcClass)
     */  
    public void setPCClass (Class pcClass) {
        if (myPC == null) {
            // myPC will be null if its class cannot be resolved 
            // until fetch.
            if (debugging())
                debug("setPCClass " + myLC + " for: " + pcClass); // NOI18N

            myPCClass = pcClass; 
            initializePC(myPM.getStoreManager());
        }
    }

    /** Tests whether this StateManager represents a instance made persistent
     * object.
     *
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#isNew ()
     * @return <code>true</code> if this StateManager represents an
     * instance made persistent in the current transaction.
     */
    public boolean isNew() {
        return myLC.isNew();
    }

    /**
     * Returns <code>true</code>, if a before image must be created. The
     * decision is based on the current lifecycle state plus other conditions
     * e.g. transaction type, restore values flag, etc.
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#isBeforeImageRequired ()
     * @return <code>true</code> if a before image must be created.
     */
    public boolean isBeforeImageRequired() {
        boolean isTransientTransactional =
            !myLC.isPersistent() && myLC.isTransactional();
        return (tx.getOptimistic() || tx.getRestoreValues() ||
            isTransientTransactional);
    }

    //
    // LifeCycleState transition requests
    //

    /** Transition to Auto-Persistent-New (persistence-by-reachability)
     */
    protected void makeAutoPersistent() {
        LifeCycleState st = myLC;
        if (debugging())
            debug("makeAutoPersistent " + myLC); // NOI18N
 
        if (myLC == null) {
            initializeSM(LifeCycleState.AP_NEW);
        } else {
            myLC = myLC.transitionToAutoPersistent(this);
        }      
        if (st != myLC) {
            // It was not a no-op...
            createAllBeforeImage();
            handleReachability(false); 
        }
        if (debugging())
            debug("makeAutoPersistent " + myLC); // NOI18N
    }

    /** Processes Array of referenced objects for possible auto-persistence
     * (persistence-by-reachability).
     * @param o Array of referenced objects
     */
    protected void makeAutoPersistent(Object[] o) {
        if (o != null) {
            for (int i = 0; i < o.length; i++) {
                if (o[i] != null) {
                    reachabilityHandler.process(o[i], myPM, false);
                } // else nothing to do.
            }
        }
    }
                
    /** Set owner on elements of an Array of SCO objects.
     * @param o array of referenced objects.
     * @param field the field number.
     * @param set true if owner field should be set, false if unset.
     */  
    protected void resetOwner(Object[] o, int field, boolean set) {
        if (o == null) {
            // Nothing to do.
            return;
        }
        for (int i = 0; i < o.length; i++) {
            if (o[i] != null) {
                resetOwner(o[i], field, set);
            } // else nothing to do.
        }
    }

    /** Set owner on elements of an Iterator of SCO objects.
     * @param it Iterator over referenced objects.
     * @param field the field number.
     * @param set true if owner field should be set, false if unset.
     */  
    protected void resetOwner(Iterator it, int field, boolean set) {
        if (it == null) {
            // Nothing to do.
            return;
        }
        while (it.hasNext()) {
            Object o = it.next();
            if (o != null) {
                if (o instanceof Map.Entry) {
                    Map.Entry mapEntry = (Map.Entry)o;
                    resetOwner(mapEntry.getKey(), field, set);
                    resetOwner(mapEntry.getValue(), field, set);
                } else {
                    resetOwner(o, field, set);
                }
            }
        }
    }

     /** Restore fields from beforeImage on commit or rollback
      * called by LifeCycle on commit or rollback transition.
      */
    protected void restoreFields() {
        //
        // Only restore if there is a before image
        //
        if (beforeImage != null) {
            myPC.jdoCopyFields(beforeImage, getFieldNums(loadedFields));
        }                         
    }

    /** Clear fields on commit or rollback
     * called by LifeCycle on commit or rollback transition.
     */
    protected void clearFields() {
        if (com.sun.persistence.support.InstanceCallbacks.class.isInstance(myPC)) {
            ((InstanceCallbacks)myPC).jdoPreClear();
        }
        // Unset owner on SCO fields.
        setSCOOwner(false); 

        // CLEAR FIELDS: myPC.clearFields();
        this.fieldManager = hollowFieldManager; // Save for callback in giveXXXField

        expectedProvider = myPC; // Save for verification.
        myPC.jdoReplaceFields(jdoClass.getPersistentNonPrimaryKeyFieldNumbers());
        expectedProvider = null; // No expected request.

        // Mark only the key fields as loaded for Application Identity
        clearLoadedFields();
    }

    /** Disconnect StateManager and PC. Called by LifeCycle when
     * transition to Transient.
     */
    protected void disconnect() {
        deregister();

        if (myPC != null) {
            // myPC will be null if its class cannot be resolved 
            // until fetch.
            // Need to set jdoFlag to READ_WRITE_OK for transient instance.
            jdoFlags = PersistenceCapable.READ_WRITE_OK;
            myPC.jdoReplaceFlags();

            transitionTransient = true;
            myPC.jdoReplaceStateManager(null);
            transitionTransient = false;
        }

        resetRef();
    }

    /** Reset all settings
     */
    protected void reset() {
        if (debugging())
            debug("reset"); // NOI18N

        beforeImage = null;
        flushedImage = null;
        this.txObjectId = this.objectId;

         dirtyFields.andNot(dirtyFields);
         biFields.andNot(biFields);
    }

    /** Refresh object inside of an active transaction as requested by
     * the LifeCycle.
     */  
    protected void refresh() {
        int[] fields = null;
        if (myPC != null) {
            // myPC will be null if its class cannot be resolved 
            // until fetch.
            resetDirtyFields();
            fields = getFieldNums(loadedFields);

            // Mark only the key fields as loaded for Application Identity
            clearLoadedFields();
        }

        fetch (myPM.getStoreManager(), fields);

    }

    /** Load all persistent fields as requested by the LifeCycle.
     */  
    protected void loadUnloaded() {
        // Check if some fields were not loaded yet:
        int[] fields = getUnloaded(jdoClass.getPersistentFieldNumbers(),
            loadedFields);
        if (fields != null && fields.length > 0) {
            StoreManager srm = myPM.getStoreManager();
            fetch (myPM.getStoreManager(), fields);
        }
    }   

    /**
    * Adds this StateManager to all caches
    */
    protected void registerTransactional() {
        myPM.register(this, objectId, true, false);
    }

    /**
    * Adds this StateManager to non-transactional caches
    */
    protected void registerNonTransactional() {
        myPM.register(this, objectId, false, false);
    }

    /**
    * Removes this StateManager from all the caches
    */
    protected void deregister() {
        if (myLC.isPersistent()) {
            myPM.deregister(objectId);
        } else {
            myPM.deregisterTransient(this);
        }
    }

    // For some unknown reason, the @see for jdoPreDelete results in an error
    // message from javadoc.  We don't know why.  We have tried to fix it,
    // and have tried javadoc in JDK 1.3 and 1.4beta1, to no avail.
    /** 
     * @see com.sun.persistence.support.InstanceCallbacks#jdoPreDelete()
     */
    protected void preDelete() {
        if (com.sun.persistence.support.InstanceCallbacks.class.isInstance(myPC)) {
            ((InstanceCallbacks)myPC).jdoPreDelete();
        }
    }

    /** If this class implements InstanceCallbacks, call the jdoPostLoad
     * method. This is done after the default fetch group values have been 
     * loaded from the store.
     */
    protected void postLoad() {
        if (com.sun.persistence.support.InstanceCallbacks.class.isInstance(myPC)) {
            ((InstanceCallbacks)myPC).jdoPostLoad();
        }
    }
    
    /**
     * Called by LifeCycleState when transition persistent instance to the
     * corresponding flushed state.
     */
    protected void markAsFlushed() {
        //
        // reset the current state to the point where we can accept more changes.
        //
        resetDirtyFields();
        myPM.markAsFlushed(this);
    }

    /**
     * Reset beforeImage on refresh or flush
     */
    protected void unsetBeforeImage() {
        if (debugging())
            debug("unsetBeforeImage"); // NOI18N

        beforeImage = null;
        biFields.andNot(biFields);
    }

    /**
     * Create a new beforeImage in an active optimistic transaction or
     * an active datastore transaction with restoreValues flag set to true
     * or for a transient-transactional instance.
     */
    protected void createBeforeImage() {
        if (debugging())
            debug("createBeforeImage: new " + (beforeImage == null) + // NOI18N
                " Tx is active: " + tx.isActive()); // NOI18N

        if (beforeImage == null && tx.isActive() &&
            isBeforeImageRequired()) {

            beforeImage = jdoImplHelper.newInstance (myPCClass, this);
            int[] fields = getFieldNums(loadedFields);
            beforeImage.jdoCopyFields(myPC, fields);
            replaceSCOWithClones(fields);

            biFields.or(loadedFields);
        }
    }

    /**
     * Verifies that this class type is a supported SCO type.
     * @param type Class type to check.
     * @return true if this type is  a supported SCO type.
     */
    protected boolean isSCOType(Class type) {
        return myPM.isSupportedSCOType(type);
    }

    //
    // Private helper methods for state transitions
    //

    /**
     * Replaces SCO instances with clones in the before image
     * to preserve the state.
     * @param fields array of field numbers to process.
     */
    private void replaceSCOWithClones(int[] fields) {
        for (int i = 0; i < fields.length; i++) {
            int field = fields[i];
            JDOField jdoField = jdoClass.getField(field);
            Class type = javaModelFactory.getJavaClass(jdoField.getType());
            if (isSCOType(type)) {
                Object o = fetchObjectField(field);
                if (o == null) {
                    // Nothing to do. 
                    continue;
                }
                Object clone = null;
                if (o instanceof SCO) {
                    clone = ((SCO)o).clone();
                } else {
                    try {
                        java.lang.reflect.Method m = 
                            o.getClass().getMethod("clone", (Class[]) null); // NOI18N
                        if (m != null) {
                            clone = m.invoke(o, (Object[]) null);
                        }
                    } catch (Exception e) {
                        throw new JDOFatalInternalException(
                            msg.msg("EXC_SCONotCloneable", o.getClass().getName())); //NOI18N
                    }
                }
                fieldManager = objectFieldManager;
                fieldManager.storeObjectField(field, clone);
                expectedProvider = beforeImage; // Save for verification.
                beforeImage.jdoReplaceField(field) ; //getFields(field));
                expectedProvider = null; // No expected request.
            }
        }
    }

    /** Returns current value from the Object type field.
     * @param field the field number
     * @return current value as Object.
     */  
    private Object fetchObjectField(int field) {
        this.fieldManager = objectFieldManager;

        expectedProvider = myPC; // Save for verification.
        myPC.jdoProvideField(field);
        expectedProvider = null; // No expected request.

        return fieldManager.fetchObjectField(field);
    }

    /** Transition referenced fields to Persistent at commit
     * (persistence-by-reachability)
     * @param commit true if it is called during commit.
     */  
    private void processReachability(boolean commit) {
       int[] relfields = jdoClass.getPersistentRelationshipFieldNumbers();
       for (int i = 0; i < relfields.length; i++) {
            Object o = fetchObjectField(relfields[i]);
            if (o != null) {
                reachabilityHandler.process(o, myPM, commit);
            } 
        }
    }

    /** Set owner on referenced SCO objects.
     * @param o referenced object.
     * @param field the field number.
     * @param set true if owner field should be set, false if unset.
     */  
    private void resetOwner(Object o, int field, boolean set) {
        if (o == null) {
            return;

        } else if(com.sun.org.apache.jdo.sco.SCO.class.isInstance(o)) {
            resetOwner((SCO)o, field, set);

        } else { // check for elements of Arrays.
            Class c = o.getClass();
            if (c.isArray() && !c.getComponentType().isPrimitive()) {
                resetOwner((Object[])o, field, set);
            } // else do nothing
        }
    }

    /** Set owner on referenced SCO objects.
     * @param sco referenced SCO object.
     * @param field the field number.
     * @param set true if owner field should be set, false if unset.
     */  
    private void resetOwner(SCO sco, int field, boolean set) {
        if (set) {
            sco.setOwner(this, field);
        } else {
            sco.unsetOwner(this, field);
        }

        // Verify elements of an SCO Collection:
        if (com.sun.org.apache.jdo.sco.SCOCollection.class.isInstance(sco)) {
            SCOCollection scoCollection = (SCOCollection)sco;
            if (set) {
                resetOwner(scoCollection.iterator(), field, set);
            } else {
                // if collection is frozen, don't thaw it to unset owner.
                resetOwner(scoCollection.eitherIterator(), field, set);
            }

        // Verify elements and keys of an SCO Map:
        } else if (com.sun.org.apache.jdo.sco.SCOMap.class.isInstance(sco)) {
            SCOMap scoMap = (SCOMap)sco;
            if (set) {
                resetOwner(scoMap.entrySet().iterator(), field, set);
            } else {
                // if map is frozen, don't thaw it to unset owner.
                resetOwner(scoMap.eitherIterator(), field, set);
            }

        } // else do nothing
    }

    /** Load field value if necessary
     * @param field the field number
     */  
    private void loadField (int field) {
        myPM.assertReadAllowed();
        LifeCycleState st = myLC;
        if (debugging())
            debug("loadField " + myPCClass.getName() + "."  // NOI18N
                + getFieldName(field) + " " + myLC); // NOI18N

        try {
            if (!loadedFields.get(field)) {
                StoreManager srm = myPM.getStoreManager();
                fetch (srm, getFields(field));
            }
            myLC = myLC.transitionReadField(this, tx);

        } catch (JDOException e) {
            if (!st.isTransactional() && myLC.isTransactional()) {
                registerNonTransactional();
            }
            myLC = st;
            throw e;
        }
        if (debugging())
            debug("Done: loadField " + myPCClass.getName() + "." +  // NOI18N
                getFieldName(field) + " " + myLC); // NOI18N
    }

    /** Preparation steps for replacingXXXField operation
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     */  
    private void loadingField(Object pc, int field) {
        if (myPC == pc && !inAfterCompletion) {
            if (debugging())
                debug("loadingField " + myPCClass.getName() + "." + // NOI18N
                    getFieldName(field)); 

            loadedFields.set(field);
        }
    }

    /** Preparation steps for setXXXField operation for non-Object type field.
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param fieldManager the FieldManager that handles double-dispatch
     */  
    private void prepareSetField(PersistenceCapable pc, int field, 
        FieldManager fieldManager) {
        // Check if it is attempt to change PrimaryKey value:
        boolean isPrimaryKey =  jdoClass.getField(field).isPrimaryKey();
        if (!allowedChangeApplicationIdentity && isPrimaryKey) {
            throw new JDOUnsupportedOptionException(
                ChangeApplicationIdentityOption);
        }
        prepareSetField1(pc, field);
        prepareSetField2(field, fieldManager);
    }

    /** Preparation steps for setXXXField operation for Object type field.
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @param fieldManager the FieldManager that handles double-dispatch
     * @param currentValue current value of the field.
     * @param newValue the new value of the field.
     */  
    private void prepareSetField(PersistenceCapable pc, int field, 
        FieldManager fieldManager, Object currentValue, Object newValue) {

        boolean present = loadedFields.get(field);
        prepareSetField1(pc, field);

        // now the value is populated, if it was not:
        if (!present) {
            currentValue = fetchObjectField(field);
        }

        boolean replace = (newValue != currentValue);

        // Verify elementType of a new SCO before proceeding further:
        if (replace) {
            assertSCOElementType(newValue, field);
        }

        prepareSetField2(field, fieldManager);

        // Everything is fine, adjust the ownership on SCO objects:
        if (replace) {
            if (currentValue != null &&
                com.sun.org.apache.jdo.sco.SCO.class.isInstance(currentValue)) {
                // Unset owner on SCO instance that is not referenced any more.
                resetOwner(currentValue, field, false);
            }
            if (newValue != null &&
                com.sun.org.apache.jdo.sco.SCO.class.isInstance(newValue)) {
                // This is the first time we set it:
                resetOwner(newValue, field, true);
            }
        }
    }

     /** Verification and load part of the preparation steps for
     * setXXXField operation.
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     */  
    private void prepareSetField1(PersistenceCapable pc, int field) {
        if (myPC != pc) {
            // It is clone that we will disconnect. But before set the
            // field value:
            expectedProvider = pc; // Save for verification.
            pc.jdoReplaceField(field) ; //getFields(field));
            expectedProvider = null; // No expected request.

            disconnectClone(pc);
        }

        // Reload field if necessary:
        if(myLC.isPersistent()) {
            loadField(field);
        }
    }

    /** Transtion write access and replace value step for setXXXField operation.
     * @param field the field number
     * @param fieldManager the FieldManager that handles double-dispatch
     */   
    private void prepareSetField2(int field, FieldManager fieldManager) {
        LifeCycleState st = myLC;

        if (debugging())
            debug("prepareSetField2 " + myLC); // NOI18N

        myLC = myLC.transitionWriteField(this, tx);

        if (debugging())
            debug("prepareSetField2 " + myLC); // NOI18N

        this.fieldManager = fieldManager; // Save for callback in giveXXXField
        expectedProvider = myPC; // Save for verification.
        myPC.jdoReplaceField(field) ; //getFields(field));
        expectedProvider = null; // No expected request.

        dirtyFields.set(field);
    }
    
    /**
     * Update existing beforeImage in a transaction.
     */
    private void updateBeforeImage(int[] fields) {
        //Do NOT create beforeImage here - this is update only.
        if (beforeImage != null) { 
            fields = getUnloaded(fields, biFields);
            if (fields != null && fields.length > 0) {
                if (debugging()) {
                    debug("updateBeforeImage ["); // NOI18N

                    for (int i = 0; i < fields.length; i++) 
                        logger.debug (fields[i] + " "); // NOI18N
                    logger.debug("]"); // NOI18N
                }
                beforeImage.jdoCopyFields(myPC, fields);
                replaceSCOWithClones(fields);
            }
            biFields.or(loadedFields);
        }
    }

    /**
     * Replace field value with tracked SCO.
     * @param sco tracked SCO instance to be replaced.
     * @param field the field number.
     */
    private void replaceSCO(SCO sco, int field) {
        this.fieldManager = objectFieldManager;

        if (sco != null) {
            resetOwner(sco, field, true);

            // Replace the field
            fieldManager.storeObjectField(field, sco);
            expectedProvider = myPC; // Save for verification.
            myPC.jdoReplaceField(field) ; //getFields(field));
            expectedProvider = null; // No expected request.
        }
    }

    /** 
     * Change owner of all SCO fields. 
     *
     * @param set true if owner should be set, false if references 
     * to this SCO instance will be nullified and owner to be set to null.
     */
    private void setSCOOwner(boolean set) {
        Object o = null;
        Class cls = null;
        JDOField jdoField = null;
        int[] nonpkfields = jdoClass.getPersistentNonPrimaryKeyFieldNumbers();

        Throwable[] err = new Throwable[nonpkfields.length];
        int l = 0;
        for (int i = 0; i < nonpkfields.length; i++) {
            int field = nonpkfields[i];

            jdoField = jdoClass.getField(field);
            if (isSCOType(javaModelFactory.getJavaClass(jdoField.getType()))) {
                o = fetchObjectField(field);
                try {
                    if (set) {
                        scoProcessor.assertSCOElementType(o, jdoField);
                    }
                    resetOwner(o, field, set);
                } catch (Throwable e) {
                    err[l++] = e;
                }
            } // else not an Object field - do nothing
        }

        if (l > 0) {
            Throwable[] t = new Throwable[l];
            System.arraycopy(err, 0, t, 0, l);
            throw new JDOUserException(msg.msg(
                "EXC_FailedToProcessAll"), t); // NOI18N
        }
    }

    /** Assert element type of an SCO Collection or key and value types
     * of an SCO Map.
     * @param o Object to be tested.
     * @param field the corresponding field number.
     * @throws JDOUserException if assertion fails.
     */  
    private void assertSCOElementType(Object o, int field) {
        scoProcessor.assertSCOElementType(o, jdoClass.getField(field));
    }

    /** Returns external representation of the transactional object id
     * that can be used by the client
     */
    private Object getTransactionalObjectId () {
        StoreManager srm = myPM.getStoreManager();
        Object oid = null;

        if (flushedImage != null) {
            oid = srm.getExternalObjectId(txObjectId, flushedImage);
        } else if (beforeImage != null) {
            oid = srm.getExternalObjectId(txObjectId, beforeImage);
        } else {
            oid = srm.getExternalObjectId(txObjectId, myPC);
        }
        return oid;
    }

    /** Create beforeImage for all fields - called by transition from TRANSIENT
     * to P_NEW 
     */
    private void createAllBeforeImage() {
        markAllDirty();
        if (debugging())
            debug("createBeforeImage"); // NOI18N

        if (isBeforeImageRequired()) {
            beforeImage = jdoImplHelper.newInstance (myPCClass, this);
            beforeImage.jdoCopyFields(myPC, getFieldNums(loadedFields));
            biFields.or(loadedFields);
        }
    } 

    /** Mark all fields as loaded and dirty - called by transition from 
     * TRANSIENT to P_NEW and T_CLEAN
     */
    private void markAllDirty() {
        for (int i = 0; i < numFields; i++) {
            loadedFields.set(i);
            dirtyFields.set(i); 
        }
    } 

    /** Initialize SM reference in PC and Oid
     */
    private void initializeSM(int newState) {
        myLC = LifeCycleState.getLifeCycleState(newState);
        final StateManagerImpl thisSM = this;

        try {
            if (myLC.isPersistent()) {
                StoreManager srm = myPM.getStoreManager();
                objectId = srm.createObjectId(this, myPM);

                myPM.register(this, objectId, true, true);
                this.txObjectId = this.objectId;

            } else {
                myPM.registerTransient(this);
            }

            // Everything OK so far. Now we can set SM reference in PC 
            // It can be done only after myLC is set to deligate validation
            // to the LC and objectId verified for uniqueness
        AccessController.doPrivileged (
            // Need to have privileges to perform jdoReplaceStateManager.
                new PrivilegedAction () {
                    public Object run () {
                        try {
                            myPC.jdoReplaceStateManager(thisSM);
                            return null;
                        }    
                        catch (SecurityException e) {
                            throw new JDOFatalUserException (msg.msg(
                                "EXC_CannotSetStateManager"), e); // NOI18N
                        }
                    }
                }
            );

        } catch (SecurityException e) {
            throw new JDOUserException(e.getMessage());

        } catch (JDOException e1) {
            if (myPM.getStateManager(objectId, myPCClass) == this) {
                deregister();
            }
            resetRef();
            throw e1;
        }
    }

     /** Clear dirtyFields list on flush
      */ 
    private void resetDirtyFields() {
        // RESOLVE: flushed Oid
        dirtyFields.andNot(dirtyFields);
    }

    /** Reset all references to null
     */
    private void resetRef() {
        if (debugging())
            debug("resetRef"); // NOI18N

        myPC = null;
        myPM = null;
        myLC = null;
        jdoClass = null;
        myPCClass = null;
        beforeImage = null;
        flushedImage = null;
        this.txObjectId = null;
        this.objectId = null;
    }

    /** Desconnects clone instance.
     * @return true if it was clone.
     */
    private boolean disconnectClone(PersistenceCapable pc) {
        if (pc != myPC) {
            // Replace jdoFlags. It will be set to
            // PersistenceCapable.READ_WRITE_OK
            // by replacingFlags().
            pc.jdoReplaceFlags();

            // Reset StateManager:
            pc.jdoReplaceStateManager(null);

            return true;
        }
        return false;
    }

    /** Verifies field provider
     * @throws JDOUserException if provider is not the one expected.
     */
    private boolean verifyProvider(PersistenceCapable pc) {
        if (pc != expectedProvider) {
            if (pc == myPC || pc == beforeImage || pc == flushedImage) {
                throw new JDOFatalInternalException(
                    msg.msg("EXC_WrongProvider")); // NOI18N
            }
            disconnectClone(pc);
            return false;
        }
        return true;
    }

    /** Fetches instance from the data store
     */  
    private void fetch(StoreManager srm) {
        fetch(srm, null);
    }

    /** Fetches specific fields in the instance from the data store 
     */  
    private void fetch(StoreManager srm, int[] fetchFields) {
        srm.fetch(this, fetchFields);
    }

    /**
     * Create a new PC instance with key fields copied from objectId
     */
    private void initializePC(StoreManager srm) {
        initializePCInfo();
        if(srm.isMediationRequiredToCopyOid()) {
            myPC = jdoImplHelper.newInstance (myPCClass, this);
            srm.copyKeyFieldsFromObjectId(this, myPCClass);
        } else {
            myPC = jdoImplHelper.newInstance (myPCClass, this, objectId);
        }
        markPKFieldsAsLoaded();
    }

    /**
    * Helper method to define the list of fields to be loaded
    * together with this field
    */
    private int[] getFields(int field) {
        // RESOLVE: SRM.dosomething(field)
        fieldArr[0] = field;
        return fieldArr;
    }

    /**
    * Helper method to convert not loaded bits to field numbers.
    */
    private int[] getUnloaded(int[] newfields, BitSet set) {
        int l = 0;
        int tmp[] = new int[newfields.length];

        for (int i = 0; i < newfields.length; i++) {
            if (set.get(newfields[i]) == false) {
                tmp[l] = newfields[i];
                l++;
            }
        }

        int[] fields = new int[l];
        System.arraycopy(tmp, 0, fields, 0, l);

        return fields;
    }

    /**
    * Helper method to convert set bits in a BitSet to field numbers.
    */
    private int[] getFieldNums(BitSet bs) {
        int length = 0;
        int tmp[] = new int[numFields];

        for (int i = 0; i < numFields; i++) {
            if (bs.get(i) == true) {
                tmp[length] = i;
                length++;
            }
        }

        int[] fields = new int[length];
        System.arraycopy(tmp, 0, fields, 0, length);

        return fields;
    }

    //
    // ------------ End methods for state transitions ------------
    //

    /** The owning StateManager uses this method to supply the 
     * value of the flags to the PersistenceCapable instance.
     * @param pc the calling PersistenceCapable instance
     * @return the value of jdoFlags to be stored in the
     * PersistenceCapable instance
     */
    public byte replacingFlags(PersistenceCapable pc) {
        if (myPC != pc) {
            // This is clone - set flags as in a transient instance:
            return PersistenceCapable.READ_WRITE_OK;
        }
        return jdoFlags;
    }

    /** Replace the current value of jdoStateManager.
     * This method is called by the PersistenceCapable whenever 
     * jdoReplaceStateManager is called and there is already
     * an owning StateManager.  This is a security precaution
     * to ensure that the owning StateManager is the only
     * source of any change to itself.
     * @param pc the calling PersistenceCapable instance
     * @return the new value for the jdoStateManager
     */ 
    public StateManager replacingStateManager (PersistenceCapable pc,
                                               StateManager sm) {
        if (myLC == null) {
            // This should never happen. LifeCycle is set the first
            // thing on makePersistent and makeTransactional.
            throw new JDOFatalInternalException(msg.msg(
               "EXC_NullLifeCycle")); // NOI18N
        }

        // This is the same PC - not beforeImage or clone.
        if (myPC == pc) {

            // This call back should happen only when LifeCycle is
            // transitioning to Transient
            if (sm == null) { // transitioning to transient...
                if (!transitionTransient) { // we are NOT transitioning to transient
                    throw new JDOFatalInternalException(msg.msg( 
                        "EXC_NotTransitionTransient")); // NOI18N
                }
                return null; // OK.

            } else if (sm == this) { // should not happen
                    throw new JDOFatalInternalException(msg.msg( 
                        "EXC_SameStateManager")); // NOI18N

            } else if (this.myPM == ((StateManagerImpl)sm).myPM) {
                // same PM && sm != null && sm != this
                // This is a race condition when makePersistent or
                // makeTransactional is called on the same PC instance
                // for the same PM. It has been already set to this SM
                // - just disconnect the other one. Return this SM so
                // it won't be replaced. 
                ((StateManagerImpl)sm).disconnect();
                return this;

            } else { // another PM && sm != null && sm != this
                // This is race condition when makePersistent or
                // makeTransactional is called on the same PC instance
                // for different PM
                throw new JDOUserException(msg.msg(
                    "EXC_PersistentInAnotherPersistenceManager"));// NOI18N
            }

        } else if (pc != beforeImage) {
            /* This indicates that the clone is calling the method.
             * Either the clone is trying to become persistent or we told it
             * to become transient.
             */
            if (sm != null) {
                // It is not becoming transient - force it:
                ((StateManagerImpl)sm).disconnect();
                disconnectClone(pc);
            }
            return null;
        }
        return null;
    }
    
    /** Return the PersistenceManager that owns this instance.
     * Called from internal methods. No validation performed.
     * @return the PersistenceManager that owns this instance
     */    
     public PersistenceManagerInternal getPersistenceManager () {
        if (debugging())
            debug("getPersistenceManager " + myPM); // NOI18N

        return myPM;
     }

    /** Return the PersistenceManager that owns this instance as
     * PersistenceManager wrapper. If called by PersistenceManagerImpl, it
     * will perform validation during this call.
     * @param pc the calling PersistenceCapable instance
     * @return the PersistenceManager that owns this instance
     */    
    public PersistenceManager getPersistenceManager (PersistenceCapable pc) {
        if (disconnectClone(pc)) {
            // This was clone. It should be transient now:
            return null;
        } else {
            myPM.hereIsStateManager(this, myPC);
            return myPM.getCurrentWrapper();
        }
    }

    /** Mark the associated PersistenceCapable field dirty.
     * <P> The StateManager will make a copy of the field
     * so it can be restored if needed later, and then mark
     * the field as modified in the current transaction.
     * @param pc the calling PersistenceCapable instance
     * @param fieldName the name of the field
     */    
    public void makeDirty (PersistenceCapable pc, String fieldName) {
        if (debugging())
            debug("makeDirty " + fieldName); // NOI18N

        if (disconnectClone(pc)) {
            // This was clone. It should be transient now:
            return;
        }
        JDOField f = jdoClass.getManagedField(fieldName);
        if (f != null) {
            this.makeDirty(f.getFieldNumber());
        } // else non-managed field - do nothing.
    }

    /** Return the object representing the JDO identity 
     * of the calling instance.  If the JDO identity is being changed in
     * the current transaction, this method returns the identity as of
     * the beginning of the transaction.
     * @param pc the calling PersistenceCapable instance
     * @return the object representing the JDO identity of the calling instance
     */    
    public Object getObjectId (PersistenceCapable pc) {
        if(disconnectClone(pc)) {
            // This was clone. It should be transient now:
            return null;
        }
        return getExternalObjectId();
    }
    
    /** Return the object representing the JDO identity 
     * of the calling instance.  If the JDO identity is being changed in
     * the current transaction, this method returns the current identity as
     * changed in the transaction.
     * @param pc the calling PersistenceCapable instance
     * @return the object representing the JDO identity of the calling instance
     */    
    public Object getTransactionalObjectId (PersistenceCapable pc){
        if (disconnectClone(pc)) {
            // This was clone. It should be transient now:
            return null;
        }
        return getTransactionalObjectId();
    }


    /** Tests whether this object is dirty.
     *   
     * Instances that have been modified, deleted, or newly
     * made persistent in the current transaction return true.
     *   
     *<P>Transient instances return false.
     *<P>
     * @see com.sun.persistence.support.spi.PersistenceCapable#jdoMakeDirty(String fieldName)
     * @param pc the calling PersistenceCapable instance
     * @return true if this instance has been modified in the current
     * transaction.
     */  
    public boolean isDirty(PersistenceCapable pc) {
        if (disconnectClone(pc) || myLC == null)
            return false;

        return myLC.isDirty();
    }

    /** Tests whether this object is transactional.
     *   
     * Instances that respect transaction boundaries return true.
     * These instances include transient instances made transactional
     * as a result of being the target of a makeTransactional method
     * call; newly made persistent or deleted persistent instances;
     * persistent instances read in data store transactions; and
     * persistent instances modified in optimistic transactions.
     *   
     *<P>Transient instances return false.
     *<P>
     * @param pc the calling PersistenceCapable instance
     * @return true if this instance is transactional.
     */  
    public boolean isTransactional(PersistenceCapable pc) { 
        if (disconnectClone(pc) || myLC == null) 
            return false;
 
        return myLC.isTransactional(); 
    } 


    /** Tests whether this object is persistent.
      *   
      * Instances whose state is stored in the data store return true.
      *   
      *<P>Transient instances return false.
      *<P>
      * @see PersistenceManager#makePersistent(Object pc)
      * @param pc the calling PersistenceCapable instance
      * @return true if this instance is persistent.
      */  
    public boolean isPersistent(PersistenceCapable pc) {  
        if (disconnectClone(pc) || myLC == null)  
            return false; 
  
        return myLC.isPersistent();  
    }  

    /** Tests whether this object has been newly made persistent.
      *
      * Instances that have been made persistent in the current transaction
      * return true.
      *
      *<P>Transient instances return false.
      *<P>
      * @see PersistenceManager#makePersistent(Object pc)
      * @param pc the calling PersistenceCapable instance
      * @return true if this instance was made persistent
      * in the current transaction.
      */
    public boolean isNew(PersistenceCapable pc) {
        if (disconnectClone(pc) || myLC == null)
            return false;

        return myLC.isNew();
    }

    /** Tests whether this object has been deleted.
      *
      * Instances that have been deleted in the current transaction return true.
      *
      *<P>Transient instances return false.
      *<P>
      * @see PersistenceManager#deletePersistent(Object pc)
      * @param pc the calling PersistenceCapable instance
      * @return true if this instance was deleted
      * in the current transaction.
      */
    public boolean isDeleted(PersistenceCapable pc) { 
        if (disconnectClone(pc) || myLC == null) 
            return false; 
 
        return myLC.isDeleted(); 
    } 

    /** Guarantee that the serializable transactional and persistent fields
     * are loaded into the instance.  This method is called by the generated
     * or user-written writeObject method prior to serialization of the
     * instance.
     * @param pc the calling PersistenceCapable instance
     */  
    public void preSerialize (PersistenceCapable pc) {
        if (disconnectClone(pc)) {
            // It was clone that we disconnected. Return as
            // for any transient instance.
            return ;
        }

        if (debugging())
            debug("preSerialize " + myLC); // NOI18N

        myLC = myLC.transitionReload(this, tx);

        if (debugging())
            debug("preSerialize " + myLC); // NOI18N

        // Check if some fields were not loaded yet:
        int[] fields = getUnloaded(
            jdoClass.getPersistentSerializableFieldNumbers(), loadedFields);

        // RESOLVE: how to skip java transient fields:
        if (fields != null && fields.length > 0) {
            StoreManager srm = myPM.getStoreManager();
            fetch (myPM.getStoreManager(), fields);
        }
    }

    /** This implementation of isLoaded will always return true.
     * So the getXXXField methods do not ever need to be implemented.
     * @param pc the calling PersistenceCapable instance
     * @param field the field number
     * @return true
     */    
    public boolean isLoaded (PersistenceCapable pc, int field) {
        if (disconnectClone(pc)) {
            // It was clone that we disconnected. Return true as
            // for any transient instance.
            return true;
        }

        loadField(field);

        return loadedFields.get(field);
    }

    //
    // getXXXField methods
    //
    // In the RI, these methods all throw a JDOFatalInternalException, since
    // their implementation is not needed.  See StateManagerImpl.isLoaded
    //

    /**
    * @see com.sun.persistence.support.spi.StateManager#getBooleanField(
    * PersistenceCapable pc, int field, boolean currentValue)
    */
    public boolean getBooleanField(PersistenceCapable pc, int field,
                                   boolean currentValue) {
        notNeededByRI("getBooleanField"); // NOI18N
        return false;
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getCharField(PersistenceCapable
    * pc, int field, char currentValue)
    */
    public char getCharField(PersistenceCapable pc, int field,
                             char currentValue) {
        notNeededByRI("getCharField"); // NOI18N
        return 'a';
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getByteField(
    * PersistenceCapable pc, int field, byte currentValue)
    */
    public byte getByteField(PersistenceCapable pc, int field,
                             byte currentValue) {
        notNeededByRI("getByteField"); // NOI18N
        return 0;
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getShortField(
    * PersistenceCapable pc, int field, short currentValue)
    */
    public short getShortField(PersistenceCapable pc, int field,
                               short currentValue) {
        notNeededByRI("getShortField"); // NOI18N
        return 0;
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getIntField(
    * PersistenceCapable pc, int field, int currentValue)
    */
    public int getIntField(PersistenceCapable pc, int field,
                           int currentValue) {
        notNeededByRI("getIntField"); // NOI18N
        return 0;
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getLongField(
    * PersistenceCapable pc, int field, long currentValue)
    */
    public long getLongField(PersistenceCapable pc, int field,
                             long currentValue) {
        notNeededByRI("getLongField"); // NOI18N
        return 0;
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getFloatField(
    * PersistenceCapable pc, int field, float currentValue)
    */
    public float getFloatField(PersistenceCapable pc, int field,
                               float currentValue) {
        notNeededByRI("getFloatField"); // NOI18N
        return 0.0F;
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getDoubleField(
    * PersistenceCapable pc, int field, double currentValue)
    */
    public double getDoubleField(PersistenceCapable pc, int field,
                                 double currentValue) {
        notNeededByRI("getDoubleField"); // NOI18N
        return 0.0;
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getStringField(
    * PersistenceCapable pc, int field, String currentValue)
    */
    public String getStringField(PersistenceCapable pc, int field,
                                 String currentValue) {
        notNeededByRI("getStringField"); // NOI18N
        return null;
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#getObjectField(
    * PersistenceCapable pc, int field, Object currentValue)
    */
    public Object getObjectField(PersistenceCapable pc, int field,
                                 Object currentValue) {
        notNeededByRI("getObjectField"); // NOI18N
        return null;
    }

    private void notNeededByRI(String s) {
        throw new JDOFatalInternalException(msg.msg(
            "EXC_NotNeededByRI", s)); // NOI18N
    }
    
    //
    // setXXXField methods
    //

    /**
    * @see com.sun.persistence.support.spi.StateManager#setBooleanField(
    * PersistenceCapable pc, int field, boolean currentValue, boolean newValue)
    */
    public void setBooleanField(PersistenceCapable pc, int field,
                                   boolean currentValue, boolean newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeBooleanField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setCharField(
    * PersistenceCapable pc, int field, char currentValue, char newValue)
    */
    public void setCharField(PersistenceCapable pc, int field,
                             char currentValue, char newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeCharField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setByteField(
    * PersistenceCapable pc, int field, byte currentValue, byte newValue)
    */
    public void setByteField(PersistenceCapable pc, int field,
                             byte currentValue, byte newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeByteField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setShortField(
    * PersistenceCapable pc, int field, short currentValue, short newValue)
    */
    public void setShortField(PersistenceCapable pc, int field,
                               short currentValue, short newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeShortField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setIntField(
    * PersistenceCapable pc, int field, int currentValue, int newValue)
    */
    public void setIntField(PersistenceCapable pc, int field,
                           int currentValue, int newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeIntField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setLongField(
    * PersistenceCapable pc, int field, long currentValue, long newValue)
    */
    public void setLongField(PersistenceCapable pc, int field,
                             long currentValue, long newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeLongField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setFloatField(
    * PersistenceCapable pc, int field, float currentValue, float newValue)
    */
    public void setFloatField(PersistenceCapable pc, int field,
                               float currentValue, float newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeFloatField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setDoubleField(
    * PersistenceCapable pc, int field, double currentValue, double newValue)
    */
    public void setDoubleField(PersistenceCapable pc, int field,
                                 double currentValue, double newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeDoubleField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setStringField(
    * PersistenceCapable pc, int field, String currentValue, String newValue)
    */
    public void setStringField(PersistenceCapable pc, int field,
                                 String currentValue, String newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeStringField(field, newValue);
        prepareSetField(pc, field, sfm);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#setObjectField(
    * PersistenceCapable pc, int field, Object currentValue, Object newValue)
    */
    public void setObjectField(PersistenceCapable pc, int field,
                                 Object currentValue, Object newValue) {
        StateFieldManager sfm = new StateFieldManager();
        sfm.storeObjectField(field, newValue);
        prepareSetField(pc, field, sfm, currentValue, newValue);
    }

    
    //
    // providedXXXField methods
    //
    
    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedBooleanField(
    * PersistenceCapable pc, int field, boolean currentValue)
    */    
    public void providedBooleanField(PersistenceCapable pc, int field,
                                     boolean currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeBooleanField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedCharField(
    * PersistenceCapable pc, int field, char currentValue)
    */    
    public void providedCharField(PersistenceCapable pc, int field,
                                  char currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeCharField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedByteField(
    * PersistenceCapable pc, int field, byte currentValue)
    */    
    public void providedByteField(PersistenceCapable pc, int field,
                                  byte currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeByteField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedShortField(
    * PersistenceCapable pc, int field, short currentValue)
    */    
    public void providedShortField(PersistenceCapable pc, int field,
                                   short currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeShortField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedIntField(
    * PersistenceCapable pc, int field, int currentValue)
    */    
    public void providedIntField(PersistenceCapable pc, int field,
                                 int currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeIntField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedLongField(
    * PersistenceCapable pc, int field, long currentValue)
    */    
    public void providedLongField(PersistenceCapable pc, int field,
                                  long currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeLongField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedFloatField(
    * PersistenceCapable pc, int field, float currentValue)
    */    
    public void providedFloatField(PersistenceCapable pc, int field,
                                   float currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeFloatField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedDoubleField(
    * PersistenceCapable pc, int field, double currentValue)
    */    
    public void providedDoubleField(PersistenceCapable pc, int field,
                                    double currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeDoubleField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedStringField(
    * PersistenceCapable pc, int field, String currentValue)
    */    
    public void providedStringField(PersistenceCapable pc, int field,
                                    String currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeStringField(field, currentValue);
        }
    }

    /** 
    * @see com.sun.persistence.support.spi.StateManager#providedObjectField(
    * PersistenceCapable pc, int field, Object currentValue)
    */    
    public void providedObjectField(PersistenceCapable pc, int field,
                                    Object currentValue) {
        if (verifyProvider(pc)) {
            fieldManager.storeObjectField(field, currentValue);
        }
    }

    
    //
    // replacingXXXField methods
    //

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingBooleanField(
    * PersistenceCapable pc, int field)
    */
    public boolean replacingBooleanField(PersistenceCapable pc, int field) {
        loadingField(pc, field); 
        return fieldManager.fetchBooleanField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingCharField(
    * PersistenceCapable pc, int field)
    */
    public char replacingCharField(PersistenceCapable pc, int field) {
        loadingField(pc, field); 
        return fieldManager.fetchCharField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingByteField(
    * PersistenceCapable pc, int field)
    */
    public byte replacingByteField(PersistenceCapable pc, int field) {
        loadingField(pc, field);
        return fieldManager.fetchByteField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingShortField(
    * PersistenceCapable pc, int field)
    */
    public short replacingShortField(PersistenceCapable pc, int field) {
        loadingField(pc, field);
        return fieldManager.fetchShortField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingIntField(
    * PersistenceCapable pc, int field)
    */
    public int replacingIntField(PersistenceCapable pc, int field) {
        loadingField(pc, field);
        return fieldManager.fetchIntField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingLongField(
    * PersistenceCapable pc, int field)
    */
    public long replacingLongField(PersistenceCapable pc, int field) {
        loadingField(pc, field);
        return fieldManager.fetchLongField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingFloatField(
    * PersistenceCapable pc, int field)
    */
    public float replacingFloatField(PersistenceCapable pc, int field) {
        loadingField(pc, field);
        return fieldManager.fetchFloatField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingDoubleField(
    * PersistenceCapable pc, int field)
    */
    public double replacingDoubleField(PersistenceCapable pc, int field) {
        loadingField(pc, field);
        return fieldManager.fetchDoubleField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingStringField(
    * PersistenceCapable pc, int field)
    */
    public String replacingStringField(PersistenceCapable pc, int field) {
        loadingField(pc, field);
        return fieldManager.fetchStringField(field);
    }

    /**
    * @see com.sun.persistence.support.spi.StateManager#replacingObjectField(
    * PersistenceCapable pc, int field)
    */
    public Object replacingObjectField(PersistenceCapable pc, int field) {
        loadingField(pc, field);
        return fieldManager.fetchObjectField(field);
    }

    //
    // Implemention of other StateManagerInternal methods
    //

    /**
     * Returns true if current state is present in the datastore.
     */
    public boolean isStored() {
        return myLC.isStored();
    }

    /**
     * Returns true if current state is flushed.
     */
    public boolean isFlushed() {
        return myLC.isFlushed();
    }

    /**
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#setDependency(
     * Object dependency)
     */
    public Object setDependency(Object dependency) {
        return this.dependency = dependency;
    }

    /**
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#getDependency()
     */
    public Object getDependency() {
        return dependency;
    }
    /**
    * @see com.sun.org.apache.jdo.state.StateManagerInternal#getObject()
    */
    public PersistenceCapable getObject() {
        return myPC;
    }

    /**
    * @see com.sun.org.apache.jdo.state.StateManagerInternal#setObjectId(Object objectId)
    */
    public void setObjectId(Object objectId) {
        myPM.replaceObjectId(this.objectId, objectId);
        this.objectId = objectId;
        // RESOLVE: what will happen if we support PK updates?
        this.txObjectId = objectId;
    }

    /** Return the object representing the JDO identity
    * of the associated instance
    * @return the object representing the JDO identity of the associated
    * instance.
    */
    public Object getInternalObjectId () {
        if (null == objectId) {
            if (debugging())
                debug("getInternalObjectId"); // NOI18N

            StoreManager srm = myPM.getStoreManager();
            objectId = srm.createObjectId(this, myPM);
            txObjectId = this.objectId;
        }
        return objectId;
    }

    /** Returns external representation of the object id that can be used
     * by the client
     */
    public Object getExternalObjectId () {
        StoreManager srm = myPM.getStoreManager();
        return srm.getExternalObjectId(objectId, myPC);
    }

    /**
    * @see com.sun.org.apache.jdo.state.StateManagerInternal#provideField(
    * int fieldNumber,
    * FieldManager fieldManager, boolean identifying)
    */
    // Synchronized to avoid conflicts w.r.t. fieldManager.
    public synchronized void provideField(int fieldNumber,
                                          FieldManager fieldManager,
                                          boolean identifying) {

        this.fieldManager = fieldManager; // Save for callback in giveXXXField
        if (identifying) {
            if (flushedImage != null) {
                expectedProvider = flushedImage; // Save for verification.
                flushedImage.jdoProvideField(fieldNumber);
            } else {
                expectedProvider = beforeImage; // Save for verification.
                beforeImage.jdoProvideField(fieldNumber); 
            }

        } else {
            expectedProvider = myPC; // Save for verification.
            myPC.jdoProvideField(fieldNumber);
        }
        expectedProvider = null; // No expected request.
    }

    /**
    * @see com.sun.org.apache.jdo.state.StateManagerInternal#provideFields(int[] fields,
    * FieldManager fieldManager, boolean identifying)
    */
    // Synchronized to avoid conflicts w.r.t. fieldManager.
    public synchronized void provideFields(int fields[],
                                           FieldManager fieldManager,
                                           boolean identifying) {

        this.fieldManager = fieldManager; // Save for callback in giveXXXField
        if (identifying) {
            if (flushedImage != null) {
                expectedProvider = flushedImage; // Save for verification.
                flushedImage.jdoProvideFields(fields);
            } else {
                expectedProvider = beforeImage; // Save for verification.
                beforeImage.jdoProvideFields(fields); 
            }

        } else {
            expectedProvider = myPC; // Save for verification.
            myPC.jdoProvideFields(fields);
        }
        expectedProvider = null; // No expected request.
    }

    /**
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#replaceFields(int[] fields,
     * FieldManager fieldManager)
     */
     // Synchronized to avoid conflicts w.r.t. fieldManager.
    public synchronized void replaceFields(int[] fields,
                                           FieldManager fieldManager) {
        this.fieldManager = fieldManager; // Save for callback in giveXXXField
        if (fields != null && fields.length > 0) {
            expectedProvider = myPC; // Save for verification.
            myPC.jdoReplaceFields(fields);
            expectedProvider = null; // No expected request.

            // Merge BeforeImage
            updateBeforeImage(fields);
        }

        this.fieldManager = null;
    }
     
    /**
    * For replacing field values in a PC with one that is provided by 
    * the FieldManager. This method does not replace fields that are
    * already loaded, even if their field number are included in the
    * specified field number array.
    * @param fields Indicates which fields should be replaced in the PC.
    * @param fieldManager FieldManager from which the field values should
    * be obtained.
    */
    protected void replaceUnloadedFields(int[] fields,
                                         FieldManager fieldManager) {
        replaceFields(getUnloaded(fields, loadedFields), fieldManager);
    }
    
    /*
     * @see com.sun.org.apache.jdo.state.StateManagerInternal#preStore()
     */
    public void preStore() {
        if (myLC.isDeleted) {
            // Don't call jdoPreStore for deleted instances.
            return;
        } else if (com.sun.persistence.support.InstanceCallbacks.class.isInstance(myPC)) {
            ((InstanceCallbacks)myPC).jdoPreStore();
        }

    }

    /**
     * Tracing method
     * @param msg String to display
     */  
    private void debug(String msg) {
        logger.debug("In StateManagerImpl " + msg); // NOI18N
    }

    /**
     * Verifies if debugging is enabled.
     * @return true if debugging is enabled.
     */
    private boolean debugging() {
        return logger.isDebugEnabled();
    }
}
