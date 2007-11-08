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

package com.sun.org.apache.jdo.impl.model.java;

import com.sun.org.apache.jdo.model.java.JavaType;

/**
 * A PrimitiveType instance represents a primitive type as defined in the
 * Java language. There are eight primitive types: <code>boolean</code>,
 * <code>byte</code>, <code>short</code>, <code>int</code>,
 * <code>long</code>, <code>char</code>, 
 * <code>float</code>, <code>double</code>.
 * <p>
 * Class PredefinedType provides public static final variables referring
 * to the JavaType representation for primtive types.
 * 
 * @see PredefinedType#booleanType
 * @see PredefinedType#byteType
 * @see PredefinedType#shortType
 * @see PredefinedType#intType
 * @see PredefinedType#longType
 * @see PredefinedType#charType
 * @see PredefinedType#floatType
 * @see PredefinedType#doubleType
 *
 * @author Michael Bouschen
 * @since JDO 1.0.1
 */
public class PrimitiveType 
    extends PredefinedType
{
    /** The JavaType of the corresponding Java wrapper class type. */
    private WrapperClassType wrapperClassType;

    /** 
     * Constructor.
     * @param clazz the Class instance representing the type
     */
    protected PrimitiveType(Class clazz)
    {
        super(clazz, null);
    }

    /** 
     * Returns <code>true</code> if this JavaType represents a primitive
     * type.
     * @return <code>true</code> if this JavaTypre represents a primitive
     * type; <code>false</code> otherwise.
     */
    public boolean isPrimitive() 
    {
        return true;
    }

    /** 
     * Returns <code>true</code> if this JavaType represents a type whoses
     * values may be treated as values rather than refernces during
     * storing. 
     * @return <code>true</code> if this JavaType represents a value type;
     * <code>false</code> otherwise.
     */
    public boolean isValue() 
    {
        return true;
    }

    // ===== Methods not defined in JavaType =====

    /** 
     * Returns the JavaType instance of the Java wrapper class that
     * corresponds to this primitive type.
     * @return the JavaType of the corresponding Java wrapper class.
     */
    public WrapperClassType getWrapperClassType()
    {
        return wrapperClassType;
    }
    
    /** 
     * Sets the JavaType instance of the corresponding Java wrapper class.
     * @param wrapperClassType the JavaType representing the corresponding
     * Java wrapper class.
     */
    void setWrapperClassType(WrapperClassType wrapperClassType)
    {
        this.wrapperClassType = wrapperClassType;
    }
    
}
