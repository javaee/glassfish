/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/util/jmx/JMXTestBase.java,v 1.2 2004/02/14 01:39:36 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/02/14 01:39:36 $
 */
 
package org.glassfish.admin.amx.util.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanRegistrationException;


public class JMXTestBase extends junit.framework.TestCase
{
	private MBeanServer	mServer;
	
		public
	JMXTestBase(  )
	{
		mServer	= createAgent();
	}
	
		protected MBeanServer
	getServer()
	{
		return( mServer );
	}
	
		private MBeanServer
	createAgent(  )
	{
		// don't register it with the Factory
		return( MBeanServerFactory.newMBeanServer() );
	}
	
		protected void
	registerMBean( Object mbean, String name )
		throws MalformedObjectNameException, InstanceAlreadyExistsException,
		NotCompliantMBeanException, MBeanRegistrationException
	{
		mServer.registerMBean( mbean, new ObjectName( name ) );
	}
	
		public void
	setUp() throws Exception
	{
	}
	
		public void
	tearDown()
		throws Exception
	{
		JMXUtil.unregisterAll( mServer );
		mServer	= null;
	}

};

