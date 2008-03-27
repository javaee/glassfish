/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/cmd/com/sun/enterprise/s1as8/cmd/S1AS8Cmd.java,v 1.24 2004/09/28 17:22:12 llc Exp $
 * $Revision: 1.24 $
 * $Date: 2004/09/28 17:22:12 $
 */
 
package com.sun.enterprise.s1as8.cmd;

import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.io.IOException;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanException;
import javax.management.JMException;
import javax.management.InvalidAttributeValueException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MBeanServerConnection;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.IllegalUsageException;


import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.framework.CmdException;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.DisallowedCmdDependency;
import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;
import com.sun.cli.jcmd.util.stringifier.ArrayStringifier;
import com.sun.cli.jcmd.util.stringifier.IteratorStringifier;
import com.sun.cli.jcmd.util.misc.ArrayConversion;
import com.sun.cli.jcmd.util.misc.ExceptionUtil;
import com.sun.cli.jcmd.util.misc.CompareUtil;
import com.sun.cli.jmxcmd.util.jmx.JMXUtil;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import com.sun.cli.jmxcmd.cmd.JMXCmd;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;


import com.sun.enterprise.s1as8.mbeans.AllDottedNames;



/**
	This command supports access to the Sun Java System Appserver dotted-name
	'get', 'set' and 'list' facilities.
 */
public class S1AS8Cmd extends JMXCmd
{
	static final class S1AS8CmdHelp extends CmdHelpImpl
	{
		public	S1AS8CmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS	= "support for Sun Java System Appserver 8 PE";
			
