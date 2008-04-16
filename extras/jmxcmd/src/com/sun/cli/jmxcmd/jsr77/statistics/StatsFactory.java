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
		public static Stats
	create( Class theInterface, final CompositeData data )
	{
		return( createStats( theInterface, compositeDataToMap( data ) ) );
	}
	
		public static Map
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
		Class			theInterface	= null;
		
		try
		{
			theInterface	= ClassUtil.classForName( typeName );
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
		public static Stats
	createStats( Class theInterface, final Map statistics )
	{
		if ( ! Stats.class.isAssignableFrom( theInterface ) )
		{
			throw new IllegalArgumentException( theInterface.getName() );
		}

		Stats	result	= null;
		
		// generate a proxy
		final MapGetterInvocationHandler	handler	= new MapGetterInvocationHandler( statistics );
		final ClassLoader					classLoader	= theInterface.getClassLoader();
		
		result	= (Stats)Proxy.newProxyInstance(
					classLoader, new Class[] { theInterface }, handler);
		
		return( result );
	}
	
	/**
		Calls createStats( theInterface, J2EEUtil.statisticsToMap( statistics ) )
	 */
		public static Stats
	createStats( Class theInterface, final Statistic[] statistics )
	{
		return( createStats( theInterface, J2EEUtil.statisticsToMap( statistics ) ) );
	}
	
		public static EJBStats
	createEJBStats( final Map statistics )
	{
		return( (EJBStats)createStats( EJBStats.class, statistics ) );
	}
	
		public static URLStats
	createURLStats( final Map statistics )
	{
		return( (URLStats)createStats( URLStats.class, statistics ) );
	}
	
		public static EntityBeanStats
	createEntityBeanStats( final Map statistics )
	{
		return( (EntityBeanStats)createStats( EntityBeanStats.class, statistics ) );
	}
	
		public static JavaMailStats
	createJavaMailStats( final Map statistics )
	{
		return( (JavaMailStats)createStats( JavaMailStats.class, statistics ) );
	}
	
		public static JCAConnectionPoolStats
	createJCAConnectionPoolStats( final Map statistics )
	{
		return( (JCAConnectionPoolStats)createStats( JCAConnectionPoolStats.class, statistics ) );
	}
	
		public static JCAConnectionStats
	createJCAConnectionStats( final Map statistics )
	{
		return( (JCAConnectionStats)createStats( JCAConnectionStats.class, statistics ) );
	}
	
		public static JCAStats
	createJCAStats( final Map statistics )
	{
		return( (JCAStats)createStats( JCAStats.class, statistics ) );
	}
	
		public static JDBCConnectionPoolStats
	createJDBCConnectionPoolStats( final Map statistics )
	{
		return( (JDBCConnectionPoolStats)createStats( JDBCConnectionPoolStats.class, statistics ) );
	}
	
		public static JDBCConnectionStats
	createJDBCConnectionStats( final Map statistics )
	{
		return( (JDBCConnectionStats)createStats( JDBCConnectionStats.class, statistics ) );
	}
	
		public static JDBCStats
	createJDBCStats( final Map statistics )
	{
		return( (JDBCStats)createStats( JDBCStats.class, statistics ) );
	}
	
		public static JMSConnectionStats
	createJMSConnectionStats( final Map statistics )
	{
		return( (JMSConnectionStats)createStats( JMSConnectionStats.class, statistics ) );
	}
	
		public static JMSConsumerStats
	createJMSConsumerStats( final Map statistics )
	{
		return( (JMSConsumerStats)createStats( JMSConsumerStats.class, statistics ) );
	}
	
		public static JMSEndpointStats
	createJMSEndpointStats( final Map statistics )
	{
		return( (JMSEndpointStats)createStats( JMSEndpointStats.class, statistics ) );
	}
	
		public static JMSProducerStats
	createJMSProducerStats( final Map statistics )
	{
		return( (JMSProducerStats)createStats( JMSProducerStats.class, statistics ) );
	}
	
		public static JMSSessionStats
	createJMSSessionStats( final Map statistics )
	{
		return( (JMSSessionStats)createStats( JMSSessionStats.class, statistics ) );
	}
	
		public static JMSStats
	createJMSStats( final Map statistics )
	{
		return( (JMSStats)createStats( JMSStats.class, statistics ) );
	}
	
		public static JTAStats
	createJTAStats( final Map statistics )
	{
		return( (JTAStats)createStats( JTAStats.class, statistics ) );
	}
	
		public static JVMStats
	createJVMStats( final Map statistics )
	{
		return( (JVMStats)createStats( JVMStats.class, statistics ) );
	}
	
		public static MessageDrivenBeanStats
	createMessageDrivenBeanStats( final Map statistics )
	{
		return( (MessageDrivenBeanStats)createStats( MessageDrivenBeanStats.class, statistics ) );
	}
	
		public static ServletStats
	createServletStats( final Map statistics )
	{
		return( (ServletStats)createStats( ServletStats.class, statistics ) );
	}
	
		public static SessionBeanStats
	createSessionBeanStats( final Map statistics )
	{
		return( (SessionBeanStats)createStats( SessionBeanStats.class, statistics ) );
	}
	
		public static StatefulSessionBeanStats
	createStatefulSessionBeanStats( final Map statistics )
	{
		return( (StatefulSessionBeanStats)createStats( StatefulSessionBeanStats.class, statistics ) );
	}
	
		public static StatelessSessionBeanStats
	createStatelessSessionBeanStats( final Map statistics )
	{
		return( (StatelessSessionBeanStats)createStats( StatelessSessionBeanStats.class, statistics ) );
	}
	

}





