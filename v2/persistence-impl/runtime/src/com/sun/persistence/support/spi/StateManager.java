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

import com.sun.persistence.support.PersistenceManager;

/** This interface is the point of contact between managed instances of
 * <code>PersistenceCapable</code> classes and the JDO implementation.  It contains
 * the methods used by <code>PersistenceCapable</code> instances to delegate behavior to 
 * the JDO implementation.
 *<P>Each managed <code>PersistenceCapable</code> instance contains a reference to a
 * <code>StateManager</code>.  A <code>StateManager</code> might manage one or multiple instances of
 * <code>PersistenceCapable</code> instances, at the choice of the implementation.
 *
 * @author  Craig Russell
 * @version 1.0
 *
 */
public interface StateManager {
    
    /** The owning <code>StateManager</code> uses this method to supply the 
     * value of the flags to the <code>PersistenceCapable</code> instance.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return the value of <code>jdoFlags</code> to be stored in the <code>PersistenceCapable</code> instance
     */
    byte replacingFlags(PersistenceCapable pc);

    /** Replace the current value of <code>jdoStateManager</code>.
     * <P>
     * This method is called by the <code>PersistenceCapable</code> whenever
     * <code>jdoReplaceStateManager</code> is called and there is already
     * an owning <code>StateManager</code>.  This is a security precaution
     * to ensure that the owning <code>StateManager</code> is the only
     * source of any change to its reference in the <code>PersistenceCapable</code>.
     * @return the new value for the <code>jdoStateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param sm the proposed new value for the <code>jdoStateManager</code>
     */ 
    StateManager replacingStateManager (PersistenceCapable pc, StateManager sm);
    
    /** Tests whether this object is dirty.
     *
     * Instances that have been modified, deleted, or newly 
     * made persistent in the current transaction return <code>true</code>.
     *
     *<P>Transient nontransactional instances return <code>false</code>.
     *<P>
     * @see PersistenceCapable#jdoMakeDirty(String fieldName)
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return <code>true</code> if this instance has been modified in the current transaction.
     */
    boolean isDirty(PersistenceCapable pc);

    /** Tests whether this object is transactional.
     *
     * Instances that respect transaction boundaries return <code>true</code>.  These instances
     * include transient instances made transactional as a result of being the
     * target of a <code>makeTransactional</code> method call; newly made persistent or deleted
     * persistent instances; persistent instances read in data store
     * transactions; and persistent instances modified in optimistic transactions.
     *
     *<P>Transient nontransactional instances return <code>false</code>.
     *<P>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return <code>true</code> if this instance is transactional.
     */
    boolean isTransactional(PersistenceCapable pc);

    /** Tests whether this object is persistent.
     *
     * Instances whose state is stored in the data store return <code>true</code>.
     *
     *<P>Transient instances return <code>false</code>.
     *<P>
     * @see PersistenceManager#makePersistent(Object pc)
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return <code>true</code> if this instance is persistent.
     */
    boolean isPersistent(PersistenceCapable pc);

    /** Tests whether this object has been newly made persistent.
     *
     * Instances that have been made persistent in the current transaction 
     * return <code>true</code>.
     *
     *<P>Transient instances return <code>false</code>.
     *<P>
     * @see PersistenceManager#makePersistent(Object pc)
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return <code>true</code> if this instance was made persistent
     * in the current transaction.
     */
    boolean isNew(PersistenceCapable pc);

    /** Tests whether this object has been deleted.
     *
     * Instances that have been deleted in the current transaction return <code>true</code>.
     *
     *<P>Transient instances return <code>false</code>.
     *<P>
     * @see PersistenceManager#deletePersistent(Object pc)
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return <code>true</code> if this instance was deleted
     * in the current transaction.
     */
    boolean isDeleted(PersistenceCapable pc);
    
    /** Return the <code>PersistenceManager</code> that owns this instance.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return the <code>PersistenceManager</code> that owns this instance
     */
    PersistenceManager getPersistenceManager (PersistenceCapable pc);
    
    /** Mark the associated <code>PersistenceCapable</code> field dirty.
     * <P>The <code>StateManager</code> will make a copy of the field
     * so it can be restored if needed later, and then mark
     * the field as modified in the current transaction.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param fieldName the name of the field
     */    
    void makeDirty (PersistenceCapable pc, String fieldName);
    
