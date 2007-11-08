/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.management;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import java.lang.reflect.Method;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayInputStream;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.remote.JMXConnectionNotification;
import javax.management.MBeanServerConnection;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.SystemInfo;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXDebug;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.TrustStoreTrustManager;
import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.HandshakeCompletedListenerImpl;


import com.sun.appserv.management.config.NodeAgentConfig;
import com.sun.appserv.management.config.JMXConnectorConfig;
import com.sun.appserv.management.config.OfflineConfigIniter;


import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.FileUtils;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.appserv.management.util.stringifier.ArrayStringifier;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;
import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.jmx.stringifier.StringifierRegistryIniter;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.jmx.MBeanServerConnectionSource;


import static com.sun.enterprise.management.PropertyKeys.*;
import com.sun.enterprise.management.monitor.AMXMonitorTestBase;


/**
	Main class that runs all the unit tests
 */
public final class TestMain implements NotificationListener
{
	private final DomainRoot				mDomainRoot;
	private HandshakeCompletedListenerImpl	mHandshakeCompletedListener;
	
	
		private static void
	printUsage()
	{
		println( "USAGE: java " + TestMain.class.getName() + " <properties-file> [name=value [name=value]*]" );
		
		final String	example	= MapUtil.toString( PropertyKeys.getDefaults(), "\n" ) +
			"\n\nAdditional properties may be included and will be placed into a Map " +
			"for use by any unit test.";
		println( "Properties file format:\n" + example );
		println( "" );
		println( "The optional property " + StringUtil.quote(TEST_CLASSES_FILE_KEY) +
			" may contain the name of a file which specifies which test classes to run. " +
			"Files should be listed with fully-qualified classnames, one per line.  " +
			"The # character may be used to comment-out classnames."
		    );
	    println( "" );
	    println( "Additional properties may also be passed directly on the command line." );
	    println( "These override any properties found in the specified properties file." );
	    println( "[all properties intended for permanent use should be defined in PropertyKeys.java]" );
	    println( "EXAMPLE:" );
	    println( "java TestMain amxtest.properties amxtest.verbose=true my-temp=true" );
	}
	
		
		private static boolean
	isHelp( final String s )
	{
		return( s.equals( "help" ) || s.equals( "--help" ) || s.equals( "-?" ) );
	}
	
		protected static void
	checkAssertsOn()
	{
		try
		{
			assert( false );
			throw new Error( "Assertions must be enabled for unit tests" );
		}
		catch( AssertionError a )
		{
		}
	}
	
	
	    private static Map<String,String>
	argsToMap( final String[] args )
	{
		final Map<String,String>   params  = new HashMap<String,String>();
		
		params.put( DEFAULT_PROPERTIES_FILE, args[ 0 ] );
		
	    for( int i = 1; i < args.length; ++i )
	    {
	        final String    pair= args[ i ];
	        final int delimIndex  = pair.indexOf( "=" );
	        String    name  = null;
	        String    value  = null;
	        if ( delimIndex < 0 )
	        {
	            name    = pair;
	            value   = null;
	        }
	        else
	        {
	            name    = pair.substring( 0, delimIndex);
	            value   = pair.substring( name.length() + 1, pair.length() );
	            
	        }
	        params.put( name, value );
	    }
	    
	    return params;
	}
	
	    private static DomainRoot
	initOffline( final File domainXML )
	{
	    final MBeanServer   server  = MBeanServerFactory.createMBeanServer( "test" );
	    assert( domainXML.exists() && domainXML.length() != 0 );
	    
	    final OfflineConfigIniter initer    = new OfflineConfigIniter( server, domainXML );
	    final DomainRoot    domainRoot  = initer.getDomainRoot();
	    
	    return domainRoot;
	}
	
