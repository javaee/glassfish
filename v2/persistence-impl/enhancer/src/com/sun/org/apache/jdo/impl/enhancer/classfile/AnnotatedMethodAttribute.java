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
 * AnnotatedMethodAttribute represents a class level attribute
 * class file which identifies the level of annotation of the class.
 */
public class AnnotatedMethodAttribute extends ClassAttribute {

    /* The expected attribute name */
    public final static String expectedAttrName = "filter.annotatedMethod";

    /* The expected attribute version */
    public final static short expectedAttrVersion = 1;

    /* Bit mask indicating that the class was filter generated */
    public final static short generatedFlag = 0x1;

    /* Bit mask indicating that the class was filter annotated */
    public final static short annotatedFlag = 0x2;

    /* Bit mask indicating that the class was "repackaged" */
    public final static short modifiedFlag = 0x4;

    /* The version of the attribute */
    private short attrVersion;

    /* Flags associated with the annotation */
    private short annotationFlags;

    /* list of targets in the code sequence delimiting inserted instruction
     * sequences.  Even index targets are a range start (inclusive) and odd
     * targets represent a range end (exclusive) */
    private InsnTarget annotationRanges[];

    /* public accessors */

    public short getVersion() {
        return attrVersion;
    }

    public void setVersion(short version) {
        attrVersion = version;
    }

    public short getFlags() {
        return annotationFlags;
    }

    public void setFlags(short flags) {
        annotationFlags = flags;
    }

    public InsnTarget[] getAnnotationRanges() {
        return annotationRanges;
    }

    public void setAnnotationRanges(InsnTarget[] ranges) {
        annotationRanges = ranges;
    }

    /**
     * Constructor
     */
    public AnnotatedMethodAttribute(
	ConstUtf8 nameAttr, short version, short annFlags,
	InsnTarget[] annRanges) {
        super(nameAttr);
        attrVersion = version;
        annotationFlags = annFlags;
        annotationRanges = annRanges;
    }

    /* package local methods */

    static AnnotatedMethodAttribute read(
	ConstUtf8 attrName, DataInputStream data, CodeEnv env)
        throws IOException {
        short version = data.readShort();
        short annFlags = data.readShort();

        short nRanges = data.readShort();

        InsnTarget ranges[] = new InsnTarget[nRanges*2];
        for (int i=0; i<nRanges; i++) {
            ranges[i*2] = env.getTarget(data.readShort());
            ranges[i*2+1] = env.getTarget(data.readShort());
        }
        return  new AnnotatedMethodAttribute(attrName, version, annFlags, ranges);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        if (annotationRanges == null)
            out.writeShort(2);
        else
            out.writeShort(4 + 2 * annotationRanges.length);
        out.writeShort(attrVersion);
        out.writeShort(annotationFlags);
        if (annotationRanges == null)
            out.writeShort(0);
        else {
            out.writeShort(annotationRanges.length / 2);
            for (int i=0; i<annotationRanges.length; i++)
                out.writeShort(annotationRanges[i].offset());
        }
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("version: " + attrVersion);
        out.println(" flags: " + annotationFlags);
        if (annotationRanges != null) {
            out.println("Annotations: ");
            for (int i=0; i<annotationRanges.length/2; i++) {
                ClassPrint.spaces(out, indent+2);
                out.println(annotationRanges[i*2] + " to " +
                            annotationRanges[i*2+1]);
            }
        }
    }
}
