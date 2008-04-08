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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanServerConnectionSource.java,v 1.1 2005/11/08 22:40:23 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:40:23 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import com.sun.appserv.management.client.ConnectionSource;


/**
	A ConnectionSource for in-process access where the MBeanServer is actually
	known and where later access to it may be desired.
 */
public final class MBeanServerConnectionSource implements ConnectionSource
{
	protected MBeanServer		mServer;
	
	/**
		Create a new instance for the specified MBeanServer
	 */
		public
	MBeanServerConnectionSource( final MBeanServer server )
	{
		mServer	= server;
	}
	
	/**
		Return the internal MBeanServer 
	 */
		public MBeanServer
	getMBeanServer( )
	{
		return( mServer );
	}
	
		public MBeanServerConnection
	getExistingMBeanServerConnection( )
	{
		return( getMBeanServer() );
	}
	
		public MBeanServerConnection
	getMBeanServerConnection( boolean forceNew )
	{
		return( getExistingMBeanServerConnection() );
	}
	
	
		public JMXConnector
	getJMXConnector( boolean forceNew )
	{
		// we can't supply one...
		return( null );
	}
}
