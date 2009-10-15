/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/EchoCmd.java,v 1.6 2003/12/19 01:48:07 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2003/12/19 01:48:07 $
 */
 
package com.sun.cli.jcmd.framework;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
	Echo operands to stdout
 */
public class EchoCmd extends CmdBase
{
		public
	EchoCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class EchoCmdHelp extends CmdHelpImpl
	{
			public
		EchoCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS		= "print output to stdout";
		static final String	SOURCE_TEXT		=
		"Prints each operand to stdout, separated by white-space.  If the first operand starts with " +
		"the '-' character, then you must preceed it with the end-of-operands token '--'";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( SOURCE_TEXT ); }
	}
		public CmdHelp
	getHelp()
	{
		return( new EchoCmdHelp() );
	}

	
	
	public final static String	NAME	= "echo";
	
	private final static CmdInfo	CMD_INFO	=
		new CmdInfoImpl( NAME, null, new OperandsInfoImpl( "message", 0) );
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( CMD_INFO )  );
	}
	
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		
		println( ArrayStringifier.stringify( operands, " " ) );
	}
}






