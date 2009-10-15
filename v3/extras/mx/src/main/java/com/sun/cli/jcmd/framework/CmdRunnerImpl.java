/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdRunnerImpl.java,v 1.3 2004/01/09 22:17:26 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/01/09 22:17:26 $
 */
package com.sun.cli.jcmd.framework;

import org.glassfish.admin.amx.util.ExceptionUtil;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import org.glassfish.admin.amx.util.DebugState;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
 
public class CmdRunnerImpl implements CmdRunner, CmdRunner.Hook
{
	final CmdEnv			mCmdEnv;
	final CmdFactory		mFactory;
	final CmdRunner.Hook	mHook;
	
	public void	preExecute( String cmdName, String[] tokens )	{}
	public void	postExecute( String cmdName, String[] tokens, int errorCode)	{}
	
		public
	CmdRunnerImpl(
		CmdFactory		factory,
		final CmdEnv	env,
		CmdRunner.Hook	hook
		)
	{
		mCmdEnv		= env;
		mFactory	= factory;
		mHook		= hook;
	}
	
		public
	CmdRunnerImpl(
		CmdFactory		factory,
		final CmdEnv	env
		)
	{
		mCmdEnv		= env;
		mFactory	= factory;
		mHook		= this;
	}
	
	public static final String	VERSION_CMD_NAME	= "version";
	public static final String	HELP_CMD_NAME		= "help";
	
	
		private Cmd
	prepareCmd( final String cmdName, final String [] tokens )
		throws Exception
	{
		mCmdEnv.put( CmdEnvKeys.TOKENS, tokens, false);
		
		final Cmd	cmd	= mFactory.createCmd( cmdName, mCmdEnv );
		if ( cmd == null )
		{
			throw new CmdNotFoundException( cmdName, ArrayStringifier.stringify( tokens, " " ));
		}
		
		return( cmd );
	}
	
	
		private void
	executeSquelch( final Cmd cmd )
	{
		try
		{
			cmd.execute( );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
		private void
	executeCmdForSubCmd( String cmdName, String operand )
	{
		try
		{
			final Cmd	cmd	= prepareCmd( cmdName, new String[] { cmdName, operand } );
			
			cmd.execute();
		}
		catch( Exception e )
		{
		}
	}
	
	/**
		Tell the user what happened

		@param cmd	the Cmd which caused the problem
		@param e	the exception to handle
	 */
		protected int
	handleCmdException( final Cmd cmd, final CmdException e )
	{
		assert( cmd != null );
		assert( e != null );
		
		int errorCode	= CmdException.GENERAL_ERROR_CODE;
		
		if ( e instanceof HelpOptionException )
		{
			executeCmdForSubCmd( HELP_CMD_NAME, ((CmdException)e).getSubCmdName() );
			errorCode	= 0;	// exit normally
		}
		else if ( e instanceof VersionOptionException )
		{
			executeCmdForSubCmd( VERSION_CMD_NAME, ((CmdException)e).getSubCmdName() );
			errorCode	= 0;	// exit normally
		}
		else
		{
			errorCode	= ((CmdException)e).getErrorCode();
			
			cmd.printError( e.getErrorCode() + " = " + e.getErrorCodeName() + " (" + e.getMessage() + ")");
			if ( e instanceof InvalidCommandException )
			{
				cmd.println( "Use 'help' for help on available commands." );
			}
			else if ( e.isUsageError() )
			{
				final CmdHelp	help	= cmd.getHelp();
				
				if ( help != null )
				{
					cmd.println( help.toString() );
				}
			}
		}
		
		return( errorCode );
	}
		
		protected static String
	quote( String s )
	{
		return( "\"" + s + "\"" );
	}
	
	/**
		Tell the user what happened

		@param e	the exception to handle
	 */
		protected int
	handleException( final Cmd cmd, final Exception e )
	{
		CmdOutput	output	= cmd;
		if ( output == null )
		{
			output	= new CmdOutputImpl( System.out, System.err );
		}
	
		int errorCode	= CmdException.GENERAL_ERROR_CODE;
		
		if ( e instanceof java.io.IOException)
		{
			errorCode	= CmdException.IO_EXCEPTION;
			output.printError( "connection died: " +  e.toString() );
		}
		else if ( e instanceof IllegalOptionException)
		{
			errorCode	= CmdException.ILLEGAL_USAGE_ERROR;
			output.printError( "ERROR: " + e.getMessage() );
		}
		else if ( e instanceof CmdNotFoundException )
		{
			final CmdNotFoundException	c	= (CmdNotFoundException)e;
			
			errorCode	= c.getErrorCode();
			output.printError( "command not found: " + quote( c.getSubCmdName() ) );
		}
		else if ( e instanceof CmdException && cmd != null)
		{
			errorCode	= handleCmdException( cmd, (CmdException)e );
		}
		else
		{
			output.printError( "ERROR: exception of type: " +
				e.getClass().getName() + ", msg = " + e.getMessage() );
				
			getOutput( cmd ).printDebug( ExceptionUtil.getStackTrace( e ) );
		}
		
		
		return( errorCode );
	}
	
		CmdOutput
	getOutput( Cmd cmd )
	{
		CmdOutput	output	= cmd;
		
		if ( output == null )
		{
			output	= (CmdOutput)mCmdEnv.get( CmdEnvKeys.CMD_OUTPUT );
			if ( output == null )
			{
				// should have been one, so create one that outputs everything
				output	= new CmdOutputImpl( System.out, System.err,
							System.out, new DebugState.Impl( true ));
			}
		}
		
		return( output );
	}
	
		CmdHistory
	getCmdHistory()
	{
		return( (CmdHistory)mCmdEnv.get( CmdEnvKeys.CMD_HISTORY ) );
	}
	
	/**
		Instantiate the appropriate command, populate the environment
		with the tokens, and execute it.
	 */
		public int
	execute( final String cmdName, final String [] tokens )
	{
		int	errorCode	= 0;
		
		getCmdHistory().addCmd( tokens );
		
		mHook.preExecute( cmdName, tokens );
		
		Cmd		cmd	= null;
		try
		{
			cmd	= prepareCmd( cmdName, tokens );
			cmd.execute( );
		}
		catch( Exception e )
		{
			errorCode	= handleException( cmd, e );
		}
		catch( Throwable t )
		{
			getOutput( cmd ).printError( "throwable: " + t.getClass().getName() +
				"(" + t.getMessage() + 
				")\n" + ExceptionUtil.getStackTrace( t ) );
			errorCode	= CmdException.GENERAL_ERROR_CODE;
		}

		
		if ( mCmdEnv.needsSave() )
		{
			try
			{
				mCmdEnv.save();
			}
			catch( Exception e )
			{
				getOutput( cmd ).printDebug( "Error saving environment: " + e.getMessage() );
			}
		}
		
		return( errorCode );
	}
};

 
 
 