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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oracle.toplink.libraries.asm.ByteVector;
import oracle.toplink.libraries.asm.ClassReader;
import oracle.toplink.libraries.asm.ClassWriter;
import oracle.toplink.libraries.asm.Label;


/**
 * StackMapFrame is used by {@link StackMapAttribute} to hold state of the stack
 * and local variables for a single execution branch.
 *
 * <i>Note that Long and Double types are represented by two entries in locals
 * and stack. Second entry sohould be always of type Top.</i>
 *
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=139">JSR 139 : Connected
 * Limited Device Configuration 1.1</a>
 *
 * @author Eugene Kuleshov
 */

public class StackMapFrame {

  public Label label;

  public List locals = new ArrayList();

  public List stack = new ArrayList();

  public int read (ClassReader cr,
                   int off, char[] buf, int codeOff, Label[] labels) {
    int n = cr.readUnsignedShort(off);
    off += 2;
    if (labels[n] == null) {
      labels[n] = new Label();
    }
    label = labels[n];
    off = readTypeInfo(cr, off, locals, labels, buf,
                       cr.readUnsignedShort(codeOff + 2));  //  maxLocals
    off = readTypeInfo(cr, off, stack, labels, buf,
                       cr.readUnsignedShort(codeOff));  // maxStack
    return off;
  }

  public void write (ClassWriter cw,
                     int maxStack, int maxLocals, ByteVector bv) {
    bv.putShort(label.getOffset());
    writeTypeInfo(bv, cw, locals, maxLocals);
    writeTypeInfo(bv, cw, stack, maxStack);
  }

  public void getLabels (Set labels) {
    labels.add(label);
    getTypeInfoLabels(labels, locals);
    getTypeInfoLabels(labels, stack);
  }

  private void getTypeInfoLabels (Set labels, List info) {
    for (Iterator it = info.iterator(); it.hasNext();) {
      StackMapType typeInfo = (StackMapType)it.next();
      if (typeInfo.getType() == StackMapType.ITEM_Uninitialized) {
        labels.add(typeInfo.getLabel());
      }
    }
  }

  private int readTypeInfo (ClassReader cr, int off,
                            List info, Label[] labels, char[] buf, int max) {
    int n = 0;
    if (max > StackMapAttribute.MAX_SIZE) {
      n = cr.readInt(off);
      off += 4;
    } else {
      n = cr.readUnsignedShort(off);
      off += 2;
    }
    for (int j = 0; j < n; j++) {
      int itemType = cr.readByte(off++);
      StackMapType typeInfo = StackMapType.getTypeInfo(itemType);
      info.add(typeInfo);
      switch (itemType) {
        // case StackMapType.ITEM_Long:  //
        // case StackMapType.ITEM_Double:  //
          // info.add(StackMapType.getTypeInfo(StackMapType.ITEM_Top));
        //   break;

        case StackMapType.ITEM_Object:  //
          typeInfo.setObject(cr.readClass(off, buf));
          off += 2;
          break;

        case StackMapType.ITEM_Uninitialized:  //
          int o = cr.readUnsignedShort(off);
          off += 2;
          if (labels[o] == null) {
            labels[o] = new Label();
          }
          typeInfo.setLabel(labels[o]);
          break;
      }
    }
    return off;
  }

  private void writeTypeInfo (ByteVector bv,
                              ClassWriter cw, List info, int max) {
    if (max > StackMapAttribute.MAX_SIZE) {
      bv.putInt(info.size());
    } else {
      bv.putShort(info.size());
    }
    for (int j = 0; j < info.size(); j++) {
      StackMapType typeInfo = (StackMapType)info.get(j);
      bv.putByte(typeInfo.getType());
      switch (typeInfo.getType()) {
        case StackMapType.ITEM_Object:  //
          bv.putShort(cw.newClass(typeInfo.getObject()));
          break;

        case StackMapType.ITEM_Uninitialized:  //
          bv.putShort(typeInfo.getLabel().getOffset());
          break;
          
      }
    }
  }

  public String toString () {
    StringBuffer sb = new StringBuffer("Frame:L");
    sb.append(System.identityHashCode(label));
    sb.append(" locals").append(locals);
    sb.append(" stack").append(stack);
    return sb.toString();
  }
}
