/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/RangeStatisticImpl.java,v 1.1 2004/10/14 19:06:24 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:24 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Set;
import java.util.Map;
import java.io.Serializable;

import javax.management.openmbean.CompositeData;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.RangeStatistic;

import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;

/**
	
 */
public class RangeStatisticImpl extends StatisticImpl
	implements RangeStatistic, Serializable
{
	static final long serialVersionUID = 6530146323113291603L;
	
	/* member names as defined by JSR 77 */
	private final long	Current;
	private final long	HighWaterMark;
	private final long	LowWaterMark;
	
	
		public
	RangeStatisticImpl(
		final String	name,
		final String	description,
		final String	unit,
		final long		startTime,
		final long		lastSampleTime,
		final long		low,
		final long		current,
		final long		high )
	{
		super( name, description, unit, startTime, lastSampleTime );
		
		if ( current < low || current > high )
		{
			throw new IllegalArgumentException();
		}
		
		Current	= current;
		HighWaterMark	= high;
		LowWaterMark	= low;
	}
	
		public
	RangeStatisticImpl( final CompositeData compositeData )
	{
		this( OpenMBeanUtil.compositeDataToMap( compositeData ) );
	}
	
		public
	RangeStatisticImpl( final Map<String,Object> m )
	{
		this( new MapStatisticImpl( m ) );
	}
	
		public
	RangeStatisticImpl( final RangeStatistic s )
	{
		super( s );
		
		Current			= s.getCurrent();
		LowWaterMark	= s.getHighWaterMark();
		HighWaterMark	= s.getLowWaterMark();
	}
	
	
		public
	RangeStatisticImpl( final MapStatistic s )
	{
		super( s );
		
		Current			= s.getlong( "Current" );
		LowWaterMark	= s.getlong( "LowWaterMark" );
		HighWaterMark	= s.getlong( "HighWaterMark" );
	}


 		public long
 	getCurrent()
 	{
 		return( Current );
 	}

 		public long
 	getHighWaterMark()
 	{
 		return( HighWaterMark );
 	}

 		public long
 	getLowWaterMark()
 	{
 		return( LowWaterMark );
 	}
}





