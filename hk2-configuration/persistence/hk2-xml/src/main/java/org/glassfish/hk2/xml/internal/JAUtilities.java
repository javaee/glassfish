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

import java.beans.Introspector;
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
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.jaxb.internal.XmlElementImpl;
import org.glassfish.hk2.xml.jaxb.internal.XmlRootElementImpl;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
public class JAUtilities {
    private final static String GET = "get";
    private final static String SET = "set";
    private final static String IS = "is";
    private final static String JAXB_DEFAULT_STRING = "##default";
    
    private final static String CLASS_ADD_ON_NAME = "_$$_Hk2_Jaxb";
    private final static HashSet<String> DO_NOT_HANDLE_METHODS = new HashSet<String>();
    
    private final HashMap<Class<?>, Class<?>> convertedCache = new HashMap<Class<?>, Class<?>>();
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
    
    public synchronized Class<?> convertRootAndLeaves(Class<?> root) {
        LinkedHashSet<Class<?>> needsToBeConverted = new LinkedHashSet<Class<?>>();
        
        getAllToConvert(root, needsToBeConverted);
        needsToBeConverted.removeAll(convertedCache.keySet());
        
        for (Class<?> convertMe : needsToBeConverted) {
            Class<?> converted;
            try {
                converted = convert(convertMe);
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Throwable e) {
                throw new MultiException(e);
            }
            
            convertedCache.put(convertMe, converted);
        }
        
        return convertedCache.get(root);
    }
    
    private Class<?> convert(Class<?> convertMe) throws Throwable {
        Logger.getLogger().debug("XmlService converting " + convertMe.getName());
        
        CtClass originalCtClass = defaultClassPool.get(convertMe.getName());
        String targetClassName = convertMe.getName() + CLASS_ADD_ON_NAME;
        
        CtClass foundClass = defaultClassPool.getOrNull(targetClassName);
        if (foundClass != null) {
            Class<?> proxy = convertMe.getClassLoader().loadClass(targetClassName);
           
            return proxy;
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
                
                XmlRootElement replacement = new XmlRootElementImpl(xre.namespace(), convertXmlRootElementName(xre, convertMe));
                
                createAnnotationCopy(targetConstPool, replacement, ctAnnotations);
            }
            else {
                createAnnotationCopy(targetConstPool, convertMeAnnotation, ctAnnotations);
            }
        }
        if (ctAnnotations != null) {
            targetClassFile.addAttribute(ctAnnotations);
        }
        
        // TODO:  Add in any class level JAXB annotations here
        
        targetCtClass.setSuperclass(superClazz);
        targetCtClass.addInterface(originalCtClass);
        
