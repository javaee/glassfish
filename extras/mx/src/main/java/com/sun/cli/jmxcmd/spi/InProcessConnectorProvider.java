/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/spi/InProcessConnectorProvider.java,v 1.6 2005/11/15 20:59:54 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2005/11/15 20:59:54 $
 */
 
package com.sun.cli.jmxcmd.spi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.remote.JMXConnector;
import javax.management.ListenerNotFoundException;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.JCmdMain;
import com.sun.cli.jmxcmd.cmd.JMXCmdEnvKeys;
import org.glassfish.admin.amx.util.SetUtil;



/**
	Supports connectivity to in-process MBeanServers.
	<p>
	Supported protocols are {@link #IN_PROCESS_PROTOCOL}
 */
public final class InProcessConnectorProvider
	extends JMXConnectorProviderBase
{
		public
	InProcessConnectorProvider()
	{
	}
	
	static class Info implements JMXConnectorProviderInfo
	{
		private static final String	DESCRIPTION	=
			"Implements the standard JSR 160 connector for in-process MBeanServers";
		private static final String	USAGE	=
			"connect --protocol inprocess --options=mbeanserver=<mbeanserver-name> [connection-name]";
		
			public String
		getDescription()
		{
			return( DESCRIPTION );
		}
			public String
		getUsage()
		{
			return( USAGE );
		}
	}
	
		public static JMXConnectorProviderInfo
	getInfo()
	{
		return( new Info() );
	}
	
	public final static String	MBEAN_SERVER_OPTION	= "mbeanserver";
	
	
	private static class MyConnector implements JMXConnector
	{
		final MBeanServer	mServer;
		boolean				mClosed;
		
		MyConnector( MBeanServer	server)
		{
			mClosed	= false;
			
			if ( server == null )
			{
				throw new IllegalArgumentException( "server cannot be null" );
			}
			
			mServer	= server;
		}
		
			public void
		close() throws IOException
		{
			mClosed	= true;
		}
		
			public void
		connect( )
		{
			connect( null );
		}
		
			public void
		connect( Map env )
		{
			if ( mServer == null )
			{
				throw new IllegalArgumentException();
			}
		}
		
			public MBeanServerConnection
		getMBeanServerConnection() throws IOException
		{
			return( mServer );
		}
		
			public MBeanServerConnection
		getMBeanServerConnection( javax.security.auth.Subject s ) throws IOException
		{
			return( mServer );
		}
		
			public String
		getConnectionId() throws IOException
		{
			return( toString() );
		}
		
		public static class UnimplementedException extends RuntimeException
		{
				public
			UnimplementedException( String msg )
			{
				super( msg );
			}
		}
		
			public void
		addConnectionNotificationListener(
			NotificationListener listener,
			NotificationFilter filter,
			Object handback)
		{
			throw new UnimplementedException( "Not implemented: addConnectionNotificationListener" );
		}
        
			public void
		removeConnectionNotificationListener( NotificationListener listener )
			throws ListenerNotFoundException
		{
			throw new UnimplementedException( "Not implemented: removeConnectionNotificationListener" );
		}
		
			public void
		removeConnectionNotificationListener(
			NotificationListener	l,
			NotificationFilter		f,
			Object					handback)
			throws ListenerNotFoundException
		{
			throw new UnimplementedException( "Not implemented: removeConnectionNotificationListener" );
		}
                                         
	}
	
		public JMXConnector
	connect( java.util.Map<String,String> m )
		throws java.io.IOException
	{
		MBeanServer server	= null;
		
		/*
			A bit "yucky", since it relies on JCmdMain, but oh well.
		 */
		final String serverName	= (String)m.get( MBEAN_SERVER_OPTION );
		if ( serverName != null )
		{
			final CmdEnv	env	= JCmdMain.getCmdMgr().getEnv();
			
            @SuppressWarnings("unchecked")
			final HashMap<String,MBeanServer>	servers	= (HashMap<String,MBeanServer>)env.get( JMXCmdEnvKeys.MBEAN_SERVERS );
		
			if ( servers != null )
			{
				server	= (MBeanServer)servers.get( serverName );
			}
			if ( server == null )
			{
				throw new IllegalArgumentException( "MBean server not found: " + serverName );
			}
		}

		return( new MyConnector( server ) );
	}
	
	public final static String	IN_PROCESS_PROTOCOL	= "inprocess";
	public static final Set<String>	SUPPORTED_PROTOCOLS	=
		SetUtil.newUnmodifiableStringSet( IN_PROCESS_PROTOCOL );
	
		protected Set<String>
	getSupportedProtocols()
	{
		return( SUPPORTED_PROTOCOLS );
	}
}




