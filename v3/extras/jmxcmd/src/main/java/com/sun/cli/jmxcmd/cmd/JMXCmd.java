/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/JMXCmd.java,v 1.29 2005/11/15 20:59:53 llc Exp $
 * $Revision: 1.29 $
 * $Date: 2005/11/15 20:59:53 $
 */
 

package com.sun.cli.jmxcmd.cmd;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.remote.JMXConnector;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerConnection;

import com.sun.cli.jcmd.framework.CmdBase;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.FileNames;
import com.sun.cli.jcmd.framework.CmdException;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import org.glassfish.admin.amx.util.Output;

import org.glassfish.admin.amx.util.DebugState;
import org.glassfish.admin.amx.util.ExceptionUtil;

import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;

import com.sun.cli.jmxcmd.support.AliasMgr;
import com.sun.cli.jmxcmd.support.AliasMgrImpl;
import com.sun.cli.jmxcmd.support.AliasMgrHashMapImpl;
import com.sun.cli.jmxcmd.support.StandardAliasesIniter;
import com.sun.cli.jmxcmd.support.CLISupport;
import com.sun.cli.jmxcmd.support.CLISupportMBean;
import com.sun.cli.jmxcmd.support.ConnectionMgr;
import com.sun.cli.jmxcmd.support.ConnectionMgrImpl;
import com.sun.cli.jmxcmd.support.ConnectInfo;

import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import com.sun.cli.jmxcmd.util.MBeanServerConnection_Debug;

import org.glassfish.admin.amx.util.jmx.JMXUtil;

import com.sun.cli.jmxcmd.spi.JMXConnectorProvider;
import com.sun.cli.jmxcmd.spi.InProcessConnectorProvider;

import com.sun.cli.jmxcmd.util.ConnectionSource;

import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;
import org.glassfish.admin.amx.util.ClassUtil;

/**
	Base class for all commands which access an MBeanServer.  Features:
		- supplies notion of connecting to the server.
		- supplies the handling for pluggable Connector providers
		- supplies aliases feature
		- supplies the notion of a target
 */
public abstract class JMXCmd extends CmdBase implements Output
{
	final String []	ALL_TARGET	= new String [] { "all" };
	
	
	public final static String		DEFAULT_CONNECTION_NAME_KEY = "DEFAULT_CONNECTION";
	
	public final static String		ALIAS_VALUE_DELIM	= " ";
	
	/**
		Instantiate with the specified environment.
		
		@param env	the CmdEnv to be used with this command.
	 */
		protected
	JMXCmd( final CmdEnv env )
	{
		super( env );
	}
	
	
	/**
		Get the ConnectionMgr that should be used.
	 */
		protected synchronized ConnectionMgr
	getConnectionMgr(  )
	{
		ConnectionMgr	mgr	= (ConnectionMgr)envGet( JMXCmdEnvKeys.CONNECTION_MGR );
		if ( mgr == null )
		{
			final String	providersString	= ((String)envGet( JMXCmdEnvKeys.PROVIDERS, "")).trim();
			mgr	= initConnectionMgr( providersString ); 
			envPut( JMXCmdEnvKeys.CONNECTION_MGR, mgr, false );
		}
		
		return( mgr );
	}
	
