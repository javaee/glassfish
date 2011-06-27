/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.util;

import java.lang.reflect.Array;

/*
	Provides methods to convert arrays of primitive types to corresponding
	arrays of Object types.
 */
public final class ArrayConversion
{
		private
	ArrayConversion( )
	{
		// disallow instantiation
	}
	
		private static Object []
	convert( Object simpleArray )
	{
		final String className	= simpleArray.getClass().getName();
		
		//final String	memberClassName	= ClassUtil.getArrayMemberClassName( className );
		
		final Class		theClass = ClassUtil.getArrayElementClass( simpleArray.getClass() );
		
		final int numItems	= Array.getLength( simpleArray );
		
		final Class elementClass	= ClassUtil.PrimitiveClassToObjectClass( theClass );
		
		final Object []	result	= (Object [])Array.newInstance( elementClass, numItems );
		
		for( int i = 0; i < numItems; ++i )
		{
			result[ i ]	= Array.get( simpleArray, i );
		}
		
		return( result );
	}
	
		public static Object []
	toAppropriateType( Object array)
	{
		return( (Object [])convert( array ) );
	}
	
	
		public static Boolean []
	toBooleans( boolean [] array )
	{
		return( (Boolean [])convert( array ) );
	}
	
		public static Character []
	toCharacters( char [] array )
	{
		return( (Character [])convert( array ) );
	}
	
		public static Byte []
	toBytes( byte [] array )
	{
		return( (Byte [])convert( array ) );
	}
	
		public static Short []
	toShorts( short [] array )
	{
		return( (Short [])convert( array ) );
	}
	
		public static Integer []
	toIntegers( int [] array )
	{
		return( (Integer [])convert( array ) );
	}
	
		public static Long []
	toLongs( long [] array )
	{
		return( (Long [])convert( array ) );
	}
	
		public static Float []
	toFloats( float [] array )
	{
		return( (Float [])convert( array ) );
	}
	
		public static Double []
	toDoubles( double [] array )
	{
		return( (Double [])convert( array ) );
	}
	
	
		public static Object []
	createObjectArrayType( final Class elementType, final int size )
		throws Exception
	{
		final Object [] result	= (Object []) Array.newInstance( elementType, size );
		
		return( result );
	}
	
		
		public static java.util.Set
	toSet( Object []	array )
	{
		java.util.Set	theSet	= null;
		if ( array.length == 0 )
		{
			theSet	= java.util.Collections.EMPTY_SET;
		}
		else if ( array.length == 1 )
		{
			theSet	= java.util.Collections.singleton( array[ 0 ] );
		}
		else
		{
			theSet	= new java.util.HashSet();
			for( int i = 0; i < array.length; ++i )
			{
				theSet.add( array[ i ] );
			}
		}
		return( theSet );
	}
	
	
		public static Object []
	setToArray( final java.util.Set s )
	{
		final java.util.Iterator	iter	= s.iterator();
		
		final Object []	out	= new Object [ s.size() ];
		
		return( setToArray( s, out ) );
	}
	
		public static Object []
	setToArray( final java.util.Set s, Object []	out )
	{
		final java.util.Iterator	iter	= s.iterator();
		
		if ( out.length != s.size() )
		{
			throw new IllegalArgumentException();
		}
		
		int	i = 0;
		while ( iter.hasNext() )
		{
			out[ i ]	= iter.next();
			++i;
		}
		
		return( out );
	}
}

