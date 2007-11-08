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

package com.sun.persistence.support.spi;

import com.sun.persistence.support.JDOHelper;
import com.sun.persistence.support.PersistenceManager;

/**
 *
 * @author Craig Russell
 * @version 1.0
 */

/**
 * A class that can be managed by a JDO implementation must implement this interface.
 *
 * <P>Every class whose instances can be managed by a JDO PersistenceManager must
 * implement the PersistenceCapable interface.
 *
 * <P>This interface defines methods that allow the implementation to manage
 * the instances.  It also defines methods that allow a JDO aware
 * application to examine the runtime state of instances.  For example,
 * an application can discover whether the instance is persistent, transactional,
 * dirty, new, or deleted; and to get its associated
 * PersistenceManager if it has one.
 *
 * <P>In the Reference Implementation, the JDO Enhancer modifies the class
 * to implement PersistenceCapable prior to loading the class into the runtime
 * environment.  The Reference Enhancer also adds code to implement the
 * methods defined by PersistenceCapable.
 *
 *<P>The extra methods in the PersistenceCapable interface might be generated
 * by pre-processing a .java file, or might be generated from a tool directly.
 * The exact technique for generating the extra methods is not specified by
 * JDO.
 *
 * <P>The PersistenceCapable interface is designed to avoid name conflicts
 * in the scope of user-defined classes.  All of its declared method
 * names are prefixed with 'jdo'.
 */
public interface PersistenceCapable {
    /** If jdoFlags is set to READ_WRITE_OK, then the fields in the default fetch group
     * can be accessed for read or write without notifying the StateManager.
     */
    static final byte READ_WRITE_OK = 0;
    
    /** If jdoFlags is set to LOAD_REQUIRED, then the fields in the default fetch group
     * cannot be accessed for read or write without notifying the StateManager.
     */
    static final byte LOAD_REQUIRED = 1;
    
    /** If jdoFlags is set to READ_OK, then the fields in the default fetch group
     * can be accessed for read without notifying the StateManager.
     */
    static final byte READ_OK = -1;
    
    /** If jdoFieldFlags for a field includes CHECK_READ, then
     * the field has been enhanced to call the jdoStateManager on read
     * if the jdoFlags setting is not READ_OK or READ_WRITE_OK.
     */
    static final byte CHECK_READ = 1;
    
    /** If jdoFieldFlags for a field includes MEDIATE_READ, then
     * the field has been enhanced to always call the jdoStateManager
     * on all reads.
     */
    static final byte MEDIATE_READ = 2;
    
    /** If jdoFieldFlags for a field includes CHECK_WRITE,
     * then the field has been enhanced to call the
     * jdoStateManager on write if the jdoFlags setting is not
     * READ_WRITE_OK;.
     */
    static final byte CHECK_WRITE = 4;
    
    /** If jdoFieldFlags for a field includes MEDIATE_WRITE, then
     * the field has been enhanced to always call the jdoStateManager
     * on all writes.
     */
    static final byte MEDIATE_WRITE = 8;
    
    /** If jdoFieldFlags for a field includes SERIALIZABLE,
     * then the field is not declared as TRANSIENT.
     */
    static final byte SERIALIZABLE = 16;
    
    /** Return the associated PersistenceManager if there is one.
     * Transactional and persistent instances return the associated
     * PersistenceManager.
     *
     * <P>Transient non-transactional instances return null.
     * <P>This method always delegates to the StateManager if it is non-null.
     * @return the PersistenceManager associated with this instance.
     */
    PersistenceManager jdoGetPersistenceManager();
    
    /** This method sets the StateManager instance that manages the state
     * of this instance. This method is normally used by the StateManager
     * during the process of making an instance persistent, transient,
     * or transactional.
     *
     * The caller of this method must have JDOPermission for the instance,
     * if the instance is not already owned by a StateManager.
     * If the parameter is null, and the StateManager approves the change,
     * then the jdoFlags field will be reset to READ_WRITE_OK.
     * If the parameter is not null, and the security manager approves
     * the change, then the jdoFlags field will be reset to LOAD_REQUIRED.
     * @param sm The StateManager which will own this instance, or null
     * to reset the instance to transient state
     * @throws SecurityException if the caller does not have JDOPermission
     * @see JDOPermission
     */
    void jdoReplaceStateManager(StateManager sm)
    throws SecurityException;
    
    /** The owning StateManager uses this method to ask the instance to
     * provide the value of the single field identified by fieldNumber.
     * @param fieldNumber the field whose value is to be provided by
     * a callback to the StateManager's
     * providedXXXField method
     */
    void jdoProvideField(int fieldNumber);
    