		public static void
	main( final String[] args )
		throws Exception
	{
	    checkAssertsOn();
		// for friendlier output via Stringifiers
		new StringifierRegistryIniter( StringifierRegistryImpl.DEFAULT );
	    
		
		if ( args.length == 0 ||
			( args.length == 1 && isHelp( args[ 0 ] ) ) )
		{
			printUsage();
			System.exit( 255 );
		}
		
		final Map<String,String>    cmdLineParams   = argsToMap( args );
		
		try
		{
			new TestMain( args.length == 0 ? null : args[ 0 ], cmdLineParams);
		}
		catch( Throwable t )
		{
			final Throwable rootCause	= ExceptionUtil.getRootCause( t );
			
			if ( rootCause instanceof java.net.ConnectException )
			{
				System.err.println( "\nERROR: The connection to the server could not be made" );
			}
			else
			{
				System.err.println( "\nERROR: exception of type: " + rootCause.getClass().getName() );
				rootCause.printStackTrace();
			}
			System.exit( -1 );
		}
	}

	private static void	println( Object o )
	{
		System.out.println( o );
	}
	
		public static String
	toString( Object o )
	{
		return( SmartStringifier.toString( o ) );
	}
	

	
		private final DomainRoot
	getDomainRoot()
	{
		return( mDomainRoot );
	}
	
	
	
		private TLSParams
	createTLSParams(
		final File		trustStoreFile,
		final String	password )
	{
		final char[] trustStorePassword	= password.toCharArray();
					
		mHandshakeCompletedListener	= new HandshakeCompletedListenerImpl();
		final TestClientTrustStoreTrustManager trustMgr =
			new TestClientTrustStoreTrustManager( trustStoreFile, trustStorePassword);

		final TLSParams	tlsParams = new TLSParams( trustMgr, mHandshakeCompletedListener );

		return( tlsParams );
	}
	
	/**
		Read connect properties from a file.
	 */
		private final Map<String,String>
	getProperties( final String file )
		throws IOException
	{
		Map<String,String>	props	= PropertyKeys.getDefaults();
		
		props.remove( TEST_CLASSES_FILE_KEY );
		
		if ( file != null )
		{
			println( "Reading properties from: " + StringUtil.quote( file ) );
			
			final String propsString	= FileUtils.fileToString( new File( file ) );
			final Properties fromFile  = new Properties();
			fromFile.load( new ByteArrayInputStream( propsString.getBytes() ) );
			
			props   = MapUtil.toStringStringMap( fromFile );
		}
		else
		{
			println( "Using default properties." );
		}
		
		return( props );
	}
	
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
			
		println( "Connecting: " + info  + "...");
		
		final AppserverConnectionSource conn	=
			new AppserverConnectionSource( AppserverConnectionSource.PROTOCOL_RMI,
				host, port, user, password, tlsParams, null);

		conn.getJMXConnector( false );
		//println( "Connected: " + info );
		