	protected final class ConnectionMgrConnectionSource
		implements ConnectionSource
	{
		private final ConnectionMgr			mConnectionMgr;
		private final String				mConnectionName;
		private volatile MBeanServerConnection		mMBeanServerConnection;
        private volatile JMXConnector                mJMXConnector;
		
			public
		ConnectionMgrConnectionSource(
			final ConnectionMgr	connectionMgr,
			final String		connectionName )
		{
			mConnectionMgr	= connectionMgr;
			mConnectionName	= connectionName;
		}
        
        public JMXConnector getJMXConnector(final boolean forceNew )
        {
            try
            {
                getMBeanServerConnection(forceNew);
            }
            catch( IOException e )
            {
                throw new RuntimeException(e);
            }
            return mJMXConnector;
        }
		
			public MBeanServerConnection
		getExistingMBeanServerConnection()
        {
            return mMBeanServerConnection;
        }
        
			public MBeanServerConnection
		getMBeanServerConnection( final boolean forceNew )
			throws java.io.IOException
		{
			if ( mMBeanServerConnection == null || forceNew )
			{
				MBeanServerConnection	conn	= mMBeanServerConnection;
			
				final ConnectInfo	connectInfo	=
					mConnectionMgr.getConnectInfo( mConnectionName );
				
				JMXConnector	jmxConn	= null;
				try
				{
					jmxConn	= mConnectionMgr.connect( mConnectionName, connectInfo, false);
				}
				catch( Exception e )
				{
					throw new IOException( e.getMessage() );
				}
                
                mJMXConnector = jmxConn;
                
				conn	= mJMXConnector.getMBeanServerConnection();
				
				// wrap the connection with our hooked version
                final DebugState ds = new DebugState.Impl(false);
				final MBeanServerConnection_Debug hookedConn = new MBeanServerConnection_Debug( conn, ds, JMXCmd.this );

				if ( "true".equalsIgnoreCase( connectInfo.getParam( connectInfo.CACHE_MBEAN_INFO ) ) )
				{
					//printDebug( "Caching the connection: " + mConnectionName);
					//hookedConn.cacheMBeanInfo( true );
				}
				else
				{
					printDebug( "NOT Caching the connection: " + mConnectionName);
				}
				
				
				mMBeanServerConnection	= hookedConn;
			}
			
			return( mMBeanServerConnection );
		}
		
	}
	
	/**
		Get the ConnectionMgr that should be used.
	 */
		protected ConnectionSource
	createConnectionSource( final String	connectionName )
		throws Exception
	{
		final ConnectInfo	connectInfo	= getConnectInfo( connectionName );
		
		if ( connectInfo == null )
		{
			throw new IllegalArgumentException( connectionName );
		}
		
		return( createConnectionSource( connectionName, connectInfo ) );
	}
	
	/**
		Get the ConnectionMgr that should be used.
	 */
		protected ConnectionSource
	createConnectionSource(
		final String		connectionName,
		final ConnectInfo	connectInfo )
		throws Exception
	{
		getConnectionMgr().connect( connectionName, connectInfo, false );
		return( new ConnectionMgrConnectionSource( getConnectionMgr(), connectionName ) );
	}
	
	/**
		Get the ConnectionMgr that should be used.
	 */
		protected ConnectionSource
	getConnectionSource( final String	connectionName )
		throws Exception
	{
		return( createConnectionSource( connectionName ) );
	}
	
	/**
		Initialized the ConnectionMgr with any configured JMXConnectorProviders.
		
		@param providers	a list of provider classnames
	 */
		private synchronized ConnectionMgr
	initConnectionMgr( String providersString )
	{
		final ConnectionMgr	mgr	= new ConnectionMgrImpl( );
		
		if ( providersString != null && providersString.length() != 0 )
		{
			final String []	providers	= providersString.split( "," );
			
			for( int i = 0; i < providers.length; ++i )
			{
				try
				{
                    @SuppressWarnings("unchecked")
					final Class<? extends JMXConnectorProvider>	theClass =
                        (Class<? extends JMXConnectorProvider>)ClassUtil.getClassFromName( providers[ i ] );
				
					mgr.addProvider( theClass );
					// println( "Added JMXConnectorProvider: " + providers[ i ] );
				}
				catch( Exception e )
				{
					printError( "WARNING: Can't add JMXConnectorProvider: " + providers[ i ] );
				}
			}
		}
		
		return( mgr );
	}
	
	
	
	/**
		Convert a connection name to the key it should use in the CmdEnv.
		
		@param connectionName	the connection name
		@return	the translated name
	 */
		protected String
	connectionNameToEnvName( String connectionName )
	{
		if ( connectionName.startsWith( JMXCmdEnvKeys.CONNECT_NAME_PREFIX ) )
		{
			throw new IllegalArgumentException( "connectionName already begins with " +
				JMXCmdEnvKeys.CONNECT_NAME_PREFIX);
		}

		return( JMXCmdEnvKeys.CONNECT_NAME_PREFIX + connectionName );
	}
	
