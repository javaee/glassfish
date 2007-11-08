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

/**
 * This class is responsible for generating instructions that access persistent
 * fields directly. This is used for FIELD based persistence.
 * 
 * @see @see com.sun.org.apache.jdo.impl.enhancer.jdo.asm.PropertyBasedStateAccessGenerator
 * @author Mahesh Kannan
 */
public class FieldBasedStateAccessGenerator
    extends PersistentStateAccessGeneratorBase
{
    FieldBasedStateAccessGenerator(String ownerClassName) {
        super(ownerClassName);
    }

    public void generateInstructionsToReadState(MethodVisitor mv,
            String fieldName, String fieldType)
    {
        mv.visitFieldInsn(GETFIELD, ownerClassName, fieldName, fieldType);
    }

    public void generateInstructionsToWriteState(MethodVisitor mv,
            String fieldName, String fieldType)
    {
        mv.visitFieldInsn(PUTFIELD, ownerClassName, fieldName, fieldType);

    }
}
