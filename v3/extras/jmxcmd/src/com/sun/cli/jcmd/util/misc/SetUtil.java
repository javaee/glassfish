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

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;


public final class SetUtil
{
		private
	SetUtil( )
	{
		// disallow instantiation
	}
	
		public static <T> T
	getSingleton( final Set<T> s )
	{
		if ( s.size() != 1 )
		{
			throw new IllegalArgumentException( s.toString() );
		}
		return( s.iterator().next() );
	}
	
	/**
		Add all items in an array to a set.
	 */
		public static <T> void
	addArray(
		final Set<T>	set,
		final T[]	    array )
	{
		for( int i = 0; i < array.length; ++i )
		{
			set.add( array[ i ] );
		}
	}
	
	/**
		Convert a Set to a String[]
	 */
		public static String[]
	toStringArray( final Set	set )
	{
		final String[]	strings	= new String[ set.size() ];
		
		final Iterator	iter	= set.iterator();
		int	i = 0;
		while ( iter.hasNext() )
		{
			strings[ i ]	= iter.next().toString();
			++i;
		}
		
		return( strings );
	}
	
	/**
		Convert a Set to a String[]
	 */
		public static Object[]
	toArray( final Set<Object>	set )
	{
		final Object[]	names	= new Object[ set.size() ];
		set.toArray( names );
		
		return( names );
	}
	
	/**
		Create a new Set with one member.
	 */
		public static <T extends Object> Set<T>
	newSet( final Collection<T> c )
	{
		final Set<T>	set	= new HashSet<T>();
		
		set.addAll( c );
		
		return( set );
	}
	
	
	
	
	/**
		Create a new Set with one member.  Additional items
		may be added.
	 */
		public static <T> Set<T>
	newSingletonSet( final T m1 )
	{
		final Set<T>	set	= new HashSet<T>();
		
		set.add( m1 );
		
		return( set );
	}
	
	/**
		Create a new Set containing all members of another.
		The returned Set is always a HashSet.
	 */
		public static <T> HashSet<T>
	copySet( final Set<T> s1 )
	{
		final HashSet<T>	set	= new HashSet<T>();
		
		set.addAll( s1 );
		
		return( set );
	}
	
	
	 
	 
	/**
		Create a new Set consisting of the contents of two sets.
	 */
		public static <T extends Object> Set<T>
	newSet(
		final Set<T> 	s1,
		final Set<T>	s2)
	{
		return( (Set<T>)newSet( s1, s2) );
	}
	
	/**
		Create a new Set consisting of the contents of three sets.
	 */
		public static <T extends Object> Set<T>
	newSet(
		final Set<T> 	s1,
		final Set<T>	s2,
		final Set<T>	s3 )
	{
		return( newSet( s1, s2, s3 ) );
	}
	
	/**
		Create a new Set consisting of the contents of four sets.
	 */
		public static <T extends Object> Set<T>
	newSet(
		final Set<T> 	s1,
		final Set<T>	s2,
		final Set<T>	s3,
		final Set<T>	s4 )
	{
		return( newSet( s1, s2, s3, s4 ) );
	}
	
	
	/**
		Create a new Set containing all array elements.
	 */
		public static <T extends Object> Set<T>
	newSet( final Set<T>...  sets )
	{
		final Set<T>	s	= new HashSet<T>();
		
		for( int i = 0; i < sets.length; ++i )
		{
			s.addAll( sets[ i ] );
		}

		return( s );
	}
	
	/**
		Create a new Set with two members.
	 */
		public static Set<Object>
	newSet(
		final Object m1,
		final Object m2 )
	{
		final Set<Object>	set	= new HashSet<Object>();
		
		set.add( m1 );
		set.add( m2 );
		
		return( set );
	}
	
	
	/**
		Create a new Set with three members.
	 */
		public static Set<Object>
	newSet(
		final Object m1,
		final Object m2, 
		final Object m3 )
	{
		final Set<Object>	set	= new HashSet<Object>();
		
		set.add( m1 );
		set.add( m2 );
		set.add( m3 );
		
		return( set );
	}
	
	/**
		Create a new Set with four members.
	 */
		public static Set<Object>
	newSet(
		final Object m1,
		final Object m2, 
		final Object m3, 
		final Object m4 )
	{
		final Set<Object>	set	= new HashSet<Object>();
		
		set.add( m1 );
		set.add( m2 );
		set.add( m3 );
		set.add( m4 );
		
		return( set );
	}
	
		public static <T extends Object> Set<T>
	newUnmodifiableSet( final T[]  objects )
	{
		return Collections.unmodifiableSet( newSet( objects, 0, objects.length ) );
	}
	
	/**
		Create a new Set containing all array elements.
	 */
		public static <T extends Object> Set<T>
	newSet( final T[]  objects )
	{
		return( newSet( objects, 0, objects.length ) );
	}


	/**
		Create a new Set containing all array elements.
	 */
		public static <T extends Object> Set<T>
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
		Return a new Set of all items in both set1 and set2.
	 */
		public static <T extends Object> Set<T>
	intersectSets(
		final Set<T>	set1,
		final Set<T> set2 )
	{
		final Set<T>	result	= SetUtil.newSet( set1 );
		result.retainAll( set2 );
		
		return( result );
	}
	
	/**
		Return a new Set of all items in set1 not in set2.
	 */
		public static <T extends Object>  Set<T>
	removeSet(
		final Set<T>	 set1,
		final Set<T>   set2 )
	{
		final Set<T>	result	= SetUtil.newSet( set1 );
		result.removeAll( set2 );
		
		return( result );
	}
	
	/**
		Return a new Set of all items not common to both sets.
	 */
		public static <T extends Object> Set<T>
	newNotCommonSet(
		final Set<T> set1,
		final Set<T> set2 )
	{
		final Set<T>	result	= SetUtil.newSet( set1, set2 );
		final Set<T>	common	= intersectSets( set1, set2);
		
		result.removeAll( common );
		
		return( result );
	}

}

