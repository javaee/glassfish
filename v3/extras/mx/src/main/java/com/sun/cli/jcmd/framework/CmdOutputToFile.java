/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdOutputToFile.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 

package com.sun.cli.jcmd.framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.glassfish.admin.amx.util.DebugState;

/**
	Sends all output to a file.
 */
public class CmdOutputToFile implements CmdOutput
{
	final PrintStream	mOutput;
	final DebugState	mDebugState;
	
		public
	CmdOutputToFile( final File theFile )
		throws java.io.IOException
	{
		this( theFile, null );
	}
	
		public
	CmdOutputToFile( final File theFile, final DebugState debugState )
		throws java.io.IOException
	{
		theFile.createNewFile( );
		mOutput	= new PrintStream( new FileOutputStream( theFile, true ) );
		
		mDebugState	= debugState;
	}
	
		public void
	print( Object o )
	{
		mOutput.print( o );
	}
	
		public void
	println( Object o )
	{
		mOutput.println( o );
	}
	
		public void
	printError( Object o )
	{
		mOutput.println( o );
	}
	
		public boolean
	getDebug( )
	{
		return( mDebugState != null && mDebugState.getDebug() );
	}
	
		public void
	printDebug( Object o )
	{
		if ( getDebug() )
		{
			mOutput.println( o );
		}
	}
	
		public void
	close()
	{
		mOutput.close();
	}
};


