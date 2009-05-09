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
package org.glassfish.admin.amx.j2ee.statistics;

import org.glassfish.admin.amx.j2ee.util.J2EEUtil;
import org.glassfish.admin.amx.util.SetUtil;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
	Generic implementation of Stats based on either a Map or a {@link CompositeData}.
	There are two ways to implement a specific type of Stats object:
	<ul>
		<li>
		The subclass extends this base class, using getValue() to fetch
		the requested value from the Map maintained by this class.
		</li>
		<li>
		Create a proxy implementing the desired Stats subclass interface and use
		an instance of this class as the {@link java.lang.reflect.InvocationHandler}.
		</li>
	</ul>
	In addition to the standard JSR 77 Stats interfaces,
	the following specific Stats interfaces are available:
	<ul>
<li>{@link org.glassfish.admin.amx.monitor.statistics.AltJDBCConnectionPoolStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.AltServletStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.ConnectionManagerStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.ConnectionPoolStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.ConnectorConnectionPoolStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.EJBCacheStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.EJBMethodStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.EJBPoolStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.HTTPListenerStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.HTTPServiceVirtualServerStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.KeepAliveStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.ThreadPoolStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.TransactionServiceStats}</li>
<li>{@link org.glassfish.admin.amx.monitor.statistics.WebModuleVirtualServerStats}</li>
	</ul>
 */
