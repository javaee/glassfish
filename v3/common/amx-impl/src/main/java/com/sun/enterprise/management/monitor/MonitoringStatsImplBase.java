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

package com.sun.enterprise.management.monitor;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;
import java.io.Serializable;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.AttributeNotFoundException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.InstanceNotFoundException;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.*;
import com.sun.appserv.management.j2ee.statistics.StatsImpl;

import com.sun.appserv.management.base.AMXDebug;

import com.sun.appserv.management.monitor.MonitoringStats;

import com.sun.appserv.management.util.j2ee.J2EEUtil;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.ArrayUtil;
import com.sun.appserv.management.j2ee.statistics.MapStatistic;
import com.sun.appserv.management.j2ee.statistics.MapStatisticImpl;
import com.sun.appserv.management.j2ee.statistics.StatisticImpl;
import com.sun.appserv.management.j2ee.statistics.StatisticFactory;

import com.sun.appserv.management.util.jmx.AttributeNameMapper;
import com.sun.appserv.management.util.jmx.AttributeNameMapperImpl;

import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.DelegateToMBeanDelegate;

/**
	Base implementation class for Monitoring MBeans that provide Stats.
*/
public abstract class MonitoringStatsImplBase extends MonitoringImplBase
{
	private MBeanInfo					mMBeanInfo;
	private final AttributeNameMapper	mFieldNameMapper;
	private final AttributeNameMapper	mStatisticNameMapper;
	
	private Set<String>					mStatisticNames;
	
		public
	MonitoringStatsImplBase( final String j2eeType, final Delegate delegate )
	{
		super( j2eeType, delegate );
		
		mMBeanInfo	= null;
		
		mFieldNameMapper		= new AttributeNameMapperImpl();
		mStatisticNameMapper	= new AttributeNameMapperImpl();
		
		mStatisticNames	= null;
	}

		public
	MonitoringStatsImplBase( final String j2eeType )
	{
		this( j2eeType, null );
	}

	/**
		The interface available in underlying "old" MBean.
	 */
	private static interface OldMonitoringMBean
	{
		public String[]		getStatisticNames();
		public Statistic[]	getStatistics();
	}
	
	/**
		Get the underlying Delegate.  Note that this proxy
		may not actually implement all the routines in MonitoringStats;
		we use only a few however, so that is OK.
	 */
		protected OldMonitoringMBean
	getMonitoringMBeanDelegate()
	{
		return (OldMonitoringMBean)getDelegateProxy(OldMonitoringMBean.class);
	}
	
	/**
		The prefix of a getter method.
	 */
	private static final String	GET	= "get";
	
	/**
		The delimiter between a Statistic name and its field value when
		exposed as an Attribute.
	 */
	private static final String	STATISTIC_DELIM	= "_";
	
	
	static private final Set<String>   IGNORE_MISSING_SUFFIXES =
	    Collections.unmodifiableSet( GSetUtil.newSet( new String[]
	    {
	        "current", "count", "description", "name",
	        "lowwatermark", "highwatermark", "unit",
	        "lastsampletime", "starttime", 
	        "lowerbound", "upperbound",
	        "maxtime", "mintime", "totaltime", "average",
	        
	        "children",
	    }));
	    
	/**
	    We don't map any of the Attributes derived from statistics.
	 */
	    protected void
	handleMissingOriginals( final Set<String> missingOriginals )
	{
	    final Set<String>   stillMissing    = new HashSet<String>();
	    
	    for( final String name : missingOriginals )
	    {
	        final int idx   = name.lastIndexOf( '-' );
	        final String suffix = name.substring( idx + 1, name.length() );
	        
	        if ( ! IGNORE_MISSING_SUFFIXES.contains( suffix ) )
	        {
	            stillMissing.add( name );
	        }
	    }
        
        super.handleMissingOriginals( stillMissing );
	}
	
	
		protected final AttributeNameMapper
	getFieldNameMapper()
	{
		return( mFieldNameMapper );
	}
		protected final AttributeNameMapper
	getStatisticNameMapper()
	{
		return( mStatisticNameMapper );
	}


	protected final static String[]	STD_FIELDS	= new String[]
	{
		// none, by default
	};
	
