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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/StringStringSource.java,v 1.2 2005/11/08 22:39:24 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:24 $
 */
package com.sun.cli.jcmd.util.misc;

import java.util.Properties;


/**
	A StringSource which obtains its strings from a String mimicking a Properties file
	key1=value1
	key2=value2
	....
	keyN=valueN
	
	etc
 */
public class StringStringSource extends StringSourceBase
{
	final Properties	mPairs;

		public
	StringStringSource( String strings, StringSource delegate )
	{
		super( delegate );
		mPairs	= new Properties();
		init( mPairs, strings );
	}
	
		private void
	init( Properties props, String strings )
	{
		// FIX: need to unescape the strings
		final String[]	lines	= strings.split( "\n" );
		
		for( int i = 0; i < lines.length; ++i )
		{
			final String	line	= lines[ i ];
			final int		delim	= line.indexOf( '=' );
			
			final String	name	= line.substring( 0, delim );
			final String	value	= line.substring( delim + 1, line.length() );
			
			mPairs.setProperty( name, value );
		}
	}
	
		public String
	getString( String id )
	{
		String	result	= mPairs.getProperty( id );
		
		if ( result == null )
		{
			result	= super.getString( id );
		}
		return( result );
	}
}

