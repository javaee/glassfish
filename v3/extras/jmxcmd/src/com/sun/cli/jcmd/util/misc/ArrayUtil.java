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

import java.lang.reflect.Array;
import com.sun.cli.jcmd.util.misc.ClassUtil;

/**
	Provides:
	- utility to check for equality
 */
public final class ArrayUtil
{
		private
	ArrayUtil( )
	{
		// disallow instantiation
	}
	
		public static boolean
	arraysEqual( Object array1, Object array2 )
	{
		boolean	equal	= array1 == array2;
		
		if ( equal )
		{
			// same object or both null
			return( true );
		}
		else if ( array1 == null || array2 == null)
		{
			return( false );
		}
		

		if ( array1.getClass() == array2.getClass() &&
			ClassUtil.objectIsArray( array1 ) &&
			Array.getLength( array1 ) == Array.getLength( array2 ) )
		{
			equal	= true;
			final int	length	= Array.getLength( array1 );
			
			for( int i = 0; i < length; ++ i )
			{
				final Object	a1	= Array.get( array1, i );
				final Object	a2	= Array.get( array2, i );
				
				if ( a1 != a2)
				{
					if ( a1 == null || a2 == null )
					{
						equal	= false;
					}
					else if ( ClassUtil.objectIsArray( a1 )  )
					{
						if ( ! arraysEqual( a1, a2 ) )
						{
							equal	= false;
						}
					}
				}

				if ( ! equal )
					break;
			}
		}
		
		return( equal );
	}
	

		public static boolean
	arrayContainsNulls( Object[] array )
	{
		boolean	containsNulls	= false;
		
		for( int i = 0; i < array.length; ++i )
		{
			if ( array[ i ] == null )
			{
				containsNulls	= true;
				break;
			}
		}
		
		return( containsNulls );
	}
	

	
	/**
		Create a new array from the original.
		
		@param items		the original array
		@param startIndex	index of the first item
		@param numItems		
		@return an array of the same type, containing numItems items
	 */
		public static Object[]
	newArray(
		final Object[]	items,
		final int		startIndex,
		final int		numItems )
	{
		final Class	theClass	= ClassUtil.getArrayElementClass( items.getClass() );
		
		final Object[]	result	= (Object[])Array.newInstance( theClass, numItems );
		System.arraycopy( items, 0, result, startIndex, numItems );
		
		return( result );
	}
	
	/**
		Create a new array consisting of originals and new.
		
		@param items1		1st array
		@param items2		2nd array
		@return an array of the same type as items1, its elements first
	 */
		public static Object[]
	newArray(
		final Object[]	items1,
		final Object[]	items2)
	{
		final Class<?>	class1	= ClassUtil.getArrayElementClass( items1.getClass() );
		final Class<?>	class2	= ClassUtil.getArrayElementClass( items2.getClass() );
		
		if ( class1.isAssignableFrom( class2 ) )
		{
			throw new IllegalArgumentException();
		}
		
		final int	length1	= Array.getLength( items1 );
		final int	length2	= Array.getLength( items2 );
		final Object[]	result	=
		    (Object[])Array.newInstance( class1, length1 + length2 );
		System.arraycopy( items1, 0, result, 0, length1 );
		System.arraycopy( items2, 0, result, length1, length2 );
		
		return( result );
	}
	
	/**
		Create a new array consisting of an original array, and a single new item.
		
		@param items		an array
		@param item		an item to append
		@return an array of the same type as items1, its elements first
	 */
		public static Object[]
	newArray(
		final Object[]	items,
		final Object	item)
	{
		final Class	theClass	= ClassUtil.getArrayElementClass( items.getClass() );
		final Object[]	result	= (Object[])Array.newInstance( theClass, items.length + 1 );
		System.arraycopy( items, 0, result, 0, items.length );
		
		result[ result.length - 1 ]	= item;
		return( result );
	}
}

























