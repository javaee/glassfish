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

import java.util.*;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

class MethodMergerClassAdapter
	extends ClassAdapter
{
	private ClassVisitor delegateCV;

	private MethodMergerAdapter cachedMV;
	
	MethodMergerClassAdapter(ClassVisitor delegateCV) {
		super(delegateCV);
		
		this.delegateCV = delegateCV;
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions)
	{
		if (cachedMV == null) {
			cachedMV = new MethodMergerAdapter(new PrintMethodAdapter(
					super.visitMethod(access, name, desc, signature, exceptions)));		
		}
		
		return cachedMV;
	}
	
	void mergeComplete() {
		cachedMV.mergeComplete();
	}
	
	
	private static class MethodMergerAdapter
		extends MethodAdapter
	{
		
		MethodMergerAdapter(MethodVisitor delegate) {
			super(delegate);
			super.visitCode();
		}
		
		public void visitCode() {
			//No op
		}
		
		public void visitMax(int maxStack, int maxLocal) {
			//No op
		}
		
		public void visitEnd() {
			//No op
		}
		
		void mergeComplete() {
			super.visitMaxs(0, 0);
			super.visitEnd();
		}

	}	
	
	private static class PrintMethodAdapter
		extends MethodAdapter
	{
	
		PrintMethodAdapter(MethodVisitor delegate) {
			super(delegate);
		}
		
		public void visitCode() {
			super.visitCode();
		}
		
		public void visitMaxs(int maxStack, int maxLocal) {
			super.visitMaxs(maxStack, maxLocal);
		}
		
		public void visitEnd() {
			super.visitEnd();
		}
		
		public void visitFieldInsn(int opcode,
                String owner,
                String name,
                String desc) {
			super.visitFieldInsn(opcode, owner, name, desc);
		}
	}
}
