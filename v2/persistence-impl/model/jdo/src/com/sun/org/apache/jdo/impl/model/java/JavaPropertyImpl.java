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

import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaMethod;
import com.sun.org.apache.jdo.model.java.JavaProperty;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * Default Implementation of the JavaProperty interface. A JavaProperty
 * instance represents a JavaBeans property.
 *
 * @author Michael Bouschen
 * @since JDO 2.0
 */
public class JavaPropertyImpl
    extends AbstractJavaMember
    implements JavaProperty
{
    /** The method object of the getter method. */
    private final JavaMethod getter;

    /** The method object of the setter method. */
    private final JavaMethod setter;

    /** The type of the property. */
    private final JavaType type;

    /** I18N support */
    private static I18NHelper msg = 
        I18NHelper.getInstance(JavaPropertyImpl.class);

    /** Constructor setting name, getter, setter, type and declaringClass. */
    public JavaPropertyImpl(String name, JavaMethod getter, JavaMethod setter,
                            JavaType type, JavaType declaringClass)
        throws ModelFatalException
    {
        super(name, declaringClass);
        this.getter = getter;
        this.setter = setter;
        this.type = type;
        if ((getter == null) && (setter == null))
            throw new ModelFatalException(
                msg.msg("EXC_MissingGetterAndSetter", //NOI18N
                         name, declaringClass.getName()));
    }
    
    // ===== Methods specified in JavaElement =====
    
    /** 
     * Returns the environment specific instance wrapped by this JavaModel
     * element.
     * <p> 
     * This implementation returns the underlying object of the
     * getter method if available; otherwise the one from the setter method. 
     * @return the environment specific instance wrapped by this JavaModel
     * element.
     */
    public Object getUnderlyingObject() 
    {
        Object underlyingObject = null;
        
        if (getter != null)
            underlyingObject = getter.getUnderlyingObject();
        else if (setter != null)
            underlyingObject = setter.getUnderlyingObject();

        return underlyingObject;
    }   

    // ===== Methods specified in JavaMember =====

    /**
     * Returns the Java language modifiers for the field represented by
     * this JavaMember, as an integer. The java.lang.reflect.Modifier class
     * should be used to decode the modifiers. 
     * <p> 
     * This implementation returns the underlying object of the getter method
     * if available; otherwise the one from the setter method.  
     * @return the Java language modifiers for this JavaMember
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers()
    {
        int modifiers = 0;
        
        if (getter != null)
            modifiers = getter.getModifiers();
        else if (setter != null)
            modifiers = setter.getModifiers();

        return modifiers;
    }
    
    // ===== Methods specified in JavaProperty =====

    /**
     * Returns the JavaMethod representation of the getter method for this
     * JavaProperty. If there is no getter method for this JavaProperty
     * (i.e. the property is write-only), then the method returns
     * <code>null</code>.
     * @return the getter method if available; or <code>null</code>
     * otherwise.
     */
    public JavaMethod getGetterMethod()
    {
        return getter;
    }

    /**
     * Returns the JavaMethod representation of the setter method for this
     * JavaProperty. If there is no setter method for this JavaProperty
     * (i.e. the property is read-only), then the method returns
     * <code>null</code>.
     * @return the setter method if available; or <code>null</code>
     * otherwise.
     */
    public JavaMethod getSetterMethod()
    {
        return setter;
    }

    /**
     * Returns the JavaType representation of the property type.
     * @return property type
     */
    public JavaType getType()
    {
        return type;
    }
}
