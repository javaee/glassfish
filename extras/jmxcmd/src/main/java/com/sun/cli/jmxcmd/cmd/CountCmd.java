/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/CountCmd.java,v 1.6 2004/06/09 03:26:48 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/06/09 03:26:48 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import javax.management.ObjectName;

import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import com.sun.cli.jcmd.util.cmd.ArgHelper;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;



/**
	Counts the number of registered MBeans.
 */
public class CountCmd extends JMXCmd
{
		public
	CountCmd( final CmdEnv env )
	{
		super( env );
	}
	
	static final class CountCmdHelp extends CmdHelpImpl
	{
		public	CountCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS		= "count the number of registered MBeans";
		private final static String	COUNT_TEXT		=
		"Counts the number of registered MBeans.\n" +
		"If no operands are supplied, all MBeans are counted, " +
		"otherwise the expression is evaluated and the number of matches is counted.";
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( COUNT_TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new CountCmdHelp() );
	}
	
	final static String	NAME		= "count";
	
	private final static CmdInfo	CMD_INFO	=
		new CmdInfoImpl( NAME, TARGETS_OPERAND_INFO);
	
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( CMD_INFO ) );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		
		establishProxy();
		
		int	numMBeans	= 0;
		if ( operands.length != 0 )
		{
			final CLISupportMBeanProxy	proxy	= getProxy();
			final ObjectName[]	objectNames	= resolveTargets( proxy, operands );
			
			numMBeans	= objectNames.length;
		}
		else
		{
			numMBeans	= getProxy().mbeanCount();
		}
		
		println( "" + numMBeans );
	}
}






