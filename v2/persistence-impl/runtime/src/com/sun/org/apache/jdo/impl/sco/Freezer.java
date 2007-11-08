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
 * Freezer.java
 *
 */

package com.sun.org.apache.jdo.impl.sco;

import java.io.PrintStream;

import java.lang.Comparable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;



import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.sco.SCOCollection;
import com.sun.org.apache.jdo.sco.SCOMap;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.JDOHelper;
import com.sun.persistence.support.JDOUserException;
import com.sun.persistence.support.spi.PersistenceCapable;


/** Freezer is a helper class with static methods to assist
 * transcribing non-ordered collection and map classes.
 * When an unordered collection or map is written to the datastore
 * and subsequently fetched, the order of elements can change.
 * This causes optimistic failures for persistent instances that
 * contain unordered collections even if the collection has not
 * changed.
 * Another issue solved by the Freezer is that if the user defines
 * an ordering or hashing based on persistent values of the
 * elements, during transcribing while fetching the instance from
 * the datastore the persistent instance must be fetched.  This
 * causes recursion in the fetch process.
 * Freezing is the process of iterating the elements of a collection
 * or iterating the entrySet elements of a map and constructing an
 * array of elements in an absolute ordering.  Freezing is done
 * during transcribing for storage.
 * Thawing is the process of iterating the frozen elements and
 * storing them in their user-visible order or using their
 * user-visible hashCode.
 * A collection or map is frozen when read from the datastore,
 * and thawed upon first application use.
 * @author Craig Russell
 * @version 1.0.2
 * @since 1.0.1
 */
public class Freezer {
    
    /** This class currently is not used as a class but only as a helper.
     * This constructor is for future use if needed.
     */
    protected Freezer() {
    }
    
    /** 
     * Holds a mapping of java.util and java.sql type names to the names 
     * of their corresponding SCO types. 
     */
    static Map convertedClassNames;

    /** Initialize convertedClassNames map. */
    static {
        convertedClassNames = new HashMap(12);
        convertedClassNames.put("java.util.ArrayList", "com.sun.org.apache.jdo.impl.sco.ArrayList"); // NOI18N
        convertedClassNames.put("java.util.Date", "com.sun.org.apache.jdo.impl.sco.Date"); // NOI18N
        convertedClassNames.put("java.util.HashMap", "com.sun.org.apache.jdo.impl.sco.HashMap"); // NOI18N
        convertedClassNames.put("java.util.HashSet", "com.sun.org.apache.jdo.impl.sco.HashSet"); // NOI18N
        convertedClassNames.put("java.util.Hashtable", "com.sun.org.apache.jdo.impl.sco.Hashtable"); // NOI18N
        convertedClassNames.put("java.util.LinkedList", "com.sun.org.apache.jdo.impl.sco.LinkedList"); // NOI18N
        convertedClassNames.put("java.sql.Date", "com.sun.org.apache.jdo.impl.sco.SqlDate"); // NOI18N
        convertedClassNames.put("java.sql.Time", "com.sun.org.apache.jdo.impl.sco.SqlTime"); // NOI18N
        convertedClassNames.put("java.sql.Timestamp", "com.sun.org.apache.jdo.impl.sco.SqlTimestamp"); // NOI18N
        convertedClassNames.put("java.util.TreeMap", "com.sun.org.apache.jdo.impl.sco.TreeMap"); // NOI18N
        convertedClassNames.put("java.util.TreeSet", "com.sun.org.apache.jdo.impl.sco.TreeSet"); // NOI18N
        convertedClassNames.put("java.util.Vector", "com.sun.org.apache.jdo.impl.sco.Vector"); // NOI18N
    }

    /**
     * I18N message handler
     */  
    private final static I18NHelper msg = I18NHelper.getInstance(
        "com.sun.org.apache.jdo.impl.sco.Bundle"); // NOI18N

    /** Provide a frozen array of elements from a Set. This method
     * does not actually freeze the Set but simply calculates the
     * frozen elements.
     * @return the Object[] containing the frozen elements.
     * @param size the number of elements in the collection.
     * @param set the Set whose elements are to be ordered.
     */    
    static public Object[] freeze(Collection set, int size) {
        Object[] result = new Object[size];
        Iterator it;
        if (set instanceof SCOCollection) {
            it = ((SCOCollection)set).eitherIterator();
        } else {
            it = set.iterator();
        }
        TreeSet ts = new TreeSet(new AbsoluteOrdering());
        while (it.hasNext()) {
            ts.add(it.next());
        }
        return ts.toArray(result);
    }
    
