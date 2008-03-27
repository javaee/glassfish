/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/TimeStatisticImpl.java,v 1.1 2004/10/14 19:06:26 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:26 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Map;
import java.io.Serializable;

import javax.management.openmbean.CompositeData;

import javax.management.j2ee.statistics.TimeStatistic;

import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;


/**
	
 */
public final class TimeStatisticImpl
	extends StatisticImpl implements TimeStatistic, Serializable
{
	static final long serialVersionUID = 1090185734375468511L;
	
	/* member names as defined by JSR 77 */
	private final long	Count;
	private final long	MinTime;
	private final long	MaxTime;
	private final long	TotalTime;
	
		public
	TimeStatisticImpl( final CompositeData compositeData )
	{
		this( OpenMBeanUtil.compositeDataToMap( compositeData ) );
	}
	
		public
	TimeStatisticImpl( final Map m )
	{
		this( new MapStatisticImpl( m ) );
	}
	
		public
	TimeStatisticImpl( final TimeStatistic s )
	{
		super( s );
		
		Count		= s.getCount();
		MinTime		= s.getMinTime();
		MaxTime		= s.getMaxTime();
		TotalTime	= s.getTotalTime();
	}
	
		public
	TimeStatisticImpl( final MapStatistic s )
	{
		super( s );
		
		Count		= s.getlong( "Count" );
		MinTime		= s.getlong( "MinTime" );
		MaxTime		= s.getlong( "MaxTime" );
		TotalTime	= s.getlong( "TotalTime" );
	}
	
		public
	TimeStatisticImpl(
		final String	name,
		final String	description,
		final String	unit,
		final long		startTime,
		final long		lastSampleTime,
		final long		count,
		final long		maxTime,
		final long		minTime,
		final long		totalTime )
	{
		super( name, description, unit, startTime, lastSampleTime );
		Count		= count;
		MaxTime		= maxTime;
		MinTime		= minTime;
		TotalTime	= totalTime;
	}


 		public long
 	getCount()
 	{
 		return( Count );
 	}

 		public long
 	getMaxTime()
 	{
 		return( MaxTime );
 	}

 		public long
 	getMinTime()
 	{
 		return( MinTime );
 	}

 		public long
 	getTotalTime()
 	{
 		return( TotalTime );
 	}
}





