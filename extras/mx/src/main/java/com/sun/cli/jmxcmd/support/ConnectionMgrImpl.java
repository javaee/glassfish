/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ConnectionMgrImpl.java,v 1.11 2005/04/06 01:36:12 llc Exp $
 * $Revision: 1.11 $
 * $Date: 2005/04/06 01:36:12 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import javax.management.remote.JMXConnector;

import com.sun.cli.jmxcmd.spi.JMXConnectorProvider;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.LineReaderImpl;



public final class ConnectionMgrImpl implements ConnectionMgr
{
	private final Map<String,Connection>		mNameConnMap;
	final List<JMXConnectorProvider>			mProviders;
	
		private static void
	dm( Object o )
	{
		System.out.println( o.toString( ) );
	}
	
		public
	ConnectionMgrImpl(  )
	{
		// do allow instantiation of this class; probably a singleton,
		// but could be useful to have more than one to segregate by user for example
		
		mNameConnMap	= new HashMap<String,Connection>();
		
		mProviders	= new ArrayList<JMXConnectorProvider>();
		try
		{
			addProvider( com.sun.cli.jmxcmd.spi.JMXMPDefaultConnectorProvider.class );
			addProvider( com.sun.cli.jmxcmd.spi.RMIDefaultConnectorProvider.class );
			addProvider( com.sun.cli.jmxcmd.spi.InProcessConnectorProvider.class );
			addProvider( com.sun.cli.jmxcmd.spi.JMXServiceURLConnectorProvider.class );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
		public ConnectInfo
	getConnectInfo( String name )
	{
		final Connection	conn	= mNameConnMap.get( name );

		return( conn == null ? null : conn.mConnectInfo );
	}
	
		private Connection
	lookup( final String	name )
	{
		final Connection	conn	= mNameConnMap.get( name );
		
		return( conn );
	}
	
		private JMXConnectorProvider
	createProvider( String className )
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		final Class<? extends JMXConnectorProvider>	theClass	= ClassUtil.getClassFromName( className ).asSubclass(JMXConnectorProvider.class);
		
		final JMXConnectorProvider	provider = (JMXConnectorProvider)theClass.newInstance( );
		
		return( provider );
	}
	
		public void
	addProvider( final Class<? extends JMXConnectorProvider> provider )
		throws IllegalAccessException, InstantiationException, ClassNotFoundException
	{
		final JMXConnectorProvider	instance = provider.newInstance( );
		
		// last added = first priority
		mProviders.add( 0, instance );
	}
	
		public void
	removeProvider( final Class<? extends JMXConnectorProvider> provider )
	{
		mProviders.remove( provider );
	}
	
	

	
		public JMXConnectorProvider []
	getProviders()
	{
		JMXConnectorProvider []		providers = new JMXConnectorProvider[ mProviders.size() ];
		
		mProviders.toArray( providers );
		return( providers );
	}
	
		private Connection
	openNew(
		final String name,
		final ConnectInfo connectInfo )
		throws Exception
	{
		final Map<String,String>	map	= createMapForConnectorProvider( connectInfo );
		
		final JMXConnectorProvider []	providers	= getProviders();
			
		Connection	conn	= null;
		for( int i = 0; i < providers.length; ++i )
		{
			final JMXConnectorProvider provider	= (JMXConnectorProvider)providers[ i ];
			
			if ( provider.isSupported( map ) )
			{
				final JMXConnector	jmxConn	= provider.connect( map );
				
				conn	= new Connection( name, connectInfo, jmxConn );
				break;
			}
		}
		
		if ( conn == null )
		{
			throw new NoProviderFoundException( "No provider found for: " + connectInfo.toString() );
		}
		
		return( conn );
	}
	
	
	static final char USER_PASSWORD_DELIM	= '=';
	
		private String
	readPassword( String filename, String username )
		throws java.io.FileNotFoundException, IOException
	{
		final java.io.File			file = new java.io.File( filename );
		String password	= null;
		
		final java.io.InputStream	is = new java.io.FileInputStream( file );
		
		try
		{
			final LineReaderImpl	reader	= new LineReaderImpl( is );
			
			String	line;
			while( (line = reader.readLine( null )) != null )
			{
				// don't trim the line; spaces are allow in the password at the end
				if ( line.length() != 0 )
				{
					final int	delimIndex	= line.indexOf( USER_PASSWORD_DELIM );
					
					if ( delimIndex <= 0 )
					{
						throw new IllegalArgumentException( "Invalid line in password file: " + line );
					}
					
					final String	user	= line.substring( 0, delimIndex );
					if ( user.equals( username ) )	// case sensitive
					{
						password	= line.substring( delimIndex + 1, line.length() );
						break;
					}
				}
			}
		}
		finally
		{
			is.close();
		}
		
		if ( password == null )
		{
			throw new IllegalArgumentException( "No password found for user: " + username );
		}
		
		
		return( password );
	}
	
		private Map<String,String>
	createMapForConnectorProvider( final ConnectInfo connectInfo )
		throws java.io.FileNotFoundException,IOException
	{
		final Map<String,String>	map	= new HashMap<String,String>();
		
		map.putAll( connectInfo.getParams() );
		
		// if we have a passwordFile, read it 
		final String	passwordFile	= (String)map.get( JMXConnectorProvider.PASSWORD_FILE );
		if ( passwordFile != null && map.get( JMXConnectorProvider.PASSWORD ) == null )
		{
			// user is required if we have a password file
			final String	username	= (String)map.get( JMXConnectorProvider.USER );
			
			if ( username == null )
			{
				throw new IllegalArgumentException( "Username required when specifying password file" );
			}
			
			final String	password	= readPassword( passwordFile, username );
			map.put( JMXConnectorProvider.PASSWORD, password );
		}
		
		if ( map.get( JMXConnectorProvider.PROTOCOL ) == null )
		{
			map.put( JMXConnectorProvider.PROTOCOL, "jmxmp" );
		}
		
		return( map );
	}
	
		public JMXConnector
	connect(
		final String		name,
		final ConnectInfo	connectInfo,
		final boolean		forceNew )
		throws Exception
	{
		Connection	conn	= lookup( name );
		
		if ( conn != null )
		{
			if ( ! connectInfo.equals( conn.mConnectInfo ) )
			{
				// same name, but different info, close it and we'll open a new one
				close( name );
				conn	= null;
			}
		}
		
		if ( conn == null || forceNew )
		{
			conn	= openNew( name, connectInfo );
			mNameConnMap.put( name, conn );
		}
		
		return( conn.mConn );
	}
	
	
		public void
	close( String name )
		throws IOException
	{
		final Connection	conn	= lookup( name );
		if ( conn != null )
		{
			mNameConnMap.remove( name  );
			conn.mConn.close();
		}
	}
	
		public Set
	getNames()
	{
		return( mNameConnMap.keySet() );
	}
	
	
	private final class Connection
	{
		public String		mName;
		public JMXConnector	mConn;
		public ConnectInfo	mConnectInfo;	// keep as a String so original can't change
		
			public
		Connection( String name, ConnectInfo connectInfo, JMXConnector conn )
		{
			assert( name != null );
			assert( connectInfo != null );
			assert( conn != null );
			
			mName			= name;
			// clone this; we don't want it changing
			mConnectInfo	= new ConnectInfo( connectInfo );
			mConn			= conn;
		}
		
			public boolean
		equals( Object o )
		{
			if ( o == this )
				return( true );
			if ( ! (o instanceof Connection ) )
			{
				return( false );
			}
			
			final Connection	rhs	= (Connection)o;
			
			return( mName.equals( rhs.mName ) && mConnectInfo.equals( rhs.mConnectInfo ) );
		}
	}
};








