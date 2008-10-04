/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EEServer.java,v 1.4 2004/10/14 19:07:03 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:03 $
 */
 
package com.sun.cli.jmxcmd.jsr77;

import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;


/**
 */
public interface J2EEServer extends J2EELogicalServer
{
	public final static String		J2EE_TYPE	= J2EETypes.J2EE_SERVER;
	
	/**
		Note that the Attribute name is case-sensitive
		"deployedObjects" as defined by JSR 77.
		
	 	@return the ObjectNames as Strings
	 */
	public String[]	getdeployedObjects();
	
	/**
		Return Set of all deployed objects.  Possible types include:
		<ul>
		<li>{@link com.sun.cli.jmxcmd.jsr77.J2EEApplication}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.WebModule}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.EJBModule}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.AppClientModule}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.ResourceAdapterModule}</li>
		</ul>
		<p>
		To obtain Resources of a particular type, use
		{@link Container#getContaineeMap}(j2eeType).
	 */
	public Set	getDeployedObjectsSet();
	
	/**
		In 8.1, there will only ever be one JVM for a J2EEServer.
		Note that the Attribute name is case-sensitive
		"javaVMs" as defined by JSR 77.
		
	 	@return the ObjectNames as Strings
	 */
	public String[]	getjavaVMs();
	
	/**
		In 8.1, there is only a single JVM for a J2EEServer.
		@return JVM
	 */
	public Map		getJVM();
	
	/**
		Note that the Attribute name is case-sensitive
		"resources" as defined by JSR 77.
		
	 	@return the ObjectNames as Strings
	 */
	public String[]		getresources();
	
	/**
		Return Set of all resources.  Possible types include:
		<ul>
		<li>{@link com.sun.cli.jmxcmd.jsr77.JDBCResource}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.JavaMailResource}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.JCAResource}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.JMSResource}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.JNDIResource}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.JTAResource}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.RMIIIOPResource}</li>
		<li>{@link com.sun.cli.jmxcmd.jsr77.URLResource}</li>
		</ul>
		<p>
		To obtain Resources of a particular type, use
		{@link Container#getContaineeMap}(j2eeType).
	 */
	public Set			getResourcesSet();
	
	/**
		Note that the Attribute name is case-sensitive
		"serverVendor" as defined by JSR 77.
		
	 	@return the server vendor, a free-form String
	 */
	public String		getserverVendor();
	
	/**
		Note that the Attribute name is case-sensitive
		"serverVersion" as defined by JSR 77.
		
	 	@return the server version, a free-form String
	 */
	public String		getserverVersion();
	



}