    /** Provide a frozen array of elements from the entrySet of a Map.
     * This method
     * does not actually freeze the Map but simply calculates the
     * frozen elements.
     * @return the Map.Entry[] of elements in the map.
     * @param size the number of entries in the map.
     * @param map the Map whose entrySet elements are to be calculated.
     */    
    static public Map.Entry[] freeze(Map map, int size) {
        Map.Entry[] result = new Map.Entry[size];
        if (size != 0) {
            TreeMap tm = new TreeMap(new AbsoluteOrdering());
            Iterator it;
            if (map instanceof SCOMap) {
                it = ((SCOMap)map).eitherIterator();
            } else {
                it = map.entrySet().iterator();
            }
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry)it.next();
                tm.put(me.getKey(), me.getValue());
            }
            return (Map.Entry[])tm.entrySet().toArray(result);
        } else {
            return result;
        }
    }
    
    /** Provide a frozen iterator of elements from the entrySet of a Map.
     * This method
     * does not actually freeze the Map but simply calculates the
     * frozen elements and returns an iterator over them.
     * @return an iterator over Map.Entry[] of elements in the map.
     * @param size the number of entries in the map.
     * @param map the Map whose entrySet elements are to be calculated.
     */    
    static public Iterator frozenIterator(Map map, int size) {
        TreeMap tm = new TreeMap(new AbsoluteOrdering());
        if (size != 0) {
            Iterator it;
            if (map instanceof SCOMap) {
                it = ((SCOMap)map).eitherIterator();
            } else {
                it = map.entrySet().iterator();
            }
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry)it.next();
                tm.put(me.getKey(), me.getValue());
            }
        }
        return tm.entrySet().iterator();
    }
    
    /** Create a map whose elements are ordered according to an
     * absolute ordering.
     * @return the map with an absolute ordering Comparator.
     */    
    static public Map createAbsoluteOrderMap() {
        return new TreeMap(new AbsoluteOrdering());
    }
    
    /** Create an iterator over frozen elements or entries.
     * @param frozen the array of frozen entries or elements.
     * @return the iterator over the entries or elements.
     */
    static public Iterator createFrozenIterator(Object[] frozen) {
        return new FrozenIterator(frozen);
    }
    
    /** Thaw the frozen elements of a map. If the elements are frozen,
     * retrieve them from the datastore and internally add them. Then return
     * the frozen state since they're not frozen any more.
     * @param map the Map to be thawed.
     * @param owner the StateManager that owns this Map.
     * @param frozenEntries the frozen entries to be thawed.
     * @return the new contents of frozenEntries.
     */
    static public Map.Entry[] thaw(SCOMap map, StateManagerInternal owner,
            Map.Entry[] frozenEntries) {
        if (frozenEntries == null || frozenEntries.length == 0) {
            return null;
        }
        int length = frozenEntries.length;
        if (owner != null) {
            // only fetch PC elements from the store
            ArrayList persistenceCapables = new ArrayList();
            for (int i = 0; i < length; ++i) {
                Map.Entry mapEntry = frozenEntries[i];
                Object key = mapEntry.getKey();
                Object value = mapEntry.getValue();
                if (key instanceof PersistenceCapable) {
                    persistenceCapables.add(key);
                }
                if (value instanceof PersistenceCapable) {
                    persistenceCapables.add(value);
                }
            }
            try {
                owner.getPersistenceManager().retrieveAll(persistenceCapables);
            } catch (JDOUserException ex) {
                // if objects don't exist, this exception is expected.
                // this will be handled later when the user iterates the 
                // collection if values are needed.
            }
        }
        for (int i = 0; i < length; ++i) {
            Map.Entry mapEntry = (Map.Entry)frozenEntries[i];
            Object key = mapEntry.getKey();
            Object value = mapEntry.getValue();
            map.putInternal(key, value);
        }
        // reset the caller's frozenEntries.
        return null;
    }
    
    /** Thaw the frozen elements of a collection. If the elements are frozen,
     * retrieve them from the datastore and internally add them. Then reset
     * the frozen state since they're not frozen any more.
     * @param sco the frozen collection to be thawed.
     * @param owner the StateManager that owns this collection.
     * @param frozenElements the elements to be thawed.
     * @return the new contents of frozenElements.
     */
    static public Object[] thaw(SCOCollection sco, StateManagerInternal owner,
            Object[] frozenElements) {
        if (frozenElements == null || frozenElements.length == 0) {
            return null;
        }
        int length = frozenElements.length;
        if (owner != null) {
            // only fetch PC elements from the store
            ArrayList persistenceCapables = new ArrayList();
            for (int i = 0; i < length; ++i) {
                Object element = frozenElements[i];
                if (element instanceof PersistenceCapable) {
                    persistenceCapables.add(element);
                }
            }
            try {
                owner.getPersistenceManager().retrieveAll(persistenceCapables);
            } catch (JDOUserException ex) {
                // if objects don't exist, this exception is expected.
                // this will be handled later when the user iterates the 
                // collection if values are needed.
            }
        }
        for (int i = 0; i < length; ++i) {
            sco.addInternal(frozenElements[i]);
        }
        // reset the caller's frozenElements.
        return null;
    }
    
    /** This class is the Comparator to impose an absolute ordering
     * on instances of PersistenceCapable, wrapper, and mutable sco types.
     */    
    static class AbsoluteOrdering implements Comparator {
        /** Implement an absolute ordering comparison for persistence
         * capable, wrapper, and mutable sco types.
         * @return -1, 0, or 1 if the first parameter is less that, equal to, or greater than the second parameter.
         * @param o1 the first parameter.
         * @param o2 the second parameter. */        
        public int compare(Object o1, Object o2) {
            String className1 = convertClassName(o1.getClass().getName());
            String className2 = convertClassName(o2.getClass().getName());
            if (className1!=className2) {
                // order by class names
                return className1.compareTo(className2);
            } else {
                // class names are the same (modulo sco Date types)
                if (o1 instanceof PersistenceCapable) {
                    // compare oids
                    Object oid1 = JDOHelper.getObjectId(o1);
                    Object oid2 = JDOHelper.getObjectId(o2);
                    if (oid1 instanceof Comparable) {
                        if (oid1 != null) {
                            return ((Comparable)oid1).compareTo(oid2);
                        } else if (o1 instanceof Comparable) {
                            return ((Comparable)o1).compareTo(o2);
                        } else {
                            return o1.hashCode() - o2.hashCode();
                        }
                    } else {
                        throw new JDOFatalInternalException(msg.msg("EXC_OidNotComparable", // NOI18N
                            oid1.getClass().getName()));
                    }
                } else if (o1 instanceof Comparable) {
                    // immutables (String, Integer, etc.) and Date use this code path
                    return ((Comparable)o1).compareTo(o2);
                } else {
                    // give up! compare instance.toString().
                    // this is used by java.util.Locale that doesn't implement Comparable.
                    return (o1.toString().compareTo(o2.toString()));
                }
            }
        }
    }
    
    /** Convert the class name to make non sco types compare to
     * their corresponding sco types.
     * @param className the actual class name.
     * @return the corresponding sco class name.
     */    
    static String convertClassName(String className) {
        String converted = (String)convertedClassNames.get(className);
        if (converted == null)
            converted = className;
        return converted;
    }
    
    /** This class iterates over an array.
     */    
    protected static class FrozenIterator implements Iterator {
        /** The array over which to iterate.
         */        
        Object[] frozen;
        int idx = 0;
        int length = 0;
        FrozenIterator(Object[] frozen) {
            this.frozen = frozen;
            length = frozen.length;
        }
        /** Return the next entry of the iteration.
         * @return the next entry of the iteration.
         */        
        public Object next() {return frozen[idx++];}
        /** Return true if the iteration is not complete.
         * @return true if the iteration is not complete.
         */        
        public boolean hasNext() {return idx < length;}
        /** This operation is not supported.
         */        
        public void remove() {throw new JDOFatalInternalException(
            msg.msg("EXC_RemoveNotSupported"));} // NOI18N
    }

    /** For debugging, print the contents of a frozen entrySet.
     * @param p where to write the output.
     * @param s an identifying string.
     * @param entries the Map.Entry[] to print.
     */    
    static public void printEntries(PrintStream p, String s, Map.Entry[] entries) {
        p.println(s);
        for (int i = 0; i < entries.length; ++i) {
            Map.Entry entry = entries[i];
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object keyoid = JDOHelper.getObjectId(key);
            Object valueoid = JDOHelper.getObjectId(value);
            p.println("Key: " + key.toString() + " OID: " + keyoid.toString()); // NOI18N
            p.println("Value: " + value.toString() + " OID: " + valueoid.toString()); // NOI18N
        }
    }
    
    /** For debugging, print the contents of a frozen collection.
     * @param p where to write the output.
     * @param s an identifying string.
     * @param elements the Object[] to print.
     */    
    static public void printElements(PrintStream p, String s, Object[] elements) {
        p.println(s);
        for (int i = 0; i < elements.length; ++i) {
            Object element = elements[i];
            Object oid = JDOHelper.getObjectId(element);
            p.println("Element: " + oid); // NOI18N
        }
    }
    
}
