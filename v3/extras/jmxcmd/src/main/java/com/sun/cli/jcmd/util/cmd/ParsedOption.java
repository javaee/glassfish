/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/ParsedOption.java,v 1.4 2005/11/08 22:39:19 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:19 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import org.glassfish.admin.amx.util.ArrayUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;


/**
	Retains the value(s) of a parsed option and provides accessor methods
	to get its name and values.
 */
public final class ParsedOption
{
	private final String		mName;
	private final String[]		mValues;
	
	/**
	 */
	ParsedOption( String name, String [] values )
	{
		if ( ! name.startsWith( OptionInfo.SHORT_OPTION_PREFIX ) )
		{
			throw new IllegalArgumentException( "Option name must start with '-'" );
		}
		
		mName	= name;
		mValues	= values;
	}
	
	/**
		Strip the leading "-" or "--" prefix.
	 */
		private String
	trimPrefix( String name )
	{
		String	prefix	= "";
		
		if ( name.startsWith( OptionInfo.LONG_OPTION_PREFIX ) )
		{
			prefix	= OptionInfo.LONG_OPTION_PREFIX;
		}
		else if ( name.startsWith( OptionInfo.SHORT_OPTION_PREFIX ) )
		{
			prefix	= OptionInfo.SHORT_OPTION_PREFIX;
		}
		
		final String	strippedName	= name.substring( prefix.length(), name.length() );
		
		return( strippedName );
	}
	
	/**
		Get the name, but without the leading "-" or "--"
	 */
		public String
	getNoPrefixName()
	{
		return( trimPrefix( mName ) );
	}
	
	/**
		Get the name, including the leading "-" or "--"
	 */
		public String
	getName()
	{
		return( mName );
	}
	
	/**
	 */
		public int
	getNumValues()
	{
		return( mValues.length );
	}
	
	/**
	 */
		public String[]
	getValues()
	{
		return( mValues );
	}
	
		public String
	getValue()
		throws IllegalOptionException
	{
		if ( mValues.length != 1 )
		{
			throw new IllegalOptionException( "expecting to get a single value, not " + mValues.length );
		}

		return( mValues[ 0 ] );
	}
	
	/**
	 */
		public Boolean
	getBoolean( )
		throws IllegalOptionException
	{
		return( new Boolean( getValue( ) ) );
	}
	
	
	/**
	 */
		public Integer
	getInteger( )
		throws IllegalOptionException
	{
		return( new Integer( getValue() ) );
	}
	
	/**
	 */
		public String
	toString()
	{
		return( mName + "=" + ArrayStringifier.stringify( mValues, "," ) );
	}
	
	/**
	 */
		public boolean
	equals( Object rhs )
	{
		if ( ! (rhs instanceof ParsedOption) )
			return( false );
			
		final ParsedOption	other	= (ParsedOption)rhs;
		boolean	equalsSoFar	= mName.equals( other.mName ) &&
								mValues.length == other.mValues.length &&
								ArrayUtil.arraysEqual( mValues, other.mValues );
		
		return( equalsSoFar );
	}
}

	

