/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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

