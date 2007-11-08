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
 * An instruction which requires a single branch offset
 * as an immediate operand .
 */
public class InsnTargetOp extends Insn {
    /* The branch target */
    InsnTarget targetOp;

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
        return true;
    }

    /**
     * Mark possible branch targets
     */
    public void markTargets() {
        targetOp.setBranchTarget();
    }

    /**
     * Return the branch target which is the immediate operand
     */
    public InsnTarget target() {
        return targetOp;
    }
    
    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnTargetOp)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnTargetOp other = (InsnTargetOp)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (!this.targetOp.isEqual(msg, other.targetOp)) {
            msg.push(String.valueOf("targetOp = "
                                    + other.targetOp));
            msg.push(String.valueOf("targetOp = "
                                    + this.targetOp));
            return false;
        }
        return true;
    }

    /* package local methods */

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        /* print offset in non-relative form for readability */
        out.println(offset() + "  " + opName(opcode()) + "  " + 
                    targetOp.offset());
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        int off = targetOp.offset() - offset();
        if (opcode() == opc_goto_w || opcode() == opc_jsr_w)
            return storeInt(buf, index, off);
        else
            return storeShort(buf, index, (short)off);
    }

    int size() {
        if (opcode() == opc_goto_w || opcode() == opc_jsr_w)
            return 5;
        return 3;
    }

    InsnTargetOp (int theOpcode, InsnTarget theOperand, int pc) {
        super(theOpcode, pc);
        targetOp = theOperand;
    }

    InsnTargetOp (int theOpcode, InsnTarget theOperand) {
        super(theOpcode, NO_OFFSET);

        targetOp = theOperand;

        switch(theOpcode) {
        case opc_ifeq:
        case opc_ifne:
        case opc_iflt:
        case opc_ifge:
        case opc_ifgt:
        case opc_ifle:
        case opc_if_icmpeq:
        case opc_if_icmpne:
        case opc_if_icmplt:
        case opc_if_icmpge:
        case opc_if_icmpgt:
        case opc_if_icmple:
        case opc_if_acmpeq:
        case opc_if_acmpne:
        case opc_goto:
        case opc_jsr:
        case opc_ifnull:
        case opc_ifnonnull:
        case opc_goto_w:
        case opc_jsr_w:
            /* Target */
            if (theOperand == null)
                throw new InsnError ("attempt to create an " + opName(theOpcode) +
                                     " with a null Target operand");
            break;

        default:
            throw new InsnError ("attempt to create an " + opName(theOpcode) +
                                 " with an InsnTarget operand");
        }
    }
}
