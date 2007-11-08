/***
 * ASM XML Adapter
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

package oracle.toplink.libraries.asm.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.toplink.libraries.asm.ClassWriter;
import oracle.toplink.libraries.asm.CodeVisitor;
import oracle.toplink.libraries.asm.Constants;
import oracle.toplink.libraries.asm.Label;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * A {@link org.xml.sax.ContentHandler ContentHandler} 
 * that transforms XML document into Java class file. This class can be feeded by any
 * kind of SAX 2.0 event producers, e.g. XML parser, XSLT or XPath engines, or custom code.
 * 
 * @see oracle.toplink.libraries.asm.xml.SAXClassAdapter
 * @see oracle.toplink.libraries.asm.xml.Processor
 * 
 * @author Eugene Kuleshov
 */
public class ASMContentHandler extends DefaultHandler implements Constants {
  /**
   * Stack of the intermediate processing contexts.
   */
  private List stack = new ArrayList();
  /**
   * Complete name of the current element.
   */
  private String match = "";

  /**
   * <tt>true</tt> if the maximum stack size and number of local variables must
   * be automatically computed.
   */
  protected boolean computeMax;
  /**
   * Output stream to write result bytecode.
   */
  protected OutputStream os;

  /**
   * Current instance of the {@link ClassWriter ClassWriter} used to write class bytecode.
   */
  protected ClassWriter cw;
  /**
   * Current instance of the {@link CodeVisitor CodeVisitor} used to write method bytecode
   */
  protected CodeVisitor mw;
  /**
   * Map of the active {@link Label Label} instances for current method.
   */
  protected Map labels;
  
  private static final String BASE = "class";
  private final Rule[] RULES = { 
      new ClassRule( BASE), 
      new InterfaceRule( BASE+"/interfaces/interface"),
      new InterfacesRule( BASE+"/interfaces"),
      new FieldRule( BASE+"/field"),
      new MethodRule( BASE+"/method"),
      new ExceptionRule( BASE+"/method/exceptions/exception"),
      new ExceptionsRule( BASE+"/method/exceptions"),
      new InnerClassRule( BASE+"/innerclass"),
	  
      new OpcodesRule( BASE+"/method/code/"),  // opcodes
      
      new TableSwitchRule( BASE+"/method/code/TABLESWITCH"),
      new TableSwitchLabelRule( BASE+"/method/code/TABLESWITCH/label"),
      new LookupSwitchRule( BASE+"/method/code/LOOKUPSWITCH"),
      new LookupSwitchLabelRule( BASE+"/method/code/LOOKUPSWITCH/label"),

      new LabelRule( BASE+"/method/code/Label"),
      new TryCatchRule( BASE+"/method/code/TryCatch"),
      new LineNumberRule( BASE+"/method/code/LineNumber"),
      new LocalVarRule( BASE+"/method/code/LocalVar"),
      new MaxRule( BASE+"/method/code/Max")
    };
  
  private static interface OpcodeGroup {
    public static final int INSN = 0;
    public static final int INSN_INT = 1;
    public static final int INSN_VAR = 2;
    public static final int INSN_TYPE = 3;
    public static final int INSN_FIELD = 4;
    public static final int INSN_METHOD = 5;
    public static final int INSN_JUMP = 6;
    public static final int INSN_LDC = 7;
    public static final int INSN_IINC = 8;
    public static final int INSN_MULTIANEWARRAY = 9;
  }
  
