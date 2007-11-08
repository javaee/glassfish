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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;


public class MethodInfoImpl
	implements MethodInfo, Opcodes
{
	private MethodNode methodNode;
	
	public MethodInfoImpl(MethodNode methodNode) {
		this.methodNode = methodNode;
	}
	
	boolean isSame(MethodNode methodNode) {
		boolean result = false;
		
		result = (methodNode.access == getAccessCode())
			&&	 (methodNode.name.equals(getName()))
			&&	 (methodNode.desc.equals(getDescriptor()));
		if (result) {
			if (methodNode.signature == null) {
				result = getSignature() == null;
			} else {
				result = methodNode.signature.equals(getSignature());
			}
		}
		return result;
	}
	
	public boolean isSame(MethodInfo otherInfo) {
		boolean result = false;
		
		result = (otherInfo.getAccessCode() == getAccessCode())
			&&	 (otherInfo.getName().equals(getName()))
			&&	 (otherInfo.getDescriptor().equals(getDescriptor()));
		if (result) {
			if (methodNode.signature == null) {
				result = getSignature() == null;
			} else {
				result = methodNode.signature.equals(getSignature());
			}
		}
		return result;
	}

	public String getName() {
		return methodNode.name;
	}
	
	public String toJavaName() {
		return getName().replaceAll("/", ".");
	}
	
	public String getDescriptor() {
		return methodNode.desc;
	}
	
	public int getAccessCode() {
		return methodNode.access;
	}
	
	public String getSignature() {
		return methodNode.signature;
	}
	
	public String[] getExceptions() {
		return (String[]) methodNode.exceptions.toArray(new String[0]);
	}
	
	public boolean isAbstract() {
		return ((Opcodes.ACC_ABSTRACT & methodNode.access) != 0);
	}

	public boolean isStatic() {
		return ((Opcodes.ACC_STATIC & methodNode.access) != 0);
	}
	
	public boolean isFinal() {
		return ((Opcodes.ACC_FINAL & methodNode.access) != 0);
	}
	
	public boolean isNative() {
		return ((Opcodes.ACC_NATIVE & methodNode.access) != 0);
	}
	
    public String getReturnTypeDescriptor() {
        return Type.getReturnType(getDescriptor()).toString();
    }
  
	public String getArgumentsAsJavaString() {
		Type[] types = Type.getArgumentTypes(getDescriptor());
		StringBuilder sbldr = new StringBuilder(")");
		String coma = "";
		for (Type type : types) {
			sbldr.append(coma).append(type.toString());
			coma = ", ";
		}
		return sbldr.toString();
	}
	
	public MethodNode getMethodNode() {
		return this.methodNode;
	}
	
	public String toString() {
		StringBuilder bldr = new StringBuilder();
		bldr.append("Method ==>").append(getName())
			.append("; ").append("type: ").append(getDescriptor())
			.append("; ").append("sig: ").append(getSignature());
		
		return bldr.toString();
	}
	
	
    public boolean isEqual(Stack<String> list, MethodInfo meth2) {
		if (! (meth2 instanceof MethodInfoImpl)) {
			list.add("method-2 not an instanceof ASMMethodInfo");
		}
		
		MethodInfoImpl method1 = (MethodInfoImpl) this;
		MethodInfoImpl method2 = (MethodInfoImpl) meth2;
        
		if (!method1.getName().equals(method2.getName())) {
            list.push(String.valueOf("methodName ==> "
                                    + method2.getName()));
            list.push(String.valueOf("methodName ==> "
                                    + method1.getName()));
            return false;
        }		
        if (method1.getAccessCode() != method1.getAccessCode()) {
			list.push(String.valueOf("accessFlags ==> 0x"
                                    + Integer.toHexString(method2.getAccessCode())));
            list.push(String.valueOf("accessFlags ==> 0x"
                                    + Integer.toHexString(method1.getAccessCode())));
            return false;
        }
        if (!method1.getDescriptor().equals(method2.getDescriptor())) {
            list.push(String.valueOf("methodSignature ==> "
                                    + method2.getSignature()));
            list.push(String.valueOf("methodSignature ==> "
                                    + method1.getSignature()));
            return false;
        }
		
		Map<String, Attribute> attrs1 = new HashMap<String, Attribute>();
		Set<String> notFoundAttrs = new HashSet<String>();

		List<Attribute> attrList1 = (List<Attribute>) method1.getMethodNode().attrs;
		if (attrList1 != null) {
			for (Attribute  attr : attrList1) {
				attrs1.put(attr.type, attr);
				notFoundAttrs.add(attr.type);
			}
		}

		boolean result = true;
		List<Attribute> attrList2 = (List<Attribute>) method2.getMethodNode().attrs;
		if (attrList2!= null) {
			for (Attribute  attr : attrList2) {	
				String attrName =attr.type;
				if (attrs1.get(attrName) == null) {
					list.push("methodAttribute ==> " + attrName);
					list.push("");
					result = false;
				} else {
					notFoundAttrs.remove(attrName);
				}
			}
		}
		
		for (String attrName : notFoundAttrs) {
			list.push("");
			list.push("methodAttribute ==> " + attrName);
			result = false;
		}

        return result;
    }

    //@olsen: made public
    public void print(PrintStream out, int indent) {
        for (int i=0; i<indent; i++) {
			out.print(' ');
        }
        out.print("'" + getName() + "'");
        out.print(" sig = " + getDescriptor());
        out.print(" accessFlags = " + Integer.toString(getAccessCode()));
        out.println(" attributes:");
		for (int i=0; i<indent+2; i++) {
			out.print(' ');
        }
		for (Attribute attr : (List<Attribute>) getMethodNode().attrs) {
			for (int i=0; i<indent+2; i++) {
				out.print(attr.type + " ");
	        }
		}
    }

	public void print(PrintWriter pw, int indent) {
		for (int i=0; i<indent; i++) {
			pw.print(' ');
		}
		pw.println("" + getAccessCode() + " " + getDescriptor() + " " + getName());
		String[] exceptions = getExceptions();
		if (exceptions.length > 0) {
			for (int i=0; i<indent; i++) {
				pw.print(' ');
			}
			pw.print("throws ");
			String coma = "";
			for (String ex : exceptions) {
				pw.print(coma + ex);
				coma = ", ";
			}
		}
	}
	
}
