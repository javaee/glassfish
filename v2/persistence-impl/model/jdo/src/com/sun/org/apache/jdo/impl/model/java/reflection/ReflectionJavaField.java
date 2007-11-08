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

import java.lang.reflect.Field;

import com.sun.org.apache.jdo.impl.model.java.BaseReflectionJavaField;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * A reflection based JavaField implementation used at runtime.  
 * The implementation takes <code>java.lang.reflect.Field</code> instances
 * to get Java related metadata about fields. 
 * 
 * @author Michael Bouschen
 * @since JDO 1.1
 * @version JDO 2.0
 */
public class ReflectionJavaField
    extends BaseReflectionJavaField
{
    /** I18N support */
    private final static I18NHelper msg =  
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.model.java.Bundle"); //NOI18N

    /** 
     * Constructor for fields w/o JDO metadata. 
     * @param field the reflection field representation.
     * @param declaringClass the JavaType of the class that declares the field.
     */
    public ReflectionJavaField(Field field, JavaType declaringClass)
    {
        super(field, declaringClass);
        this.type = getJavaTypeForClass(field.getType());
    }
    
    /** 
     * Constructor for fields having JDO metadata.
     * @param fieldName the name of the field.
     * @param type the field type.
     * @param declaringClass the JavaType of the class that declares the field.
     */
    public ReflectionJavaField(String fieldName, JavaType type, 
                               JavaType declaringClass)
    {
        super(fieldName, declaringClass);
        this.type = type;
    }
    
    /**
     * Returns the JavaType representation of the field type.
     * @return field type
     */
    public JavaType getType()
    {
        if (type == null) {
            type = getJavaTypeForClass(getField().getType());
        }
        return type;
    }

    // ===== Methods not defined in JavaField =====

    /** 
     * Returns a JavaType instance for the specified Class object. 
     * This method provides a hook such that ReflectionJavaField subclasses can
     * implement their own mapping of Class objects to JavaType instances. 
     */
    public JavaType getJavaTypeForClass(Class clazz)
    {
        return ((ReflectionJavaType)getDeclaringClass()).getJavaTypeForClass(clazz);
    }
}
