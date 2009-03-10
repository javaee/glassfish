/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/cmd/com/sun/enterprise/s1as8/cmd/DottedNameProxyFactory.java,v 1.2 2004/01/31 04:44:02 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/01/31 04:44:02 $
 */
 
package com.sun.enterprise.s1as8.cmd;

import javax.management.ObjectName;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServerConnection;

public final class DottedNameProxyFactory
{
	private static final String		MBEAN_NAME	= "com.sun.appserv:name=dotted-name-get-set,type=dotted-name-support";
	private static ObjectName		MBEAN_OBJECTNAME	= initGetSetObjectName();
	
		private final static ObjectName
	initGetSetObjectName()
	{
		try
		{
			return( new ObjectName( MBEAN_NAME ) );
		}
		catch( MalformedObjectNameException e )
		{
			assert( false );	// can't happen
		}
		
		return( null );
	}
	
	private DottedNameProxyFactory( )	{}
	
		public static DottedNameProxy
	createProxy( MBeanServerConnection conn )
	{
		return( (DottedNameProxy)MBeanServerInvocationHandler.newProxyInstance(
			conn, MBEAN_OBJECTNAME, DottedNameProxy.class, false ) );
	}
}







