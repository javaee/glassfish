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

package oracle.toplink.libraries.asm.tree.analysis;

import java.util.ArrayList;
import java.util.List;

import oracle.toplink.libraries.asm.Constants;
import oracle.toplink.libraries.asm.Type;
import oracle.toplink.libraries.asm.tree.AbstractInsnNode;
import oracle.toplink.libraries.asm.tree.IincInsnNode;
import oracle.toplink.libraries.asm.tree.MethodInsnNode;
import oracle.toplink.libraries.asm.tree.MultiANewArrayInsnNode;
import oracle.toplink.libraries.asm.tree.VarInsnNode;

/**
 * A symbolic execution stack frame. A stack frame contains a set of local 
 * variable slots, and an operand stack. Warning: long and double values are 
 * represented by <i>two</i> slots in local variables, and by <i>one</i> slot 
 * in the operand stack.
 * 
 * @author Eric Bruneton
 */

public class Frame {
  
  /**
   * The local variables of this frame.
   */
  
  private Value[] locals;
  
  /**
   * The operand stack of this frame.
   */
  
  private Value[] stack;

  /**
   * The number of elements in the operand stack.
   */
  
  private int top;
  
  /**
   * Constructs a new frame with the given size.
   *  
   * @param nLocals the maximum number of local variables of the frame.
   * @param nStack the maximum stack size of the frame.
   */
  
  public Frame (final int nLocals, final int nStack) {
    this.locals = new Value[nLocals];
    this.stack = new Value[nStack];
  }
  
  /**
   * Constructs a new frame that is identical to the given frame.
   * 
   * @param src a frame. 
   */
  
  public Frame (final Frame src) {
    this(src.locals.length, src.stack.length);
    init(src);
  }
  
  /**
   * Copies the state of the given frame into this frame.
   * 
   * @param src a frame.
   * @return this frame.
   */
  
  public Frame init (final Frame src) {
    System.arraycopy(src.locals, 0, locals, 0, locals.length);
    System.arraycopy(src.stack, 0, stack, 0, src.top);
    top = src.top;
    return this;
  }
  
  /**
   * Returns the maximum number of local variables of this frame.
   * 
   * @return the maximum number of local variables of this frame.
   */
  
  public int getLocals () {
    return locals.length;
  }
  
  /**
   * Returns the value of the given local variable.
   * 
   * @param i a local variable index.
   * @return the value of the given local variable.
   * @throws AnalyzerException if the variable does not exist.
   */
  
  public Value getLocal (final int i) throws AnalyzerException {
    if (i >= locals.length) {
      throw new AnalyzerException("Trying to access an inexistant local variable");
    }
    return locals[i];
  }
  
  /**
   * Sets the value of the given local variable.
   * 
   * @param i a local variable index.
   * @param value the new value of this local variable.
   * @throws AnalyzerException if the variable does not exist.
   */
  
  public void setLocal (final int i, final Value value) throws AnalyzerException {
    if (i >= locals.length) {
      throw new AnalyzerException("Trying to access an inexistant local variable");
    }
    locals[i] = value;
  }

  /**
   * Returns the number of values in the operand stack of this frame. Long and
   * double values are treated as single values.
   * 
   * @return the number of values in the operand stack of this frame.
   */
  
  public int getStackSize () {
    return top;
  }
  
  /**
   * Returns the value of the given operand stack slot.
   * 
   * @param i the index of an operand stack slot.
   * @return the value of the given operand stack slot.
   * @throws AnalyzerException if the operand stack slot does not exist.
   */
  
  public Value getStack (final int i) throws AnalyzerException {
    if (i >= top) {
      throw new AnalyzerException("Trying to access an inexistant stack element");
    }
    return stack[i];
  }
  
  /**
   * Clears the operand stack of this frame.
   */
  
  public void clearStack () {
    top = 0;
  }

  /**
   * Pops a value from the operand stack of this frame.
   * 
   * @return the value that has been popped from the stack.
   * @throws AnalyzerException if the operand stack is empty.
   */
  
