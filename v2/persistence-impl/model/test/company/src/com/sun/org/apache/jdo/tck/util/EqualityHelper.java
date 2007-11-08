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

package com.sun.org.apache.jdo.tck.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.math.BigDecimal;

/**
 * This is a utility class to support equality checking. An EqualityHelper
 * object defines the context of a deepEquals call, because it keeps track
 * of objects that have already been processed. This avoids endless
 * recursion when comparing cyclic data structures for deep equality.
 * <p>
 * Furthermore, EqualityHelper provides convenience methods for checking
 * deep equality, equality and close enough (for floating point values). 
 *
 * @author Michael Bouschen
 * @since 1.1 
 */
public class EqualityHelper {
    
    /** Used when comparing float values close enough. */
    public static float FLOAT_EPSILON = (float)Math.pow(2.0, -20.0);

    /** Used when comparing double values close enough. */
    public static double DOUBLE_EPSILON = Math.pow(2.0, -52.0);

    /** Comparator used in method deepEquals comparing maps. */
    private static Comparator entryKeyComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                Object key1 = ((Map.Entry)o1).getKey();
                Object key2 = ((Map.Entry)o2).getKey();
                return ((Comparable)key1).compareTo(key2);
            }
        };

    /** Collection of instances that have been processed already in the
     * context of this EqualityHelper instance 
     */
    private Collection processed = new HashSet();

    // Methods to support keeping track of instances that have been
    // processed already.

    /** Returns <code>true</code> if the specified instance has been
     * processed already in the context of this
     * <code>EqualityHelper</code>. 
     * @param obj the instance to be checked.
     * @return <code>true</code> if the instance has been processed
     * already; <code>false</code> otherwise.
     */
    public boolean isProcessed(Object obj) {
        return processed.contains(obj);
    }
    
    /** Marks the specified instance as processed in the context of this
     * <code>EqualityHelper</code>. This means the instance is added to the 
     * collection of processed instances. 
     * @param obj instance marked as processed
     */
    public void markProcessed(Object obj) {
        processed.add(obj);
    }
    
    /** Clears the collection of processed instances of this
     * <code>EqualityHelper</code>. No instance is marked as processed in
     * the context of this <code>EqualityHelper</code> after calling this
     * method. 
     */
    public void clearProcessed() {
        processed.clear();
    }

    // Deep equality support methods

    /** Returns <code>true</code> if the specified instances are "deep
     * equal". 
     * @param me one object to be tested for deep equality
     * @param other the other object to be tested for deep equality
     * @return <code>true</code> if the objects are deep equal.
     */
    public boolean deepEquals(DeepEquality me, DeepEquality other) {
        if (me == other)
            return true;
        if ((me == null) || (other == null))
            return false;
        if (!me.getClass().isAssignableFrom(other.getClass()))
            return false; 
        if (isProcessed(me))
            return true;
        markProcessed(me);
        return me.deepCompareFields(other, this);
    }

    /** Returns <code>true</code> if the specified instances are "deep
     * equal". The method compares the two instances via the deepEquals
     * method if they implement DeepEquals; compares the two instances via 
     * deepEquals if they implement Collection or Map, and otherwise
     * compares the instances using equals. 
     * @param me one object to be tested for deep equality
     * @param other the other object to be tested for deep equality
     * @return <code>true</code> if the objects are deep equal.
     */
    public  boolean deepEquals(Object me, Object other) {
        if (me == other)
            return true;
        if ((me == null) || (other == null))
            return false;
        if ((me instanceof DeepEquality) && (other instanceof DeepEquality))
            return deepEquals((DeepEquality)me, (DeepEquality)other);
        if ((me instanceof Collection) && (other instanceof Collection))
            return deepEquals((Collection)me, (Collection)other);
        if ((me instanceof Map) && (other instanceof Map))
            return deepEquals((Map)me, (Map)other);
        return me.equals(other);
    }

    /** Returns <code>true</code> if the specified collections are "deep 
     * equal". Two collections are deep equal, if they have the same size
     * and their corresponding elements are deep equal after sorting 
     * using the natural ordering of the elements. The method throws a
     * <code>ClassCastException</code> if the elements are not Comparable
     * or if they are not mutually comparable.
     * @param mine one collection to be tested for deep equality
     * @param other the other collection to be tested for deep equality
     * @return <code>true</code> if the collections are deep equal.
     * @throws ClassCastException if the collections contain elements that
     * are not mutually comparable.
     */
    public boolean deepEquals(Collection mine, Collection other) {
        if (mine == other)
            return true;
        if ((mine == null) || (other == null))
            return false;

        // Return false, if the size differs
        if (mine.size() != other.size())
            return false;
        // Now check the elements 
        List myList = new ArrayList(mine);
        Collections.sort(myList);
        List otherList = new ArrayList(other);
        Collections.sort(otherList);
        for (int i = 0; i < myList.size(); i++) {
            if (!deepEquals(myList.get(i), otherList.get(i)))
                return false;
        }
        return true;
    }

    /** Returns <code>true</code> if the specified maps are "deep
     * equal". Two maps are deep equal, if they have the same size and the 
     * values of the corresponding keys compare deep equal. The method
     * throws a <code>ClassCastException</code> if keys or values are not
     * Comparable or if they are not mutually comparable.
     * @param mine one map to be tested for deep equality
     * @param other the other map to be tested for deep equality
     * @return <code>true</code> if the maps are deep equal.
     * @throws ClassCastException if the maps contain keys or values that 
     * are not mutually comparable.
     */
    public boolean deepEquals(Map mine, Map other) {
        if (mine == other)
            return true;
        if ((mine == null) || (other == null))
            return false;

        // Return false, if the size differs
        if (mine.size() != other.size())
            return false;

        // Now check the elements 
        List myList = new ArrayList(mine.entrySet());
        Collections.sort(myList, entryKeyComparator);
        List otherList = new ArrayList(other.entrySet());
        Collections.sort(otherList, entryKeyComparator);

        for (int i = 0; i < myList.size(); i++) {
            Map.Entry entry1 = (Map.Entry)myList.get(i);
            Map.Entry entry2 = (Map.Entry)otherList.get(i);
            // compare the keys
            if (!deepEquals(entry1.getKey(), entry2.getKey()))
                return false;
            // compare the values
            if (!deepEquals(entry1.getValue(), entry2.getValue()))
                return false;
        }
        return true;
    }

    // Shallow equality support methods

    /** Returns <code>true</code> if the specified collections are "shallow
     * equal". Two collections are shallow equal, if they have the same size 
     * and their corresponding elements are equal after sorting using the
     * natural ordering. 
     * @param mine one collection to be tested for shallow equality
     * @param other the other collection to be tested for shallow equality
     * @return <code>true</code> if the collections are deep equal.
     */
    public boolean shallowEquals(Collection mine, Collection other) {
        if (mine == other)
            return true;
        if ((mine == null) || (other == null))
            return false;

        // Return false, if the size differs
        if (mine.size() != other.size())
            return false;
        // Now check the elements 
        List myList = new ArrayList(mine);
        Collections.sort(myList);
        List otherList = new ArrayList(other);
        Collections.sort(otherList);
        return myList.equals(otherList);
    }

    // Equality support methods

    /** Returns <code>true</code> if the specified objects are equal. 
     * This is a helper method checking for identical and <code>null</code>
     * objects before delegating to the regular equals method.
     * @param o1 one object to be tested for equality
     * @param o2 the other object to be tested for equality
     * @return <code>true</code> if the specified objects are equal.
     */
    public boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if ((o1 == null) || (o2 == null))
            return false;
        return o1.equals(o2);
    }

    /** Returns <code>true</code>, if compare called for the specified
     * BigDecimal objects returns <code>0</code>. Please note, two
     * BigDecimal instances are not equal (using equals) if their scale
     * differs, and this method compares the values, ignoring scale. 
     * @param bd1 one object to be tested for equality
     * @param bd2 the other object to be tested for equality
     * @return <code>true</code> if the specified BigDecimal objects are
     * equal.
     */
    public boolean equals(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == bd2)
            return true;
        if ((bd1 == null) || (bd2 == null))
            return false;
        return bd1.compareTo(bd2) == 0;
    }

    // Methods to support "close enough" comparison

    /** Returns <code>true</code> if the specified objects are close
     * enough to be considered to be equal for a deep equals
     * comparison. The method delegates to the method taking double
     * or float values if the specified objects are Float or Double
     * wrappers. Otherwise it delegates to equals. 
     * @param o1 one object to be tested for close enough 
     * @param o2 the other object to be tested for close enough 
     * @return <code>true</code> if the specified values are close enough.
     */
    public boolean closeEnough(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if ((o1 == null) || (o2 == null))
            return false;
       
        if ((o1 instanceof Double) && (o2 instanceof Double))
            return closeEnough(((Double)o1).doubleValue(), 
                               ((Double)o2).doubleValue()); 
        else if ((o1 instanceof Float) && (o2 instanceof Float))
            return closeEnough(((Float)o1).floatValue(), 
                               ((Float)o2).floatValue()); 
        else if ((o1 instanceof BigDecimal) && (o2 instanceof BigDecimal))
            return ((BigDecimal)o1).compareTo((BigDecimal)o2) == 0;
        else
            return o1.equals(o2);
    }

    /** Returns <code>true</code> if the specified float values are close
     * enough to be considered to be equal for a deep equals
     * comparison. Floating point values are not exact, so comparing them
     * using <code>==</code> might not return useful results. This method
     * checks that both double values are within some percent of each
     * other. 
     * @param d1 one double to be tested for close enough 
     * @param d2 the other double to be tested for close enough 
     * @return <code>true</code> if the specified values are close enough.
     */
    public boolean closeEnough(double d1, double d2) {
        if (d1 == d2)
            return true;

        double diff = Math.abs(d1 - d2);
        return diff < Math.abs((d1 + d2) * DOUBLE_EPSILON);
    }

    /**
     * Returns <code>true</code> if the specified float values are close
     * enough to be considered to be equal for a deep equals
     * comparison. Floating point values are not exact, so comparing them 
     * using <code>==</code> might not return useful results. This method
     * checks that both float values are within some percent of each
     * other. 
     * @param f1 one float to be tested for close enough 
     * @param f2 the other float to be tested for close enough 
     * @return <code>true</code> if the specified values are close enough.
     */
    public boolean closeEnough(float f1, float f2) {
        if (f1 == f2)
            return true;

        float diff = Math.abs(f1 - f2);
        return diff < Math.abs((f1 + f2) * FLOAT_EPSILON);
    }

}
