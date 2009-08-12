/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/ClassSourceFromStrings.java,v 1.4 2005/11/08 22:39:16 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:16 $
 */
package com.sun.cli.jcmd.framework;

import java.util.List;
import java.util.ArrayList;
import org.glassfish.admin.amx.util.ClassUtil;

/**
	An implementation of ClassSource which uses classnames. 
 */
public final class ClassSourceFromStrings implements ClassSource
{
	private final String[]	mClassnames;
	private boolean			mErrorIfNotFound;
	
		public
	ClassSourceFromStrings( String[] classnames, boolean errorIfNotFound )
		throws ClassNotFoundException
	{
		mClassnames			= classnames;
		mErrorIfNotFound	= errorIfNotFound;
		
		if ( mErrorIfNotFound )
		{
			// produce an error right now if there's going to be one
			_getClasses();
		}
	}
	
		private Class[]
	_getClasses( )
		throws ClassNotFoundException
	{
		Class[]	classes	= null;
		
		if ( mClassnames == null)
		{
			classes	=	new Class[ 0 ];
		}
		else
		{
			final List<Class>	list	= new ArrayList<Class>();
			
			for( int i = 0; i < mClassnames.length; ++i )
			{
				try
				{
					final Class theClass	= ClassUtil.getClassFromName( mClassnames[ i ] );
					
					list.add( theClass );
				}
				catch( ClassNotFoundException e )
				{
					if ( mErrorIfNotFound )
					{
						throw e;
					}
				}
			}
			
			classes	=	new Class[ list.size() ];
			list.toArray( classes );
		}
		
		return( classes );
	}
	
	/**
		Get an array of Classes.
	 */
		public Class[]
	getClasses( )
	{
		Class[]	classes	= null;
		
		try
		{
			classes	= _getClasses( );
		}
		catch( Exception e )
		{
			// OK, call would have exception from previous check if desired
		}
		
		return( classes );
	}
};



