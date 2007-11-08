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

import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;

/**
 * This interface defines the methods to generate PrimaryKey related methods.
 * These methods copy to / from primary key classes. The objects that implement this
 * interface either use direct field access or use getter/setter to access the 
 * persistence state of the bean.
 * 
 * @see com.sun.org.apache.jdo.impl.enhancer.jdo.asm.FieldAccessMediator
 * @see com.sun.org.apache.jdo.impl.enhancer.jdo.asm.PropertyBasedStateAccessGenerator
 * 
 * @author Mahesh Kannan
 */
public interface OIDMethodsHandler {

    public String getKeyClassName();

    public void generateNewObjectIDInstance(MethodVisitor mv);

    public void generateNewObjectIdInstanceStringMethod(MethodVisitor mv);

    public void generateCopyFromOID(MethodVisitor mv,
            FieldInfo thisClassKeyRef, FieldInfo keyClassKeyRef);

    public void generateCopyToOID(MethodVisitor mv, FieldInfo thisClassKeyRef,
            FieldInfo keyClassKeyRef);

    public void generateCopyToOIDOFS(MethodVisitor mv, String pcOwnerClassName,
            int pcFieldIndex, String ofsPath, String javaType, String sig,
            FieldInfo keyFieldRef);

    public void generateCopyFromOIDOFC(MethodVisitor mv,
            String pcOwnerClassName, int pcFieldIndex, String ofsPath,
            String javaType, String sig, FieldInfo keyFieldRef);

    public void generateNewObjectIDInstanceObjectMethod(MethodVisitor mv,
            String ownerClassName, String ofsPath);

}
