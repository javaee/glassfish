/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */


package com.sun.org.apache.jdo.impl.enhancer.classfile;


import java.io.PrintStream;
import java.util.Stack;

/**
 * An instruction which requires a single constant from the constant
 * pool as an immediate operand 
 */
public class InsnConstOp extends Insn {
    /* The constant from the constant pool */
    private ConstBasic constValue;

    /* public accessors */

    public int nStackArgs() {
        int n = VMOp.ops[opcode()].nStackArgs();
        if (n >= 0) 
            return n;
        switch (opcode()) {
        case opc_putstatic:
        case opc_putfield:
        {
            ConstFieldRef fld = (ConstFieldRef) constValue;
            String sig = fld.nameAndType().signature().asString();
            if (sig.equals("J") || sig.equals("D"))
                return (opcode() == opc_putfield) ? 3 : 2;
            return (opcode() == opc_putfield) ? 2 : 1;
        }
        case opc_invokevirtual:
        case opc_invokespecial:
        case opc_invokestatic:
            /* handle interface invoke too */
        case opc_invokeinterface:
        {
            ConstBasicMemberRef meth = (ConstBasicMemberRef) constValue;
            String sig = meth.nameAndType().signature().asString();
            int nMethodArgWords = Descriptor.countMethodArgWords(sig);
            return nMethodArgWords +
                ((opcode() == opc_invokestatic) ? 0 : 1);
        }
        default:
            throw new InsnError("unexpected variable opcode");
        }
    }

    public int nStackResults() {
        int n = VMOp.ops[opcode()].nStackResults();
        if (n >= 0) 
            return n;
        switch (opcode()) {
        case opc_getstatic:
        case opc_getfield:
        {
            ConstFieldRef fld = (ConstFieldRef) constValue;
            String sig = fld.nameAndType().signature().asString();
            if (sig.equals("J") || sig.equals("D"))
                return 2;
            return 1;
        }
        case opc_invokevirtual:
        case opc_invokespecial:
        case opc_invokestatic:
            /* handle interface invoke too */
        case opc_invokeinterface:
        {
            ConstBasicMemberRef meth = (ConstBasicMemberRef) constValue;
            return Descriptor.countMethodReturnWords(
                meth.nameAndType().signature().asString());
        }
        default:
            throw new InsnError("unexpected variable opcode");
        }
    }

    public String argTypes() {
        switch (opcode()) {
        case opc_putstatic:
        case opc_putfield:
        {
            ConstFieldRef fld = (ConstFieldRef) constValue;
            String sig = fld.nameAndType().signature().asString();
            if (opcode() == opc_putstatic)
                return sig;
            else
                return descriptorTypeOfObject(fld) + sig;
        }
        case opc_invokevirtual:
        case opc_invokespecial:
        case opc_invokestatic:
            /* handle interface invoke too */
        case opc_invokeinterface:
        {
            ConstBasicMemberRef meth = (ConstBasicMemberRef) constValue;
            String argSig =
                Descriptor.extractArgSig(meth.nameAndType().signature().asString());
            if (opcode() == opc_invokestatic)
                return argSig;
            else
                return descriptorTypeOfObject(meth) + argSig;
        }
        default:
            return VMOp.ops[opcode()].argTypes();
        }
    }

    public String resultTypes() {
        switch (opcode()) {
        case opc_invokevirtual:
        case opc_invokespecial:
        case opc_invokestatic:
            /* handle interface invoke too */
        case opc_invokeinterface:
        {
            ConstBasicMemberRef meth = (ConstBasicMemberRef) constValue;
            String resultSig = Descriptor.extractResultSig(
                meth.nameAndType().signature().asString());
            if (resultSig.equals("V"))
                return "";
            return resultSig;
        }
        case opc_getstatic:
        case opc_getfield:
        {
            ConstFieldRef fld = (ConstFieldRef) constValue;
            return fld.nameAndType().signature().asString();
        }
        case opc_ldc:
        case opc_ldc_w:
        case opc_ldc2_w:
        {
            ConstValue constVal = (ConstValue) constValue;
            return constVal.descriptor();
        }
        default:
            return VMOp.ops[opcode()].resultTypes();
        }
    }

    public boolean branches() {
        /* invokes don't count as a branch */
        return false;
    }

    /**
     * Return the constant pool entry which is the immediate operand
     */
    public ConstBasic value() {
        return constValue;
    }
    
