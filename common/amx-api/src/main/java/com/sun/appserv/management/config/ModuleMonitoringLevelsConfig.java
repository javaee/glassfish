/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
 
/*
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/ModuleMonitoringLevelsConfig.java,v 1.2 2007/05/05 05:30:34 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:30:34 $
 */


package com.sun.appserv.management.config;

import java.util.Map;

import com.sun.appserv.management.base.XTypes;



/**
	Configuration for the &lt;module-monitoring-levels&gt; element.
	
	Each monitoring level can be one of the values defined in
	{@link ModuleMonitoringLevelValues}.
 */
public interface ModuleMonitoringLevelsConfig extends ConfigElement, PropertiesAccess
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.MODULE_MONITORING_LEVELS_CONFIG;
	
	/**
		Get a Map keyed by Attribute name of all monitoring levels.
		Attribute names correspond to the various get methods eg
		"JVM" for {@link #getJVM},
		"ConnectorService" for {@link #getConnectorService}, etc.
		<p>
		The possible levels are as defined in {@link ModuleMonitoringLevelValues}.
	 */
	public Map<String,String>		getAllLevels();
	
	/**
		Change all monitoring levels to the specified value.
		
		@param value one of the values in {@link ModuleMonitoringLevelValues}
	 */
	public void		changeAll( final String value );
	
	public String	getJVM();
	
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void		setJVM( final String value );
	
	public String	getConnectorService();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void		setConnectorService( final String value );
	
	public String	getJMSService();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void		setJMSService( final String value );
	
	public String   getConnectorConnectionPool();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void     setConnectorConnectionPool( final String value );

	public String   getEJBContainer();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void     setEJBContainer( final String value );

	public String   getHTTPService();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void     setHTTPService( final String value );

	public String   getJDBCConnectionPool();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void     setJDBCConnectionPool( final String value );

	public String   getORB();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void     setORB( final String value );

	public String   getThreadPool();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void     setThreadPool( final String value );

	public String   getTransactionService();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void     setTransactionService( final String value );

	public String   getWebContainer();
	/** @param value one of the values in {@link ModuleMonitoringLevelValues} */
	public void     setWebContainer( final String value );




}
