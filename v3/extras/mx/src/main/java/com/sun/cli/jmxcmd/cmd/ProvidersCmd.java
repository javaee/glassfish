/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/ProvidersCmd.java,v 1.5 2004/02/06 02:11:23 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2004/02/06 02:11:23 $
 */
 
package com.sun.cli.jmxcmd.cmd;


import org.glassfish.admin.amx.util.stringifier.IteratorStringifier;

import com.sun.cli.jmxcmd.spi.JMXConnectorProvider;
import com.sun.cli.jcmd.util.misc.StringifiedList;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.IllegalUsageException;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;
import org.glassfish.admin.amx.util.ClassUtil;


/**
	Manages protocol providers.
 */
public class ProvidersCmd extends JMXCmd
{
		public
	ProvidersCmd( final CmdEnv env )
	{
		super( env );
	}
	

	static final class ProvidersCmdHelp extends CmdHelpImpl
	{
			public
		ProvidersCmdHelp(  )
		{
			super( getCmdInfos() );
		}
		
		static final String	SYNOPSIS	= "manage JMXConnectorProviders";
			
		static final String	PROVIDERS_TEXT		=
		SHOW_PROVIDERS_NAME + " -- show the configured providers\n" +
		ADD_PROVIDER_NAME + " -- add the JMXConnectorProvider with the specified classname\n" +
		REMOVE_PROVIDER_NAME + " -- remove the JMXConnectorProvider with the specified classname\n" +
		"";

			public String
		getName()
		{
			return( PROVIDERS_NAME );
		}
		
			public String[]
		getNames()
		{
			return( ProvidersCmd.getCmdInfos().getNames() );
		}
	
			public String
		getSynopsis()
		{
			return( formSynopsis( SYNOPSIS ) );
		}
		
			public String
		getText()
		{
			return( PROVIDERS_TEXT );
		}
	}



		public CmdHelp
	getHelp()
	{
		return( new ProvidersCmdHelp( ) );
	}
	
	
	static final String	PROVIDERS_NAME			= "providers";
	static final String	SHOW_PROVIDERS_NAME		= "show-providers";
	static final String	ADD_PROVIDER_NAME		= "add-provider";
	static final String	REMOVE_PROVIDER_NAME	= "remove-provider";
	
	private static final OperandsInfo	CLASSNAME_OPERAND	=
		new OperandsInfoImpl( "<classname>", 1, 1);
	
	private final static CmdInfo	SHOW_PROVIDERS_INFO	=
		new CmdInfoImpl( SHOW_PROVIDERS_NAME );
	
	private final static CmdInfo	ADD_PROVIDER_INFO	=
		new CmdInfoImpl( ADD_PROVIDER_NAME, CLASSNAME_OPERAND);
		
	private final static CmdInfo	REMOVE_PROVIDER_INFO	=
		new CmdInfoImpl( REMOVE_PROVIDER_NAME, CLASSNAME_OPERAND);

		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( SHOW_PROVIDERS_INFO, ADD_PROVIDER_INFO, REMOVE_PROVIDER_INFO) );
	}



		StringifiedList
	createProviderList()
	{
		return( new StringifiedList( (String)envGet( JMXCmdEnvKeys.PROVIDERS ) ) );
	}
	
	
		void
	addProvider(  )
		throws Exception
	{
		requireNumOperands( 1, "A provider classname must be specified");
		
		final String providerClassname	= getOperands()[ 0 ];
		
		final StringifiedList	list	= createProviderList();
		
		if ( list.exists( providerClassname ) )
		{
			println( "Provider already in list: " + providerClassname );
		}
		else
		{
			// if this succeeds, then we'll update the env
			try
			{
                final Class<?> clazz = ClassUtil.getClassFromName( providerClassname );
                @SuppressWarnings("unchecked")
                final Class<JMXConnectorProvider> c = (Class<JMXConnectorProvider>)clazz;
				getConnectionMgr().addProvider( c );
			}
			catch( Exception e )
			{
				printError( "WARNING: unable to instantiate provider (added to list anyway): " +
					e.getMessage() );
			}
			
			list.append( providerClassname );
			
			envPut( JMXCmdEnvKeys.PROVIDERS, list.toString(), true );
		}
	}
	
		void
	removeProvider(  )
		throws Exception
	{
		requireNumOperands( 1, "A provider classname must be specified");
		
		final String providerClassname	= getOperands()[ 0 ];
		
		final StringifiedList	list	= createProviderList();
		
		if ( ! list.exists( providerClassname ) )
		{
			println( "Provider not found: " + providerClassname );
		}
		else
		{
			// if this succeeds, then we'll update the env
			try
			{
                final Class<?> clazz = ClassUtil.getClassFromName( providerClassname );
                @SuppressWarnings("unchecked")
                final Class<JMXConnectorProvider> c = (Class<JMXConnectorProvider>)clazz;
				getConnectionMgr().removeProvider( c );
			}
			catch( Exception e )
			{
				// ignore
			}
			
			list.remove( providerClassname );
			
			envPut( JMXCmdEnvKeys.PROVIDERS, list.toString(), true );
		}
	}
	
		void
	displayProviders(  )
		throws Exception
	{
		println( "Configured (non-built-in) providers: " );
		final String	list	= IteratorStringifier.stringify( createProviderList().iterator(), "\n" );
		
		println( (list.length() == 0) ? "<none>" : list );
		
		println( "\nProviders successfully loaded (includes built-ins): " );
		final JMXConnectorProvider []	providersLoaded	= getConnectionMgr().getProviders();
		for ( int i = 0; i < providersLoaded.length; ++i )
		{
			JMXConnectorProvider provider	= providersLoaded[ i ];
			
			println( provider.getClass().getName() );
		}
	}
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmdName		= getSubCmdNameAsInvoked();
		
		if ( cmdName.equals( ADD_PROVIDER_NAME ) )
		{
			addProvider();
		}
		else if ( cmdName.equals( REMOVE_PROVIDER_NAME ) )
		{
			removeProvider( );
		}
		else if ( cmdName.equals( SHOW_PROVIDERS_NAME ) )
		{
			displayProviders();
		}
		else
		{
			throw new IllegalUsageException( "illegal command" );
		}
	}
}






