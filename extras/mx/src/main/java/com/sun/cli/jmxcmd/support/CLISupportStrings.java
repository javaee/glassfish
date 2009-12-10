/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/CLISupportStrings.java,v 1.1 2003/11/21 21:23:48 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:48 $
 */
 

package com.sun.cli.jmxcmd.support;

/**
	Strings used by CLISupport proxy and related classes
 */
public final class CLISupportStrings
{
	public final static String	ALL_TARGET						= "*:*";
	public final static String	CLI_SUPPORT_TARGET				= "system:type=cli,name=cli-support";
	public final static String	ALIAS_MGR_TARGET				= "system:type=AliasMgr";
	
	public final static String	CLI_SUPPORT_TESTEE_TARGET			= "Test:name=support-test,type=test";
	public final static String	CLI_SIMPLE_TESTEE_TARGET		= "Test:name=simple-test,type=test";
	
	public final static char [] 	kObjectNameChars	= { ',', '=', ':' };
}
