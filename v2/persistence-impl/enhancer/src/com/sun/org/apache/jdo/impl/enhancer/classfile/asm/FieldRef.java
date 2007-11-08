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

import java.io.PrintWriter;

import org.objectweb.asm.Opcodes;

import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;

public class FieldRef
	implements FieldInfo, Opcodes
{
	private int access;
	
	private String className;
	
	private String fieldName;
	
	private String fieldType;

	public FieldRef(int access, String className, String fieldName, String fieldType) {
		this.access = access;
		this.className = className;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
	}
	
	public String getName() {
		return fieldName;
	}
	
	public String toJavaName() {
		return getName().replaceAll("/", ".");
	}
	
	public int getAccessCode() {
		return access;
	}
	
	public String getDescriptor() {
		return fieldType;
	}
	
	public String getSignature() {
		return null;
	}

	public boolean isStatic() {
		return ((Opcodes.ACC_STATIC & access) != 0);
	}
	
	public boolean isAbstract() {
		return ((Opcodes.ACC_ABSTRACT & access) != 0);
	}

	public boolean isFinal() {
		return ((Opcodes.ACC_FINAL & access) != 0);
	}
	
	public boolean isTransient() {
		return ((Opcodes.ACC_TRANSIENT & access) != 0);
	}
	
	public void print(PrintWriter pw, int indent) {
		for (int i=0; i<indent; i++) {
			pw.print(' ');
		}
		pw.println("" + getAccessCode() + " " + getDescriptor() + " " + getName());
	}
	
}
