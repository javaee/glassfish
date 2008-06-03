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
package com.sun.appserv.management.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;

import java.util.Map;

/**
	 Configuration for the &lt;config&gt; element.
 */
public interface ConfigConfig
	extends PropertiesAccess, SystemPropertiesAccess,
	NamedConfigElement, Container, DefaultValues
{
	/**
		Configuration of the config element itself.
	 */
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.CONFIG_CONFIG;
	/**
		Return the IIOPServiceConfig.
	 */
	public IIOPServiceConfig	getIIOPServiceConfig();
	
	/**
		Return the HTTPServiceConfig.
	 */
	public HTTPServiceConfig	getHTTPServiceConfig();
	
	/**
		Return the SecurityServiceConfig.
	 */
	public SecurityServiceConfig	getSecurityServiceConfig();
	
	/**
		Return the MonitoringServiceConfig.
	 */
	public MonitoringServiceConfig	getMonitoringServiceConfig();
	
	/**
		Return the AdminServiceConfig.
	 */
	public AdminServiceConfig	getAdminServiceConfig();
    
	/** @since Glassfish V3 */
	public ThreadPoolsConfig         getThreadPoolsConfig();
	
	/**
        @Deprecated use {@link ThreadPoolsConfig#getThreadPoolConfigMap}
	 */
	public Map<String,ThreadPoolConfig>	getThreadPoolConfigMap();
    
    
	/**
        @Deprecated use {@link ThreadPoolsConfig#createThreadPoolConfig}
	 */
	public ThreadPoolConfig	createThreadPoolConfig( String name, Map<String,String> optional );

	/**
        @Deprecated use {@link ThreadPoolsConfig#removeThreadPoolConfig}
	 */
	public void			removeThreadPoolConfig( String name );

	/**
	    Return the DiagnosticServiceConfig.  May be null.
	    @since AppServer 9.0
        */
	public DiagnosticServiceConfig getDiagnosticServiceConfig();
    
	/**
		Return the WebContainerConfig.
	 */
	public WebContainerConfig	getWebContainerConfig() ;
	
	/**
		Return the EJBContainerConfig.
	 */
	public EJBContainerConfig	getEJBContainerConfig() ;
	
	/**
		Return the MDBContainerConfig.
	 */
	public MDBContainerConfig	getMDBContainerConfig();
	
	/**
		Return the JavaConfig.
	 */
	public JavaConfig	getJavaConfig();
	
	/**
		Return the JMSServiceConfig.
	 */
	public JMSServiceConfig	getJMSServiceConfig();
	
	/**
		Return the LogServiceConfig.
	 */
	public LogServiceConfig	getLogServiceConfig();
	
	/**
		Return the TransactionServiceConfig.
	 */
	public TransactionServiceConfig	getTransactionServiceConfig();
	
	/**
		Return the AvailabilityServiceConfig.
	 */
	public AvailabilityServiceConfig	getAvailabilityServiceConfig();

	/**
		Return the ConnectorServiceConfig.
	 */
	public ConnectorServiceConfig	getConnectorServiceConfig();
	
	/**
	    Create the ConnectorServiceConfig if it doesn't already exist.
	 */
	public ConnectorServiceConfig	createConnectorServiceConfig();
	public void     removeConnectorServiceConfig();

	
	/**
	    Create the DiagnosticServiceConfig.
	    @since AppServer 9.0
        */
	public DiagnosticServiceConfig createDiagnosticServiceConfig();
	
	/**
	    Remove the DiagnosticServiceConfig.
	    @since AppServer 9.0
        */
	public void removeDiagnosticServiceConfig();
	

	/**
	Return the Group Management Service configuration.
	@since AppServer 9.0
	*/
	public GroupManagementServiceConfig getGroupManagementServiceConfig();
	
	/**                  
        When set to "true" then any changes to the system (e.g.       
        applications deployed, resources created) will be             
        automatically applied to the affected servers without a       
        restart being required. When set to "false" such changes will 
        only be picked up by the affected servers when each server    
        restarts.
        @since AppServer 9.0                                                
     */
	public boolean   getDynamicReconfigurationEnabled();
	
	/**
	    @see #getDynamicReconfigurationEnabled
        @since AppServer 9.0
	 */
	public void      setDynamicReconfigurationEnabled( boolean enabled );
	
	/**
	    @return ManagementRulesConfig (may be null );
	 */
	public ManagementRulesConfig    getManagementRulesConfig();
}

















