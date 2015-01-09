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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.jaxb.internal.XmlElementImpl;
import org.glassfish.hk2.xml.jaxb.internal.XmlRootElementImpl;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
public class JAUtilities {
    /* package */ final static String GET = "get";
    /* package */ final static String SET = "set";
    /* package */ final static String IS = "is";
    /* package */ final static String LOOKUP = "lookup";
    /* package */ final static String ADD = "add";
    /* package */ final static String REMOVE = "remove";
    /* package */ final static String JAXB_DEFAULT_STRING = "##default";
    
    private final static String CLASS_ADD_ON_NAME = "_$$_Hk2_Jaxb";
    private final static HashSet<String> DO_NOT_HANDLE_METHODS = new HashSet<String>();
    
    private final HashMap<Class<?>, UnparentedNode> interface2NodeCache = new HashMap<Class<?>, UnparentedNode>();
    private final HashMap<Class<?>, UnparentedNode> proxy2NodeCache = new HashMap<Class<?>, UnparentedNode>();
    private final ClassPool defaultClassPool = ClassPool.getDefault(); // TODO:  We probably need to be more sophisticated about this
    private final CtClass superClazz;
    
    static {
        DO_NOT_HANDLE_METHODS.add("hashCode");
        DO_NOT_HANDLE_METHODS.add("equals");
        DO_NOT_HANDLE_METHODS.add("toString");
        DO_NOT_HANDLE_METHODS.add("annotationType");
    }
    
    /* package */ JAUtilities() {
        try {
            superClazz = defaultClassPool.get(BaseHK2JAXBBean.class.getName());
        }
        catch (NotFoundException e) {
            throw new MultiException(e);
        }
        
    }
    
    public synchronized UnparentedNode getNode(Class<?> type) {
        UnparentedNode retVal = proxy2NodeCache.get(type);
        if (retVal == null) return interface2NodeCache.get(type);
        return retVal;
    }
    
    public synchronized UnparentedNode convertRootAndLeaves(Class<?> root) {
        LinkedHashSet<Class<?>> needsToBeConverted = new LinkedHashSet<Class<?>>();
        
        getAllToConvert(root, needsToBeConverted);
        needsToBeConverted.removeAll(interface2NodeCache.keySet());
        
        for (Class<?> convertMe : needsToBeConverted) {
            UnparentedNode converted;
            try {
                converted = convert(convertMe);
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Throwable e) {
                throw new MultiException(e);
            }
            
            interface2NodeCache.put(convertMe, converted);
        }
        
        return interface2NodeCache.get(root);
    }
    
    private static Map<String, String> getXmlNameMap(Class<?> convertMe) {
        Map<String, String> xmlNameMap = new HashMap<String, String>();
        for (Method originalMethod : convertMe.getMethods()) {
            String setterVariable = Utilities.isSetter(originalMethod);
            if (setterVariable == null) {
                setterVariable = Utilities.isGetter(originalMethod);
                if (setterVariable == null) continue;
            }
            
            XmlElement xmlElement = originalMethod.getAnnotation(XmlElement.class);
            if (xmlElement != null) {
                if (JAXB_DEFAULT_STRING.equals(xmlElement.name())) {
                    xmlNameMap.put(setterVariable, setterVariable);
                }
                else {
                    xmlNameMap.put(setterVariable, xmlElement.name());
                }
            }
            else {
                XmlAttribute xmlAttribute = originalMethod.getAnnotation(XmlAttribute.class);
                if (xmlAttribute != null) {
                    if (JAXB_DEFAULT_STRING.equals(xmlAttribute.name())) {
                        xmlNameMap.put(setterVariable, setterVariable);
                    }
                    else {
                        xmlNameMap.put(setterVariable, xmlAttribute.name());
                    }
                }
            }
            
        }
        
        return xmlNameMap;
    }
    
