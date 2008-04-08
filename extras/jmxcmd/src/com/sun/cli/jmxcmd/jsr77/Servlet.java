/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/Servlet.java,v 1.4 2004/10/14 19:07:07 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:07 $
 */
 
package com.sun.cli.jmxcmd.jsr77;
 

import javax.management.j2ee.statistics.ServletStats;

/**
 */
public interface Servlet extends J2EEManagedObject
{
	public final static String	J2EE_TYPE	= J2EETypes.SERVLET;
	
	public ServletStats	getStats();
	
	
	/**
		Class loading time

	*/
	public int	getClassLoadTime();

	/**
		Fully qualified class name of the managed object

	*/
	public String	getEngineName();

	/**
		Error count

	*/
	public int	getErrorCount();

	/**
		Event provider support for this managed object

	*/
	public boolean	getEventProvider();

	/**
		Load time

	*/
	public long	getLoadTime();

	/**
		Maximum processing time of a request

	*/
	public long	getMaxTime();

	/**
		Type of the modeled resource.

	*/
	public String	getModelerType();

	/**
		Cumulative processing time

	*/
	public long	getProcessingTime();

	/**
		Number of requests processed by this wrapper

	*/
	public int	getRequestCount();

// -------------------- Operations --------------------
	/**
		@return String[]
	*/
	public String[]	findMappings();



	
}
