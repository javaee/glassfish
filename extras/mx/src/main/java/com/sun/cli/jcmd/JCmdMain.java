/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/JCmdMain.java,v 1.18 2005/11/08 22:39:15 llc Exp $
 * $Revision: 1.18 $
 * $Date: 2005/11/08 22:39:15 $
 */
 
package com.sun.cli.jcmd;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.ListIterator;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistryImpl;

import com.sun.cli.jcmd.util.cmd.ArgHelper;
import com.sun.cli.jcmd.util.cmd.ArgHelperImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.ParsedOption;
import com.sun.cli.jcmd.util.misc.TokenizerException;

import com.sun.cli.jcmd.framework.CmdMgr;
import com.sun.cli.jcmd.framework.FileNames;
import org.glassfish.admin.amx.util.ClassUtil;

/**
	Main entry point to start jcmd.  It is intended that a script be used to wrap the invocation of this class because
	specifying the name of the CLI and the CmdMgr are generally desired (unless it's just jcmd itself).  See
	JCmdKeys for the variables defining these elements:
	
	General syntax is as follows:
	[boot [--name|-n=<cli-name>] [--cmd-mgr-class|-c=<classname>]]
	[meta [--prefs-dir|-s=<dir>] [--debug|-d] ]
	[subcommand [options] [operands] [--props-file|-f=<file>] [--prop|-p=<name=value>]
	
	Generally, a script would incorporate fixed 'boot' and 'meta' options, and pass along the user
	arguments as the [subcommand...] part.
 */

public class JCmdMain
{
	boolean			mDebug;
	String			mCLIName;
	String			mCmdMgrClassname;
	
		public static void
	main(String args[])
	{
		System.exit( new JCmdMain( ).doMain( args ) );
	}
	
	
	
		private
	JCmdMain(  )
	{
		mCLIName				= "jcmd";
		mCmdMgrClassname		= JCmdKeys.DEFAULT_CMD_MGR_CLASSNAME;
	}
	
		void
	printDebug( String s )
	{
		if ( mDebug )
		{
			System.out.println( "#DEBUG: " + s );
		}
	}
	
	
	
	private static final OptionInfo[]	BOOT_OPTIONS	= 
	{
		JCmdKeys.CLI_NAME_OPTION,
		JCmdKeys.CMD_MGR_NAME_OPTION,
		JCmdKeys.DEBUG_META_OPTION,
	};
	
