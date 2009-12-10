/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/GetCmd.java,v 1.9 2004/01/30 07:58:10 llc Exp $
 * $Revision: 1.9 $
 * $Date: 2004/01/30 07:58:10 $
 */
 
package com.sun.cli.jmxcmd.cmd;


import com.sun.cli.jmxcmd.support.ResultsForGetSet;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import com.sun.cli.jcmd.framework.IllegalUsageException;


/**
	Get MBean Attributes.
 */
public class GetCmd extends GetSetCmd
{
	final static String	GET				= "get";
	
		public
	GetCmd( final CmdEnv env )
	{
		super( env );
	}
	
	
	static final class GetCmdHelp extends CmdHelpImpl
	{
		public	GetCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	GET_NAME		= "get";
		private final static String	GET_SYNOPSIS	= "get: display one or more attributes on the specified target(s).";
		private final static String	GET_TEXT		= 
	"Specify the attributes in a comma-separated list. " +
	"The following special targets are also available:\n"+
	"*  all attributes\n" +
	"*r all read-only attributes\n" +
	"*w all writeable attributes\n" +
	"\n'get' Examples: \n" +
	"get * *                    -- gets all attributes on all MBeans\n" +
	"get Count,Timeout MyMBean  -- gets the Count and Timeout attributes on the MBean 'MyMBean'\n";
		
		
		public String	getSynopsis()	{	return( GET_SYNOPSIS ); }
		public String	getText()		{	return( GET_TEXT ); }
	}


		public CmdHelp
	getHelp()
	{
		return( new GetCmdHelp() );
	}
	
	
	public static final OptionInfo	VERBOSE_OPTION = createVerboseOption();
	
	private static final CmdInfo	GET_INFO	= new CmdInfoImpl( GET,
		new OptionsInfoImpl( new OptionInfo[] { VERBOSE_OPTION } ),
		new OperandsInfoImpl( ATTR_LIST_ARG + " " + TARGET_LIST_ARG, 1));
			
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( GET_INFO ) );
	}

	
		protected void
	executeInternal()
		throws Exception
	{
		final String			cmd	= getSubCmdNameAsInvoked();
		
		String  	attributes	= null;
		String []	targets		= null;
		
		final boolean verbose	= getVerbose();
			
		if ( cmd.equalsIgnoreCase( GET )  )
		{
			attributes	= getAttributes();
			targets		= getTargets();
			
			if ( targets == null )
			{
				throw new IllegalUsageException( cmd, "no targets have been specified" );
			}
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
		
		printDebug( "Getting attributes: " + attributes );
		printDebug( "Against targets: " + ArrayStringifier.stringify( targets, "\n" ) );
		
		establishProxy();
		final ResultsForGetSet []	results	= getProxy().mbeanGet( attributes, targets );
		
		for ( int i = 0; i < results.length; ++i )
		{
			final ResultsForGetSet	result	= results[ i ];
			
			if ( result.getAttributes().size() != 0 || verbose )
			{
				println( SmartStringifier.toString( result ) );
				println( "" );
			}
		}
		
		envPut( JMXCmdEnvKeys.GET_RESULT, results, false );
	}
}

