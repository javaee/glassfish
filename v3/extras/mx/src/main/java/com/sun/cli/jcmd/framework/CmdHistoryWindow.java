/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdHistoryWindow.java,v 1.3 2004/01/09 22:17:26 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/01/09 22:17:26 $
 */
 

package com.sun.cli.jcmd.framework;

import java.util.Set;


/**
	Interface describing a command history.
 */
public final class CmdHistoryWindow implements CmdHistory
{
	final CmdHistory	mHistory;
	final int			mFirst;
	int					mLast;
	
		public
	CmdHistoryWindow( CmdHistory history, int first, int last)
	{
		if (	first < history.getFirstCmd().getID() ||
				last > history.getLastCmd().getID() ||
				first > last + 1 )
		{
			throw new IllegalArgumentException( "Illegal range: " + first + ", " + last );
		}
		
		mHistory	= history;
		mFirst		= first;
		mLast		= last;
		
	}
	
		public
	CmdHistoryWindow( CmdHistory history,  int last)
	{
		this( history, history.getFirstCmd().getID(), last );
		
	}
	
	public	CmdHistoryItem	getFirstCmd()	{ return( mHistory.getCmd( mFirst ) ); }
	public	CmdHistoryItem	getLastCmd()	{ return( mHistory.getCmd( mLast ) ); }
	
		public CmdHistoryItem
	getCmd( int id )
	{
		if ( id < mFirst || id > mLast )
		{
			throw new IllegalArgumentException( "Illegal cmd ID: " + id );
		}

		return( mHistory.getCmd( id ) );
	}
	
		public CmdHistoryItem[]
	getRange( int first, int last )
	{
		if (	first < getFirstCmd().getID() ||
				last > getLastCmd().getID() ||
				first > last + 1 )
		{
			throw new IllegalArgumentException( "Illegal range: " + first + ", " + last );
		}
		
		return( mHistory.getRange( first, last ) );
	}
	
		public void
	truncate( int last )
	{
		mLast	= last;
	}
	
		public void
	clear( )
	{
		mLast	= mFirst -1;
	}
	
		public CmdHistoryItem[]
	getAll()
	{
		return( mHistory.getRange( mFirst, mLast ) );
	}
	
		public int
	addCmd( String[] tokens )
	{
		throw new IllegalArgumentException( "can't add a command to a windowed history" );
	}
};
	