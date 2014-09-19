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
package org.glassfish.hk2.configuration.persistence.properties.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileBean;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileService;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service
public class PropertyFileHandleImpl implements PropertyFileHandle {
    private final static int MAX_TRIES = 10000;
    private final static char SEPARATOR = '.';
    
    private final Object lock = new Object();
    private HashMap<TypeData, Map<String, String>> lastRead = new HashMap<TypeData, Map<String, String>>();
    private boolean open = true;
    
    private final String specificType;
    private final String defaultType;
    private final String defaultInstanceName;
    private final Hub hub;
    private final ClassReflectionHelper reflectionHelper = new ClassReflectionHelperImpl();
    
    /* package */ PropertyFileHandleImpl(String specificType, String defaultType, String defaultInstanceName, Hub hub) {
        this.specificType = emptyNull(specificType);
        this.defaultType = emptyNull(defaultType);
        this.defaultInstanceName = emptyNull(defaultInstanceName);
        this.hub = hub;
    }
    
    private static String emptyNull(String input) {
        if (input == null) return null;
        input = input.trim();
        if (input.length() <= 0) return null;
        return input;
    }
    
    private static String getDefaultType(String typeString, String defaultDefault, String defaultValue) {
        String defaultReturn = (defaultDefault == null) ? defaultValue : defaultDefault ;
        
        if (typeString == null || typeString.isEmpty()) {
            return defaultReturn;
        }
        
        return typeString;
    }
    
    private String getDefaultType(String typeString) {
        return getDefaultType((typeString == null) ? specificType : typeString, defaultType, PropertyFileService.DEFAULT_TYPE_NAME);
    }
    
    private String getDefaultInstance(String instanceString) {
        return getDefaultType(instanceString, defaultInstanceName, PropertyFileService.DEFAULT_INSTANCE_NAME);
    }
    
    private static void addMultiValue(Map<TypeData, Map<String, String>> buildMe, TypeData key, String param, String value) {
        Map<String, String> addToMe = buildMe.get(key);
        if (addToMe == null) {
            addToMe = new HashMap<String, String>();
            buildMe.put(key, addToMe);
        }
        
        addToMe.put(param, value);
    }
    
    
    private void extractData(String keyString, String value, Map<TypeData, Map<String, String>> buildMe) {
        int firstDotIndex = keyString.indexOf(SEPARATOR);
        int secondDotIndex = -1;
        if (firstDotIndex >= 0) {
            secondDotIndex = keyString.indexOf(SEPARATOR, firstDotIndex + 1);
        }
        
        if (firstDotIndex <= 0) {
            // keyString itself is the parameter name
            TypeData td = new TypeData(
                    getDefaultType(null),
                    getDefaultInstance(null));
            
            addMultiValue(buildMe, td, keyString, value);
            return;
        }
        
        // Have at least one dot
        if (secondDotIndex >= 0) {
            // Have two dots
            String typeName = keyString.substring(0, firstDotIndex);
            String instanceName = keyString.substring(firstDotIndex + 1, secondDotIndex);
            String propName = keyString.substring(secondDotIndex + 1);
            
            TypeData td = new TypeData(
                    typeName,
                    instanceName);
            
            addMultiValue(buildMe, td, propName, value);
            return;
        }
        
        // Only one dot
        String instanceName = keyString.substring(0, firstDotIndex);
        String propName = keyString.substring(firstDotIndex + 1);
        
        TypeData td = new TypeData(
                getDefaultType(null),
                instanceName);
        
        addMultiValue(buildMe, td, propName, value);
    }
    
    private void removeInstances(WriteableBeanDatabase wbd, HashMap<TypeData, Map<String, String>> allBeans) {
        HashSet<String> newReadTypes = getTypes(allBeans);
        HashSet<String> oldReadTypes = getTypes(lastRead);
        
        HashSet<String> removeTypes = new HashSet<String>(oldReadTypes);
        removeTypes.removeAll(newReadTypes);
        // removeTypes now contains all of the types that were previously added but which are now gone
        
        for (String removeType : removeTypes) {
            WriteableType wt = wbd.getWriteableType(removeType);
            if (wt == null) continue;
            
            HashSet<String> instances = getInstances(removeType, lastRead);
            for (String instance : instances) {
                wt.removeInstance(instance);
            }
        }
        
        // Now we handle types that have removed individual instances
        for (String oldType : oldReadTypes) {
            WriteableType wt = wbd.getWriteableType(oldType);
            if (wt == null) continue;
            
            if (!newReadTypes.contains(oldType)) continue;
            
            HashSet<String> newReadInstances = getInstances(oldType, allBeans);
            HashSet<String> removeOldInstances = getInstances(oldType, lastRead);
            removeOldInstances.removeAll(newReadInstances);
            // removeOldInstances now contains the set of instances that need to be removed completely
            
            for (String instance : removeOldInstances) {
                wt.removeInstance(instance);
            }
        }
    }
    
