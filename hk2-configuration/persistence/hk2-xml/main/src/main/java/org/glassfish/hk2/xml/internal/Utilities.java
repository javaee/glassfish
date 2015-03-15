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
package org.glassfish.hk2.xml.internal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.configuration.hub.api.WriteableType;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
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
            config.addActiveDescriptor(cDesc);
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
        
        ParentedNode childNode = myParent.__getModel().getChild(childProperty);
        if (childNode == null) {
            throw new IllegalArgumentException("There is no child with xmlTag " + childProperty + " of " + myParent);
        }
        
        Object allMyChildren = myParent.__getBeanLikeMap().get(childProperty);
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
            for (String nonChildProperty : childToCopy.__getModel().getNonChildProperties()) {
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
            if (childNode.getChild().getKeyProperty() == null) {
                throw new IllegalArgumentException("Attempted to add an unkeyed child with key " + childKey + " in " + myParent);
            }
                
            child._setProperty(childNode.getChild().getKeyProperty(), childKey);
            child._setKeyValue(childKey);
        }
            
        child._setModel(childNode.getChild(), myParent._getClassReflectionHelper());
        child._setParent(myParent);
        child._setSelfXmlTag(childNode.getChildName());
        child._setKeyValue(childKey);
        if (childNode.isMultiChildList() || childNode.isMultiChildArray()) {
            child._setInstanceName(myParent._getInstanceName() + Utilities.INSTANCE_PATH_SEPARATOR + child._getKeyValue());
        }
        else {
            child._setInstanceName(myParent._getInstanceName() + Utilities.INSTANCE_PATH_SEPARATOR + childNode.getChildName());
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
            
            myParent.__getBeanLikeMap().put(childProperty, finalChildList);
            
            if (!myParent.__getModel().getUnKeyedChildren().contains(childProperty)) {
                Map<String, BaseHK2JAXBBean> byKeyMap = myParent.__getChildren().get(childProperty);
                if (byKeyMap == null) {
                    byKeyMap = new HashMap<String, BaseHK2JAXBBean>();
                    myParent.__getChildren().put(childProperty, byKeyMap);
                }
                
                byKeyMap.put(child._getKeyValue(), child);
            }
        }
        else {
            if (writeableDatabase != null){
                myParent.changeInHub(childProperty, child, writeableDatabase);
            }
            
            myParent.__getBeanLikeMap().put(childProperty, child);
        }
        
        return child;
    }
    
    @SuppressWarnings("unchecked")
    private static void handleChildren(BaseHK2JAXBBean child, BaseHK2JAXBBean childToCopy, DynamicChangeInfo changeInformation) {
        Map<String, ParentedNode> childrenMap = childToCopy.__getModel().getChildrenProperties();
        
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
        
        for (Map<String, BaseHK2JAXBBean> specificChildren : root.__getChildren().values()) {
            for (BaseHK2JAXBBean child : specificChildren.values()) {
                externalAdd(child, config, writeableDatabase);
            }
        }
        
        for (String unkeyedChildProperty : root.__getModel().getUnKeyedChildren()) {
            Object unkeyedRawChild = root.__getBeanLikeMap().get(unkeyedChildProperty);
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
        for (String nonChildProperty : childToCopy.__getModel().getNonChildProperties()) {
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
    public static BaseHK2JAXBBean internalRemove(
            BaseHK2JAXBBean myParent,
            String childProperty,
            String childKey,
            int index,
            DynamicChangeInfo changeInformation,
            WriteableBeanDatabase writeableDatabase,
            DynamicConfiguration dynamicService) {
        throw new AssertionError("Remove not yet implemented");
    }
    
}
