/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/CompositeDataStatistic.java,v 1.1 2004/10/14 19:06:23 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:23 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Map;

import javax.management.j2ee.statistics.Statistic;

import javax.management.openmbean.CompositeData;

import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;

/**
	Implementation of Statistic which expects a CompositeData to contain
	them by name.
 */
public class CompositeDataStatistic extends MapStatisticImpl
{
	/**
		Create a new CompositeDataStatistic using the specified {@link CompositeData}.
		Create from a {@link CompositeData}
	 */
		public
	CompositeDataStatistic( final CompositeData compositeData )
	{
		this( OpenMBeanUtil.compositeDataToMap( compositeData ) );
	}
	
	/**
		Create a new instance using the specified {@link Map}
		whose keys are the Statistic names.
	 */
		public
	CompositeDataStatistic( final Map<String,Object> map )
	{
		super( map );
	}
	
	/**
		Create a new instance using the specified Statistic as the source
		for the members.
	 */
		public
	CompositeDataStatistic( final Statistic statistic )
	{
		super( statistic );
	}
}





