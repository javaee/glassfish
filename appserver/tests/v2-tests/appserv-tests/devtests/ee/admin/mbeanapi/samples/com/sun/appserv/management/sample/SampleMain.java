/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.appserv.management.sample;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.management.remote.JMXConnector;
import javax.management.InstanceNotFoundException;

import com.sun.appserv.management.DomainRoot;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.XTypes;

import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.TrustStoreTrustManager;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;

/**
	Main class demonstrating a variety of MBean API (AMX) usages.
	Enters an interactive loop in which the user can run various commands.
 */
public final class SampleMain
{
	private final DomainRoot				mDomainRoot;
	private HandshakeCompletedListenerImpl	mHandshakeCompletedListener;
	
		public static void
	main( final String[] args )
	{
		if ( args.length > 1 )
		{
			SampleUtil.println( "Specify a properties file or nothing." );
			System.exit( 255 );
		}
		
		try
		{
			new SampleMain( args.length == 1 ? args[ 0 ] : "SampleMain.properties" );
		}
		catch( Throwable t )
		{
			SampleUtil.getRootCause( t ).printStackTrace();
		}
	}
	
	
	final String	QUIT			= "quit";
	final String	LIST			= "list";
	final String	DEPLOY			= "deploy";
	final String	UNDEPLOY		= "undeploy";
	final String	QUERY			= "query";
	final String	SHOW_HIERARCHY	= "show-hierarchy";
	final String	START_SERVER	= "start-server";
	final String	STOP_SERVER		= "stop-server";
	final String	LIST_ATTRIBUTES	= "list-attributes";
	final String	LIST_DOTTED_NAMES	= "list-dotted-names";
	final String	LIST_PROPERTIES	= "list-properties";
	final String	SET_MONITORING	= "set-monitoring";
	final String	DEMO_JMX_MONITOR	= "demo-jmx-monitor";
	final String	RUN_ALL			= "run-all";
	
	final String[]	MENU_CHOICES	= new String[]
	{
		DEMO_JMX_MONITOR, DEPLOY, LIST_ATTRIBUTES, LIST, LIST_DOTTED_NAMES, LIST_PROPERTIES, QUERY, QUIT, RUN_ALL,
		START_SERVER, STOP_SERVER,
		SHOW_HIERARCHY, SET_MONITORING, UNDEPLOY
	};
	
	final String MENU	= SampleUtil.arrayToString( MENU_CHOICES, "  ", "\n");
	final String PROMPT	= "Commands:\n" + MENU + "\nEnter command> ";
	
	
	private static final class IllegalUsageException extends Exception
	{
		IllegalUsageException()	{}
	}
	 
		private void
	require(
		final boolean	test,
		final String	msg )
		throws IllegalUsageException
	{
		if ( ! test )
		{
			SampleUtil.println( msg );
			throw new IllegalUsageException();
		}
	}
	
		private void
	handleChoice(
		final Samples	samples,
		final String	line )
		throws IOException, IllegalUsageException, InstanceNotFoundException
	{
		final String[]	parts	= line.split( "[ \t]+" );
		final int		numArgs	= parts.length - 1;
		final String	cmd	= parts[ 0 ];
		
		if ( cmd.length() != 0 )
		{
			SampleUtil.println( "cmd: " + SampleUtil.toString( parts ) );
		}
		
		if ( cmd.equals( QUIT ) || cmd.equals( "q" ) )
		{
			require( numArgs == 0, "Usage: " + QUIT );
			System.exit( 0 );
		}
		else if ( cmd.length() == 0 )
		{
			// do nothing
		}
		else if ( cmd.equals( DEPLOY ) )
		{
			require( numArgs >= 1, "Usage: " + DEPLOY + " <archive-name>" );
			for( int i = 1; i < parts.length; ++i )
			{
				samples.deploy( new File( parts[ i ] ) );
			}
		}
		else if ( cmd.equals( UNDEPLOY ) )
		{
			require( numArgs >= 1, "Usage: " + UNDEPLOY + " [<name>[ <name>]*]" );
			for( int i = 1; i < parts.length; ++i )
			{
				samples.undeploy( parts[ i ] );
			}
		}
		else if ( cmd.equals( START_SERVER ) )
		{
			require( numArgs == 1, "Usage: " + START_SERVER + " <server-name>" );
			samples.startServer( parts[ 1 ] );
		}
		else if ( cmd.equals( STOP_SERVER ) )
		{
			require( numArgs == 1, "Usage: " + STOP_SERVER + " <server-name>" );
			samples.stopServer( parts[ 1 ] );
		}
		else if ( cmd.equals( LIST ) )
		{
			require( numArgs == 0, "Usage: " + LIST );
			samples.handleList();
		}
		else if ( cmd.equals( SHOW_HIERARCHY ) )
		{
			if ( numArgs == 0 )
			{
				samples.displayHierarchy();
			}
			else
			{
				for( int i = 1; i < parts.length; ++i )
				{
					samples.displayHierarchy( parts[ i ] );
				}
			}
		}
		else if ( cmd.equals( QUERY) )
		{
			require( numArgs == 0, "Usage: " + QUERY );
			
			samples.demoQuery();
		}
		else if ( cmd.equals( LIST_ATTRIBUTES ) )
		{
			if ( numArgs == 0 )
			{
				samples.displayAllAttributes( getDomainRoot() );
			}
			else
			{
				for( int i = 1; i < parts.length; ++i )
				{
					samples.displayAllAttributes( parts[ i ] );
				}
			}
		}
		else if ( cmd.equals( LIST_DOTTED_NAMES ) )
		{
			require( numArgs == 0, "Usage: " + LIST_DOTTED_NAMES );
			samples.displayDottedNames( );
		}
		else if ( cmd.equals( LIST_PROPERTIES ) )
		{
			require( numArgs == 0, "Usage: " + LIST_PROPERTIES );
			samples.displayAllProperties( );
		}
		else if ( cmd.equals( RUN_ALL ) )
		{
			require( numArgs == 0, "Usage: " + RUN_ALL );
			
			for( int i = 0; i < MENU_CHOICES.length; ++i )
			{
				final String	choice	= MENU_CHOICES[ i ];
				
				if ( ! ( choice.equals( QUIT ) || choice.equals( RUN_ALL ) ) )
				{
					handleChoice( samples, choice );
				}
			}
		}
		else if ( cmd.equals( DEMO_JMX_MONITOR ) )
		{
			samples.demoJMXMonitor();
		}
		else if ( cmd.equals( SET_MONITORING ) )
		{
			require( numArgs == 2, "Usage: " + SET_MONITORING + " <config-name> HIGH|LOW|OFF" );
			
			samples.setMonitoring( parts[ 1 ], parts[ 2 ]);
		}
		else
		{
			SampleUtil.println( "Unknown command: " + line );
		}
	}
	
