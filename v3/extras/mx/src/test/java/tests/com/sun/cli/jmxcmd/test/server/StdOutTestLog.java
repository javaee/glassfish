/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/server/StdOutTestLog.java,v 1.2 2003/11/21 22:15:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/21 22:15:45 $
 */
 
package com.sun.cli.jmxcmd.test.server;

public final class StdOutTestLog implements TestLog
{
		public void
	print( Object msg )
	{
		System.out.print( msg.toString() );
	}
	
		public void
	println( Object msg )
	{
		System.out.println( msg.toString() );
	}
};

