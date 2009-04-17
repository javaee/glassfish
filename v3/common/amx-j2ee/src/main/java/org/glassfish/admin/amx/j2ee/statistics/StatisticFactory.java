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

import org.glassfish.admin.amx.util.jmx.OpenMBeanUtil;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.TypeCast;

import javax.management.j2ee.statistics.BoundaryStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.openmbean.CompositeData;
import java.util.Map;

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
	create( Class<? extends Statistic> theInterface, final CompositeData data )
	{
		return( create( theInterface, OpenMBeanUtil.compositeDataToMap( data ) ) );
	}
	
	private static final String	COUNT_STATISTIC			= CountStatistic.class.getName();
	private static final String	TIME_STATISTIC			= TimeStatistic.class.getName();
	private static final String	RANGE_STATISTIC			= RangeStatistic.class.getName();
	private static final String	BOUNDARY_STATISTIC		= BoundaryStatistic.class.getName();
	private static final String	BOUNDED_RANGE_STATISTIC	= BoundedRangeStatistic.class.getName();
	private static final String	STRING_STATISTIC		= StringStatistic.class.getName();
	private static final String	MAP_STATISTIC			= MapStatistic.class.getName();
	private static final String	NUMBER_STATISTIC                = NumberStatistic.class.getName();
	
	
	
	/**
		Create a new Statistic using the specified CompositeData.
		The CompositeType of data must be an appropriate Statistic class.
	 */
		public static Statistic
	create( final CompositeData data )
	{
		final String	typeName	= data.getCompositeType().getTypeName();
		Class<? extends Statistic>	theInterface	= null;
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
		else if ( typeName.equals( STRING_STATISTIC ) )
		{
			theInterface	= StringStatistic.class;
		}
		else if ( typeName.equals( NUMBER_STATISTIC ) )
		{
			theInterface	= NumberStatistic.class;
		}
		else if ( typeName.equals( MAP_STATISTIC ) )
		{
			theInterface	= MapStatistic.class;
		}
		else
		{
			try
			{
				theInterface	= TypeCast.asClass( ClassUtil.classForName( typeName ) );
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
	create( Class<? extends Statistic> theInterface, final Map<String,?> mappings )
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
		else if ( theInterface == StringStatistic.class )
		{
			result	= new StringStatisticImpl( mappings );
		}
		else if ( theInterface == NumberStatistic.class )
		{
			result	= new NumberStatisticImpl( mappings );
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
	

	
	private static final Class<? extends Statistic>[] KNOWN_STATISTICS = TypeCast.asArray( new Class[]
	{
		CountStatistic.class,
		TimeStatistic.class,
		BoundedRangeStatistic.class,	// must come before RangeStatistic,BoundaryStatistic
		RangeStatistic.class,
		BoundaryStatistic.class,
		StringStatistic.class,
		NumberStatistic.class,
	} );
	
	private static final String	INTERNAL_STRING_STATISTIC_CLASSNAME	=
		"com.sun.enterprise.admin.monitor.stats.StringStatisticImpl";
		
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
		
		if ( theInterface == MapStatistic.class && ! (s instanceof MapStatisticImpl) )
		{
			if ( s.getClass().getName().equals( INTERNAL_STRING_STATISTIC_CLASSNAME ) )
			{
				theInterface	= StringStatistic.class;
			}
			else
			{
				throw new IllegalArgumentException( "Unknown statistic class: " + s.getClass().getName() );
			}
		}
		
		return( theInterface );
	}
	
		

}





























