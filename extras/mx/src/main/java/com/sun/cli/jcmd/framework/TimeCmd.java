/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jcmd.framework;

import java.util.Collections;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
 */
public class TimeCmd extends CmdBase
{
	private final static Map<String,Long> mTimeMap	=
	    Collections.synchronizedMap( new HashMap<String,Long>() );
	
		public
	TimeCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class TimeCmdHelp extends CmdHelpImpl
	{
			public
		TimeCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS		= "get current and/or elapsed time";
		static final String	SOURCE_TEXT		=
		"time -- prints the current time\n" +
		"mark-time <name> prints the current time, and names it with 'name'\n" +
		"time-since [<name>] displays time since last time <name> was marked";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SOURCE_TEXT ); }
	}
		public CmdHelp
	getHelp()
	{
		return( new TimeCmdHelp() );
	}

	
	
	public final static String	NAME				= "time";
	public final static String	TIME_CMD_NAME		= "time";
	public final static String	MARK_TIME_CMD_NAME	= "mark-time";
	public final static String	TIME_SINCE_CMD_NAME	= "time-since";
	
	private final static CmdInfo	TIME_CMD_INFO	=
		new CmdInfoImpl( TIME_CMD_NAME );
		
	private final static CmdInfo	MARK_TIME_CMD_INFO	=
		new CmdInfoImpl( MARK_TIME_CMD_NAME, null, new OperandsInfoImpl( "marker-name", 1, 1) );
		
	private final static CmdInfo	TIME_SINCE_CMD_INFO	=
		new CmdInfoImpl( TIME_SINCE_CMD_NAME, null, new OperandsInfoImpl( "marker-name", 0, 1) );
	
	private final static CmdInfo[]	CMD_INFOS =
		new CmdInfo[] { TIME_CMD_INFO, MARK_TIME_CMD_INFO, TIME_SINCE_CMD_INFO };
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( CMD_INFOS  ) );
	}
	
	private static final String	LAST	= "last";
	
		private void
	displayTime( final long theTime )
	{
		println( new Date( theTime ).toString() + " " + (theTime % 1000) + " ms" );
	}
	
	
		private void
	displayTime( )
	{
		final long	now	= now();
		
		displayTime( now );
		putTime( LAST, now );
	}
		
		private final void
	putTime(
		final String	markerName,
		final long		theTime )
	{
		mTimeMap.put( markerName, new Long( theTime ) );
	}

		private void
	markTime( final String markerName )
	{
		final long	now	= now();
		
		putTime( markerName, now );
		putTime( LAST, now );
		
		displayTime( now );
	}
		
		private void
	timeSince( final String markerName )
		throws CmdException
	{
		final long	now	= System.currentTimeMillis();
		
		final Long	start	= (Long)mTimeMap.get( markerName );
		
		if ( start == null )
		{
			throw new CmdException( getSubCmdNameAsInvoked(),
					"marker " + quote( markerName ) + " not found" );
		}

		final long	elapsed	= now - start.longValue();
		
		println( (elapsed / 1000.0) + " seconds" );
		
		putTime( LAST, now );
	}
	
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		final String	cmd	= getSubCmdNameAsInvoked();
		
		if ( cmd.equals( TIME_CMD_NAME ) )
		{
			displayTime();
		}
		else if ( cmd.equals( MARK_TIME_CMD_NAME ) )
		{
			markTime( operands[ 0 ] );
		}
		else if ( cmd.equals( TIME_SINCE_CMD_NAME ) )
		{
			timeSince( operands.length == 0 ?  LAST : operands[ 0 ] );
		}
		else
		{
		}
	}
}