	/**
		Initialize the field-name mapping for any names that required
		special conversion.
	 */
		protected void
	initFieldNameMapper()
	{
		final AttributeNameMapper	m	= getFieldNameMapper();
		
		assert( (STD_FIELDS.length % 2) == 0 );
		for( int i = 0; i < STD_FIELDS.length - 1; ++i )
		{
			m.addMapping( STD_FIELDS[ i ], STD_FIELDS[ i + 1 ] );
		}
	}
	
	
	protected final static String[]	STD_STATISTICS	= new String[]
	{
		"id", "ID",
		"Id", "ID",
	};
	
	/**
		Initialize the Statistic-name mapping for any names that required
		special conversion.
	 */
		protected void
	initStatisticNameMapper()
	{
		final AttributeNameMapper	m	= getStatisticNameMapper();
		
		final String[]	mappings	= STD_STATISTICS;
		
		for( int i = 0; i < mappings.length -1; ++i )
		{
			m.addMapping( mappings[ i ], mappings[ i + 1 ] );
		}
	}
	
	/**
		Get an Attribute value by looking for a corresponding Statistic.
		
		@param statisticName as seen in the MBeanInfo of this MBean
		@param fieldName as seen in the MBeanInfo of this MBean
	 */
		protected Object
	getAttributeFromStatistic(
		final String	statisticName,
		final String	fieldName )
		throws AttributeNotFoundException
	{
		try
		{
			final Statistic s	= getStatistic( statisticName );
			assert( s instanceof MapStatistic );
			
			final String	methodName	= JMXUtil.GET + fieldName;
			final Method	m	= s.getClass().getMethod( methodName, (Class[])null);
			
			//final MapStatisticImpl	ms	= new MapStatisticImpl( s );
			final Object result	= m.invoke( s, (Object[])null );
			return( result );
		}
		catch( Exception e )
		{
		    debug( "getAttributeFromStatistic: exception getting statistic " +
		        statisticName + e  + "\n" + 
		        ExceptionUtil.getStackTrace( ExceptionUtil.getRootCause( e ) ) );
			throw new AttributeNotFoundException( statisticName );
		}
	}
	
	/**
		Handle getting of artificial Attributes which are derived from Statistics.
		
		@param name the Attribute name
	 */
		protected Object
	getAttributeManually( final String name )
		throws AttributeNotFoundException
	{
		final int	idx	= name.indexOf( STATISTIC_DELIM );
		
		Object	result	= null;
		
		if ( idx > 0 )
		{
			// Attribute name is of the form <statistic-name>_<field-name>
			final String	statisticName	= name.substring( 0, idx );
			final String	fieldName	= name.substring( idx + 1, name.length() );
			
			result	= getAttributeFromStatistic( statisticName, fieldName );
		}
		else
		{
		    result  = super.getAttributeManually( name );
		}
		
		return( result );
	}
	
