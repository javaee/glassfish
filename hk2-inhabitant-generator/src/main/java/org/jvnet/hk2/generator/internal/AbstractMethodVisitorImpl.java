/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.jvnet.hk2.generator.internal;

import org.glassfish.hk2.external.org.objectweb.asm.AnnotationVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.Attribute;
import org.glassfish.hk2.external.org.objectweb.asm.Label;
import org.glassfish.hk2.external.org.objectweb.asm.MethodVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.Opcodes;

/**
 * This in only here to keep the main-line code less messy
 * 
 * @author jwells
 *
 */
public abstract class AbstractMethodVisitorImpl extends MethodVisitor {
    /**
     * The constructor that gives the implemented version to the superclass
     */
    public AbstractMethodVisitorImpl() {
        super(Opcodes.ASM6);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitAnnotationDefault()
     */
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitAttribute(org.objectweb.asm.Attribute)
     */
    @Override
    public void visitAttribute(Attribute arg0) {

    }
    
    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitCode()
     */
    @Override
    public void visitCode() {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitFrame(int, int, java.lang.Object[], int, java.lang.Object[])
     */
    @Override
    public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3,
            Object[] arg4) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitIincInsn(int, int)
     */
    @Override
    public void visitIincInsn(int arg0, int arg1) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitInsn(int)
     */
    @Override
    public void visitInsn(int arg0) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitIntInsn(int, int)
     */
    @Override
    public void visitIntInsn(int arg0, int arg1) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitJumpInsn(int, org.objectweb.asm.Label)
     */
    @Override
    public void visitJumpInsn(int arg0, Label arg1) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitLabel(org.objectweb.asm.Label)
     */
    @Override
    public void visitLabel(Label arg0) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitLdcInsn(java.lang.Object)
     */
    @Override
    public void visitLdcInsn(Object arg0) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitLineNumber(int, org.objectweb.asm.Label)
     */
    @Override
    public void visitLineNumber(int arg0, Label arg1) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitLocalVariable(java.lang.String, java.lang.String, java.lang.String, org.objectweb.asm.Label, org.objectweb.asm.Label, int)
     */
    @Override
    public void visitLocalVariable(String arg0, String arg1, String arg2,
            Label arg3, Label arg4, int arg5) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitLookupSwitchInsn(org.objectweb.asm.Label, int[], org.objectweb.asm.Label[])
     */
    @Override
    public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitMaxs(int, int)
     */
    @Override
    public void visitMaxs(int arg0, int arg1) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitMultiANewArrayInsn(java.lang.String, int)
     */
    @Override
    public void visitMultiANewArrayInsn(String arg0, int arg1) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitParameterAnnotation(int, java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1,
            boolean arg2) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitTableSwitchInsn(int, int, org.objectweb.asm.Label, org.objectweb.asm.Label[])
     */
    @Override
    public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
            Label[] arg3) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitTryCatchBlock(org.objectweb.asm.Label, org.objectweb.asm.Label, org.objectweb.asm.Label, java.lang.String)
     */
    @Override
    public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
            String arg3) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitTypeInsn(int, java.lang.String)
     */
    @Override
    public void visitTypeInsn(int arg0, String arg1) {

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitVarInsn(int, int)
     */
    @Override
    public void visitVarInsn(int arg0, int arg1) {

    }

}
