/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/ObjectNameQueryMBeanImpl.java,v 1.2 2005/11/08 22:40:24 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:40:24 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import java.util.Set;

import javax.management.StandardMBean;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanRegistration;

/**
	An MBean implementing ObjectNameQueryMBean
 */
public class ObjectNameQueryMBeanImpl
	extends StandardMBean
	implements ObjectNameQueryMBean, MBeanRegistration
{
	private MBeanServerConnection		mConn;
	private ObjectNameQueryImpl			mImpl;
	
		public
	ObjectNameQueryMBeanImpl()
		throws NotCompliantMBeanException
	{
		super( ObjectNameQueryMBean.class );
	}
	
		public Set
	matchAll( ObjectName startingSetPattern, String [] regexNames, String [] regexValues )
				throws MalformedObjectNameException, java.io.IOException
	{
		final Set	candidates	= mConn.queryNames( startingSetPattern, null );
		
		return( mImpl.matchAll( candidates, regexNames, regexValues ) );
	}
	
		public Set
	matchAll( Set startingSet, String [] regexNames, String [] regexValues )
	{
		return( mImpl.matchAll( startingSet, regexNames, regexValues ) );
	}
				
		public Set
	matchAny( ObjectName startingSetPattern, String [] regexNames, String [] regexValues )
				throws MalformedObjectNameException, java.io.IOException
	{
		final Set	candidates	= mConn.queryNames( startingSetPattern, null );
		
		return( mImpl.matchAny( candidates, regexNames, regexValues ) );
	}
				
		public Set
	matchAny( Set startingSet, String [] regexNames, String [] regexValues )
	{
		return( mImpl.matchAny( startingSet, regexNames, regexValues ) );
	}
	
	
	
		public ObjectName
	preRegister( final MBeanServer server, final ObjectName name)
	{
		mConn		= server;
		
		mImpl		= new ObjectNameQueryImpl( );
		return( name );
	}
	
		public void
	postRegister( Boolean registrationDone )
	{
	}
	
		public void
	preDeregister()
	{
		// nothing to do
	}
		public void
	postDeregister()
	{
		// nothing to do
	}

}






