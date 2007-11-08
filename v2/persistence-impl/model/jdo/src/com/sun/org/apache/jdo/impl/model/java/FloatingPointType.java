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

/**
 * A FloatingPointType instance represents a floating point type as defined
 * in the Java language. There are two floating point types: 
 * <code>float</code> and <code>double</code>.
 * <p>
 * Class PredefinedType provides public static final variables referring
 * to the JavaType representation for floating point types.
 * 
 * @see PredefinedType#floatType
 * @see PredefinedType#doubleType
 *
 * @author Michael Bouschen
 * @since JDO 1.0.1
 */
public class FloatingPointType
    extends PrimitiveType 
{
    /** Constructor. */
    public FloatingPointType(Class clazz)
    {
        super(clazz);
    }

    /** 
     * Returns <code>true</code> if this JavaType represents a floating
     * point type. 
     * @return <code>true</code> if this JavaType represents a floating
     * point type; <code>false</code> otherwise.
     */
    public boolean isFloatingPoint() 
    {
        return true;
    }

    /**
     * Returns <code>true</code> if this JavaType represents an orderable
     * type as specified by JDO.
     * @return <code>true</code> if this JavaType represents an orderable
     * type; <code>false</code> otherwise.
     */
    public boolean isOrderable() 
    {
        return true;
    }
}