		private final static String	TEXT		=
			"Get/set/list one or more dotted names from a Sun Java System Appserver. " +
			"Wildcards are supported to the extent that SJSA supports them.  See the SJSA for more information.\n\n" +
			"SJSA uses 'lazy loading' such that MBeans may not be loaded until getMBeanInfo() is called with " +
			"the MBean's fully-qualified ObjectName.  The s1as8-load command provides a way to force an MBean to load.\n\n" +
			"";

		
		public String	getName()		{	return( NAME ); }
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TEXT ); }
	}
	
		public
	S1AS8Cmd( final CmdEnv env )
	{
		super( env );
	}
	
	final static String	NAME					= "s1as8-cmd";
	final static String	GET_NAME				= "s1as8-get";
	final static String	SET_NAME				= "s1as8-set";
	final static String	LIST_NAME				= "s1as8-list";
	final static String	LOAD_NAME				= "s1as8-load";
	final static String	LOAD_DOTTED_NAMES_NAME	= "s1as8-load-dotted-names";
	final static String	SET_MONITORING_NAME		= "s1as8-set-monitoring";

		public static String []
	getNames( )
	{
		return( new String [] { NAME, GET_NAME, SET_NAME,
			LIST_NAME, LOAD_NAME, LOAD_DOTTED_NAMES_NAME, SET_MONITORING_NAME, } );
	}
	
		public CmdHelp
	getHelp()
	{
		return( new S1AS8CmdHelp() );
	}
	
	private final static OptionInfo MONITOR_OPTION	= new OptionInfoImpl( "monitor", "m" );
	private final static OptionInfo ERROR_IF_TOO_FEW_OPTION	= new OptionInfoImpl( "error-if-too-few", "e", "min-results");
	
	private static final OptionInfo[]	GET_OPTIONS_INFO_ARRAY	=
	{
		MONITOR_OPTION,
		ERROR_IF_TOO_FEW_OPTION,
	};
	
	private static final OptionInfo[]	LIST_OPTIONS_INFO_ARRAY	=
	{
		MONITOR_OPTION,
	};

	
	private final static CmdInfo	GET_INFO	=
		new CmdInfoImpl( GET_NAME,
				new OptionsInfoImpl( GET_OPTIONS_INFO_ARRAY ),
				new OperandsInfoImpl( "<dotted-name>[ <dotted-name>]", 1) );
				
	private final static CmdInfo	SET_INFO	=
		new CmdInfoImpl( SET_NAME,
				new OperandsInfoImpl( "<dotted-name=<value>>[ <dotted-name=<value>]*", 1) );
				
	private final static CmdInfo	LIST_INFO	=
		new CmdInfoImpl( LIST_NAME,
				new OptionsInfoImpl( LIST_OPTIONS_INFO_ARRAY ),
				new OperandsInfoImpl( "<dotted-name>[ <dotted-name>]*", 1) );
				
	private final static CmdInfo	LOAD_INFO	=
		new CmdInfoImpl( LOAD_NAME,
				new OperandsInfoImpl( "<object-name>", 1,1) );
				
	private final static CmdInfo	LOAD_DOTTED_NAMES_INFO	=
		new CmdInfoImpl( LOAD_DOTTED_NAMES_NAME,
				new OperandsInfoImpl( "<connection-name>", 1,1) );
				
	private final static CmdInfo	SET_MONITORING_INFO	=
		new CmdInfoImpl( SET_MONITORING_NAME,
				new OperandsInfoImpl( "[OFF | LOW | HIGH]", 1,1) );
		
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( new CmdInfo[]
			{
			GET_INFO,
			SET_INFO,
			LIST_INFO,
			LOAD_INFO,
			LOAD_DOTTED_NAMES_INFO,
			SET_MONITORING_INFO
			})
		);
	}
	
	
	
	private static final String		GET_SET_MBEAN_NAME			= "com.sun.appserv:name=dotted-name-get-set,type=dotted-name-support";
	private static ObjectName		GET_SET_MBEAN_OBJECTNAME	= initGetSetObjectName();
	
	
		private final static ObjectName
	initGetSetObjectName()
	{
		try
		{
			return( new ObjectName( GET_SET_MBEAN_NAME ) );
		}
		catch( MalformedObjectNameException e )
		{
			e.printStackTrace();
			assert( false );	// can't happen
		}
		
		return( null );
	}
	
	
		DottedNameProxy
	getDottedNameProxy()
		throws IOException
	{
	/*
		try
		{
			getConnection().getMBeanInfo( GET_SET_MBEAN_OBJECTNAME );
		} catch( Exception e )
		{
			e.printStackTrace();
		}
		*/
		
	printDebug( "getting proxy to: " + GET_SET_MBEAN_OBJECTNAME );
		return( (DottedNameProxy)MBeanServerInvocationHandler.newProxyInstance(
			getConnection(), GET_SET_MBEAN_OBJECTNAME, DottedNameProxy.class, false ) );
	}
	
		void
	verifyMinResultsForGet( final String expr, final int minNumberOfResults, final Object result )
		throws CmdException
	{
		// check that each results is an Attribute[]
		if ( result instanceof Attribute[] )
		{
			final Attribute[]	attrs	= (Attribute[])result;
			if ( attrs.length < minNumberOfResults )
			{
				throw new CmdException( getSubCmdNameAsInvoked(),
					"caller specifed that at least " + minNumberOfResults +
					" are required for expression " + quote( expr ));
			}
		}
		else
		{
			throw new CmdException( getSubCmdNameAsInvoked(),
				"caller specifed that at least " + minNumberOfResults +
				" are required, but the resulting value was of class " + result.getClass().getName() );
		}
	}
	
		private Object[]
	handleGet( final String[]	operands, boolean monitor )
		throws IllegalOptionException, CmdException, IOException
	{
		final DottedNameProxy	proxy	= getDottedNameProxy();
		
		// one result for each input operand
		final Object[]	results	= monitor ?
			proxy.dottedNameMonitoringGet( operands ) : proxy.dottedNameGet( operands );
		
		final int	minNumberOfResults	=
			getInteger( ERROR_IF_TOO_FEW_OPTION.getShortName(), new Integer( 0 )).intValue();
		
		if ( minNumberOfResults >= 1 )
		{
			for( int i = 0; i < results.length; ++i )
			{
				verifyMinResultsForGet( operands[ i ], minNumberOfResults, results[ i ] );
			}
		}
		
		return( results );
	}
	
		private Object[]
	handleSet( final String[]	operands )
		throws IOException
	{
		final DottedNameProxy	proxy	= getDottedNameProxy();
		
		final Object[]	results	= proxy.dottedNameSet( operands );
		
		return( results );
		
	}
	
	
	

	
		private Object[]
	handleList( final String[]	operands, boolean monitor )
		throws IOException
	{
		final DottedNameProxy	proxy	= getDottedNameProxy();
		
		final Object[]	results	= monitor ?
			proxy.dottedNameMonitoringList( operands ) : proxy.dottedNameList( operands );
		
		return( results );
	}
	
		private ObjectName[]
	handleLoad( final String[]	objectNames )
		throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException,
			ReflectionException, java.io.IOException, CmdException
	{
		final MBeanServerConnection	conn	= getConnection();
		
		final ArrayList		results	= new ArrayList();
		for( int i = 0; i < objectNames.length; ++i )
		{
			final ObjectName	objectName	= new ObjectName( objectNames[ i ] );
			final MBeanInfo	info	= conn.getMBeanInfo( objectName );
			if ( info == null )
			{
				throw new CmdException( LOAD_NAME, "MBeanInfo is null for object: " + objectName.toString() );
			}
			
			results.add( objectName );
		}
		
		return( (ObjectName[])results.toArray( new ObjectName[ results.size() ] ) );
	}
		
		private void
	loadDottedNamesMBean( final String	inProcessMBeanServerName )
		throws CmdException, MalformedObjectNameException,
			InstanceAlreadyExistsException, NotCompliantMBeanException, MBeanRegistrationException,
			IOException
	{
		final String		baseName	= "proxy:name=dotted-names-proxy,category=";
		final ObjectName	configName	= new ObjectName( baseName + "config" );
		final ObjectName	monitorName	= new ObjectName( baseName + "monitor" );
		
		final MBeanServer	server	= findMBeanServer( inProcessMBeanServerName );
		if ( server == null )
		{
			throw new CmdException( getSubCmdNameAsInvoked(), "MBeanServer " + inProcessMBeanServerName + " not found." );
		}
		
		final AllDottedNames	configMBean	= new AllDottedNames( getConnection(), false );
		server.registerMBean( configMBean, configName );
		println( "Registered mbean: " + configName );
		
		/*
		final AllDottedNames	monitorMBean	= new AllDottedNames( getConnection(), true );
		server.registerMBean( monitorMBean, monitorName );
		println( "Registered mbean: " + monitorName );
		*/
	}
	
		private void
	displayLoaded( final ObjectName[]	objectNames )
	{
		println( "Successfully performed getMBeanInfo() on:" );
		for( int i = 0; i < objectNames.length; ++i )
		{
			println( objectNames[ i ].toString() );
		}
	}
	
	private static final String	MONITORING_MBEAN_NAME	=
		"amx:j2eeType=X-ModuleMonitoringLevelsConfig,name=na,X-ConfigConfig=server-config,X-MonitoringServiceConfig=na";
	
	
	private static final Set	 LEGAL_MONITORING_LEVELS_SET;
	private static final String[] LEGAL_MONITORING_LEVELS =
	{
		"OFF", "LOW", "HIGH"
	};
	static
	{
		LEGAL_MONITORING_LEVELS_SET	= ArrayConversion.arrayToSet( LEGAL_MONITORING_LEVELS );
	}
	
		private void
	setMonitoringLevel( String level )
		throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException,
			ReflectionException, MBeanException, InvalidAttributeValueException,
			AttributeNotFoundException,
			java.io.IOException, CmdException
	{
		if ( ! LEGAL_MONITORING_LEVELS_SET.contains( level ) )
		{
			throw new CmdException( getSubCmdNameAsInvoked(), "Illegal monitoring level: " + level );
		}

		final ObjectName			objectName	= new ObjectName( MONITORING_MBEAN_NAME );
		final MBeanServerConnection	conn	= getConnection();
		
		handleLoad( new String[ ] { MONITORING_MBEAN_NAME });
		println( "Loaded monitoring MBean: " + MONITORING_MBEAN_NAME );
		
		final Map	levelsMap	= (Map)conn.getAttribute( objectName, "AllLevels" );
		
		final Iterator	iter	= levelsMap.keySet().iterator();
		while ( iter.hasNext() )
		{
			final String	module	= (String)iter.next();
			
			conn.setAttribute( objectName, new Attribute( module, level ) );
			println( "Set monitoring level for " + module +
				" from " + levelsMap.get( module ) + " to " + level );
		}
	}
	
		private void
	displayResults( final Object[]	results )
	{
		if ( results.length == 0 )
		{
			println( "No results returned." );
		}
		else
		{
			final Stringifier	stringifier	= new SmartStringifier( "\n", false );
			
			for( int i = 0; i < results.length; ++i )
			{
				println( stringifier.stringify( results[ i ] ) );
					
				if ( getDebug() && results[ i ] instanceof Exception )
				{
					final Exception e	= (Exception)results[ i ];
					printDebug( ExceptionUtil.getStackTrace( e ) );
				}
			}
		}
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		final String	cmd			= getSubCmdNameAsInvoked();
		
		establishProxy();
		
		Object[]	results	= null;
		
		if ( cmd.equals( GET_NAME ) )
		{
			final boolean	monitor	= getBoolean( MONITOR_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
		
			requireNumOperands( 1 );
			results	= handleGet( operands, monitor );
		}
		else if ( cmd.equals( SET_NAME )  )
		{
			requireNumOperands( 1 );
			results	= handleSet( operands);
		}
		else if ( cmd.equals( LIST_NAME ))
		{
			final boolean	monitor	= getBoolean( MONITOR_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
			
			requireNumOperands( 1 );
			results	= handleList( operands, monitor );
		}
		else if ( cmd.equals( LOAD_NAME ) )
		{
			requireNumOperands( 1 );
			final ObjectName[]	loaded	= handleLoad( operands );
			
			displayLoaded( loaded );
		}
		else if ( cmd.equals( LOAD_DOTTED_NAMES_NAME ) )
		{
			requireNumOperands( 1, "requires name of in-process MBeanServer" );
			loadDottedNamesMBean( operands[ 0 ] );
		}
		else if ( cmd.equals( SET_MONITORING_NAME ) )
		{
			final String level	= operands.length == 0 ? "HIGH" : operands[ 0 ];
			
			setMonitoringLevel( level );
			results	= null;
		}
		else
		{
			throw new IllegalUsageException( "Unknown command: " + cmd );
		}
		
		if ( results != null )
		{
			displayResults( results );
		}
	}
}






