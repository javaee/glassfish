/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/GetterInvocationHandler.java,v 1.1 2004/10/14 19:06:23 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:23 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.sun.cli.jmxcmd.util.jmx.JMXUtil;
import com.sun.cli.jcmd.util.misc.StringUtil;

/**
	Abstract base {@link InvocationHandler} for any getXXX() method.
	<br><b>Internal use only</b>
 */
public abstract class GetterInvocationHandler implements InvocationHandler,Serializable
{
	static final long serialVersionUID = 7293181901362984709L;

	/**
	 */
		public
	GetterInvocationHandler()
	{
	}
	
	protected abstract Object	getValue( String name );
	protected abstract boolean	containsValue( String name );
	
	/**
	*/
		public Object
	invoke(
		Object		myProxy,
    	Method		method,
		Object[]	args )
   		throws java.lang.Throwable
   	{
   		Object			result	= null;
   		final String	methodName		= method.getName();
   		final int		numArgs	= args == null ? 0 : args.length;
   		
   		if ( numArgs == 0 && JMXUtil.isGetter( method ) )
   		{
   			final String	name	= StringUtil.stripPrefix( methodName, JMXUtil.GET );
   			
   			result	= getValue( name );
   			if ( result == null && ! containsValue( name ) )
   			{
   				throw new NoSuchMethodException( methodName );
   			}
   		}
   		else if ( method.getName().equals( "equals" ) &&
   			numArgs == 1 )
   		{
   			result	= new Boolean( equals( args[ 0 ] ) );
   		}
   		else if ( numArgs == 0 && method.getName().equals( "toString" ) &&
   			method.getReturnType() == String.class )
   		{
   			result	= this.toString();
   		}
   		else if ( numArgs == 0 && method.getName().equals( "hashCode" ) &&
   			method.getReturnType() == int.class )
   		{
   			result	= new Integer( this.hashCode() );
   		}
   		else
   		{
   			throw new IllegalArgumentException( methodName );
   		}

   		return( result );
   	}
   
}





