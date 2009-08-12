/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdAliasMgr.java,v 1.3 2005/11/08 22:39:16 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:16 $
 */
 

package com.sun.cli.jcmd.framework;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.cli.jcmd.util.misc.SafeSave;
import org.glassfish.admin.amx.util.ArrayConversion;
import org.glassfish.admin.amx.util.FileUtils;


/**
	Manager for Cmd aliases
 */
public class CmdAliasMgr
{
	final Map<String,String>	mMap;
	boolean			mNeedsSave;
	
		public
	CmdAliasMgr()
	{
		mMap		= new HashMap<String,String>();
		mNeedsSave	= false;
	}
	
		public void
	createAlias( String name, String value )
	{
		mMap.put( name, value );
		mNeedsSave	= true;
	}
	
		public void
	deleteAlias( String name )
	{
		mMap.remove( name );
		mNeedsSave	= true;
	}
	
		public String
	getAliasValue( String name )
	{
		return( (String)mMap.get( name ) );
	}
	
	
		public String[]
	getAliasNames(  )
	{
		final Set<String>	names	= getAliasNamesSet();
		
		String[]	result	= null;
		if ( names.size() == 0 )
		{
			result	= new String[ 0 ];
		}
		else
		{
			result	= (String[])ArrayConversion.setToArray( names, true );
		}
		
		return( result );
	}
	
		public Set<String>
	getAliasNamesSet(  )
	{
		return( mMap.keySet() );
	}
	
	/**
		Save command aliases
	 */
		public synchronized void
	save( File dest )
		throws IOException
	{
		// save the history as one item per line
		final File	temp	= SafeSave.getTempFile( dest );
		
		FileWriter	out	= null;
		try
		{
			out	= new FileWriter( temp );
			
			out.write( toString() );
			mNeedsSave	= false;
		}
		finally
		{
			if ( out != null )
			{
				out.close();
			}
			SafeSave.replaceWithNew( dest, temp );
		}
	}
	
	
	private final static char	NEWLINE	= '\n';
	private final static char	LINE_DELIM	= NEWLINE;
	private final static char	NAME_VALUE_DELIM	= '=';
	/**
		Read command aliases from a String.
	 */
		public synchronized void
	fromString( String s )
	{
		final String[]	lines	= s.split( "" + LINE_DELIM );
	
		boolean	saveNeedsSave	= mNeedsSave;
		
		for( int i = 0; i < lines.length; ++i )
		{
			final String	line	= lines[ i ].trim();
			
			if ( line.length() != 0 )
			{
				final int		delimIndex	= line.indexOf( NAME_VALUE_DELIM );
				final String	name	= line.substring( 0, delimIndex );
				final String	value	= line.substring( delimIndex + 1, line.length() );
				
				createAlias( name, value );
			}
		}
		
		mNeedsSave	= saveNeedsSave;
	}
	
	/**
		Read command aliases from a file
	 */
		public void
	restore( File src )
		throws FileNotFoundException, IOException
	{
		fromString( FileUtils.fileToString( src ) );
	}
	
	
		public String
	toString()
	{
		final String[]		names	= getAliasNames();
		final StringBuffer	buf	= new StringBuffer();
		
		for ( int i = 0; i < names.length; ++i )
		{
			final String value	= getAliasValue( names[ i ] );
			if ( value != null )
			{
				buf.append( names[ i ] + "=" + value + "\n" );
			}
		}
		
		return( buf.toString() );
	}
}









