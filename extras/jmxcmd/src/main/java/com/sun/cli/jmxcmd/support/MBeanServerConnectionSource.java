/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/MBeanServerConnectionSource.java,v 1.1 2003/11/26 02:47:42 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/26 02:47:42 $
 */
 

package com.sun.cli.jmxcmd.support;

import javax.management.MBeanServerConnection;

/** 
	A name-based source of MBeanServerConnection
 */
public interface MBeanServerConnectionSource
{
	/**
		@param name free-form String denoting a named connection
	 */
	public MBeanServerConnection	getMBeanServerConnection( String name ) throws java.io.IOException;
};

