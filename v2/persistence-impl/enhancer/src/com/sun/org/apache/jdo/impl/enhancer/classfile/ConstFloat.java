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
import java.util.Stack;

/**
 * Class representing a float constant in the constant pool of a class file
 */
public class ConstFloat extends ConstValue {
    /* The tag value associated with ConstFloat */
    public static final int MyTag = CONSTANTFloat;

    /* The value */
    private float floatValue;

    /* public accessors */

    /**
     * The tag of this constant entry
     */
    public int tag() { return MyTag; }

    /**
     * return the value associated with the entry
     */
    public float value() {
        return floatValue;
    }

    /**
     * Return the descriptor string for the constant type.
     */
    public String descriptor() {
        return "F";
    }

    /**
     * A printable representation
     */
    public String toString() {
        return "CONSTANTFloat(" + indexAsString() + "): " + 
            "floatValue(" + Float.toString(floatValue) + ")";
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof ConstFloat)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        ConstFloat other = (ConstFloat)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (this.floatValue != other.floatValue) {
            msg.push(String.valueOf("floatValue = "
                                    + other.floatValue));
            msg.push(String.valueOf("floatValue = "
                                    + this.floatValue));
            return false;
        }
        return true;
    }

    /* package local methods */

    ConstFloat(float f) {
        floatValue = f;
    }

    void formatData(DataOutputStream b) throws IOException {
        b.writeFloat(floatValue);
    }

    static ConstFloat read(DataInputStream input) throws IOException {
        return new ConstFloat(input.readFloat());
    }

    void resolve(ConstantPool p) { }
}