	/**
		A bug in the underlying MBeans is present.
	 */
	private static final boolean	BUG_STATISTIC_NAMES	= true;
	
	
		private String[]
	originalToDerivedStatisticNames( final String[] names )
	{
		final String[]	derived	= new String[ names.length ];
		
		for( int i = 0; i < names.length; ++i )
		{
			derived[ i ]	= originalToDerivedStatisticName( names[ i ] );
		}
		
		return( derived );
	}
	
	
		private void
	checkUnderlyingMBean()
	{
		assert( BUG_STATISTIC_NAMES ); 
                
		if ( BUG_STATISTIC_NAMES  /* && ! getJ2EEType().equals( "X-BeanMethodMonitor" ) */ )
		{
		    final Delegate  delegate    = getDelegate();
		    
            if ( delegate == null) return;
            
			final String[]	claimedNames	= getMonitoringMBeanDelegate().getStatisticNames();
			if ( claimedNames == null )
			{
			    throw new RuntimeException( "Delegate " + 
			        " used by AMX MBean " + getObjectName() +
			        " returned null StatisticNames array" );
			}
			else if ( claimedNames.length == 0 )
			{
			    throw new RuntimeException( "Delegate " + 
			        " used by AMX MBean " + getObjectName() +
			        " returned empty StatisticNames array" );
			}

			final Statistic[]	statistics	= getMonitoringMBeanDelegate().getStatistics();
			if ( statistics == null )
			{
			    throw new RuntimeException( "Delegate " + 
			        " used by AMX MBean " + getObjectName() +
			        " returned null Statistics array" );
			}
			else if ( statistics.length == 0 )
			{
			    throw new RuntimeException( "Delegate " + 
			        " used by AMX MBean " + getObjectName() +
			        " returned empty Statistics array" );
			}
			   
			try
			{
				final Set<String>		actualSet	= new HashSet<String>();
				
				final String[]	namesFromGetStatistics	= new String[ statistics.length ];
				
				for( int i = 0; i < statistics.length; ++i )
				{
					final String	name	= StringUtil.upperCaseFirstLetter( statistics[ i ].getName() );
					namesFromGetStatistics[ i ]	= name;
					
					if ( ! actualSet.contains( name ) )
					{
						actualSet.add( name );
					}
					else
					{
						logWarning( "MBean delegate " + 
						    " for " + getObjectName() +
							" returns Statistic with duplicate name: " + name +
							" from getStatistics() call.\n" );
					}
				}
				
				final Set<String>	claimedSet	= GSetUtil.newStringSet( claimedNames );
				if ( ! claimedSet.equals( actualSet ) )
				{
					final Set<String>	missing	= new HashSet<String>( claimedSet );
					missing.removeAll( actualSet );
					
					// assume workarounds are in place
					final String msg = "\nMBean delegate " + " for " + getObjectName() +
						" does not provide Statistic(s): " + missing + " from getStatistics() call, " +
					"\ngetStatisticNames() = " + toString( claimedSet ) + 
					"\nnames from getStatistics() = " + toString( namesFromGetStatistics ) + "\n";
					
	                AMXDebug.getInstance().getOutput(
	                    "MonitoringStatsImplBase.checkUnderlyingMBean" ).println( msg );
					logFine( msg );
				}
			}
			catch( Exception e )
			{
				final Throwable rootCause	= ExceptionUtil.getRootCause( e );
				logWarning( "MBean delegate " + 
				    " doesn't work, used by AMX MBean: " +
					getObjectName() + "\n" +
					rootCause.getClass().getName() + "\n" +
					ExceptionUtil.getStackTrace( rootCause ) );
			}
		}
	}
	
	
		private final String[]
	initStatisticNames()
	{
		String[]	names	= null;
		
		if ( BUG_STATISTIC_NAMES )
		{
			names	= getStats().getStatisticNames();
			if ( names == null || names.length == 0 )
			{
			    throw new RuntimeException( "Stats are null or empty for: " + getObjectName());
			}
		}
		else
		{
			names	= originalToDerivedStatisticNames( getMonitoringMBeanDelegate().getStatisticNames() );
		}
		
		return( names );
	}
	
	/**
		Gets the names of all the Statistics.
		@return a String[] of statistic names
	*/
		public String[]
	getStatisticNames()
	{
		return( GSetUtil.toStringArray( mStatisticNames ) );
	}

		public CompositeDataSupport
	getOpenStatistic(String name)
	{
		final Statistic statistic = getStatistic( name );
		
		try
		{
			return J2EEUtil.statisticToCompositeData(statistic);
		}
		catch(OpenDataException e)
		{
			throw new RuntimeException(e);
		}
	}

		public CompositeDataSupport[]
	getOpenStatistics( final String[] names)
	{
		final CompositeDataSupport[] result	= new CompositeDataSupport[names.length];
		
		for(int i = 0; i < names.length; i++)
		{
			result[ i ] = getOpenStatistic( names[ i ] );
		}
		return result;
	}

