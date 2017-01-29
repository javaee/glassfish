/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.internal;

import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Customize;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;
import org.glassfish.hk2.xml.internal.Differences.AddData;
import org.glassfish.hk2.xml.internal.Differences.AddRemoveMoveDifference;
import org.glassfish.hk2.xml.internal.Differences.Difference;
import org.glassfish.hk2.xml.internal.Differences.MoveData;
import org.glassfish.hk2.xml.internal.Differences.RemoveData;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;
import org.glassfish.hk2.xml.internal.alt.AltClass;
import org.glassfish.hk2.xml.internal.alt.AltMethod;
import org.glassfish.hk2.xml.internal.alt.MethodInformationI;
import org.glassfish.hk2.xml.internal.alt.clazz.ClassAltClassImpl;
import org.glassfish.hk2.xml.internal.alt.papi.ArrayTypeAltClassImpl;
import org.glassfish.hk2.xml.internal.alt.papi.TypeElementAltClassImpl;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * @author jwells
 *
 */
public class Utilities {
    /** Separator for instance names */
    public final static char INSTANCE_PATH_SEPARATOR = '.';
    
    private final static String CLASS_ADD_ON_NAME = "_Hk2_Jaxb";
    
    private final static String NOT_UNIQUE_UNIQUE_ID = "not-unique";
    
    private final static String EMPTY_STRING = "";
    
    /* package */ static String convertXmlRootElementName(XmlRootElement root, Class<?> clazz) {
        if (!"##default".equals(root.name())) return root.name();
        
        String simpleName = clazz.getSimpleName();
        
        char asChars[] = simpleName.toCharArray();
        StringBuffer sb = new StringBuffer();
        
        boolean firstChar = true;
        boolean lastCharWasCapital = false;
        for (char asChar : asChars) {
            if (firstChar) {
                firstChar = false;
                if (Character.isUpperCase(asChar)) {
                    lastCharWasCapital = true;
                    sb.append(Character.toLowerCase(asChar));
                }
                else {
                    lastCharWasCapital = false;
                    sb.append(asChar);
                }
            }
            else {
                if (Character.isUpperCase(asChar)) {
                    if (!lastCharWasCapital) {
                        sb.append('-');
                    }
                    
                    sb.append(Character.toLowerCase(asChar));
                    
                    lastCharWasCapital = true;
                }
                else {
                    sb.append(asChar);
                    
                    lastCharWasCapital = false;
                }
            }
        }
        
        return sb.toString();
    }
    
