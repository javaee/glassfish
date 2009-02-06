/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/MBeanProxyMgr.java,v 1.9 2004/05/10 23:22:47 llc Exp $
 * $Revision: 1.9 $
 * $Date: 2004/05/10 23:22:47 $
 */
 

/*
	The interface for the manager of MBean proxies.
 */
 
package com.sun.cli.jmxcmd.support;

import java.io.IOException;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.MBeanServerConnection;

/**
	Interface for the manager of MBean proxies
 */
public interface MBeanProxyMgr 
{
	/**
		Add, and maintain live, proxies to all MBeans matching the specified
		named and/or patterns.
		
		@param namesAndPatterns	set of ObjectName (name or pattern)
		@param newNames			true if the proxy ObjectNames should be made unique by addition of a Property
		@param cachedMBeanInfoRefreshMillis		refresh interval for MBeanInfo, 0 for no caching
		@param cachedAttributesRefreshMillis	refresh interval for Attribute values, 0 for no caching
	 */
	public void addProxies( ObjectName[] namesAndPatterns, boolean useNewNames,
			int cachedMBeanInfoRefreshMillis,
			int cachedAttributesRefreshMillis )
			throws IOException;
		
	public MBeanServerConnection	getProxyMBeanServerConnection( ObjectName proxyObjectName );
	public ObjectName				getProxyTarget( ObjectName proxyObjectName );
	public MBeanProxyInfo			getProxyInfo( final ObjectName proxyObjectName );
	
	/**
		Return a Set consisting of the MBeanProxyInfo of all proxies within the MBeanServer
		containing this ProxyMgr.
	 */
	public Set			getProxyObjectNames();
}

