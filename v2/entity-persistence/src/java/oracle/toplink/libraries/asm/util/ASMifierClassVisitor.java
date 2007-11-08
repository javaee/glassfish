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

import java.io.FileInputStream;
import java.io.PrintWriter;

import oracle.toplink.libraries.asm.Attribute;
import oracle.toplink.libraries.asm.ClassReader;
import oracle.toplink.libraries.asm.CodeVisitor;
import oracle.toplink.libraries.asm.Constants;
import oracle.toplink.libraries.asm.Type;
import oracle.toplink.libraries.asm.util.attrs.ASMifiable;

/**
 * A {@link PrintClassVisitor PrintClassVisitor} that prints the ASM code that
 * generates the classes it visits. This class visitor can be used to quickly
 * write ASM code to generate some given bytecode:
 * <ul>
 * <li>write the Java source code equivalent to the bytecode you want to
 * generate;</li>
 * <li>compile it with <tt>javac</tt>;</li>
 * <li>make a {@link ASMifierClassVisitor} visit this compiled
 * class (see the {@link #main main} method);</li>
 * <li>edit the generated source code, if necessary.</li>
 * </ul>
 * The source code printed when visiting the <tt>Hello</tt> class is the
 * following:
 * <p>
 * <blockquote>
 * <pre>
 * import oracle.toplink.libraries.asm.*;
 * import java.io.FileOutputStream;
 *
 * public class Dump implements Constants {
 *
 * public static void main (String[] args) throws Exception {
 *
 * ClassWriter cw = new ClassWriter(false);
 * CodeVisitor cv;
 *
 * cw.visit(ACC_PUBLIC + ACC_SUPER, "Hello", "java/lang/Object", null, "Hello.java");
 *
 * {
 * cv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
 * cv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
 * cv.visitLdcInsn("hello");
 * cv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
 * cv.visitInsn(RETURN);
 * cv.visitMaxs(2, 1);
 * }
 * {
 * cv = cw.visitMethod(ACC_PUBLIC, "&lt;init&gt;", "()V", null, null);
 * cv.visitVarInsn(ALOAD, 0);
 * cv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "&lt;init&gt;", "()V");
 * cv.visitInsn(RETURN);
 * cv.visitMaxs(1, 1);
 * }
 * cw.visitEnd();
 *
 * FileOutputStream os = new FileOutputStream("Dumped.class");
 * os.write(cw.toByteArray());
 * os.close();
 * }
 * }
 * </pre>
 * </blockquote>
 * where <tt>Hello</tt> is defined by:
 * <p>
 * <blockquote>
 * <pre>
 * public class Hello {
 *
 *   public static void main (String[] args) {
 *     System.out.println("hello");
 *   }
 * }
 * </pre>
 * </blockquote>
 * 
 * @author Eric Bruneton, Eugene Kuleshov
 */

public class ASMifierClassVisitor extends PrintClassVisitor {

  private static final int ACCESS_CLASS = 262144;
  private static final int ACCESS_FIELD = 524288;
  private static final int ACCESS_INNER = 1048576;

  /**
   * Prints the ASM source code to generate the given class to the standard
   * output.
   * <p>
   * Usage: ASMifierClassVisitor [-debug]
   * &lt;fully qualified class name or class file name&gt;
   *
   * @param args the command line arguments.
   *
   * @throws Exception if the class cannot be found, or if an IO exception
   *      occurs.
   */

  public static void main (final String[] args) throws Exception {
    if (args.length < 1 || args.length > 2) {
      printUsage();
    }
    int i = 0;
    boolean skipDebug = true;
    if (args[0].equals("-debug")) {
      i = 1;
      skipDebug = false;
      if (args.length != 2) {
        printUsage();
      }
    }
    ClassReader cr;
    if (args[i].endsWith(".class")) {
      cr = new ClassReader(new FileInputStream(args[i]));
    } else {
      cr = new ClassReader(args[i]);
    }
    cr.accept(new ASMifierClassVisitor(
      new PrintWriter(System.out)), getDefaultAttributes(), skipDebug);
  }

  private static void printUsage () {
    System.err.println("Prints the ASM code to generate the given class.");
    System.err.println("Usage: ASMifierClassVisitor [-debug] " +
                       "<fully qualified class name or class file name>");
    System.exit(-1);
  }

  /**
   * Constructs a new {@link ASMifierClassVisitor} object.
   *
   * @param pw the print writer to be used to print the class.
   */

  public ASMifierClassVisitor (final PrintWriter pw) {
    super(pw);
  }

