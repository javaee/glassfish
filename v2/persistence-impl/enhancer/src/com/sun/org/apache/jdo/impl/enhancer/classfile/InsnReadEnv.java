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

/**
 * Environment for decoding byte codes into instructions
 */
class InsnReadEnv {

    /* The parent method environment */
    private CodeEnv codeEnv;

    /* The byte codes to be decoded */
    private byte[] byteCodes;

    /* The index into byteCodes for the next instruction to be decoded */
    private int currPc;

    /**
     * Constructor
     */
    InsnReadEnv(byte[] bytes, CodeEnv codeEnv) {
        this.byteCodes = bytes;
        this.currPc = 0;
        this.codeEnv = codeEnv;
    }

    /**
     * Return the index of the next instruction to decode
     */
    int currentPC() {
        return currPc;
    }

    /**
     * Are there more byte codes to decode?
     */
    boolean more() {
        return currPc < byteCodes.length;
    }

    /**
     * Get a single byte from the byte code stream
     */
    byte getByte() {
        if (!more())
            throw new InsnError("out of byte codes");

        return byteCodes[currPc++];
    }

    /**
     * Get a single unsigned byte from the byte code stream
     */
    int getUByte() {
        return getByte() & 0xff;
    }

    /**
     * Get a short from the byte code stream
     */
    int getShort() {
        byte byte1 = byteCodes[currPc++];
        byte byte2 = byteCodes[currPc++];
        return (byte1 << 8) | (byte2 & 0xff);
    }

    /**
     * Get an unsigned short from the byte code stream
     */
    int getUShort() {
        return getShort() & 0xffff;
    }

    /**
     * Get an int from the byte code stream
     */
    int getInt() {
        byte byte1 = byteCodes[currPc++];
        byte byte2 = byteCodes[currPc++];
        byte byte3 = byteCodes[currPc++];
        byte byte4 = byteCodes[currPc++];
        return (byte1 << 24) | ((byte2 & 0xff) << 16) |
	    ((byte3  & 0xff) << 8) | (byte4 & 0xff);
    }

    /**
     * Get the constant pool which applies to the method being decoded
     */
    ConstantPool pool() {
        return codeEnv.pool();
    }

    /**
     * Get the canonical InsnTarget instance for the specified
     * pc within the method.
     */
    InsnTarget getTarget(int targ) {
        return codeEnv.getTarget(targ);
    }
}
