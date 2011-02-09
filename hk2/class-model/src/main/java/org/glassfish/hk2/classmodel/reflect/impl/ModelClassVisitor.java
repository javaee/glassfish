/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.ExtensibleType;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.EmptyVisitor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ASM class visitor, used to build to model
 *
 * @author Jerome Dochez
 */
@SuppressWarnings("unchecked")
public class ModelClassVisitor implements ClassVisitor {

    private static Logger logger = Logger.getLogger(ModelClassVisitor.class.getName());
  
    private final ParsingContext ctx;
    private final TypeBuilder typeBuilder;
    private final URI definingURI;
    private final String entryName;
    TypeImpl type;
    boolean deepVisit =false;
    final VisitingContext visitingContext = new VisitingContext();
    private final ModelFieldVisitor fieldVisitor = new ModelFieldVisitor(visitingContext);
    private final ModelMethodVisitor methodVisitor = new ModelMethodVisitor(visitingContext);
    private final ModelAnnotationVisitor annotationVisitor = new ModelAnnotationVisitor(visitingContext);

    public ModelClassVisitor(ParsingContext ctx, URI definingURI, String entryName) {
        this.ctx = ctx;
        this.definingURI = definingURI;
        this.entryName = entryName;
        typeBuilder = ctx.getTypeBuilder(definingURI);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {        

        String parentName = (superName!=null?org.objectweb.asm.Type.getObjectType(superName).getClassName():null);
        TypeProxy parent = (parentName!=null?typeBuilder.getHolder(parentName, typeBuilder.getType(access)):null);
        if (parent!=null && !parentName.equals(Object.class.getName())) {
            // put a temporary parent until we eventually visit it. 
            TypeImpl parentType = typeBuilder.getType(access, parentName, null);
            parent.set(parentType);
        }
        String className = org.objectweb.asm.Type.getObjectType(name).getClassName();
        URI classDefURI=null;
        try {
            int index = entryName.length() - name.length() - 6;
            if (null == definingURI || index==0) {
                classDefURI=definingURI;
            } else {
                String newPath=(index>0?definingURI.getPath() + entryName.substring(0, index):definingURI.getPath());
                classDefURI = new URI(definingURI.getScheme(), newPath, definingURI.getFragment());
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        
        if (logger.isLoggable(Level.FINER)) {
          logger.log(Level.FINER, "visiting {0} with classDefURI={1}", new Object[] {entryName, classDefURI});
        }
            
        type = ctx.getTypeBuilder(classDefURI).getType(access, className, parent);
        type.getProxy().visited();
        type.addDefiningURI(classDefURI);
        deepVisit =ctx.getConfig().getAnnotationsOfInterest().isEmpty();

        // reverse index
        if (parent!=null) {
            parent.addSubTypeRef(type);
        }


        try {
            ExtensibleTypeImpl classModel = (ExtensibleTypeImpl) type;
            for (String intf : interfaces) {
                String interfaceName = org.objectweb.asm.Type.getObjectType(intf).getClassName();
                TypeProxy<InterfaceModel> typeProxy = typeBuilder.getHolder(interfaceName, InterfaceModel.class);
                classModel.isImplementing(typeProxy);
                if (classModel instanceof ClassModel)
                    typeProxy.getImplementations().add((ClassModel) classModel);

            }
        } catch(ClassCastException e) {
            // ignore
        }

    }

    @Override
    public void visitSource(String source, String debug) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        desc = unwrap(desc);
        
        final AnnotationTypeImpl at = (AnnotationTypeImpl) typeBuilder.getType(Opcodes.ACC_ANNOTATION, desc, null);
        final AnnotationModelImpl am = new AnnotationModelImpl(type, at);

        // reverse index
        at.getAnnotatedElements().add(type);

        // forward index
        type.addAnnotation(am);

        if (ctx.getConfig().getAnnotationsOfInterest().contains(desc)) {
            logger.log(Level.FINER, "Inspecting fields of {0}", type.getName());
            deepVisit =true;
        }
        visitingContext.annotation=am;
        return annotationVisitor;
    }

    @Override
    public void visitAttribute(Attribute attr) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FieldVisitor visitField(int access, final String name, final String desc, final String signature, final Object value) {

        if (!deepVisit) {
            return null;
        }

        ClassModelImpl cm;
        try {
             cm = (ClassModelImpl) type;
        } catch (Exception e) {
            return null;
        }

        org.objectweb.asm.Type asmType = org.objectweb.asm.Type.getType(desc);
//        if (type==org.objectweb.asm.Type.INT_TYPE || type==org.objectweb.asm.Type.BOOLEAN_TYPE) {
//            return null;
//        }

        TypeProxy<?> fieldType =  typeBuilder.getHolder(asmType.getClassName());
        final FieldModelImpl field = typeBuilder.getFieldModel(name, fieldType, cm);
        visitingContext.field = field;

        // reverse index.
        fieldType.getRefs().add(field);

        // forward index
        cm.addField(field);
        return fieldVisitor;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!deepVisit) {
            return null;
        }

        ExtensibleType cm;
        try {
             cm = (ExtensibleType) type;
        } catch (Exception e) {
            return null;
        }
        final MethodModelImpl method = new MethodModelImpl(name, cm, (signature==null?desc:signature));
        type.addMethod(method);
        visitingContext.method = method;
        return methodVisitor;
    }

    @Override
    public void visitEnd() {
        type=null;
    }                                                            

    private String unwrap(String desc) {
        return org.objectweb.asm.Type.getType(desc).getClassName();
    }

    private static class VisitingContext {
        FieldModelImpl field;
        MethodModelImpl method;
        AnnotationModelImpl annotation;

    }

    private class ModelMethodVisitor extends EmptyVisitor implements MethodVisitor {

        private final VisitingContext context;

        private ModelMethodVisitor(VisitingContext context) {
            this.context = context;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (context.method==null) {
                // probably an annotation method, ignore
                return null;
            }
          
            AnnotationTypeImpl annotationType = (AnnotationTypeImpl) typeBuilder.getType(Opcodes.ACC_ANNOTATION, unwrap(desc), null);
            AnnotationModelImpl annotationModel = new AnnotationModelImpl(context.method, annotationType);

            // reverse index.
            annotationType.getAnnotatedElements().add(context.method);

            // forward index
            context.method.addAnnotation(annotationModel);
            context.annotation = annotationModel;
            
            return annotationVisitor;
        }

        @Override
        public void visitEnd() {
//            context.method=null;
        }
    }

    private class ModelFieldVisitor extends EmptyVisitor implements FieldVisitor {

        private final VisitingContext context;

        private ModelFieldVisitor(VisitingContext context) {
            this.context = context;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String s, boolean b) {
            FieldModelImpl field = context.field;

            AnnotationTypeImpl annotationType = (AnnotationTypeImpl) typeBuilder.getType(Opcodes.ACC_ANNOTATION, unwrap(s), null );
            AnnotationModelImpl annotationModel = new AnnotationModelImpl(field, annotationType);

            // reverse index.
            annotationType.getAnnotatedElements().add(field);

            // forward index
            field.addAnnotation(annotationModel);
            context.annotation = annotationModel;
            return annotationVisitor;
        }

        @Override
        public void visitEnd() {
            context.field = null;
        }
    }

    private class ModelAnnotationVisitor extends EmptyVisitor implements AnnotationVisitor {
        private final VisitingContext context;

        private ModelAnnotationVisitor(VisitingContext context) {
            this.context = context;
        }
        
        @Override
        public void visit(String name, Object value) {
            if (context.annotation==null) return;
            context.annotation.addValue(name, value);
        }

        @Override
        public void visitEnd() {
            context.annotation=null;
        }
    }
}
