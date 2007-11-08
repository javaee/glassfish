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
 * An instance of class NullType represents the type of the null expression
 * in Java. It is compatible to all reference types.
 * 
 * @author Michael Bouschen
 * @since JDO 1.0.1
 */
public class NullType
    extends AbstractJavaType
{
    /** The singleton NullType instance. */
    public static final NullType nullType = new NullType();

    /** 
     * Creates new a NullType instance. This constructor should not be 
     * called directly; instead, the singleton instance  {@link #nullType}
     * should be used. 
     */
    protected NullType() {}

    /** 
     * Returns true if this JavaType is compatible with the specified
     * JavaType. This implementation returns <code>true</code>, if the
     * specified javaType is a not a primitive type, because the type of
     * null is compatiple with all reference types.
     * @param javaType the type this JavaType is checked with.
     * @return <code>true</code> if this is compatible with the specified
     * type; <code>false</code> otherwise.
     */
    public boolean isCompatibleWith(JavaType javaType)
    {
        return !javaType.isPrimitive();
    }
    
    /** 
     * Returns the name of the type.  
     * @return type name
     */
    public String getName()
    {
        return "<null type>"; //NOI18N
    }

}
