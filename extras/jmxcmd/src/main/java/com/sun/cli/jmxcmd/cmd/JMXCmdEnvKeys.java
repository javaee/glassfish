/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/JMXCmdEnvKeys.java,v 1.4 2004/05/01 01:09:48 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/05/01 01:09:48 $
 */
 

package com.sun.cli.jmxcmd.cmd;



/**
	Keys for options supported/required by the framework.
 */
public final class JMXCmdEnvKeys
{
	private JMXCmdEnvKeys()	{/*disallow*/}
	
	/**
		Key for the CLISupportMBeanProxy object in the CmdEnv.
	 */
	public final static String	PROXY		= "PROXY";
	
	/**
		Key for the MBeanServerConnection object in the CmdEnv.
	 */
	public final static String	CONNECTION_SOURCE	= "CONNECTION_SOURCE";
	
	/**
		Key for String of target(s) denoting default targets (space separated)
	 */
	public final static String	TARGETS		= "TARGET";
	
	/**
		Key for ObjectNames last resolved from resolveTargets()
	 */
	public final static String	LAST_RESOLVED_TARGETS		= "LAST_TARGETS";
	
	/**
		Name of auto-maintained alias for last resolved targets.
	 */
	public final static String	LAST_RESOLVED_ALIAS		= "last-targets";
	
	
	/**
		Name of alias maintained by 'target' command denoting the
		default targets
	 */
	public final static String	TARGETS_ALIAS	= "target";
	
	/**
		Prefix for the keys in the CmdEnv of all connection configurations (String)
	 */
	public final static String	CONNECT_NAME_PREFIX	= "CONNECT_";
	
	/**
		Key for the AliasMgr in the CmdEnv.
	 */
	public final static String	ALIAS_MGR	= "ALIAS_MGR";
	
	/**
		Key for the list of JMXConnectorProvider classnames (String), comma-separated
	 */
	public final static String	PROVIDERS	= "PROVIDERS";
	
	/**
		Key for the HashMap of MBeanServers, keys are user-assigned names from
		creation of the MBeanServer.
	 */
	public final static String	MBEAN_SERVERS	= "MBEAN-SERVERS";
	
	/**
		Key for the ConnectionMgr.
	 */
	public final static String	CONNECTION_MGR	= "CONNECTION_MGR";
	
	
	/**
		Key for the InvokeResult[] from the last operation invoked.
	 */
	public final static String	INVOKE_RESULT	= "INVOKE_RESULT";
	
	/**
		Key for the ResultsForGetSet[] from the last get.
	 */
	public final static String	GET_RESULT	= "GET_RESULT";
	
	/**
		Key for the ResultsForGetSet[] from the last set.
	 */
	public final static String	SET_RESULT	= "SET_RESULT";
	
	/**
		Key for the InspectResult
		[] from the last set.
	 */
	public final static String	INSPECT_RESULT	= "INSPECT_RESULT";
	
	/**
		Key for the find command last result
		A Set of ObjectNames
	 */
	public final static String	FIND_RESULT	= "FIND_RESULT";
	
	/**
		Name of alias maintained by 'find' command for the results
		of the last find.
	 */
	public final static String	LAST_FOUND_ALIAS	= "last-found";
	
	
	/**
		CmdEnv key/meta-property which decides whether to debug the MBeanServerConnection
	 */
	public final static String	DEBUG_CONNECTION		= "debug-connection";
};


