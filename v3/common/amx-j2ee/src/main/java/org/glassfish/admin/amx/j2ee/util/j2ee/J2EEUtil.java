/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admin.amx.j2ee.util;

import org.glassfish.admin.amx.j2ee.statistics.MapStatistic;
import org.glassfish.admin.amx.j2ee.statistics.NumberStatistic;
import org.glassfish.admin.amx.j2ee.statistics.StringStatistic;

import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.OpenMBeanUtil;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.SetUtil;

import javax.management.j2ee.statistics.*;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.glassfish.admin.amx.j2ee.StateManageable;

/**
	J2EE JSR 77 utilities
 */
public class J2EEUtil
{
	private final static String	GET		= "get";
	
	private	J2EEUtil()	{}
	
	
	private static final Set<String>	IGNORE	=
	    SetUtil.newUnmodifiableStringSet( "getClass" );
	
	/**
		Convert the Statistic into a Map containing all the name/value pairs
		obtained by finding method names that match a getter pattern
		getXXX().  Any Statistic can thus be converted into a Map which preserves
		all its members.
		
		@param statistic any Statistic or subclass
	 */
		public static Map<String,Object>
	statisticToMap( Statistic statistic )
	{
		final Map<String,Object>	result	= new HashMap<String,Object>();
		
		if ( statistic instanceof MapStatistic )
		{
			result.putAll( ((MapStatistic)statistic).asMap() );
		}
		else
		{
			// get all public methods
			final Method[]	methods	= statistic.getClass().getMethods();

			for( int i = 0; i < methods.length; ++i )
			{
				final Method	method		= methods[ i ];
				final String	methodName	= method.getName();
				
				if ( methodName.startsWith( GET ) &&
						! IGNORE.contains( methodName ) &&
						method.getParameterTypes().length == 0 &&
						method.getExceptionTypes().length == 0 &&
						method.getReturnType() != Void.class )
				{
					try
					{
						final Object	value = method.invoke( statistic, (Object[])null );
						
						final String name	= methodName.substring( GET.length(), methodName.length() );
						result.put( name, value );
					}
					catch( Exception e )
					{
						// ignore
					}
				}
			}
		}

		return( result );
	}
	
	
	/**
		Get the type name for a Statistic.
	 */
		public static String
	getStatisticType( Statistic statistic )
	{
		String	type	= getType( STATISTIC_CLASSES, statistic );
		
		if ( type == null )
		{
			type	= statistic.getClass().getName();
		}

		return( type );
	}
	
	
	/**
		Get the type matching the object.
	 */
		private static String
	getType( Class[] classes, Object o )
	{
		String	type	= null;
		
		for( int i = 0; i < classes.length; ++i )
		{
			final Class	theClass	= classes[ i ];
			
			if ( theClass.isInstance( o ) )
			{
				type	= theClass.getName();
				break;
			}
		}
		return( type );
	}
	
	private static final Class[] STATISTIC_CLASSES	= 
	{
		BoundaryStatistic.class,
		BoundedRangeStatistic.class,
		TimeStatistic.class,
		RangeStatistic.class,
		CountStatistic.class,
		StringStatistic.class,
		NumberStatistic.class,
		MapStatistic.class,
	};
	
	
	
		private static CompositeType
	statisticMapToCompositeType( final String statisticType, final Map<String,?> map )
		throws OpenDataException
	{
		final String	description	= "J2EE management statistic " + statisticType;
		
		return( OpenMBeanUtil.mapToCompositeType( statisticType, description, map, null) );
	}
	
	
	/**
		Get the JMX OpenMBean CompositeType corresponding to the Statistic.
	 */
		public static CompositeType
	statisticToCompositeType( Statistic statistic )
		throws OpenDataException
	{
		final String	statisticType	= getStatisticType( statistic );
		final Map<String,Object>    map = statisticToMap( statistic );
		
		return( statisticMapToCompositeType( statisticType, map ) );
	}
	
	/**
		Convert a Statistic into a JMX CompositeDataSupport.
		
		@param statistic any Statistic or subclass
	 */
		public static CompositeDataSupport
	statisticToCompositeData( Statistic statistic )
		throws OpenDataException
	{
		final String			statisticType	= getStatisticType( statistic );
		
		final Map<String,Object>    map = statisticToMap( statistic );
	
		final CompositeType		type	= statisticMapToCompositeType( statisticType, map );
		
		return( new CompositeDataSupport( type, map ) );
	}
	
	
	private static final Class[] STATS_CLASSES	= 
	{
		EJBStats.class,
		MessageDrivenBeanStats.class,
		SessionBeanStats.class,
		StatefulSessionBeanStats.class,
		StatelessSessionBeanStats.class,
		EntityBeanStats.class,
		JavaMailStats.class,
		JCAConnectionStats.class,
		JCAConnectionPoolStats.class,
		JDBCConnectionPoolStats.class,
		JCAStats.class,
		JDBCConnectionPoolStats.class,
		JDBCStats.class,
		JMSConnectionStats.class,
		JMSConsumerStats.class,
		JMSEndpointStats.class,
		JMSProducerStats.class,
		JMSSessionStats.class,
		JMSStats.class,
		JVMStats.class,
		ServletStats.class,
		URLStats.class
	};
	
