/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/server/TestServer.java,v 1.5 2005/05/19 20:03:54 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2005/05/19 20:03:54 $
 */
 
package com.sun.cli.jmxcmd.test.server;

// java imports
//
import java.util.Arrays;

// RI imports
//
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.jmxmp.JMXMPConnectorServer;

//import com.sun.jdmk.comm.HtmlAdaptorServer;

import com.sun.cli.jmxcmd.support.AliasMgr;
import com.sun.cli.jmxcmd.support.AliasMgrImpl;
import com.sun.cli.jmxcmd.support.AliasMgrMBean;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;

import com.sun.cli.jmxcmd.support.CLISupportStrings;
import com.sun.cli.jmxcmd.support.CLISupport;
import com.sun.cli.jmxcmd.support.AliasMgrHashMapImpl;

import com.sun.cli.jmxcmd.test.mbeans.CLISupportTestee;
import com.sun.cli.jmxcmd.test.mbeans.CLISupportSimpleTestee;
import com.sun.cli.jmxcmd.support.StringifierRegistryIniter;
import com.sun.cli.jmxcmd.support.StandardAliasesIniter;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistryImpl;


import com.sun.cli.jcmd.util.cmd.ArgHelperImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;

//import com.sun.enterprise.jmx.kstat.kstatMgr;

//import com.sun.jdmk.comm.HtmlAdaptorServer;

@org.junit.Ignore
public class TestServer
{
	MBeanServer		mServer;
	final boolean	mTestInProcess;
	
		private MBeanServer
	createAgent(  )
	{
		final MBeanServer server = MBeanServerFactory.createMBeanServer( "Test" );
		
		return( server );
	}
	
	
		private void
	registerConnectors( int connectorPort )
	{
		// create the StandardConnector
		try
		{
			final JMXServiceURL	url	= new JMXServiceURL( "service:jmx:jmxmp://localhost:" + connectorPort );
			final JMXMPConnectorServer	connector	= new JMXMPConnectorServer( url, null, mServer );
			
		    final String name	= ":name=JMXMPConnectorServer,type=connector,port=" + connectorPort;
		    registerMBean( mServer, connector, name);
		    connector.start();
		}
		catch( Exception e )
		{
		    System.out.println("\tCould not create the StandardConnector");
		    e.printStackTrace();
		}
		
	}
	
		private void
	registerAdapters( int adapterPort )
	{
		/*
		// CREATE and START a new HTML adaptor
		final HtmlAdaptorServer html = new HtmlAdaptorServer();
		try
		{
		    final String name = ":name=html,type=adapter,port=" + adapterPort;
		    registerMBean( mServer, html, name);
			html.start();  
		}
		catch(Exception e)
		{
		    System.out.println("\tCould not create the HTML adaptor.");
		    e.printStackTrace();
		}
		*/
	}
	

		private static void
	p( Object arg )
	{
		System.out.println( arg.toString() );
	}


		private void
	registerMBean( MBeanServer conn, Object mbean, String name )
		throws Exception
	{
		conn.registerMBean( mbean, new ObjectName( name ) );
		p( "registered object: " + name );
	}
	
	public interface DottedNameTesterMBean
	{
		public String	getString();
		public void	setString( String value );
		
		public Boolean	getBoolean();
		public void	setBoolean( Boolean value );
	}
	
	private static class DottedNameTester implements DottedNameTesterMBean
	{
		String	mString;
		Boolean	mBoolean;
		Integer	mInteger;
		
			public
		DottedNameTester()
		{
			super();
			
			mString		= "string";
			mBoolean	= Boolean.FALSE;
			mInteger	= new Integer( 0 );
			
		}
		
			public String
		getString()
		{
			return( mString );
		}
		
			public void
		setString( String s)
		{
			mString	= s;
		}
		
		
			public Boolean
		getBoolean()
		{
			return( mBoolean );
		}
		
			public void
		setBoolean( Boolean b)
		{
			mBoolean	= b;
		}
	}
	

	static private final String	DEFAULT_ALIAS_FILE	= "test-server-aliases.txt";
	
		private void
	AddMBeans( MBeanServer conn )
		throws Exception
	{
		// setup alias mgr
		final AliasMgrHashMapImpl	aliasImpl	= new AliasMgrHashMapImpl();
		try
		{
			aliasImpl.load( new java.io.File( DEFAULT_ALIAS_FILE ) );
		}
		catch( Exception e )
		{
			// ignore
		}
		final AliasMgrMBean	aliasMgr	= new AliasMgrImpl( aliasImpl );
			
		StandardAliasesIniter.init( aliasMgr );
		
		StandardMBean	mbean	= new StandardMBean( aliasMgr, AliasMgrMBean.class );
		registerMBean( conn, mbean , CLISupportStrings.ALIAS_MGR_TARGET );
		
		
		// setup CLI support, using alias mgr via a proxy (don't use directly)
		
		final CLISupport	cliSupport		= new CLISupport( conn, aliasMgr);
		
		registerMBean( conn, cliSupport, CLISupportStrings.CLI_SUPPORT_TARGET );
		
		// register our testees
		
		registerMBean( conn, new CLISupportTestee( ), CLISupportStrings.CLI_SUPPORT_TESTEE_TARGET );
		
		registerMBean( conn, new CLISupportSimpleTestee( ), CLISupportStrings.CLI_SIMPLE_TESTEE_TARGET );
		
		//registerMBean( conn, new kstatMgr( ), "kstat:name=kstat-mgr,type=kstat-mgr" );
		
	}
    
		private 
	TestServer( final int port, final boolean testInProcess ) throws Exception
	{
		mTestInProcess	= testInProcess;
		
		new StringifierRegistryIniter( StringifierRegistryImpl.DEFAULT );
		
		mServer	= createAgent(  );
		registerConnectors( port );
		registerAdapters( 8082 );
		
		AddMBeans( mServer );
	}

		public static void
	main(String args[])
	{
		try
		{
			final OptionsInfoImpl		optionInfo	= new OptionsInfoImpl( );
			optionInfo.addOptions( "port:p,1 testInProcess:t" );
			
			final ArgHelperImpl	argHelper	= new ArgHelperImpl( Arrays.asList( args ).listIterator(), optionInfo);
			
			final Integer port	= argHelper.getIntegerValue( "--port", null);
			if ( port == null )
			{
				System.out.println( "USAGE: TestServer --port=<port-number> [--testInProcess]" );
				System.exit( 1 );
			}
			final Boolean	testInProcess	= argHelper.getBooleanValue( "testInProcess", Boolean.FALSE );
			
			final TestServer	server	= new TestServer( port.intValue(), testInProcess.booleanValue() );
			
			if ( testInProcess.booleanValue() )
			{
				final AliasMgrHashMapImpl		aliasMgrImpl	= new AliasMgrHashMapImpl();
				aliasMgrImpl.load( new java.io.File( DEFAULT_ALIAS_FILE ) );
				final AliasMgr					aliasMgr	= new AliasMgrImpl( aliasMgrImpl );
				final CLISupport				cliSupport	= new CLISupport( server.mServer, aliasMgr );
				
				final CLISupportMBeanProxy	proxy	= new CLISupportMBeanProxy( aliasMgr, cliSupport  ) ;
				
				final CLISupportTester	tester	= new CLISupportTester( server.mServer, proxy );
				tester.Run();
			}
			
			p( "Server is running." );
		}
		catch( Exception e )
		{
			p( e );
		}
	}
};


