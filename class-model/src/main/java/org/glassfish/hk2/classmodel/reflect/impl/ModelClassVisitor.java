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

import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.external.org.objectweb.asm.*;
import org.glassfish.hk2.external.org.objectweb.asm.signature.SignatureReader;

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
public class ModelClassVisitor extends ClassVisitor {

    private static Logger logger = Logger.getLogger(ModelClassVisitor.class.getName());
  
    private final ParsingContext ctx;
    private final TypeBuilder typeBuilder;
    private final URI definingURI;
    private final String entryName;
    TypeImpl type;
    boolean deepVisit =false;
    private final ClassVisitingContext classContext;
    private final MemberVisitingContext visitingContext;
    private final ModelFieldVisitor fieldVisitor;
    private final ModelMethodVisitor methodVisitor;
    private final ModelAnnotationVisitor annotationVisitor;
    private final ModelDefaultAnnotationVisitor defaultAnnotationVisitor;
    private static int discarded=0;
    private boolean isApplicationClass;


    public ModelClassVisitor(ParsingContext ctx, URI definingURI, String entryName,
                             boolean isApplicationClass) {
        super(Opcodes.ASM5);
        
        this.ctx = ctx;
        this.definingURI = definingURI;
        this.entryName = entryName;
        typeBuilder = ctx.getTypeBuilder(definingURI);
        classContext = new ClassVisitingContext();
        visitingContext = new MemberVisitingContext(ctx.getConfig().modelUnAnnotatedMembers());
        fieldVisitor = new ModelFieldVisitor(visitingContext);
        methodVisitor = new ModelMethodVisitor(visitingContext);
        annotationVisitor = new ModelAnnotationVisitor();
        defaultAnnotationVisitor = new ModelDefaultAnnotationVisitor(methodVisitor.getContext());
        this.isApplicationClass = isApplicationClass;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String parentName = (superName!=null?org.glassfish.hk2.external.org.objectweb.asm.Type.getObjectType(superName).getClassName():null);
        TypeProxy parent = null;
        Class<? extends Type> typeType = typeBuilder.getType(access);
        if (!typeType.equals(AnnotationType.class)) {
            parent = (parentName!=null?typeBuilder.getHolder(parentName, typeType):null);
        }
        if (parent!=null && !parentName.equals(Object.class.getName())) {
            // put a temporary parent until we eventually visit it. 
            TypeImpl parentType = typeBuilder.getType(access, parentName, null);
            parent.set(parentType);
        }
        String className = org.glassfish.hk2.external.org.objectweb.asm.Type.getObjectType(name).getClassName();
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

//        if (!new File(classDefURI).exists()) {
//          throw new IllegalStateException(entryName + ": " + classDefURI.toString());
//        }
        
        type = ctx.getTypeBuilder(classDefURI).getType(access, className, parent);
        type.setApplicationClass(isApplicationClass);
        type.getProxy().visited();
        type.addDefiningURI(classDefURI);
        deepVisit = ctx.getConfig().getAnnotationsOfInterest().isEmpty();

        classContext.type = type;
        classContext.interfaces = interfaces;
        classContext.parent = parent;
        // reverse index
        if (parent!=null) {
            parent.addSubTypeRef(type);
        }


        try {
            ExtensibleTypeImpl classModel = (ExtensibleTypeImpl) type;
            if (signature!=null) {
                SignatureReader reader = new SignatureReader(signature);
                SignatureVisitorImpl signatureVisitor = new SignatureVisitorImpl(typeBuilder);
                reader.accept(signatureVisitor);
                if (!signatureVisitor.getImplementedInterfaces().isEmpty()) {
                    for (ParameterizedInterfaceModelImpl pim : signatureVisitor.getImplementedInterfaces()) {
                        if (pim.getRawInterfaceProxy()!=null) {
                            classModel.isImplementing(pim);
                            if (classModel instanceof ClassModel) {
                                pim.getRawInterfaceProxy().
                                    addImplementation((ClassModel) classModel);
                            }
                        }
                    }
                }
            } else {
                if (!typeType.equals(AnnotationType.class)) {
                    for (String intf : interfaces) {
                        String interfaceName = org.glassfish.hk2.external.org.objectweb.asm.Type.getObjectType(intf).getClassName();
                        TypeImpl interfaceModel = typeBuilder.getType(Opcodes.ACC_INTERFACE, interfaceName, null);
                        TypeProxy<InterfaceModel> typeProxy = typeBuilder.getHolder(interfaceName, InterfaceModel.class);
                        if (typeProxy.get() == null) {
                            typeProxy.set((InterfaceModel) interfaceModel);
                        }
                        
                        classModel.isImplementing(typeProxy);
                        if (classModel instanceof ClassModel) {
                            typeProxy.addImplementation((ClassModel) classModel);
                        }
                    }
                }
            }
        } catch(ClassCastException e) {
            // ignore
        } catch(Exception ne) {
            ne.printStackTrace();
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
        annotationVisitor.getContext().annotation=am;
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

        ExtensibleTypeImpl cm;
        if (!(type instanceof ExtensibleTypeImpl)) {
            logger.severe("Field visitor invoked for field " + name +
                "in type " + type.getName() + " which is not a ClassModel type instance but a "
                + type.getClass().getName());
            return null;
        }
        cm = (ExtensibleTypeImpl) type;

        org.glassfish.hk2.external.org.objectweb.asm.Type asmType = org.glassfish.hk2.external.org.objectweb.asm.Type.getType(desc);

        TypeProxy<?> fieldType =  typeBuilder.getHolder(asmType.getClassName());
        if (fieldType==null) return null;
        final FieldModelImpl field = typeBuilder.getFieldModel(name, fieldType, cm);
        fieldVisitor.getContext().field = field;
        fieldVisitor.getContext().typeDesc = desc;
        fieldVisitor.getContext().access = access;
        fieldVisitor.getContext().classModel = cm;

        return fieldVisitor;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!deepVisit) {
            return null;
        }


        ExtensibleType cm;
        if (!(type instanceof ExtensibleType)) {
            logger.severe("Method visitor invoked for method " + name +
                    "in type " + type.getName() + " which is not an ExtensibleType type instance but a "
                    + type.getClass().getName());
            return null;
        }
        cm = (ExtensibleType) type;

        methodVisitor.getContext().method = new MethodModelImpl(name, cm, (signature==null?desc:signature));
        return methodVisitor;
    }

