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
 * An instance of class ErrorType represents an erroneous type. Components
 * such as the semantic analysis may use this type to indicate an error
 * situtaion. It is compatible to all other types. 
 * 
 * @author Michael Bouschen
 * @since JDO 1.0.1
 */
public class ErrorType
    extends AbstractJavaType
{
    /** The singleton ErrorType instance. */
    public static final ErrorType errorType = new ErrorType();

    /** 
     * Creates new a ErrorType instance. This constructor should not be 
     * called directly; instead, the singleton instance  {@link #errorType}
     * should be used. 
     */
    protected ErrorType() {}

    /** 
     * Returns true if this JavaType is compatible with the specified
     * JavaType. This implementation always returns <code>true</code>,
     * because ErrorType is compatible with any other type.
     * @param javaType the type this JavaType is checked with.
     * @return <code>true</code> if this is compatible with the specified
     * type; <code>false</code> otherwise.
     */
    public boolean isCompatibleWith(JavaType javaType)
    {
        return true;
    }
    
    /** 
     * Returns the name of the type.  
     * @return type name
     */
    public String getName()
    {
        return "<error type>"; //NOI18N
    }

}