        Map<String, String> xmlNameMap = new HashMap<String, String>();
        for (Method originalMethod : convertMe.getMethods()) {
            String setterVariable = isSetter(originalMethod);
            if (setterVariable == null) {
                setterVariable = isGetter(originalMethod);
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
        
        for (Method originalMethod : convertMe.getMethods()) {            
            String setterVariable = isSetter(originalMethod);
            String getterVariable = isGetter(originalMethod);
            
            if (setterVariable == null && getterVariable == null) {
                throw new RuntimeException("Unknown method type, neither setter nor getter: " + originalMethod);
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
            
            Class<?> childType = null;
            if (setterVariable != null) {
                setterVariable = xmlNameMap.get(setterVariable);
                
                Class<?> setterType = originalMethod.getParameterTypes()[0];
                
                if (List.class.equals(setterType)) {
                    Type typeChildType = ReflectionHelper.getFirstTypeArgument(originalMethod.getGenericParameterTypes()[0]);
                    
                    Class<?> baseChildType = ReflectionHelper.getRawClass(typeChildType);
                    if (baseChildType == null || !convertedCache.containsKey(baseChildType)) {
                        throw new RuntimeException("Unknown child type: " + childType + " of class " +
                            ((baseChildType == null) ? "null" : baseChildType.getName()));
                        
                    }
                    
                    childType = convertedCache.get(baseChildType);
                }
                else if (setterType.isInterface()) {
                    childType = convertedCache.get(setterType);
                }
                
                sb.append(setterType.getName() + " arg0) { super._setProperty(\"" + setterVariable + "\", arg0); }");
            }
            else if (getterVariable != null) {
                if (xmlNameMap.containsKey(getterVariable)) {
                    getterVariable = xmlNameMap.get(getterVariable);
                }
                
                Class<?> returnType = originalMethod.getReturnType();
                
                if (List.class.equals(returnType)) {
                    Type typeChildType = ReflectionHelper.getFirstTypeArgument(originalMethod.getGenericReturnType());
                    
                    Class<?> baseChildType = ReflectionHelper.getRawClass(typeChildType);
                    if (baseChildType == null || !convertedCache.containsKey(baseChildType)) {
                        throw new RuntimeException("Unknown child type: " + childType + " of class " +
                            ((baseChildType == null) ? "null" : baseChildType.getName()));
                        
                    }
                    
                    childType = convertedCache.get(baseChildType);
                }
                else if (returnType.isInterface()) {
                    childType = convertedCache.get(returnType);
                }
                
                String cast = "";
                String superMethodName = "_getProperty";
                if (int.class.equals(returnType)) {
                    superMethodName += "I"; 
                }
                else if (long.class.equals(returnType)) {
                    superMethodName += "J";
                }
                else if (boolean.class.equals(returnType)) {
                    superMethodName += "Z";
                }
                else if (byte.class.equals(returnType)) {
                    superMethodName += "B";
                }
                else if (char.class.equals(returnType)) {
                    superMethodName += "C";
                }
                else if (short.class.equals(returnType)) {
                    superMethodName += "S";
                }
                else if (float.class.equals(returnType)) {
                    superMethodName += "F";
                }
                else if (double.class.equals(returnType)) {
                    superMethodName += "D";
                }
                else {
                    cast = "(" + returnType.getName() + ") ";
                }
                
                sb.append(") { return " + cast + "super." + superMethodName + "(\"" + getterVariable + "\"); }");
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
                            (Class<?>) childType);
                }
                    
                createAnnotationCopy(methodConstPool, convertMeAnnotation, ctAnnotations);
                
            }
            
            if (ctAnnotations != null) {
                methodInfo.addAttribute(ctAnnotations);
            }
            
            targetCtClass.addMethod(addMeCtMethod);
        }
        
        Class<?> proxy = targetCtClass.toClass(convertMe.getClassLoader(), convertMe.getProtectionDomain());
        
        return proxy;
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
    
    private static String isGetter(Method method) {
        String name = method.getName();
        
        if (name.startsWith(GET)) {
            if (name.length() <= GET.length()) return null;
            if (method.getParameterTypes().length != 0) return null;
            if (void.class.equals(method.getReturnType())) return null;
            
            String variableName = name.substring(GET.length());
            
            return Introspector.decapitalize(variableName);
        }
        
        if (name.startsWith(IS)) {
            if (name.length() <= IS.length()) return null;
            if (method.getParameterTypes().length != 0) return null;
            if (boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType())) {
                String variableName = name.substring(IS.length());
                
                return Introspector.decapitalize(variableName);
            }
            
            return null;
        }
        
        return null;
    }
    
    private static String isSetter(Method method) {
        String name = method.getName();
        
        if (name.startsWith(SET)) {
            if (name.length() <= SET.length()) return null;
            if (method.getParameterTypes().length != 1) return null;
            if (void.class.equals(method.getReturnType())) {
                String variableName = name.substring(SET.length());
                
                return Introspector.decapitalize(variableName);
            }
            
            return null;
        }
        
        return null;
    }
    
    private static String convertXmlRootElementName(XmlRootElement root, Class<?> clazz) {
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

}
