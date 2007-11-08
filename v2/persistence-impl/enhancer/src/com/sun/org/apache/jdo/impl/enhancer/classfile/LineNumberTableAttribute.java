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
 * LineNumberTableAttribute represents a line number table attribute
 * within a CodeAttribute within a class file
 */
public class LineNumberTableAttribute extends ClassAttribute {
    /* The expected attribute name */
    public final static String expectedAttrName = "LineNumberTable";

    /* The line numbers */
    private short lineNumbers[];

    /* The corresponding instructions */
    private InsnTarget targets[];

    /* public accessors */

    /**
     * Constructor
     */
    public LineNumberTableAttribute(
	ConstUtf8 nameAttr, short lineNums[], InsnTarget targets[]) {
        super(nameAttr);
        lineNumbers = lineNums;
        this.targets = targets;
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof LineNumberTableAttribute)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        LineNumberTableAttribute other = (LineNumberTableAttribute)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        // intentionally ingore any linenumber differences
        return true;
    }

    /* package local methods */

    static LineNumberTableAttribute read(
	ConstUtf8 attrName, DataInputStream data, CodeEnv env)
        throws IOException {
        int nLnums = data.readUnsignedShort();
        short lineNums[] = new short[nLnums];
        InsnTarget targs[] = new InsnTarget[nLnums];
        for (int i=0; i<nLnums; i++) {
            targs[i] = env.getTarget(data.readShort());
            lineNums[i] = data.readShort();
        }
        return  new LineNumberTableAttribute(attrName, lineNums, targs);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        int nlines = lineNumbers.length;
        out.writeInt(2+4*nlines);
        out.writeShort(nlines);
        for (int i=0; i<nlines; i++) {
            out.writeShort(targets[i].offset());
            out.writeShort(lineNumbers[i]);
        }
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("Line Numbers: ");
        for (int i=0; i<lineNumbers.length; i++) {
            ClassPrint.spaces(out, indent+2);
            out.println(Integer.toString(lineNumbers[i]) + " @ " +
                        Integer.toString(targets[i].offset()));
        }
    }
}

