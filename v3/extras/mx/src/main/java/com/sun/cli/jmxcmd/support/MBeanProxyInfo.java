/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/MBeanProxyInfo.java,v 1.2 2004/04/24 02:10:18 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/04/24 02:10:18 $
 */
 

/*
	The interface for the manager of MBean proxies.
 */
 
package com.sun.cli.jmxcmd.support;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;

public final class MBeanProxyInfo
{
	public final ObjectName				mTarget;
	public final ObjectName				mProxyName;
	public final MBeanServerConnection	mConn;
	
		public
	MBeanProxyInfo(
		ObjectName			proxyName,
		ObjectName			target,
		MBeanServerConnection conn  )
	{
		mConn				= conn;
		mTarget				= target;
		mProxyName			= proxyName;
	}
	
		public String
	toString()
	{
		return( mProxyName + " => " + mTarget );
	}
};
