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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/RegexUtil.java,v 1.9 2005/11/08 22:39:23 llc Exp $
 * $Revision: 1.9 $
 * $Date: 2005/11/08 22:39:23 $
 */
 
package com.sun.cli.jcmd.util.misc;

import java.util.regex.Pattern;

/**
	Useful utilities for regex handling
 */
public final class RegexUtil
{
		private
	RegexUtil()
	{
		// disallow instantiation
	}
	
	
	private final static char BACKSLASH	= '\\';
	
	/**
		These characters will be escaped by wildcardToJavaRegex()
	 */
	public static final String REGEX_SPECIALS	= BACKSLASH + "[]^$?+{}()|-!";
	
	
	
	/**
		Converts each String to a Pattern using wildcardToJavaRegex
		
		@param exprs	String[] of expressions
		@return	Pattern[], one for each String
	 */
		public static Pattern[]
	exprsToPatterns( final String[]	exprs )
	{
		return( exprsToPatterns( exprs, 0 ) );
	}
	
	/**
		Converts each String to a Pattern using wildcardToJavaRegex, passing the flags.
		
		@param exprs	String[] of expressions
		@param flags	flags to pass to Pattern.compile
		@return	Pattern[], one for each String
	 */
		public static Pattern[]
	exprsToPatterns( final String[]	exprs, int flags )
	{
		final Pattern[]	patterns	= new Pattern[ exprs.length ];
		
		for( int i = 0; i < exprs.length; ++i )
		{
			patterns[ i ]	= Pattern.compile( wildcardToJavaRegex( exprs[ i ]), flags  );
		}
		return( patterns );
	}
	
	/**
		Supports the single wildcard "*".  There is no support for searching for
		a literal "*".
		
		Convert a string to a form suitable for passing to java.util.regex.
	 */
		public static String
	wildcardToJavaRegex( String input )
	{
		String	converted	= input;
		
		if ( input != null )
		{
			final int 			length	= input.length();
			final StringBuffer	buf	= new StringBuffer();
			
			for( int i = 0; i < length; ++i )
			{
				final char	theChar	= input.charAt( i );
				
				if ( theChar == '.' )
				{
					buf.append( "[.]" );
				}
				else if ( theChar == '*' )
				{
					buf.append( ".*" );
				}
				else if ( REGEX_SPECIALS.indexOf( theChar ) >= 0 )
				{
					// '[' begins a set of characters
					buf.append( "" + BACKSLASH + theChar );
				}
				else
				{
					buf.append( theChar );
				}
			}
			
			converted	= buf.toString();
			
		}
		return( converted );
	}
}

