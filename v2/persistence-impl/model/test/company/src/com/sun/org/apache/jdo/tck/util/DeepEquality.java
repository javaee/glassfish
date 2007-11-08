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

/**
 * This <code>DeepEquality</code> interface defines a method indicating
 * whether some other object is "deep equal to" this object.
 * <p>
 * Two objects are deep equal, if all the fields are deep equal. The
 * sematics of deep equal on the fields depends of the field type:
 * <ul>
 * <li>fields of type <code>boolean</code> or an integral type are deep
 * equal, if they compare true using <code>==</code></li> 
 * <li>fields of type <code>float</code> and <code>double</code> are deep
 * equal, if they are close enough as defined by methods closeEnough
 * provided by class <code>EqualityHelper</code></li> 
 * <li>fields of Java wrapper classes are equal if their wrapped primitive
 * values are deep equal</li>
 * <li>fields of type BigDecimal are equal, if compareTo returns 0 </li>
 * <li>collection fields are deep equal, if they have the same size and
 * their corresponding elements compare deep equal after sorting using the
 * natural ordering.</li>  
 * <li>map fields compare deep equal, if they have the same size and both
 * keys and values compare deep equal after sorting the entries using the
 * natural ordering of the keys.</li> 
 * <li>fields of type <code>DeepEquality</code> are deep equal, if method 
 * <code>deepEquals</code> returns <code>true</code></li>
 * <li>fields of other types are deep equal, if method <code>equals</code>
 * returns <code>true</code></li> 
 * </ul>
 * The <code>EqualityHelper</code> instance passed to the
 * <code>deepEquals</code> method keeps track of instances that have
 * already been processed to avoid endless recursion for cyclic data
 * structures. 
 *
 * @author Michael Bouschen
 * @since 1.1
 */
public interface DeepEquality {

    /** 
     * Returns <code>true</code> if all the fields of this instance are
     * deep equal to the corresponding fields of the specified Employee.
     * @param other the object with which to compare.
     * @param helper EqualityHelper to keep track of instances that have
     * already been processed. 
     * @return <code>true</code> if all the fields are deep equal;
     * <code>false</code> otherwise. 
     * @throws ClassCastException if the specified instances' type prevents
     * it from being compared to this instance. 
     */
    public boolean deepCompareFields(DeepEquality other, EqualityHelper helper);
}
