/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/MBeanServerIniter.java,v 1.1 2003/11/21 21:23:44 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:44 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import javax.management.MBeanServer;

/**
	Used by mbs-start command to initialize the MBeanServer
 */
public interface MBeanServerIniter
{
	public void	initMBeanServer( String serverName, MBeanServer server ) throws Exception;
}






