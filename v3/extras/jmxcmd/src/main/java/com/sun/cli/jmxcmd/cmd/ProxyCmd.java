/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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



