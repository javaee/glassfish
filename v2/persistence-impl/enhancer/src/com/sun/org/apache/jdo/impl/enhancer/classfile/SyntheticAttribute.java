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

//@olsen: fix 4467428, added class for synthetic attribute to be added
// to generated jdo fields and methods

/**
 * A SyntheticAttribute is a fixed-length attribute in the attributes table
 * of ClassFile, ClassField, and ClassMethod structures.  A class member
 * that does not appear in the source code must be marked using a
 * SyntheticAttribute. 
 */
public class SyntheticAttribute extends ClassAttribute {
    /* The expected name of this attribute */
    public static final String expectedAttrName = "Synthetic";

    /** 
     * Construct a constant value attribute
     */
    public SyntheticAttribute(ConstUtf8 attrName) {
        super(attrName);
        //System.out.println("new SyntheticAttribute()");
    }

    /* package local methods */

    static SyntheticAttribute read(ConstUtf8 attrName,
                                   DataInputStream data,
                                   ConstantPool pool)
        throws IOException {
        return new SyntheticAttribute(attrName);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        final int attributeBytesLength = 0;
        out.writeInt(attributeBytesLength);
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(expectedAttrName);
    }
}
