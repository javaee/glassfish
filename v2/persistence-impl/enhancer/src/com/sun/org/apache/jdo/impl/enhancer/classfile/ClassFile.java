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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;
import java.security.MessageDigest;
import java.security.DigestOutputStream;
import java.security.NoSuchAlgorithmException;
import java.io.DataOutputStream;


/**
 * ClassFile models the structure of a class as represented within
 * a class file.
 */
final public class ClassFile implements VMConstants, Serializable {

    /* Class file constants */
    public static final int magic = 0xcafebabe;
    
    /*@craig added more flexible version checking.
     */
    public static final short[] [] jdkMajorMinorVersions = new short[][] {
        new short[] {45,3}, // jdk 1.1
        new short[] {46,0}, // jdk 1.2
        new short[] {47,0}, // jdk 1.3
        new short[] {48,0}  // jdk 1.4
    };
    public static final List jdkVersions = 
        convertMajorMinorVersions(jdkMajorMinorVersions);

    public static final String supportedVersions = printSupportedVersions();
    
    private int majorVersion = 0;
    private int minorVersion = 0;
    
    /* The constant pool for the class file */
    private ConstantPool constantPool = new ConstantPool();

    /* access flag bit mask - see VMConstants */
    private int accessFlags = 0;

    /* The name of the class */
    private ConstClass thisClassName;

    /* The name of the super class */
    private ConstClass superClassName;

    /* A list of the interfaces which the class implements
     * The contents are ConstClass objects
     */
    private Vector classInterfaces = new Vector();

    /* A list of the fields which the class contains
     * The contents are ClassField objects
     */
    private Vector classFields = new Vector();

    /* A list of the methods which the class defines
     * The contents are ClassMethod objects
     */
    private Vector classMethods = new Vector();

    /* A list of the attributes associated with the class */
    private AttributeVector classAttributes = new AttributeVector();

    /** Static methods 
     * Added for major.minor compatibility checking
     */
    private static List convertMajorMinorVersions(short[][] majorMinor) {
        int length = majorMinor.length;
        List result = new ArrayList(length);
        for (int i = 0; i < length; i++) {
            result.add(new Integer(majorMinor[i][0] * 65536 + majorMinor[i][1]));
        }
        return result;
    }
    
    private static boolean isSupportedVersion(short major, short minor) {
        Integer version = new Integer(major*65536 + minor);
        return jdkVersions.contains(version);
    }
    
    public static final String printSupportedVersions() {
        StringBuffer buf = new StringBuffer("{"); //NOI18N
        int length = jdkMajorMinorVersions.length;
        for (int i = 0; i < length; i++) {
            int major = jdkMajorMinorVersions[i][0];
            int minor = jdkMajorMinorVersions[i][1];
            buf.append("{"); buf.append(major); buf.append(","); 
            buf.append(minor); buf.append("}"); //NOI18N
        }
        buf.append("}"); //NOI18N
        return buf.toString();
    }

    /* public accessors */



    /**
     * Return the constant pool for the class file
     */
    public ConstantPool pool() {
        return constantPool;
    }

    /**
     * Return the access flags for the class - see VMConstants
     */
    public int access() {
        return accessFlags;
    }

    /**
     * Is the class final?
     */
    final public boolean isFinal() {
        return (accessFlags & ACCFinal) != 0;
    }

    /**
     * Is the class an interface?
     */
    final public boolean isInterface() {
        return (accessFlags & ACCInterface) != 0;
    }

    /**
     * Is the class public?
     */
    final public boolean isPublic() {
        return (accessFlags & ACCPublic) != 0;
    }

    /**
     * Is the class abstract?
     */
    final public boolean isAbstract() {
        return (accessFlags & ACCAbstract) != 0;
    }


    /**
     * Set the access flags for the class - see VMConstants
     */
    public void setAccessFlags (int flags) {
        accessFlags = flags;
    }

    /**
     * Return the name of the class
     */
    public ConstClass className() {
        return thisClassName;
    }

    /**
     * Return the name of the class as a string
     */
    //@olsen: added method
    public String classNameString() {
        return (thisClassName == null) ? null : thisClassName.asString();
    }

    /**
     * Return the name of the super class
     */
    public ConstClass superName() {
        return superClassName;
    }

    /**
     * Return the name of the super class as a string
     */
    public String superNameString() {
        return (superClassName == null) ? null : superClassName.asString();
    }

    /**
     * Set the name of the super class
     */
    public void setSuperName(ConstClass superCl) {
        superClassName = superCl;
    }

