/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/MapStatisticImpl.java,v 1.1 2004/10/14 19:06:24 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:24 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.io.Serializable;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

import javax.management.openmbean.CompositeData;

import javax.management.j2ee.statistics.Statistic;

import com.sun.cli.jmxcmd.util.j2ee.J2EEUtil;
import com.sun.cli.jcmd.util.misc.MapUtil;

/**
	Generic implementation of Statistic which contains its members in a Map.
 */
public class MapStatisticImpl implements MapStatistic, Serializable
{
	static final long serialVersionUID = -5921306849633125922L;
	
	final Map	mItems;
	
	/**
	 */
		public
	MapStatisticImpl( final Map map )
	{
		mItems	= new HashMap( map );
	}
	
	/**
	 */
		public
	MapStatisticImpl( final Statistic statistic )
	{
		if ( statistic instanceof MapStatistic )
		{
			mItems	= new HashMap();
			
			mItems.putAll( ((MapStatistic)statistic).asMap() );
		}
		else
		{
			mItems	= J2EEUtil.statisticToMap( statistic );
		}
	}
	
	/**
		Get a Statistic value which is expected to be a Long (long)
	 */
		public final long
	getlong( String name )
	{
		final Object	value	= getValue( name );
		
		if ( ! (value instanceof Long) )
		{
			throw new IllegalArgumentException( 
				"MapStatisticImpl.getLong: expecting Long for " + name +
				", got " + value + " of class " + value.getClass() +
				", all values: " + toString() );
			
		}
			
		return( ((Long)value).longValue() );
	}
	
	/**
		Get a Statistic value which is expected to be an Integer (int)
	 */
		public final int
	getint( String name )
	{
		return( ((Integer)getValue( name )).intValue() );
	}
	
	
	/**
		Get a Statistic value which is expected to be a String
	 */
		public final String
	getString( String name )
	{
		return( (String)getValue( name ) );
	}

	
	/**
		Get a Statistic value which is expected to be any Object
	 */
		public final Object
	getValue( String name )
	{
		final Object	value	= mItems.get( name );
		
		if ( value == null && ! mItems.containsKey( name ) )
		{
			throw new IllegalArgumentException( name );
		}
		
		return( value );
	}
	

	
	/**
		Get the description for this Statistic
	 */
 		public String
 	getDescription()
 	{
 		return( getString( "Description" ) );
 	}
 	
	
	/**
		Get the last sample time for this Statistic
	 */
 		public long
 	getLastSampleTime()
 	{
 		return( getlong( "LastSampleTime" ) );
 	}
 	
	/**
		Get the name of this Statistic
	 */
		public String
	getName()
	{
 		return( getString( "Name" ) );
	}
	
	/**
		Get the name of this Statistic
	 */
		public String
	setName( final String newName )
	{
		if ( newName == null || newName.length() == 0 )
		{
			throw new IllegalArgumentException();
		}
		
		final String	oldName	= getName();
		
		mItems.put( "Name", newName );
		
 		return( oldName  );
	}
	
	/**
		Get the start time for this Statistic
	 */
		public long
	getStartTime()
	{
 		return( getlong( "StartTime" ) );
	}
	
	
	/**
		Get the units associated with this statistic.
	 */
		public String
	getUnit()
	{
 		return( getString( "Unit" ) );
	}
	
	/**
		Get the fields associated with this statistic.
		
		Note the name--"get" is avoided so it won't be introspected
		as another Statistic field.
		
		@return an unmodifiableSet of the field names (String)
	 */
		public Set
	valueNames()
	{
 		return( Collections.unmodifiableSet( mItems.keySet() ) );
	}
	
	
		public Map
	asMap()
	{
		return( Collections.unmodifiableMap( mItems ) );
	}
	
		public String
	toString()
	{
		return( MapUtil.toString( mItems ) );
	}
	
		public boolean
	equals( final Object rhs )
	{
		boolean	equals	= false;
		
		if ( rhs instanceof MapStatistic )
		{
			equals	= MapUtil.mapsEqual( asMap(), ((MapStatistic)rhs).asMap() );
		}
		return( equals );
	}
}





