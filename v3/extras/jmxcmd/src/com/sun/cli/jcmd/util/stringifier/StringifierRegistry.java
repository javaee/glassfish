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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/StringifierRegistry.java,v 1.4 2005/11/08 22:39:27 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:27 $
 */
 
package com.sun.cli.jcmd.util.stringifier;



/**
	A registry for mapping classes to Stringifiers
 */
public interface StringifierRegistry
{
	/**
		Add a mapping from a Class to a Stringifier
		
		@param theClass	the Class to which the Stringifier should be associated
		@param stringifier	the Stringifier for the class
	 */
		public void
	add( Class theClass, Stringifier stringifier );
	
	
	/**
		Lookup a Stringifier from a Class.
		
		@param theClass	the Class
		@return the Stringifier, or null if not found
	 */
		public Stringifier
	lookup( Class theClass );
}