    private static String SET = "set";
    
    private static Set<String> getPossibleSetterNames(String key) {
        LinkedHashSet<String> retVal = new LinkedHashSet<String>(2);
        
        char c = key.charAt(0);
        retVal.add(SET + Character.toUpperCase(c) + key.substring(1));
        
        StringBuffer sb = new StringBuffer(SET);
        sb.append(Character.toUpperCase(c));
        
        boolean foundFirstUpper = false;
        for (int lcv = 1; lcv < key.length(); lcv++) {
            c = key.charAt(lcv);
            boolean isUpper = Character.isUpperCase(c);
            
            if (isUpper || foundFirstUpper) {
                foundFirstUpper = true;
                sb.append(c);
            }
            else {
                sb.append(Character.toUpperCase(c));
            }
        }
        
        retVal.add(sb.toString());
        
        return retVal;
    }
    
    private Method findMethod(Class<?> clazz, Set<String> possibleSetterNames) {
        Set<MethodWrapper> wrappers = reflectionHelper.getAllMethods(clazz);
        
        for (MethodWrapper wrapper : wrappers) {
            Method method = wrapper.getMethod();
            
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) continue;
            
            Class<?> parameters[] = method.getParameterTypes();
            if (parameters.length != 1) continue;
            
            String methodName = method.getName();
            for (String searchName : possibleSetterNames) {
                if (methodName.equals(searchName)) {
                    return method;
                }
            }
        }
        
