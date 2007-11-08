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

import oracle.toplink.libraries.asm.Attribute;
import oracle.toplink.libraries.asm.ClassVisitor;
import oracle.toplink.libraries.asm.CodeVisitor;
import oracle.toplink.libraries.asm.Constants;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * A {@link oracle.toplink.libraries.asm.ClassVisitor ClassVisitor} that generates SAX 2.0 
 * events from the visited class. It can feed any kind of  
 * {@link org.xml.sax.ContentHandler ContentHandler}, 
 * e.g. XML serializer, XSLT or XQuery engines. 
 * 
 * @see oracle.toplink.libraries.asm.xml.Processor
 * @see oracle.toplink.libraries.asm.xml.ASMContentHandler
 * 
 * @author Eugene Kuleshov
 */
public final class SAXClassAdapter implements ClassVisitor {
  private ContentHandler h;
  private boolean singleDocument;

  /**
   * Constructs a new {@link SAXClassAdapter SAXClassAdapter} object.
   * 
   * @param h content handler that will be used to send SAX 2.0 events.
   * @param singleDocument if <tt>true</tt> adapter will not produce
   *   {@link ContentHandler#startDocument() startDocument()} and 
   *   {@link ContentHandler#endDocument() endDocument()} events.
   */
  public SAXClassAdapter( ContentHandler h, boolean singleDocument) {
    this.h = h;
    this.singleDocument = singleDocument;
    if( !singleDocument) {
      try {
        h.startDocument();
      } catch( SAXException ex) {
        throw new RuntimeException( ex.getException());
      }
    }
  }

  public final void visit( int version, int access, String name, String superName, String[] interfaces, String sourceFile) {
    try {
      StringBuffer sb = new StringBuffer();
      if(( access & Constants.ACC_PUBLIC)!=0) sb.append( "public ");
      if(( access & Constants.ACC_PRIVATE)!=0) sb.append( "private ");
      if(( access & Constants.ACC_PROTECTED)!=0) sb.append( "protected ");
      if(( access & Constants.ACC_FINAL)!=0) sb.append( "final ");
      if(( access & Constants.ACC_SUPER)!=0) sb.append( "super ");
      if(( access & Constants.ACC_INTERFACE)!=0) sb.append( "interface ");
      if(( access & Constants.ACC_ABSTRACT)!=0) sb.append( "abstract ");
      if(( access & Constants.ACC_SYNTHETIC)!=0) sb.append( "synthetic ");
      if(( access & Constants.ACC_ANNOTATION)!=0) sb.append( "annotation ");
      if(( access & Constants.ACC_ENUM)!=0) sb.append( "enum ");
      if(( access & Constants.ACC_DEPRECATED)!=0) sb.append( "deprecated ");
      
      AttributesImpl attrs = new AttributesImpl();
      attrs.addAttribute( "", "access", "access", "", sb.toString());
      if( name!=null) attrs.addAttribute( "", "name", "name", "", name);
      if( superName!=null) attrs.addAttribute( "", "parent", "parent", "", superName);
      if( sourceFile!=null) attrs.addAttribute( "", "source", "source", "", sourceFile);
      attrs.addAttribute( "", "major", "major", "", new Integer(version & 0xFFFF).toString());
      attrs.addAttribute( "", "minor", "minor", "", new Integer(version >>> 16).toString());
      h.startElement( "", "class", "class", attrs);
      
      h.startElement( "", "interfaces", "interfaces", new AttributesImpl());
      if( interfaces!=null && interfaces.length>0) {
        for( int i = 0; i < interfaces.length; i++) {
          AttributesImpl attrs2 = new AttributesImpl();
          attrs2.addAttribute( "", "name", "name", "", interfaces[ i]);
          h.startElement( "", "interface", "interface", attrs2);
          h.endElement( "", "interface", "interface");
        }
      }
	  h.endElement( "", "interfaces", "interfaces");
      
    } catch( SAXException ex) {
      throw new RuntimeException( ex.getException());
    }
  }

  public final void visitField( int access, String name, String desc, Object value, Attribute attrs) {
    StringBuffer sb = new StringBuffer();
    if(( access & Constants.ACC_PUBLIC)!=0) sb.append( "public ");
    if(( access & Constants.ACC_PRIVATE)!=0) sb.append( "private ");
    if(( access & Constants.ACC_PROTECTED)!=0) sb.append( "protected ");
    if(( access & Constants.ACC_STATIC)!=0) sb.append( "static ");
    if(( access & Constants.ACC_FINAL)!=0) sb.append( "final ");
    if(( access & Constants.ACC_VOLATILE)!=0) sb.append( "volatile ");
    if(( access & Constants.ACC_TRANSIENT)!=0) sb.append( "transient ");
    if(( access & Constants.ACC_SYNTHETIC)!=0) sb.append( "synthetic ");
    if(( access & Constants.ACC_ENUM)!=0) sb.append( "enum ");
    if(( access & Constants.ACC_DEPRECATED)!=0) sb.append( "deprecated ");
    
    AttributesImpl att = new AttributesImpl();
    att.addAttribute( "", "access", "access", "", sb.toString());
    att.addAttribute( "", "name", "name", "", name);
    att.addAttribute( "", "desc", "desc", "", desc);
    if( value!=null) {
      att.addAttribute( "", "value", "value", "", encode( value.toString()));
    }
    try {
      h.startElement( "", "field", "field", att);
      h.endElement( "", "field", "field");
    } catch( SAXException ex) {
      throw new RuntimeException( ex.toString());
    }
  }

