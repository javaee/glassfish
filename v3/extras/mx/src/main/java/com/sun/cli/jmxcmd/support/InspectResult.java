/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.support;

import java.io.Serializable;

import javax.management.*;


/**
	The result of inspecting an MBean. This class should be treated as a struct; code should
	not be added to it and it should remain final.
 */
public final class InspectResult implements Serializable
{
	public ObjectInstance				objectInstance;
	public String						summary;
	/**
		The entire MBeanInfo (if requested)
	 */
	public MBeanInfo					mbeanInfo;
	
	/**
		The requested Attributes
	 */
	public MBeanAttributeInfo []		attrInfo;
	
	/**
		The requested operations
	 */
	public MBeanOperationInfo []		operationsInfo;
	
	/**
		The requested constructors
	 */
	public MBeanConstructorInfo []		constructorsInfo;
	
	/**
		The requested notifications
	 */
	public MBeanNotificationInfo []		notificationsInfo;
	public boolean						includeDescription;
	
		public
	InspectResult( ObjectInstance instance )
	{
		objectInstance		= instance;
		mbeanInfo			= null;
		summary				= null;
		attrInfo			= null;
		operationsInfo		= null;
		constructorsInfo	= null;
		notificationsInfo	= null;
		includeDescription	= true;
	}
	
		public
	InspectResult(
		ObjectInstance				instance,
		MBeanInfo					mbeanInfoIn,
		String						summaryIn,
		MBeanAttributeInfo []		attributes,
		MBeanOperationInfo []		operations,
		MBeanConstructorInfo []		constructors,
		MBeanNotificationInfo []	notifs)
	{
		mbeanInfo			= mbeanInfoIn;
		objectInstance		= instance;
		summary				= summaryIn;
		attrInfo			= attributes;
		operationsInfo		= operations;
		constructorsInfo	= constructors;
		notificationsInfo	= notifs;
		includeDescription	= true;
	}
}



