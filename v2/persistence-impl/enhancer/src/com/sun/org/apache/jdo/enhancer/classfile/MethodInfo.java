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

import java.io.PrintStream;
import java.util.Stack;


public interface MethodInfo
	extends MemberInfo
{

	public String getArgumentsAsJavaString();
	
	public String[] getExceptions();
	
	public boolean isNative();
	
	public boolean isSame(MethodInfo other);
	
	public boolean isEqual(Stack<String> list, MethodInfo meth2);
	
	public void print(PrintStream out, int indent);
    
    public String getReturnTypeDescriptor();
	
}
