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


import java.io.PrintStream;
import java.util.Stack;

/**
 * Special instruction form for the opc_iinc instruction
 */
public class InsnIInc extends Insn {

    /* The local variable slot to be incremented */
    private int localVarIndex;

    /* The amount by which the slot is to be incremented */
    private int value;

    /* public accessors */

    public int nStackArgs() {
        return 0;
    }

    public int nStackResults() {
        return 0;
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        return "";
    }

    /**
     * What are the types of the stack results?
     */
    public String resultTypes() {
        return "";
    }

    public boolean branches() {
        return false;
    }

    /**
     * The local variable slot to be incremented
     */
    public int varIndex() {
        return localVarIndex;
    }

    /**
     * The amount by which the slot is to be incremented 
     */
    public int incrValue() {
        return value;
    }
  
    /**
     * Constructor for opc_iinc instruction
     */
    public InsnIInc (int localVarIndex, int value) {
        this(localVarIndex, value, NO_OFFSET);
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnIInc)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnIInc other = (InsnIInc)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (this.localVarIndex != other.localVarIndex) {
            msg.push(String.valueOf("localVarIndex = "
                                    + other.localVarIndex));
            msg.push(String.valueOf("localVarIndex = "
                                    + this.localVarIndex));
            return false;
        }
        if (this.value != other.value) {
            msg.push(String.valueOf("value = "
                                    + other.value));
            msg.push(String.valueOf("value = "
                                    + this.value));
            return false;
        }
        return true;
    }

    /* package local methods */

    InsnIInc (int localVarIndex, int value, int pc) {
        super(opc_iinc, pc);

        this.localVarIndex = localVarIndex;
        this.value =value;
    }

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_iinc  " + 
                    localVarIndex + "," + value);
    }

    int store(byte[] buf, int index) {
        if (isWide())
            buf[index++] = (byte) opc_wide;
        buf[index++] = (byte) opcode();
        if (isWide()) {
            index = storeShort(buf, index, (short) localVarIndex);
            index = storeShort(buf, index, (short) value);
        } else {
            buf[index++] = (byte)localVarIndex;
            buf[index++] = (byte)value;
        }
        return index;
    }

    int size() {
        return isWide() ? 6 : 3;
    }

    private boolean isWide() {
        return (value > 127 || value < -128 || localVarIndex > 255);
    }

}
