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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

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
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;
import org.glassfish.hk2.xml.internal.Differences.AddData;
import org.glassfish.hk2.xml.internal.Differences.AddRemoveData;
import org.glassfish.hk2.xml.internal.Differences.AddRemoveMoveDifference;
import org.glassfish.hk2.xml.internal.Differences.Difference;
import org.glassfish.hk2.xml.internal.Differences.MoveData;
import org.glassfish.hk2.xml.internal.Differences.RemoveData;
import org.glassfish.hk2.xml.internal.alt.AdapterInformation;
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
    private final static char XML_PATH_SEPARATOR = '/';
    
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
        String selfXmlTag = bean._getSelfXmlTag();
        String baseKeySegment = bean._getKeyValue();
        
        if (baseKeySegment == null) {
            baseKeySegment = selfXmlTag;
            if (baseKeySegment != null) {
              baseKeySegment = baseKeySegment.replace(XML_PATH_SEPARATOR, INSTANCE_PATH_SEPARATOR);
            }
        }
        else {
            String xmlWrapperTag = null;
            if (selfXmlTag != null) {
                int pathSep = selfXmlTag.indexOf(XML_PATH_SEPARATOR);
                if (pathSep > 0) {
                    xmlWrapperTag = selfXmlTag.substring(0, pathSep);
                }
            }
            
            if (xmlWrapperTag != null) {
                baseKeySegment = xmlWrapperTag + INSTANCE_PATH_SEPARATOR + baseKeySegment;
            }
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
    public static void internalModifyChild(
            BaseHK2JAXBBean myParent,
            String childPropertyNamespace,
            String childProperty,
            Object currentValue,
            Object newValue,
            XmlRootHandleImpl<?> root,
            DynamicChangeInfo<?> changeInformation,
            XmlDynamicChange xmlDynamicChange
            ) {
        ParentedModel childNode = myParent._getModel().getChild(childPropertyNamespace, childProperty);
        if (childNode == null) {
            throw new IllegalArgumentException("There is no child with xmlTag " + childProperty + " of " + myParent);
        }
        
        Differences differences = new Differences();
        String xmlTag = childNode.getChildXmlTag();
        
        if (ChildType.ARRAY.equals(childNode.getChildType())) {
            int newLength = Array.getLength(newValue);
            Object newArrayWithCopies = Array.newInstance(childNode.getChildModel().getOriginalInterfaceAsClass(), newLength);
                    
            for (int lcv = 0; lcv < newLength; lcv++) {
                BaseHK2JAXBBean aBean = (BaseHK2JAXBBean) Array.get(newValue, lcv);
                if (aBean == null) {
                    throw new IllegalArgumentException("The new array may not have null elements, the element at index " + lcv + " is null");
                }
                
                XmlRootHandle<?> aRoot = aBean._getRoot();
                if (aRoot != null) {
                    if (!aRoot.equals(root)) {
                        throw new IllegalArgumentException("Can not have a bean from a different tree added with set method.  The element at index "
                                + lcv + " is from tree " + aRoot);
                    }
                    
                    aBean = createUnrootedBeanTreeCopy(aBean);
                }
                
                Array.set(newArrayWithCopies, lcv, aBean);
            }
            
            getArrayDifferences(childNode,
                    currentValue, newArrayWithCopies,
                    differences,
                    xmlTag, myParent);
        }
        else if (ChildType.LIST.equals(childNode.getChildType())) {
            List<BaseHK2JAXBBean> newValueAsList = (List<BaseHK2JAXBBean>) newValue;
            
            List<BaseHK2JAXBBean> newListWithCopies = new ArrayList<BaseHK2JAXBBean>(newValueAsList.size());
            for (BaseHK2JAXBBean aBean : newValueAsList) {
                if (aBean == null) {
                    throw new IllegalArgumentException("The new list may not have null elements");
                }
                
                XmlRootHandle<?> aRoot = aBean._getRoot();
                if (aRoot != null) {
                    if (!aRoot.equals(root)) {
                        throw new IllegalArgumentException("Can not have a bean from a different tree added with set method");
                    }
                    
                    aBean = createUnrootedBeanTreeCopy(aBean);
                }
                
                newListWithCopies.add(aBean);
            }
            
            getListDifferences(childNode,
                    currentValue, newListWithCopies,
                    differences,
                    xmlTag, myParent);
            
        }
        else if (ChildType.DIRECT.equals(childNode.getChildType())) {
            BaseHK2JAXBBean aBean = (BaseHK2JAXBBean) newValue;
            
            XmlRootHandle<?> aRoot = aBean._getRoot();
            if (aRoot != null) {
                if (!aRoot.equals(root)) {
                    throw new IllegalArgumentException("Can not have a bean from a different tree added with set method (direct child)");
                }
                
                aBean = createUnrootedBeanTreeCopy(aBean);
            }
            
            getAllDifferences((BaseHK2JAXBBean) currentValue, aBean, differences);
        }
        else {
            throw new AssertionError("Unknown child type: " + childNode.getChildType());
        }
        
        if (!differences.getDifferences().isEmpty()) {
            Utilities.applyDiff(differences, changeInformation);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public static BaseHK2JAXBBean internalAdd(
            BaseHK2JAXBBean myParent,
            String childPropertyNamespace,
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
        
        if (childKey != null && myParent._lookupChild(childPropertyNamespace, childProperty, childKey) != null) {
            throw new IllegalStateException("There is already a child with name " + childKey + " for child " + childProperty);
        }
        
        if (rawChild != null && !(rawChild instanceof BaseHK2JAXBBean)) {
            throw new IllegalArgumentException("The child added must be from XmlService.createBean");
        }
        
        ParentedModel childNode = myParent._getModel().getChild(childPropertyNamespace, childProperty);
        if (childNode == null) {
            throw new IllegalArgumentException("There is no child with xmlTag " + childProperty + " of " + myParent);
        }
        
        Object allMyChildren = myParent._getProperty(childPropertyNamespace, childProperty);
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
            
            if (changeList && index > multiChildren.size()) {
                throw new IllegalArgumentException(
                        "The index given to add child " + childProperty + " to " + myParent + " is not in range (" +
                index + "," + multiChildren.size() + ")");
            }
            
            if (index == -1) {
                index = multiChildren.size();
            }
        }
        
        BaseHK2JAXBBean child = createBean(childNode.getChildModel().getProxyAsClass());
        child._setClassReflectionHelper(myParent._getClassReflectionHelper());
        
        if (rawChild != null) {
            // Handling of children will be handled once the real child is better setup
            BaseHK2JAXBBean childToCopy = (BaseHK2JAXBBean) rawChild;
            for (QName nonChildProperty : childToCopy._getModel().getNonChildProperties().keySet()) {
                String nonChildPropNamespace = QNameUtilities.getNamespace(nonChildProperty);
                String nonChildPropKey = nonChildProperty.getLocalPart();
                
                Object value = childToCopy._getProperty(nonChildPropNamespace, nonChildPropKey);
                if (value == null) continue;
                
                child._setProperty(nonChildPropNamespace, nonChildPropKey, value, false, true);
            }
        }
        
        if (childKey == null) {
            if (childNode.getChildModel().getKeyProperty() != null) {
                if (rawChild != null) {
                    String keyPropNamespace = QNameUtilities.getNamespace(childNode.getChildModel().getKeyProperty());
                    String keyPropKey = childNode.getChildModel().getKeyProperty().getLocalPart();
                    
                    childKey = (String) child._getProperty(keyPropNamespace, keyPropKey);
                }
                    
                if (childKey == null) {
                    throw new IllegalArgumentException("Attempted to create child with xmlTag " + childProperty +
                        " with no key field in " + myParent);
                }
                    
                child._setKeyValue(childKey);
            }
            else if (!ChildType.DIRECT.equals(childNode.getChildType())) {
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
        else { /* childKey != null */
            if (childNode.getChildModel().getKeyProperty() == null) {
                throw new IllegalArgumentException("Attempted to add an unkeyed child with key " + childKey + " in " + myParent);
            }
            
            QName keyProp = childNode.getChildModel().getKeyProperty();
                
            child._setProperty(QNameUtilities.getNamespace(keyProp), keyProp.getLocalPart(), childKey, false, true);
            child._setKeyValue(childKey);
        }
        
        child._setParent(myParent);
        child._setSelfXmlTag(childNode.getChildXmlNamespace(), constructXmlTag(childNode.getXmlWrapperTag(), childNode.getChildXmlTag()));
        child._setKeyValue(childKey);
        if (childKey != null) {
            child._setInstanceName(myParent._getInstanceNamespace(),
                    composeInstanceName(myParent._getInstanceName(), child._getKeyValue(), childNode.getXmlWrapperTag()));
        }
        else {
            child._setInstanceName(myParent._getInstanceNamespace(),
                    composeInstanceName(myParent._getInstanceName(), childNode.getChildXmlTag(), childNode.getXmlWrapperTag()));
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
                myParent._changeInHub(childPropertyNamespace, childProperty, finalChildList, xmlDynamicChange.getBeanDatabase());
            }
            
            myParent._setProperty(childPropertyNamespace, childProperty, finalChildList, false, true);
        }
        else {
            // Direct child
            if (xmlDynamicChange.getBeanDatabase() != null){
                myParent._changeInHub(childPropertyNamespace, childProperty, child, xmlDynamicChange.getBeanDatabase());
            }
            
            myParent._setProperty(childPropertyNamespace, childProperty, child, false, true);
        }
        
        return child;
    }
    
    private static String composeInstanceName(String parentName, String mySegment, String xmlWrapperTag) {
        if (xmlWrapperTag == null) {
            return parentName + INSTANCE_PATH_SEPARATOR + mySegment;
        }
        
        return parentName + INSTANCE_PATH_SEPARATOR + xmlWrapperTag +  INSTANCE_PATH_SEPARATOR + mySegment;
        
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
        Map<QName, ParentedModel> childrenMap = childToCopy._getModel().getChildrenProperties();
        
        for (Map.Entry<QName, ParentedModel> childsChildrenEntry : childrenMap.entrySet()) {
            QName childsChildProperty = childsChildrenEntry.getKey();
            ParentedModel childsChildParentNode = childsChildrenEntry.getValue();
            
            String childsChildPropertyNamespace = QNameUtilities.getNamespace(childsChildProperty);
            String childsChildPropertyKey = childsChildProperty.getLocalPart();
            
            if (!ChildType.DIRECT.equals(childsChildParentNode.getChildType())) {
                List<BaseHK2JAXBBean> childsChildren = null;
                if (ChildType.LIST.equals(childsChildParentNode.getChildType())) {
                    childsChildren = (List<BaseHK2JAXBBean>) childToCopy._getProperty(childsChildPropertyNamespace, childsChildPropertyKey);
                }
                else {
                    Object arrayChildsChildren = childToCopy._getProperty(childsChildPropertyNamespace, childsChildPropertyKey);
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
                    BaseHK2JAXBBean grandchild = internalAdd(child, childsChildPropertyNamespace, childsChildPropertyKey,
                            childsChild, null, -1, changeInformation, xmlDynamicChange, addedServices, false);
                    
                    copiedChildArray.add(grandchild);
                    Array.set(asArray, lcv++, grandchild);
                }
                
                if (ChildType.LIST.equals(childsChildParentNode.getChildType())) {
                    child._setProperty(childsChildPropertyNamespace, childsChildPropertyKey, copiedChildArray, false, true);
                }
                else {
                    child._setProperty(childsChildPropertyNamespace, childsChildPropertyKey, asArray, false, true);
                }
            }
            else {
                BaseHK2JAXBBean childsChild = (BaseHK2JAXBBean) childToCopy._getProperty(childsChildPropertyNamespace, childsChildPropertyKey);
                if (childsChild == null) continue;
                
                BaseHK2JAXBBean grandchild = internalAdd(child, childsChildPropertyNamespace, childsChildPropertyKey,
                        childsChild, null, -1, changeInformation, xmlDynamicChange, addedServices, false);
                
                child._setProperty(childsChildPropertyNamespace, childsChildPropertyKey, grandchild, false, true);
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
        
        for (QName keyedChildProperty : root._getModel().getKeyedChildren()) {
            String keyedChildPropertyNamespace = QNameUtilities.getNamespace(keyedChildProperty);
            String keyedChildPropertyKey = keyedChildProperty.getLocalPart();
            
            Object keyedRawChild = root._getProperty(keyedChildPropertyNamespace, keyedChildPropertyKey);
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
        
        for (QName unkeyedChildProperty : root._getModel().getUnKeyedChildren()) {
            String unkeyedChildPropertyNamespace = QNameUtilities.getNamespace(unkeyedChildProperty);
            String unkeyedChildPropertyKey = unkeyedChildProperty.getLocalPart();
            
            Object unkeyedRawChild = root._getProperty(unkeyedChildPropertyNamespace, unkeyedChildPropertyKey);
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
        for (QName nonChildProperty : childToCopy._getModel().getNonChildProperties().keySet()) {
            String nonChildPropertyNamespace = QNameUtilities.getNamespace(nonChildProperty);
            String nonChildPropertyKey = nonChildProperty.getLocalPart();
            
            Object value = childToCopy._getProperty(nonChildPropertyNamespace, nonChildPropertyKey);
            if (value == null) continue;
            
            child._setProperty(nonChildPropertyNamespace, nonChildPropertyKey, value, false);
        }
        
        if (rootNode.getKeyProperty() != null) {
            QName rootKeyProperty = rootNode.getKeyProperty();
            
            child._setKeyValue((String) child._getProperty(QNameUtilities.getNamespace(rootKeyProperty), rootKeyProperty.getLocalPart())); 
        }
        
        QName rName = rootNode.getRootName();
        String rNameNamespace = QNameUtilities.getNamespace(rName);
        String rNameKey = rName.getLocalPart();
        
        child._setSelfXmlTag(rNameNamespace, rNameKey);
        child._setInstanceName(QNameUtilities.getNamespace(rootNode.getRootName()), rootNode.getRootName().getLocalPart());
        
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
            String childPropertyNamespace,
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
        
        ParentedModel removeMeParentedNode = myParent._getModel().getChild(childPropertyNamespace, childProperty);
        ModelImpl removeMeNode = removeMeParentedNode.getChildModel();
        BaseHK2JAXBBean rootForDeletion = null;
        
        if (!ChildType.DIRECT.equals(removeMeParentedNode.getChildType())) {
            if (childKey == null && index < 0 && instanceToRemove == null) return null;
            
            if (ChildType.LIST.equals(removeMeParentedNode.getChildType())) {
                List<BaseHK2JAXBBean> removeFromList = (List<BaseHK2JAXBBean>) myParent._getProperty(childPropertyNamespace, childProperty);
                
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
                        myParent._changeInHub(childPropertyNamespace, childProperty, listWithObjectRemoved, xmlDynamicChange.getBeanDatabase());
                    }
                
                    myParent._setProperty(childPropertyNamespace, childProperty, listWithObjectRemoved, false, true);
                }
            }
            else {
                // array children
                Object removeFromArray = myParent._getProperty(childPropertyNamespace, childProperty);
                
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
                        myParent._changeInHub(childPropertyNamespace, childProperty, arrayWithObjectRemoved, xmlDynamicChange.getBeanDatabase());
                    }
                
                    myParent._setProperty(childPropertyNamespace, childProperty, arrayWithObjectRemoved, false, true);
                }
            }
        }
        else {
            // Direct child
            rootForDeletion = (BaseHK2JAXBBean) myParent._getProperty(childPropertyNamespace, childProperty);
            if (rootForDeletion == null) return null;
            
            if (changeList) {
                if (xmlDynamicChange.getBeanDatabase() != null) {
                    myParent._changeInHub(childPropertyNamespace, childProperty, null, xmlDynamicChange.getBeanDatabase());
                }
            
                myParent._setProperty(childPropertyNamespace, childProperty, null, false, true);
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
            String childPropertyNamespace = parentedChild.getChildXmlNamespace();
            String childPropertyName = parentedChild.getChildXmlTag();
            
            switch (parentedChild.getChildType()) {
            case LIST:
                List<BaseHK2JAXBBean> listChildren = (List<BaseHK2JAXBBean>) fromMe._getProperty(childPropertyNamespace, childPropertyName);
                if (listChildren != null) {
                    for (BaseHK2JAXBBean listChild : listChildren) {
                        getDescriptorsToRemove(listChild, descriptorsToRemove);
                    }
                }
                break;
            case ARRAY:
                Object arrayChildren = fromMe._getProperty(childPropertyNamespace, childPropertyName);
                if (arrayChildren != null) {
                    int arrayLength = Array.getLength(arrayChildren);
                    
                    for (int lcv = 0; lcv < arrayLength; lcv++) {
                        BaseHK2JAXBBean bean = (BaseHK2JAXBBean) Array.get(arrayChildren, lcv);
                        getDescriptorsToRemove(bean, descriptorsToRemove);
                    }
                }
                break;
            case DIRECT:
                BaseHK2JAXBBean bean = (BaseHK2JAXBBean) fromMe._getProperty(childPropertyNamespace, childPropertyName);
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
    
    private final static String ENUM_FROM_VALUE_METHOD_NAME = "fromValue";
    private final static Class<?> ENUM_FROM_VALUE_PARAM_TYPES[] = { String.class };
    
    /**
     * Returns the default value given the string version of the default and
     * the expected result (non-child properties)
     * 
     * @param givenStringDefault
     * @param expectedClass
     * @return
     */
    public static Object getDefaultValue(String givenStringDefault, Class<?> expectedClass, Map<String, String> namespaceMap) {
        if (givenStringDefault == null || JAUtilities.JAXB_DEFAULT_DEFAULT.equals(givenStringDefault)) {
            if (int.class.equals(expectedClass) || Integer.class.equals(expectedClass)) {
                return DEFAULT_INTEGER;
            }
            if (long.class.equals(expectedClass) || Long.class.equals(expectedClass)) {
                return DEFAULT_LONG;
            }
            if (boolean.class.equals(expectedClass) || Boolean.class.equals(expectedClass)) {
                return DEFAULT_BOOLEAN;
            }
            if (short.class.equals(expectedClass) || Short.class.equals(expectedClass)) {
                return DEFAULT_SHORT;
            }
            if (byte.class.equals(expectedClass) || Byte.class.equals(expectedClass)) {
                return DEFAULT_BYTE;
            }
            if (char.class.equals(expectedClass) || Character.class.equals(expectedClass)) {
                return DEFAULT_CHARACTER;
            }
            if (float.class.equals(expectedClass) || Float.class.equals(expectedClass)) {
                return DEFAULT_FLOAT;
            }
            if (double.class.equals(expectedClass) || Double.class.equals(expectedClass)) {
                return DEFAULT_DOUBLE;
            }
            
            return null;
        }
        
        if (String.class.equals(expectedClass)) {
            return givenStringDefault;
        }
        if (int.class.equals(expectedClass) || Integer.class.equals(expectedClass)) {
            return Integer.parseInt(givenStringDefault);
        }
        if (long.class.equals(expectedClass) || Long.class.equals(expectedClass)) {
            return Long.parseLong(givenStringDefault);
        }
        if (boolean.class.equals(expectedClass) || Boolean.class.equals(expectedClass)) {
            return Boolean.parseBoolean(givenStringDefault);
        }
        if (short.class.equals(expectedClass) || Short.class.equals(expectedClass)) {
            return Short.parseShort(givenStringDefault);
        }
        if (byte.class.equals(expectedClass) || Byte.class.equals(expectedClass)) {
            return Byte.parseByte(givenStringDefault);
        }
        if (char.class.equals(expectedClass) || Character.class.equals(expectedClass)) {
            return givenStringDefault.charAt(0);
        }
        if (float.class.equals(expectedClass) || Float.class.equals(expectedClass)) {
            return Float.parseFloat(givenStringDefault);
        }
        if (double.class.equals(expectedClass) || Double.class.equals(expectedClass)) {
            return Double.parseDouble(givenStringDefault);
        }
        if (expectedClass.isArray() && byte.class.equals(expectedClass.getComponentType())) {
            return givenStringDefault.getBytes();
            // return DatatypeConverter.parseHexBinary(givenStringDefault);
        }
        if (expectedClass.isEnum()) {
            try {
                Method m = expectedClass.getMethod(ENUM_FROM_VALUE_METHOD_NAME, ENUM_FROM_VALUE_PARAM_TYPES);
                if (!ReflectionHelper.isStatic(m)) {
                    throw new IllegalArgumentException("Method " + m + " is not static");
                }
                
                Object params[] = new Object[1];
                params[0] = givenStringDefault;
                
                return ReflectionHelper.invoke(null, m, params, true);
            }
            catch (Throwable th) {
                // Ignore, we cannot get this default
                throw new AssertionError("An enum with a default must have a fromValue(String) method to return the value for " +
                  expectedClass.getName() + " and default value " + givenStringDefault, th);
            }
        }
        if (QName.class.equals(expectedClass)) {
            int indexOfColon = givenStringDefault.indexOf(':');
            if (indexOfColon < 0) {
                return new QName(givenStringDefault);
            }
            
            String prefix = givenStringDefault.substring(0, indexOfColon);
            String localPart = givenStringDefault.substring(indexOfColon + 1);
            String namespaceURI = namespaceMap.get(prefix);
            if (namespaceURI == null) return null;
            
            return new QName(namespaceURI, localPart, prefix);
        }
        
        throw new AssertionError("Default for type " + expectedClass.getName() + " not implemented with default " + givenStringDefault);
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
            unfinished._setProperty(unresolvedRef.getPropertyNamespace(), unresolvedRef.getPropertyName(), reference);
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
        
        Map<QName, ParentedModel> childrenByName = model.getChildrenByName();
        for (Map.Entry<QName, ParentedModel> entry : childrenByName.entrySet()) {
            QName propertyName = entry.getKey();
            ParentedModel parentModel = entry.getValue();
            
            String propertyNameNamespace = QNameUtilities.getNamespace(propertyName);
            String propertyNameKey = propertyName.getLocalPart();
            
            Object child = rootBean._getProperty(propertyNameNamespace, propertyNameKey);
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
    
    private static Map<String, Integer> getIndexMap(List<BaseHK2JAXBBean> list, String keyPropertyNamespace, String keyProperty) {
        Map<String, Integer> retVal = new HashMap<String, Integer>();
        for (int lcv = 0; lcv < list.size(); lcv++) {
            BaseHK2JAXBBean bean = list.get(lcv);
            String key = bean._getKeyValue();
            
            if (key == null) {
                key = (String) bean._getProperty(keyPropertyNamespace, keyProperty);
                if (key == null) {
                    throw new AssertionError("Found a keyed bean with no key " + bean + " at index " + lcv + " in " + list);
                }
            }
            
            retVal.put(key, lcv);
        }
        
        return retVal;
    }
    
    private static Map<String, Integer> getIndexMapArray(Object array, String keyPropertyNamespace, String keyProperty) {
        Map<String, Integer> retVal = new HashMap<String, Integer>();
        
        int length = Array.getLength(array);
        for (int lcv = 0; lcv < length; lcv++) {
            BaseHK2JAXBBean bean = (BaseHK2JAXBBean) Array.get(array, lcv);
            String key = bean._getKeyValue();
            
            if (key == null) {
                key = (String) bean._getProperty(keyPropertyNamespace, keyProperty);
                if (key == null) {
                    throw new AssertionError("Found a keyed bean with no key " + bean + " at index " + lcv);
                }
            }
            
            retVal.put(key, lcv);
        }
        
        return retVal;
    }
    
    private static void getAllDifferences(BaseHK2JAXBBean source,
            BaseHK2JAXBBean other,
            Differences differences) {
        Difference localDifference = new Difference(source);
        
        ModelImpl sourceModel = source._getModel();
        
        Map<String, Object> sourceMap = source._getBeanLikeMap();
        Map<String, Object> otherMap = other._getBeanLikeMap();
        
        Map<QName, ChildDataModel> nonChildProperties = sourceModel.getNonChildProperties();
        
        for (Map.Entry<QName, ChildDataModel> nonChildPropertyEntry : nonChildProperties.entrySet()) {
            QName nonChildProperty = nonChildPropertyEntry.getKey();
            ChildDataModel dataModel = nonChildPropertyEntry.getValue();
            
            String nonChildPropertyNamespace = QNameUtilities.getNamespace(nonChildProperty);
            String nonChildPropertyKey = nonChildProperty.getLocalPart();
                    
            Object sourceValue = sourceMap.get(nonChildProperty);
            Object otherValue = otherMap.get(nonChildProperty);
            
            if (!dataModel.isReference()) {
                if (!GeneralUtilities.safeEquals(sourceValue, otherValue)) {
                    localDifference.addNonChildChange(new PropertyChangeEvent(source, nonChildPropertyKey, sourceValue, otherValue));
                }
            }
            else {
                // Comparing references
                if (sourceValue != null && otherValue == null) {
                    localDifference.addNonChildChange(new PropertyChangeEvent(source, nonChildPropertyKey, sourceValue, otherValue));
                }
                else if (sourceValue == null && otherValue != null) {
                    localDifference.addNonChildChange(new PropertyChangeEvent(source, nonChildPropertyKey, sourceValue, otherValue));
                }
                else if (sourceValue != null) {
                    BaseHK2JAXBBean sourceReference = (BaseHK2JAXBBean) sourceValue;
                    BaseHK2JAXBBean otherReference = (BaseHK2JAXBBean) otherValue;
                    
                    String sourceReferenceKey = sourceReference._getKeyValue();
                    String otherReferenceKey = otherReference._getKeyValue();
                    
                    if (!GeneralUtilities.safeEquals(sourceReferenceKey, otherReferenceKey)) {
                        localDifference.addNonChildChange(new PropertyChangeEvent(source, nonChildPropertyKey, sourceValue, otherValue));
                    }
                    
                }
                
            }
        }
        
        Map<QName, ParentedModel> childProperties = sourceModel.getChildrenByName();
        for (Map.Entry<QName, ParentedModel> childEntry : childProperties.entrySet()) {
            QName xmlTag = childEntry.getKey();
            ParentedModel pModel = childEntry.getValue();
            
            String xmlTagNamespace = QNameUtilities.getNamespace(xmlTag);
            String xmlTagKey = xmlTag.getLocalPart();
            
            Object sourceValue = sourceMap.get(xmlTag);
            Object otherValue = otherMap.get(xmlTag);
            
            if (ChildType.DIRECT.equals(pModel.getChildType())) {
                if (sourceValue == null && otherValue != null) {
                    // This is just a pure add
                    localDifference.addAdd(xmlTagKey, (BaseHK2JAXBBean) otherValue, -1); 
                }
                else if (sourceValue != null && otherValue == null) {
                    // A pure remove
                    localDifference.addRemove(xmlTagKey, new RemoveData(xmlTagKey, (BaseHK2JAXBBean) sourceValue));
                }
                else if (sourceValue != null) {
                    QName keyProperty = pModel.getChildModel().getKeyProperty();
                    if (keyProperty == null) {
                        getAllDifferences((BaseHK2JAXBBean) sourceValue, (BaseHK2JAXBBean) otherValue, differences);
                    }
                    else {
                        String keyPropertyNamespace = QNameUtilities.getNamespace(keyProperty);
                        String keyPropertyKey = keyProperty.getLocalPart();
                        
                        String sourceKey = (String) ((BaseHK2JAXBBean) sourceValue)._getProperty(keyPropertyNamespace, keyPropertyKey);
                        String otherKey = (String) ((BaseHK2JAXBBean) otherValue)._getProperty(keyPropertyNamespace, keyPropertyKey);
                        
                        if (GeneralUtilities.safeEquals(sourceKey, otherKey)) {
                            getAllDifferences((BaseHK2JAXBBean) sourceValue, (BaseHK2JAXBBean) otherValue, differences);
                        }
                        else {
                            localDifference.addDirectReplace(xmlTagKey, (BaseHK2JAXBBean) otherValue, new RemoveData(xmlTagKey, (BaseHK2JAXBBean) sourceValue));
                        }
                    }
                }
            }
            else if (ChildType.LIST.equals(pModel.getChildType())) {
                getListDifferences(pModel,
                        sourceValue, otherValue,
                        differences,
                        xmlTagKey, source);
            }
            else if (ChildType.ARRAY.equals(pModel.getChildType())) {
                getArrayDifferences(pModel,
                        sourceValue, otherValue,
                        differences,
                        xmlTagKey, source);
            }
        }
        
        if (localDifference.isDirty()) {
            differences.addDifference(localDifference);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void getListDifferences(ParentedModel pModel,
            Object sourceValue, Object otherValue,
            Differences differences,
            String xmlTag, BaseHK2JAXBBean source) {
        Difference localDifference = new Difference(source);
        
        QName keyProperty = pModel.getChildModel().getKeyProperty();
        
        List<BaseHK2JAXBBean> sourceValueList = (List<BaseHK2JAXBBean>) sourceValue;
        List<BaseHK2JAXBBean> otherValueList = (List<BaseHK2JAXBBean>) otherValue;
        
        if (sourceValueList == null) sourceValueList = Collections.emptyList();
        if (otherValueList == null) otherValueList = Collections.emptyList();
        
        if (keyProperty != null) {
            String keyPropertyNamespace = QNameUtilities.getNamespace(keyProperty);
            String keyPropertyKey = keyProperty.getLocalPart();
            
            Map<String, Integer> sourceIndexMap = getIndexMap(sourceValueList, keyPropertyNamespace, keyPropertyKey);
            Map<String, Integer> otherIndexMap = getIndexMap(otherValueList, keyPropertyNamespace, keyPropertyKey);
            
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
                if (otherKeyValue == null) {
                    otherKeyValue = (String) otherBean._getProperty(keyPropertyNamespace, keyPropertyKey);
                }
                
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
        
        if (localDifference.isDirty()) {
            differences.addDifference(localDifference);
        }
    }
    
    private static void getArrayDifferences(ParentedModel pModel,
            Object sourceValue, Object otherValue,
            Differences differences,
            String xmlTag, BaseHK2JAXBBean source) {
        Difference localDifference = new Difference(source);

        QName keyProperty = pModel.getChildModel().getKeyProperty();
        
        Object sourceArray = (sourceValue == null) ? new BaseHK2JAXBBean[0] : sourceValue ;
        Object otherArray = (otherValue == null) ? new BaseHK2JAXBBean[0] : otherValue;
        
        if (keyProperty != null) {
            String keyPropertyNamespace = QNameUtilities.getNamespace(keyProperty);
            String keyPropertyKey = keyProperty.getLocalPart();
            
            Map<String, Integer> sourceIndexMap = getIndexMapArray(sourceArray, keyPropertyNamespace, keyPropertyKey);
            Map<String, Integer> otherIndexMap = getIndexMapArray(otherArray, keyPropertyNamespace, keyPropertyKey);
            
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
                if (otherKeyValue == null) {
                    otherKeyValue = (String) otherBean._getProperty(keyPropertyNamespace, keyPropertyKey);
                }
                
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
                ParentedModel parentedModel = model.getChild("", xmlKey);
                ChildType childType = parentedModel.getChildType();
                
                boolean changeList = ChildType.DIRECT.equals(childType);
                
                Object oldListOrArray = source._getProperty("", xmlKey);
                Map<Integer, BaseHK2JAXBBean> arrayChanges = null;
                
                if (!changeList) {
                  arrayChanges = new LinkedHashMap<Integer, BaseHK2JAXBBean>();
                }
                
                for (AddRemoveData ard : childDiffs.getDirectReplaces()) {
                    RemoveData removed = ard.getRemove();
                    AddData added = ard.getAdd();
                    
                    BaseHK2JAXBBean addMe = added.getToAdd();
                    String addedKey = addMe._getKeyValue();
                    
                    BaseHK2JAXBBean removedBean = (BaseHK2JAXBBean) source._doRemove("", xmlKey, removed.getChildKey(), removed.getIndex(), removed.getChild(), false);
                    BaseHK2JAXBBean addedBean = (BaseHK2JAXBBean) source._doAdd("", xmlKey, addMe, addedKey, -1, false);
                    
                    allSourceChanges.add(new PropertyChangeEvent(source, xmlKey, removedBean, addedBean));
                }
                
                for (AddData added : childDiffs.getAdds()) {
                    BaseHK2JAXBBean addMe = added.getToAdd();
                    int index = added.getIndex();
                
                    BaseHK2JAXBBean addedBean = (BaseHK2JAXBBean) source._doAdd("", xmlKey, addMe, null, index, false);
                    if (!changeList) {
                        arrayChanges.put(index, addedBean);
                    }
                    else {
                        allSourceChanges.add(new PropertyChangeEvent(source, xmlKey, null, addedBean));
                    }
                }
                
                for (RemoveData removed : childDiffs.getRemoves()) {
                    source._doRemove("", xmlKey, removed.getChildKey(), removed.getIndex(), removed.getChild(), false);
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
                source._changeInHub(events, wbd);
            }
            
            for (PropertyChangeEvent pce : events) { 
                source._setProperty("", pce.getPropertyName(), pce.getNewValue(), false, true);
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
            String propNamespace = parentedModel.getChildXmlNamespace();
            String propName = parentedModel.getChildXmlTag();
            
            Object rawChild = bean._getProperty(propNamespace, propName);
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
    
    private static AltAnnotation isSpecifiedAdapted(AltMethod method) {
        AltAnnotation adapterAnnotation = method.getAnnotation(XmlJavaTypeAdapter.class.getName());
        return adapterAnnotation;
    }
    
    private static AdapterInformation getAdapterInformation(AltMethod method) {
        AltAnnotation adapterAnnotation = isSpecifiedAdapted(method);
        if (adapterAnnotation == null) return null;
        
        AltClass adapter = adapterAnnotation.getClassValue("value");
        AltClass valueType = adapter.getSuperParameterizedType(ClassAltClassImpl.XML_ADAPTER, 0);
        AltClass boundType = adapter.getSuperParameterizedType(ClassAltClassImpl.XML_ADAPTER, 1);
        
        return new AdapterInformationImpl(adapter, valueType, boundType);
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
    
    private static AltClass getTrueChildTypeFromAdapter(AltAnnotation adapter) {
        AltClass adapterClass = adapter.getClassValue("value");
        if (adapterClass == null) return null;
        
        return adapterClass.getSuperParameterizedType(ClassAltClassImpl.XML_ADAPTER, 0);
    }
    
    private static AltClass getReturnTypeFromAdapter(AltAnnotation adapter) {
        AltClass adapterClass = adapter.getClassValue("value");
        if (adapterClass == null) return null;
        
        return adapterClass.getSuperParameterizedType(ClassAltClassImpl.XML_ADAPTER, 1);
    }
    
    public static MethodInformationI getMethodInformation(AltMethod m, NameInformation xmlNameMap) {
        if (m.getMethodInformation() != null) {
            return m.getMethodInformation();
        }
        
        boolean isCustom = isSpecifiedCustom(m);
        AdapterInformation adapter = getAdapterInformation(m);
        if (isCustom && adapter != null) {
            throw new RuntimeException("The method " + m + " must not be marked both with @Custom and @XmlJavaTypeAdapter");
        }
        
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
        AltClass listParameterizedType = null;
        if (getterVariable != null) {
            // This is a getter
            methodType = MethodType.GETTER;
            variable = getterVariable;
            
            AltClass returnType = m.getReturnType();
            gsType = returnType;
            
            if (List.class.getName().equals(returnType.getName())) {
                isList = true;
                listParameterizedType = m.getFirstTypeArgument();
                
                if (listParameterizedType == null) {
                    throw new RuntimeException("Cannot find child type of method " + m);
                }
                
                if (adapter != null) {
                    AltClass adapterReturnType = adapter.getBoundType();
                    if (!GeneralUtilities.safeEquals(listParameterizedType, adapterReturnType)) {
                        throw new RuntimeException("The return type of an adapted method (" + listParameterizedType + ") must match the annotation " + adapterReturnType +
                                " in " + m);
                    }
                    
                    if (adapter.isChild()) {
                      baseChildType = adapter.getValueType();
                    }
                }
                else if (listParameterizedType.isInterface()) {
                    baseChildType = listParameterizedType;
                }
            }
            else if (returnType.isArray()) {
                AltClass arrayType = returnType.getComponentType();
                if (adapter != null) {
                    AltClass adapterReturnType = adapter.getBoundType();
                    if (!GeneralUtilities.safeEquals(arrayType, adapterReturnType)) {
                        throw new RuntimeException("The return type of an adapted method (" + arrayType + ") must match the annotation " + adapterReturnType +
                                " in " + m);
                    }
                    
                    if (adapter.isChild()) {
                      baseChildType = adapter.getValueType();
                    }
                }
                else if (arrayType.isInterface()) {
                    isArray = true;
                    baseChildType = arrayType;
                }
            }
            else if (adapter != null){
                AltClass adapterReturnType = adapter.getBoundType();
                if (!GeneralUtilities.safeEquals(returnType, adapterReturnType)) {
                    throw new RuntimeException("The return type of an adapted method (" + returnType + ") must match the annotation " + adapterReturnType +
                            " in " + m);
                }
                
                if (adapter.isChild()) {
                  baseChildType = adapter.getValueType();
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
                
                listParameterizedType = m.getFirstTypeArgumentOfParameter(0);
                
                if (listParameterizedType == null) {
                    throw new RuntimeException("Cannot find child type of method " + m);
                }
                if (adapter != null) {
                    AltClass adapterReturnType = adapter.getBoundType();
                    if (!GeneralUtilities.safeEquals(listParameterizedType, adapterReturnType)) {
                        throw new RuntimeException("The return type of an adapted method (" + listParameterizedType + ") must match the annotation " + adapterReturnType +
                                " in " + m);
                    }
                    
                    if (adapter.isChild()) {
                      baseChildType = adapter.getValueType();
                    }
                }
                else if (listParameterizedType.isInterface()) {
                    baseChildType = listParameterizedType;
                }
            }
            else if (setterType.isArray()) {
                AltClass arrayType = setterType.getComponentType();
                if (adapter != null) {
                    AltClass adapterReturnType = adapter.getBoundType();
                    if (!GeneralUtilities.safeEquals(listParameterizedType, adapterReturnType)) {
                        throw new RuntimeException("The return type of an adapted method (" + arrayType + ") must match the annotation " + adapterReturnType +
                                " in " + m);
                    }
                    
                    if (adapter.isChild()) {
                      baseChildType = adapter.getValueType();
                    }
                }
                else if (arrayType.isInterface()) {
                    isArray = true;
                    baseChildType = arrayType;
                }
            }
            else if (adapter != null) {
                AltClass adapterReturnType = adapter.getBoundType();
                if (!GeneralUtilities.safeEquals(setterType, adapterReturnType)) {
                    throw new RuntimeException("The return type of an adapted method (" + setterType + ") must match the annotation " + adapterReturnType +
                            " in " + m);
                }
                
                if (adapter.isChild()) {
                  baseChildType = adapter.getValueType();
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
        
        String repPropNamespace = xmlNameMap.getNamespaceMap(variable);
        String repPropName = xmlNameMap.getNameMap(variable);
        
        
        QName representedProperty;
        if (repPropName == null) {
            representedProperty = QNameUtilities.createQName("", variable);
        }
        else {
            representedProperty = QNameUtilities.createQName(repPropNamespace, repPropName);
        }
        
        String defaultValue = xmlNameMap.getDefaultNameMap(variable);
        
        String xmlWrapperTag = xmlNameMap.getXmlWrapperTag(variable);
        
        boolean key = false;
        if ((m.getAnnotation(XmlID.class.getName()) != null) || (m.getAnnotation(XmlIdentifier.class.getName()) != null)) {
            key = true;
        }
        
        boolean isReference = xmlNameMap.isReference(variable);
        Format format = xmlNameMap.getFormat(variable);
        
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
                format,
                listParameterizedType,
                xmlWrapperTag,
                adapter);
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
    
    /**
     * TODO:  References that are outside of the scope of this bean will be... difficult or impossible
     * to fill in.  So, like, how do we handle them?
     * 
     * @param copyMe
     * @return
     */
    private static BaseHK2JAXBBean createUnrootedBeanTreeCopy(BaseHK2JAXBBean copyMe) {
        BaseHK2JAXBBean copy = null;
        try {
            copy = Utilities.doCopy(copyMe, null, null, null, null, null);
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Throwable th) {
            throw new RuntimeException(th);
        }
        
        return copy;
    }
    
    public static <T> BaseHK2JAXBBean doCopy(BaseHK2JAXBBean copyMe,
            DynamicChangeInfo<T> copyController,
            BaseHK2JAXBBean theCopiedParent,
            XmlRootHandleImpl<?> rootHandle,
            Map<ReferenceKey, BaseHK2JAXBBean> referenceMap,
            List<UnresolvedReference> unresolved) throws Throwable {
        if (copyMe == null) return null;
        
        BaseHK2JAXBBean retVal = Utilities.createBean(copyMe.getClass());
        retVal._shallowCopyFrom(copyMe, (referenceMap == null));
        
        ModelImpl myModel = retVal._getModel();
        
        Set<QName> childrenProps = copyMe._getChildrenXmlTags();
        for (QName childProp : childrenProps) {
            String childPropNamespace = QNameUtilities.getNamespace(childProp);
            String childPropKey = childProp.getLocalPart();
            
            Object child = copyMe._getProperty(childPropNamespace, childPropKey);
            if (child == null) continue;
            
            if (child instanceof List) {
                List<?> childList = (List<?>) child;
                
                ArrayList<Object> toSetChildList = new ArrayList<Object>(childList.size());
                
                for (Object subChild : childList) {
                    BaseHK2JAXBBean copiedChild = doCopy((BaseHK2JAXBBean) subChild, copyController, retVal, rootHandle, referenceMap, unresolved);
                    
                    toSetChildList.add(copiedChild);
                }
                
                // Sets the list property into the parent
                retVal._setProperty(childPropNamespace, childPropKey, toSetChildList);
            }
            else if (child.getClass().isArray()) {
                int length = Array.getLength(child);
                
                ParentedModel pm = myModel.getChild(childPropNamespace, childPropKey);
                ModelImpl childModel = pm.getChildModel();
                
                Class<?> childInterface = childModel.getOriginalInterfaceAsClass();
                
                Object toSetChildArray = Array.newInstance(childInterface, length);
                
                for (int lcv = 0; lcv < length; lcv++) {
                    Object subChild = Array.get(child, lcv);
                    
                    BaseHK2JAXBBean copiedChild = doCopy((BaseHK2JAXBBean) subChild, copyController, retVal, rootHandle, referenceMap, unresolved);
                    
                    Array.set(toSetChildArray, lcv, copiedChild);
                }
                
                // Sets the array property into the parent
                retVal._setProperty(childPropNamespace, childPropKey, toSetChildArray);
            }
            else {
                // A direct child
                BaseHK2JAXBBean copiedChild = doCopy((BaseHK2JAXBBean) child, copyController, retVal, rootHandle, referenceMap, unresolved);
                
                retVal._setProperty(childPropNamespace, childPropKey, copiedChild);
            }
        }
        
        if (theCopiedParent != null) {
            retVal._setParent(theCopiedParent);
        }
        
        QName keyPropertyName = retVal._getKeyPropertyName();
        if (referenceMap != null && keyPropertyName != null) {
            String keyProperty = retVal._getKeyValue();
            if (keyProperty != null) {
                referenceMap.put(new ReferenceKey(myModel.getOriginalInterface(), keyProperty), retVal);
            }
            
            // Now try to resolve any references, and if we can not add them to the unfinished list
            Map<QName, ChildDataModel> nonChildrenProps = myModel.getNonChildProperties();
            for (Map.Entry<QName, ChildDataModel> nonChild : nonChildrenProps.entrySet()) {
                QName xmlTag = nonChild.getKey();
                ChildDataModel cdm = nonChild.getValue();
                
                String xmlTagNamespace = QNameUtilities.getNamespace(xmlTag);
                String xmlTagKey = xmlTag.getLocalPart();
                
                if (!cdm.isReference()) continue;
                
                Object fromReferenceRaw = copyMe._getProperty(xmlTagNamespace, xmlTagKey);
                if (fromReferenceRaw == null) continue;
                if (!(fromReferenceRaw instanceof BaseHK2JAXBBean)) continue;
                BaseHK2JAXBBean fromReference = (BaseHK2JAXBBean) fromReferenceRaw;
                
                String fromKeyValue = fromReference._getKeyValue();
                
                ReferenceKey rk = new ReferenceKey(cdm.getChildType(), fromKeyValue);
                
                BaseHK2JAXBBean toReference = referenceMap.get(rk);
                if (toReference != null) {
                    retVal._setProperty(xmlTagNamespace, xmlTagKey, toReference);
                }
                else {
                    // Must go in unfinished list
                    unresolved.add(new UnresolvedReference(cdm.getChildType(), fromKeyValue, xmlTagNamespace, xmlTagKey, retVal));
                }
            }
        }
        
        if (rootHandle != null) {
            retVal._setDynamicChangeInfo(rootHandle, copyController, false);
        }
        
        return retVal;
    }
    
    public static String safeString(String originalValue) {
        if (originalValue == null) return null;
        
        if (Generator.JAXB_DEFAULT_DEFAULT.equals(originalValue)) {
            return "\\u0000";
        }
        
        return originalValue;
    }
    
    public static void calculateNamespaces(BaseHK2JAXBBean bean, XmlRootHandleImpl<?> root, Map<String, String> currentValues) {
        BaseHK2JAXBBean parent = (BaseHK2JAXBBean) bean._getParent();
        if (parent != null) {
            calculateNamespaces(parent, root, currentValues);
        }
        
        Map<String, String> packageOnly;
        if (root != null) {
            packageOnly = root.getPackageNamespace(bean.getClass());
        }
        else {
            packageOnly = PackageToNamespaceComputable.calculateNamespaces(bean.getClass());
        }
        currentValues.putAll(packageOnly);
    }
    
    public static String constructXmlTag(String wrapper, String tag) {
        if (wrapper == null) return tag;
        return wrapper + "/" + tag;
    }
}
