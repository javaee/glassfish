/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jcmd.framework;

import java.util.Set;
import java.util.HashSet;

/**
	Events may be issued by commands and/or the framework.  This object
	manages the list of listeners and also manages the dispatch of events.
 */
public class CmdEventMgr
{
	private final Set<CmdEventListener>	mListeners;
	
		public
	CmdEventMgr()
	{
		mListeners	= new HashSet<CmdEventListener>();
	}
	
		public synchronized void
	addListener( CmdEventListener listener )
	{
		mListeners.add( listener );
	}
	
	
		public synchronized void
	removeListener( CmdEventListener listener )
	{
		mListeners.remove( listener );
	}
	
		public synchronized CmdEventListener[]
	getListeners()
	{
		return( (CmdEventListener[])
			mListeners.toArray( new CmdEventListener[ mListeners.size() ] ) );
	}
	
		public void
	dispatchEvent( CmdEvent event )
	{
		// get our own list
		final CmdEventListener[] listeners	= getListeners();
		// now we have our own list; any changes don't matter
		
		for( int i = 0; i < listeners.length; ++i )
		{
			listeners[ i ].acceptCmdEvent( event );
		}
	}
};



