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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Field;

import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * This class provides a basic JavaField implementation using a reflection
 * Field instance. The implementation supports lazy initialization of the
 * wrapped reflection field instance (see 
 * {@link #BaseReflectionJavaField(String fieldName, JavaType declaringClass)}.
 * <p>
 * Note, this implementation is not connected to a JavaModelFactory, thus
 * it can only support predefined types as field types.
 * @see PredefinedType
 * @author Michael Bouschen
 * @since JDO 1.1
 * @version JDO 2.0
 */
public class BaseReflectionJavaField
    extends AbstractJavaMember
    implements JavaField
{
    /** The wrapped java.lang.reflect.Field instance. */
    private Field field;

    /** The type of the field. */
    protected JavaType type;

    /** I18N support */
    private final static I18NHelper msg = 
        I18NHelper.getInstance(BaseReflectionJavaField.class);

    /** 
     * Constructor taking a reflection field representation. The specifie
     * field must not be <code>null</code>. 
     * @param field the java.lang.reflect.Field instance
     * @param declaringClass the JavaType of the declaring class or interface.
     */
    protected BaseReflectionJavaField(Field field, JavaType declaringClass)
    {
        super((field == null) ? null : field.getName(), declaringClass);
        if (field == null)
            throw new ModelFatalException(msg.msg(
                "ERR_InvalidNullFieldInstance", "BaseReflectionJavaField.<init>")); //NOI18N
        this.field = field;
    }
    
    /** 
     * Constructor taking the field name. This constructor allows lazy
     * initialization of the field reference. 
     * @param fieldName the name of the field.
     * @param declaringClass the JavaType of the declaring class or interface.
     */
    protected BaseReflectionJavaField(String fieldName, JavaType declaringClass)
    {
        super(fieldName, declaringClass);
    }

    // ===== Methods specified in JavaElement =====

    /**
     * Returns the environment specific instance wrapped by this JavaModel
     * element. This implementation returns the
     * <code>java.lang.reflect.Field</code> instance for this JavaField.
     * @return the environment specific instance wrapped by this JavaModel
     * element.
     */
    public Object getUnderlyingObject() 
    {
        return getField();
    }

    // ===== Methods specified in JavaMember =====

    /**
     * Returns the Java language modifiers for the field represented by
     * this JavaField, as an integer. The java.lang.reflect.Modifier class
     * should be used to decode the modifiers. 
     * @return the Java language modifiers for this JavaField
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers()
    {
        ensureInitializedField();
        return field.getModifiers();
    }

    // ===== Methods specified in JavaField =====

    /**
     * Returns the JavaType representation of the field type.
     * @return field type
     */
    public JavaType getType()
    {
        if (type == null) {
            ensureInitializedField();
            String typeName = field.getType().getName();
            // Note, this only checks for predefined types!
            type = PredefinedType.getPredefinedType(typeName);
        }
        return type;
    }
    
    // ===== Methods not specified in JavaField =====

    /** 
     * Returns the java.lang.reflect.Field that is wrapped by this
     * JavaField.
     * @return the java.lang.reflect.Field instance.
     */
    protected Field getField()
    {
        ensureInitializedField();
        return this.field;
    }

    /**
     * Helper method to retrieve the java.lang.reflect.Field for the specified
     * field name.
     * @param clazz the Class instance of the declaring class or interface
     * @param fieldName the field name
     * @return the java.lang.reflect.Field for the specified field name.
     */
    public static Field getDeclaredFieldPrivileged(final Class clazz, 
                                                   final String fieldName)
    {
        if ((clazz == null) || (fieldName == null))
            return null;

        return (Field) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run () {
                    try {
                        return clazz.getDeclaredField(fieldName);
                    }
                    catch (SecurityException ex) {
                        throw new ModelFatalException(
                            msg.msg("EXC_CannotGetDeclaredField", //NOI18N
                                    clazz.getName()), ex); 
                    }
                    catch (NoSuchFieldException ex) {
                        return null; // do nothing, just return null
                    }
                    catch (LinkageError ex) {
                        throw new ModelFatalException(msg.msg(
                           "EXC_ClassLoadingError", clazz.getName(), //NOI18N
                           ex.toString()));
                    }
                }
            }
            );
    }

    /**
     * Helper method to retrieve the declared java.lang.reflect.Field
     * instances for the specified class.
     * @param clazz the Class instance of the declaring class or interface
     * @return the java.lang.reflect.Field instances for the declared fields
     * of the specified class.
     */
    public static Field[] getDeclaredFieldsPrivileged(final Class clazz)
    {
        if (clazz == null)
            return null;

        return (Field[]) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run () {
                    try {
                        return clazz.getDeclaredFields();
                    }
                    catch (SecurityException ex) {
                        throw new ModelFatalException(
                            msg.msg("EXC_CannotGetDeclaredFields", //NOI18N
                                    clazz.getName()), ex); 
                    }
                    catch (LinkageError ex) {
                        throw new ModelFatalException(msg.msg(
                           "EXC_ClassLoadingError", clazz.getName(), //NOI18N
                           ex.toString()));
                    }
                }
            }
            );
    }

    // ===== Internal helper methods =====
    
    /**
     * This method makes sure the reflection field is set.
     */
    protected void ensureInitializedField()
    {
        if (this.field == null) {
            this.field = getDeclaredFieldPrivileged(
                ((BaseReflectionJavaType)getDeclaringClass()).getJavaClass(),
                getName());
            if (field == null) {
                throw new ModelFatalException(msg.msg(
                    "ERR_MissingFieldInstance", //NOI18N
                    "BaseReflectionJavaField.ensureInitializedField", getName())); //NOI18N
            }
        }
    }

}