  public Value pop () throws AnalyzerException {
    if (top == 0) {
      throw new AnalyzerException("Cannot pop operand off an empty stack.");
    }
    return stack[--top];
  }
  
  /**
   * Pushes a value into the operand stack of this frame.
   * 
   * @param value the value that must be pushed into the stack.
   * @throws AnalyzerException if the operand stack is full.
   */
  
  public void push (final Value value) throws AnalyzerException {
    if (top >= stack.length) {
      throw new AnalyzerException("Insufficient maximum stack size.");
    }
    stack[top++] = value;
  }
  
  public void execute (
    final AbstractInsnNode insn, 
    final Interpreter interpreter) throws AnalyzerException
  {
    Value value1, value2, value3, value4;
    List values;
    int var;
    
    switch (insn.getOpcode()) {
      case Constants.NOP:
        break;
      case Constants.ACONST_NULL:
      case Constants.ICONST_M1:
      case Constants.ICONST_0:
      case Constants.ICONST_1:
      case Constants.ICONST_2:
      case Constants.ICONST_3:
      case Constants.ICONST_4:
      case Constants.ICONST_5:
      case Constants.LCONST_0:
      case Constants.LCONST_1:
      case Constants.FCONST_0:
      case Constants.FCONST_1:
      case Constants.FCONST_2:
      case Constants.DCONST_0:
      case Constants.DCONST_1:
      case Constants.BIPUSH:
      case Constants.SIPUSH:
      case Constants.LDC:
        push(interpreter.newOperation(insn));
        break;
      case Constants.ILOAD:
      case Constants.LLOAD:
      case Constants.FLOAD:
      case Constants.DLOAD:
      case Constants.ALOAD:
        push(interpreter.copyOperation(insn, getLocal(((VarInsnNode)insn).var)));
        break;
      case Constants.IALOAD:
      case Constants.LALOAD:
      case Constants.FALOAD:
      case Constants.DALOAD:
      case Constants.AALOAD:
      case Constants.BALOAD:
      case Constants.CALOAD:
      case Constants.SALOAD:
        value2 = pop();
        value1 = pop();
        push(interpreter.binaryOperation(insn, value1, value2));
        break;
      case Constants.ISTORE:
      case Constants.LSTORE:
      case Constants.FSTORE:
      case Constants.DSTORE:
      case Constants.ASTORE:
        value1 = interpreter.copyOperation(insn, pop());
        var = ((VarInsnNode)insn).var;
        setLocal(var, value1);
        if (value1.getSize() == 2) {
          setLocal(var + 1, interpreter.newValue(null));
        }
        if (var > 0 && getLocal(var - 1).getSize() == 2) {
          setLocal(var - 1, interpreter.newValue(null));
        }
        break;
      case Constants.IASTORE:
      case Constants.LASTORE:
      case Constants.FASTORE:
      case Constants.DASTORE:
      case Constants.AASTORE:
      case Constants.BASTORE:
      case Constants.CASTORE:
      case Constants.SASTORE:
        value3 = pop();
        value2 = pop();
        value1 = pop();
        interpreter.ternaryOperation(insn, value1, value2, value3);
        break;
      case Constants.POP:
        if (pop().getSize() == 2) {
          throw new AnalyzerException("Illegal use of POP");
        }
        break;
      case Constants.POP2:
        if (pop().getSize() == 1) {
          if (pop().getSize() != 1) {
            throw new AnalyzerException("Illegal use of POP2");
          }
        }
        break;
      case Constants.DUP:
        value1 = pop();
        if (value1.getSize() != 1) {
          throw new AnalyzerException("Illegal use of DUP");
        }
        push(interpreter.copyOperation(insn, value1));
        push(interpreter.copyOperation(insn, value1));
        break;
      case Constants.DUP_X1:
        value1 = pop();
        value2 = pop();
        if (value1.getSize() != 1 || value2.getSize() != 1) {
          throw new AnalyzerException("Illegal use of DUP_X1");
        }
        push(interpreter.copyOperation(insn, value1));
        push(interpreter.copyOperation(insn, value2));
        push(interpreter.copyOperation(insn, value1));
        break;
      case Constants.DUP_X2:
        value1 = pop();
        if (value1.getSize() == 1) {
          value2 = pop();
          if (value2.getSize() == 1) {
            value3 = pop();
            if (value3.getSize() == 1) {
              push(interpreter.copyOperation(insn, value1));
              push(interpreter.copyOperation(insn, value3));
              push(interpreter.copyOperation(insn, value2));
              push(interpreter.copyOperation(insn, value1));
              break;
            }
          } else {
            push(interpreter.copyOperation(insn, value1));
            push(interpreter.copyOperation(insn, value2));
            push(interpreter.copyOperation(insn, value1));
            break;
          }
        }
        throw new AnalyzerException("Illegal use of DUP_X2");
      case Constants.DUP2:
        value1 = pop();
        if (value1.getSize() == 1) {
          value2 = pop();
          if (value2.getSize() == 1) {
            push(interpreter.copyOperation(insn, value2));
            push(interpreter.copyOperation(insn, value1));
            push(interpreter.copyOperation(insn, value2));
            push(interpreter.copyOperation(insn, value1));
            break;
          }
        } else {
          push(interpreter.copyOperation(insn, value1));
          push(interpreter.copyOperation(insn, value1));
          break;
        }
        throw new AnalyzerException("Illegal use of DUP2");
      case Constants.DUP2_X1:
        value1 = pop();
        if (value1.getSize() == 1) {
          value2 = pop();
          if (value2.getSize() == 1) {
            value3 = pop();
            if (value3.getSize() == 1) {
              push(interpreter.copyOperation(insn, value2));
              push(interpreter.copyOperation(insn, value1));
              push(interpreter.copyOperation(insn, value3));
              push(interpreter.copyOperation(insn, value2));
              push(interpreter.copyOperation(insn, value1));
              break;
            }
          }
        } else {
          value2 = pop();
          if (value2.getSize() == 1) {
            push(interpreter.copyOperation(insn, value1));
            push(interpreter.copyOperation(insn, value2));
            push(interpreter.copyOperation(insn, value1));
            break;
          }
        }
        throw new AnalyzerException("Illegal use of DUP2_X1");
      case Constants.DUP2_X2:
        value1 = pop();
        if (value1.getSize() == 1) {
          value2 = pop();
          if (value2.getSize() == 1) {
            value3 = pop();
            if (value3.getSize() == 1) {
              value4 = pop();
              if (value4.getSize() == 1) {
                push(interpreter.copyOperation(insn, value2));
                push(interpreter.copyOperation(insn, value1));
                push(interpreter.copyOperation(insn, value4));
                push(interpreter.copyOperation(insn, value3));
                push(interpreter.copyOperation(insn, value2));
                push(interpreter.copyOperation(insn, value1));
                break;
              }
            } else {
              push(interpreter.copyOperation(insn, value2));
              push(interpreter.copyOperation(insn, value1));
              push(interpreter.copyOperation(insn, value3));
              push(interpreter.copyOperation(insn, value2));
              push(interpreter.copyOperation(insn, value1));
              break;
            }
          }
        } else {
          value2 = pop();
          if (value2.getSize() == 1) {
            value3 = pop();
            if (value3.getSize() == 1) {
              push(interpreter.copyOperation(insn, value1));
              push(interpreter.copyOperation(insn, value3));
              push(interpreter.copyOperation(insn, value2));
              push(interpreter.copyOperation(insn, value1));
              break;
            }
          } else {
            push(interpreter.copyOperation(insn, value1));
            push(interpreter.copyOperation(insn, value2));
            push(interpreter.copyOperation(insn, value1));
            break;
          }
        }
        throw new AnalyzerException("Illegal use of DUP2_X2");
      case Constants.SWAP:
        value2 = pop();
        value1 = pop();
        if (value1.getSize() != 1 || value2.getSize() != 1) {
          throw new AnalyzerException("Illegal use of SWAP");
        }
        push(interpreter.copyOperation(insn, value2));
        push(interpreter.copyOperation(insn, value1));
        break;
      case Constants.IADD:
      case Constants.LADD:
      case Constants.FADD:
      case Constants.DADD:
      case Constants.ISUB:
      case Constants.LSUB:
      case Constants.FSUB:
      case Constants.DSUB:
      case Constants.IMUL:
      case Constants.LMUL:
      case Constants.FMUL:
      case Constants.DMUL:
      case Constants.IDIV:
      case Constants.LDIV:
      case Constants.FDIV:
      case Constants.DDIV:
      case Constants.IREM:
      case Constants.LREM:
      case Constants.FREM:
      case Constants.DREM:
        value2 = pop();
        value1 = pop();
        push(interpreter.binaryOperation(insn, value1, value2));
        break;
      case Constants.INEG:
      case Constants.LNEG:
      case Constants.FNEG:
      case Constants.DNEG:
        push(interpreter.unaryOperation(insn, pop()));
        break;
      case Constants.ISHL:
      case Constants.LSHL:
      case Constants.ISHR:
      case Constants.LSHR:
      case Constants.IUSHR:
      case Constants.LUSHR:
      case Constants.IAND:
      case Constants.LAND:
      case Constants.IOR:
      case Constants.LOR:
      case Constants.IXOR:
      case Constants.LXOR:
        value2 = pop();
        value1 = pop();
        push(interpreter.binaryOperation(insn, value1, value2));
        break;
      case Constants.IINC:
        var = ((IincInsnNode)insn).var;
        setLocal(var, interpreter.unaryOperation(insn, getLocal(var)));
        break;
      case Constants.I2L:
      case Constants.I2F:
      case Constants.I2D:
      case Constants.L2I:
      case Constants.L2F:
      case Constants.L2D:
      case Constants.F2I:
      case Constants.F2L:
      case Constants.F2D:
      case Constants.D2I:
      case Constants.D2L:
      case Constants.D2F:
      case Constants.I2B:
      case Constants.I2C:
      case Constants.I2S:
        push(interpreter.unaryOperation(insn, pop()));
        break;
      case Constants.LCMP:
      case Constants.FCMPL:
      case Constants.FCMPG:
      case Constants.DCMPL:
      case Constants.DCMPG:
        value2 = pop();
        value1 = pop();
        push(interpreter.binaryOperation(insn, value1, value2));
        break;
      case Constants.IFEQ:
      case Constants.IFNE:
      case Constants.IFLT:
      case Constants.IFGE:
      case Constants.IFGT:
      case Constants.IFLE:
        interpreter.unaryOperation(insn, pop());
        break;
      case Constants.IF_ICMPEQ:
      case Constants.IF_ICMPNE:
      case Constants.IF_ICMPLT:
      case Constants.IF_ICMPGE:
      case Constants.IF_ICMPGT:
      case Constants.IF_ICMPLE:
      case Constants.IF_ACMPEQ:
      case Constants.IF_ACMPNE:
        value2 = pop();
        value1 = pop();
        interpreter.binaryOperation(insn, value1, value2);
        break;
      case Constants.GOTO:
        break;
      case Constants.JSR:
        push(interpreter.newOperation(insn));
        break;
      case Constants.RET:
        break;
      case Constants.TABLESWITCH:
      case Constants.LOOKUPSWITCH:
      case Constants.IRETURN:
      case Constants.LRETURN:
      case Constants.FRETURN:
      case Constants.DRETURN:
      case Constants.ARETURN:
        interpreter.unaryOperation(insn, pop());
        break;
      case Constants.RETURN:
        break;
      case Constants.GETSTATIC:
        push(interpreter.newOperation(insn));
        break;
      case Constants.PUTSTATIC:
        interpreter.unaryOperation(insn, pop());
        break;
      case Constants.GETFIELD:
        push(interpreter.unaryOperation(insn, pop()));
        break;
      case Constants.PUTFIELD:
        value2 = pop();
        value1 = pop();
        interpreter.binaryOperation(insn, value1, value2);
        break;
      case Constants.INVOKEVIRTUAL:
      case Constants.INVOKESPECIAL:
      case Constants.INVOKESTATIC:
      case Constants.INVOKEINTERFACE:
        values = new ArrayList();
        String desc = ((MethodInsnNode)insn).desc;
        for (int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
          values.add(0, pop());
        }
        if (insn.getOpcode() != Constants.INVOKESTATIC) {
          values.add(0, pop());
        }
        if (Type.getReturnType(desc) == Type.VOID_TYPE) {
          interpreter.naryOperation(insn, values);
        } else {
          push(interpreter.naryOperation(insn, values));
        }
        break;
      case Constants.NEW:
        push(interpreter.newOperation(insn));
        break;
      case Constants.NEWARRAY:
      case Constants.ANEWARRAY:
      case Constants.ARRAYLENGTH:
        push(interpreter.unaryOperation(insn, pop()));
        break;
      case Constants.ATHROW:
        interpreter.unaryOperation(insn, pop());
        break;
      case Constants.CHECKCAST:
      case Constants.INSTANCEOF:
        push(interpreter.unaryOperation(insn, pop()));
        break;
      case Constants.MONITORENTER:
      case Constants.MONITOREXIT:
        interpreter.unaryOperation(insn, pop());
        break;
      case Constants.MULTIANEWARRAY:
        values = new ArrayList();
        for (int i = ((MultiANewArrayInsnNode)insn).dims; i > 0; --i) {
          values.add(0, pop());
        }
        push(interpreter.naryOperation(insn, values));
        break;
      case Constants.IFNULL:
      case Constants.IFNONNULL:
        interpreter.unaryOperation(insn, pop());
        break;
      default:
        throw new RuntimeException("Illegal opcode");
    }
  }

