/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.configuration.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.glassfish.hk2.configuration.api.Configured;
import org.glassfish.hk2.configuration.api.Dynamicity;

/**
 * For JavaBean or Bean-Like-Map utilities
 * 
 * @author jwells
 *
 */
public class BeanUtilities {
    private final static String GET = "get";
    private final static String IS = "is";
    
    private static String firstUpper(String s) {
        if (s == null || s.length() <= 0) {
            return s;
        }
        
        char firstChar = Character.toUpperCase(s.charAt(0));
        
        return firstChar + s.substring(1);
    }
    
    /**
     * Gets the value from the given attribute from the given bean
     * Safe to give both a bean-like map and a java bean
     * 
     * @param attribute
     * @param bean
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object getBeanPropertyValue(String attribute, BeanInfo beanInfo) {
        if (Configured.BEAN_KEY.equals(attribute)) return beanInfo.getBean();
        if (Configured.TYPE.equals(attribute)) return beanInfo.getTypeName();
        if (Configured.INSTANCE.equals(attribute)) return beanInfo.getInstanceName();
        
        Object bean = beanInfo.getBean();
        if (bean instanceof Map) {
            
            Map<String, Object> beanLikeMap = (Map<String, Object>) bean;
            return beanLikeMap.get(attribute);
        }
        
        attribute = firstUpper(attribute);
        
        String methodName = GET + attribute;
        
        Class<?> beanClass = bean.getClass();
        
        Method m = null;
        try {
            m = beanClass.getMethod(methodName, new Class[0]);
        }
        catch (NoSuchMethodException me) {
            methodName = IS + attribute;
            
            try {
                m = beanClass.getMethod(methodName, new Class[0]);
            }
            catch (NoSuchMethodException me2) {
                throw new IllegalArgumentException("The bean " + bean + " has no getter for attribute " + attribute);
            }
        }
        
        m.setAccessible(true);
        
        try {
            return m.invoke(bean, new Object[0]);
        }
        catch (InvocationTargetException e) {
            Throwable th = e.getTargetException();
            throw new IllegalStateException(th);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
        
    }
    
    private final static String EMPTY = "";
    
    public static boolean isEmpty(String s) {
        if (s == null) return true;
        return EMPTY.equals(s);
    }
    
    /**
     * Gets the parameter name from a field
     * 
     * @param f
     * @return
     */
    public static String getParameterNameFromField(Field f, boolean onlyDynamic) {
        Configured c = f.getAnnotation(Configured.class);
        if (c == null) return null;
        
        if (onlyDynamic && !Dynamicity.FULLY_DYNAMIC.equals(c.dynamicity())) {
            return null;
        }
        
        String key = c.value();
        if (isEmpty(key)) {
            key = f.getName();
        }
        
        return key;
    }
    
    public static String getParameterNameFromMethod(Method m, int paramIndex) {
        Annotation annotations[] = m.getParameterAnnotations()[paramIndex];
        
        for (Annotation annotation : annotations) {
            if (Configured.class.equals(annotation.annotationType())) {
                Configured configured = (Configured) annotation;
                if (!Dynamicity.FULLY_DYNAMIC.equals(configured.dynamicity())) return null;
                
                String retVal = ((Configured) annotation).value();
                if (isEmpty(retVal)) return null;
                return retVal;
            }
        }
        
        return null;
    }
    
    public static boolean hasDynamicParameter(Method m) {
        for (Annotation annotations[] : m.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (Configured.class.equals(annotation.annotationType())) {
                    Configured configured = (Configured) annotation;
                    
                    if (Dynamicity.FULLY_DYNAMIC.equals(configured.dynamicity())) return true;
                }
            }
        }
        
        return false;
    }

}
