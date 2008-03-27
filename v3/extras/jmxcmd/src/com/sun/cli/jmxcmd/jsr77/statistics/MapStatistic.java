/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/statistics/MapStatistic.java,v 1.1 2004/10/14 19:06:24 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:06:24 $
 */

package com.sun.cli.jmxcmd.jsr77.statistics;

import java.util.Set;
import java.util.Map;

import javax.management.j2ee.statistics.Statistic;

/**
	A Statistic which contains its members in a Map.
 */
public interface MapStatistic extends Statistic
{
	/**
		Get a Statistic value which is expected to be a Long (long)
	 */
	public long	getlong( String name );
	
	/**
		Get a Statistic value which is expected to be an Integer (int)
	 */
	public int	getint( String name );
	
	
	/**
		Get a Statistic value which is expected to be a String
	 */
	public String	getString( String name );

	
	/**
		Get a Statistic value which is expected to be any Object
	 */
	public Object	getValue( String name );
	
	/**
		Set the name of this Statistic
	 */
	public String	setName( final String newName );

	
	/**
		Get the values associated with this statistic.
		
		Note the name--"get" is avoided so it won't be introspected
		as another Statistic field.
		
		@return an unmodifiableSet of the field names (String)
	 */
	public Set	valueNames();
	
	
	public Map		asMap();
	public String	toString();
}





