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
 * Special instruction form for the opc_lookupswitch instruction
 */
public class InsnLookupSwitch extends Insn {
    /* The target for the default case */
    private InsnTarget defaultOp;

    /* The int constants against which to perform the lookup */
    private int[] matchesOp;

    /* The branch targets for the cases corresponding to the entries in
     * the matchesOp array */
    private InsnTarget[] targetsOp;

    /* public accessors */

    public int nStackArgs() {
        return 1;
    }

    public int nStackResults() {
        return 0;
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        return "I";
    }

    /**
     * What are the types of the stack results?
     */
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
     * Return the defaultTarget for the switch
     */
    public InsnTarget defaultTarget() {
        return defaultOp;
    }

    /**
     * Return the case values of the switch.
     */
    public int[] switchCases() {
        return matchesOp;
    }

    /**
     * Return the targets for the cases of the switch.
     */
    public InsnTarget[] switchTargets() {
        return targetsOp;
    }

    /**
     * Constructor for opc_lookupswitch
     */
    public InsnLookupSwitch(InsnTarget defaultOp, int[] matchesOp,
                            InsnTarget[] targetsOp) {
        this(defaultOp, matchesOp, targetsOp, NO_OFFSET);
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnLookupSwitch)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnLookupSwitch other = (InsnLookupSwitch)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (!this.defaultOp.isEqual(msg, other.defaultOp)) {
            msg.push(String.valueOf("defaultOp = "
                                    + other.defaultOp));
            msg.push(String.valueOf("defaultOp = "
                                    + this.defaultOp));
            return false;
        }

        if (this.matchesOp.length != other.matchesOp.length) {
            msg.push("matchesOp.length "
                     + String.valueOf(other.matchesOp.length));
            msg.push("matchesOp.length "
                     + String.valueOf(this.matchesOp.length));
            return false;
        }
        for (int i = 0; i < matchesOp.length; i++) {
            int m1 = this.matchesOp[i];
            int m2 = other.matchesOp[i];
            if (m1 != m2) {
                msg.push("matchesOp[" + i + "] = " + String.valueOf(m2));
                msg.push("matchesOp[" + i + "] = " + String.valueOf(m1));
                return false;
            }
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

    InsnLookupSwitch(InsnTarget defaultOp, int[] matchesOp,
                     InsnTarget[] targetsOp, int offset) {
        super(opc_lookupswitch, offset);

        this.defaultOp = defaultOp; 
        this.matchesOp = matchesOp;
        this.targetsOp = targetsOp;

        if (defaultOp == null || targetsOp == null || matchesOp == null ||
            targetsOp.length != matchesOp.length)
            throw new InsnError ("attempt to create an opc_lookupswitch" +
                                 " with invalid operands");
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_lookupswitch  ");
        for (int i=0; i<matchesOp.length; i++) {
            ClassPrint.spaces(out, indent+2);
            out.println(matchesOp[i] + " -> " + targetsOp[i].offset());
        }
        ClassPrint.spaces(out, indent+2);
        out.println("default -> " + defaultOp.offset());
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        index = (index + 3) & ~3;
        index = storeInt(buf, index, defaultOp.offset() - offset());
        index = storeInt(buf, index, targetsOp.length);
        for (int i=0; i<targetsOp.length; i++) {
            index = storeInt(buf, index, matchesOp[i]);
            index = storeInt(buf, index, targetsOp[i].offset() - offset());
        }
        return index;
    }

    int size() {
        /* account for the instruction, 0-3 bytes of pad, 2 ints */
        int basic = ((offset() + 4) & ~3) - offset() + 8;
        /* Add 8*number of offsets */
        return basic + targetsOp.length*8;
    }

    static InsnLookupSwitch read (InsnReadEnv insnEnv, int myPC) {
        /* eat up any padding */
        int thisPC = myPC +1;
        for (int pads = ((thisPC + 3) & ~3) - thisPC; pads > 0; pads--)
            insnEnv.getByte();
        InsnTarget defaultTarget = insnEnv.getTarget(insnEnv.getInt() + myPC);
        int npairs = insnEnv.getInt();
        int matches[] = new int[npairs];
        InsnTarget[] offsets = new InsnTarget[npairs];
        for (int i=0; i<npairs; i++) {
            matches[i] = insnEnv.getInt();
            offsets[i] = insnEnv.getTarget(insnEnv.getInt() + myPC);
        }
        return new InsnLookupSwitch(defaultTarget, matches, offsets, myPC);
    }
}
