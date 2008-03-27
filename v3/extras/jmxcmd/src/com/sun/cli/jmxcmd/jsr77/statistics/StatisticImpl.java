/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/StatisticImpl.java,v 1.1 2004/10/14 19:06:25 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:25 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.io.Serializable;
import java.util.Map;

import javax.management.j2ee.statistics.Statistic;

import com.sun.cli.jcmd.util.misc.MapUtil;
import com.sun.cli.jmxcmd.util.j2ee.J2EEUtil;


/**
	Implementation of Statistic which records its values in member
	variables.
 */
public class StatisticImpl implements Statistic, Serializable
{
	static final long serialVersionUID = -8120492090789878204L;
	
	/* members names as defined by JSR 77 */
	
	private final String	mName;
	private final String	mDescription;
	private final String	mUnit;
	private final long		mLastSampleTime;
	private final long		mStartTime;
	
		public
	StatisticImpl( 
		final String	name,
		final String	description,
		final String	unit,
		final long		startTime,
		final long		lastSampleTime )
	{
		mName	= name;
		mDescription	= description;
		mUnit	= unit;
		mLastSampleTime	= lastSampleTime;
		mStartTime	= startTime;
	}
	
		public
	StatisticImpl( final Statistic s )
	{
		mName			= s.getName();
		mDescription		= s.getDescription();
		mUnit			= s.getUnit();
		mLastSampleTime	= s.getLastSampleTime();
		mStartTime		= s.getStartTime();
	}
	
	/**
		Get the description for this Statistic
	 */
 		public String
 	getDescription()
 	{
 		return( mDescription );
 	}
 	
	
	/**
		Get the last sample time for this Statistic
	 */
 		public long
 	getLastSampleTime()
 	{
 		return( mLastSampleTime );
 	}
 	
	/**
		Get the name of this Statistic
	 */
		public String
	getName()
	{
 		return( mName );
	}
	
	/**
		Get the start time for this Statistic
	 */
		public long
	getStartTime()
	{
 		return( mStartTime );
	}
	
	
	/**
		Get the units associated with this statistic.
	 */
		public String
	getUnit()
	{
 		return( mUnit );
	}
	
		public String
	toString()
	{
		final Map	m	= J2EEUtil.statisticToMap( this );
		
		return( MapUtil.toString( m, ", " ) );
	}
	
		public boolean
	equals( final Object rhs )
	{
		boolean	equals	= false;
		
		if ( rhs instanceof Statistic )
		{
			final Statistic	s	= (Statistic)rhs;
			
			equals	= getName().equals( s.getName() ) &&
						getUnit().equals( s.getUnit() ) &&
						getDescription().equals( s.getDescription() ) &&
						getStartTime() == s.getStartTime() &&
						getLastSampleTime() == s.getLastSampleTime();
		}
		return( equals );
	}
	
}





