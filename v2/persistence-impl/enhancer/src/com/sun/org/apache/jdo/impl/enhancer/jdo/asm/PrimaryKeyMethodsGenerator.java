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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceSignatureVisitor;

import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.impl.enhancer.jdo.ClassFileAnalyzer;

import com.sun.org.apache.jdo.impl.enhancer.util.Support;
import com.sun.org.apache.jdo.impl.enhancer.classfile.asm.ClassInfoImpl;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.EnhancerConstants;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.JDOConstants;

/**
 * PrimaryKeyMethodsGenerator is responsible for generating the bytecodes for
 *  methods that accesses primary key fields
 *
 * @author Mahesh Kannan
 *
 */
class PrimaryKeyMethodsGenerator
	extends Support
	implements Opcodes, JDOConstants, EnhancerConstants
{
	
	private ClassInfo classInfo;
	
	private ClassFileAnalyzer analyzer;
	
	private String ownerClassName;
	
	private String pkSignature;
	
	private ClassNode classNode;
	
	private BuilderHelper builderHelper;
	
    private PersistentStateAccessGenerator pcStateAccessGenerator;
    
    private boolean isPropertyBasedAccess;
    
    private PersistentStateAccessGenerator _keyStateAccessGenerator;
    
    private String pkClassName;
    
    private OIDMethodsHandler oidMethodsHandler;
    
    private static boolean debug = true;
    
	PrimaryKeyMethodsGenerator(ClassInfo classInfo, ClassFileAnalyzer analyzer,
			BuilderHelper builderHelper,
            PersistentStateAccessGenerator pctateAccessGenerator,
            boolean isPropertyBasedAccess) {
		
		this.classInfo = classInfo;
		this.analyzer = analyzer;
		this.builderHelper = builderHelper;
        this.pcStateAccessGenerator = pctateAccessGenerator;
        this.isPropertyBasedAccess = isPropertyBasedAccess;
        
		this.ownerClassName = classInfo.getName();
		this.classNode = ((ClassInfoImpl) classInfo).getClassNode();
        
        System.out.println("[PKMethodGenerator] PKClassName: " + analyzer.getKeyClassName());
        iniitializeOIDMethodsHandler();
	}
    
    private void iniitializeOIDMethodsHandler() {
        pkClassName = analyzer.getKeyClassName();
        System.out.println("[**PKMethodsGenerator**]: " + pkClassName);
        
        if (pkClassName == null) {
            //DataStore Identity??
        } else if (SingleFieldIdentityOIDMethodsHandler.isSingleFieldIdentityType(pkClassName)) {
            final FieldInfo pkField = builderHelper.getKeyClassKeyFieldRefs()[0];
            final int pkFieldIndex = builderHelper.getKeyFieldIndexes()[0];
            String pkFieldOwnerClassName = analyzer.getPCKeyOwnerClassName();
            String pkFieldName = pkField.getName();
            String pkFieldType = pkField.getDescriptor();
            if (debug) {
                System.out.println("\tisSingleFieldIdentity  : TRUE");
                System.out.println("\townerClassName         : " + ownerClassName);
                System.out.println("\tpkFieldOwnerClassName  : " + pkFieldOwnerClassName);
                System.out.println("\tpkFieldIndex           : " + pkFieldIndex);
                System.out.println("\tpkFieldName            : " + pkFieldName);
                System.out.println("\tpkFieldType            : " + pkFieldType);
            }
            oidMethodsHandler = new SingleFieldIdentityOIDMethodsHandler(pkFieldOwnerClassName,
                    pkFieldName, pkFieldType, pkFieldIndex, pcStateAccessGenerator);
            pkClassName = oidMethodsHandler.getKeyClassName();
        } else {
            if (debug) {
                System.out.println("\tisSingleFieldIdentity  : FALSE");
                System.out.println("\townerClassName         : " + ownerClassName);
                System.out.println("\tpkFieldOwnerClassName  : " + analyzer.getPCKeyOwnerClassName());
                System.out.println("\tpkFieldName            : " + pkClassName);
            }
            _keyStateAccessGenerator = (isPropertyBasedAccess)
                ? new PropertyBasedStateAccessGenerator(pkClassName)
                : new FieldBasedStateAccessGenerator(pkClassName);
            oidMethodsHandler = new CompositePKOIDMethodsHandler(pkClassName,
                    pcStateAccessGenerator, _keyStateAccessGenerator);
        }
    }
    
	void generateJDONewObjectIdInstanceMethod() {
        final String methodName = JDO_PC_jdoNewObjectIdInstance_Name;   
        final String methodSig = JDO_PC_jdoNewObjectIdInstance_Sig;
        final int accessFlags = JDO_PC_jdoNewObjectIdInstance_Mods;
        
        MethodVisitor mv = classNode.visitMethod(ACC_PUBLIC,
        		methodName, methodSig, null, null);
        mv.visitCode();
        if (pkClassName == null){
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
        } else {
            oidMethodsHandler.generateNewObjectIDInstance(mv);
        }
        mv.visitEnd();
	}
	
	void addJDONewObjectIdInstanceStringMethod() {
        final String methodName = JDO_PC_jdoNewObjectIdInstance_String_Name;
        final String methodSig = JDO_PC_jdoNewObjectIdInstance_String_Sig;
        final int accessFlags = JDO_PC_jdoNewObjectIdInstance_String_Mods;
		MethodVisitor mv = classNode.visitMethod(accessFlags,
				methodName,	methodSig, null, null);
		mv.visitCode();
        if (pkClassName == null){
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
        } else {
            oidMethodsHandler.generateNewObjectIdInstanceStringMethod(mv);
        }
		mv.visitEnd();
    }
    
    void addJDONewObjectIdInstanceObjectMethod() {
        final String methodName = JDO_PC_jdoNewObjectIdInstance_Object_Name;
        final String methodSig = JDO_PC_jdoNewObjectIdInstance_Object_Sig;
        final int accessFlags = JDO_PC_jdoNewObjectIdInstance_Object_Mods;
        MethodVisitor mv = classNode.visitMethod(accessFlags,
                methodName, methodSig, null, null);
        mv.visitCode();
        if (pkClassName == null){
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
        } else {
            oidMethodsHandler.generateNewObjectIDInstanceObjectMethod(mv, ownerClassName,
                    JDO_ObjectIdFieldSupplier_Path);
        }
        mv.visitEnd();
	}
	
	void addJDOCopyKeyFieldsToObjectIdMethod() {
		addJDOCopyKeyFieldsToFromObjectIdMethod(true);
	}
	
	void addJDOCopyKeyFieldsFromObjectIdMethod() {
		addJDOCopyKeyFieldsToFromObjectIdMethod(false);
	}
	
	private void addJDOCopyKeyFieldsToFromObjectIdMethod(boolean toOid) {
        final String methodName;
        final String methodSig;
        final int accessFlags;
        if (toOid) {
            methodName = JDO_PC_jdoCopyKeyFieldsToObjectId_Name;
            methodSig = JDO_PC_jdoCopyKeyFieldsToObjectId_Sig;
            accessFlags = JDO_PC_jdoCopyKeyFieldsToObjectId_Mods;        
        } else {
            methodName = JDO_PC_jdoCopyKeyFieldsFromObjectId_Name;
            methodSig = JDO_PC_jdoCopyKeyFieldsFromObjectId_Sig;
            accessFlags = JDO_PC_jdoCopyKeyFieldsFromObjectId_Mods;
        }
			
		MethodVisitor mv = classNode.visitMethod(accessFlags, methodName,
				methodSig, null, null);
	
		mv.visitCode();
        if (pkClassName == null){
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
            return;
        }
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(INSTANCEOF, pkClassName);
		Label l0 = new Label();
		mv.visitJumpInsn(IFNE, l0);
		mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
		mv.visitInsn(DUP);
		mv.visitLdcInsn("arg1");
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, pkClassName);
		mv.visitVarInsn(ASTORE, 2);
        
		// check argument or delegate to superclass
        final boolean isPCRoot = analyzer.isAugmentableAsRoot();
        if (!isPCRoot) {
			//TBD
        }
		
        // get types of and field references of the key fields
        final int keyFieldCount = analyzer.getKeyFieldCount();
        final FieldInfo[] keyFieldRefs = builderHelper.getKeyFieldRefs();
        final FieldInfo[] keyClassKeyFieldRefs = builderHelper.getKeyClassKeyFieldRefs();
        //affirm(keyFieldRefs.length == keyFieldCount);
        //affirm(keyClassKeyFieldRefs.length == keyFieldCount);

        // generate the assignment statements
        int maxFieldSize = 0;
        for (int i = 0; i < keyFieldCount; i++) {
            // assign key field
            final FieldInfo thisClassKeyRef = keyFieldRefs[i];
            final FieldInfo keyClassKeyRef = keyClassKeyFieldRefs[i];
            //affirm(thisClassKeyRef != null);
            //affirm(keyClassKeyRef != null);
            if (toOid) {
                oidMethodsHandler.generateCopyToOID(mv, thisClassKeyRef, keyClassKeyRef);
            } else {
                oidMethodsHandler.generateCopyFromOID(mv, thisClassKeyRef, keyClassKeyRef);
            }
        }
		mv.visitInsn(RETURN);
		mv.visitMaxs(3, 3);
    
	}
	
	void addJDOCopyKeyFieldsToFromObjectIdOIFSMethod(boolean toOid) {
        final String methodName;
        final String methodSignature;
        final int accessFlags;
        if (toOid) {
            methodName = JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Name;
			methodSignature = JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Sig;
            accessFlags = JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Mods;        
        } else {
            methodName = JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Name;
			methodSignature = JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Sig;
            accessFlags = JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Mods;
        }
			
		MethodVisitor mv = classNode.visitMethod(ACC_PUBLIC, methodName, 
				methodSignature, null, null);
		mv.visitCode();
        if (pkClassName == null) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 4);
            return;
        }
		mv.visitVarInsn(ALOAD, 1);
		Label l0 = new Label();
		mv.visitJumpInsn(IFNONNULL, l0);
		mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
		mv.visitInsn(DUP);
		mv.visitLdcInsn("arg1");
		mv.visitMethodInsn(INVOKESPECIAL,
				"java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(INSTANCEOF, pkClassName);
		Label l1 = new Label();
		mv.visitJumpInsn(IFNE, l1);
		mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
		mv.visitInsn(DUP);
		mv.visitLdcInsn("arg2");
		mv.visitMethodInsn(INVOKESPECIAL,
				"java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l1);
		
		mv.visitVarInsn(ALOAD, 2);
		mv.visitTypeInsn(CHECKCAST, pkClassName);
		mv.visitVarInsn(ASTORE, 3);
		
        // call super.jdoCopyKeyFieldsToObjectId(oid)
        final boolean isPCRoot = analyzer.isAugmentableAsRoot();
        if (!isPCRoot) {
			String superKeyOwnerClassName = analyzer.getPCSuperKeyOwnerClassName();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKESPECIAL,
					superKeyOwnerClassName, methodName, methodSignature);
        }
			
		if (toOid) {
			appendStatementsForCopyKeyFieldsToOidOFS(mv);
		} else {
			appendStatementsForCopyKeyFieldsFromOidOFC(mv);
		}
	}
	
    private void appendStatementsForCopyKeyFieldsToOidOFS(MethodVisitor mv) {

		// get field references of the key fields
		final int keyFieldCount = analyzer.getKeyFieldCount();
		final FieldInfo[] keyFieldRefs = builderHelper.getKeyClassKeyFieldRefs();
		final int[] keyFieldIndexes = analyzer.getKeyFieldIndexes();
		//affirm(keyFieldRefs.length == keyFieldCount);
		//affirm(keyFieldIndexes.length == keyFieldCount);

        PersistentStateAccessGenerator keyStateAccessGenerator =
            _keyStateAccessGenerator;
		// generate the field access statements
		for (int i = 0; i < keyFieldCount; i++) {
			// get field no, constant field ref, and signature for field
			final int keyFieldIndex = keyFieldIndexes[i];
			final FieldInfo ref = keyFieldRefs[i];
			affirm(ref != null);
			final String sig = ref.getDescriptor();
			affirm(sig != null && sig.length() > 0);

			String javaType = BuilderHelper.getJavaTypeForSignature(sig, true);
			// generate the field copying depending on its type
			
            oidMethodsHandler.generateCopyToOIDOFS(mv, ownerClassName, keyFieldIndex,
                    JDO_ObjectIdFieldSupplier_Path, javaType, sig, ref);
		}
		
		mv.visitInsn(RETURN);
		mv.visitMaxs(5, 4);
		mv.visitEnd();
	}

	
    private void appendStatementsForCopyKeyFieldsFromOidOFC(MethodVisitor mv) {

		// get field references of the key fields
		final int keyFieldCount = analyzer.getKeyFieldCount();
		final FieldInfo[] keyFieldRefs = builderHelper.getKeyClassKeyFieldRefs();
		final int[] keyFieldIndexes = analyzer.getKeyFieldIndexes();
		//affirm(keyFieldRefs.length == keyFieldCount);
		//affirm(keyFieldIndexes.length == keyFieldCount);

        PersistentStateAccessGenerator keyStateAccessGenerator =
            _keyStateAccessGenerator;
		// generate the field access statements
		for (int i = 0; i < keyFieldCount; i++) {
			// get field no, constant field ref, and signature for field
			final int keyFieldIndex = keyFieldIndexes[i];
			final FieldInfo ref = keyFieldRefs[i];
			//affirm(ref != null);
			final String sig = ref.getDescriptor();
			//affirm(sig != null && sig.length() > 0);
			String javaType = BuilderHelper.getJavaTypeForSignature(sig, true);
			// generate the field copying depending on its type
			
            oidMethodsHandler.generateCopyFromOIDOFC(mv, ownerClassName, keyFieldIndex,
                    JDO_ObjectIdFieldConsumer_Path, javaType, sig, ref);
		}
		
		mv.visitInsn(RETURN);
		mv.visitMaxs(5, 4);
		mv.visitEnd();
	}
	
}