	/**
		Convert a key as used in the CmdEnv to its connection name
		
		@param envKey	the key
		@return	the translated name
	 */
		protected String
	envNameToConnectionName( String envKey )
	{
		return( envKey.substring( JMXCmdEnvKeys.CONNECT_NAME_PREFIX.length(), envKey.length() ) );
	}
	
	
		private synchronized Map<String,MBeanServer>
	getMBeanServers( )
	{
        @SuppressWarnings("unchecked")
		Map<String,MBeanServer>	map	= (HashMap<String,MBeanServer>)envGet( JMXCmdEnvKeys.MBEAN_SERVERS );
		
		if ( map == null )
		{
			map	= new HashMap<String,MBeanServer>();
			envPut( JMXCmdEnvKeys.MBEAN_SERVERS, map, false );
		}
		
		return( map );
	}
	
		protected java.util.Set<String>
	getMBeanServerNames( )
	{
		return( getMBeanServers().keySet() );
	}
	
		protected void
	addMBeanServer( String name, MBeanServer server )
	{
		removeMBeanServer( name );
		
		getMBeanServers().put( name, server );
	}
	
		protected MBeanServer
	findMBeanServer( String name )
	{
		return( (MBeanServer)getMBeanServers().get( name ) );
	}
	
	
		protected MBeanServer
	getMBeanServer()
		throws CmdException
	{
		final MBeanServer	server	= findMBeanServer( getDefaultConnectionName() );
		
		if ( server == null )
		{
			throw new CmdException( getSubCmdNameAsInvoked(), "Not connected to a local MBeanServer" );
		}
		
		return( server );
	}
	
		protected void
	removeMBeanServer( String name )
	{
		final MBeanServer	server	= findMBeanServer( name );
		if ( server != null )
		{
			getMBeanServers().remove( name );
			
			try
			{
				JMXUtil.unregisterAll( server );
			}
			catch( Exception e )
			{
				printDebug( ExceptionUtil.getStackTrace( e ) );
			}
			
			MBeanServerFactory.releaseMBeanServer( server );
		}
	}
	
		protected ConnectInfo
	getMBeanServerConnectInfo( final String name )
	{
		final HashMap<String,String>	params	= new HashMap<String,String>();
		
		params.put( JMXConnectorProvider.PROTOCOL,
			InProcessConnectorProvider.IN_PROCESS_PROTOCOL );
		params.put( InProcessConnectorProvider.MBEAN_SERVER_OPTION, name );
			
		final ConnectInfo	connectInfo	= new ConnectInfo( params );
		
		return( connectInfo );
	}
	
	
	
	/**
		Convenience class when a simple implement is desired.
	 */
	private final boolean	getDebugConnection()
	{
		boolean	debugConnection	= false;
		
		final String value	= (String)envGet( JMXCmdEnvKeys.DEBUG_CONNECTION );
		if ( value != null )
		{
			debugConnection	= value.equals( TRUE_STRING );
		}
		
		return( debugConnection );
	}
	
	private static final String	TRUE_STRING	= Boolean.TRUE.toString();
	private final class ConnectionDebugState implements DebugState
	{
		public ConnectionDebugState( ) {}
		public boolean	getDebug()
		{
			return( getDebugConnection() );
		}
	}
	
	/**
		Set the current proxy assigning it the specified name and connection parameters.
		Update the environment with JMXCmdEnvKeys.PROXY, JMXCmdEnvKeys.CONNECTION and 
		connectionNameToEnvName( name ).
		
		Does not display anything to the user.
		
		@param name				the name to associate with the connection parameters
		@param connectInfo		the connection parameters
		@return a proxy to a CLISupportMBeanProxy
	 */
		private CLISupportMBeanProxy
	setupProxy(
		final String name,
		final ConnectInfo connectInfo )
		throws Exception
	{
		final ConnectionSource			connSource	= createConnectionSource( name );
        MBeanServerConnection conn = null;
		try
        {
            conn		= connSource.getMBeanServerConnection( false );
        }
        catch( final Exception e )
        {
            getConnectionMgr().close(name);
            conn = connSource.getMBeanServerConnection( true );
        }
        
		//final MBeanServerConnection conn	=
			//new org.glassfish.admin.amx.util.jmx.MBeanServerConnection_Perf( connX, getOutput() );
		

		final CLISupportMBean		cliSupport		= new CLISupport( conn, getAliasMgr());
		final CLISupportMBeanProxy	proxy	= new CLISupportMBeanProxy( getAliasMgr(), cliSupport );
			
		envPut( connectionNameToEnvName( name ), connectInfo.toString(), true );
		envPut( JMXCmdEnvKeys.PROXY, proxy, false );
		envPut( JMXCmdEnvKeys.CONNECTION_SOURCE, connSource, false );
		
		return( proxy );
	}
	
