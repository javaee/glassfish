/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.testing.junit.internal;

import java.util.LinkedList;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.jvnet.hk2.annotations.Contract;
import org.glassfish.hk2.external.org.objectweb.asm.AnnotationVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.Attribute;
import org.glassfish.hk2.external.org.objectweb.asm.ClassVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.FieldVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.MethodVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.Opcodes;

/**
 * @author jwells
 *
 */
public class ClassVisitorImpl extends ClassVisitor {
    private final static String SERVICE_CLASS_FORM = "Lorg/jvnet/hk2/annotations/Service;";
    private final static String NAME = "name";
    private final static String VALUE = "value";
    
    private final DynamicConfiguration config;
    private final boolean verbose;
    
    private String implName;
    private final LinkedList<String> iFaces = new LinkedList<String>();
    private Class<?> scopeClass;
    private final LinkedList<String> qualifiers = new LinkedList<String>();
    private boolean isAService = false;
    private String name;
    
    /**
     * Creates this with the config to add to if this is a service
     * @param config
     * @param verbose true if we should print out any service we are binding
     */
    public ClassVisitorImpl(DynamicConfiguration config, boolean verbose) {
        super(Opcodes.ASM5);
        
        this.config = config;
        this.verbose = verbose;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public void visit(int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces) {
        implName = name.replace("/", ".");
        
        for (String i : interfaces) {
            String iFace = i.replace("/", ".");
            try {
                Class<?> iClass = this.getClass().getClassLoader().loadClass(iFace);
                if (iClass.isAnnotationPresent(Contract.class)) {
                    iFaces.add(iClass.getName());
                }
            }
            catch (Throwable th) {
                // Ignore, simply can't be loaded
            }
        }
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (!visible) return null;
        
        if (SERVICE_CLASS_FORM.equals(desc)) {
            isAService = true;
            
            return new ServiceAnnotationVisitor();
        }
        
        if (!desc.startsWith("L")) return null;
            
        String loadQualifierName = desc.substring(1, desc.length() -1).replace("/", ".");
        Class<?> annoClass;
        try {
            annoClass = Class.forName(loadQualifierName);
        }
        catch (Throwable th) {
            return null;
        }
            
        if (annoClass.isAnnotationPresent(Scope.class)) {
            scopeClass = annoClass;
        }
        else if (annoClass.isAnnotationPresent(Qualifier.class)) {
            qualifiers.add(annoClass.getName());
            
            if (Named.class.equals(annoClass)) {
                return new NamedAnnotationVisitor(getDefaultName());
            }
        }
        
        return null;
    }
    
    private String getDefaultName() {
        if (implName == null) throw new IllegalStateException();
        
        int index = implName.lastIndexOf('.');
        if (index <= 0) return implName;
        
        return implName.substring(index + 1);
    }
        

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitAttribute(org.objectweb.asm.Attribute)
     */
    @Override
    public void visitAttribute(Attribute arg0) {
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        if (!isAService) return;
        
        DescriptorImpl di = new DescriptorImpl();
        di.setImplementation(implName);
        if (scopeClass == null) {
            // The default for classes with Service is Singelton
            di.setScope(Singleton.class.getName());
        }
        else {
            di.setScope(scopeClass.getName());
        }
        
        di.addAdvertisedContract(implName);
        for (String iFace : iFaces) {
            di.addAdvertisedContract(iFace);
        }
        
        for (String qualifier : qualifiers) {
            di.addQualifier(qualifier);
        }
        
        if (name != null) {
            di.setName(name);
        }
        
        if (verbose) {
            System.out.println("Binding service " + di);
        }
        config.bind(di);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public FieldVisitor visitField(int arg0, String arg1, String arg2,
            String arg3, Object arg4) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
            String arg3, String[] arg4) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitOuterClass(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void visitOuterClass(String arg0, String arg1, String arg2) {
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitSource(java.lang.String, java.lang.String)
     */
    @Override
    public void visitSource(String arg0, String arg1) {
        
    }
    
    private class ServiceAnnotationVisitor extends AnnotationVisitor {
        public ServiceAnnotationVisitor() {
            super(Opcodes.ASM5);
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
         */
        @Override
        public void visit(String annotationName, Object value) {
            if (annotationName.equals(NAME)) {
                name = (String) value;
            }
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
         */
        @Override
        public AnnotationVisitor visitArray(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitEnum(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void visitEnum(String arg0, String arg1, String arg2) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    private class NamedAnnotationVisitor extends AnnotationVisitor {
        private final String defaultName;
        private boolean nameSet = false;
        
        public NamedAnnotationVisitor(String defaultName) {
            super(Opcodes.ASM5);
            
            this.defaultName = defaultName;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
         */
        @Override
        public void visit(String annotationName, Object value) {
            if (annotationName.equals(VALUE)) {
                name = (String) value;
                nameSet = true;
            }
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
         */
        @Override
        public AnnotationVisitor visitArray(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
         */
        @Override
        public void visitEnd() {
            if (nameSet) return;
            
            name = defaultName;
        }

        /* (non-Javadoc)
         * @see org.objectweb.asm.AnnotationVisitor#visitEnum(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void visitEnum(String arg0, String arg1, String arg2) {
            // TODO Auto-generated method stub
            
        }
        
    }

}
