/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/ProxyCmd.java,v 1.17 2004/07/22 22:23:30 llc Exp $
 * $Revision: 1.17 $
 * $Date: 2004/07/22 22:23:30 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import javax.management.*;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdException;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.IllegalUsageException;
import com.sun.cli.jcmd.framework.ClassSource;
import com.sun.cli.jcmd.framework.ClassSourceFromStrings;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.misc.StringifiedList;
import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

import com.sun.cli.jmxcmd.support.MBeanProxyMgrImpl;
import com.sun.cli.jmxcmd.support.MBeanProxyMgr;
import com.sun.cli.jmxcmd.support.AliasMgr;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import com.sun.cli.jmxcmd.util.ConnectionSource;

	
/**
	Create proxies for MBeans into in-process MBeanServers
 */
public class ProxyCmd extends JMXCmd
{
	public final static String	CREATE_PROXIES_CMD	= "create-proxies";
	
		public
	ProxyCmd( final CmdEnv env )
	{
		super( env );
	}
	
	static final class ProxyCmdHelp extends CmdHelpImpl
	{
		public	ProxyCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS	= "create and manage mbean proxies";
			
		private final static String	HELP_TEXT		=
	"Create proxies for MBeans into a local MBeanServer.\n\n" +
	"Example of proxying all MBeans from the server 'MyServer' into the local MBeanServer 'mbs1':\n" +
	"    start-mbs mbs1\n" +
	"    connect MyServer\n" +
	"    create-proxies " + LOCAL_SERVER_OPTION.toString() + " " +
				NEW_NAMES_OPTION.toString() + " " + CACHE_MBEAN_INFO_OPTION.toString() + " " +
				CACHE_ATTRIBUTES_OPTION.toString() + " " + TARGETS_OPERAND_INFO + "\n" +
	"";

		
		public String	getSynopsis()	{	return( formSynopsis(SYNOPSIS ) ); }
		public String	getText()		{	return( HELP_TEXT ); }
	}
	
		public CmdHelp
	getHelp()
	{
		return( new ProxyCmdHelp() );
	}

	private final static OptionInfo VERBOSE_OPTION		= createVerboseOption();
	private final static OptionInfo LOCAL_SERVER_OPTION	=
					new OptionInfoImpl( "mbean-server", "m", "name", true );
	private final static OptionInfo NEW_NAMES_OPTION	= new OptionInfoImpl( "new-names", "n" );
	private final static OptionInfo CACHE_MBEAN_INFO_OPTION	=
					new OptionInfoImpl( "mbeaninfo-refresh-millis", "i", "millis");
	private final static OptionInfo CACHE_ATTRIBUTES_OPTION	=
					new OptionInfoImpl( "attribute-refresh-millis", "a", "millis");
	
	static private final OptionInfo[]	OPTIONS_INFO	=
	{
		LOCAL_SERVER_OPTION,
		VERBOSE_OPTION,
		NEW_NAMES_OPTION, 
		CACHE_MBEAN_INFO_OPTION,
		CACHE_ATTRIBUTES_OPTION,
	};
	