	/**
		Remove the current proxy from the environment.
	 */
		void
	clearProxy(  )
	{
		envRemove( JMXCmdEnvKeys.PROXY );
		envRemove( JMXCmdEnvKeys.CONNECTION_SOURCE );
	}
	
	/**
		The current connection has received an IOException.
	 */
		protected MBeanServerConnection
	connectionIOException( IOException e, boolean refresh )
		throws IOException
	{
		clearProxy();
		
		MBeanServerConnection	conn	= null;
		if ( true || refresh )
		{
			try
			{
				establishProxy();
				conn	= getConnection();
			}
			catch( Exception ee )
			{
				printError( "Can't reestablish connection" );
                ee.printStackTrace();
				throw e;
			}
		}
		return( conn );
	}
	/**
		The current connection has received an IOException.
		protected MBeanServerConnection
	connectionIOException( IOException e )
		throws IOException
	{
		return( connectionIOException( e, true ) );
	}
	 */
	
	
	/**
		Establish a proxy with the specified connection parameters and name,
		and inform the user accordingly.
		
		@param name				the name to associate with the connection parameters
		@param connectInfo		the connection parameters
		@return a proxy to a CLISupportMBeanProxy
	 */
		CLISupportMBeanProxy
	establishProxy( String name, ConnectInfo connectInfo  )
		throws Exception
	{
		CLISupportMBeanProxy	proxy	= null;
		
		proxy	= setupProxy( name, connectInfo );
		
		final String host		= connectInfo.getParam( JMXConnectorProvider.HOST );
		final String port		= connectInfo.getParam( JMXConnectorProvider.PORT );
		String protocol			= connectInfo.getParam( JMXConnectorProvider.PROTOCOL );
		if ( protocol == null )
		{
			protocol 	= "jmxmp";
		}
		
		println( "Established connection to " + name + " using " + protocol );
	
		envPut( DEFAULT_CONNECTION_NAME_KEY, name, true );
		return( proxy );
	}
	
	/**
		Get the name of the default (current) connection.  The connection may or
		may not be connected.
	 */
		protected String
	getDefaultConnectionName()
	{
		return( (String)envGet( DEFAULT_CONNECTION_NAME_KEY ) );
	}
	
	/**
		Same as establishProxy( DEFAULT_CONNECTION_NAME, connectInfo )
		
		@param connectInfo		the connection parameters
		@return a proxy to a CLISupportMBeanProxy
	 */
		CLISupportMBeanProxy
	establishProxy( ConnectInfo connectInfo )
		throws Exception
	{
		final String	connectionName	= getDefaultConnectionName();
		if ( connectionName == null )
		{
			throw new CmdException( getSubCmdNameAsInvoked(), "No default connection is available" );
		}
		
		final CLISupportMBeanProxy	proxy	= establishProxy( connectionName, connectInfo );
		return( proxy );
	}
	
	/**
		Get the ConnectInfo for a connection (not necessarily the current one).
		
		@param connectionName
		@return ConnectInfo
	 */
		protected ConnectInfo
	getConnectInfo( String connectionName )
	{
		ConnectInfo		info	= null;
		if ( connectionName != null )
		{
			final String	envName			= connectionNameToEnvName( connectionName );
			final String	connectString	= (String)envGet( envName );
			
			if ( connectString != null )
			{
				info	= new ConnectInfo( connectString );
			}
			else
			{
				info	= getMBeanServerConnectInfo( connectionName );
			}
		}
		
		return( info );
	}
	
