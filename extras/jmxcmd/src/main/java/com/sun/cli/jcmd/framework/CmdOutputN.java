/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdOutputN.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 
package com.sun.cli.jcmd.framework;

/**
	Directs output to to other CmdOutputs
 */

public class CmdOutputN implements CmdOutput
{
	private final CmdOutput[]		mOutputs;
	
		public
	CmdOutputN( CmdOutput[]	outputs )
		throws java.io.IOException
	{
		mOutputs	= outputs;
	}
	
		public void
	print( Object o )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].print( o );
		}
	}
	
		public void
	println( Object o )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].print( o + "\n" );
		}
	}
	
		public void
	printError( Object o )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].printError( o );
		}
	}
	
		public boolean
	getDebug(  )
	{
		boolean	debug	= false;
		
		for( int i = 0; i < mOutputs.length; ++i )
		{
			if ( mOutputs[ i ].getDebug() )
			{
				debug	= true;
				break;
			}
		}
		return( debug );
	}
	
		public void
	printDebug( Object o )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].printDebug( o );
		}
	}
	
	
		public void
	close( )
	{
		for( int i = 0; i < mOutputs.length; ++i )
		{
			mOutputs[ i ].close();
		}
	}
}
	

