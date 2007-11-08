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

package com.sun.org.apache.jdo.impl.model.java.runtime.jdk5;

import com.sun.org.apache.jdo.impl.model.java.JavaPropertyImpl;
import com.sun.org.apache.jdo.model.java.JavaMethod;
import com.sun.org.apache.jdo.model.java.JavaType;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import com.sun.org.apache.jdo.impl.model.java.runtime.*;

/**
 * Default implementation for the JavaProperty interfaces. A JavaProperty
 * represents a JavaBeans property.
 *
 * @author Michael Bouschen
 */
public class JDK5RuntimeJavaProperty 
        extends JavaPropertyImpl
{
    /** */
    public JDK5RuntimeJavaProperty(String name, 
        JavaMethod getter, JavaMethod setter,
        JavaType type, JavaType declaringClass)
    {
        super(name, getter, setter, type, declaringClass);
    }
 
    // ===== methods specified in JavaMember =====
    
    /**
     * Returns the JavaType representation of the component type of the type
     * of the property, if the property type is an array or collection. The
     * method returns <code>null</code>, if the property type is not an array
     * or collection.
     * @return the component type of the property type in case of an array or
     * collection.
     */
    public JavaType getComponentType() 
    {
        Class componentClass = ComponentTypeHelper.getComponentClass(this);
        return (componentClass == null) ? null : 
            getJavaTypeForClass(componentClass);
    }
    
    // ===== Methods not specified in JavaProperty =====
    
    /** 
     * Returns a JavaType instance for the specified Class object. 
     * This method provides a hook such that ReflectionJavaField subclasses can
     * implement their own mapping of Class objects to JavaType instances. 
     */
    public JavaType getJavaTypeForClass(Class clazz)
    {
        return ((JDK5RuntimeJavaType) getDeclaringClass()).
                getJavaTypeForClass(clazz);
    }
}