    /** Return the object representing the JDO identity 
     * of the calling instance.  If the JDO identity is being changed in
     * the current transaction, this method returns the identity as of
     * the beginning of the transaction.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return the object representing the JDO identity of the calling instance
     */    
    Object getObjectId (PersistenceCapable pc);

    /** Return the object representing the JDO identity 
     * of the calling instance.  If the JDO identity is being changed in
     * the current transaction, this method returns the current identity as
     * changed in the transaction.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @return the object representing the JDO identity of the calling instance
     */    
    Object getTransactionalObjectId (PersistenceCapable pc);

    /** Return <code>true</code> if the field is cached in the calling
     * instance.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @return whether the field is cached in the calling instance
     */    
    boolean isLoaded (PersistenceCapable pc, int field);
    
    /** Guarantee that the serializable transactional and persistent fields
     * are loaded into the instance.  This method is called by the generated
     * <code>jdoPreSerialize</code> method prior to serialization of the
     * instance.
     * @param pc the calling <code>PersistenceCapable</code> instance
     */    
    void preSerialize (PersistenceCapable pc);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    boolean getBooleanField (PersistenceCapable pc, int field, boolean currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    char getCharField (PersistenceCapable pc, int field, char currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    byte getByteField (PersistenceCapable pc, int field, byte currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    short getShortField (PersistenceCapable pc, int field, short currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    int getIntField (PersistenceCapable pc, int field, int currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    long getLongField (PersistenceCapable pc, int field, long currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    float getFloatField (PersistenceCapable pc, int field, float currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    double getDoubleField (PersistenceCapable pc, int field, double currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    String getStringField (PersistenceCapable pc, int field, String currentValue);
    
    /** Return the value for the field.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     * @return the new value for the field
     */    
    Object getObjectField (PersistenceCapable pc, int field, Object currentValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setBooleanField (PersistenceCapable pc, int field, boolean currentValue, boolean newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setCharField (PersistenceCapable pc, int field, char currentValue, char newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setByteField (PersistenceCapable pc, int field, byte currentValue, byte newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setShortField (PersistenceCapable pc, int field, short currentValue, short newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setIntField (PersistenceCapable pc, int field, int currentValue, int newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setLongField (PersistenceCapable pc, int field, long currentValue, long newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setFloatField (PersistenceCapable pc, int field, float currentValue, float newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setDoubleField (PersistenceCapable pc, int field, double currentValue, double newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setStringField (PersistenceCapable pc, int field, String currentValue, String newValue);

    /** Mark the field as modified by the user.
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @param currentValue the current value of the field
     * @param newValue the proposed new value of the field */    
    void setObjectField (PersistenceCapable pc, int field, Object currentValue, Object newValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedBooleanField (PersistenceCapable pc, int field, boolean currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedCharField (PersistenceCapable pc, int field, char currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedByteField (PersistenceCapable pc, int field, byte currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedShortField (PersistenceCapable pc, int field, short currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedIntField (PersistenceCapable pc, int field, int currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedLongField (PersistenceCapable pc, int field, long currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedFloatField (PersistenceCapable pc, int field, float currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedDoubleField (PersistenceCapable pc, int field, double currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedStringField (PersistenceCapable pc, int field, String currentValue);

    /** The value of the field requested to be provided to the <code>StateManager</code>
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @param currentValue the current value of the field
     */    
    void providedObjectField (PersistenceCapable pc, int field, Object currentValue);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number
     * @return the new value for the field
     */    
    boolean replacingBooleanField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    char replacingCharField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    byte replacingByteField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    short replacingShortField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    int replacingIntField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    long replacingLongField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    float replacingFloatField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    double replacingDoubleField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    String replacingStringField (PersistenceCapable pc, int field);

    /** The replacing value of the field in the calling instance
     * @param pc the calling <code>PersistenceCapable</code> instance
     * @param field the field number 
     * @return the new value for the field
     */    
    Object replacingObjectField (PersistenceCapable pc, int field);

}

