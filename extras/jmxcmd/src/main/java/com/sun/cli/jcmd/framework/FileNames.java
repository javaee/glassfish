/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/FileNames.java,v 1.4 2003/12/15 23:30:03 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2003/12/15 23:30:03 $
 */
package com.sun.cli.jcmd.framework;

import java.io.File;

public final class FileNames
{
	final String	mBaseName;
	final File		mPrefsDir;
	
	private static FileNames	sInstance;
	
		private
	FileNames( File homeDir, String baseName )
	{
		mBaseName	= baseName;
		mPrefsDir	= homeDir;
	}
	
		public static synchronized void
	init( String homeDir, String baseName )
	{
		if ( homeDir == null || homeDir.length() == 0 )
		{
			homeDir	= getDefaultPrefsDir();
		}

		sInstance	= new FileNames( new File( homeDir ), baseName );
	}
	
		public synchronized static FileNames
	getInstance( )
	{
		if ( sInstance == null )
		{
			throw new NullPointerException( "FileNames instance not initialized" );
		}
		return( sInstance );
	}

	public final static  String	PROPS_FILE_SUFFIX		= "-env.props";
	public final static String	HISTORY_FILE_SUFFIX		= "-history";
	public final static String	CMD_ALIASES_FILE_SUFFIX	= "-cmd-aliases";
	
		private static String
	getDefaultPrefsDir()
	{
		// look first for a system property 
		String homeDir	= System.getProperty( "user.home" );
		if ( homeDir == null )
		{
			homeDir	= ".";
		}
		
		return( homeDir );
	}
	
		String
	getFileSeparator()
	{
		return( System.getProperty( "file.separator" ) );
	}

		public File
	getPrefsDir()
	{
		return( mPrefsDir );
	}
	
	/**
		Return a File in the PrefsDir.  The final name is formed as follows:
		<prefs-dir>/<prefix><filename>
		
		The 'prefix' part is the name of the CLI currently running.
	 */
		public File
	getPrefsDirFile( String filename )
	{
		return( new File( getPrefsDirPrefix() + mBaseName + filename ) );
	}
		
		private String
	getPrefsDirPrefix()
	{
		return( getPrefsDir().toString() + getFileSeparator() );
	}
	
		public File
	getEnvFile()
	{
		final String name	= getPrefsDirPrefix() + mBaseName + PROPS_FILE_SUFFIX;
		return( new File( name ) );
	}
	
		public File
	getHistoryFile()
	{
		final String name	= getPrefsDirPrefix() + mBaseName + HISTORY_FILE_SUFFIX;
		return( new File( name ) );
	}
	
		public File
	getCmdAliasesFile()
	{
		final String name	= getPrefsDirPrefix() + mBaseName + CMD_ALIASES_FILE_SUFFIX;
		return( new File( name ) );
	}
}

