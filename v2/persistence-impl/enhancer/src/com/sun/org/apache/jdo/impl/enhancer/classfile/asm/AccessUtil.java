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

package com.sun.org.apache.jdo.impl.enhancer.classfile.asm;

import org.objectweb.asm.Opcodes;


public class AccessUtil
	implements Opcodes
{
	
	public static boolean isFinal(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_FINAL) != 0);
	}

	public static boolean isPublic(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_PUBLIC) != 0);
	}

	public static boolean isProtected(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_PROTECTED) != 0);
	}

	public static boolean isPrivate(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_PRIVATE) != 0);
	}

	public static boolean isInterface(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_INTERFACE) != 0);
	}
	
	public static boolean isAbstract(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_ABSTRACT) != 0);
	}
	
	public static boolean isSynthetic(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_SYNTHETIC) != 0);
	}
	
	public static boolean isTransient(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_TRANSIENT) != 0);
	}
	
	public static boolean isAnnotation(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_ANNOTATION) != 0);
	}
	
	public static boolean isEnum(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_ENUM) != 0);
	}

	public static boolean isStatic(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_STATIC) != 0);
	}

	public static boolean isNative(int accessFlag) {
		return ((accessFlag & Opcodes.ACC_NATIVE) != 0);
	}
	
	public static String typeName(int accessFlag) {
		if (!isAbstract(accessFlag)) {
			if (isAnnotation(accessFlag)) {
				return "@interface";
			} else if (isEnum(accessFlag)) {
				return "enum";
			} else {
				return "class";
			}
		} else {
			if (isInterface(accessFlag)) {
				return "interface";
			} else {
				return "abstract class";
			}
		}
	}
	
	public static String asString(int accessFlag) {
		StringBuilder sbldr = new StringBuilder();

		int localAccess = accessFlag;
		
		if (isPublic(accessFlag)) {
			sbldr.append("public ");
		}

		sbldr.append(typeName(accessFlag) + " ");
		
		return sbldr.toString();
		
	}
	
}