    private UnparentedNode convert(Class<?> convertMe) throws Throwable {
        Logger.getLogger().debug("XmlService converting " + convertMe.getName());
        UnparentedNode retVal = new UnparentedNode(convertMe);
        
        CtClass originalCtClass = defaultClassPool.get(convertMe.getName());
        String targetClassName = convertMe.getName() + CLASS_ADD_ON_NAME;
        
        CtClass foundClass = defaultClassPool.getOrNull(targetClassName);
        if (foundClass != null) {
            Class<?> proxy = convertMe.getClassLoader().loadClass(targetClassName);
            
            for (java.lang.annotation.Annotation convertMeAnnotation : convertMe.getAnnotations()) {
                if (!XmlRootElement.class.equals(convertMeAnnotation.annotationType())) continue;
                
                XmlRootElement xre = (XmlRootElement) convertMeAnnotation;
                    
                String rootName = Utilities.convertXmlRootElementName(xre, convertMe);
                retVal.setRootName(rootName);
            }
            
            Map<String, String> xmlNameMap = getXmlNameMap(convertMe);
            
            HashMap<UnparentedNode, String> childTypes = new HashMap<UnparentedNode, String>();
            MethodInformation foundKey = null;
            for (Method originalMethod : convertMe.getMethods()) {
                MethodInformation mi = getMethodInformation(originalMethod, xmlNameMap);
                
                if (mi.key) {
                    if (foundKey != null) {
                        throw new RuntimeException("Class " + convertMe.getName() + " has multiple key properties (" + originalMethod.getName() +
                                " and " + foundKey.originalMethod.getName());
                    }
                    foundKey = mi;
                    
                    retVal.setKeyProperty(mi.representedProperty);
                }
                
                boolean getterOrSetter = false;
                UnparentedNode childType = null;
                if (MethodType.SETTER.equals(mi.methodType)) {
                    getterOrSetter = true;
                    if (mi.baseChildType != null) {
                        if (!interface2NodeCache.containsKey(mi.baseChildType)) {
                            throw new RuntimeException("The child of type " + mi.baseChildType.getName() + " is unknown for method " + mi.originalMethod);
                        }
                        
                        childType = interface2NodeCache.get(mi.baseChildType);
                    }
                }
                else if (MethodType.GETTER.equals(mi.methodType)) {
                    getterOrSetter = true;
                    if (mi.baseChildType != null) {
                        if (!interface2NodeCache.containsKey(mi.baseChildType)) {
                            throw new RuntimeException("The child of type " + mi.baseChildType.getName() + " is unknown for method " + mi.originalMethod);
                        }
                        
                        childType = interface2NodeCache.get(mi.baseChildType);
                    }
                }
                
                if (getterOrSetter) {
                    if (childType != null) {
                        if (childTypes.containsKey(childType)) {
                            String variableName = childTypes.get(childType);
                            if (!variableName.equals(mi.representedProperty)) {
                                throw new RuntimeException(
                                    "Multiple children of " + convertMe.getName() +
                                    " cannot have the same type.  Consider extending one or more of these to disambiguate the child: " +
                                    childType.getOriginalInterface().getName());
                            }
                        }
                        else {
                            childTypes.put(childType, mi.representedProperty);
                        
                            retVal.addChild(mi.representedProperty, mi.isList, childType);
                        }
                    }
                    else {
                        retVal.addNonChildProperty(mi.representedProperty);
                    }
                }
            }
            
            retVal.setTranslatedClass(proxy);
            proxy2NodeCache.put(proxy, retVal);
            
            return retVal;
        }
        
        CtClass targetCtClass = defaultClassPool.makeClass(targetClassName);
        ClassFile targetClassFile = targetCtClass.getClassFile();
        targetClassFile.setVersionToJava5();
        ConstPool targetConstPool = targetClassFile.getConstPool();
        
        AnnotationsAttribute ctAnnotations = null;
        for (java.lang.annotation.Annotation convertMeAnnotation : convertMe.getAnnotations()) {
            if (Contract.class.equals(convertMeAnnotation.annotationType())) {
                // We do NOT want the generated class to be in the set of contracts, so
                // skip this one if it is there
                continue;
            }
            
            if (ctAnnotations == null) {
                ctAnnotations = new AnnotationsAttribute(targetConstPool, AnnotationsAttribute.visibleTag);
            }
            
            if (XmlRootElement.class.equals(convertMeAnnotation.annotationType())) {
                XmlRootElement xre = (XmlRootElement) convertMeAnnotation;
                
                String rootName = Utilities.convertXmlRootElementName(xre, convertMe);
                retVal.setRootName(rootName);
                
                XmlRootElement replacement = new XmlRootElementImpl(xre.namespace(), rootName);
                
                createAnnotationCopy(targetConstPool, replacement, ctAnnotations);
            }
            else {
                createAnnotationCopy(targetConstPool, convertMeAnnotation, ctAnnotations);
            }
        }
        if (ctAnnotations != null) {
            targetClassFile.addAttribute(ctAnnotations);
        }
        
        targetCtClass.setSuperclass(superClazz);
        targetCtClass.addInterface(originalCtClass);
        
        Map<String, String> xmlNameMap = getXmlNameMap(convertMe);
        
        HashMap<UnparentedNode, String> childTypes = new HashMap<UnparentedNode, String>();
        MethodInformation foundKey = null;
        for (Method originalMethod : convertMe.getMethods()) {
            MethodInformation mi = getMethodInformation(originalMethod, xmlNameMap);
            
            if (mi.key) {
                if (foundKey != null) {
                    throw new RuntimeException("Class " + convertMe.getName() + " has multiple key properties (" + originalMethod.getName() +
                            " and " + foundKey.originalMethod.getName());
                }
                foundKey = mi;
                
                retVal.setKeyProperty(mi.representedProperty);
            }
            
            String name = originalMethod.getName();
            
            StringBuffer sb = new StringBuffer("public ");
            
            Class<?> originalRetType = originalMethod.getReturnType();
            if (originalRetType == null || void.class.equals(originalRetType)) {
                sb.append("void ");
            }
            else {
                sb.append(originalRetType.getName() + " ");
            }
            
            sb.append(name + "(");
            
            UnparentedNode childType = null;
            boolean getterOrSetter = false;
            if (MethodType.SETTER.equals(mi.methodType)) {
                getterOrSetter = true;
                if (mi.baseChildType != null) {
                    if (!interface2NodeCache.containsKey(mi.baseChildType)) {
                        throw new RuntimeException("The child of type " + mi.baseChildType.getName() + " is unknown for method " + mi.originalMethod);
                    }
                    
                    childType = interface2NodeCache.get(mi.baseChildType);
                }
                
                sb.append(mi.getterSetterType.getName() + " arg0) { super._setProperty(\"" + mi.representedProperty + "\", arg0); }");
            }
            else if (MethodType.GETTER.equals(mi.methodType)) {
                getterOrSetter = true;
                if (mi.baseChildType != null) {
                    if (!interface2NodeCache.containsKey(mi.baseChildType)) {
                        throw new RuntimeException("The child of type " + mi.baseChildType.getName() + " is unknown for method " + mi.originalMethod);
                    }
                    
                    childType = interface2NodeCache.get(mi.baseChildType);
                }
                
                String cast = "";
                String superMethodName = "_getProperty";
                if (int.class.equals(mi.getterSetterType)) {
                    superMethodName += "I"; 
                }
                else if (long.class.equals(mi.getterSetterType)) {
                    superMethodName += "J";
                }
                else if (boolean.class.equals(mi.getterSetterType)) {
                    superMethodName += "Z";
                }
                else if (byte.class.equals(mi.getterSetterType)) {
                    superMethodName += "B";
                }
                else if (char.class.equals(mi.getterSetterType)) {
                    superMethodName += "C";
                }
                else if (short.class.equals(mi.getterSetterType)) {
                    superMethodName += "S";
                }
                else if (float.class.equals(mi.getterSetterType)) {
                    superMethodName += "F";
                }
                else if (double.class.equals(mi.getterSetterType)) {
                    superMethodName += "D";
                }
                else {
                    cast = "(" + mi.getterSetterType.getName() + ") ";
                }
                
                sb.append(") { return " + cast + "super." + superMethodName + "(\"" + mi.representedProperty + "\"); }");
            }
            else if (MethodType.LOOKUP.equals(mi.methodType)) {
                sb.append("java.lang.String arg0) { return (" + originalRetType.getName() +
                        ") super._lookupChild(\"" + mi.representedProperty + "\", arg0); }");
                
            }
            else if (MethodType.ADD.equals(mi.methodType)) {
                Class<?>[] paramTypes = originalMethod.getParameterTypes();
                if (paramTypes.length == 1) {
                    sb.append(paramTypes[0].getName() + " arg0) { super._doAdd(\"" + mi.representedProperty + "\",");
                    
                    if (paramTypes[0].isInterface()) {
                        sb.append("arg0, null, -1); }");
                    }
                    else if (String.class.equals(paramTypes[0])) {
                        sb.append("null, arg0, -1); }");
                    }
                    else {
                        sb.append("null, null, arg0); }");
                    }
                }
                else {
                    sb.append(paramTypes[0].getName() + " arg0, int arg1) { super._doAdd(\"" + mi.representedProperty + "\",");
                    
                    if (paramTypes[0].isInterface()) {
                        sb.append("arg0, null, arg1); }");
                    }
                    else {
                        sb.append("null, arg0, arg1); }");
                    }
                }
            }
            else if (MethodType.REMOVE.equals(mi.methodType)) {
                Class<?>[] paramTypes = originalMethod.getParameterTypes();
                if (String.class.equals(paramTypes[0])) {
                    sb.append("java.lang.String arg0) { return (" + originalRetType.getName()  +
                            ") super._doRemove(\"" + mi.representedProperty + "\", arg0, -1); }");
                }
                else {
                    sb.append("int arg0) { return (" + originalRetType.getName()  +
                            ") super._doRemove(\"" + mi.representedProperty + "\", null, arg0); }");
                }
            }
            
            if (getterOrSetter) {
                if (childType != null) {
                    if (childTypes.containsKey(childType)) {
                        String variableName = childTypes.get(childType);
                        if (!variableName.equals(mi.representedProperty)) {
                            throw new RuntimeException(
                                "Multiple children of " + convertMe.getName() +
                                " cannot have the same type.  Consider extending one or more of these to disambiguate the child: " +
                                childType.getOriginalInterface().getName());
                        }
                    }
                    else {
                        childTypes.put(childType, mi.representedProperty);
                    
                        retVal.addChild(mi.representedProperty, mi.isList, childType);
                    }
                }
                else {
                    retVal.addNonChildProperty(mi.representedProperty);
                }
            }
            
            CtMethod addMeCtMethod = CtNewMethod.make(sb.toString(), targetCtClass);
            MethodInfo methodInfo = addMeCtMethod.getMethodInfo();    
            ConstPool methodConstPool = methodInfo.getConstPool();
           
            ctAnnotations = null;
            for (java.lang.annotation.Annotation convertMeAnnotation : originalMethod.getAnnotations()) {
                if (ctAnnotations == null) {
                    ctAnnotations = new AnnotationsAttribute(methodConstPool, AnnotationsAttribute.visibleTag);
                }
                
                if ((childType != null) && XmlElement.class.equals(convertMeAnnotation.annotationType())) {
                    XmlElement original = (XmlElement) convertMeAnnotation;
                        
                    // Use generated child class
                    convertMeAnnotation = new XmlElementImpl(
                            original.name(),
                            original.nillable(),
                            original.required(),
                            original.namespace(),
                            original.defaultValue(),
                            childType.getTranslatedClass());
                }
                    
                createAnnotationCopy(methodConstPool, convertMeAnnotation, ctAnnotations);
                
            }
            
            if (ctAnnotations != null) {
                methodInfo.addAttribute(ctAnnotations);
            }
            
            targetCtClass.addMethod(addMeCtMethod);
        }
        
        Class<?> proxy = targetCtClass.toClass(convertMe.getClassLoader(), convertMe.getProtectionDomain());
        
        retVal.setTranslatedClass(proxy);
        proxy2NodeCache.put(proxy, retVal);
        
        return retVal;
    }
    
