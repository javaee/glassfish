/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/StatsFactory.java,v 1.1 2004/10/14 19:06:25 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:25 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Map;
import java.lang.reflect.Proxy;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import javax.management.j2ee.statistics.*;

import com.sun.cli.jmxcmd.util.jmx.OpenMBeanUtil;
import com.sun.cli.jmxcmd.util.j2ee.J2EEUtil;
import com.sun.cli.jcmd.util.misc.ClassUtil;

/**
	Factory to create Stats subclasses of any kind, based on supplied interface type 
	and a Map or CompositeData containing the Statistics.
 */
public final class StatsFactory 
{
	private	StatsFactory()	{}
	
	
	/**
		Create a new Stats using the specified CompositeData
		
		@param theInterface		interface which the Stats should implement, must extend Stats
	 */
		public static <T extends Stats> Stats
	create( Class<T> theInterface, final CompositeData data )
	{
		return( createStats( theInterface, compositeDataToMap( data ) ) );
	}
	
		public static Map<String,Object>
	compositeDataToMap( final CompositeData data )
	{
		return( OpenMBeanUtil.compositeDataToMap( data ) );
	}
	
	
	/**
		Create a Stats using Stats class found as data.getCompositeType().getTypeName().
		If this interface is not available, a generic Stats interface will be used.
	 */
		public static Stats
	createStats( final CompositeData data )
	{
		final String	typeName		= data.getCompositeType().getTypeName();
		Class<? extends Stats>			theInterface	= null;
		
		try
		{
			theInterface	= (Class<? extends Stats>)ClassUtil.classForName( typeName );
		}
		catch( Exception e )
		{
			theInterface	= Stats.class;
		}
		
		return( create( theInterface, data ) );
	}
	
	
	/**
		Create a new Stats using the specified Map.  The standard JSR 77
		Statistic types are handled appropriately. Custom (non-standard) Stats
		may also be used; in this case a proxy is returned which implements
		the interface specified by theClass.
		
		@param	theInterface	the Stats sub-interface which the resulting should implement
		@param	statistics		a Map containing keys of type String and their Statistic values
	 */
		public static <C extends Stats,T extends Statistic> Stats
	createStats( Class<C> theInterface, final Map<String,Object> statistics )
	{
		if ( ! Stats.class.isAssignableFrom( theInterface ) )
		{
			throw new IllegalArgumentException( theInterface.getName() );
		}

		Stats	result	= null;
		
		// generate a proxy
		final MapGetterInvocationHandler<Statistic>	handler	= new MapGetterInvocationHandler<Statistic>( statistics );
		final ClassLoader					classLoader	= theInterface.getClassLoader();
		
		result	= (Stats)Proxy.newProxyInstance(
					classLoader, new Class[] { theInterface }, handler);
		
		return( result );
	}
	
	/**
		Calls createStats( theInterface, J2EEUtil.statisticsToMap( statistics ) )
	 */
		public static Stats
	createStats( Class<? extends Stats> theInterface, final Statistic[] statistics )
	{
		return( createStats( theInterface, J2EEUtil.statisticsToMap( statistics ) ) );
	}
	
		public static <T extends Statistic> EJBStats
	createEJBStats( final Map<String,T> statistics )
	{
		return( (EJBStats)createStats( EJBStats.class, statistics ) );
	}
	
		public static <T extends Statistic> URLStats
	createURLStats( final Map<String,T> statistics )
	{
		return( (URLStats)createStats( URLStats.class, statistics ) );
	}
	
		public static <T extends Statistic> EntityBeanStats
	createEntityBeanStats( final Map<String,T> statistics )
	{
		return( (EntityBeanStats)createStats( EntityBeanStats.class, statistics ) );
	}
	
    /*
		public static <T extends Statistic> JavaMailStats
	createJavaMailStats( final Map<String,T> statistics )
	{
		return( (JavaMailStats)createStats( JavaMailStats.class, statistics ) );
	}
    */
	
		public static <T extends Statistic> JCAConnectionPoolStats
	createJCAConnectionPoolStats( final Map<String,T> statistics )
	{
		return( (JCAConnectionPoolStats)createStats( JCAConnectionPoolStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JCAConnectionStats
	createJCAConnectionStats( final Map<String,T> statistics )
	{
		return( (JCAConnectionStats)createStats( JCAConnectionStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JCAStats
	createJCAStats( final Map<String,T> statistics )
	{
		return( (JCAStats)createStats( JCAStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JDBCConnectionPoolStats
	createJDBCConnectionPoolStats( final Map<String,T> statistics )
	{
		return( (JDBCConnectionPoolStats)createStats( JDBCConnectionPoolStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JDBCConnectionStats
	createJDBCConnectionStats( final Map<String,T> statistics )
	{
		return( (JDBCConnectionStats)createStats( JDBCConnectionStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JDBCStats
	createJDBCStats( final Map<String,T> statistics )
	{
		return( (JDBCStats)createStats( JDBCStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JMSConnectionStats
	createJMSConnectionStats( final Map<String,T> statistics )
	{
		return( (JMSConnectionStats)createStats( JMSConnectionStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JMSConsumerStats
	createJMSConsumerStats( final Map<String,T> statistics )
	{
		return( (JMSConsumerStats)createStats( JMSConsumerStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JMSEndpointStats
	createJMSEndpointStats( final Map<String,T> statistics )
	{
		return( (JMSEndpointStats)createStats( JMSEndpointStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JMSProducerStats
	createJMSProducerStats( final Map<String,T> statistics )
	{
		return( (JMSProducerStats)createStats( JMSProducerStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JMSSessionStats
	createJMSSessionStats( final Map<String,T> statistics )
	{
		return( (JMSSessionStats)createStats( JMSSessionStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JMSStats
	createJMSStats( final Map<String,T> statistics )
	{
		return( (JMSStats)createStats( JMSStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JTAStats
	createJTAStats( final Map<String,T> statistics )
	{
		return( (JTAStats)createStats( JTAStats.class, statistics ) );
	}
	
		public static <T extends Statistic> JVMStats
	createJVMStats( final Map<String,T> statistics )
	{
		return( (JVMStats)createStats( JVMStats.class, statistics ) );
	}
	
		public static <T extends Statistic> MessageDrivenBeanStats
	createMessageDrivenBeanStats( final Map<String,T> statistics )
	{
		return( (MessageDrivenBeanStats)createStats( MessageDrivenBeanStats.class, statistics ) );
	}
	
		public static <T extends Statistic> ServletStats
	createServletStats( final Map<String,T> statistics )
	{
		return( (ServletStats)createStats( ServletStats.class, statistics ) );
	}
	
		public static <T extends Statistic> SessionBeanStats
	createSessionBeanStats( final Map<String,T> statistics )
	{
		return( (SessionBeanStats)createStats( SessionBeanStats.class, statistics ) );
	}
	
		public static <T extends Statistic> StatefulSessionBeanStats
	createStatefulSessionBeanStats( final Map<String,T> statistics )
	{
		return( (StatefulSessionBeanStats)createStats( StatefulSessionBeanStats.class, statistics ) );
	}
	
		public static <T extends Statistic> StatelessSessionBeanStats
	createStatelessSessionBeanStats( final Map<String,T> statistics )
	{
		return( (StatelessSessionBeanStats)createStats( StatelessSessionBeanStats.class, statistics ) );
	}
	

}





