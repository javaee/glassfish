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
 * ClassField models the static and non-static fields of a class within
 * a class file.
 */

final public class ClassField extends ClassMember {
  /* access flag bit mask - see VMConstants */
  private int accessFlags;

  /* The name of the field */
  private ConstUtf8 fieldName;

  /* The type signature of the field */
  private ConstUtf8 fieldSignature;

  /* The attributes associated with the field */
  private AttributeVector fieldAttributes;
  

  /* public accessors */

  /**
   * Is the field transient?
   */
  public boolean isTransient() {
    return (accessFlags & ACCTransient) != 0;
  }

  /**
   * Return the access flags for the field - see VMConstants
   */
  public int access() {
    return accessFlags;
  }

  /**
   * Update the access flags for the field - see VMConstants
   */
  public void setAccess(int newFlags) {
    accessFlags = newFlags;
  }

  /**
   * Return the name of the field
   */
  public ConstUtf8 name() {
    return fieldName;
  }

  /**
   * Change the name of the field
   */
  public void changeName(ConstUtf8 name) {
    fieldName = name;
  }

  /**
   * Return the type signature of the field
   */
  public ConstUtf8 signature() {
    return fieldSignature;
  }

  /**
   * Change the type signature of the field
   */
  public void changeSignature(ConstUtf8 newSig) {
    fieldSignature = newSig;
  }

  /**
   * Return the attributes associated with the field
   */
  public AttributeVector attributes() {
    return fieldAttributes;
  }

  /**
   * Construct a class field object
   */
  public ClassField(int accFlags, ConstUtf8 name, ConstUtf8 sig,
                    AttributeVector field_attrs) {
    accessFlags = accFlags;
    fieldName = name;
    fieldSignature = sig;
    fieldAttributes = field_attrs;
  }

  /* package local methods */

  static ClassField read(DataInputStream data, ConstantPool pool) 
    throws IOException {
    ClassField f = null;
    int accessFlags = data.readUnsignedShort();
    int name_index = data.readUnsignedShort();
    int sig_index = data.readUnsignedShort();
    AttributeVector fieldAttribs = AttributeVector.readAttributes(data, pool);
    f = new ClassField(accessFlags, 
		       (ConstUtf8) pool.constantAt(name_index),
		       (ConstUtf8) pool.constantAt(sig_index),
		       fieldAttribs);
    return f;
  }

  void write (DataOutputStream data) throws IOException {
    data.writeShort(accessFlags);
    data.writeShort(fieldName.getIndex());
    data.writeShort(fieldSignature.getIndex());
    fieldAttributes.write(data);
  }

  void print(PrintStream out, int indent) {
    ClassPrint.spaces(out, indent);
    out.print("'" + fieldName.asString() + "'");
    out.print(" sig = " + fieldSignature.asString());
    out.print(" access_flags = " + Integer.toString(accessFlags));
    out.println(" attributes:");
    fieldAttributes.print(out, indent+2);
  }
}