  /**
   * Map of the opcode names to opcode and opcode group
   */
  static final Map OPCODES = new HashMap();
  static {
    OPCODES.put( "NOP", new Opcode( NOP, OpcodeGroup.INSN));
    OPCODES.put( "ACONST_NULL", new Opcode( ACONST_NULL, OpcodeGroup.INSN));
    OPCODES.put( "ICONST_M1", new Opcode( ICONST_M1, OpcodeGroup.INSN));
    OPCODES.put( "ICONST_0", new Opcode( ICONST_0, OpcodeGroup.INSN));
    OPCODES.put( "ICONST_1", new Opcode( ICONST_1, OpcodeGroup.INSN));
    OPCODES.put( "ICONST_2", new Opcode( ICONST_2, OpcodeGroup.INSN));
    OPCODES.put( "ICONST_3", new Opcode( ICONST_3, OpcodeGroup.INSN));
    OPCODES.put( "ICONST_4", new Opcode( ICONST_4, OpcodeGroup.INSN));
    OPCODES.put( "ICONST_5", new Opcode( ICONST_5, OpcodeGroup.INSN));
    OPCODES.put( "LCONST_0", new Opcode( LCONST_0, OpcodeGroup.INSN));
    OPCODES.put( "LCONST_1", new Opcode( LCONST_1, OpcodeGroup.INSN));
    OPCODES.put( "FCONST_0", new Opcode( FCONST_0, OpcodeGroup.INSN));
    OPCODES.put( "FCONST_1", new Opcode( FCONST_1, OpcodeGroup.INSN));
    OPCODES.put( "FCONST_2", new Opcode( FCONST_2, OpcodeGroup.INSN));
    OPCODES.put( "DCONST_0", new Opcode( DCONST_0, OpcodeGroup.INSN));
    OPCODES.put( "DCONST_1", new Opcode( DCONST_1, OpcodeGroup.INSN));
    OPCODES.put( "BIPUSH", new Opcode( BIPUSH, OpcodeGroup.INSN_INT));
    OPCODES.put( "SIPUSH", new Opcode( SIPUSH, OpcodeGroup.INSN_INT));
    OPCODES.put( "LDC", new Opcode( LDC, OpcodeGroup.INSN_LDC));
    OPCODES.put( "ILOAD", new Opcode( ILOAD, OpcodeGroup.INSN_VAR));
    OPCODES.put( "LLOAD", new Opcode( LLOAD, OpcodeGroup.INSN_VAR));
    OPCODES.put( "FLOAD", new Opcode( FLOAD, OpcodeGroup.INSN_VAR));
    OPCODES.put( "DLOAD", new Opcode( DLOAD, OpcodeGroup.INSN_VAR));
    OPCODES.put( "ALOAD", new Opcode( ALOAD, OpcodeGroup.INSN_VAR));
    OPCODES.put( "IALOAD", new Opcode( IALOAD, OpcodeGroup.INSN));
    OPCODES.put( "LALOAD", new Opcode( LALOAD, OpcodeGroup.INSN));
    OPCODES.put( "FALOAD", new Opcode( FALOAD, OpcodeGroup.INSN));
    OPCODES.put( "DALOAD", new Opcode( DALOAD, OpcodeGroup.INSN));
    OPCODES.put( "AALOAD", new Opcode( AALOAD, OpcodeGroup.INSN));
    OPCODES.put( "BALOAD", new Opcode( BALOAD, OpcodeGroup.INSN));
    OPCODES.put( "CALOAD", new Opcode( CALOAD, OpcodeGroup.INSN));
    OPCODES.put( "SALOAD", new Opcode( SALOAD, OpcodeGroup.INSN));
    OPCODES.put( "ISTORE", new Opcode( ISTORE, OpcodeGroup.INSN_VAR));
    OPCODES.put( "LSTORE", new Opcode( LSTORE, OpcodeGroup.INSN_VAR));
    OPCODES.put( "FSTORE", new Opcode( FSTORE, OpcodeGroup.INSN_VAR));
    OPCODES.put( "DSTORE", new Opcode( DSTORE, OpcodeGroup.INSN_VAR));
    OPCODES.put( "ASTORE", new Opcode( ASTORE, OpcodeGroup.INSN_VAR));
    OPCODES.put( "IASTORE", new Opcode( IASTORE, OpcodeGroup.INSN));
    OPCODES.put( "LASTORE", new Opcode( LASTORE, OpcodeGroup.INSN));
    OPCODES.put( "FASTORE", new Opcode( FASTORE, OpcodeGroup.INSN));
    OPCODES.put( "DASTORE", new Opcode( DASTORE, OpcodeGroup.INSN));
    OPCODES.put( "AASTORE", new Opcode( AASTORE, OpcodeGroup.INSN));
    OPCODES.put( "BASTORE", new Opcode( BASTORE, OpcodeGroup.INSN));
    OPCODES.put( "CASTORE", new Opcode( CASTORE, OpcodeGroup.INSN));
    OPCODES.put( "SASTORE", new Opcode( SASTORE, OpcodeGroup.INSN));
    OPCODES.put( "POP", new Opcode( POP, OpcodeGroup.INSN));
    OPCODES.put( "POP2", new Opcode( POP2, OpcodeGroup.INSN));
    OPCODES.put( "DUP", new Opcode( DUP, OpcodeGroup.INSN));
    OPCODES.put( "DUP_X1", new Opcode( DUP_X1, OpcodeGroup.INSN));
    OPCODES.put( "DUP_X2", new Opcode( DUP_X2, OpcodeGroup.INSN));
    OPCODES.put( "DUP2", new Opcode( DUP2, OpcodeGroup.INSN));
    OPCODES.put( "DUP2_X1", new Opcode( DUP2_X1, OpcodeGroup.INSN));
    OPCODES.put( "DUP2_X2", new Opcode( DUP2_X2, OpcodeGroup.INSN));
    OPCODES.put( "SWAP", new Opcode( SWAP, OpcodeGroup.INSN));
    OPCODES.put( "IADD", new Opcode( IADD, OpcodeGroup.INSN));
    OPCODES.put( "LADD", new Opcode( LADD, OpcodeGroup.INSN));
    OPCODES.put( "FADD", new Opcode( FADD, OpcodeGroup.INSN));
    OPCODES.put( "DADD", new Opcode( DADD, OpcodeGroup.INSN));
    OPCODES.put( "ISUB", new Opcode( ISUB, OpcodeGroup.INSN));
    OPCODES.put( "LSUB", new Opcode( LSUB, OpcodeGroup.INSN));
    OPCODES.put( "FSUB", new Opcode( FSUB, OpcodeGroup.INSN));
    OPCODES.put( "DSUB", new Opcode( DSUB, OpcodeGroup.INSN));
    OPCODES.put( "IMUL", new Opcode( IMUL, OpcodeGroup.INSN));
    OPCODES.put( "LMUL", new Opcode( LMUL, OpcodeGroup.INSN));
    OPCODES.put( "FMUL", new Opcode( FMUL, OpcodeGroup.INSN));
    OPCODES.put( "DMUL", new Opcode( DMUL, OpcodeGroup.INSN));
    OPCODES.put( "IDIV", new Opcode( IDIV, OpcodeGroup.INSN));
    OPCODES.put( "LDIV", new Opcode( LDIV, OpcodeGroup.INSN));
    OPCODES.put( "FDIV", new Opcode( FDIV, OpcodeGroup.INSN));
    OPCODES.put( "DDIV", new Opcode( DDIV, OpcodeGroup.INSN));
    OPCODES.put( "IREM", new Opcode( IREM, OpcodeGroup.INSN));
    OPCODES.put( "LREM", new Opcode( LREM, OpcodeGroup.INSN));
    OPCODES.put( "FREM", new Opcode( FREM, OpcodeGroup.INSN));
    OPCODES.put( "DREM", new Opcode( DREM, OpcodeGroup.INSN));
    OPCODES.put( "INEG", new Opcode( INEG, OpcodeGroup.INSN));
    OPCODES.put( "LNEG", new Opcode( LNEG, OpcodeGroup.INSN));
    OPCODES.put( "FNEG", new Opcode( FNEG, OpcodeGroup.INSN));
    OPCODES.put( "DNEG", new Opcode( DNEG, OpcodeGroup.INSN));
    OPCODES.put( "ISHL", new Opcode( ISHL, OpcodeGroup.INSN));
    OPCODES.put( "LSHL", new Opcode( LSHL, OpcodeGroup.INSN));
    OPCODES.put( "ISHR", new Opcode( ISHR, OpcodeGroup.INSN));
    OPCODES.put( "LSHR", new Opcode( LSHR, OpcodeGroup.INSN));
    OPCODES.put( "IUSHR", new Opcode( IUSHR, OpcodeGroup.INSN));
    OPCODES.put( "LUSHR", new Opcode( LUSHR, OpcodeGroup.INSN));
    OPCODES.put( "IAND", new Opcode( IAND, OpcodeGroup.INSN));
    OPCODES.put( "LAND", new Opcode( LAND, OpcodeGroup.INSN));
    OPCODES.put( "IOR", new Opcode( IOR, OpcodeGroup.INSN));
    OPCODES.put( "LOR", new Opcode( LOR, OpcodeGroup.INSN));
    OPCODES.put( "IXOR", new Opcode( IXOR, OpcodeGroup.INSN));
    OPCODES.put( "LXOR", new Opcode( LXOR, OpcodeGroup.INSN));
    OPCODES.put( "IINC", new Opcode( IINC, OpcodeGroup.INSN_IINC));
    OPCODES.put( "I2L", new Opcode( I2L, OpcodeGroup.INSN));
    OPCODES.put( "I2F", new Opcode( I2F, OpcodeGroup.INSN));
    OPCODES.put( "I2D", new Opcode( I2D, OpcodeGroup.INSN));
    OPCODES.put( "L2I", new Opcode( L2I, OpcodeGroup.INSN));
    OPCODES.put( "L2F", new Opcode( L2F, OpcodeGroup.INSN));
    OPCODES.put( "L2D", new Opcode( L2D, OpcodeGroup.INSN));
    OPCODES.put( "F2I", new Opcode( F2I, OpcodeGroup.INSN));
    OPCODES.put( "F2L", new Opcode( F2L, OpcodeGroup.INSN));
    OPCODES.put( "F2D", new Opcode( F2D, OpcodeGroup.INSN));
    OPCODES.put( "D2I", new Opcode( D2I, OpcodeGroup.INSN));
    OPCODES.put( "D2L", new Opcode( D2L, OpcodeGroup.INSN));
    OPCODES.put( "D2F", new Opcode( D2F, OpcodeGroup.INSN));
    OPCODES.put( "I2B", new Opcode( I2B, OpcodeGroup.INSN));
    OPCODES.put( "I2C", new Opcode( I2C, OpcodeGroup.INSN));
    OPCODES.put( "I2S", new Opcode( I2S, OpcodeGroup.INSN));
    OPCODES.put( "LCMP", new Opcode( LCMP, OpcodeGroup.INSN));
    OPCODES.put( "FCMPL", new Opcode( FCMPL, OpcodeGroup.INSN));
    OPCODES.put( "FCMPG", new Opcode( FCMPG, OpcodeGroup.INSN));
    OPCODES.put( "DCMPL", new Opcode( DCMPL, OpcodeGroup.INSN));
    OPCODES.put( "DCMPG", new Opcode( DCMPG, OpcodeGroup.INSN));
    OPCODES.put( "IFEQ", new Opcode( IFEQ, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IFNE", new Opcode( IFNE, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IFLT", new Opcode( IFLT, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IFGE", new Opcode( IFGE, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IFGT", new Opcode( IFGT, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IFLE", new Opcode( IFLE, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IF_ICMPEQ", new Opcode( IF_ICMPEQ, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IF_ICMPNE", new Opcode( IF_ICMPNE, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IF_ICMPLT", new Opcode( IF_ICMPLT, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IF_ICMPGE", new Opcode( IF_ICMPGE, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IF_ICMPGT", new Opcode( IF_ICMPGT, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IF_ICMPLE", new Opcode( IF_ICMPLE, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IF_ACMPEQ", new Opcode( IF_ACMPEQ, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IF_ACMPNE", new Opcode( IF_ACMPNE, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "GOTO", new Opcode( GOTO, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "JSR", new Opcode( JSR, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "RET", new Opcode( RET, OpcodeGroup.INSN_VAR));
    // OPCODES.put( "TABLESWITCH", new Opcode( TABLESWITCH, "visiTableSwitchInsn"));
    // OPCODES.put( "LOOKUPSWITCH", new Opcode( LOOKUPSWITCH, "visitLookupSwitch"));
    OPCODES.put( "IRETURN", new Opcode( IRETURN, OpcodeGroup.INSN));
    OPCODES.put( "LRETURN", new Opcode( LRETURN, OpcodeGroup.INSN));
    OPCODES.put( "FRETURN", new Opcode( FRETURN, OpcodeGroup.INSN));
    OPCODES.put( "DRETURN", new Opcode( DRETURN, OpcodeGroup.INSN));
    OPCODES.put( "ARETURN", new Opcode( ARETURN, OpcodeGroup.INSN));
    OPCODES.put( "RETURN", new Opcode( RETURN, OpcodeGroup.INSN));
    OPCODES.put( "GETSTATIC", new Opcode( GETSTATIC, OpcodeGroup.INSN_FIELD));
    OPCODES.put( "PUTSTATIC", new Opcode( PUTSTATIC, OpcodeGroup.INSN_FIELD));
    OPCODES.put( "GETFIELD", new Opcode( GETFIELD, OpcodeGroup.INSN_FIELD));
    OPCODES.put( "PUTFIELD", new Opcode( PUTFIELD, OpcodeGroup.INSN_FIELD));
    OPCODES.put( "INVOKEVIRTUAL", new Opcode( INVOKEVIRTUAL, OpcodeGroup.INSN_METHOD));
    OPCODES.put( "INVOKESPECIAL", new Opcode( INVOKESPECIAL, OpcodeGroup.INSN_METHOD));
    OPCODES.put( "INVOKESTATIC", new Opcode( INVOKESTATIC, OpcodeGroup.INSN_METHOD));
    OPCODES.put( "INVOKEINTERFACE", new Opcode( INVOKEINTERFACE, OpcodeGroup.INSN_METHOD));
    OPCODES.put( "NEW", new Opcode( NEW, OpcodeGroup.INSN_TYPE));
    OPCODES.put( "NEWARRAY", new Opcode( NEWARRAY, OpcodeGroup.INSN_INT));
    OPCODES.put( "ANEWARRAY", new Opcode( ANEWARRAY, OpcodeGroup.INSN_TYPE));
    OPCODES.put( "ARRAYLENGTH", new Opcode( ARRAYLENGTH, OpcodeGroup.INSN));
    OPCODES.put( "ATHROW", new Opcode( ATHROW, OpcodeGroup.INSN));
    OPCODES.put( "CHECKCAST", new Opcode( CHECKCAST, OpcodeGroup.INSN_TYPE));
    OPCODES.put( "INSTANCEOF", new Opcode( INSTANCEOF, OpcodeGroup.INSN_TYPE));
    OPCODES.put( "MONITORENTER", new Opcode( MONITORENTER, OpcodeGroup.INSN));
    OPCODES.put( "MONITOREXIT", new Opcode( MONITOREXIT, OpcodeGroup.INSN));
    OPCODES.put( "MULTIANEWARRAY", new Opcode( MULTIANEWARRAY, OpcodeGroup.INSN_MULTIANEWARRAY));
    OPCODES.put( "IFNULL", new Opcode( IFNULL, OpcodeGroup.INSN_JUMP));
    OPCODES.put( "IFNONNULL", new Opcode( IFNONNULL, OpcodeGroup.INSN_JUMP));
  }

  /**
   * Constructs a new {@link ASMContentHandler ASMContentHandler} object.
   * 
   * @param os output stream to write generated class. 
   * @param computeMax <tt>true</tt> if the maximum stack size and the maximum
   *      number of local variables must be automatically computed. 
   *      This value is passed to {@link ClassWriter ClassWriter}
   *      instance.
   */
  public ASMContentHandler( OutputStream os, boolean computeMax) {
    this.os = os;
    this.computeMax = computeMax;
  }

  /**
   * Returns the bytecode of the class that was build with underneath class writer.
   *
   * @return the bytecode of the class that was build with underneath class writer or
   *        null if there are no classwriter created.
   */
  public byte[] toByteArray() {
    return cw==null ? null : cw.toByteArray();
  }
  
  /**
   * Process notification of the start of an XML element being reached.
   * 
   * @param ns - The Namespace URI, or the empty string if the element has no
   *                Namespace URI or if Namespace processing is not being performed.
   * @param localName - The local name (without prefix), or the empty string if Namespace
   *                processing is not being performed.
   * @param qName - The qualified name (with prefix), or the empty string if
   *                qualified names are not available.
   * @param list - The attributes attached to the element. If there are no
   *                attributes, it shall be an empty Attributes object.
   * @exception SAXException if a parsing error is to be reported
   */
  public final void startElement( String ns, String localName, String qName, Attributes list) throws SAXException {
    // the actual element name is either in localName or qName, depending
    // on whether the parser is namespace aware
    String name = localName;
    if( name==null || name.length()<1) {
      name = qName;
    }

    // Compute the current matching rule
    StringBuffer sb = new StringBuffer( match);
    if( match.length() > 0) {
      sb.append( '/');
    }
    sb.append( name);
    match = sb.toString();

    // Fire "begin" events for all relevant rules
    for( int i = 0; i < RULES.length; i++) {
      if( RULES[ i].match( match, name)) {
        RULES[ i].begin( name, list);
      }
    }
  }

  /**
   * Process notification of the end of an XML element being reached.
   *
   * @param ns - The Namespace URI, or the empty string if the
   *   element has no Namespace URI or if Namespace processing is not
   *   being performed.
   * @param localName - The local name (without prefix), or the empty
   *   string if Namespace processing is not being performed.
   * @param qName - The qualified XML 1.0 name (with prefix), or the
   *   empty string if qualified names are not available.
   * 
   * @exception SAXException if a parsing error is to be reported
   */
  public final void endElement( String ns, String localName, String qName) throws SAXException {
    // the actual element name is either in localName or qName, depending
    // on whether the parser is namespace aware
    String name = localName;
    if( name == null || name.length() < 1) {
      name = qName;
    }

    // Fire "end" events for all relevant rules in reverse order
    for( int i = 0; i < RULES.length; i++) {
      if( RULES[ i].match( match, name)) {
        RULES[ i].end( name);
      }
    }

    // Recover the previous match expression
    int slash = match.lastIndexOf( '/');
    if( slash >= 0) {
      match = match.substring( 0, slash);
    } else {
      match = "";
    }
  }

  /**
   * Process notification of the end of a document and write generated bytecode 
   * into output stream.
   *
   * @exception SAXException if parsing or writing error is to be reported.
   */
  public final void endDocument() throws SAXException {
    try {
      os.write( cw.toByteArray());
    } catch( IOException ex) {
      throw new SAXException( ex.toString(), ex);
    }
  }


  /**
   * Return the top object on the stack without removing it. If there are no
   * objects on the stack, return <code>null</code>.
   */
  final Object peek() {
    return stack.size()==0 ? null : stack.get( stack.size()-1);
  }


  /**
   * Return the n'th object down the stack, where 0 is the top element
   * and [getCount()-1] is the bottom element.  If the specified index
   * is out of range, return <code>null</code>.
   *
   * @param n Index of the desired element, where 0 is the top of the stack,
   *  1 is the next element down, and so on.
   */
  final Object peek(int n) {
    return stack.size()<( n+1) ? null : stack.get( n);
  }


  /**
   * Pop the top object off of the stack, and return it.  If there are
   * no objects on the stack, return <code>null</code>.
   */
  final Object pop() {
    return stack.size()==0 ? null : stack.remove( stack.size()-1);
  }

  /**
   * Push a new object onto the top of the object stack.
   *
   * @param object The new object
   */
  final void push(Object object) {
    stack.add( object);
  }


  /**
   * Rule 
   */
  private abstract class Rule {
    protected String path;

    public Rule( String path) {
      this.path = path;
    }

    public final String getPath() {
      return path;
    }

    public void begin( String name, Attributes attrs) {
    }
    
    public void end( String name) {
    }

    public boolean match( String match, String element) {
      return path.equals( match);
    }

    protected final Object getValue( String desc, String val) {
      Object value = null;
      if( val!=null) {
        if( desc.equals( "Ljava/lang/String;")) {
          value =  decode( val);
        } else if( desc.equals( "Ljava/lang/Integer;") || 
            desc.equals( "Z") || desc.equals( "B") || desc.equals( "C") || desc.equals( "I") || desc.equals( "S")) {
          value = new Integer( val);
        } else if( desc.equals( "Ljava/lang/Long;") || desc.equals( "J")) {
          value = new Long( val);
        } else if( desc.equals( "Ljava/lang/Float;") || desc.equals( "F")) {
          value = new Float( val);
        } else if( desc.equals( "Ljava/lang/Double;") || desc.equals( "D")) {
          value = new Double( val);
        } else {
          throw new RuntimeException( "Invalid value:"+val+" desc:"+desc+" ctx:"+this);
        }
      }
      return value;
    }

    private final String decode( String val) {
      StringBuffer sb = new StringBuffer( val.length());
      try {
        int n = 0;
        while( n < val.length()) {
          char c = val.charAt( n);
          if( c == '\\') {
            n++;
            c = val.charAt( n);
            if( c == '\\') {
              sb.append( '\\');
            } else {
              n++; // skip 'u'
              sb.append( ( char) Integer.parseInt( val.substring( n, n + 4), 16));
              n += 3;
            }
          } else {
            sb.append( c);
          }
          n++;
        }
        
      } catch( RuntimeException ex) {
        System.err.println( val+"\n"+ex.toString());
        ex.printStackTrace();
        throw ex;
      }
      return sb.toString();
    }

    protected final Label getLabel( Object label) {
      Label lbl = ( Label) labels.get( label);
      if( lbl==null) {
        lbl = new Label();
        labels.put( label, lbl);
      }
      return lbl;
    }
    
    public String toString() {
      return path;
    }

    protected final CodeVisitor getCodeVisitor() {
      if( mw==null) {
        Map vals = ( Map) pop();
        int access = Integer.parseInt(( String) vals.get( "access"), 16);
        String name = ( String) vals.get( "name");
        String desc = ( String) vals.get( "desc");
        mw = cw.visitMethod( access, name, desc, null, null);
      }
      return mw;
    }

    protected final int getAccess( String s) {
      int access = 0;
      if( s.indexOf( "public")!=-1) access |= Constants.ACC_PUBLIC;
      if( s.indexOf( "private")!=-1) access |= Constants.ACC_PRIVATE;
      if( s.indexOf( "protected")!=-1) access |= Constants.ACC_PROTECTED;
      if( s.indexOf( "static")!=-1) access |= Constants.ACC_STATIC;
      if( s.indexOf( "final")!=-1) access |= Constants.ACC_FINAL;
      if( s.indexOf( "super")!=-1) access |= Constants.ACC_SUPER;
      if( s.indexOf( "synchronized")!=-1) access |= Constants.ACC_SYNCHRONIZED;
      if( s.indexOf( "volatile")!=-1) access |= Constants.ACC_VOLATILE;
      if( s.indexOf( "bridge")!=-1) access |= Constants.ACC_BRIDGE;
      if( s.indexOf( "varargs")!=-1) access |= Constants.ACC_VARARGS;
      if( s.indexOf( "transient")!=-1) access |= Constants.ACC_TRANSIENT;
      if( s.indexOf( "native")!=-1) access |= Constants.ACC_NATIVE;
      if( s.indexOf( "interface")!=-1) access |= Constants.ACC_INTERFACE;
      if( s.indexOf( "abstract")!=-1) access |= Constants.ACC_ABSTRACT;
      if( s.indexOf( "strict")!=-1) access |= Constants.ACC_STRICT;
      if( s.indexOf( "synthetic")!=-1) access |= Constants.ACC_SYNTHETIC;
      if( s.indexOf( "annotation")!=-1) access |= Constants.ACC_ANNOTATION;
      if( s.indexOf( "enum")!=-1) access |= Constants.ACC_ENUM;
      if( s.indexOf( "deprecated")!=-1) access |= Constants.ACC_DEPRECATED;
      return access;
    }
    
  }

  /**
   * ClassRule
   */
  private final class ClassRule extends Rule {

    public ClassRule( String path) {
      super( path);
    }
    
    public final void begin( String name, Attributes attrs) {
      int major = Integer.parseInt( attrs.getValue( "major"));
      int minor = Integer.parseInt( attrs.getValue( "minor"));
      cw = new ClassWriter( computeMax);
      Map vals = new HashMap();
      vals.put( "version", new Integer(minor << 16 | major));
      vals.put( "access", attrs.getValue( "access"));
      vals.put( "name", attrs.getValue( "name"));
      vals.put( "parent", attrs.getValue( "parent"));
      vals.put( "source", attrs.getValue( "source"));
      vals.put( "interfaces", new ArrayList());
      push( vals);  
      // values will be extracted in InterfacesRule.end();
    }

  }


  /**
   * InterfaceRule 
   */
  private final class InterfaceRule extends Rule {

    public InterfaceRule( String path) {
      super( path);
    }

    public final void begin( String name, Attributes attrs) {
      (( List) (( Map) peek()).get( "interfaces")).add( attrs.getValue( "name"));
    }
    
  }
  
  
  /**
   * InterfacesRule
   */
  private final class InterfacesRule extends Rule {

    public InterfacesRule( String path) {
      super( path);
    }

    public final void end( String element) {
      Map vals = ( Map) pop();
      int version = (( Integer)vals.get( "version")).intValue();
      int access = getAccess(( String) vals.get( "access"));
      String name = ( String) vals.get( "name");
      String parent = ( String) vals.get( "parent");
      String source = ( String) vals.get( "source");
      String[] interfaces = ( String[])(( List) vals.get( "interfaces")).toArray( new String[ 0]);
      cw.visit( version, access, name, parent, interfaces, source);
    }
    
  }

  
  /**
   * FieldRule
   */
  private final class FieldRule extends Rule {

    public FieldRule( String path) {
      super( path);
    }

    public final void begin( String element, Attributes attrs) {
      int access = getAccess( attrs.getValue( "access"));
      String name = attrs.getValue( "name");
      String desc = attrs.getValue( "desc");
      Object value = getValue( desc, attrs.getValue( "value"));
      cw.visitField( access, name, desc, value, null);
    }

  }
  
  
  /**
   * MethodRule
   */
  private final class MethodRule extends Rule {

    public MethodRule( String path) {
      super( path);
    }
    
    public final void begin( String name, Attributes attrs) {
      labels = new HashMap();
      Map vals = new HashMap();
      vals.put( "access", attrs.getValue( "access"));
      vals.put( "name", attrs.getValue( "name"));
      vals.put( "desc", attrs.getValue( "desc"));
      vals.put( "exceptions", new ArrayList());
      push( vals);  
      // values will be extracted in ExceptionsRule.end();
    }
    
    public final void end( String name) {
      mw = null;
      labels = null;
    }
    
  }

  
  /**
   * MethodRule
   */
  private final class InnerClassRule extends Rule {

    public InnerClassRule( String path) {
      super( path);
    }
    
    public final void begin( String element, Attributes attrs) {
      int access = getAccess( attrs.getValue( "access"));
      String name = attrs.getValue( "name");
      String outerName = attrs.getValue( "outerName");
      String innerName = attrs.getValue( "innerName");
      cw.visitInnerClass( name, outerName, innerName, access);
    }
    
    public final void end( String name) {
      mw = null;
      labels = null;
    }
    
  }

  
  /**
   * ExceptionRule 
   */
  private final class ExceptionRule extends Rule {

    public ExceptionRule( String path) {
      super( path);
    }

    public final void begin( String name, Attributes attrs) {
      (( List) (( Map) peek()).get( "exceptions")).add( attrs.getValue( "name"));
    }
    
  }
  
  
  /**
   * ExceptionsRule
   */
  private final class ExceptionsRule extends Rule {

    public ExceptionsRule( String path) {
      super( path);
    }

    public final void end( String element) {
      Map vals = ( Map) pop();
      int access = getAccess(( String) vals.get( "access"));
      String name = ( String) vals.get( "name");
      String desc = ( String) vals.get( "desc");
      String[] exceptions = ( String[])(( List) vals.get( "exceptions")).toArray( new String[ 0]);
      
      mw = cw.visitMethod( access, name, desc, exceptions, null);
    }
    
  }

  
  /**
   * TableSwitchRule
   */
  private class TableSwitchRule extends Rule {

    public TableSwitchRule( String path) {
      super( path);
    }

    public final void begin( String name, Attributes attrs) {
      Map vals = new HashMap();
      vals.put( "min", attrs.getValue( "min"));
      vals.put( "max", attrs.getValue( "max"));
      vals.put( "dflt", attrs.getValue( "dflt"));
      vals.put( "labels", new ArrayList());
      push( vals);
    }
    
    public final void end( String name) {
      Map vals = ( Map) pop();
      int min = Integer.parseInt(( String) vals.get( "min"));
      int max = Integer.parseInt(( String) vals.get( "max"));
      Label dflt = getLabel( vals.get( "dflt"));
      Label[] lbls = ( Label[])(( List) vals.get( "labels")).toArray( new Label[ 0]);
      getCodeVisitor().visitTableSwitchInsn( min, max, dflt, lbls);
    }
    
  }

  
  /**
   * TableSwitchLabelRule
   */
  private final class TableSwitchLabelRule extends Rule {

    public TableSwitchLabelRule( String path) {
      super( path);
    }
  
    public final void begin( String name, Attributes attrs) {
      (( List) (( Map) peek()).get( "labels")).add( getLabel( attrs.getValue( "name")));
    }
    
  }

  
  /**
   * LookupSwitchRule
   */
  private final class LookupSwitchRule extends Rule {

    public LookupSwitchRule( String path) {
      super( path);
    }

    public final void begin( String name, Attributes attrs) {
      Map vals = new HashMap();
      vals.put( "dflt", attrs.getValue( "dflt"));
      vals.put( "labels", new ArrayList());
      vals.put( "keys", new ArrayList());
      push( vals);
    }
    
    public final void end( String name) {
      Map vals = ( Map) pop();
      Label dflt = getLabel( vals.get( "dflt"));
      List keyList = ( List) vals.get( "keys");
      Label[] lbls = ( Label[])(( List) vals.get( "labels")).toArray( new Label[ 0]);
      int[] keys = new int[ keyList.size()];
      for( int i = 0; i < keys.length; i++) {
        keys[ i] = Integer.parseInt(( String) keyList.get( i));
      }
      getCodeVisitor().visitLookupSwitchInsn( dflt, keys, lbls);
    }
    
  }

  
  /**
   * LookupSwitchLabelRule
   */
  private final class LookupSwitchLabelRule extends Rule {

    public LookupSwitchLabelRule( String path) {
      super( path);
    }
  
    public final void begin( String name, Attributes attrs) {
      Map vals = ( Map) peek();
      (( List) vals.get( "labels")).add( getLabel( attrs.getValue( "name")));
      (( List) vals.get( "keys")).add( attrs.getValue( "key"));
    }
    
  }
  
  
  /**
   * LabelRule
   */
  private final class LabelRule extends Rule {

    public LabelRule( String path) {
      super( path);
    }

    public final void begin( String name, Attributes attrs) {
      getCodeVisitor().visitLabel( getLabel( attrs.getValue( "name")));
    }

  }
  
  
  /**
   * TryCatchRule
   */
  private final class TryCatchRule extends Rule {

    public TryCatchRule( String path) {
      super( path);
    }

    public final void begin( String name, Attributes attrs) {
      Label start = getLabel( attrs.getValue( "start"));
      Label end = getLabel( attrs.getValue( "end"));
      Label handler = getLabel( attrs.getValue( "handler"));
      String type = attrs.getValue( "type");
      getCodeVisitor().visitTryCatchBlock( start, end, handler, type);
    }

  }
  

  /**
   * LineNumberRule
   */
  private final class LineNumberRule extends Rule {

    public LineNumberRule( String path) {
      super( path);
    }

    public final void begin( String name, Attributes attrs) {
      int line = Integer.parseInt( attrs.getValue( "line"));
      Label start = getLabel( attrs.getValue( "start"));
      getCodeVisitor().visitLineNumber( line, start);
    }

  }
  

  /**
   * LocalVarRule
   */
  private final class LocalVarRule extends Rule {

    public LocalVarRule( String path) {
      super( path);
    }

    public final void begin( String element, Attributes attrs) {
      String name = attrs.getValue( "name");
      String desc = attrs.getValue( "desc");
      Label start = getLabel( attrs.getValue( "start"));
      Label end = getLabel( attrs.getValue( "end"));
      int var = Integer.parseInt( attrs.getValue( "var"));
      getCodeVisitor().visitLocalVariable( name, desc, start, end, var);
    }

  }
  

  /**
   * OpcodesRule
   */
  private final class OpcodesRule extends Rule {

    public OpcodesRule( String path) {
      super( path);
    }

    public boolean match( String match, String element) {
      return match.startsWith( path) && OPCODES.containsKey( element);
    }
    
    public final void begin( String element, Attributes attrs) {
      Opcode o = (( Opcode) OPCODES.get( element));
      if( o==null) return;
        
      switch( o.type) {
        case OpcodeGroup.INSN:
          getCodeVisitor().visitInsn( o.opcode);
          break;
          
        case OpcodeGroup.INSN_FIELD:
          getCodeVisitor().visitFieldInsn( o.opcode, attrs.getValue( "owner"), attrs.getValue( "name"), attrs.getValue( "desc"));
          break;
        
        case OpcodeGroup.INSN_INT:
          getCodeVisitor().visitIntInsn( o.opcode, Integer.parseInt( attrs.getValue( "value")));
          break;
        
        case OpcodeGroup.INSN_JUMP:
          getCodeVisitor().visitJumpInsn( o.opcode, getLabel( attrs.getValue( "label")));
          break;
        
        case OpcodeGroup.INSN_METHOD:
          getCodeVisitor().visitMethodInsn( o.opcode, attrs.getValue( "owner"), attrs.getValue( "name"), attrs.getValue( "desc"));
          break;
        
        case OpcodeGroup.INSN_TYPE:
          getCodeVisitor().visitTypeInsn( o.opcode, attrs.getValue( "desc"));
          break;
        
        case OpcodeGroup.INSN_VAR:
          getCodeVisitor().visitVarInsn( o.opcode, Integer.parseInt( attrs.getValue( "var")));
          break;

        case OpcodeGroup.INSN_IINC:
          getCodeVisitor().visitIincInsn( Integer.parseInt( attrs.getValue( "var")), Integer.parseInt( attrs.getValue( "inc")));
          break;          
        
        case OpcodeGroup.INSN_LDC:
          getCodeVisitor().visitLdcInsn( getValue( attrs.getValue( "desc"), attrs.getValue( "cst")));
          break;
        
        case OpcodeGroup.INSN_MULTIANEWARRAY:
          getCodeVisitor().visitMultiANewArrayInsn( attrs.getValue( "desc"), Integer.parseInt( attrs.getValue( "dims")));
          break;
          
        default:
          throw new RuntimeException( "Invalid element: "+element+" at "+match);
        
      }
    }
  }
  

  /**
   * MaxRule
   */
  private final class MaxRule extends Rule {

    public MaxRule( String path) {
      super( path);
    }

    public final void begin( String element, Attributes attrs) {
      int maxStack = Integer.parseInt( attrs.getValue( "maxStack"));
      int maxLocals = Integer.parseInt( attrs.getValue( "maxLocals"));
      getCodeVisitor().visitMaxs( maxStack, maxLocals);
    }

  }


  /**
   * Opcode
   */
  private final static class Opcode {
    public int opcode;
    public int type;
    
    public Opcode( int opcode, int type) {
      this.opcode = opcode;
      this.type = type;
    }

  }
  
}

