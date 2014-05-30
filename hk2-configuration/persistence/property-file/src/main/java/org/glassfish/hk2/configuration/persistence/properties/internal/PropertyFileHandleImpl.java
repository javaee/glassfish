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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileHandle;
import org.glassfish.hk2.configuration.persistence.properties.PropertyFileService;
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
    
    private void modifyValues(WriteableBeanDatabase wbd, HashMap<TypeData, Map<String, String>> allBeans) {
        for (Map.Entry<TypeData, Map<String, String>> entry : lastRead.entrySet()) {
            TypeData oldKey = entry.getKey();
            if (!allBeans.containsKey(oldKey)) return;
            
            Map<String, String> newBean = allBeans.get(oldKey);
            
            String type = oldKey.typeName;
            String instance = oldKey.instanceName;
            
            WriteableType wt = wbd.findOrAddWriteableType(type);
            if (wt.getInstance(instance) == null) {
                wt.addInstance(instance, newBean);
            }
            else {
                wt.modifyInstance(instance, newBean);
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
            
            WriteableType wt = wbd.findOrAddWriteableType(typeName);
            if (wt.getInstance(instanceName) != null) {
                wt.modifyInstance(instanceName, entry.getValue());
            }
            else {
                wt.addInstance(instanceName, entry.getValue());
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
