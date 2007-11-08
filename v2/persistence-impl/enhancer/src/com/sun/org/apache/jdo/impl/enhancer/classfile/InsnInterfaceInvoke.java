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
 * Special instruction form for the opc_invokeinterface instruction
 */
public class InsnInterfaceInvoke extends InsnConstOp {
    /* The number of arguments to the interface method */
    private int nArgsOp;

    /* public accessors */

    public int nStackArgs() {
        return super.nStackArgs();
    }

    public int nStackResults() {
        return super.nStackResults();
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        return super.argTypes();
    }

    /**
     * What are the types of the stack results?
     */
    public String resultTypes() {
        return super.resultTypes();
    }

    public boolean branches() {
        return false;
    }

    /**
     * Return the interface to be invoked
     */
    public ConstInterfaceMethodRef method() {
        return (ConstInterfaceMethodRef) value();
    }

    /**
     * Return the number of arguments to the interface
     */
    public int nArgs() {
        return nArgsOp;
    }

    /**
     * constructor for opc_invokeinterface
     */
    public InsnInterfaceInvoke (ConstInterfaceMethodRef methodRefOp, 
                                int nArgsOp) {
        this(methodRefOp, nArgsOp, NO_OFFSET);
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnInterfaceInvoke)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnInterfaceInvoke other = (InsnInterfaceInvoke)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (this.nArgsOp != other.nArgsOp) {
            msg.push(String.valueOf("nArgsOp = "
                                    + other.nArgsOp));
            msg.push(String.valueOf("nArgsOp = "
                                    + this.nArgsOp));
            return false;
        }
        return true;
    }

    /* package local methods */

    InsnInterfaceInvoke (ConstInterfaceMethodRef methodRefOp, int nArgsOp,
                         int offset) {
        super(opc_invokeinterface, methodRefOp, offset);

        this.nArgsOp = nArgsOp; 

        if (methodRefOp == null || nArgsOp < 0)
            throw new InsnError ("attempt to create an opc_invokeinterface" +
                                 " with invalid operands");
    }

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_invokeinterface  " + 
                    "pool(" + method().getIndex() + ")," + nArgsOp);
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        index = storeShort(buf, index, (short)method().getIndex());
        buf[index++] = (byte) nArgsOp;
        buf[index++] = (byte) 0;
        return index;
    }

    int size() {
        return 5;
    }

    static InsnInterfaceInvoke read(InsnReadEnv insnEnv, int myPC) {
        ConstInterfaceMethodRef iface = (ConstInterfaceMethodRef)
            insnEnv.pool().constantAt(insnEnv.getUShort());
        int nArgs = insnEnv.getUByte();
        insnEnv.getByte(); // eat reserved arg
        return new InsnInterfaceInvoke(iface, nArgs, myPC);
    }
}
