/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/CmdMgr.java,v 1.3 2005/12/25 03:45:29 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:29 $
 */
 
package com.sun.cli.jmx.cmd;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.sun.cli.util.stringifier.ArrayStringifier;
import com.sun.cli.util.stringifier.SmartStringifier;
import com.sun.cli.util.LineReaderImpl;
import com.sun.cli.util.ClassUtil;
import com.sun.cli.jmx.cmd.CmdFactoryIniter;

public final class CmdMgr
{
	final CmdEnvImpl			mCmdEnv;
	final CmdFactory			mCmdFactory;
	final CmdRunner				mCmdRunner;
	
		private static void
	dm( Object o )
	{
		System.out.println( SmartStringifier.toString( o ) );
	}
	
		public
	CmdMgr(  )
		throws Exception
	{
		mCmdEnv		= new CmdEnvImpl();
		mCmdFactory	= new CmdFactory( );
		mCmdRunner	= new CmdRunnerImpl( mCmdFactory, mCmdEnv );
		
		mCmdEnv.put( CmdBase.ENV_CMD_RUNNER, mCmdRunner, false);
		mCmdEnv.put( CmdBase.ENV_CMD_FACTORY, mCmdFactory, false);
	}


	
		
	private final static Class [] CMDS =
	{
		GetCmd.class,
		SetCmd.class,
		FindCmd.class,
		InspectCmd.class,
		CreateCmd.class,
		DeleteCmd.class,
		ListenCmd.class,
		InvokeCmd.class,
		CreateAliasCmd.class,
		DeleteAliasCmd.class,
		ResolveAliasCmd.class,
		ListAliasesCmd.class,
		TargetCmd.class,
		ConnectCmd.class,
		HelpCmd.class,
		SourceCmd.class,
		CountCmd.class,
		DomainsCmd.class,
		ConfigureCmd.class,
		SetenvCmd.class,
	};
		
		private void
	initCmds()
		throws Exception
	{
		final CmdFactoryIniter	initer = new CmdFactoryIniter( mCmdFactory, CMDS );
		
		// initialize all non-built-in commands
		final ConfigureCmd.ClassList	list =
				new ConfigureCmd.ClassList( (String)mCmdEnv.get( CmdBase.ENV_COMMANDS ) );
		final java.util.Iterator	iter	= list.iterator();
		while ( iter.hasNext() )
		{
			final String	classname	= (String)iter.next();
			
			initer.addMappings( ClassUtil.getClassFromName( classname ) );
		}
	}
	
		void
	handleSingle( String [] args )
		throws Exception
	{
		// one-off command mode.  Only allowed meta option is a connect string "--connect"
		final String	OPTIONS	= "connect,1";
					
		final ArgHelperImpl	argHelper	= new ArgHelperImpl( Arrays.asList( args ).listIterator(),
								new ArgHelperOptionsInfo( OPTIONS ));
		
		String		lines	= "";
		
		final String connectString		= argHelper.getString( "connect", null );
		if ( connectString != null )
		{
			CmdReader.processLine( "connect " + connectString, mCmdRunner );
		}
		
		// now execute the remainder of the line
		final String []	remainder	= argHelper.getOperands();
		
		// the first operand is the command name
		mCmdRunner.execute( remainder[ 0 ], remainder );
	}
		
		void
	handleInteractive(  )
	{
		final LineReaderImpl	lineReader	= new LineReaderImpl( System.in );
		final CmdReader			reader		= new CmdReader();
		
		try
		{
			reader.goInteractive( lineReader, mCmdRunner );
		}
		catch( Throwable t )
		{
			dm( "Exception from processing commands: " + t.getMessage() );
			t.printStackTrace();
		}
	}
	
		public void
	run( final String [] args )
		throws Exception
	{
		final File	file	= JMXAdminFileNames.getPropsFile();
		mCmdEnv.load( file );
		
		initCmds();
		
			
		// if there are no arguments, start up in interactive mode
		if ( args.length == 0 || (args.length == 1 && args[ 0 ].equals( "multi" ) ) )
		{
			handleInteractive();
		}
		else
		{
			handleSingle( args );
		}
		
		// CAUTION: if a ctrl-c is received, the JVM seems to kill us half-way through
		// storing mCmdEnv, which ends up destroying it.  Pause briefly to avoid this (hack).
		Thread.sleep( 100 );
		mCmdEnv.store( file );
	}
}

