/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.org.apache.jdo.impl.enhancer.jdo.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;

/**
 * Class to generate methods for accessing non SingleFieldIdentity classes.
 * 
 * @author Mahesh Kannan
 */
class CompositePKOIDMethodsHandler
        implements OIDMethodsHandler, Opcodes
{

    protected String pkClassName;

    protected PersistentStateAccessGenerator pcStateAccessGenerator;

    protected PersistentStateAccessGenerator keyStateAccessGenerator;

    CompositePKOIDMethodsHandler(String pkClassName,
            PersistentStateAccessGenerator pcStateAccessGenerator,
            PersistentStateAccessGenerator keyStateAccessGenerator) {
        this.pkClassName = pkClassName;
        this.pcStateAccessGenerator = pcStateAccessGenerator;
        this.keyStateAccessGenerator = keyStateAccessGenerator;
    }

    public String getKeyClassName() {
        return pkClassName;
    }

    public void generateNewObjectIDInstance(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, pkClassName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, pkClassName, "<init>", "()V");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 1);
    }

    public void generateNewObjectIdInstanceStringMethod(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, pkClassName);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, pkClassName, "<init>",
                "(Ljava/lang/String;)V");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(3, 2);
    }

    public void generateCopyToOID(MethodVisitor mv, FieldInfo thisClassKeyRef,
            FieldInfo keyClassKeyRef)
    {
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 0);
        pcStateAccessGenerator.generateInstructionsToReadState(mv,
                thisClassKeyRef.getName(), thisClassKeyRef.getDescriptor());
        keyStateAccessGenerator.generateInstructionsToWriteState(mv,
                keyClassKeyRef.getName(), keyClassKeyRef.getDescriptor());
    }

    public void generateCopyFromOID(MethodVisitor mv,
            FieldInfo thisClassKeyRef, FieldInfo keyClassKeyRef)
    {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        keyStateAccessGenerator.generateInstructionsToReadState(mv,
                keyClassKeyRef.getName(), keyClassKeyRef.getDescriptor());
        pcStateAccessGenerator.generateInstructionsToWriteState(mv,
                thisClassKeyRef.getName(), thisClassKeyRef.getDescriptor());
    }

    public void generateCopyToOIDOFS(MethodVisitor mv, String pcOwnerClassName,
            int pcFieldIndex, String ofsPath, String javaType, String sig,
            FieldInfo keyFieldRef)
    {
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(GETSTATIC, pcOwnerClassName,
                "jdoInheritedFieldCount", "I");
        BuilderHelper.generateIntegerConstantInstruction(mv, pcFieldIndex);
        mv.visitInsn(IADD);
        mv.visitMethodInsn(INVOKEINTERFACE, ofsPath, "fetch" + javaType
                + "Field", "(I)" + sig);
        keyStateAccessGenerator.generateInstructionsToWriteState(mv,
                keyFieldRef.getName(), keyFieldRef.getDescriptor());
    }

    public void generateCopyFromOIDOFC(MethodVisitor mv,
            String pcOwnerClassName, int pcFieldIndex, String ofcPath,
            String javaType, String sig, FieldInfo keyFieldRef)
    {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(GETSTATIC, pcOwnerClassName,
                "jdoInheritedFieldCount", "I");
        BuilderHelper.generateIntegerConstantInstruction(mv, pcFieldIndex);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ALOAD, 3);
        keyStateAccessGenerator.generateInstructionsToReadState(mv, keyFieldRef
                .getName(), keyFieldRef.getDescriptor());
        mv.visitMethodInsn(INVOKEINTERFACE, ofcPath, "store" + javaType
                + "Field", "(I" + keyFieldRef.getDescriptor() + ")V");
    }

    public void generateNewObjectIDInstanceObjectMethod(MethodVisitor mv,
            String ownerClassName, String ofsPath)
    {
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        return;
    }
}