    /**
     * Return the list of the interfaces which the class implements
     * The contents are ConstClass objects
     */
    public Vector interfaces() {
        return classInterfaces;
    }

    /**
     * Add an interface to the list of the interfaces which the class implements
     */
    public void addInterface (ConstClass iface) {
        classInterfaces.addElement(iface);
    }

    /**
     * Return the list of the fields which the class contains
     * The contents are ClassField objects
     */
    public Vector fields() {
        return classFields;
    }

    /**
     * Add a field to the list of the fields which the class contains
     */
    public void addField (ClassField field) {
        classFields.addElement(field);
    }

    /**
     * Add a field to the list of the fields which the class contains,
     * at the index'th position.
     */
    public void addField(ClassField field, int index) {
        classFields.insertElementAt(field, index);
    }

    /**
     * Return the list of the methods which the class defines
     * The contents are ClassMethod objects
     */
    public Vector methods() {
        return classMethods;
    }

    /**
     * Look for a method with the specified name and type signature
     */
    public ClassMethod findMethod(String methodName, String methodSig) {
        for (Enumeration e = methods().elements(); e.hasMoreElements();) {
            ClassMethod method = (ClassMethod) e.nextElement();
            if (method.name().asString().equals(methodName) &&
                method.signature().asString().equals(methodSig))
                return method;
        }
        return null;
    }

    /**
     * Add a method to the list of the methods which the class defines
     */
    public void addMethod(ClassMethod method) {
        classMethods.addElement(method);
    }

    /**
     * Look for a field with the specified name
     */
    public ClassField findField(String fieldName) {
        for (Enumeration e = fields().elements(); e.hasMoreElements();) {
            ClassField field = (ClassField) e.nextElement();
            if (field.name().asString().equals(fieldName))
                return field;
        }
        return null;
    }

    /**
     * Return the list of the attributes associated with the class
     */
    public AttributeVector attributes() {
        return classAttributes;
    }

    /**
     * Returns the class name in user ('.' delimited) form.
     */
    //@olsen: moved from ClassControl to ClassFile
    public String userClassName()
    {
        return userClassFromVMClass(classNameString());
    }
  
    /**
     * Returns the class name in user ('.' delimited) form.
     */
    //@olsen: moved from ClassControl to ClassFile
    static public String userClassFromVMClass(String vmName)
    {
        return vmName.replace('/', '.');
    }
  
    /**
     * Returns the class name in VM ('/' delimited) form.
     */
    //@olsen: moved from ClassControl to ClassFile
    static public String vmClassFromUserClass(String userName)
    {
        return userName.replace('.', '/');
    }
  
    /**
     * Returns the vm package name for this class.
     */
    //@olsen: moved from ClassControl to ClassFile
    public String pkg()
    {
        return packageOf(classNameString());
    }
  
    /**
     * Returns the vm package name for the vm class name.
     */
    //@olsen: moved from ClassControl to ClassFile
    static public String packageOf(String vmName)
    {
        int last = vmName.lastIndexOf('/');
        if (last < 0)
            return "";
        return vmName.substring(0, last);
    }


    /* Constructors */

    /**
     * Construct a ClassFile from an input stream
     */
    public ClassFile(DataInputStream data) throws ClassFormatError {
        this(data, true);
    }

    public ClassFile(DataInputStream data,
                     boolean allowJDK12ClassFiles) throws ClassFormatError {
        try {
            int thisMagic = data.readInt();
            if (thisMagic != magic)
                throw new ClassFormatError("Bad magic value for input");

            short thisMinorVersion = data.readShort();
            short thisMajorVersion = data.readShort();
            /*@craig changed checking only target 1.1 and 1.2 to more
             * general check for a list of versions.
             */
             if (isSupportedVersion(thisMajorVersion, thisMinorVersion)) {
                minorVersion = thisMinorVersion;
                majorVersion = thisMajorVersion;
            } else {
                throw new ClassFormatError("Bad version number: {" +
                                           thisMajorVersion + "," + 
                                           thisMinorVersion +
                                           "} expected one of: " +
                                           supportedVersions);
            }

            readConstants(data);
            accessFlags = data.readUnsignedShort();
            thisClassName = (ConstClass)
                constantPool.constantAt(data.readUnsignedShort());
            superClassName = (ConstClass)
                constantPool.constantAt(data.readUnsignedShort());
            readInterfaces(data);
            readFields(data);
            readMethods(data);
            classAttributes = AttributeVector.readAttributes(data, constantPool);
        } catch (IOException e) {
            throw new ClassFormatError("IOException during reading: " + 
                                       e.getMessage());
        }
        //@olsen: added println() for debugging
        //System.out.println("ClassFile(): new class = " + 
        //thisClassName.asString());
    }

