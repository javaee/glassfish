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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.jaxb.internal.XmlRootElementImpl;

/**
 * @author jwells
 *
 */
public class JAUtilities {
    private final static String GET = "get";
    private final static String SET = "set";
    private final static String IS = "is";
    
    private final static String CLASS_ADD_ON_NAME = "_$$_Hk2_Jaxb";
    
    private final HashMap<Class<?>, Class<?>> convertedCache = new HashMap<Class<?>, Class<?>>();
    private final ClassPool defaultClassPool = ClassPool.getDefault(); // TODO:  We probably need to be more sophisticated about this
    private final CtClass superClazz;
    
    /* package */ JAUtilities() {
        try {
            superClazz = defaultClassPool.get(BaseHK2JAXBBean.class.getName());
        }
        catch (NotFoundException e) {
            throw new MultiException(e);
        }
        
    }

    private static void brainDump() throws Exception {
        /*
        ProxyFactory pf = new ProxyFactory();
        pf.setSuperclass(BaseHK2JAXBBean.class);
        pf.setInterfaces(new Class[] { Museum.class });
        
        Class<?> mfclass = pf.createClass();
        byte[] mfClassAsBytes = serialize(mfclass);
        
        
        ByteArrayClassPath ccp = new ByteArrayClassPath(mfclass.getName(), mfClassAsBytes);
        System.out.println("JRW(-01) dcp=" + defaultClassPool);
        defaultClassPool.insertClassPath(ccp);
        System.out.println("JRW(01) dcp=" + defaultClassPool);
        
        ClassPool defaultClassPool = ClassPool.getDefault();
        
        CtClass superClazz = defaultClassPool.get(BaseHK2JAXBBean.class.getName());
        // CtClass museumClazz = defaultClassPool.get(Museum.class.getName());
        
        
        // String museumClassName = Museum.class.getName() + "_$$_hk2_jaxb";
        
        // CtClass museumCtClass = defaultClassPool.makeClass(museumClassName);
        // ClassFile mcf = museumCtClass.getClassFile();
        
        // museumCtClass.setSuperclass(superClazz);
        // museumCtClass.addInterface(museumClazz);
        
        Annotation xmlRootElement = new XmlRootElementImpl("##default", "museum");
        // AnnotationsAttribute aa = (AnnotationsAttribute) mcf.getAttribute(AnnotationsAttribute.visibleTag);
        // aa.addAnnotation(xmlRootElement);
        
        // System.out.println("JRW(05) museumCtClass=" + museumCtClass);
        
        {
            CtMethod nameGetMethod =
                CtNewMethod.make("public java.lang.String getName() { return (java.lang.String) super._getProperty(\"name\"); }", museumCtClass);
            
            // nameGetMethod.setAttribute(name, data);
        
            // museumCtClass.addMethod(nameGetMethod);
        
            CtMethod nameSetMethod =
                CtNewMethod.make("public void setName(java.lang.String arg0) { super._setProperty(\"name\", arg0); }", museumCtClass);
        
            // museumCtClass.addMethod(nameSetMethod);
        }
        
        {
            CtMethod nameGetMethod =
                    CtNewMethod.make("public int getId() { java.lang.Integer i = (java.lang.Integer) super._getProperty(\"id\"); return i.intValue(); }", museumCtClass);
            
            // museumCtClass.addMethod(nameGetMethod);
            
            CtMethod nameSetMethod =
                    CtNewMethod.make("public void setId(int arg0) { super._setProperty(\"id\", new java.lang.Integer(arg0)); }", museumCtClass);
            
            // museumCtClass.addMethod(nameSetMethod);
        }
        
        {
            CtMethod nameGetMethod =
                    CtNewMethod.make("public int getAge() { java.lang.Integer i = (java.lang.Integer) super._getProperty(\"age\"); return i.intValue(); }", museumCtClass);
            
            museumCtClass.addMethod(nameGetMethod);
            
            CtMethod nameSetMethod =
                    CtNewMethod.make("public void setAge(int arg0) { super._setProperty(\"age\", new java.lang.Integer(arg0)); }", museumCtClass);
            
            museumCtClass.addMethod(nameSetMethod);
        }
        
        Class<?> proxy = museumCtClass.toClass(Museum.class.getClassLoader(), Museum.class.getProtectionDomain());
        
        System.out.println("JRW(10) who knows? proxy=" + proxy.getName());
        */
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
            catch (Exception e) {
                throw new MultiException(e);
            }
            
            convertedCache.put(convertMe, converted);
        }
        
        return convertedCache.get(root);
    }
    
    private Class<?> convert(Class<?> convertMe) throws Exception {
        CtClass originalCtClass = defaultClassPool.get(convertMe.getName());
        String targetClassName = convertMe.getName() + CLASS_ADD_ON_NAME;
        
        CtClass targetCtClass = defaultClassPool.makeClass(targetClassName);
        ClassFile targetClassFile = targetCtClass.getClassFile();
        
        // TODO:  Add in any class level JAXB annotations here
        
        targetCtClass.setSuperclass(superClazz);
        targetCtClass.addInterface(originalCtClass);
        
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
            
            if (setterVariable != null) {
                Class<?> setterType = originalMethod.getParameterTypes()[0];
                
                // TODO:  Add in any JAXB annotations here
                
                sb.append(setterType.getName() + " arg0) { super._setProperty(\"" + setterVariable + "\", arg0); }");
            }
            else if (getterVariable != null) {
                Class<?> returnType = originalMethod.getReturnType();
                
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
            
            System.out.println("JRW(10) JA sb=" + sb.toString());
            
            CtMethod addMeCtMethod = CtNewMethod.make(sb.toString(), targetCtClass);
            targetCtClass.addMethod(addMeCtMethod);
        }
        
        Class<?> proxy = targetCtClass.toClass(convertMe.getClassLoader(), convertMe.getProtectionDomain());
        
        return proxy;
    }
    
    
    
    private static void getAllToConvert(Class<?> toBeConverted, LinkedHashSet<Class<?>> needsToBeConverted) {
        if (needsToBeConverted.contains(toBeConverted)) return;
        needsToBeConverted.add(toBeConverted);
        
        // Find all the children
        for (Method method : toBeConverted.getMethods()) {
            String methodName = method.getName();
            if (!methodName.startsWith(GET)) continue;
            
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

}
