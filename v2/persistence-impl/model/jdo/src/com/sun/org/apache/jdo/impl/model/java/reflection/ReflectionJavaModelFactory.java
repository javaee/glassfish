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

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.impl.model.java.AbstractJavaModelFactory;
import com.sun.org.apache.jdo.impl.model.java.BaseReflectionJavaType;
import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * A reflection based JavaModelFactory implementation. 
 * The implementation takes <code>java.lang.Class</code> and
 * <code>java.lang.reflect.Field</code> instances to get Java related
 * metadata about types and fields. This implementation caches JavaModel
 * instances per ClassLoader.
 * 
 * @author Michael Bouschen
 * @since JDO 2.0
 */
public abstract class ReflectionJavaModelFactory
    extends AbstractJavaModelFactory
{    
    /** I18N support */
    private final static I18NHelper msg =  
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.model.java.Bundle"); //NOI18N

    /**
     * Creates a new empty JavaModel instance. A factory implementation may
     * use the specified key when caching the new JavaModel instance. 
     * <p>
     * This implementation only accepts <code>java.lang.ClassLoader</code>
     * instances as key and does not support <code>null</code> keys. A
     * ModelException indicates an invalid key.
     * <p>
     * The method automatically sets the parent/child relationship for the
     * created JavaModel according to the parent/child relationship of the 
     * ClassLoader passed as key. 
     * @param key the key that may be used to cache the returned JavaModel
     * instance. 
     * @return a new JavaModel instance.
     * @exception ModelException if impossible; the key is of an
     * inappropriate type or the key is <code>null</code> and this
     * JavaModelFactory does not support <code>null</code> keys.
     */
    public JavaModel createJavaModel(Object key)
        throws ModelException
    {
        if ((key == null) || (!(key instanceof ClassLoader)))
            throw new ModelException(msg.msg("EXC_InvalidJavaModelKey", //NOI18N
                (key==null?"null":key.getClass().getName()))); //NOI18N
        
        ClassLoader classLoader = (ClassLoader)key;
        JavaModel javaModel = newJavaModelInstance(classLoader);

        // check parent <-> child relationship
        if (classLoader != ClassLoader.getSystemClassLoader()) {
            // if the specified classLoader is not the system class loader
            // try to get the parent class loader and update the parent property
            try {
                ClassLoader parentClassLoader = classLoader.getParent();
                if (parentClassLoader != null) {
                    javaModel.setParent(getJavaModel(parentClassLoader));
                }
            }
            catch (SecurityException ex) {
                // ignore => parentClassLoader and parent JavaModel are null
            }
        }

        return javaModel;
    }

    /**
     * Returns the JavaModel instance for the specified key.
     * @param key the key used to cache the returned JavaModel instance
     */
    public JavaModel getJavaModel(Object key)
    {
        if (key == null) {
            // null classLoader might happen for classes loaded by the
            // bootstrap class loder
            key = ClassLoader.getSystemClassLoader();
        }
        return super.getJavaModel(key);
    }
    
    /**
     * Returns a JavaType instance for the specified type description
     * (optional operation). This method is a convenience method and a
     * short cut for <code>getJavaModel(key).getJavaType(typeName)</code>.
     * <p>
     * The ReflectionJavaModelFactory supports this short cut and accepts
     * <code>java.lang.Class</code> instances as valid arguments for this
     * method. The method throws a 
     * {@link org.apache.jdo.model.ModelFatalException}, if the specified
     * type descriptor is not a <code>java.lang.Class</code> instance. 
     * @param typeDesc the type description
     * @return a JavaType instance for the specified type.
     * @exception ModelFatalException the specified type description is not
     * a <code>java.lang.Class</code> instance.
     */
    public JavaType getJavaType(Object typeDesc)
    {
        if (typeDesc == null)
            return null;

        try {
            Class clazz = (Class)typeDesc;
            ClassLoader classLoader = getClassLoaderPrivileged(clazz);
            return getJavaModel(classLoader).getJavaType(clazz);
        }
        catch (ClassCastException ex) {
            throw new ModelFatalException(msg.msg("EXC_InvalidTypeDesc", //NOI18N
                typeDesc.getClass().getName()));
        }
    }

    // ===== Methods not defined in JavaModelFactory =====

    /**
     * Calls getClassLoader on the specified class instance in a
     * doPrivileged block. Any SecurityException is wrapped into a
     * ModelFatalException. 
     * @return the class loader that loaded the specified class instance.
     * @exception ModelFatalException wraps the SecurityException thrown by
     * getClassLoader.
     */
    public ClassLoader getClassLoaderPrivileged(final Class clazz)
    {
        if (clazz == null)
            return null;

        try { 
            return (ClassLoader) AccessController.doPrivileged(
                new PrivilegedAction () {
                    public Object run () {
                        return clazz.getClassLoader();
                    }
                }
                );
        }
        catch (SecurityException ex) {
            throw new ModelFatalException(
                msg.msg("EXC_CannotGetClassLoader", clazz), ex); //NOI18N
        }
    }

    /**
     * Returns the <code>java.lang.Class</code> wrapped in the specified 
     * JavaType. 
     * @return the <code>java.lang.Class</code> for the specified
     * JavaType. 
     * @exception ModelFatalException the specified JavaType does
     * not wrap a <code>java.lang.Class</code> instance.
     */
    public Class getJavaClass(JavaType javaType) 
    {
        if (javaType == null)
            return null;
        
        try {
            return ((BaseReflectionJavaType)javaType).getJavaClass();
        }
        catch (ClassCastException ex) {
            throw new ModelFatalException(msg.msg(
                "EXC_InvalidJavaType", javaType.getClass())); //NOI18N
        }
    }

    //========= Internal helper methods ==========
    
    /** 
     * Creates a new instance of the JavaModel implementation class.
     * <p>
     * This implementation returns a <code>ReflectionJavaModel</code>
     * instance.
     * @return a new JavaModel instance.
     */
    protected JavaModel newJavaModelInstance(ClassLoader classLoader) {
        return new ReflectionJavaModel(classLoader, this);
    }
}
