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
 * sco.HashMap.java
 */
 
package com.sun.org.apache.jdo.impl.sco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.sco.SCOMap;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.JDOHelper;
import com.sun.persistence.support.JDOUserException;


/**
 * A mutable 2nd class object that represents HashMap.
 * @author Marina Vatkina
 * @version 1.0.1
 * @see java.util.HashMap
 */
public class HashMap extends java.util.HashMap implements SCOMap {

    private transient StateManagerInternal owner;

    private transient int fieldNumber = -1;

    private transient   Class keyType;

    private transient   Class valueType;

    private transient boolean allowNulls;

    private transient java.util.ArrayList addedKeys = new java.util.ArrayList();
    private transient java.util.ArrayList addedValues = new java.util.ArrayList();

    private transient java.util.ArrayList removedKeys = new java.util.ArrayList();
    private transient java.util.ArrayList removedValues = new java.util.ArrayList();

    private transient Map.Entry[] frozenEntries = null;
    
    /** 
     * I18N message handler 
     */ 
    private final static I18NHelper msg = I18NHelper.getInstance( 
        "com.sun.org.apache.jdo.impl.sco.Bundle");  // NOI18N

    private final static String _HashMap = "HashMap"; // NOI18N

    /**   
     * Creates a new empty <code>HashMap</code> object.
     *    
     * @param keyType the type of the keys allowed.
     * @param valueType the type of the values allowed.
     * @param allowNulls true if nulls are allowed.
     * @see java.util.HashMap
     */  
    public HashMap(Class keyType, Class valueType, boolean allowNulls) {
        super(); 
        this.keyType = keyType;
        this.valueType = valueType;
        this.allowNulls = allowNulls;
    } 

    /**   
     * Creates a new empty <code>HashMap</code> object that has 
     * the specified initial capacity.
     *    
     * @param keyType the type of the keys allowed.
     * @param valueType the type of the values allowed.
     * @param allowNulls true if nulls are allowed
     * @param      initialCapacity   the initial capacity of the hash map. 
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero.
     * @see java.util.HashMap
     */  
    public HashMap(Class keyType, Class valueType, boolean allowNulls, 
        int initialCapacity) {

        super(initialCapacity); 
        this.keyType = keyType;
        this.valueType = valueType;
        this.allowNulls = allowNulls;
    } 

    /**   
     * Creates a new empty <code>HashMap</code> object that has 
     * the specified initial capacity..
     *    
     * @param keyType the type of the keys allowed.
     * @param valueType the type of the values allowed.
     * @param allowNulls true if nulls are allowed
     * @param      initialCapacity   the initial capacity of the hash map. 
     * @param      loadFactor        the load factor of the hash map.
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero.
     * @see java.util.HashMap
     */  
    public HashMap(Class keyType, Class valueType, boolean allowNulls, 
        int initialCapacity, float loadFactor) {

        super(initialCapacity, loadFactor); 
        this.keyType = keyType;
        this.valueType = valueType;
        this.allowNulls = allowNulls;
    } 

    // -------------------------Public Methods------------------

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the HashMap previously associated
     *         <tt>null</tt> with the specified key.
     * @see java.util.HashMap
     */
    public Object put(Object key, Object value) {
        SCOHelper.debug(_HashMap, "put"); // NOI18N

        // Check both the key and the value:
        Throwable[] err = new Throwable[2];
        int l = 0;

        try {
            SCOHelper.assertNullKeysAllowed(key, allowNulls);
            SCOHelper.assertKeyType(key, keyType);
        } catch (Throwable ex) {
            err[l++] = ex;
        }
        try {
            SCOHelper.assertNullValuesAllowed(value, allowNulls);
            SCOHelper.assertValueType(value, valueType);
        } catch (Throwable ex) {
            err[l++] = ex;
        }
        SCOHelper.validateResult(l, err);

        // Mark the field as dirty
        this.makeDirty();

        thaw();
        Object o = process(key, value);

        // Apply updates
        this.trackUpdates(true);

        return o;
    }  

    /**
    * Copies all of the mappings from the specified map to this one.
     *
     * These mappings replace any mappings that this map had for any of the
     * keys currently in the specified Map.
     *
     * @param t Mappings to be stored in this map.
     * @see java.util.HashMap
     */  
    public void putAll(Map t) {
        SCOHelper.debug(_HashMap, "putAll"); // NOI18N

        // iterate the collection and make a list of wrong elements.
        Throwable[] err = new Throwable[2*t.size()];
        int l = 0;

        Iterator i = t.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            Object k = e.getKey();
            Object v = e.getValue();

            // Check both the key and the value:
            try {
                SCOHelper.assertNullKeysAllowed(k, allowNulls);
                SCOHelper.assertKeyType(k, keyType);
            } catch (Throwable ex) {
                err[l++] = ex;
            }
            try {
                SCOHelper.assertNullValuesAllowed(v, allowNulls);
                SCOHelper.assertValueType(v, valueType);
            } catch (Throwable ex) {
                err[l++] = ex;
            }
        }
        SCOHelper.validateResult(l, err);

