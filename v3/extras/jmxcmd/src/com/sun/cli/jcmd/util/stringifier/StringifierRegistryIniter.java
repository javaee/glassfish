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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/StringifierRegistryIniter.java,v 1.2 2005/11/08 22:39:27 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:27 $
 */
 
package com.sun.cli.jcmd.util.stringifier;

/**
	Interface for adding a Stringifier.
 */
public interface StringifierRegistryIniter
{
	/**
		Add a mapping from the class to its Stringifier
	 */
	public void					add( Class theClass, Stringifier theStringifier );
	
	/**
		Get the registry in use by this Stringifier
	 */
	public StringifierRegistry	getRegistry();
}



