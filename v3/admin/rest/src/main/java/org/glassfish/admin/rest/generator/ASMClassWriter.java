/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.generator;


import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Ludovic Champenois
 */
public class ASMClassWriter implements ClassWriter, Opcodes {
    private static final String GENERATED_PATH ="org/glassfish/admin/rest/resources/generated/";

    private org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(0);
    private String className;
    private String baseClassName;
    private String resourcePath;

    public ASMClassWriter(String className, String baseClassName, String resourcePath) {
        this.className = className;
        this.baseClassName = baseClassName;
        this.resourcePath = resourcePath;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, GENERATED_PATH + className , null,
                "org/glassfish/admin/rest/resources/" + baseClassName , null);

        if (resourcePath != null) {
            AnnotationVisitor av0 = cw.visitAnnotation("Ljavax/ws/rs/Path;", true);
            av0.visit("value", "/" + resourcePath + "/");
            av0.visitEnd();
        }


        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "org/glassfish/admin/rest/resources/" + baseClassName , "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();


    }

    @Override
    public void createGetCommandResourcePaths(List<CommandResourceMetaData> commandMetaData) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createGetCommandResource(String commandResourceClassName, String resourcePath) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get" + commandResourceClassName , "()L"+ GENERATED_PATH  + commandResourceClassName + ";", null, null);

        AnnotationVisitor av0 = mv.visitAnnotation("Ljavax/ws/rs/Path;", true);
        av0.visit("value", resourcePath + "/");
        av0.visitEnd();

        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, GENERATED_PATH + className, "resourceContext", "Lcom/sun/jersey/api/core/ResourceContext;");
        mv.visitLdcInsn(Type.getType("LGENERATED_PATH" + commandResourceClassName + ";"));
        mv.visitMethodInsn(INVOKEINTERFACE, "com/sun/jersey/api/core/ResourceContext", "getResource", "(Ljava/lang/Class;)Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, GENERATED_PATH + commandResourceClassName );
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    @Override
    public void createCommandResourceConstructor(String commandResourceClassName, String commandName, String httpMethod, boolean linkedToParent, CommandResourceMetaData.ParameterMetaData[] commandParams, String commandDisplayName, String commandAction) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void done() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createGetDeleteCommand(String commandName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getDeleteCommand", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(commandName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void createGetPostCommand(String commandName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getPostCommand", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(commandName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void createGetChildResource(String path, String childResourceClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get" + childResourceClassName, "()L"+ GENERATED_PATH + childResourceClassName + ";", null, null);

        AnnotationVisitor av0 = mv.visitAnnotation("Ljavax/ws/rs/Path;", true);
        av0.visit("value", path + "/");
        av0.visitEnd();

        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, GENERATED_PATH + className, "resourceContext", "Lcom/sun/jersey/api/core/ResourceContext;");
        mv.visitLdcInsn(Type.getType("LGENERATED_PATH" + childResourceClassName + ";"));
        mv.visitMethodInsn(INVOKEINTERFACE, "com/sun/jersey/api/core/ResourceContext", "getResource", "(Ljava/lang/Class;)Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, GENERATED_PATH + childResourceClassName);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, GENERATED_PATH + className, "getEntity", "()Lorg/jvnet/hk2/config/Dom;");
        mv.visitLdcInsn(path);
        mv.visitMethodInsn(INVOKEVIRTUAL, GENERATED_PATH + childResourceClassName, "setParentAndTagName", "(Lorg/jvnet/hk2/config/Dom;Ljava/lang/String;)V");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(3, 2);
        mv.visitEnd();
    }

    @Override
    public void createGetChildResourceForListResources(String keyAttributeName, String childResourceClassName) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get" + childResourceClassName , "(Ljava/lang/String;)LGENERATED_PATH" + childResourceClassName + ";", null, null);

        AnnotationVisitor av0 = mv.visitAnnotation("Ljavax/ws/rs/Path;", true);
        av0.visit("value", "{" + keyAttributeName + "}/");
        av0.visitEnd();


        av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
        av0.visit("value", keyAttributeName);
        av0.visitEnd();

        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, GENERATED_PATH +"List" + childResourceClassName , "resourceContext", "Lcom/sun/jersey/api/core/ResourceContext;");
        mv.visitLdcInsn(Type.getType("L" + GENERATED_PATH + childResourceClassName + ";"));
        mv.visitMethodInsn(INVOKEINTERFACE, "com/sun/jersey/api/core/ResourceContext", "getResource", "(Ljava/lang/Class;)Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, GENERATED_PATH + childResourceClassName );
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, GENERATED_PATH + "List" + childResourceClassName , "entity", "Ljava/util/List;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, GENERATED_PATH + childResourceClassName , "setBeanByKey", "(Ljava/util/List;Ljava/lang/String;)V");
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    @Override
    public void createGetPostCommandForCollectionLeafResource(String postCommandName) {
        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "getPostCommand", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(postCommandName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void createGetDeleteCommandForCollectionLeafResource(String deleteCommandName) {
        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "getDeleteCommand", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(deleteCommandName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void createGetDisplayNameForCollectionLeafResource(String displayName) {
        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "getName", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(displayName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public byte[] getByteClass() {
        cw.visitEnd();
        return cw.toByteArray();
    }
}