	/**
		Get the type for use in a {@link CompositeType} type.
	 */
		public static String
	getStatsType( Stats stats )
	{
		String	type	= getType( STATS_CLASSES, stats );
		
		if ( type == null )
		{
			type	= stats.getClass().getName();
		}
		
		return( type );
	}
	
	
	/**
		Convert a Stats object into a CompositeDataSupport, which is a standard
		type in JMX which can be serialized.
		<p>
		The CompositeData entries will keyed by the name of the Statistic.  The value
		of each Statistic will also be a CompositeData, keyed by the value names
		found in that statistic.
		<p>
		For example, the JVMStats object would be converted into a CompositeData with the entries:
		<p>
		<ul>
		<li>HeapSize</li>
			<ul>
			<li>Description</li>
			<li>LastSampleTime</li>
			<li>Name</li>
			<li>StartTime</li>
			<li>Unit</li>
			<li>LowerBound</li>
			<li>UpperBound</li>
			<li>HighWaterMark</li>
			<li>LowWaterMark</li>
			<li>Current</li>
			</ul>
		<li>UpTime</li>
			<ul>
			<li>Description</li>
			<li>LastSampleTime</li>
			<li>Name</li>
			<li>StartTime</li>
			<li>Unit</li>
			<li>Count</li>
			</ul>
		</ul>
		
		@param stats any JSR 77 Stats object
	 */
		public static CompositeDataSupport
	statsToCompositeData( final Stats stats )
		throws OpenDataException
	{
		final String			statsType	= getStatsType( stats );
		
		final Statistic[]		statistics	= stats.getStatistics();
		final String[]			names			= new String[ statistics.length ];
		final CompositeData[]	datas			= new CompositeData[ names.length ];
		final String[]			itemDescriptions	= new String[ names.length ];
		final CompositeType[]	itemTypes			= new CompositeType[ names.length ];
		
		for( int i = 0; i < names.length; ++i )
		{
			names[ i ]				= statistics[ i ].getName();
			datas[ i ]				= statisticToCompositeData( statistics[ i ] );
			itemTypes[ i ]			= datas[ i ].getCompositeType();
			itemDescriptions[ i ]	= statistics[ i ].getName();
		}
		
		final CompositeType	type	= new CompositeType(
				statsType,
				"CompositeData for " + statsType,
				names,
				itemDescriptions, 
				itemTypes );
		
		return( new CompositeDataSupport( type, names, datas ) );
	}
	
	
	/**
		Turn a Stats object into a Map containing all its Statistics, keyed by Statistic.getName()
		and value of the Statistic.
	 */
		public static Map<String,Statistic>
	statsToMap( final Stats stats )
	{
		final Statistic[]		statistics	= stats.getStatistics();
		
		return( statisticsToMap( statistics ) );
	}


	/**
		Convert a Statistic[] into a Map keyed by the name of the Statistic, with
		value being the Statistic.
		
		@return Map keyed by name of the Statistic
	 */
		public static Map<String,Statistic>
	statisticsToMap( final Statistic[] statistics )
	{
		final Map<String,Statistic> m	= new HashMap<String,Statistic>();
		
		for( int i = 0; i < statistics.length; ++i )
		{
			final String	name	= statistics[ i ].getName();
			m.put( name, statistics[ i ] );
		}
		
		return( m );
	}
	
	
		public static Method[]
	getStatisticGetterMethodsUsingNames( final Stats stats)
		throws NoSuchMethodException
	{
		final String[]	statisticNames	= stats.getStatisticNames();
		
		final Class<? extends Stats>	statsClass	= stats.getClass();
		final Method[]		methods		= new Method[ statisticNames.length ];
		
		final Set<String>   missing = new HashSet<String>();
		
		for( int i = 0; i < statisticNames.length; ++i )
		{
	        //System.out.print( statisticNames[i ] + ", " );
	        final String methodName = JMXUtil.GET + statisticNames[ i ];
	        try
	        {
			methods[ i ]	= statsClass.getMethod( methodName, (Class[])null );
			}
			catch( final NoSuchMethodException e )
			{
			    missing.add( methodName );
			}
		}
		
		if ( missing.size() != 0 )
		{
		    throw new NoSuchMethodException(
		        "Missing methods: in object of class " + stats.getClass().getName() +
		        ": " + CollectionUtil.toString( missing, ", ") );
	    }
	    
		return( methods );
	}
	
		public static Method[]
	getStatisticGetterMethodsUsingIntrospection( final Stats stats)
	{
		final Method[]		candidates		= stats.getClass().getMethods();
		
		final List<Method>	results	= new ArrayList<Method>();
		for( int methodIdx = 0; methodIdx < candidates.length; ++methodIdx )
		{
			final Method	method	= candidates[ methodIdx ];
			final String	methodName	= method.getName();
			
			final Class	returnType	= method.getReturnType();
			
			if ( JMXUtil.isGetter( method ) &&
				Statistic.class.isAssignableFrom( returnType ) &&
				method.getParameterTypes().length == 0 )
			{
				results.add( method );
			}
		}
	
		return results.toArray( new Method[ results.size() ] );
	}

    /**
        @return String equivalent of a {@link StateManageable} state
     */
        public static String
    getStateManageableString( final int state )
    {
        String s = null;
        
        if ( state == StateManageable.STATE_STARTING )
        {
            s   = "STATE_STARTING";
        }
        else if ( state == StateManageable.STATE_RUNNING )
        {
            s   = "STATE_RUNNING";
        }
        else if ( state == StateManageable.STATE_STOPPING )
        {
            s   = "STATE_STOPPING";
        }
        else if ( state == StateManageable.STATE_STOPPED )
        {
            s   = "STATE_STOPPED";
        }
        else if ( state == StateManageable.STATE_FAILED )
        {
            s   = "STATE_FAILED";
        }
        else
        {
            throw new IllegalArgumentException( "" + state );
        }
        return s;
    }

}





