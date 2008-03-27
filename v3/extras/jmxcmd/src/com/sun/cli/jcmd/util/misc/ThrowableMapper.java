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
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/ThrowableMapper.java,v 1.1 2005/11/08 22:39:24 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:39:24 $
 */

package com.sun.cli.jcmd.util.misc;

import java.util.Set;

import com.sun.cli.jcmd.util.misc.SetUtil;
import com.sun.cli.jcmd.util.misc.GSetUtil;
import com.sun.cli.jcmd.util.misc.ExceptionUtil;



/**
	Maps a Throwable to another one in order to avoid the transfer
	of non-standard Exception types, which could result in
	ClassNotFoundException on the client.
	<p>
	Any Throwable which either is, or contains,
	a Throwable which is not in the allowed packages is converted.
 */
public class ThrowableMapper 
{
	final Throwable	mOriginal;
	
	final Set<String>	mOKPackages;
	
	/**
		By default, any Throwable whose package does not start with one
		of these packages must be mapped to something standard.
	 */
	protected final static Set<String>		OK_PACKAGES	=
			GSetUtil.newUnmodifiableStringSet( "java.", "javax.", "com.sun.cli.jcmd." );
	
		public
	ThrowableMapper( final Throwable t )
	{
		mOriginal	= t;
		mOKPackages	= OK_PACKAGES;
	}
	
		protected boolean
	shouldMap( final Throwable t )
	{
		final String 	tClass	= t.getClass().getName();
		
		boolean	shouldMap	= true;
		
		for( final String prefix : mOKPackages )
		{
			if ( tClass.startsWith( prefix ) )
			{
				shouldMap	= false;
				break;
			}
		}
		
		return( shouldMap );
	}
	
		
		protected String
	getFullMsg(	final Throwable t)
	{
		final String	msg	= t.getClass().getName() + ": " +
							t.getMessage() + ":\n" +
							ExceptionUtil.getStackTrace( t );
		return( msg );
	}
	
		protected Throwable
	map( final Throwable t )
	{
		Throwable		result	= t;
		
		if ( t != null )
		{
			final Throwable		tCause			= t.getCause();
			final Throwable		tCauseMapped	= map( tCause );
			
			// if either this Exception or its cause needs/was mapped,
			// then we must form a new Exception
			if ( shouldMap( t ) || tCauseMapped != tCause )
			{
				final String	fullMsg	= getFullMsg( t );
				
				if ( t instanceof Error )
				{
					result	= new Error( fullMsg, tCauseMapped );
				}
				else if ( t instanceof RuntimeException )
				{
					result	= new RuntimeException( fullMsg, tCauseMapped );
				}
				else
				{
					result	= new Exception( fullMsg, tCauseMapped );
				}
			}
			else
			{
				result	= t;
			}
		}

		return( result );
	}
	
	/**
		Map the original Throwable to one that is OK.
	 */
		public Throwable
	map()
	{
		return( map( mOriginal ) );
	}
}








