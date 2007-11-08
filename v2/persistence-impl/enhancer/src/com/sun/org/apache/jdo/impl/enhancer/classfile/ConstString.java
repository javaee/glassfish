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
 * Class representing a class specification in the constant pool
 *
 * ConstString strictly speaking is not a ConstantValue in the
 * Java VM sense.  However, the compiler generates ConstantValue attributes
 * which refer to ConstString entries.  This occurs for initialized static
 * final String fields.  I've changed ConstString to be a ConstValue for 
 * now as a simplification.
 */
public class ConstString extends ConstValue {
    /* The tag associated with ConstClass entries */
    public static final int MyTag = CONSTANTString;

    /* The name of the class being referred to */
    private ConstUtf8 stringValue;

    /* The index of name of the class being referred to
     *  - used while reading from a class file */
    private int stringValueIndex;

    /* public accessors */

    /**
     * Return the tag for this constant
     */
    public int tag() { return MyTag; }

    /**
     * Return the utf8 string calue
     */
    public ConstUtf8 value() {
        return stringValue;
    }

    /**
     * Return the descriptor string for the constant type.
     */
    public String descriptor() {
        return "Ljava/lang/String;";
    }

    /**
     * A printable representation 
     */
    public String toString() {
        return "CONSTANTString(" + indexAsString() + "): " + 
            "string(" + stringValue.asString() + ")";
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof ConstString)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        ConstString other = (ConstString)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (!this.stringValue.isEqual(msg, other.stringValue)) {
            msg.push(String.valueOf("stringValue = "
                                    + other.stringValue));
            msg.push(String.valueOf("stringValue = "
                                    + this.stringValue));
            return false;
        }
        return true;
    }

    /* package local methods */

    ConstString(ConstUtf8 s) {
        stringValue = s;
    }

    ConstString(int sIndex) {
        stringValueIndex = sIndex;
    }

    void formatData(DataOutputStream b) throws IOException {
        b.writeShort(stringValue.getIndex());
    }
    static ConstString read(DataInputStream input) throws IOException {
        return new ConstString(input.readUnsignedShort());
    }
    void resolve(ConstantPool p) {
        stringValue = (ConstUtf8) p.constantAt(stringValueIndex);
    }
}
