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

import java.io.IOException;
import java.io.InputStream;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import com.sun.org.apache.jdo.enhancer.classfile.ClassFileParser;
import com.sun.org.apache.jdo.enhancer.classfile.ClassFileParserEventListener;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.enhancer.classfile.MethodDescriptor;
import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;

public class ClassFileParserImpl
    implements ClassFileParser
{
    List<ClassFileParserEventListener> listeners =
        new ArrayList<ClassFileParserEventListener>();
    
    public void parse(InputStream is)
        throws IOException
    {
        ClassInfoImpl classInfo = new ClassInfoImpl(is);
        parse(classInfo);
    }
    
    public void parse(ClassInfo classInfo)
        throws IOException
    {
        ClassInfoImpl asmClassInfo = null;
        if (classInfo instanceof ClassInfoImpl) {
            asmClassInfo = (ClassInfoImpl) classInfo;
        } else {
            asmClassInfo = new ClassInfoImpl(classInfo.getName());
        }
        
        scanForFields(asmClassInfo);
        scanForMethods(asmClassInfo);
    }

    public boolean registerClassFileParserEventListener(
            ClassFileParserEventListener listener)
    {
        return listeners.add(listener); 
    }
    
    public boolean removeClassFileParserEventListener(
            ClassFileParserEventListener listener)
    {
        return listeners.remove(listener);
    }

    private void scanForFields(ClassInfoImpl classInfo) {
        Collection<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {
            for (ClassFileParserEventListener listener : listeners) {
                listener.onFieldDeclaration(field);
            }
        }
    }
    
    private void scanForMethods(ClassInfoImpl classInfo) {
        Collection<MethodInfo> methods = classInfo.methods();
        for (MethodInfo method : methods) {
            if (("<init>".equals(method.getName()))
                        || ("<clinit>".equals(method.getName())))
            {
                continue;
            }
            for (ClassFileParserEventListener listener : listeners) {
                listener.onMethodBegin(method);
            }
            scanInstructions(method);
            for (ClassFileParserEventListener listener : listeners) {
                listener.onMethodEnd(method);
            }
        }
    }
    
    private void scanInstructions(MethodInfo method) {
        MethodNode methodNode = ((MethodInfoImpl) method).getMethodNode();
        
        String methodName = method.getName();
        List<AbstractInsnNode> instructions = methodNode.instructions;
        
        for (AbstractInsnNode instruction : instructions) {
            if (instruction.getType() == AbstractInsnNode.FIELD_INSN) {
                FieldInsnNode fieldInstruction = (FieldInsnNode) instruction;
                int opcode = fieldInstruction.getOpcode();
                boolean isRead = false;
                boolean isGetOrPutField = false;
                
                switch (opcode) {
                case Opcodes.GETFIELD:
                    isRead = true;
                    isGetOrPutField = true;
                    break;
                case Opcodes.PUTFIELD:
                    isRead = false;
                    isGetOrPutField = true;
                    break;
                }
                
                if (isGetOrPutField) {
                    for (ClassFileParserEventListener listener : listeners) {
                        listener.onFieldAccess(methodName, isRead,
                                fieldInstruction.owner,
                                fieldInstruction.name,  fieldInstruction.desc);
                    }
                }
            } else if (instruction.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInstruction = (MethodInsnNode) instruction;
                ClassFileParserEventListener.MethodInvocationType invType
                     = ClassFileParserEventListener.MethodInvocationType.INTERFACE;
                switch (instruction.getOpcode()) {
                case Opcodes.INVOKEINTERFACE:
                    invType = invType.INTERFACE;
                    break;
                case Opcodes.INVOKESPECIAL:
                    invType = invType.SPECIAL;
                    break;
                case Opcodes.INVOKESTATIC:
                    invType = invType.STATIC;
                    break;
                case Opcodes.INVOKEVIRTUAL:
                    invType = invType.VIRTUAL;
                    break;
                }
                //if (instruction.getOpcode() == Opcodes.INVOKESTATIC) {
                    Type retType = Type.getReturnType(methodInstruction.desc);
                    Type[] paramTypes = Type.getArgumentTypes(methodInstruction.desc);
                    int paramSz = paramTypes.length;
                    String[] paramStrs = new String[paramSz];
                    for (int i=0; i<paramSz; i++) {
                        paramStrs[i] = paramTypes[i].toString();
                    }
                    for (ClassFileParserEventListener listener : listeners) {
                        listener.onMethodInvoke(invType, methodName,
                                methodInstruction.owner,
                                methodInstruction.name,  methodInstruction.desc,
                                retType.toString(), paramStrs);
                    }
                //}
            }
        }
    }
    
}

class EchoClassFileParserEventListenerAdapter
    implements ClassFileParserEventListener {
    
    public void onFieldDeclaration(FieldInfo fieldInfo) {
        System.out.println("Field: " + fieldInfo);
    }

    public void onMethodBegin(MethodInfo methodInfo) {
        System.out.println(methodInfo);
    }

    public MethodDescriptor onFieldAccess(String currentMethodName, boolean isRead,
            String ownerClassName, String fieldName, String fieldType)
    {
        System.out.println("\t" + (isRead ? "get" : "put")
                + " " + ownerClassName + "." + fieldName + " " + fieldType);
        return null;
    }

    public void onMethodEnd(MethodInfo methodInfo) {
        ;
    }
    
    public MethodDescriptor onMethodInvoke(
            ClassFileParserEventListener.MethodInvocationType invType,
            String currentMethodName,
            String ownerClassName, String methodName, String methodType,
            String returnType, String[] paramTypes)
    {
        System.out.print("** ");
        System.out.print(returnType);
        System.out.print(" " + ownerClassName + "." + methodName + "(");
        for (String param : paramTypes) {
            System.out.print(" {{" + param + "}}");
        }
        System.out.println(")");
        return null;
    }


}


