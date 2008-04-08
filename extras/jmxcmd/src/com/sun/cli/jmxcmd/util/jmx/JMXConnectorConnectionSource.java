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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/JMXConnectorConnectionSource.java,v 1.1 2005/11/08 22:40:22 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:40:22 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import com.sun.appserv.management.client.ConnectionSource;


/**
	A ConnectionSource for in-process access where the MBeanServer is actually
	known and where later access to it may be desired.
 */
public final class JMXConnectorConnectionSource
	implements ConnectionSource
{
	protected JMXConnector	mJMXConnector;
	
		public
	JMXConnectorConnectionSource( final JMXConnector connector )
		throws IOException
	{
		if ( connector == null )
		{
			throw new IllegalArgumentException();
		}
		
		mJMXConnector	= connector;
		
		// make sure it's good, now
		getMBeanServerConnection( false );
	}
	
	
	
		public MBeanServerConnection
	getExistingMBeanServerConnection( )
	{
		try
		{
			return( mJMXConnector.getMBeanServerConnection() );
		}
		catch( IOException e )
		{
		}
		return( null );
	}
	
		public MBeanServerConnection
	getMBeanServerConnection( boolean forceNew )
		throws IOException
	{
		return( mJMXConnector.getMBeanServerConnection() );
	}
	
	/**
		@return the existing JMXConnector 
	 */
		public JMXConnector
	getJMXConnector( final boolean forceNew )
		throws IOException
	{
		// all we have is what is already present; no way to create a new one
		return( mJMXConnector );
	}
}
