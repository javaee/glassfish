/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/EntityBean.java,v 1.4 2004/10/14 19:07:02 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:02 $
 */
 
package com.sun.cli.jmxcmd.jsr77;
 

import javax.management.j2ee.statistics.EntityBeanStats;

/**
 */
public interface EntityBean extends EJB
{
	public final static String	J2EE_TYPE	= J2EETypes.ENTITY_BEAN;
	

	public EntityBeanStats	getStats();
	
}
