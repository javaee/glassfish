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
import java.util.List;

import oracle.toplink.libraries.asm.ByteVector;
import oracle.toplink.libraries.asm.ClassReader;
import oracle.toplink.libraries.asm.ClassWriter;
import oracle.toplink.libraries.asm.Type;

/**
 * Annotation data contains an annotated type and its array of the element-value
 * pairs. Structure is in the following format:
 * <pre>
 *   annotation {
 *     u2 type_index;
 *     u2 num_element_value_pairs;
 *     {
 *       u2 element_name_index;
 *       element_value value;
 *     } element_value_pairs[num_element_value_pairs];
 *   }
 * </pre>
 * The items of the annotation structure are as follows:
 * <dl>
 * <dt>type_index</dt>
 * <dd>The value of the type_index item must be a valid index into the constant_pool
 *     table. The constant_pool entry at that index must be a CONSTANT_Utf8_info
 *     structure representing a field descriptor representing the annotation 
 *     interface corresponding to the annotation represented by this annotation 
 *     structure.</dd>
 * <dt>num_element_value_pairs</dt>
 * <dd>The value of the num_element_value_pairs item gives the number of element-value
 *     pairs in the annotation represented by this annotation structure. Note that a
 *     maximum of 65535 element-value pairs may be contained in a single annotation.</dd>
 * <dt>element_value_pairs</dt>
 * <dd>Each value of the element_value_pairs table represents a single element-value
 *     pair in the annotation represented by this annotation structure.
 *     Each element_value_pairs entry contains the following two items:
 *     <dt>element_name_index</dt>
 *     <dd>The value of the element_name_index item must be a valid index into the
 *         constant_pool table. The constant_pool entry at that index must be a
 *         CONSTANT_Utf8_info structure representing the name of the annotation type
 *         element corresponding to this element_value_pairs entry.</dd>
 *     <dt>value</dt>
 *     <dd>The value item represents the value in the element-value pair represented by
 *         this element_value_pairs entry.</dd>
 *     </dl>
 *     </dd>
 * </dl>
 * 
 * The element_value structure is a discriminated union representing the value of a
 * element-value pair. It is used to represent values in all class file attributes
 * that describe annotations ( RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations,
 * RuntimeVisibleParameterAnnotations, and RuntimeInvisibleParameterAnnotations).
 * <p>
 * The element_value structure has the following format:
 * <pre>
 *   element_value {
 *     u1 tag;
 *     union {
 *       u2   const_value_index;
 *       {
 *         u2   type_name_index;
 *         u2   const_name_index;
 *       } enum_const_value;
 *       u2   class_info_index;
 *       annotation annotation_value;
 *       {
 *         u2    num_values;
 *         element_value values[num_values];
 *       } array_value;
 *     } value;
 *   }
 * </pre>
 * The items of the element_value structure are as follows:
 * <dl>
 * <dt>tag</dt>
 * <dd>The tag item indicates the type of this annotation element-value pair. The letters
 *     'B', 'C', 'D', 'F', 'I', 'J', 'S', and 'Z' indicate a primitive type. These
 *     letters are interpreted as BaseType characters (Table 4.2). The other legal
 *     values for tag are listed with their interpretations in this table:
 *     <pre>
 *     tag  value Element Type
 *     's'  String
 *     'e'  enum constant
 *     'c'  class
 *     '@'  annotation type
 *     '['  array
 *   </pre>
 *   </dd>
 * <dt>value</dt>
 * <dd>The value item represents the value of this annotation element. This item is
 *     a union. The tag item, above, determines which item of the union is to be used:
 *   <dl>
 *   <dt>const_value_index</dt>
 *   <dd>The const_value_index item is used if the tag item is one of 'B', 'C', 'D',
 *       'F', 'I', 'J', 'S', 'Z', or 's'. The value of the const_value_index item must
 *       be a valid index into the constant_pool table. The constant_pool entry at
 *       that index must be of the correct entry type for the field type designated by
 *       the tag item, as specified in table 4.6, with one exception: if the tag is
 *       's', the the value of the const_value_index item must be the index of a
 *       CONSTANT_Utf8 structure, rather than a CONSTANT_String.</dd>
 *   <dt>enum_const_value</dt>
 *   <dd>The enum_const_value item is used if the tag item is 'e'. The
 *       enum_const_value item consists of the following two items:
 *     <dl>
 *     <dt>type_name_index</dt>
 *     <dd>The value of the type_name_index item must be a valid index into the
 *         constant_pool table. The constant_pool entry at that index must be a
 *         CONSTANT_Utf8_info structure representing the binary name (JLS 13.1) of the
 *         type of the enum constant represented by this element_value structure.</dd>
 *     <dt>const_name_index</dt>
 *     <dd>The value of the const_name_index item must be a valid index into the
 *         constant_pool table. The constant_pool entry at that index must be a
 *         CONSTANT_Utf8_info structure representing the simple name of the enum
 *         constant represented by this element_value structure.</dd>
 *     </dl>
 *     </dd>
 *   <dt>class_info_index</dt>
 *   <dd>The class_info_index item is used if the tag item is 'c'. The
 *       class_info_index item must be a valid index into the constant_pool table.
 *       The constant_pool entry at that index must be a CONSTANT_Utf8_info 
 *       structure representing the return descriptor of the type that is 
 *       reified by the class represented by this element_value structure.</dd>
 *   <dt>annotation_value</dt>
 *   <dd>The annotation_value item is used if the tag item is '@'. The element_value
 *       structure represents a "nested" {@link oracle.toplink.libraries.asm.attrs.Annotation annotation}.</dd>
 *   <dt>array_value</dt>
 *   <dd>The array_value item is used if the tag item is '['. The array_value item
 *       consists of the following two items:
 *     <dl>
 *     <dt>num_values</dt>
 *     <dd>The value of the num_values item gives the number of elements in the
 *         array-typed value represented by this element_value structure. Note that a
 *         maximum of 65535 elements are permitted in an array-typed element value.</dd>
 *     <dt>values</dt>
 *     <dd>Each element of the values table gives the value of an element of the
 *         array-typed value represented by this {@link AnnotationElementValue element_value structure}.</dd>
 *     </dl>
 *     </dd>
 *   </dl>
 *   </dd>
 * </dl>
 *
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=175">JSR 175 : A Metadata
 * Facility for the Java Programming Language</a>
 *
 * @author Eugene Kuleshov
 */

