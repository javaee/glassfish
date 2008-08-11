/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/StatisticsProvider.java,v 1.1 2004/10/14 19:07:08 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:07:08 $
 */

package com.sun.cli.jmxcmd.jsr77;

import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;

/**
	Defines access to Stats.
 */
public interface StatisticsProvider
{
	/**
		Return a Stats object
		
		@return the Stats object or null if not available
	 */
	public Stats		getStats();
	
	/**
		Return the ObjectName of the MonitoringMBean which provides
		monitoring statistics, or null if not available.
		<bold>Note</bold>: this extends the JSR 77 specification
		
		@return the ObjectName or null if not available
	 */
	public ObjectName	getMonitoringObjectName();
}
