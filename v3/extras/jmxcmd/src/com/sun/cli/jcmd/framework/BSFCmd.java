/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/BSFCmd.java,v 1.3 2004/02/27 01:20:14 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/02/27 01:20:14 $
 */
 
package com.sun.cli.jcmd.framework;


//import org.apache.bsf.BSFManager;
import com.ibm.bsf.BSFManager;
import com.ibm.bsf.util.ObjectRegistry;

import com.sun.cli.jcmd.JCmdKeys;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdBase;

import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import com.sun.cli.jcmd.util.misc.StringUtils;

import com.sun.cli.jcmd.util.stringifier.ArrayStringifier;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;


/**
	Provides Bean Scripting Framework support.
 */
public class BSFCmd extends CmdBase
{
		public
	BSFCmd( final CmdEnv env )
	{
		super( env );
	}
	
	
	static final class BSFCmdHelp extends CmdHelpImpl
	{
			public
		BSFCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS		= "supports scripting via the Bean Scripting Framework";
			
		static final String	TEXT		= 
	"bsf-list list all registered objects by name" + NEWLINE +
	"The following scripting commands are available:" + NEWLINE +
	"";

		public String	getName()		{	return( BSF_NAME ); }
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		
			public String
		getText()
		{
			final StringBuffer	buf	= new StringBuffer( TEXT );
			
			for( int i = 0; i < LANG_INFOS.length; ++i )
			{
				final LangInfo	info	= LANG_INFOS[ i ];
				
				buf.append( info.mCmdName + ": " + info.mLanguage + NEWLINE );
				
			}
			
			return( buf.toString() );
		}
	}
	
		public CmdHelp
	getHelp()
	{
		return( new BSFCmdHelp() );
	}

	static final String	BSF_NAME		= "bsf";
	static final String	LIST_NAME		= "bsf-list";
	static final String	REGISTER_NAME	= "bsf-register";
	
		public static CmdInfos
	getCmdInfos( )
	{
		final int NUM_EXTRA	= 2;
		final CmdInfo[]		infos	= new CmdInfo[ NUM_EXTRA + LANG_INFOS.length ];
		final OperandsInfo	operandsInfo	= new OperandsInfoImpl( "arg[ arg]*", 1);
		
		for( int i = 0; i < LANG_INFOS.length; ++i )
		{
			final LangInfo	info	= LANG_INFOS[ i ];
			
			infos[ i ]	= new CmdInfoImpl( info.mCmdName, operandsInfo );
		}
		
		infos[ infos.length - NUM_EXTRA ]		= new CmdInfoImpl( LIST_NAME );
		infos[ infos.length - NUM_EXTRA + 1 ]	= new CmdInfoImpl( REGISTER_NAME,
					new OperandsInfoImpl( "bsf-name", 1, 1) );
		
		return( new CmdInfos( infos ) );
	}


	private static final String	BSF_MGR_KEY	= "BSFMgr";
	
	private static final class LangInfo
	{
		public final String		mLanguage;
		public final String		mCmdName;
		public final String		mEngineClassname;
		public final String[]	mExtensions;
		
			public
		LangInfo(
			String		language,
			String		cmdName,
			String		engineClassname,
			String[]	extensions )
		{
			mLanguage			= language;
			mCmdName			= cmdName;
			mEngineClassname	= engineClassname;
			mExtensions			= extensions;
		}
		
			boolean
		cmdMatches( String cmd )
		{
			return( mCmdName.equals( cmd ) );
		}
	};
	
	private static final String	JACL		= "Jacl";
	private static final String	JAVACLASS	= "JavaClass";
	private static final String	JAVASCRIPT	= "JavaScript";
	private static final String	JPYTHON		= "JPython";
	private static final String	XSLT		= "XSLT";
	private static final String	NETREXX		= "NetRexx";
	
	private static final LangInfo[]		LANG_INFOS	=
	{
		new LangInfo( JACL, 		"jacl",	"com.ibm.bsf.engines.jacl.JaclEngine",				new String[] { ".jacl" } ),
		new LangInfo( JACL, 		"jc",	"com.ibm.bsf.engines.javaclass.JavaClassEngine",	new String[] { ".class" } ),
		new LangInfo( JAVASCRIPT,	"js",	"com.ibm.bsf.engines.javascript.JavaScriptEngine",	new String[] { ".js" } ),
		new LangInfo( JPYTHON,		"jp",	"com.ibm.bsf.engines.jpython.JPythonEngine",		new String[] { ".jp" } ),
		new LangInfo( XSLT,			"xslt",	"com.ibm.bsf.engines.xslt.XSLTEngine",				new String[] { ".xslt" } ),
		new LangInfo( NETREXX,		"nr",	"com.ibm.bsf.engines.netrexx.NetRexxEngine",		new String[] { ".nrx" } ),
		
	};
	
	
	/**
		Register all known languages.
	 */
		private void
	registerLanguages( )
	{
		for( int i = 0; i < LANG_INFOS.length; ++i )
		{
			final LangInfo	info	= LANG_INFOS[ i ];
			
			BSFManager.registerScriptingEngine( info.mLanguage, info.mEngineClassname, info.mExtensions );
		}
	}
	
