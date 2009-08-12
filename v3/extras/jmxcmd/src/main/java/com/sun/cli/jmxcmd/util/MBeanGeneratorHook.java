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
 
package com.sun.cli.jmxcmd.util;


import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;


/**
	Hook to be passed to MBeanGenerator
 */
public interface MBeanGeneratorHook
{
	public String		getPackageName( MBeanInfo info );
	public String		getClassname( MBeanInfo info );
	public String		getExceptions( MBeanOperationInfo info );
	public String[]		getParamNames( MBeanOperationInfo info );
	
	public String		getHeaderComment( MBeanInfo info );
	public String		getInterfaceComment( MBeanInfo info );
	public String		getGetterComment( MBeanAttributeInfo info, String actualName );
	public String		getSetterComment( MBeanAttributeInfo info, String actualName );
	public String		getOperationComment( MBeanOperationInfo info, String[] paramNames );
}