    private static void createAnnotationCopy(ConstPool parent, java.lang.annotation.Annotation javaAnnotation,
            AnnotationsAttribute retVal) throws Throwable {
        Annotation annotation = new Annotation(javaAnnotation.annotationType().getName(), parent);
        
        for (Method javaAnnotationMethod : javaAnnotation.annotationType().getMethods()) {
            if (javaAnnotationMethod.getParameterTypes().length != 0) continue;
            if (DO_NOT_HANDLE_METHODS.contains(javaAnnotationMethod.getName())) continue;
            
            Class<?> javaAnnotationType = javaAnnotationMethod.getReturnType();
            if (String.class.equals(javaAnnotationType)) {
                String value = (String) ReflectionHelper.invoke(javaAnnotation, javaAnnotationMethod, new Object[0], false);
                
                annotation.addMemberValue(javaAnnotationMethod.getName(), new StringMemberValue(value, parent));
            }
            else if (boolean.class.equals(javaAnnotationType)) {
                boolean value = (Boolean) ReflectionHelper.invoke(javaAnnotation, javaAnnotationMethod, new Object[0], false);
                
                annotation.addMemberValue(javaAnnotationMethod.getName(), new BooleanMemberValue(value, parent));
            }
            else if (Class.class.equals(javaAnnotationType)) {
                Class<?> value = (Class<?>) ReflectionHelper.invoke(javaAnnotation, javaAnnotationMethod, new Object[0], false);
                String sValue;
                if (value == null) {
                    sValue = null;
                }
                else {
                    sValue = value.getName();
                }
                
                annotation.addMemberValue(javaAnnotationMethod.getName(), new ClassMemberValue(sValue, parent));
            }
            else {
                throw new AssertionError("Annotation type " + javaAnnotationType.getName() + " is not yet implemented");
            }
            
        }
        
        retVal.addAnnotation(annotation);
    }
    
