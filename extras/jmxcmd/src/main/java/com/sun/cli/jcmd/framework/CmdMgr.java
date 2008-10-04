/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdMgr.java,v 1.6 2004/02/07 00:43:08 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/02/07 00:43:08 $
 */
 
package com.sun.cli.jcmd.framework;

/**
	A CmdMgr is called directly from main() to handle the command.
	
	A CmdMgr must have a constructor taking a java.util.Map.  The Map
	will contain all the available meta-options (see JCmd.CmdEnvKeys).
 */
public interface CmdMgr
{
	/**
		Execute the initial command using the supplied arguments.  With no
		arguments, the CmdMgr should enter interactive mode.
		
		@param args		arguments, as passed by JVM to the main class
		@return			an integer 0-255 indicating the result code
		@throws			an Exception
	 */
	public int 	run( final String [] args ) throws Exception;
	
	/**
		Get the CmdEnv this CmdMgr has.
		
		@return 	the CmdEnv this CmdMgr has
	 */
	public CmdEnv	getEnv();
}

