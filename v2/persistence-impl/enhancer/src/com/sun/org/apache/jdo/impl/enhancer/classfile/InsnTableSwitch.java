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
 * Special instruction form for the opc_tableswitch instruction
 */
public class InsnTableSwitch extends Insn {
    /* The lowest value in the jump table */
    private int lowOp;

    /* The default target for the switch */
    private InsnTarget defaultOp;

    /* The targets for the switch - a switch value of lowOp dispatches
     * to targetsOp[0], lowOp+1 dispatches to targetsOp[1], etc. */
    private InsnTarget[] targetsOp;

    /* public accessors */

    public int nStackArgs() {
        return 1;
    }

    public int nStackResults() {
        return 0;
    }

    public String argTypes() {
        return "I";
    }

    public String resultTypes() {
        return "";
    }

    public boolean branches() {
        return true;
    }

    /**
     * Mark possible branch targets
     */
    public void markTargets() {
        defaultOp.setBranchTarget();
        for (int i=0; i<targetsOp.length; i++)
            targetsOp[i].setBranchTarget();
    }

    /**
     * Return the lowest case for the switch
     */
    public int lowCase() {
        return lowOp;
    }

    /**
     * Return the defaultTarget for the switch
     */
    public InsnTarget defaultTarget() {
        return defaultOp;
    }

    /**
     * Return the targets for the cases of the switch.
     */
    public InsnTarget[] switchTargets() {
        return targetsOp;
    }

    /**
     * Constructor for opc_tableswitch
     */
    //@olsen: made public
    public InsnTableSwitch(int lowOp, InsnTarget defaultOp, 
                           InsnTarget[] targetsOp) {
        this(lowOp, defaultOp, targetsOp, NO_OFFSET);
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnTableSwitch)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnTableSwitch other = (InsnTableSwitch)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (this.lowOp != other.lowOp) {
            msg.push(String.valueOf("lowOp = "
                                    + other.lowOp));
            msg.push(String.valueOf("lowOp = "
                                    + this.lowOp));
            return false;
        }

        if (!this.defaultOp.isEqual(msg, other.defaultOp)) {
            msg.push(String.valueOf("defaultOp = "
                                    + other.defaultOp));
            msg.push(String.valueOf("defaultOp = "
                                    + this.defaultOp));
            return false;
        }

        if (this.targetsOp.length != other.targetsOp.length) {
            msg.push("targetsOp.length "
                     + String.valueOf(other.targetsOp.length));
            msg.push("targetsOp.length "
                     + String.valueOf(this.targetsOp.length));
            return false;
        }
        for (int i = 0; i < targetsOp.length; i++) {
            InsnTarget t1 = this.targetsOp[i];
            InsnTarget t2 = other.targetsOp[i];
            if (!t1.isEqual(msg, t2)) {
                msg.push("targetsOp[" + i + "] = " + String.valueOf(t2));
                msg.push("targetsOp[" + i + "] = " + String.valueOf(t1));
                return false;
            }
        }
        return true;
    }

    /* package local methods */

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_tableswitch  ");
        for (int i=0; i<targetsOp.length; i++) {
            int index = i + lowOp;
            if (targetsOp[i].offset() != defaultOp.offset()) {
                ClassPrint.spaces(out, indent+2);
                out.println(index + " -> " + targetsOp[i].offset());
            }
        }
        ClassPrint.spaces(out, indent+2);
        out.println("default -> " + defaultOp.offset());
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        index = (index + 3) & ~3;
        index = storeInt(buf, index, defaultOp.offset() - offset());
        index = storeInt(buf, index, lowOp);
        index = storeInt(buf, index, lowOp+targetsOp.length-1);
        for (int i=0; i<targetsOp.length; i++)
            index = storeInt(buf, index, targetsOp[i].offset() - offset());
        return index;
    }

    int size() {
        /* account for the instruction, 0-3 bytes of pad, 3 ints */
        int basic = ((offset() + 4) & ~3) - offset() + 12;
        /* Add 4*number of offsets */
        return basic + targetsOp.length*4;
    }


    InsnTableSwitch(int lowOp, InsnTarget defaultOp, 
                    InsnTarget[] targetsOp, int offset) {
        super(opc_tableswitch, offset);

        this.lowOp = lowOp;
        this.defaultOp = defaultOp; 
        this.targetsOp = targetsOp;

        if (defaultOp == null || targetsOp == null)
            throw new InsnError ("attempt to create an opc_tableswitch" +
                                 " with invalid operands");
    }

    static InsnTableSwitch read (InsnReadEnv insnEnv, int myPC) {
        /* eat up any padding */
        int thisPC = myPC +1;
        for (int pads = ((thisPC + 3) & ~3) - thisPC; pads > 0; pads--)
            insnEnv.getByte();
        InsnTarget defaultTarget = insnEnv.getTarget(insnEnv.getInt() + myPC);
        int low = insnEnv.getInt();
        int high = insnEnv.getInt();
        InsnTarget[] offsets = new InsnTarget[high - low + 1];
        for (int i=0; i<offsets.length; i++)
            offsets[i] = insnEnv.getTarget(insnEnv.getInt() + myPC);
        return new InsnTableSwitch(low, defaultTarget,   offsets, myPC);
    }
}
