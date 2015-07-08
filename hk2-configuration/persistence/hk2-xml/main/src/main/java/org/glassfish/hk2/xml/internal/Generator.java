/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.glassfish.hk2.api.AnnotationLiteral;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.xml.api.annotations.Customize;
import org.glassfish.hk2.xml.api.annotations.DefaultChild;
import org.glassfish.hk2.xml.api.annotations.Hk2XmlPreGenerate;
import org.glassfish.hk2.xml.api.annotations.PluralOf;
import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;
import org.glassfish.hk2.xml.internal.alt.AltAnnotation;
import org.glassfish.hk2.xml.internal.alt.AltClass;
import org.glassfish.hk2.xml.internal.alt.AltEnum;
import org.glassfish.hk2.xml.internal.alt.AltMethod;
import org.glassfish.hk2.xml.internal.alt.clazz.AnnotationAltAnnotationImpl;
import org.glassfish.hk2.xml.internal.alt.clazz.ClassAltClassImpl;
import org.glassfish.hk2.xml.jaxb.internal.XmlElementImpl;
import org.glassfish.hk2.xml.jaxb.internal.XmlRootElementImpl;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
public class Generator {
    private final static boolean DEBUG_METHODS = Boolean.parseBoolean(GeneralUtilities.getSystemProperty(
            "org.jvnet.hk2.properties.xmlservice.jaxb.methods", "false"));
    
    
    private final static String JAXB_DEFAULT_STRING = "##default";
    public final static String JAXB_DEFAULT_DEFAULT = "\u0000";
    private final static String NO_CHILD_PACKAGE = "java.";
    public final static String STATIC_GET_MODEL_METHOD_NAME = "__getModel";
    
    private final static Set<String> NO_COPY_ANNOTATIONS = new HashSet<String>(Arrays.asList(new String[] {
            Contract.class.getName(),
            XmlTransient.class.getName(),
            Hk2XmlPreGenerate.class.getName()
    }));
    
