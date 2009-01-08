/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdOutputNull.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 
package com.sun.cli.jcmd.framework;

/**
	Directs output to the "bit bucket".
 */
public final class CmdOutputNull implements CmdOutput
{
		public
	CmdOutputNull( )
	{
	}
	
		public void
	print( Object o )
	{
	}
	
		public void
	println( Object o )
	{
	}
	
		public void
	printError( Object o )
	{
	}
	
		public boolean
	getDebug(  )
	{
		return( false );
	}
	
		public void
	printDebug( Object o )
	{
	}
	
	
		public void
	close( )
	{
	}
};