        boolean modified = false;
        // Mark the field as dirty
        this.makeDirty();

        thaw();
        for (i = t.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next(); 
            process(e.getKey(), e.getValue());
        }

        // Apply updates
        this.trackUpdates(true);
    } 

     /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     * @see java.util.HashMap
     */  
    public Object remove(Object key) {
        SCOHelper.debug(_HashMap, "remove"); // NOI18N

        // Mark the field as dirty
        this.makeDirty();

        thaw();
        boolean removed = false;

        // Nothing is added to the removed collections if the key has not
        // been in the map before:
        if (super.containsKey(key)) {
            if (addedKeys.remove(key) == false) {
                removedKeys.add(key);
            }
            removed = true;
        }

        Object o = super.remove(key);

        // Remove old value if there was one:
        if (removed && addedValues.remove(o) == false) {
            removedValues.add(o);
        }

        // Apply updates
        this.trackUpdates(removed);

        return o;
    }    


    /**
     * Removes all of the elements from this map.
     * @see java.util.HashMap
     */  
    public void clear() {
        SCOHelper.debug(_HashMap, "clear"); // NOI18N

        // Mark the field as dirty
        this.makeDirty();

        thaw();
        for (Iterator i = super.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            removedKeys.add(e.getKey());
            removedValues.add(e.getValue());
        }

        super.clear();
        addedKeys.clear();
        addedValues.clear();

        // Apply updates
        this.trackUpdates(true);

    }     
 
    /**
     * Creates and returns a copy of this object.
     *   
     * <P>Mutable Second Class Objects are required to provide a public
     * clone method in order to allow for copying PersistenceCapable
     * objects. In contrast to Object.clone(), this method must not throw a
     * CloneNotSupportedException.
     */  
    public Object clone() {
        SCOHelper.debug(_HashMap, "clone"); // NOI18N

        Object obj = super.clone();
        if (obj instanceof SCO) {
            ((SCO)obj).unsetOwner(owner, fieldNumber);
        }
        return obj;
    }

    /** These methods need to thaw the map before performing the operation.
     */
    public boolean containsKey(Object key) {
        thaw();
        return super.containsKey(key);
    }
    public boolean containsValue(Object value) {
        thaw();
        return super.containsValue(value);
    }
    public Set entrySet() {
        thaw();
        return super.entrySet();
    }
    public boolean equals(Object o) {
        thaw();
        return super.equals(o);
    }
    public Object get(Object key) {
        thaw();
        return super.get(key);
    }
    public int hashCode() {
        thaw();
        return super.hashCode();
    }
    public boolean isEmpty() {
        thaw();
        return super.isEmpty();
    }
    public Set keySet() {
        thaw();
        return super.keySet();
    }
    public int size() {
        if (isFrozen()) {
            return frozenEntries.length;
        } else {
            return super.size();
        }
    }
    public Collection values() {
        thaw();
        return super.values();
    }
    public String toString() {
        thaw();
        return super.toString();
    }
    
    /**
     * @see SCOMap#reset()
     */  
    public void reset() {
        addedKeys.clear();
        addedValues.clear();
        removedKeys.clear();
        removedValues.clear();
    }

    /**
     * @see SCOMap#putInternal(Object key, Object value)
     */  
    public void putInternal(Object key, Object value) {
        super.put(key, value);
    }


    /**
     * @see SCOMap#putAllInternal(Map t)
     */  
    public void putAllInternal(Map t) {
        for (Iterator i = t.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            super.put(e.getKey(), e.getValue());
        }
    }    

    /** 
     * @see SCOMap#getAddedKeys()
     */  
    public Collection getAddedKeys() { 
        return (Collection)addedKeys; 
    }     
 
    /** 
     * @see SCOMap#getAddedValues()
     */  
    public Collection getAddedValues() { 
        return (Collection)addedValues; 
    }     
 
    /**  
     * @see SCOMap#getRemovedKeys()
     */  
    public Collection getRemovedKeys()  {  
        return (Collection)removedKeys;  
    }      

    /**  
     * @see SCOMap#getRemovedValues()
     */  
    public Collection getRemovedValues()  {  
        return (Collection)removedValues;  
    }      

    /** 
     * @see SCOMap#clearInternal()
     */   
    public void clearInternal() {    
        super.clear(); 
        this.reset(); 
    }  

    /**
     * @see SCOMap#removeInternal(Object key)
     */  
    public void removeInternal(Object key) {
        super.remove(key);
    }

    /**
     * @see SCO#unsetOwner(Object owner, int fieldNumber)
     */
    public void unsetOwner(Object owner, int fieldNumber) { 
        // Unset only if owner and fieldNumber match.
        if (this.owner == owner && this.fieldNumber == fieldNumber) {
            this.owner = null; 
            this.fieldNumber = -1;
        }
    }

    /**
     * @see SCO#setOwner (Object owner, int fieldNumber)
     */
    public void setOwner (Object owner, int fieldNumber) {
        // Set only if it was not set before.
        if (this.owner == null && owner instanceof StateManagerInternal) {
            this.owner = (StateManagerInternal)owner;    
            this.fieldNumber = fieldNumber;
        }
    }

    /** 
     * @see SCO#getOwner()
     */   
    public Object getOwner() {   
        return SCOHelper.getOwner(owner);
    } 
 
    /**  
     * @see SCO#getFieldName()
     */   
    public String getFieldName() {
        return SCOHelper.getFieldName(owner, fieldNumber);   
    }

    /**
     * Notify StateManager to mark field as dirty.
     */
    private void makeDirty() {
        if (owner != null) {
            owner.makeDirty(fieldNumber); //
        }
     }   
    /**
     * Notify StateManager to process the changes.
     */  
    private void trackUpdates(boolean modified) {
        if (modified && owner != null) {
               owner.trackUpdates(fieldNumber, this);
        }
    }

    /**
     * @see SCOMap#getKeyType() {
     */  
    public Class getKeyType() {
        return keyType;
    }

    /**
     * @see SCOMap#getValueType() {
     */  
    public Class getValueType() {
        return valueType;
    }

    /**
     * @see SCOMap#allowNulls() {
     */  
    public boolean allowNulls() {
        return allowNulls;
    }

    /**
     * Processes single put operation in this map.
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. 
     */
    private Object process(Object key, Object value) {
        thaw();
        // Key is added to the addedKeys collection only if it has not
        // been in the map before:
        if (!super.containsKey(key)) {
            if (removedKeys.remove(key) == false) {
                addedKeys.add(key);
            }
        }

        Object o = super.put(key, value);

        // Remove old value:
        if (addedValues.remove(o) == false) {
            removedValues.add(o);
        }

        // Add new value:
        if (removedValues.remove(value) == false) {
            addedValues.add(value);
        }

        return o;
    }
    
    /** Returns the frozen state of this Map.
     * @since 1.0.1
     * @return the frozen state.
     */    
    private boolean isFrozen() {
        return frozenEntries != null;
    }
    
    /** Returns the frozen contents of this Map as a Map.Entry[].
     * @return the frozen entries.
     * @since 1.0.1
     */
    private Map.Entry[] getFrozen() {
        if (!isFrozen()) {
            frozenEntries = Freezer.freeze(this, super.size());
        }
        return frozenEntries;
    }
    
    /** Set the contents of this Map from the frozen entries.
     * @since 1.0.1
     * @param entries the frozen entries
     */
    public void setFrozen(Map.Entry[] entries) {
        frozenEntries = entries;
    }
    
    /** Get an iterator regardless of whether the map is frozen.
     * If frozen, get a frozen iterator.
     * If thawed, get a regular iterator.
     * @since 1.0.1
     * @return the iterator.
     */
    public Iterator eitherIterator() {
        if (isFrozen()) {
            return frozenIterator();
        } else {
            return super.entrySet().iterator();
        }
    }
    
    /** Get an iterator over the frozen elements of this map. This allows
     * iteration of the elements without thawing them, as is needed for
     * transcription.
     * @since 1.0.1
     * @return the iterator.
     */
    public Iterator frozenIterator() {
        return Freezer.createFrozenIterator(getFrozen());
    }
    
    /**
     * Thaw the frozen elements of this map. If the elements are frozen,
     * retrieve them from the datastore and internally add them. Then reset
     * the frozen state since they're not frozen any more.
     * @since 1.0.1
     */
    private void thaw() {
        if (isFrozen()) {
            frozenEntries = Freezer.thaw(this, owner, frozenEntries);
        }
    }
    
}
