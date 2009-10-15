/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/MapStringifier.java,v 1.2 2005/11/08 22:39:27 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:39:27 $
 */
 
package org.glassfish.admin.amx.util.stringifier;

import java.util.Map;
import org.glassfish.admin.amx.util.MapUtil;

/**
	Stringifies an Iterator, using an optional element Stringifier
 */
 
public final class MapStringifier implements Stringifier
{
	private final String	mItemsDelim;

		public 
	MapStringifier(  )
	{
		this( "," );
	}
	
		public 
	MapStringifier( final String delim )
	{
		mItemsDelim		= delim;
	}
	
	
	/*
		Static variant when direct call will suffice.
	 */
		public static String
	stringify( final Map m, final String delim )
	{
		if ( m == null )
		{
			return( "null" );
		}

		final MapStringifier	stringifier	= new MapStringifier( delim );
		
		return( stringifier.stringify( m ) );
	}
	
	/*
		Static variant when direct call will suffice.
	 */
		public String
	stringify( Object o )
	{
		assert( o instanceof Map ) : "not a Map: " + o.getClass().getName();
		
		return( MapUtil.toString( (Map)o, mItemsDelim ) );
	}
	
	
	public final static MapStringifier DEFAULT = new MapStringifier( "," );
}

