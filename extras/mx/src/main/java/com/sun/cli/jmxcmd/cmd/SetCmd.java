/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/SetCmd.java,v 1.6 2004/02/06 02:11:23 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/02/06 02:11:23 $
 */
 
package com.sun.cli.jmxcmd.cmd;


import com.sun.cli.jmxcmd.support.ResultsForGetSet;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.*;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

/**
	Set MBean Attributes.
 */
public class SetCmd extends GetSetCmd
{
		public
	SetCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class SetCmdHelp extends CmdHelpImpl
	{
		public	SetCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS		= "set one or more attributes on the specified target(s)";
		private final static String	SET_TEXT		= 
	"Specify a comma-separated list of name-value pairs.\n" +
	"\n'set' Examples: \n" +
	"set Timeout=10,Count=20 MyMBean  -- sets the Timeout attribute to 10 and Count attribute to 20 " +
	"on the MBean 'MyMBean'\n";
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SET_TEXT ); }
	}

	private final static String	SET_NAME		= "set";	


		public CmdHelp
	getHelp()
	{
		return( new SetCmdHelp() );
	}
	
		
	private static final CmdInfo	SET_INFO	= new CmdInfoImpl( SET_NAME,
		new OperandsInfoImpl( "<name>=<value>[,<name>=<value>]* [target [target]*]", 1));
			
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( SET_INFO ) );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String  attributes	= getAttributes();
		final String []	targets		= getTargets();
		
		establishProxy();
		final ResultsForGetSet []	results	= getProxy().mbeanSet( attributes, targets );
		println( ArrayStringifier.stringify( results, "\n\n" ) );
		
		envPut( JMXCmdEnvKeys.SET_RESULT, results, false );
	}
}