public class Annotation {

  /**
   * A fully qualified class name in internal form (see {@link Type Type}).
   */
  public String type;
  
  /**
   * <code>List</code> of <code>Object[]{name, value}</code> pairs.
   * Where name is <code>String</code> and value is one of
   * <code>Byte</code>, <code>Character</code>, <code>Double</code>, 
   * <code>Float</code>, <code>Integer</code>, <code>Long</code>, <code>Short</code>, 
   * <code>Boolean</code>, <code>String</code>, 
   * <code>Annotation.EnumConstValue</code>, <code>Type</code>, 
   * <code>Annotation</code> or <code>Object[]</code>.
   */
  public List elementValues = new ArrayList();

  public Annotation() {
  }
  
  public Annotation( String type) {
    this.type = type;
  }

  public void add (String name, Object value) {
    elementValues.add(new Object[]{name, value});
  }

  /**
   * Reads annotation data structures.
   *
   * @param cr the class that contains the attribute to be read.
   * @param off index of the first byte of the data structure.
   * @param buf buffer to be used to call {@link ClassReader#readUTF8 readUTF8},
   *      {@link ClassReader#readClass(int,char[]) readClass} or {@link
   *      ClassReader#readConst readConst}.
   *
   * @return offset position in bytecode after reading annotation
   */

  public int read (ClassReader cr, int off, char[] buf) {
    type = cr.readUTF8(off, buf);
    int numElementValuePairs = cr.readUnsignedShort(off + 2);
    off += 4;
    int[] aoff = new int[] { off};
    for (int i = 0; i < numElementValuePairs; i++) {
      String elementName = cr.readUTF8(aoff[ 0], buf);
      aoff[ 0] += 2;
      elementValues.add(new Object[]{elementName, readValue(cr, aoff, buf)});
    }
    return aoff[ 0];
  }

