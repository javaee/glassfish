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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaMethod;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.util.I18NHelper;

/** 
 * Helper class to introspect a ReflectionJavaType representing a class to
 * find its properties.
 *
 * @author Michael Bouschen
 * @since JDO 2.0
 */
public class ReflectionJavaTypeIntrospector
{
    /** I18N support */
    private final static I18NHelper msg = I18NHelper.getInstance(
        "com.sun.org.apache.jdo.impl.model.java.Bundle"); //NOI18N
    
    /** 
     * Adds declared properties to the specified ReflectionJavaType instance. 
     * @param beanClass the class to be introspected
     */
    public void addDeclaredJavaProperties(ReflectionJavaType beanClass) 
    {
        Class clazz = beanClass.getJavaClass();
        PropertyDescriptor[] descrs = 
            getPublicAndProtectedPropertyDescriptors(clazz);
        if (descrs != null) {
            for (int i = 0; i < descrs.length; i++) {
                PropertyDescriptor descr = descrs[i];
                if (descr == null) continue;
                String name = descr.getName();
                JavaType type = 
                    beanClass.getJavaTypeForClass(descr.getPropertyType());
                Method getter = descr.getReadMethod();
                JavaMethod javaGetter = (getter == null) ? null : 
                    beanClass.createJavaMethod(getter);
                Method setter = descr.getWriteMethod();
                JavaMethod javaSetter = (setter == null) ? null : 
                    beanClass.createJavaMethod(setter);
                beanClass.createJavaProperty(name, javaGetter, 
                                             javaSetter, type);
            }
        }
    }

    // ===== Implementation using java.beans.Introspector =====

