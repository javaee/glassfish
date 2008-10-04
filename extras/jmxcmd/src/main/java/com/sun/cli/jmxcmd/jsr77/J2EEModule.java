/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EEModule.java,v 1.4 2004/10/14 19:07:03 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:03 $
 */
 
package com.sun.cli.jmxcmd.jsr77;

import javax.management.ObjectName;


/**
 */
public interface J2EEModule extends J2EEDeployedObject
{
		
	/**
		Note that the Attribute name is case-sensitive
		"javaVM" as defined by JSR 77.
		
	 	@return String[] of ObjectName
	 */
	public String[]	getjavaVMs();

	/**
		@return the JVM this module runs in
	 */
	public JVM			getJVM();
}
