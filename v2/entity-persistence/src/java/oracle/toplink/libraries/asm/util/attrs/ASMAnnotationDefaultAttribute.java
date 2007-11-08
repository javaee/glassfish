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

import java.util.List;
import java.util.Map;

import oracle.toplink.libraries.asm.Attribute;
import oracle.toplink.libraries.asm.ClassReader;
import oracle.toplink.libraries.asm.Label;
import oracle.toplink.libraries.asm.Type;
import oracle.toplink.libraries.asm.attrs.Annotation;
import oracle.toplink.libraries.asm.attrs.AnnotationDefaultAttribute;
import oracle.toplink.libraries.asm.attrs.Annotation.EnumConstValue;

/**
 * An {@link ASMifiable} {@link AnnotationDefaultAttribute} sub class.
 *
 * @author Eugene Kuleshov
 */

public class ASMAnnotationDefaultAttribute extends AnnotationDefaultAttribute 
  implements ASMifiable  
{

  protected Attribute read (ClassReader cr, int off,
    int len, char[] buf, int codeOff, Label[] labels) 
  {
    AnnotationDefaultAttribute attr = 
      (AnnotationDefaultAttribute)super.read(
        cr, off, len, buf, codeOff, labels);
    
    ASMAnnotationDefaultAttribute result = new ASMAnnotationDefaultAttribute();
    result.defaultValue = attr.defaultValue;
    return result;
  }
   
  public void asmify (StringBuffer buf, String varName, Map labelNames) {
    buf.append("AnnotationDefaultAttribute ").append(varName)
      .append(" = new AnnotationDefaultAttribute();\n");
    String val = asmify(defaultValue, buf, varName + "Val"); 
    buf.append(varName).append(".defaultValue = ")
      .append(val).append(";\n");
  }
  
  static void asmifyAnnotations (StringBuffer buf, String varName, List annotations) {
    if (annotations.size() > 0) {
      buf.append("{\n");
      for (int i = 0; i < annotations.size(); i++) {
        String val = varName + "ann" + i;
        asmify((Annotation)annotations.get(i), buf, val);
        buf.append(varName).append(".annotations.add( ").append(val).append(");\n");
      }
      buf.append("}\n");
    }
  }

  static void asmifyParameterAnnotations (StringBuffer buf, String varName, List parameters) {
    if (parameters.size() > 0) {
      buf.append("{\n");
      for (int i = 0; i < parameters.size(); i++) {
        String val = varName + "param" + i;
        buf.append( "List ").append( val).append( " = new ArrayList();\n");
        List annotations = (List)parameters.get(i);
        if (annotations.size() > 0) {
          buf.append("{\n");
          for (int i1 = 0; i1 < annotations.size(); i1++) {
            String val1 = val + "ann" + i1;
            asmify((Annotation)annotations.get(i1), buf, val1);
            buf.append(val).append(".add( ").append(val1).append(");\n");
          }
          buf.append("}\n");
        }
        buf.append(varName).append(".parameters.add( ").append(val).append(");\n\n");
      }
      buf.append("}\n");
    }
  }

  static String asmify (Annotation a, StringBuffer buf, String varName) {
    buf.append("Annotation ").append(varName)
    	.append(" = new Annotation(\"").append(a.type).append("\");\n");
    List elementValues = a.elementValues;
    if (elementValues.size() > 0) {
      // buf.append("{\n");
      for (int i = 0; i < elementValues.size(); i++) {
        Object[] values = (Object[])elementValues.get(i);
        String val = asmify(values[1], buf, varName + "val" + i);
        buf.append(varName).append(".add( \"")
          .append(values[0]).append("\", ").append(val).append(");\n");
      }
      // buf.append("}\n");
    }
    return varName;
  }

  static String asmify (Object value, StringBuffer buf, String valName) {
    if (value instanceof String) {
      return "\""+value+"\"";

    } else if (value instanceof Integer) {
      return "new Integer("+value+")"; 
      	
    } else if (value instanceof Byte) {
      return "new Byte((byte)"+value+")"; 

    } else if (value instanceof Character) {
      return "new Character((char)"+((int)((Character)value).charValue())+")"; 

    } else if (value instanceof Double) {
      return "new Double(\""+value+"\")"; 

    } else if (value instanceof Float) {
      return "new Float(\""+value+"\")"; 

    } else if (value instanceof Long) {
      return "new Long("+value+"L)"; 

    } else if (value instanceof Short) {
      return "new Short((short)"+value+")"; 

    } else if (value instanceof Boolean) {
      return "new Boolean("+value+")"; 

    } else if (value instanceof EnumConstValue) {
      EnumConstValue e = (EnumConstValue) value;
      return "new Annotation.EnumConstValue(\""+e.typeName+"\", \""+e.constName+"\")";

    } else if (value instanceof Type) {
      Type t = (Type)value;
      return "Type.getType(\""+t.getDescriptor()+"\")";

    } else if (value instanceof Annotation) {
      return asmify((Annotation)value, buf, valName);

    } else if (value instanceof Object[]) {
      Object[] v = (Object[])value;
      buf.append("Object[] ").append(valName)
        .append(" = new Object[").append(v.length).append("];\n");
      for (int i = 0; i < v.length; i++) {
        String val = asmify(v[i], buf, valName+"Arr"+i);
        buf.append(valName+"["+i+"] = ").append(val).append(";\n");
      }
      return valName;
    
    } else if( value instanceof byte[]) {
      byte[] v = (byte[])value;
      StringBuffer sb = new StringBuffer( "new byte[] {");
	  String sep = "";
      for (int i = 0; i < v.length; i++) {
	    sb.append(sep).append(v[i]);
	    sep = ", ";
	  }
	  sb.append("}");
      return sb.toString();
      
    } else if( value instanceof char[]) {
      char[] v = (char[])value;
      StringBuffer sb = new StringBuffer( "new char[] {");
	  String sep = "";
      for (int i = 0; i < v.length; i++) {
	    sb.append(sep).append("(char)").append((int)v[i]);
	    sep = ", ";
	  }
	  sb.append("}");
      return sb.toString();
      
    } else if( value instanceof short[]) {
      short[] v = (short[])value;
      StringBuffer sb = new StringBuffer( "new short[] {");
	  String sep = "";
      for (int i = 0; i < v.length; i++) {
	    sb.append(sep).append("(short)").append(v[i]);
	    sep = ", ";
	  }
	  sb.append("}");
      return sb.toString();
      
    } else if( value instanceof long[]) {
      long[] v = (long[])value;
      StringBuffer sb = new StringBuffer( "new long[] {");
	  String sep = "";
      for (int i = 0; i < v.length; i++) {
	    sb.append(sep).append(v[i]).append("L");
	    sep = ", ";
	  }
	  sb.append("}");
      return sb.toString();
      
    } else if( value instanceof int[]) {
      int[] v = (int[])value;
      StringBuffer sb = new StringBuffer( "new int[] {");
	  String sep = "";
      for (int i = 0; i < v.length; i++) {
	    sb.append(sep).append(v[i]);
	    sep = ", ";
	  }
	  sb.append("}");
      return sb.toString();
      
    } else if( value instanceof boolean[]) {
      boolean[] v = (boolean[])value;
      StringBuffer sb = new StringBuffer( "new boolean[] {");
	  String sep = "";
      for (int i = 0; i < v.length; i++) {
	    sb.append(sep).append(v[i]);
	    sep = ", ";
	  }
	  sb.append("}");
      return sb.toString();
      
    } else if( value instanceof float[]) {
      float[] v = (float[])value;
      StringBuffer sb = new StringBuffer( "new float[] {");
	  String sep = "";
      for (int i = 0; i < v.length; i++) {
	    sb.append(sep).append(v[i]).append("f");
	    sep = ", ";
	  }
	  sb.append("}");
      return sb.toString();
      
    } else if( value instanceof double[]) {
      double[] v = (double[])value;
      StringBuffer sb = new StringBuffer( "new double[] {");
	  String sep = "";
      for (int i = 0; i < v.length; i++) {
	    sb.append(sep).append(v[i]).append("d");
	    sep = ", ";
	  }
	  sb.append("}");
      return sb.toString();
      
    } else {
      throw new IllegalArgumentException( "Invalid value: "+value.getClass().getName()+" : "+value);
      
    }
    // return null;
  }
}
