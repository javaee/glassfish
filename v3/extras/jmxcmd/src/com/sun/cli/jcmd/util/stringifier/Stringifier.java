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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/Stringifier.java,v 1.3 2005/11/08 22:39:27 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:27 $
 */
 
 
package com.sun.cli.jcmd.util.stringifier;

/**
	Convert an object to a String.  The intent of this is to provide a flexible means
	to control the string representation of an Object. The toString() routine has many
	issues, including:
	- appropriateness for end-user viewing (within a CLI for example)
	- an object may not have implemented a toString() method
	- the output of toString() may simply be unacceptable (eg class@eebc1933)
	- it may be desirable to have many variations on the output
	- modifying toString() requires modifying the orignal class; a Stringifier
	or many of them can exist independently, making it easy to apply many different
	types of formatting to the same class.
	
	The intended use is generally to have a separate class implement Stringifier, rather
	than the class to be stringified.
 */
public interface Stringifier
{
	/**
		Produce a String representation of an object.  The actual output has no
		other semantics; each Stringifier may choose to target a particular type
		of user.
		<p>
		The resulting String should be suitable for display to a user.
		
		@param object	the Object for which a String should be produced
	 */
	public String	stringify( Object object );
}

