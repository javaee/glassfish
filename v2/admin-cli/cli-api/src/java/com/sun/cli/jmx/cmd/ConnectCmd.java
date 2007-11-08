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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/ConnectCmd.java,v 1.3 2005/12/25 03:45:32 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:32 $
 */
 
package com.sun.cli.jmx.cmd;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import javax.management.MBeanServerConnection;

import com.sun.cli.util.stringifier.ArrayStringifier;
import com.sun.cli.util.StringValuePersister;
import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.util.stringifier.IteratorStringifier;
import com.sun.cli.util.ClassUtil;

import com.sun.cli.jmx.spi.JMXConnectorProvider;

import com.sun.cli.jmx.spi.JMXConnectorProviderInfo;


/*
	Invokes the 'list' commmand on all specified targets
 */
public class ConnectCmd extends JMXCmd
{
		public
	ConnectCmd( final CmdEnv env )
	{
		super( env );
	}
	
		int
	getNumRequiredOperands()
	{
		// require 1, by default
		return( 0 );
	}
	
		static JMXConnectorProviderInfo
	getProviderInfo( final Class theClass )
		throws Exception
	{
		return( JMXConnectorProviderInfo.InfoGetter.getInfo( theClass ) );
	}
	
		public String
	getUsage()
	{
		final String usage	= CmdStrings.CONNECT_HELP.toString();
		
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
	
	
	static private final String	OPTIONS_INFO	=
	"list host,1 port,1 user,1 password-file,1 protocol,1 options,1";
		
		ArgHelper.OptionsInfo
	getOptionInfo()
		throws ArgHelper.IllegalOptionException
	{
		return( new ArgHelperOptionsInfo( OPTIONS_INFO ) );
	}

	
		void
	establishProxy()
		throws Exception
	{
		// defeat default behavior; this is the 'connect' command after all
	}
	
		ConnectInfo
	getDefaultConnectInfo()
		throws com.sun.cli.jmx.support.ArgParserException
	{
		final String	envName	= connectionNameToEnvName( DEFAULT_CONNECTION_NAME );
		
		final String	defaultString	= (String)envGet( envName );
		
		final ConnectInfo connectInfo	= defaultString == null ?
			null : new ConnectInfo( defaultString );
		
		return( connectInfo );
	}
	
	final static String	NAME		= "connect";
	final static String	NAME_ABBREV	= "c";
	
		public static String []
	getNames( )
	{
		return( new String [] { NAME, NAME_ABBREV } );
	}
		
	
		boolean
	isValidConnectionName( String name )
	{
		return( name.indexOf( ':' ) < 0 && name.indexOf( '@' ) < 0 );
	}
	
	
	
		boolean
	isNamedConnection( String name )
	{
		final String key	= connectionNameToEnvName( name );
		
		final String value	= (String)envGet( key );
		
		return( value != null );
	}
	
		void
	handleNamedConnect( String name )
		throws Exception
	{
		// see if it's a name for an existing connection
		final String connectString = (String)envGet( connectionNameToEnvName( name ) );
		if ( connectString != null )
		{
			final ConnectInfo	connectInfo	= new ConnectInfo( connectString );
			
			if ( connectInfo.equals( getConnectionMgr().getConnectInfo( name ) ) )
			{
				println( "Connection already active: " + connectInfo.toString() );
				setProxy( name, connectInfo );
			}
			else
			{
				setProxy( name, connectInfo );
				
				println( "Connection " + name +
					" (" + connectInfo.toString() + ") is now the active connection" );
			}
			envPut( connectionNameToEnvName( DEFAULT_CONNECTION_NAME ), connectInfo.toString(), true);
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
		envRemove( connectionNameToEnvName( name ) );
		envPut( connectionNameToEnvName( name ), connectInfo.toString(), true);
		handleNamedConnect( name );
	}
	
		static void
	maybePut( Map m, String key, Object value )
	{
		if ( value != null )
		{
			m.put( key, value );
		}
	}
	
		void
	listNamedConnections()
		throws Exception
	{
		println( "Named connections:" );
		
		final Iterator	iter	= getEnvKeys( ENV_CONNECT_NAME_BASE + ".+" ).iterator();
		
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
	
	
		void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		
		// at least one argument
		if ( operands.length > 1 )
		{
			throw new IllegalArgumentException( "requires 0 or 1 operands" );
		}
		
		if ( getBoolean( "list", null ) != null )
		{
			listNamedConnections();
			println( "" );
			listActiveConnections();
		}
		else if ( countOptions() == 0 )
		{
			if ( operands.length == 0 )
			{
				// connect to default connection
				final ConnectInfo	connectInfo	= getDefaultConnectInfo();
				if ( connectInfo != null )
				{
					handleNamedConnect( DEFAULT_CONNECTION_NAME );
				}
				else
				{
					printError( "No default connection available.  Please specify a connect string.\n" );
					printUsage();
				}
			}
			else
			{
				// named connection
				handleNamedConnect( operands[ 0 ] );
			}
		}
		else
		{
			final String	host			= getString( "host", "localhost" );
			final String	port			= getString( "port", null );
			final String	protocol		= getString( "protocol", "jmxmp" );
			final String	user			= getString( "user", null );
			final String	passwordFile	= getString( "password-file", null );
			final String	options			= getString( "options", null );
			
			final HashMap	params	= new HashMap();
			maybePut( params, JMXConnectorProvider.HOST, host );
			maybePut( params, JMXConnectorProvider.PORT, port );
			maybePut( params, JMXConnectorProvider.PROTOCOL, protocol );
			maybePut( params, JMXConnectorProvider.USER, user );
			maybePut( params, JMXConnectorProvider.PASSWORD_FILE, passwordFile );
		
		
			final String	connectionName	= (operands.length > 0) ?
									operands[ 0 ] : DEFAULT_CONNECTION_NAME;
			
			handleConnect( connectionName, new ConnectInfo( params ) );
		}
	}
	
}






