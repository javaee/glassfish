/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/MapGetterInvocationHandler.java,v 1.1 2004/10/14 19:06:24 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:24 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Map;
import java.io.Serializable;

import com.sun.cli.jcmd.util.misc.MapUtil;

/**
	Implements getXXX() based on a Map whose keys are the XXX part of the getXXX() method name.
	Serializable so that it may be used to return a result remotely.
	<br><b>Internal use only</b>
 */
public class MapGetterInvocationHandler
	extends GetterInvocationHandler
	implements Serializable 
{
	static final long serialVersionUID = -8751876448821319456L;

	private final Map	mMap;
	
	/**
		Create a new instance using the Map, which is <b>not</b> copied.
	 */
		public
	MapGetterInvocationHandler( final Map map )
	{
		mMap	= map;
	}
	
		protected Map
	getMap()
	{
		return( mMap );
	}
	
		protected Object
	getValue( final String name )
	{
		return( mMap.get( name ) );
	}

		protected boolean
	containsValue( String name )
	{
		return( mMap.containsKey( name ) );
	}
	
		public boolean
	equals( final Object rhs )
	{
		boolean	equals	= false;
		
		if ( rhs instanceof MapGetterInvocationHandler )
		{
			equals	= MapUtil.mapsEqual( mMap,
				((MapGetterInvocationHandler)rhs).mMap );
		}
		 
		return( equals );
	}
	
	
		public String
	toString( )
	{
		return( MapUtil.toString( mMap ) );
	}
}





