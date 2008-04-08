/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/util/misc/ExceptionUtil.java,v 1.3 2007/05/05 05:31:05 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2007/05/05 05:31:05 $
 */
 
package com.sun.appserv.management.util.misc;

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
    getStackTrace()
    {
        return toString(new Exception("STACK TRACE"));
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
		final ArrayList<Throwable>	list	= new ArrayList<Throwable>();
		
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

