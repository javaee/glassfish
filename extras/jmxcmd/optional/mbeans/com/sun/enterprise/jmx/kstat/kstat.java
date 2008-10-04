/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/mbeans/com/sun/enterprise/jmx/kstat/kstat.java,v 1.2 2003/11/12 02:07:23 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 02:07:23 $
 */
package com.sun.enterprise.jmx.kstat;

import java.util.HashMap;
import java.util.Set;

import com.sun.cli.jcmd.util.misc.ClassUtil;
import com.sun.cli.jcmd.util.stringifier.SmartStringifier;

public final class kstat
{
	final String				mModuleName;
	final int					mInstanceNumber;
	final String				mkstatName;
	final HashMap				mAttributes;
	
	public static class kstatAttribute
	{
		public final String		mName;
		public final Object		mValue;
		
		
			private static Object
		tryType( Class theClass, String input )
		{
			Object	value	= null;
			
			try
			{
				value	= ClassUtil.InstantiateFromString( theClass, input );
			}
			catch( Exception e )
			{
				// ignore
			}
			return( value );
		}
		
			private static Object
		createValue( String input )
		{
			Object	value	= tryType( Long.class, input );
			if ( value == null )
			{
				value	= tryType( Double.class, input );
			}
			if ( value == null )
			{
				value	= input;
			}
			
			return( value );
		}
		
			public
		kstatAttribute( String name, String value )
		{
			mName	= name;
			mValue	= createValue( value );
		}
			public String
		toString()
		{
			return( mName + "=" + mValue );
		}
	};
	
		public
	kstat(
		String				moduleName,
		int					instanceNumber,
		String				kstatName )
	{
		mModuleName		= moduleName;
		mkstatName		= kstatName;
		mInstanceNumber	= instanceNumber;
		mAttributes		= new HashMap();
	}
		public void
	addAttribute( kstatAttribute attr )
	{
		mAttributes.put( attr.mName, attr );
	}
	
		String
	getModuleName()
	{
		return( mModuleName );
	}
	
		int
	getInstanceNumber()
	{
		return( mInstanceNumber );
	}
	
		String
	getName()
	{
		return( mkstatName );
	}
	
		Class
	getAttributeType( String name )
	{
		final Object	value	= getValue( name );
		return( value.getClass() );
	}
	
		String
	getScopedName( char delim )
	{
		return( getScopedName( mModuleName, mInstanceNumber, mkstatName, delim ) );
	}
	
		String
	getScopedName(  )
	{
		return( getScopedName( ':' ) );
	}
	
		public static String
	getScopedName( String moduleName, int instanceNumber, String name, char delim)
	{
		return( moduleName + delim + instanceNumber + delim + name );
	}
	
		public static String
	getScopedName( String moduleName, int instanceNumber, String name )
	{
		return( getScopedName( moduleName, instanceNumber, name, ':' ) );
	}
	
		Set
	getAttributeNames()
	{
		return( mAttributes.keySet() );
	}

		public Object
	getValue( String attributeName )
	{
		final kstatAttribute	attr	= (kstatAttribute)mAttributes.get( attributeName );
		
		Object	value	= null;
		if ( attr != null )
		{
			value	= attr.mValue;
		}
		
		return( value );
	}
	
		public String
	toString()
	{
		return( getScopedName( ':' ) + "\n" + SmartStringifier.toString( mAttributes ) );
	}
};