    @Override
    public void visitEnd() {
        type=null;
    }                                                            

    private String unwrap(String desc) {
        return org.glassfish.hk2.external.org.objectweb.asm.Type.getType(desc).getClassName();
    }

    private static class ClassVisitingContext {
        TypeImpl type;
        TypeProxy parent;
        String[] interfaces;
    }

    private static class MemberVisitingContext {
        final boolean modelUnAnnotatedMembers;

        private MemberVisitingContext(boolean modelUnAnnotatedMembers) {
            this.modelUnAnnotatedMembers = modelUnAnnotatedMembers;
        }
    }

    private static class FieldVisitingContext extends MemberVisitingContext {
        FieldModelImpl field;
        String typeDesc;
        ExtensibleTypeImpl classModel;
        int access;

        private FieldVisitingContext(boolean modelUnAnnotatedMembers) {
            super(modelUnAnnotatedMembers);
        }
    }

    private static class MethodVisitingContext extends MemberVisitingContext {
        MethodModelImpl method;

        private MethodVisitingContext(boolean modelUnAnnotatedMembers) {
            super(modelUnAnnotatedMembers);
        }
    }

    private static class AnnotationVisitingContext {
        AnnotationModelImpl annotation;
    }

    private class ModelMethodVisitor extends MethodVisitor {

        private final MethodVisitingContext context;

        private ModelMethodVisitor(MemberVisitingContext context) {
            super(Opcodes.ASM5);
            
            this.context = new MethodVisitingContext(context.modelUnAnnotatedMembers);
        }

        MethodVisitingContext getContext() {
            return context;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (context.method==null) {
                // probably an annotation method, ignore
                return null;
            }
          
            AnnotationTypeImpl annotationType = (AnnotationTypeImpl) typeBuilder.getType(Opcodes.ACC_ANNOTATION, unwrap(desc), null);
            AnnotationModelImpl am = new AnnotationModelImpl(context.method, annotationType);

            // reverse index.
            annotationType.getAnnotatedElements().add(context.method);

            // forward index
            context.method.addAnnotation(am);
            annotationVisitor.getContext().annotation= am;

            return annotationVisitor;
        }

        @Override
        public void visitEnd() {
            if (context.modelUnAnnotatedMembers || !context.method.getAnnotations().isEmpty()) {
                type.addMethod(context.method);
            }
//            context.method=null;
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
          return defaultAnnotationVisitor;
        }
    }
    
    
    private class ModelDefaultAnnotationVisitor extends AnnotationVisitor {

      private final MethodVisitingContext context;
      
      public ModelDefaultAnnotationVisitor(MethodVisitingContext visitingContext) {
          super(Opcodes.ASM5);
        this.context = visitingContext;
      }

      public void visit(java.lang.String desc, java.lang.Object value) {
        AnnotationTypeImpl am = (AnnotationTypeImpl) context.method.owner;
        am.addDefaultValue(context.method.getName(), value);
      }
    }

    
    private class ModelFieldVisitor extends FieldVisitor {

        private final FieldVisitingContext context;

        private ModelFieldVisitor(MemberVisitingContext context) {
            super(Opcodes.ASM5);
            
            this.context = new FieldVisitingContext(context.modelUnAnnotatedMembers);
        }

        FieldVisitingContext getContext() {
            return context;
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
            annotationVisitor.getContext().annotation = annotationModel;
            return annotationVisitor;
        }

        @Override
        public void visitEnd() {

            // if we have been requested to model unannotated members OR the field has annotations.
            if (context.modelUnAnnotatedMembers || !context.field.getAnnotations().isEmpty()) {

                // reverse index.
                context.field.type.addFieldRef(context.field);

                // forward index
                if ((Opcodes.ACC_STATIC & context.access)==Opcodes.ACC_STATIC) {
                    context.classModel.addStaticField(context.field);
                } else {
                    context.classModel.addField(context.field);
                }
            }

            context.field = null;
        }
    }

    private class ModelAnnotationVisitor extends AnnotationVisitor {
        private final AnnotationVisitingContext context;

        private ModelAnnotationVisitor() {
            super(Opcodes.ASM5);
            
            this.context = new AnnotationVisitingContext();
        }

        AnnotationVisitingContext getContext() {
            return context;
        }
        
        @Override
        public void visit(String name, Object value) {
            if (context.annotation==null) return;
            context.annotation.addValue(name, value);
        }
        
        @Override
        public AnnotationVisitor visitArray(String name) {
            if (context.annotation == null) return null;
            context.annotation.addValue(name, null);
            return null;
            
        }

        @Override
        public void visitEnd() {
//            context.annotation=null;
        }
    }
    
}
