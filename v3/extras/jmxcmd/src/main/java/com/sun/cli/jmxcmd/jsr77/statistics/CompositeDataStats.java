/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/CompositeDataStats.java,v 1.1 2004/10/14 19:06:23 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:23 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Set;
import java.util.Map;


import javax.management.openmbean.CompositeData;

import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.Statistic;

import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;

/**
	Implementation of Stats which expects a CompositeData to contain
	the Statistics keyed by name, with values of CompositeData.
 */
public final class CompositeDataStats implements Stats
{
	final Map<String,Object>	mItems;
	
	/**
		Create a new CompositeDataStats using the specified CompositeData
	 */
		public
	CompositeDataStats( final CompositeData compositeData )
	{
		mItems	= OpenMBeanUtil.compositeDataToMap( compositeData );
	}
	
		public Statistic
	getStatistic( String statisticName )
	{
		final CompositeData	statData	= (CompositeData)mItems.get( statisticName );
		if ( statData == null && ! mItems.containsKey( statisticName ) )
		{
			throw new IllegalArgumentException( statisticName );
		}
		
		return( new CompositeDataStatistic( statData ) );
	}
	
		public String[]
	getStatisticNames()
	{
		final Set<String>	nameSet	= mItems.keySet();
		
		return nameSet.toArray( new String[ nameSet.size() ] );
	}
	
		public Statistic[]
	getStatistics()
	{
		final String[]		names		= getStatisticNames();
		final Statistic[]	statistics	= new Statistic[ names.length ];
		
		for( int i = 0; i < names.length; ++i )
		{
			statistics[ i ]	= getStatistic( names[ i ] );
		}
		
		return( statistics );
	}
   
}





