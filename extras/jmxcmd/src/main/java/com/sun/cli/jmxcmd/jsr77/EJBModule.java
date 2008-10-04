/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/EJBModule.java,v 1.4 2004/10/14 19:07:02 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:02 $
 */
 
package com.sun.cli.jmxcmd.jsr77;

import java.util.Set;


/**
 */
public interface EJBModule extends J2EEModule
{
	public final static String	J2EE_TYPE	= J2EETypes.EJB_MODULE;
	
	/**
		Note that the Attribute name is case-sensitive
		"servlets" as defined by JSR 77.
		
		@return the ObjectNames of the ejbs, as Strings
	 */
	public String[]	getejbs();


	public Set	getEJBSet();
}
