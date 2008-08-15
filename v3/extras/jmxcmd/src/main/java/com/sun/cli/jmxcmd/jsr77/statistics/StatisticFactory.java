/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/StatisticFactory.java,v 1.1 2004/10/14 19:06:25 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:25 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;
import java.lang.reflect.Proxy;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.BoundaryStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;
import com.sun.cli.jcmd.util.misc.ClassUtil;
import com.sun.cli.jcmd.util.misc.SetUtil;
import com.sun.cli.jcmd.util.misc.MapUtil;

/**
	Creates Statistic implementations based on CompositeData or Map.
 */
public final class StatisticFactory 
{
	private	StatisticFactory()	{}
	
	
	/**
		Create a new Statistic using the specified CompositeData
		
		@param theInterface		interface which the Statistic should implement, must extend Statistic
	 */
		public static Statistic
	create( Class theInterface, final CompositeData data )
	{
		return( create( theInterface, OpenMBeanUtil.compositeDataToMap( data ) ) );
	}
	
	private static final String	COUNT_STATISTIC			= CountStatistic.class.getName();
	private static final String	TIME_STATISTIC			= TimeStatistic.class.getName();
	private static final String	RANGE_STATISTIC			= RangeStatistic.class.getName();
	private static final String	BOUNDARY_STATISTIC		= BoundaryStatistic.class.getName();
	private static final String	BOUNDED_RANGE_STATISTIC	= BoundedRangeStatistic.class.getName();
	private static final String	MAP_STATISTIC			= MapStatistic.class.getName();
	
	
	
	/**
		Create a new Statistic using the specified CompositeData.
		The CompositeType of data must be an appropriate Statistic class.
	 */
		public static Statistic
	create( final CompositeData data )
	{
		final String	typeName	= data.getCompositeType().getTypeName();
		Class	theInterface	= null;
		
		if ( typeName.equals( COUNT_STATISTIC ) )
		{
			theInterface	= CountStatistic.class;
		}
		else if ( typeName.equals( TIME_STATISTIC ) )
		{
			theInterface	= TimeStatistic.class;
		}
		else if ( typeName.equals( RANGE_STATISTIC ) )
		{
			theInterface	= RangeStatistic.class;
		}
		else if ( typeName.equals( BOUNDARY_STATISTIC ) )
		{
			theInterface	= BoundaryStatistic.class;
		}
		else if ( typeName.equals( BOUNDED_RANGE_STATISTIC ) )
		{
			theInterface	= BoundedRangeStatistic.class;
		}
		else if ( typeName.equals( MAP_STATISTIC ) )
		{
			theInterface	= MapStatistic.class;
		}
		else
		{
			try
			{
				theInterface	= ClassUtil.classForName( typeName );
			}
			catch( Exception e )
			{
				theInterface	= Statistic.class;
			}
		}
		
		return( create( theInterface, data ) );
	}
	
	
	/**
		Create a new Statistic using the specified map.  The standard JSR 77
		types are handled appropriately. Custom (non-standard) Statistics
		may also be used; in this case a proxy is returned which implements
		the interface specified by theClass.
		
		@param	theInterface	the interface which the resulting statistic implements
		@param	mappings		a Map containing keys of type String and their Object values
	 */
		public static Statistic
	create( Class theInterface, final Map<String,Object> mappings )
	{
		Statistic	result	= null;
		
		// hopefully specific classes are faster than a proxy...
		if ( theInterface == CountStatistic.class )
		{
			result	= new CountStatisticImpl( mappings );
		}
		else if ( theInterface == RangeStatistic.class )
		{
			result	= new RangeStatisticImpl( mappings );
		}
		else if ( theInterface == BoundaryStatistic.class )
		{
			result	= new BoundaryStatisticImpl( mappings );
		}
		else if ( theInterface == BoundedRangeStatistic.class )
		{
			result	= new BoundedRangeStatisticImpl( mappings );
		}
		else if ( theInterface == TimeStatistic.class )
		{
			result	= new TimeStatisticImpl( mappings );
		}
		else if ( theInterface == MapStatistic.class )
		{
			result	= new MapStatisticImpl( mappings );
		}
		else
		{
			throw new IllegalArgumentException(
				"Unsupported Statistic interface: " + theInterface.getName() );
		}
		
		return( result );
	}
	

	@SuppressWarnings("unchecked")
	private static final Class<? extends Statistic>[] KNOWN_STATISTICS	= new Class[]
	{
		CountStatistic.class,
		TimeStatistic.class,
		BoundedRangeStatistic.class,	// must come before RangeStatistic,BoundaryStatistic
		RangeStatistic.class,
		BoundaryStatistic.class,
	};
	
		public static Class<? extends Statistic>
	getInterface( final Statistic s )
	{
		final Class<? extends Statistic>	implClass	= s.getClass();
		
		Class<? extends Statistic>	theInterface	= MapStatistic.class;
		
		for( int i = 0; i < KNOWN_STATISTICS.length; ++i )
		{
			final Class<? extends Statistic>	candidateInterface	= KNOWN_STATISTICS[ i ];
			
			if ( candidateInterface.isAssignableFrom( implClass )  )
			{
				theInterface	= candidateInterface;
				break;
			}
		}
		
		return( theInterface );
	}
	
		

}





























