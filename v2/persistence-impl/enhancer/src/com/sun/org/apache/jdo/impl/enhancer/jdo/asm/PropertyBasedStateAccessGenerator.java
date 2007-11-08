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

import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.JDOConstants;

/**
 * This class is responsible for generating instructions that access persistent
 * fields using getter/setter. This is used for PROPERTY based persistence.
 * 
 * @see com.sun.org.apache.jdo.impl.enhancer.jdo.asm.FieldBasedStateAccessGenerator
 * @author Mahesh Kannan
 */
public class PropertyBasedStateAccessGenerator
    extends PersistentStateAccessGeneratorBase
{

    PropertyBasedStateAccessGenerator(String ownerClassName) {
        super(ownerClassName);
    }
    
    public void generateInstructionsToReadState(MethodVisitor mv, String fieldName, String fieldType) {
        mv.visitMethodInsn(INVOKEVIRTUAL, ownerClassName, JDOConstants.ORIG_GETTER_PREFIX
                + initUpperCase(fieldName), "()" + fieldType);
    }

    public void generateInstructionsToWriteState(MethodVisitor mv, String fieldName, String fieldType) {
        mv.visitMethodInsn(INVOKEVIRTUAL, ownerClassName, JDOConstants.ORIG_SETTER_PREFIX
                + initUpperCase(fieldName), "(" + fieldType + ")V");
    }
    
    protected static String initUpperCase(String str) {
        return (str.length() == 1) 
        ? "" + Character.toUpperCase(str.charAt(0))
        : Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
