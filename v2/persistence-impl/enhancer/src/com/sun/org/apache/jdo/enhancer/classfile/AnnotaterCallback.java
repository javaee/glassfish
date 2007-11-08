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

package com.sun.org.apache.jdo.enhancer.classfile;

/**
 * AnnotaterCallback receive callbacks from Annotator, CallFileParser upon a) A
 * begining of a method b) A field is accessed, c) A static method invocation d)
 * A method end. The MethodDescritor returned from onFieldAccess and
 * onStaticMethodInvoke are completely ignored by a ClassFileParser but are used
 * by the
 * 
 * @see Annotator
 * @see ClassFileParserEventListener
 * @see ClassInfo
 * @author Mahesh Kannan
 */
public interface AnnotaterCallback {

    public MethodDescriptor onFieldAccess(String currentMethodName,
            boolean isRead, String ownerClassName, String fieldName,
            String fieldType);

    public MethodDescriptor onStaticMethodInvoke(String currentMethodName,
            String ownerClassName, String methodName, String methodType,
            String returnType, String[] paramTypes);

}
