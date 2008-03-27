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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/GSetUtil.java,v 1.1 2005/11/08 22:39:22 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:39:22 $
 */
 
package com.sun.cli.jcmd.util.misc;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Arrays;


/**
    Utilities for working with sets using JDK 1.5 generics.
 */
public final class GSetUtil
{
		private
	GSetUtil( )
	{
		// disallow instantiation
	}
	
		public static <T> T
	getSingleton( final Set<? extends T> s )
	{
		if ( s.size() != 1 )
		{
			throw new IllegalArgumentException( s.toString() );
		}
		return( s.iterator().next() );
	}
	
		public static <T> void
	addArray(
		final Set<T>	set,
		final T[]	    array )
	{
		for( final T item : array )
		{
			set.add( item );
		}
	}
	
		public static <T> Set<T>
	newSet( final Collection<? extends T> c )
	{
		final HashSet<T>	set	= new HashSet<T>();
		
		set.addAll( c );
		
		return( set );
	}
	
	
	/**
		Create a new Set with one member.
	 */
		public static <T> Set<T>
	newSet( final T item )
	{
		final Set<T>	set	= new HashSet<T>();
		set.add( item );
		
		return( set );
	}
	
	/**
		Create a new Set containing all members of another.
		The returned Set is always a HashSet.
	 */
		public static <T> HashSet<T>
	copySet( final Set<? extends T> s1 )
	{
		final HashSet<T>	set	= new HashSet<T>();
		
		set.addAll( s1 );
		
		return( set );
	}
	
	
		public static <T> Set<? extends T>
	newSet(
		final T m1,
		final T m2 )
	{
		final HashSet<T>	set	= new HashSet<T>();
		
		set.add( m1 );
		set.add( m2 );
		
		return( set );
	}
	
	/*
		public static <T> Set<T>
	newSet(
		final T m1,
		final T m2, 
		final T m3 )
	{
		final HashSet<T>	set	= new HashSet<T>();
		
		set.add( m1 );
		set.add( m2 );
		set.add( m3 );
		
		return( set );
	}
	*/
	
		public static <T> Set<T>
	newSet(
		final T m1,
		final T m2, 
		final T m3, 
		final T m4 )
	{
		final HashSet<T>	set	= new HashSet<T>();
		
		set.add( m1 );
		set.add( m2 );
		set.add( m3 );
		set.add( m4 );
		
		return( set );
	}
	
	
	/**
		Create a new Set containing all array elements.
	 */
		public static <T> Set<T>
	newSet( final T[]  objects )
	{
		return( newSet( objects, 0, objects.length ) );
	}
	
		public static <T> Set<T>
	newSet( final Set<T> s1, final Set<T> s2 )
	{
	    final Set<T>    both    = new HashSet<T>();
	    both.addAll( s1 );
	    both.addAll( s2 );
	    
	    return both;
	}
	


	/**
		Create a new Set containing all array elements.
	 */
		public static <T> Set<T>
	newSet(
		final T[]   objects,
		final int   startIndex,
		final int   numItems )
	{
		final Set<T>	set	= new HashSet<T>();
		
		for( int i = 0; i < numItems; ++i )
		{
			set.add( objects[ startIndex + i ] );
		}

		return( set );
	}
	
    /**
		Convert a Set to a String[]
	 */
		public static String[]
	toStringArray( final Set<?>	s )
	{
		final String[]	strings	= new String[ s.size() ];
		
		int	i = 0;
		for( final Object o : s )
		{
			strings[ i ]	= "" + o;
			++i;
		}
		
		return( strings );
	}
	
		public static String[]
	toSortedStringArray( final Set<?>	s )
	{
		final String[]	strings	= toStringArray( s );
		
		Arrays.sort( strings );
		
		return( strings );
	}
	
	    public static Set<String>
	newStringSet( final String... args)
	{
	    final Set<String>   set   = new HashSet<String>();
	    
	    for( final String s : args )
	    {
	        set.add( s );
	    }
	    return set;
	}
	
	
	    public static Set<String>
	newUnmodifiableStringSet( final String... args)
	{
	    return Collections.unmodifiableSet( newStringSet( args ) );
	}
	
	    public static Set<String>
	newStringSet( final Object... args)
	{
	    final Set<String>   set   = new HashSet<String>();
	    
	    for( final Object o : args )
	    {
	        set.add( o == null ? null : "" + o );
	    }
	    return set;
	}
}























