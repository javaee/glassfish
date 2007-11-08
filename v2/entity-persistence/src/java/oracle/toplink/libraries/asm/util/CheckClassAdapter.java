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
import java.util.List;

import oracle.toplink.libraries.asm.ClassAdapter;
import oracle.toplink.libraries.asm.ClassReader;
import oracle.toplink.libraries.asm.ClassVisitor;
import oracle.toplink.libraries.asm.CodeVisitor;
import oracle.toplink.libraries.asm.Constants;
import oracle.toplink.libraries.asm.Attribute;
import oracle.toplink.libraries.asm.Label;
import oracle.toplink.libraries.asm.tree.AbstractInsnNode;
import oracle.toplink.libraries.asm.tree.MethodNode;
import oracle.toplink.libraries.asm.tree.TreeClassAdapter;
import oracle.toplink.libraries.asm.tree.analysis.Analyzer;
import oracle.toplink.libraries.asm.tree.analysis.SimpleVerifier;
import oracle.toplink.libraries.asm.tree.analysis.Frame;

/**
 * A {@link ClassAdapter ClassAdapter} that checks that its methods are properly
 * used. More precisely this class adapter checks each method call individually,
 * based <i>only</i> on its arguments, but does <i>not</i> check the
 * <i>sequence</i> of method calls. For example, the invalid sequence
 * <tt>visitField(ACC_PUBLIC, "i", "I", null)</tt> <tt>visitField(ACC_PUBLIC,
 * "i", "D", null)</tt> will <i>not</i> be detected by this class adapter.
 * 
 * @author Eric Bruneton
 */

public class CheckClassAdapter extends ClassAdapter {

  /**
   * <tt>true</tt> if the visit method has been called.
   */

  private boolean start;

  /**
   * <tt>true</tt> if the visitEnd method has been called.
   */

  private boolean end;

  /**
   * Checks a given class.
   * <p>
   * Usage: CheckClassAdapter
   * &lt;fully qualified class name or class file name&gt;
   *
   * @param args the command line arguments.
   *
   * @throws Exception if the class cannot be found, or if an IO exception
   *      occurs.
   */

  public static void main (final String[] args) throws Exception {
    if (args.length != 1) {
      printUsage();
    }
    ClassReader cr;
    if (args[0].endsWith(".class")) {
      cr = new ClassReader(new FileInputStream(args[0]));
    } else {
      cr = new ClassReader(args[0]);
    }
    
    TreeClassAdapter ca = new TreeClassAdapter(null);
    cr.accept(new CheckClassAdapter(ca), true);
    
    List methods = ca.classNode.methods;
    for (int i = 0; i < methods.size(); ++i) {
      MethodNode method = (MethodNode)methods.get(i);
      if (method.instructions.size() > 0) {
        Analyzer a = new Analyzer(new SimpleVerifier());
        try {
          a.analyze(ca.classNode, method);
          continue;
        } catch (Exception e) {
          e.printStackTrace();
        }
        final Frame[] frames = a.getFrames();
        
        System.out.println(method.name + method.desc);
        TraceCodeVisitor cv = new TraceCodeVisitor(null) {
          public void visitMaxs (int maxStack, int maxLocals) {
            for (int i = 0; i < text.size(); ++i) {
              String s = frames[i] == null ? "null" : frames[i].toString();
              while (s.length() < maxStack+maxLocals+1) {
                s += " ";
              }
              System.out.print(
                  Integer.toString(i + 100000).substring(1) + " " + s + " : " 
                  + text.get(i));
            }
            System.out.println();
          }
        };
        for (int j = 0; j < method.instructions.size(); ++j) {
          Object insn = method.instructions.get(j);
          if (insn instanceof AbstractInsnNode) {
            ((AbstractInsnNode)insn).accept(cv);
          } else {
            cv.visitLabel((Label)insn);
          }
        }
        cv.visitMaxs(method.maxStack, method.maxLocals);
      }
    }
  }

  private static void printUsage () {
    System.err.println("TODO.");
    System.err.println("Usage: CheckClassAdapter " +
                       "<fully qualified class name or class file name>");
    System.exit(-1);
  }

  /**
   * Constructs a new {@link CheckClassAdapter CheckClassAdapter} object.
   *
   * @param cv the class visitor to which this adapter must delegate calls.
   */

  public CheckClassAdapter (final ClassVisitor cv) {
    super(cv);
  }

  public void visit (
    final int version,
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    if (start) {
      throw new IllegalStateException("visit must be called only once");
    } else {
      start = true;
    }
    checkState();
    checkAccess(
      access,
      Constants.ACC_PUBLIC + 
      Constants.ACC_FINAL +   
      Constants.ACC_SUPER +   
      Constants.ACC_INTERFACE +   
      Constants.ACC_ABSTRACT +   
      Constants.ACC_SYNTHETIC +   
      Constants.ACC_ANNOTATION +
      Constants.ACC_ENUM +
      Constants.ACC_DEPRECATED);
    CheckCodeAdapter.checkInternalName(name, "class name");
    if (name.equals("java/lang/Object")) {
      if (superName != null) {
        throw new IllegalArgumentException(
          "The super class name of the Object class must be 'null'");
      }
    } else {
      CheckCodeAdapter.checkInternalName(superName, "super class name");
    }
    if ((access & Constants.ACC_INTERFACE) != 0) {
      if (!superName.equals("java/lang/Object")) {
        throw new IllegalArgumentException(
          "The super class name of interfaces must be 'java/lang/Object'");
      }
    }
    if (interfaces != null) {
      for (int i = 0; i < interfaces.length; ++i) {
        CheckCodeAdapter.checkInternalName(
          interfaces[i], "interface name at index " + i);
      }
    }
    cv.visit(version, access, name, superName, interfaces, sourceFile);
  }