    public static BaseHK2JAXBBean createBean(Class<?> implClass) {
        try {
            Constructor<?> noArgsConstructor = implClass.getConstructor();
    
            return (BaseHK2JAXBBean) ReflectionHelper.makeMe(noArgsConstructor, new Object[0], false);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }
    
    private static String getKeySegment(BaseHK2JAXBBean bean) {
        String baseKeySegment = bean._getKeyValue();
        if (baseKeySegment == null) {
            baseKeySegment = bean._getSelfXmlTag();
        }
        
        return baseKeySegment;
    }
    
    /**
     * Creates an instance name by traveling up the parent chain.  The
     * parent chain must therefor already be correctly setup
     * 
     * @param bean The non-null bean from where to get the instancename
     * @return A unique instance name.  The combination of the xml path
     * and the instance name should uniquely identify the location of
     * any node in a single tree
     */
    public static String createInstanceName(BaseHK2JAXBBean bean) {
        if (bean._getParent() == null) {
            return getKeySegment(bean);
        }
        
        return createInstanceName((BaseHK2JAXBBean) bean._getParent()) + INSTANCE_PATH_SEPARATOR + getKeySegment(bean);
    }
    
    public static ActiveDescriptor<?> advertise(WriteableBeanDatabase wbd, DynamicConfiguration config, BaseHK2JAXBBean bean) {
        ActiveDescriptor<?> selfDescriptor = null;
        if (config != null) {
            AbstractActiveDescriptor<?> cDesc = BuilderHelper.createConstantDescriptor(bean);
            
            // We make these singletons to ensure that any InstanceLifecycleListeners will only be called once
            cDesc.setScopeAsAnnotation(ServiceLocatorUtilities.getSingletonAnnotation());
            
            if (bean._getKeyValue() != null) {
                cDesc.setName(bean._getKeyValue());
            }
            selfDescriptor = config.addActiveDescriptor(cDesc);
            
            bean._setSelfDescriptor(selfDescriptor);
        }
        
        if (wbd != null) {
            WriteableType wt = wbd.findOrAddWriteableType(bean._getXmlPath());
            wt.addInstance(bean._getInstanceName(), bean._getBeanLikeMap(), bean);
        }
        
        return selfDescriptor;
    }
    
    /**
     * Converts a getter name to a setter name (works with
     * both IS getters and GET getters)
     * 
     * @param getterName Non-null getter name starting with is or get
     * @return The corresponding setter name
     */
    public static String convertToSetter(String getterName) {
        if (getterName.startsWith(JAUtilities.IS)) {
            return JAUtilities.SET + getterName.substring(JAUtilities.IS.length());
        }
        
        if (!getterName.startsWith(JAUtilities.GET)) {
            throw new IllegalArgumentException("Unknown getter format: " + getterName);
        }
        
        return JAUtilities.SET + getterName.substring(JAUtilities.GET.length());
    }
    
    /**
     * Converts the Name from the Element to a String
     * @param name
     * @return
     */
    public static String convertNameToString(Name name) {
        if (name == null) return null;
        return name.toString();
    }
    
    public static AltClass convertTypeMirror(TypeMirror typeMirror, ProcessingEnvironment processingEnv) {
        if (TypeKind.VOID.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.VOID;
        }
        if (TypeKind.BOOLEAN.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.BOOLEAN;
        }
        if (TypeKind.INT.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.INT;
        }
        if (TypeKind.LONG.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.LONG;
        }
        if (TypeKind.BYTE.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.BYTE;
        }
        if (TypeKind.CHAR.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.CHAR;
        }
        if (TypeKind.SHORT.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.SHORT;
        }
        if (TypeKind.FLOAT.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.FLOAT;
        }
        if (TypeKind.DOUBLE.equals(typeMirror.getKind())) {
            return ClassAltClassImpl.DOUBLE;
        }
        if (TypeKind.DECLARED.equals(typeMirror.getKind())) {
            DeclaredType dt = (DeclaredType) typeMirror;
            
            TypeElement typeElement = (TypeElement) dt.asElement();
            
            TypeElementAltClassImpl addMe = new TypeElementAltClassImpl(typeElement, processingEnv);
            
            return addMe;
        }
        if (TypeKind.ARRAY.equals(typeMirror.getKind())) {
            ArrayType at = (ArrayType) typeMirror;
            
            return new ArrayTypeAltClassImpl(at, processingEnv);
        }
        if (TypeKind.TYPEVAR.equals(typeMirror.getKind())) {
            TypeVariable tv = (TypeVariable) typeMirror;
            
            TypeMirror upperBound = tv.getUpperBound();
            if (upperBound != null && TypeKind.DECLARED.equals(upperBound.getKind())) {
                return convertTypeMirror(upperBound, processingEnv);
            }
            
            return ClassAltClassImpl.OBJECT;
        }
        
        throw new AssertionError("Unknown parameter kind: " + typeMirror.getKind());
    }
    
    @SuppressWarnings("unchecked")
    public static BaseHK2JAXBBean internalAdd(
            BaseHK2JAXBBean myParent,
            String childProperty,
            Object rawChild,
            String childKey,
            int index,
            DynamicChangeInfo<?> changeInformation,
            XmlDynamicChange xmlDynamicChange,
            List<ActiveDescriptor<?>> addedServices,
            boolean changeList) {
        if (index < -1) {
            throw new IllegalArgumentException("Unknown index " + index);
        }
        
        if (childKey != null && myParent._lookupChild(childProperty, childKey) != null) {
            throw new IllegalStateException("There is already a child with name " + childKey + " for child " + childProperty);
        }
        
        if (rawChild != null && !(rawChild instanceof BaseHK2JAXBBean)) {
            throw new IllegalArgumentException("The child added must be from XmlService.createBean");
        }
        
        ParentedModel childNode = myParent._getModel().getChild(childProperty);
        if (childNode == null) {
            throw new IllegalArgumentException("There is no child with xmlTag " + childProperty + " of " + myParent);
        }
        
        Object allMyChildren = myParent._getProperty(childProperty);
        List<Object> multiChildren = null;
        if (!ChildType.DIRECT.equals(childNode.getChildType())) {
            if (allMyChildren == null) {
                multiChildren = new ArrayList<Object>(10);
            }
            else {
                if (ChildType.LIST.equals(childNode.getChildType())) {
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
        
        BaseHK2JAXBBean child = createBean(childNode.getChildModel().getProxyAsClass());
        child._setClassReflectionHelper(myParent._getClassReflectionHelper());
        
        if (rawChild != null) {
            // Handling of children will be handled once the real child is better setup
            BaseHK2JAXBBean childToCopy = (BaseHK2JAXBBean) rawChild;
            for (String nonChildProperty : childToCopy._getModel().getNonChildProperties().keySet()) {
                Object value = childToCopy._getProperty(nonChildProperty);
                if (value == null) continue;
                
                child._setProperty(nonChildProperty, value, false, true);
            }
        }
        
        if (childKey == null) {
            if (!ChildType.DIRECT.equals(childNode.getChildType())) {
                if (childNode.getChildModel().getKeyProperty() != null) {
                    if (rawChild != null) {
                        childKey = (String) child._getProperty(childNode.getChildModel().getKeyProperty());
                    }
                    
                    if (childKey == null) {
                        throw new IllegalArgumentException("Attempted to create child with xmlTag " + childProperty +
                            " with no key field in " + myParent);
                    }
                    
                    child._setKeyValue(childKey);
                }
                else {
                    // This is a multi-child with no key and no key property, must generate a key
                    if (myParent._getChangeControl() == null) {
                        childKey = NOT_UNIQUE_UNIQUE_ID;
                        child._setKeyValue(NOT_UNIQUE_UNIQUE_ID);
                    }
                    else {
                        childKey = myParent._getChangeControl().getGeneratedId();
                        child._setKeyValue(childKey);
                    }
                }
            }
        }
        else { /* childKey != null */
            if (childNode.getChildModel().getKeyProperty() == null) {
                throw new IllegalArgumentException("Attempted to add an unkeyed child with key " + childKey + " in " + myParent);
            }
                
            child._setProperty(childNode.getChildModel().getKeyProperty(), childKey, false, true);
            child._setKeyValue(childKey);
        }
        
        child._setParent(myParent);
        child._setSelfXmlTag(childNode.getChildXmlTag());
        child._setKeyValue(childKey);
        if (!ChildType.DIRECT.equals(childNode.getChildType())) {
            child._setInstanceName(myParent._getInstanceName() + Utilities.INSTANCE_PATH_SEPARATOR + child._getKeyValue());
        }
        else {
            child._setInstanceName(myParent._getInstanceName() + Utilities.INSTANCE_PATH_SEPARATOR + childNode.getChildXmlTag());
        }
        
        // Now freeze it
        child._setDynamicChangeInfo((XmlRootHandleImpl<?>) myParent._getRoot(), changeInformation);
        
        externalAdd(child, xmlDynamicChange.getDynamicConfiguration(), xmlDynamicChange.getBeanDatabase(), addedServices);
        
        Utilities.invokeVetoableChangeListeners(changeInformation, child, null, child, EMPTY_STRING,
                myParent._getClassReflectionHelper());
        
        if (rawChild != null) {
            // Now we handle the children
            handleChildren(child, (BaseHK2JAXBBean) rawChild, changeInformation, addedServices, xmlDynamicChange);
        }
        
        if (!changeList) {
            return child;
        }
        
        // Now modify the actual list
        if (multiChildren != null) {
            // List or Array child
            multiChildren.add(index, child);
            
            Object finalChildList;
            if (ChildType.LIST.equals(childNode.getChildType())) {
                finalChildList = Collections.unmodifiableList(multiChildren);
            }
            else {
                // ARRAY
                finalChildList = Array.newInstance(childNode.getChildModel().getOriginalInterfaceAsClass(), multiChildren.size());
                for (int lcv = 0; lcv < multiChildren.size(); lcv++) {
                    Array.set(finalChildList, lcv, multiChildren.get(lcv));
                }
            }
            
            if (xmlDynamicChange.getBeanDatabase() != null) {
                myParent.changeInHub(childProperty, finalChildList, xmlDynamicChange.getBeanDatabase());
            }
            
            myParent._setProperty(childProperty, finalChildList, false, true);
        }
        else {
            // Direct child
            if (xmlDynamicChange.getBeanDatabase() != null){
                myParent.changeInHub(childProperty, child, xmlDynamicChange.getBeanDatabase());
            }
            
            myParent._setProperty(childProperty, child, false, true);
        }
        
        return child;
    }
    
    /**
     * Given the fully qualified class name of the interface representing a bean,
     * returns the class name of the proxy
     * 
     * @param iFaceName the never null fully qualified class name of the bean interface
     * @return the class name of the proxy
     */
    public static String getProxyNameFromInterfaceName(String iFaceName) {
        return iFaceName + CLASS_ADD_ON_NAME;
    }
    
    @SuppressWarnings("unchecked")
    private static void handleChildren(BaseHK2JAXBBean child,
            BaseHK2JAXBBean childToCopy,
            DynamicChangeInfo<?> changeInformation,
            List<ActiveDescriptor<?>> addedServices,
            XmlDynamicChange xmlDynamicChange) {
        Map<String, ParentedModel> childrenMap = childToCopy._getModel().getChildrenProperties();
        
        for (Map.Entry<String, ParentedModel> childsChildrenEntry : childrenMap.entrySet()) {
            String childsChildProperty = childsChildrenEntry.getKey();
            ParentedModel childsChildParentNode = childsChildrenEntry.getValue();
            
            if (!ChildType.DIRECT.equals(childsChildParentNode.getChildType())) {
                List<BaseHK2JAXBBean> childsChildren = null;
                if (ChildType.LIST.equals(childsChildParentNode.getChildType())) {
                    childsChildren = (List<BaseHK2JAXBBean>) childToCopy._getProperty(childsChildProperty);
                }
                else {
                    Object arrayChildsChildren = childToCopy._getProperty(childsChildProperty);
                    if (arrayChildsChildren != null) {
                        // This object is an array
                        childsChildren = new ArrayList<BaseHK2JAXBBean>(Array.getLength(arrayChildsChildren));
                        for (int lcv = 0; lcv < Array.getLength(arrayChildsChildren); lcv++) {
                            childsChildren.add(lcv, (BaseHK2JAXBBean) Array.get(arrayChildsChildren, lcv));
                        }
                    }
                }
                
                if (childsChildren == null) continue;
                if (childsChildren.size() <= 0) continue;
                
                ArrayList<BaseHK2JAXBBean> copiedChildArray = new ArrayList<BaseHK2JAXBBean>(childsChildren.size());
                Object asArray = Array.newInstance(childsChildParentNode.getChildModel().getOriginalInterfaceAsClass(), childsChildren.size());
                int lcv = 0;
                for (BaseHK2JAXBBean childsChild : childsChildren) {
                    BaseHK2JAXBBean grandchild = internalAdd(child, childsChildProperty,
                            childsChild, null, -1, changeInformation, xmlDynamicChange, addedServices, false);
                    
                    copiedChildArray.add(grandchild);
                    Array.set(asArray, lcv++, grandchild);
                }
                
                if (ChildType.LIST.equals(childsChildParentNode.getChildType())) {
                    child._setProperty(childsChildProperty, copiedChildArray, false, true);
                }
                else {
                    child._setProperty(childsChildProperty, asArray, false, true);
                }
            }
            else {
                BaseHK2JAXBBean childsChild = (BaseHK2JAXBBean) childToCopy._getProperty(childsChildProperty);
                if (childsChild == null) continue;
                
                BaseHK2JAXBBean grandchild = internalAdd(child, childsChildProperty,
                        childsChild, null, -1, changeInformation, xmlDynamicChange, addedServices, false);
                
                child._setProperty(childsChildProperty, grandchild, false, true);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void externalAdd(BaseHK2JAXBBean root, DynamicConfiguration config, WriteableBeanDatabase writeableDatabase, List<ActiveDescriptor<?>> addedDescriptors) {
        if (config == null && writeableDatabase == null) return;
        
        ActiveDescriptor<?> added = Utilities.advertise(writeableDatabase, config, root);
        if (added != null) {
            addedDescriptors.add(added);
        }
        
        for (String keyedChildProperty : root._getModel().getKeyedChildren()) {
            Object keyedRawChild = root._getProperty(keyedChildProperty);
            if (keyedRawChild == null) continue;
            
            if (keyedRawChild instanceof Iterable) {
                Iterable<BaseHK2JAXBBean> iterable = (Iterable<BaseHK2JAXBBean>) keyedRawChild;
                for (BaseHK2JAXBBean child : iterable) {
                    externalAdd(child, config, writeableDatabase, addedDescriptors);
                }
            }
            else if (keyedRawChild.getClass().isArray()) {
                int aLength = Array.getLength(keyedRawChild);
                for (int lcv = 0; lcv < aLength; lcv++) {
                    BaseHK2JAXBBean child = (BaseHK2JAXBBean) Array.get(keyedRawChild, lcv);
                    externalAdd(child, config, writeableDatabase, addedDescriptors);
                }
                
            }
            else {
                externalAdd((BaseHK2JAXBBean) keyedRawChild, config, writeableDatabase, addedDescriptors);
            }
        }
        
        for (String unkeyedChildProperty : root._getModel().getUnKeyedChildren()) {
            Object unkeyedRawChild = root._getProperty(unkeyedChildProperty);
            if (unkeyedRawChild == null) continue;
            
            if (unkeyedRawChild instanceof Iterable) {
                Iterable<BaseHK2JAXBBean> unkeyedMultiChildren = (Iterable<BaseHK2JAXBBean>) unkeyedRawChild;
                for (BaseHK2JAXBBean child : unkeyedMultiChildren) {
                    externalAdd(child, config, writeableDatabase, addedDescriptors);
                }
            }
            else if (unkeyedRawChild.getClass().isArray()) {
                int aLength = Array.getLength(unkeyedRawChild);
                for (int lcv = 0; lcv < aLength; lcv++) {
                    BaseHK2JAXBBean child = (BaseHK2JAXBBean) Array.get(unkeyedRawChild, lcv);
                    externalAdd(child, config, writeableDatabase, addedDescriptors);
                }
            }
            else {
                externalAdd((BaseHK2JAXBBean) unkeyedRawChild, config, writeableDatabase, addedDescriptors);
            }
            
        }
    }
    
    /**
     * Called with write lock held
     * @param rootNode
     * @param rawRoot
     * @param changeInfo
     * @param helper
     * @param writeableDatabase
     * @param dynamicService
     * @return
     */
    public static BaseHK2JAXBBean _addRoot(ModelImpl rootNode,
            Object rawRoot,
            DynamicChangeInfo<?> changeInfo,
            ClassReflectionHelper helper,
            WriteableBeanDatabase writeableDatabase,
            DynamicConfiguration dynamicService,
            List<ActiveDescriptor<?>> addedServices,
            XmlRootHandleImpl<?> xmlRootHandle) {
        if (!(rawRoot instanceof BaseHK2JAXBBean)) {
            throw new IllegalArgumentException("The root added must be from XmlService.createBean");
        }
        
        BaseHK2JAXBBean child = Utilities.createBean(rootNode.getProxyAsClass());
        child._setClassReflectionHelper(helper);
        
        // Handling of children will be handled once the real child is better setup
        BaseHK2JAXBBean childToCopy = (BaseHK2JAXBBean) rawRoot;
        for (String nonChildProperty : childToCopy._getModel().getNonChildProperties().keySet()) {
            Object value = childToCopy._getProperty(nonChildProperty);
            if (value == null) continue;
            
            child._setProperty(nonChildProperty, value, false);
        }
        
        if (rootNode.getKeyProperty() != null) {
            child._setKeyValue((String) child._getProperty(rootNode.getKeyProperty())); 
        }
        
        child._setSelfXmlTag(rootNode.getRootName());
        child._setInstanceName(rootNode.getRootName());
        
        handleChildren(child, childToCopy, changeInfo, addedServices, XmlDynamicChange.EMPTY);
            
        // Now freeze it
        child._setDynamicChangeInfo(xmlRootHandle, changeInfo);
        
        externalAdd(child, dynamicService, writeableDatabase, addedServices);
        
        return child;
    }
    
    /**
     * Write lock must be held
     * @param myParent
     * @param childProperty
     * @param childKey
     * @param index
     * @param changeInformation
     * @param writeableDatabase
     * @param dynamicService
     * @return
     */
    @SuppressWarnings("unchecked")
    public static BaseHK2JAXBBean internalRemove(
            BaseHK2JAXBBean myParent,
            String childProperty,
            String childKey,
            int index,
            Object childToRemove,
            DynamicChangeInfo<?> changeInformation,
            XmlDynamicChange xmlDynamicChange,
            boolean changeList) {
        if (childProperty == null) return null;
        
        String instanceToRemove = null;
        if (childKey == null && index < 0 && childToRemove != null) {
            // Need to check that the parent is the same as me
            if (!(childToRemove instanceof BaseHK2JAXBBean)) {
                throw new IllegalArgumentException("Removed child must be a child of the parent " + myParent + " but is not of the correct type " +
                  childToRemove.getClass().getName());
            }
            
            BaseHK2JAXBBean childToRemoveBean = (BaseHK2JAXBBean) childToRemove;
            BaseHK2JAXBBean childToRemoveParent = (BaseHK2JAXBBean) childToRemoveBean._getParent();
            
            if (childToRemoveParent == null) {
                throw new IllegalArgumentException("Removed child must be a child of the parent " + myParent + " but has no parent of its own");
            }
            
            if (!childToRemoveParent.equals(myParent)) {
                throw new IllegalArgumentException("Removed child must be a child of the parent " + myParent + " but has a different parent " +
                  childToRemoveParent);
            }
            
            instanceToRemove = childToRemoveBean._getInstanceName();
        }
        
        ParentedModel removeMeParentedNode = myParent._getModel().getChild(childProperty);
        ModelImpl removeMeNode = removeMeParentedNode.getChildModel();
        BaseHK2JAXBBean rootForDeletion = null;
        
        if (!ChildType.DIRECT.equals(removeMeParentedNode.getChildType())) {
            if (childKey == null && index < 0 && instanceToRemove == null) return null;
            
            if (ChildType.LIST.equals(removeMeParentedNode.getChildType())) {
                List<BaseHK2JAXBBean> removeFromList = (List<BaseHK2JAXBBean>) myParent._getProperty(childProperty);
                
                if (removeFromList == null) return null;
                
                int minusOneSize = removeFromList.size() - 1;
                if (minusOneSize < 0) return null;
                
                List<BaseHK2JAXBBean> listWithObjectRemoved = new ArrayList<BaseHK2JAXBBean>(minusOneSize + 1);
                
                if (childKey != null || instanceToRemove != null) {
                    String comparisonKey = (childKey != null) ? childKey : instanceToRemove ;
                    
                    for (BaseHK2JAXBBean candidate : removeFromList) {
                        String candidateKeyValue;
                        if (childKey != null) {
                            candidateKeyValue = candidate._getKeyValue();
                        }
                        else {
                            candidateKeyValue = candidate._getInstanceName();
                        }
                        
                        if (GeneralUtilities.safeEquals(candidateKeyValue, comparisonKey)) {
                            rootForDeletion = candidate;
                        }
                        else {
                            listWithObjectRemoved.add(candidate);
                        }
                    }
                }
                else {
                    // unkeyed, index >= 0
                    if (index >= removeFromList.size()) {
                        return null;
                    }
                    
                    for (int lcv = 0; lcv < removeFromList.size(); lcv++) {
                        if (lcv == index) {
                            rootForDeletion = removeFromList.get(lcv);
                        }
                        else {
                            listWithObjectRemoved.add(removeFromList.get(lcv));
                        }
                    }
                }
                
                if (rootForDeletion == null) return null;
                
                if (changeList) {
                    if (xmlDynamicChange.getBeanDatabase() != null) {
                        myParent.changeInHub(childProperty, listWithObjectRemoved, xmlDynamicChange.getBeanDatabase());
                    }
                
                    myParent._setProperty(childProperty, listWithObjectRemoved, false, true);
                }
            }
            else {
                // array children
                Object removeFromArray = myParent._getProperty(childProperty);
                
                if (removeFromArray == null) return null;
                
                int removeFromArrayLength = Array.getLength(removeFromArray);
                if (removeFromArrayLength == 0) return null;
                
                Class<?> arrayType = removeMeNode.getOriginalInterfaceAsClass();
                
                Object arrayWithObjectRemoved = Array.newInstance(arrayType, removeFromArrayLength - 1);
                
                if (childKey != null || instanceToRemove != null) {
                    String comparisonKey = (childKey != null) ? childKey : instanceToRemove ;
                    
                    int removeIndex = -1;
                    for (int lcv = 0; lcv < removeFromArrayLength; lcv++) {
                        BaseHK2JAXBBean candidate = (BaseHK2JAXBBean) Array.get(removeFromArray, lcv);
                        
                        String candidateKeyValue;
                        if (childKey != null) {
                            candidateKeyValue = candidate._getKeyValue();
                        }
                        else {
                            candidateKeyValue = candidate._getInstanceName();
                        }
                        
                        if (GeneralUtilities.safeEquals(candidateKeyValue, comparisonKey)) {
                            rootForDeletion = candidate;
                            removeIndex = lcv;
                            break;
                        }
                    }
                    
                    if (rootForDeletion == null) return null;
                    
                    int addIndex = 0;
                    for (int lcv = 0; lcv < removeFromArrayLength; lcv++) {
                        if (lcv == removeIndex) continue;
                        
                        Array.set(arrayWithObjectRemoved, addIndex++, Array.get(removeFromArray, lcv));
                    }
                }
                else if (index >= 0) {
                    // unkeyed, index >= 0
                    if (index >= removeFromArrayLength) {
                        return null;
                    }
                    
                    rootForDeletion = (BaseHK2JAXBBean) Array.get(removeFromArray, index);
                    
                    int addIndex = 0;
                    for (int lcv = 0; lcv < removeFromArrayLength; lcv++) {
                        if (lcv == index) continue;
                        
                        Array.set(arrayWithObjectRemoved, addIndex++, Array.get(removeFromArray, lcv));
                    }
                }
                else {
                    int removeIndex = -1;
                    for (int lcv = 0; lcv < removeFromArrayLength; lcv++) {
                        BaseHK2JAXBBean candidate = (BaseHK2JAXBBean) Array.get(removeFromArray, lcv);
                        
                        String candidateKeyValue = candidate._getInstanceName();
                        
                        if (GeneralUtilities.safeEquals(candidateKeyValue, instanceToRemove)) {
                            rootForDeletion = candidate;
                            removeIndex = lcv;
                            break;
                        }
                    }
                    
                    if (rootForDeletion == null) return null;
                    
                    int addIndex = 0;
                    for (int lcv = 0; lcv < removeFromArrayLength; lcv++) {
                        if (lcv == removeIndex) continue;
                        
                        Array.set(arrayWithObjectRemoved, addIndex++, Array.get(removeFromArray, lcv));
                    }
                    
                }
                
                if (rootForDeletion == null) return null;
                
                if (changeList) {
                    if (xmlDynamicChange.getBeanDatabase() != null) {
                        myParent.changeInHub(childProperty, arrayWithObjectRemoved, xmlDynamicChange.getBeanDatabase());
                    }
                
                    myParent._setProperty(childProperty, arrayWithObjectRemoved, false, true);
                }
            }
        }
        else {
            // Direct child
            rootForDeletion = (BaseHK2JAXBBean) myParent._getProperty(childProperty);
            if (rootForDeletion == null) return null;
            
            if (changeList) {
                if (xmlDynamicChange.getBeanDatabase() != null) {
                    myParent.changeInHub(childProperty, null, xmlDynamicChange.getBeanDatabase());
                }
            
                myParent._setProperty(childProperty, null, false, true);
            }
        }
        
        // Need to get all the beans to delete
        invokeAllDeletedChangeListeners(changeInformation, rootForDeletion, myParent._getClassReflectionHelper());
        
        if (xmlDynamicChange.getDynamicConfiguration() != null) {
            HashSet<ActiveDescriptor<?>> descriptorsToRemove = new HashSet<ActiveDescriptor<?>>();
            
            getDescriptorsToRemove(rootForDeletion, descriptorsToRemove);
            
            for (ActiveDescriptor<?> descriptorToRemove : descriptorsToRemove) {
                xmlDynamicChange.getDynamicConfiguration().addUnbindFilter(BuilderHelper.createSpecificDescriptorFilter(descriptorToRemove));
            }
        }
        
        if (xmlDynamicChange.getBeanDatabase() != null) {
            String rootXmlPath = rootForDeletion._getXmlPath();
            String rootInstanceName = rootForDeletion._getInstanceName();
            
            WriteableType rootType = xmlDynamicChange.getBeanDatabase().getWriteableType(rootXmlPath);
            if (rootType != null) {
                rootType.removeInstance(rootInstanceName);
            
                String typeRemovalIndicator = rootXmlPath + BaseHK2JAXBBean.XML_PATH_SEPARATOR;
            
                Set<WriteableType> allTypes = xmlDynamicChange.getBeanDatabase().getAllWriteableTypes();
                for (WriteableType allType : allTypes) {
                    if (allType.getName().startsWith(typeRemovalIndicator)) {
                        
                        Map<String, Instance> allInstances = allType.getInstances();
                        
                        Set<String> removeMe = new LinkedHashSet<String>();
                        
                        // Do not forget to put the dot at the end or it will remove too much
                        String rootInstancePrefix = rootInstanceName + ".";
                        for (String iKey : allInstances.keySet()) {
                            if (!iKey.startsWith(rootInstancePrefix)) continue;
                            removeMe.add(iKey);
                        }
                        
                        for (String iKey : removeMe) {
                            allType.removeInstance(iKey);
                        }
                    }
                }
            }
        }
        
        return rootForDeletion;
    }
    
    @SuppressWarnings("unchecked")
    private static void getDescriptorsToRemove(BaseHK2JAXBBean fromMe, HashSet<ActiveDescriptor<?>> descriptorsToRemove) {
        ActiveDescriptor<?> fromMeDescriptor = fromMe._getSelfDescriptor();
        if (fromMeDescriptor == null) return;
        
        descriptorsToRemove.add(fromMeDescriptor);
        
        ModelImpl model = fromMe._getModel();
        if (model == null) return;
        
        for (ParentedModel parentedChild : model.getAllChildren()) {
            String childPropertyName = parentedChild.getChildXmlTag();
            
            switch (parentedChild.getChildType()) {
            case LIST:
                List<BaseHK2JAXBBean> listChildren = (List<BaseHK2JAXBBean>) fromMe._getProperty(childPropertyName);
                if (listChildren != null) {
                    for (BaseHK2JAXBBean listChild : listChildren) {
                        getDescriptorsToRemove(listChild, descriptorsToRemove);
                    }
                }
                break;
            case ARRAY:
                Object arrayChildren = fromMe._getProperty(childPropertyName);
                if (arrayChildren != null) {
                    int arrayLength = Array.getLength(arrayChildren);
                    
                    for (int lcv = 0; lcv < arrayLength; lcv++) {
                        BaseHK2JAXBBean bean = (BaseHK2JAXBBean) Array.get(arrayChildren, lcv);
                        getDescriptorsToRemove(bean, descriptorsToRemove);
                    }
                }
                break;
            case DIRECT:
                BaseHK2JAXBBean bean = (BaseHK2JAXBBean) fromMe._getProperty(childPropertyName);
                if (bean != null) {
                    getDescriptorsToRemove(bean, descriptorsToRemove);
                }
                break;
            default:
                throw new AssertionError("Unknown child type " + parentedChild.getChildType());
            }
            
        }
    }
    
    private final static Boolean DEFAULT_BOOLEAN = Boolean.FALSE;
    private final static Byte DEFAULT_BYTE = new Byte((byte) 0);
    private final static Character DEFAULT_CHARACTER = new Character((char) 0);
    private final static Short DEFAULT_SHORT = new Short((short) 0);
    private final static Integer DEFAULT_INTEGER = new Integer(0);
    private final static Long DEFAULT_LONG = new Long(0L);
    private final static Float DEFAULT_FLOAT = new Float(0);
    private final static Double DEFAULT_DOUBLE = new Double((double) 0);
    
    /**
     * Returns the default value given the string version of the default and
     * the expected result (non-child properties)
     * 
     * @param givenStringDefault
     * @param expectedClass
     * @return
     */
    public static Object getDefaultValue(String givenStringDefault, Class<?> expectedClass) {
        if (givenStringDefault == null || JAUtilities.JAXB_DEFAULT_DEFAULT.equals(givenStringDefault)) {
            if (int.class.equals(expectedClass)) {
                return DEFAULT_INTEGER;
            }
            if (long.class.equals(expectedClass)) {
                return DEFAULT_LONG;
            }
            if (boolean.class.equals(expectedClass)) {
                return DEFAULT_BOOLEAN;
            }
            if (short.class.equals(expectedClass)) {
                return DEFAULT_SHORT;
            }
            if (byte.class.equals(expectedClass)) {
                return DEFAULT_BYTE;
            }
            if (char.class.equals(expectedClass)) {
                return DEFAULT_CHARACTER;
            }
            if (float.class.equals(expectedClass)) {
                return DEFAULT_FLOAT;
            }
            if (double.class.equals(expectedClass)) {
                return DEFAULT_DOUBLE;
            }
            
            return null;
        }
        
        if (String.class.equals(expectedClass)) {
            return givenStringDefault;
        }
        if (int.class.equals(expectedClass)) {
            return Integer.parseInt(givenStringDefault);
        }
        if (long.class.equals(expectedClass)) {
            return Long.parseLong(givenStringDefault);
        }
        if (boolean.class.equals(expectedClass)) {
            return Boolean.parseBoolean(givenStringDefault);
        }
        if (short.class.equals(expectedClass)) {
            return Short.parseShort(givenStringDefault);
        }
        if (byte.class.equals(expectedClass)) {
            return Byte.parseByte(givenStringDefault);
        }
        if (char.class.equals(expectedClass)) {
            return givenStringDefault.charAt(0);
        }
        if (float.class.equals(expectedClass)) {
            return Float.parseFloat(givenStringDefault);
        }
        if (double.class.equals(expectedClass)) {
            return Double.parseDouble(givenStringDefault);
        }
        if (expectedClass.isArray() && byte.class.equals(expectedClass.getComponentType())) {
            return givenStringDefault.getBytes();
            // return DatatypeConverter.parseHexBinary(givenStringDefault);
        }
        
        throw new AssertionError("Default for type " + expectedClass.getName() + " not implemented");
    }
    
    public static void fillInUnfinishedReferences(Map<ReferenceKey, BaseHK2JAXBBean> referenceMap,
            List<UnresolvedReference> unresolved) {
        List<Throwable> errors = new LinkedList<Throwable>();
        
        for (UnresolvedReference unresolvedRef : unresolved) {
            ReferenceKey key = new ReferenceKey(unresolvedRef.getType(), unresolvedRef.getXmlID());
            BaseHK2JAXBBean reference = referenceMap.get(key);
            if (reference == null) {
                errors.add(new IllegalStateException("No Reference was found for " + unresolvedRef));
            }
            
            BaseHK2JAXBBean unfinished = unresolvedRef.getUnfinished();
            unfinished._setProperty(unresolvedRef.getPropertyName(), reference);
        }
        
        if (!errors.isEmpty()) {
            throw new MultiException(errors);
        }
    }
    
    public static Method findSuitableCustomizerMethod(Class<?> cClass, String methodName, Class<?>[] params, Class<?> topInterface) {
        try {
            return cClass.getMethod(methodName, params);
        }
        catch (NoSuchMethodException nsme) {
            // Go on
        }
        
        if (topInterface == null) return null;
        
        {
            // First look for an exact match which should be prioritized over others
            int altParamsLength = params.length + 1;
            Class<?> exactParams[] = new Class<?>[altParamsLength];
            
            exactParams[0] = topInterface;
            for (int lcv = 0; lcv < params.length; lcv++) {
                exactParams[lcv+1] = params[lcv];
            }
            
            try {
                return cClass.getMethod(methodName, exactParams);
            }
            catch (NoSuchMethodException nsme) {
                // Go on
            }
        }
        
        
        for (Method candidate : cClass.getMethods()) {
            if (!methodName.equals(candidate.getName())) continue;
            
            int altParamsLength = params.length + 1;
            Class<?> candidateParams[] = candidate.getParameterTypes();
            
            if (candidateParams.length != altParamsLength) continue;
            
            if (!candidateParams[0].isAssignableFrom(topInterface)) continue;
            
            // Now check all the other params
            boolean found = true;
            for (int lcv = 1; lcv < altParamsLength; lcv++) {
                if (!candidateParams[lcv].equals(params[lcv - 1])) {
                    found = false;
                    break;
                }
            }
            if (!found) continue;
            
            return candidate;
            
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private static void invokeAllDeletedChangeListeners(DynamicChangeInfo<?> control,
            BaseHK2JAXBBean rootBean,
            ClassReflectionHelper helper) {
        ModelImpl model = rootBean._getModel();
        
        Map<String, ParentedModel> childrenByName = model.getChildrenByName();
        for (Map.Entry<String, ParentedModel> entry : childrenByName.entrySet()) {
            String propertyName = entry.getKey();
            ParentedModel parentModel = entry.getValue();
            
            Object child = rootBean._getProperty(propertyName);
            if (child == null) continue;
            
            if (ChildType.LIST.equals(parentModel.getChildType())) {
                List<BaseHK2JAXBBean> listChildren = (List<BaseHK2JAXBBean>) child;
                
                for (BaseHK2JAXBBean grandchild : listChildren) {
                    invokeAllDeletedChangeListeners(control, grandchild, helper);
                }
            }
            else if (ChildType.ARRAY.equals(parentModel.getChildType())) {
                int length = Array.getLength(child);
                
                for (int lcv = 0; lcv < length; lcv++) {
                    BaseHK2JAXBBean grandchild = (BaseHK2JAXBBean) Array.get(child, lcv);
                    invokeAllDeletedChangeListeners(control, grandchild, helper);
                }
            }
            else if (ChildType.DIRECT.equals(parentModel.getChildType())) {
                BaseHK2JAXBBean grandchild = (BaseHK2JAXBBean) child;
                
                invokeAllDeletedChangeListeners(control, grandchild, helper);
            }
        }
        
        invokeVetoableChangeListeners(control, rootBean, rootBean, null, EMPTY_STRING, helper);
    }
    
    @SuppressWarnings("unchecked")
    public static void invokeVetoableChangeListeners(DynamicChangeInfo<?> control,
            BaseHK2JAXBBean source,
            Object oldValue,
            Object newValue,
            String propertyName,
            ClassReflectionHelper helper) {
        if (control == null) return;
        
        Validator validator = control.findValidator();
        if (validator != null) {
            ModelImpl model = source._getModel();
            
            String javaName = model.getJavaNameFromKey(propertyName, helper);
            
            if (javaName != null) {
                Set<ConstraintViolation<BaseHK2JAXBBean>> violations =
                    validator.<BaseHK2JAXBBean>validateValue((Class<BaseHK2JAXBBean>) source.getClass(), javaName, newValue);
                if (violations != null && !violations.isEmpty()) {
                    throw new MultiException(new ConstraintViolationException(violations));
                }
            }
        }
        
        List<VetoableChangeListener> vetoers = control.getChangeListeners();
            
        PropertyChangeEvent event = new PropertyChangeEvent(source,
                    propertyName, oldValue, newValue);
            
        List<Throwable> errors = new LinkedList<Throwable>();
        for (VetoableChangeListener listener : vetoers) {
            try {
                listener.vetoableChange(event);
            }
            catch (PropertyVetoException pve) {
                // In this case we do NOT run the subsequent listeners
                errors.add(pve);
                throw new MultiException(errors);
            }
            catch (Throwable th) {
                // In this case we DO run the subsequent listeners but will
                // report all the errors in the end
                errors.add(th);
            }
        }
        if (!errors.isEmpty()) {
            throw new MultiException(errors);
        }
    }
    
    /**
     * Must have write lock of source held though this is only doing reading
     * 
     * @param classReflectionHelper
     * @param source
     * @param other
     * @return
     */
    public static Differences getDiff(BaseHK2JAXBBean source,
            BaseHK2JAXBBean other) {
        ModelImpl sourceModel = source._getModel();
        ModelImpl otherModel = other._getModel();
        
        if (!sourceModel.equals(otherModel)) {
            throw new AssertionError("Can only diff two beans of the same type.  Source is " + sourceModel + " other is " + otherModel);
        }
        
        Differences retVal = new Differences();
        
        getAllDifferences(source, other, retVal);
        
        return retVal;
    }
    
    private static Map<String, Integer> getIndexMap(List<BaseHK2JAXBBean> list) {
        Map<String, Integer> retVal = new HashMap<String, Integer>();
        for (int lcv = 0; lcv < list.size(); lcv++) {
            BaseHK2JAXBBean bean = list.get(lcv);
            String key = bean._getKeyValue();
            
            if (key == null) {
                throw new AssertionError("Found a keyed bean with no key " + bean + " at index " + lcv + " in " + list);
            }
            
            retVal.put(key, lcv);
        }
        
        return retVal;
    }
    
    private static Map<String, Integer> getIndexMapArray(Object array) {
        Map<String, Integer> retVal = new HashMap<String, Integer>();
        
        int length = Array.getLength(array);
        for (int lcv = 0; lcv < length; lcv++) {
            BaseHK2JAXBBean bean = (BaseHK2JAXBBean) Array.get(array, lcv);
            String key = bean._getKeyValue();
            
            if (key == null) {
                throw new AssertionError("Found a keyed bean with no key " + bean + " at index " + lcv);
            }
            
            retVal.put(key, lcv);
        }
        
        return retVal;
    }
    
    @SuppressWarnings("unchecked")
    private static void getAllDifferences(BaseHK2JAXBBean source,
            BaseHK2JAXBBean other,
            Differences differences) {
        Difference localDifference = new Difference(source);
        
        ModelImpl sourceModel = source._getModel();
        
        Map<String, Object> sourceMap = source._getBeanLikeMap();
        Map<String, Object> otherMap = other._getBeanLikeMap();
        
        Map<String, ChildDataModel> nonChildProperties = sourceModel.getNonChildProperties();
        
        for (Map.Entry<String, ChildDataModel> nonChildPropertyEntry : nonChildProperties.entrySet()) {
            String nonChildProperty = nonChildPropertyEntry.getKey();
            ChildDataModel dataModel = nonChildPropertyEntry.getValue();
                    
            Object sourceValue = sourceMap.get(nonChildProperty);
            Object otherValue = otherMap.get(nonChildProperty);
            
            if (!dataModel.isReference()) {
                if (!GeneralUtilities.safeEquals(sourceValue, otherValue)) {
                    localDifference.addNonChildChange(new PropertyChangeEvent(source, nonChildProperty, sourceValue, otherValue));
                }
            }
            else {
                // Comparing references
                if (sourceValue != null && otherValue == null) {
                    localDifference.addNonChildChange(new PropertyChangeEvent(source, nonChildProperty, sourceValue, otherValue));
                }
                else if (sourceValue == null && otherValue != null) {
                    localDifference.addNonChildChange(new PropertyChangeEvent(source, nonChildProperty, sourceValue, otherValue));
                }
                else if (sourceValue != null) {
                    BaseHK2JAXBBean sourceReference = (BaseHK2JAXBBean) sourceValue;
                    BaseHK2JAXBBean otherReference = (BaseHK2JAXBBean) otherValue;
                    
                    String sourceReferenceKey = sourceReference._getKeyValue();
                    String otherReferenceKey = otherReference._getKeyValue();
                    
                    if (!GeneralUtilities.safeEquals(sourceReferenceKey, otherReferenceKey)) {
                        localDifference.addNonChildChange(new PropertyChangeEvent(source, nonChildProperty, sourceValue, otherValue));
                    }
                    
                }
                
            }
        }
        
        Map<String, ParentedModel> childProperties = sourceModel.getChildrenByName();
        for (Map.Entry<String, ParentedModel> childEntry : childProperties.entrySet()) {
            String xmlTag = childEntry.getKey();
            ParentedModel pModel = childEntry.getValue();
            
            Object sourceValue = sourceMap.get(xmlTag);
            Object otherValue = otherMap.get(xmlTag);
            
            if (ChildType.DIRECT.equals(pModel.getChildType())) {
                if (sourceValue == null && otherValue != null) {
                    // This is just a pure add
                    localDifference.addAdd(xmlTag, (BaseHK2JAXBBean) otherValue, -1); 
                }
                else if (sourceValue != null && otherValue == null) {
                    // A pure remove
                    localDifference.addRemove(xmlTag, new RemoveData(xmlTag, (BaseHK2JAXBBean) sourceValue));
                }
                else if (sourceValue != null) {
                    getAllDifferences((BaseHK2JAXBBean) sourceValue, (BaseHK2JAXBBean) otherValue, differences);
                }
            }
            else if (ChildType.LIST.equals(pModel.getChildType())) {
                String keyProperty = pModel.getChildModel().getKeyProperty();
                
                List<BaseHK2JAXBBean> sourceValueList = (List<BaseHK2JAXBBean>) sourceValue;
                List<BaseHK2JAXBBean> otherValueList = (List<BaseHK2JAXBBean>) otherValue;
                
                if (sourceValueList == null) sourceValueList = Collections.emptyList();
                if (otherValueList == null) otherValueList = Collections.emptyList();
                
                if (keyProperty != null) {
                    Map<String, Integer> sourceIndexMap = getIndexMap(sourceValueList);
                    Map<String, Integer> otherIndexMap = getIndexMap(otherValueList);
                    
                    for (BaseHK2JAXBBean sourceBean : sourceValueList) {
                        String sourceKeyValue = sourceBean._getKeyValue();
                        
                        if (!otherIndexMap.containsKey(sourceKeyValue)) {
                            localDifference.addRemove(xmlTag, new RemoveData(xmlTag, sourceKeyValue, sourceBean));
                        }
                        else {
                            int sourceIndex = sourceIndexMap.get(sourceKeyValue);
                            int otherIndex = otherIndexMap.get(sourceKeyValue);
                            
                            Object otherBean = otherValueList.get(otherIndex);
                            
                            if (otherIndex != sourceIndex) {
                                localDifference.addMove(xmlTag, new MoveData(sourceIndex, otherIndex));
                            }
                            
                            // Need to know sub-differences
                            getAllDifferences(sourceBean, (BaseHK2JAXBBean) otherBean, differences);
                        }
                    }
                    
                    for (BaseHK2JAXBBean otherBean : otherValueList) {
                        String otherKeyValue = otherBean._getKeyValue();
                        
                        if (!sourceIndexMap.containsKey(otherKeyValue)) {
                            int addedIndex = otherIndexMap.get(otherKeyValue);
                            
                            localDifference.addAdd(xmlTag, otherBean, addedIndex);
                        }
                    }
                }
                else {
                    // Both lists are there, this is an unkeyed list, we go *purely* on list size
                    UnkeyedDiff unkeyedDiff = new UnkeyedDiff(sourceValueList, otherValueList, source, pModel);
                    Differences unkeyedDiffs = unkeyedDiff.compute();
                    
                    differences.merge(unkeyedDiffs);
                }
            }
            else if (ChildType.ARRAY.equals(pModel.getChildType())) {
                String keyProperty = pModel.getChildModel().getKeyProperty();
                
                Object sourceArray = (sourceValue == null) ? new BaseHK2JAXBBean[0] : sourceValue ;
                Object otherArray = (otherValue == null) ? new BaseHK2JAXBBean[0] : otherValue;
                
                if (keyProperty != null) {
                    Map<String, Integer> sourceIndexMap = getIndexMapArray(sourceArray);
                    Map<String, Integer> otherIndexMap = getIndexMapArray(otherArray);
                    
                    int sourceLength = Array.getLength(sourceArray);
                    
                    for (int lcv = 0; lcv < sourceLength; lcv++) {
                        BaseHK2JAXBBean sourceBean = (BaseHK2JAXBBean) Array.get(sourceArray, lcv);
                        
                        String sourceKeyValue = sourceBean._getKeyValue();
                        
                        if (!otherIndexMap.containsKey(sourceKeyValue)) {
                            // Removing this bean
                            localDifference.addRemove(xmlTag, new RemoveData(xmlTag, sourceKeyValue, sourceBean));
                        }
                        else {
                            int sourceIndex = sourceIndexMap.get(sourceKeyValue);
                            int otherIndex = otherIndexMap.get(sourceKeyValue);
                            
                            BaseHK2JAXBBean otherBean = (BaseHK2JAXBBean) Array.get(otherArray, otherIndex);
                            
                            if (sourceIndex != otherIndex) {
                                // Bean was moved
                                localDifference.addMove(xmlTag, new MoveData(sourceIndex, otherIndex));
                            }
                            
                            // Get all changes to sub bean
                            getAllDifferences(sourceBean, otherBean, differences);
                        }
                    }
                    
                    int otherLength = Array.getLength(otherArray);
                    
                    for (int lcv = 0; lcv < otherLength; lcv++) {
                        BaseHK2JAXBBean otherBean = (BaseHK2JAXBBean) Array.get(otherArray, lcv);
                        
                        String otherKeyValue = otherBean._getKeyValue();
                        
                        if (!sourceIndexMap.containsKey(otherKeyValue)) {
                            // This is an add
                            localDifference.addAdd(xmlTag, otherBean, lcv);
                        }
                    }
                }
                else {  
                    // Both lists are there, this is an unkeyed list
                    UnkeyedDiff unkeyedDiff = new UnkeyedDiff((Object[]) sourceArray, (Object[]) otherArray, source, pModel);
                    Differences unkeyedDiffs = unkeyedDiff.compute();
                    
                    differences.merge(unkeyedDiffs);
                }
            }
        }
        
        if (localDifference.isDirty()) {
            differences.addDifference(localDifference);
        }
    }
    
    /**
     * Must have write lock of source held
     * 
     * @param classReflectionHelper
     * @param source
     * @param other
     * @return
     */
    public static void applyDiff(Differences differences, DynamicChangeInfo<?> changeControl) {
        for (Difference difference : differences.getDifferences()) {
            BaseHK2JAXBBean source = difference.getSource();
            
            List<PropertyChangeEvent> allSourceChanges = new LinkedList<PropertyChangeEvent>();
            allSourceChanges.addAll(difference.getNonChildChanges());
            
            if (!difference.hasChildChanges()) {
                applyAllSourceChanges(source, allSourceChanges, changeControl);
                continue;
            }
            
            ModelImpl model = source._getModel();
            
            for (Map.Entry<String, AddRemoveMoveDifference> childEntry : difference.getChildChanges().entrySet()) {
                String xmlKey = childEntry.getKey();
                AddRemoveMoveDifference childDiffs = childEntry.getValue();
                ParentedModel parentedModel = model.getChild(xmlKey);
                ChildType childType = parentedModel.getChildType();
                
                boolean changeList = ChildType.DIRECT.equals(childType);
                
                Object oldListOrArray = source._getProperty(xmlKey);
                Map<Integer, BaseHK2JAXBBean> arrayChanges = null;
                
                if (!changeList) {
                  arrayChanges = new LinkedHashMap<Integer, BaseHK2JAXBBean>();
                }
                
                for (AddData added : childDiffs.getAdds()) {
                    BaseHK2JAXBBean addMe = added.getToAdd();
                    int index = added.getIndex();
                
                    BaseHK2JAXBBean addedBean = (BaseHK2JAXBBean) source._doAdd(xmlKey, addMe, null, index, false);
                    if (!changeList) {
                        arrayChanges.put(index, addedBean);
                    }
                    else {
                        allSourceChanges.add(new PropertyChangeEvent(source, xmlKey, null, addedBean));
                    }
                }
                
                for (RemoveData removed : childDiffs.getRemoves()) {
                    source._doRemove(xmlKey, removed.getChildKey(), removed.getIndex(), removed.getChild(), false);
                    if (changeList) {
                        allSourceChanges.add(new PropertyChangeEvent(source, xmlKey, oldListOrArray, null));
                    }
                }
                
                for (MoveData md : childDiffs.getMoves()) {
                    BaseHK2JAXBBean movedBean = getLOABean(oldListOrArray, childType, md.getOldIndex());
                    
                    if (!changeList) {
                        arrayChanges.put(md.getNewIndex(), movedBean);
                    }
                }
                
                if (!changeList) {
                    int newSize = childDiffs.getNewSize(getLOASize(oldListOrArray, childType));
                    Object newListOrArray = createLOA(childType, newSize, parentedModel.getChildModel());
                    
                    for (int lcv = 0; lcv < newSize; lcv++) {
                        BaseHK2JAXBBean toPut = arrayChanges.get(lcv);
                        
                        if (toPut == null) {
                            toPut = getLOABean(oldListOrArray, childType, lcv);
                        }
                        
                        putLOABean(newListOrArray, childType, lcv, toPut);
                    }
                    
                    allSourceChanges.add(new PropertyChangeEvent(source, xmlKey, oldListOrArray, newListOrArray));
                }
            }
            
            applyAllSourceChanges(source, allSourceChanges, changeControl);
        }
    }
    
    private static void applyAllSourceChanges(BaseHK2JAXBBean source, List<PropertyChangeEvent> events, DynamicChangeInfo<?> changeControl) {
        boolean success = false;
        XmlDynamicChange xmlDynamicChange = changeControl.startOrContinueChange(source);
        try {
            WriteableBeanDatabase wbd = xmlDynamicChange.getBeanDatabase();
            
            boolean madeAChange = false;
            for (PropertyChangeEvent pce : events) {
                if (!GeneralUtilities.safeEquals(pce.getOldValue(), pce.getNewValue())) {
                    madeAChange = true;
                    Utilities.invokeVetoableChangeListeners(changeControl,
                            source,
                            pce.getOldValue(),
                            pce.getNewValue(),
                            pce.getPropertyName(),
                            source._getClassReflectionHelper());
                }
            }
            
            if (!madeAChange) {
                success = true;
                
                return;
            }
            
            if (wbd != null) {
                source.changeInHub(events, wbd);
            }
            
            for (PropertyChangeEvent pce : events) { 
                source._setProperty(pce.getPropertyName(), pce.getNewValue(), false, true);
            }
            
            success = true;
        }
        finally {
            changeControl.endOrDeferChange(success);
        }
    }
    
    private static Object createLOA(ChildType type, int size, ModelImpl childModel) {
        if (ChildType.ARRAY.equals(type)) {
            return Array.newInstance(childModel.getOriginalInterfaceAsClass(), size);
        }
        
        return new ArrayList<BaseHK2JAXBBean>(size);
    }
    
    @SuppressWarnings("unchecked")
    private static void putLOABean(Object listOrArray, ChildType type, int index, BaseHK2JAXBBean putMe) {
        if (ChildType.ARRAY.equals(type)) {
            Array.set(listOrArray, index, putMe);
            return;
        }

        List<BaseHK2JAXBBean> list = (List<BaseHK2JAXBBean>) listOrArray;
            
        list.add(index, putMe);
    }
    
    @SuppressWarnings("unchecked")
    private static BaseHK2JAXBBean getLOABean(Object listOrArray, ChildType type, int index) {
        if (ChildType.ARRAY.equals(type)) {
            return (BaseHK2JAXBBean) Array.get(listOrArray, index);
        }
        
        if (ChildType.LIST.equals(type)) {
            List<BaseHK2JAXBBean> list = (List<BaseHK2JAXBBean>) listOrArray;
            
            return list.get(index);
        }
        
        return (BaseHK2JAXBBean) listOrArray;
        
    }
    
    // LOA stands for List Or Array
    @SuppressWarnings("unchecked")
    private static int getLOASize(Object listOrArray, ChildType type) {
        if (ChildType.ARRAY.equals(type)) {
            return Array.getLength(listOrArray);
        }
        if (ChildType.LIST.equals(type)) {
            List<BaseHK2JAXBBean> list = (List<BaseHK2JAXBBean>) listOrArray;
            
            return list.size();
        }
        
        // Direct is always size 1
        return 1;
    }
    
    /**
     * Calculates and sets the add cost for the given bean
     * and sets all add costs for this bean and all its
     * children
     * 
     * @param bean The bean to calculate and set the
     * add cost on
     * @return The add cost, which will be at least one,
     * or -1 if the bean is null
     */
    @SuppressWarnings("unchecked")
    public static int calculateAddCost(BaseHK2JAXBBean bean) {
        if (bean == null) return -1;
        
        int knownValue = bean.__getAddCost();
        if (knownValue >= 0) return knownValue;
        
        int retVal = 1;
        
        ModelImpl model = bean._getModel();
        for (ParentedModel parentedModel : model.getAllChildren()) {
            String propName = parentedModel.getChildXmlTag();
            
            Object rawChild = bean._getProperty(propName);
            if (rawChild == null) continue;
            
            switch (parentedModel.getChildType()) {
            case DIRECT:
                {
                    BaseHK2JAXBBean child = (BaseHK2JAXBBean) rawChild;
                    int childCost = calculateAddCost(child);
                
                    retVal += childCost;
                }
                break;
            case LIST:
                List<BaseHK2JAXBBean> childList = (List<BaseHK2JAXBBean>) rawChild;
                for (BaseHK2JAXBBean child : childList) {
                    int childCost = calculateAddCost(child);
                    
                    retVal += childCost;
                }
                break;
            case ARRAY:
                int length = Array.getLength(rawChild);
                for (int lcv = 0; lcv < length; lcv++) {
                    BaseHK2JAXBBean child = (BaseHK2JAXBBean) Array.get(rawChild, lcv);
                    int childCost = calculateAddCost(child);
                    
                    retVal += childCost;
                }
                break;
            default:
                throw new AssertionError("Unknown child type " + parentedModel.getChildType());
            }
        }
        
        bean.__setAddCost(retVal);
        return retVal;
    }
    
    public static List<AltMethod> prioritizeMethods(List<AltMethod> methods, String specifiedOrdering[], NameInformation xmlMap) {
        if (specifiedOrdering == null || specifiedOrdering.length <= 0) {
            return methods;
        }
        
        Map<String, Integer> orderingAsMap = new HashMap<String, Integer>();
        for (int lcv = 0; lcv < specifiedOrdering.length; lcv++) {
            orderingAsMap.put(specifiedOrdering[lcv], lcv);
        }
        
        Map<AltMethod, Integer> secondarySort = new HashMap<AltMethod, Integer>();
        int lcv = 0;
        for (AltMethod method : methods) {
            secondarySort.put(method, lcv);
            lcv++;
        }
        
        TreeSet<AltMethod> orderedSet = new TreeSet<AltMethod>(new SpecifiedOrderComparator(orderingAsMap, secondarySort, xmlMap));
        orderedSet.addAll(methods);
        
        ArrayList<AltMethod> retVal = new ArrayList<AltMethod>(orderedSet);
        return retVal;
    }
    
    private static boolean isSpecifiedCustom(AltMethod method) {
        AltAnnotation customAnnotation = method.getAnnotation(Customize.class.getName());
        return (customAnnotation != null);
    }
    
    public static String isSetter(AltMethod method) {
        String name = method.getName();
        
        if (name.startsWith(JAUtilities.SET)) {
            if (name.length() <= JAUtilities.SET.length()) return null;
            if (method.getParameterTypes().size() != 1) return null;
            if (void.class.getName().equals(method.getReturnType().getName())) {
                String variableName = name.substring(JAUtilities.SET.length());
                
                return Introspector.decapitalize(variableName);
            }
            
            return null;
        }
        
        return null;
    }
    
    public static String isGetter(AltMethod method) {
        String name = method.getName();
        
        if (name.startsWith(JAUtilities.GET)) {
            if (name.length() <= JAUtilities.GET.length()) return null;
            if (method.getParameterTypes().size() != 0) return null;
            if (void.class.getName().equals(method.getReturnType().getName())) return null;
            
            String variableName = name.substring(JAUtilities.GET.length());
            
            return Introspector.decapitalize(variableName);
        }
        
        if (name.startsWith(JAUtilities.IS)) {
            if (name.length() <= JAUtilities.IS.length()) return null;
            if (method.getParameterTypes().size() != 0) return null;
            if (boolean.class.getName().equals(method.getReturnType().getName()) || Boolean.class.getName().equals(method.getReturnType().getName())) {
                String variableName = name.substring(JAUtilities.IS.length());
                
                return Introspector.decapitalize(variableName);
            }
            
            return null;
        }
        
        return null;
    }
    
    private static String isLookup(AltMethod method, NameInformation nameInformation) {
        String name = method.getName();
        
        String retVal = nameInformation.getLookupVariableName(name);
        if (retVal == null) return null;
        
        List<AltClass> parameterTypes = method.getParameterTypes();
        if (parameterTypes.size() != 1) return null;
        if (!String.class.getName().equals(parameterTypes.get(0).getName())) return null;
            
        if (method.getReturnType() == null || void.class.getName().equals(method.getReturnType().getName())) return null;
        
        return retVal;
    }
    
    private static String isAdd(AltMethod method, NameInformation nameInformation) {
        String name = method.getName();
        
        String retVal = nameInformation.getAddVariableName(name);
        if (retVal == null) return null;
        
        if (!void.class.getName().equals(method.getReturnType().getName()) &&
                !method.getReturnType().isInterface()) return null;
        
        List<AltClass> parameterTypes = method.getParameterTypes();
        if (parameterTypes.size() > 2) return null;
        
        if (parameterTypes.size() == 0) return retVal;
        
        AltClass param0 = parameterTypes.get(0);
        AltClass param1 = null;
        if (parameterTypes.size() == 2) {
            param1 = parameterTypes.get(1);
        }
        
        if (String.class.getName().equals(param0.getName()) ||
                int.class.getName().equals(param0.getName()) ||
                param0.isInterface()) {
            // Yes, this is possibly an add
            if (parameterTypes.size() == 1) {
                // add(int), add(String), add(interface) are legal adds
                return retVal;
            }
            
            if (int.class.getName().equals(param0.getName())) {
                // If int is first there must not be any other parameter
                return null;
            }
            else if (String.class.getName().equals(param0.getName())) {
                // add(String, int) is a legal add
                if (int.class.getName().equals(param1.getName())) return retVal;
            }
            else {
                // add(interface, int) is a legal add
                if (int.class.getName().equals(param1.getName())) return retVal;
            }
        }
        return null;
    }
    
    private static String isRemove(AltMethod method, NameInformation nameInformation) {
        String name = method.getName();
        
        String retVal = nameInformation.getRemoveVariableName(name);
        if (retVal == null) return null;
        
        AltClass returnType = method.getReturnType();
        if (returnType == null) returnType = ClassAltClassImpl.VOID;
        
        if (!boolean.class.getName().equals(returnType.getName()) &&
                !returnType.isInterface() &&
                !void.class.getName().equals(returnType.getName())) return null;
        
        List<AltClass> parameterTypes = method.getParameterTypes();
        if (parameterTypes.size() > 1) return null;
        
        if (parameterTypes.size() == 0) return retVal;
        
        AltClass param0 = parameterTypes.get(0);
        
        if (String.class.getName().equals(param0.getName()) ||
                int.class.getName().equals(param0.getName())||
                param0.isInterface()) return retVal;
        
        return null;
    }
    
    public static MethodInformationI getMethodInformation(AltMethod m, NameInformation xmlNameMap) {
        if (m.getMethodInformation() != null) {
            return m.getMethodInformation();
        }
        
        boolean isCustom = isSpecifiedCustom(m);
        String setterVariable = null;
        String getterVariable = null;
        String lookupVariable = null;
        String addVariable = null;
        String removeVariable = null;
        
        if (!isCustom) {
            setterVariable = isSetter(m);
            if (setterVariable == null) {
                getterVariable = isGetter(m);
                if (getterVariable == null) {
                    lookupVariable = isLookup(m, xmlNameMap);
                    if (lookupVariable == null) {
                        addVariable = isAdd(m, xmlNameMap);
                        if (addVariable == null) {
                            removeVariable = isRemove(m, xmlNameMap);
                        }
                    }
                }
            }
        }
        
        MethodType methodType;
        AltClass baseChildType = null;
        AltClass gsType = null;
        String variable = null;
        boolean isList = false;
        boolean isArray = false;
        if (getterVariable != null) {
            // This is a getter
            methodType = MethodType.GETTER;
            variable = getterVariable;
            
            AltClass returnType = m.getReturnType();
            gsType = returnType;
            
            if (List.class.getName().equals(returnType.getName())) {
                isList = true;
                AltClass typeChildType = m.getFirstTypeArgument();
                
                baseChildType = typeChildType;
                if (baseChildType == null) {
                    throw new RuntimeException("Cannot find child type of method " + m);
                }
            }
            else if (returnType.isArray()) {
                AltClass arrayType = returnType.getComponentType();
                if (arrayType.isInterface()) {
                    isArray = true;
                    baseChildType = arrayType;
                }
            }
            else if (returnType.isInterface() && !returnType.getName().startsWith(Generator.NO_CHILD_PACKAGE)) {
                baseChildType = returnType;
            }
        }
        else if (setterVariable != null) {
            // This is a setter
            methodType = MethodType.SETTER;
            variable = setterVariable;
            
            AltClass setterType = m.getParameterTypes().get(0);
            gsType = setterType;
            
            if (List.class.getName().equals(setterType.getName())) {
                isList = true;
                AltClass typeChildType = m.getFirstTypeArgumentOfParameter(0);
                
                baseChildType = typeChildType;
                if (baseChildType == null) {
                    throw new RuntimeException("Cannot find child type of method " + m);
                }
            }
            else if (setterType.isArray()) {
                AltClass arrayType = setterType.getComponentType();
                if (arrayType.isInterface()) {
                    isArray = true;
                    baseChildType = arrayType;
                }
            }
            else if (setterType.isInterface() && !setterType.getName().startsWith(Generator.NO_CHILD_PACKAGE)) {
                baseChildType = setterType;
            }
        }
        else if (lookupVariable != null) {
            // This is a lookup
            methodType = MethodType.LOOKUP;
            variable = lookupVariable;
            
            AltClass lookupType = m.getReturnType();
            gsType = lookupType;
        }
        else if (addVariable != null) {
            // This is an add
            methodType = MethodType.ADD;
            variable = addVariable;
        }
        else if (removeVariable != null) {
            // This is an remove
            methodType = MethodType.REMOVE;
            variable = removeVariable;
        }
        else {
            methodType = MethodType.CUSTOM;
        }
        
        String representedProperty = xmlNameMap.getNameMap(variable);
        if (representedProperty == null) representedProperty = variable;
        
        String defaultValue = xmlNameMap.getDefaultNameMap(variable);
        
        boolean key = false;
        if ((m.getAnnotation(XmlID.class.getName()) != null) || (m.getAnnotation(XmlIdentifier.class.getName()) != null)) {
            key = true;
        }
        
        boolean isReference = xmlNameMap.isReference(variable);
        boolean isElement = xmlNameMap.isElement(variable);
        
        return new MethodInformation(m,
                methodType,
                variable,
                representedProperty,
                defaultValue,
                baseChildType,
                gsType,
                key,
                isList,
                isArray,
                isReference,
                isElement);
    }
    
    private static MethodInformationI getAndSetMethodInformation(AltMethod am, NameInformation xmlMap) {
        MethodInformationI retVal = am.getMethodInformation();
        if (retVal != null) return retVal;
        
        retVal = getMethodInformation(am, xmlMap);
        am.setMethodInformation(retVal);
        
        return retVal;
    }
    
    /**
     * This comparator is not 100% state free, since it will set the MethodInformation
     * if it needs on the AltMethod
     * 
     * @author jwells
     *
     */
    private final static class SpecifiedOrderComparator implements Comparator<AltMethod> {
        private final Map<String, Integer> specifiedOrder;
        private final Map<AltMethod, Integer> secondarySort;
        private final NameInformation xmlMap;
        
        private SpecifiedOrderComparator(Map<String, Integer> specifiedOrder, Map<AltMethod, Integer> secondarySort, NameInformation xmlMap) {
            this.specifiedOrder = specifiedOrder;
            this.secondarySort = secondarySort;
            this.xmlMap = xmlMap;
        }
        
        private int secondarySort(AltMethod o1, AltMethod o2) {
            Integer p1 = secondarySort.get(o1);
            Integer p2 = secondarySort.get(o2);
            
            int pr1 = p1;
            int pr2 = p2;
            
            return pr2 - pr1;
        }

        @Override
        public int compare(AltMethod o1, AltMethod o2) {
            if (o1.equals(o2)) return 0;
            
            MethodInformationI methodInfo1 = getAndSetMethodInformation(o1, xmlMap);
            MethodInformationI methodInfo2 = getAndSetMethodInformation(o2, xmlMap);
            
            String prop1 = methodInfo1.getDecapitalizedMethodProperty();
            String prop2 = methodInfo2.getDecapitalizedMethodProperty();
            
            if (prop1 == null && prop2 == null) {
                return secondarySort(o1, o2);
            }
            if (prop1 != null && prop2 == null) {
                return -1;
            }
            if (prop1 == null && prop2 != null) {
                return 1;
            }
            
            // Both properties are not null
            
            Integer priority1 = specifiedOrder.get(prop1);
            Integer priority2 = specifiedOrder.get(prop2);
            
            if (priority1 != null && priority2 == null) {
                return -1;
            }
            if (priority1 == null && priority2 != null) {
                return 1;
            }
            if (priority1 != null && priority2 != null) {
                int p1 = priority1;
                int p2 = priority2;
                
                if (p1 < p2) {
                    return -1;
                }
                
                if (p1 > p2) {
                    return 1;
                }
                
                // Fall through, use other criteria
            }
            
            
            return secondarySort(o1, o2);
        }
    }
}
