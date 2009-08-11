/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ParseResult.java,v 1.1 2003/11/21 21:23:49 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:49 $
 */
 

 
package com.sun.cli.jmxcmd.support;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
 */
final class ParseResult
{
	final static int	LITERAL_STRING	= 0;
	final static int	OTHER			= 1;
	final static int	ARRAY			= 2;
	
	int				mType;
	String			mTypeCast;
	String			mName;
	Object			mData;
	

	ParseResult( int type, Object data)
	{
		mType		= type;
		mTypeCast	= null;
		mName		= null;
		mData		= data;
	}
	
	ParseResult( int type, Object data, String typecast)
	{
		mType		= type;
		mTypeCast	= typecast;
		mName		= null;
		mData		= data;
	}
	
	ParseResult( int type, Object data, String typecast, String name)
	{
		mType		= type;
		mTypeCast	= typecast;
		mName		= name;
		mData		= data;
	}
	
		public int
	getType()
	{
		return( mType );
	}
	
		public void
	setType( int type )
	{
		mType	= type;
		if ( ! (mData instanceof String) )
		{
			throw new IllegalArgumentException(
				"can't set non-String to type LITERAL_STRING: " + mData.getClass().getName());
		}
	}
	
		public Object
	getData()
	{
		return( mData );
	}
	
		public void
	setData( Object data )
	{
		mData	= data;
	}
	
		public void
	setTypeCast( String typeCast )
	{
		mTypeCast	= typeCast;
	}
	
		public String
	getTypeCast()
	{
		return( mTypeCast );
	}
	
	
		public void
	setName( String name )
	{
		mName	= name;
	}
	
		public String
	getName()
	{
		return( mName );
	}
	
		public String
	toString()
	{
		return( this.toString( ',' ) );
	}
	
		boolean
	equalString( String s1, String s2 )
	{
		if ( s1 == s2 )
			return( true );
		
		// they can't both be null now
		
		if ( s1 != null )
		{
			return( s1.equals( s2 ) );
		}
		
		// s2 != null
		return( s2.equals( s1 ) );
	}
	
		public static boolean
	checkEqualArrays( final ParseResult [] lhs, final ParseResult [] rhs )
	{
		if ( lhs == rhs )
			return( true );
			
		boolean	equal	= lhs.length == rhs.length;
		
		if ( equal )
		{
			for( int i = 0; i < lhs.length; ++i )
			{
				equal	= lhs[ i ].mData.equals( rhs[ i ].mData );
				if ( ! equal )
					break;
			}
		}
		
		return( equal );
	}
	
		public boolean
	equals( final Object o )
	{
		if ( o == this )
			return( true );
		if ( ! (o instanceof ParseResult) )
			return( false );
		
		
		final ParseResult	rhs	= (ParseResult)o;
		
		boolean	dataEqual	= false;
		
		if ( mData instanceof ParseResult [] )
		{
			dataEqual	= checkEqualArrays( (ParseResult [])mData, (ParseResult [])rhs.mData);
		}
		else
		{
			dataEqual	= mData.equals( rhs.mData );
		}
		
		final boolean equal	=
			mType == rhs.mType &&
			dataEqual &&
			equalString( mName, rhs.mName ) &&
			equalString( mTypeCast, rhs.mTypeCast );
		
			
		return( equal );
	}
	
		public String
	toString( final char delim )
	{
		final int 		type	= getType();
		final Object	data	= getData();
		
		String	result	= "";
		
		if ( type == ParseResult.LITERAL_STRING )
		{
			result	= (String)data;
		}
		else if ( type == ParseResult.OTHER )
		{
			result	= (String)data;
		}
		else if ( type == ParseResult.ARRAY )
		{
			final ParseResult []	contents	= (ParseResult [])data;
			
			result	= "{" + ArrayStringifier.stringify( contents, "" + delim ) + "}";
		}
		else
		{
			assert( false );
		}
		
		final String typeCast	= getTypeCast();
		if ( typeCast != null )
		{
			result	= "(" + typeCast + ")" + result;
		}
		
		final String	name	= getName();
		if ( name != null )
		{
			result	= name + "=" + result;
		}
		
		return( result );
	}
}

