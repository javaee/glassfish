/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/mbeans/com/sun/enterprise/jmx/kstat/kstatMgrMBean.java,v 1.2 2003/10/03 22:43:33 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/10/03 22:43:33 $
 */
package com.sun.enterprise.jmx.kstat;

/**
	Solaris-specific MBean to support kstat
 */
public interface kstatMgrMBean
{
	public void	initkstats() throws Exception;	// load all kstat MBeans
	public void	clearkstats();	// clear all kstat MBeans
	public void	refresh( String scopedName ) throws Exception;
	public void	refresh( ) throws Exception;
};



