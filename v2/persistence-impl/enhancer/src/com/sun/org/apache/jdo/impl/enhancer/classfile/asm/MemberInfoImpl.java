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

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.sun.org.apache.jdo.enhancer.classfile.MemberInfo;

public class MemberInfoImpl
	implements MemberInfo, Opcodes
{
	protected int accessCode;
	
	protected String name;
	
	protected String descriptor;
	
	protected String signature;
	
	protected void initializeMemberInfo(ClassNode node) {
		this.accessCode = node.access;
		this.name = node.name;
		this.descriptor = "L" + name + ";";
		this.signature = node.signature;
	}

	protected void initializeMemberInfo(FieldNode node) {
		this.accessCode = node.access;
		this.name = node.name;
		this.descriptor = node.desc;
		this.signature = node.signature;
	}

	protected void initializeMemberInfo(MethodNode node) {
		this.accessCode = node.access;
		this.name = node.name;
		this.descriptor = node.desc;
		this.signature = node.signature;
	}

	public int getAccessCode() {
		return this.accessCode;
	}

	public String getName() {
		return this.name;
	}
	
	public String getDescriptor() {
		return this.descriptor;
	}
	
	public String getSignature() {
		return this.signature;
	}
	
	public boolean isFinal() {
		return AccessUtil.isFinal(getAccessCode());
	}

	public boolean isPublic() {
		return AccessUtil.isPublic(accessCode);
	}

	public boolean isProtected() {
		return AccessUtil.isProtected(accessCode);
	}

	public boolean isPrivate() {
		return AccessUtil.isPrivate(accessCode);
	}

	public boolean isInterface() {
		return AccessUtil.isInterface(accessCode);
	}
	
	public boolean isAbstract() {
		return AccessUtil.isAbstract(accessCode);
	}
	
	public boolean isSynthetic() {
		return AccessUtil.isSynthetic(accessCode);
	}
	
	public boolean isTransient() {
		return AccessUtil.isTransient(accessCode);
	}
	
	public boolean isStatic() {
		return AccessUtil.isStatic(accessCode);
	}
	
	public boolean isAnnotation() {
		return AccessUtil.isAnnotation(accessCode);
	}
	
	public boolean isEnum() {
		return AccessUtil.isEnum(accessCode);
	}
	
	public boolean isClass() {
		return (!isInterface() && !isEnum() && !isAnnotation());
	}
	
	public String toJavaName() {
		return getName().replaceAll("/", ".");
	}

	public void print(PrintWriter pw, int indent) {
		printSpaces(pw, indent);
		pw.println("Class: " + toString());
	}
	
	public String toString() {
		StringBuilder sbldr = new StringBuilder();
		if (isPublic()) {
			sbldr.append("public ");
		} else if (isProtected()) {
			sbldr.append("protected ");
		} else if (isPrivate()) {
			sbldr.append("private ");
		}
		
		if (isStatic()) {
			sbldr.append("static ");
		}
		if (isFinal()) {
			sbldr.append("final ");
		}
		
		if (isClass()) {
			sbldr.append("class ");
		} else {
			sbldr.append(descriptor);
			if (signature != null) {
				sbldr.append(signature);
			}
			sbldr.append(" ");
		}
		sbldr.append(getUnqualifiedName())
			.append(" ");
	
		return sbldr.toString();
	}
	

	protected void printSpaces(PrintWriter pw, int indent) {
		for (int i=0; i<indent; i++) {
			pw.print(' ');
		}
	}
	
	public String getUnqualifiedName() {
		int idx = name.lastIndexOf('/');
		return (idx >= 0) 
			? name.substring(idx+1)
			: name; 
	}
	
	
}
