/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/cmd/com/sun/enterprise/s1as8/cmd/S1AS8CmdSource.java,v 1.5 2005/05/20 00:09:39 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2005/05/20 00:09:39 $
 */
 
package com.sun.enterprise.s1as8.cmd;

import com.sun.cli.jcmd.framework.CmdSource;

public class S1AS8CmdSource implements CmdSource
{
		public
	S1AS8CmdSource()
	{
	   final String CLASS    = "com.sun.appserv.management.DomainRoot";
	   
	   try
	   {
	      Class.forName( CLASS );
	   }
	   catch( Exception e )
	   {
            throw new RuntimeException( "Class " + CLASS +
                " not found; put amx-client.jar into the classpath" );
	   }
	}
	
	private final static Class[]	CLASSES	=
	{
		S1AS8Cmd.class,
		S1AS8TestCmd.class,
	};
	
		public Class[]
	getClasses( )
	{
		return( CLASSES );
	}
}






