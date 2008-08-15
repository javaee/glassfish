/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/BoundaryStatisticImpl.java,v 1.1 2004/10/14 19:06:22 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:22 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Map;
import java.io.Serializable;

import javax.management.openmbean.CompositeData;
import javax.management.j2ee.statistics.BoundaryStatistic;

import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;

/**
	Serializable implementation of a BoundaryStatistic
 */
public class BoundaryStatisticImpl extends StatisticImpl
	implements BoundaryStatistic, Serializable
{
	static final long serialVersionUID = -5190567251179453418L;
	
	private long	LowerBound;
	private long	UpperBound;
	
	
		public
	BoundaryStatisticImpl(
		final String	name,
		final String	description,
		final String	unit,
		final long		startTime,
		final long		lastSampleTime,
		final long		lower,
		final long		upper )
	{
		super( name, description, unit, startTime, lastSampleTime );
		
		if ( LowerBound > UpperBound )
		{
			throw new IllegalArgumentException();
		}
		
		LowerBound	= lower;
		UpperBound	= upper;
	}
	
	/**
		Base the Statistic on the {@link CompositeData}
	 */
		public
	BoundaryStatisticImpl( final CompositeData compositeData )
	{
		this( OpenMBeanUtil.compositeDataToMap( compositeData ) );
	}
	
		public
	BoundaryStatisticImpl( final Map<String,Object> m )
	{
		this( new MapStatisticImpl( m ) );
	}
	
	
		public
	BoundaryStatisticImpl( final MapStatistic s )
	{
		super( s );
		
		LowerBound	= s.getlong( "LowerBound" );
		UpperBound	= s.getlong( "UpperBound" );
	}
	
		public
	BoundaryStatisticImpl( final BoundaryStatistic s )
	{
		super( s );
		
		LowerBound	= s.getLowerBound();
		UpperBound	= s.getUpperBound();
	}
	
		public long
	getLowerBound()
	{
		return( LowerBound );
	}
	
		public long
	getUpperBound()
	{
		return( UpperBound );
	}
}





