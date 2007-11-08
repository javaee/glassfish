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
import java.util.Stack;

/**
 * Class representing a name and an associated type in the constant pool
 * of a class file
 */
public class ConstNameAndType extends ConstBasic {
    /* The tag value associated with ConstDouble */
    public static final int MyTag = CONSTANTNameAndType;

    /* The name of interest */
    private ConstUtf8 theName;

    /* The index of the name to be resolved
     *   - used during class file reading */
    private int theNameIndex;

    /* The type signature associated with the name */
    private ConstUtf8 typeSignature;

    /* The index of the signature to be resolved
     *   - used during class file reading */
    private int typeSignatureIndex;

    /* public accessors */

    /**
     * The tag of this constant entry
     */
    public int tag () { return MyTag; }

    /**
     * Return the name
     */
    public ConstUtf8 name() {
        return theName;
    }

    /**
     * Return the type signature associated with the name
     */
    public ConstUtf8 signature() {
        return typeSignature;
    }

    /**
     * Modify the signature
     */
    public void changeSignature(ConstUtf8 newSig) {
        typeSignature = newSig;
    }

    /**
     * A printable representation
     */
    public String toString () {
        return "CONSTANTNameAndType(" + indexAsString() + "): " + 
            "name(" + theName.toString() + ") " +
            " type(" + typeSignature.toString() + ")";
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof ConstNameAndType)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        ConstNameAndType other = (ConstNameAndType)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (!this.theName.isEqual(msg, other.theName)) {
            msg.push(String.valueOf("theName = "
                                    + other.theName));
            msg.push(String.valueOf("theName = "
                                    + this.theName));
            return false;
        }
        if (!this.typeSignature.isEqual(msg, other.typeSignature)) {
            msg.push(String.valueOf("typeSignature = "
                                    + other.typeSignature));
            msg.push(String.valueOf("typeSignature = "
                                    + this.typeSignature));
            return false;
        }
        return true;
    }

    /* package local methods */

    ConstNameAndType (ConstUtf8 n, ConstUtf8 sig) {
        theName = n; typeSignature = sig;
    }

    ConstNameAndType (int n, int sig) {
        theNameIndex = n; typeSignatureIndex = sig;
    }

    void formatData (DataOutputStream b) throws IOException {
        b.writeShort(theName.getIndex());
        b.writeShort(typeSignature.getIndex());
    }

    static ConstNameAndType read (DataInputStream input) throws IOException {
        int cname = input.readUnsignedShort();
        int sig = input.readUnsignedShort();

        return new ConstNameAndType (cname, sig);
    }

    void resolve (ConstantPool p) {
        theName = (ConstUtf8) p.constantAt(theNameIndex);
        typeSignature = (ConstUtf8) p.constantAt(typeSignatureIndex);
    }
}
