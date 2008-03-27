/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanProxyFactory.java,v 1.5 2004/02/13 01:58:52 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2004/02/13 01:58:52 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.NotificationBroadcaster;

import java.lang.reflect.Proxy;

/**
	Factory for creating MBean proxies
 */
public final class MBeanProxyFactory
{
	private	MBeanProxyFactory()	{}
   	
	/**
		Instantiates a new proxy using the default AttributeNameMangler and with any desired number
		of interfaces.  If you want NotificationBroadcaster as one of the interfaces, you must
		supply it in the list.
		
		@param connectionSource 	the connection to use
		@param objectName	the target MBean which will be invoked by the proxy
		@param interfaceClasses	all interfaces the proxy should implement
		@param mangler		the optional name mangler.
		
		@return the new Proxy implementing the specified interface
	 */
		public static Object
	newProxyInstance(
		ConnectionSource		connectionSource,
		ObjectName				objectName,
		Class[]					interfaceClasses,
		AttributeNameMangler	mangler )
		throws IOException
	{
		final MBeanProxyHandler	handler	= new MBeanProxyHandler( connectionSource, objectName, mangler );
		final ClassLoader		classLoader	= interfaceClasses[ 0 ].getClassLoader();
		
		final Object proxy	= Proxy.newProxyInstance( classLoader, interfaceClasses, handler);
		
		return( proxy );
	}
	
	
	/**
		Instantiates a new proxy using the default AttributeNameMangler.
		
		@param connectionSource 		the connection to use
		@param objectName				the target MBean which will be invoked by the proxy
		@param interfaceClass			all interfaces the proxy should implement
		@param notificationBroadcaster	set to true if the proxy should implement NotificationBroadcaster
		@return the new Proxy implementing the specified interface
	 */
		public static Object
	newProxyInstance(
		ConnectionSource		connectionSource,
		ObjectName				objectName,
		Class					interfaceClass,
		boolean					notificationBroadcaster  )
		throws IOException
	{
		return( newProxyInstance( connectionSource, objectName,
					interfaceClass, notificationBroadcaster, null ) );
	}
	
	
	/**
		Instantiates a new proxy using the specified AttributeNameMangler.
		The name mangler should produce names that match those of the interface.
		
		
		@param connectionSource 	the connection to use
		@param objectName	the target MBean which will be invoked by the proxy
		@param interfaceClass	all interfaces the proxy should implement
		@param notificationBroadcaster		set to true if the proxy should implement NotificationBroadcaster
		@param mangler		the mangler
		@return the new Proxy implementing the specified interface
	 */
		public static Object
	newProxyInstance(
		ConnectionSource		connectionSource,
		ObjectName				objectName,
		Class					interfaceClass,
		boolean					notificationBroadcaster,
		AttributeNameMangler	mangler )
		throws IOException
	{
		Class[]	interfaces	= null;
		if ( notificationBroadcaster )
		{
			interfaces	= new Class[] { interfaceClass, NotificationBroadcaster.class };
		}
		else
		{
			interfaces	= new Class[] { interfaceClass };
		}
		
		return( newProxyInstance( connectionSource, objectName, interfaces, mangler ) );
	}
}





