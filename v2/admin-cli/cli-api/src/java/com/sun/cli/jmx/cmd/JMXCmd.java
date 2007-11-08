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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/JMXCmd.java,v 1.3 2005/12/25 03:45:38 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:38 $
 */
 

package com.sun.cli.jmx.cmd;

import java.net.MalformedURLException;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.MBeanServerConnection;

import com.sun.cli.jmx.support.AliasMgr;
import com.sun.cli.jmx.support.AliasMgrHashMapImpl;
import com.sun.cli.jmx.support.AliasMgrMBean;
import com.sun.cli.jmx.support.StandardAliasesIniter;
import com.sun.cli.jmx.support.CLISupport;
import com.sun.cli.jmx.support.CLISupportMBean;

import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.util.stringifier.SmartStringifier;
import com.sun.cli.util.stringifier.Stringifier;
import com.sun.cli.util.stringifier.ArrayStringifier;

import com.sun.cli.jmx.spi.JMXConnectorProvider;

public abstract class JMXCmd extends CmdBase
{
	final String []	ALL_TARGET	= new String [] { "all" };
	
	public final static String	ENV_PROXY	= "PROXY";
	public final static String	ENV_CONNECTION	= "CONNECTION";
	public final static String	ENV_TARGET	= "TARGET";
	public final static String	ENV_CONNECT_NAME_BASE	= "CONNECT_";
	public final static String	ENV_ALIAS_MGR	= "ALIAS_MGR";
	public final static String	ENV_PROVIDERS	= "PROVIDERS";
	
	final static String		DEFAULT_CONNECTION_NAME = "DEFAULT";
	
	private static ConnectionMgrImpl	sConnectionMgr	= null;
	private static AliasMgrMBean		sAliasMgr		= createAliasMgr();
	
	
	JMXCmd( final CmdEnv env )
	{
		super( env );
		
		env.put( ENV_ALIAS_MGR, sAliasMgr, false);
		
		// force initialization
	}
	
	
		ConnectionMgr
	getConnectionMgr(  )
		throws Exception
	{
		initConnectionMgr();
		return( sConnectionMgr );
	}
	
		void
	initConnectionMgr(  )
		throws IllegalAccessException, InstantiationException, ClassNotFoundException
	{
		if ( sConnectionMgr != null )
			return;
			
		sConnectionMgr	= new ConnectionMgrImpl( );
		
		final String	providersString	= ((String)envGet( ENV_PROVIDERS, "")).trim();
		
		if ( providersString != null && providersString.length() != 0 )
		{
			final String []	providers	= providersString.split( "," );
			
			for( int i = 0; i < providers.length; ++i )
			{
				try
				{
					final Class	theClass	= Class.forName( providers[ i ] );
				
					sConnectionMgr.addProvider( theClass );
					println( "Added JMXConnectorProvider: " + providers[ i ] );
				}
				catch( Exception e )
				{
					printError( "WARNING: Can't add JMXConnectorProvider: " + providers[ i ] );
				}
			}
		}
	}
	
	
		static AliasMgrMBean
	createAliasMgr()
	{
		final AliasMgrHashMapImpl	aliasMgrImpl	= new AliasMgrHashMapImpl();
		final AliasMgr				aliasMgr		= new AliasMgr( aliasMgrImpl );
		
		
		try
		{
			aliasMgrImpl.load( JMXAdminFileNames.getAliasesFile() );
			StandardAliasesIniter.init( aliasMgr );
		}
		catch( Exception e )
		{
			StandardAliasesIniter.init( aliasMgr );
			System.err.println( "can't load aliases file: " + e.getMessage());
			
		}
		return( aliasMgr );
	}
	
	
		private CLISupportMBean
	getCLISupport( MBeanServerConnection conn )
		throws Exception
	{
		final CLISupportMBean		cliSupport		= new CLISupport( conn, sAliasMgr);
		
		return( cliSupport );
	}
	
		String
	connectionNameToEnvName( String connectionName )
	{
		return( ENV_CONNECT_NAME_BASE + connectionName );
	}
	
		String
	envNameToConnectionName( String envName )
	{
		return( envName.substring( ENV_CONNECT_NAME_BASE.length(), envName.length() ) );
	}
	
