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
package com.sun.appserv.management.util.misc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
    @deprecated use GSetUtil
 */
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
	toArray( final Set<?>	set )
	{
		final Object[]	names	= new Object[ set.size() ];
		set.toArray( names );
		
		return( names );
	}
	
	/**
		Create a new Set with one member.
	 */
		public static <T> Set<T>
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
		Create a new Set containing all array elements.
	 */
		public static <T> Set<T>
	newSet( final Set<? extends T>[]  sets )
	{
		final Set<T>	s	= new HashSet<T>();
		
		for( int i = 0; i < sets.length; ++i )
		{
			s.addAll( sets[ i ] );
		}

		return( s );
	}
	 
	 /*
	 private void
    doIt()
    {
        final Set<String>   strings  = new HashSet<String>();
        
        *
        final Set<String>[] stringSets    = new Set[] { strings, strings};
        final Set<String>   test    = newSet( stringSets );
        
        final Set<Integer>  n1  = new HashSet<Integer>();
        final Set<Float>    n2  = new HashSet<Float>();
        final Set<Number>[] numberSets = new Set[] { n1, n2 };
        final Set<Number>   nn  = newSet( numberSets );
        
        final Set<? extends java.io.Serializable>   x   = newSet(s1, n1);
        
        *
        final Set<String>   xx   = newSet(strings, strings);
        final Set<String>   xxx   = newSet(strings, strings, strings);
        final Set<String>   xxxx   = newSet(strings, strings, strings, strings);
        
        final Set<String>  aa = newSet( "1", "2" );
        final Set<String>  aaa = newSet( "1", "2", "3" );
       // final Set<String>  aaaa = newSet( "1", "2", "3", new Integer(10) );
        
        
        final Set<String>  zzzz = newSet( "1", "2", "3", "4", "6", "7");
    }
    */
   
	
	
	/**
		Create a new Set consisting of the contents of two sets.
	 */
		public static <T> Set<T>
	newSet(
		final Set<? extends T> 	s1,
		final Set<? extends T>	s2)
	{
	    final Set<T>    result  = new HashSet<T>();
	    result.addAll( s1 );
	    result.addAll( s2 );
	    
		return result;
	}
	
	/**
		Create a new Set consisting of the contents of three sets.
	 */
		public static <T> Set<T>
	newSet(
		final Set<? extends T> 	s1,
		final Set<? extends T>	s2,
		final Set<? extends T>	s3 )
	{
	    return newSet( newSet( s1, s2 ), s3 );
	}
	
	/**
		Create a new Set consisting of the contents of four sets.
	 */
		public static <T> Set<T>
	newSet(
		final Set<? extends T> 	s1,
		final Set<? extends T>	s2,
		final Set<? extends T>	s3,
		final Set<? extends T>	s4 )
	{
	    return newSet( newSet( s1, s2 ), newSet( s3, s4) );
	}

		public static Set<String>
	newUnmodifiableSet( final String[]  objects )
	{
		return GSetUtil.newUnmodifiableStringSet( objects );
	}
	
	/**
		Create a new Set containing all array elements.
	 */
		public static <T> Set<T>
	newSet( final T...  objects )
	{
		return( newSet( objects, 0, objects.length ) );
	}


	/**
		Create a new Set containing all array elements.
	 */
		public static <T> Set<T>
	newSet(
		final T []  objects,
		final int	startIndex,
		final int	numItems )
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
		public static <T> Set<T>
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
		public static <T> Set<T>
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
		public static <T> Set<T>
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