  public void visit (
    final int version,
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    int n = name.lastIndexOf( "/");
    if( n>-1) {
      text.add("package asm."+name.substring( 0, n).replace( '/', '.')+";\n");
    }
    
    text.add("import oracle.toplink.libraries.asm.*;\n");
    text.add("import oracle.toplink.libraries.asm.attrs.*;\n");
    text.add("import java.util.*;\n\n");
    text.add("public class "+(n==-1 ? name : name.substring( n+1))+"Dump implements Constants {\n\n");
    text.add("public static byte[] dump () throws Exception {\n\n");
    text.add("ClassWriter cw = new ClassWriter(false);\n");
    text.add("CodeVisitor cv;\n\n");

    buf.setLength(0);
    buf.append("cw.visit(");    
    switch(version) {
      case Constants.V1_1:
        buf.append("V1_1");
        break;
      
      case Constants.V1_2:
        buf.append("V1_2");
        break;
      
      case Constants.V1_3:
        buf.append("V1_3");
        break;
      
      case Constants.V1_4:
        buf.append("V1_4");
        break;
      
      case Constants.V1_5:
        buf.append("V1_5");
        break;
  
      default:
        buf.append(version);
        break;
    }
    buf.append(", ");
    appendAccess(access | ACCESS_CLASS);
    buf.append(", ");
    appendConstant(buf, name);
    buf.append(", ");
    appendConstant(buf, superName);
    buf.append(", ");
    if (interfaces != null && interfaces.length > 0) {
      buf.append("new String[] {");
      for (int i = 0; i < interfaces.length; ++i) {
        buf.append(i == 0 ? " " : ", ");
        appendConstant(buf, interfaces[i]);
      }
      buf.append(" }");
    } else {
      buf.append("null");
    }
    buf.append(", ");
    appendConstant(buf, sourceFile);
    buf.append(");\n\n");
    text.add(buf.toString());
  }

  public void visitInnerClass (
    final String name,
    final String outerName,
    final String innerName,
    final int access)
  {
    buf.setLength(0);
    buf.append("cw.visitInnerClass(");
    appendConstant(buf, name);
    buf.append(", ");
    appendConstant(buf, outerName);
    buf.append(", ");
    appendConstant(buf, innerName);
    buf.append(", ");
    appendAccess(access | ACCESS_INNER);
    buf.append(");\n\n");
    text.add(buf.toString());
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value,
    final Attribute attrs)
  {
    buf.setLength(0);

    int n = 1;
    if (attrs != null) {
      buf.append("{\n");
      buf.append("// FIELD ATTRIBUTES\n");
      Attribute a = attrs;
      while (a != null) {
        if (a instanceof ASMifiable) {
          ((ASMifiable)a).asmify(buf, "fieldAttrs" + n, null);
          if (n > 1) {
            buf.append("fieldAttrs" + (n - 1) + " = fieldAttrs" + n + ";\n");
          }
	      n++;
        } else {
          buf.append("// WARNING! skipped non standard field attribute of type ");
          buf.append(a.type).append("\n");
        }
        a = a.next;
      }
    }

    buf.append("cw.visitField(");
    appendAccess(access | ACCESS_FIELD);
    buf.append(", ");
    appendConstant(buf, name);
    buf.append(", ");
    appendConstant(buf, desc);
    buf.append(", ");
    appendConstant(buf, value);

    if (n==1) {
      buf.append(", null);\n\n");
    } else {
      buf.append(", fieldAttrs1);\n");
      buf.append("}\n\n");
    }

    text.add(buf.toString());
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions,
    final Attribute attrs)
  {
    buf.setLength(0);

    buf.append("{\n");

    int n = 1;
    if (attrs != null) {
      buf.append("// METHOD ATTRIBUTES\n");
      Attribute a = attrs;
      while (a != null) {
        if (a instanceof ASMifiable) {
          ((ASMifiable)a).asmify(buf, "methodAttrs" + n, null);
          if (n > 1) {
            buf.append("methodAttrs" + (n - 1) + ".next = methodAttrs" + n + ";\n");
          }
          n++;
        } else {
          buf.append("// WARNING! skipped non standard method attribute of type ");
          buf.append(a.type).append("\n");
        }
        a = a.next;
      }
    }

    buf.append("cv = cw.visitMethod(");
    appendAccess(access);
    buf.append(", ");
    appendConstant(buf, name);
    buf.append(", ");
    appendConstant(buf, desc);
    buf.append(", ");
    if (exceptions != null && exceptions.length > 0) {
      buf.append("new String[] {");
      for (int i = 0; i < exceptions.length; ++i) {
        buf.append(i == 0 ? " " : ", ");
        appendConstant(buf, exceptions[i]);
      }
      buf.append(" }");
    } else {
      buf.append("null");
    }
    if (n==1) {
      buf.append(", null);\n");
    } else {
      buf.append(", methodAttrs1);\n");
    }

    text.add(buf.toString());
    PrintCodeVisitor pcv = new ASMifierCodeVisitor();
    text.add(pcv.getText());
    text.add("}\n");
    return pcv;
  }

  public void visitAttribute (final Attribute attr) {
    buf.setLength(0);
    if (attr instanceof ASMifiable) {
      buf.append("{\n");
      buf.append("// CLASS ATRIBUTE\n");
      ((ASMifiable)attr).asmify(buf, "attr", null);
      buf.append("cw.visitAttribute(attr);\n");
      buf.append("}\n");
    } else {
      buf.append("// WARNING! skipped a non standard class attribute of type \"");
      buf.append(attr.type).append("\"\n");
    }
    text.add(buf.toString());
  }

