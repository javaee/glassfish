/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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