    /** 
     * Returns an array of PropertyDescriptor instances representing the
     * declared public properties of the specified class.
     * @param clazz the class to be introspected
     * @return array of PropertyDescriptor instances for declared public
     * properties.
     */
    private PropertyDescriptor[] getPublicPropertyDescriptors(Class clazz) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(
                clazz, clazz.getSuperclass());
            return beanInfo.getPropertyDescriptors();
        }
        catch (IntrospectionException ex) {
            throw new ModelFatalException(msg.msg(
                "ERR_CannotIntrospectClass", clazz.getName()), ex); //NOI18N
        }
    }

    // ===== Implementation using hand-written Introspector =====

    /**
     * Returns an array of PropertyDescriptor instances representing the
     * declared public and protected properties of the specified class.
     * @param clazz the class to be introspected
     * @return array of PropertyDescriptor instances for declared public and
     * protected properties.
     */
    private PropertyDescriptor[] getPublicAndProtectedPropertyDescriptors(
        Class clazz) {
        return new PropertyStore(clazz).getPropertyDescriptors();
    }

    /**
     * Helper class to introspect a class in order to find properties.
     * The class provides a public method {@link #getPropertyDescriptors()}
     * returning an array of PropertyDescriptors. Each PropertyDescriptor 
     * represents a public or protected property of the class specified as
     * constructor argument. This code is inspired by the implementation
     * of java.beans.Introspector class. 
     * <p>
     * Class PropertyStore uses the following algorithm to identify the
     * properties:
     * <ul>
     * <li>Iterate the declared non-static methods that are public or
     * protected.</li>
     * <li>A no-arg method returning a value and having a name staring with
     * "get" is a potential getter method of a property.</li>
     * <li>A no-arg method returning a boolean value and a name starting with
     * "is" is a potential getter method of a property.</li>
     * <li>A void method with a single argument and having a name starting
     * with "set" is a potential setter method of a property.</li>
     * <li>If there exsists an "is" and a "get" method, the "is" method is
     * used as the getter method. </li>
     * <li>If there is a getter method and multiple setter methods, it chooses
     * the setter where the argument has exactly the same type as the getter
     * return type.</li>
     * <li>If there no such matching getter method, none of the setter methods
     * correspond to a property.</li>
     * </ul>
     */
    static class PropertyStore extends HashMap {

        private static final String GET_PREFIX = "get";    //NOI18N
        private static final int    GET_PREFIX_LENGTH = 3;
        private static final String SET_PREFIX = "set";    //NOI18N
        private static final int    SET_PREFIX_LENGTH = 3;
        private static final String IS_PREFIX = "is";      //NOI18N
        private static final int    IS_PREFIX_LENGTH = 2;

        /** The declared method instances for the specified class. */
        private final Method[] declaredMethods;

        /** Constructor. */
        public PropertyStore(final Class clazz) {
            this.declaredMethods = (Method[]) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return clazz.getDeclaredMethods();
                    }});
        }

        /**
         * Returns an array of PropertyDescriptors. Each PropertyDescriptor 
         * represents a public or protected property of the class specified as
         * constructor argument.
         * @return array of public and protected properties
         */
        public PropertyDescriptor[] getPropertyDescriptors() {
            // iterate all declared methods
            for (int i = 0; i < declaredMethods.length; i++) {
                Method method = declaredMethods[i];
                int mods = method.getModifiers();
                
                // we are only interested in non-static methods that are
                // public or protected.
                if (Modifier.isStatic(mods) || 
                    (!Modifier.isPublic(mods) && !Modifier.isProtected(mods))) {
                    continue;
                }

                String name = method.getName();
                Class paramTypes[] = method.getParameterTypes();
                Class resultType = method.getReturnType();
                
                switch (paramTypes.length) {
                case 0:
                    // no args => possible getter
                    if (name.startsWith(GET_PREFIX) && 
                        resultType != void.class) {
                        String propName = Introspector.decapitalize(
                            name.substring(GET_PREFIX_LENGTH));
                        addGetter(propName, method);
                    }
                    else if (name.startsWith(IS_PREFIX) && 
                             resultType == boolean.class) {
                        String propName = Introspector.decapitalize(
                            name.substring(IS_PREFIX_LENGTH));
                        addGetter(propName, method);
                    }
                    break;
                case 1:
                    // one arg => possible setter
                    if (name.startsWith(SET_PREFIX) && 
                        resultType == void.class) {
                        String propName = Introspector.decapitalize(
                            name.substring(GET_PREFIX_LENGTH));
                        addSetter(propName, method);
                    }
                    break;
                }
            }
            
            // Now merge getters and setters
            List properties = processProperties();
            return (PropertyDescriptor[]) properties.toArray(
                new PropertyDescriptor[properties.size()]);
        }
        
        /**
         * Adds a getter method to the methods list for the property with the
         * specified name.
         * @param propName the name of the property.
         * @param method the getter method.
         */
        private void addGetter(String propName, Method method) {
            try {
                addPropertyDescriptor(
                    propName, new PropertyDescriptor(propName, method, null));
            }
            catch (IntrospectionException ex) {
                throw new ModelFatalException(
                    msg.msg("ERR_CannotCreatePropertyDescriptor", //NOI18N
                            propName, method.getName()), ex); 
            }
        }
    
        /**
         * Adds a setter method to the methods list for the property with the
         * specified name.
         * @param propName the name of the property.
         * @param method the setter method.
         */
        private void addSetter(String propName, Method method) {
            try {
                addPropertyDescriptor(
                    propName, new PropertyDescriptor(propName, null, method));
            }
            catch (IntrospectionException ex) {
                throw new ModelFatalException(
                    msg.msg("ERR_CannotCreatePropertyDescriptor", //NOI18N 
                            propName, method.getName()), ex);
            }
        }

        /**
         * Adds a the specified (incomplete) PropertyDescriptor to the list of
         * PropertyDescriptor candidates managed by this PropertyStore. The
         * method initializes the list of PropertyDescriptors, in case it is
         * the first PropertyDescriptor for the property with the specified
         * name.
         * @param propName the name of the property.
         * @param pd new PropertyDescriptor.
         */
        private synchronized void addPropertyDescriptor(
            String propName, PropertyDescriptor pd) {
            if (pd == null) {
                // nothing to be added
                return;
            }
            
            List list = (List) get(propName);
            if (list == null) {
                list = new ArrayList();
                put(propName, list);
            }
            list.add(pd);
        }
        
        /**
         * The method returns a list of PropertyDescriptors for the properties
         * managed by this PropertyStore. It iterates the all properties
         * and analyzes the candidate PropertyDescriptors (by calling method 
         * {@link #processProperty(List)}.
         * @return list of PropertyDescriptors
         */
        private synchronized List processProperties() {
            List result = new ArrayList();
            for (Iterator i = values().iterator(); i.hasNext();) {
                PropertyDescriptor pd = processProperty((List) i.next());
                if (pd != null) {
                    result.add(pd);
                }
            }
            return result;
        }
        
        /** 
         * The method analyzes the specified list of candidate
         * PropertyDescriptors and returns a single PropertyDescriptor
         * describing the property. It iterates the candidate list in order to
         * find a getter PropertyDescriptor. If there is such a
         * PropertyDescriptor it looks for a corresponding setter
         * PropertyDescriptor and updates the getter PropertyDescriptor with
         * the write method of the setter. It then returns the getter
         * PropertyDescriptor. If there is no getter PropertyDescriptor and a
         * single setter PropertyDescriptor it returns the setter
         * PropertyDescriptor. Otherwise it returns <code>null</code> which
         * means the list of candidate PropertyDescriptors does not qualify
         * for a valid property.
         * @param candidates the list of candidate PropertyDescriptors
         * @return a PropertyDescriptor describing a property or
         * <code>null</code> if the candidate PropertyDescriptors do not
         * qualify for a valid property.
         */
        private PropertyDescriptor processProperty(List candidates) {
            if (candidates == null)
                return null;
            
            PropertyDescriptor getter = null;
            PropertyDescriptor setter = null;
            
            // First iteration: check getter methods 
            for (Iterator i = candidates.iterator(); i.hasNext();) {
                PropertyDescriptor pd = (PropertyDescriptor) i.next();
                if (pd.getReadMethod() != null) {
                    if (getter != null) {
                        // Found getter, but do not overwrite "is" getter
                        // stored before
                        String name = getter.getReadMethod().getName();
                        if (!name.startsWith(IS_PREFIX)) {
                            getter = pd;
                        }
                    }
                    else {
                        // Store getter
                        getter = pd;
                    }
                }
            }
            
            // Second iteration: check setter methods. This cannot be combined
            // with the first iteration, because I need the property type of
            // the getter to find the corresponding setter.
            for (Iterator i = candidates.iterator(); i.hasNext();) {
                PropertyDescriptor pd = (PropertyDescriptor) i.next();
                if (pd.getWriteMethod() != null) {
                    if (getter != null) {
                        if (pd.getPropertyType() == getter.getPropertyType()) {
                            // Found setter that corresponds to getter => 
                            // store setter and stop iterating the candidates
                            setter = pd;
                            break;
                        }
                    }
                    else if (setter != null) {
                        // Found multiple setters w/o getter =>
                        // no property, remove stored setter
                        setter = null;
                        break;
                    }
                    else {
                        // Found setter => store it
                        setter = pd;
                    }
                }
            }
            
            // check stored getter and setter
            if (getter != null) {
                if (setter != null) {
                    // getter and setter => merge setter into getter and
                    // return getter
                    try {
                        getter.setWriteMethod(setter.getWriteMethod());
                    }
                    catch (IntrospectionException ex) {
                        throw new ModelFatalException(
                            msg.msg("ERR_CannotSetWriteMethod", //NOI18N
                                    getter.getName()), ex); 
                    }            
                }
                return getter;
            }
            return setter;
        }
    }
    
}
