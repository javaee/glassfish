/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EEApplication.java,v 1.4 2004/10/14 19:07:02 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:02 $
 */
 
package com.sun.cli.jmxcmd.jsr77;
 
import java.util.Set;


/**
 */
public interface J2EEApplication extends J2EEDeployedObject
{
	public final static String	J2EE_TYPE	= J2EETypes.J2EE_APPLICATION;
	
	/**
		@return the ObjectNames of the modules, as Strings
 		<p>
		Note that the Attribute name is case-sensitive
		"modules" as defined by JSR 77.
	 */
	public String[]	getmodules();


	public Set	getModuleSet();
}
