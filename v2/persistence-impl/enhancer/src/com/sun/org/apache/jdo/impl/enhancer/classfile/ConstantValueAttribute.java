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
 * ConstantValueAttribute represents a constant value attribute 
 * in a class file.  These attributes are used as initialization
 * values for static fields.
 */
public class ConstantValueAttribute extends ClassAttribute {
    /* The expected name of this attribute */
    public static final String expectedAttrName = "ConstantValue";

    /* The value */
    private ConstValue constantValue;

    /* public accessors */

    public ConstValue value() {
        return constantValue;
    }

    /** 
     * Construct a constant value attribute
     */
    public ConstantValueAttribute(ConstUtf8 attrName, ConstValue value) {
        super(attrName);
        constantValue = value;
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof ConstantValueAttribute)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        ConstantValueAttribute other = (ConstantValueAttribute)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (!this.constantValue.isEqual(msg, other.constantValue)) {
            msg.push(String.valueOf("constantValue = "
                                    + other.constantValue));
            msg.push(String.valueOf("constantValue = "
                                    + this.constantValue));
            return false;
        }
        return true;
    }

    /* package local methods */

    static ConstantValueAttribute read(ConstUtf8 attrName,
                                       DataInputStream data, ConstantPool pool)
        throws IOException {
        int index = 0;
        index = data.readUnsignedShort();

        return new ConstantValueAttribute(attrName,
                                          (ConstValue) pool.constantAt(index));
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        out.writeInt(2);
        out.writeShort(constantValue.getIndex());
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("ConstantValue: " + constantValue.toString());
    }
}

