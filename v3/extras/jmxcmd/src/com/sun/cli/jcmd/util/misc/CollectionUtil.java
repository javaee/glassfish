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

import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
import java.lang.reflect.Array;

import com.sun.cli.jcmd.util.stringifier.SmartStringifier;

/**
	Various helper utilities for Collections.
 */
public final class CollectionUtil
{
		private
	CollectionUtil( )
	{
		// disallow instantiation
	}
	
	
	/**
		@return a String
	 */
		public static String
	toString(
		final Collection c,
		final String	 delim )
	{
	    final String[]  strings   = toStringArray( c );
	    Arrays.sort( strings );
	    
		return StringUtil.toString( delim, (Object[])strings );
	}
	
		public static String
	toString( final Collection c )
	{
	    return toString( c, ", " );
	}

	
		public static <T> T
	getSingleton( final Collection<T> s )
	{
		if ( s.size() != 1 )
		{
			throw new IllegalArgumentException();
		}
		return( s.iterator().next() );
	}
	
	/**
		Add all items in an array to a set.
	 */
		public static <T extends Object> void
	addArray(
		final Collection<T>	c,
		final T[]		array )
	{
		for( int i = 0; i < array.length; ++i )
		{
			c.add( array[ i ] );
		}
	}
	
	/**
		@return String[]
	 */
		public static String[]
	toStringArray( final Collection	c )
	{
		final String[]	strings	= new String[ c.size() ];
		
		int	i = 0;
		for( final Object o : c )
		{
			strings[ i ]	= SmartStringifier.toString( o );
			++i;
		}
		
		return( strings );
	}
	
	/**
		@param c	the Collection
		@param elementClass	 the type of the element, must be non-primitive
		@return array of <elementClass>[] elements
	 */
		public static <T> Object[]
	toArray(
		final Collection<? extends T> c,
		final Class<T>	             elementClass )
	{
		final Object[] items = (Object[])Array.newInstance( elementClass, c.size() );
		
		c.toArray( items );
		
		return( items );
	}
	
	/**
		@return Object[]
	 */
		public static Object[]
	toArray( final Collection<?>	set )
	{
		final Object[]	names	= new Object[ set.size() ];
		set.toArray( names );
		
		return( names );
	}
}

