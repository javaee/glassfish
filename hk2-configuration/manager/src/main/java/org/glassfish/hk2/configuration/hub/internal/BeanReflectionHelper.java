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
package org.glassfish.hk2.configuration.hub.internal;

import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.configuration.hub.api.BeanDatabase;
import org.glassfish.hk2.configuration.hub.api.Type;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.Pretty;

/**
 * @author jwells
 *
 */
public class BeanReflectionHelper {
    private final static String GET = "get";
    private final static String IS = "is";
    
    /**
     * Returns the property name if this is a getter
     * 
     * @param method The method to investigate for being a property
     * @return The property name or null if not a getter
     */
    private static String isAGetter(MethodWrapper method) {
        Method m = method.getMethod();
        String name = m.getName();
        
        if (void.class.equals(m.getReturnType())) {
            // Only methods that return something
            return null;
        }
        
        Class<?> params[] = m.getParameterTypes();
        if (params.length != 0) {
            // Only methods with no arguments
            return null;
        }
        
        if ((m.getModifiers() & Modifier.PUBLIC) == 0) {
            // Only public methods
            return null;
        }
        
        int capIndex;
        if (name.startsWith(GET) && (name.length() > GET.length())) {
            capIndex = GET.length();
        }
        else if (name.startsWith(IS) && (name.length() > IS.length())) {
            capIndex = IS.length();
        }
        else {
            // Only method that start with is or get
            return null;
        }
        
        if (!Character.isUpperCase(name.charAt(capIndex))) {
            // Only methods whose next letter is uppercase
            return null;
        }
        
        String rawPropName = name.substring(capIndex);
        
        return Introspector.decapitalize(rawPropName);
    }
    
    private static Method findMethod(Method m, Class<?> c) {
        String name = m.getName();
        Class<?> params[] = new Class<?>[0];
        
        try {
            return c.getMethod(name, params);
        }
        catch (Throwable th) {
            return null;
        }
    }
    
    private static Object getValue(Object bean, Method m) {
        try {
            return m.invoke(bean, new Object[0]);
        }
        catch (Throwable th) {
            return null;
        }
    }
    
    private static PropertyChangeEvent[] getMapChangeEvents(Map<String, Object> oldBean, Map<String, Object> newBean) {
        LinkedList<PropertyChangeEvent> retVal = new LinkedList<PropertyChangeEvent>();
        
        for (Map.Entry<String, Object> entry : oldBean.entrySet()) {
            String key = entry.getKey();
            Object oldValue = entry.getValue();
            Object newValue = newBean.get(key);
            
            if (!GeneralUtilities.safeEquals(oldValue, newValue)) {
                retVal.add(new PropertyChangeEvent(newBean,
                    key,
                    oldValue,
                    newValue));
            }
        }
        
        return retVal.toArray(new PropertyChangeEvent[retVal.size()]);
    }
    
    @SuppressWarnings("unchecked")
    public static PropertyChangeEvent[] getChangeEvents(ClassReflectionHelper helper, Object oldBean, Object newBean) {
        if (oldBean instanceof Map) {
            return getMapChangeEvents((Map<String, Object>) oldBean, (Map<String, Object>) newBean);
        }
        
        LinkedList<PropertyChangeEvent> retVal = new LinkedList<PropertyChangeEvent>();
        
        Set<MethodWrapper> methods = helper.getAllMethods(oldBean.getClass());
        
        for (MethodWrapper wrapper : methods) {
            String propName = isAGetter(wrapper);
            if (propName == null) continue;
            
            Method method = wrapper.getMethod();
            
            Method newMethod = findMethod(method, newBean.getClass());
            if (newMethod == null) continue;
            
            Object oldValue = getValue(oldBean, method);
            Object newValue = getValue(newBean, newMethod);
            
            if (GeneralUtilities.safeEquals(oldValue, newValue)) continue;
            
            // Has changed!
            retVal.add(new PropertyChangeEvent(newBean,
                    propName,
                    oldValue,
                    newValue));
        }
        
        return retVal.toArray(new PropertyChangeEvent[retVal.size()]);
    }
    
    public static void dumpDatabase(BeanDatabase database, PrintStream stream) {
        for (Type type : database.getAllTypes()) {
            Set<String> instanceNames = type.getInstances().keySet();
            
            stream.println(type.getName() + " -> " + Pretty.collection(instanceNames));
        }
        
    }

}
