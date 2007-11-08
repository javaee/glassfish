/***
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

package oracle.toplink.libraries.asm.util;

import oracle.toplink.libraries.asm.Attribute;
import oracle.toplink.libraries.asm.ClassVisitor;
import oracle.toplink.libraries.asm.util.attrs.ASMLocalVariableTypeTableAttribute;
import oracle.toplink.libraries.asm.util.attrs.ASMAnnotationDefaultAttribute;
import oracle.toplink.libraries.asm.util.attrs.ASMEnclosingMethodAttribute;
import oracle.toplink.libraries.asm.util.attrs.ASMRuntimeInvisibleAnnotations;
import oracle.toplink.libraries.asm.util.attrs.ASMRuntimeInvisibleParameterAnnotations;
import oracle.toplink.libraries.asm.util.attrs.ASMRuntimeVisibleAnnotations;
import oracle.toplink.libraries.asm.util.attrs.ASMRuntimeVisibleParameterAnnotations;
import oracle.toplink.libraries.asm.util.attrs.ASMSignatureAttribute;
import oracle.toplink.libraries.asm.util.attrs.ASMSourceDebugExtensionAttribute;
import oracle.toplink.libraries.asm.util.attrs.ASMStackMapAttribute;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class visitor that prints the classes it visits.
 * 
 * @author Eric Bruneton, Eugene Kuleshov
 */

public abstract class PrintClassVisitor implements ClassVisitor {

  /**
   * The text to be printed. Since the code of methods is not necessarily
   * visited in sequential order, one method after the other, but can be
   * interlaced (some instructions from method one, then some instructions from
   * method two, then some instructions from method one again...), it is not
   * possible to print the visited instructions directly to a sequential
   * stream. A class is therefore printed in a two steps process: a string tree
   * is constructed during the visit, and printed to a sequential stream at the
   * end of the visit. This string tree is stored in this field, as a string
   * list that can contain other string lists, which can themselves contain
   * other string lists, and so on.
   */

  protected final List text;

  /**
   * A buffer that can be used to create strings.
   */

  protected final StringBuffer buf;

  /**
   * The print writer to be used to print the class.
   */

  protected final PrintWriter pw;

  /**
   * Constructs a new {@link PrintClassVisitor PrintClassVisitor} object.
   *
   * @param pw the print writer to be used to print the class.
   */

  protected PrintClassVisitor (final PrintWriter pw) {
    this.text = new ArrayList();
    this.buf = new StringBuffer();
    this.pw = pw;
  }

  public void visitEnd () {
    printList(text);
    pw.flush();
  }

  /**
   * Prints the given string tree to {@link #pw pw}.
   *
   * @param l a string tree, i.e., a string list that can contain other string
   *      lists, and so on recursively.
   */

  private void printList (final List l) {
    for (int i = 0; i < l.size(); ++i) {
      Object o = l.get(i);
      if (o instanceof List) {
        printList((List)o);
      } else {
        pw.print(o.toString());
      }
    }
  }

  public static Attribute[] getDefaultAttributes () {
    try {
      return new Attribute[] {
        new ASMAnnotationDefaultAttribute(),
        new ASMRuntimeInvisibleAnnotations(),
        new ASMRuntimeInvisibleParameterAnnotations(),
        new ASMRuntimeVisibleAnnotations(),
        new ASMRuntimeVisibleParameterAnnotations(),
        new ASMStackMapAttribute(),
        new ASMSourceDebugExtensionAttribute(),
        new ASMSignatureAttribute(),
        new ASMEnclosingMethodAttribute(),
        new ASMLocalVariableTypeTableAttribute()
      };
    } catch (Exception e) {
      return new Attribute[0];
    }
  }
}
