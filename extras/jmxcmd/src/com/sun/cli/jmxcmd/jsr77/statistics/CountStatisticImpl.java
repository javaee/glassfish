/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/CountStatisticImpl.java,v 1.1 2004/10/14 19:06:23 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:23 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Map;
import java.io.Serializable;

import javax.management.openmbean.CompositeData;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;

import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;


/**
	
 */
public class CountStatisticImpl extends StatisticImpl
	implements CountStatistic, Serializable
{
	static final long serialVersionUID = -4868791714488583778L;
	
	/* member name as defined by JSR 77 */
	private final long	Count;
	
		public
	CountStatisticImpl(
		final String	name,
		final String	description,
		final String	unit,
		final long		startTime,
		final long		lastSampleTime,
		final long		count )
	{
		super( name, description, unit, startTime, lastSampleTime );
		Count	= count;
	}
	
		public
	CountStatisticImpl( final CompositeData compositeData )
	{
		this( OpenMBeanUtil.compositeDataToMap( compositeData ) );
	}
	
		public
	CountStatisticImpl( final CountStatistic s )
	{
		super( s );
		Count	= s.getCount();
	}
	
		public
	CountStatisticImpl( final MapStatistic s )
	{
		super( s );
		Count	= s.getlong( "Count" );
	}
	
		public
	CountStatisticImpl( final Map data )
	{
		this( new MapStatisticImpl( data ) );
	}


 		public long
 	getCount()
 	{
 		return( Count );
 	}
 	
 	
		public boolean
	equals( final Object rhs )
	{
		boolean	equals	= super.equals( rhs ) && (rhs instanceof CountStatistic);
		
		if ( equals )
		{
			final CountStatistic	s	= (CountStatistic)rhs;
			
			equals	= getCount() == s.getCount();
		}
		return( equals );
	}
}





