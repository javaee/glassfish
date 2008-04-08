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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/StringSource.java,v 1.3 2005/11/08 22:39:24 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:24 $
 */
 
package com.sun.cli.jcmd.util.misc;

/**
	An abstraction for getting a String, given an id string.
 */
public interface StringSource
{
	/**
		Get the String referred to by 'id'
		
		@param id	the id (key) for the String
		@return 	the String, or null if not found
	 */
	public String	getString( String id );
	
	/**
		Get the String referred to by 'id'.
		
		@param id			the id (key) for the String
		@param defaultValue	the default value to be returned if string was not found
		@return 			the String, or defaultValue if not found
	 */
	public String	getString( String id, String defaultValue );
};



