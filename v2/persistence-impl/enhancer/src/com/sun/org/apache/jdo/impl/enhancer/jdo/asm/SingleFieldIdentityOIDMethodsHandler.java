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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.EnhancerConstants;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.JDOConstants;

/**
 * SingleFieldIdentityOIDMethodsHandler handles all the primary key
 *  related methods for Single field Key.
 * 
 * @see com.sun.org.apache.jdo.impl.enhancer.jdo.asm.CompositePKOIDMethodsHandler
 * @author Mahesh Kannan
 *
 */
public class SingleFieldIdentityOIDMethodsHandler
    implements OIDMethodsHandler, Opcodes, JDOConstants, EnhancerConstants
{
    private static Map singleFieldIdentityMap = new HashMap();
    
    private String singleFieldClassName;
    
    private String pkFieldOwnerClassName;
    
    private String pkFieldName;
    
    private String pkFieldType;
    
    private int pkFieldIndex;
    
    private PersistentStateAccessGenerator pcStateAccessGenerator;
    
    private SingleFieldMetaInfo singleFieldMetaInfo;
    
    static {
        SingleFieldMetaInfo metaInfo = null;
        
        
        metaInfo = new SingleFieldMetaInfo("B", "byte", "Byte", "java/lang/Byte",
                "com/sun/persistence/support/identity/ByteIdentity");
        SingleFieldIdentityOIDMethodsHandler.addMetaInfo(metaInfo);
        
        metaInfo = new SingleFieldMetaInfo("C", "char", "Char", "java/lang/Character",
                "com/sun/persistence/support/identity/CharIdentity");
        metaInfo.isNumberType = false;
        SingleFieldIdentityOIDMethodsHandler.addMetaInfo(metaInfo);
        
        metaInfo = new SingleFieldMetaInfo("I", "int", "Int", "java/lang/Integer",
                "com/sun/persistence/support/identity/IntIdentity");
        SingleFieldIdentityOIDMethodsHandler.addMetaInfo(metaInfo);
        
        metaInfo = new SingleFieldMetaInfo("J", "long", "Long", "java/lang/Long",
                "com/sun/persistence/support/identity/LongIdentity");
        SingleFieldIdentityOIDMethodsHandler.addMetaInfo(metaInfo);
        
        metaInfo = new SingleFieldMetaInfo("S", "short", "Short", "java/lang/Short",
                "com/sun/persistence/support/identity/ShortIdentity");
        SingleFieldIdentityOIDMethodsHandler.addMetaInfo(metaInfo);
        
        metaInfo = new SingleFieldMetaInfo("Ljava/lang/String;", "String", "String", "java/lang/String",
                "com/sun/persistence/support/identity/StringIdentity");
        metaInfo.isNumberType = false;
        SingleFieldIdentityOIDMethodsHandler.addMetaInfo(metaInfo);
        
    }
  
    SingleFieldIdentityOIDMethodsHandler(String pkFieldOwnerClassName, String pkFieldName,
            String pkFieldType, int pkFieldIndex,
            PersistentStateAccessGenerator pcStateAccessGenerator) {
        
        this.singleFieldMetaInfo = (SingleFieldMetaInfo) singleFieldIdentityMap.get(pkFieldType);
        System.out.println("[**SingleFieldIdentity** Looking up meta for: " + pkFieldType
                + " ==> GOT: " + this.singleFieldMetaInfo);
        this.singleFieldClassName = this.singleFieldMetaInfo.singleFieldClassName;
        
        this.pkFieldOwnerClassName = pkFieldOwnerClassName;
        this.pkFieldName = pkFieldName;
        this.pkFieldType = pkFieldType;
        this.pkFieldIndex = pkFieldIndex;
        this.pcStateAccessGenerator = pcStateAccessGenerator;
        
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("pkFieldOwnerClassName: " + pkFieldOwnerClassName);
        System.out.println("pkFieldName: " + pkFieldName);
        System.out.println("pkFieldType: " + pkFieldType);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
    
    static boolean isSingleFieldIdentityType(String fieldType) {
        return singleFieldIdentityMap.get(fieldType) != null;
    }
    
    public String getKeyClassName() {
        return singleFieldClassName;
    }
    
    public void generateNewObjectIDInstance(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, singleFieldClassName);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(pkFieldOwnerClassName.replace('/', '.'));
        mv.visitMethodInsn(INVOKESTATIC, pkFieldOwnerClassName,
                SUNJDO_PC_sunjdoClassForName_Name,
                SUNJDO_PC_sunjdoClassForName_Sig);
        mv.visitVarInsn(ALOAD, 0);
        pcStateAccessGenerator.generateInstructionsToReadState(mv, pkFieldName, pkFieldType);
        mv.visitMethodInsn(INVOKESPECIAL, singleFieldClassName, "<init>",
                "(Ljava/lang/Class;" + pkFieldType + ")V");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(5, 1);
    }
    
    public void generateNewObjectIdInstanceStringMethod(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, singleFieldClassName);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(pkFieldOwnerClassName.replace('/', '.'));
        mv.visitMethodInsn(INVOKESTATIC, pkFieldOwnerClassName,
                SUNJDO_PC_sunjdoClassForName_Name,
                SUNJDO_PC_sunjdoClassForName_Sig);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, singleFieldClassName, "<init>",
                "(Ljava/lang/Class;Ljava/lang/String;)V");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(4, 2);
    }
    
    public void generateCopyFromOID(MethodVisitor mv,
            FieldInfo thisClassKeyRef, FieldInfo keyClassKeyRef) {
        boolean isLong = singleFieldMetaInfo.primitiveVMDesc.equals("J");
        int localVarIndex = isLong ? 4 : 3;
        boolean isPKFieldAnObject = this.pkFieldType.startsWith("L");

        BuilderHelper.initializeFieldToZero(mv, singleFieldMetaInfo.primitiveVMDesc, 2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, singleFieldClassName);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, singleFieldClassName);
        mv.visitVarInsn(ASTORE, localVarIndex);
        mv.visitVarInsn(ALOAD, localVarIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, singleFieldClassName, "getKey",
                "()"+singleFieldMetaInfo.primitiveVMDesc);
        mv.visitVarInsn(BuilderHelper.getAStoreOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException",
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 0);
        
        if (isPKFieldAnObject) {
            mv.visitTypeInsn(NEW, singleFieldMetaInfo.wrapperVMPath);
            mv.visitInsn(DUP);
            mv.visitVarInsn(BuilderHelper.getALoadOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
            mv.visitMethodInsn(INVOKESPECIAL, singleFieldMetaInfo.wrapperVMPath, "<init>",
                    "(" + singleFieldMetaInfo.primitiveVMDesc + ")V");
        } else {
            mv.visitVarInsn(BuilderHelper.getALoadOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        }
        pcStateAccessGenerator.generateInstructionsToWriteState(mv, pkFieldName, pkFieldType);
        //mv.visitInsn(RETURN);
        //mv.visitMaxs(3, 4);
        
        
        /*
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, singleFieldClassName);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException",
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, singleFieldClassName);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, singleFieldClassName, "getKey",
                "()" + singleFieldMetaInfo.primitiveTypeName);
        pcStateAccessGenerator.generateInstructionsToWriteState(mv, pkFieldName, pkFieldType);
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        */
    }
    
    public void generateCopyToOID(MethodVisitor mv,
            FieldInfo thisClassKeyRef, FieldInfo keyClassKeyRef) {
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException",
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitMaxs(3, 2);
    }
    
    public void generateCopyFromOIDOFC(MethodVisitor mv, String pcOwnerClassName,
            int pcFieldIndex, String ofcPath, String javaType, String sig,
            FieldInfo keyFieldRef) {
               mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(GETSTATIC, pcOwnerClassName, "jdoInheritedFieldCount", "I");
        BuilderHelper.generateIntegerConstantInstruction(mv, pcFieldIndex);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, singleFieldClassName, "getKey", "()"
                + singleFieldMetaInfo.primitiveVMDesc);
        boolean isPKFieldAnObject = this.pkFieldType.startsWith("L");

        if (BuilderHelper.isStringType(this.pkFieldType)) {
            System.out.println("==> Generating code for String type");
            mv.visitMethodInsn(INVOKEINTERFACE,
                    ofcPath, "storeStringField", "(ILjava/lang/String;)V");
        } else if (isPKFieldAnObject) {
            mv.visitMethodInsn(INVOKESTATIC, singleFieldMetaInfo.wrapperVMPath,
                    "valueOf", "(" + singleFieldMetaInfo.primitiveVMDesc + ")L"
                    + singleFieldMetaInfo.wrapperVMPath + ";");
            mv.visitMethodInsn(INVOKEINTERFACE,
                    ofcPath, "storeObjectField", "(ILjava/lang/Object;)V");
        } else {
            mv.visitMethodInsn(INVOKEINTERFACE,
                    ofcPath, "store" + javaType + "Field", "(I"
                    + singleFieldMetaInfo.primitiveVMDesc + ")V");
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 4);     //Ignored
        mv.visitEnd();
    }
   
    public void generateCopyToOIDOFS(MethodVisitor mv, String pcOwnerClassName,
            int pcFieldIndex, String ofsPath, String javaType, String sig,
            FieldInfo keyFieldRef) {
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException",
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
    }
    
 
    public void generateNewObjectIDInstanceObjectMethod(MethodVisitor mv,
            String ownerClassName, String ofsPath) {
        boolean isLong = singleFieldMetaInfo.primitiveVMDesc.equals("J");
        int localVarIndex = isLong ? 4 : 3;
        boolean isPKFieldAnObject = this.pkFieldType.startsWith("L");

        BuilderHelper.initializeFieldToZero(mv, singleFieldMetaInfo.primitiveVMDesc, 2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, ofsPath);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, ofsPath);
        mv.visitVarInsn(ASTORE, localVarIndex);
        mv.visitVarInsn(ALOAD, localVarIndex);
        mv.visitFieldInsn(GETSTATIC, ownerClassName, "jdoInheritedFieldCount", "I");
        BuilderHelper.generateIntegerConstantInstruction(mv, pkFieldIndex);
        mv.visitInsn(IADD);     
        mv.visitMethodInsn(INVOKEINTERFACE, ofsPath,
                "fetch" + singleFieldMetaInfo.javaWrapperTypeName + "Field",
                "(I)" + singleFieldMetaInfo.primitiveVMDesc);
        /*
        if (isPKFieldAnObject) {
            mv.visitMethodInsn(INVOKEVIRTUAL, singleFieldMetaInfo.wrapperVMPath,
                    singleFieldMetaInfo.primitiveTypeName+"Value",
                    "()"+singleFieldMetaInfo.primitiveVMDesc);
        }
        */
        mv.visitVarInsn(BuilderHelper.getAStoreOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, ownerClassName);
        Label l2 = new Label();
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, ownerClassName);
        mv.visitVarInsn(ASTORE, localVarIndex);
        mv.visitVarInsn(ALOAD, localVarIndex);
        pcStateAccessGenerator.generateInstructionsToReadState(mv, pkFieldName, pkFieldType);
        if (isPKFieldAnObject) {
            mv.visitMethodInsn(INVOKEVIRTUAL, singleFieldMetaInfo.wrapperVMPath,
                    singleFieldMetaInfo.primitiveTypeName+"Value",
                    "()"+singleFieldMetaInfo.primitiveVMDesc);
        }
        mv.visitVarInsn(BuilderHelper.getAStoreOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, "java/lang/Number");
        Label l3 = new Label();
        mv.visitJumpInsn(IFEQ, l3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
        mv.visitVarInsn(ASTORE, localVarIndex);
        mv.visitVarInsn(ALOAD, localVarIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number",
                singleFieldMetaInfo.primitiveTypeName+"Value",
                "()"+singleFieldMetaInfo.primitiveVMDesc);
        mv.visitVarInsn(BuilderHelper.getAStoreOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
        Label l4 = new Label();
        mv.visitJumpInsn(IFEQ, l4);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        mv.visitVarInsn(ASTORE, localVarIndex);
        mv.visitVarInsn(ALOAD, localVarIndex);
        mv.visitMethodInsn(INVOKESTATIC, singleFieldMetaInfo.wrapperVMPath,
                "parse" + singleFieldMetaInfo.javaWrapperTypeName , "(Ljava/lang/String;)"
                + singleFieldMetaInfo.primitiveVMDesc);
        mv.visitVarInsn(BuilderHelper.getAStoreOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l4);
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException",
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l1);
        mv.visitTypeInsn(NEW, singleFieldMetaInfo.singleFieldClassName);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(ownerClassName.replace('/', '.'));
        mv.visitMethodInsn(INVOKESTATIC, ownerClassName,
                SUNJDO_PC_sunjdoClassForName_Name,
                SUNJDO_PC_sunjdoClassForName_Sig);
        mv.visitVarInsn(BuilderHelper.getALoadOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        mv.visitMethodInsn(INVOKESPECIAL, singleFieldClassName, "<init>",
                "(Ljava/lang/Class;" + singleFieldMetaInfo.primitiveVMDesc + ")V");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(localVarIndex+1, localVarIndex+1);
        
        /*
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, ofsPath);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, ofsPath);
        mv.visitVarInsn(ASTORE, localVarIndex);
        mv.visitVarInsn(ALOAD, localVarIndex);
        mv.visitFieldInsn(GETSTATIC, ownerClassName, "jdoInheritedFieldCount", "I");
        BuilderHelper.generateIntegerConstantInstruction(mv, pkFieldIndex);
        mv.visitInsn(IADD);
        mv.visitMethodInsn(INVOKEINTERFACE, ofsPath,
                "fetch" + singleFieldMetaInfo.javaWrapperTypeName + "Field",
                "(I)" + singleFieldMetaInfo.primitiveVMDesc);
        mv.visitVarInsn(BuilderHelper.getAStoreOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        
        
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, "java/lang/Number");
        Label l2 = new Label();
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
        mv.visitVarInsn(ASTORE, localVarIndex);
        mv.visitVarInsn(ALOAD, localVarIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue",
                "()" + singleFieldMetaInfo.primitiveVMDesc);
        mv.visitVarInsn(BuilderHelper.getAStoreOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
        Label l3 = new Label();
        mv.visitJumpInsn(IFEQ, l3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        mv.visitVarInsn(ASTORE, localVarIndex);
        mv.visitVarInsn(ALOAD, localVarIndex);
        mv.visitMethodInsn(INVOKESTATIC, singleFieldMetaInfo.wrapperVMPath,
                "parse" + singleFieldMetaInfo.javaWrapperTypeName , "(Ljava/lang/String;)"
                + singleFieldMetaInfo.primitiveVMDesc);
        mv.visitVarInsn(BuilderHelper.getAStoreOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l3);
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>",
                "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l1);
        mv.visitTypeInsn(NEW, singleFieldMetaInfo.singleFieldClassName);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(ownerClassName.replace('/', '.'));
        mv.visitMethodInsn(INVOKESTATIC, ownerClassName,
                SUNJDO_PC_sunjdoClassForName_Name,
                SUNJDO_PC_sunjdoClassForName_Sig);
        mv.visitVarInsn(BuilderHelper.getALoadOpcode(singleFieldMetaInfo.primitiveVMDesc), 2);
        mv.visitMethodInsn(INVOKESPECIAL, singleFieldClassName, "<init>",
                "(Ljava/lang/Class;" + singleFieldMetaInfo.primitiveVMDesc + ")V");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(localVarIndex+1, localVarIndex+1);
        */
    }
    
  
    private static void addMetaInfo(SingleFieldMetaInfo metaInfo) {
        singleFieldIdentityMap.put(metaInfo.primitiveVMDesc, metaInfo);
        singleFieldIdentityMap.put(metaInfo.primitiveTypeName, metaInfo);
        singleFieldIdentityMap.put(metaInfo.javaWrapperTypeName, metaInfo);
        singleFieldIdentityMap.put(metaInfo.wrapperVMPath, metaInfo);
        singleFieldIdentityMap.put("L"+metaInfo.wrapperVMPath+";", metaInfo);
        singleFieldIdentityMap.put(
                metaInfo.wrapperVMPath.replace('/', '.'), metaInfo);
        singleFieldIdentityMap.put(metaInfo.singleFieldClassName, metaInfo);
        singleFieldIdentityMap.put(
                metaInfo.singleFieldClassName.replace('/', '.'), metaInfo);
    }
    
    private static class SingleFieldMetaInfo {
        String primitiveVMDesc;         //  J
        String primitiveTypeName;       //  long
        String javaWrapperTypeName;     //  Long
        String wrapperVMPath;           //  java/lang/Long
        String singleFieldClassName;    //  com/sun/persistence/support/identity/LongIdentity
        boolean isNumberType = true;    //  false only for char and String

        SingleFieldMetaInfo(String vmType, String primitiveType,
                String javaWrapperType, String vmPath,
                String sfcn) {
            this.primitiveVMDesc = vmType;
            this.primitiveTypeName = primitiveType;
            this.javaWrapperTypeName = javaWrapperType;
            this.wrapperVMPath = vmPath;
            this.singleFieldClassName = sfcn;
        }
        
        public String toString() {
            return "<<" + primitiveVMDesc + " " + singleFieldClassName + ">>";
        }
    }

}