    private static void getAllToConvert(Class<?> toBeConverted, LinkedHashSet<Class<?>> needsToBeConverted) {
        if (needsToBeConverted.contains(toBeConverted)) return;
        
        // Find all the children
        for (Method method : toBeConverted.getMethods()) {
            String methodName = method.getName();
            if (!methodName.startsWith(GET)) continue;
            
            Class<?> returnClass = method.getReturnType();
            if (returnClass.isInterface() && !(List.class.equals(returnClass))) {
                // The assumption is that this is a non-instanced child
                getAllToConvert(returnClass, needsToBeConverted);
                
                continue;
            }
            
            Type retType = method.getGenericReturnType();
            if (retType == null || !(retType instanceof ParameterizedType)) continue;
            
            Class<?> returnRawClass = ReflectionHelper.getRawClass(retType);
            if (returnRawClass == null || !List.class.equals(returnRawClass)) continue;
            
            Type listReturnType = ReflectionHelper.getFirstTypeArgument(retType);
            if (Object.class.equals(listReturnType)) continue;
            
            Class<?> childClass = ReflectionHelper.getRawClass(listReturnType);
            if (childClass == null || Object.class.equals(childClass)) continue;
            
            getAllToConvert(childClass, needsToBeConverted);
        }
        
        needsToBeConverted.add(toBeConverted);
    }
    
