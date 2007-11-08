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
 * An instruction which requires a integral constant as an immediate operand 
 */

public class InsnIntOp extends Insn {
    /* The operand */
    private int operandValue;

    /* public accessors */

    public int nStackArgs() {
        return VMOp.ops[opcode()].nStackArgs();
    }

    public int nStackResults() {
        return VMOp.ops[opcode()].nStackResults();
    }

    public String argTypes() {
        return VMOp.ops[opcode()].argTypes();
    }

    public String resultTypes() {
        return VMOp.ops[opcode()].resultTypes();
    }

    public boolean branches() {
        return opcode() == opc_ret;
    }

    public int value() {
        return operandValue;
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnIntOp)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnIntOp other = (InsnIntOp)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (this.operandValue != other.operandValue) {
            msg.push(String.valueOf("operandValue = "
                                    + other.operandValue));
            msg.push(String.valueOf("operandValue = "
                                    + this.operandValue));
            return false;
        }
        return true;
    }

    /* package local methods */

    static String primType(int primIndex) {
        switch (primIndex) {
        case T_BOOLEAN:
            return "boolean";
        case T_CHAR:
            return "char";
        case T_FLOAT:
            return "float";
        case T_DOUBLE:
            return "double";
        case T_BYTE:
            return "byte";
        case T_SHORT:
            return "short";
        case T_INT:
            return "int";
        case T_LONG:
            return "long";
        default:
            throw new InsnError ("Invalid primitive type(" + primIndex + ")");
        }
    }

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        if (opcode() == opc_newarray) 
            out.println(offset() + "  opc_newarray  " + primType(operandValue));
        else
            out.println(offset() + "  " + opName(opcode()) + "  " + operandValue);
    }

    int store(byte[] buf, int index) {
        if (size() == 4) {
            /* prefix with an opc_wide */
            buf[index++] = (byte) opc_wide;
        }

        buf[index++] = (byte) opcode();
        if (size() > 2)
            buf[index++] = (byte)(operandValue >> 8);
        buf[index++] = (byte)(operandValue & 0xff);
        return index;
    }


    /* return the size of the instruction in bytes */

    int size() {
        switch(opcode()) {
        case opc_bipush:
        case opc_newarray:
            /* These are always 1 byte constants */
            return 2;

        case opc_sipush: /* a short constant */
            /* This is always a 2 byte constant */
            return 3;

        case opc_iload:
        case opc_lload:
        case opc_fload:
        case opc_dload:
        case opc_aload:
        case opc_istore:
        case opc_lstore:
        case opc_fstore:
        case opc_dstore:
        case opc_astore:
        case opc_ret:
            /* These can be one or two byte constants specifying a local var.
             * If a two byte constant, the constant is prefixed by a wide
             * instruction */
            if (operandValue < 256)
                return 2;
            else
                return 4;

        default:
            throw new InsnError ("invalid instruction " + opName(opcode()) +
                                 " with an integer operand");
        }
    }


    InsnIntOp (int theOpcode, int theOperand, int pc) {
        super(theOpcode, pc);

        operandValue = theOperand;
    }


    InsnIntOp (int theOpcode, int theOperand) {
        super(theOpcode, NO_OFFSET);

        operandValue = theOperand;
        switch(theOpcode) {
        case opc_bipush:
        case opc_newarray:
            /* These are always 1 byte constants */

        case opc_sipush: /* a short constant */
            /* This is always a 2 byte constant */

        case opc_dload:
        case opc_lload:
        case opc_iload:
        case opc_fload:
        case opc_aload:
        case opc_istore:
        case opc_lstore:
        case opc_fstore:
        case opc_dstore:
        case opc_astore:
        case opc_ret:
            /* These can be one or two byte constants specifying a local var */
            break;

        default:
            throw new InsnError ("attempt to create an " + opName(theOpcode) +
                                 " with an integer operand");
        }
    }
}
