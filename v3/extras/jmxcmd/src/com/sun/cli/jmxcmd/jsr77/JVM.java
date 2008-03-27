/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/JVM.java,v 1.4 2004/10/14 19:07:05 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:05 $
 */
 
package com.sun.cli.jmxcmd.jsr77;


/**
	Identifies a Java VM being utilized by a server.
 */
public interface JVM extends J2EEManagedObject
{
	public final static String	J2EE_TYPE	= J2EETypes.JVM;
	
	/**
		Note that the Attribute name is case-sensitive
		"javaVendor" as defined by JSR 77.
	 */
	public String		getjavaVendor();
	
	/**
		Note that the Attribute name is case-sensitive
		"javaVersion" as defined by JSR 77.
	 */
	public String		getjavaVersion();
	
	/**
		Note that the Attribute name is case-sensitive
		"node" as defined by JSR 77.
	 
	 	@return the fully-qualified hostname
	 */
	public String	getnode();


}
