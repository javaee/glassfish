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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/CollectionStringifier.java,v 1.3 2005/11/08 22:39:26 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:26 $
 */
 
package com.sun.cli.jcmd.util.stringifier;

import java.util.Collection;
import java.util.Iterator;

public class CollectionStringifier implements Stringifier
{
	public final static CollectionStringifier DEFAULT = new CollectionStringifier( "," );
	
	public final String			mDelim;
	public final Stringifier	mElementStringifier;
	
		public 
	CollectionStringifier( String delim )
	{
		this( delim, SmartStringifier.DEFAULT );
	}
	
		public 
	CollectionStringifier( Stringifier elementStringifier )
	{
		this( ",", elementStringifier );
	}
	
		public 
	CollectionStringifier( String delim, Stringifier elementStringifier )
	{
		mDelim				= delim;
		mElementStringifier	= elementStringifier;
	}
	
		public String
	stringify( Object o )
	{
		final Collection	c		= (Collection)o;
		final Iterator		iter	= c.iterator();
		
		String	result	= IteratorStringifier.DEFAULT.stringify( iter, mDelim, mElementStringifier);
		
		return( result );
	}
	
	
		public static String
	toString( final Object o, final String delim )
	{
		final Collection	c		= (Collection)o;
		final Iterator		iter	= c.iterator();
		
		String	result	= IteratorStringifier.DEFAULT.stringify( iter, delim );
		
		return( result );
	}
}
