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

package com.sun.org.apache.jdo.impl.model.java.reflection;

import java.lang.reflect.Method;

import com.sun.org.apache.jdo.model.java.JavaMethod;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.impl.model.java.AbstractJavaMember;

/**
 * A reflection based JavaMethod implementation.
 * The implementation takes <code>java.lang.reflect.Method</code> instances
 * to get Java related metadata about methods. 
 * 
 * @author Michael Bouschen
 * @since JDO 2.0
 */
public class ReflectionJavaMethod
    extends AbstractJavaMember
    implements JavaMethod
{
    /** The wrapped java.lang.reflect.Method instance. */
    private final Method method;
    
    /** 
     * Constructor.
     * @param method the reflection method representation.
     * @param declaringClass the JavaType of the class that declares the field.
     */
    public ReflectionJavaMethod(Method method, JavaType declaringClass)
    {
        super(method.getName(), declaringClass);
        this.method = method;
    }

    // ===== Methods specified in JavaElement =====

    /**
     * Returns the environment specific instance wrapped by this JavaModel
     * element. This implementation returns the
     * <code>java.lang.reflect.Method</code> instance for this JavaMethod.
     * @return the environment specific instance wrapped by this JavaModel
     * element.
     */
    public Object getUnderlyingObject() 
    {
        return method;
    }

    // ===== Methods specified in JavaMember =====
    
    /**
     * Returns the Java language modifiers for the field represented by
     * this JavaMember, as an integer. The java.lang.reflect.Modifier class
     * should be used to decode the modifiers. 
     * @return the Java language modifiers for this JavaMember
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers()
    {
        return method.getModifiers();
    }

    /** */
    public JavaType getType() 
    {
        return getReturnType();
    }

    // ===== Methods specified in JavaMethod =====

    /**
     * Returns the JavaType representation of the method return type.
     * @return method return type.
     */
    public JavaType getReturnType()
    {
        return getJavaTypeForClass(method.getReturnType());
    }

    /**
     * Returns an array of JavaType instances that represent the formal
     * parameter types, in declaration order, of the method represented by
     * this JavaMethod instance.
     * @return the types of teh formal parameters.
     */
    public JavaType[] getParameterTypes()
    {
        Class[] params = method.getParameterTypes();
        JavaType[] paramTypes = new JavaType[params.length];
        for (int i = 0; i < params.length; i++) {
            paramTypes[i] = getJavaTypeForClass(params[i]);
        }
        return paramTypes;
    }
    
    // ===== Methods not defined in JavaMethod =====

    /** 
     * Returns a JavaType instance for the specified Class object. 
     * This method provides a hook such that ReflectionJavaField subclasses can
     * implement their own mapping of Class objects to JavaType instances. 
     */
    public JavaType getJavaTypeForClass(Class clazz)
    {
        return ((ReflectionJavaType) getDeclaringClass()).
            getJavaTypeForClass(clazz);
    }
}