    /** The owning StateManager uses this method to ask the instance to
     * provide the values of the multiple fields identified by fieldNumbers.
     * @param fieldNumbers the fields whose values are to be provided by
     * multiple callbacks to the StateManager's
     * providedXXXField method
     */
    void jdoProvideFields(int[] fieldNumbers);
    
    /** The owning StateManager uses this method to ask the instance to
     * replace the value of the single field identified by number.
     * @param fieldNumber the field whose value is to be replaced by
     * a callback to the StateManager's
     * replacingXXXField method
     */
    void jdoReplaceField(int fieldNumber);
    
    /** The owning StateManager uses this method to ask the instance to
     * replace the values of the multiple fields identified by number.
     * @param fieldNumbers the fields whose values are to be replaced by
     * multiple callbacks to the StateManager's
     * replacingXXXField method
     */
    void jdoReplaceFields(int[] fieldNumbers);
    
    /** The owning StateManager uses this method to ask the instance to
     * replace the value of the flags by calling back the StateManager
     * replacingFlags method.
     */
    void jdoReplaceFlags();
    
    /** Copy field values from another instance of the same class
     * to this instance.
     *<P>This method will throw an exception if the other instance is
     * not managed by the same StateManager as this instance.
     * @param other the PC instance from which field values are to be copied
     * @param fieldNumbers the field numbers to be copied into this instance
     */
    void jdoCopyFields(Object other, int[] fieldNumbers);
    
    /** Explicitly mark this instance and this field dirty.
     * Normally, PersistenceCapable classes are able to detect changes made
     * to their fields.  However, if a reference to an array is given to a
     * method outside the class, and the array is modified, then the
     * persistent instance is not aware of the change.  This API allows the
     * application to notify the instance that a change was made to a field.
     *
     *<P>The field name should be the fully qualified name, including package
     * name and class name of the class declaring the field.  This allows
     * unambiguous identification of the field to be marked dirty.
     * If multiple classes declare the same field, and
     * if the package and class name are not provided by the parameter in
     * this API, then the field marked
     * dirty is the field declared by the most derived class.
     * <P>Transient instances ignore this method.
     *<P>
     * @param fieldName the name of the field to be marked dirty.
     */
    void jdoMakeDirty(String fieldName);
    
    /** Return a copy of the JDO identity associated with this instance.
     *
     * <P>Persistent instances of PersistenceCapable classes have a JDO identity
     * managed by the PersistenceManager.  This method returns a copy of the
     * ObjectId that represents the JDO identity.
     *
     * <P>Transient instances return null.
     *
     * <P>The ObjectId may be serialized
     * and later restored, and used with a PersistenceManager from the same JDO
     * implementation to locate a persistent instance with the same data store
     * identity.
     *
     * <P>If the JDO identity is managed by the application, then the ObjectId may
     * be used with a PersistenceManager from any JDO implementation that supports
     * the PersistenceCapable class.
     *
     * <P>If the JDO identity is not managed by the application or the data store,
     * then the ObjectId returned is only valid within the current transaction.
     * <P>If the JDO identity is being changed in the transaction, this method
     * returns the object id as of the beginning of the current transaction.
     *
     * @see PersistenceManager#getObjectId(Object pc)
     * @see PersistenceManager#getObjectById(Object oid, boolean validate)
     * @return a copy of the ObjectId of this instance as of the beginning of the transaction.
     */
    Object jdoGetObjectId();
    
    /** Return a copy of the JDO identity associated with this instance.
     * This method is the same as jdoGetObjectId if the identity of the
     * instance has not changed in the current transaction.
     * <P>If the JDO identity is being changed in the transaction, this method
     * returns the current object id as modified in the current transaction.
     *
     * @see #jdoGetObjectId()
     * @see PersistenceManager#getObjectId(Object pc)
     * @see PersistenceManager#getObjectById(Object oid, boolean validate)
     * @return a copy of the ObjectId of this instance as modified in the transaction.
     */
    Object jdoGetTransactionalObjectId();
    
    /** Tests whether this object is dirty.
     *
     * Instances that have been modified, deleted, or newly
     * made persistent in the current transaction return true.
     *
     *<P>Transient instances return false.
     *<P>
     * @see JDOHelper#isDirty(Object pc)
     * @see JDOHelper#makeDirty(Object pc, String fieldName)
     * @see #jdoMakeDirty(String fieldName)
     * @return true if this instance has been modified in the current transaction.
     */
    boolean jdoIsDirty();
    