    /**
     * Converts the given interface into a JAXB implementation proxy
     * 
     * @param convertMe
     * @param superClazz
     * @param defaultClassPool
     * @return
     * @throws Throwable
     */
    public static CtClass generate(AltClass convertMe,
            CtClass superClazz,
            ClassPool defaultClassPool) throws Throwable {
        String modelOriginalInterface = convertMe.getName();
        
        String modelTranslatedClass = Utilities.getProxyNameFromInterfaceName(modelOriginalInterface);
        if (DEBUG_METHODS) {
            Logger.getLogger().debug("Converting " + convertMe.getName() + " to " + modelTranslatedClass);
        }
        
        CtClass targetCtClass = defaultClassPool.makeClass(modelTranslatedClass);
        ClassFile targetClassFile = targetCtClass.getClassFile();
        targetClassFile.setVersionToJava5();
        ConstPool targetConstPool = targetClassFile.getConstPool();
        
        Model compiledModel = new Model(modelOriginalInterface, modelTranslatedClass);
        AnnotationsAttribute ctAnnotations = null;
        for (AltAnnotation convertMeAnnotation : convertMe.getAnnotations()) {
            if (NO_COPY_ANNOTATIONS.contains(convertMeAnnotation.annotationType())) {
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
            
            if (XmlRootElement.class.getName().equals(convertMeAnnotation.annotationType())) {
                String modelRootName = convertXmlRootElementName(convertMeAnnotation, convertMe);
                compiledModel.setRootName(modelRootName);
                
                XmlRootElement replacement = new XmlRootElementImpl(convertMeAnnotation.getStringValue("namespace"), modelRootName);
               
                createAnnotationCopy(targetConstPool, replacement, ctAnnotations);
            }
            else {
                createAnnotationCopy(targetConstPool, convertMeAnnotation, ctAnnotations);
            }
        }
        if (ctAnnotations != null) {
            targetClassFile.addAttribute(ctAnnotations);
        }
        
        CtClass originalCtClass = defaultClassPool.getOrNull(convertMe.getName());
        if (originalCtClass == null) {
            originalCtClass = defaultClassPool.makeInterface(convertMe.getName());
        }
        
        targetCtClass.setSuperclass(superClazz);
        targetCtClass.addInterface(originalCtClass);
        
        NameInformation xmlNameMap = getXmlNameMap(convertMe);
        HashSet<String> alreadyAddedNaked = new HashSet<String>();
        
        List<AltMethod> allMethods = convertMe.getMethods();
        if (DEBUG_METHODS) {
            Logger.getLogger().debug("Analyzing " + allMethods.size() + " methods of " + convertMe.getName());
        }
        
        HashSet<String> setters = new HashSet<String>();
        HashMap<String, MethodInformation> getters = new HashMap<String, MethodInformation>();
        for (AltMethod wrapper : allMethods) {
            MethodInformation mi = getMethodInformation(wrapper, xmlNameMap);
            if (mi.isKey()) {
                compiledModel.setKeyProperty(mi.getRepresentedProperty());
            }
            
            if (!MethodType.CUSTOM.equals(mi.getMethodType())) {
                createInterfaceForAltClassIfNeeded(mi.getGetterSetterType(), defaultClassPool);
            }
            else {
                // Custom methods can have all sorts of missing types, need to do all return and param types
                AltMethod original = mi.getOriginalMethod();
                AltClass originalReturn = original.getReturnType();
                if (!ClassAltClassImpl.VOID.equals(originalReturn)) {
                    createInterfaceForAltClassIfNeeded(originalReturn, defaultClassPool);
                }
                
                for (AltClass parameter : original.getParameterTypes()) {
                    createInterfaceForAltClassIfNeeded(parameter, defaultClassPool);
                }
                
            }
            
            if (DEBUG_METHODS) {
                Logger.getLogger().debug("Analyzing method " + mi + " of " + convertMe.getSimpleName());
            }
            
            String name = wrapper.getName();
            
            StringBuffer sb = new StringBuffer("public ");
            
            AltClass originalRetType = wrapper.getReturnType();
            boolean isVoid;
            if (originalRetType == null || void.class.getName().equals(originalRetType.getName())) {
                sb.append("void ");
                isVoid = true;
            }
            else {
                sb.append(getCompilableClass(originalRetType) + " ");
                isVoid = false;
            }
            
            sb.append(name + "(");
            
            AltClass childType = null;
            boolean getterOrSetter = false;
            if (MethodType.SETTER.equals(mi.getMethodType())) {
                getterOrSetter = true;
                setters.add(mi.getRepresentedProperty());
                
                childType = mi.getBaseChildType();
                
                sb.append(getCompilableClass(mi.getGetterSetterType()) + " arg0) { super._setProperty(\"" + mi.getRepresentedProperty() + "\", arg0); }");
            }
            else if (MethodType.GETTER.equals(mi.getMethodType())) {
                getterOrSetter = true;
                getters.put(mi.getRepresentedProperty(), mi);
                
                childType = mi.getBaseChildType();
                
                String cast = "";
                String superMethodName = "_getProperty";
                if (int.class.getName().equals(mi.getGetterSetterType().getName())) {
                    superMethodName += "I"; 
                }
                else if (long.class.getName().equals(mi.getGetterSetterType().getName())) {
                    superMethodName += "J";
                }
                else if (boolean.class.getName().equals(mi.getGetterSetterType().getName())) {
                    superMethodName += "Z";
                }
                else if (byte.class.getName().equals(mi.getGetterSetterType().getName())) {
                    superMethodName += "B";
                }
                else if (char.class.getName().equals(mi.getGetterSetterType().getName())) {
                    superMethodName += "C";
                }
                else if (short.class.getName().equals(mi.getGetterSetterType().getName())) {
                    superMethodName += "S";
                }
                else if (float.class.getName().equals(mi.getGetterSetterType().getName())) {
                    superMethodName += "F";
                }
                else if (double.class.getName().equals(mi.getGetterSetterType().getName())) {
                    superMethodName += "D";
                }
                else {
                    cast = "(" + getCompilableClass(mi.getGetterSetterType()) + ") ";
                }
                
                sb.append(") { return " + cast + "super." + superMethodName + "(\"" + mi.getRepresentedProperty() + "\"); }");
            }
            else if (MethodType.LOOKUP.equals(mi.getMethodType())) {
                sb.append("java.lang.String arg0) { return (" + getCompilableClass(originalRetType) +
                        ") super._lookupChild(\"" + mi.getRepresentedProperty() + "\", arg0); }");
                
            }
            else if (MethodType.ADD.equals(mi.getMethodType())) {
                String returnClause = "";
                if (!isVoid) {
                    createInterfaceForAltClassIfNeeded(originalRetType, defaultClassPool);
                    
                    returnClause = "return (" + getCompilableClass(originalRetType) + ") ";
                }
                
                List<AltClass> paramTypes = wrapper.getParameterTypes();
                if (paramTypes.size() == 0) {
                    sb.append(") { " + returnClause + "super._doAdd(\"" + mi.getRepresentedProperty() + "\", null, null, -1); }");
                }
                else if (paramTypes.size() == 1) {
                    sb.append(paramTypes.get(0).getName() + " arg0) { " + returnClause + "super._doAdd(\"" + mi.getRepresentedProperty() + "\",");
                    
                    if (paramTypes.get(0).isInterface()) {
                        sb.append("arg0, null, -1); }");
                    }
                    else if (String.class.getName().equals(paramTypes.get(0).getName())) {
                        sb.append("null, arg0, -1); }");
                    }
                    else {
                        sb.append("null, null, arg0); }");
                    }
                }
                else {
                    sb.append(paramTypes.get(0).getName() + " arg0, int arg1) { " + returnClause + "super._doAdd(\"" + mi.getRepresentedProperty() + "\",");
                    
                    if (paramTypes.get(0).isInterface()) {
                        sb.append("arg0, null, arg1); }");
                    }
                    else {
                        sb.append("null, arg0, arg1); }");
                    }
                }
            }
            else if (MethodType.REMOVE.equals(mi.getMethodType())) {
                List<AltClass> paramTypes = wrapper.getParameterTypes();
                String cast = "";
                String function = "super._doRemoveZ(\"";
                if (!boolean.class.getName().equals(originalRetType.getName())) {
                    cast = "(" + getCompilableClass(originalRetType) + ") ";
                    function = "super._doRemove(\"";
                }
                
                if (paramTypes.size() == 0) {
                    sb.append(") { return " + cast + function +
                            mi.getRepresentedProperty() + "\", null, -1); }");
                }
                else if (String.class.getName().equals(paramTypes.get(0).getName())) {
                    sb.append("java.lang.String arg0) { return " + cast  + function +
                            mi.getRepresentedProperty() + "\", arg0, -1); }");
                }
                else {
                    sb.append("int arg0) { return " + cast + function +
                            mi.getRepresentedProperty() + "\", null, arg0); }");
                }
            }
            else if (MethodType.CUSTOM.equals(mi.getMethodType())) {
                List<AltClass> paramTypes = wrapper.getParameterTypes();
                
                StringBuffer classSets = new StringBuffer();
                StringBuffer valSets = new StringBuffer();
                
                int lcv = 0;
                for (AltClass paramType : paramTypes) {
                    createInterfaceForAltClassIfNeeded(paramType, defaultClassPool);
                    
                    if (lcv == 0) {
                        sb.append(getCompilableClass(paramType) + " arg" + lcv);
                    }
                    else {
                        sb.append(", " + getCompilableClass(paramType) + " arg" + lcv);
                    }
                    
                    classSets.append("mParams[" + lcv + "] = " + getCompilableClass(paramType) + ".class;\n");
                    valSets.append("mVars[" + lcv + "] = ");
                    if (int.class.getName().equals(paramType.getName())) {
                        valSets.append("new java.lang.Integer(arg" + lcv + ");\n");
                    }
                    else if (long.class.getName().equals(paramType.getName())) {
                        valSets.append("new java.lang.Long(arg" + lcv + ");\n");
                    }
                    else if (boolean.class.getName().equals(paramType.getName())) {
                        valSets.append("new java.lang.Boolean(arg" + lcv + ");\n");
                    }
                    else if (byte.class.getName().equals(paramType.getName())) {
                        valSets.append("new java.lang.Byte(arg" + lcv + ");\n");
                    }
                    else if (char.class.getName().equals(paramType.getName())) {
                        valSets.append("new java.lang.Character(arg" + lcv + ");\n");
                    }
                    else if (short.class.getName().equals(paramType.getName())) {
                        valSets.append("new java.lang.Short(arg" + lcv + ");\n");
                    }
                    else if (float.class.getName().equals(paramType.getName())) {
                        valSets.append("new java.lang.Float(arg" + lcv + ");\n");
                    }
                    else if (double.class.getName().equals(paramType.getName())) {
                        valSets.append("new java.lang.Double(arg" + lcv + ");\n");
                    }
                    else {
                        valSets.append("arg" + lcv + ";\n");
                    }
                    
                    lcv++;
                }
                
                sb.append(") { Class[] mParams = new Class[" + paramTypes.size() + "];\n");
                sb.append("Object[] mVars = new Object[" + paramTypes.size() + "];\n");
                sb.append(classSets.toString());
                sb.append(valSets.toString());
                
                String cast = "";
                String superMethodName = "_invokeCustomizedMethod";
                if (int.class.getName().equals(originalRetType.getName())) {
                    superMethodName += "I"; 
                }
                else if (long.class.getName().equals(originalRetType.getName())) {
                    superMethodName += "J";
                }
                else if (boolean.class.getName().equals(originalRetType.getName())) {
                    superMethodName += "Z";
                }
                else if (byte.class.getName().equals(originalRetType.getName())) {
                    superMethodName += "B";
                }
                else if (char.class.getName().equals(originalRetType.getName())) {
                    superMethodName += "C";
                }
                else if (short.class.getName().equals(originalRetType.getName())) {
                    superMethodName += "S";
                }
                else if (float.class.getName().equals(originalRetType.getName())) {
                    superMethodName += "F";
                }
                else if (double.class.getName().equals(originalRetType.getName())) {
                    superMethodName += "D";
                }
                else if (!isVoid) {
                    cast = "(" + getCompilableClass(originalRetType) + ") ";
                }
                
                if (!isVoid) {
                    sb.append("return " + cast);
                }
                sb.append("super." + superMethodName + "(\"" + name + "\", mParams, mVars);}");
            }
            
            if (DEBUG_METHODS) {
                // Hidden behind static because of potential expensive toString costs
                Logger.getLogger().debug("Adding method for " + convertMe.getSimpleName() + " with implementation " + sb);
            }
            
            CtMethod addMeCtMethod;
            try {
                addMeCtMethod = CtNewMethod.make(sb.toString(), targetCtClass);
            }
            catch (CannotCompileException cce) {
                MultiException me = new MultiException(cce);
                me.addError(new AssertionError("Cannot compile method with source " + sb.toString()));
                throw me;
            }
            if (wrapper.isVarArgs()) {
                addMeCtMethod.setModifiers(addMeCtMethod.getModifiers() | Modifier.VARARGS);
            }
            MethodInfo methodInfo = addMeCtMethod.getMethodInfo();
            ConstPool methodConstPool = methodInfo.getConstPool();
           
            ctAnnotations = null;
            for (AltAnnotation convertMeAnnotation : wrapper.getAnnotations()) {
                if (ctAnnotations == null) {
                    ctAnnotations = new AnnotationsAttribute(methodConstPool, AnnotationsAttribute.visibleTag);
                }
                
                if ((childType != null) && XmlElement.class.getName().equals(convertMeAnnotation.annotationType())) {
                        
                    String translatedClassName = Utilities.getProxyNameFromInterfaceName(childType.getName());
                    java.lang.annotation.Annotation anno = new XmlElementImpl(
                            convertMeAnnotation.getStringValue("name"),
                            convertMeAnnotation.getBooleanValue("nillable"),
                            convertMeAnnotation.getBooleanValue("required"),
                            convertMeAnnotation.getStringValue("namespace"),
                            convertMeAnnotation.getStringValue("defaultValue"),
                            translatedClassName);
                    
                    createAnnotationCopy(methodConstPool, anno, ctAnnotations);
                }
                else {  
                    createAnnotationCopy(methodConstPool, convertMeAnnotation, ctAnnotations);
                }
            }
            
            if (getterOrSetter) {
                if (childType != null) {
                    Map<String, String> defaultChild = null;
                    AltAnnotation defaultChildAnnotation= mi.getOriginalMethod().getAnnotation(DefaultChild.class.getName());
                    if (defaultChildAnnotation != null) {
                        String[] defaultStrings = defaultChildAnnotation.getStringArrayValue("value");
                        
                        defaultChild = convertDefaultChildValueArray(defaultStrings);
                    }
                    
                    compiledModel.addChild(mi.getDecapitalizedMethodProperty(),
                            childType.getName(),
                            mi.getRepresentedProperty(),
                            getChildType(mi.isList(), mi.isArray()),
                            mi.getDefaultValue(),
                            defaultChild);
                }
                else {
                    compiledModel.addNonChild(mi.getRepresentedProperty(), mi.getDefaultValue(),
                            mi.getGetterSetterType().getName());
                }
            }
            
            if (getterOrSetter && childType != null &&
                    xmlNameMap.hasNoXmlElement(mi.getRepresentedProperty()) &&
                    !alreadyAddedNaked.contains(mi.getRepresentedProperty())) {
                alreadyAddedNaked.add(mi.getRepresentedProperty());
                if (ctAnnotations == null) {
                    ctAnnotations = new AnnotationsAttribute(methodConstPool, AnnotationsAttribute.visibleTag);
                }
                
                java.lang.annotation.Annotation convertMeAnnotation;
                String translatedClassName = Utilities.getProxyNameFromInterfaceName(childType.getName());
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
            
            String getterName = mi.getOriginalMethod().getName();
            String setterName = Utilities.convertToSetter(getterName);
            
            StringBuffer sb = new StringBuffer("private void " + setterName + "(");
            sb.append(getCompilableClass(mi.getGetterSetterType()) + " arg0) { super._setProperty(\"" + mi.getRepresentedProperty() + "\", arg0); }");
            
            CtMethod addMeCtMethod = CtNewMethod.make(sb.toString(), targetCtClass);
            targetCtClass.addMethod(addMeCtMethod);
        }
        
        generateStaticModelFieldAndAbstractMethodImpl(targetCtClass,
                compiledModel,
                defaultClassPool);
        
        return targetCtClass;
    }
    
    /* package */ static ChildType getChildType(boolean isList, boolean isArray) {
        if (isList) return ChildType.LIST;
        if (isArray) return ChildType.ARRAY;
        return ChildType.DIRECT;
    }
    
    private static void generateStaticModelFieldAndAbstractMethodImpl(CtClass targetCtClass,
            Model model,
            ClassPool defaultClassPool) throws CannotCompileException, NotFoundException {
        StringBuffer sb = new StringBuffer();
        
        sb.append("private static final org.glassfish.hk2.xml.internal.Model INIT_MODEL() {\n");
        sb.append("org.glassfish.hk2.xml.internal.Model retVal = new org.glassfish.hk2.xml.internal.Model(\"");
        
        sb.append(model.getOriginalInterface() + "\",\"" + model.getTranslatedClass() + "\");\n");
        
        if (model.getRootName() != null) {
            sb.append("retVal.setRootName(\"" + model.getRootName() + "\");\n");
        }
        
        if (model.getKeyProperty() != null) {
            sb.append("retVal.setKeyProperty(\"" + model.getKeyProperty() + "\");\n");
        }
        
        Map<String, ParentedModel> childrenByName = model.getChildrenByName();
        for (Map.Entry<String, ParentedModel> entry : childrenByName.entrySet()) {
            // TODO: Generate a Map for defaultChild strings
            
            sb.append("retVal.addChild(" +
              asParameter(entry.getKey()) + "," +
              asParameter(entry.getValue().getChildInterface()) + "," +
              asParameter(entry.getValue().getChildXmlTag()) + "," +
              asParameter(entry.getValue().getChildType()) + "," +
              asParameter(entry.getValue().getGivenDefault()) + "," +
              asParameter((String) null) + ");\n");
        }
        
        Map<String, ChildDataModel> nonChildProperties = model.getNonChildProperties();
        for (Map.Entry<String, ChildDataModel> entry : nonChildProperties.entrySet()) {
            sb.append("retVal.addNonChild(" +
              asParameter(entry.getKey()) + "," +
              asParameter(entry.getValue().getDefaultAsString()) + "," +
              asParameter(entry.getValue().getChildType()) + ");\n");
        }
        
        sb.append("return retVal; }");
        
        targetCtClass.addMethod(CtNewMethod.make(sb.toString(), targetCtClass));
        
        CtClass modelCt = defaultClassPool.get(Model.class.getName());
        
        CtField sField = new CtField(modelCt, "MODEL", targetCtClass);
        sField.setModifiers(Modifier.STATIC | Modifier.FINAL | Modifier.PRIVATE);
        
        targetCtClass.addField(sField, CtField.Initializer.byCall(targetCtClass, "INIT_MODEL"));
        
        CtMethod aMethod = CtNewMethod.make(
                "public org.glassfish.hk2.xml.internal.Model _getModel() { return MODEL; }" , targetCtClass);
        
        targetCtClass.addMethod(aMethod);
        
        CtMethod sMethod = CtNewMethod.make(
                "public static final org.glassfish.hk2.xml.internal.Model __getModel() { return MODEL; }" , targetCtClass);
        
        targetCtClass.addMethod(sMethod);
    }
    
    private static String asParameter(String me) {
        if (me == null) return "null";
        return "\"" + me + "\"";
    }
    
    private static String asParameter(ChildType ct) {
        switch(ct) {
        case DIRECT:
            return ChildType.class.getName() + ".DIRECT";
        case LIST:
            return ChildType.class.getName() + ".LIST";
        case ARRAY:
            return ChildType.class.getName() + ".ARRAY";
        default:
            throw new AssertionError("unknown ChildType " + ct);
        }
    }
    
    private static void createAnnotationCopy(ConstPool parent, java.lang.annotation.Annotation javaAnnotation,
            AnnotationsAttribute retVal) throws Throwable {
        createAnnotationCopy(parent, new AnnotationAltAnnotationImpl(javaAnnotation, null), retVal);
    }
    
    private static void createAnnotationCopy(ConstPool parent, AltAnnotation javaAnnotation,
            AnnotationsAttribute retVal) throws Throwable {
        Annotation annotation = new Annotation(javaAnnotation.annotationType(), parent);
        
        Map<String, Object> annotationValues = javaAnnotation.getAnnotationValues();
        for (Map.Entry<String, Object> entry : annotationValues.entrySet()) {
            String valueName = entry.getKey();
            Object value = entry.getValue();
            
            Class<?> javaAnnotationType = value.getClass();
            if (String.class.equals(javaAnnotationType)) {
                annotation.addMemberValue(valueName, new StringMemberValue((String) value, parent));
            }
            else if (Boolean.class.equals(javaAnnotationType)) {
                boolean bvalue = (Boolean) value;
                
                annotation.addMemberValue(valueName, new BooleanMemberValue(bvalue, parent));
            }
            else if (AltClass.class.isAssignableFrom(javaAnnotationType)) {
                AltClass altJavaAnnotationType = (AltClass) value;
                
                String sValue;
                if (javaAnnotation.annotationType().equals(XmlElement.class.getName()) &&
                        (javaAnnotation.getStringValue("getTypeByName") != null)) {
                    sValue = javaAnnotation.getStringValue("getTypeByName");
                }
                else {
                    sValue = altJavaAnnotationType.getName();
                }
                
                annotation.addMemberValue(valueName, new ClassMemberValue(sValue, parent));
            }
            else if (Integer.class.equals(javaAnnotationType)) {
                int ivalue = (Integer) value;
                
                annotation.addMemberValue(valueName, new IntegerMemberValue(parent, ivalue));
            }
            else if (Long.class.equals(javaAnnotationType)) {
                long lvalue = (Long) value;
                
                annotation.addMemberValue(valueName, new LongMemberValue(lvalue, parent));
            }
            else if (Double.class.equals(javaAnnotationType)) {
                double dvalue = (Double) value;
                
                annotation.addMemberValue(valueName, new DoubleMemberValue(dvalue, parent));
            }
            else if (Byte.class.equals(javaAnnotationType)) {
                byte bvalue = (Byte) value;
                
                annotation.addMemberValue(valueName, new ByteMemberValue(bvalue, parent));
            }
            else if (Character.class.equals(javaAnnotationType)) {
                char cvalue = (Character) value;
                
                annotation.addMemberValue(valueName, new CharMemberValue(cvalue, parent));
            }
            else if (Short.class.equals(javaAnnotationType)) {
                short svalue = (Short) value;
                
                annotation.addMemberValue(valueName, new ShortMemberValue(svalue, parent));
            }
            else if (Float.class.equals(javaAnnotationType)) {
                float fvalue = (Float) value;
                
                annotation.addMemberValue(valueName, new FloatMemberValue(fvalue, parent));
            }
            else if (AltEnum.class.isAssignableFrom(javaAnnotationType)) {
                AltEnum evalue = (AltEnum) value;
                
                EnumMemberValue jaEnum = new EnumMemberValue(parent);
                jaEnum.setType(evalue.getDeclaringClass());
                jaEnum.setValue(evalue.getName());
                    
                annotation.addMemberValue(valueName, jaEnum);
            }
            else if (javaAnnotationType.isArray()) {
                Class<?> typeOfArray = javaAnnotationType.getComponentType();
                
                MemberValue arrayValue[];
                if (int.class.equals(typeOfArray)) {
                    int[] iVals = (int[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new IntegerMemberValue(parent, iVals[lcv]);
                    }
                }
                else if (String.class.equals(typeOfArray)) {
                    String[] iVals = (String[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new StringMemberValue(iVals[lcv], parent);
                    }
                }
                else if (long.class.equals(typeOfArray)) {
                    long[] iVals = (long[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new LongMemberValue(iVals[lcv], parent);
                    }
                }
                else if (boolean.class.equals(typeOfArray)) {
                    boolean[] iVals = (boolean[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new BooleanMemberValue(iVals[lcv], parent);
                    }
                }
                else if (float.class.equals(typeOfArray)) {
                    float[] iVals = (float[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new FloatMemberValue(iVals[lcv], parent);
                    }
                }
                else if (double.class.equals(typeOfArray)) {
                    double[] iVals = (double[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new DoubleMemberValue(iVals[lcv], parent);
                    }
                }
                else if (byte.class.equals(typeOfArray)) {
                    byte[] iVals = (byte[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new ByteMemberValue(iVals[lcv], parent);
                    }
                }
                else if (char.class.equals(typeOfArray)) {
                    char[] iVals = (char[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new CharMemberValue(iVals[lcv], parent);
                    }
                }
                else if (short.class.equals(typeOfArray)) {
                    short[] iVals = (short[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        arrayValue[lcv] = new ShortMemberValue(iVals[lcv], parent);
                    }
                }
                else if (AltEnum.class.isAssignableFrom(typeOfArray)) {
                    AltEnum[] iVals = (AltEnum[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        EnumMemberValue jaEnum = new EnumMemberValue(parent);
                        jaEnum.setType(iVals[lcv].getDeclaringClass());
                        jaEnum.setValue(iVals[lcv].getName());
                        
                        arrayValue[lcv] = jaEnum;
                    }
                }
                else if (AltClass.class.isAssignableFrom(typeOfArray)) {
                    AltClass[] iVals = (AltClass[]) value;
                    
                    arrayValue = new MemberValue[iVals.length];
                    for (int lcv = 0; lcv < iVals.length; lcv++) {
                        AltClass arrayElementClass = iVals[lcv];
                        
                        arrayValue[lcv] = new ClassMemberValue(arrayElementClass.getName(), parent);
                    }
                }
                else {
                    throw new AssertionError("Array type " + typeOfArray.getName() + " is not yet implemented for " + valueName);
                }
                
                ArrayMemberValue arrayMemberValue = new ArrayMemberValue(parent);
                arrayMemberValue.setValue(arrayValue);
                
                annotation.addMemberValue(valueName, arrayMemberValue);
                
            }
            else {
                throw new AssertionError("Annotation type " + javaAnnotationType.getName() + " is not yet implemented for " + valueName);
            }
            
        }
        
        retVal.addAnnotation(annotation);
    }
    
    private static String getMethodName(MethodType methodType, String unDecapitalizedVariable, AltAnnotation instructions) {
        String retVal;
        
        switch (methodType) {
            case ADD:
                retVal = instructions.getStringValue("add");
                break;
            case REMOVE:
                retVal = instructions.getStringValue("remove");
                break;
            case LOOKUP:
                retVal = instructions.getStringValue("lookup");
                break;
            default:
                throw new AssertionError("Only ADD, REMOVE and LOOKUP supported");
        }
        
        if (!PluralOf.USE_NORMAL_PLURAL_PATTERN.equals(retVal)) {
            // We got the specific name for the method, overrides any algorithm
            return retVal;
        }
        
        String pluralOf = instructions.getStringValue("value");
        if (!PluralOf.USE_NORMAL_PLURAL_PATTERN.equals(pluralOf)) {
            // We got a specific name for the singular, use it
            switch (methodType) {
            case ADD:
                return JAUtilities.ADD + pluralOf;
            case REMOVE:
                return JAUtilities.REMOVE + pluralOf;
            case LOOKUP:
                return JAUtilities.LOOKUP + pluralOf;
            default:
                throw new AssertionError("Only add, remove and lookup supported");
            }
        }
        
        // Do the algorithm
        if (unDecapitalizedVariable.endsWith("s")) {
            unDecapitalizedVariable = unDecapitalizedVariable.substring(0, unDecapitalizedVariable.length() - 1);
        }
        
        switch (methodType) {
        case ADD:
            return JAUtilities.ADD + unDecapitalizedVariable;
        case REMOVE:
            return JAUtilities.REMOVE + unDecapitalizedVariable;
        case LOOKUP:
            return JAUtilities.LOOKUP + unDecapitalizedVariable;
        default:
            throw new AssertionError("Only add, remove and lookup supported");
        }
        
    }
    
    /* package */ static NameInformation getXmlNameMap(AltClass convertMe) {
        Map<String, XmlElementData> xmlNameMap = new HashMap<String, XmlElementData>();
        HashSet<String> unmappedNames = new HashSet<String>();
        Map<String, String> addMethodToVariableMap = new HashMap<String, String>();
        Map<String, String> removeMethodToVariableMap = new HashMap<String, String>();
        Map<String, String> lookupMethodToVariableMap = new HashMap<String, String>();
        
        for (AltMethod originalMethod : convertMe.getMethods()) {
            String setterVariable = isSetter(originalMethod);
            if (setterVariable == null) {
                setterVariable = isGetter(originalMethod);
                if (setterVariable == null) continue;
            }
            
            AltAnnotation pluralOf = null;
            AltAnnotation xmlElement = originalMethod.getAnnotation(XmlElement.class.getName());
            if (xmlElement != null) {
                // Get the pluralOf from the method
                pluralOf = originalMethod.getAnnotation(PluralOf.class.getName());
                
                String defaultValue = xmlElement.getStringValue("defaultValue");
                
                if (JAXB_DEFAULT_STRING.equals(xmlElement.getStringValue("name"))) {
                    xmlNameMap.put(setterVariable, new XmlElementData(setterVariable, defaultValue));
                }
                else {
                    xmlNameMap.put(setterVariable, new XmlElementData(xmlElement.getStringValue("name"), defaultValue));
                }
            }
            else {
                AltAnnotation xmlAttribute = originalMethod.getAnnotation(XmlAttribute.class.getName());
                if (xmlAttribute != null) {
                    if (JAXB_DEFAULT_STRING.equals(xmlAttribute.getStringValue("name"))) {
                        xmlNameMap.put(setterVariable, new XmlElementData(setterVariable, JAXB_DEFAULT_DEFAULT));
                    }
                    else {
                        xmlNameMap.put(setterVariable, new XmlElementData(xmlAttribute.getStringValue("name"), JAXB_DEFAULT_DEFAULT));
                    }
                }
                else {
                    unmappedNames.add(setterVariable);
                }
            }
            
            if (pluralOf == null) pluralOf = new AnnotationAltAnnotationImpl(new PluralOfDefault(), null);
            
            String unDecapitalizedVariable = originalMethod.getName().substring(3);
            
            addMethodToVariableMap.put(getMethodName(MethodType.ADD, unDecapitalizedVariable, pluralOf), setterVariable);
            removeMethodToVariableMap.put(getMethodName(MethodType.REMOVE, unDecapitalizedVariable, pluralOf), setterVariable);
            lookupMethodToVariableMap.put(getMethodName(MethodType.LOOKUP, unDecapitalizedVariable, pluralOf), setterVariable);
        }
        
        Set<String> noXmlElementNames = new HashSet<String>();
        for (String unmappedName : unmappedNames) {
            if (!xmlNameMap.containsKey(unmappedName)) {
                noXmlElementNames.add(unmappedName);
            }
        }
        
        return new NameInformation(xmlNameMap, noXmlElementNames,
                addMethodToVariableMap,
                removeMethodToVariableMap,
                lookupMethodToVariableMap);
    }
    
    /* package */ static MethodInformation getMethodInformation(AltMethod m, NameInformation xmlNameMap) {
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
            else if (returnType.isInterface() && !returnType.getName().startsWith(NO_CHILD_PACKAGE)) {
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
            else if (setterType.isInterface() && !setterType.getName().startsWith(NO_CHILD_PACKAGE)) {
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
        
        return new MethodInformation(m,
                methodType,
                variable,
                representedProperty,
                defaultValue,
                baseChildType,
                gsType,
                key,
                isList,
                isArray);
    }
    
    private static String convertXmlRootElementName(AltAnnotation root, AltClass clazz) {
        String rootName = root.getStringValue("name");
        
        if (!"##default".equals(rootName)) return rootName;
        
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
    
    /* package */ static String isGetter(AltMethod method) {
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
    
    private static boolean isSpecifiedCustom(AltMethod method) {
        AltAnnotation customAnnotation = method.getAnnotation(Customize.class.getName());
        return (customAnnotation != null);
        
    }
    
    private static String isSetter(AltMethod method) {
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
        
        if (method.getReturnType() == null || void.class.getName().equals(method.getReturnType().getName())) return null;
        
        AltClass returnType = method.getReturnType();
        if (!boolean.class.getName().equals(returnType.getName()) && !returnType.isInterface()) return null;
        
        List<AltClass> parameterTypes = method.getParameterTypes();
        if (parameterTypes.size() > 1) return null;
        
        if (parameterTypes.size() == 0) return retVal;
        
        AltClass param0 = parameterTypes.get(0);
        
        if (String.class.getName().equals(param0.getName()) ||
                int.class.getName().equals(param0.getName())) return retVal;
        return null;
    }
    
    private static String getCompilableClass(AltClass clazz) {
        int depth = 0;
        while (clazz.isArray()) {
            depth++;
            clazz = clazz.getComponentType();
        }
        
        StringBuffer sb = new StringBuffer(clazz.getName());
        for (int lcv = 0; lcv < depth; lcv++) {
            sb.append("[]");
        }
        
        return sb.toString();
    }
    
    private static AltClass getUltimateNonArrayClass(AltClass clazz) {
        if (clazz == null) return null;
        
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        
        return clazz;
    }
    
    private static void createInterfaceForAltClassIfNeeded(AltClass toFix, ClassPool defaultClassPool) {
        if (toFix == null) return;
        toFix = getUltimateNonArrayClass(toFix);
        
        String fixerClass = toFix.getName();
        if (defaultClassPool.getOrNull(fixerClass) == null) {
            defaultClassPool.makeInterface(fixerClass);
        }
    }
    
    private static Map<String, String> convertDefaultChildValueArray(String[] values) {
        LinkedHashMap<String, String> retVal = new LinkedHashMap<String, String>();
        if (values == null) return retVal;
        for (String value : values) {
            value = value.trim();
            if ("".equals(value)) continue;
            if (value.charAt(0) == '=') {
                throw new AssertionError("First character of " + value + " may not be an =");
            }
            
            int indexOfEquals = value.indexOf('=');
            if (indexOfEquals < 0) {
                retVal.put(value, null);
            }
            else {
                String key = value.substring(0, indexOfEquals);
                
                String attValue;
                if (indexOfEquals >= (value.length() - 1)) {
                    attValue = null;
                }
                else {
                    attValue = value.substring(indexOfEquals + 1);
                }
                
                retVal.put(key, attValue);
            }
        }
        
        return retVal;
    }
    
    private static final class PluralOfDefault extends AnnotationLiteral<PluralOf> implements PluralOf {
        private static final long serialVersionUID = 4358923840720264176L;

        /* (non-Javadoc)
         * @see org.glassfish.hk2.xml.api.annotations.PluralOf#value()
         */
        @Override
        public String value() {
            return PluralOf.USE_NORMAL_PLURAL_PATTERN;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.xml.api.annotations.PluralOf#add()
         */
        @Override
        public String add() {
            return PluralOf.USE_NORMAL_PLURAL_PATTERN;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.xml.api.annotations.PluralOf#remove()
         */
        @Override
        public String remove() {
            return PluralOf.USE_NORMAL_PLURAL_PATTERN;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.xml.api.annotations.PluralOf#lookup()
         */
        @Override
        public String lookup() {
            return PluralOf.USE_NORMAL_PLURAL_PATTERN;
        }
        
    }

}
