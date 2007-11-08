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


package com.sun.persistence.deployment.impl.reflection;

import com.sun.persistence.api.deployment.JavaModel.FieldOrProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * This class represents either a persistent property or persistent field. This
 * implements {@link FieldOrProperty} using reflection. This is used during
 * deployment process. This class uses covariant return type in overriding
 * method signatures. It returns {@link java.lang.Class} as the JavaType where
 * as {@link FieldOrProperty} returns Object as the JavaType.
 *
 * @author Sanjeeb Sahoo
 */
public class FieldOrPropertyImpl implements AnnotatedElement, FieldOrProperty {

    /* name of this member */
    private String name;

    /* not null, iff this is a field */
    private Field field;

    /* not null, iff this is a method */
    private Method getter;

    FieldOrPropertyImpl(String name, Field f) {
        this.name = name;
        field = f;
    }

    FieldOrPropertyImpl(String name, Method getter) {
        this.name = name;
        this.getter = getter;
    }

    /**
     * @return true iff this is a field
     */
    public boolean isField() {
        return field != null;
    }

    /**
     * @return true iff this is a method
     */
    public boolean isMethod() {
        return field != null;
    }

    /**
     * @return the name of this property. Applies JavaBean rules for naming.
     */
    public String getName() {
        return name;
    }

    /**
     * @see java.lang.reflect.Field#getGenericType()
     * @see java.lang.reflect.Method#getGenericReturnType()
     */
    public Type getGenericType() {
        if (isField())
            return field.getGenericType();
        else
            return getter.getGenericReturnType();
    }

    /**
     * @see java.lang.reflect.Field#getType()
     * @see java.lang.reflect.Method#getReturnType()
     */
    public java.lang.Class<?> getJavaType() {
        if (isField()) {
            return field.getType();
        } else {
            return getter.getReturnType();
        }
    }

    public Object getUnderlyingObject() {
        if (isField()) {
            return field;
        } else {
            return getter;
        }
    }

    // AnnotatedInterface implementation methods...

    /**
     * {@inheritDoc}
     */
    public <T extends Annotation> T getAnnotation(
            java.lang.Class<T> annotationType) {
        return getAnnotatedElement().getAnnotation(annotationType);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAnnotationPresent(
            java.lang.Class<? extends java.lang.annotation.Annotation> annotationType) {
        return getAnnotatedElement().isAnnotationPresent(annotationType);
    }

    /**
     * {@inheritDoc}
     */
    public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
        return getAnnotatedElement().getDeclaredAnnotations();
    }

    /**
     * {@inheritDoc}
     */
    public java.lang.annotation.Annotation[] getAnnotations() {
        return getAnnotatedElement().getAnnotations();
    }

    private AnnotatedElement getAnnotatedElement() {
        if (isField())
            return field;
        else
            return getter;
    }

}
