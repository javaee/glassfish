/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/AliasMgrHashMapImpl.java,v 1.5 2004/10/14 19:06:28 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2004/10/14 19:06:28 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;

import com.sun.cli.jcmd.util.misc.StringEscaper;
import com.sun.cli.jcmd.util.misc.SafeSave;
import org.glassfish.admin.amx.util.StringUtil;

public final class AliasMgrHashMapImpl implements AliasMgrSPI
{
	final Map<String,String>	mMap;
	String			mFilename;
	Flusher			mFlusher;
	final StringEscaper	mEscaper;
	
	private boolean	mNeedsFlush;
	private static long	FLUSH_INTERVAL_MILLIS	= 1 * 1000;
	
		public
	AliasMgrHashMapImpl()
	{
		mEscaper	= new StringEscaper( CHARS_NEEDING_ESCAPING );
		
		mMap	= new HashMap<String,String>();
	}
	
	public boolean	needsFlush()	{ return( mNeedsFlush ); }
	
	
	/*
		Newline and carriage return may be problems when persisting to properties file
	 */
	static final private char	NEWLINE		='\n';
	static final private char	RETURN		='\r';
	static final private String	CHARS_NEEDING_ESCAPING	= ("" + NEWLINE) + RETURN;
	
	
		private String
	escapeAliasValue( String s )
	{
		return( mEscaper.escape( s ) );
	}
	
		private String
	unescapeAliasValue( String s )
	{
		return( mEscaper.unescape( s ) );
	}
	
		public synchronized void
	create( String aliasName, String value ) throws Exception
	{
		mMap.put( aliasName, value );
		createFlusher();
	}
	
		public synchronized String
	get( String aliasName )
	{
		return( mMap.get( aliasName ) );
	}
	
		public synchronized void
	delete( String aliasName )
		throws Exception
	{
		mMap.remove( aliasName );
		createFlusher();
	}
	
		public synchronized Set<String>
	getNames()
	{
		return( mMap.keySet() );
	}
	
		public synchronized void
	save(  )
		throws IOException
	{
		if ( mFilename != null )
		{
			save( new File( mFilename ) );
		}
	}
	
	
		private void
	createFlusher()
	{
		mNeedsFlush	= true;
		new Flusher();
	}
	
	private class Flusher implements Runnable
	{
			public
		Flusher()
		{
			if ( mFlusher == null )
			{
				mFlusher	= this;
				new Thread( null, this, "AliasMgrHashMapImpl.Flusher" + this.toString() ).start();
			}
		}
		
			public void
		run()
		{
			try
			{
				Thread.sleep( FLUSH_INTERVAL_MILLIS );
			}
			catch( InterruptedException e )
			{
			}
			
			if ( needsFlush() )
			{
				try
				{
					save();
				}
				catch( IOException e )
				{
					// hmmm...
				}
			}
			
			mFlusher	= null;
		}
	}
	
		public synchronized void
	save( File theFile )
		throws IOException
	{
		mFilename	= theFile.getPath();
		
		final Set		names	= getNames();
		final Iterator	iter	= names.iterator();
		final String []	pairs	= new String [ names.size() ];
		
		for( int i = 0; i < pairs.length; ++i )
		{
			final String	name	= (String)iter.next();

			pairs[ i ]	= name + "=" + escapeAliasValue( get( name ) );
		}
		
		
		final File			temp	= SafeSave.getTempFile( theFile );
		final FileWriter	out		= new FileWriter( temp );
		
		for( int i = 0; i < pairs.length; ++i )
		{
			out.write( pairs[ i ] + StringUtil.LS );
		}
		
		out.close();
		
		SafeSave.replaceWithNew( theFile, temp);
		mNeedsFlush		= false;
	}
	
		private String
	readLine( FileReader in )
		throws IOException
	{
		StringBuffer	buf	= new StringBuffer();
		
		while ( true )
		{
			final int	i =in.read();
			if ( i < 0 )
			{
				return( null );
			}
		
			final char	theChar	= (char)i;
			if ( theChar == '\n' || theChar == '\r' )
			{
				// ignore blank lines
				if ( buf.length() == 0 )
					continue;
				break;
			}
			
			buf.append( theChar );
		}
		return( buf.toString() );
	}
	
		public synchronized void
	load( File theFile )
		throws Exception
	{
		mFilename	= theFile.getPath();
		
		final FileReader	in	= new FileReader( theFile );
		
		while ( true )
		{
			final String	line	= readLine( in );
			if ( line == null )
				break;
			try
			{
				final String pair	= unescapeAliasValue( line );
					
				final int		separatorOffset	= pair.indexOf( '=' );
				
				final String name	= pair.substring( 0, separatorOffset );
				final String value	= pair.substring( separatorOffset + 1, pair.length() );
				
				create( name, value );
			}
			catch( Exception e )
			{
				System.err.println( "Illegal alias pair: " + line );
			}
		}
		
		in.close();
	}
};