		public CompositeDataSupport
	getOpenStats()
	{
		final Stats	stats	= getStats();
		
		CompositeDataSupport	result	= null;
		// can't make a CompositeDataSupport if there are no Statistics
		if ( stats.getStatisticNames().length != 0 )
		{
			try
			{
				result	= J2EEUtil.statsToCompositeData( stats );
			}
			catch(OpenDataException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			logWarning( "No Statistics available for: " + getObjectName() );
		}
		
		return( result );
	}

		public Statistic
	getStatistic( final String name)
	{
		final Stats		stats = getStats();
		
		return stats.getStatistic( name );
	}

		public Statistic[]
	getStatistics(final String[] desiredNames)
	{
		final Stats			stats	= getStats();
		final Statistic[]	result	= new Statistic[ desiredNames.length ];
		
		for( int i = 0; i < desiredNames.length; ++i )
		{
			final Statistic	statistic	= stats.getStatistic( desiredNames[ i ] );
			
			// OK to return null if not found--see Javadoc
			result[ i ]	= statistic;
		}
		
		return( result );
	}

	/**
		Convert a Statistic-name from its underlying name to the one we expose.
		
		@param underlyingName
	 */
		protected String
	originalToDerivedStatisticName( final String underlyingName )
	{
		String		result	= getStatisticNameMapper().originalToDerived( underlyingName );
		result	= StringUtil.upperCaseFirstLetter( result );
		
		return( result );
	}
	
		protected String
	derivedToOriginalStatisticName( final String derivedName )
	{
		return( getStatisticNameMapper().derivedToOriginal( derivedName ) );
	}
	
		protected final Statistic[]
	getStatisticsFromDelegate()
	{
	    if ( getDelegate() == null )
	    {
	        throw new NullPointerException();
	    }
		return( getStatisticsFromDelegate( getDelegate() ) );
	}

		protected final Statistic[]
	checkDuplicateStatistics(
		final Delegate d,
		final Statistic[] statistics)
	{
		// check to see if any names are duplicated
		final Set<String>	actualNames	= new HashSet<String>();
		for ( int i = 0; i < statistics.length; ++i )
		{
			final String	name	= statistics[ i ].getName();
			
			if ( actualNames.contains( name ) )
			{
				throw new RuntimeException( 
					"MonitoringStatsImplBase.checkDuplicateStatistics: " +
					getObjectName() +
						"Statistic " + StringUtil.quote( name ) + " is duplicated in getStatistics(): " +
					 	" as supplied from Delegate of " + StringUtil.quote( getObjectName() )+
					 	", please see bug #6179364"  );
			}
			else
			{
				actualNames.add( name );
			}
		}
		
		if ( actualNames.size() != statistics.length )
		{
			final String[] claimedNames = (String[])d.invoke( "getStatisticNames", null,  null );
		
			final Set<String>	missingNames	= GSetUtil.newStringSet( claimedNames );
			missingNames.removeAll( actualNames );
			
			throw new RuntimeException(
				"MonitoringStatsImplBase.getStatisticsFromDelegateRaw: " + missingNames.size() + 
				" Statistic names as found in Statistics from getStatistics() are missing: {" +
				toString( missingNames ) +
			 	"} from Delegate of " + StringUtil.quote( getObjectName() ) + ", please see bug #6179364" );
		}
		
		return( statistics );
	}

		protected Statistic[]
	getStatisticsFromDelegateRaw( final Delegate d )
	{
	    try
	    {
    		final Statistic[] statistics = (Statistic[])d.invoke( "getStatistics", null,  null );
    		
    		checkDuplicateStatistics( d, statistics );
    		
    		return( statistics );
		}
		catch( Exception e )
		{
		    final Throwable rootCause   = ExceptionUtil.getRootCause( e );
		    
		    logWarning( "MonitoringStatsImplBase: the com.sun.appserv Delegate MBean for AMX MBean " +
		        getObjectName() + " threw an exception: " + rootCause +
		    ", stack = \n" + ExceptionUtil.getStackTrace( rootCause ) );
		}
		return new Statistic[0];
	}


	private void
	debug( String s )
	{
		System.out.println( s );
	}
	
	
	/**
		Get all Statistics from the delegate (our only available call API).
		Statistic names are translated appropriately.
	 */
		protected Statistic[]
	getStatisticsFromDelegate( final Delegate d )
	{
		try
		{
			final Statistic[] statistics = getStatisticsFromDelegateRaw( d );
			
			// translate the names to be the ones we expose in MBeanInfo
			for( int i = 0; i < statistics.length; ++i )
			{
				final Statistic	origStatistic	= statistics[ i ];
			
				final MapStatistic	m	= new MapStatisticImpl( origStatistic );
				
				final String	convertedName	= originalToDerivedStatisticName( origStatistic.getName() );
				if ( ! convertedName.equals( origStatistic.getName() ) )
				{
					m.setName( convertedName );
				}
				
				final Class<? extends Statistic> theClass	=
				    StatisticFactory.getInterface( origStatistic );
				assert( theClass != null );
				
				// this will create one which implements the requisite interfaces
				statistics[ i ]	= StatisticFactory.create( theClass, m.asMap() );
				
				assert( theClass.isAssignableFrom( statistics[ i ].getClass() ));
			}
			
			return( statistics );
		}
		catch (Exception e)
		{
			final Throwable	rootCause	= ExceptionUtil.getRootCause( e );
			
			if ( ! ( rootCause instanceof InstanceNotFoundException ) )
			{
				// don't rethrow--will make MBeanServer unuseable as it has a bug if we throw
				// an exception of of getMBeanInfo() which halts any further processing of the query
				//NOTE: WARNING_CHANGED_TO_FINE	
				logWarning( "Can't get Statistics from delegate for " + getObjectName() +
					"\n" + rootCause.getMessage() + "\n" + ExceptionUtil.getStackTrace( rootCause ) );
			}
			throw new RuntimeException( e );
		}
	}
	
	// default interface implemented by Stats that we return
	private final Class[]	STATS_IMPL_INTERFACES	= new Class[]
	{
		Serializable.class,
		Stats.class,
	};
	
	/**
		Get the specific type of Stats interface (if any) that should be implemented
		by a Stats returned from getStats().
		
		@return an interface, or null if the interface is generic Stats
	 */
	protected abstract Class	getStatsInterface();
	
	
		protected StatsImpl
	createStatsImpl()
	{
		return new StatsImpl( getStatisticsFromDelegate() );
	}
	
	/*
		protected void
	serializeTest( final Object toSerialize )
		throws java.io.IOException, ClassNotFoundException
	{
		final java.io.ByteArrayOutputStream	os	= new java.io.ByteArrayOutputStream( 2048 );
		
		final java.io.ObjectOutputStream	oos	= new java.io.ObjectOutputStream( os );
		
		oos.writeObject( toSerialize );
		oos.close();
		
		final byte[]	bytes	= os.toByteArray();
		
		final java.io.ObjectInputStream	is	= new java.io.ObjectInputStream( new java.io.ByteArrayInputStream( bytes ) );
		
		final Object	result	= is.readObject();
		
		if ( ! result.equals( toSerialize ) )
		{
			assert( false ):
				"statistics not equal: " + toSerialize + " != " + result;
		}
	}
	*/
	
	
		public String
	getStatsInterfaceName()
	{
		return( getStatsInterface().getName() );
	}
	
		public Stats
	getStats()
	{
		final Class	statsInterface	= getStatsInterface();
		assert( statsInterface == null || Stats.class.isAssignableFrom( statsInterface ) );
		
		Class[] implementedInterfaces	= null;
		if ( statsInterface == null )
		{
			implementedInterfaces	= STATS_IMPL_INTERFACES;
			logInfo( "getStats: no Stats interface found for " + getObjectName() );
		}
		else
		{
			implementedInterfaces	= (Class[])
				ArrayUtil.newArray( STATS_IMPL_INTERFACES, statsInterface );
		}
		
		final StatsImpl			impl	= createStatsImpl();
		final ClassLoader		classLoader	= this.getClass().getClassLoader();
		
		final Stats stats	= (Stats)
			Proxy.newProxyInstance( classLoader, implementedInterfaces, impl );
	
	
	/*try
	{
		serializeTest( stats );
	} catch( Throwable t )
	{
		System.out.println( "getStats: Stats proxy serialization FAILED for " + getObjectName() );
		ExceptionUtil.getRootCause( t ).printStackTrace();
	}
	*/
		
		return( stats );
	}

	
		public final boolean
	refresh()
	{
		mMBeanInfo	= null;
		clearAttributeInfos();
		
		return( true );
	}
	
	/**
		Given a Statistic-name and a field-name, create an Attribute name.
		
		@param statisticName	Statistic-name
		@param fieldName
		@return a name suitable for an Attribute
	 */
		private String
	makeAttributeName(
		final String 	statisticName,
		final String	fieldName )
	{
		final String	attributeName	= statisticName + STATISTIC_DELIM + fieldName;

		return( attributeName );
	}
	
	/**
		JSR 77 defines these fields as having type "long", so we must define them that
		way as well, in spite of the underlying bug in the old monitoring MBeans, which
		declares them all as Strings.
	 */
	private static final Set<String> LONG_FIELDS = GSetUtil.newUnmodifiableStringSet(
		 "Count", "LastSampleTime", "StartTime", "LowerBound", "UpperBound",
		 "HighWaterMark", "LowWaterMark", "MaxTime", "MinTime", "TotalTime" );
	
	/**
		Determine the Java class of a field.
	 */
		private Class
	determineFieldType(
		final String	fieldName,
		final Object	value )
	{
		Class	theClass	= String.class;
		
		if ( value != null )
		{
			theClass	= ClassUtil.ObjectClassToPrimitiveClass( value.getClass() );
		}
		else if ( LONG_FIELDS.contains( fieldName ) )
		{
			theClass	= long.class;
		}
		
		return( theClass );
	}
	
	/**
		Convert a single Statistic to MBeanAttributeInfo.
		
		@return a Map, keyed by the Attribute name, value of MBeanAttributeInfo
	 */
		private Map<String,MBeanAttributeInfo>
	statisticToMBeanAttributeInfos( final Statistic s )
	{
		final Map<String,Object>	src	= new MapStatisticImpl( s ).asMap();
		
		final String	statisticName	= s.getName();
		
		final Map<String,MBeanAttributeInfo>	result	= new HashMap<String,MBeanAttributeInfo>();
		for( final String fieldName : src.keySet() )
		{
			final Object	value	= src.get( fieldName );
			// if the value is null, always make it a String
			final String	type	= determineFieldType( fieldName, value ).getName();
			
			final String	attributeName	= makeAttributeName( statisticName, fieldName );
			
			final MBeanAttributeInfo	attributeInfo	= new MBeanAttributeInfo( attributeName, type, "",
												true, false, false );
			result.put( attributeName, attributeInfo );
		}
		return( result );
	}
	
	/**
		Create MBeanInfo by taking default MBeanInfo and adding Attributes to it corresponding
		to all available Statistics.
	 */
		private synchronized MBeanInfo
	createMBeanInfo()
	{
		final MBeanInfo	baseMBeanInfo	= super.getMBeanInfo();
		
		MBeanInfo	mbeanInfo	= baseMBeanInfo;
		
		if ( getMBeanServer() != null && getDelegate() != null )
		{
			assert( getDelegate() != null ) : "null delegate for: " + getObjectName();
			
			final Map<String,MBeanAttributeInfo>
			    newAttrs	= new HashMap<String,MBeanAttributeInfo>();
			
			final Statistic[]	statistics	= getStats().getStatistics();
			for( int i = 0; i < statistics.length; ++i )
			{
				final Map<String,MBeanAttributeInfo> attrInfos	=
				    statisticToMBeanAttributeInfos( statistics[ i ] );
				
				newAttrs.putAll( attrInfos );
			}
			
			final MBeanAttributeInfo[]	dynamicAttrInfos	=
				new MBeanAttributeInfo[ newAttrs.keySet().size() ];
			newAttrs.values().toArray( dynamicAttrInfos );
			
			final MBeanAttributeInfo[]	attrInfos	=
				JMXUtil.mergeMBeanAttributeInfos( dynamicAttrInfos, baseMBeanInfo.getAttributes() );
			
			mbeanInfo	= JMXUtil.newMBeanInfo( baseMBeanInfo, attrInfos );
		}
		
		return( mbeanInfo );
	}
	
		protected ObjectName
	preRegisterHook( final ObjectName   objectName )
	{
		initFieldNameMapper();
		initStatisticNameMapper();
		
		// ensure that it gets generated anew now that we are registered and can access everything
		refresh();
		
		assert( MonitoringStats.class.isAssignableFrom( getInterface() )  ) :
			"MBean extends MonitoringStatsImpl but does not have MonitoringStats interface: " + getObjectName();
		
		if ( BUG_STATISTIC_NAMES )
		{
			checkUnderlyingMBean();
		}
		
		mStatisticNames	= GSetUtil.newUnmodifiableStringSet( initStatisticNames() );
	    
	    return objectName;
	}
	
	private static final MBeanInfo	EMPTY_MBEAN_INFO	=
		new MBeanInfo( "Empty", "Failed to create MBeanInfo", null, null, null, null);
		
		public synchronized MBeanInfo
	getMBeanInfo()
	{
		if ( mMBeanInfo == null )
		{
			try
			{
				mMBeanInfo	= createMBeanInfo();
			}
			catch( Throwable t )
			{
				// if we throw an exception, it will kill the MBeanServer's ability
				// to function
				final Throwable rootCause	= ExceptionUtil.getRootCause( t );
				
				// when an old mbean gets unregistered, this object does too,
				// but the MBeanServer calls getMBeanInfo() before unregistering us,
				// putting this MBean in the awkward position of supplying MBeanInfo
				// from a delegate that has disappeared.
				if ( ! ( rootCause instanceof InstanceNotFoundException) )
				{
					getMBeanLogger().warning( "can't create MBeanInfo for: " + getObjectName() +
						"\n" + rootCause.getClass() + ": " + rootCause.getMessage() + ":\n" +
						ExceptionUtil.getStackTrace( rootCause ) );
				}
				
				mMBeanInfo	= EMPTY_MBEAN_INFO;
			}
		}
		
		return( mMBeanInfo );
	}
}
























