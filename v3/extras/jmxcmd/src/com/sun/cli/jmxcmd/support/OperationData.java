/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/OperationData.java,v 1.1 2003/11/21 21:23:49 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:49 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.lang.reflect.Array;

/**
 */
final class OperationData
{
	String					mName		= null;
	ParsedObject []			mArgInfo		= null;
	
		public
	OperationData( String name, ParsedObject [] argInfo )
	{
		mName		= name;
		mArgInfo	= argInfo;
	}
	
	
		String []
	getSignature()
	{
		final int		numArgs	= Array.getLength( mArgInfo );
		
		final String []	signature	= new String[ numArgs ];
		
		for ( int i = 0; i < numArgs; ++i )
		{
			signature[ i ]	= mArgInfo[ i ].mClass.getName();
		}
		
		return( signature );
		
	}
		Object []
	getArgs()
	{
		final int		numArgs	= Array.getLength( mArgInfo );
		
		final Object []	args	= new Object[ numArgs ];
		
		for ( int i = 0; i < numArgs; ++i )
		{
			args[ i ]	= mArgInfo[ i ].mObject;
		}
		
		return( args );
	}
}
	
	