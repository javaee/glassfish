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



