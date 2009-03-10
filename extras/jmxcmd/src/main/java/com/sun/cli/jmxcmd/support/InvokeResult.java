/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/InvokeResult.java,v 1.3 2004/04/26 07:29:39 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/04/26 07:29:39 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.io.Serializable;


import javax.management.ObjectName;


/**
 */
public final class InvokeResult implements Serializable
{
	final ObjectName	mObjectName;
	Object				mResult;
	Throwable			mThrowable;
	
	public static final class ResultType
	{
		final int	mResultType;
			private
		ResultType( int value )
		{
			mResultType	= value;
		}
	}
	
	public final static ResultType	SUCCESS		= new ResultType( 0 );
	public final static ResultType	FAILURE		= new ResultType( 1 );
	public final static ResultType	NOT_FOUND	= new ResultType( 2 );
	
		public
	InvokeResult( ObjectName name, Object result, Throwable t )
	{
		mObjectName		= name;
		mResult			= result;
		mThrowable		= t;
	}
	
	/**
		Get the ObjectName upon which invoke() was called.
	 */
		public ObjectName
	getInvokee()
	{
		return( mObjectName );
	}
	
	/**
		Return one of { NOT_FOUND, SUCCESS, FAILURE }
	 */
		public ResultType
	getResultType()
	{
		ResultType	result	= NOT_FOUND;
		
		if ( mThrowable == null )
		{
			result	= SUCCESS;
		}
		else
		{
			if ( mThrowable instanceof java.lang.NoSuchMethodException )
			{
				result	= NOT_FOUND;
			}
			else
			{
				result	= FAILURE;
			}
		}
		
		return( result );
	}
	
	
	/**
		Get the Object which results from the method invocation.
		
		An Exception will be thrown if the method did not succeed.
	 */
		public Object
	getResult()
	{
		if ( getResultType() != SUCCESS )
		{
			throw new IllegalArgumentException( "Can't get result for failed invoke" );
		}
		
		return( mResult );
	}
	
	/**
		If the invocation failed due to a Throwable being thrown, return it.
		
		Null will be returned if the invocation succeeded or the operation was not
		found.
	 */
		public Throwable
	getThrowable()
	{
		return( mThrowable );
	}
	
	/**
		Determine the invocation failed due to a NoSucMethodException.
	 */
		public boolean
	noSuchMethod()
	{
		return( mThrowable instanceof NoSuchMethodException );
	}
}



