/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/HistoryCmd.java,v 1.15 2004/01/10 00:12:15 llc Exp $
 * $Revision: 1.15 $
 * $Date: 2004/01/10 00:12:15 $
 */
 
package com.sun.cli.jcmd.framework;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.util.misc.StringEscaper;
import org.glassfish.admin.amx.util.ArrayConversion;

/**
	Echo operands to stdout
 */
public class HistoryCmd extends CmdBase
{
		public
	HistoryCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class HistoryCmdHelp extends CmdHelpImpl
	{
			public
		HistoryCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS		= "show history or repeat command(s)";
			
		static final String	SOURCE_TEXT		=
		"The 'history' command displays past commands.\n\n" +
		"The 'repeat' command repeats command(s).  If no command-number is specified, " +
		"then the last command is repeated.  If a command " +
		"number is specifed or a command number range is specified, then those command(s) are repeated\n";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SOURCE_TEXT ); }
	}
		public CmdHelp
	getHelp()
	{
		return( new HistoryCmdHelp() );
	}
	
	public final static String	REPEAT_PREFIX		= "!";
	
	public final static String	REPEAT_NAME				= "repeat";
	public final static String	REPEAT_LAST_NAME		= REPEAT_PREFIX + REPEAT_PREFIX;
	public final static String	HISTORY_NAME			= "history";
	public final static String	CLEAR_HISTORY_NAME		= "clear-history";
	
	
	
	private final static CmdInfo	REPEAT_CMD_INFO	=
		new CmdInfoImpl( REPEAT_NAME, null, new OperandsInfoImpl( "[<first>[ <last>]]", 0, 2) );
		
	private final static CmdInfo	REPEAT_LAST_INFO	=
		new CmdInfoImpl( REPEAT_LAST_NAME );
		
	private final static CmdInfo	HISTORY_INFO	=
		new CmdInfoImpl( HISTORY_NAME, null, new OperandsInfoImpl( "count", 0, 1) );
		
	private final static CmdInfo	CLEAR_HISTORY_INFO	=
		new CmdInfoImpl( CLEAR_HISTORY_NAME );
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( HISTORY_INFO, CLEAR_HISTORY_INFO, REPEAT_CMD_INFO, REPEAT_LAST_INFO) );
	}

		protected OptionsInfo
	getOptionsInfo()
		throws IllegalOptionException, CmdException
	{
		final CmdInfos	infos	= getMyCmdInfos();
		assert( infos != null );
		
		final String	cmdName	= getSubCmdNameAsInvoked();
		
		CmdInfo	info	= infos.get( cmdName );
		if ( info == null )
		{
			assert( cmdName.startsWith( REPEAT_PREFIX ) );
			info	= REPEAT_CMD_INFO;
		}
		
		return( info.getOptionsInfo() );
	}
	
		private boolean
	isRepeatCmd( final String name )
	{
		return( name.startsWith( REPEAT_PREFIX ) ||
			name.equals( REPEAT_NAME ));
	}
	
	
		private void
	repeatCmd( final String[] tokens )
		throws CmdException
	{
		final CmdRunner	cmdRunner	= (CmdRunner)envGet( CmdEnvKeys.CMD_RUNNER );

		println( "Repeating: " + quote( ArrayStringifier.stringify( tokens, " " ) ) );
		final int resultCode	= cmdRunner.execute( tokens[ 0 ], tokens );
		if ( resultCode != 0 )
		{
			throw new CmdException( tokens[ 0 ],
				"repeat of command failed: " +
				quote( ArrayStringifier.stringify( tokens, " " ) ),
				resultCode );
		}
	}
	
		private void
	repeatCmd(
		final CmdHistory		history,
		final CmdHistoryItem	item )
		throws CmdException
	{
		final int	saveLast	= history.getLastCmd().getID();
		
		final String[]	tokens	= item.getTokens();
		final String	cmdName	= tokens[ 0 ];
		if ( isRepeatCmd( cmdName ) )
		{
			final String[]	operands	=
				(String[])ArrayConversion.subArray( tokens, 1, tokens.length -1 );
			
			// repeated repeat commands are relative to their original place
			final CmdHistoryWindow	window	= new CmdHistoryWindow( history, item.getID() );
			handleRepeatCmd( cmdName, window, operands );
		}
		else
		{
			repeatCmd( item.getTokens() );
		}
		
		history.truncate( saveLast );
	}
	
		private void
	repeatCmdRange(
		final CmdHistory	history,
		int					startNumber,
		int					stopNumber )
		throws CmdException
	{
		for( int i = startNumber; i <= stopNumber; ++i )
		{
			if ( i <= history.getLastCmd().getID() )
			{
				repeatCmd( history, history.getCmd( i ) );
			}
			else
			{
				throw new CmdException( getSubCmdNameAsInvoked(), "Command number " + i + " does not exist" );
			}
		}
	}
	
	private final static int	ALL_HISTORY	= 0;
		void
	showHistory( CmdHistory history )
	{
		showHistory( history, ALL_HISTORY );
	}
	
		void
	showHistory( final CmdHistory history, final int howFarBack )
	{
		final CmdHistoryItem[]	cmds	= history.getAll();
		
		int numToShow	= howFarBack == ALL_HISTORY ? cmds.length : howFarBack;
		if ( numToShow > cmds.length )
		{
			numToShow	= cmds.length;
		}

		final int	startIndex	= cmds.length - numToShow;
		for( int i = startIndex; i < cmds.length; ++i )
		{
			final String	cmdString	= cmds[ i ].toString();
			
			println( cmds[ i ].getID() + ": " +
				new StringEscaper( ).escape( cmdString ) );
		}
	}
	
		int
	findCmdFromPrefix( final CmdHistory history, String prefix, int startIndex)
	{
		int	cmdID	= -1;
		
		// search backwards
		for( int i = startIndex; i >= 0; --i )
		{
			final CmdHistoryItem	cmd	= history.getCmd( i );
			if ( cmd.getTokens()[0].startsWith( prefix ) )
			{
				cmdID	= cmd.getID();
				break;
			}
		}
		
		return( cmdID );
	}

		void
	handleRepeatCmd( String cmd, CmdHistory history, String[] operands )
		throws CmdException, IllegalUsageException
	{
		printDebug( "Repeating: " + cmd );
		printDebug( "Tokens: " + ArrayStringifier.stringify( operands, ", " ) );
		
		if (	cmd.equals( REPEAT_NAME ) ||
				cmd.equals( REPEAT_LAST_NAME ) )
		{
			if ( cmd.equals( REPEAT_LAST_NAME ) && operands.length != 0 )
			{
				throw new IllegalUsageException( cmd );
			}
			
			if ( operands.length == 0 )
			{
				// repeat the last command
				final int idToRepeat	= history.getLastCmd().getID() - 1;
				repeatCmdRange( history, idToRepeat, idToRepeat );
			}
			else if ( operands.length == 1 )
			{
				// repeat command X
				final int	cmdNumber	= new Integer( operands[ 0 ] ).intValue();
				repeatCmdRange( history, cmdNumber, cmdNumber );
			}
			else if ( operands.length == 2 )
			{
				// repeat a range of commands
				final int	startNumber	= new Integer( operands[ 0 ] ).intValue();
				final int	stopNumber	= new Integer( operands[ 1 ] ).intValue();
				
				repeatCmdRange( history, startNumber, stopNumber );
			}
			else
			{
				throw new IllegalUsageException( cmd );
			}
		}
		else if ( cmd.startsWith( REPEAT_PREFIX ) )
		{
			if ( operands.length != 0 )
			{
				throw new IllegalUsageException( cmd );
			}
			
			final String	idString	= cmd.substring( 1, cmd.length() );
			
			int	cmdID	= -1;
			try
			{
				cmdID	= new Integer( idString ).intValue();
			}
			catch( NumberFormatException e )
			{
				cmdID	= findCmdFromPrefix( history, idString, history.getLastCmd().getID() - 1);
				if ( cmdID < 0 )
				{
					throw new InvalidCommandException( cmd, "no matching previous command" );
				}
			}
			printDebug( "self #" + history.getLastCmd().getID() );
			printDebug( "repeating #" + cmdID );
			repeatCmdRange( history, cmdID, cmdID );
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String		cmd			= getSubCmdNameAsInvoked();
		final String [] 	operands	= getOperands();
		
		// 'history' includes everything but this command
		final CmdHistory	historyIncludingThis	= getCmdHistory();
								
		if ( cmd.equals( HISTORY_NAME )  )
		{
			// 'history' includes everything but this command
			final CmdHistory	history	= new CmdHistoryWindow( historyIncludingThis,
									historyIncludingThis.getLastCmd().getID() - 1 );
									
			if ( operands.length == 0 )
			{
				showHistory( history );
			}
			else
			{
				showHistory( history, new Integer( operands[ 0 ] ).intValue() );
			}
		}
		else if ( cmd.equals( REPEAT_NAME ) || cmd.equals( REPEAT_LAST_NAME ) ||
					cmd.startsWith( REPEAT_PREFIX )  )
		{
			handleRepeatCmd( cmd, historyIncludingThis, operands );
		}
		else if ( cmd.equals( CLEAR_HISTORY_NAME ) )
		{
			historyIncludingThis.clear();
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
	}
}






