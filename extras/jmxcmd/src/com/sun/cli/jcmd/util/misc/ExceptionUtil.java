/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package com.sun.cli.jcmd.util.misc;

import java.util.List;
import java.util.ArrayList;


/**
	Useful utilities for Exceptions
 */
public final class ExceptionUtil
{
		private
	ExceptionUtil()
	{
		// disallow instantiation
	}
	
	    public static String
	toString( final Throwable t )
	{
	    final String SEP    = System.getProperty( "line.separator" );
	    
	    final Throwable rootCause   = getRootCause( t );
	    
	    return rootCause.getClass().getName() + ": " +
	        StringUtil.quote( rootCause.getMessage() ) + SEP +
	        getStackTrace( rootCause );
	}
		
	/**
		Get the chain of exceptions via getCause(). The first element is the
		Exception passed.
		
		@param start	the Exception to traverse
		@return		a Throwable[] or an Exception[] as appropriate
	 */
		public static Throwable[]
	getCauses( final Throwable start )
	{
		final List<Throwable>	list	= new ArrayList<Throwable>();
		
		boolean	haveNonException	= false;
		
		Throwable t	= start;
		while ( t != null )
		{
			list.add( t );
			
			if ( ! ( t instanceof Exception ) )
			{
				haveNonException	= true;
			}
			
			final Throwable temp	= t.getCause();
			if ( temp == null )
				break;
			t	= temp;
		}
		
		final Throwable[]	results	= haveNonException ?
			new Throwable[ list.size() ] : new Exception[ list.size() ];
		
		list.toArray( results );
		
		return( results );
	}
	
	
	/**
		Get the original troublemaker.
		
		@param e	the Exception to dig into
		@return		the original Throwable that started the problem
	 */
		public static Throwable
	getRootCause( final Throwable e )
	{
		final Throwable[]	causes	= getCauses( e );
		
		return( causes[ causes.length - 1 ] );
	}
	
	/**
		Get the stack trace as a String.
		
		@param t	the Throwabe whose stack trace should be gotten
		@return		a String containing the stack trace
	 */
		public static String
	getStackTrace( Throwable t )
	{
		final StringBuffer	buf	= new StringBuffer();
		final StackTraceElement[]	elems	= t.getStackTrace();
		
		for( int i = 0; i < elems.length; ++i )
		{
			buf.append( elems[ i ] );
			buf.append( "\n" );
		}
		
		
		return( buf.toString() );
	}
}

