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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
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
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.MethodWrapper;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.utilities.reflection.internal.ClassReflectionHelperImpl;
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
    private final static boolean DEBUG_METHODS = Boolean.parseBoolean(GeneralUtilities.getSystemProperty(
            "org.jvnet.hk2.properties.xmlservice.jaxb.methods", "false"));
    
    /* package */ final static String GET = "get";
    /* package */ final static String SET = "set";
    /* package */ final static String IS = "is";
    /* package */ final static String LOOKUP = "lookup";
    /* package */ final static String ADD = "add";
    /* package */ final static String REMOVE = "remove";
    /* package */ final static String JAXB_DEFAULT_STRING = "##default";
    public final static String JAXB_DEFAULT_DEFAULT = "\u0000";
    
    private final static String CLASS_ADD_ON_NAME = "_$$_Hk2_Jaxb";
    private final static HashSet<String> DO_NOT_HANDLE_METHODS = new HashSet<String>();
    private final static String NO_CHILD_PACKAGE = "java.";
    
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
        ClassReflectionHelper helper = new ClassReflectionHelperImpl();
        LinkedHashSet<Class<?>> needsToBeConverted = new LinkedHashSet<Class<?>>();
        
        getAllToConvert(root, needsToBeConverted, new HashSet<Class<?>>());
        
        if (DEBUG_METHODS) {
            Logger.getLogger().debug("Converting " + needsToBeConverted.size() + " nodes for root " + root.getName());
        }
        
        needsToBeConverted.removeAll(interface2NodeCache.keySet());
        
        LinkedList<UnparentedNode> contributions = new LinkedList<UnparentedNode>();
        for (Class<?> convertMe : needsToBeConverted) {
            UnparentedNode converted;
            try {
                converted = convert(convertMe, helper);
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Throwable e) {
                throw new MultiException(e);
            }
            
            interface2NodeCache.put(convertMe, converted);
            contributions.add(converted);
        }
        
        for (UnparentedNode node : contributions) {
            for (ParentedNode child : node.getAllChildren()) {
                if (child.getChild().isPlaceholder()) {
                    UnparentedNode nonPlaceholder = interface2NodeCache.get(child.getChild().getOriginalInterface());
                    if (nonPlaceholder == null) {
                        throw new RuntimeException("The child of type " + child.getChild().getOriginalInterface().getName() +
                                " is unknown for " + node);  
                    }
                    
                    child.setChild(nonPlaceholder);
                }
            }
        }
        
        helper.dispose();
        return interface2NodeCache.get(root);
    }
    
    private static NameInformation getXmlNameMap(Class<?> convertMe) {
        Map<String, XmlElementData> xmlNameMap = new HashMap<String, XmlElementData>();
        HashSet<String> unmappedNames = new HashSet<String>();
        
        for (Method originalMethod : convertMe.getMethods()) {
            String setterVariable = Utilities.isSetter(originalMethod);
            if (setterVariable == null) {
                setterVariable = Utilities.isGetter(originalMethod);
                if (setterVariable == null) continue;
            }
            
            XmlElement xmlElement = originalMethod.getAnnotation(XmlElement.class);
            if (xmlElement != null) {
                String defaultValue = xmlElement.defaultValue();
                
                if (JAXB_DEFAULT_STRING.equals(xmlElement.name())) {
                    xmlNameMap.put(setterVariable, new XmlElementData(setterVariable, defaultValue));
                }
                else {
                    xmlNameMap.put(setterVariable, new XmlElementData(xmlElement.name(), defaultValue));
                }
            }
            else {
                XmlAttribute xmlAttribute = originalMethod.getAnnotation(XmlAttribute.class);
                if (xmlAttribute != null) {
                    if (JAXB_DEFAULT_STRING.equals(xmlAttribute.name())) {
                        xmlNameMap.put(setterVariable, new XmlElementData(setterVariable, JAXB_DEFAULT_DEFAULT));
                    }
                    else {
                        xmlNameMap.put(setterVariable, new XmlElementData(xmlAttribute.name(), JAXB_DEFAULT_DEFAULT));
                    }
                }
                else {
                    unmappedNames.add(setterVariable);
                }
            }
        }
        
        Set<String> noXmlElementNames = new HashSet<String>();
        for (String unmappedName : unmappedNames) {
            if (!xmlNameMap.containsKey(unmappedName)) {
                noXmlElementNames.add(unmappedName);
            }
        }
        
        return new NameInformation(xmlNameMap, noXmlElementNames);
    }
    
    private UnparentedNode convert(Class<?> convertMe, ClassReflectionHelper helper) throws Throwable {
        Logger.getLogger().debug("XmlService converting " + convertMe.getName());
        UnparentedNode retVal = new UnparentedNode(convertMe);
        
        String targetClassName = convertMe.getName() + CLASS_ADD_ON_NAME;
        CtClass foundClass = defaultClassPool.getOrNull(targetClassName);
        
        if (foundClass == null) {
            generate(convertMe,
                    helper,
                    superClazz,
                    defaultClassPool);
            
            foundClass = defaultClassPool.getOrNull(targetClassName);
        }
        
        Class<?> proxy = convertMe.getClassLoader().loadClass(targetClassName);
            
        for (java.lang.annotation.Annotation convertMeAnnotation : convertMe.getAnnotations()) {
            if (!XmlRootElement.class.equals(convertMeAnnotation.annotationType())) continue;
                
            XmlRootElement xre = (XmlRootElement) convertMeAnnotation;
                    
            String rootName = Utilities.convertXmlRootElementName(xre, convertMe);
            retVal.setRootName(rootName);
        }
            
        NameInformation xmlNameMap = getXmlNameMap(convertMe);
            
        HashMap<Class<?>, String> childTypes = new HashMap<Class<?>, String>();
        MethodInformation foundKey = null;
        for (MethodWrapper wrapper : helper.getAllMethods(convertMe)) {
            Method originalMethod = wrapper.getMethod();
            MethodInformation mi = getMethodInformation(originalMethod, xmlNameMap);
                
            if (DEBUG_METHODS) {
                Logger.getLogger().debug("Analyzing method " + mi + " of " + convertMe.getSimpleName());
            }
                
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
                        // Must use a placeholder
                        childType = new UnparentedNode(mi.baseChildType, true);
                    }
                    else {
                        childType = interface2NodeCache.get(mi.baseChildType);
                    }
                }
            }
            else if (MethodType.GETTER.equals(mi.methodType)) {
                getterOrSetter = true;
                if (mi.baseChildType != null) {
                    if (!interface2NodeCache.containsKey(mi.baseChildType)) {
                        // Must use a placeholder
                        childType = new UnparentedNode(mi.baseChildType, true);
                    }
                    else {
                        childType = interface2NodeCache.get(mi.baseChildType);
                    }
                }
            }
                
            if (getterOrSetter) {
                if (childType != null) {
                    childTypes.put(childType.getOriginalInterface(), mi.representedProperty);
                        
                    retVal.addChild(mi.representedProperty, mi.isList, mi.isArray, childType);
                }
                else {
                    retVal.addNonChildProperty(mi.representedProperty, mi.defaultValue);
                }
            }
        }
            
        retVal.setTranslatedClass(proxy);
        proxy2NodeCache.put(proxy, retVal);
            
        return retVal;
    }
    
    private static Class<?> generate(Class<?> convertMe,
            ClassReflectionHelper helper,
            CtClass superClazz,
            ClassPool defaultClassPool) throws Throwable {
        String targetClassName = convertMe.getName() + CLASS_ADD_ON_NAME;
        
        CtClass targetCtClass = defaultClassPool.makeClass(targetClassName);
        ClassFile targetClassFile = targetCtClass.getClassFile();
        targetClassFile.setVersionToJava5();
        ConstPool targetConstPool = targetClassFile.getConstPool();
        
        AnnotationsAttribute ctAnnotations = null;
        for (java.lang.annotation.Annotation convertMeAnnotation : convertMe.getAnnotations()) {
            if (Contract.class.equals(convertMeAnnotation.annotationType()) ||
                    XmlTransient.class.equals(convertMeAnnotation.annotationType())) {
                // We do NOT want the generated class to be in the set of contracts, so
                // skip this one if it is there.
                // We also DO want our own class to be processed by JAXB even
                // if the interface is not.  This is needed for the Eclipselink
                // Moxy version of JAXB, which does some processing of interfaces
                // we do not want them to do
                continue;
            }
            
            if (ctAnnotations == null) {
                ctAnnotations = new AnnotationsAttribute(targetConstPool, AnnotationsAttribute.visibleTag);
            }
            
            if (XmlRootElement.class.equals(convertMeAnnotation.annotationType())) {
                XmlRootElement xre = (XmlRootElement) convertMeAnnotation;
                
                String rootName = Utilities.convertXmlRootElementName(xre, convertMe);
                
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
        
        CtClass originalCtClass = defaultClassPool.get(convertMe.getName());
        
        targetCtClass.setSuperclass(superClazz);
        targetCtClass.addInterface(originalCtClass);
        
        NameInformation xmlNameMap = getXmlNameMap(convertMe);
        HashSet<String> alreadyAddedNaked = new HashSet<String>();
        
        HashMap<Class<?>, String> childTypes = new HashMap<Class<?>, String>();
        
        Set<MethodWrapper> allMethods = helper.getAllMethods(convertMe);
        if (DEBUG_METHODS) {
            Logger.getLogger().debug("Analyzing " + allMethods.size() + " methods of " + convertMe.getName());
        }
        
        HashSet<String> setters = new HashSet<String>();
        HashMap<String, MethodInformation> getters = new HashMap<String, MethodInformation>();
        for (MethodWrapper wrapper : allMethods) {
            Method originalMethod = wrapper.getMethod();
            MethodInformation mi = getMethodInformation(originalMethod, xmlNameMap);
            
            if (DEBUG_METHODS) {
                Logger.getLogger().debug("Analyzing method " + mi + " of " + convertMe.getSimpleName());
            }
            
            String name = originalMethod.getName();
            
            StringBuffer sb = new StringBuffer("public ");
            
            Class<?> originalRetType = originalMethod.getReturnType();
            boolean isVoid;
            if (originalRetType == null || void.class.equals(originalRetType)) {
                sb.append("void ");
                isVoid = true;
            }
            else {
                sb.append(Utilities.getCompilableClass(originalRetType) + " ");
                isVoid = false;
            }
            
            sb.append(name + "(");
            
            Class<?> childType = null;
            boolean getterOrSetter = false;
            if (MethodType.SETTER.equals(mi.methodType)) {
                getterOrSetter = true;
                setters.add(mi.representedProperty);
                
                childType = mi.baseChildType;
                
                sb.append(Utilities.getCompilableClass(mi.getterSetterType) + " arg0) { super._setProperty(\"" + mi.representedProperty + "\", arg0); }");
            }
            else if (MethodType.GETTER.equals(mi.methodType)) {
                getterOrSetter = true;
                getters.put(mi.representedProperty, mi);
                
                childType = mi.baseChildType;
                
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
                    cast = "(" + Utilities.getCompilableClass(mi.getterSetterType) + ") ";
                }
                
                sb.append(") { return " + cast + "super." + superMethodName + "(\"" + mi.representedProperty + "\"); }");
            }
            else if (MethodType.LOOKUP.equals(mi.methodType)) {
                sb.append("java.lang.String arg0) { return (" + Utilities.getCompilableClass(originalRetType) +
                        ") super._lookupChild(\"" + mi.representedProperty + "\", arg0); }");
                
            }
            else if (MethodType.ADD.equals(mi.methodType)) {
                Class<?>[] paramTypes = originalMethod.getParameterTypes();
                if (paramTypes.length == 0) {
                    sb.append(") { super._doAdd(\"" + mi.representedProperty + "\", null, null, -1); }");
                }
                else if (paramTypes.length == 1) {
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
                String cast = "";
                String function = "super._doRemoveZ(\"";
                if (!boolean.class.equals(originalRetType)) {
                    cast = "(" + Utilities.getCompilableClass(originalRetType) + ") ";
                    function = "super._doRemove(\"";
                }
                
                if (paramTypes.length == 0) {
                    sb.append(") { return " + cast + function +
                            mi.representedProperty + "\", null, -1); }");
                }
                else if (String.class.equals(paramTypes[0])) {
                    sb.append("java.lang.String arg0) { return " + cast  + function +
                            mi.representedProperty + "\", arg0, -1); }");
                }
                else {
                    sb.append("int arg0) { return " + cast + function +
                            mi.representedProperty + "\", null, arg0); }");
                }
            }
            else if (MethodType.CUSTOM.equals(mi.methodType)) {
                Class<?>[] paramTypes = originalMethod.getParameterTypes();
                
                StringBuffer classSets = new StringBuffer();
                StringBuffer valSets = new StringBuffer();
                
                int lcv = 0;
                for (Class<?> paramType : paramTypes) {
                    if (lcv == 0) {
                        sb.append(Utilities.getCompilableClass(paramType) + " arg" + lcv);
                    }
                    else {
                        sb.append(", " + Utilities.getCompilableClass(paramType) + " arg" + lcv);
                    }
                    
                    classSets.append("mParams[" + lcv + "] = " + Utilities.getCompilableClass(paramType) + ".class;\n");
                    valSets.append("mVars[" + lcv + "] = ");
                    if (int.class.equals(paramType)) {
                        valSets.append("new java.lang.Integer(arg" + lcv + ");\n");
                    }
                    else if (long.class.equals(paramType)) {
                        valSets.append("new java.lang.Long(arg" + lcv + ");\n");
                    }
                    else if (boolean.class.equals(paramType)) {
                        valSets.append("new java.lang.Boolean(arg" + lcv + ");\n");
                    }
                    else if (byte.class.equals(paramType)) {
                        valSets.append("new java.lang.Byte(arg" + lcv + ");\n");
                    }
                    else if (char.class.equals(paramType)) {
                        valSets.append("new java.lang.Character(arg" + lcv + ");\n");
                    }
                    else if (short.class.equals(paramType)) {
                        valSets.append("new java.lang.Short(arg" + lcv + ");\n");
                    }
                    else if (float.class.equals(paramType)) {
                        valSets.append("new java.lang.Float(arg" + lcv + ");\n");
                    }
                    else if (double.class.equals(paramType)) {
                        valSets.append("new java.lang.Double(arg" + lcv + ");\n");
                    }
                    else {
                        valSets.append("arg" + lcv + ";\n");
                    }
                    
                    lcv++;
                }
                
                sb.append(") { Class[] mParams = new Class[" + paramTypes.length + "];\n");
                sb.append("Object[] mVars = new Object[" + paramTypes.length + "];\n");
                sb.append(classSets.toString());
                sb.append(valSets.toString());
                
                String cast = "";
                String superMethodName = "_invokeCustomizedMethod";
                if (int.class.equals(originalRetType)) {
                    superMethodName += "I"; 
                }
                else if (long.class.equals(originalRetType)) {
                    superMethodName += "J";
                }
                else if (boolean.class.equals(originalRetType)) {
                    superMethodName += "Z";
                }
                else if (byte.class.equals(originalRetType)) {
                    superMethodName += "B";
                }
                else if (char.class.equals(originalRetType)) {
                    superMethodName += "C";
                }
                else if (short.class.equals(originalRetType)) {
                    superMethodName += "S";
                }
                else if (float.class.equals(originalRetType)) {
                    superMethodName += "F";
                }
                else if (double.class.equals(originalRetType)) {
                    superMethodName += "D";
                }
                else if (!isVoid) {
                    cast = "(" + Utilities.getCompilableClass(originalRetType) + ") ";
                }
                
                if (!isVoid) {
                    sb.append("return " + cast);
                }
                sb.append("super." + superMethodName + "(\"" + name + "\", mParams, mVars);}");
            }
            
            if (getterOrSetter && 
                    (childType != null) &&
                    !childTypes.containsKey(childType)) {
                childTypes.put(childType, mi.representedProperty);
            }
            
            if (DEBUG_METHODS) {
                // Hidden behind static because of potential expensive toString costs
                Logger.getLogger().debug("Adding method for " + convertMe.getSimpleName() + " with implementation " + sb);
            }
            
            CtMethod addMeCtMethod = CtNewMethod.make(sb.toString(), targetCtClass);
            if (originalMethod.isVarArgs()) {
                addMeCtMethod.setModifiers(addMeCtMethod.getModifiers() | Modifier.VARARGS);
            }
            MethodInfo methodInfo = addMeCtMethod.getMethodInfo();
            ConstPool methodConstPool = methodInfo.getConstPool();
           
            ctAnnotations = null;
            for (java.lang.annotation.Annotation convertMeAnnotation : originalMethod.getAnnotations()) {
                if (ctAnnotations == null) {
                    ctAnnotations = new AnnotationsAttribute(methodConstPool, AnnotationsAttribute.visibleTag);
                }
                
                if ((childType != null) && XmlElement.class.equals(convertMeAnnotation.annotationType())) {
                    XmlElement original = (XmlElement) convertMeAnnotation;
                        
                    
                    String translatedClassName = childType.getName() + CLASS_ADD_ON_NAME;
                    convertMeAnnotation = new XmlElementImpl(
                            original.name(),
                            original.nillable(),
                            original.required(),
                            original.namespace(),
                            original.defaultValue(),
                            translatedClassName);
                }
                    
                createAnnotationCopy(methodConstPool, convertMeAnnotation, ctAnnotations);
            }
            
            if (getterOrSetter && childType != null &&
                    xmlNameMap.hasNoXmlElement(mi.representedProperty) &&
                    !alreadyAddedNaked.contains(mi.representedProperty)) {
                alreadyAddedNaked.add(mi.representedProperty);
                if (ctAnnotations == null) {
                    ctAnnotations = new AnnotationsAttribute(methodConstPool, AnnotationsAttribute.visibleTag);
                }
                
                java.lang.annotation.Annotation convertMeAnnotation;
                String translatedClassName = childType.getName() + CLASS_ADD_ON_NAME;
                convertMeAnnotation = new XmlElementImpl(
                        JAXB_DEFAULT_STRING,
                        false,
                        false,
                        JAXB_DEFAULT_STRING,
                        JAXB_DEFAULT_DEFAULT,
                        translatedClassName);
                
                createAnnotationCopy(methodConstPool, convertMeAnnotation, ctAnnotations);
            }
            
            if (ctAnnotations != null) {
                methodInfo.addAttribute(ctAnnotations);
            }
            
            targetCtClass.addMethod(addMeCtMethod);
        }
        
        // Now generate the invisible setters for JAXB
        for (Map.Entry<String, MethodInformation> getterEntry : getters.entrySet()) {
            String getterProperty = getterEntry.getKey();
            MethodInformation mi = getterEntry.getValue();
            
            if (setters.contains(getterProperty)) continue;
            
            String getterName = mi.originalMethod.getName();
            String setterName = Utilities.convertToSetter(getterName);
            
            StringBuffer sb = new StringBuffer("private void " + setterName + "(");
            sb.append(Utilities.getCompilableClass(mi.getterSetterType) + " arg0) { super._setProperty(\"" + mi.representedProperty + "\", arg0); }");
            
            CtMethod addMeCtMethod = CtNewMethod.make(sb.toString(), targetCtClass);
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
                String sValue;
                if (javaAnnotation instanceof XmlElementImpl) {
                    sValue = ((XmlElementImpl) javaAnnotation).getTypeByName();
                }
                else {
                    Class<?> value = (Class<?>) ReflectionHelper.invoke(javaAnnotation, javaAnnotationMethod, new Object[0], false);
                
                    if (value == null) {
                        sValue = null;
                    }
                    else {
                        sValue = value.getName();
                    }
                }
                
                annotation.addMemberValue(javaAnnotationMethod.getName(), new ClassMemberValue(sValue, parent));
            }
            else {
                throw new AssertionError("Annotation type " + javaAnnotationType.getName() + " is not yet implemented");
            }
            
        }
        
        retVal.addAnnotation(annotation);
    }
    
    private static void getAllToConvert(Class<?> toBeConverted,
            LinkedHashSet<Class<?>> needsToBeConverted,
            Set<Class<?>> cycleDetector) {
        if (needsToBeConverted.contains(toBeConverted)) return;
        
        if (cycleDetector.contains(toBeConverted)) return;
        cycleDetector.add(toBeConverted);
        
        try {
            // Find all the children
            for (Method method : toBeConverted.getMethods()) {
                if (Utilities.isGetter(method) == null) continue;
                
                Class<?> returnClass = method.getReturnType();
                if (returnClass.isInterface() && !(List.class.equals(returnClass))) {
                    // The assumption is that this is a non-instanced child
                    if (returnClass.getName().startsWith(NO_CHILD_PACKAGE)) continue;
                    
                    getAllToConvert(returnClass, needsToBeConverted, cycleDetector);
                    
                    continue;
                }
                
                if (returnClass.isArray()) {
                    Class<?> aType = returnClass.getComponentType();
                    
                    if (aType.isInterface()) {
                        getAllToConvert(aType, needsToBeConverted, cycleDetector);
                        
                        continue;
                    }
                }
                
                Type retType = method.getGenericReturnType();
                if (retType == null || !(retType instanceof ParameterizedType)) continue;
                
                Class<?> returnRawClass = ReflectionHelper.getRawClass(retType);
                if (returnRawClass == null || !List.class.equals(returnRawClass)) continue;
                
                Type listReturnType = ReflectionHelper.getFirstTypeArgument(retType);
                if (Object.class.equals(listReturnType)) continue;
                
                Class<?> childClass = ReflectionHelper.getRawClass(listReturnType);
                if (childClass == null || Object.class.equals(childClass)) continue;
                
                getAllToConvert(childClass, needsToBeConverted, cycleDetector);
            }
            
            needsToBeConverted.add(toBeConverted);
        }
        finally {
            cycleDetector.remove(toBeConverted);
        }
    }
    
    private static MethodInformation getMethodInformation(Method m, NameInformation xmlNameMap) {
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
        
        MethodType methodType;
        Class<?> baseChildType = null;
        Class<?> gsType = null;
        String variable = null;
        boolean isList = false;
        boolean isArray = false;
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
            else if (returnType.isArray()) {
                Class<?> arrayType = returnType.getComponentType();
                if (arrayType.isInterface()) {
                    isArray = true;
                    baseChildType = arrayType;
                }
            }
            else if (returnType.isInterface() && !returnType.getName().startsWith(NO_CHILD_PACKAGE)) {
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
            else if (setterType.isArray()) {
                Class<?> arrayType = setterType.getComponentType();
                if (arrayType.isInterface()) {
                    isArray = true;
                    baseChildType = arrayType;
                }
            }
            else if (setterType.isInterface() && !setterType.getName().startsWith(NO_CHILD_PACKAGE)) {
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
            methodType = MethodType.CUSTOM;
        }
        
        String representedProperty = xmlNameMap.getNameMap(variable);
        if (representedProperty == null) representedProperty = variable;
        
        String defaultValue = xmlNameMap.getDefaultNameMap(variable);
        
        boolean key = false;
        if ((m.getAnnotation(XmlID.class) != null) || (m.getAnnotation(XmlIdentifier.class) != null)) {
            key = true;
        }
        
        return new MethodInformation(m,
                methodType,
                representedProperty,
                defaultValue,
                baseChildType,
                gsType,
                key,
                isList,
                isArray);
    }
    
    private static class MethodInformation {
        private final Method originalMethod;
        private final MethodType methodType;
        private final Class<?> getterSetterType;
        private final String representedProperty;
        private final String defaultValue;
        private final Class<?> baseChildType;
        private final boolean key;
        private final boolean isList;
        private final boolean isArray;
        
        private MethodInformation(Method originalMethod,
                MethodType methodType,
                String representedProperty,
                String defaultValue,
                Class<?> baseChildType,
                Class<?> gsType,
                boolean key,
                boolean isList,
                boolean isArray) {
            this.originalMethod = originalMethod;
            this.methodType = methodType;
            this.representedProperty = representedProperty;
            this.defaultValue = defaultValue;
            this.baseChildType = baseChildType;
            this.getterSetterType = gsType;
            this.key = key;
            this.isList = isList;
            this.isArray = isArray;
        }
        
        @Override
        public String toString() {
            return "MethodInformation(name=" + originalMethod.getName() + "," +
              "type=" + methodType + "," +
              "getterType=" + getterSetterType + "," +
              "representedProperty=" + representedProperty + "," +
              "defaultValue=" + defaultValue + "," +
              "baseChildType=" + baseChildType + "," +
              "key=" + key + "," +
              "isList=" + isList + "," +
              "isArray=" + isArray + "," +
              System.identityHashCode(this) + ")";
              
        }
    }
    
    private static class NameInformation {
        private final Map<String, XmlElementData> nameMapping;
        private final Set<String> noXmlElement;
        
        private NameInformation(Map<String, XmlElementData> nameMapping, Set<String> unmappedNames) {
            this.nameMapping = nameMapping;
            this.noXmlElement = unmappedNames;
        }
        
        private String getNameMap(String mapMe) {
            if (mapMe == null) return null;
            if (!nameMapping.containsKey(mapMe)) return mapMe;
            return nameMapping.get(mapMe).name;
        }
        
        private String getDefaultNameMap(String mapMe) {
            if (mapMe == null) return JAXB_DEFAULT_DEFAULT;
            if (!nameMapping.containsKey(mapMe)) return JAXB_DEFAULT_DEFAULT;
            return nameMapping.get(mapMe).defaultValue;
        }
        
        private boolean hasNoXmlElement(String variableName) {
            if (variableName == null) return true;
            return noXmlElement.contains(variableName);
        }
    }
    
    private static class XmlElementData {
        private final String name;
        private final String defaultValue;
        
        private XmlElementData(String name, String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }
    }
    
    private static enum MethodType {
        GETTER,
        SETTER,
        LOOKUP,
        ADD,
        REMOVE,
        CUSTOM
    }
}
