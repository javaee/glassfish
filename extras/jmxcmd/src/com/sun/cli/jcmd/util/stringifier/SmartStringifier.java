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
package com.sun.cli.jcmd.util.stringifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.lang.reflect.Array;


import com.sun.cli.jcmd.util.misc.ClassUtil;
import com.sun.cli.jcmd.util.misc.ArrayConversion;


/**
	Stringifies an Object in the "best" possible way, using the
	StringifierRegistry.DEFAULT registry and/or internal logic.
 */
public final class SmartStringifier implements Stringifier
{
	public static final SmartStringifier	DEFAULT	= new SmartStringifier( "," );
	private final String			mMultiDelim;
	private final boolean			mEncloseArrays;
	protected StringifierRegistry	mRegistry;
	
		public
	SmartStringifier()
	{
		this( "," );
	}
	
		public
	SmartStringifier( String multiDelim )
	{
		this ( multiDelim, true );
	}
	
		public
	SmartStringifier( String multiDelim, boolean encloseArrays )
	{
		this ( StringifierRegistryImpl.DEFAULT, multiDelim, encloseArrays );
	}
	
		public
	SmartStringifier( StringifierRegistry registry, String multiDelim, boolean encloseArrays)
	{
		mMultiDelim		= multiDelim;
		mEncloseArrays	= encloseArrays;
		mRegistry		= registry;
	}
	
		public void
	setRegistry( StringifierRegistry registry )
	{
		mRegistry	= registry;
	}
	
	
	private final static Class [] STRINGIFIER_REGISTRY_LOOKUPS	=
	{
		Iterator.class,
		Collection.class,
		HashMap.class
	};
		private Stringifier
	getStringifier( final Object target )
	{
		if ( target == null )
			return( null );
			
		final Class<?> targetClass	= target.getClass();
		
		Stringifier	stringifier	= mRegistry.lookup( targetClass );
		
		if ( target instanceof javax.management.ObjectName )
		{
		    assert( stringifier != null );
		}
		
		if ( stringifier == null )
		{
			// exact match failed...look for match in defined order
			final int numLookups	= STRINGIFIER_REGISTRY_LOOKUPS.length;
			for( int i = 0; i < numLookups; ++i )
			{
				final Class<?> theClass	= STRINGIFIER_REGISTRY_LOOKUPS[ i ];
				
				stringifier	= mRegistry.lookup( theClass );
				if ( stringifier != null &&
				    theClass.isAssignableFrom( targetClass ) )
				{
					break;
				}
			}
		}
		
		if ( stringifier == null )
		{
			// see if there is a Stringifier for any superclass;
			Class	tempClass	= targetClass;
			while ( tempClass != Object.class )
			{
				stringifier	= mRegistry.lookup( tempClass );
				if ( stringifier != null )
				{
					break;
				}
				
				tempClass	= tempClass.getSuperclass();
			}
			
		}
		
		if ( stringifier == null )
		{
			final Class[]	interfaces	= targetClass.getInterfaces();
			if ( interfaces.length != 0 )
			{
				stringifier	= new InterfacesStringifier( interfaces );
			}
		}
	
		return( stringifier );
	}
	
		private String
	smartStringify( Object target )
	{
		String	result	= null;
		
		if ( ClassUtil.objectIsArray( target ) )
		{
			Object []	theArray	= null;
			
			final Class	elementClass	=
				ClassUtil.getArrayElementClass( target.getClass() );
				
			if ( ClassUtil.IsPrimitiveClass( elementClass ) )
			{
				theArray	= ArrayConversion.toAppropriateType( target );
			}
			else
			{
				theArray	= (Object [])target;
			}
			
			
			result	= ArrayStringifier.stringify( theArray, mMultiDelim, this);
			if ( mEncloseArrays )
			{
				result = "{" + result + "}";
			}
		}
		else
		{
			Stringifier	stringifier	= getStringifier( target );
			
			if ( stringifier != null && stringifier.getClass() == this.getClass() )
			{
				// avoid recursive call to self
				stringifier	= null;
			}
			
			if ( stringifier != null )
			{
				result	= stringifier.stringify( target );
			}
		}
		
		if ( result == null )
		{
			result	= target.toString();
		}

		return( result );
	}

		public static String
	toString( Object target )
	{
		return( DEFAULT.stringify( target ) );
	}
	
		public String
	stringify( Object target )
	{
		if ( target == null )
		{
			return( "<null>" );
		}
		
		return( smartStringify( target ) );
	}
}

