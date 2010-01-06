/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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

 
 
 