public class StatsImpl
	extends MapGetterInvocationHandler<Statistic>
	implements Stats, Serializable
{
	static final long serialVersionUID = 6228973710059979557L;
	
	/**
		Create a Stats from a CompositeData, whose keys are the Statistic names
		and whose values are CompositeData for the Statistic.
	 */
		public
	StatsImpl( final CompositeData compositeData )
	{
		this( createStatisticsMap( compositeData ) );
	}
	
		private static Map<String,Statistic>
	requireSerializableMap( final Map<String, Statistic> m )
	{
		// required every entry to be a Statistic
		for( final Statistic s : m.values() )
		{
			if ( ! (s instanceof Statistic ) )
			{
				throw new IllegalArgumentException();
			}
		}
		
		Map<String,Statistic>	sMap	= null;
		
		if ( m instanceof Serializable )
		{
			sMap	= m;
		}
		else
		{
			sMap	= new HashMap<String, Statistic>( m );
		}
		
		return( sMap );
	}
	
	/**
		Ensure that every Statistic is an instance of one of our acceptable
		implementations. If you are adding a custom statistic, add that
        statistic here in the if clause.
	 */
		private static Statistic
	requireStatisticImpl( final Statistic statisticIn )
	{
		Statistic	statisticOut	= null;
		
		if ( statisticIn instanceof CountStatisticImpl ||
			statisticIn instanceof RangeStatisticImpl ||
			statisticIn instanceof BoundedRangeStatisticImpl ||
			statisticIn instanceof BoundaryStatisticImpl ||
			statisticIn instanceof TimeStatisticImpl  ||
			statisticIn instanceof NumberStatisticImpl  ||
			statisticIn instanceof StringStatisticImpl
			)
		{
			statisticOut	= statisticIn;
		}
		else if ( statisticIn instanceof MapStatistic )
		{
			final Class<? extends Statistic> theInterface	=
			    StatisticFactory.getInterface( statisticIn );
			
			if ( theInterface != MapStatistic.class )
			{
				statisticOut = StatisticFactory.create( theInterface,
								J2EEUtil.statisticToMap( statisticIn ) );
			}
			else if ( ! ( statisticIn instanceof MapStatisticImpl ) )
			{
				// it's a MapStatistic, but not our implementation.
				statisticOut	= new MapStatisticImpl( statisticIn );
			}
		}
		else
		{
			// some weird kind, convert it to generic Statistic
			assert( false ) :
				"requireStatisticImpl: unsupported Statistic type of class " + statisticIn.getClass().getName();
			statisticOut	= new MapStatisticImpl( statisticIn );
		}
		
		return( statisticOut );
	}
	
	/**
		Ensure that every Statistic is an instance of StatisticsImpl.
	 */
		private static Statistic[]
	requireStatisticImpl( final Statistic[] statisticsIn )
	{
		boolean	convert	= false;
		
		// avoid conversion if already in desired form
		for( int i = 0; i < statisticsIn.length; ++i )
		{
			if ( ! (statisticsIn[ i ] instanceof MapStatistic ) )
			{
				convert	= true;
				break;
			}
		}
		
		Statistic[]		statisticsOut	= null;
		if ( convert )
		{
			statisticsOut	= new Statistic[ statisticsIn.length ];
			
			for( int i = 0; i <
			 statisticsIn.length; ++i )
			{
				statisticsOut[ i ]	= requireStatisticImpl( statisticsIn[ i ] );
			}
		}
		else
		{
			statisticsOut	= statisticsIn;
		}
		
		return( statisticsOut );
	}

	/**
		Create a Stats from a Map, whose keys are the Statistic names
		and whose values are the Statistics.
	 */
		public
	StatsImpl( final Map<String, Statistic> statisticsIn )
	{
		super( requireSerializableMap( statisticsIn ) );
	}
	
	
	/**
	 */
		public
	StatsImpl( final Statistic[]	statistics )
	{
		this( createStatisticsMap( statistics ) );
	}
	
	
		private static Map<String,Statistic>
	createStatisticsMap( final CompositeData compositeData )
	{
		final CompositeType	compositeType	= compositeData.getCompositeType();
		final Set		keySet	= compositeType.keySet();
		final Iterator	iter	= keySet.iterator();
		
		final Map<String,Statistic>	map	= new HashMap<String,Statistic>();
		
		while ( iter.hasNext() )
		{
			final String		name	= (String)iter.next();
			final CompositeData	data	= (CompositeData)compositeData.get( name );
			
			map.put( name, StatisticFactory.create( data ) );
		}
		
		return( map );
	}
	
		private static Map<String,Statistic>
	createStatisticsMap( final Statistic[]	statistics )
	{
		final Map<String,Statistic>	m	= new HashMap<String,Statistic>();
		
		for( int i = 0; i < statistics.length; ++i )
		{
			final Statistic	statistic	= requireStatisticImpl( statistics[ i ] );
			
			if ( statistic != null )
			{
				m.put( statistic.getName(), statistic );
			}
		}
		
		return( m );
	}
	
	
		public Statistic
	getStatistic( String statisticName )
	{
		try
		{
			return getValue( statisticName );
		}
		catch( Exception e )
		{
			final String msg	= "NOT a Statistic: " + statisticName +
				" of class " + getValue( statisticName ).getClass();
			throw new RuntimeException( msg, e );
		}
	}
	
		public String[]
	getStatisticNames()
	{
		final Set<String>	names	= getMap().keySet();
		
		return( SetUtil.toStringArray( names ) );
	}
	
		public Statistic[]
	getStatistics()
	{
		final Collection<Statistic>	values	= getMap().values();
		final Statistic[]	statistics	= new Statistic[ values.size() ];
		
		return values.toArray( statistics );
	}
	
		public String
	toString()
	{
		final StringBuffer	buf	= new StringBuffer();
		
		final Statistic[]	statistics	= getStatistics();
		buf.append( statistics.length + " Statistics:\n" );
		for( int i = 0; i < statistics.length; ++i )
		{
			buf.append( statistics[ i ].toString() + "\n");
		}
		
		return( buf.toString() );
	}
	
	
		public boolean
	equals( final Object rhs )
	{
		boolean	equals	= false;
		
		if ( rhs != null && rhs instanceof Stats )
		{
			final Stats	stats	= (Stats)rhs;
			
			final String[]	myNames	= getStatisticNames();
			final String[]	rhsNames	= stats.getStatisticNames();
			if ( myNames.length == rhsNames.length )
			{
				equals	= true;
				
				for( int i = 0; i < myNames.length; ++i )
				{
					final String	statisticName	= myNames[ i ];
					final Statistic	myStatistic		= getStatistic( statisticName );
					final Statistic	rhsStatistic	= stats.getStatistic( statisticName );
					
					if ( ! myStatistic.equals( rhsStatistic ) )
					{
						equals	= false;
						break;
					}
				}
			}
		}
		return( equals );
	}
	
		public Object
	invoke(
		Object		myProxy,
    	Method		method,
		Object[]	args )
   		throws java.lang.Throwable
   	{
   		Object			result	= null;
   		final String	methodName		= method.getName();
   		final int		numArgs	= args == null ? 0 : args.length;
   		
   		if ( numArgs == 0 && methodName.equals( "getStatisticNames" ) )
   		{
   			result	= getStatisticNames();
   		}
   		else if ( numArgs == 0 && methodName.equals( "getStatistics" ) )
   		{
   			result	= getStatistics();
   		}
   		else if ( numArgs	== 1  && methodName.equals( "getStatistic" ) &&
   			method.getReturnType() == Statistic.class &&
   			method.getParameterTypes()[ 0 ] == String.class )
   		{
   			result	= getStatistic( (String)args[ 0 ] );
   		}
   		else
   		{
   			result	= super.invoke( myProxy, method, args );
   		}

   		return( result );
   	}
}






