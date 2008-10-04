/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EEDomain.java,v 1.4 2004/10/14 19:07:02 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:02 $
 */
 
package com.sun.cli.jmxcmd.jsr77;

import java.util.Map;


/**
	The discovery and navigation of all managed objects in the J2EE
	management system begins with the J2EEDomain.

	@see J2EEServer
	@see J2EECluster
	@see J2EEApplication
	@see JVM
	@see AppClientModule
	@see EJBModule
	@see WebModule
	@see ResourceAdapterModule
	@see EntityBean
	@see StatefulSessionBean
	@see StatelessSessionBean
	@see MessageDrivenBean
	@see Servlet
	@see JavaMailResource
	@see JCAResource
	@see JCAConnectionFactory
	@see JCAManagedConnectionFactory
	@see JDBCResource
	@see JDBCDataSource
	@see JDBCDriver
	@see JMSResource
	@see JNDIResource
	@see JTAResource
	@see RMIIIOPResource
	@see URLResource
 */
public interface J2EEDomain
	extends J2EEManagedObject
{
	public final static String		J2EE_TYPE	= J2EETypes.J2EE_DOMAIN;
	
	/**
		Note that the Attribute name is case-sensitive
		"servers" as defined by JSR 77.
		
		@return the ObjectNames of the J2EEServers, as Strings
	 */
	public String[]	getservers();


	/**
		@return a Map keyed by server name of J2EEServer
	 */
	public Map		getServerMap();
	
	/**
		@return the ObjectNames of the J2EEClusters, as Strings
	 */
	public String[]	getclusters();
	 /**
       @return a Map keyed by cluster name of J2EECluster
    */
   public Map        getClusterMap(); 
}
