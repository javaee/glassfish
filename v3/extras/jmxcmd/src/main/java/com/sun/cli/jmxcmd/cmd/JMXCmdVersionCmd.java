/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/JMXCmdVersionCmd.java,v 1.6 2004/01/10 03:02:31 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/01/10 03:02:31 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import com.sun.cli.jcmd.framework.CmdBase;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.VersionCmd;

import com.sun.cli.jcmd.util.cmd.CmdInfos;


/**
	This command should be subclassed by the CLI making use of the framework.
 */
public class JMXCmdVersionCmd extends VersionCmd
{
		public
	JMXCmdVersionCmd( final CmdEnv env )
	{
		super( env );
	}
		public static CmdInfos
	getCmdInfos(  )
	{
		return( VersionCmd.getCmdInfos( ) );
	}
	
	public static final String VERSION_STRING	= 
	"version 2.0 alpha 1";
	
	public static final String COPYRIGHT_STRING	= 
	"Copyright 2003-2009 Sun Microsystems, Inc.\n" +
	"All rights reserved.  Use is subject to license terms.\n";

		protected void
	printVersion()
	{
		println( VERSION_STRING );
		println( COPYRIGHT_STRING );
	}
}
