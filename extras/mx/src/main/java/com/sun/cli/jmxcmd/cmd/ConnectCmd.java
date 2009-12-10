/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.cmd;


import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;



import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.RequiredOptionDependency;
import com.sun.cli.jcmd.util.cmd.DisallowedCmdDependency;


import com.sun.cli.jmxcmd.spi.JMXConnectorProvider;
import com.sun.cli.jmxcmd.spi.JMXConnectorProviderInfo;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.IllegalUsageException;

import com.sun.cli.jcmd.util.misc.TokenizerImpl;
import com.sun.cli.jcmd.util.misc.TokenizerParams;
import com.sun.cli.jcmd.util.misc.TokenizerException;


import com.sun.cli.jmxcmd.support.ConnectInfo;
import com.sun.cli.jmxcmd.support.ConnectionMgr;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;




/**
	Manages connections to MBeanServers.
 */
public class ConnectCmd extends JMXCmd
{
		public
	ConnectCmd( final CmdEnv env )
	{
		super( env );
	}
	
		static JMXConnectorProviderInfo
	getProviderInfo( final Class theClass )
		throws Exception
	{
		return( JMXConnectorProviderInfo.InfoGetter.getInfo( theClass ) );
	}

		public CmdHelp
	getHelp()
	{
		return( new ConnectCmdHelp() );
	}

	final static String	CONNECT_NAME			= "connect";
	final static String	LIST_CONNECTIONS_NAME	= "list-connections";
	final static String	CLOSE_CONNECTION_NAME	= "close-connection";
	
	
	public final class ConnectCmdHelp extends CmdHelpImpl
	{
		public	ConnectCmdHelp()	{ super( getCmdInfos() ); }
		
		
	static final String	SYNOPSIS			= "connect to an MBeanServer";
	
	private final static String	CONNECT_TEXT		=
	"Connects to the specified host and port with optional username and password and protocol.\n" +
	"Examples:\n" +
	"    connect --port 8686 local-rmi  // new connection named 'local-rmi'\n" +
	"    connect --port 8687 --protocol jmxmp local-jmxmp   // new connection named 'local-jmxmp'\n" +
	"    connect local-rmi              // use existing connection\n" +
	"    connect local-jmxmp            // use existing connection\n" +
	"    list-connections               // use existing connection\n" +
	"    close-connection local-rmi     // close connection\n" +
    "\n" +
	"\nNotes:\n" +
	"If --host is not specified, then localhost is used.\n" +
	"If --protocol is not specified, the rmi (rmi_jrmp) is used.  \n" +
	"If user and password-file are not specified, no user and password are used." +
	"Additional options as name/value pairs may be specified and will be passed to the JMX connector " +
	"as additional configuration data.\n" + 
	"\nThe file specified by --password-file should be of the following format:\n" +
	"user1=password1\n" +
	"user2=password2\n" +
	"...\n" +
	"In this case, the --user option is required, and will be used to lookup the password in the file." +
	"";

		
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( CONNECT_TEXT ); }
		
