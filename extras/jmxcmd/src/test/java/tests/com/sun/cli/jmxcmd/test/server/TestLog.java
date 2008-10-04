/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/server/TestLog.java,v 1.2 2003/11/21 22:15:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/21 22:15:45 $
 */
 
package com.sun.cli.jmxcmd.test.server;

public interface TestLog
{
	public void	print( Object msg );
	public void	println( Object msg );
};

