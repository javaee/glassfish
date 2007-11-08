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

package com.sun.org.apache.jdo.impl.model.java.runtime;

import com.sun.org.apache.jdo.impl.model.java.reflection.ReflectionJavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;

/**
 * A reflection based JavaModel implementation used at runtime.  
 * The implementation takes <code>java.lang.Class</code> and
 * <code>java.lang.reflect.Field</code> instances to get Java related
 * metadata about types and fields. 
 * <p>
 * The RuntimeJavaModelFactory caches JavaModel instances per ClassLoader.
 * The RuntimeJavaModel implementation will use this ClassLoader to lookup
 * any type by name. This makes sure that the type name is unique.
 *
 * @author Michael Bouschen
 * @since JDO 1.0.1
 * @version JDO 2.0
 */
public class RuntimeJavaModel
    extends ReflectionJavaModel
{
    /** Constructor. */
    public RuntimeJavaModel(ClassLoader classLoader, 
                            RuntimeJavaModelFactory declaringJavaModelFactory) 
    {
        super(classLoader, declaringJavaModelFactory);
    }
    
    /** 
     * Creates a new instance of the JavaType implementation class.
     * <p>
     * This implementation returns a RuntimeJavaType instance.
     * @param clazz the Class instance representing the type
     * @return a new JavaType instance
     */
    protected JavaType newJavaTypeInstance(Class clazz)
    {
        return new RuntimeJavaType(clazz, this);
    }
    
}
