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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sun.org.apache.jdo.enhancer.classfile.AnnotaterCallback;
import com.sun.org.apache.jdo.enhancer.classfile.MethodDescriptor;

class FieldAccessMediator
	extends ClassAdapter
	implements Opcodes {

	private AnnotaterCallback listener;
	
	FieldAccessMediator(ClassVisitor cv, AnnotaterCallback listener) {
		super(cv);
		this.listener = listener;
	}
	
	public MethodVisitor visitMethod (
		final int access,
		final String name,
		final String desc,
		final String signature,
		final String[] exceptions)
	{
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		return mv == null ? null : new FieldAccessConverter(name, mv, listener);
	}
}

class FieldAccessConverter
	extends MethodAdapter
	implements Opcodes {

	private String currentMethodName;
	
	private AnnotaterCallback listener;
	
	FieldAccessConverter(String methodName, MethodVisitor mv, AnnotaterCallback listener)
	{
		super(mv);
		this.listener = listener;
		this.currentMethodName = methodName;
	}
	
	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		

		MethodDescriptor methodDesc = null;

		boolean isGetField = false;
		switch (opcode) {
		case GETFIELD:
			isGetField = true;
			//no break here
		case PUTFIELD:
			methodDesc = listener.onFieldAccess(currentMethodName,
					isGetField, owner, name, desc);
		}

			
		if (methodDesc == null) {
			super.visitFieldInsn(opcode, owner, name, desc);
		} else {
			/*
			 System.out.println("**Replacing FIELDACCESS WITH: "
					+ methodDesc.declaringClass 
					+ "; methodName " + methodDesc.methodName
					+ "; paramSignature: " + methodDesc.paramSignature);
			*/
			super.visitMethodInsn(INVOKESTATIC, methodDesc.declaringClass,
					methodDesc.methodName, methodDesc.paramSignature);
		}
	}
}
