/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ConnectInfo.java,v 1.5 2004/06/04 01:06:59 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2004/06/04 01:06:59 $
 */
 

package com.sun.cli.jmxcmd.support;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import java.net.MalformedURLException;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.sun.cli.jmxcmd.support.ArgParserImpl;
import com.sun.cli.jmxcmd.support.ArgParserException;


/**
	Class encapsulating connection information as a list of name/value pairs.
	The values in this class are typically those used by implementors of 
	{@link com.sun.cli.jmxcmd.spi.JMXConnectorProvider}.
 */
public final class ConnectInfo
{
	private final Map<String,String>		mParams;
	
	public final static char	ESCAPE_CHAR	= '\\';
	public final static char	VALUE_DELIM		= '=';
	public final static char	PAIR_DELIM		= ',';
	
	/**
		Key for the option of caching MBeanInfo
	 */
	public final static String	CACHE_MBEAN_INFO	= "cache-mbean-info";
	
		static Map<String,String>
	connectStringToParams( final String connectString )
	{
		final Map<String,String>	m	= new java.util.HashMap<String,String>();
		
		final ArgParserImpl.ParseChars	parseChars	= new ArgParserImpl.ParseChars();
		parseChars.mArgDelim	= PAIR_DELIM;
		final ArgParserImpl	parser	= new ArgParserImpl();
		
		String []	pairs	= null;
		try
		{
			pairs	= parser.ParseNames( connectString );
		}
		catch( ArgParserException e )
		{
			throw new IllegalArgumentException( connectString );
		}
			
		for( int i = 0; i < pairs.length; ++i )
		{
			final String	pair	= pairs[ i ];
			final int		delimIndex	= pair.indexOf( VALUE_DELIM );
			
			if ( delimIndex <= 0 )
			{
				throw new IllegalArgumentException( "Illegal connect string: " + connectString );
			}
				
				
			final String	name	= pair.substring( 0, delimIndex );
			final String	value	= pair.substring( delimIndex + 1, pair.length() );
			
			m.put( name, value );
		}

		return( m );
	}

	
	/**
		A connect string consists of a series of name/value pairs eg:
		host=myhost,port=9993,user=foo
		
		toString() produces such a String
	*/
		public
	ConnectInfo( final String connectString )
	{
		this( connectStringToParams( connectString ) );
	}
	
	/**
		Instantiate using an existing Map
		
		@param params	a map containing connect info
	 */
		public
	ConnectInfo( final Map<String,String> params )
	{
		mParams	= params;
	}

	/**
		Instantiate using an existing ConnectInfo as a template.
	 */
		public
	ConnectInfo( final ConnectInfo rhs )
	{
		mParams	= new HashMap<String,String>(rhs.mParams);
	}
	
		public Map<String,String>
	getParams()
	{
		return( Collections.unmodifiableMap( mParams ) );
	}

		static String
	escapeString( char charToEscape, String stringToEscape )
	{
		String	result	= stringToEscape;
		
		if ( result.indexOf( charToEscape ) >= 0 )
		{
			result	= result.replaceAll( "" + charToEscape,
								ESCAPE_CHAR + "" + charToEscape );
		}
		return( result );
	}
	
		static String
	escapeString( String value )
	{
		String	escapedValue	= escapeString( PAIR_DELIM, value );
		escapedValue	= escapeString( VALUE_DELIM, escapedValue );
		
		return( escapedValue );
	}
	
		String
	paramsToString( )
	{
		StringBuffer	buf	= new StringBuffer();
        for( final String key : mParams.keySet() )
		{
			final String	value	=(String)mParams.get( key );
			
			final String	pair	= key + VALUE_DELIM + escapeString( value );
			
			buf.append( PAIR_DELIM + pair );
		}
		
		String	result	= buf.toString();
		if ( result.length() != 0 )
		{
			// strip leading ":"
			result	= result.substring( 1, result.length() );
		}
		
		return( result );
	}
	
		public String
	getParam( String name )
	{
		return( mParams.get( name ) );
	}
		
		public String
	toString()
	{
		return( paramsToString() );
	}
	
	
		public boolean
	equals( Object o )
	{
		if ( o == this )
		{
			return( true );
		}
			
		if ( ! (o instanceof ConnectInfo ) )
		{
			return( false );
		}
		
		final ConnectInfo	rhs	= (ConnectInfo)o;
		
		return( mParams.equals( rhs.mParams ) );
	}
}







