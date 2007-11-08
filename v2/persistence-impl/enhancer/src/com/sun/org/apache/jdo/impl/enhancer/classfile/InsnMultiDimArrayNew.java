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
 * Special instruction form for the opc_multianewarray instruction
 */
public class InsnMultiDimArrayNew extends Insn {
    /* The array class for creation */
    private ConstClass classOp;

    /* The number of dimensions present on the stack */
    private int nDimsOp;

    /* public accessors */

    public boolean isSimpleLoad() {
        return false;
    }

    public int nStackArgs() {
        return nDimsOp;
    }

    public int nStackResults() {
        return 1;
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<nDimsOp; i++) {
            buf.append("I");
        }
        return buf.toString();
    }

    /**
     * What are the types of the stack results?
     */
    public String resultTypes() {
        return "A";
    }

    public boolean branches() {
        return false;
    }

    /**
     * Return the array class being created
     */
    public ConstClass arrayClass() {
        return classOp;
    }

    /**
     * Sets the array class being created
     */
    public void setArrayClass(ConstClass classOp) {
        this.classOp = classOp;
    }

    /**
     * Return the number of dimensions of the array class being created
     */
    public int nDims() {
        return nDimsOp;
    }

    /**
     * Constructor for opc_multianewarray.
     * classOp must be an array class
     * nDimsOp must be > 0 and <= number of array dimensions for classOp
     */
    public InsnMultiDimArrayNew (ConstClass classOp, int nDimsOp) {
        this(classOp, nDimsOp, NO_OFFSET);
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnMultiDimArrayNew)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnMultiDimArrayNew other = (InsnMultiDimArrayNew)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (!this.classOp.isEqual(msg, other.classOp)) {
            msg.push(String.valueOf("classOp = "
                                    + other.classOp));
            msg.push(String.valueOf("classOp = "
                                    + this.classOp));
            return false;
        }
        if (this.nDimsOp != other.nDimsOp) {
            msg.push(String.valueOf("nDimsOp = "
                                    + other.nDimsOp));
            msg.push(String.valueOf("nDimsOp = "
                                    + this.nDimsOp));
            return false;
        }
        return true;
    }

    /* package local methods */

    InsnMultiDimArrayNew (ConstClass classOp, int nDimsOp, int offset) {
        super(opc_multianewarray, offset);

        this.classOp = classOp;
        this.nDimsOp = nDimsOp; 

        if (classOp == null || nDimsOp < 1)
            throw new InsnError ("attempt to create an opc_multianewarray" +
                                 " with invalid operands");
    }  

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_multianewarray  pool(" +
                    classOp.getIndex() + ")," + nDimsOp);
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        index = storeShort(buf, index, (short) classOp.getIndex());
        buf[index++] = (byte) nDimsOp;
        return index;
    }

    int size() {
        return 4;
    }

    static InsnMultiDimArrayNew read (InsnReadEnv insnEnv, int myPC) {
        ConstClass classOp = (ConstClass)
            insnEnv.pool().constantAt(insnEnv.getUShort());
        int nDims = insnEnv.getUByte();
        return new InsnMultiDimArrayNew(classOp, nDims, myPC);
    }
}
