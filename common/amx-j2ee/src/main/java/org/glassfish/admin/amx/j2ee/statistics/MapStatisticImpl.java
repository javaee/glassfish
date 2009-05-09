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
import org.glassfish.admin.amx.util.MapUtil;

import javax.management.j2ee.statistics.Statistic;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
	Generic implementation of Statistic which contains its members in a Map.
 */
public class MapStatisticImpl implements MapStatistic, Serializable
{
	static final long serialVersionUID = -5921306849633125922L;
	
	final Map<String,Object>	mItems;
	
	/**
	 */
		public
	MapStatisticImpl( final Map<String,?> map )
	{
		mItems	= new HashMap<String,Object>( map );
	}
	
	/**
	 */
		public
	MapStatisticImpl( final Statistic statistic )
	{
		if ( statistic instanceof MapStatistic )
		{
			mItems	= new HashMap<String,Object>();
			
			mItems.putAll( ((MapStatistic)statistic).asMap() );
		}
		else
		{
			mItems	= J2EEUtil.statisticToMap( statistic );
		}
	}
	
	/**
		Get a Statistic value which is expected to be a Long (long)
	 */
		public final long
	getlong( final String name )
	{
		final Object	value	= getValue( name );
		
		if ( ! (value instanceof Long) )
		{
			throw new IllegalArgumentException( 
				"MapStatisticImpl.getLong: expecting Long for " + name +
				", got " + value + " of class " + value.getClass() +
				", all values: " + toString() );
			
		}
			
		return( ((Long)value).longValue() );
	}
	
	/**
		Get a Statistic value which is expected to be an Integer (int)
	 */
		public final int
	getint( final String name )
	{
		return( ((Integer)getValue( name )).intValue() );
	}
	
	
	/**
		Get a Statistic value which is expected to be a String
	 */
		public final String
	getString( final String name )
	{
		return( (String)getValue( name ) );
	}

	
	/**
		Get a Statistic value which is expected to be any Object
	 */
		public final Object
	getValue( final String name )
	{
		final Object	value	= mItems.get( name );
		
		if ( value == null && ! mItems.containsKey( name ) )
		{
			throw new IllegalArgumentException( name );
		}
		
		return( value );
	}
	

	
	/**
		Get the description for this Statistic
	 */
 		public String
 	getDescription()
 	{
 		return( getString( "Description" ) );
 	}
 	
	
	/**
		Get the last sample time for this Statistic
	 */
 		public long
 	getLastSampleTime()
 	{
 		return( getlong( "LastSampleTime" ) );
 	}
 	
	/**
		Get the name of this Statistic
	 */
		public String
	getName()
	{
 		return( getString( "Name" ) );
	}
	
	/**
		Get the name of this Statistic
	 */
		public String
	setName( final String newName )
	{
		if ( newName == null || newName.length() == 0 )
		{
			throw new IllegalArgumentException();
		}
		
		final String	oldName	= getName();
		
		mItems.put( "Name", newName );
		
 		return( oldName  );
	}
	
	/**
		Get the start time for this Statistic
	 */
		public long
	getStartTime()
	{
 		return( getlong( "StartTime" ) );
	}
	
	
	/**
		Get the units associated with this statistic.
	 */
		public String
	getUnit()
	{
 		return( getString( "Unit" ) );
	}
	
	/**
		Get the fields associated with this statistic.
		
		Note the name--"get" is avoided so it won't be introspected
		as another Statistic field.
		
		@return an unmodifiableSet of the field names (String)
	 */
		public Set
	valueNames()
	{
 		return( Collections.unmodifiableSet( mItems.keySet() ) );
	}
	
	
		public Map<String,Object>
	asMap()
	{
		return( Collections.unmodifiableMap( mItems ) );
	}
	
		public String
	toString()
	{
		return( MapUtil.toString( mItems ) );
	}
	
 	    public int
 	hashCode()
 	{
 	    return mItems.hashCode();
 	}
	
		public boolean
	equals( final Object rhs )
	{
		boolean	equals	= false;
		
		if ( rhs instanceof MapStatistic )
		{
			equals	= MapUtil.mapsEqual( asMap(), ((MapStatistic)rhs).asMap() );
		}
		return( equals );
	}
}





