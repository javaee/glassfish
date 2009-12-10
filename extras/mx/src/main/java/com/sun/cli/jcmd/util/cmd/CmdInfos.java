/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/CmdInfos.java,v 1.4 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import java.util.List;
import java.util.ArrayList;



/**
	Interface for info about a particular subcommand.  There should be one
	of these for every command that a Cmd class implements.
 */
public final class CmdInfos
{
	private final List<CmdInfo>	mInfos;
	
		public
	CmdInfos()
	{
		mInfos	= new ArrayList<CmdInfo>();
	}
	
	/**
		The order of the CmdInfo within the array is significant and is retained;
		the first item is by default the overall name for the entire group.
		
		@param infos	array of CmdInfo
	 */
		public
	CmdInfos( CmdInfo[] infos )
	{
		this();
		
		for( int i = 0; i < infos.length; ++i )
		{
			add( infos[ i ] );
		}
	}
	
		public
	CmdInfos( CmdInfo info )
	{
		this( new CmdInfo[] { info } );
	}
	
		public
	CmdInfos( CmdInfo info1, CmdInfo info2)
	{
		this( new CmdInfo[] { info1, info2 } );
	}
	
		public
	CmdInfos( CmdInfo info1, CmdInfo info2, CmdInfo info3 )
	{
		this( new CmdInfo[] { info1, info2, info3 } );
	}
	
		public
	CmdInfos( CmdInfo info1, CmdInfo info2, CmdInfo info3, CmdInfo info4 )
	{
		this( new CmdInfo[] { info1, info2, info3, info4} );
	}
	
		public
	CmdInfos( CmdInfo info1, CmdInfo info2, CmdInfo info3, CmdInfo info4, CmdInfo info5 )
	{
		this( new CmdInfo[] { info1, info2, info3, info4, info5} );
	}
	
		public void
	add( CmdInfo info )
	{
		mInfos.add( info );
	}
	
		public int
	size(  )
	{
		return( mInfos.size() );
	}
	
		public CmdInfo
	get( int i )
	{
		return( (CmdInfo)mInfos.get( i ) );
	}
	
		public CmdInfo
	get( String name )
	{
		CmdInfo	item	= null;
		
		for( int i = 0; i < mInfos.size(); ++i )
		{
			final CmdInfo	info	= get( i );
			
			if ( name.equals( info.getName() ) )
			{
				item	= info;
				break;
			}
		}
			
		return( item );
	}
	
	
	/**
		Return a list of all the command names.  The order is significant; the first name
		is the overall name for the group.
	 */
		public List<String>
	getNamesList( )
	{
		final List<String>	names	= new ArrayList<String>();
		
		for( int i = 0; i < mInfos.size(); ++i )
		{
			names.add( get( i ).getName() );
		}
		
		return( names );
	}
	
	
	/**
		Return a list of all the command names.  The order is significant; the first name
		is the overall name for the group.
	 */
		public String[]
	getNames( )
	{
		final List<String>	namesList	= getNamesList();
		
		return( (String[])namesList.toArray( new String[ namesList.size() ] ) );
	}
	
		public CmdInfo[]
	toArray()
	{
		return( (CmdInfo[])mInfos.toArray( new CmdInfo[ mInfos.size() ] ) );
	}
	
		public String
	toString()
	{
		final StringBuffer	buf	= new StringBuffer();
		
		final String[]	names	= getNames();
		for( int i = 0; i < names.length; ++i )
		{
			buf.append( get( names[ i ] ).toString() + "\n" );
		}
		
		return( buf.toString() );
	}
}





