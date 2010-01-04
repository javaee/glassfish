/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/SourceCmd.java,v 1.11 2004/01/15 20:33:27 llc Exp $
 * $Revision: 1.11 $
 * $Date: 2004/01/15 20:33:27 $
 */
 
package com.sun.cli.jcmd.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import com.sun.cli.jcmd.util.cmd.LineReaderImpl;


import com.sun.cli.jcmd.framework.CmdBase;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdRunner;
import com.sun.cli.jcmd.framework.CmdReader;
import com.sun.cli.jcmd.framework.FileNotFoundException;

import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;


/**
	Reads commands from a file.
 */


public class SourceCmd extends CmdBase
{
		public
	SourceCmd( final CmdEnv env )
	{
		super( env );
	}
	
	private static final class SourceCmdHelp extends CmdHelpImpl
	{
			public
		SourceCmdHelp()
		{ 
			super( getCmdInfos() );
		}
		
		static final String	SYNOPSIS		= "read commands file a file";
		static final String	SOURCE_TEXT		= "Reads commands from a file and executes them.";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SOURCE_TEXT ); }
	}

	static final String	SOURCE_NAME		= "source";
	private final static OptionInfo CONTINUE_ON_ERROR_OPTION	= new OptionInfoImpl( "continue-on-error", "c" );
	private final static CmdInfo	SOURCE_CMD_INFO	=
		new CmdInfoImpl( SOURCE_NAME,
				new OptionsInfoImpl( new OptionInfo[] { CONTINUE_ON_ERROR_OPTION, createVerboseOption() }  ),
				new OperandsInfoImpl( PATH_OPERAND, 1, 1 ) );
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( SOURCE_CMD_INFO ) );
	}

	
		public CmdHelp
	getHelp()
	{
		return( new SourceCmdHelp() );
	}
	
	
	public final static String	SOURCE_CMD_WORKING_DIRECTORY	= "SOURCE_CMD_WORKING_DIRECTORY";
	
		FileInputStream
	openFile( String f )
		throws FileNotFoundException
	{
		final String	cmd	= getSubCmdNameAsInvoked();
		
		FileInputStream	inputStream	= null;
		try
		{
			printDebug( "SourceCmd.openFile: " + f );
			inputStream	= new FileInputStream( f );
		}
		catch ( IOException e )
		{
			final String	wd	= (String)envGet( SOURCE_CMD_WORKING_DIRECTORY );
			
			if ( wd != null )
			{
				try
				{
					final String	wdFile	= wd + "/" + f;
					
					printDebug( "SourceCmd.openFile using wd: " + f );
					inputStream	= new FileInputStream( wdFile  );
				}
				catch( IOException e2 )
				{
					throw new FileNotFoundException( cmd, f );
				}
			}
			else
			{
				throw new FileNotFoundException( cmd, f );
			}
		}
		
		return( inputStream );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd	= getSubCmdNameAsInvoked();
		final String [] operands	= getOperands();
		final String	fileName	= operands[ 0 ];
		final String	startingDir	= (String)envGet( SOURCE_CMD_WORKING_DIRECTORY );
		
		final CmdRunner	cmdRunner	= (CmdRunner)envGet( CmdEnvKeys.CMD_RUNNER );
		
		FileInputStream	inputStream	= openFile( fileName );
		
		final String	directory	= new File( fileName ).getParent();
		printDebug( "SourceCmd.executeInternal: set wd to " + directory );
		envPut( SOURCE_CMD_WORKING_DIRECTORY, directory, false);

		final boolean	continueOnError	=
			getBoolean( CONTINUE_ON_ERROR_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
			
		final LineReaderImpl	lineReader	= new LineReaderImpl( inputStream );
		
		try
		{
			String	line;
			
			final boolean	verbose	= getVerbose();
			while ( (line = lineReader.readLine( null )) != null )
			{
				final CmdReader	reader	= new CmdReader( this, new CmdAliasLineHook( getCmdAliasMgr() ) );
				
				if ( verbose )
				{
					println( "> " + line );
				}
				
				final int	resultCode	= reader.processLine( line, cmdRunner );
				
				if ( resultCode != 0 )
				{
					if ( continueOnError )
					{
						println( "source -- ignoring error " + resultCode + " from command " + quote( line ) );
					}
					else
					{
						// be sure to correctly propogate the resultCode out
						throw new CmdException( cmd,
							"source: stopping due to error " + resultCode + " from command " +
								quote( line ) + " in file " + quote( fileName ),
							resultCode );
					}
				}
				
				println( "" );
			}
		}
		finally
		{
			inputStream.close();
			
			// restore original working directory
			if ( startingDir == null )
			{
				envRemove( SOURCE_CMD_WORKING_DIRECTORY );
			}
			else
			{
				envPut( SOURCE_CMD_WORKING_DIRECTORY, startingDir, false);
			}
		}
		
	}
}