    /**
     * Modify the referenced constant
     */
    public void setValue(ConstBasic newValue) {
        checkConstant(newValue);
        constValue = newValue;
    }
    
    /**
     * Compares this instance with another for structural equality.
     */
    //@olsen: added method
    public boolean isEqual(Stack msg, Object obj) {
        if (!(obj instanceof InsnConstOp)) {
            msg.push("obj/obj.getClass() = "
                     + (obj == null ? null : obj.getClass()));
            msg.push("this.getClass() = "
                     + this.getClass());
            return false;
        }
        InsnConstOp other = (InsnConstOp)obj;

        if (!super.isEqual(msg, other)) {
            return false;
        }
        
        if (!this.constValue.isEqual(msg, other.constValue)) {
            msg.push(String.valueOf("constValue = "
                                    + other.constValue));
            msg.push(String.valueOf("constValue = "
                                    + this.constValue));
            return false;
        }
        return true;
    }

    /* package local methods */

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  " + opName(opcode()) + "  pool(" + 
                    constValue.getIndex() + ")");
    }

    int store(byte[] buf, int index) {
        if (opcode() == opc_ldc && !isNarrowldc())
            buf[index++] = (byte) opc_ldc_w;
        else
            buf[index++] = (byte) opcode();
        int constIndex = constValue.getIndex();
        if (size() == 3)
            buf[index++] = (byte) (constIndex >> 8);
        buf[index++] = (byte)(constIndex & 0xff);
        return index;
    }

    int size() {
        return isNarrowldc() ? 2 : 3;
    }

    private boolean isNarrowldc() {
        return (opcode() == opc_ldc && constValue.getIndex() < 256);
    }
    

    InsnConstOp(int theOpcode, ConstBasic theOperand) {
        this(theOpcode, theOperand, NO_OFFSET);
    }

    InsnConstOp(int theOpcode, ConstBasic theOperand, int pc) {
        super(theOpcode, pc);
        constValue = theOperand;
        checkConstant(theOperand);
        if (theOpcode == opc_invokeinterface) 
            throw new InsnError("attempt to create an " + opName(theOpcode) +
                                " as an InsnConstOp instead of InsnInterfaceInvoke");
    }

    /* used only by InsnInterfaceInvoke, to make sure that opc_invokeinterface cannot
     * come through the wrong path and miss its extra nArgsOp */
    InsnConstOp(int theOpcode, ConstInterfaceMethodRef theOperand, int pc) {
        super(theOpcode, pc);
        constValue = theOperand;
        checkConstant(theOperand);
    }

    private void checkConstant(ConstBasic operand) {
        switch(opcode()) {
        case opc_ldc:
        case opc_ldc_w:
        case opc_ldc2_w:
            /* ConstValue */
            if (operand == null ||
                (! (operand instanceof ConstValue)))
                throw new InsnError ("attempt to create an " + opName(opcode()) +
                                     " without a ConstValue operand");
            break;

        case opc_getstatic:
        case opc_putstatic:
        case opc_getfield:
        case opc_putfield:
            /* ConstFieldRef */
            if (operand == null ||
                (! (operand instanceof ConstFieldRef)))
                throw new InsnError ("attempt to create an " + opName(opcode()) +
                                     " without a ConstFieldRef operand");
            break;

        case opc_invokevirtual:
        case opc_invokespecial:
        case opc_invokestatic:
            /* ConstMethodRef */
            if (operand == null ||
                (! (operand instanceof ConstMethodRef)))
                throw new InsnError ("attempt to create an " + opName(opcode()) +
                                     " without a ConstMethodRef operand");
            break;
      
        case opc_invokeinterface:
            /* ConstInterfaceMethodRef */
            if (operand == null ||
                (! (operand instanceof ConstInterfaceMethodRef)))
                throw new InsnError("Attempt to create an " + opName(opcode()) +
                                    " without a ConstInterfaceMethodRef operand");
            break;

        case opc_new:
        case opc_anewarray:
        case opc_checkcast:
        case opc_instanceof:
            /* ConstClass */
            if (operand == null ||
                (! (operand instanceof ConstClass)))
                throw new InsnError ("attempt to create an " + opName(opcode()) +
                                     " without a ConstClass operand");
            break;

        default:
            throw new InsnError ("attempt to create an " + opName(opcode()) +
                                 " with a constant operand");
        }
    }

    private final String descriptorTypeOfObject(ConstBasicMemberRef memRef) {
        String cname = memRef.className().className().asString();
        return "L" + cname + ";";
    }
}
