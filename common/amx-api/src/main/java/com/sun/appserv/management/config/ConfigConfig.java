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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/ConfigConfig.java,v 1.2 2007/05/05 05:30:32 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:30:32 $
 */


package com.sun.appserv.management.config;

import java.util.Map;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Container;

/**
	 Configuration for the &lt;config&gt; element.
 */
public interface ConfigConfig
	extends PropertiesAccess, SystemPropertiesAccess,
	NamedConfigElement, Container
{
	/**
		Configuration of the config element itself.
	 */
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.CONFIG_CONFIG;
	
	
	/**
		Calls Container.getContaineeMap( XTypes.THREAD_POOL_CONFIG ).
		@return Map of ThreadPoolConfig proxies, keyed by name.
		@see com.sun.appserv.management.base.Container#getContaineeMap
	 */
	public Map<String,ThreadPoolConfig>	getThreadPoolConfigMap();
	

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
		
	/**
		Create a new &lt;thread-pool>
		
		@param name			name of the &lt;thread-pool> (thread-pool-id)
		@param optional		Map of optional attributes and properties whose keys
		are defined here. (eg:- MIN_THREAD_POOL_SIZE_KEY)
		@return A proxy to the ThreadPoolConfig MBean.
		@see ThreadPoolConfigKeys
	 */
	public ThreadPoolConfig	createThreadPoolConfig( String name, Map<String,String> optional );

	/**
		Removes a thread-pool element.

		@param name			name of the &lt;thread-pool> (thread-pool-id)
	 */
	public void			removeThreadPoolConfig( String name );
	
	
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
	    Return the DiagnosticServiceConfig.  May be null.
	    @since AppServer 9.0
        */
	public DiagnosticServiceConfig getDiagnosticServiceConfig();
	
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

















