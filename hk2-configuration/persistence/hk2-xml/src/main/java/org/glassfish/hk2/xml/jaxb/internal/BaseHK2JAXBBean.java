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
package org.glassfish.hk2.xml.jaxb.internal;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.BeanReflectionHelper;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlHubCommitMessage;
import org.glassfish.hk2.xml.api.annotations.Customizer;
import org.glassfish.hk2.xml.internal.DynamicChangeInfo;
import org.glassfish.hk2.xml.internal.ParentedNode;
import org.glassfish.hk2.xml.internal.UnparentedNode;
import org.glassfish.hk2.xml.internal.Utilities;

/**
 * @author jwells
 *
 */
public class BaseHK2JAXBBean implements XmlHk2ConfigurationBean, Serializable {
    private static final long serialVersionUID = 8149986319033910297L;

    private final static boolean DEBUG_GETS_AND_SETS = Boolean.parseBoolean(GeneralUtilities.getSystemProperty(
            "org.jvnet.hk2.properties.xmlservice.jaxb.getsandsets", "false"));
    
    private final static String EMPTY = "";
    private final static char XML_PATH_SEPARATOR = '/';
    private final static String NOT_UNIQUE_UNIQUE_ID = "not-unique";
    
    /**
     * All fields, including child lists and direct children
     */
    private final ConcurrentHashMap<String, Object> beanLikeMap = new ConcurrentHashMap<String, Object>();
    
    /**
     * All children whose type has an identifier.  First key is the xml parameter name, second
     * key is the identifier of the specific child.  Used in lookup operations
     */
    private final Map<String, Map<String, BaseHK2JAXBBean>> children = new HashMap<String, Map<String, BaseHK2JAXBBean>>();
    
    /** The model for this, including lists of all children property names */
    private UnparentedNode model;
    
    /** The parent of this instance, or null if this is a root (or has not been fully initialized yet) */
    private Object parent;
    
    /** My own XmlTag, which is determined either by my parent or by my root value */
    private String selfXmlTag;
    
    /** The full instance name this takes, with names from keyed children or ids from unkeyed multi children */
    private String instanceName;
    
    /** The value of my key field, if I have one */
    private String keyValue;
    
    /** The global classReflectionHelper, which minimizes reflection */
    private ClassReflectionHelper classReflectionHelper;
    
    /** My own full xmlPath from root */
    private String xmlPath = EMPTY;
    
    /**
     * This object contains the tree locks
     * Once this has been set then all other fields should have been set,
     * and at that point this object is ready for its life as an
     * in-memory node in a tree hierarchy
     */
    private volatile transient DynamicChangeInfo changeControl;
    
    /**
     * For JAXB and Serialization
     */
    public BaseHK2JAXBBean() {
    }
    
    @SuppressWarnings("unchecked")
    public void _setProperty(String propName, Object propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        if (DEBUG_GETS_AND_SETS) {
            // Hidden behind static because of potential expensive toString costs
            Logger.getLogger().debug("XmlService setting property " + propName + " to " + propValue + " in " + this);
        }
        
        if (propValue != null && (propValue instanceof List)) {
            // All lists are unmodifiable and ArrayLists
            if (propValue instanceof ArrayList) {
                propValue = Collections.unmodifiableList((ArrayList<Object>) propValue);
            }
            else {
                propValue = Collections.unmodifiableList(new ArrayList<Object>((List<Object>) propValue));
            }
        }
        
        if (changeControl == null) {
            beanLikeMap.put(propName, propValue);
        }
        else {
            changeControl.getWriteLock().lock();
            try {
                changeInHub(propName, propValue);
                
                beanLikeMap.put(propName, propValue);
            }
            finally {
                changeControl.getWriteLock().unlock();
            }
        }
    }
    
