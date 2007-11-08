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
import java.util.Vector;
import java.util.Stack;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A list of attributes within a class file.
 * These lists occur in several places within a class file
 *    - at class level
 *    - at method level
 *    - at field level
 *    - at attribute level
 */
public class AttributeVector {

    /* Vector of ClassAttribute */
    private ClassAttribute attributes[] = null;

    /**
     * Returns the i'th attribute in the array
     */
    private ClassAttribute attrAt(int i) {
        return attributes[i];
    }

    /**
     * Construct an empty AttributeVector
     */
    public AttributeVector() { }

    /**
     * Add an element to the vector
     */
    public void addElement(ClassAttribute attr) {
        if (attributes == null)
            attributes = new ClassAttribute[1];
        else {
            ClassAttribute newAttributes[] = new ClassAttribute[attributes.length+1];
            System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
            attributes = newAttributes;
        }
        attributes[attributes.length-1] = attr;
    }

    public Enumeration elements() {
        class AttributeVectorEnumeration implements Enumeration {
            private ClassAttribute[] attributes;
            private int current = 0;

            AttributeVectorEnumeration(ClassAttribute attrs[]) {
                attributes = attrs;
            }

            public boolean hasMoreElements() {
                return attributes != null && current < attributes.length;
            }
            public Object nextElement() {
                if (!hasMoreElements())
                    throw new NoSuchElementException();
                return attributes[current++];
            }
        }

        return new AttributeVectorEnumeration(attributes);
    }

    /**
     * Look for an attribute of a specific name
     */
    public ClassAttribute findAttribute(String attrName) {
        Enumeration e = elements();
        while (e.hasMoreElements()) {
            ClassAttribute attr = (ClassAttribute) e.nextElement();
            if (attr.attrName().asString().equals(attrName))
                return attr;
        }
        return null;
    }

    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof AttributeVector)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        AttributeVector other = (AttributeVector)obj;

        if (this.attributes.length != other.attributes.length) {
            msg.push("attributes.length "
                     + String.valueOf(other.attributes.length));
            msg.push("attributes.length "
                     + String.valueOf(this.attributes.length));
            return false;
        }

        // sort attributes by name
        class ClassAttributeComparator implements Comparator {
            public int compare(Object o1, Object o2) {
                ClassAttribute a1 = (ClassAttribute)o1;
                ClassAttribute a2 = (ClassAttribute)o2;
                String s1 = a1.attrName().asString();
                String s2 = a2.attrName().asString();
                return s1.compareTo(s2);
            }
        }
        ClassAttributeComparator comparator = new ClassAttributeComparator();
        ClassAttribute[] thisAttributes
            = (ClassAttribute[])this.attributes.clone();
        ClassAttribute[] otherAttributes
            = (ClassAttribute[])other.attributes.clone();
        Arrays.sort(thisAttributes, comparator);
        Arrays.sort(otherAttributes, comparator);
        for (int i = 0; i < attributes.length; i++) {
            ClassAttribute a1 = thisAttributes[i];
            ClassAttribute a2 = otherAttributes[i];
            if (!a1.isEqual(msg, a2)) {
                msg.push("attributes[i] = " + String.valueOf(a2));
                msg.push("attributes[i] = " + String.valueOf(a1));
                return false;
            }
        }
        return true;
    }

    /**
     * General attribute reader
     */
    static AttributeVector readAttributes(
	DataInputStream data, ConstantPool constantPool)
	throws IOException {
        AttributeVector attribs = new AttributeVector();
        int n_attrs = data.readUnsignedShort();
        while (n_attrs-- > 0) {
            attribs.addElement(ClassAttribute.read(data, constantPool));
        }
        return attribs;
    }

    /**
     * ClassMethod attribute reader
     */
    static AttributeVector readAttributes(
	DataInputStream data, CodeEnv codeEnv)
	throws IOException {
        AttributeVector attribs = new AttributeVector();
        int n_attrs = data.readUnsignedShort();
        while (n_attrs-- > 0) {
            attribs.addElement(ClassAttribute.read(data, codeEnv));
        }
        return attribs;
    }

    /**
     * Write the attributes to the output stream
     */
    void write(DataOutputStream out) throws IOException {
        if (attributes == null) {
            out.writeShort(0);
        } else {
            out.writeShort(attributes.length);
            for (int i=0; i<attributes.length; i++)
                attributes[i].write(out);
        }
    }

    /**
     * Print a description of the attributes
     */
    void print(PrintStream out, int indent) {
        if (attributes != null) {
            for (int i=0; i<attributes.length; i++)
                attributes[i].print(out, indent);
        }
    }

    /**
     * Print a brief summary of the attributes
     */
    //@olsen: added 'out' and 'indent' parameters
    void summarize(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println((attributes == null ? 0 : attributes.length) +
                           " attributes");
    }
}
