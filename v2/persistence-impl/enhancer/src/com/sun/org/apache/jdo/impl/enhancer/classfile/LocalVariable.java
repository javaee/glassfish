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

/**
 * Represents a local variable within a LocalVariableTable within
 * a CodeAttribute in a class file.
 */
public class LocalVariable {
    /* The pc at which the variable becomes effecive */
    private InsnTarget varStartPC; /* inclusive */

    /* The pc at which the variable becomes in-effecive */
    private InsnTarget varEndPC;   /* exclusive */

    /* The name of the variable */
    private ConstUtf8 varName;

    /* The type signature of the variable */
    private ConstUtf8 varSig;

    /* The slot to which the variable is assigned */
    private int varSlot;

    /* public accessors */

    /**
     * Constructor for a local variable
     */
    public LocalVariable(InsnTarget startPC, InsnTarget endPC,
                         ConstUtf8 name, ConstUtf8 sig, int slot) {
        varStartPC = startPC;
        varEndPC = endPC;
        varName = name;
        varSig = sig;
        varSlot = slot;
    }

    /* package local methods */

    static LocalVariable read(DataInputStream data, CodeEnv env)
        throws IOException {
        int startPC = data.readUnsignedShort();
        InsnTarget startPCTarget = env.getTarget(startPC);
        int length = data.readUnsignedShort();
        InsnTarget endPCTarget = env.getTarget(startPC+length);
        ConstUtf8 name = 
            (ConstUtf8) env.pool().constantAt(data.readUnsignedShort());
        ConstUtf8 sig = 
            (ConstUtf8) env.pool().constantAt(data.readUnsignedShort());
        int slot = data.readUnsignedShort();
        return new LocalVariable(startPCTarget, endPCTarget, name, sig, slot);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(varStartPC.offset());
        out.writeShort(varEndPC.offset() - varStartPC.offset());
        out.writeShort((varName == null) ? 0 : varName.getIndex());
        out.writeShort((varSig == null) ? 0 : varSig.getIndex());
        out.writeShort(varSlot);
    }

    public void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("'" + ((varName == null) ? "(null)" : varName.asString()) + "'");
        out.print(" sig = " + ((varSig == null) ? "(null)" : varSig.asString()));
        out.print(" start_pc = " + Integer.toString(varStartPC.offset()));
        out.print(" length = " +
                  Integer.toString(varEndPC.offset() - varStartPC.offset()));
        out.println(" slot = " + Integer.toString(varSlot));
    }
}