  /**
   * Merges this frame with the given frame.
   *  
   * @param frame a frame.
   * @param interpreter the interpreter used to merge values.
   * @return <tt>true</tt> if this frame has been changed as a result of the
   *      merge operation, or <tt>false</tt> otherwise.
   * @throws AnalyzerException if the frames have incompatible sizes.
   */
  
  public boolean merge (final Frame frame, final Interpreter interpreter) 
    throws AnalyzerException 
  {
    if (top != frame.top) {
      throw new AnalyzerException("Incompatible stack heights");
    }
    boolean changes = false;
    for (int i = 0; i < locals.length; ++i) {
      Value v = interpreter.merge(locals[i], frame.locals[i]);
      if (v != locals[i]) {
        locals[i] = v;
        changes |= true;
      }
    }
    for (int i = 0; i < top; ++i) {
      Value v = interpreter.merge(stack[i], frame.stack[i]);
      if (v != stack[i]) {
        stack[i] = v;
        changes |= true;
      }
    }
    return changes;
  }
  
  /**
   * Merges this frame with the given frame (case of a RET instruction).

   * @param frame a frame
   * @param access the local variables that have been accessed by the 
   *     subroutine to which the RET instruction corresponds.
   * @return <tt>true</tt> if this frame has been changed as a result of the
   *      merge operation, or <tt>false</tt> otherwise.
   */
  
  public boolean merge (final Frame frame, final boolean[] access) {
    boolean changes = false;
    for (int i = 0; i < locals.length; ++i) {
      if (!access[i] && !locals[i].equals(frame.locals[i])) {
        locals[i] = frame.locals[i];
        changes = true;
      }
    }
    return changes;
  }
  
  /**
   * Returns a string representation of this frame.
   * 
   * @return a string representation of this frame.
   */
  
  public String toString () {
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < locals.length; ++i) {
      b.append(locals[i]);
    }
    b.append(' ');
    for (int i = 0; i < top; ++i) {
      b.append(stack[i].toString());
    }
    return b.toString();
  }
}
