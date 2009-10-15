/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.support;

import java.lang.reflect.Array;

/**
 */
final class OperationData
{
	final String			mName;
	final ParsedObject []	mArgInfo;
	
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
		final int		numArgs	= mArgInfo.length;
		
		final Object []	args	= new Object[ numArgs ];
		
		for ( int i = 0; i < numArgs; ++i )
		{
			args[ i ]	= mArgInfo[ i ].mObject;
		}
		
		return( args );
	}
}
	
	