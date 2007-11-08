/**
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package oracle.toplink.libraries.asm.attrs;

import oracle.toplink.libraries.asm.Attribute;
import oracle.toplink.libraries.asm.ByteVector;
import oracle.toplink.libraries.asm.ClassReader;
import oracle.toplink.libraries.asm.ClassWriter;
import oracle.toplink.libraries.asm.Label;


/**
 * The EnclosingMethod attribute is an optional fixed-length attribute in 
 * the attributes table of the ClassFile structure. A class must have an 
 * EnclosingMethod attribute if and only if it is a local class or an 
 * anonymous class. A class may have no more than one EnclosingMethod attribute.
 * 
 * The EnclosingMethod attribute has the following format:
 * <pre>
 *   EnclosingMethod_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u2 class_index
 *     u2 method_index;
 *   }
 * </pre>
 * The items of the EnclosingMethod_attribute structure are as follows:
 * <dl>
 * <dt>attribute_name_index</dt>
 * <dd>The value of the attribute_name_index item must be a valid index
 * into the constant_pool table. The constant_pool entry at that index
 * must be a CONSTANT_Utf8_info structure representing the
 * string "EnclosingMethod".</dd>
 * <dt>attribute_length</dt>
 * <dd>The value of the attribute_length item is four.</dd>
 * <dt>class_index</dt>
 * <dd>The value of the class_index item must be a valid index into the
 * constant_pool table. The constant_pool entry at that index must be a
 * CONSTANT_Class_info structure representing the
 * innermost class that encloses the declaration of the current class.</dd>
 * <dt>method_index</dt>
 * <dd>If the current class is not immediately enclosed by a method or
 * constructor, then the value of the method_index item must be zero.
 * Otherwise, the value of the method_index item must be a valid
 * index into the constant_pool table. The constant_pool entry at that
 * index must be a CONSTANT_NameAndType_info structure
 * representing a the name and type of a method in the class
 * referenced by the class_index attribute above. It is the
 * responsibility of the Java compiler to ensure that the method
 * identified via the method_index is indeed the closest lexically
 * enclosing method of the class that contains this EnclosingMethod
 * attribute.</dd>
 * </dl>
 *
 * @author Eugene Kuleshov
 */

public class EnclosingMethodAttribute extends Attribute {
  
  public String owner;
  public String name;
  public String desc;

  public EnclosingMethodAttribute () {
    super("EnclosingMethod");
  }
  
  public EnclosingMethodAttribute (String owner, String name, String desc) {
    this();
    this.owner = owner;
    this.name = name;
    this.desc = desc;
  }

  protected Attribute read (ClassReader cr, int off,
                            int len, char[] buf, int codeOff, Label[] labels) {
    // CONSTANT_Class_info
    String o = cr.readClass( off, buf);
    // CONSTANT_NameAndType_info (skip CONSTANT_NameAndType tag)
    int index = cr.getItem(cr.readUnsignedShort(off + 2));
    String n = null;
    String d = null;
    if( index!=0) {
	    n = cr.readUTF8(index, buf);
	    d = cr.readUTF8(index + 2, buf);
    }
    return new EnclosingMethodAttribute( o, n, d);
  }

  protected ByteVector write (ClassWriter cw, byte[] code,
                              int len, int maxStack, int maxLocals) {
    return new ByteVector().putShort(cw.newClass(owner))
      .putShort( name==null || desc==null ? 0 : cw.newNameType(name, desc));
  }

  public String toString () {
    return new StringBuffer("owner:").append( owner)
      .append(" name:").append(name)
      .append(" desc:").append(desc).toString();
  } 
}

