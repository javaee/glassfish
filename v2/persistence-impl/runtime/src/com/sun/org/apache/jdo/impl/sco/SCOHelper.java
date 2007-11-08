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

package com.sun.org.apache.jdo.impl.sco;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.sco.SCOCollection;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.JDOUserException;

/** 
 * Helper class used by Tracked SCO implementations. Contains static
 * methods to allow to simplify implementations of SCO classes in the
 * same package without extending the same base class, which is 
 * impossible as each Tracked SCO class extends corresponding "regular" SCO.
 * 
 * @author Marina Vatkina 
 */
class SCOHelper { 

    /**
     * Message keys for assertions performed.
     */
    private final static String EXC_ElementNullsNotAllowed = 
        "EXC_ElementNullsNotAllowed"; // NOI18N
    private final static String EXC_KeyNullsNotAllowed = 
        "EXC_KeyNullsNotAllowed"; // NOI18N
    private final static String EXC_ValueNullsNotAllowed = 
        "EXC_ValueNullsNotAllowed"; // NOI18N
    private final static String EXC_ElementClassCastException = 
        "EXC_ElementClassCastException"; // NOI18N
    private final static String EXC_KeyClassCastException = 
        "EXC_KeyClassCastException"; // NOI18N
    private final static String EXC_ValueClassCastException = 
        "EXC_ValueClassCastException"; // NOI18N

    /**
     * I18N message handler
     */  
    private final static I18NHelper msg = I18NHelper.getInstance(
        "com.sun.org.apache.jdo.impl.sco.Bundle"); // NOI18N

    /**
     * Logger for SCO classes.
     */
    private static final Log logger = LogFactory.getFactory().getInstance(
        "com.sun.org.apache.jdo.impl.sco"); // NOI18N

    /**
     * Verifies if null values are allowed for elements of an SCOCollection.
     *   
     * @param o     the object to validate
     * @param allowNulls true if nulls are allowed
     * @throws JDOUserException if null values are not allowed and the component
     * to insert is null.
     */
    protected static void assertNullsAllowed(Object o, boolean allowNulls) {
        assertNulls(o, allowNulls, EXC_ElementNullsNotAllowed);
    }
     
    /**
     * Verifies if null values are allowed for keys of an SCOMap.
     *   
     * @param o     the key to validate
     * @param allowNulls true if nulls are allowed
     * @throws JDOUserException if null values are not allowed and the key
     * to set is null.
     */
    protected static void assertNullKeysAllowed(Object o, boolean allowNulls) {
        assertNulls(o, allowNulls, EXC_KeyNullsNotAllowed);
    }
     
    /**
     * Verifies if null values are allowed for values of an SCOMap.
     *   
     * @param o     the value to validate.
     * @param allowNulls true if nulls are allowed.
     * @throws JDOUserException if null values are not allowed and the value
     * to insert is null.
     */
    protected static void assertNullValuesAllowed(Object o, boolean allowNulls) {
        assertNulls(o, allowNulls, EXC_ValueNullsNotAllowed);
    }
     
    /**
     * Verifies that the component to insert is of correct type
     *
     * @param o     the object to validate.
     * @param elementType the element types allowed.
     * @throws JDOUserException if validation fails.
     */
    protected static void assertElementType(Object o, Class elementType) {
        assertType(o, elementType, EXC_ElementClassCastException);
    }

    /**
     * Verifies that the key to insert is of correct type.
     *
     * @param o     the key to validate.
     * @param keyType the key types allowed.
     * @throws JDOUserException if validation fails.
     */
    protected static void assertKeyType(Object o, Class keyType) {
        assertType(o, keyType, EXC_KeyClassCastException);
    }

    /**
     * Verifies that the value to insert is of correct type.
     *
     * @param o     the value to validate.
     * @param valueType the value types allowed.
     * @throws JDOUserException if validation fails.
     */
    protected static void assertValueType(Object o, Class valueType) {
        assertType(o, valueType, EXC_ValueClassCastException);
    }

    /**
     * Helper method to validate errors on processing arrays of objects.
     * @param l actual size of the array
     * @param err array of Throwable to validate
     * @throws JDOUserException if <code>l</code> is greater than 0.
     */
    protected static void validateResult(int l, Throwable[] err) {
        if (l > 0) {
            Throwable[] t = new Throwable[l];
            System.arraycopy(err, 0, t, 0, l);
            throw new JDOUserException(msg.msg(
                "EXC_FailedToProcessAll"), t); // NOI18N
        }
    }

    /**
     * Verifies if nulls are allowed as instances of this SCO.
     *   
     * @param o     the instance to validate.
     * @param allowNulls true if nulls are allowed.
     * @param exc message key for the exception to be thrown.
     * @throws JDOUserException if validation fails.
     */
    protected static void assertNulls(Object o, boolean allowNulls, String exc) {
        if (allowNulls == false && o == null) {
                throw new JDOUserException(msg.msg(exc));
        }
    }

    /**
     * Verifies that the value to insert is of correct type.
     *
     * @param o     the value to validate.
     * @param type the Class of types allowed.
     * @param exc message key for the exception to be thrown.
     * @throws JDOUserException if validation fails.
     */
    private static void assertType(Object o, Class type, String exc) {
        if (type != null && ! type.isAssignableFrom(o.getClass())) {
           throw new JDOUserException(msg.msg(
                exc, type.getName()), // NOI18N
                new Exception[] {new ClassCastException()}, o);
        }
    }

    /** 
     * Returns the owner object of the SCO instance 
     *    
     * @return owner object 
     */   
    protected static Object getOwner(StateManagerInternal owner) {   
        return ((owner == null)? null : owner.getObject());  
    } 
 
    /**  
     * Returns the field name 
     *    
     * @return field name as java.lang.String 
     */   
    protected static String getFieldName(StateManagerInternal owner, int fieldNumber) {
        return ((owner == null)? null : owner.getFieldName(fieldNumber));   
    }

    /**
     * Tracing method for other SCO implementations.
     * @param name Class name of an instance calling the method.
     * @param msg String to display.
     */  
    protected static void debug(String name, String msg) {
        logger.debug("In " + name + " " + msg); // NOI18N
    }
}