        return null;
    }
    
    private Object convertValue(String value, Class<?> intoMe) {
        if (value == null) return value;
        
        if (String.class.equals(intoMe)) return value;
        if (boolean.class.equals(intoMe) || Boolean.class.equals(intoMe)) {
            return Boolean.parseBoolean(value);
        }
        if (short.class.equals(intoMe) || Short.class.equals(intoMe)) {
            return Short.parseShort(value);
        }
        if (int.class.equals(intoMe) || Integer.class.equals(intoMe)) {
            return Integer.parseInt(value);
        }
        if (long.class.equals(intoMe) || Long.class.equals(intoMe)) {
            return Long.parseLong(value);
        }
        if (float.class.equals(intoMe) || Float.class.equals(intoMe)) {
            return Float.parseFloat(value);
        }
        if (byte.class.equals(intoMe) || Byte.class.equals(intoMe)) {
            return Byte.parseByte(value);
        }
        if (double.class.equals(intoMe) || Double.class.equals(intoMe)) {
            return Double.parseDouble(value);
        }
        if (char.class.equals(intoMe) || Character.class.equals(intoMe)) {
            if (value.length() < 0) return ((char) 0);
            return value.charAt(0);
        }
        
        // OK, none of the normal ones, lets see if it has a public constructor
        // that takes a String
        Constructor<?> constructor;
        try {
            constructor = intoMe.getConstructor(String.class);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not convert value " + value + " into class " + intoMe.getName());
        }
        
        try {
            return constructor.newInstance(value);
        }
        catch (InstantiationException ie) {
            throw new IllegalArgumentException("Could not create value " + value + " from class " + intoMe.getName(), ie);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not create value " + value + " from class " + intoMe.getName(), e);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create value " + value + " from class " + intoMe.getName(), e);
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Could not create value " + value + " from class " + intoMe.getName(), e.getTargetException());
        }
    }
    
    private Object convertBean(String typeName, Map<String, String> rawBean) {
        Instance instance = hub.getCurrentDatabase().getInstance(
                PropertyFileBean.TYPE_NAME,
                PropertyFileBean.INSTANCE_NAME);
        PropertyFileBean propertyFileBean = (PropertyFileBean) ((instance == null) ? null : instance.getBean());
        if (propertyFileBean == null) return rawBean;
        
        Class<?> beanClass = propertyFileBean.getTypeMapping(typeName);
        if (beanClass == null) return rawBean;
        
        // OK, at this point we need to convert the map to a real bean
        try {
            Object target = beanClass.newInstance();
            
            for (Map.Entry<String, String> entry : rawBean.entrySet()) {
                String key = entry.getKey();
                
                // Could be two of them
                Set<String> possibleSetterNames = getPossibleSetterNames(key);
                Method method = findMethod(beanClass, possibleSetterNames);
                if (method == null) {
                    throw new IllegalArgumentException("Could not find a setter for property names " + Pretty.collection(possibleSetterNames));
                }
                
                Class<?> methodParamType = method.getParameterTypes()[0];
                
                Object params[] = new Object[1];
                params[0] = convertValue(entry.getValue(), methodParamType);
                
                method.invoke(target, params);
            }
            
            return target;
        }
        catch (Throwable th) {
            throw new IllegalArgumentException("Error converting to bean type " + beanClass.getName(), th);
        }
        
        
    }
    
    private void modifyValues(WriteableBeanDatabase wbd, HashMap<TypeData, Map<String, String>> allBeans) {
        for (Map.Entry<TypeData, Map<String, String>> entry : lastRead.entrySet()) {
            TypeData oldKey = entry.getKey();
            if (!allBeans.containsKey(oldKey)) return;
            
            Map<String, String> newBean = allBeans.get(oldKey);
            
            String type = oldKey.typeName;
            String instance = oldKey.instanceName;
            
            Object convertedNewBean = convertBean(type, newBean);
            
            WriteableType wt = wbd.findOrAddWriteableType(type);
            if (wt.getInstance(instance) == null) {
                wt.addInstance(instance, convertedNewBean);
            }
            else {
                wt.modifyInstance(instance, convertedNewBean);
            }
        }
    }
    
    private void addValues(WriteableBeanDatabase wbd, HashMap<TypeData, Map<String, String>> allBeans) {
        for (Map.Entry<TypeData, Map<String, String>> entry : allBeans.entrySet()) {
            TypeData newKey = entry.getKey();
            if (lastRead.containsKey(newKey)) {
                // This was a modification, not an add
                continue;
            }
            
            String typeName = newKey.typeName;
            String instanceName = newKey.instanceName;
            
            Object convertedNewBean = convertBean(typeName, entry.getValue());
            
            WriteableType wt = wbd.findOrAddWriteableType(typeName);
            if (wt.getInstance(instanceName) != null) {
                wt.modifyInstance(instanceName, convertedNewBean);
            }
            else {
                wt.addInstance(instanceName, convertedNewBean);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#readProperties(java.util.Properties)
     */
    @Override
    public void readProperties(Properties properties) {
        if (properties == null) throw new IllegalArgumentException();
        
        HashMap<TypeData, Map<String, String>> allBeans = new HashMap<TypeData, Map<String, String>>();
        for (Object fullKey : properties.keySet()) {
            if (!(fullKey instanceof String)) continue;
            
            String sFullKey = (String) fullKey;
            String value = properties.getProperty(sFullKey);
            extractData(sFullKey, value, allBeans);
        }
        
        synchronized (lock) {
            if (!open) {
                throw new IllegalStateException("This handle has been closed");
            }
            
            boolean success = false;
            for (int lcv = 0; lcv < MAX_TRIES; lcv++) {
                WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
            
                removeInstances(wbd, allBeans);
            
                modifyValues(wbd, allBeans);
            
                addValues(wbd, allBeans);
            
                try {
                    wbd.commit();
                    success = true;
                    break;
                }
                catch (IllegalStateException ise) {
                    // Lost race, try again
                }
            }
        
            if (!success) {
                throw new IllegalStateException("Could not update database after " + MAX_TRIES + " iterations");
            }
            
            lastRead = allBeans;
        }
    }
    
    private static HashSet<String> getTypes(HashMap<TypeData, Map<String, String>> lastRead) {
        HashSet<String> retVal = new HashSet<String>();
        
        for (TypeData td : lastRead.keySet()) {
            retVal.add(td.typeName);
        }
        
        return retVal;
    }
    
    private static HashSet<String> getInstances(String typeName, HashMap<TypeData, Map<String, String>> lastRead) {
        HashSet<String> retVal = new HashSet<String>();
        
        for (TypeData td : lastRead.keySet()) {
            if (td.typeName.equals(typeName)) {
                retVal.add(td.instanceName);
            }
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#getSpecificType()
     */
    @Override
    public String getSpecificType() {
        return specificType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#getDefaultType()
     */
    @Override
    public String getDefaultType() {
        return defaultType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#getDefaultInstanceName()
     */
    @Override
    public String getDefaultInstanceName() {
        return defaultInstanceName;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle#dispose()
     */
    @Override
    public void dispose() {
        synchronized (lock) {
            if (!open) return;
            open = false;
            
            reflectionHelper.dispose();
            
            HashMap<TypeData, Map<String, String>> allBeans = new HashMap<TypeData, Map<String, String>>();
            
            for (int lcv = 0; lcv < MAX_TRIES; lcv++) {
                WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
                
                removeInstances(wbd, allBeans);
                
                try {
                    wbd.commit();
                    break;
                }
                catch (IllegalStateException ise) {
                    // keep on truckin
                }
            }
            
            // success or not
            lastRead = allBeans;
        }

    }
    
    private static class TypeData {
        private final String typeName;
        private final String instanceName;
        private final int hashCode;
        
        private TypeData(String typeName,
                String instanceName) {
            this.typeName = typeName;
            this.instanceName = instanceName;
            
            hashCode = typeName.hashCode() ^ instanceName.hashCode();
        }
        
        public int hashCode() {
            return hashCode;
        }
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof TypeData)) return false;
            TypeData other = (TypeData) o;
            
            return (typeName.equals(other.typeName) &&
                    instanceName.equals(other.instanceName));
        }
    }

}
