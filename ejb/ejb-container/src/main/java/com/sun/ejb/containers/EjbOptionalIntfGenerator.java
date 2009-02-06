/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.ejb.containers;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.Serializable;

import com.sun.enterprise.deployment.util.TypeUtil;

public class EjbOptionalIntfGenerator
    extends ClassLoader
    implements Opcodes {

    private static final int INTF_FLAGS = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;

    private static final String DELEGATE_FIELD_NAME = "__ejb31_delegate";

    private Map<String, byte[]> classMap = new HashMap<String, byte[]>();

    private Map<String, Class> loadedClasses = new HashMap<String, Class>()
            ;
    private ClassLoader loader;


    private static final boolean _debug = Boolean.valueOf(System.getProperty("emit.ejb.optional.interface"));

    EjbOptionalIntfGenerator(ClassLoader loader) {
        super(loader);
        this.loader = loader;
    }

    public Class loadClass(String name)
        throws ClassNotFoundException
    {
        byte[] classData = (byte []) classMap.get(name);
        
        if (classData != null) {
            Class clz = loadedClasses.get(name);
            if (clz == null) {
                clz = super.defineClass(name, classData, 0, classData.length);
                loadedClasses.put(name, clz);
            }

            return clz;
        } else {
            return loader.loadClass(name);
        }
    }

    public void generateOptionalLocalInterface(Class ejbClass, String intfClassName)
        throws Exception {

        ClassWriter cw = new ClassWriter(INTF_FLAGS);

//        ClassVisitor tv = (_debug)
//                ? new TraceClassVisitor(cw, new PrintWriter(System.out)) : cw;
        ClassVisitor tv = cw;
        String intfInternalName = intfClassName.replace('.', '/');
        String objectInternalName = Type.getType(Object.class).getInternalName();
        tv.visit(V1_1, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                intfInternalName, null,
                Type.getType(Object.class).getInternalName(), null);

        Set<java.lang.reflect.Method> allMethods = new HashSet<java.lang.reflect.Method>();
        for (Class clz = ejbClass; clz != Object.class; clz = clz.getSuperclass()) {
            java.lang.reflect.Method[] beanMethods = clz.getDeclaredMethods();
            for (java.lang.reflect.Method m : beanMethods) {
                int mod = m.getModifiers();
                if ((Modifier.isPublic(mod)) &&
                        (! Modifier.isStatic(mod)) &&
                        (! Modifier.isAbstract(mod)) &&
                        (! Modifier.isFinal(mod)) ) {
                    if( !hasSameSignatureAsExisting(m, allMethods)) {
                        generateInterfaceMethod(tv, m);
                        allMethods.add(m);
                    }
                }
            }
        }

        tv.visitEnd();

        byte[] classData = cw.toByteArray();
        classMap.put(intfClassName, classData);
    }

    private boolean hasSameSignatureAsExisting(java.lang.reflect.Method toMatch,
                                               Set<java.lang.reflect.Method> methods) {
        boolean sameSignature = false;
        for(java.lang.reflect.Method m : methods) {
            if( TypeUtil.sameMethodSignature(m, toMatch) ) {
                sameSignature = true;
                break;
            }
        }
        return sameSignature;
    }

    public void generateOptionalLocalInterfaceSubClass(Class superClass, String subClassName,
                                                       Class delegateClass)
        throws Exception {

        ClassWriter cw = new ClassWriter(INTF_FLAGS);

//        ClassVisitor tv = (_debug)
//                ? new TraceClassVisitor(cw, new PrintWriter(System.out)) : cw;
        ClassVisitor tv = cw;
        boolean isSuperClassSerializable = superClass.isAssignableFrom(Serializable.class);

        String[] interfaces = null;

        if (Serializable.class.isAssignableFrom(superClass)) {
            interfaces = new String[] {
                OptionalLocalInterfaceProvider.class.getName().replace('.', '/')};
        } else {
            interfaces = new String[] {
                OptionalLocalInterfaceProvider.class.getName().replace('.', '/'),
                Type.getType(Serializable.class).getInternalName()};
        }

        tv.visit(V1_1, ACC_PUBLIC, subClassName.replace('.', '/'), null,
                Type.getType(superClass).getInternalName(), interfaces);

        String fldDesc = Type.getDescriptor(delegateClass);
        FieldVisitor fv = tv.visitField(ACC_PRIVATE, DELEGATE_FIELD_NAME,
                fldDesc, null, null);
        fv.visitEnd();


        Method m = Method.getMethod("void <init> ()");
        GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, tv);
        mg.loadThis();
        mg.invokeConstructor(Type.getType(superClass), m);
        mg.returnValue();
        mg.endMethod();

        generateSetDelegateMethod(tv, delegateClass, subClassName);
        Set<java.lang.reflect.Method> allMethods = new HashSet<java.lang.reflect.Method>();
        
        for (Class clz = superClass; clz != Object.class; clz = clz.getSuperclass()) {
            java.lang.reflect.Method[] beanMethods = clz.getDeclaredMethods();
            for (java.lang.reflect.Method mth : beanMethods) {
                if (Modifier.isPublic(mth.getModifiers())) {
                    if( !hasSameSignatureAsExisting(mth, allMethods)) {
                        generateBeanMethod(tv, subClassName, mth, delegateClass);
                        allMethods.add(mth);
                    }
                }
            }
        }

        tv.visitEnd();

        byte[] classData = cw.toByteArray();
        classMap.put(subClassName, classData);
    }


    private static void generateInterfaceMethod(ClassVisitor cv, java.lang.reflect.Method m)
        throws Exception {

        String methodName = m.getName();
        Type returnType = Type.getReturnType(m);
        Type[] argTypes = Type.getArgumentTypes(m);

        Method asmMethod = new Method(methodName, returnType, argTypes);
        GeneratorAdapter cg = new GeneratorAdapter(ACC_PUBLIC + ACC_ABSTRACT,
                asmMethod, null, getExceptionTypes(m), cv);
        cg.endMethod();

    }

    private static void generateBeanMethod(ClassVisitor cv, String subClassName,
                                           java.lang.reflect.Method m, Class delegateClass)
        throws Exception {

        String methodName = m.getName();
        Type returnType = Type.getReturnType(m);
        Type[] argTypes = Type.getArgumentTypes(m);
        Method asmMethod = new Method(methodName, returnType, argTypes);

        GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, asmMethod, null,
                getExceptionTypes(m), cv);
        mg.loadThis();
        mg.visitFieldInsn(GETFIELD, subClassName.replace('.', '/'),
                DELEGATE_FIELD_NAME, Type.getType(delegateClass).getDescriptor());
        mg.loadArgs();
        mg.invokeInterface(Type.getType(delegateClass), asmMethod);
        mg.returnValue();
        mg.endMethod();

    }

    private static Type[] getExceptionTypes(java.lang.reflect.Method m) {
        Class[] exceptions = m.getExceptionTypes();
        Type[] eTypes = new Type[exceptions.length];
        for (int i=0; i<exceptions.length; i++) {
            eTypes[i] = Type.getType(exceptions[i]);
        }

        return eTypes;
    }

    private static void generateSetDelegateMethod(ClassVisitor cv, Class delegateClass,
                                                  String subClassName)
        throws Exception {

        String delegateInternalName = Type.getType(delegateClass).getInternalName();
        Class optProxyClass = OptionalLocalInterfaceProvider.class;
        java.lang.reflect.Method proxyMethod = optProxyClass.getMethod(
                "setOptionalLocalIntfProxy", java.lang.reflect.Proxy.class);

        String methodName = proxyMethod.getName();
        Type returnType = Type.getReturnType(proxyMethod);
        Type[] argTypes = Type.getArgumentTypes(proxyMethod);
        Type[] eTypes = getExceptionTypes(proxyMethod);

        Method asmMethod = new Method(methodName, returnType, argTypes);
        GeneratorAdapter mg2 = new GeneratorAdapter(ACC_PUBLIC, asmMethod, null, eTypes, cv);
        mg2.visitVarInsn(ALOAD, 0);
        mg2.visitVarInsn(ALOAD, 1);
        mg2.visitTypeInsn(CHECKCAST, delegateClass.getName().replace('.', '/'));
        String delIntClassDesc = Type.getType(delegateClass).getDescriptor();
        mg2.visitFieldInsn(PUTFIELD, subClassName.replace('.', '/'),
                DELEGATE_FIELD_NAME, delIntClassDesc);
        mg2.returnValue();
        mg2.endMethod();
    }
}
