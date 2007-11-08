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
 * SCOProcessor.java
 *
 * Created on September 26, 2001, 9:29 AM
 */

package com.sun.org.apache.jdo.impl.state;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.impl.sco.SqlTimestamp;
import com.sun.org.apache.jdo.model.jdo.JDOCollection;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOMap;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.sco.SCOCollection;
import com.sun.org.apache.jdo.sco.SCODate;
import com.sun.org.apache.jdo.sco.SCOMap;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.*;

/**
 * This is the helper class to process SCO-related requests
 * from the StateManager.
 *
 * @author  Marina Vatkina
 * @version 1.0
 */
class SCOProcessor {

    /** The singleton ReachabilityHandler instance. */    
    private static final SCOProcessor singleton = new SCOProcessor();

    /**
     * Map of possible processors.
     */
    private final HashMap processors = new HashMap();
    private final HashMap scoprocessors = new HashMap();

    /**
     * I18N message handler
     */
    private final static I18NHelper msg = 
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.state.Bundle"); // NOI18N

    /** RuntimeJavaModelFactory. */
    private static final RuntimeJavaModelFactory javaModelFactory =
        (RuntimeJavaModelFactory) AccessController.doPrivileged(
            new PrivilegedAction () {
                public Object run () {
                    return RuntimeJavaModelFactory.getInstance();
                }
            }
        );