	/**
		In order to be able to display what's registered, we have to maintain
		a redundant map, since this is not made available by ObjectRegistry.
	 */
	private final class MyObjectRegistry extends ObjectRegistry
	{
		public	MyObjectRegistry()	{ this( null ); }
		
		private final java.util.Map	mMap;
		
			public
		MyObjectRegistry( ObjectRegistry parent )
		{
			super( parent );
			
			mMap	= new java.util.HashMap();
		}
		
			public void
		register( String name, Object obj)
        {
        	super.register( name, obj );
        	
        	mMap.put( name, obj );
        }
			public void
		unregister( String name )
        {
        	super.unregister( name );
        	
        	mMap.remove( name );
        }
        
        	public java.util.Set
        getAllNames()
        {
        	return( mMap.keySet() );
        }
	}
	
	/**
		The BSFManager must persist across invocations so that state may be preserved.
		Accordingly, store it in the CmdEnv.
	 */
		private BSFManager
	getBSFMgr()
	{
		BSFManager	mgr	= (BSFManager)envGet( BSF_MGR_KEY );
		if ( mgr == null )
		{
			mgr	= new BSFManager();
			envPut( BSF_MGR_KEY, mgr, false );
			
			mgr.setObjectRegistry( new MyObjectRegistry( mgr.getObjectRegistry() ) );
			registerLanguages( );
		}
		return( mgr );
	}
	
	/**
		Map a command name to the script language
		
		@return the script language, or null if the command is not a script language
	 */
		private String
	getLanguage( String cmd )
	{
		String	language	= null;
		
		for( int i = 0; i < LANG_INFOS.length; ++i )
		{
			if ( LANG_INFOS[ i ].cmdMatches( cmd ) )
			{
				language	= LANG_INFOS[ i ].mLanguage;
				break;
			}
		}
		
		return( language );
	}
	
		private String
	buildArgString( final String[] args )
	{
		return( ArrayStringifier.stringify( args, " ") );
	}
	
		private void
	listRegistered( )
	{
		final MyObjectRegistry	registry	= (MyObjectRegistry)getBSFMgr().getObjectRegistry();
		
		println( SmartStringifier.toString( registry.getAllNames() ) );
	}
	
		protected void
	bsfRegister( String	name, Object o )
	{
		getBSFMgr().registerBean( name, o );
	}
	

	/**
		Register LAST_RESULT object under the specified name
	 */
		protected void
	bsfRegister( String	name )
	{
		final Object	o	=  envGet( JCmdKeys.LAST_RESULT );
		if ( o != null )
		{
			bsfRegister( name, o );
			assert( getBSFMgr().lookupBean( name ) == o );
			println( "Registered LAST_RESULT as " + StringUtils.quote( name ) );
		}
		else
		{
			printError( "No last-result found" );
		}
		
	}
	
	
	/*
	this needs to go into a jmxcmd class, not here, so as to avoid introducing
	a jmx-dependency.  probably this BSFCmd should be subclassed by jmxcmd and its
	mapping replaced with the subclass
	
		private void
	bsfRegisterMBean( String	nameValuePair )
	{
		final int	delimIndex	= nameValuePair.indexOf( "=" );
		
		final String	name		= nameValuePair.substring( 0, delimIndex);
		final String	objectName	= nameValuePair.substring( delimIndex + 1, nameValuePair.length());
		
		println( "register MBean " + StringUtils.quote( objectName ) +
			" with name " + StringUtils.quote( name ) );
		
		// need to generate a proxy implementing DynamicMBean
		final DynamicMBean	proxy	= MBeanProxyFactory.newProxyInstance( 
			connectionSource, objectName, javax.management.DynamicMBean.class, false );
		
		bsfRegister( name, proxy );
	}
	*/
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd			= getSubCmdNameAsInvoked();
		final String []	operands	= getOperands();
		
		final BSFManager	mgr			= getBSFMgr();
		final String		language	= getLanguage( cmd );
		if ( language != null )
		{
			final String	argString	= buildArgString( operands );
		
			// execute the line with the appropriate scripting language
			final Object	result	= mgr.eval( language, "cli input", 0, 0, argString );
			println( SmartStringifier.toString( result ) );
			
			envPut( JCmdKeys.LAST_RESULT, result, false );
		}
		else if ( cmd.equals( LIST_NAME ) )
		{
			listRegistered();
		}
		else if ( cmd.equals( REGISTER_NAME ) )
		{
			bsfRegister( operands[ 0 ] );
		}
		else
		{
			printError( "unknown bsf command: " + cmd );
		}
	}
}









