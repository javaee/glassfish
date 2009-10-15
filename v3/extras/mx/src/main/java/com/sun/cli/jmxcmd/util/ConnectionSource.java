/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.util;

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



