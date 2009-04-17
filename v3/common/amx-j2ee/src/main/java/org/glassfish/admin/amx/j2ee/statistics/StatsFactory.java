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
import org.glassfish.admin.amx.util.jmx.OpenMBeanUtil;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.TypeCast;

import javax.management.j2ee.statistics.*;
import javax.management.openmbean.CompositeData;
import java.lang.reflect.Proxy;
import java.util.Map;

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
	create( Class<? extends Stats> theInterface, final CompositeData data )
	{
	    final Map<String,Statistic> statistics  = TypeCast.asMap( compositeDataToMap( data ) );
	    
		return( createStats( theInterface, statistics ) );
	}
	
		public static Map<String,Statistic>
	compositeDataToMap( final CompositeData data )
	{
		return( TypeCast.asMap( OpenMBeanUtil.compositeDataToMap( data ) ) );
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
			theInterface	= TypeCast.asClass( ClassUtil.classForName( typeName ) );
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
		public static <T extends Stats> T
	createStats( Class<T> theInterface, final Map<String,Statistic> statistics )
	{
		if ( ! Stats.class.isAssignableFrom( theInterface ) )
		{
			throw new IllegalArgumentException( theInterface.getName() );
		}

		// generate a proxy
		final MapGetterInvocationHandler	handler	=
		    new MapGetterInvocationHandler<Statistic>( statistics );
		final ClassLoader					classLoader	= theInterface.getClassLoader();
		
		final Class<T>[]    interfaces  = TypeCast.asArray( new Class[] { theInterface } );
		
		final Object proxy  = Proxy.newProxyInstance( classLoader, interfaces, handler);
		
		return theInterface.cast( proxy );
	}
	
	private void test()
	{
	    createStats( EJBStats.class, new java.util.HashMap<String,Statistic>() );
	}
	
	/**
		Calls createStats( theInterface, J2EEUtil.statisticsToMap( statistics ) )
	 */
		public static <T extends Stats> T
	createStats( Class<T> theInterface, final Statistic[] statistics )
	{
	    final Map<String,Statistic>  statisticsMap  = J2EEUtil.statisticsToMap( statistics );
	    
		return( createStats( theInterface, statisticsMap ) );
	}
	
		public static EJBStats
	createEJBStats( final Map<String,Statistic> statistics )
	{
		return( createStats( EJBStats.class, statistics ) );
	}
	
		public static URLStats
	createURLStats( final Map<String,Statistic> statistics )
	{
		return( createStats( URLStats.class, statistics ) );
	}
	
		public static EntityBeanStats
	createEntityBeanStats( final Map<String,Statistic> statistics )
	{
		return( createStats( EntityBeanStats.class, statistics ) );
	}
	
		public static JCAConnectionPoolStats
	createJCAConnectionPoolStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JCAConnectionPoolStats.class, statistics ) );
	}
	
		public static JCAConnectionStats
	createJCAConnectionStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JCAConnectionStats.class, statistics ) );
	}
	
		public static JCAStats
	createJCAStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JCAStats.class, statistics ) );
	}
	
		public static JDBCConnectionPoolStats
	createJDBCConnectionPoolStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JDBCConnectionPoolStats.class, statistics ) );
	}
	
		public static JDBCConnectionStats
	createJDBCConnectionStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JDBCConnectionStats.class, statistics ) );
	}
	
		public static JDBCStats
	createJDBCStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JDBCStats.class, statistics ) );
	}
	
		public static JMSConnectionStats
	createJMSConnectionStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JMSConnectionStats.class, statistics ) );
	}
	
		public static JMSConsumerStats
	createJMSConsumerStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JMSConsumerStats.class, statistics ) );
	}
	
		public static JMSEndpointStats
	createJMSEndpointStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JMSEndpointStats.class, statistics ) );
	}
	
		public static JMSProducerStats
	createJMSProducerStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JMSProducerStats.class, statistics ) );
	}
	
		public static JMSSessionStats
	createJMSSessionStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JMSSessionStats.class, statistics ) );
	}
	
		public static JMSStats
	createJMSStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JMSStats.class, statistics ) );
	}
	
		public static JTAStats
	createJTAStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JTAStats.class, statistics ) );
	}
	
		public static JVMStats
	createJVMStats( final Map<String,Statistic> statistics )
	{
		return( createStats( JVMStats.class, statistics ) );
	}
	
		public static MessageDrivenBeanStats
	createMessageDrivenBeanStats( final Map<String,Statistic> statistics )
	{
		return( createStats( MessageDrivenBeanStats.class, statistics ) );
	}
	
		public static ServletStats
	createServletStats( final Map<String,Statistic> statistics )
	{
		return( createStats( ServletStats.class, statistics ) );
	}
	
		public static SessionBeanStats
	createSessionBeanStats( final Map<String,Statistic> statistics )
	{
		return( createStats( SessionBeanStats.class, statistics ) );
	}
	
		public static StatefulSessionBeanStats
	createStatefulSessionBeanStats( final Map<String,Statistic> statistics )
	{
		return( createStats( StatefulSessionBeanStats.class, statistics ) );
	}
	
		public static StatelessSessionBeanStats
	createStatelessSessionBeanStats( final Map<String,Statistic> statistics )
	{
		return( createStats( StatelessSessionBeanStats.class, statistics ) );
	}
	
		public static JavaMailStats
	createJavaMailStats( final Map<String,Statistic> statistics )
	{
	    // bizarre compiler error in JDK 1.5.0_06 forces us to inline the code here
	    final Class<JavaMailStats>  theInterface    = JavaMailStats.class;
		JavaMailStats	result	= null;
		
		// generate a proxy
		final MapGetterInvocationHandler	handler	=
		    new MapGetterInvocationHandler<Statistic>( statistics );
		final ClassLoader					classLoader	= theInterface.getClassLoader();
		
		final Class<JavaMailStats>[]    interfaces  = TypeCast.asArray( new Class[] { theInterface } );
		
		result	= (JavaMailStats)Proxy.newProxyInstance( classLoader, interfaces, handler);
		
		return result;
	}

}





