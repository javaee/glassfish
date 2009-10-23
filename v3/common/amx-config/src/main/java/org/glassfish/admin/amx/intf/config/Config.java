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
package org.glassfish.admin.amx.intf.config;

import java.util.Map;
import org.glassfish.admin.amx.core.AMXProxy;

import org.glassfish.admin.amx.intf.config.grizzly.NetworkConfig;

/**
Configuration for the &lt;config&gt; element.
 */
public interface Config
        extends PropertiesAccess, SystemPropertiesAccess,
        NamedConfigElement
{
    /**
    Configuration of the config element itself.
     */
    /**
    Return the IiopService.
     */
    public IiopService getIiopService();

    /**
    Return the HttpService.
     */
    public HttpService getHttpService();

    /**
    Return the NetworkConfig.
     */
    public NetworkConfig getNetworkConfig();

    /**
    Return the SecurityService.
     */
    public SecurityService getSecurityService();

    /**
    Return the MonitoringService.
     */
    public MonitoringService getMonitoringService();

    /**
    Return the AdminService.
     */
    public AdminService getAdminService();

    /** @since Glassfish V3 */
    public ThreadPools getThreadPools();

    /**
    Return the DiagnosticService.  May be null.
    @since AppServer 9.0
     */
    public DiagnosticService getDiagnosticService();

    /**
    Return the WebContainer.
     */
    public WebContainer getWebContainer();

    /**
    Return the EJBContainer.
     */
    public EjbContainer getEjbContainer();

    /**
    Return the MDBContainer.
     */
    public MdbContainer getMdbContainer();
    
    
    public AlertService getAlertService();

    /**
    Return the JavaConfig.
     */
    public JavaConfig getJava();

    /**
    Return the JMSService.
     */
    public JmsService getJmsService();

    /**
    Return the LogService.
     */
    public LogService getLogService();

    /**
    Return the TransactionService.
     */
    public TransactionService getTransactionService();

    /**
    Return the AvailabilityService.
     */
    public AvailabilityService getAvailabilityService();

    /**
    Return the ConnectorService.
     */
    public ConnectorService getConnectorService();

    /**
    Return the Group Management Service configuration.
    @since AppServer 9.0
     */
    public GroupManagementService getGroupManagementService();

    /**
    When set to "true" then any changes to the system (e.g.
    applications deployed, resources created) will be
    automatically applied to the affected servers without a
    restart being required. When set to "false" such changes will
    only be picked up by the affected servers when each server
    restarts.
    @since AppServer 9.0
     */
    
    public String getDynamicReconfigurationEnabled();

    /**
    @see #getDynamicReconfigurationEnabled
    @since AppServer 9.0
     */
    public void setDynamicReconfigurationEnabled(String enabled);

    /**
    @return ManagementRules (may be null );
     */
    public ManagementRules getManagementRules();
    
    public JavaConfig getJavaConfig();
}

















