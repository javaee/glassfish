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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import com.sun.org.apache.jdo.impl.model.java.PredefinedType;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaMethod;
import com.sun.org.apache.jdo.model.java.JavaProperty;
import com.sun.org.apache.jdo.model.java.JavaType;

/** 
 * Helper class to calculate the component type of he type of a field, method
 * or property in a J2SE5.0 environment. 
*/
public class ComponentTypeHelper
{
    /**
     * Returns the Class instance representing the component type of the
     * type of the specified field.
     */
    public static Class getComponentClass(JavaField javaField)
    {
        JavaType type = javaField.getType();
        Field field = (Field) javaField.getUnderlyingObject();
        return getComponentClass(type, field.getGenericType());
    }

    /**
     * Returns the Class instance representing the component type of the
     * return type of the specified method.
     */
    public static Class getComponentClass(JavaMethod javaMethod)
    {
        JavaType type = javaMethod.getReturnType();
        if (type == PredefinedType.voidType)
            // return null for void method
            return null;
        Method method = (Method) javaMethod.getUnderlyingObject();
        return getComponentClass(type, method.getGenericReturnType());
    }

    /**
     * Returns the Class instance representing the component type of the
     * type of the specified property.
     */
    public static Class getComponentClass(JavaProperty javaProperty)
    {
        Class componentClass = null;
        JavaMethod getter = javaProperty.getGetterMethod();
        JavaMethod setter = javaProperty.getSetterMethod();
        if (getter != null)
            componentClass = getComponentClass(getter);
        else if (setter != null) {
            JavaType[] paramJavaTypes = setter.getParameterTypes();
            assert(paramJavaTypes.length == 1);
            Method method = (Method) setter.getUnderlyingObject();
            Type[] genericParamTypes = method.getGenericParameterTypes();
            assert(genericParamTypes.length == 1);
            componentClass = 
                getComponentClass(paramJavaTypes[0], genericParamTypes[0]);
        }
        return componentClass;
    }

    // ===== Internal helper methods =====

    /** Helper method. */
    private static Class getComponentClass(JavaType type, Type genericType)
    {
        Class componentClass = null;
        if (type.isArray()) {
            Class clazz = (Class) type.getUnderlyingObject();
            componentClass = clazz.getComponentType(); 
        }
        else if (type.isJDOSupportedCollection()) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                Type[] actualTypeArgs = pt.getActualTypeArguments();
                assert(actualTypeArgs.length == 1);
                assert(actualTypeArgs[0] instanceof Class);
                componentClass = (Class) actualTypeArgs[0];
            }
        }
        return componentClass;
    }
    
    
}
