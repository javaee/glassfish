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
 * ConstFieldRef represents a reference to a field of some class
 * in the constant pool of a class file.
 */
public class ConstFieldRef extends ConstBasicMemberRef {
    /* The tag associated with ConstFieldRef */
    public static final int MyTag = CONSTANTFieldRef;

    /* public accessors */
    public int tag () { return MyTag; }

    public String toString () {
        return "CONSTANTFieldRef(" + indexAsString() + "): " + 
            super.toString();
    }

    /* package local methods */

    ConstFieldRef (ConstClass cname, ConstNameAndType NT) {
        super(cname, NT);
    }

    ConstFieldRef (int cnameIndex, int NT_index) {
        super(cnameIndex, NT_index);
    }

    static ConstFieldRef read (DataInputStream input) throws IOException {
        int cname = input.readUnsignedShort();
        int NT = input.readUnsignedShort();
        return new ConstFieldRef (cname, NT);
    }
}
