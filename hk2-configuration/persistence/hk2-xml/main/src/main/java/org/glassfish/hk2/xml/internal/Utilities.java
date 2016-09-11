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

package org.glassfish.hk2.xml.internal;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.configuration.hub.api.Instance;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.internal.alt.AltClass;
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
    
    public static void advertise(WriteableBeanDatabase wbd, DynamicConfiguration config, BaseHK2JAXBBean bean) {
        if (config != null) {
            AbstractActiveDescriptor<?> cDesc = BuilderHelper.createConstantDescriptor(bean);
            if (bean._getKeyValue() != null) {
                cDesc.setName(bean._getKeyValue());
            }
            ActiveDescriptor<?> selfDescriptor = config.addActiveDescriptor(cDesc);
            
            bean._setSelfDescriptor(selfDescriptor);
        }
        
        if (wbd != null) {
            WriteableType wt = wbd.findOrAddWriteableType(bean._getXmlPath());
            wt.addInstance(bean._getInstanceName(), bean._getBeanLikeMap());
        }
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
                
                child._setProperty(nonChildProperty, value, false);
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
                
            child._setProperty(childNode.getChildModel().getKeyProperty(), childKey, false);
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
            
            if (writeableDatabase != null) {
                myParent.changeInHub(childProperty, finalChildList, writeableDatabase);
            }
            
            myParent._setProperty(childProperty, finalChildList, false);
        }
        else {
            if (writeableDatabase != null){
                myParent.changeInHub(childProperty, child, writeableDatabase);
            }
            
            myParent._setProperty(childProperty, child, false);
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
    private static void handleChildren(BaseHK2JAXBBean child, BaseHK2JAXBBean childToCopy, DynamicChangeInfo changeInformation) {
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
                            childsChild, null, -1, changeInformation, null, null);
                    
                    copiedChildArray.add(grandchild);
                    Array.set(asArray, lcv++, grandchild);
                }
                
                if (ChildType.LIST.equals(childsChildParentNode.getChildType())) {
                    child._setProperty(childsChildProperty, copiedChildArray, false);
                }
                else {
                    child._setProperty(childsChildProperty, asArray, false);
                }
            }
            else {
                BaseHK2JAXBBean childsChild = (BaseHK2JAXBBean) childToCopy._getProperty(childsChildProperty);
                if (childsChild == null) continue;
                
                BaseHK2JAXBBean grandchild = internalAdd(child, childsChildProperty,
                        childsChild, null, -1, changeInformation, null, null);
                
                child._setProperty(childsChildProperty, grandchild, false);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void externalAdd(BaseHK2JAXBBean root, DynamicConfiguration config, WriteableBeanDatabase writeableDatabase) {
        if (config == null && writeableDatabase == null) return;
        
        Utilities.advertise(writeableDatabase, config, root);
        
        for (String keyedChildProperty : root._getModel().getKeyedChildren()) {
            Object keyedRawChild = root._getProperty(keyedChildProperty);
            if (keyedRawChild == null) continue;
            
            if (keyedRawChild instanceof Iterable) {
                Iterable<BaseHK2JAXBBean> iterable = (Iterable<BaseHK2JAXBBean>) keyedRawChild;
                for (BaseHK2JAXBBean child : iterable) {
                    externalAdd(child, config, writeableDatabase);
                }
            }
            else if (keyedRawChild.getClass().isArray()) {
                int aLength = Array.getLength(keyedRawChild);
                for (int lcv = 0; lcv < aLength; lcv++) {
                    BaseHK2JAXBBean child = (BaseHK2JAXBBean) Array.get(keyedRawChild, lcv);
                    externalAdd(child, config, writeableDatabase);
                }
                
            }
            else {
                externalAdd((BaseHK2JAXBBean) keyedRawChild, config, writeableDatabase);
            }
        }
        
        for (String unkeyedChildProperty : root._getModel().getUnKeyedChildren()) {
            Object unkeyedRawChild = root._getProperty(unkeyedChildProperty);
            if (unkeyedRawChild == null) continue;
            
            if (unkeyedRawChild instanceof Iterable) {
                Iterable<BaseHK2JAXBBean> unkeyedMultiChildren = (Iterable<BaseHK2JAXBBean>) unkeyedRawChild;
                for (BaseHK2JAXBBean child : unkeyedMultiChildren) {
                    externalAdd(child, config, writeableDatabase);
                }
            }
            else if (unkeyedRawChild.getClass().isArray()) {
                int aLength = Array.getLength(unkeyedRawChild);
                for (int lcv = 0; lcv < aLength; lcv++) {
                    BaseHK2JAXBBean child = (BaseHK2JAXBBean) Array.get(unkeyedRawChild, lcv);
                    externalAdd(child, config, writeableDatabase);
                }
            }
            else {
                externalAdd((BaseHK2JAXBBean) unkeyedRawChild, config, writeableDatabase);
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
            DynamicChangeInfo changeInfo,
            ClassReflectionHelper helper,
            WriteableBeanDatabase writeableDatabase,
            DynamicConfiguration dynamicService) {
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
        
        handleChildren(child, childToCopy, changeInfo);
            
        // Now freeze it
        child._setDynamicChangeInfo(changeInfo);
        
        externalAdd(child, dynamicService, writeableDatabase);
        
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
            DynamicChangeInfo changeInformation,
            WriteableBeanDatabase writeableDatabase,
            DynamicConfiguration dynamicService) {
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
                
                if (writeableDatabase != null) {
                    myParent.changeInHub(childProperty, listWithObjectRemoved, writeableDatabase);
                }
                
                myParent._setProperty(childProperty, listWithObjectRemoved, false);
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
                
                if (writeableDatabase != null) {
                    myParent.changeInHub(childProperty, arrayWithObjectRemoved, writeableDatabase);
                }
                
                myParent._setProperty(childProperty, arrayWithObjectRemoved, false);
            }
        }
        else {
            rootForDeletion = (BaseHK2JAXBBean) myParent._getProperty(childProperty);
            if (rootForDeletion == null) return null;
            
            if (writeableDatabase != null) {
                myParent.changeInHub(childProperty, null, writeableDatabase);
            }
            
            myParent._setProperty(childProperty, null, false);
        }
        
        if (dynamicService != null) {
            HashSet<ActiveDescriptor<?>> descriptorsToRemove = new HashSet<ActiveDescriptor<?>>();
            
            getDescriptorsToRemove(rootForDeletion, descriptorsToRemove);
            
            for (ActiveDescriptor<?> descriptorToRemove : descriptorsToRemove) {
                dynamicService.addUnbindFilter(BuilderHelper.createSpecificDescriptorFilter(descriptorToRemove));
            }
        }
        
        if (writeableDatabase != null) {
            String rootXmlPath = rootForDeletion._getXmlPath();
            String rootInstanceName = rootForDeletion._getInstanceName();
            
            WriteableType rootType = writeableDatabase.getWriteableType(rootXmlPath);
            if (rootType != null) {
                rootType.removeInstance(rootInstanceName);
            
                String typeRemovalIndicator = rootXmlPath + BaseHK2JAXBBean.XML_PATH_SEPARATOR;
            
                Set<WriteableType> allTypes = writeableDatabase.getAllWriteableTypes();
                for (WriteableType allType : allTypes) {
                    if (allType.getName().startsWith(typeRemovalIndicator)) {
                        
                        Map<String, Instance> allInstances = allType.getInstances();
                        
                        Set<String> removeMe = new LinkedHashSet<String>();
                        for (String iKey : allInstances.keySet()) {
                            if (!iKey.startsWith(rootInstanceName)) continue;
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
}