  public void visitEnd () {
    text.add("cw.visitEnd();\n\n");
    // text.add("FileOutputStream os = new FileOutputStream(\"Dumped.class\");\n");
    // text.add("os.write(cw.toByteArray());\n");
    // text.add("os.close();\n");
    text.add("return cw.toByteArray();\n");
    text.add("}\n");
    text.add("}\n");
    super.visitEnd();
  }

  /**
   * Appends a string representation of the given access modifiers to {@link
   * #buf buf}.
   *
   * @param access some access modifiers.
   */

  void appendAccess (final int access) {
    boolean first = true;
    if ((access & Constants.ACC_PUBLIC) != 0) {
      buf.append("ACC_PUBLIC");
      first = false;
    }
    if ((access & Constants.ACC_PRIVATE) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_PRIVATE");
      first = false;
    }
    if ((access & Constants.ACC_PROTECTED) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_PROTECTED");
      first = false;
    }
    if ((access & Constants.ACC_FINAL) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_FINAL");
      first = false;
    }
    if ((access & Constants.ACC_STATIC) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_STATIC");
      first = false;
    }
    if ((access & Constants.ACC_SYNCHRONIZED) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      if ((access & ACCESS_CLASS) != 0) {
        buf.append("ACC_SUPER");
      } else {
        buf.append("ACC_SYNCHRONIZED");
      }
      first = false;
    }
    if ((access & Constants.ACC_VOLATILE) != 0 && (access & ACCESS_FIELD) != 0 ) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_VOLATILE");
      first = false;
    }
    if ((access & Constants.ACC_BRIDGE) != 0 &&
        (access & ACCESS_CLASS) == 0 && (access & ACCESS_FIELD) == 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_BRIDGE");
      first = false;
    }
    if ((access & Constants.ACC_VARARGS) != 0 &&
        (access & ACCESS_CLASS) == 0 && (access & ACCESS_FIELD) == 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_VARARGS");
      first = false;
    }
    if ((access & Constants.ACC_TRANSIENT) != 0 &&
        (access & ACCESS_FIELD) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_TRANSIENT");
      first = false;
    }
    if ((access & Constants.ACC_NATIVE) != 0 &&
        (access & ACCESS_CLASS) == 0 &&
        (access & ACCESS_FIELD) == 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_NATIVE");
      first = false;
    }
    if ((access & Constants.ACC_ENUM) != 0 &&
         ((access & ACCESS_CLASS) != 0 || 
          (access & ACCESS_FIELD) != 0 || 
          (access & ACCESS_INNER) != 0)) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_ENUM");
      first = false;
    }
    if ((access & Constants.ACC_ANNOTATION) != 0 &&
        ((access & ACCESS_CLASS) != 0)) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_ANNOTATION");
      first = false;
    }
    if ((access & Constants.ACC_ABSTRACT) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_ABSTRACT");
      first = false;
    }
    if ((access & Constants.ACC_INTERFACE) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_INTERFACE");
      first = false;
    }
    if ((access & Constants.ACC_STRICT) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_STRICT");
      first = false;
    }
    if ((access & Constants.ACC_SYNTHETIC) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_SYNTHETIC");
      first = false;
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_DEPRECATED");
      first = false;
    }
    if (first) {
      buf.append("0");
    }
  }

  /**
   * Appends a string representation of the given constant to the given buffer.
   *
   * @param buf a string buffer.
   * @param cst an {@link java.lang.Integer Integer}, {@link java.lang.Float
   *      Float}, {@link java.lang.Long Long}, {@link java.lang.Double Double}
   *      or {@link String String} object. May be <tt>null</tt>.
   */

  static void appendConstant (final StringBuffer buf, final Object cst) {
    if (cst == null) {
      buf.append("null");
    } else if (cst instanceof String) {
      String s = (String)cst;
      buf.append("\"");
      for (int i = 0; i < s.length(); ++i) {
        char c = s.charAt(i);
        if (c == '\n') {
          buf.append("\\n");
        } else if (c == '\r') {
            buf.append("\\r");
        } else if (c == '\\') {
          buf.append("\\\\");
        } else if (c == '"') {
          buf.append("\\\"");
        } else if( c<0x20 || c>0x7f) {
          buf.append( "\\u");
          if( c<0x10) {
            buf.append( "000");
          } else if( c<0x100) {
            buf.append( "00");
          } else if( c<0x1000) {
            buf.append( "0");
          }
          buf.append( Integer.toString( c, 16));
        } else {
          buf.append(c);
        }
      }
      buf.append("\"");
    } else if (cst instanceof Type) {
      buf.append("Type.getType(\"");
      buf.append(((Type)cst).getDescriptor());
      buf.append("\")");
    } else if (cst instanceof Integer) {
      buf.append("new Integer(")
        .append(cst)
        .append(")");
    } else if (cst instanceof Float) {
      buf.append("new Float(\"")
        .append(cst).append("\")");
    } else if (cst instanceof Long) {
      buf.append("new Long(")
        .append(cst)
        .append("L)");
    } else if (cst instanceof Double) {
      buf.append("new Double(\"")
        .append(cst).append("\")");
    }
  }
}