		private void
	demo()
		throws IOException
	{
		final LineReaderImpl	in	= new LineReaderImpl( System.in );
		
		final Samples	samples	= new Samples( getDomainRoot() );
		while ( true )
		{
			final String	line	= in.readLine( "\n" + PROMPT );
			try
			{
				handleChoice( samples, line.trim() );
			}
			catch( IllegalUsageException e )
			{
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
		private final DomainRoot
	getDomainRoot()
	{
		return( mDomainRoot );
	}
	
	
	
	
		private TLSParams
	createTLSParams(
		final String	trustStore,
		final String	password )
	{
		final File trustStoreFile	= new File( trustStore );
		final char[] trustStorePassword	= password.toCharArray();
					
		mHandshakeCompletedListener	= new HandshakeCompletedListenerImpl();
		final TrustStoreTrustManager trustMgr =
			new TrustStoreTrustManager( trustStoreFile, trustStorePassword);
		trustMgr.setPrompt( true );

		final TLSParams	tlsParams = new TLSParams( trustMgr, mHandshakeCompletedListener );

		return( tlsParams );
	}
	
	/**
		Read connect properties from a file.
	 */
		private final Properties
	getConnectProperties( final String file )
		throws IOException
	{
		final Properties	props	= new Properties();
		
		if ( file != null )
		{
			SampleUtil.println( "Reading properties from: " + SampleUtil.quote( file ) );
			final File	f	= new File( file );
			
			if ( f.exists() )
			{
				final FileInputStream	is	= new FileInputStream( f );
				try
				{
					props.load( is );
				}
				finally
				{
					is.close();
				}
			}
			else
			{
				SampleUtil.println("File \"" + file + " does not exist, using defaults." );
			}
		}
		
		return( props );
	}
	
	private final static String	DEFAULT_TRUST_STORE_FILE		= "~/.keystore";
	private final static String	DEFAULT_TRUST_STORE_PASSWORD	= "changeme";
	
	/**
		@param host	hostname or IP address of Domain Admin Server
		@param port	RMI administrative port
		@param user	admin user
		@param password admin user password
		@param tlsParams TLS parameters, may be null
		@return AppserverConnectionSource
	 */
		public static AppserverConnectionSource
	connect(
		final String	host,
		final int		port,
		final String	user,
		final String	password,
		final TLSParams	tlsParams )
		throws IOException
	{
		final String info = "host=" + host + ", port=" + port +
			", user=" + user + ", password=" + password +
			", tls=" + (tlsParams != null);
			
		SampleUtil.println( "Connecting...:" + info );
		
		final AppserverConnectionSource conn	=
			new AppserverConnectionSource( AppserverConnectionSource.PROTOCOL_RMI,
				host, port, user, password, tlsParams, null);
		
		// force the connection now
		conn.getJMXConnector( false );

		SampleUtil.println( "Connected: " + info );
		
		return( conn );
	}
	
	
	/**
	 */
		public
	SampleMain( final String optionalPropertiesFile )
		throws IOException
	{
		final Properties	props	= getConnectProperties( optionalPropertiesFile );
		
		final String	host	= props.getProperty( "connect.host", "localhost" );
		final int		port	= Integer.parseInt( props.getProperty( "connect.port", "8686" ) );
		final String	user	= props.getProperty( "connect.user", "admin" );
		final String	password	= props.getProperty( "connect.password", "admin123" );
		final String	trustStore	= props.getProperty( "connect.truststore", DEFAULT_TRUST_STORE_FILE);
		final String	trustStorePassword	=
				props.getProperty( "connect.truststorePassword", DEFAULT_TRUST_STORE_PASSWORD);
		final boolean	useTLS	= 
			Boolean.valueOf( props.getProperty( "connect.useTLS", "false" ) ).booleanValue();
		
		final TLSParams	tlsParams	= useTLS ?
			createTLSParams( trustStore, trustStorePassword) : null;
		
		final AppserverConnectionSource	conn 	= connect( host, port, user, password, tlsParams );
		
		if ( mHandshakeCompletedListener != null )
		{
			SampleUtil.println( "HandshakeCompletedEvent: " +
				SampleUtil.toString( mHandshakeCompletedListener.getLastEvent() ) );
		}
		
		mDomainRoot	= conn.getDomainRoot();

		
		try
		{
			demo( );
		}
		finally
		{
			// close the connection (not necessary, but here for as an example)
			conn.getJMXConnector( false ).close();
		}
	}
}









