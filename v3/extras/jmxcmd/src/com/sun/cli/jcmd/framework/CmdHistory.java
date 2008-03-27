/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdHistory.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 

package com.sun.cli.jcmd.framework;

import java.util.Set;


/**
	Interface describing a command history.
 */
public interface CmdHistory
{
	/**
		Get the first command in the history.
	 */
	public	CmdHistoryItem	getFirstCmd();
	
	/**
		Get the last command in the history.
	 */
	public	CmdHistoryItem	getLastCmd();
	
	/**
		Get the history item identified by 'id'
	 */
	public	CmdHistoryItem	getCmd( int id );
	
	/**
		Get all the commands in the history.
	 */
	public	CmdHistoryItem[]	getAll();
	
	/**
		Get a range of commands
	 */
	public	CmdHistoryItem[]	getRange( int first, int last );
	
	/**
		Truncate the history to the specified ID
	 */
	public void	truncate( int last );
	
	
	/**
		Remove all history
	 */
	public void	clear( );
	
	/**
		Add a command to the end (most recent part) of the list. Returns
		its ID.
		
		@param tokens	the command as parsed
	 */
	public	int	addCmd( String[] tokens );
};
	