  /**
   * Writes annotation data structures.
   *
   * @param bv the byte array form to store data structures.
   * @param cw the class to which this attribute must be added. This parameter
   *      can be used to add to the constant pool of this class the items that
   *      corresponds to this attribute.
   */

  public void write (ByteVector bv, ClassWriter cw) {
    bv.putShort(cw.newUTF8(type));
    bv.putShort(elementValues.size());
    for (int i = 0; i < elementValues.size(); i++) {
      Object[] value = (Object[])elementValues.get(i);
      bv.putShort(cw.newUTF8((String)value[0]));
      writeValue(bv, value[1], cw);
    }
  }

  /**
   * Utility method to read List of annotations. Each element of annotations
   * List will have Annotation instance.
   *
   * @param annotations the List to store parameters annotations.
   * @param cr the class that contains the attribute to be read.
   * @param off index of the first byte of the data structure.
   * @param buf buffer to be used to call {@link ClassReader#readUTF8 readUTF8},
   *      {@link ClassReader#readClass(int,char[]) readClass} or {@link
   *      ClassReader#readConst readConst}.
   *
   * @return offset position in bytecode after reading annotations
   */

  public static int readAnnotations (
    List annotations, ClassReader cr, int off, char[] buf) {
    int size = cr.readUnsignedShort(off);
    off += 2;
    for (int i = 0; i < size; i++) {
      Annotation ann = new Annotation();
      off = ann.read(cr, off, buf);
      annotations.add(ann);
    }
    return off;
  }

  /**
   * Utility method to read List of parameters annotations.
   *
   * @param parameters the List to store parameters annotations.
   *     Each element of the parameters List will have List of Annotation
   *     instances.
   * @param cr the class that contains the attribute to be read.
   * @param off index of the first byte of the data structure.
   * @param buf buffer to be used to call {@link ClassReader#readUTF8 readUTF8},
   *      {@link ClassReader#readClass(int,char[]) readClass} or {@link
   *      ClassReader#readConst readConst}.
   */

  public static void readParameterAnnotations (
    List parameters, ClassReader cr, int off, char[] buf) {
    int numParameters = cr.b[off++] & 0xff;
    for (int i = 0; i < numParameters; i++) {
      List annotations = new ArrayList();
      off = Annotation.readAnnotations(annotations, cr, off, buf);
      parameters.add(annotations);
    }
  }

  /**
   * Utility method to write List of annotations.
   *
   * @param bv the byte array form to store data structures.
   * @param annotations the List of annotations to write.
   *     Elements should be instances of the Annotation class.
   * @param cw the class to which this attribute must be added. This parameter
   *     can be used to add to the constant pool of this class the items that
   *     corresponds to this attribute.
   *
   * @return the byte array form with saved annotations.
   */

  public static ByteVector writeAnnotations (ByteVector bv,
                                             List annotations, ClassWriter cw) {
    bv.putShort(annotations.size());
    for (int i = 0; i < annotations.size(); i++) {
      ((Annotation)annotations.get(i)).write(bv, cw);
    }
    return bv;
  }

  /**
   * Utility method to write List of parameters annotations.
   *
   * @param bv the byte array form to store data structures.
   * @param parameters the List of parametars to write. Elements should be
   *     instances of the List that contains instances of the Annotation class.
   * @param cw the class to which this attribute must be added. This parameter
   *     can be used to add to the constant pool of this class the items that
   *     corresponds to this attribute.
   *
   * @return the byte array form with saved annotations.
   */

  public static ByteVector writeParametersAnnotations (ByteVector bv,
                                                       List parameters,
                                                       ClassWriter cw) {
    bv.putByte(parameters.size());
    for (int i = 0; i < parameters.size(); i++) {
      writeAnnotations(bv, (List)parameters.get(i), cw);
    }
    return bv;
  }

  /**
   * Returns annotation values in the format described in JSR-175 for Java
   * source code.
   * 
   * @param annotations a list of annotations.
   * @return annotation values in the format described in JSR-175 for Java
   *      source code.
   */

