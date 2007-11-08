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
 * ExceptionRange represents a range an exception handler within
 * a method in class file.
 */
public class ExceptionRange {
    /* The start of the exception hander (inclusive) */
    private InsnTarget excStartPC;

    /* The end of the exception hander (exclusive) */
    private InsnTarget excEndPC;

    /* The exception handler code */
    private InsnTarget excHandlerPC;

    /* The exception specification */
    private ConstClass excCatchType;

    /* public accessors */

    /**
     * return the start of the exception hander (inclusive)
     */
    public InsnTarget startPC() {
        return excStartPC;
    }

    /**
     * return the end of the exception hander (exclusive)
     */
    public InsnTarget endPC() {
        return excEndPC;
    }

    /**
     * return the exception handler code
     */
    public InsnTarget handlerPC() {
        return excHandlerPC;
    }

    /** 
     * return the exception specification
     * a null return value means a catch of any (try/finally)
     */
    public ConstClass catchType() {
        return excCatchType;
    }

    /**
     * constructor 
     */
    public ExceptionRange(InsnTarget startPC, InsnTarget endPC,
                          InsnTarget handlerPC, ConstClass catchType) {
        excStartPC = startPC;
        excEndPC = endPC;
        excHandlerPC = handlerPC;
        excCatchType = catchType;
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof ExceptionRange)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        ExceptionRange other = (ExceptionRange)obj;

        if (!this.excStartPC.isEqual(msg, other.excStartPC)) {
            msg.push(String.valueOf("excStartPC = "
                                    + other.excStartPC));
            msg.push(String.valueOf("excStartPC = "
                                    + this.excStartPC));
            return false;
        }
        if (!this.excEndPC.isEqual(msg, other.excEndPC)) {
            msg.push(String.valueOf("excEndPC = "
                                    + other.excEndPC));
            msg.push(String.valueOf("excEndPC = "
                                    + this.excEndPC));
            return false;
        }
        if (!this.excHandlerPC.isEqual(msg, other.excHandlerPC)) {
            msg.push(String.valueOf("excHandlerPC = "
                                    + other.excHandlerPC));
            msg.push(String.valueOf("excHandlerPC = "
                                    + this.excHandlerPC));
            return false;
        }
        if (!this.excCatchType.isEqual(msg, other.excCatchType)) {
            msg.push(String.valueOf("excCatchType = "
                                    + other.excCatchType));
            msg.push(String.valueOf("excCatchType = "
                                    + this.excCatchType));
            return false;
        }
        return true;
    }

    /* package local methods */

    static ExceptionRange read(DataInputStream data, CodeEnv env)
        throws IOException {
        InsnTarget startPC = env.getTarget(data.readUnsignedShort());
        InsnTarget endPC = env.getTarget(data.readUnsignedShort());
        InsnTarget handlerPC = env.getTarget(data.readUnsignedShort());
        ConstClass catchType =
            (ConstClass) env.pool().constantAt(data.readUnsignedShort());
        return new ExceptionRange(startPC, endPC, handlerPC, catchType);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(excStartPC.offset());
        out.writeShort(excEndPC.offset());
        out.writeShort(excHandlerPC.offset());
        out.writeShort(excCatchType == null ? 0 : excCatchType.getIndex());
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("Exc Range:");
        if (excCatchType == null)
            out.print("any");
        else
            out.print("'" + excCatchType.asString() + "'");
        out.print(" start = " + Integer.toString(excStartPC.offset()));
        out.print(" end = " + Integer.toString(excEndPC.offset()));
        out.println(" handle = " + Integer.toString(excHandlerPC.offset()));
    }
}
