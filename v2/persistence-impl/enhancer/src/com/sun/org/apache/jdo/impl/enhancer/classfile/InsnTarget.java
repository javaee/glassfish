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
 * InsnTarget is a pseudo-instruction which represents a branch target
 * in an instruction stream.
 */
public class InsnTarget extends Insn {

    private boolean branchTarget = false;

    public int nStackArgs() {
        return 0;
    }

    public int nStackResults() {
        return 0;
    }

    public String argTypes() {
        return "";
    }

    public String resultTypes() {
        return "";
    }

    public boolean branches() {
        return false;
    }

    public void setBranchTarget() {
        branchTarget = true;
    }

    /* not valid unless method instructions processed specially */
    public boolean isBranchTarget() {
        return branchTarget;
    }

    /**
     * Constructor
     */
    public InsnTarget() {
        super(opc_target, NO_OFFSET);
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnTarget)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnTarget other = (InsnTarget)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (this.branchTarget != other.branchTarget) {
            msg.push(String.valueOf("branchTarget = "
                                    + other.branchTarget));
            msg.push(String.valueOf("branchTarget = "
                                    + this.branchTarget));
            return false;
        }
        return true;
    }

    /* package local methods */

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + ":");
    }

    int store(byte buf[], int index) {
        return index;
    }

    int size() {
        return 0;
    }

    InsnTarget(int offset) {
        super(opc_target, offset);
    }
}
