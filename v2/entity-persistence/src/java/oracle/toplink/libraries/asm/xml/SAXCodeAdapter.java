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

import java.util.HashMap;
import java.util.Map;

import oracle.toplink.libraries.asm.Attribute;
import oracle.toplink.libraries.asm.CodeVisitor;
import oracle.toplink.libraries.asm.Constants;
import oracle.toplink.libraries.asm.Label;
import oracle.toplink.libraries.asm.Type;
import oracle.toplink.libraries.asm.util.PrintCodeVisitor;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A {@link oracle.toplink.libraries.asm.CodeVisitor CodeVisitor} that generates SAX 2.0
 * events from the visited code. 
 * 
 * @see oracle.toplink.libraries.asm.xml.SAXClassAdapter
 * @see oracle.toplink.libraries.asm.xml.Processor
 * 
 * @author Eugene Kuleshov
 */
public final class SAXCodeAdapter implements CodeVisitor {
  private ContentHandler h;
  private Map labelNames;

  /**
   * Constructs a new {@link SAXCodeAdapter SAXCodeAdapter} object.
   * 
   * @param h content handler that will be used to send SAX 2.0 events. 
   */
  public SAXCodeAdapter( ContentHandler h) {
    this.h = h;
    labelNames = new HashMap();
  }

  public final void visitInsn( int opcode) {
    addElement( PrintCodeVisitor.OPCODES[ opcode], new AttributesImpl());
  }

  public final void visitIntInsn( int opcode, int operand) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "value", "value", "", Integer.toString( operand));
    addElement( PrintCodeVisitor.OPCODES[ opcode], attrs);
  }

  public final void visitVarInsn( int opcode, int var) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "var", "var", "", Integer.toString( var));
    addElement( PrintCodeVisitor.OPCODES[ opcode], attrs);
  }

  public final void visitTypeInsn( int opcode, String desc) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "desc", "desc", "", desc);
    addElement( PrintCodeVisitor.OPCODES[ opcode], attrs);
  }

  public final void visitFieldInsn( int opcode, String owner, String name, String desc) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "owner", "owner", "", owner);
    attrs.addAttribute( "", "name", "name", "", name);
    attrs.addAttribute( "", "desc", "desc", "", desc);
    addElement( PrintCodeVisitor.OPCODES[ opcode], attrs);
  }

  public final void visitMethodInsn( int opcode, String owner, String name, String desc) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "owner", "owner", "", owner);
    attrs.addAttribute( "", "name", "name", "", name);
    attrs.addAttribute( "", "desc", "desc", "", desc);
    addElement( PrintCodeVisitor.OPCODES[ opcode], attrs);
  }

  public final void visitJumpInsn( int opcode, Label label) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "label", "label", "", getLabel( label));
    addElement( PrintCodeVisitor.OPCODES[ opcode], attrs);
  }

  public final void visitLabel( Label label) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "name", "name", "", getLabel( label));
    addElement( "Label", attrs);
  }

  public final void visitLdcInsn( Object cst) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "cst", "cst", "", SAXClassAdapter.encode( cst.toString()));
    attrs.addAttribute( "", "desc", "desc", "", Type.getDescriptor( cst.getClass()));
    addElement( PrintCodeVisitor.OPCODES[ Constants.LDC], attrs);
  }

  public final void visitIincInsn( int var, int increment) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "var", "var", "", Integer.toString( var));
    attrs.addAttribute( "", "inc", "inc", "", Integer.toString( increment));
    addElement( PrintCodeVisitor.OPCODES[ Constants.IINC], attrs);
  }

  public final void visitTableSwitchInsn( int min, int max, Label dflt, Label[] labels) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "min", "min", "", Integer.toString( min));
    attrs.addAttribute( "", "max", "max", "", Integer.toString( max));
    attrs.addAttribute( "", "dflt", "dflt", "", getLabel( dflt));
    String o = PrintCodeVisitor.OPCODES[ Constants.TABLESWITCH];
    addStart( o, attrs);
    for( int i = 0; i < labels.length; i++) {
      AttributesImpl att2 = new AttributesImpl();
      att2.addAttribute( "", "name", "name", "", getLabel( labels[ i]));
      addElement( "label", att2);
    }
    addEnd( o);
  }

  public final void visitLookupSwitchInsn( Label dflt, int[] keys, Label[] labels) {
    AttributesImpl att = new AttributesImpl();
    att.addAttribute( "", "dflt", "dflt", "", getLabel( dflt));
    String o = PrintCodeVisitor.OPCODES[ Constants.LOOKUPSWITCH];
    addStart( o, att);
    for( int i = 0; i < labels.length; i++) {
      AttributesImpl att2 = new AttributesImpl();
      att2.addAttribute( "", "name", "name", "", getLabel( labels[ i]));
      att2.addAttribute( "", "key", "key", "", Integer.toString( keys[ i]));
      addElement( "label", att2);
    }
    addEnd( o);
  }

  public final void visitMultiANewArrayInsn( String desc, int dims) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "desc", "desc", "", desc);
    attrs.addAttribute( "", "dims", "dims", "", Integer.toString( dims));
    addElement( PrintCodeVisitor.OPCODES[ Constants.MULTIANEWARRAY], attrs);
  }

  public final void visitTryCatchBlock( Label start, Label end, Label handler, String type) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "start", "start", "", getLabel( start));
    attrs.addAttribute( "", "end", "end", "", getLabel( end));
    attrs.addAttribute( "", "handler", "handler", "", getLabel( handler));
    if( type!=null) attrs.addAttribute( "", "type", "type", "", type);
    addElement( "TryCatch", attrs);
  }

  public final void visitMaxs( int maxStack, int maxLocals) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "maxStack", "maxStack", "", Integer.toString( maxStack));
    attrs.addAttribute( "", "maxLocals", "maxLocals", "", Integer.toString( maxLocals));
    addElement( "Max", attrs);

    addEnd( "code");
    addEnd( "method");
    // TODO ensure it it is ok to close method element here
  }

  public final void visitLocalVariable( String name, String desc, Label start, Label end, int index) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "name", "name", "", name);
    attrs.addAttribute( "", "desc", "desc", "", desc);
    attrs.addAttribute( "", "start", "start", "", getLabel( start));
    attrs.addAttribute( "", "end", "end", "", getLabel( end));
    attrs.addAttribute( "", "var", "var", "", Integer.toString( index));
    addElement( "LocalVar", attrs);
  }

  public final void visitLineNumber( int line, Label start) {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute( "", "line", "line", "", Integer.toString( line));
    attrs.addAttribute( "", "start", "start", "", getLabel( start));
    addElement( "LineNumber", attrs);
  }

  public final void visitAttribute( Attribute attr) {
    // TODO Auto-generated SAXCodeAdapter.visitAttribute
  }

  private final String getLabel( Label label) {
    String name = (String) labelNames.get( label);
    if( name==null) {
      name = Integer.toString( labelNames.size());
      labelNames.put( label, name);
    }
    return name;
  }
  
  private final void addElement( String name, Attributes attrs) {
    addStart( name, attrs);
    addEnd( name);
  }

  private final void addStart( String name, Attributes attrs) {
    try {
      h.startElement( "", name, name, attrs);
    } catch( SAXException ex) {
      throw new RuntimeException( ex.toString());
    }
  }

  private final void addEnd( String name) {
    try {
      h.endElement( "", name, name);
    } catch( SAXException ex) {
      throw new RuntimeException( ex.toString());
    }
  }

}