			public String
		toString()
		{
			final String usage	= super.toString();
			
			String extra	= null;
				
			try
			{
				extra	= "\n\nConnector usage:\n";
			
				final JMXConnectorProvider []	providers	= getConnectionMgr().getProviders();
				 
				
				for ( int i = 0; i < providers.length; ++i )
				{
					extra	= extra + providers[ i ].getClass().getName() + ":\n";
					
					final JMXConnectorProviderInfo	info	= getProviderInfo( providers[ i ].getClass() );
					
					if ( info != null )
					{
						extra	= extra + info.getUsage() + "\n";
					}
					else
					{
						extra	= extra + "<none available>";
					}
					
					extra	= extra + "\n";
				}
			}
			catch( Exception e )
			{
			}
			
			return( usage + extra  );
		}
	}
	
	private final static OptionInfo HOST_OPTION		= new OptionInfoImpl( "host", "h", "host-or-ip");
	private final static OptionInfo PORT_OPTION		= new OptionInfoImpl( "port", "p", "port-number");
	private final static OptionInfo USER_OPTION		= new OptionInfoImpl( "user", "u", "username");
	private final static OptionInfo PASSWORD_FILE_OPTION	= createPasswordFileOption();
	private final static OptionInfo SASL_OPTION		= createSaslOption();
	private final static OptionInfo JNDI_NAME_OPTION	= new OptionInfoImpl( "jndi-name", "j", "name");
	private final static OptionInfo URL_OPTION	= new OptionInfoImpl( "url", "l", "url");
	private final static OptionInfo TRUSTSTORE_OPTION	=
				new OptionInfoImpl( "truststore", "t", PATH_ARG, false );
	private final static OptionInfo PROTOCOL_OPTION		= new OptionInfoImpl( "protocol", "r", "id" );
	private final static OptionInfo OPTIONS_OPTION		=
			new OptionInfoImpl( "options", "o", "<name>=<value>[,<name>=<value>]*" );
	private final static OptionInfo CACHE_MBEAN_INFO_OPTION		= new OptionInfoImpl( "cache-mbean-info", "c" );
	
	private final static OptionInfo PROMPT_OPTION	= createPromptOption();
	
	static private final OptionInfo[]	OPTION_INFOS	=
	{
		HOST_OPTION,
		PORT_OPTION,
		USER_OPTION,
		PASSWORD_FILE_OPTION,
		CACHE_MBEAN_INFO_OPTION,
		PROMPT_OPTION,
		SASL_OPTION,
		JNDI_NAME_OPTION,
		URL_OPTION,
		TRUSTSTORE_OPTION,
		PROTOCOL_OPTION,
		OPTIONS_OPTION,
	};
	
		
	static
	{
		// we can't assume anything about the combination of others options; that is up to the
		// providers
		
		// close-connection does not allow any options; add this to each option
		final DisallowedCmdDependency	closeD	= new DisallowedCmdDependency( new String[] { CLOSE_CONNECTION_NAME } );
		HOST_OPTION.addDependency( closeD );
		PORT_OPTION.addDependency( closeD );
		USER_OPTION.addDependency( closeD );
		PASSWORD_FILE_OPTION.addDependency( closeD );
		PROTOCOL_OPTION.addDependency( closeD );
		OPTIONS_OPTION.addDependency( closeD );
		TRUSTSTORE_OPTION.addDependency( closeD );
		TRUSTSTORE_OPTION.addDependency( new RequiredOptionDependency( PASSWORD_FILE_OPTION ) );
		SASL_OPTION.addDependency( closeD );
		
		// list-connections does not allow any options; add this to each option
		final DisallowedCmdDependency	noListCmd	=
				new DisallowedCmdDependency( new String[] { LIST_CONNECTIONS_NAME } );
		HOST_OPTION.addDependency( noListCmd);
		PORT_OPTION.addDependency( noListCmd );
		USER_OPTION.addDependency( noListCmd );
		PASSWORD_FILE_OPTION.addDependency( noListCmd );
		TRUSTSTORE_OPTION.addDependency( noListCmd );
		PROTOCOL_OPTION.addDependency( noListCmd );
		OPTIONS_OPTION.addDependency( noListCmd );
		SASL_OPTION.addDependency( noListCmd );
		
		SASL_OPTION.addDependency( new RequiredOptionDependency( USER_OPTION ) );
	}


	private final static CmdInfo	CONNECT_INFO	=
		new CmdInfoImpl( CONNECT_NAME,
		new OptionsInfoImpl( OPTION_INFOS ),
		new OperandsInfoImpl( "name", 0, 1) );
		
	private final static CmdInfo	LIST_CONNECTIONS_INFO	=
		new CmdInfoImpl( LIST_CONNECTIONS_NAME );
		
	private final static CmdInfo	CLOSE_CONNECTION_INFO	=
		new CmdInfoImpl( CLOSE_CONNECTION_NAME, null, new OperandsInfoImpl( "<name>", 1, 1) );
		
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( CONNECT_INFO, LIST_CONNECTIONS_INFO, CLOSE_CONNECTION_INFO ) );
	}
	
    @Override
		protected void
	establishProxy()
		throws Exception
	{
		// defeat default behavior; this is the 'connect' command after all
	}
	
		void
	handleNamedConnect( String name )
		throws Exception
	{
		ConnectInfo	connectInfo	= null;
		
		// see if it's a name for an existing connection
		final String connectString = (String)envGet( connectionNameToEnvName( name ) );
		if ( connectString == null )
		{
			if ( findMBeanServer( name ) != null )
			{
				connectInfo	= getMBeanServerConnectInfo( name );
			}
		}
		else
		{
			printDebug( "Connecting: " + connectString );
			connectInfo	= new ConnectInfo( connectString );
		}
		
		if ( connectInfo != null )
		{
			if ( connectInfo.equals( getConnectionMgr().getConnectInfo( name ) ) )
			{
				println( "Connection already active: " + connectInfo.toString() );
				establishProxy( name, connectInfo );
			}
			else
			{
				establishProxy( name, connectInfo );
				
				println( "Connection " + name + " (" + connectInfo.toString() + ") is now the active connection" );
			}
			envPut( DEFAULT_CONNECTION_NAME_KEY, name, true );
		}
		else
		{
			printError( "ERROR: No such named connection: " + name );
		}
	}
	
	
		void
	handleConnect( final String name, final ConnectInfo connectInfo )
		throws Exception
	{
		final String	envName	= connectionNameToEnvName( name );
		
		printDebug( "handleConnect: setting: " + envName + "=" + connectInfo.toString() ); 
		envRemove( envName );
		envPut( envName, connectInfo.toString(), true);
		
		handleNamedConnect( name );
	}
	
		static <T,V> void
	maybePut( Map<T,V> m, T key, V value )
	{
		if ( value != null )
		{
			m.put( key, value );
		}
	}
	
	
		static void
	addOptions( String optionsString, Map<String,String> m )
		throws TokenizerException
	{
		// Tokenize them into name/value pairs
		final char		PAIR_DELIM	= ',';
		
		final TokenizerParams	params	= new TokenizerParams();
		params.mDelimiters	= "" + PAIR_DELIM;
		params.ensureDelimitersEscapable();
		final TokenizerImpl tk	= new TokenizerImpl( optionsString, params);
			
		final String[]	tokens	= tk.getTokens();
		
		final char	NAME_VALUE_DELIM	= '=';
		for( int i = 0; i < tokens.length; ++i )
		{
			final String	pair	= tokens[ i ];
			int		delimIndex	= pair.indexOf( NAME_VALUE_DELIM );
			if ( delimIndex < 0 )
			{
				delimIndex	= pair.length();
			}
			
			final String	name	= pair.substring( 0, delimIndex );
			final String	value	= pair.substring( delimIndex + 1, pair.length() );
			
			System.out.println( "Adding option: " + name + " = " + value );
			m.put( name, value );
		}
	}
	
	
		void
	listNamedConnections()
		throws Exception
	{
		println( "Named connections:" );
		
		final Iterator	iter	= getEnvKeys( JMXCmdEnvKeys.CONNECT_NAME_PREFIX + ".+" ).iterator();
		
		while ( iter.hasNext() )
		{
			final String	envName	= (String)iter.next();
			final String	value	= (String)envGet( envName );
			
			println( envNameToConnectionName( envName ) + ": " + new ConnectInfo( value ) );
		}
	}
	
	
		void
	listActiveConnections()
		throws Exception
	{
		println( "Active connections:" );
		
		final ConnectionMgr	mgr		= getConnectionMgr();
		final Iterator		iter	= mgr.getNames().iterator();
		
		while ( iter.hasNext() )
		{
			final String	name	= (String)iter.next();
			
			println( name + ": " + mgr.getConnectInfo( name ) );
		}
	}
	
	
		protected void
	closeConnections( String[] connectionNames )
		throws Exception
	{
		for( int i = 0; i < connectionNames.length; ++i )
		{
			final String name	= connectionNames[ i ];
			
			if ( getConnectionMgr().getConnectInfo( name ) != null )
			{
				getConnectionMgr().close( name );
			}
			else
			{
				println( "Connection " + name + " is not open." );
			}
		}
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd			= getSubCmdNameAsInvoked();
		final String [] operands	= getOperands();
		
		// at least one argument
		if ( operands.length > 1 )
		{
			throw new IllegalArgumentException( "requires 0 or 1 operands" );
		}
		
		if ( cmd.equals( LIST_CONNECTIONS_NAME )  )
		{
			listNamedConnections();
			println( "" );
			listActiveConnections();
		}
		else if ( cmd.equals( CLOSE_CONNECTION_NAME ) )
		{
			requireNumOperands( 1 );
			closeConnections( operands );
		}
		else if ( countOptions() == 0 )
		{
			String	name	= null;
			
			// named connection
			if ( operands.length == 0 )
			{
				name	= getDefaultConnectionName();
				
				if ( name == null )
				{
					throw new IllegalUsageException( cmd, "no default connection is available" );
				}
			}
			else
			{
				if ( operands.length != 1 )
				{
					throw new IllegalUsageException( cmd, "exactly one connection name is required" );
				}
				name	= operands[ 0 ];
			}
			
			handleNamedConnect( name );
		}
		else
		{
			final String	host			= getString( HOST_OPTION.getShortName(), "localhost" );
			final String	port			= getString( PORT_OPTION.getShortName(), null);
			String	protocol		= getString( PROTOCOL_OPTION.getShortName(), null );
			final String	user			= getString( USER_OPTION.getShortName(), null );
			final String	passwordFile	= getString( PASSWORD_FILE_OPTION.getShortName(), null );
			final String	trustStoreFile	= getString( TRUSTSTORE_OPTION.getShortName(), null );
			final String	jndiName		= getString( JNDI_NAME_OPTION.getShortName(), "/jmxrmi" );
			final String	url				= getString( URL_OPTION.getShortName(), null );
			final String	options			= getString( OPTIONS_OPTION.getShortName(), null );
			final boolean	prompt			= getBoolean( PROMPT_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
			final String	sasl			= getString( SASL_OPTION.getShortName(), null );
			final boolean	cacheMBeanInfo	=
				getBoolean( CACHE_MBEAN_INFO_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
        
            if ( protocol == null || protocol.equals("rmi_jrmp") )
            {
                protocol = "rmi";
            }
			
			final HashMap<String,String>	params	= new HashMap<String,String>();
			maybePut( params, JMXConnectorProvider.HOST, host );
			maybePut( params, JMXConnectorProvider.PORT, port );
			maybePut( params, JMXConnectorProvider.PROTOCOL, protocol );
			maybePut( params, JMXConnectorProvider.USER, user );
			maybePut( params, JMXConnectorProvider.PASSWORD_FILE, passwordFile );
			maybePut( params, JMXConnectorProvider.TRUSTSTORE_FILE, trustStoreFile );
			maybePut( params, JMXConnectorProvider.SASL, sasl );
			maybePut( params, JMXConnectorProvider.JNDI_NAME, jndiName );
			maybePut( params, JMXConnectorProvider.URL, url );
			maybePut( params, ConnectInfo.CACHE_MBEAN_INFO, "" + cacheMBeanInfo );
		
			if ( passwordFile != null || prompt )
			{
				if ( user != null )
				{
					params.put( JMXConnectorProvider.PASSWORD,
						getPasswordForUser( user, passwordFile, prompt ) );
				}
				
			}
			else if ( user != null )
			{
				if ( passwordFile == null && ! prompt )
				{
					throw new IllegalUsageException( cmd, USER_OPTION.getLongName() + " requires a password or prompt" );
				}
				
				params.put( JMXConnectorProvider.PASSWORD,
					getPasswordForUser( user, passwordFile, prompt ) );
			}
			
			if ( trustStoreFile != null )
			{
				params.put( JMXConnectorProvider.TRUSTSTORE_PASSWORD,
					getPasswordForUser( JMXConnectorProvider.TRUSTSTORE_USER, passwordFile, prompt ) );
			}
			
			if ( options != null )
			{
				addOptions( options, params );
			}
			
			final String	connectionName	= (operands.length > 0) ?
									operands[ 0 ] : "DEFAULT";
			
			handleConnect( connectionName, new ConnectInfo( params ) );
		}
	}
	
}






