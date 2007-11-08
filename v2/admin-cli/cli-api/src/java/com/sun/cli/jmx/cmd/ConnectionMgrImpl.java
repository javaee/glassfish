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
 
/*
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/ConnectionMgrImpl.java,v 1.3 2005/12/25 03:45:33 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:33 $
 */
 

package com.sun.cli.jmx.cmd;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;

import com.sun.cli.jmx.spi.JMXConnectorProvider;
import com.sun.cli.util.LineReaderImpl;

final class Connection
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

public class ConnectionMgrImpl implements ConnectionMgr
{
	private final Map		mNameConnMap;
	final ArrayList			mProviders;
	
		private static void
	dm( Object o )
	{
		System.out.println( o.toString( ) );
	}
	
		public
	ConnectionMgrImpl(  )
		throws IllegalAccessException, InstantiationException, ClassNotFoundException
	{
		// do allow instantiation of this class; probably a singleton,
		// but could be useful to have more than one to segregate by user for example
		
		mNameConnMap	= new HashMap();
		
		mProviders	= new ArrayList();
		addProvider( com.sun.cli.jmx.spi.JMXMPDefaultConnectorProvider.class );
	}
	
		public ConnectInfo
	getConnectInfo( String name )
	{
		final Connection	conn	= (Connection)mNameConnMap.get( name );
		
		return( conn == null ? null : conn.mConnectInfo );
	}
	
		Connection
	lookup( final String	name )
	{
		final Connection	conn	= (Connection)mNameConnMap.get( name );
		
		return( conn );
	}
	
		JMXConnectorProvider
	createProvider( String className )
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		final Class	theClass	= Class.forName( className );
		
		final JMXConnectorProvider	provider = (JMXConnectorProvider)theClass.newInstance( );
		
		return( provider );
	}
	
		public void
	addProvider( Class provider )
		throws IllegalAccessException, InstantiationException, ClassNotFoundException
	{
		final JMXConnectorProvider	instance = (JMXConnectorProvider)provider.newInstance( );
		
		// last added = first priority
		mProviders.add( 0, instance );
	}
	
		public void
	removeProvider( Class provider )
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
	
		Connection
	openNew( String name, ConnectInfo connectInfo )
		throws java.io.IOException, java.net.MalformedURLException, NoProviderFoundException
	{
		final Map	map	= createMapForConnectorProvider( connectInfo );
		
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
	
	
	final char USER_PASSWORD_DELIM	= '=';
	
		String
	readPassword( String filename, String username )
		throws java.io.FileNotFoundException, java.io.IOException
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
	
		Map
	createMapForConnectorProvider( final ConnectInfo connectInfo )
		throws java.io.FileNotFoundException,java.io.IOException
	{
		final HashMap	map	= new HashMap();
		
		map.putAll( connectInfo.mParams );
		
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
	connect( final String name, final ConnectInfo connectInfo )
		throws java.io.IOException, java.net.MalformedURLException, NoProviderFoundException
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
		if ( conn == null )
		{
			conn	= openNew( name, connectInfo );
			mNameConnMap.put( name, conn );
		}
		
		return( conn.mConn );
	}
	
		public void
	close( String name )
		throws java.io.IOException
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
};