    /**
     * Construct a bare bones class, ready for additions
     */
    public ClassFile(String cname, String supername) {
        thisClassName = constantPool.addClass(cname);
        superClassName = constantPool.addClass(supername);
        //@olsen: added println() for debugging
        //System.out.println("ClassFile(): new bare class file = " + 
        //thisClassName);
    }

    /**
     * Write the Class file to the data output stream
     */
    public
    void write (DataOutputStream buff) throws IOException {
        buff.writeInt(magic);
        buff.writeShort(minorVersion);
        buff.writeShort(majorVersion);
        constantPool.write(buff);
        buff.writeShort(accessFlags);
        buff.writeShort(thisClassName.getIndex());
        //@lars: superclass may be null (java.lang.Object); 
        //VMSpec 2nd ed., section 4.1
        buff.writeShort(superClassName == null ? 0 : superClassName.getIndex());
        //buff.writeShort(superClassName.getIndex());
        writeInterfaces(buff);
        writeFields(buff);
        writeMethods(buff);
        classAttributes.write(buff);
    }

    /**
     * Returns a byte array representation of this class.
     */
    public byte[] getBytes() throws java.io.IOException {
        /* Write the class bytes to a file, for debugging. */

        String writeClassToDirectory =
            System.getProperty("filter.writeClassToDirectory");
        if (writeClassToDirectory != null) {
            String filename = writeClassToDirectory + java.io.File.separator +
                thisClassName.asString() + ".class";
            System.err.println("Writing class to file " + filename);
            DataOutputStream stream = new DataOutputStream(
                new java.io.FileOutputStream(filename));
            write(stream);
            stream.close();
        }

        /* Get the class bytes and return them. */

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        write(new DataOutputStream(byteStream));

        return byteStream.toByteArray();
    }

    //@olsen: added method
    public void print(PrintStream out) {
        print(out, 0);
    }
    
    //@olsen: added 'indent' parameter
    public void print(PrintStream out, int indent) {
        constantPool.print(out, indent);
        out.println();

        ClassPrint.spaces(out, indent);
        out.println("majorVersion = " + Integer.toString(majorVersion));
        ClassPrint.spaces(out, indent);
        out.println("minorVersion = " + Integer.toString(minorVersion));
        ClassPrint.spaces(out, indent);
        out.println("accessFlags = " + Integer.toString(accessFlags));
        ClassPrint.spaces(out, indent);
        out.println("className = " + thisClassName.asString());
        ClassPrint.spaces(out, indent);
        out.println("superClassName = " + superClassName.asString());
        ClassPrint.spaces(out, indent);
        out.print("Interfaces =");
        for (int i=0; i<classInterfaces.size(); i++) {
            out.print(" "
                      + ((ConstClass)classInterfaces.elementAt(i)).asString());
        }
        out.println();

        ClassPrint.spaces(out, indent);
        out.println("fields =");
        for (int i=0; i<classFields.size(); i++) {
            ((ClassField) classFields.elementAt(i)).print(out, indent + 3);
        }

        ClassPrint.spaces(out, indent);
        out.println("methods =");
        for (int i=0; i<classMethods.size(); i++) {
            ((ClassMethod) classMethods.elementAt(i)).print(out, indent + 3);
        }

        ClassPrint.spaces(out, indent);
        out.println("attributes =");
        classAttributes.print(out, indent + 3);

    }

    //@olsen: made public
    //@olsen: added 'out' and 'indent' parameters
    public void summarize(PrintStream out, int indent) {
        constantPool.summarize(out, indent);
        int codeSize = 0;
        for (int i=0; i<classMethods.size(); i++) {
            codeSize += ((ClassMethod)classMethods.elementAt(i)).codeSize();
        }
        ClassPrint.spaces(out, indent);
        out.println(classMethods.size() + " methods in "
                    + codeSize + " bytes");
        ClassPrint.spaces(out, indent);
        out.println(classFields.size() + " fields");
    }

    /* package local methods */

    /*
     * class file reading helpers
     */
    private void readConstants (DataInputStream data) throws IOException {
        constantPool = new ConstantPool(data);
    }

    private void readInterfaces(DataInputStream data) throws IOException {
        int nInterfaces = data.readUnsignedShort();
        while (nInterfaces-- > 0) {
            int interfaceIndex = data.readUnsignedShort();
            ConstClass ci = null;
            if (interfaceIndex != 0)
                ci = (ConstClass) constantPool.constantAt(interfaceIndex);
            classInterfaces.addElement(ci);
        }
    }

