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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanServerConnectionConnectionSource.java,v 1.1 2005/11/08 22:40:23 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:40:23 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import com.sun.appserv.management.client.ConnectionSource;


/**
	A ConnectionSource which wraps an already created MBeanServerConnection
 */
public final class MBeanServerConnectionConnectionSource implements ConnectionSource
{
	protected MBeanServerConnection		mConn;
	
	/**
		Create a new instance for the specified MBeanServerConnection
	 */
		public
	MBeanServerConnectionConnectionSource( final MBeanServerConnection conn )
	{
		mConn	= conn;
	}
	
		public MBeanServerConnection
	getMBeanServerConnection( boolean forceNew )
		throws IOException
	{
		if ( mConn == null )
		{
			throw new IOException();
		}

		return( mConn );
	}
	
		public MBeanServerConnection
	getExistingMBeanServerConnection( )
	{
		return( mConn );
	}
	
	
		public JMXConnector
	getJMXConnector( boolean forceNew )
		throws IOException
	{
		// we can't supply one...
		return( null );
	}
}
