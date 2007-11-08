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

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */


package com.sun.org.apache.jdo.impl.enhancer.classfile;

import java.io.*;
import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;

/**
 * ClassMethod models the static and non-static methods of a class within
 * a class file.  This includes constructors and initializer code.
 */
public class ClassMethod extends ClassMember {
    /* The name of the constructor code */
    public final static String intializerName = "<init>";

    /* The name of the static initializer code */
    public final static String staticIntializerName = "<clinit>";

    /* access flag bit mask - see VMConstants */
    private int accessFlags;

    /* The name of the method */
    private ConstUtf8 methodName;

    /* The type signature of the method */
    private ConstUtf8 methodSignature;

    /* The attributes associated with the field */
    private AttributeVector methodAttributes;
  
  
    /* public accessors */

    /**
     * Return the access flags for the method - see VMConstants
     */
    public int access() {
        return accessFlags;
    }

    /**
     * Update the access flags for the field - see VMConstants
     */
    public void setAccess(int newFlags) {
        accessFlags = newFlags;
    }

    /**
     * Is the method abstract?
     */
    public boolean isAbstract() {
        return (accessFlags & ACCAbstract) != 0;
    }

    /**
     * Is the method native?
     */
    public boolean isNative() {
        return (accessFlags & ACCNative) != 0;
    }

    /**
     * Return the name of the method
     */
    public ConstUtf8 name() {
        return methodName;
    }

    /**
     * Change the name of the method
     */
    public void changeName(ConstUtf8 name) {
        methodName = name;
    }

    /**
     * Return the type signature of the method
     */
    public ConstUtf8 signature() {
        return methodSignature;
    }

    /**
     * Change the type signature of the method
     */
    public void changeSignature(ConstUtf8 newSig) {
        methodSignature = newSig;
    }

    /**
     * Return the attributes associated with the method
     */
    public AttributeVector attributes() {
        return methodAttributes;
    }

    /**
     * Construct a class method object
     */
  
    public ClassMethod(int accFlags, ConstUtf8 name, ConstUtf8 sig,
                       AttributeVector methodAttrs) {
        accessFlags = accFlags;
        methodName = name;
        methodSignature = sig;
        methodAttributes = methodAttrs;
    }

    /**
     * Returns the size of the method byteCode (if any)
     */
    int codeSize() {
        CodeAttribute codeAttr = codeAttribute();
        return (codeAttr == null) ? 0  : codeAttr.codeSize();
    }

    /**
     * Returns the CodeAttribute associated with this method (if any)
     */
    public CodeAttribute codeAttribute() {
        Enumeration e = methodAttributes.elements();
        while (e.hasMoreElements()) {
            ClassAttribute attr = (ClassAttribute) e.nextElement();
            if (attr instanceof CodeAttribute)
                return (CodeAttribute) attr;
        }
        return null;
    }

    /**
     * Returns the ExceptionsAttribute associated with this method (if any)
     */
    //@olsen: added method
    public ExceptionsAttribute exceptionsAttribute() {
        Enumeration e = methodAttributes.elements();
        while (e.hasMoreElements()) {
            ClassAttribute attr = (ClassAttribute) e.nextElement();
            if (attr instanceof ExceptionsAttribute)
                return (ExceptionsAttribute) attr;
        }
        return null;
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof ClassMethod)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        ClassMethod other = (ClassMethod)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (this.accessFlags != other.accessFlags) {
            msg.push(String.valueOf("accessFlags = 0x"
                                    + Integer.toHexString(other.accessFlags)));
            msg.push(String.valueOf("accessFlags = 0x"
                                    + Integer.toHexString(this.accessFlags)));
            return false;
        }
        if (!this.methodName.isEqual(msg, other.methodName)) {
            msg.push(String.valueOf("methodName = "
                                    + other.methodName));
            msg.push(String.valueOf("methodName = "
                                    + this.methodName));
            return false;
        }
        if (!this.methodSignature.isEqual(msg, other.methodSignature)) {
            msg.push(String.valueOf("methodSignature = "
                                    + other.methodSignature));
            msg.push(String.valueOf("methodSignature = "
                                    + this.methodSignature));
            return false;
        }
        if (!this.methodAttributes.isEqual(msg, other.methodAttributes)) {
            msg.push(String.valueOf("methodAttributes = "
                                    + other.methodAttributes));
            msg.push(String.valueOf("methodAttributes = "
                                    + this.methodAttributes));
            return false;
        }
        return true;
    }

    //@olsen: made public
    public void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("'" + methodName.asString() + "'");
        out.print(" sig = " + methodSignature.asString());
        out.print(" accessFlags = " + Integer.toString(accessFlags));
        out.println(" attributes:");
        methodAttributes.print(out, indent+2);
    }

    /* package local methods */

    static ClassMethod read(DataInputStream data, ConstantPool pool) 
        throws IOException {
        int accessFlags = data.readUnsignedShort();
        int nameIndex = data.readUnsignedShort();
        int sigIndex = data.readUnsignedShort();
        ClassMethod f = 
            new ClassMethod(accessFlags, 
                            (ConstUtf8) pool.constantAt(nameIndex),
                            (ConstUtf8) pool.constantAt(sigIndex),
                            null);

        f.methodAttributes = AttributeVector.readAttributes(data, pool);
        return f;
    }

    void write(DataOutputStream data) throws IOException {
        CodeAttribute codeAttr = codeAttribute();
        data.writeShort(accessFlags);
        data.writeShort(methodName.getIndex());
        data.writeShort(methodSignature.getIndex());
        methodAttributes.write(data);
    }
}


