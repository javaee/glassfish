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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/ConfigureCmd.java,v 1.3 2005/12/25 03:45:32 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:32 $
 */
 
package com.sun.cli.jmx.cmd;

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.cli.util.stringifier.IteratorStringifier;

import com.sun.cli.jmx.spi.JMXConnectorProvider;
import com.sun.cli.util.ClassUtil;


public class ConfigureCmd extends JMXCmd
{
	public final static char	CLASSNAME_DELIM	= ',';
	
		public
	ConfigureCmd( final CmdEnv env )
	{
		super( env );
	}
	
		int
	getNumRequiredOperands()
	{
		return( 0 );
	}
	
	
		String
	getUsage()
	{
		return( CmdStrings.CONFIGURE_HELP.toString() );
	}
	
	public final static String NAME			= "configure";
	public final static String SHOW_CONFIG	= "show-config";
	public final static String ADD_PROVIDER	= "add-provider";
	public final static String REMOVE_PROVIDER	= "remove-provider";
	public final static String ADD_CMD			= "add-cmd";
	public final static String REMOVE_CMD		= "remove-cmd";
	
	private final static String [] NAMES	= new String []
	{
		 NAME, SHOW_CONFIG, ADD_PROVIDER, REMOVE_PROVIDER, ADD_CMD, REMOVE_CMD 
	};
	
		public static String []
	getNames( )
	{
		return( NAMES );
	}
	
	
	static private final String	OPTIONS_INFO	= "";
		
	
		ArgHelper.OptionsInfo
	getOptionInfo()
		throws ArgHelper.IllegalOptionException
	{
		return( new ArgHelperOptionsInfo( OPTIONS_INFO ) );
	}
	
	static class ClassList
	{
		final ArrayList	mItems;
		
		ClassList( String providersString )
		{
			mItems	= new ArrayList();
			
			if ( providersString != null )
			{
				final String []	list	= providersString.trim().split( "" + CLASSNAME_DELIM );
				
				// first listed should be first in priority, so add to end
				for ( int i = 0; i < list.length; ++i )
				{
					mItems.add( list[ i ] );
				}
			}
		}
		
			public String
		toString()
		{
			return( IteratorStringifier.stringify( mItems.iterator(), "" + CLASSNAME_DELIM ) );
		}
		
			public String []
		toArray()
		{
			return( (String [])mItems.toArray( new String[ mItems.size() ] ) );
		}
		
			public boolean
		exists( String name )
		{
			return( mItems.contains( name ) );
		}
		
			public Iterator
		iterator()
		{
			return( mItems.iterator() );
		}
		
			public void
		add( String name )
		{
			// add it in front--last added is first in priority
			mItems.add( 0, name );
		}
		
			public void
		remove( String name )
		{
			// add it in front--last added is first in priority
			mItems.remove( name );
		}
	}
	
		ClassList
	createProviderList()
	{
		return( new ClassList( (String)envGet( ENV_PROVIDERS ) ) );
	}
	
		ClassList
	createCmdList()
	{
		return( new ClassList( (String)envGet( ENV_COMMANDS ) ) );
	}
	
		boolean
	isLegalClassname( String name )
	{
		// should test this properly, but at least make sure it doesn't have our delim
		return( name.indexOf( CLASSNAME_DELIM ) < 0 );
	}
	
		void
	addProvider(  )
		throws Exception
	{
		requireNumOperands( 1, "A provider classname must be specified");
		
		final String providerClassname	= getOperands()[ 0 ];
		
		ClassList	list	= createProviderList();
		
		if ( list.exists( providerClassname ) )
		{
			println( "Provider already in list: " + providerClassname );
		}
		else if ( ! isLegalClassname( providerClassname ) )
		{
			printError( "Illegal classname: " + providerClassname );
		}
		else
		{
			// if this succeeds, then we'll update the env
			try
			{
				getConnectionMgr().addProvider( Class.forName( providerClassname ) );
			}
			catch( Exception e )
			{
				printError( "WARNING: unable to instantiate provider (added to list anyway): " +
					e.getMessage() );
			}
			
			list.add( providerClassname );
			
			envPut( ENV_PROVIDERS, list.toString(), true );
		}
	}
	
		void
	removeProvider(  )
		throws Exception
	{
		requireNumOperands( 1, "A provider classname must be specified");
		
		final String providerClassname	= getOperands()[ 0 ];
		
		ClassList	list	= createProviderList();
		
		if ( ! list.exists( providerClassname ) )
		{
			println( "Provider not found: " + providerClassname );
		}
		else
		{
			// if this succeeds, then we'll update the env
			try
			{
				final Class theClass	= Class.forName( providerClassname );
				getConnectionMgr().removeProvider( theClass );
			}
			catch( Exception e )
			{
				// ignore
			}
			
			list.remove( providerClassname );
			
			envPut( ENV_PROVIDERS, list.toString(), true );
		}
	}
	
		void
	displayProviders(  )
		throws Exception
	{
		println( "Available providers: " );
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
	
		void
	displayCmds(  )
		throws Exception
	{
		println( "Commands configured: " );
		final String	list	= IteratorStringifier.stringify( createCmdList().iterator(), "\n" );
		
		println( (list.length() == 0) ? "<none>" : list );
	}
	
		void
	displayConfig(  )
		throws Exception
	{
		displayProviders();
		
		println( "" );
		
		displayCmds();
	}
	
		void
	addCmd()
		throws Exception
	{
		requireNumOperands( 1, "A command classname must be specified");
		
		final String commandClassname	= getOperands()[ 0 ];
		
		final ClassList	list	= createCmdList();
		
		if ( list.exists( commandClassname ) )
		{
			println( "Command already in list: " + commandClassname );
		}
		else if ( ! isLegalClassname( commandClassname ) )
		{
			printError( "Illegal classname: " + commandClassname );
		}
		else
		{
			final CmdFactoryIniter	initer = new CmdFactoryIniter( getCmdFactory() );
			final Class	theClass	= ClassUtil.getClassFromName( commandClassname );
			
			initer.addMappings( theClass );
			
			list.add( commandClassname );
			envPut( ENV_COMMANDS, list.toString(), true );
		}
	}
	
	
		void
	removeCmd()
		throws Exception
	{
		requireNumOperands( 1, "A classname must be specified");
		
		final String	classname	= getOperands()[ 0 ];
		final ClassList	list		= createCmdList();
		
		if ( ! list.exists( classname ) )
		{
			println( "Command not registered: " + classname );
		}
		else
		{
			// remove it from the preferences
			list.remove( classname );
			envPut( ENV_PROVIDERS, list.toString(), true );
			
			// remove it from the factory
			final CmdFactoryIniter	initer = new CmdFactoryIniter( getCmdFactory() );
			initer.removeMappings( ClassUtil.getClassFromName( classname ) );

		}
	}
	
	
		void
	executeInternal()
		throws Exception
	{
		final String	cmdName		= getCmdNameAsInvoked();
		
		if ( cmdName.equals( SHOW_CONFIG ) )
		{
			displayConfig();
		}
		else if ( cmdName.equals( ADD_PROVIDER ) )
		{
			addProvider();
		}
		else if ( cmdName.equals( REMOVE_PROVIDER ) )
		{
			removeProvider( );
		}
		else if ( cmdName.equals( ADD_CMD ) )
		{
			addCmd( );
		}
		else if ( cmdName.equals( REMOVE_CMD ) )
		{
			removeCmd( );
		}
		else
		{
			printUsage();
		}
	}
}






