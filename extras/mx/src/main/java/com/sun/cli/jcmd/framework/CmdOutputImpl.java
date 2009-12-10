/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdOutputImpl.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 

package com.sun.cli.jcmd.framework;

import java.io.PrintStream;
import org.glassfish.admin.amx.util.DebugState;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;


/**
	Wraps the concepts of stdout, error out and debug out.
 */
public class CmdOutputImpl implements CmdOutput
{
	final PrintStream	mOutput;
	final PrintStream	mErrorOutput;
	final PrintStream	mDebugOutput;
	final DebugState	mDebugState;
	
	/**
		Create a new instance with outputs set to System.out, System.err, debug off
	 */
		public
	CmdOutputImpl(  )
	{
		this( System.out, System.err, null, null );
	}
	
	/**
		Create a new instance with the specified outputs, debug output *off*.
		
		@param output		the PrintStream for normal output
		@param errorOutput	the PrintStream for error output
	 */
		public
	CmdOutputImpl( PrintStream output, PrintStream errorOutput )
	{
		this( output, errorOutput, null, null );
	}
	
	/**
		Create a new instance with the specified outputs, debug output as specified.
		
		@param output		the PrintStream for normal output
		@param errorOutput	the PrintStream for error output
		@param debugOutput	the PrintStream for debug output
		@param debugState	the DebugState governing checks for debugging output
	 */
		public
	CmdOutputImpl(
		PrintStream	output,
		PrintStream	errorOutput,
		PrintStream	debugOutput,
		DebugState debugState)
	{
		mOutput			= output;
		mErrorOutput	= errorOutput;
		mDebugOutput	= debugOutput;
		
		mDebugState	= debugState;
	}
	
		private String
	objectToString( Object o )
	{
		String	s	= null;
		
		if ( o == null )
		{
			s	= "null";
		}
		else
		{
			s	= SmartStringifier.toString( o );
		}
		return( s );
	}

		public void
	print( Object o )
	{
		mOutput.print( objectToString( o ) );
	}
	
		public void
	println( Object o )
	{
		mOutput.println( objectToString( o ) );
	}
	
		public void
	printError( Object o )
	{
		mErrorOutput.println( "ERROR: " + objectToString( o ) );
	}
		
		public boolean
	getDebug()
	{
		return( mDebugState != null && mDebugState.getDebug() );
	}
	
		public void
	printDebug( Object o )
	{
		if ( getDebug() )
		{
			mDebugOutput.println( "#DEBUG: " + objectToString( o ) );
		}
	}
	
		public void
	close( )
	{
	}
};


