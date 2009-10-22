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

import java.lang.reflect.ReflectPermission;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.Permission;

import com.sun.enterprise.deployment.util.TypeUtil;

public class EjbOptionalIntfGenerator
    implements Opcodes {

    private static final int INTF_FLAGS = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;

    private static final String DELEGATE_FIELD_NAME = "__ejb31_delegate";

    private Map<String, byte[]> classMap = new HashMap<String, byte[]>();

    private Map<String, Class> loadedClasses = new HashMap<String, Class>()
            ;
    private ClassLoader loader;

    private ProtectionDomain protectionDomain;


    private static final boolean _debug = Boolean.valueOf(System.getProperty("emit.ejb.optional.interface"));

    public EjbOptionalIntfGenerator(ClassLoader loader) {
        this.loader = loader;
    }

    public Class loadClass(final String name)
        throws ClassNotFoundException
    {
        Class clz = null;

        try {
            clz = loader.loadClass(name);
        } catch(ClassNotFoundException cnfe) {

            final byte[] classData = (byte []) classMap.get(name);

            if (classData != null) {

                clz = (Class) java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction() {
                            public java.lang.Object run() {
                                return makeClass(name, classData, protectionDomain, loader);
                            }
                        }
                );


            }
        }

        if( clz == null ) {

            throw new ClassNotFoundException(name);
        }

        return clz;       
    }

    public void generateOptionalLocalInterface(Class ejbClass, String intfClassName)
        throws Exception {

        if( protectionDomain == null ) {
            ejbClass.getProtectionDomain();
        }

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

        if( protectionDomain == null ) {
            superClass.getProtectionDomain();
        }

        ClassWriter cw = new ClassWriter(INTF_FLAGS);

       ClassVisitor tv = cw;
              //  new TraceClassVisitor(cw, new PrintWriter(System.out));
        
        //ClassVisitor tv = cw;
        boolean isSuperClassSerializable = superClass.isAssignableFrom(Serializable.class);

        String[] interfaces = new String[] {
                OptionalLocalInterfaceProvider.class.getName().replace('.', '/'),
                com.sun.ejb.spi.io.IndirectlySerializable.class.getName().replace('.', '/')
        };

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

        generateGetSerializableObjectFactoryMethod(tv, fldDesc, subClassName.replace('.', '/'));

        Set<java.lang.reflect.Method> allMethods = new HashSet<java.lang.reflect.Method>();
        
        for (Class clz = superClass; clz != Object.class; clz = clz.getSuperclass()) {
            java.lang.reflect.Method[] beanMethods = clz.getDeclaredMethods();
            for (java.lang.reflect.Method mth : beanMethods) {
                if( !hasSameSignatureAsExisting(mth, allMethods)) {

                    int modifiers = mth.getModifiers();
                    boolean isPublic = Modifier.isPublic(modifiers);
                    boolean isPrivate = Modifier.isPrivate(modifiers);
                    boolean isProtected = Modifier.isProtected(modifiers);
                    boolean isPackage = !isPublic && !isPrivate && !isProtected;

                    boolean isStatic = Modifier.isStatic(modifiers);

                    if (isPublic && !isStatic) {
                        generateBeanMethod(tv, subClassName, mth, delegateClass);
                    } else if( (isPackage || isProtected) && !isStatic ) {
                        generateNonAccessibleMethod(tv, mth);
                    }                    
                    allMethods.add(mth);
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

    private static void generateNonAccessibleMethod(ClassVisitor cv,
                                           java.lang.reflect.Method m)
        throws Exception {

        String methodName = m.getName();
        Type returnType = Type.getReturnType(m);
        Type[] argTypes = Type.getArgumentTypes(m);
        Method asmMethod = new Method(methodName, returnType, argTypes);

        // Only called for non-static Protected or Package access
        int access =  ACC_PUBLIC;

        GeneratorAdapter mg = new GeneratorAdapter(access, asmMethod, null,
                getExceptionTypes(m), cv);

        mg.throwException(Type.getType(javax.ejb.EJBException.class),
                "Illegal non-business method access on no-interface view");
        
        mg.returnValue();
        
        mg.endMethod();

    }

    private static void generateGetSerializableObjectFactoryMethod(ClassVisitor classVisitor,
                                                                   String fieldDesc,
                                                                   String classDesc) {

        MethodVisitor cv = classVisitor.visitMethod(ACC_PUBLIC, "getSerializableObjectFactory", "()Lcom/sun/ejb/spi/io/SerializableObjectFactory;", null, new String[] { "java/io/IOException" });
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, classDesc, DELEGATE_FIELD_NAME, fieldDesc);
        cv.visitTypeInsn(CHECKCAST, "com/sun/ejb/spi/io/IndirectlySerializable");
        cv.visitMethodInsn(INVOKEINTERFACE, "com/sun/ejb/spi/io/IndirectlySerializable", "getSerializableObjectFactory", "()Lcom/sun/ejb/spi/io/SerializableObjectFactory;");
        cv.visitInsn(ARETURN);
        cv.visitMaxs(1, 1);

        
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

       // Name that Java uses for constructor methods
    private static final String CONSTRUCTOR_METHOD_NAME = "<init>" ;

    // Name that Java uses for a classes static initializer method
    private static final String STATIC_INITIALIZER_METHOD_NAME = "<clinit>" ;

     // A Method for the protected ClassLoader.defineClass method, which we access
    // using reflection.  This requires the supressAccessChecks permission.
    private static final java.lang.reflect.Method defineClassMethod = AccessController.doPrivileged(
	new PrivilegedAction<java.lang.reflect.Method>() {
	    public java.lang.reflect.Method run() {
		try {
		    java.lang.reflect.Method meth = ClassLoader.class.getDeclaredMethod(
			"defineClass", String.class,
			byte[].class, int.class, int.class,
			ProtectionDomain.class ) ;
		    meth.setAccessible( true ) ;
		    return meth ;
		} catch (Exception exc) {
		    throw new RuntimeException(
			"Could not find defineClass method!", exc ) ;
		}
	    }
	}
    ) ;

    private static final Permission accessControlPermission =
	    new ReflectPermission( "suppressAccessChecks" ) ;

    // This requires a permission check
    private Class<?> makeClass( String name, byte[] def, ProtectionDomain pd,
	    ClassLoader loader ) {

	SecurityManager sman = System.getSecurityManager() ;
	if (sman != null)
	    sman.checkPermission( accessControlPermission ) ;

	try {
	    return (Class)defineClassMethod.invoke( loader,
		name, def, 0, def.length, pd ) ;
	} catch (Exception exc) {
	    throw new RuntimeException( "Could not invoke defineClass!",
		exc ) ;
	}
    }
}
