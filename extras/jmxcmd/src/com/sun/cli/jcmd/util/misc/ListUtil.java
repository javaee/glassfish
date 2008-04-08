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
 */
 
package com.sun.cli.jcmd.util.misc;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public final class ListUtil
{
		private
	ListUtil( )
	{
		// disallow instantiation
	}
	
	/**
		Add all items in an array to a list.
	 */
		public static <T> void
	addArray(
		final List<T>		list,
		final T[]	array )
	{
		for( int i = 0; i < array.length; ++i )
		{
			list.add( array[ i ] );
		}
	}
	
	/**
		Convert a List to a String[]
	 */
		public static String[]
	toStringArray( final List	list )
	{
		final String[]	names	= new String[ list.size() ];
		
		int i   = 0;
		for( final Object o : list )
		{
		    names[ i ] = "" + o;
		    ++i;
		}
		
		return( names );
	}
	
	/**
		Create a new List from a Collection
	 */
		public static <T> List<T>
	newListFromCollection( final Collection<T> c )
	{
		final ArrayList<T>	list	= new ArrayList<T>();
		
		list.addAll( c );
		
		return( list );
	}
	
	/**
		Create a new List from a Collection
	 */
		public static <T> List<T>
	newListFromIterator( final Iterator<T> iter )
	{
		final List<T>	list	= new ArrayList<T>();
		
		while ( iter.hasNext() )
		{
			list.add( iter.next() );
		}
		
		return( list );
	}
	
	/**
		Create a new List with one member.
	 */
		public static <T> List<T>
	newList( final T m1 )
	{
		final List<T>	list	= new ArrayList<T>();
		
		list.add( m1 );
		
		return( list );
	}
	
	/**
		Create a new List with two members.
	 */
		public static <T> List<T>
	newList(
		final T m1,
		final T m2 )
	{
		final List<T>	list	= new ArrayList<T>();
		
		list.add( m1 );
		list.add( m2 );
		
		return( list );
	}
	
	
	/**
		Create a new List with three members.
	 */
		public static <T> List<T>
	newList(
		final T m1,
		final T m2, 
		final T m3 )
	{
		final List<T>	list	= new ArrayList<T>();
		
		list.add( m1 );
		list.add( m2 );
		list.add( m3 );
		
		return( list );
	}
	
	/**
		Create a new List with four members.
	 */
		public static <T> List<T>
	newList(
		final T m1,
		final T m2, 
		final T m3, 
		final T m4 )
	{
		final List<T>	list	= new ArrayList<T>();
		
		list.add( m1 );
		list.add( m2 );
		list.add( m3 );
		list.add( m4 );
		
		return( list );
	}
	
	/**
		Create a new List with four members.
	 */
		public static <T> List<T>
	newList(
		final T m1,
		final T m2, 
		final T m3, 
		final T m4, 
		final T m5 )
	{
		final List<T>	list	= new ArrayList<T>();
		
		list.add( m1 );
		list.add( m2 );
		list.add( m3 );
		list.add( m4 );
		list.add( m5 );
		
		return( list );
	}
	
	

		public static <T> List<T>
	newListFromArray( final T []  items )
	{
		final List<T>	list	= new ArrayList<T>();
		
		for( int i = 0; i < items.length; ++i )
		{
			list.add( items[ i ] );
		}

		return( list );
	}

	/**
		Return a new List in reverse order. Because the List is new,
		it works on any list, modifiable or not.
	 */
		public static <T> List<T>
	reverse( final List<T> list )
	{
		final int	numItems	= list.size();
		final List<T>	result		= new ArrayList<T>( numItems );
		
		for( int i = 0; i < numItems; ++i )
		{
			result.add( list.get( numItems - i -1 ) );
		}
		
		return( result );
	}

}

