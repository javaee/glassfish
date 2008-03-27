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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/Output.java,v 1.3 2005/11/08 22:39:23 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:23 $
 */
 

package com.sun.cli.jcmd.util.misc;


/**
	The API that should be used to output from a Cmd running within the framework.
 */
public interface Output extends DebugSink
{
	/**
		Output a message without a newline.
		
		@param o	the Object to output
	 */
	public void	print( Object o );
	
	/**
		Output a message with a newline.
		
		@param o	the Object to output
	 */
	public void	println( Object o );
	
	/**
		Output a message to error output
		
		@param o	the Object to output
	 */
	public void	printError( Object o );
	
	
	/**
		Output a debug error message if getDebug() is currently true.
		
		@param o	the Object to output
	 */
	public void	printDebug( Object o );
	
	
	/**
		Done with it, can be destroyed.
	 */
	public void close();
};


