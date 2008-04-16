/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/ImmutableConnectionSource.java,v 1.1 2004/01/30 20:59:08 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/01/30 20:59:08 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import javax.management.MBeanServerConnection;

/**
	A source of an MBeanServerConnection.
 */
public final class ImmutableConnectionSource implements ConnectionSource
{
	final MBeanServerConnection	mConnection;
	
		public
	ImmutableConnectionSource( MBeanServerConnection	connection )
	{
		mConnection	= connection;
	}

	/**
		Return the connection source (the one that this object was constructed with)
	 */
		public MBeanServerConnection
	getMBeanServerConnection( boolean forceNew )
	{
		return( mConnection );
	}
}



