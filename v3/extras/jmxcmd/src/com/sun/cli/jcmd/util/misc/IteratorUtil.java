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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public final class IteratorUtil
{
		private
	IteratorUtil( )
	{
		// disallow instantiation
	}
	
	
		public static Object[]
	toArray( final Iterator<?> iter)
	{
		final List<Object>	list	= new ArrayList<Object>();
		
		while ( iter.hasNext() )
		{
			final Object	elem	= iter.next();
			list.add( elem );
		}
		
		final Object[]	result	= new Object[ list.size() ];
		list.toArray( result );
		
		return( ArrayConversion.specializeArray( result ) );
	}
	
		public static Class
	getUniformClass( final Iterator<?> iter)
	{
		Class	theClass	= null;
		
		if ( iter.hasNext() )
		{
			theClass	= iter.next().getClass();
		}

		while ( iter.hasNext() )
		{
			if ( iter.next().getClass() != theClass )
			{
				theClass	= null;
				break;
			}
		}
		
		return( theClass );
	}
	
		public static <T extends Object> boolean
	isUniformClass(
		final Iterator<Class<?>>	iter,
		final Class<T>	theClass,
		final boolean	exactMatch )
	{
		boolean	isUniform	= true;
		
		while ( iter.hasNext() )
		{
			final Class	nextClass	= iter.next().getClass();
			
			if ( nextClass != theClass )
			{
				if ( exactMatch )
				{
					isUniform	= false;
					break;
				}
				
				if ( ! theClass.isAssignableFrom( nextClass ) )
				{
					isUniform	= false;
					break;
				}
			}
		}
		
		return( isUniform );
	}
}

