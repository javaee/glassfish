
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/ArrayStringifier.java,v 1.3 2005/11/08 22:39:26 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:26 $
 */
 
package com.sun.cli.jcmd.util.stringifier;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

/**
	Stringifies an array, using an optional array element Stringifier
 */
 
public final class ArrayStringifier implements Stringifier
{
	final String			mDelim;
	final Stringifier		mElementStringifier;
	boolean					mAddBraces;
	
	static final char	LEFT_BRACE	= '{';
	static final char	RIGHT_BRACE	= '}';
	static final String	DEFAULT_DELIM=",";
	
		public 
	ArrayStringifier()
	{
		this( SmartStringifier.DEFAULT );
		mAddBraces	= false;
	}
		public 
	ArrayStringifier( boolean addBraces )
	{
		this( DEFAULT_DELIM, SmartStringifier.DEFAULT, addBraces );
	}
	
		public
	ArrayStringifier( String delim )
	{
		this( delim, false );
	}
	
		public
	ArrayStringifier( String delim, boolean addBraces)
	{
		this( delim, SmartStringifier.DEFAULT, addBraces );
	}
	
		public
	ArrayStringifier( Stringifier elementStringifier )
	{
		this( DEFAULT_DELIM, elementStringifier );
	}
	
		public
	ArrayStringifier( String delim, Stringifier elementStringifier )
	{
		this( delim, elementStringifier, false );
	}
	
		public
	ArrayStringifier( String delim, Stringifier elementStringifier, boolean addBraces)
	{
		mDelim				= delim;
		mElementStringifier	= elementStringifier;
		mAddBraces			= addBraces;
	}
	
		static String
	addBraces( boolean add, String s )
	{
		String	out	= s;
		if ( add )
		{
			out	= LEFT_BRACE + s + RIGHT_BRACE;
		}
		return( out );
	}
	
		public String
	stringify( Object o )
	{
		final String	s	= this.stringify( (Object [])o, mDelim, mElementStringifier );
		
		return( addBraces( mAddBraces, s ) );
	}
	
	/**
		Static variant when direct call will suffice.
	 */
		public static String
	stringify( Object [] o, String delim, Stringifier stringifier )
	{
		final Iterator	iter		= Arrays.asList( o ).iterator();
		final IteratorStringifier	iterStringifier	= new IteratorStringifier( delim, stringifier );
		
		final String	s	= iterStringifier.stringify( iter );
		return( s );
	}
	
	/**
		Static variant when direct call will suffice.
	 */
		public static String
	stringify( Object [] o, String delim )
	{
		return( stringify( o, delim, SmartStringifier.DEFAULT ) );
	}
	
	public final static ArrayStringifier DEFAULT = new ArrayStringifier( "," );
}

