/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EETypes.java,v 1.1 2004/10/14 19:07:04 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:07:04 $
 */
 
package com.sun.cli.jmxcmd.jsr77;

import java.util.Set;
import java.util.Collections;

import com.sun.cli.jcmd.util.misc.SetUtil;

/**
	See JSR 77.3-1.<br>
 */
public final class J2EETypes
{
	/**
		The javax.management.ObjectName property key denoting the type of the MBean.
	 */
	public final static String	J2EE_TYPE_KEY			= "j2eeType";
	
	/**
		The ObjectName property key denoting the name of the MBean.
	 */
	public final static String	NAME_KEY			= "name";
	
	public final static String	J2EE_DOMAIN					= "J2EEDomain";
	public final static String	J2EE_SERVER					= "J2EEServer";
	public final static String	J2EE_CLUSTER				= "X-J2EECluster";
	public final static String	J2EE_APPLICATION			= "J2EEApplication";
	public final static String	APP_CLIENT_MODULE			= "AppClientModule";
	public final static String	EJB_MODULE					= "EJBModule";
	public final static String	WEB_MODULE					= "WebModule";
	public final static String	RESOURCE_ADAPTER_MODULE		= "ResourceAdapterModule";
	public final static String	ENTITY_BEAN					= "EntityBean";
	public final static String	STATEFULL_SESSION_BEAN		= "StatefulSessionBean";
	public final static String	STATELESS_SESSION_BEAN		= "StatelessSessionBean";
	public final static String	MESSAGE_DRIVEN_BEAN			= "MessageDrivenBean";
	public final static String	SERVLET						= "Servlet";
	public final static String	JAVA_MAIL_RESOURCE			= "JavaMailResource";
	public final static String	JCA_RESOURCE				= "JCAResource";
	public final static String	JCA_CONNECTION_FACTORY		= "JCAConnectionFactory";
	public final static String	JCA_MANAGED_CONNECTION_FACTORY	= "JCAManagedConnectionFactory";
	public final static String	JDBC_RESOURCE				= "JDBCResource";
	public final static String	JDBC_DATA_SOURCE			= "JDBCDataSource";
	public final static String	JDBC_DRIVER					= "JDBCDriver";
	public final static String	JMS_RESOURCE				= "JMSResource";
	public final static String	JNDI_RESOURCE				= "JNDIResource";
	public final static String	JTA_RESOURCE				= "JTAResource";
	public final static String	RMI_IIOP_RESOURCE			= "RMI_IIOPResource";
	public final static String	URL_RESOURCE				= "URL_Resource";
	public final static String	JVM							= "JVM";
	
	/**
		Set consisting of all standard JSR 77 j2eeTypes
	 */
	public static final Set	ALL_STD	= Collections.unmodifiableSet(
	SetUtil.newSet( new String[]
	{
		J2EE_DOMAIN,
		J2EE_SERVER,
		J2EE_APPLICATION,
		APP_CLIENT_MODULE,
		EJB_MODULE,
		WEB_MODULE,
		RESOURCE_ADAPTER_MODULE,
		ENTITY_BEAN,
		STATEFULL_SESSION_BEAN,
		STATELESS_SESSION_BEAN,
		MESSAGE_DRIVEN_BEAN,
		SERVLET,
		JAVA_MAIL_RESOURCE,
		JCA_RESOURCE,
		JCA_CONNECTION_FACTORY,
		JCA_MANAGED_CONNECTION_FACTORY,
		JDBC_RESOURCE,
		JDBC_DATA_SOURCE,
		JDBC_DRIVER,
		JMS_RESOURCE,
		JNDI_RESOURCE,
		JTA_RESOURCE,
		RMI_IIOP_RESOURCE,
		URL_RESOURCE,
		JVM,
	} ) );


}
