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

/**
 * InsnUtils provides a set of static methods which serve to 
 * select vm instructions during code annotation.
 */
public
class InsnUtils implements VMConstants {

    /**
     * Return the best instruction for loading a value from the constant
     * pool onto the stack - hopefully use short form
     */
    //@olsen: added method
    public static Insn constantValue(ConstValue value) {
        int tag = value.tag();
        switch (tag) {
        case CONSTANTInteger :
        case CONSTANTFloat :
        case CONSTANTString :
            //@olsen: check index range to select opc_ldc vs. opc_ldc_w
            int opcode = (value.getIndex() <= 0xFF ? opc_ldc : opc_ldc_w);
            return Insn.create(opcode, value);
        case CONSTANTLong :
        case CONSTANTDouble :
            //@olsen: require opc_ldc2_w (there's no short form: opc_ldc2)
            return Insn.create(opc_ldc2_w, value);
        default:
            throw new InsnError("bad constant tag");
        }
    }
    
    /**
     * Return the best instruction for loading the specified String
     * constant onto the stack - hopefully use short form
     */
    //@olsen: added method
    public static Insn stringConstant(String s, ConstantPool pool) {
        //@olsen: need to carefully select opc_ldc/opc_ldc_w/opc_ldc2_w
        return constantValue(pool.addString(s));
    }

    /**
     * Return the best instruction for loading the specified integer 
     * constant onto the stack - hopefully use short form
     */
    public static Insn integerConstant(int i, ConstantPool pool) {
        //@olsen: removed redundant 'else's
        //@olsen: added use of opc_iconst_m1
        if (i == -1)
            return Insn.create(opc_iconst_m1);
        if (i == 0)
            return Insn.create(opc_iconst_0);
        if (i == 1)
            return Insn.create(opc_iconst_1);
        if (i == 2)
            return Insn.create(opc_iconst_2);
        if (i == 3)
            return Insn.create(opc_iconst_3);
        if (i == 4)
            return Insn.create(opc_iconst_4);
        if (i == 5)
            return Insn.create(opc_iconst_5);
        if (i >= -128 && i < 128)
            return Insn.create(opc_bipush, i);
        //@olsen: added use of opc_sipush
        if (i >= -32768 && i < 32768)
            return Insn.create(opc_sipush, i);
        //@olsen: need to carefully select opc_ldc/opc_ldc_w/opc_ldc2_w
        //return Insn.create(opc_ldc, pool.addInteger(i));
        return constantValue(pool.addInteger(i));
    }

    /**
     * Return the best instruction for loading the specified long constant onto
     * the stack.
     */
    public static Insn longConstant(long l, ConstantPool pool) {
        //@olsen: removed redundant 'else's
        if (l == 0)
            return Insn.create(opc_lconst_0);
        if (l == 1)
            return Insn.create(opc_lconst_1);
        //@olsen: need to carefully select opc_ldc/opc_ldc_w/opc_ldc2_w
        //return Insn.create(opc_ldc2_w, pool.addLong(l));
        return constantValue(pool.addLong(l));
    }

    /**
     * Return the best instruction for loading the specified float constant onto
     * the stack.
     */
    public static Insn floatConstant(float f, ConstantPool pool) {
        //@olsen: removed redundant 'else's
        if (f == 0)
            return Insn.create(opc_fconst_0);
        if (f == 1)
            return Insn.create(opc_fconst_1);
        if (f == 2)
            return Insn.create(opc_fconst_2);
        //@olsen: need to carefully select opc_ldc/opc_ldc_w/opc_ldc2_w
        //return Insn.create(opc_ldc, pool.addFloat(f));
        return constantValue(pool.addFloat(f));
    }

    /**
     * Return the best instruction for loading the specified double constant onto
     * the stack.
     */
    public static Insn doubleConstant(double d, ConstantPool pool) {
        //@olsen: removed redundant 'else's
        if (d == 0)
            return Insn.create(opc_dconst_0);
        if (d == 1)
            return Insn.create(opc_dconst_1);
        //@olsen: need to carefully select opc_ldc/opc_ldc_w/opc_ldc2_w
        //return Insn.create(opc_ldc2_w, pool.addDouble(d));
        return constantValue(pool.addDouble(d));
    }

