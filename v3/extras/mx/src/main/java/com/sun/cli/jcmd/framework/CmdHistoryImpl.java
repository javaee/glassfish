/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdHistoryImpl.java,v 1.9 2005/11/15 20:21:42 llc Exp $
 * $Revision: 1.9 $
 * $Date: 2005/11/15 20:21:42 $
 */
 

package com.sun.cli.jcmd.framework;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import com.sun.cli.jcmd.util.misc.StringEscaper;
import com.sun.cli.jcmd.util.misc.SafeSave;
import org.glassfish.admin.amx.util.FileUtils;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;


/**
	Implements CmdHistory
 */
public class CmdHistoryImpl implements CmdHistory
{
	final MyArrayList<CmdHistoryItem>	mCmds;
	File				mAutoSaveFile;
	AutoSaver			mAutoSaver;
	boolean				mNeedsSave;
	
	private static final class MyArrayList<T> extends ArrayList<T>
	{
	    static final long serialVersionUID = -2736985541929697451L;
	    
			public
		MyArrayList()
		{
		}
		
		// expose this for our use
			public void
		removeRange( int start, int end )
		{
			super.removeRange( start, end );
		}	
	}
	
		synchronized int
	getNextCmdID()
	{
		final CmdHistoryItem	item	= getLastCmd();
		int		nextID	= 1;
		
		if ( item != null )
		{
			nextID	= item.getID() + 1;
		}
		return( nextID );
	}
	
		public
	CmdHistoryImpl()
	{
		mCmds		= new MyArrayList<CmdHistoryItem>();
		mAutoSaver	= null;
		mNeedsSave	= false;
	}
	
		public
	CmdHistoryImpl( String historyString )
	{
		mCmds		= new MyArrayList<CmdHistoryItem>();
		
		fromString( historyString );
		
		mNeedsSave	= false;
	}
	
		public
	CmdHistoryImpl( CmdHistory history, int first, int last )
	{
		mCmds		= new MyArrayList<CmdHistoryItem>();
		
		final CmdHistoryItem[]	items	= history.getRange( first, last );
		
		for( int i = 0; i < items.length; ++i )
		{
			mCmds.add( items[ i ] );
		}
		
		mNeedsSave	= true;
	}
	
		private int
	idToIndex( int id )
	{
		return( id - getFirstCmd().getID() );
	}
	
		public synchronized void
	truncate( int last )
	{
		mCmds.removeRange( idToIndex( last + 1 ), mCmds.size() );
		createAutoSaver();
	}
	
	
		public synchronized CmdHistoryItem
	getFirstCmd()
	{
		CmdHistoryItem	item	= null;
		
		if ( mCmds.size() != 0 )
		{
			item	= (CmdHistoryItem)mCmds.get( 0 );
		}
		return( item );
	}
	
		public synchronized CmdHistoryItem
	getLastCmd()
	{
		CmdHistoryItem	item	= null;
		
		if ( mCmds.size() != 0 )
		{
			item	= (CmdHistoryItem)mCmds.get( mCmds.size() - 1 );
		}
		return( item );
	}
	
		private synchronized void
	addCmd( CmdHistoryItem item )
	{
		mCmds.add( item );
		createAutoSaver();
	}
	
		public synchronized int
	addCmd( String[] tokens )
	{
		if ( tokens == null || tokens.length == 0)
		{
			throw new IllegalArgumentException( "must have at least one token" );
		}
		
		int	id	= getNextCmdID();
		final CmdHistoryItem		item	= new CmdHistoryItem( id, tokens );
		
		addCmd( item );
		
		return( id );
	}
	
		public synchronized CmdHistoryItem
	getCmd( int i )
	{
		return( (CmdHistoryItem)mCmds.get( idToIndex( i ) ));
	}
	
	
	/**
		Get a range of commands
	 */
		public synchronized CmdHistoryItem[]
	getRange( int first, int last )
	{
		if (	first < getFirstCmd().getID() ||
				last > getLastCmd().getID() ||
				first > last + 1 )
		{
			throw new IllegalArgumentException( "Illegal range: " + first + ", " + last );
		}
		
		final CmdHistoryItem[]	items	= new CmdHistoryItem[ 1 + (last - first) ];
		
		for( int i = 0; i < items.length; ++i )
		{
			items[ i ]	= getCmd( first + i );
		}
		
		return( items );
	}
	
	
		public synchronized CmdHistoryItem[]
	getAll()
	{
		return( (CmdHistoryItem[])mCmds.toArray( new CmdHistoryItem[ mCmds.size() ] ) );
	}
	
