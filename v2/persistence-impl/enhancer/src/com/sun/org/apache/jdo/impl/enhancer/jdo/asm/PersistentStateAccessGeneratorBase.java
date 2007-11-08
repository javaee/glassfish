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

package com.sun.org.apache.jdo.impl.enhancer.jdo.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.JDOConstants;

/**
 * A Base class that provides methods to access the persistent state of the
 * bean. The persistent state of the bean can be accessed either directly (FIELD
 * based persistence) OR through getter/setter (PROPERTY based persistence)
 * 
 * @see com.sun.org.apache.jdo.impl.enhancer.jdo.asm.FieldAccessMediator
 * @see com.sun.org.apache.jdo.impl.enhancer.jdo.asm.PropertyBasedStateAccessGenerator
 * @author Mahesh Kannan
 */
abstract class PersistentStateAccessGeneratorBase
        implements PersistentStateAccessGenerator, JDOConstants, Opcodes
{
    protected String ownerClassName;

    protected PersistentStateAccessGeneratorBase(String ownerClassName) {
        this.ownerClassName = ownerClassName;
    }

    public final void generateInstructionsToReadState(MethodVisitor mv,
            FieldInfo fieldInfo)
    {
        generateInstructionsToReadState(mv, fieldInfo.getName(), fieldInfo
                .getDescriptor());
    }

    public final void generateInstructionsToWriteState(MethodVisitor mv,
            FieldInfo fieldInfo)
    {
        generateInstructionsToWriteState(mv, fieldInfo.getName(), fieldInfo
                .getDescriptor());
    }

    public final void appendDirectWriteReturn(MethodVisitor mv,
            FieldInfo fieldInfo)
    {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(
                BuilderHelper.getALoadOpcode(fieldInfo.getDescriptor()), 1);
        generateInstructionsToWriteState(mv, fieldInfo);
        mv.visitInsn(RETURN);
    }

    public final void appendDirectReadReturn(MethodVisitor mv,
            FieldInfo fieldInfo)
    {
        mv.visitVarInsn(ALOAD, 0);
        generateInstructionsToReadState(mv, fieldInfo);
        mv.visitInsn(BuilderHelper.getReturnOpcode(fieldInfo.getDescriptor()));
    }
}
