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
import org.objectweb.asm.util.TraceSignatureVisitor;

import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.impl.enhancer.jdo.ClassFileAnalyzer;

import com.sun.org.apache.jdo.impl.enhancer.classfile.asm.FieldRef;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.EnhancerConstants;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.JDOConstants;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.NameHelper;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;

/**
 * A Class to contains (internal) methods called only the Builder
 * 
 * @author kmahesh
 *
 */
class BuilderHelper
	extends Support
	implements Opcodes, JDOConstants, EnhancerConstants
{

	private ClassInfo classInfo;
	
	private String ownerClassName;
	
	private ClassFileAnalyzer analyzer;
	
    private FieldInfo[] keyFieldRefs;
	
	private FieldInfo[] keyClassKeyFieldRefs;
	
	private FieldInfo[] keyClassKeyRefa = null;
	
	private FieldInfo[] annotatedFieldRefs = null;
    
    private PersistentStateAccessGenerator stateAccessGenerator;
	
	BuilderHelper(ClassInfo classInfo, ClassFileAnalyzer analyzer,
            PersistentStateAccessGenerator stateAccessGenerator) {
		this.classInfo = classInfo;
		this.analyzer = analyzer;
		this.ownerClassName = classInfo.getName();
        this.stateAccessGenerator = stateAccessGenerator;
	}
	
	static void generateIntegerConstantInstruction(MethodVisitor mv, int val) {
		switch (val) {
		case -1:
			mv.visitInsn(ICONST_M1);
			break;
		case 0:
			mv.visitInsn(ICONST_0);
			break;
		case 1:
			mv.visitInsn(ICONST_1);
			break;
		case 2:
			mv.visitInsn(ICONST_2);
			break;
		case 3:
			mv.visitInsn(ICONST_3);
			break;
		case 4:
			mv.visitInsn(ICONST_4);
			break;
		case 5:
			mv.visitInsn(ICONST_5);
			break;
        default:
			if (val >= -128 && val < 128)
				mv.visitIntInsn(BIPUSH, val);
			else if (val >= -32768 && val < 32768)
				mv.visitIntInsn(SIPUSH, val);
			else 
				mv.visitLdcInsn(new Integer(val));
		}
	}
    
    static void initializeFieldToZero(MethodVisitor mv, String fieldDesc, int fieldIndex) {
        switch (fieldDesc.charAt(0)) {
        case 'C':
            mv.visitIntInsn(BIPUSH, 48);
            mv.visitVarInsn(ISTORE, fieldIndex);
            break;
        case 'B':
        case 'S':
        case 'I':  
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, fieldIndex);
            break;
        case 'J':
            mv.visitInsn(LCONST_0);
            mv.visitVarInsn(LSTORE, fieldIndex);
            break;
        case 'L':
            if (fieldDesc.equals("Ljava/lang/String;")) {
                mv.visitInsn(ACONST_NULL);
                mv.visitVarInsn(ASTORE, 2);
                return;
            }
        default:
            throw new IllegalArgumentException("Cannot initialize non-number / String field: " + fieldDesc);
        }
    }
	
    void initJdoInheritedFieldCount(MethodVisitor mv) { 
		if (analyzer.isAugmentableAsRoot()) {
			mv.visitInsn(ICONST_0);
		} else {
			mv.visitMethodInsn(INVOKESTATIC, classInfo.getSuperClassName(),
					JDO_PC_jdoGetManagedFieldCount_Name,
					JDO_PC_jdoGetManagedFieldCount_Sig);
		}
		mv.visitFieldInsn(PUTSTATIC, ownerClassName,
				JDO_PC_jdoInheritedFieldCount_Name,
				JDO_PC_jdoInheritedFieldCount_Sig);
	}
	
	void initJdoFieldNames(MethodVisitor mv) {

		
        final int managedFieldCount = analyzer.getManagedFieldCount();
        final String[] managedFieldNames = analyzer.getAnnotatedFieldNames();
       
        // create array
        //affirm(NameHelper.elementPathForSig(JDO_PC_jdoFieldNames_Sig)
               //.equals(JAVA_String_Path));
		generateIntegerConstantInstruction(mv, managedFieldCount);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

        // initialize elements
        for (int i = 0; i < managedFieldCount; i++) {
			mv.visitInsn(DUP);
			generateIntegerConstantInstruction(mv, i);
			mv.visitLdcInsn(managedFieldNames[i]);
			mv.visitInsn(AASTORE);
        }
		
		mv.visitFieldInsn(PUTSTATIC,
				ownerClassName, JDO_PC_jdoFieldNames_Name,
					JDO_PC_jdoFieldNames_Sig);
    }

    void initJdoFieldTypes(MethodVisitor mv) {
		
        final int managedFieldCount = analyzer.getManagedFieldCount();
        final String[] managedFieldSigs = analyzer.getAnnotatedFieldSigs();
        affirm(managedFieldSigs.length >= managedFieldCount);
        
        //affirm(NameHelper.elementPathForSig(JDO_PC_jdoFieldTypes_Sig).equals(JAVA_Class_Path));
		

		generateIntegerConstantInstruction(mv, managedFieldCount);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        for (int i = 0; i < managedFieldCount; i++) {
			mv.visitInsn(DUP);
			generateIntegerConstantInstruction(mv, i);
			final String sig = managedFieldSigs[i];
			Type fieldType = Type.getType(sig);
			
			String primitiveWrapperType = null;
			String classPathToLoad = null;
			switch (sig.charAt(0)) {
			case 'B': 
				primitiveWrapperType = "java/lang/Byte";
				break;
			case 'C': 
				primitiveWrapperType = "java/lang/Character";
				break;
			case 'D': 
				primitiveWrapperType = "java/lang/Double";
				break;
			case 'F': 
				primitiveWrapperType = "java/lang/Float";
				break;
			case 'I': 
				primitiveWrapperType = "java/lang/Integer";
				break;
			case 'J': 
				primitiveWrapperType = "java/lang/Long";
				break;
			case 'S': 
				primitiveWrapperType = "java/lang/Short";
				break;
			case 'Z': 
				primitiveWrapperType = "java/lang/Boolean";
				break;
			case 'L':
				classPathToLoad = NameHelper.typeForSig(sig);
				break;
			case '[':
				classPathToLoad = NameHelper.typeForPath(sig);
				break;
			}
			
			if (primitiveWrapperType != null) {
				mv.visitFieldInsn(GETSTATIC,
						primitiveWrapperType, "TYPE", "Ljava/lang/Class;");
			} else {
				mv.visitLdcInsn(classPathToLoad);
				mv.visitMethodInsn(INVOKESTATIC, ownerClassName,
						SUNJDO_PC_sunjdoClassForName_Name,
                        SUNJDO_PC_sunjdoClassForName_Sig);
			}
			mv.visitInsn(AASTORE);
        }
		mv.visitFieldInsn(PUTSTATIC,
				ownerClassName, JDO_PC_jdoFieldTypes_Name,
				JDO_PC_jdoFieldTypes_Sig);
    }
   
    void initJdoFieldFlags(MethodVisitor mv) {

        final int managedFieldCount = analyzer.getManagedFieldCount();
        final int[] managedFieldFlags = analyzer.getAnnotatedFieldFlags();
        
		generateIntegerConstantInstruction(mv, managedFieldCount);
		mv.visitIntInsn(NEWARRAY, Opcodes.T_BYTE);

        // initialize elements
        for (int i = 0; i < managedFieldCount; i++) {
			mv.visitInsn(DUP);
			generateIntegerConstantInstruction(mv, i);
            final int flags = managedFieldFlags[i];

            // ensure we're using [opc_iconst_x .. opc_bipush]
            affirm(-128 <= flags && flags < 128);
			generateIntegerConstantInstruction(mv, flags);
			mv.visitInsn(BASTORE);
        }

		mv.visitFieldInsn(PUTSTATIC,
				ownerClassName,
					JDO_PC_jdoFieldFlags_Name, JDO_PC_jdoFieldFlags_Sig);

    }
    
    void initJdoPersistenceCapableSuperclass(MethodVisitor mv) 
    {
        final String pcSuperName = analyzer.getPCSuperClassName();
        final String pcRootName = analyzer.getPCRootClassName();
        affirm(pcSuperName == null || pcRootName != null);

        if (pcSuperName == null) {
			mv.visitInsn(ACONST_NULL);
        } else {
			mv.visitLdcInsn(pcSuperName.replace('/', '.'));
			mv.visitMethodInsn(INVOKESTATIC, pcSuperName,
					SUNJDO_PC_sunjdoClassForName_Name,
                    SUNJDO_PC_sunjdoClassForName_Sig);
        }
		mv.visitFieldInsn(PUTSTATIC,
				ownerClassName,
				JDO_PC_jdoPersistenceCapableSuperclass_Name,
				JDO_PC_jdoPersistenceCapableSuperclass_Sig);
    }
	
	void initRegisterClass(MethodVisitor mv) {
		mv.visitLdcInsn(classInfo.toJavaName());
		mv.visitMethodInsn(INVOKESTATIC, ownerClassName,
				SUNJDO_PC_sunjdoClassForName_Name,
				SUNJDO_PC_sunjdoClassForName_Sig);
		mv.visitFieldInsn(GETSTATIC, ownerClassName,
				JDO_PC_jdoFieldNames_Name, JDO_PC_jdoFieldNames_Sig);
		mv.visitFieldInsn(GETSTATIC, ownerClassName,
				JDO_PC_jdoFieldTypes_Name, JDO_PC_jdoFieldTypes_Sig);
		mv.visitFieldInsn(GETSTATIC, ownerClassName,
				JDO_PC_jdoFieldFlags_Name, JDO_PC_jdoFieldFlags_Sig);
		mv.visitFieldInsn(GETSTATIC, ownerClassName,
				JDO_PC_jdoPersistenceCapableSuperclass_Name, JDO_PC_jdoPersistenceCapableSuperclass_Sig);
		
		if (classInfo.isAbstract()) {
			mv.visitInsn(ACONST_NULL);
		} else {
			mv.visitTypeInsn(NEW, ownerClassName);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, ownerClassName, "<init>", "()V");
		}

		mv.visitMethodInsn(INVOKESTATIC, JDO_JDOImplHelper_Path,
				JDO_JDOImplHelper_registerClass_Name, JDO_JDOImplHelper_registerClass_Sig);
	}
	
	void generateNullCheckForSM(MethodVisitor mv) {
		mv.visitVarInsn(ALOAD, 2);
		Label nonNullLabel = new Label();
		mv.visitJumpInsn(IFNONNULL, nonNullLabel);
		mv.visitTypeInsn(NEW, JAVA_IllegalStateException_Path);
		mv.visitInsn(DUP);
		mv.visitLdcInsn("arg0.jdoStateManager");
		mv.visitMethodInsn(INVOKESPECIAL, JAVA_IllegalStateException_Path,
				"<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(ATHROW);
		mv.visitLabel(nonNullLabel);
	}
	
	void generateNullCheckForPCArgument(MethodVisitor mv) {
		mv.visitVarInsn(ALOAD, 1);
		Label nonNullLabel = new Label();
		mv.visitJumpInsn(IFNONNULL, nonNullLabel);
		mv.visitTypeInsn(NEW, JAVA_IllegalArgumentException_Path);
		mv.visitInsn(DUP);
		mv.visitLdcInsn("arg1");
		mv.visitMethodInsn(INVOKESPECIAL, JAVA_IllegalArgumentException_Path,
				"<init>", "(Ljava/lang/String;)V");
		mv.visitInsn(ATHROW);
		mv.visitLabel(nonNullLabel);
	}
	
	static String getJavaTypeForSignature(String sig) {
		return BuilderHelper.getJavaTypeForSignature(sig, false);
	}

	static String getJavaTypeForSignature(String sig, boolean resolveStringType) {
		switch (sig.charAt(0)) {
		case 'B': 
			return "Byte";
		case 'C': 
			return "Char";
		case 'D': 
			return "Double";
		case 'F': 
			return "Float";
		case 'I': 
			return "Int";
		case 'J': 
			return "Long";
		case 'S': 
			return "Short";
		case 'Z': 
			return "Boolean";
		case 'L':
			if (resolveStringType) {
				if (sig.equals("Ljava/lang/String;")) {
					return "String";
				}
			}
			return "Object";
		case '[':
			return "Object";
		default :
			throw new IllegalArgumentException("Illegal signature: " + sig);
		} 
	}
	
    static boolean isPrimitiveType(String sig) {
        switch (sig.charAt(0)) {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
            return true;
        case 'L':
        case '[':
            return false;
        default:
            throw new IllegalArgumentException("Illegal signature: " + sig);
        }
    }

    static boolean isStringOrPrimitiveType(String sig) {
        return isStringType(sig) || isPrimitiveType(sig);
    }

	static boolean isStringType(String sig) {
		switch (sig.charAt(0)) {
		case 'B': 
		case 'C': 
		case 'D': 
		case 'F': 
		case 'I': 
		case 'J': 
		case 'S':
		case 'Z':
		case '[':
			return false;
		case 'L':
			return (sig.equals("Ljava/lang/String;"));
		default :
			throw new IllegalArgumentException("Illegal signature: "  +sig);
		}
	}
	
    static int getReturnOpcode(String sig) {
		switch (sig.charAt(0)) {
		case 'Z':
		case 'C':
		case 'B':
		case 'S':
		case 'I':
			return IRETURN;
		case 'J':
			return LRETURN;
		case 'F':
			return FRETURN;
		case 'D':
			return DRETURN;
		case 'L':
		case '[':
			return ARETURN;
		default:
			throw new IllegalArgumentException("Illegal signature: " + sig);
		}
	}
    
    static int getALoadOpcode(String sig) {
        switch (sig.charAt(0)) {
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
            return ILOAD;
        case 'J':
            return LLOAD;
        case 'F':
            return FLOAD;
        case 'D':
            return DLOAD;
        case 'L':
        case '[':
            return ALOAD;
        default:
            throw new IllegalArgumentException("Illegal signature: " + sig);
        }
    }
    
    static int getAStoreOpcode(String sig) {
        switch (sig.charAt(0)) {
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
            return ISTORE;
        case 'J':
            return LSTORE;
        case 'F':
            return FSTORE;
        case 'D':
            return DSTORE;
        case 'L':
        case '[':
            return ASTORE;
        default:
            throw new IllegalArgumentException("Illegal signature: " + sig);
        }
    }

	
	static String getJavaTypeCastSignature(String signature) {
		SignatureReader sigReader = new SignatureReader(signature);
		TraceSignatureVisitor sigWriter = new TraceSignatureVisitor(512);
		sigReader.accept(sigWriter);
		String declStr = sigWriter.getDeclaration();
		
		boolean isObjectType = declStr.startsWith(" extends"); 
		String castStr = (isObjectType ? declStr.substring(9) : signature);
		String typeCastString = castStr.replace('.', '/');	
		return typeCastString;
	}
	
    FieldInfo[] getAnnotatedFieldRefs()
    {
        // create field references in constant pool if not done yet
        if (annotatedFieldRefs == null) {
            final int annotatedFieldCount = analyzer.getAnnotatedFieldCount();
            final String[] annotatedFieldNames
                = analyzer.getAnnotatedFieldNames();
            final String[] annotatedFieldSigs
                = analyzer.getAnnotatedFieldSigs();
			final int[] annotatedFieldMods
            = analyzer.getAnnotatedFieldMods();
            //affirm(annotatedFieldNames.length == annotatedFieldCount);
            //affirm(annotatedFieldSigs.length == annotatedFieldCount);
            
            // add field references to constant pool
            annotatedFieldRefs = new FieldInfo[annotatedFieldCount];
            for (int i = 0; i < annotatedFieldCount; i++) {
				int access = annotatedFieldMods[i];
                final String name = annotatedFieldNames[i];
                final String sig = annotatedFieldSigs[i];
                annotatedFieldRefs[i] = new FieldRef(access, ownerClassName, name, sig);
                //affirm(annotatedFieldRefs[i] != null);
            }
        }
        //affirm(annotatedFieldRefs != null);
        return annotatedFieldRefs;
    }
	
	int[] getKeyFieldIndexes() {
        return analyzer.getKeyFieldIndexes();
    }
    
	FieldInfo[] getKeyFieldRefs() {
        // get field references if not done yet
        if (keyFieldRefs == null) {
            final FieldInfo[] annotatedFieldRefs = getAnnotatedFieldRefs();
            final int keyFieldCount = analyzer.getKeyFieldCount();
            final int[] keyFieldIndexes = analyzer.getKeyFieldIndexes();
            //affirm(keyFieldIndexes.length == keyFieldCount);
            
            // add field references
            keyFieldRefs = new FieldInfo[keyFieldCount];
            for (int i = 0; i < keyFieldCount; i++) {
                keyFieldRefs[i] = annotatedFieldRefs[keyFieldIndexes[i]];
                //affirm(keyFieldRefs[i] != null);
            }
        }

        //affirm(keyFieldRefs != null);
        return keyFieldRefs;
    }
	
    FieldInfo[] getKeyClassKeyFieldRefs() {
        // get field references if not done yet
        if (keyClassKeyFieldRefs == null) {
            final String keyClassName = analyzer.getKeyClassName();
            //affirm(keyClassName != null);
            final int keyFieldCount = analyzer.getKeyFieldCount();
            final FieldInfo[] keyFieldRefs = getKeyFieldRefs();
            //affirm(keyFieldRefs.length == keyFieldCount);
            
            // add field references
            keyClassKeyFieldRefs = new FieldInfo[keyFieldCount];
            for (int i = 0; i < keyFieldCount; i++) {
                final FieldInfo fieldRef = keyFieldRefs[i];
				final int access = fieldRef.getAccessCode();
                final String name = fieldRef.getName();
                final String type = fieldRef.getDescriptor();
                keyClassKeyFieldRefs[i]
                    = new FieldRef(access, keyClassName, name, type);
                //affirm(keyClassKeyFieldRefs[i] != null);
            }
        }
        //affirm(keyClassKeyFieldRefs != null);
        return keyClassKeyFieldRefs;
    }

}