	private final static char	SPACE	= ' ';
	private final static char	NEWLINE	= '\n';
	private final static char	RETURN	= '\r';
	private final static char	TOKEN_DELIM	= SPACE;
	private final static char	LINE_DELIM	= NEWLINE;
	private final static String	CHARS_TO_ESCAPE	= "" + TOKEN_DELIM + LINE_DELIM + RETURN;
	
	/**
		Delimiter between command ID and its tokens
	 */
	private final static char	ID_DELIM	= ':';
	
	/**
		Turn the history into one giant String.
	 */
		public synchronized String
	toString()
	{
		final CmdHistoryItem[]	all	= getAll();
		final StringBuffer		buf	= new StringBuffer();
		
		final StringEscaper	escaper	= new StringEscaper( CHARS_TO_ESCAPE );
		
		for( int i = 0; i < all.length; ++i )
		{
			final CmdHistoryItem	item	= all[ i ];
			
			final String	prefix	= i + ":";
			
			// ensure that each token is escaped 
			final String[]	tokens	= item.getTokens();
			for ( int tokenIdx = 0; tokenIdx < tokens.length; ++tokenIdx )
			{
				tokens[ tokenIdx ]	= escaper.escape( tokens[ tokenIdx ] );
			}
			
			// turn the tokens into one String
			final String	tokensString	= ArrayStringifier.stringify( tokens, "" + TOKEN_DELIM );
			
			buf.append( prefix + tokensString + LINE_DELIM);
		}
		
		return( buf.toString() );
	}
	
	/**
		Save command history.
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
	
	/**
		Clear command history.
	 */
		public synchronized void
	clear()
	{
		mCmds.clear();
	}
	
		public synchronized void
	autoSave( File theFile )
	{
		mAutoSaveFile	= theFile;
		
		createAutoSaver();
	}
	
		private synchronized void
	createAutoSaver()
	{
		mNeedsSave	= true;
		new AutoSaver();
	}
	
	
	private final static long	FLUSH_INTERVAL_MILLIS	= 5 * 1000;
	private class AutoSaver implements Runnable
	{
			public
		AutoSaver()
		{
			if ( mAutoSaver == null )
			{
				mAutoSaver	= this;
				new Thread( null, this, "CmdHistoryImpl.AutoSaver" + this.toString() ).start();
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
			
			if ( mNeedsSave && mAutoSaveFile != null )
			{
				try
				{
					save( mAutoSaveFile );
				}
				catch( IOException e )
				{
					// hmmm...
				}
			}
			
			mAutoSaver	= null;
		}
	}
	
	

	/**
		Restore command history from a String.
	 */
		public synchronized void
	fromString( String s )
	{
		clear();
		
		final String[]	lines	= s.split( "" + LINE_DELIM );
	
		final StringEscaper	escaper	= new StringEscaper( CHARS_TO_ESCAPE );
		for( int i = 0; i < lines.length; ++i )
		{
			final String	line	= escaper.unescape( lines[ i ].trim() );
			
			if ( line.length() != 0 )
			{
				final int		delimIndex	= line.indexOf( ID_DELIM );
				final int		id	= new Integer( line.substring( 0, delimIndex ) ).intValue();
				final String	tokensString	= line.substring( delimIndex + 1, line.length());
				final String[]	tokens	= tokensString.split( "" + TOKEN_DELIM );
				
				final CmdHistoryItem	item	= new CmdHistoryItem( id, tokens );
				addCmd( item );
			}
		}
	}
	
	/**
		Restore command history from a file.
	 */
		public void
	restore( File src )
		throws FileNotFoundException, IOException
	{
		final boolean	saveNeedsSave	= mNeedsSave;
		
		final String	history	= FileUtils.fileToString( src );
		fromString( history );
		
		mNeedsSave	= saveNeedsSave;
	}
	
};
	