    /** Constructs a new <code>SCOProcessor</code> without specific
     * parameters. Initializes processors maps.
     */
    private SCOProcessor() { 
        /**  
         * Processors for SCO-related requests.
         */  
        CollectionProcessor _collectionProcessor = new CollectionProcessor();
        MapProcessor _mapProcessor = new MapProcessor();
        DateProcessor _dateProcessor = new DateProcessor();
        
        // Non-SCO mappings.
        processors.put(java.util.Date.class, _dateProcessor);
        processors.put(java.sql.Date.class, _dateProcessor);
        processors.put(java.sql.Time.class, _dateProcessor);
        processors.put(java.sql.Timestamp.class, _dateProcessor);
        
        processors.put(java.util.ArrayList.class, _collectionProcessor);
        processors.put(java.util.Vector.class, _collectionProcessor);
        processors.put(java.util.HashSet.class, _collectionProcessor);
        processors.put(java.util.LinkedList.class, _collectionProcessor);
        processors.put(java.util.TreeSet.class, _collectionProcessor);
        
        processors.put(java.util.HashMap.class, _mapProcessor);
        processors.put(java.util.Hashtable.class, _mapProcessor);
        processors.put(java.util.TreeMap.class, _mapProcessor);
        
        // SCO mappings.
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.Date.class, _dateProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.SqlDate.class, _dateProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.SqlTime.class, _dateProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.SqlTimestamp.class, _dateProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.ArrayList.class,
                          _collectionProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.Vector.class,
                          _collectionProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.HashSet.class,
                          _collectionProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.LinkedList.class,
                          _collectionProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.TreeSet.class,
                          _collectionProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.HashMap.class, _mapProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.Hashtable.class, _mapProcessor);
        scoprocessors.put(com.sun.org.apache.jdo.impl.sco.TreeMap.class, _mapProcessor);
    }

    /** 
     * Get the SCOProcessor singleton instance.
     * @return an instance of SCOProcessor
     */    
    public static SCOProcessor getInstance()
    {
        return singleton;
    }

    /**
     * Process requests to trackUpdates for SCO changes.
     * @param sm - StateManagerImpl instance that requested processing.
     * @param field the field number associated with this SCO.
     * @param sco Object to process.
     */
    void trackUpdates (StateManagerImpl sm, int field, SCO sco) {
        Processor p = (Processor) scoprocessors.get(sco.getClass());
        p.trackUpdates(sm, field, sco);
    }

    /**  
     * Process requests to create a tracked SCO instance for the corresponding
     * JDK SCO.
     * @param o Object to be replaced with tracked SCO instance.
     * @param jdoField the JDOField associated with this number.
     * @param pm the PersistenceManagerInternal instance associated with the
     * caller.
     */
    SCO getSCOField(Object o, JDOField jdoField,
        PersistenceManagerInternal pm) {
        if (o != null) {
            Processor p = (Processor) processors.get(o.getClass());
            if (p != null) {
                return p.getSCOField(o, jdoField, pm);
            }
        }
        return null;
    }

   /** Assert element type of an SCO Collection or key and value types
     * of an SCO Map.
     * @param o Object to be tested.
     * @param jdoField the corresponding JDOField element.
     * @throws JDOUserException if assertion fails.
     */
    void assertSCOElementType(Object o, JDOField jdoField) {
        if (o != null) {
            Processor p = (Processor) scoprocessors.get(o.getClass());
            if (p != null) {
                p.assertSCOElementType(o, jdoField);
            }
        }
    }

    /** An abstract class that knows how process SCO-related requests.
     */
    abstract class Processor {
        /** Makes newly added instances to an SCO Collection auto-persistent
         * and fixes ownership on referenced SCO instances.
         * @param sm - StateManagerImpl instance that requested processing.
         * @param field the field number associated with this SCO.
         * @param sco Object to process.
         */
        abstract void trackUpdates(StateManagerImpl sm, int field, SCO sco);

        /**
         * Replace field reference that contain java.util SCO instances
         * with tracked SCOs. No recursion is performed on this operation.
         * @param o Object to be replaced with tracked SCO instance.
         * @param jdoField the JDOField associated with this number.
         * @param pm the PersistenceManagerInternal instance associated with the
         * caller.
         */
        abstract SCO getSCOField(Object o, JDOField jdoField,
                                 PersistenceManagerInternal pm);

        /** Assert element type of an SCO Collection or key and value types 
         * of an SCO Map. 
         * @param o Object to be tested. 
         * @param jdoField the corresponding JDOField element. 
         * @throws JDOUserException if assertion fails. 
         */ 
        abstract void assertSCOElementType(Object o, JDOField jdoField);
    }

    /** Processor for trackUpdates request for SCODate.
     */
    class DateProcessor extends Processor {
        /** Makes newly added instances to an SCO Collection auto-persistent
         * and fixes ownership on referenced SCO instances. No-op for Date.
         * @param sm - StateManagerImpl instance that requested processing.
         * @param field the field number associated with this SCO.
         * @param sco Object to process.
         */
        void trackUpdates(StateManagerImpl sm, int field, SCO sco) {}

        /** 
         * Replace field reference that contain java.util SCO instances 
         * with tracked SCOs. No recursion is performed on this operation. 
         * @param o Object to be replaced with tracked SCO instance. 
         * @param jdoField the JDOField associated with this number.
         * @param pm the PersistenceManagerInternal instance associated with the
         * caller.
         */
        SCO getSCOField(Object o, JDOField jdoField,
                        PersistenceManagerInternal pm) {
            SCODate sco = (SCODate)pm.newSCOInstanceInternal(o.getClass());
            sco.setTimeInternal(((Date)o).getTime());
            if (java.sql.Timestamp.class.isInstance(o)) {
                int n = ((java.sql.Timestamp)o).getNanos();
                ((SqlTimestamp)sco).setNanosInternal(n);
            }
            return sco;
        }

        /** Assert element type of an SCO Collection or key and value types 
         * of an SCO Map. No-op for SCODate.
         * @param o Object to be tested.  
         * @param jdoField the corresponding JDOField element.  
         * @throws JDOUserException if assertion fails.  
         */
        void assertSCOElementType(Object o, JDOField jdoField) {}
    }

    /** Processor for trackUpdates request for SCOCollection.
     */
    class CollectionProcessor extends Processor {

        /** Makes newly added instances to an SCO Collection auto-persistent
         * and fixes ownership on referenced SCO instances.
         * @param sm - StateManagerImpl instance that requested processing.
         * @param field the field number associated with this SCO.
         * @param sco Object to process.
         */  
        void trackUpdates(StateManagerImpl sm, int field, SCO sco) {
            // We are interested in the added list for possible autoPersistence
            // transitions. Both added and removed lists are verified if owners
            // of referenced SCO instances should be fixed.
            SCOCollection c = (SCOCollection)sco;
            Collection added = c.getAdded();
            Collection removed = c.getRemoved();

            if (added != null) {
                sm.makeAutoPersistent(added.toArray());
                sm.resetOwner(added.toArray(), field, true);
            }
            if (removed != null) {
                sm.resetOwner(removed.toArray(), field, false);
            }
            // Clear added and removed lists
            c.reset();
        }    

        /**  
         * Replace field reference that contain java.util SCO instances  
         * with tracked SCOs. No recursion is performed on this operation.  
         * @param o Object to be replaced with tracked SCO instance.  
         * @param jdoField the JDOField associated with this number. 
         * @param pm the PersistenceManagerInternal instance associated with the
         * caller.
         */ 
        SCO getSCOField(Object o, JDOField jdoField,
                        PersistenceManagerInternal pm) {
            Comparator cr = null;
            Class el = null;
            boolean allowNulls = true;

            Collection c = (Collection)o;
            JDORelationship rl = jdoField.getRelationship();
            if (rl != null && rl.isJDOCollection()) {
                el = javaModelFactory.getJavaClass(((JDOCollection)rl).
                                                   getElementType());
            }

            // If it is a sorted set check for comparator:
            if (java.util.SortedSet.class.isInstance(c)) {
                cr = ((SortedSet)c).comparator();
            }

            // RESOLVE: allowNulls...
            return (SCO) pm.newCollectionInstanceInternal(
                 c.getClass(), ((el == null)? Object.class : el), allowNulls,
                 new Integer(c.size()), null, c, cr);
        }

        /** Assert element type of an SCO Collection.
         * @param o Object to be tested.   
         * @param jdoField the corresponding JDOField element.   
         * @throws JDOUserException if assertion fails.   
         */ 
        void assertSCOElementType(Object o, JDOField jdoField) {
            Class c = ((SCOCollection)o).getElementType();

            Class el = null;
            JDORelationship rl = jdoField.getRelationship(); 
            if (rl != null && rl.isJDOCollection()) { 
                el = javaModelFactory.getJavaClass(((JDOCollection)rl).
                                                   getElementType()); 
            }

            if (el != null && !(el.isAssignableFrom(c))) {
                throw new JDOUserException(msg.msg(
                   "EXC_WrongElementType",  // NOI18N
                   c.getName(), el.getName()));
            }
        }
    }    

    /** Processor for trackUpdates request for SCOMap.
     */ 
    class MapProcessor extends Processor {

        /** Makes newly added instances to an SCO Map auto-persistent
         * and fixes ownership on referenced SCO instances.
         * @param sm - StateManagerImpl instance that requested processing.
         * @param field the field number associated with this SCO.
         * @param sco Object to process.
         */
        void trackUpdates(StateManagerImpl sm, int field, SCO sco) {
            // We are interested in the added lists for possible autoPersistence
            // transitions. Both added and removed lists are verified if owners
            // of referenced SCO instances should be fixed for keys and values.
            SCOMap m = (SCOMap)sco;
            Collection addedKeys = m.getAddedKeys();
            Collection addedValues = m.getAddedValues();
            Collection removedKeys = m.getRemovedKeys();
            Collection removedValues = m.getRemovedValues();

            if (addedKeys != null) {
                sm.makeAutoPersistent(addedKeys.toArray());
                sm.resetOwner(addedKeys.toArray(), field, true);
            }
            if (addedValues != null) {
                sm.makeAutoPersistent(addedValues.toArray());
                sm.resetOwner(addedValues.toArray(), field, true);
            }
            if (removedKeys != null) {
                sm.resetOwner(removedKeys.toArray(), field, false);
            }
            if (removedValues != null) {
                sm.resetOwner(removedValues.toArray(), field, false);
            }
            // Clear added and removed lists
            m.reset();
        }

        /**   
         * Replace field reference that contain java.util SCO instances   
         * with tracked SCOs. No recursion is performed on this operation.   
         * @param o Object to be replaced with tracked SCO instance.   
         * @param jdoField the JDOField associated with this number.  
         * @param pm the PersistenceManagerInternal instance associated with the
         * caller.
         */  
        SCO getSCOField(Object o, JDOField jdoField,
                        PersistenceManagerInternal pm) {
            Comparator cr = null;
            boolean allowNulls = true;

            Map m = (Map)o;

            // If it is a sorted set check for comparator:
            if (java.util.SortedMap.class.isInstance(m)) {
                cr = ((SortedMap)m).comparator();
            }

            // Key/value types:
            Class el = null;
            Class k = null;
            JDORelationship rl = jdoField.getRelationship();
            if (rl != null && rl.isJDOMap()) {
                el = javaModelFactory.getJavaClass(((JDOMap)rl).getValueType());
                k =  javaModelFactory.getJavaClass(((JDOMap)rl).getKeyType());
            }

            // RESOLVE: allowNulls...
            return (SCOMap) pm.newMapInstanceInternal(
                m.getClass(), ((k == null)? Object.class :k),
                ((el == null)? Object.class : el), allowNulls,
                new Integer(m.size()), null, m, cr);
        }

        /** Assert key and value type of an SCO Map.
         * @param o Object to be tested.    
         * @param jdoField the corresponding JDOField element.    
         * @throws JDOUserException if assertion fails.    
         */  
        void assertSCOElementType(Object o, JDOField jdoField) { 
            SCOMap m = (SCOMap)o;
            JDORelationship rl = jdoField.getRelationship();
            if (rl != null && rl.isJDOMap()) {
                JDOMap rm = (JDOMap) rl;

                Class c = m.getValueType();
                Class el = javaModelFactory.getJavaClass(rm.getValueType());

                if (el != null && !(el.isAssignableFrom(c))) {
                    throw new JDOUserException(msg.msg(
                       "EXC_WrongValueType",  // NOI18N
                       c.getName(), el.getName()));
                }

                c = m.getKeyType();
                el = javaModelFactory.getJavaClass(rm.getKeyType());

                if (el != null && !(el.isAssignableFrom(c))) {
                    throw new JDOUserException(msg.msg(
                       "EXC_WrongKeyType",  // NOI18N
                       c.getName(), el.getName()));
                }
            }
        }
 
    }
}
