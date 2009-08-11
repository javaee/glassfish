/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/TargetCmd.java,v 1.7 2004/02/06 02:11:23 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2004/02/06 02:11:23 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import javax.management.ObjectName;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionDependency;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.IllegalUsageException;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;


/**
	Manages the default targets.
 */
public class TargetCmd extends JMXCmd
{
		public
	TargetCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class TargetCmdHelp extends CmdHelpImpl
	{
		public	TargetCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS		= "display or set the target MBean(s)";
		private final static String	TARGET_TEXT		=
	"With no arguments, displays the current target(s).  Otherwise, the targets are taken as specified. " +
	"If the --clear option is given, then the default target is cleared.";

		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TARGET_TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new TargetCmdHelp() );
	}
	
	private final static String	NAME				= "target";
	private final static String	CLEAR_TARGET_NAME	= "clear-target";
		
		
	private final static CmdInfo	TARGET_INFO	=
		new CmdInfoImpl( NAME, TARGETS_OPERAND_INFO );
	private final static CmdInfo	CLEAR_TARGET_INFO	=
		new CmdInfoImpl( CLEAR_TARGET_NAME );
		
	
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( TARGET_INFO, CLEAR_TARGET_INFO) );
	}


		void
	displayExistingTarget()
	{
		println( "Targets:\n" +  envGet( JMXCmdEnvKeys.TARGETS ) );
	}
	
		void
	warnEmptyTargets( final String [] targets )
		throws Exception
	{
		// issue warning if some targets could not be resolved to anything
		for ( int i = 0; i < targets.length; ++i )
		{
			final String	target	= targets[ i ];
			
			final ObjectName []	objects	=
				resolveTargets( getProxy(), new String [] { target } );
				
			if ( objects.length == 0 )
			{
				println( "WARNING: target " +
					target + " does not resolve to any objects" );
			}
		}
	}
	
		void
	setTargets( final String [] targets )
		throws Exception
	{
		putEnvTargets( targets );
		warnEmptyTargets( targets );
		
		getAliasMgr().deleteAlias( JMXCmdEnvKeys.TARGETS_ALIAS );
		getAliasMgr().createAlias( JMXCmdEnvKeys.TARGETS_ALIAS, ArrayStringifier.stringify( targets, " " ) );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		final String	cmd	= getSubCmdNameAsInvoked();
		
		assert( operands != null );
		
		if ( cmd.equals( CLEAR_TARGET_NAME ) )
		{
			if ( operands.length != 0 )
			{
				throw new IllegalUsageException( CLEAR_TARGET_NAME + " takes no operands" );
			}

			envRemove( JMXCmdEnvKeys.TARGETS );
			getAliasMgr().deleteAlias( JMXCmdEnvKeys.TARGETS_ALIAS );
		}
		else
		{
		
			if ( operands.length == 0 )
			{
				displayExistingTarget();
			}
			else
			{
				establishProxy();
				setTargets( operands );
			}
		}
	}
}



