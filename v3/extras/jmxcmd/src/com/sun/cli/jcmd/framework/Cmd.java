/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/Cmd.java,v 1.4 2004/07/12 19:42:54 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/07/12 19:42:54 $
 */
 

package com.sun.cli.jcmd.framework;

import com.sun.cli.jcmd.util.cmd.CmdInfo;

/**
	A command may be plugged into jmxcmd so long as it implements
	this interface and provides a constructor with the following 
	signature:
	
	cmd( CmdEnv env )
	
	Most commands will want to extend CmdBase or JMXCmd to inherit
	much functionality, rather than implementing this interface
	directly.
 */
public interface Cmd extends CmdOutput
{
	/**
		Execute the command.  The command should throw a CmdException
		with an appropriate error code if an error occurs.
		
		Alternately, it may allow exceptions to propogate out; in this
		case a generic error code will be supplied.
	 */
	public void	execute( ) throws Exception;
	
	/**
		Get a CmdHelp describing for this command.  If there is none,
		return null.
	 */
	public CmdHelp		getHelp( );
	
	/*
		Each Cmd should also implement:
		
		public static CmdInfo[]		getCmdInfos( String name );
	 */
};

