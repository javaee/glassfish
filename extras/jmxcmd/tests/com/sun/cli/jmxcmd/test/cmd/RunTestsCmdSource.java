/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/cmd/RunTestsCmdSource.java,v 1.1 2003/12/09 01:53:11 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/12/09 01:53:11 $
 */
 
package com.sun.cli.jmxcmd.test.cmd;


import com.sun.cli.jcmd.framework.CmdSource;


/**
	Run the JUnit tests.
 */
public class RunTestsCmdSource implements CmdSource
{
		public
	RunTestsCmdSource(  )
	{
	}
	
	
	private final static Class [] CMD_CLASSES = 
	{
		RunTestsCmd.class,
	};
	
		public Class[]
	getClasses( )
	{
		return( CMD_CLASSES );
	}
}






