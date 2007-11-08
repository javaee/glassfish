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

import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactory;
import com.sun.org.apache.jdo.enhancer.classfile.TypeHelper;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactoryFactory;

import com.sun.org.apache.jdo.enhancer.classfile.AnnotaterCallback;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;
import com.sun.org.apache.jdo.impl.enhancer.jdo.ClassFileAnalyzer;
import com.sun.org.apache.jdo.impl.enhancer.jdo.ClassFileBuilder;

import com.sun.org.apache.jdo.impl.enhancer.util.Support;

import com.sun.org.apache.jdo.impl.enhancer.classfile.asm.ClassInfoImpl;
import com.sun.org.apache.jdo.impl.enhancer.classfile.asm.MethodInfoImpl;
import com.sun.org.apache.jdo.impl.enhancer.classfile.asm.AccessUtil;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.EnhancerConstants;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.JDOConstants;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.JDONameHelper;

public class ClassFileBuilderImpl
    extends Support
    implements ClassFileBuilder, Opcodes, JDOConstants, EnhancerConstants
{

    private ClassInfo classInfo;

    private ClassFileAnalyzer analyzer;

    private ClassNode classNode;

    private String ownerClassName;

    private BuilderHelper builderHelper;

    private PrimaryKeyMethodsGenerator pkMethodsGenerator;
    
    private PersistentStateAccessGenerator pcStateAccessGenerator;
    
    public ClassFileBuilderImpl(ClassInfo classInfo, ClassFileAnalyzer analyzer) {
        affirm(classInfo != null);
        affirm(analyzer != null);
        this.classInfo = classInfo;
        this.analyzer = analyzer;
        
        classNode = ((ClassInfoImpl) classInfo).getClassNode();
        ownerClassName = classInfo.getName();

        boolean isPropertyBasedAccess = analyzer.isPropertyBasedPersistence();
        
        this.pcStateAccessGenerator = (isPropertyBasedAccess)
            ? new PropertyBasedStateAccessGenerator(ownerClassName)
            : new FieldBasedStateAccessGenerator(ownerClassName);

        builderHelper = new BuilderHelper(classInfo, analyzer, pcStateAccessGenerator);
        pkMethodsGenerator = new PrimaryKeyMethodsGenerator(classInfo,
                analyzer, builderHelper, pcStateAccessGenerator, isPropertyBasedAccess);

    }

    public void mediateFieldAccess(MethodInfo methodInfo,
            AnnotaterCallback listener)
    {
        if (false) {
            System.out.println("mediatingFieldAccess for: "
                    + methodInfo.getName() + " " + methodInfo.getDescriptor()
                    + " " + methodInfo.getSignature());
        }
        MethodInfo removedMethodInfo = classInfo.removeMethod(methodInfo
                .getName(), methodInfo.getDescriptor(), methodInfo
                .getSignature());

        affirm(removedMethodInfo != null);
        FieldAccessMediator clzAdapter = new FieldAccessMediator(
                ((ClassInfoImpl) classInfo).getClassNode(), listener);
        ((MethodInfoImpl) removedMethodInfo).getMethodNode().accept(clzAdapter);
    }

    // ----------------------------------------------------------------------
    // Generic Augmentation
    // ----------------------------------------------------------------------

    /**
     * Build the jdoSetStateManager method for the class. public final
     * synchronized void jdoReplaceStateManager(com.sun.persistence.StateManager
     * sm) { final com.sun.persistence.StateManager s = this.jdoStateManager; if
     * (s != null) { this.jdoStateManager = s.replacingStateManager(this, sm);
     * return; } // throws exception if not authorized
     * JDOImplHelper.checkAuthorizedStateManager(sm); this.jdoStateManager = sm;
     * this.jdoFlags = LOAD_REQUIRED; }
     */
    public void addJDOReplaceStateManager() {
        final String pcRootName = analyzer.getPCRootClassName();
        final String jdoMethodName = JDO_PC_jdoReplaceStateManager_Name;
        final String jdoMethodSig = JDO_PC_jdoReplaceStateManager_Sig;
        final int jdoAccessFlags = JDO_PC_jdoReplaceStateManager_Mods;
        MethodVisitor mv = classNode.visitMethod(jdoAccessFlags, jdoMethodName,
                jdoMethodSig, null, null);
        
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                JDO_SM_replacingStateManager_Name, JDO_SM_replacingStateManager_Sig);
        
        mv.visitFieldInsn(PUTFIELD, pcRootName,
                JDO_PC_jdoStateManager_Name, JDO_StateManager_Sig);
        mv.visitInsn(RETURN);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, JDO_JDOImplHelper_Path,
                JDO_JDOImplHelper_checkAuthorizedStateManager_Name,
                JDO_JDOImplHelper_checkAuthorizedStateManager_Sig);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, pcRootName,
                JDO_PC_jdoStateManager_Name, JDO_StateManager_Sig);
        
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, pcRootName,
                JDO_PC_jdoFlags_Name, JDO_PC_jdoFlags_Sig);
        mv.visitInsn(RETURN);
        mv.visitMaxs(4, 3);
        
        mv.visitEnd();
    }

    /**
     * Build the jdoReplaceFlags method for the class. public final void
     * jdoReplaceFlags() { final StateManager sm = this.jdoStateManager; if (sm !=
     * null) { this.jdoFlags = sm.replacingFlags(this); } }
     */
    public void addJDOReplaceFlags() {
        final String pcRootName = analyzer.getPCRootClassName();
        ClassNode classNode = ((ClassInfoImpl) classInfo).getClassNode();
        final String jdoMethodName = JDO_PC_jdoReplaceFlags_Name;
        final String jdoMethodSig = JDO_PC_jdoReplaceFlags_Sig;
        final int jdoAccessFlags = JDO_PC_jdoReplaceFlags_Mods;
        MethodVisitor mv = classNode.visitMethod(jdoAccessFlags, jdoMethodName,
                jdoMethodSig, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, pcRootName,
                JDO_PC_jdoStateManager_Name, JDO_StateManager_Sig);
        mv.visitVarInsn(Opcodes.ASTORE, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, l0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, JDO_StateManager_Path,
                JDO_SM_replacingFlags_Name, JDO_SM_replacingFlags_Sig);
        mv.visitFieldInsn(Opcodes.PUTFIELD, pcRootName, JDO_PC_jdoFlags_Name,
                JDO_PC_jdoFlags_Sig);
        mv.visitLabel(l0);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 2);
        mv.visitEnd();
    }

    /**
     * Build the jdoMakeDirty method for the class. public final void
     * jdoMakeDirty(java.lang.String fieldname) { final
     * com.sun.persistence.StateManager sm = this.jdoStateManager; if (sm != null) {
     * sm.makeDirty(this, fieldname); } }
     */
    public void addJDOMakeDirtyMethod() {
        final String pcRootName = analyzer.getPCRootClassName();
        final String jdoMethodName = JDO_PC_jdoMakeDirty_Name;
        final String jdoMethodSig = JDO_PC_jdoMakeDirty_Sig;
        final int jdoAccessFlags = JDO_PC_jdoMakeDirty_Mods;
        MethodVisitor mv = classNode.visitMethod(jdoAccessFlags, jdoMethodName,
                jdoMethodSig, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                JDO_SM_makeDirty_Name, JDO_SM_makeDirty_Sig);
        mv.visitLabel(l0);
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    /**
     * Build the writeObject method for the class. private void
     * writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
     * jdoPreSerialize(); out.defaultWriteObject(); }
     */
    public void addWriteObjectMethod() {

        final String methodName = JAVA_Object_writeObject_Name;
        final String methodSig = JAVA_Object_writeObject_Sig;
        final int accessFlags = JAVA_Object_writeObject_Mods;
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, ownerClassName,
                JDO_PC_jdoPreSerialize_Name, JDO_PC_jdoPreSerialize_Sig);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_ObjectOutputStream_Path,
                JAVA_ObjectOutputStream_defaultWriteObject_Name,
                JDO_PC_jdoPreSerialize_Sig);

        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
    }

    /**
     * Build the jdoPreSerialize method for the class. protected final void
     * jdoPreSerialize() { final com.sun.persistence.StateManager sm =
     * this.jdoStateManager; if (sm != null) { sm.preSerialize(this); } }
     */
    public void addJDOPreSerializeMethod() {
        final String pcRootName = analyzer.getPCRootClassName();
        MethodVisitor mv = classNode.visitMethod(JDO_PC_jdoPreSerialize_Mods,
                JDO_PC_jdoPreSerialize_Name, JDO_PC_jdoPreSerialize_Sig, null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                JDO_SM_preSerialize_Name, JDO_SM_preSerialize_Sig);
        mv.visitLabel(l0);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * Build an interrogative method for the class.
     */
    public void addJDOIsPersistentMethod() {
        addJDOInterrogativeMethod(JDO_PC_jdoIsPersistent_Name,
                JDO_PC_jdoIsPersistent_Sig, JDO_PC_jdoIsPersistent_Mods,
                JDO_SM_isPersistent_Name, JDO_SM_isPersistent_Sig);
    }

    /**
     * Build an interrogative method for the class.
     */
    public void addJDOIsTransactionalMethod() {
        addJDOInterrogativeMethod(JDO_PC_jdoIsTransactional_Name,
                JDO_PC_jdoIsTransactional_Sig, JDO_PC_jdoIsTransactional_Mods,
                JDO_SM_isTransactional_Name, JDO_SM_isTransactional_Sig);
    }

    /**
     * Build an interrogative method for the class.
     */
    public void addJDOIsNewMethod() {
        addJDOInterrogativeMethod(JDO_PC_jdoIsNew_Name, JDO_PC_jdoIsNew_Sig,
                JDO_PC_jdoIsNew_Mods, JDO_SM_isNew_Name, JDO_SM_isNew_Sig);
    }

    /**
     * Build an interrogative method for the class.
     */
    public void addJDOIsDeletedMethod() {
        addJDOInterrogativeMethod(JDO_PC_jdoIsDeleted_Name,
                JDO_PC_jdoIsDeleted_Sig, JDO_PC_jdoIsDeleted_Mods,
                JDO_SM_isDeleted_Name, JDO_SM_isDeleted_Sig);
    }

    /**
     * Build an interrogative method for the class.
     */
    public void addJDOIsDirtyMethod() {
        addJDOInterrogativeMethod(JDO_PC_jdoIsDirty_Name,
                JDO_PC_jdoIsDirty_Sig, JDO_PC_jdoIsDirty_Mods,
                JDO_SM_isDirty_Name, JDO_SM_isDirty_Sig);
    }

    /**
     * Build an interrogative method named methodName for the class. public
     * boolean isXXX() { final StateManager sm = this.jdoStateManager; if (sm ==
     * null) return false; return sm.isXXXX(this); }
     */
    private void addJDOInterrogativeMethod(final String methodName,
            final String methodSig, final int accessFlags,
            final String delegateName, final String delegateSig)
    {
        final String pcRootName = analyzer.getPCRootClassName();
        ClassNode classNode = ((ClassInfoImpl) classInfo).getClassNode();
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                delegateName, delegateSig);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * Build an object query method for the class.
     */
    public void addJDOGetPersistenceManagerMethod() {
        addJDOObjectQueryMethod(JDO_PC_jdoGetPersistenceManager_Name,
                JDO_PC_jdoGetPersistenceManager_Sig,
                JDO_PC_jdoGetPersistenceManager_Mods,
                JDO_SM_getPersistenceManager_Name,
                JDO_SM_getPersistenceManager_Sig);
    }

    /**
     * Build an object query method for the class.
     */
    public void addJDOGetObjectIdMethod() {
        addJDOObjectQueryMethod(JDO_PC_jdoGetObjectId_Name,
                JDO_PC_jdoGetObjectId_Sig, JDO_PC_jdoGetObjectId_Mods,
                JDO_SM_getObjectId_Name, JDO_SM_getObjectId_Sig);
    }

    /**
     * Build an object query method for the class.
     */
    public void addJDOGetTransactionalObjectIdMethod() {
        addJDOObjectQueryMethod(JDO_PC_jdoGetTransactionalObjectId_Name,
                JDO_PC_jdoGetTransactionalObjectId_Sig,
                JDO_PC_jdoGetTransactionalObjectId_Mods,
                JDO_SM_getTransactionalObjectId_Name,
                JDO_SM_getTransactionalObjectId_Sig);
    }

    /**
     * Build an object query method for the class. public final XXX jdoGetYYY() {
     * final com.sun.persistence.StateManager sm = this.jdoStateManager; if (sm !=
     * null) { return sm.getYYY(this); } return null; }
     */
    private void addJDOObjectQueryMethod(final String methodName,
            final String methodSig, final int accessFlags,
            final String delegateName, final String delegateSig)
    {
        final String pcRootName = analyzer.getPCRootClassName();
        ClassNode classNode = ((ClassInfoImpl) classInfo).getClassNode();
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                delegateName, delegateSig);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l0);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * Build the jdoArrayArgumentIteration method for the class.
     */
    public void addJDOProvideFieldsMethod() {
        addJDOArrayArgumentIterationMethod(JDO_PC_jdoProvideFields_Name,
                JDO_PC_jdoProvideFields_Sig, JDO_PC_jdoProvideFields_Mods,
                JDO_PC_jdoProvideField_Name, JDO_PC_jdoProvideField_Sig);
    }

    /**
     * Build the jdoArrayArgumentIteration method for the class.
     */
    public void addJDOReplaceFieldsMethod() {
        addJDOArrayArgumentIterationMethod(JDO_PC_jdoReplaceFields_Name,
                JDO_PC_jdoReplaceFields_Sig, JDO_PC_jdoReplaceFields_Mods,
                JDO_PC_jdoReplaceField_Name, JDO_PC_jdoReplaceField_Sig);
    }

    /**
     * Build the jdoArrayArgumentIteration method for the class. public final
     * void jdoXXXFields(int[] fieldnumbers) { final int n =
     * fieldnumbers.length; for (int i = 0; i < n; i++) {
     * this.jdoXXXField(fieldnumbers[i]); } }
     */
    public void addJDOArrayArgumentIterationMethod(final String methodName,
            final String methodSig, final int accessFlags,
            final String delegateName, final String delegateSig)
    {
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNONNULL, l0);
        mv.visitTypeInsn(NEW, JAVA_IllegalArgumentException_Path);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1");
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_IllegalArgumentException_Path,
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 3);
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitInsn(IALOAD);
        mv.visitMethodInsn(INVOKEVIRTUAL, ownerClassName, delegateName,
                delegateSig);
        mv.visitIincInsn(3, 1);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IF_ICMPLT, l2);
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 4);
        mv.visitEnd();
    }

    /**
     * Build the sunjdoClassForName method for the class. public final Class
     * sunjdoClassForName(java.lang.String classname) { try { return
     * Class.forName(classname); catch (ClassNotFoundException ex) { throw new
     * NoClassDefFoundError(ex.getMessage()); } }
     */
    public void addSunJDOClassForNameMethod() {
        final String methodName = SUNJDO_PC_sunjdoClassForName_Name;
        final String methodSig = SUNJDO_PC_sunjdoClassForName_Sig;
        final int accessFlags = SUNJDO_PC_sunjdoClassForName_Mods;
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_Class_Path,
                JAVA_Class_forName_Name, JAVA_Class_forName_Sig);
        mv.visitInsn(ARETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitTypeInsn(NEW, JAVA_NoClassDefFoundError_Path);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_Throwable_Path,
                JAVA_Throwable_getMessage_Name, JAVA_Throwable_getMessage_Sig);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_NoClassDefFoundError_Path,
                JAVA_NoClassDefFoundError_NoClassDefFoundError_Name,
                JAVA_NoClassDefFoundError_NoClassDefFoundError_Sig);
        mv.visitInsn(ATHROW);
        mv.visitTryCatchBlock(l0, l1, l1, "java/lang/ClassNotFoundException");
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    // ----------------------------------------------------------------------
    // Specific Augmentation
    // ----------------------------------------------------------------------

    /**
     * Build the jdoGetManagedFieldCount method for the class. protected static
     * int jdoGetManagedFieldCount() { return jdoInheritedFieldCount + X; }
     */

    public void addJDOGetManagedFieldCountMethod() {

        final String methodName = JDO_PC_jdoGetManagedFieldCount_Name;
        final String methodSig = JDO_PC_jdoGetManagedFieldCount_Sig;
        final int accessFlags = JDO_PC_jdoGetManagedFieldCount_Mods;

        final int managedFieldCount = analyzer.getManagedFieldCount();
        affirm(managedFieldCount >= 0);

        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();

        if (analyzer.isAugmentableAsRoot()) {
            builderHelper.generateIntegerConstantInstruction(mv,
                    managedFieldCount);
        } else {
            mv.visitFieldInsn(GETSTATIC, ownerClassName,
                    "jdoInheritedFieldCount", "I");
            builderHelper.generateIntegerConstantInstruction(mv,
                    managedFieldCount);
            mv.visitInsn(IADD);
        }
        mv.visitInsn(IRETURN);
        mv.visitMaxs(2, 0);
        mv.visitEnd();
    }

    /**
     * Build the static initialization code for the class. static {
     * jdoInheritedFieldCount = 0 | super.jdoGetManagedFieldCount();
     * jdoFieldNames = new String[]{ ... }; jdoFieldTypes = new Class[]{ ... };
     * jdoFieldFlags = new byte[]{ ... }; jdoPersistenceCapableSuperclass = ...;
     * com.sun.persistence.JDOImplHelper.registerClass( XXX.class, jdoFieldNames,
     * jdoFieldTypes, jdoFieldFlags, jdoPersistenceCapableSuperclass, new XXX() ); }
     */

    public void addStaticInitialization() {
        final String methodName = JAVA_clinit_Name;
        final String methodSig = JAVA_clinit_Sig;
        final int accessFlags = JAVA_clinit_Mods;

        MethodMergerClassAdapter merger = null;
        MethodVisitor mv = null;
        MethodInfo staticInitMethod = null;
        if (analyzer.hasStaticInitializer()) {
            staticInitMethod = classInfo.removeMethod(methodName, methodSig,
                    null);
            affirm(staticInitMethod != null);
            merger = new MethodMergerClassAdapter(classNode);
            mv = merger.visitMethod(staticInitMethod.getAccessCode(),
                    staticInitMethod.getName(), staticInitMethod
                            .getDescriptor(), staticInitMethod.getSignature(),
                    staticInitMethod.getExceptions());
        } else {
            mv = classNode.visitMethod(accessFlags, methodName, methodSig,
                    null, null);
        }

        mv.visitCode();

        builderHelper.initJdoInheritedFieldCount(mv);
        builderHelper.initJdoFieldNames(mv);
        builderHelper.initJdoFieldTypes(mv);
        builderHelper.initJdoFieldFlags(mv);
        builderHelper.initJdoPersistenceCapableSuperclass(mv);
        builderHelper.initRegisterClass(mv);

        if (staticInitMethod != null) {
            MethodNode statMethodNode = ((MethodInfoImpl) staticInitMethod)
                    .getMethodNode();
            affirm(merger != null);
            statMethodNode.accept(merger);
        } else {
            mv.visitInsn(RETURN);
        }

        mv.visitMaxs(7, 0);
        mv.visitEnd();

        if (merger != null) {
            merger.mergeComplete();
        }
    }

    /**
     * Build the jdoNewInstance method for the class. public PersistenceCapable
     * jdoNewInstance(StateManager sm) { final XXX pc = new XXX(); pc.jdoFlags =
     * 1; // == LOAD_REQUIRED pc.jdoStateManager = sm; return pc; }
     */
    public void addJDONewInstanceMethod() {
        final String pcRootName = analyzer.getPCRootClassName();
        final String methodName = JDO_PC_jdoNewInstance_Name;
        final String methodSig = JDO_PC_jdoNewInstance_Sig;
        final int accessFlags = JDO_PC_jdoNewInstance_Mods;
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, ownerClassName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ownerClassName, "<init>", "()V");
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, pcRootName, JDO_PC_jdoFlags_Name,
                JDO_PC_jdoFlags_Sig);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }

    /**
     * Build the jdoNewInstance method for the class. public PersistenceCapable
     * jdoNewInstance(StateManager sm, Object oid) { final XXX pc = new XXX();
     * pc.jdoCopyKeyFieldsFromObjectId(oid); pc.jdoFlags = 1; // ==
     * LOAD_REQUIRED pc.jdoStateManager = sm; return pc; }
     */
    public void addJDONewInstanceOidMethod() {
        final String pcRootName = analyzer.getPCRootClassName();
        final String methodName = JDO_PC_jdoNewInstance_Object_Name;
        final String methodSig = JDO_PC_jdoNewInstance_Object_Sig;
        final int accessFlags = JDO_PC_jdoNewInstance_Object_Mods;
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, ownerClassName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ownerClassName, "<init>", "()V");
        mv.visitVarInsn(ASTORE, 3);

        final String pcKeyOwnerClassName = analyzer.getPCKeyOwnerClassName();
        affirm(pcKeyOwnerClassName != null);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, pcKeyOwnerClassName,
                JDO_PC_jdoCopyKeyFieldsFromObjectId_Name,
                JDO_PC_jdoCopyKeyFieldsFromObjectId_Sig);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, pcRootName, JDO_PC_jdoFlags_Name,
                JDO_PC_jdoFlags_Sig);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 4);
        mv.visitEnd();
    }

    /**
     * Build the jdoProvideField method for the class. public void
     * jdoProvideField(int fieldnumber) { final com.sun.persistence.StateManager
     * sm = this.jdoStateManager; switch(fieldnumber - jdoInheritedFieldCount) {
     * case 0: sm.providedXXXField(this, fieldnumber, this.yyy); return; case 1:
     * ... default: <if (isPCRoot) {> throw new
     * com.sun.persistence.JDOFatalInternalException(); <} else {>
     * super.jdoProvideField(fieldnumber); <}> } }
     */
    public void addJDOProvideFieldMethod() {
        final String pcRootName = analyzer.getPCRootClassName();
        final String methodName = JDO_PC_jdoProvideField_Name;
        final String methodSignature = JDO_PC_jdoProvideField_Sig;
        final int accessFlags = JDO_PC_jdoProvideField_Mods;
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSignature, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_PC_jdoStateManager_Sig);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitFieldInsn(GETSTATIC, ownerClassName,
                JDO_PC_jdoInheritedFieldCount_Name,
                JDO_PC_jdoInheritedFieldCount_Sig);
        mv.visitInsn(ISUB);

        final int managedFieldCount = analyzer.getManagedFieldCount();
        final String[] managedFieldSigs = analyzer.getAnnotatedFieldSigs();
        final String[] managedFieldNames = analyzer.getAnnotatedFieldNames();

        // affirm(managedFieldSigs.length >= managedFieldCount);
        // affirm(managedFieldRefs.length >= managedFieldCount);

        Label[] switchLabels = new Label[managedFieldCount];

        for (int i = 0; i < managedFieldCount; i++) {
            switchLabels[i] = new Label();
        }
        Label defaultSwitchLabel = new Label();

        mv.visitTableSwitchInsn(0, managedFieldCount - 1, defaultSwitchLabel,
                switchLabels);

        for (int i = 0; i < managedFieldCount; i++) {
            mv.visitLabel(switchLabels[i]);
            builderHelper.generateNullCheckForSM(mv);

            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ALOAD, 0);
            pcStateAccessGenerator.generateInstructionsToReadState(mv,
                    managedFieldNames[i], managedFieldSigs[i]);
            String provideReturnDesc = 
                BuilderHelper.isPrimitiveType(managedFieldSigs[i])
                || BuilderHelper.isStringType(managedFieldSigs[i])
                ? managedFieldSigs[i] : "Ljava/lang/Object;";
            mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                    "provided"
                            + BuilderHelper.getJavaTypeForSignature(
                                    managedFieldSigs[i], true) + "Field", "("
                            + JDO_PersistenceCapable_Sig + "I"
                            + provideReturnDesc + ")V");
            mv.visitInsn(RETURN);
        }

        mv.visitLabel(defaultSwitchLabel);
        generateDefaultOrSuperCallForSwitch(mv, methodName, methodSignature);
        mv.visitMaxs(5, 3);
        mv.visitEnd();

    }

    /**
     * Build the jdoReplaceField method for the class. public void
     * jdoReplaceField(int fieldnumber) { final com.sun.persistence.StateManager
     * sm = this.jdoStateManager; switch(fieldnumber - jdoInheritedFieldCount) {
     * case 0: this.yyy = (XXX)sm.replacingXXXField(this, fieldnumber); return;
     * case 1: ... default: <if (isPCRoot) {> throw new
     * com.sun.persistence.JDOFatalInternalException(); <} else {>
     * super.jdoReplaceField(fieldnumber); <}> } }
     */

    public void addJDOReplaceFieldMethod() {
        final String pcRootName = analyzer.getPCRootClassName();
        final String methodName = JDO_PC_jdoReplaceField_Name;
        final String methodSignature = JDO_PC_jdoReplaceField_Sig;
        final int accessFlags = JDO_PC_jdoReplaceField_Mods;
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSignature, null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ASTORE, 2);

        mv.visitVarInsn(ILOAD, 1);
        mv.visitFieldInsn(GETSTATIC, ownerClassName,
                JDO_PC_jdoInheritedFieldCount_Name,
                JDO_PC_jdoInheritedFieldCount_Sig);
        mv.visitInsn(ISUB);

        final int managedFieldCount = analyzer.getManagedFieldCount();
        final String[] managedFieldSigs = analyzer.getAnnotatedFieldSigs();
        final String[] managedFieldNames = analyzer.getAnnotatedFieldNames();

        // affirm(managedFieldSigs.length >= managedFieldCount);
        // affirm(managedFieldRefs.length >= managedFieldCount);

        Label[] switchLabels = new Label[managedFieldCount];

        for (int i = 0; i < managedFieldCount; i++) {
            switchLabels[i] = new Label();
        }
        Label defaultSwitchLabel = new Label();

        mv.visitTableSwitchInsn(0, managedFieldCount - 1, defaultSwitchLabel,
                switchLabels);

        for (int i = 0; i < managedFieldCount; i++) {
            mv.visitLabel(switchLabels[i]);
            builderHelper.generateNullCheckForSM(mv);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);

            String javaTypeStr = BuilderHelper.getJavaTypeForSignature(
                    managedFieldSigs[i], true);

            String fetchReturnDesc = 
                BuilderHelper.isPrimitiveType(managedFieldSigs[i])
                || BuilderHelper.isStringType(managedFieldSigs[i])
                ? managedFieldSigs[i] : "Ljava/lang/Object;";
            mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                    "replacing" + javaTypeStr + "Field", "("
                            + JDO_PersistenceCapable_Sig + "I)"
                            + fetchReturnDesc);
            if (!BuilderHelper.isPrimitiveType(managedFieldSigs[i])) {
                if (!BuilderHelper.isStringType(managedFieldSigs[i])) {
                    mv.visitTypeInsn(CHECKCAST, BuilderHelper
                            .getJavaTypeCastSignature(managedFieldSigs[i]));
                }
            }
            pcStateAccessGenerator.generateInstructionsToWriteState(mv,
                    managedFieldNames[i], managedFieldSigs[i]);
            mv.visitInsn(RETURN);
        }

        mv.visitLabel(defaultSwitchLabel);
        generateDefaultOrSuperCallForSwitch(mv, methodName, methodSignature);

        mv.visitMaxs(4, 3);
        mv.visitEnd();
    }

    public void addJDOCopyFieldsMethod() {
        final String pcRootName = analyzer.getPCRootClassName();
        final String methodName = JDO_PC_jdoCopyFields_Name;
        final String methodSig = JDO_PC_jdoCopyFields_Sig;
        final int accessFlags = JDO_PC_jdoCopyFields_Mods;
        MethodVisitor mv = classNode.visitMethod(JDO_PC_jdoCopyFields_Mods,
                methodName, JDO_PC_jdoCopyFields_Sig, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ownerClassName,
                JDO_PC_jdoStateManager_Name, JDO_StateManager_Sig);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNONNULL, l0);
        mv.visitTypeInsn(NEW, JAVA_IllegalStateException_Path);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg0.jdoStateManager");
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_IllegalStateException_Path,
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, ownerClassName);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNE, l1);
        mv.visitTypeInsn(NEW, JAVA_IllegalArgumentException_Path);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1");
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_IllegalArgumentException_Path,
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 2);
        Label l2 = new Label();
        mv.visitJumpInsn(IFNONNULL, l2);
        mv.visitTypeInsn(NEW, JAVA_IllegalArgumentException_Path);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg2");
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_IllegalArgumentException_Path,
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, ownerClassName);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitFieldInsn(GETFIELD, ownerClassName,
                JDO_PC_jdoStateManager_Name, JDO_StateManager_Sig);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ownerClassName,
                JDO_PC_jdoStateManager_Name, JDO_StateManager_Sig);
        Label l3 = new Label();
        mv.visitJumpInsn(IF_ACMPEQ, l3);
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("arg1.jdoStateManager");
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_IllegalArgumentException_Path,
                "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitVarInsn(ISTORE, 4);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 5);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitVarInsn(ILOAD, 4);
        Label l5 = new Label();
        mv.visitJumpInsn(IF_ICMPGE, l5);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitInsn(IALOAD);
        mv.visitMethodInsn(INVOKEVIRTUAL, ownerClassName, "jdoCopyField", "(L"
                + ownerClassName + ";I)V");
        mv.visitIincInsn(5, 1);
        mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l5);
        mv.visitInsn(RETURN);
        mv.visitMaxs(4, 6);
        mv.visitEnd();
    }

    /**
     * Build the jdoCopyField method for the class. protected final void
     * jdoCopyField(XXX pc, int fieldnumber) { switch(fieldnumber -
     * jdoInheritedFieldCount) { case 0: this.yyy = pc.yyy; return; case 1: ...
     * default: <if (isPCRoot) {> throw new
     * com.sun.persistence.JDOFatalInternalException(); <} else {>
     * super.jdoCopyField(pc, fieldnumber); <}> } }
     */

    public void addJDOCopyFieldMethod() {

        String methodName = JDO_PC_jdoCopyField_Name;
        final int accessFlags = JDO_PC_jdoCopyField_Mods;
        final String methodSignature = JDONameHelper
                .getJDO_PC_jdoCopyField_Sig(ownerClassName);

        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSignature, null, null);
        mv.visitCode();

        mv.visitVarInsn(ILOAD, 2);
        mv.visitFieldInsn(GETSTATIC, ownerClassName,
                JDO_PC_jdoInheritedFieldCount_Name,
                JDO_PC_jdoInheritedFieldCount_Sig);
        mv.visitInsn(ISUB);

        final int managedFieldCount = analyzer.getManagedFieldCount();
        final String[] managedFieldSigs = analyzer.getAnnotatedFieldSigs();
        final String[] managedFieldNames = analyzer.getAnnotatedFieldNames();
        affirm(managedFieldSigs.length >= managedFieldCount);

        Label[] switchLabels = new Label[managedFieldCount];

        for (int i = 0; i < managedFieldCount; i++) {
            switchLabels[i] = new Label();
        }
        Label defaultSwitchLabel = new Label();

        if (managedFieldCount <= 1) {
            mv.visitLookupSwitchInsn(defaultSwitchLabel, new int[] { 0 },
                    switchLabels);
        } else {
            mv.visitTableSwitchInsn(0, managedFieldCount - 1,
                    defaultSwitchLabel, switchLabels);
        }

        for (int i = 0; i < managedFieldCount; i++) {
            mv.visitLabel(switchLabels[i]);

            builderHelper.generateNullCheckForPCArgument(mv);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            pcStateAccessGenerator.generateInstructionsToReadState(mv, 
                    managedFieldNames[i], managedFieldSigs[i]);
            pcStateAccessGenerator.generateInstructionsToWriteState(mv, 
                    managedFieldNames[i], managedFieldSigs[i]);
            mv.visitInsn(RETURN);
        }

        mv.visitLabel(defaultSwitchLabel);
        appendEndCopyField(mv, methodName);
        mv.visitMaxs(3, 3); // ignored
        mv.visitEnd();
    }

    /**
     * Build the jdoNewObjectIdInstance method for the class. public
     * java.lang.Object jdoNewObjectIdInstance() { return new XXX(); }
     */
    public void addJDONewObjectIdInstanceMethod() {
        pkMethodsGenerator.generateJDONewObjectIdInstanceMethod();
    }

    /**
     * Build the jdoNewObjectIdInstance method for the class. public
     * java.lang.Object jdoNewObjectIdInstance(String str) { return new
     * XXX(str); }
     */
    public void addJDONewObjectIdInstanceStringMethod() {
        pkMethodsGenerator.addJDONewObjectIdInstanceStringMethod();
    }

    /**
     * Build the jdoNewObjectIdInstance method for the class. public
     * java.lang.Object jdoNewObjectIdInstance(String str) { return new
     * XXX(str); }
     */
    public void addJDONewObjectIdInstanceObjectMethod() {
        pkMethodsGenerator.addJDONewObjectIdInstanceObjectMethod();
    }

    public void addJDOCopyKeyFieldsToObjectIdMethod() {
        pkMethodsGenerator.addJDOCopyKeyFieldsToObjectIdMethod();
    }

    public void addJDOCopyKeyFieldsFromObjectIdMethod() {
        pkMethodsGenerator.addJDOCopyKeyFieldsFromObjectIdMethod();
    }

    public void addJDOCopyKeyFieldsToObjectIdOIFSMethod() {
        pkMethodsGenerator.addJDOCopyKeyFieldsToFromObjectIdOIFSMethod(true);
    }

    public void addJDOCopyKeyFieldsFromObjectIdOIFCMethod() {
        pkMethodsGenerator.addJDOCopyKeyFieldsToFromObjectIdOIFSMethod(false);
    }

    /**
     * Build an accessor method for direct read access. static xxx final YYY
     * jdoGetyyy(XXX instance) { // augmentation: grant direct read access
     * return instance.yyy; }
     */

    public void addJDODirectReadAccessMethod(String methodName,
            String methodSig, int accessFlags, int fieldIndex)
    {
        FieldInfo fieldInfo = builderHelper.getAnnotatedFieldRefs()[fieldIndex];
        String fieldType = fieldInfo.getDescriptor();
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        pcStateAccessGenerator.appendDirectReadReturn(mv, fieldInfo);
        mv.visitMaxs(3, 3); // ignored
        mv.visitEnd();
    }

    public void addJDOCheckedReadAccessMethod(String methodName,
            String methodSig, int accessFlags, int fieldIndex)
    {
        final String pcRootName = analyzer.getPCRootClassName();
        FieldInfo fieldInfo = builderHelper.getAnnotatedFieldRefs()[fieldIndex];
        String fieldType = fieldInfo.getDescriptor();
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoFlags_Name,
                JDO_PC_jdoFlags_Sig);
        Label l0 = new Label();
        mv.visitJumpInsn(IFGT, l0);
        pcStateAccessGenerator.appendDirectReadReturn(mv, fieldInfo);
        mv.visitLabel(l0);
        appendMediatedReadAccess(mv, fieldIndex, fieldInfo, 1);
        mv.visitMaxs(3, 3); // ignored
        mv.visitEnd();
    }

    /**
     * Build an accessor method for mediated read access. static xxx final YYY
     * jdoGetyyy(XXX instance) { // augmentation: mediate read access final
     * com.sun.persistence.StateManager sm = instance.jdoStateManager; if (sm ==
     * null) { return instance.yyy; } if (sm.isLoaded(instance,
     * instance.jdoInheritedFieldCount + y)) { return instance.yyy; } return
     * (YYY)sm.getYYYField(instance, instance.jdoInheritedFieldCount + x,
     * instance.yyy); }
     */
    public void addJDOMediatedReadAccessMethod(String methodName,
            String methodSig, int accessFlags, int fieldIndex)
    {
        FieldInfo fieldInfo = builderHelper.getAnnotatedFieldRefs()[fieldIndex];
        String fieldType = fieldInfo.getDescriptor();
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        appendMediatedReadAccess(mv, fieldIndex, fieldInfo, 1);
        mv.visitMaxs(3, 3); // ignored
        mv.visitEnd();
    }

    public void addJDODirectWriteAccessMethod(String methodName,
            String methodSig,
            // int accessMods,
            int accessFlags, int fieldIndex)
    {
        FieldInfo fieldInfo = builderHelper.getAnnotatedFieldRefs()[fieldIndex];
        String fieldType = fieldInfo.getDescriptor();
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        pcStateAccessGenerator.appendDirectWriteReturn(mv, fieldInfo);
        mv.visitMaxs(3, 3); // ignored
        mv.visitEnd();
    }

    public void addJDOCheckedWriteAccessMethod(String methodName,
            String methodSig,
            // int accessMods,
            int accessFlags, int fieldIndex)
    {
        final String pcRootName = analyzer.getPCRootClassName();
        FieldInfo fieldInfo = builderHelper.getAnnotatedFieldRefs()[fieldIndex];
        String fieldType = fieldInfo.getDescriptor();
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoFlags_Name,
                JDO_PC_jdoFlags_Sig);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        pcStateAccessGenerator.appendDirectWriteReturn(mv, fieldInfo);
        mv.visitLabel(l0);

        final int fieldSize = ((fieldType.equals("J") || fieldType.equals("D")) ? 2
                : 1);
        final int varStart = fieldSize + 1;
        appendMediatedWriteAccess(mv, fieldIndex, fieldInfo, varStart);

        mv.visitMaxs(5, 3);
        mv.visitEnd();
    }

    public void addJDOMediatedWriteAccessMethod(String methodName,
            String methodSig,
            // int accessMods,
            int accessFlags, int fieldIndex)
    {
        FieldInfo fieldInfo = builderHelper.getAnnotatedFieldRefs()[fieldIndex];
        String fieldType = fieldInfo.getDescriptor();
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, null);

        final int fieldSize = ((fieldType.equals("J") || fieldType.equals("D")) ? 2
                : 1);
        final int varStart = fieldSize + 1;
        appendMediatedWriteAccess(mv, fieldIndex, fieldInfo, varStart);

        mv.visitMaxs(5, 3);
        mv.visitEnd();
    }

    public void addJDOPreSerializeCall(String methodName, String methodSig) {
        affirm(methodName != null);
        affirm(methodSig != null);

        MethodInfoImpl existingMethod = null;

        existingMethod = (MethodInfoImpl) classInfo.findMethod(methodName,
                methodSig, null);
        affirm(existingMethod != null,
                "Attempt to add code to a non-existent method.");

        int accessFlags = existingMethod == null ? ACC_PRIVATE : existingMethod
                .getAccessCode();
        // check the found method
        affirm(!AccessUtil.isAbstract(accessFlags),
                "Attempt to add code to an abstract method.");
        affirm(!AccessUtil.isNative(accessFlags),
                "Attempt to add code to a native method.");

        MethodNode existingMethodNode = existingMethod.getMethodNode();
        List<String> exceptionList = existingMethodNode.exceptions;
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodSig, null, exceptionList.toArray(new String[0]));
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, ownerClassName,
                JDO_PC_jdoPreSerialize_Name, JDO_PC_jdoPreSerialize_Sig);

        System.out.println("Appending existing "
                + existingMethodNode.instructions.size() + " instructions...");
        ((MethodNode) mv).instructions.addAll(existingMethodNode.instructions);

        // mv.visitMaxs(existingMethodNode.maxStack,
        // existingMethodNode.maxLocals);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public void annotateForPropertyBasedPersistence() {
        final int managedFieldCount = analyzer.getManagedFieldCount();
        final String[] managedFieldDescs = analyzer.getAnnotatedFieldSigs();
        final String[] managedFieldNames = analyzer.getAnnotatedFieldNames();
        
        ClassInfoFactory factory = null;
        try {
            factory = ClassInfoFactoryFactory.getClassInfoFactory();
        } catch (Throwable th) {
            
        }
        TypeHelper typeHelper = factory.createTypeHelper();
        
        for (int i=0; i<managedFieldCount; i++) {
            String propertyName = managedFieldNames[i];
            String desc = managedFieldDescs[i];
            String getterName = "get" + initUpperCase(propertyName);
            String setterName = "set" + initUpperCase(propertyName);
            //String type = typeHelper.getReturnTypeAsJavaString();
            MethodInfo getter = classInfo.findMethod(getterName,
                    "()"+managedFieldDescs[i], null);
            MethodInfo setter = classInfo.findMethod(setterName,
                    "("+managedFieldDescs[i]+")V", null);
            System.out.println("ManagedField["+i+"]: " + getter);
            System.out.println("ManagedField["+i+"]: " + setter);
            
            if (getter != null) {
                handleMethodForPropertyBasedPersistence(propertyName, desc, getter, true);
            }
            
            if (setter != null) {
                handleMethodForPropertyBasedPersistence(propertyName, desc, setter, false);
            }
        }
    }
    
    /** ********************************************************************************* */
    /** ********************************************************************************* */
    /**
     * *************** ONLY PRIVATE METHODS ARE LISTED
     * BELOW****************************
     */
    /** ********************************************************************************* */
    /** ********************************************************************************* */

    private static String initUpperCase(String str) {
        String firstUpper = "" + Character.toUpperCase(str.charAt(0));
        if (str.length() == 1) {
            return firstUpper;
        }
        
        return firstUpper + str.substring(1);
    }
    
    private void generateDefaultOrSuperCallForSwitch(MethodVisitor mv,
            String methodName, String methodSignature)
    {
        if (analyzer.isAugmentableAsRoot()) {
            mv.visitTypeInsn(NEW, JAVA_IllegalArgumentException_Path);
            mv.visitInsn(DUP);
            mv.visitLdcInsn("arg2");
            mv.visitMethodInsn(INVOKESPECIAL,
                    JAVA_IllegalArgumentException_Path, "<init>",
                    "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
        } else {
            final String superClassName = classInfo.getSuperClassName();
            affirm(superClassName != null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, superClassName, methodName,
                    methodSignature);
            mv.visitInsn(RETURN);
        }
    }

    private void appendEndCopyField(MethodVisitor mv, String methodName) {

        // throw exception or delegate to PC superclass
        final boolean isPCRoot = analyzer.isAugmentableAsRoot();
        if (isPCRoot) {
            mv.visitTypeInsn(NEW, JAVA_IllegalArgumentException_Path);
            mv.visitInsn(DUP);
            mv.visitLdcInsn("arg2");
            mv.visitMethodInsn(INVOKESPECIAL,
                    JAVA_IllegalArgumentException_Path, "<init>",
                    "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
        } else {
            // call super.jdoCopyField(XXX, int)
            // must use pcSuperClass (not the immediate superclass) in order
            // to match formal parameter of jdoCopyField
            final String pcSuperClassName = analyzer.getPCSuperClassName();
            System.out.println("For " + ownerClassName + " pcRoot: "
                    + pcSuperClassName);
            affirm(pcSuperClassName != null);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, pcSuperClassName, methodName,
                    "(L" + pcSuperClassName + ";I)V");

            mv.visitInsn(RETURN);
        }
    }

    /**
     * Build an accessor method for direct read access. static xxx final YYY
     * jdoGetyyy(XXX instance) { // augmentation: grant direct read access
     * return instance.yyy; }
     */
    private void appendMediatedReadAccess(MethodVisitor mv, int fieldIndex,
            FieldInfo fieldInfo, int varStart)
    {

        final String fieldName = fieldInfo.getName();
        final String fieldSig = fieldInfo.getDescriptor();

        // affirm(sig != null && sig.length() > 0);
        final String pcRootName = analyzer.getPCRootClassName();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ASTORE, varStart);
        mv.visitVarInsn(ALOAD, varStart);
        Label nonNullLabel = new Label();
        mv.visitJumpInsn(IFNONNULL, nonNullLabel);
        pcStateAccessGenerator.appendDirectReadReturn(mv, fieldInfo);
        mv.visitLabel(nonNullLabel);
        mv.visitVarInsn(ALOAD, varStart);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, ownerClassName, "jdoInheritedFieldCount",
                "I");
        BuilderHelper.generateIntegerConstantInstruction(mv, fieldIndex);
        mv.visitInsn(IADD);
        mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                JDO_SM_isLoaded_Name, JDO_SM_isLoaded_Sig);
        Label eqLabel = new Label();
        mv.visitJumpInsn(IFEQ, eqLabel);
        pcStateAccessGenerator.appendDirectReadReturn(mv, fieldInfo);
        mv.visitLabel(eqLabel);
        mv.visitVarInsn(ALOAD, varStart);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, ownerClassName,
                JDO_PC_jdoInheritedFieldCount_Name,
                JDO_PC_jdoInheritedFieldCount_Sig);
        BuilderHelper.generateIntegerConstantInstruction(mv, fieldIndex);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ALOAD, 0);
        pcStateAccessGenerator.generateInstructionsToReadState(mv, fieldInfo);
        String javaTypeStr = BuilderHelper.getJavaTypeForSignature(fieldSig,
                true);

        if (BuilderHelper.isPrimitiveType(fieldSig)
                || BuilderHelper.isStringType(fieldSig)) {
            mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path, "get"
                    + javaTypeStr + "Field", "(" + JDO_PersistenceCapable_Sig
                    + "I" + fieldSig + ")" + fieldSig);
            // No type cast check required!!
        } else {
            mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                    JDO_SM_getObjectField_Name, JDO_SM_getObjectField_Sig);
            String fieldCastPath = BuilderHelper
                    .getJavaTypeCastSignature(fieldSig);
            mv.visitTypeInsn(CHECKCAST, fieldCastPath);

        }
        mv.visitInsn(BuilderHelper.getReturnOpcode(fieldSig));
    }

    private void appendMediatedWriteAccess(MethodVisitor mv, int fieldIndex,
            FieldInfo fieldInfo, int varStart)
    {
        final String fieldName = fieldInfo.getName();
        final String fieldType = fieldInfo.getDescriptor();
        final String pcRootName = analyzer.getPCRootClassName();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, pcRootName, JDO_PC_jdoStateManager_Name,
                JDO_StateManager_Sig);
        mv.visitVarInsn(ASTORE, varStart);
        mv.visitVarInsn(ALOAD, varStart);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNONNULL, l1);
        pcStateAccessGenerator.appendDirectWriteReturn(mv, fieldInfo);
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, varStart);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, ownerClassName,
                JDO_PC_jdoInheritedFieldCount_Name,
                JDO_PC_jdoInheritedFieldCount_Sig);
        BuilderHelper.generateIntegerConstantInstruction(mv, fieldIndex);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ALOAD, 0);
        pcStateAccessGenerator.generateInstructionsToReadState(mv, fieldInfo);
        mv.visitVarInsn(BuilderHelper.getALoadOpcode(fieldType), 1);

        String javaTypeStr = BuilderHelper.getJavaTypeForSignature(fieldType,
                true);

        if (BuilderHelper.isStringOrPrimitiveType(fieldType)) {
            mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path, "set"
                    + javaTypeStr + "Field", "(" + JDO_PersistenceCapable_Sig + "I"
                    + fieldType + fieldType + ")V");
        } else {
            mv.visitMethodInsn(INVOKEINTERFACE, JDO_StateManager_Path,
                    JDO_SM_setObjectField_Name, 
                    JDO_SM_setObjectField_Sig);
        }
        mv.visitInsn(RETURN);

    }

    private MethodNode findMethodNode(String methodName, String methodDesc) {
        MethodNode node = null;
        for (MethodNode method : (List<MethodNode>) classNode.methods) {
            if (methodName.equals(method.name)) {
                if (methodDesc.equals(method.desc)) {
                    node = method;
                }
            }
        }
        return node;
    }
    
    void handleMethodForPropertyBasedPersistence(String propertyName, String desc,
            MethodInfo methodInfo, boolean isGetter) {
        //Add a new method
        //if (true) { return; }
        int accessFlags = methodInfo.getAccessCode();
        String methodName = (isGetter ? JDOConstants.ORIG_GETTER_PREFIX : JDOConstants.ORIG_SETTER_PREFIX)
            + initUpperCase(propertyName);
        String methodDesc = methodInfo.getDescriptor();
        String[] exceptions = methodInfo.getExceptions();
        
        MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
                methodDesc, null, exceptions);
        

        if (isGetter) {
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            String retType = Type.getReturnType(desc).toString();
            mv.visitMethodInsn(INVOKESTATIC, ownerClassName, "jdoGet" + propertyName,
                    "(L"+ownerClassName+";)"+retType);
            mv.visitInsn(builderHelper.getReturnOpcode(desc));
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        } else {
            
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);        
            mv.visitVarInsn(builderHelper.getALoadOpcode(desc), 1);
            
            mv.visitMethodInsn(INVOKESTATIC, ownerClassName, "jdoSet" + propertyName,
                    "(L" + ownerClassName + ";" + desc + ")V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        
        classInfo.removeMethod(methodInfo);
        classInfo.removeMethod(methodName, methodDesc, null);
        
        MethodNode userMethod = ((MethodInfoImpl) methodInfo).getMethodNode();
        MethodNode syntheticMethod = (MethodNode) mv;
        syntheticMethod.name = userMethod.name;
        userMethod.name = methodName;
        classNode.methods.add(userMethod);
        classNode.methods.add(syntheticMethod);
        //swapMethods(userMethod, syntheticMethod);
        
        
    }
    
    private void swapMethods(MethodNode m1, MethodNode m2) {
        
        
        String tempMethodName = m1.name;
        
        List tempvisibleAnnotations = m1.visibleAnnotations;
        List tempinvisibleAnnotations = m1.invisibleAnnotations;
        List tempattrs = m1.attrs;
        Object tempannotationDefault = m1.annotationDefault;
        List[] tempvisibleParameterAnnotations = m1.visibleParameterAnnotations;
        List[] tempinvisibleParameterAnnotations = m1.invisibleParameterAnnotations;
        List tempinstructions = m1.instructions;
        List temptryCatchBlocks = m1.tryCatchBlocks;
        int tempmaxStack = m1.maxStack;
        int tempmaxLocals = m1.maxLocals;
        List templocalVariables = m1.localVariables;
        List templineNumbers = m1.lineNumbers;
        
        m1.name = m2.name;
        m1.visibleAnnotations = m2.visibleAnnotations;
        m1.invisibleAnnotations = m2.invisibleAnnotations;
        m1.attrs = m2.attrs;
        m1.annotationDefault = m2.annotationDefault;
        m1.visibleParameterAnnotations = m2.visibleParameterAnnotations;
        m1.invisibleParameterAnnotations = m2.invisibleParameterAnnotations;
        m1.instructions.clear();     
        m1.instructions.addAll(m2.instructions);
        m1.tryCatchBlocks.clear();
        m1.tryCatchBlocks.addAll(m2.tryCatchBlocks);
        m1.maxStack = m2.maxStack;
        m1.maxLocals = m2.maxLocals;
        m1.localVariables = m2.localVariables;
        m1.lineNumbers = m2.lineNumbers;
        
        m2.name = tempMethodName;
        m2.visibleAnnotations = tempvisibleAnnotations;
        m2.invisibleAnnotations = tempinvisibleAnnotations;
        m2.attrs = tempattrs;
        m2.annotationDefault = tempannotationDefault;
        m2.visibleParameterAnnotations = tempvisibleParameterAnnotations;
        m2.invisibleParameterAnnotations = tempinvisibleParameterAnnotations;
        m2.instructions.clear();
        m2.instructions.addAll(tempinstructions);
        m2.tryCatchBlocks.clear();
        m2.tryCatchBlocks.addAll(temptryCatchBlocks);
        m2.maxStack = tempmaxStack;
        m2.maxLocals = tempmaxLocals;
        m2.localVariables = templocalVariables;
        m2.lineNumbers = templineNumbers;
    }
}
