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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.Enumeration;

/**
 * ExceptionsAttribute represents a method attribute in a class file
 * listing the checked exceptions for the method.
 */
public class ExceptionsAttribute extends ClassAttribute {
    public final static String expectedAttrName = "Exceptions";

    /* The list of checked exceptions */
    private Vector exceptionTable;

    /* public accessors */

    /**
     *  Return an enumeration of the checked exceptions
     */
    public Enumeration exceptions() {
        return exceptionTable.elements();
    }

    /**
     * Returns the vector of the checked exceptions.
     */
    //@olsen: added method
    public Vector getExceptions() {
        return exceptionTable;
    }

    /**
     * Constructor
     */
    public ExceptionsAttribute(ConstUtf8 attrName, Vector excTable) {
        super(attrName);
        exceptionTable = excTable;
    }

    /**
     * Convenience Constructor - for single exception
     */
    public ExceptionsAttribute(ConstUtf8 attrName, ConstClass exc) {
        super(attrName);
        exceptionTable = new Vector(1);
        exceptionTable.addElement(exc);
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof ExceptionsAttribute)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        ExceptionsAttribute other = (ExceptionsAttribute)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }

        if (this.exceptionTable.size() != other.exceptionTable.size()) {
            msg.push("exceptionTable.size() "
                     + String.valueOf(other.exceptionTable.size()));
            msg.push("exceptionTable.size() "
                     + String.valueOf(this.exceptionTable.size()));
            return false;
        }

        // sort exceptions by name
        class ConstClassComparator implements Comparator {
            public int compare(Object o1, Object o2) {
                ConstClass c1 = (ConstClass)o1;
                ConstClass c2 = (ConstClass)o2;
                String s1 = c1.className().asString();
                String s2 = c2.className().asString();
                return s1.compareTo(s2);
            }
        }
        ConstClassComparator comparator = new ConstClassComparator();
        ConstClass[] thisExceptionTable
            = (ConstClass[])this.exceptionTable.toArray(new ConstClass[0]);
        ConstClass[] otherExceptionTable
            = (ConstClass[])other.exceptionTable.toArray(new ConstClass[0]);
        Arrays.sort(thisExceptionTable, comparator);
        Arrays.sort(otherExceptionTable, comparator);
        for (int i = 0; i < exceptionTable.size(); i++) {
            ConstClass c1 = thisExceptionTable[i];
            ConstClass c2 = otherExceptionTable[i];
            if (!c1.isEqual(msg, c2)) {
                msg.push("exceptionTable[i] = " + String.valueOf(c2));
                msg.push("exceptionTable[i] = " + String.valueOf(c1));
                return false;
            }
        }
        return true;
    }

    /* package local methods */

    static ExceptionsAttribute read(ConstUtf8 attrName,
                                    DataInputStream data, ConstantPool pool)
        throws IOException {
        int nExcepts = data.readUnsignedShort();
        Vector excTable = new Vector();
        while (nExcepts-- > 0) {
            int excIndex = data.readUnsignedShort();
            ConstClass exc_class = null;
            if (excIndex != 0)
                exc_class = (ConstClass) pool.constantAt(excIndex);
            excTable.addElement(exc_class);
        }
        
        return new ExceptionsAttribute(attrName, excTable);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        out.writeInt(2+2*exceptionTable.size());
        out.writeShort(exceptionTable.size());
        for (int i=0; i<exceptionTable.size(); i++)
            out.writeShort(((ConstClass) exceptionTable.elementAt(i)).getIndex());
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("Exceptions:");
        for (int i=0; i<exceptionTable.size(); i++)
            out.print(" " + ((ConstClass) exceptionTable.elementAt(i)).asString());
        out.println();
    }
  
}