  public void visitInnerClass (
    final String name,
    final String outerName,
    final String innerName,
    final int access)
  {
    checkState();
    CheckCodeAdapter.checkInternalName(name, "class name");
    if (outerName != null) {
      CheckCodeAdapter.checkInternalName(outerName, "outer class name");
    }
    if (innerName != null) {
      CheckCodeAdapter.checkIdentifier(innerName, "inner class name");
    }
    checkAccess(
      access, 
      Constants.ACC_PUBLIC +
      Constants.ACC_PRIVATE +
      Constants.ACC_PROTECTED +
      Constants.ACC_STATIC +
      Constants.ACC_FINAL +
      Constants.ACC_INTERFACE +
      Constants.ACC_ABSTRACT +
      Constants.ACC_SYNTHETIC +
      Constants.ACC_ANNOTATION +
      Constants.ACC_ENUM);
    cv.visitInnerClass(name, outerName, innerName, access);
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value,
    final Attribute attrs)
  {
    checkState();
    checkAccess(
      access, 
      Constants.ACC_PUBLIC +
      Constants.ACC_PRIVATE +
      Constants.ACC_PROTECTED +
      Constants.ACC_STATIC +
      Constants.ACC_FINAL +
      Constants.ACC_VOLATILE +
      Constants.ACC_TRANSIENT +
      Constants.ACC_SYNTHETIC +
      Constants.ACC_ENUM +
      Constants.ACC_DEPRECATED);
    CheckCodeAdapter.checkIdentifier(name, "field name");
    CheckCodeAdapter.checkDesc(desc, false);
    if (value != null) {
      CheckCodeAdapter.checkConstant(value);
    }
    cv.visitField(access, name, desc, value, attrs);
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions,
    final Attribute attrs)
  {
    checkState();
    checkAccess(
      access, 
      Constants.ACC_PUBLIC +
      Constants.ACC_PRIVATE +
      Constants.ACC_PROTECTED +
      Constants.ACC_STATIC +
      Constants.ACC_FINAL +
      Constants.ACC_SYNCHRONIZED +
      Constants.ACC_BRIDGE +
      Constants.ACC_VARARGS +
      Constants.ACC_NATIVE +
      Constants.ACC_ABSTRACT +
      Constants.ACC_STRICT +
      Constants.ACC_SYNTHETIC +
      Constants.ACC_DEPRECATED);
    CheckCodeAdapter.checkMethodIdentifier(name, "method name");
    CheckCodeAdapter.checkMethodDesc(desc);
    if (exceptions != null) {
      for (int i = 0; i < exceptions.length; ++i) {
        CheckCodeAdapter.checkInternalName(
          exceptions[i], "exception name at index " + i);
      }
    }
    return new CheckCodeAdapter(
      cv.visitMethod(access, name, desc, exceptions, attrs));
  }

  public void visitAttribute (final Attribute attr) {
    checkState();
    if (attr == null) {
      throw new IllegalArgumentException(
        "Invalid attribute (must not be null)");
    }
  }

  public void visitEnd () {
    checkState();
    end = true;
    cv.visitEnd();
  }

  // ---------------------------------------------------------------------------

  /**
   * Checks that the visit method has been called and that visitEnd has not been
   * called.
   */

  private void checkState () {
    if (!start) {
      throw new IllegalStateException(
        "Cannot visit member before visit has been called.");
    }
    if (end) {
      throw new IllegalStateException(
        "Cannot visit member after visitEnd has been called.");
    }
  }

  /**
   * Checks that the given access flags do not contain invalid flags. This
   * method also checks that mutually incompatible flags are not set
   * simultaneously.
   *
   * @param access the access flags to be checked
   * @param possibleAccess the valid access flags.
   */

  static void checkAccess (final int access, final int possibleAccess) {
    if ((access & ~possibleAccess) != 0) {
      throw new IllegalArgumentException("Invalid access flags: " + access);
    }
    int pub = ((access & Constants.ACC_PUBLIC) != 0 ? 1 : 0);
    int pri = ((access & Constants.ACC_PRIVATE) != 0 ? 1 : 0);
    int pro = ((access & Constants.ACC_PROTECTED) != 0 ? 1 : 0);
    if (pub + pri + pro > 1) {
      throw new IllegalArgumentException(
        "public private and protected are mutually exclusive: " + access);
    }
    int fin = ((access & Constants.ACC_FINAL) != 0 ? 1 : 0);
    int abs = ((access & Constants.ACC_ABSTRACT) != 0 ? 1 : 0);
    if (fin + abs > 1) {
      throw new IllegalArgumentException(
        "final and abstract are mutually exclusive: " + access);
    }
  }
}