	/**
		Establish a proxy to the current default connection.
	 */
		protected void
	establishProxy(  )
		throws Exception
	{
		if ( envGet( JMXCmdEnvKeys.PROXY ) == null )
		{
			establishProxy( getConnectInfo( getDefaultConnectionName() )  );
		}
		
		if ( envGet( JMXCmdEnvKeys.PROXY ) == null )
		{
			throw new java.io.IOException( "can't connect" );
		}
	}
	
	/**
		Get the current proxy.
		
		@return the current proxy or null if none
	 */
		protected CLISupportMBeanProxy
	getProxy()
		throws Exception
	{
		establishProxy();
		final CLISupportMBeanProxy	proxy	= (CLISupportMBeanProxy)envGet( JMXCmdEnvKeys.PROXY );
		
		return( proxy );
	}
	

	/**
		Get the current connection
	 */
		protected MBeanServerConnection
	getConnection(  )
	{
		final String	connectionName	= getDefaultConnectionName();
		
		MBeanServerConnection	conn	= null;
		try
		{
			final ConnectionSource	connSource	= getConnectionSource( connectionName );
			
			conn	=  connSource.getMBeanServerConnection( false );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
		
		return( conn );
	}
	
	
	protected final static String	ALIASES_FILE_SUFFIX	= "-aliases.props";
	
		public static java.io.File
	getAliasesFile()
	{
		return( FileNames.getInstance().getPrefsDirFile( ALIASES_FILE_SUFFIX ) );
	}
	
	/**
		Create the aliase manager and initialize it from the aliases file
	 */
		static AliasMgr
	createAliasMgr()
	{
		final AliasMgrHashMapImpl	aliasMgrSPI	= new AliasMgrHashMapImpl();
		final AliasMgr				aliasMgr	= new AliasMgrImpl( aliasMgrSPI );
		
		final java.io.File	aliasesFile	= getAliasesFile();
		if ( aliasesFile.exists() )
		{
			try
			{
				aliasMgrSPI.load( aliasesFile );
			}
			catch( Exception e )
			{
				System.err.println( "can't load aliases file " +
					aliasesFile + ": " + e.getMessage());
			}
		}
		StandardAliasesIniter.init( aliasMgr );
			
		return( aliasMgr );
	}
	
	
	/**
		Get the alias manager.
		
		@return the current proxy or null if none
	 */
		protected AliasMgr
	getAliasMgr()
	{
		AliasMgr	aliasMgr	= (AliasMgr)envGet( JMXCmdEnvKeys.ALIAS_MGR );
		
		if ( aliasMgr == null )
		{
			aliasMgr	= createAliasMgr();
			envPut( JMXCmdEnvKeys.ALIAS_MGR, aliasMgr, false);
		}
		
		return( aliasMgr );
	}
	
	/**
		Setup prior to execution of the command
	 */
		protected void
	preExecute()
		throws Exception
	{
		getConnectionMgr();
		super.preExecute();
	}
	
	/**
		Handle the exception.  If it's an IOException, this means that the current
		connection has died.
		
		
		@param e	the exception
	 */
		protected void
	handleException( final Exception e ) throws Exception
	{
		if ( e instanceof IOException )
		{
			connectionIOException( (IOException)e, true );
		}
		
		super.handleException( e );
	}
	
	
	/**
		Get the implicit targets in the environment.
		
		@return a String[] of targets
	 */
		protected String []
	getEnvTargets()
	{
		final String	targetsEnv	= (String)envGet( JMXCmdEnvKeys.TARGETS );
		if ( targetsEnv == null )
			return( null );
		
		// convert to String []
		final String []	targets	= (String [])
							TargetPersister.DEFAULT.asObject( targetsEnv );
		
		return( targets );
	}
	
		protected ObjectName[]
	resolveTargets( CLISupportMBeanProxy proxy, final String[] targets )
		 throws Exception
	{
		final ObjectName[]	resolved	= proxy.resolveTargets( targets );
		
		envPut( JMXCmdEnvKeys.LAST_RESOLVED_TARGETS, resolved, false );
		
		// create an alias out of the results
		final String	aliasValue	= ArrayStringifier.stringify( resolved, ALIAS_VALUE_DELIM );
		getAliasMgr().deleteAlias( JMXCmdEnvKeys.LAST_RESOLVED_ALIAS );
		getAliasMgr().createAlias( JMXCmdEnvKeys.LAST_RESOLVED_ALIAS, aliasValue );
		
		
		return( resolved );
	}
	
	
	private final static class TargetPersister
		implements com.sun.cli.jcmd.util.misc.ValuePersister
	{
		final static TargetPersister	DEFAULT	= new TargetPersister();
		final static String				TARGET_DELIM	= ALIAS_VALUE_DELIM;
		
			public String
		asString( Object value )
		{
			return( ArrayStringifier.stringify( (String [])value, TARGET_DELIM ) );
		}
		
			public Object
		asObject( String value )
		{
			return( value.split( TARGET_DELIM ) );
		}
	}
	
	/**
		Store the targets into the environment which can be retreived by
		getEnvTargets().
		
		@param targets	the targets to store.
	 */
		protected void
	putEnvTargets( String [] targets )
	{
		// put targets into environment
		final String	targetsStr	= TargetPersister.DEFAULT.asString( targets );
		envPut( JMXCmdEnvKeys.TARGETS, targetsStr, true);
	}
	
	/**
		Determine what the targets are for the command.  If none were specified, use
		the environment targets.
		
		@return a String[] of targets
	 */
		protected String []
	getTargets()
	{
		String [] 	targets	= getOperands();
		
		if ( targets.length == 0 )
		{
			targets	= getEnvTargets();
		}
		
		return( targets );
	}
	
	
		protected String
	promptForPassword( String msg )
		throws IOException
	{
		return( promptUser( msg ) );
	}
	
		protected String
	getPasswordForUser( String user, String passwordFile, boolean prompt)
		throws IOException, CmdException
	{
		if ( user == null )
		{
			throw new IllegalArgumentException( "User must be non-null" );
		}
		
		if ( passwordFile == null && ! prompt )
		{
			throw new IllegalArgumentException( "Must specify either password file or prompt" );
		}
		
		String	password	= null;
				
		if ( passwordFile != null )
		{
			final FileInputStream	input	= new FileInputStream( passwordFile );
			try
			{
				final Properties	props	= new Properties();
				props.load( input );
				
				password	= (String)props.get( user );
			}
			finally
			{
				input.close();
			}
		}
		
		if ( password == null )
		{
			if ( prompt )
			{
				password	= promptForPassword( "Enter password for " + user + ":"  );
			}
			else
			{
				throw new CmdException( getSubCmdNameAsInvoked(),
					"No password found for user " + quote( user ) +
					" in file " + quote( passwordFile.toString() ) );
			}
		}
		
		return( password );
	}
	
	
	/**
		Create the standard "attributes" option.
	 */
		protected static OptionInfoImpl
	createAttributesOption()
	{
		return( new OptionInfoImpl( "attributes", "a", ATTR_LIST_ARG) );
	}
		protected static OptionInfoImpl
	createPasswordFileOption()
	{
		return( new OptionInfoImpl( "password-file", "f", PATH_ARG, false ) );
	}
	
		protected static OptionInfoImpl
	createPromptOption()
	{
		return( new OptionInfoImpl( "prompt", "P" ) );
	}
	
		protected static OptionInfoImpl
	createSaslOption()
	{
		return( new OptionInfoImpl( "sasl", "s", "PLAIN|DIGEST-MD5", false ) );
	}
	
	
	
	protected static final String	PORT_NUMBER_ARG	= "port-number";
	
	protected static final String	ATTR_LIST_ARG	= "attr-expr[,attr-expr]*";
	protected static final String	TARGET_LIST_ARG	= "target[ target]*";
	
	protected static final OperandsInfo	TARGETS_OPERAND_INFO	=
		new OperandsInfoImpl( TARGET_LIST_ARG, 0 );
};