    /** Tests whether this object is transactional.
     *
     * Instances whose state is associated with the current transaction
     * return true.
     *
     *<P>Transient instances return false.
     *<P>
     * @see JDOHelper#isTransactional(Object pc)
     * @see PersistenceManager#makeTransactional(Object pc)
     * @return true if this instance is transactional.
     */
    boolean jdoIsTransactional();
    
    /** Tests whether this object is persistent.
     * Instances that represent persistent objects in the data store
     * return true.
     * @see JDOHelper#isPersistent(Object pc)
     * @see PersistenceManager#makePersistent(Object pc)
     * @return true if this instance is persistent.
     */
    boolean jdoIsPersistent();
    
    /** Tests whether this object has been newly made persistent.
     *
     * Instances that have been made persistent in the current transaction
     * return true.
     *
     *<P>Transient instances return false.
     *<P>
     * @see JDOHelper#isNew(Object pc)
     * @see PersistenceManager#makePersistent(Object pc)
     * @return true if this instance was made persistent
     * in the current transaction.
     */
    boolean jdoIsNew();
    
    /** Tests whether this object has been deleted.
     *
     * Instances that have been deleted in the current transaction return true.
     *
     *<P>Transient instances return false.
     *<P>
     * @see JDOHelper#isDeleted(Object pc)
     * @see PersistenceManager#deletePersistent(Object pc)
     * @return true if this instance was deleted
     * in the current transaction.
     */
    boolean jdoIsDeleted();
    
    /** Return a new instance of this class, with the jdoStateManager set to the
     * parameter, and jdoFlags set to LOAD_REQUIRED.
     * <P>This method is used as a performance optimization as an alternative to
     * using reflection to construct a new instance.  It is used by the
     * JDOImplHelper class method newInstance.
     * @return a new instance of this class.
     * @see JDOImplHelper#newInstance(Class pcClass, StateManager sm)
     * @param sm the StateManager that will own the new instance.
     */
    PersistenceCapable jdoNewInstance(StateManager sm);
    
    /** Return a new instance of this class, with the jdoStateManager set to the
     * parameter, key fields initialized to the values in the oid, and jdoFlags
     * set to LOAD_REQUIRED.
     * <P>This method is used as a performance optimization as an alternative to
     * using reflection to construct a new instance of a class that uses
     * application identity.  It is used by the
     * JDOImplHelper class method newInstance.
     * @return a new instance of this class.
     * @see JDOImplHelper#newInstance(Class pcClass, StateManager sm)
     * @param sm the StateManager that will own the new instance.
     * @param oid an instance of the object id class (application identity).
     */
    PersistenceCapable jdoNewInstance(StateManager sm, Object oid);
    
    /** Create a new instance of the ObjectId class for this PersistenceCapable class.
     * The fields will have their Java default values.
     * @return the new instance created.
     */
    Object jdoNewObjectIdInstance();
    
    /** Create a new instance of the ObjectId class for this PersistenceCapable
     * class, using the String form of the constructor.
     * The fields will have their Java default values.
     * @return the new instance created.
     * @param str the String form of the object identity instance
     */
    Object jdoNewObjectIdInstance(String str);
    
    

    /** Create a new instance of the ObjectId class for this PersistenceCapable
     * class. If the object id class is one of the SingleFieldIdentity subclasses,
     *      then the argument passed as parameter will have to be one of the following:
     *      a) PersistenceCapable.ObjectIdFieldSupplier. In this case the appropriate
     *          fetchXXXField() on the ObjectIdFieldSupplier will be called to initialize
     *          the object id instance.
     *      b) java.lang.Number. In this case the object id instance class must be either
     *          ByteIdentity OR ShortIdentity OR IntIdentity OR LongIdentity.
     *      c) java.lang.String. In this case, a new instance of the ObjectId class for
     *          this PersistenceCapable class is created using the String form of the constructor.
     *      d) java.lang.Character. In this case, the object id instance class must be CharIdentity.
     *          A new instance of the CharIdentity class is created using the char form of the
     *          constructor. The char value returned by ((Character) obj).charValue() will be
     *          passed as argument to the constructor.
     *
     * If the object id class is not one of the SingleFieldIdentity subclasses,
     *      then the argument passed as parameter must be an instance of
     *      PersistenceCapable.ObjectIdFieldSupplier. In this case a new object id
     *      instance will be created and initialzed using the
     *      jdoCopyKeyFieldsToId(ObjectiIdFieldSupplier ofs, Object obj) method
     *
     * @return the new instance created.
     * @param obj the object that will be used to initialize the object identity instance
     *
     *   The identity instance returned has no relationship with the values of the 
     *   primary key fields of the persistence-capable instance on which the method is called.
     */
    Object jdoNewObjectIdInstance(Object obj);
    
