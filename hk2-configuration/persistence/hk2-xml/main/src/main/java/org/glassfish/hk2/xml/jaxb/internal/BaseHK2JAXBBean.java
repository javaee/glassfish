/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
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

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Customizer;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.internal.ChildDataModel;
import org.glassfish.hk2.xml.internal.ChildType;
import org.glassfish.hk2.xml.internal.DynamicChangeInfo;
import org.glassfish.hk2.xml.internal.ModelImpl;
import org.glassfish.hk2.xml.internal.ParentedModel;
import org.glassfish.hk2.xml.internal.Utilities;
import org.glassfish.hk2.xml.internal.XmlDynamicChange;
import org.glassfish.hk2.xml.internal.XmlRootHandleImpl;

/**
 * @author jwells
 *
 */
public abstract class BaseHK2JAXBBean implements XmlHk2ConfigurationBean, Serializable {
    private static final long serialVersionUID = 8149986319033910297L;

    private final static boolean DEBUG_GETS_AND_SETS = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
        @Override
        public Boolean run() {
            return Boolean.parseBoolean(
                System.getProperty("org.jvnet.hk2.properties.xmlservice.jaxb.getsandsets", "false"));
        }
            
    });
    
    private final static String EMPTY = "";
    public final static char XML_PATH_SEPARATOR = '/';
    
    /**
     * All fields, including child lists and direct children
     */
    private HashMap<String, Object> beanLikeMap = new HashMap<String, Object>();
    
    private HashMap<String, Object> backupMap = null;
    
    /**
     * All children whose type has an identifier.  First key is the xml parameter name, second
     * key is the identifier of the specific child.  Used in lookup operations.  Works
     * as a cache, may not be completely accurate and must be flushed on remove
     * operations
     */
    private final Map<String, Map<String, BaseHK2JAXBBean>> keyedChildrenCache = new ConcurrentHashMap<String, Map<String, BaseHK2JAXBBean>>();
    
    /** The model for this, including lists of all children property names */
    // private UnparentedNode model;
    
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
    private volatile transient DynamicChangeInfo<?> changeControl;
    
    /**
     * The root of this bean, or null if there is no root
     * for this bean
     */
    private volatile transient XmlRootHandleImpl<?> root;
    
    /**
     * If true this bean has been given to the user to
     * add/remove or call getters or setters.  This can
     * happen in two ways, the first being via being parsed
     * from an XML file, the other is via dynamic creation
     */
    private boolean active = false;
    
    /**
     * The descriptor that this bean is advertised with
     */
    private transient ActiveDescriptor<?> selfDescriptor;
    
    /**
     * The cost to add this bean to a tree, which
     * is 1 plus the add cost of all children.  This
     * value is transient and is only calculated under
     * locks and may not be accurate after intended use.
     * The add cost and the remove cost are the same
     */
    private transient int addCost = -1;
    
    /**
     * For JAXB and Serialization
     */
    public BaseHK2JAXBBean() {
    }
    
    public void _setProperty(String propName, Object propValue) {
        _setProperty(propName, propValue, true);
    }
    
    public void _setProperty(String propName, Object propValue, boolean changeInHub) {
        _setProperty(propName, propValue, changeInHub, false);
    }
    
    @SuppressWarnings("unchecked")
    public void _setProperty(String propName, Object propValue, boolean changeInHub, boolean rawSet) {
        if (propName == null) throw new IllegalArgumentException("properyName may not be null");
        
        if (DEBUG_GETS_AND_SETS) {
            // Hidden behind static because of potential expensive toString costs
            Logger.getLogger().debug("XmlService setting property " + propName + " to " + propValue + " in " + this + " rawSet=" + rawSet);
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
            if (active) {
                synchronized (this) {
                    beanLikeMap.put(propName, propValue);
                }
            }
            else {
                beanLikeMap.put(propName, propValue);
            }
        }
        else {
            boolean doAdd = false;
            boolean doRemove = false;
            boolean doModify = false;
            Object currentValue = null;
            
            if (!rawSet) {
                changeControl.getReadLock().lock();
                try {
                    currentValue = beanLikeMap.get(propName);
                
                    // If both null this goes, or if they are somehow exactly the same
                    if (currentValue == propValue) return;
                
                    ModelImpl model = _getModel();
                    ParentedModel childModel = model.getChild(propName);
                    if (childModel != null) {
                        if (ChildType.DIRECT.equals(childModel.getChildType())) {
                            if (currentValue == null && propValue != null) {
                                // This is an add
                                doAdd = true;
                            }
                            else if (currentValue != null && propValue == null) {
                                // This is a remove
                                doRemove = true;
                            }
                            else {
                                // Both beans are different
                                doModify = true;
                            }
                        }
                        else {
                            // Just not going to work
                            doModify = true;
                        }
                    }
                }
                finally {
                    changeControl.getReadLock().unlock();
                }
            }
            
            if (doAdd) {
                _doAdd(propName, propValue, null, -1);
                return;
            }
            if (doRemove) {
                _doRemove(propName, null, -1, currentValue);
                return;
            }
            if (doModify) {
                throw new IllegalStateException(
                        "A bean may not be modified with a set method, instead directly manipulate the fields of the existing bean or use add and remove methods");
            }
            
            String keyProperty = _getModel().getKeyProperty();
            if (keyProperty != null && propName.equals(keyProperty) && (keyValue != null)) {
                throw new IllegalArgumentException("The key property of a bean (" + keyProperty + ") may not be changed from " +
                  keyValue + " to " + propValue);
            }
            
            changeControl.getWriteLock().lock();
            try {
                changeControl.startOrContinueChange(this);
                boolean success = false;
                
                try {
                    if (!rawSet) {
                      Utilities.invokeVetoableChangeListeners(changeControl, this,
                        beanLikeMap.get(propName), propValue, propName, classReflectionHelper);
                    }
                
                    if (changeInHub) {
                        changeInHubDirect(propName, propValue);
                    }
                
                    if (backupMap == null) {
                        backupMap = new HashMap<String, Object>(beanLikeMap);
                    }
                
                    beanLikeMap.put(propName, propValue);
                    
                    success = true;
                }
                finally {
                    changeControl.endOrDeferChange(success);
                }
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
    
    private Object _getProperty(String propName, Class<?> expectedClass) {
        return _getProperty(propName, expectedClass, null);
    }
    
    private Object _getProperty(String propName, Class<?> expectedClass, ParentedModel parentNode) {
        boolean isSet;
        Object retVal;
        boolean doDefaulting = active ? true : false;
        
        if (changeControl == null) {
            if (active) {
                synchronized (this) {
                    isSet = beanLikeMap.containsKey(propName);
                    retVal = beanLikeMap.get(propName);
                }
            }
            else {
                isSet = beanLikeMap.containsKey(propName);
                retVal = beanLikeMap.get(propName);
            }
        }
        else {
            changeControl.getReadLock().lock();
            try {
                doDefaulting = true;
                isSet = beanLikeMap.containsKey(propName);
                retVal = beanLikeMap.get(propName);
            }
            finally {
                changeControl.getReadLock().unlock();
            }
        }
        
        if (doDefaulting && (retVal == null) && !isSet) {
            if (expectedClass != null) {
                retVal = Utilities.getDefaultValue(_getModel().getDefaultChildValue(propName), expectedClass);
            }
            else if (parentNode != null) {
                switch (parentNode.getChildType()) {
                case LIST:
                    retVal = Collections.EMPTY_LIST;
                    break;
                case ARRAY:
                    Class<?> cType = parentNode.getChildModel().getOriginalInterfaceAsClass();
                    retVal = Array.newInstance(cType, 0);
                    break;
                case DIRECT:
                default:
                    break;
                
                }
                
            }
        }
        
        if (DEBUG_GETS_AND_SETS) {
            // Hidden behind static because of potential expensive toString costs
            Logger.getLogger().debug("XmlService getting property " + propName + "=" + retVal + " in " + this);
        }
        
        return retVal;
    }
    
    /**
     * Called by proxy
     * 
     * @param propName Property of child or non-child element or attribute
     * @return Value
     */
    public Object _getProperty(String propName) {
        if (!_getModel().isChildProperty(propName)) {
            return _getProperty(propName, _getModel().getNonChildType(propName));
        }
        
        ParentedModel parent = _getModel().getChild(propName);
        return _getProperty(propName, null, parent);
    }
    
    /**
     * Called by proxy
     * 
     * @param propName
     * @return
     */
    public boolean _getPropertyZ(String propName) {
        return (Boolean) _getProperty(propName, boolean.class);
    }
    
    /**
     * Called by proxy
     * 
     * @param propName
     * @return
     */
    public byte _getPropertyB(String propName) {
        return (Byte) _getProperty(propName, byte.class);
    }
    
    /**
     * Called by proxy
     * 
     * @param propName
     * @return
     */
    public char _getPropertyC(String propName) {
        return (Character) _getProperty(propName, char.class);
    }
    
    /**
     * Called by proxy
     * 
     * @param propName
     * @return
     */
    public short _getPropertyS(String propName) {
        return (Short) _getProperty(propName, short.class);
    }
    
    /**
     * Called by proxy
     * 
     * @param propName
     * @return
     */
    public int _getPropertyI(String propName) {
        return (Integer) _getProperty(propName, int.class);
    }
    
    /**
     * Called by proxy
     * 
     * @param propName
     * @return
     */
    public float _getPropertyF(String propName) {
        return (Float) _getProperty(propName, float.class);
    }
    
    /**
     * Called by proxy
     * 
     * @param propName
     * @return
     */
    public long _getPropertyJ(String propName) {
        return (Long) _getProperty(propName, long.class);
    }
    
    /**
     * Called by proxy
     * 
     * @param propName
     * @return
     */
    public double _getPropertyD(String propName) {
        return (Double) _getProperty(propName, double.class);
    }
    
    @SuppressWarnings("unchecked")
    private Object internalLookup(String propName, String keyValue) {
        // First look in the cache
        Object retVal = null;
        
        Map<String, BaseHK2JAXBBean> byName;
        byName = keyedChildrenCache.get(propName);
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
                        byName = new ConcurrentHashMap<String, BaseHK2JAXBBean>();
                        
                        keyedChildrenCache.put(propName, byName);
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
                        byName = new ConcurrentHashMap<String, BaseHK2JAXBBean>();
                        
                        keyedChildrenCache.put(propName, byName);
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
    
    public Object _doAdd(String childProperty, Object rawChild, String childKey, int index) {
        return _doAdd(childProperty, rawChild, childKey, index, true);
    }
    
    public Object _doAdd(String childProperty, Object rawChild, String childKey, int index, boolean changeList) {
        if (changeControl == null) {
            return Utilities.internalAdd(this, childProperty, rawChild, childKey, index, null, XmlDynamicChange.EMPTY, new LinkedList<ActiveDescriptor<?>>(), changeList);
        }
        
        changeControl.getWriteLock().lock();
        try {
            Object oldValue = beanLikeMap.get(childProperty);
            
            LinkedList<ActiveDescriptor<?>> addedServices = new LinkedList<ActiveDescriptor<?>>();
            Object retVal;
            boolean success = false;
            XmlDynamicChange change = changeControl.startOrContinueChange(this);
            try {
                retVal = Utilities.internalAdd(this, childProperty, rawChild, childKey, index, changeControl, change, addedServices, changeList);
                
                Object newValue = beanLikeMap.get(childProperty);
                
                Utilities.invokeVetoableChangeListeners(changeControl, this,
                        oldValue, newValue, childProperty, classReflectionHelper);
                
                success = true;
            }
            finally {
                changeControl.endOrDeferChange(success);
            }
            
            ServiceLocator locator = changeControl.getServiceLocator();
            for (ActiveDescriptor<?> added : addedServices) {
                // Ensures that any defaulting listeners are run
                locator.getServiceHandle(added).getService();
            }
            
            return retVal;
        }
        finally {
            changeControl.getWriteLock().unlock();
        }
    }
    
    public Object _invokeCustomizedMethod(String methodName, Class<?>[] params, Object[] values) {
        if (DEBUG_GETS_AND_SETS) {
            // Hidden behind static because of potential expensive toString costs
            Logger.getLogger().debug("XmlService invoking customized method " + methodName +
                    " with params " + Arrays.toString(params) + " adn values " + Arrays.toString(values));
        }
        Class<?> tClass = getClass();
        Customizer customizer = tClass.getAnnotation(Customizer.class);
        if (customizer == null) {
            throw new RuntimeException("Method " + methodName + " was called on class " + tClass.getName() +
                    " with no customizer, failing");
        }
        
        Class<?> cClassArray[] = customizer.value();
        String cNameArray[] = customizer.name();
        
        if (cNameArray.length > 0 && cClassArray.length != cNameArray.length) {
            throw new RuntimeException("The @Customizer annotation must have the value and name arrays be of equal size.  " +
              "The class array is of size " + cClassArray.length + " while the name array is of size " + cNameArray.length +
              " for class " + tClass.getName());
        }
        
        LinkedList<Throwable> errors = new LinkedList<Throwable>();
        for (int lcv = 0; lcv < cClassArray.length; lcv++) {
            Class<?> cClass = cClassArray[lcv];
            String cName = (cNameArray.length == 0) ? null : cNameArray[lcv] ;
            
            Object cService = null;
            if (cName == null || "".equals(cName)) {
                cService = changeControl.getServiceLocator().getService(cClass);
            }
            else {
                cService = changeControl.getServiceLocator().getService(cClass, cName);
            }
        
            if (cService == null) {
                if (customizer.failWhenMethodNotFound()) {
                    errors.add(new RuntimeException("Method " + methodName + " was called on class " + tClass.getName() +
                        " but service " + cClass.getName() + " with name " + cName + " was not found"));
                }
            
                continue;
            }
        
            
            ModelImpl model = _getModel();
            Class<?> topInterface = (model == null) ? null : model.getOriginalInterfaceAsClass() ;
            
            Method cMethod = Utilities.findSuitableCustomizerMethod(cClass, methodName, params, topInterface);
            if (cMethod == null) {
                if (customizer.failWhenMethodNotFound()) {
                    errors.add(new RuntimeException("No customizer method with name " + methodName + " was found on customizer " + cClass.getName() +
                            " with parameters " + Arrays.toString(params) + " for bean " + tClass.getName()));
                }
                
                continue;
            }
             
            boolean useAlt = false;
            if (cMethod.getParameterTypes().length == (params.length + 1)) useAlt = true;
            
            if (useAlt) {
                Object altValues[] = new Object[values.length + 1];
                altValues[0] = this;
                for (int lcv2 = 0; lcv2 < values.length; lcv2++) {
                    altValues[lcv2 + 1] = values[lcv2];
                }
            
                values = altValues;
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
        
        if (errors.isEmpty()) {
            return null;
        }
        
        throw new MultiException(errors);
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
    
    public Object _doRemove(String childProperty, String childKey, int index, Object child) {
        return _doRemove(childProperty, childKey, index, child, true);
    }
    
    public Object _doRemove(String childProperty, String childKey, int index, Object child, boolean changeList) {
        if (changeControl == null) {
            Object retVal = Utilities.internalRemove(this, childProperty, childKey, index, child, null, XmlDynamicChange.EMPTY, changeList);
            
            if (retVal != null) {
                keyedChildrenCache.remove(childProperty);
            }
            
            // return Utilities.internalRemove(this, childProperty, childKey, index, child, null, XmlDynamicChange.EMPTY);
            return retVal;
        }
        
        changeControl.getWriteLock().lock();
        try {
            XmlDynamicChange xmlDynamicChange = changeControl.startOrContinueChange(this);
            
            Object retVal;
            boolean success = false;
            try {
                Object oldVal = beanLikeMap.get(childProperty);
                
                retVal = Utilities.internalRemove(this, childProperty, childKey, index, child, changeControl, xmlDynamicChange, changeList);
                
                Object newVal = beanLikeMap.get(childProperty);
                
                Utilities.invokeVetoableChangeListeners(changeControl, this,
                        oldVal, newVal, childProperty, classReflectionHelper);
                
                success = true;
            }
            finally {
                changeControl.endOrDeferChange(success);
            }
            
            if (retVal != null) {
                keyedChildrenCache.remove(childProperty);
            }
            
            return retVal;
        }
        finally {
            changeControl.getWriteLock().unlock();
        }
    }
    
    public boolean _doRemoveZ(String childProperty, String childKey, int index, Object child) {
        Object retVal = _doRemove(childProperty, childKey, index, child);
        return (retVal != null);
    }

    public boolean _hasProperty(String propName) {
        if (changeControl == null) {
            if (active) {
                synchronized (this) {
                    return beanLikeMap.containsKey(propName);
                }
            }
            
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
            if (active) {
                synchronized (this) {
                    return Collections.unmodifiableMap(new HashMap<String, Object>(beanLikeMap));
                }
            }
            return Collections.unmodifiableMap(new HashMap<String, Object>(beanLikeMap));
        }
        
        changeControl.getReadLock().lock();
        try {
            return Collections.unmodifiableMap(new HashMap<String, Object>(beanLikeMap));
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
        return _getModel().getKeyProperty();
    }
    
    public void _setClassReflectionHelper(ClassReflectionHelper helper) {
        this.classReflectionHelper = helper;
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
    
    public void _setDynamicChangeInfo(XmlRootHandleImpl<?> root, DynamicChangeInfo<?> change) {
        _setDynamicChangeInfo(root, change, true);
    }
    
    /**
     * Once this is set the dynamic change protocol is in effect,
     * and all paths can be calculated
     * 
     * @param change The change control object
     * @param doXmlPathCalculation if true then this should calculate the xml path at this time
     * (all the parent information must be correct).  If this is false it is assumed that this
     * is some sort of copy operation where the xmlPath has been pre-calculated and does not
     * need to be modified
     */
    public void _setDynamicChangeInfo(XmlRootHandleImpl<?> root, DynamicChangeInfo<?> change, boolean doXmlPathCalculation) {
        if (doXmlPathCalculation) {
            xmlPath = calculateXmlPath(this);
        }
        
        changeControl = change;
        this.root = root;
        if (changeControl != null) active = true;
    }
    
    /**
     * Once this has been set the bean is considered active, and
     * so defaulting can happen on the bean
     */
    public void _setActive() {
        active = true;
    }
    
    /**
     * Read lock must be held
     * 
     * @return The set of all children tags
     */
    public Set<String> _getChildrenXmlTags() {
        HashSet<String> retVal = new HashSet<String>(_getModel().getKeyedChildren());
        retVal.addAll(_getModel().getUnKeyedChildren());
        
        return retVal;
    }
    
    /**
     * This copy method ONLY copies non-child and
     * non-parent and non-reference fields and so is
     * not a full copy.  The children and parent and
     * reference and lock information need to be filled
     * in later so as not to have links from one tree into
     * another.  The read lock of copyMe should be held
     * 
     * @param copyMe The non-null bean to copy FROM
     */
    public void _shallowCopyFrom(BaseHK2JAXBBean copyMe) {
        selfXmlTag = copyMe.selfXmlTag;
        instanceName = copyMe.instanceName;
        keyValue = copyMe.keyValue;
        xmlPath = copyMe.xmlPath;
        
        Map<String, Object> copyBeanLikeMap = copyMe._getBeanLikeMap();
        
        ModelImpl copyModel = copyMe._getModel();
        for (Map.Entry<String, Object> entrySet : copyBeanLikeMap.entrySet()) {
            String xmlTag = entrySet.getKey();
            
            if (copyModel.getKeyedChildren().contains(xmlTag) || copyMe._getModel().getUnKeyedChildren().contains(xmlTag)) continue;
            
            ChildDataModel cdm = copyModel.getNonChildProperties().get(xmlTag);
            if (cdm != null && cdm.isReference()) {
                continue;
            }
            
            beanLikeMap.put(entrySet.getKey(), entrySet.getValue());
        }
    }
    
    /**
     * Called under write lock
     * 
     * @param propName The name of the property to change
     * @param propValue The new value of the property
     */
    public boolean changeInHub(String propName, Object propValue, WriteableBeanDatabase wbd) {
        Object oldValue = beanLikeMap.get(propName);
        if (GeneralUtilities.safeEquals(oldValue, propValue)) {
            // Calling set, but the value was not in fact changed
            return false;
        }
        
        WriteableType wt = wbd.getWriteableType(xmlPath);
        
        HashMap<String, Object> modified = new HashMap<String, Object>(beanLikeMap);
        modified.put(propName, propValue);
            
        wt.modifyInstance(instanceName, modified);
            
        return true;
    }
    
    /**
     * Called under write lock
     * 
     * @param propName The name of the property to change
     * @param propValue The new value of the property
     */
    public boolean changeInHub(List<PropertyChangeEvent> events, WriteableBeanDatabase wbd) {
        WriteableType wt = wbd.getWriteableType(xmlPath);
        HashMap<String, Object> modified = new HashMap<String, Object>(beanLikeMap);
        
        boolean madeAChange = false;
        for (PropertyChangeEvent event : events) {
            String propName = event.getPropertyName();
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            
            if (GeneralUtilities.safeEquals(oldValue, newValue)) {
                continue;
            }
            
            madeAChange = true;
            modified.put(propName, newValue);   
        }
        
        if (madeAChange) {
            wt.modifyInstance(instanceName, modified);
        }
            
        return madeAChange;
    }
    
    /**
     * Write lock MUST be held
     * @param propName
     * @param propValue
     */
    private void changeInHubDirect(String propName, Object propValue) {
        if (changeControl == null) return;
        
        XmlDynamicChange xmlDynamicChange = changeControl.startOrContinueChange(this);
        
        boolean success = false;
        try {
            WriteableBeanDatabase wbd = xmlDynamicChange.getBeanDatabase();
            
            if (wbd == null) {
                success = true;
                return;
            }
            
            changeInHub(propName, propValue, wbd);
            
            success = true;
        }
        finally {
            changeControl.endOrDeferChange(success);
        }
        
    }
    
    /**
     * Gets the change control information for this bean
     * 
     * @return the change control information for this bean
     */
    public DynamicChangeInfo<?> _getChangeControl() {
        return changeControl;
    }
    
    /**
     * Returns the reflection helper for this bean
     * 
     * @return The reflection helper for this bean
     */
    public ClassReflectionHelper _getClassReflectionHelper() {
        return classReflectionHelper;
    }
    
    public void _setSelfDescriptor(ActiveDescriptor<?> selfDescriptor) {
        this.selfDescriptor = selfDescriptor;
    }
    
    @Override
    public ActiveDescriptor<?> _getSelfDescriptor() {
        return selfDescriptor;
    }
    
    /**
     * Write lock must be held
     */
    public void __activateChange() {
        backupMap = null;
        addCost = -1;
    }
    
    /**
     * Write lock must be held
     */
    public void __rollbackChange() {
        if (backupMap == null) return;
        
        beanLikeMap = backupMap;
        backupMap = null;
    }
    
    /**
     * Gets the root associated with this bean.  If this bean
     * has no associated root this will return null
     * 
     * @return The root of this bean, or null if this bean
     * is not associated with a root
     */
    public XmlRootHandle<?> _getRoot() {
        return root;
    }
    
    public void __setAddCost(int addCost) {
        this.addCost = addCost;
    }
    
    public int __getAddCost() {
        return addCost;
    }
    
    @Override
    public String toString() {
        return "BaseHK2JAXBBean(XmlPath=" + xmlPath +
                ",instanceName=" + instanceName +
                ",keyValue=" + keyValue +
                ",model=" + _getModel() + 
                "," + System.identityHashCode(this) + ")";
    }
}