	private final static CmdInfo	PROXY_INFO	=
		new CmdInfoImpl( CREATE_PROXIES_CMD,
				new OptionsInfoImpl( OPTIONS_INFO ),
				TARGETS_OPERAND_INFO );
		
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( PROXY_INFO ) );
	}

	private static final String	JMXCMD_DOMAIN	= "jmxcmd";
	
		private ObjectName
	getProxyMgrObjectName( final String serverID )
	{
		return( JMXUtil.newObjectName( JMXCMD_DOMAIN, "src-mbean-server=" + serverID ) );
	}
	
		private MBeanProxyMgr
	getProxyMgr(
		final MBeanServer		server,
		final ConnectionSource	proxiedServerConnectionSource )
		throws Exception
	{
		final MBeanServerConnection	conn	=
			proxiedServerConnectionSource.getMBeanServerConnection( false );
		final String		srcServerID	= JMXUtil.getMBeanServerID( conn  );
		final ObjectName	mgrName	= getProxyMgrObjectName( srcServerID );
		
		if ( ! server.isRegistered( mgrName )  )
		{
			final MBeanProxyMgrImpl		mgr	= new MBeanProxyMgrImpl( proxiedServerConnectionSource );
			server.registerMBean( mgr, mgrName );
		}
		
		final MBeanProxyMgr mgr	= (MBeanProxyMgr)
				MBeanServerInvocationHandler.newProxyInstance( server, mgrName, MBeanProxyMgr.class, false );
		
		return( mgr );
	}

		private boolean
	isIgnoreProxy( final ObjectName name )
	{
		boolean	ignore	= false;
		
		if ( name.getDomain().equals( "JMImplementation" ) )
		{
			// don't/can't proxy the MBeanServerDelegate MBean
			ignore	= true;
		}
		
		return( ignore );
	}
	
	
		private Set<String>
	resolveAliases( final String[]	targets )
	{
		final Set<String>	results	= new HashSet<String>();
		
		final AliasMgr	mgr	= getAliasMgr();
		
		for( int i = 0; i < targets.length; ++i )
		{
			final String	aliasValue	= mgr.getAliasValue( targets[ i ] );
			
			if ( aliasValue == null )
			{
				// not an alias
				results.add( targets[ i ] );
			}
			else
			{
				final String[]	components	= TargetAliasesCmd.aliasValueToComponents( aliasValue );
				results.addAll( resolveAliases( components ) );
			}
		}
		
		return( results );
	}
	
		private Set<ObjectName>
	stringsToObjectNames( final Set<String>	exprs )
		throws MalformedObjectNameException
	{
		final Set<ObjectName>	results	= new HashSet<ObjectName>();
		
		final Iterator<String>	iter	= exprs.iterator();
		while ( iter.hasNext() )
		{
			final String	orig	= iter.next();
			
			String	expr	= orig;
			
			if ( expr.indexOf( ":" ) < 0 )
			{
				// no domain, add "*:" );
				expr	= "*:" + orig;
			}
			
			// force everything to a pattern; alternative is to force
			// user to always add ",*".
			if ( ! ( expr.endsWith( JMXUtil.WILD_PROP ) || expr.endsWith( JMXUtil.WILD_ALL ) ) )
			{
				expr	= expr + JMXUtil.WILD_PROP;
			}
			
			final ObjectName	objectName	= new ObjectName( expr );
			
			results.add( objectName );
		}
		
		return( results );
	}
	
		private void
	addProxies(
		final String	destServerName,
		final String[]	targets,
		boolean			newNames,
		int				mbeanInfoCacheRefreshMillis,
		int				attributeCacheRefreshMillis )
		throws Exception
	{
		final String		cmd	= getSubCmdNameAsInvoked();
		final MBeanServer	server	= findMBeanServer( destServerName );
		
		if ( server == null )
		{
			throw new CmdException( cmd, "local MBeanServer " +
				quote( destServerName ) + " does not exist." );
		}
		
		// make sure the connection exists to the MBeanServer in which the Proxies will reside
		getConnectionMgr().connect( destServerName, getConnectInfo( destServerName ), false );
		
		final String	srcConnectionName	= getDefaultConnectionName();
		final ConnectionSource	connectionSource	= getConnectionSource( srcConnectionName );
		final MBeanProxyMgr	proxyMgr	= getProxyMgr( server, connectionSource );
		
		final Set<String>	resolvedStrings	= resolveAliases( targets );
		final Set<ObjectName>	objectNames		= stringsToObjectNames( resolvedStrings );
		
		final ObjectName[]	objectNamesArray	= new ObjectName[ objectNames.size() ];
		objectNames.toArray( objectNamesArray );
		
		proxyMgr.addProxies( objectNamesArray, newNames,
			mbeanInfoCacheRefreshMillis, attributeCacheRefreshMillis);
		
		println( "Adding proxies based on these patterns: " + SmartStringifier.toString( objectNames ) );
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd		= getSubCmdNameAsInvoked();
		final String [] targets	= getTargets();
		
		final boolean	newNames	=
			getBoolean( NEW_NAMES_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
			
		final int	cacheRefreshInterval	=
			getInteger( CACHE_MBEAN_INFO_OPTION.getShortName(), new Integer(0) ).intValue();
			
		final int	cacheAttributesInterval	=
			getInteger( CACHE_ATTRIBUTES_OPTION.getShortName(), new Integer(0) ).intValue();
			
		if ( cmd.equals( CREATE_PROXIES_CMD ) )
		{
			final String	mbeanServerName	= getString( LOCAL_SERVER_OPTION.getShortName(), null );
			addProxies( mbeanServerName, targets,
				newNames, cacheRefreshInterval, cacheAttributesInterval);
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
	}
}