    /** Copy fields from this PersistenceCapable instance to the Object Id instance.
     * @param oid the ObjectId target of the key fields
     */
    void jdoCopyKeyFieldsToObjectId(Object oid);
    
    /** Copy fields from an outside source to the key fields in the ObjectId.
     * This method is generated in the PersistenceCapable class to generate
     * a call to the field manager for each key field in the ObjectId.  For
     * example, an ObjectId class that has three key fields (int id,
     * String name, and Float salary) would have the method generated:
     * <P>void jdoCopyKeyFieldsToObjectId
     * <P>        (ObjectIdFieldSupplier fm, Object objectId) {
     * <P>    EmployeeKey oid = (EmployeeKey)objectId;
     * <P>    oid.id = fm.fetchIntField (0);
     * <P>    oid.name = fm.fetchStringField (1);
     * <P>    oid.salary = fm.fetchObjectField (2);
     * <P>}
     * <P>The implementation is responsible for implementing the
     * ObjectIdFieldSupplier to produce the values for the key fields.
     * @param oid the ObjectId target of the copy.
     * @param fm the field supplier that supplies the field values.
     */
    void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid);
    
    /** Copy fields to an outside source from the key fields in the ObjectId.
     * This method is generated in the PersistenceCapable class to generate
     * a call to the field manager for each key field in the ObjectId.  For
     * example, an ObjectId class that has three key fields (int id,
     * String name, and Float salary) would have the method generated:
     * <P>void copyKeyFieldsFromObjectId
     * <P>        (ObjectIdFieldConsumer fm, Object objectId) {
     * <P>     EmployeeKey oid = (EmployeeKey)objectId;
     * <P>     fm.storeIntField (0, oid.id);
     * <P>     fm.storeStringField (1, oid.name);
     * <P>     fm.storeObjectField (2, oid.salary);
     * <P>}
     * <P>The implementation is responsible for implementing the
     * ObjectIdFieldManager to store the values for the key fields.
     * @param oid the ObjectId source of the copy.
     * @param fm the field manager that receives the field values.
     */
    void jdoCopyKeyFieldsFromObjectId(ObjectIdFieldConsumer fm, Object oid);
    
    /** This interface is a convenience interface that allows an instance to
     * implement both ObjectIdFieldSupplier and ObjectIdFieldConsumer.
     */
    static interface ObjectIdFieldManager extends ObjectIdFieldConsumer, ObjectIdFieldSupplier {}
    
    /** This interface is used to provide fields to the Object id instance.  It is used
     * by the method copyKeyFieldsToObjectId.  When the method is called, the
     * generated code calls the instance of ObjectIdFieldManager for each field in
     * the object id.
     */
    static interface ObjectIdFieldSupplier {
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        boolean fetchBooleanField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        char fetchCharField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        byte fetchByteField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        short fetchShortField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        int fetchIntField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        long fetchLongField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        float fetchFloatField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        double fetchDoubleField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        String fetchStringField(int fieldNumber);
        
        /** Fetch one field from the field manager.  This field will be stored in the
         * proper field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @return the value of the field to be stored into the ObjectId.
         */
        Object fetchObjectField(int fieldNumber);
    }
    
    /** This interface is used to store fields from the Object id instance.  It is used
     * by the method copyKeyFieldsFromObjectId.  When the method is called, the
     * generated code calls the instance of ObjectIdFieldManager for each field in
     * the object id.
     */
    static interface ObjectIdFieldConsumer {
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeBooleanField(int fieldNumber, boolean value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeCharField(int fieldNumber, char value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeByteField(int fieldNumber, byte value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeShortField(int fieldNumber, short value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeIntField(int fieldNumber, int value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeLongField(int fieldNumber, long value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeFloatField(int fieldNumber, float value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeDoubleField(int fieldNumber, double value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeStringField(int fieldNumber, String value);
        
        /** Store one field into the field manager.  This field was retrieved from
         * the field of the ObjectId.
         * @param fieldNumber the field number of the key field.
         * @param value the value of the field from the ObjectId.
         */
        void storeObjectField(int fieldNumber, Object value);
    }
}
