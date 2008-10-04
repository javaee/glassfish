/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/ConnectionSource.java,v 1.1 2004/01/29 20:25:46 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/01/29 20:25:46 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import javax.management.MBeanServerConnection;

/**
	A source of an MBeanServerConnection.
 */
public interface ConnectionSource
{
	/**
		Return a valid MBeanServerConnection, making a new connection if necessary, or
		returning an existing one if still valid.
		
		Should not be called frequently, as the check for validity will make a remote
		call.
	 */
	public MBeanServerConnection	getMBeanServerConnection( boolean forceNew )
										throws java.io.IOException;
}



