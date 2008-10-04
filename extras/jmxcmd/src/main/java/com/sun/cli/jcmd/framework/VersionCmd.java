/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/VersionCmd.java,v 1.7 2004/02/06 02:11:22 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2004/02/06 02:11:22 $
 */
 
package com.sun.cli.jcmd.framework;

import com.sun.cli.jcmd.framework.Cmd;
import com.sun.cli.jcmd.framework.CmdBase;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;


/**
	This command should be subclassed by the CLI making use of the framework.
 */
public abstract class VersionCmd extends CmdBase
{
		public
	VersionCmd( final CmdEnv env )
	{
		super( env );
	}
	
	static final class VersionCmdHelp extends CmdHelpImpl
	{
			public
		VersionCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS			= "display the version";
		static final String	VERSION_TEXT		= "Displays the version of this CLI.";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( VERSION_TEXT ); }
	}


		public CmdHelp
	getHelp()
	{
		return( new VersionCmdHelp() );
	}
	
	static final String	VERSION_NAME		= "version";
	
	
	private final static CmdInfo	VERSION_INFO		= new CmdInfoImpl( VERSION_NAME, OperandsInfoImpl.NONE);
	private final static CmdInfo	VERSION_LONG_INFO	= new CmdInfoImpl( VERSION_OPTION_LONG, OperandsInfoImpl.NONE);
	private final static CmdInfo	VERSION_SHORT_INFO	= new CmdInfoImpl( VERSION_OPTION_SHORT, OperandsInfoImpl.NONE);
		
	
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( VERSION_INFO, VERSION_LONG_INFO, VERSION_SHORT_INFO ) );
	}


	
	/**
		Print the version (preferably in the following format):
		
		<name> [(<package-name>)] <version>
		<copyright-notice>
		
	 */
	protected abstract void	printVersion();
	
	/**
		There usually will be a single operand which is the name of the subcommand (if any)
		that was specified.  This command could be invoked as:
		
		--version|-V
		<sub-command> --version|-V
	 */
		protected void
	executeInternal()
		throws Exception
	{
		printVersion();
	}
}
