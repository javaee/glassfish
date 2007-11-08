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

package oracle.toplink.libraries.asm.util.attrs;

import java.util.Map;

import oracle.toplink.libraries.asm.Attribute;
import oracle.toplink.libraries.asm.ClassReader;
import oracle.toplink.libraries.asm.Label;
import oracle.toplink.libraries.asm.attrs.LocalVariableType;
import oracle.toplink.libraries.asm.attrs.LocalVariableTypeTableAttribute;

/**
 * An {@link ASMifiable} {@link LocalVariableTypeTableAttribute} sub class.
 *
 * @author Eugene Kuleshov
 */

public class ASMLocalVariableTypeTableAttribute
  extends LocalVariableTypeTableAttribute 
  implements ASMifiable 
{

  protected Attribute read( ClassReader cr, int off, int len, char[] buf, int codeOff, Label[] labels) {
    LocalVariableTypeTableAttribute attr = ( LocalVariableTypeTableAttribute) super.read(cr, off, len, buf, codeOff, labels);

    ASMLocalVariableTypeTableAttribute res = new ASMLocalVariableTypeTableAttribute();
    res.getTypes().addAll( attr.getTypes());
    return res;
  }

  public void asmify( StringBuffer buf, String varName, Map labelNames) {
    buf.append("LocalVariableTypeTableAttribute ").append(varName)
      .append(" = new LocalVariableTypeTableAttribute();\n");

    buf.append("{\n");
    buf.append("LocalVariableTypeTableAttribute ").append(varName).append("Attr");
    buf.append(" = new LocalVariableTypeTableAttribute();\n");
    if (types.size() > 0) {
      for (int i = 0; i < types.size(); i++) {
        asmify((LocalVariableType)types.get(i), buf, varName + "type" + i, labelNames);
      }
    }
    buf.append(varName).append(".visitAttribute(").append(varName);
    buf.append("Attr);\n}\n");
  }

  private void asmify( LocalVariableType type, StringBuffer buf, String varName, Map labelNames) {
    declareLabel(buf, labelNames, type.start);
    declareLabel(buf, labelNames, type.end);
    buf.append("{\n");
    
    buf.append("LocalVariableType ").append(varName).append( " = new LocalVariableType();\n");
    
    buf.append(varName).append(".start = ").append( labelNames.get( type.start)).append(";\n");
    buf.append(varName).append(".end = ").append( labelNames.get( type.end)).append(";\n");
    buf.append(varName).append(".name = \"").append( type.name).append( "\";\n");
    buf.append(varName).append(".signature = ").append( type.signature).append( "\";\n");
    buf.append(varName).append(".index = ").append( type.index).append( "\";\n");
    
    buf.append( "cvAttr.types.add(").append(varName).append(");\n");
    buf.append("}\n");
  }

  static void declareLabel (StringBuffer buf, Map labelNames, Label l) {
    String name = (String)labelNames.get(l);
    if (name == null) {
      name = "l" + labelNames.size();
      labelNames.put(l, name);
      buf.append("Label ").append(name).append(" = new Label();\n");
    }
  }
  
}