    public void _setProperty(String propName, byte propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Byte) propValue);
    }
    
    public void _setProperty(String propName, boolean propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Boolean) propValue);
    }
    
    public void _setProperty(String propName, char propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Character) propValue);
    }
    
    public void _setProperty(String propName, short propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Short) propValue);
    }
    
    public void _setProperty(String propName, int propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Integer) propValue);
    }
    
    public void _setProperty(String propName, float propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Float) propValue);
    }
    
    public void _setProperty(String propName, long propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Long) propValue);
    }
    
    public void _setProperty(String propName, double propValue) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        _setProperty(propName, (Double) propValue);
    }
    
    public Object _getProperty(String propName) {
        Object retVal;
        if (changeControl == null) {
            retVal = beanLikeMap.get(propName);
        }
        else {
            changeControl.getReadLock().lock();
            try {
                retVal = beanLikeMap.get(propName);
            }
            finally {
                changeControl.getReadLock().unlock();
            }
        }
        
        if (DEBUG_GETS_AND_SETS) {
            // Hidden behind static because of potential expensive toString costs
            Logger.getLogger().debug("XmlService getting property " + propName + "=" + retVal + " in " + this);
        }
        
        return retVal;
    }
    
    public boolean _getPropertyZ(String propName) {
        return (Boolean) _getProperty(propName);
    }
    
    public byte _getPropertyB(String propName) {
        return (Byte) _getProperty(propName);
    }
    
    public char _getPropertyC(String propName) {
        return (Character) _getProperty(propName);
    }
    
    public short _getPropertyS(String propName) {
        return (Short) _getProperty(propName);
    }
    
    public int _getPropertyI(String propName) {
        return (Integer) _getProperty(propName);
    }
    
    public float _getPropertyF(String propName) {
        return (Float) _getProperty(propName);
    }
    
    public long _getPropertyJ(String propName) {
        return (Long) _getProperty(propName);
    }
    
    public double _getPropertyD(String propName) {
        return (Double) _getProperty(propName);
    }
    
    @SuppressWarnings("unchecked")
    private Object internalLookup(String propName, String keyValue) {
        // First look in the cache
        Object retVal = null;
        
        Map<String, BaseHK2JAXBBean> byName = children.get(propName);
        if (byName != null) {
            retVal = byName.get(keyValue);
        }
        
        
        if (retVal != null) {
            // Found it in cache!
            return retVal;
        }
        
        // Now do it the hard way
        Object prop = _getProperty(propName);
        if (prop == null) return null;  // Just not found
        
        if (prop instanceof List) {
            for (BaseHK2JAXBBean child : (List<BaseHK2JAXBBean>) prop) {
                if (GeneralUtilities.safeEquals(keyValue, child._getKeyValue())) {
                    // Add it to the cache
                    if (byName == null) {
                        byName = new HashMap<String, BaseHK2JAXBBean>();
                        
                        children.put(propName, byName);
                    }
                    
                    byName.put(keyValue, child);
                    
                    // and return
                    return child;
                }
            }
        }
        else if (prop.getClass().isArray()) {
            for (Object childRaw : (Object[]) prop) {
                BaseHK2JAXBBean child = (BaseHK2JAXBBean) childRaw;
                
                if (GeneralUtilities.safeEquals(keyValue, child._getKeyValue())) {
                    // Add it to the cache
                    if (byName == null) {
                        byName = new HashMap<String, BaseHK2JAXBBean>();
                        
                        children.put(propName, byName);
                    }
                    
                    byName.put(keyValue, child);
                    
                    // and return
                    return child;
                }
            }
        }
        
        // Just not found
        return null;
    }
    
    public Object _lookupChild(String propName, String keyValue) {
        if (changeControl == null) {
            return internalLookup(propName, keyValue);
        }
        
        changeControl.getReadLock().lock();
        try {
            return internalLookup(propName, keyValue);
        }
        finally {
            changeControl.getReadLock().unlock();
        }
    }
    
    public void _doAdd(String childProperty, Object rawChild, String childKey, int index) {
        if (changeControl == null) {
            internalAdd(this, childProperty, rawChild, childKey, index, null, null, null);
            return;
        }
        
        changeControl.getWriteLock().lock();
        try {
            Hub hub = changeControl.getHub();
            WriteableBeanDatabase wbd = (hub == null) ? null : hub.getWriteableDatabaseCopy();
            DynamicConfiguration config = (changeControl.getDynamicConfigurationService() == null) ? null :
                changeControl.getDynamicConfigurationService().createDynamicConfiguration();
            
            internalAdd(this, childProperty, rawChild, childKey, index, changeControl, wbd, config);
            
            if (config != null) {
                config.commit();
            }
            
            if (wbd != null) {
                wbd.commit(new XmlHubCommitMessage() {});
            }
            
            return;
        }
        finally {
            changeControl.getWriteLock().unlock();
        }
    }
    
    public Object _invokeCustomizedMethod(String methodName, Class<?>[] params, Object[] values) {
        Class<?> tClass = getClass();
        Customizer customizer = tClass.getAnnotation(Customizer.class);
        if (customizer == null) {
            throw new RuntimeException("Method " + methodName + " was called on class " + tClass.getName() +
                    " with no customizer, failing");
        }
        
        Class<?> cClass = customizer.value();
        String cName = (customizer.name().equals("")) ? null : customizer.name() ;
        
        Object cService = null;
        if (cName == null) {
            cService = changeControl.getServiceLocator().getService(cClass);
        }
        else {
            cService = changeControl.getServiceLocator().getService(cClass, cName);
        }
        
        if (cService == null) {
            if (customizer.failWhenMethodNotFound()) {
                throw new RuntimeException("Method " + methodName + " was called on class " + tClass.getName() +
                        " but service " + cClass.getName() + " with name " + cName + " was not found");
            }
            
            return null;
        }
        
        Method cMethod;
        try {
            cMethod = cClass.getMethod(methodName, params);
        }
        catch (NoSuchMethodException nsme) {
            if (customizer.failWhenMethodNotFound()) {
                throw new RuntimeException(nsme);
            }
            
            return null;
        }
        
        try {
            return ReflectionHelper.invoke(cService, cMethod, values, false);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    public int _invokeCustomizedMethodI(String methodName, Class<?>[] params, Object[] values) {
        return ((Integer) _invokeCustomizedMethod(methodName, params, values)).intValue();
    }
    
    public long _invokeCustomizedMethodJ(String methodName, Class<?>[] params, Object[] values) {
        return ((Long) _invokeCustomizedMethod(methodName, params, values)).longValue();
    }
    
    public boolean _invokeCustomizedMethodZ(String methodName, Class<?>[] params, Object[] values) {
        return ((Boolean) _invokeCustomizedMethod(methodName, params, values)).booleanValue();
    }
    
    public byte _invokeCustomizedMethodB(String methodName, Class<?>[] params, Object[] values) {
        return ((Byte) _invokeCustomizedMethod(methodName, params, values)).byteValue();
    }
    
    public char _invokeCustomizedMethodC(String methodName, Class<?>[] params, Object[] values) {
        return ((Character) _invokeCustomizedMethod(methodName, params, values)).charValue();
    }
    
    public short _invokeCustomizedMethodS(String methodName, Class<?>[] params, Object[] values) {
        return ((Short) _invokeCustomizedMethod(methodName, params, values)).shortValue();
    }
    
    public float _invokeCustomizedMethodF(String methodName, Class<?>[] params, Object[] values) {
        return ((Float) _invokeCustomizedMethod(methodName, params, values)).floatValue();
    }
    
    public double _invokeCustomizedMethodD(String methodName, Class<?>[] params, Object[] values) {
        return ((Double) _invokeCustomizedMethod(methodName, params, values)).doubleValue();
    }
    
    public static BaseHK2JAXBBean _addRoot(UnparentedNode rootNode,
            Object rawRoot,
            DynamicChangeInfo changeInfo,
            ClassReflectionHelper helper,
            WriteableBeanDatabase writeableDatabase,
            DynamicConfiguration dynamicService) {
        if (!(rawRoot instanceof BaseHK2JAXBBean)) {
            throw new IllegalArgumentException("The root added must be from XmlService.createBean");
        }
        
        BaseHK2JAXBBean child = Utilities.createBean(rootNode.getTranslatedClass());
        
        // Handling of children will be handled once the real child is better setup
        BaseHK2JAXBBean childToCopy = (BaseHK2JAXBBean) rawRoot;
        for (String nonChildProperty : childToCopy.model.getNonChildProperties()) {
            Object value = childToCopy._getProperty(nonChildProperty);
            if (value == null) continue;
            
            child._setProperty(nonChildProperty, value);
        }
        
        if (rootNode.getKeyProperty() != null) {
            child._setKeyValue((String) child._getProperty(rootNode.getKeyProperty())); 
        }
            
        child._setModel(rootNode, helper);
        child._setSelfXmlTag(rootNode.getRootName());
        child._setInstanceName(rootNode.getRootName());
        
        handleChildren(child, childToCopy, changeInfo);
            
        // Now freeze it
        child._setDynamicChangeInfo(changeInfo);
        
        externalAdd(child, dynamicService, writeableDatabase);
        
        return child;
    }
    
    @SuppressWarnings("unchecked")
    private static BaseHK2JAXBBean internalAdd(
            BaseHK2JAXBBean myParent,
            String childProperty,
            Object rawChild,
            String childKey,
            int index,
            DynamicChangeInfo changeInformation,
            WriteableBeanDatabase writeableDatabase,
            DynamicConfiguration dynamicService) {
        if (index < -1) {
            throw new IllegalArgumentException("Unknown index " + index);
        }
        
        if (childKey != null && myParent._lookupChild(childProperty, childKey) != null) {
            throw new IllegalStateException("There is already a child with name " + childKey + " for child " + childProperty);
        }
        
        if (rawChild != null && !(rawChild instanceof BaseHK2JAXBBean)) {
            throw new IllegalArgumentException("The child added must be from XmlService.createBean");
        }
        
        ParentedNode childNode = myParent.model.getChild(childProperty);
        if (childNode == null) {
            throw new IllegalArgumentException("There is no child with xmlTag " + childProperty + " of " + myParent);
        }
        
        Object allMyChildren = myParent.beanLikeMap.get(childProperty);
        List<Object> multiChildren = null;
        if (childNode.isMultiChildList() || childNode.isMultiChildArray()) {
            if (allMyChildren == null) {
                multiChildren = new ArrayList<Object>(10);
            }
            else {
                if (childNode.isMultiChildList()) {
                    multiChildren = new ArrayList<Object>((List<Object>) allMyChildren);
                }
                else {
                    multiChildren = new ArrayList<Object>(Arrays.asList((Object[]) allMyChildren));
                }
            }
            
            if (index > multiChildren.size()) {
                throw new IllegalArgumentException("The index given to add child " + childProperty + " to " + myParent + " is not in range");
            }
            
            if (index == -1) {
                index = multiChildren.size();
            }
        }
        else if (allMyChildren != null) {
            throw new IllegalStateException("Attempting to add direct child of " + myParent + " of name " + childProperty + " but there is already one there");
        }
        
        BaseHK2JAXBBean child = Utilities.createBean(childNode.getChild().getTranslatedClass());
        if (rawChild != null) {
            // Handling of children will be handled once the real child is better setup
            BaseHK2JAXBBean childToCopy = (BaseHK2JAXBBean) rawChild;
            for (String nonChildProperty : childToCopy.model.getNonChildProperties()) {
                Object value = childToCopy._getProperty(nonChildProperty);
                if (value == null) continue;
                
                child._setProperty(nonChildProperty, value);
            }
        }
        
        if (childKey == null) {
            if (childNode.isMultiChildList() || childNode.isMultiChildArray()) {
                if (childNode.getChild().getKeyProperty() != null) {
                    if (rawChild != null) {
                        childKey = (String) child._getProperty(childNode.getChild().getKeyProperty());
                    }
                    
                    if (childKey == null) {
                        throw new IllegalArgumentException("Attempted to create child with xmlTag " + childProperty +
                            " with no key field in " + myParent);
                    }
                    
                    child._setKeyValue(childKey);
                }
                else {
                    // This is a multi-child with no key and no key property, must generate a key
                    if (myParent.changeControl == null) {
                        childKey = NOT_UNIQUE_UNIQUE_ID;
                        child._setKeyValue(NOT_UNIQUE_UNIQUE_ID);
                    }
                    else {
                        childKey = myParent.changeControl.getGeneratedId();
                        child._setKeyValue(childKey);
                    }
                }
            }
        }
        else { /* childKey != null */
            if (childNode.getChild().getKeyProperty() == null) {
                throw new IllegalArgumentException("Attempted to add an unkeyed child with key " + childKey + " in " + myParent);
            }
                
            child._setProperty(childNode.getChild().getKeyProperty(), childKey);
            child._setKeyValue(childKey);
        }
            
        child._setModel(childNode.getChild(), myParent.classReflectionHelper);
        child._setParent(myParent);
        child._setSelfXmlTag(childNode.getChildName());
        child._setKeyValue(childKey);
        if (childNode.isMultiChildList() || childNode.isMultiChildArray()) {
            child._setInstanceName(myParent.instanceName + Utilities.INSTANCE_PATH_SEPARATOR + child._getKeyValue());
        }
        else {
            child._setInstanceName(myParent.instanceName + Utilities.INSTANCE_PATH_SEPARATOR + childNode.getChildName());
        }
        
        if (rawChild != null) {
            // Now we handle the children
            handleChildren(child, (BaseHK2JAXBBean) rawChild, changeInformation);
        }
            
        // Now freeze it
        child._setDynamicChangeInfo(changeInformation);
        
        externalAdd(child, dynamicService, writeableDatabase);
        
        // Now modify the actual list
        if (multiChildren != null) {
            multiChildren.add(index, child);
            List<Object> finalChildList = Collections.unmodifiableList(multiChildren);
            
            if (writeableDatabase != null) {
                myParent.changeInHub(childProperty, finalChildList, writeableDatabase);
            }
            
            myParent.beanLikeMap.put(childProperty, finalChildList);
            
            if (!myParent.model.getUnKeyedChildren().contains(childProperty)) {
                Map<String, BaseHK2JAXBBean> byKeyMap = myParent.children.get(childProperty);
                if (byKeyMap == null) {
                    byKeyMap = new HashMap<String, BaseHK2JAXBBean>();
                    myParent.children.put(childProperty, byKeyMap);
                }
                
                byKeyMap.put(child._getKeyValue(), child);
            }
        }
        else {
            if (writeableDatabase != null){
                myParent.changeInHub(childProperty, child, writeableDatabase);
            }
            
            myParent.beanLikeMap.put(childProperty, child);
        }
        
        return child;
    }
    
    @SuppressWarnings("unchecked")
    private static void handleChildren(BaseHK2JAXBBean child, BaseHK2JAXBBean childToCopy, DynamicChangeInfo changeInformation) {
        Map<String, ParentedNode> childrenMap = childToCopy.model.getChildrenProperties();
        
        for (Map.Entry<String, ParentedNode> childsChildrenEntry : childrenMap.entrySet()) {
            String childsChildProperty = childsChildrenEntry.getKey();
            ParentedNode childsChildParentNode = childsChildrenEntry.getValue();
            
            if (childsChildParentNode.isMultiChildList() || childsChildParentNode.isMultiChildArray()) {
                List<BaseHK2JAXBBean> childsChildren;
                if (childsChildParentNode.isMultiChildList()) {
                    childsChildren = (List<BaseHK2JAXBBean>) childToCopy._getProperty(childsChildProperty);
                }
                else {
                    childsChildren = Arrays.asList((BaseHK2JAXBBean[]) childToCopy._getProperty(childsChildProperty));
                }
                
                if (childsChildren == null) continue;
                if (childsChildren.size() <= 0) continue;
                
                ArrayList<BaseHK2JAXBBean> copiedChildArray = new ArrayList<BaseHK2JAXBBean>(childsChildren.size());
                for (BaseHK2JAXBBean childsChild : childsChildren) {
                    BaseHK2JAXBBean grandchild = internalAdd(child, childsChildProperty,
                            childsChild, null, -1, changeInformation, null, null);
                    
                    copiedChildArray.add(grandchild);
                }
                
                child._setProperty(childsChildProperty, copiedChildArray);
            }
            else {
                BaseHK2JAXBBean childsChild = (BaseHK2JAXBBean) childToCopy._getProperty(childsChildProperty);
                if (childsChild == null) continue;
                
                BaseHK2JAXBBean grandchild = internalAdd(child, childsChildProperty,
                        childsChild, null, -1, changeInformation, null, null);
                
                child._setProperty(childsChildProperty, grandchild);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void externalAdd(BaseHK2JAXBBean root, DynamicConfiguration config, WriteableBeanDatabase writeableDatabase) {
        if (config == null && writeableDatabase == null) return;
        
        Utilities.advertise(writeableDatabase, config, root);
        
        for (Map<String, BaseHK2JAXBBean> specificChildren : root.children.values()) {
            for (BaseHK2JAXBBean child : specificChildren.values()) {
                externalAdd(child, config, writeableDatabase);
            }
        }
        
        for (String unkeyedChildProperty : root.model.getUnKeyedChildren()) {
            Object unkeyedRawChild = root.beanLikeMap.get(unkeyedChildProperty);
            if (unkeyedRawChild == null) continue;
            
            if (unkeyedRawChild instanceof List) {
                List<BaseHK2JAXBBean> unkeyedMultiChildren = (List<BaseHK2JAXBBean>) unkeyedRawChild;
                for (BaseHK2JAXBBean child : unkeyedMultiChildren) {
                    externalAdd(child, config, writeableDatabase);
                }
            }
            else {
                externalAdd((BaseHK2JAXBBean) unkeyedRawChild, config, writeableDatabase);
            }
            
        }
    }
    
    public Object _doRemove(String childProperty, String childKey, int index) {
        throw new AssertionError("Add not yet implemented: childProperty=" + childProperty + " childKey=" + childKey + " index=" + index);
    }

    public boolean _hasProperty(String propName) {
        if (changeControl == null) {
            return beanLikeMap.containsKey(propName);
        }
        
        changeControl.getReadLock().lock();
        try {
            return beanLikeMap.containsKey(propName);
        }
        finally {
            changeControl.getReadLock().unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean#getBeanLikeMap()
     */
    @Override
    public Map<String, Object> _getBeanLikeMap() {
        if (changeControl == null) {
            return Collections.unmodifiableMap(beanLikeMap);
        }
        
        changeControl.getReadLock().lock();
        try {
            return Collections.unmodifiableMap(beanLikeMap);
        }
        finally {
            changeControl.getReadLock().unlock();
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean#getParent()
     */
    @Override
    public Object _getParent() {
        return parent;
    }
    
    public void _setParent(Object parent) {
        this.parent = parent;
    }
    
    public void _setSelfXmlTag(String selfXmlTag) {
        this.selfXmlTag = selfXmlTag;
    }
    
    public String _getSelfXmlTag() {
        return selfXmlTag;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean#_getXmlPath()
     */
    @Override
    public String _getXmlPath() {
        return xmlPath;
    }
    
    public void _setInstanceName(String name) {
        instanceName = name;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean#_getInstanceName()
     */
    @Override
    public String _getInstanceName() {
        return instanceName;
    }
    
    public void _setKeyValue(String key) {
        keyValue = key;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean#_getKeyPropertyName()
     */
    @Override
    public String _getKeyPropertyName() {
        return model.getKeyProperty();
    }
    
    public void _setModel(UnparentedNode model, ClassReflectionHelper helper) {
        this.model = model;
        this.classReflectionHelper = helper;
    }
    
    public UnparentedNode _getModel() {
        return model;
    }
    
    @Override
    public String _getKeyValue() {
        return keyValue;
    }
    
    private static String calculateXmlPath(BaseHK2JAXBBean leaf) {
        LinkedList<String> stack = new LinkedList<String>();
        while (leaf != null) {
            stack.addFirst(leaf._getSelfXmlTag());
            
            leaf = (BaseHK2JAXBBean) leaf._getParent();
        }
        
        StringBuffer sb = new StringBuffer();
        for (String component : stack) {
            sb.append(XML_PATH_SEPARATOR + component);
        }
        
        return sb.toString();
    }
    
    /**
     * Once this is set the dynamic change protocol is in effect,
     * and all paths can be calculated
     * 
     * @param change The change control object
     */
    public void _setDynamicChangeInfo(DynamicChangeInfo change) {
        xmlPath = calculateXmlPath(this);
        
        changeControl = change;
    }
    
    /**
     * Read lock must be held
     * 
     * @return The set of all children tags
     */
    public Set<String> _getChildrenXmlTags() {
        HashSet<String> retVal = new HashSet<String>(children.keySet());
        retVal.addAll(model.getUnKeyedChildren());
        
        return retVal;
    }
    
    /**
     * This copy method ONLY copies NON child and
     * non parent fields, and so is not a full copy.  The
     * children and parent and lock information need to
     * be filled in later so as not to have links from
     * one tree into another.  The read lock of copyMe
     * should be held
     * 
     * @param copyMe The non-null bean to copy FROM
     */
    public void _shallowCopyFrom(BaseHK2JAXBBean copyMe) {
        selfXmlTag = copyMe.selfXmlTag;
        instanceName = copyMe.instanceName;
        model = copyMe.model;
        keyValue = copyMe.keyValue;
        xmlPath = copyMe.xmlPath;
        
        for (Map.Entry<String, Object> entrySet : copyMe.beanLikeMap.entrySet()) {
            String xmlTag = entrySet.getKey();
            
            if (copyMe.children.containsKey(xmlTag) || copyMe.model.getUnKeyedChildren().contains(xmlTag)) continue;
            
            beanLikeMap.put(entrySet.getKey(), entrySet.getValue());
        }
    }
    
    private PropertyChangeEvent changes[] = null;
    
    public void _merge(BaseHK2JAXBBean other, WriteableBeanDatabase writeableDatabase) {
        if (changes != null) throw new IllegalStateException("Bean " + this + " has a merge on-going");
        
        Map<String, Object> otherMap = other._getBeanLikeMap();
        
        WriteableType wt = writeableDatabase.getWriteableType(xmlPath);
        
        changes = BeanReflectionHelper.getChangeEvents(classReflectionHelper,
                beanLikeMap, otherMap);
        
        // TODO:  Children
        
        wt.modifyInstance(instanceName, otherMap, changes);
    }
    
    /**
     * Write lock for tree must be held
     * 
     * @param success If the transaction committed successfully
     */
    public void _endMerge(boolean success) {
        if (changes == null) throw new IllegalStateException("Bean " + this + " does not have a known merge");
        if (!success) {
            changes = null;
            return;
        }
        
        for (PropertyChangeEvent pce : changes) {
            String propName = pce.getPropertyName();
            Object newValue = pce.getNewValue();
            
            beanLikeMap.put(propName, newValue);
            
            // TODO: Children
        }
        
        changes = null;
    }
    
    /**
     * Called under write lock
     * 
     * @param propName The name of the property to change
     * @param propValue The new value of the property
     */
    private boolean changeInHub(String propName, Object propValue, WriteableBeanDatabase wbd) {
        if (GeneralUtilities.safeEquals(beanLikeMap.get(propName), propValue)) {
            // Calling set, but the value was not in fact changed
            return false;
        }
        
        WriteableType wt = wbd.getWriteableType(xmlPath);
        
        HashMap<String, Object> modified = new HashMap<String, Object>(beanLikeMap);
        modified.put(propName, propValue);
        
        wt.modifyInstance(instanceName, modified);
        
        return true;
    }
    
    private void changeInHub(String propName, Object propValue) {
        Hub hub = (changeControl != null) ? changeControl.getHub() : null;
        if (hub == null) return;
        
        WriteableBeanDatabase wbd = hub.getWriteableDatabaseCopy();
        boolean changed = changeInHub(propName, propValue, wbd);
        
        if (changed) {
            wbd.commit(new XmlHubCommitMessage() {});
        
            changeControl.incrementChangeNumber();
        }
    }
    
    @Override
    public String toString() {
        return "BaseHK2JAXBBean(XmlPath=" + xmlPath + ",instanceName=" + instanceName + ",keyValue=" + keyValue + "," + System.identityHashCode(this) + ")";
    }
}