    private static MethodInformation getMethodInformation(Method m, Map<String, String> xmlNameMap) {
        String setterVariable = Utilities.isSetter(m);
        String getterVariable = null;
        String lookupVariable = null;
        String addVariable = null;
        String removeVariable = null;
        
        if (setterVariable == null) {
            getterVariable = Utilities.isGetter(m);
            if (getterVariable == null) {
                lookupVariable = Utilities.isLookup(m);
                if (lookupVariable == null) {
                    addVariable = Utilities.isAdd(m);
                    if (addVariable == null) {
                        removeVariable = Utilities.isRemove(m);
                    }
                }
            }
        }
        
        if (setterVariable == null && getterVariable == null && lookupVariable == null && addVariable == null && removeVariable == null) {
            throw new RuntimeException("Unknown method type, neither setter nor getter nor lookup: " + m);
        }
        
        MethodType methodType;
        Class<?> baseChildType = null;
        Class<?> gsType = null;
        String variable;
        boolean isList = false;
        if (getterVariable != null) {
            // This is a getter
            methodType = MethodType.GETTER;
            variable = getterVariable;
            
            Class<?> returnType = m.getReturnType();
            gsType = returnType;
            
            if (List.class.equals(returnType)) {
                isList = true;
                Type typeChildType = ReflectionHelper.getFirstTypeArgument(m.getGenericReturnType());
                
                baseChildType = ReflectionHelper.getRawClass(typeChildType);
                if (baseChildType == null) {
                    throw new RuntimeException("Cannot find child type of method " + m);
                }
            }
            else if (returnType.isInterface()) {
                baseChildType = returnType;
            }
        }
        else if (setterVariable != null) {
            // This is a setter
            methodType = MethodType.SETTER;
            variable = setterVariable;
            
            Class<?> setterType = m.getParameterTypes()[0];
            gsType = setterType;
            
            if (List.class.equals(setterType)) {
                isList = true;
                Type typeChildType = ReflectionHelper.getFirstTypeArgument(m.getGenericParameterTypes()[0]);
                
                baseChildType = ReflectionHelper.getRawClass(typeChildType);
                if (baseChildType == null) {
                    throw new RuntimeException("Cannot find child type of method " + m);
                }
            }
            else if (setterType.isInterface()) {
                baseChildType = setterType;
            }
        }
        else if (lookupVariable != null) {
            // This is a lookup
            methodType = MethodType.LOOKUP;
            variable = lookupVariable;
            
            Class<?> lookupType = m.getReturnType();
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
            variable = addVariable;
        }
        else {
            throw new RuntimeException("Unknown method type: " + m);
        }
        
        String representedProperty = xmlNameMap.get(variable);
        if (representedProperty == null) representedProperty = variable;
        
        boolean key = false;
        if ((m.getAnnotation(XmlID.class) != null) || (m.getAnnotation(XmlIdentifier.class) != null)) {
            key = true;
        }
        
        return new MethodInformation(m, methodType, representedProperty, baseChildType, gsType, key, isList);
    }
    
    private static class MethodInformation {
        private final Method originalMethod;
        private final MethodType methodType;
        private final Class<?> getterSetterType;
        private final String representedProperty;
        private final Class<?> baseChildType;
        private final boolean key;
        private final boolean isList;
        
        private MethodInformation(Method originalMethod,
                MethodType methodType,
                String representedProperty,
                Class<?> baseChildType,
                Class<?> gsType,
                boolean key,
                boolean isList) {
            this.originalMethod = originalMethod;
            this.methodType = methodType;
            this.representedProperty = representedProperty;
            this.baseChildType = baseChildType;
            this.getterSetterType = gsType;
            this.key = key;
            this.isList = isList;
        }
        
        @Override
        public String toString() {
            return "MethodInformation(" + originalMethod.getName() + "," +
              methodType + "," +
              getterSetterType + "," +
              representedProperty + "," +
              baseChildType + "," +
              key + "," +
              System.identityHashCode(this) + ")";
              
        }
    }
    
    private static enum MethodType {
        GETTER,
        SETTER,
        LOOKUP,
        ADD,
        REMOVE
    }
}
