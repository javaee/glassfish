/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EEManagedObject.java,v 1.4 2004/10/14 19:07:03 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:03 $
 */
 
package com.sun.cli.jmxcmd.jsr77;
 


 /**
 	The J2EEManagedObject model is the base model of all managed objects
 	in the J2EE Management Model. All managed objects in the J2EE Platform
 	must implement the J2EEManagedObject model.
  */
 public interface J2EEManagedObject
 {
 	/**
 		The ObjectName of the J2EEManagedObject.
 		All managed objects must have a unique name within the context of
 		the management domain. The name must not be null.
 		<p>
		Note that the Attribute name is case-sensitive
		"getobjectName" as defined by JSR 77.
 		
 		@return the ObjectName of the object, as a String
 	 */
	public String	getobjectName();
	
	/**
		If true, indicates that the managed object provides event
		notification about events that occur on that object.
		
 		NOTE: JSR 77 defines the Attribute name as "eventProvider".
	 */
	public boolean		iseventProvider();
	
	/**
		If true, indicates that this managed object implements the
		StateManageable model and is state manageable.
		<p>
		Note that the Attribute name is case-sensitive
		"stateManageable" as defined by JSR 77.
	 */
	public boolean		isstateManageable();
	
	/**
		If true, indicates that the managed object supports performance
		statistics and therefore implements the StatisticsProvider model.
		<p>
		Note that the Attribute name is case-sensitive
		"statisticProvider" as defined by JSR 77.
	 */
	public boolean		isstatisticProvider();

}
