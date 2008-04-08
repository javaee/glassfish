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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/IteratorStringifier.java,v 1.3 2005/11/08 22:39:26 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:26 $
 */
 
package com.sun.cli.jcmd.util.stringifier;

import java.util.Iterator;


/**
	Stringifies an Iterator, using an optional element Stringifier
 */
 
public final class IteratorStringifier extends IteratorStringifierBase
{
		public 
	IteratorStringifier()
	{
		super();
	}
	
		public 
	IteratorStringifier( String delim )
	{
		super( delim );
	}
	
		public 
	IteratorStringifier( Stringifier elementStringifier )
	{
		super( elementStringifier );
	}
	
		public 
	IteratorStringifier( String delim, Stringifier elementStringifier )
	{
		super( delim, elementStringifier );
	}
	
	
		public void
	stringifyElement(
		Object			elem,
		String			delim,
		StringBuffer	buf)
	{
		if ( elem == null )
		{
			buf.append( "null" );
		}
		else
		{
			buf.append( mElementStringifier.stringify( elem ) );
		}
	}
	
	
	/*
		Static variant when direct call will suffice.
	 */
		public static String
	stringify( Iterator iter, String delim )
	{
		final IteratorStringifier	stringifier	= new IteratorStringifier( delim );
		
		return( stringifier.stringify( iter, delim, SmartStringifier.DEFAULT ) );
	}
	
	
	public final static IteratorStringifier DEFAULT = new IteratorStringifier( "," );
}