		CLISupportMBeanProxy
	setProxy( String name, ConnectInfo connectInfo )
		throws Exception
	{
		JMXConnector	jmxConnector	= null;
		
		try
		{
			jmxConnector	= getConnectionMgr().connect( name, connectInfo );
		}
		catch( java.io.IOException e )
		{
			throw new Exception( e.getMessage() + " (" + connectInfo.toString() + ")", e);
		}
		
		final MBeanServerConnection	managedServer	= jmxConnector.getMBeanServerConnection();

		final CLISupportMBean		cliSupport		= getCLISupport( managedServer );
			
		final CLISupportMBeanProxy	proxy	= new CLISupportMBeanProxy( sAliasMgr, cliSupport );
			
		envPut( connectionNameToEnvName( name ), connectInfo.toString(), true );
		envPut( ENV_PROXY, proxy, false );
		envPut( ENV_CONNECTION, managedServer, false );
		
		return( proxy );
	}
	

		void
	clearProxy(  )
	{
		envRemove( ENV_PROXY );
	}

		CLISupportMBeanProxy
	establishProxy( String name, ConnectInfo connectInfo  )
		throws Exception
	{
		CLISupportMBeanProxy	proxy	= null;
		
		proxy	= setProxy( name, connectInfo );
		
		final String host		= connectInfo.getParam( JMXConnectorProvider.HOST );
		final String port		= connectInfo.getParam( JMXConnectorProvider.PORT );
		String protocol			= connectInfo.getParam( JMXConnectorProvider.PROTOCOL );
		if ( protocol == null )
		{
			protocol 	= "jmxmp";
		}
		
		println( "Established connection to " +
			host + ":" + port + " using " + protocol );

		return( proxy );
	}
	
	
		CLISupportMBeanProxy
	establishProxy( ConnectInfo connectInfo )
		throws Exception
	{
		final String	envName	= connectionNameToEnvName( DEFAULT_CONNECTION_NAME );
		
		final CLISupportMBeanProxy	proxy	= establishProxy( envName , connectInfo );
		
		return( proxy );
	}
	
		void
	establishProxy(  )
		throws Exception
	{
		if ( envGet( ENV_PROXY ) == null )
		{
			final String	envName	= connectionNameToEnvName( DEFAULT_CONNECTION_NAME );
		
			final String connectString	= (String)envGet( envName );
			if ( connectString != null )
			{
				establishProxy( new ConnectInfo( connectString ) );
			}
		}
		
		if ( envGet( ENV_PROXY ) == null )
		{
			throw new java.io.IOException( "can't connect" );
		}
	}
	
		CLISupportMBeanProxy
	getProxy()
	{
		final CLISupportMBeanProxy	proxy	= (CLISupportMBeanProxy)envGet( ENV_PROXY );
		
		return( proxy );
	}
	
		MBeanServerConnection
	getConnection()
	{
		final MBeanServerConnection	conn	= (MBeanServerConnection)envGet( ENV_CONNECTION );
		
		return( conn );
	}
	
		AliasMgrMBean
	getAliasMgr()
	{
		final AliasMgrMBean	proxy	= (AliasMgrMBean)envGet( ENV_ALIAS_MGR );
		
		return( proxy );
	}
	
		void
	preExecute()
		throws Exception
	{
		getConnectionMgr();
		super.preExecute();
	}
	
		void
	handleException( final Exception e )
	{
		super.handleException( e );
	
		if ( e instanceof java.io.IOException )
		{
			clearProxy();
		}
	}
	
	
		String []
	getEnvTargets()
	{
		final String	targetsEnv	= (String)envGet( ENV_TARGET );
		if ( targetsEnv == null )
			return( null );
		
		// convert to String []
		final String []	targets	= (String [])
							TargetPersister.DEFAULT.asObject( targetsEnv );
		
		return( targets );
	}
	
		void
	putEnvTargets( String [] targets )
	{
		// put targets into environment
		final String	targetsStr	= TargetPersister.DEFAULT.asString( targets );
		envPut( ENV_TARGET, targetsStr, true);
	}
	
		String []
	getTargets()
	{
		String [] 	targets	= getOperands();
		
		if ( targets.length == 0 )
		{
			targets	= getEnvTargets();
		}
		
		return( targets );
	}
	
	
	final static class TargetPersister implements com.sun.cli.util.ValuePersister
	{
		final static TargetPersister	DEFAULT	= new TargetPersister();
		final static String				TARGET_DELIM	= " ";
		
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
};