		private String[]
	processBootOptions( final String[] args )
		throws IllegalOptionException, java.io.IOException, TokenizerException
	{
		String[]	remainingArgs	= args;
		
		final boolean	haveBootCmd	= args.length != 0 &&
							args[ 0 ].equals( JCmdKeys.BOOT_OPTIONS_CMD );
		if ( haveBootCmd )
		{
			final ListIterator<String>	iter	= Arrays.asList( args ).listIterator();
			iter.next();	// skip the first one which is BOOT_OPTIONS_CMD
			
			final ArgHelperImpl	argHelper	=
				new ArgHelperImpl( iter, new OptionsInfoImpl( BOOT_OPTIONS ) );
			
			mDebug				= argHelper.getBooleanValue( JCmdKeys.DEBUG_META_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
			mCLIName			= argHelper.getStringValue( JCmdKeys.CLI_NAME_OPTION.getShortName(), mCLIName );
			mCmdMgrClassname	= argHelper.getStringValue( JCmdKeys.CMD_MGR_NAME_OPTION.getShortName(), mCmdMgrClassname );
			
            printDebug( "boot args[]= " + ArrayStringifier.stringify( args, " " ) );
                
			remainingArgs	= argHelper.getOperands();
		}
		
		return( remainingArgs );
	}
	
		
	private static final OptionInfo[]	META_OPTIONS	= 
	{
		JCmdKeys.PREFS_DIR_META_OPTION,
		JCmdKeys.PROPERTIES_FILE_META_OPTION,
		JCmdKeys.PROPERTY_META_OPTION,
	};
	

		private static boolean
	isMetaCommand( final String[] args )
	{
		return( args.length != 0 && args[ 0 ].equals( JCmdKeys.META_OPTIONS_CMD ) );
	}
	
		private static void
	mergeProps( final Properties src, final Properties dest)
	{
		final java.util.Enumeration	e	= src.propertyNames();
		
		while ( e.hasMoreElements() )
		{
			final String	key	= (String)e.nextElement();
			dest.put( key, src.get( key ) );
		}
	}
	
	/**
		Process meta options.  The meta command may be repeated an arbitrary number
		of times, so the code should assume that previously processed options may
		already be present.
	 */
		private String[]
	processMetaOptions( final String[] args, Map<String,Object> metaOptionsMap )
		throws IllegalOptionException, java.io.IOException, TokenizerException
	{
		final ListIterator<String>	iter	= Arrays.asList( args ).listIterator();
		iter.next();	// skip the first one which is META_OPTIONS_CMD
		
		final ArgHelperImpl	argHelper	=
			new ArgHelperImpl( iter, new OptionsInfoImpl( META_OPTIONS ) );
		
		final String prefsDir	= argHelper.getStringValue( JCmdKeys.PREFS_DIR_META_OPTION.getShortName(), null );
		// it's ok if prefsDir is null; default will be used
		metaOptionsMap.put( JCmdKeys.PREFS_DIR_META_OPTION.getLongName(), prefsDir );
		metaOptionsMap.put( JCmdKeys.DEBUG_META_OPTION.getLongName(), "" + mDebug );       
                 				
		final Properties props	= getProperties( argHelper );
		if ( mDebug )
		{
            printDebug( "meta args[]= " + ArrayStringifier.stringify( args, " " ) );

			final Properties	curProps	= (Properties)metaOptionsMap.get( JCmdKeys.PROPERTIES );
			if ( curProps == null )
			{
				// none yet, these are the new props
				metaOptionsMap.put( JCmdKeys.PROPERTIES, props );
			}
			else
			{
				mergeProps( props, curProps );
			}

		}
		
		final String[]	remainingArgs	= argHelper.getOperands();
		
		return( remainingArgs );
	}
	
		private int
	doMain(  String[] args  )
	{
		int 	resultCode	= 0;
		boolean	singleInvocation	= false;
		String	commandName	= "<none>";
		
		try
		{
			String[]	remainingArgs	= processBootOptions( args );
			
			final Map<String,Object>	metaOptionsMap	= new HashMap<String,Object>();
			metaOptionsMap.put( JCmdKeys.PROPERTIES, new Properties() );
			
			/**
				The meta command may be repeated any number of times
			*/
			while ( isMetaCommand( remainingArgs ) )
			{
				remainingArgs	= processMetaOptions( remainingArgs, metaOptionsMap );
			}
			
			sCmdMgr	= init( mCLIName, metaOptionsMap, mCmdMgrClassname );
			
			printDebug( "------------------------------------------------------------" );

			if ( remainingArgs.length != 0 )
			{
				singleInvocation	= true;
				commandName			= remainingArgs[ 0 ];
			}
			resultCode	= sCmdMgr.run( remainingArgs );
		}
		catch( Exception e )
		{
			System.err.println( "ERROR starting: " + e );
			e.printStackTrace();
			
			resultCode	= 255;
		}
		
		// if it was a single command, then print its status 
		if ( resultCode != 0 && singleInvocation )
		{
			System.err.println( "Command \"" + commandName + "\" failed with exit code: " + resultCode );
		}
		
		return( resultCode );
	}
		
	/**
		Create a CmdMgr of the specified class, passing it the env file.
	 */
		static CmdMgr
	createCmdMgr( String className, final Map metaOptions )
		throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, InvocationTargetException,
			IllegalAccessException
	{
		final Class<?>	theClass	= ClassUtil.getClassFromName( className );
		
		final Class<?>[]		sig	= new Class[] { Map.class };
		final Constructor<?>	c	= theClass.getConstructor( sig );
		
		return ( (CmdMgr)c.newInstance( new Object[] { metaOptions } ) );
	}
	
		public CmdMgr
	init(
		String	cliName,
		Map<String,Object>	metaOptions,
		String	cmdMgrClassname )
		throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, InvocationTargetException,
			IllegalAccessException
	{
		final String	prefsDir	= (String)metaOptions.get( JCmdKeys.PREFS_DIR_META_OPTION.getLongName() );
		
		printDebug( JCmdKeys.CLI_NAME_OPTION.getLongName() + "=" + cliName );
		printDebug( JCmdKeys.CMD_MGR_NAME_OPTION.getLongName() + "=" + cmdMgrClassname );
		
		FileNames.init( prefsDir, cliName );
		metaOptions.put( JCmdKeys.CLI_NAME_OPTION.getLongName(), cliName );
		
		printDebug( "Prefs dir = " + FileNames.getInstance().getPrefsDir() );
		
		new JCmdStringifierRegistryIniter( StringifierRegistryImpl.DEFAULT );
		
		final CmdMgr	mgr	= createCmdMgr( cmdMgrClassname, metaOptions );
		
		return( mgr );
	}
	
	/**
		Load all property files and props options into a single Properties object
		and return it.
	 */
		Properties
	getProperties( ArgHelper	argHelper )
		throws IllegalOptionException, IOException
	{
		final Properties		allProps	= new Properties();
		final ParsedOption[]	propFileOptions	=
			argHelper.getOptionInstances( JCmdKeys.PROPERTIES_FILE_META_OPTION.getShortName() );
		final ParsedOption[]	propOptions		=
			argHelper.getOptionInstances( JCmdKeys.PROPERTY_META_OPTION.getShortName() );
		
		for( int i = 0; i < propFileOptions.length; ++i )
		{
			File	f	= new File( propFileOptions[ i ].getValue() );
			
			final FileInputStream	is	= new FileInputStream( f );
			try
			{
				allProps.load( is );
			}
			finally
			{
				is.close();
			}
		}
		
		// now add all the individual properties
		for( int i = 0; i < propOptions.length; ++i )
		{
			final String	pair	= propOptions[ i ].getValue();
			final int		delimIndex	= pair.indexOf( '=' );
			if ( delimIndex <= 1 )
			{
				throw new IllegalOptionException( "Illegal property pair: " + pair );
			}
			
			final String	name	= pair.substring( 0, delimIndex );
			final String	value	= pair.substring( delimIndex + 1, pair.length() );
			
			allProps.setProperty( name, value );
		}
		
		return( allProps );
	}
	
	
	private static CmdMgr	sCmdMgr	= null;
		public static CmdMgr
	getCmdMgr()
	{
		return( sCmdMgr );
	}
};