		return( conn );
	}
	
	private final class PropertyGetter
	{
		final Map<String,Object>	mItems;
		
		public PropertyGetter( final Map<String,Object> props )
		{
		    mItems  = new HashMap<String,Object>();
		    mItems.putAll( props );
		}
		
		    public Object
		get( final String key )
		{
			Object	result	= System.getProperty( key );
			if ( result == null )
			{
				result	= mItems.get( key );
			}

			return( result );
		}
		
		public String	getString( final String key )	{ return( (String)get( key ) ); }
		public File		getFile( final String key )
		{
			final String  value	= getString( key );
			
			return( value == null ? null : new File( value ) );
		}
		public int		getint( final String key )		{ return( Integer.parseInt( getString( key ) ) ); }
		public Integer	getInteger( final String key )	{ return( new Integer( getString( key ) ) ); }
		public boolean	getboolean( final String key )	{ return( Boolean.valueOf( getString( key ) ).booleanValue() ); }
		public Boolean	getBoolean( final String key )	{ return( Boolean.valueOf( getString( key ) ) ); }
	};
	
	
		private AppserverConnectionSource
	_getConnectionSource(
	    final PropertyGetter getter,
	    final String         host,
	    final int            port )
		throws IOException
	{
		final String	user	= getter.getString( USER_KEY );
		final String	password	= getter.getString( PASSWORD_KEY );
		final File		trustStore	= getter.getFile( TRUSTSTORE_KEY);
		final String	trustStorePassword	= getter.getString( TRUSTSTORE_PASSWORD_KEY);
		final boolean	useTLS	=  getter.getboolean( USE_TLS_KEY );
		
		final TLSParams	tlsParams	= useTLS ?
			createTLSParams( trustStore, trustStorePassword) : null;
		
		AppserverConnectionSource	conn = null;
		
		try
		{
    		conn 	= connect( host, port, user, password, tlsParams );
    		if ( mHandshakeCompletedListener != null )
    		{
    			assert( mHandshakeCompletedListener.getLastEvent() != null );
    			println( "HandshakeCompletedEvent: " +
    				toString( mHandshakeCompletedListener.getLastEvent() ) );
    		}
		}
		catch( IOException e )
		{
		    if ( useTLS )
		    {
		        // try without TLS
		        println( "Attempting connection without TLS..." );
    		    conn 	= connect( host, port, user, password, null );
		    }
		}
		
		if ( conn != null )
		{
		    conn.getJMXConnector( false ).addConnectionNotificationListener( this, null, conn );
		}
		
		return( conn );
	}
	
		private AppserverConnectionSource
	_getConnectionSource( final PropertyGetter getter )
		throws IOException
	{
		final String	host	= getter.getString( HOST_KEY );
		final int		port	= getter.getint( PORT_KEY );
		
		return _getConnectionSource( getter, host, port );
	}
	
		
		private AppserverConnectionSource
	getConnectionSource(
		final PropertyGetter	getter,
		boolean					retry )
		throws Exception
	{
		AppserverConnectionSource	conn	= null;
		
		final long PAUSE_MILLIS = 3*1000;
		
		for( int i = 0; i < 5; ++i )
		{
			try
			{
				conn	= _getConnectionSource( getter );
				break;
			}
			catch( Exception e )
			{
				final Throwable rootCause	= ExceptionUtil.getRootCause( e );
				
				if ( rootCause instanceof java.net.ConnectException )
				{
					println( "ConnectException: " + rootCause.getMessage() +
						"...retry..." );
					Thread.sleep( PAUSE_MILLIS );
					continue;
				}
				throw e;
			}
		}

		return( conn );
	}
	
	
	
		public void
	handleNotification(
		final Notification	notifIn, 
		final Object		handback) 
	{
		if ( notifIn instanceof JMXConnectionNotification )
		{
			final String type	= notifIn.getType();
			if ( type.equals( JMXConnectionNotification.FAILED ) )
			{
				System.err.println( "\n\n### JMXConnection FAILED: " + handback + "\n\n" );
			}
			else if ( type.equals( JMXConnectionNotification.CLOSED ) )
			{
				System.err.println( "\n\n### JMXConnection CLOSED: " + handback + "\n\n" );
			}
			else if ( type.equals( JMXConnectionNotification.OPENED ) )
			{
				System.err.println( "\n\n### JMXConnection OPENED: " + handback + "\n\n" );
			}
			else if ( type.equals( JMXConnectionNotification.NOTIFS_LOST ) )
			{
				System.err.println( "\n\n### JMXConnection NOTIFS_LOST: " + handback + "\n\n" + notifIn );
				Observer.getInstance().notifsLost();
			}
		}
	}

		private void
	printItems(
		final String[]	items,
		final String	prefix )
	{
		for( int i = 0; i < items.length; ++i )
		{
			println( prefix + items[ i ] );
		}
	}
	
	    private String[]
	classesToStrings( final Set<Class<junit.framework.TestCase>> classes )
	{
		final String[]  names   = new String[ classes.size() ];
		
		int i = 0;
		for ( final Class<?> c : classes )
		{
		    names[ i ] = c.getName();
		    ++i;
		}
		return names;
	}
	
		private void
	warnUntestedClasses( final List<Class<junit.framework.TestCase>>	actual )
	{
		final Set<Class<junit.framework.TestCase>>	actualSet   = GSetUtil.newSet( actual );
		final Set<Class<junit.framework.TestCase>>	allSet      = GSetUtil.newSet( Tests.getTestClasses() );
		
		final Set<Class<junit.framework.TestCase>>	untested	= GSetUtil.newSet( allSet );
		untested.removeAll( actualSet );
		if ( untested.size() != 0 )
		{
			println( "\nWARNING: the following tests WILL NOT BE RUN:" );
			final String[]  names   = classesToStrings( untested );
			for( int i = 0; i < names.length; ++i )
			{
			    names[i]    = "!" + names[i] + "!";   // indicate not being run
			}

			println( ArrayStringifier.stringify( names, "\n" ) );
			println( "" );
		}
		
		final Set<Class<junit.framework.TestCase>>	extras	= GSetUtil.newSet( actualSet );
		extras.removeAll( actualSet );
		if ( extras.size() != 0 )
		{
			println( "\nNOTE: the following non-default tests WILL BE RUN:" );
			final String[]  names   = classesToStrings( extras );
			
			println( ArrayStringifier.stringify( names, "\n" ) );
			println( "" );
		}
	}
	
	    private void
	warnDisabledTests()
	{
	    final String WARNING =
"----------------------------------------\n" +
"-                                      -\n" +
"- NOTE:                                -\n" +
"- Generic tests currently disabled for -\n" +
"- AMX MBeans which reside in non-DAS   -\n" +
"- server instances eg Logging, CallFlow.-\n" +
"- Denoted by 'remoteIncomplete'        -\n" +
"-                                      -\n" +
"-                                      -\n" +
"----------------------------------------";

	    println( WARNING );
	}
		private List<Class<junit.framework.TestCase>>
	getTestClasses( final File	testsFile )
		throws FileNotFoundException, IOException
	{
		List<Class<junit.framework.TestCase>> testClasses	= null;

		if ( testsFile == null  )
		{
			testClasses	= Tests.getTestClasses();
			println( "NO TEST FILE SPECIFIED--TESTING ALL CLASSES in " + Tests.class.getName());
		}
		else
		{
			println( "Reading test classes from: " + StringUtil.quote( testsFile.toString() ));
			
			final String	fileString	= FileUtils.fileToString( testsFile );
			final String	temp	= fileString.replaceAll( "\r\n", "\n" ).replaceAll( "\r", "\n" );
			final String[]	classnames	= temp.split( "\n" );
			
			testClasses	= new ArrayList<Class<junit.framework.TestCase>>();
			
			for( int i = 0; i < classnames.length; ++i )
			{
				final String	classname	= classnames[ i ].trim();
						
				if ( classname.length() != 0 && ! classname.startsWith( "#" ) )
				{
					try
					{
						final Class<junit.framework.TestCase> theClass	=
						    TypeCast.asClass( ClassUtil.getClassFromName( classname ) );
						
						testClasses.add( theClass );
					}
					catch( Throwable t )
					{
					    final String msg    = "Can't load test class: " + classname;
					    
						throw new Error( msg, t);
					}
				}
			}
			
			warnUntestedClasses( testClasses );
			warnDisabledTests();
		}
		
		return( testClasses );
	}


		private void
	warnUnknownProperties( final Map<String,String> props )
	{
		final Map<String,String>	known	= new HashMap<String,String>( getDefaults() );
		final Map<String,String>	unknown	= new HashMap<String,String>( props );
		
		unknown.keySet().removeAll( known.keySet() );
		if ( unknown.keySet().size() != 0 )
		{
			println( "\nNOTE: the following properties are not recognized but " +
			"will be included in the environment for use by unit tests:");
			println( MapUtil.toString( unknown, "\n" ) );
			println( "" );
		}
	}
	
	private static final String RMI_PROTOCOL_IN_CONFIG    = "rmi_jrmp";
	
	    public Map<String,AppserverConnectionSource>
	getNodeAgentConnections(
	    final DomainRoot        domainRoot,
	    final PropertyGetter    getter )
	{
	    final Map<String,NodeAgentConfig>   nodeAgentConfigs =
	        domainRoot.getDomainConfig().getNodeAgentConfigMap();
	    
	    final Map<String,AppserverConnectionSource> nodeAgentConnections =
	        new HashMap<String,AppserverConnectionSource>();
	    
	    println( "" );
	    println( "Contacting node agents..." );
	    
	    for( final NodeAgentConfig nodeAgentConfig : nodeAgentConfigs.values() )
	    {
	        final String    nodeAgentName   = nodeAgentConfig.getName();
	        
	        final JMXConnectorConfig    connConfig  = nodeAgentConfig.getJMXConnectorConfig();
	        
	        if ( ! connConfig.getEnabled() )
	        {
	            println( nodeAgentName + ": DISABLED CONNECTOR");
	            continue;
	        }
	        
	        final String    address    = connConfig.getAddress();
	        final int       port    = Integer.parseInt( connConfig.getPort() );
	        final boolean   tlsEnabled  = connConfig.getSecurityEnabled();
	        final String    protocol    = connConfig.getProtocol();
	        
	        if ( ! RMI_PROTOCOL_IN_CONFIG.equals( protocol ) )
	        {
	            println( nodeAgentName + ": UNSUPPORTED CONNECTOR PROTOCOL: " + protocol );
	            continue;
	        }
	        
	        // See if we can connect
	        try
	        {
	            final AppserverConnectionSource asConn    =
	                _getConnectionSource( getter, address, port );
	            final MBeanServerConnection conn    = asConn.getMBeanServerConnection( false );
	            final boolean   alive   =
	                conn.isRegistered( JMXUtil.getMBeanServerDelegateObjectName() );
	            assert( alive );
	            
	            nodeAgentConnections.put( nodeAgentName, asConn );
	            println( nodeAgentName + ": ALIVE" );
	        }
	        catch( Exception e )
	        {
	            println( "Node agent " + nodeAgentConfig.getName() + 
	                " could not be contacted: " + e.getClass().getName() );
	            println( nodeAgentName + ": COULD NOT BE CONTACTED" );
	            continue;
	        }
	    }
	    
	    println( "" );
	    
	    return nodeAgentConnections;
	}
	
	    private Capabilities
	getCapabilities( final Class c )
	{
	    Capabilities    capabilities    = AMXTestBase.getDefaultCapabilities();
	    
	    try
	    {
		    final Method getCapabilities	= c.getDeclaredMethod( "getCapabilities", (Class[])null );
		    
		    capabilities    = (Capabilities)getCapabilities.invoke( null, (Object[])null );
		}
		catch( Exception e )
		{
		}
		
		return capabilities;
    }
	
		private List<Class<junit.framework.TestCase>>
    filterTestClasses(
        final DomainRoot         domainRoot,
        final PropertyGetter     getter,
        final List<Class<junit.framework.TestCase>>         classes )
    {
		final boolean	offline	= getter.getboolean( TEST_OFFLINE_KEY );
		
		final SystemInfo    systemInfo  = domainRoot == null ? null : domainRoot.getSystemInfo();
		
		final boolean   clustersSupported   = systemInfo == null ?
		    false : systemInfo.supportsFeature( SystemInfo.CLUSTERS_FEATURE );
		    
		final boolean   multipleServersSupported   = systemInfo == null ?
		    false : systemInfo.supportsFeature( SystemInfo.MULTIPLE_SERVERS_FEATURE );
		    
		final boolean   monitorsSupported   = ! offline;
		
		final List<Class<junit.framework.TestCase>>   included   = new ArrayList<Class<junit.framework.TestCase>>();
		final List<Class<junit.framework.TestCase>>   omitted    = new ArrayList<Class<junit.framework.TestCase>>();
		for( final Class<junit.framework.TestCase> c : classes )
		{
		    boolean include   = true;
		    
		    final Capabilities  capabilities = getCapabilities( c );
		    
	        if (  (! monitorsSupported ) &&
	            AMXMonitorTestBase.class.isAssignableFrom( c ) )
	        {
	            include = false;
	        }
		    else if ( offline && ! capabilities.getOfflineCapable() )
	        {
	            include = false;
	        }
		    else if ( ClusterSupportRequired.class.isAssignableFrom( c ) &&
		        ! clustersSupported )
		    {
	            include = false;
		    }
		    else if ( MultipleServerSupportRequired.class.isAssignableFrom( c ) &&
		        ! multipleServersSupported )
		    {
	            include = false;
		    }
		    
		    if ( include )
		    {
		        included.add( c );
		    }
		    else
		    {
		        omitted.add( c );
		    }
		}
		
		return included;
    }
    
		
	/**
	 */
		public
	TestMain(
	    final String optionalPropertiesFile,
	    final Map<String,String>   cmdLineParams )
		throws Exception
	{
	    AMXDebug.getInstance().setAll( true );
	    
	    checkAssertsOn();
	    
		final Map<String,String>    props	= getProperties( optionalPropertiesFile );
		
		final Map<String,String>    envIn = new HashMap<String,String>( props );
		envIn.putAll( cmdLineParams );
		warnUnknownProperties( envIn );
		
		final Map<String,Object>    env = new HashMap<String,Object>();
		env.putAll( envIn );
		
		println( "" );
		println( "ENVIRONMENT:\n" + MapUtil.toString( env, "\n" ) );
		println( "" );
		
		final PropertyGetter	getter	= new PropertyGetter( env );
		
    	ConnectionSource	conn    = null;
    		
		final boolean	testOffline	= getter.getboolean( TEST_OFFLINE_KEY );
	    if ( testOffline )
	    {
	        final String    domainXML   = getter.getString( DOMAIN_XML_KEY );
	        mDomainRoot = initOffline( new File( domainXML ) );
	        
	        final MBeanServer   server  = (MBeanServer)
	            Util.getExtra( mDomainRoot ).getConnectionSource().getExistingMBeanServerConnection();
	    
    	    final Set<ObjectName>    mbeans =
    	        JMXUtil.queryNames( server, Util.newObjectName( "*:*" ), null );
    	    //println( "\n\n------------------------------------------" );
    	    //println( "MBeans registered:" );
    	    //println( CollectionUtil.toString( mbeans, "\n" ) );
    	    //println( "\n\n" );
	            
	        conn    = new MBeanServerConnectionSource( server );
	    }
	    else
	    {
    		if ( getter.getboolean( CONNECT_KEY ) )
    		{
    		    final AppserverConnectionSource acs  	= getConnectionSource( getter, true );
    		
    		    if ( acs == null )
    		    {
    		        throw new IOException( "Can't connect to server" );
    		    }

    		    mDomainRoot	= acs.getDomainRoot();
    		    
    		    conn    = acs;
    		}
    		else
    		{
    	        mDomainRoot = null;
    	        conn       = null;
    	    }
	    }
		
		if ( mDomainRoot != null )
		{
		    Observer.create( mDomainRoot );
		}
		
		final boolean	expandedTesting	= testOffline ?
		    false :getter.getboolean( EXPANDED_TESTING_KEY );
		
		if ( mDomainRoot != null && expandedTesting )
		{
		    final Map<String,AppserverConnectionSource> connections =
		        getNodeAgentConnections( mDomainRoot, getter );
		    
		    env.put( NODE_AGENTS_KEY, connections );
		}
		
		
		final boolean	threaded	= getter.getboolean( RUN_THREADED_KEY );
		
		if ( getter.getboolean( VERBOSE_KEY ) )
		{
			println( "VERBOSE mode enabled" );
			if ( threaded )
			{
				println( "NOTE: timings displayed when running " +
				    "threaded tests will be impacted by other concurrent tests." );
			}
		}
		
		final List<Class<junit.framework.TestCase>> specifiedClasses	= 
		    getTestClasses( getter.getFile( TEST_CLASSES_FILE_KEY ) );
		
		final List<Class<junit.framework.TestCase>> testClasses   =
		    filterTestClasses( mDomainRoot, getter, specifiedClasses );
		
		final int iterations	= getter.getInteger( ITERATIONS_KEY ).intValue();
		iterateTests(
		    testClasses,
		    iterations,
		    conn,
		    threaded,
		    Collections.unmodifiableMap( env ) );
		
		
		println( "" );
		println( "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" );
		println( ">>>> Please inspect amxtest.coverage <<<<" );
		println( "                   ^                     " );
		println( "                   ^                     " );
		println( "                   ^                     " );
		println( "                   ^                     " );
	}
	
	    private void
	iterateTests(
	    final List<Class<junit.framework.TestCase>> testClasses,
	    final int                iterations,
	    final ConnectionSource   conn,
	    final boolean            threaded,
	    final Map<String,Object> env )
	    throws Exception
	{
		for( int i = 0; i < iterations; ++i )
		{
			if ( iterations != 1 )
			{
			    println( "#########################################################" );
				println( "\n### ITERATION " + (i+1) );
			    println( "#########################################################" );
			}

			final long	start	= System.currentTimeMillis();
	    
			final TestRunner	runner	= new TestRunner( conn );
			runner.runAll( testClasses, threaded,  env );
			
			final long	elapsed	= System.currentTimeMillis() - start;
			println( "Time to run tests: " + (elapsed/1000) + " seconds" );
		}
	}
}