    private void writeInterfaces(DataOutputStream data) throws IOException {
        data.writeShort(classInterfaces.size());
        for (int i=0; i<classInterfaces.size(); i++) {
            ConstClass ci = (ConstClass) classInterfaces.elementAt(i);
            int interfaceIndex = 0;
            if (ci != null)
                interfaceIndex = ci.getIndex();
            data.writeShort(interfaceIndex);
        }
    }

    private void readFields(DataInputStream data) throws IOException {
        int nFields = data.readUnsignedShort();
        while (nFields-- > 0) {
            classFields.addElement (ClassField.read(data, constantPool));
        }
    }

    private void writeFields (DataOutputStream data) throws IOException {
        data.writeShort(classFields.size());
        for (int i=0; i<classFields.size(); i++)
            ((ClassField)classFields.elementAt(i)).write(data);
    }

    private void readMethods (DataInputStream data) throws IOException {
        int nMethods = data.readUnsignedShort();
        while (nMethods-- > 0) {
            classMethods.addElement (ClassMethod.read(data, constantPool));
        }
    }

    private void writeMethods (DataOutputStream data) throws IOException {
        data.writeShort(classMethods.size());
        for (int i=0; i<classMethods.size(); i++)
            ((ClassMethod)classMethods.elementAt(i)).write(data);
    }

}

abstract class ArraySorter {
    protected ArraySorter() {}

    /* return the size of the array being sorted */
    abstract int size();

    /* return -1 if o1 < o2, 0 if o1 == o2, 1 if o1 > o2 */
    abstract int compare(int o1Index, int o2Index);

    /* Swap the elements at index o1Index and o2Index */
    abstract void swap(int o1Index, int o2Index);

    void sortArray() {
        sortArray(0, size()-1);
    }

    private void sortArray(int start, int end) {
        if (end > start) {
            swap(start, (start+end)/2);
            int last = start;
            for (int i = start+1; i<=end; i++) {
                if (compare(i, start) < 0)
                    swap (++last, i);
            }
            swap(start, last);
            sortArray(start, last-1);
            sortArray(last+1, end);
        }
    }
}

class InterfaceArraySorter extends ArraySorter {
    private ConstClass theArray[];

    InterfaceArraySorter(ConstClass[] interfaces) {
        theArray = interfaces;
    }

    /* return the size of the array being sorted */
    int size() { return theArray.length; }

    /* return -1 if o1 < o2, 0 if o1 == o2, 1 if o1 > o2 */
    int compare(int o1Index, int o2Index) {
        return theArray[o1Index].asString().compareTo(
            theArray[o2Index].asString());
    }

    /* Swap the elements at index o1Index and o2Index */
    void swap(int o1Index, int o2Index) {
        ConstClass tmp = theArray[o1Index];
        theArray[o1Index] = theArray[o2Index];
        theArray[o2Index] = tmp;
    }
}

class FieldArraySorter extends ArraySorter {
    private ClassField theArray[];

    FieldArraySorter(ClassField[] fields) {
        theArray = fields;
    }

    /* return the size of the array being sorted */
    int size() { return theArray.length; }

    /* return -1 if o1 < o2, 0 if o1 == o2, 1 if o1 > o2 */
    int compare(int o1Index, int o2Index) {
        return theArray[o1Index].name().asString().compareTo(
            theArray[o2Index].name().asString());
    }

    /* Swap the elements at index o1Index and o2Index */
    void swap(int o1Index, int o2Index) {
        ClassField tmp = theArray[o1Index];
        theArray[o1Index] = theArray[o2Index];
        theArray[o2Index] = tmp;
    }
}

class MethodArraySorter extends ArraySorter {
    private ClassMethod theArray[];

    MethodArraySorter(ClassMethod[] methods) {
        theArray = methods;
    }

    /* return the size of the array being sorted */
    int size() { return theArray.length; }

    /* return -1 if o1 < o2, 0 if o1 == o2, 1 if o1 > o2 */
    int compare(int o1Index, int o2Index) {
        int cmp = theArray[o1Index].name().asString().compareTo(
            theArray[o2Index].name().asString());
        if (cmp == 0) {
            cmp = theArray[o1Index].signature().asString().compareTo(
                theArray[o2Index].signature().asString());
        }
        return cmp;
    }

    /* Swap the elements at index o1Index and o2Index */
    void swap(int o1Index, int o2Index) {
        ClassMethod tmp = theArray[o1Index];
        theArray[o1Index] = theArray[o2Index];
        theArray[o2Index] = tmp;
    }
}
