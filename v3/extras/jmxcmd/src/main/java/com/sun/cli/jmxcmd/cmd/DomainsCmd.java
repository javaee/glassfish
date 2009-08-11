/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/DomainsCmd.java,v 1.7 2004/02/06 02:11:22 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2004/02/06 02:11:22 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
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
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;




/**
	Displays the available JMX domains.
 */
public class DomainsCmd extends JMXCmd
{
		public
	DomainsCmd( final CmdEnv env )
	{
		super( env );
	}
	
	static final class DomainsCmdHelp extends CmdHelpImpl
	{
		private final static String	DOMAINS_SYNOPSIS	= "display the available domains";
		private final static String	DOMAINS_TEXT		= "Displays the availabled domains.";

		public	DomainsCmdHelp()	{ super( getCmdInfos() ); }
		
		public String	getSynopsis()	{	return( formSynopsis( DOMAINS_SYNOPSIS ) ); }
		public String	getText()		{	return( DOMAINS_TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new DomainsCmdHelp() );
	}
	
	final static String	DOMAINS_NAME				= "domains";
	final static String	GET_DEFAULT_DOMAIN_NAME		= "get-default-domain";
	
	private final static CmdInfo	DOMAINS_INFO			=
				new CmdInfoImpl( DOMAINS_NAME );
	private final static CmdInfo	GET_DEFAULT_DOMAIN_INFO	=
				new CmdInfoImpl( GET_DEFAULT_DOMAIN_NAME );
	
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( DOMAINS_INFO, GET_DEFAULT_DOMAIN_INFO ) );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd	= getSubCmdNameAsInvoked();
		
		establishProxy();
		if ( cmd.equals( DOMAINS_NAME ) )
		{
			final String [] domains	= getProxy().mbeanDomains();
			println( ArrayStringifier.stringify( domains, "\n" ) );
		}
		else if ( cmd.equals( GET_DEFAULT_DOMAIN_NAME ) )
		{
			final String domain	= getProxy().mbeanGetDefaultDomain();
			println( domain );
		}
	}
}






