/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/EventProvider.java,v 1.4 2004/10/14 19:07:02 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:02 $
 */
 
package com.sun.cli.jmxcmd.jsr77;


/**
 */
public interface EventProvider 
{
	/**
		<p>
		Note that the Attribute name is case-sensitive
		"types" as defined by JSR 77.
	 */
	public String[]	gettypes();
}