  public static String stringAnnotations (List annotations) {
    StringBuffer sb = new StringBuffer();
    if (annotations.size() > 0) {
      for (int i = 0; i < annotations.size(); i++) {
        sb.append('\n').append(annotations.get(i));
      }
    } else {
      sb.append( "<none>");
    }
    return sb.toString();
  }

  /**
   * Returns parameter annotation values in the format described in JSR-175
   * for Java source code.
   * 
   * @param parameters a list of parameter annotations.
   * @return parameter annotation values in the format described in JSR-175
   *      for Java source code.
   */

  public static String stringParameterAnnotations (List parameters) {
    StringBuffer sb = new StringBuffer();
    String sep = "";
    for (int i = 0; i < parameters.size(); i++) {
      sb.append(sep).append(stringAnnotations((List)parameters.get(i)));
      sep = ", ";
    }
    return sb.toString();
  }

  /**
   * Reads element_value data structures.
   *
   * @param cr the class that contains the attribute to be read.
   * @param off index of the first byte of the data structure.
   * @param buf buffer to be used to call {@link ClassReader#readUTF8 readUTF8},
   *      {@link ClassReader#readClass(int,char[]) readClass} or {@link
   *      ClassReader#readConst readConst}.
   *
   * @return offset position in bytecode after reading annotation
   */

