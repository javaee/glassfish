/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License. You can obtain
 *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *  Sun designates this particular file as subject to the "Classpath" exception
 *  as provided by Sun in the GPL Version 2 section of the License file that
 *  accompanied this code.  If applicable, add the following below the License
 *  Header, with the fields enclosed by brackets [] replaced by your own
 *  identifying information: "Portions Copyrighted [year]
 *  [name of copyright owner]"
 *
 *  Contributor(s):
 *
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */

package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.objectweb.asm.*;

/**
 * ASM class visitor, used to build to model
 *
 * @author Jerome Dochez
 */
public class ModelClassVisitor implements ClassVisitor {

    private final ParsingContext ctx;
    private final TypeBuilder typeBuilder;
    TypeImpl type;
    boolean visitField=false;

    public ModelClassVisitor(ParsingContext ctx) {
        this.ctx = ctx;
        if (!(ctx.getTypes() instanceof TypeBuilder)) {
            throw new RuntimeException("Wrong context for this model class visitor");
        }
        typeBuilder = (TypeBuilder) ctx.getTypes();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {        

        String parentName = (superName!=null?org.objectweb.asm.Type.getObjectType(superName).getClassName():null);
        TypeProxy parent = (parentName!=null?typeBuilder.getHolder(parentName):null);
        String className = org.objectweb.asm.Type.getObjectType(name).getClassName();
        type = typeBuilder.getType(access, className, parent);
        visitField=ctx.getConfig().getInjectionTargetAnnotations().isEmpty();

        // reverse index
        if (parent!=null) {
            parent.getSubTypeRefs().add(type);
        }


        try {
            ClassModelImpl classModel = (ClassModelImpl) type;
            for (String intf : interfaces) {
                String interfaceName = org.objectweb.asm.Type.getObjectType(intf).getClassName();
                InterfaceModelImpl im = typeBuilder.getInterface(interfaceName);
                classModel.isImplementing(im);
                im.addImplementation(classModel);

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

        final AnnotationTypeImpl at = typeBuilder.getAnnotation(desc);
        final AnnotationModelImpl am = new AnnotationModelImpl(type, at);

        // reverse index
        at.getReferences().add(type);

        // forward index
        type.addAnnotation(am);

        if (ctx.getConfig().getInjectionTargetAnnotations().contains(desc)) {
            System.out.println("Inspecting fields of " + type.getName());
            visitField=true;
        }

        return new AnnotationVisitor() {
            @Override
            public void visit(String name, Object value) {
                am.addValue(name, value);
            }

            @Override
            public void visitEnum(String name, String desc, String value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void visitEnd() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
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

        if (!visitField) {
            return null;
        }

        ClassModelImpl cm;
        try {
             cm = (ClassModelImpl) type;
        } catch (Exception e) {
            return null;
        }

        org.objectweb.asm.Type type = org.objectweb.asm.Type.getType(desc);
        if (type==org.objectweb.asm.Type.INT_TYPE || type==org.objectweb.asm.Type.BOOLEAN_TYPE) {
            return null;
        }

        TypeProxy fieldType =  typeBuilder.getHolder(type.getClassName());
        final FieldModelImpl field = typeBuilder.getFieldModel(name, fieldType);

        // reverse index.
        fieldType.getFieldRefs().add(field);

        // forward index
        cm.addField(field);
        return new FieldVisitor() {
            @Override
            public AnnotationVisitor visitAnnotation(String s, boolean b) {
                AnnotationTypeImpl annotationType = typeBuilder.getAnnotation(unwrap(s));
                AnnotationModelImpl annotationModel = new AnnotationModelImpl(field, annotationType);

                // reverse index.
                annotationType.getReferences().add(field);

                // forward index
                field.addAnnotation(annotationModel);
                return null;
            }

            @Override
            public void visitAttribute(Attribute attribute) {

            }

            @Override
            public void visitEnd() {

            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return null;
    }

    @Override
    public void visitEnd() {
        //To change body of implemented methods use File | Settings | File Templates.
    }                                                            

    private String unwrap(String desc) {
        return org.objectweb.asm.Type.getType(desc).getClassName();
    }
}
