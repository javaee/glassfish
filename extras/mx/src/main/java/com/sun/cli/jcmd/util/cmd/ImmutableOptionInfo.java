/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jcmd.util.cmd;

import java.util.Set;
import java.util.Collections;

		
/**
	Internal class used to keep information about the options.
 */
public final class ImmutableOptionInfo implements OptionInfo
{
	final OptionInfo	mInfo;
	
	public String	getLongName()		{	return( mInfo.getLongName() ); }
	public String	getShortName()		{	return( mInfo.getShortName() ); }
	public int		getNumValues()		{	return( mInfo.getNumValues() ); }
	public String[]	getValueNames()		{	return( mInfo.getValueNames() ); }
	public boolean	isBoolean()			{	return( mInfo.isBoolean() ); }
	public boolean	isRequired()		{	return( mInfo.isRequired() ); }
	public boolean	matches( String name )	{	return( mInfo.matches( name ) ); }
	public String	toString()				{	return( mInfo.toString() ); }
	public String	toDisplayString()		{	return( mInfo.toDisplayString() ); }
	public boolean	equals(	Object rhs )	{	return( mInfo.equals( rhs ) ); }
	
	/**
		Create a new immutable wrapper around the specified OptionInfo.
		
		@param info		the wrapped info
	 */
		public
	ImmutableOptionInfo( OptionInfo info )
	{
		mInfo	= info;
	}
	
	public java.util.Set<String>	getSynonyms()
	{
		return( Collections.unmodifiableSet( mInfo.getSynonyms() ) );
	}
	
		public void
	addDependency( OptionDependency 	dependency )
	{
		throw new IllegalArgumentException( "Attempt to modify immutable OptionInfo" );
	}
	
		public Set<OptionDependency>
	getDependencies(  )
	{
		return( Collections.unmodifiableSet( mInfo.getDependencies() ) );
	}
}