  protected static Object readValue (ClassReader cr, int[] off, char[] buf) {
    Object value = null;
    int tag = cr.readByte(off[ 0]++);
    switch (tag) {
      case 'I':  // pointer to CONSTANT_Integer
      case 'J':  // pointer to CONSTANT_Long
      case 'D':  // pointer to CONSTANT_Double
      case 'F':  // pointer to CONSTANT_Float
        value = cr.readConst(cr.readUnsignedShort(off[0]), buf);
        off[0] += 2;
        break;

      case 'B':  // pointer to CONSTANT_Byte
        value = new Byte(( byte) cr.readInt( cr.getItem( cr.readUnsignedShort(off[0]))));
        off[0] += 2;
        break;
        
      case 'C':  // pointer to CONSTANT_Char
        value = new Character(( char) cr.readInt( cr.getItem( cr.readUnsignedShort(off[0]))));
        off[0] += 2;
        break;
        
      case 'S':  // pointer to CONSTANT_Short
        value = new Short(( short) cr.readInt( cr.getItem( cr.readUnsignedShort(off[0]))));
        off[0] += 2;
        break;
        
      case 'Z':  // pointer to CONSTANT_Boolean
        value = cr.readInt( cr.getItem( cr.readUnsignedShort(off[0])))==0 ? Boolean.FALSE : Boolean.TRUE;
        off[0] += 2;
        break;
        
      case 's':  // pointer to CONSTANT_Utf8
        value = cr.readUTF8(off[0], buf);
        off[0] += 2;
        break;

      case 'e':  // enum_const_value
        // TODO verify the data structures
        value = new EnumConstValue(cr.readUTF8(off[0], buf), cr.readUTF8(off[0] + 2, buf));
        off[0] += 4;
        break;

      case 'c':  // class_info
        value = Type.getType(cr.readUTF8(off[0], buf));
        off[0] += 2;
        break;

      case '@':  // annotation_value
        value = new Annotation();
        off[0] = ((Annotation) value).read(cr, off[0], buf);
        break;

      case '[':  // array_value
        int size = cr.readUnsignedShort(off[0]);
        off[0] += 2;

        //PATCH for GF#1624 (From ASM Team) - handle empty annotation array value 
        if(size==0) return new Object[0];
        
        int childTag = cr.readByte( off[ 0]);
        switch( childTag) {
	      case 'I':  // pointer to CONSTANT_Integer
            {
		      int[] v = new int[ size];
		      for( int i = 0; i < size; i++) {
			    off[ 0]++;  // skip element tag		      
			    v[ i] = cr.readInt( cr.getItem( cr.readUnsignedShort(off[0])));
			    off[ 0] += 2;
	          }
		      value = v;
            }
	        break;
	        
	      case 'J':  // pointer to CONSTANT_Long
            {
		      long[] v = new long[ size];
		      for( int i = 0; i < size; i++) {
			    off[ 0]++;  // skip element tag		      
			    v[ i] = cr.readLong( cr.getItem( cr.readUnsignedShort(off[0])));
			    off[ 0] += 2;
	          }
		      value = v;
            }
	        break;
	        
	      case 'D':  // pointer to CONSTANT_Double
            {
		      double[] v = new double[ size];
		      for( int i = 0; i < size; i++) {
			    off[ 0]++;  // skip element tag		      
			    v[ i] = Double.longBitsToDouble( cr.readLong( cr.getItem( cr.readUnsignedShort(off[0]))));
			    off[ 0] += 2;
	          }
		      value = v;
            }
	        break;
	        
	      case 'F':  // pointer to CONSTANT_Float
	        {
	          float[] v = new float[ size];
	          for( int i = 0; i < size; i++) {
		        off[ 0]++;  // skip element tag		      
		        v[ i] = Float.intBitsToFloat( cr.readInt( cr.getItem( cr.readUnsignedShort(off[0]))));
		        off[ 0] += 2;
              }
	          value = v;
	        }
	        break;
	
	      case 'B':  // pointer to CONSTANT_Byte
            {
		      byte[] v = new byte[ size];
		      for( int i = 0; i < size; i++) {
			    off[ 0]++;  // skip element tag		      
			    v[ i] = ( byte) cr.readInt( cr.getItem( cr.readUnsignedShort(off[0])));
			    off[ 0] += 2;
	          }
		      value = v;
            }
	        break;
	          
	      case 'C':  // pointer to CONSTANT_Char
            {
		      char[] v = new char[ size];
		      for( int i = 0; i < size; i++) {
			    off[ 0]++;  // skip element tag		      
			    v[ i] = ( char) cr.readInt( cr.getItem( cr.readUnsignedShort(off[0])));
			    off[ 0] += 2;
	          }
		      value = v;
            }
	        break;
	          
	      case 'S':  // pointer to CONSTANT_Short
            {
		      short[] v = new short[ size];
		      for( int i = 0; i < size; i++) {
			    off[ 0]++;  // skip element tag		      
			    v[ i] = ( short) cr.readInt( cr.getItem( cr.readUnsignedShort(off[0])));
			    off[ 0] += 2;
	          }
		      value = v;
            }
	        break;
	          
	      case 'Z':  // pointer to CONSTANT_Boolean
            {
		      boolean[] v = new boolean[ size];
		      for( int i = 0; i < size; i++) {
			    off[ 0]++;  // skip element tag		      
			    v[ i] = cr.readInt( cr.getItem( cr.readUnsignedShort(off[0])))!=0;
			    off[ 0] += 2;
	          }
		      value = v;
            }
	        break;
	          
          default:
	        Object[] v = new Object[ size];
	        value = v;
	        for (int i = 0; i < size; i++) {
	          v[i] = readValue(cr, off, buf);
	        }
	        break;
        }
        
    }
    return value;
  }

  /**
   * Writes element_value data structures.
   *
   * @param bv the byte array form to store data structures.
   * @param value
   * @param cw the class to which this attribute must be added. This parameter
   *      can be used to add to the constant pool of this class the items that
   *      corresponds to this attribute.
   * @return bv.
   */

