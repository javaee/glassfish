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
package com.sun.cli.jcmd.util.misc;

/**
 */
public interface DebugOut extends DebugSink
{
	/**
		Return true if debugging is on.
	 */
	public boolean		getDebug();
	
	/**
		Return the arbitrary identifier for this instance.
	 */
	public String		getID();
	
	/**
	    If getDebug() returns true, output the arguments
	 */
	public void     debug( final Object... args );
	
	public void debugMethod( final String methodName,  final Object... args );
	public void debugMethod( final String methodName, final String msg, final Object... args );
}


