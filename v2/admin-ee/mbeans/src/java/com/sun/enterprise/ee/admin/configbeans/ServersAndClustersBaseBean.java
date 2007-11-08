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

package com.sun.enterprise.ee.admin.configbeans;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.server.Constants;


import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.admin.configbeans.BaseConfigBean;

import com.sun.enterprise.ee.admin.ExceptionHandler;

import java.util.logging.Logger;
import java.util.logging.Level; 

import javax.management.ObjectName;

import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;

public abstract class ServersAndClustersBaseBean extends BaseConfigBean implements IAdminConstants
{    
    private static final StringManager _strMgr = 
        StringManager.getManager(ServersAndClustersBaseBean.class);

    private static Logger _logger = null;      
    
    public ServersAndClustersBaseBean(ConfigContext configContext) {
        super(configContext);
    }    
    
    protected static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }       
        
    private static ExceptionHandler _handler = null;
    
    //The exception handler is used to parse and log exceptions
    protected static ExceptionHandler getExceptionHandler() 
    {
        if (_handler == null) {
            _handler = new ExceptionHandler(getLogger());
        }
        return _handler;
    }           
                   
    /**
     * Return true if the specified objectType is a system application that should be referenced by 
     * (i.e. deployed to) the newly created server
     */
    private boolean isSystemApp(String objectType)
    {
        if (objectType.equals(Constants.SYSTEM_ALL) || 
            objectType.equals(Constants.SYSTEM_INSTANCE)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Add an application reference to a server
     */
    protected void addApplicationReference(Object clusterOrServer, boolean enabled, String name) 
        throws ConfigException
    {
        
        addApplicationReference(clusterOrServer, enabled, name, null);
    }
            
    protected abstract void addApplicationReference(Object clusterOrServer, boolean enabled, String name, 
        String virtualServers) throws ConfigException;
    
    /**
     * Iterate through all of the applications, looking for system applications that should
     * be referenced by (i.e. deployed to) the newly created server.
     */
    protected void addSystemApplications(Object clusterOrServer) throws ConfigException
    {        
        final ConfigContext configContext = getConfigContext();
        final Domain domain = ConfigAPIHelper.getDomainConfigBean(
            configContext);
        final Applications applications = domain.getApplications();
        ConnectorModule[] connectors = applications.getConnectorModule();        
        for (int i = 0; i < connectors.length; i++) {
            if (isSystemApp(connectors[i].getObjectType())) {
                addApplicationReference(clusterOrServer, connectors[i].isEnabled(),
                    connectors[i].getName());
            }
        }
        EjbModule[] ejbs = applications.getEjbModule();
        for (int i = 0; i < ejbs.length; i++) {
            if (isSystemApp(ejbs[i].getObjectType())) {
                addApplicationReference(clusterOrServer, ejbs[i].isEnabled(),
                    ejbs[i].getName());
            }
        }
        J2eeApplication[] apps = applications.getJ2eeApplication();
        for (int i = 0; i < apps.length; i++) {
            if (isSystemApp(apps[i].getObjectType())) {
                addApplicationReference(clusterOrServer, apps[i].isEnabled(),
                    apps[i].getName());
            }
        }
        WebModule[] webs = applications.getWebModule();
        for (int i = 0; i < webs.length; i++) {
            if (isSystemApp(webs[i].getObjectType())) {
                addApplicationReference(clusterOrServer, webs[i].isEnabled(),
                    webs[i].getName());
            }
        }

	Mbean[] mbeans = applications.getMbean();
	for (int i = 0; i < mbeans.length; i++) {
	    if (isSystemApp(mbeans[i].getObjectType())) {
	        addApplicationReference(clusterOrServer, mbeans[i].isEnabled(),
			       	        mbeans[i].getName());
            }
	}

        //There doesnt seem to be a way to add system lifecycle module.
        //Adding this hack temperorily until this is disussed and 
        //decided.
        LifecycleModule[] lcs = applications.getLifecycleModule();
        for (LifecycleModule lcm : lcs) {
            if (lcm.getName().equals("JBIFramework")) {
                addApplicationReference(clusterOrServer, lcm.isEnabled(),
                    lcm.getName());
            }
        }
    }
    
    protected abstract void addResourceReference(Object clusterOrServer, boolean enabled, String name) 
        throws ConfigException;
        
    protected void addSystemResources(
        Object clusterOrServer) throws ConfigException
    {
        final ConfigContext configContext = getConfigContext();
        final Domain domain = ConfigAPIHelper.getDomainConfigBean(
            configContext);
        final Resources resources = domain.getResources();       
        ConnectorResource[] crs = resources.getConnectorResource();
        for (int i = 0; i < crs.length; i++) {
            if (isSystemApp(crs[i].getObjectType())) {
                addResourceReference(clusterOrServer, crs[i].isEnabled(), 
                    crs[i].getJndiName());
            }
        }
                
        JdbcResource[] jrs = resources.getJdbcResource();
        for (int i = 0; i < jrs.length; i++) {
            if (isSystemApp(jrs[i].getObjectType())) {
                addResourceReference(clusterOrServer, jrs[i].isEnabled(), 
                    jrs[i].getJndiName());
            }
        }
    }    
    
    protected Config validateSharedConfiguration(ConfigContext configContext, String configName) 
        throws ConfigException, InstanceException
    {
        //Check to ensure that the configuration is not the default-config
        //template. No-one is allowed to refer to it.
        if (configName.equals(DEFAULT_CONFIGURATION_NAME)) {
            throw new InstanceException(_strMgr.getString("cannotReferenceDefaultConfigTemplate", 
                DEFAULT_CONFIGURATION_NAME));
        }

        //Check to ensure that the configuration is not the configuration 
        //of the domain administration server
        final Server das = ServerHelper.getDAS(configContext);
        final Config dasConfig = ServerHelper.getConfigForServer(configContext, das.getName());     
        final String dasConfigName = dasConfig.getName();
        if (configName.equals(dasConfigName)) {
            throw new InstanceException(_strMgr.getString("cannotReferenceDASConfig", 
                dasConfigName));
        }
                
        //Get the configuration specified by configName and ensure that it exists
        return ConfigAPIHelper.getConfigByName(configContext, 
            configName);
    }
}
