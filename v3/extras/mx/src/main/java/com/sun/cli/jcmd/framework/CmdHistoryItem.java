/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdHistoryItem.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 

package com.sun.cli.jcmd.framework;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

public final class CmdHistoryItem
{
	private final int		mID;
	private final String[]	mTokens;
	
		public
	CmdHistoryItem( int id, String[] tokens )
	{
		mID		= id;
		mTokens	= tokens;
	}
	
	public int	getID()			{ return( mID ); };
	
		public String[]
	getTokens()
	{
		final String[]	tokensCopy	= new String[ mTokens.length ];
		
		System.arraycopy( mTokens, 0, tokensCopy, 0, mTokens.length );
		return( tokensCopy );
	}
	
		public String
	toString()
	{
		return( ArrayStringifier.stringify( mTokens, " " ) );
	}
}