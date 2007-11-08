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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;

public class ClassInfoImpl
	extends MemberInfoImpl
	implements ClassInfo
{
	private ClassNode classNode;
	
	private Map<String, FieldInfo> fieldInfoMap;
	
	private List<MethodInfo> methodInfoList;

    public ClassInfoImpl(String className)
        throws IOException
    {      
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(className);
			init(fis);
		} finally {
			try { fis.close(); } catch (Exception ex) {}
		}
    }
	
    public ClassInfoImpl(InputStream is)
        throws IOException
    {   
		init(is);
    }
	
	private void init(InputStream is)
		throws IOException
	{
		int sz = is.available();
		byte[] classData = new byte[sz];
		
		is.read(classData);
        
		ClassReader classReader = new ClassReader(classData);
		classNode = new ClassNode();
        classReader.accept(classNode, false);
		
		super.initializeMemberInfo(classNode);
    }

	public String getSuperClassName() {
		return classNode.superName;
	}

	public String getJavaSuperClassName() {
		return getSuperClassName().replaceAll("/", ".");
	}
	
	public Collection<FieldInfo> fields() {
		if (fieldInfoMap == null) {
			fieldInfoMap = new HashMap<String, FieldInfo>();
			List<FieldNode> fieldList = classNode.fields;
			for (FieldNode field : fieldList) {
				fieldInfoMap.put(field.name, new FieldInfoImpl(field));
			}
		}
		return fieldInfoMap.values();
	}
	
	public Collection<MethodInfo> methods() {
		if (methodInfoList == null) {
			methodInfoList = new ArrayList<MethodInfo>();
			int sz = classNode.methods.size();
			for (int i=0; i<sz; i++) {
				MethodNode method = (MethodNode) classNode.methods.get(i);
				methodInfoList.add(new MethodInfoImpl(method));
			}
		}
		return methodInfoList;
	}
	
	
	public Collection<String> interfaces() {
		return (Collection<String>) classNode.interfaces;
	}
	
	public void write(OutputStream os)
		throws IOException
	{
		ClassWriter cw = new ClassWriter(true, false);

		classNode.accept(cw);
		byte[] classData = cw.toByteArray();
		
		os.write(classData);
		os.flush();
	}

	public void dump(PrintWriter pw) {
		
	}
		
	public void addInterface(String name) {
		classNode.interfaces.add(name);
	}
	
	public void addField(int access, String name, String type, String sig) {
		classNode.visitField(access, name, type, sig, null);
		fieldInfoMap = null;
	}
	
	public FieldInfo findField(String name) {
		fields();
		return fieldInfoMap.get(name);
	}
	

	public MethodInfo findMethod(String name, String desc, String sig) {
		int index = findIndexInternal(name, desc, sig);
		if (index != -1) {
			return methodInfoList.get(index);
		}
		return null;
	}
	
	private void affirm(boolean condition) {
		if (condition == false) {
			throw new IllegalStateException("condition is not true!!");
		}
	}

	public MethodInfo removeMethod(String name, String desc, String sig) {
		if (false) {
			System.out.println("**ClassInfoImpl : " + this + " attempting to remove "
				+ name + " " + desc  + " " + sig);
		}
		int index = findIndexInternal(name, desc, sig);
		if (index != -1) {
			MethodNode mNode = (MethodNode) classNode.methods.get(index);
			affirm(mNode != null);
			affirm(mNode.name.equals(name));
			affirm(mNode.desc.equals(desc));			
			classNode.methods.remove(index);						
			affirm(methodInfoList.get(index) != null);
			MethodInfo retVal = methodInfoList.remove(index);
			methodInfoList = null;		
			return retVal;
		}
		return null;
	}
	
	public boolean removeMethod(MethodInfo methInfo) {
		int index = findIndexInternal(methInfo);
		if (index != -1) {
			classNode.methods.remove(index);
			methodInfoList = null;
			return true;
		}
		return false;
	}

    public void print(PrintWriter out, int indent) {

        out.println("className = " + classNode.name);
        printSpaces(out, indent);
        printSpaces(out, indent);
        out.println("majorVersion = " + classNode.version);
        printSpaces(out, indent);
        out.println("minorVersion = " + classNode.version);
        printSpaces(out, indent);
        out.println("accessFlags = " + classNode.access);
        printSpaces(out, indent);
        out.println("superClassName = " + classNode.superName);
        printSpaces(out, indent);
        out.print("Interfaces =");
        for (String inter : (List<String>) interfaces()) {
            out.print(" " + inter);
        }
        out.println();

        printSpaces(out, indent);
        out.println("fields =");
		for (FieldInfo field : fields()) {
            field.print(out, indent + 3);
        }

        printSpaces(out, indent);
        out.println("methods =");
		for (MethodInfo method : methods()) {
            method.print(out, indent + 3);
        }

        printSpaces(out, indent);
        out.println("attributes =");
		for (Attribute attr : (List<Attribute>) classNode.attrs) {
			printSpaces(out, indent+3);
			out.print(attr.type + " ");
		}
    }
	
    public void summarize(PrintWriter pw, int indent) {
        int instructionCount = 0;
		pw.println();
        for (MethodNode node : (List<MethodNode>) classNode.methods) {
			instructionCount += node.instructions.size();	
		} 
		printSpaces(pw, indent);
		pw.println(methods().size() + " methods with "
                    + instructionCount + " instructions");
		printSpaces(pw, indent);
		pw.println(classNode.fields.size() + " fields");
    }
	
	public ClassNode getClassNode() {
		return this.classNode;
	}
	
	public int findIndexInternal(MethodInfo methInfo) {
		MethodNode methodNode = ((MethodInfoImpl) methInfo).getMethodNode();
		List<MethodNode> list = (List<MethodNode>) classNode.methods;
		int sz = list.size();
		
		for (int index=0; index<sz; index++) {
			if (list.get(index) == methodNode) {
				return index;
			}
		}
		
		return findIndexInternal(methInfo.getName(),
				methInfo.getDescriptor(), methInfo.getSignature());
	}
	
	public int findIndexInternal(String name, String desc, String signature) {
		int sz = methods().size();
		for (int index=0; index<sz; index++) {
			MethodInfo node = methodInfoList.get(index);
			boolean matchFound = (node.getName().equals(name))
				&& (node.getDescriptor().equals(desc));	
			if (matchFound) {
				if (node.getSignature() == null) {
					matchFound = signature == null;
				} else {
					matchFound = node.getSignature().equals(signature);
				}
			
				if (matchFound) {
					return index;
				}
			}
		}
		return -1;
	}

	public static void main(String[] args)
		throws Exception
	{
		ClassInfoImpl classInfo = new ClassInfoImpl(args[0]);
		System.out.println("Class Info: " + classInfo);
		
		classInfo.addField(Opcodes.ACC_PUBLIC, "jdoStateManager",
				"javax/jdo/spi.StateManager", null);
	}
}