  public final CodeVisitor visitMethod( int access, String name, String desc, String[] exceptions, Attribute attrs) {
    StringBuffer sb = new StringBuffer();
    if(( access & Constants.ACC_PUBLIC)!=0) sb.append( "public ");
    if(( access & Constants.ACC_PRIVATE)!=0) sb.append( "private ");
    if(( access & Constants.ACC_PROTECTED)!=0) sb.append( "protected ");
    if(( access & Constants.ACC_STATIC)!=0) sb.append( "static ");
    if(( access & Constants.ACC_FINAL)!=0) sb.append( "final ");
    if(( access & Constants.ACC_SYNCHRONIZED)!=0) sb.append( "synchronized ");
    if(( access & Constants.ACC_BRIDGE)!=0) sb.append( "bridge ");
    if(( access & Constants.ACC_VARARGS)!=0) sb.append( "varargs ");
    if(( access & Constants.ACC_NATIVE)!=0) sb.append( "native ");
    if(( access & Constants.ACC_ABSTRACT)!=0) sb.append( "abstract ");
    if(( access & Constants.ACC_STRICT)!=0) sb.append( "strict ");
    if(( access & Constants.ACC_SYNTHETIC)!=0) sb.append( "synthetic ");
    if(( access & Constants.ACC_DEPRECATED)!=0) sb.append( "deprecated ");
    
    try {
      AttributesImpl att = new AttributesImpl();
      att.addAttribute( "", "access", "access", "", sb.toString());
      att.addAttribute( "", "name", "name", "", name);
      att.addAttribute( "", "desc", "desc", "", desc);
      h.startElement( "", "method", "method", att);

      h.startElement( "", "exceptions", "exceptions", new AttributesImpl());
      if( exceptions!=null && exceptions.length>0) {
        for( int i = 0; i < exceptions.length; i++) {
          AttributesImpl att2 = new AttributesImpl();
          att2.addAttribute( "", "name", "name", "", exceptions[ i]);
          h.startElement( "", "exception", "exception", att2);
          h.endElement( "", "exception", "exception");
        }
      }
      h.endElement( "", "exceptions", "exceptions");
      if(( access & ( Constants.ACC_ABSTRACT | Constants.ACC_INTERFACE | Constants.ACC_NATIVE))>0) {
        h.endElement( "", "method", "method");
      } else {
        h.startElement( "", "code", "code", new AttributesImpl());
      }
      
    } catch( SAXException ex) {
      throw new RuntimeException( ex.toString());
    }
    
    return new SAXCodeAdapter( h);
  }

  public final void visitInnerClass( String name, String outerName, String innerName, int access) {
    StringBuffer sb = new StringBuffer();
    if(( access & Constants.ACC_PUBLIC)!=0) sb.append( "public ");
    if(( access & Constants.ACC_PRIVATE)!=0) sb.append( "private ");
    if(( access & Constants.ACC_PROTECTED)!=0) sb.append( "protected ");
    if(( access & Constants.ACC_STATIC)!=0) sb.append( "static ");
    if(( access & Constants.ACC_FINAL)!=0) sb.append( "final ");
    if(( access & Constants.ACC_SUPER)!=0) sb.append( "super ");
    if(( access & Constants.ACC_INTERFACE)!=0) sb.append( "interface ");
    if(( access & Constants.ACC_ABSTRACT)!=0) sb.append( "abstract ");
    if(( access & Constants.ACC_SYNTHETIC)!=0) sb.append( "synthetic ");
    if(( access & Constants.ACC_ANNOTATION)!=0) sb.append( "annotation ");
    if(( access & Constants.ACC_ENUM)!=0) sb.append( "enum ");
    if(( access & Constants.ACC_DEPRECATED)!=0) sb.append( "deprecated ");
    
    try {
      AttributesImpl attrs = new AttributesImpl();
      attrs.addAttribute( "", "access", "access", "", sb.toString());
      if( name!=null) attrs.addAttribute( "", "name", "name", "", name);
      if( outerName!=null) attrs.addAttribute( "", "outerName", "outerName", "", outerName);
      if( innerName!=null) attrs.addAttribute( "", "innerName", "innerName", "", innerName);
      h.startElement( "", "innerclass", "innerclass", attrs);
      h.endElement( "", "innerclass", "innerclass");
    
    } catch( SAXException ex) {
      throw new RuntimeException( ex.toString());
    
    }
  }

  public final void visitAttribute( Attribute attr) {
    // TODO Auto-generated SAXClassAdapter.visitAttribute
  }

  public final void visitEnd() {
    try {
      h.endElement( "", "class", "class");
      if( !singleDocument) {
        h.endDocument();
      }
    } catch( SAXException ex) {
      ex.getException().printStackTrace();
      ex.printStackTrace();
      throw new RuntimeException( ex.toString());
    }
  }
  
  static final String encode( String s) {
    StringBuffer sb = new StringBuffer();
    for( int i = 0; i<s.length(); i++) {
      char c = s.charAt( i);
      if( c=='\\') {
        sb.append( "\\\\");
      } else if( c<0x20 || c>0x7f) {
        sb.append( "\\u");
        if( c<0x10) {
          sb.append( "000");
        } else if( c<0x100) {
          sb.append( "00");
        } else if( c<0x1000) {
          sb.append( "0");
        }
        sb.append( Integer.toString( c, 16));
      } else {
        sb.append( c);
      }
    }
    return sb.toString();
  }

}

