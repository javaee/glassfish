/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdEnvKeys.java,v 1.6 2004/01/15 20:33:27 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2004/01/15 20:33:27 $
 */
 

package com.sun.cli.jcmd.framework;



/**
	Keys for options supported/required by the framework.
 */
public final class CmdEnvKeys
{
	private CmdEnvKeys()	{/*disallow*/}
	
	/**
		CmdEnv key for the tokens of the command being executed.  The creator
		of the instance must place the tokens into the environment.
		
		Tokens must be a String[]
	 */
	public final static String	TOKENS		= "TOKENS";
	
	/**
		CmdEnv key for the CmdHistory
	 */
	public final static String	CMD_HISTORY	= "CMD_HISTORY";
	
	/**
		CmdEnv key for the CmdAliasMgr
	 */
	public final static String	CMD_ALIAS_MGR	= "CMD_ALIAS_MGR";
	
	/**
		CmdEnv key for the CmdRunner
	 */
	public final static String	CMD_RUNNER	= "CMD_RUNNER";
	
	/**
		CmdEnv key for the CmdFactory
	 */
	public final static String	CMD_FACTORY	= "CMD_FACTORY";
	
	/**
		CmdEnv key for the list of CmdSource classes (String)
	 */
	public final static String	COMMAND_SOURCES	= "COMMAND_SOURCES";
	
	
	/**
		CmdEnv key for the CmdOutput that should be used when emitting
		output.
	 */
	public final static String	CMD_OUTPUT	= "CMD_OUTPUT";
	
	
	/**
		CmdEnv key which sets debug status
	 */
	public final static String	DEBUG		= "debug";
	
	/**
		CmdEnv key which sets verbose status.
	 */
	public final static String	VERBOSE		= "verbose";
	
	
	/**
		CmdEnv key for the CmdEventListenerList
	 */
	public final static String	CMD_EVENT_MGR	= "CMD_EVENT_MGR";
	
	/**
		CmdEnv key for classpath of additional classes.
	 */
	public final static String	ADDITIONAL_CLASSPATH	= "ADDITIONAL_CLASSPATH";
	
	
	/**
		CmdEnv key for UnknownCmdHelper
	 */
	public final static String	UNKNOWN_CMD_HELPER	= "UNKNOWN_CMD_HELPER";
	
	
	/**
		CmdEnv key for optional startup file. If present, the script will be 
		executed upon startup.
	 */
	public final static String	STARTUP_SCRIPT	= "STARTUP_SCRIPT";
};
	