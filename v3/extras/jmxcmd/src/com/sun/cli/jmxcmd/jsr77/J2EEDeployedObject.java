/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EEDeployedObject.java,v 1.4 2004/10/14 19:07:02 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:02 $
 */
 
package com.sun.cli.jmxcmd.jsr77;

/**
 */
public interface J2EEDeployedObject extends J2EEManagedObject, StateManageable
{
	/**
		The deploymentDescriptor string must contain the original XML
		deployment descriptor that was created for this module during
		the deployment process.
		<p>
		Note that the Attribute name is case-sensitive
		"deploymentDescriptor" as defined by JSR 77.
	*/
	public String	getdeploymentDescriptor();
	
	/**
		The J2EEServer this module is deployed on.
		Get the ObjectNames, as String.
		<p>
		Note that the Attribute name is case-sensitive
		"server" as defined by JSR 77.
		
		@return the ObjectName of the server, as a String
	 */
	public String	getserver();


	public J2EEServer	getServer();
}