    /**
     * Return the best instruction for storing a reference to a local
     * variable slot
     */
    public static Insn aStore(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_astore_0);
        else if (i == 1)
            return Insn.create(opc_astore_1);
        else if (i == 2)
            return Insn.create(opc_astore_2);
        else if (i == 3)
            return Insn.create(opc_astore_3);
        return Insn.create(opc_astore, i);
    }

    /**
     * Return the best instruction for storing an int to a local
     * variable slot
     */
    public static Insn iStore(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_istore_0);
        else if (i == 1)
            return Insn.create(opc_istore_1);
        else if (i == 2)
            return Insn.create(opc_istore_2);
        else if (i == 3)
            return Insn.create(opc_istore_3);
        return Insn.create(opc_istore, i);
    }

    /**
     * Return the best instruction for storing a float to a local
     * variable slot
     */
    public static Insn fStore(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_fstore_0);
        else if (i == 1)
            return Insn.create(opc_fstore_1);
        else if (i == 2)
            return Insn.create(opc_fstore_2);
        else if (i == 3)
            return Insn.create(opc_fstore_3);
        return Insn.create(opc_fstore, i);
    }

    /**
     * Return the best instruction for storing a long to a local
     * variable slot
     */
    public static Insn lStore(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_lstore_0);
        else if (i == 1)
            return Insn.create(opc_lstore_1);
        else if (i == 2)
            return Insn.create(opc_lstore_2);
        else if (i == 3)
            return Insn.create(opc_lstore_3);
        return Insn.create(opc_lstore, i);
    }

    /**
     * Return the best instruction for storing a double to a local
     * variable slot
     */
    public static Insn dStore(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_dstore_0);
        else if (i == 1)
            return Insn.create(opc_dstore_1);
        else if (i == 2)
            return Insn.create(opc_dstore_2);
        else if (i == 3)
            return Insn.create(opc_dstore_3);
        return Insn.create(opc_dstore, i);
    }

    /**
     * Return the best instruction for loading a reference from a local
     * variable slot
     */
    public static Insn aLoad(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_aload_0);
        else if (i == 1)
            return Insn.create(opc_aload_1);
        else if (i == 2)
            return Insn.create(opc_aload_2);
        else if (i == 3)
            return Insn.create(opc_aload_3);
        return Insn.create(opc_aload, i);
    }

    /**
     * Return the best instruction for loading an int from a local
     * variable slot
     */
    public static Insn iLoad(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_iload_0);
        else if (i == 1)
            return Insn.create(opc_iload_1);
        else if (i == 2)
            return Insn.create(opc_iload_2);
        else if (i == 3)
            return Insn.create(opc_iload_3);
        return Insn.create(opc_iload, i);
    }

    /**
     * Return the best instruction for loading a float from a local
     * variable slot
     */
    public static Insn fLoad(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_fload_0);
        else if (i == 1)
            return Insn.create(opc_fload_1);
        else if (i == 2)
            return Insn.create(opc_fload_2);
        else if (i == 3)
            return Insn.create(opc_fload_3);
        return Insn.create(opc_fload, i);
    }

    /**
     * Return the best instruction for loading a long from a local
     * variable slot
     */
    public static Insn lLoad(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_lload_0);
        else if (i == 1)
            return Insn.create(opc_lload_1);
        else if (i == 2)
            return Insn.create(opc_lload_2);
        else if (i == 3)
            return Insn.create(opc_lload_3);
        return Insn.create(opc_lload, i);
    }

    /**
     * Return the best instruction for loading a double from a local
     * variable slot
     */
    public static Insn dLoad(int i, ConstantPool pool) {
        if (i == 0)
            return Insn.create(opc_dload_0);
        else if (i == 1)
            return Insn.create(opc_dload_1);
        else if (i == 2)
            return Insn.create(opc_dload_2);
        else if (i == 3)
            return Insn.create(opc_dload_3);
        return Insn.create(opc_dload, i);
    }

    /**
     * Return the best instruction for loading a value from a local
     * variable slot
     */
    public static Insn load(int tp, int i, ConstantPool pool) {
        switch(tp) {
            //@olsen: added these cases:
        case T_BOOLEAN:
        case T_CHAR:
        case T_BYTE:
        case T_SHORT:
            //@olsen: end added cases
        case T_INT:
            return iLoad(i, pool);
        case T_FLOAT:
            return fLoad(i, pool);
        case T_DOUBLE:
            return dLoad(i, pool);
        case T_LONG:
            return lLoad(i, pool);
        case TC_OBJECT:
            return aLoad(i, pool);
        default:
            throw new InsnError("bad load type");
        }
    }

    /**
     * Return the best instruction for storing a value to a local
     * variable slot
     */
    public static Insn store(int tp, int i, ConstantPool pool) {
        switch(tp) {
            //@olsen: added these cases:
        case T_BOOLEAN:
        case T_CHAR:
        case T_BYTE:
        case T_SHORT:
            //@olsen: end added cases
        case T_INT:
            return iStore(i, pool);
        case T_FLOAT:
            return fStore(i, pool);
        case T_DOUBLE:
            return dStore(i, pool);
        case T_LONG:
            return lStore(i, pool);
        case TC_OBJECT:
            return aStore(i, pool);
        default:
            throw new InsnError("bad store type");
        }
    }
}