  protected static ByteVector writeValue (ByteVector bv, Object value, ClassWriter cw) {
    if (value instanceof String) {
	  bv.putByte('s');
      bv.putShort(cw.newUTF8((String)value));
    
    } else if (value instanceof EnumConstValue) {
      bv.putByte('e');
      bv.putShort(cw.newUTF8(((EnumConstValue)value).typeName));
      bv.putShort(cw.newUTF8(((EnumConstValue)value).constName));
    
    } else if (value instanceof Type) {
      bv.putByte('c');
      bv.putShort(cw.newUTF8(((Type)value).getDescriptor()));
    
    } else if (value instanceof Annotation) {
      bv.putByte('@');
      ((Annotation)value).write(bv, cw);
    
    } else if (value instanceof Object[]) {
      bv.putByte('[');
      Object[] v = (Object[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        writeValue(bv, v[i], cw);
      }
    
    } else if( value instanceof byte[]) {
      bv.putByte('[');
      byte[] v = (byte[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        bv.putByte('B');
        bv.putShort(cw.newConstInt(v[i]));
      }
      
    } else if( value instanceof short[]) {
      bv.putByte('[');
      short[] v = (short[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        bv.putByte('S');
        bv.putShort(cw.newConstInt(v[i]));
      }
      
    } else if( value instanceof int[]) {
      bv.putByte('[');
      int[] v = (int[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        bv.putByte('I');
        bv.putShort(cw.newConstInt(v[i]));
      }
      
    } else if( value instanceof char[]) {
      bv.putByte('[');
      char[] v = (char[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        bv.putByte('C');
        bv.putShort(cw.newConstInt(v[i]));
      }
      
    } else if( value instanceof boolean[]) {
      bv.putByte('[');
      boolean[] v = (boolean[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        bv.putByte('Z');
        bv.putShort(cw.newConstInt(v[i] ? 1 : 0));
      }
      
    } else if( value instanceof long[]) {
      bv.putByte('[');
      long[] v = (long[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        bv.putByte('J');
        bv.putShort(cw.newConstLong(v[i]));
      }
      
    } else if( value instanceof float[]) {
      bv.putByte('[');
      float[] v = (float[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        bv.putByte('F');
        bv.putShort(cw.newConstFloat(v[i]));
      }
      
    } else if( value instanceof double[]) {
      bv.putByte('[');
      double[] v = (double[])value;
      bv.putShort(v.length);
      for (int i = 0; i < v.length; i++) {
        bv.putByte('D');
        bv.putShort(cw.newConstDouble(v[i]));
      }
      
    } else {
  	  int tag = -1;
      if (value instanceof Integer) {
	    tag = 'I';
	  } else if (value instanceof Byte) {
	    tag = 'B';
	  } else if (value instanceof Character) {
	    tag = 'C';
	  } else if (value instanceof Double) {
	    tag = 'D';
	  } else if (value instanceof Float) {
	    tag = 'F';
	  } else if (value instanceof Long) {
	    tag = 'J';
	  } else if (value instanceof Short) {
	    tag = 'S';
	  } else if (value instanceof Boolean) {
	    tag = 'Z';
      }
	  bv.putByte(tag);
      bv.putShort(cw.newConst(value));
    
    }

    return bv;
  }

  
  /**
   * Returns value in the format described in JSR-175 for Java source code.
   * 
   * @return value in the format described in JSR-175 for Java source code.
   */

  public String toString () {
    StringBuffer sb = new StringBuffer("@").append(type);
    // shorthand syntax for marker annotation
    if (elementValues.size() > 0) {
      sb.append(" ( ");
      String sep = "";
      for (int i = 0; i < elementValues.size(); i++) {
        Object[] value = (Object[])elementValues.get(i);
        // using shorthand syntax for single-element annotation
        if ( !( elementValues.size()==1 || "value".equals( elementValues.get( 0)))) {
          sb.append(sep).append(value[0]).append(" = ");
        }
        if(value[1] instanceof Object[]) {
	      Object[] v = ( Object[]) value[1];
          sb.append("{");
          String sep2 = "";
	      for( int j = 0; j < v.length; j++) {
            sb.append(sep2).append(v[ j]);
            sep2 = ", ";
          }
	      sb.append("}");
        } else {
          sb.append(value[1]);
        }
        sep = ", ";
      }
      sb.append(" )");
    }
    return sb.toString();
  }
  
  
  /**
   * Container class used to store enum_const_value structure.
   */
  public static class EnumConstValue {

    public String typeName;

    public String constName;

    public EnumConstValue (String typeName, String constName) {
      this.typeName = typeName;
      this.constName = constName;
    }

    public String toString () {
      return typeName + ":" + constName;
    }
  }
  
}

