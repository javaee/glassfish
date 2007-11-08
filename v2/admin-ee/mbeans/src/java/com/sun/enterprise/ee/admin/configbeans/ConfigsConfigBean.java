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

import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;

import com.sun.enterprise.ee.admin.servermgmt.EEDomainsManager;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.common.Status;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;

import com.sun.enterprise.ee.admin.ExceptionHandler;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

import com.sun.enterprise.admin.configbeans.BaseConfigBean;

import com.sun.logging.ee.EELogDomains;

import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList; 

/**
 *
 * @author  kebbs
 */
public class ConfigsConfigBean extends BaseConfigBean implements IAdminConstants
{
    
    private static final StringManager _strMgr = 
        StringManager.getManager(ConfigsConfigBean.class);

    private static Logger _logger = null;
        
    private static Logger getLogger() 
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

    /** Creates a new instance of ConfigsConfigMBean */
    public ConfigsConfigBean(ConfigContext configContext) {
        super(configContext);
    }
 
    private void addConfigurationProperties(Config configuration, Properties props)
        throws ConfigException 
    {
        if (props != null) {            
            for (Enumeration e = props.propertyNames(); e.hasMoreElements() ;) {
                String propName = (String)e.nextElement();
                String propValue = (String)props.getProperty(propName);
                if (propValue != null) {
                    SystemProperty epOriginal = configuration.getSystemPropertyByName(
                        propName);
                    if (epOriginal != null) {
                        configuration.removeSystemProperty(epOriginal,
                            OVERWRITE);
                    }
                    SystemProperty ep = new SystemProperty();
                    ep.setName(propName);
                    ep.setValue(propValue);                    
                    configuration.addSystemProperty(ep, OVERWRITE);
                }
            }
        }
    }
    
    String createStandAloneConfiguration(String name, 
        Properties props) throws ConfigException
    {
        String standaloneConfigName = ConfigAPIHelper.getStandAloneConfigurationName(name);
        copyConfiguration(DEFAULT_CONFIGURATION_NAME, 
            standaloneConfigName, props);
        return standaloneConfigName;
    }            
        
    /**
     * copyConfiguration can be passed a configContext so that the caller can 
     * provide the configContext.
     */
    public void copyConfiguration(String sourceConfigName,
        String newConfigName, Properties props) throws ConfigException 
    {   
        try {
            final ConfigContext configContext = getConfigContext();
            //Fetch the source configuration and ensure that it exists.
            Config sourceConfig = ConfigAPIHelper.getConfigByName(configContext,
                sourceConfigName); 

	    //"server-config" cannot be the source
            if ("server-config".equals(sourceConfigName)) {
                throw new ConfigException(_strMgr.getString(
                    "configurationSourceInvalid", sourceConfigName));
            }

            //Ensure that a configuration with the standalone configuration name 
            //does not already exist.                        
            Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);            
            Configs configs = domain.getConfigs();
            Config newConfig = configs.getConfigByName(newConfigName);
            if (newConfig != null) {
                throw new ConfigException(_strMgr.getString(
                    "configurationAlreadyExists", newConfigName));
            }

	    //"domain" is a restricted name
            if ("domain".equals(newConfigName)) {
                throw new ConfigException(_strMgr.getString(
                    "configurationNameRestricted", newConfigName));
            }

	    // Ensure that the name is a valid target meaning that there are no
	    // servers and clusters with the same name                     
	    Servers servers = domain.getServers();
	    Server server = servers.getServerByName(newConfigName);
            if (server != null) {
                throw new ConfigException(_strMgr.getString(
                    "configurationNameAlreadyExistsAsServer", newConfigName));
            }

	    Clusters clusters = domain.getClusters();
	    Cluster cluster = clusters.getClusterByName(newConfigName);
            if (cluster != null) {
                throw new ConfigException(_strMgr.getString(
                    "configurationNameAlreadyExistsAsCluster", newConfigName));
            }

	    NodeAgents nas = domain.getNodeAgents();
	    NodeAgent na = nas.getNodeAgentByName(newConfigName);
            if (na != null) {
                throw new ConfigException(_strMgr.getString(
                    "configurationNameAlreadyExistsAsNodeAgent", newConfigName));
            }

            //Clone the source configuration to make the new configuration.        
            newConfig = (Config)sourceConfig.clone();            
            newConfig.setConfigContext(sourceConfig.getConfigContext());
            
            //Add/override properties in the cloned configuration
            addConfigurationProperties(newConfig, props);

            //Copy the configuration directory from the central repository
            EEDomainsManager mgr = new EEDomainsManager();
            mgr.copyConfigururation(new RepositoryConfig(), 
                sourceConfigName, newConfigName);  
            
            //Set the name of the newly created standalone configuration and add it to the 
            //list of configurations
            newConfig.setName(newConfigName);
            configs.addConfig(newConfig, OVERWRITE);            
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.copyConfiguration.Exception", 
                new String[] {sourceConfigName, newConfigName});
        }
    }
        
    public void deleteConfiguration(String configName)
        throws ConfigException 
    {
        try {
            final ConfigContext configContext = getConfigContext();
            //Check to ensure that the configuration is not the default-config
            //template. No-one is allowed to refer to it.
            if (configName.equals(DEFAULT_CONFIGURATION_NAME)) {
                throw new ConfigException(_strMgr.getString("cannotDeleteDefaultConfigTemplate", 
                    DEFAULT_CONFIGURATION_NAME));
            }

            //Fetch the source configuration and ensure that it exists.
            Config config = ConfigAPIHelper.getConfigByName(configContext,
                configName); 

            //Ensure that no-one (i.e. server instances or clusters) is referencing the 
            //configuration.   
            if (ConfigAPIHelper.isConfigurationReferenced(configContext, configName)) {                
                throw new ConfigException(_strMgr.getString("configurationNotEmpty", 
                    configName, ConfigAPIHelper.getConfigurationReferenceesAsString(
                        configContext, configName)));
            }

            //Delete the configuration directory from the central repository
            EEDomainsManager mgr = new EEDomainsManager();
            mgr.deleteConfigururation(new RepositoryConfig(), 
                configName);  
            
            //Remove the configuration
            Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);            
            Configs configs = domain.getConfigs();
            configs.removeConfig(config, OVERWRITE);
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.deleteConfiguration.Exception", configName);            
        }
    }        
            
    
    public String[] listConfigurationsAsString(String targetName)    
        throws ConfigException 
    {        
        try {
            TargetType[] validTypes = new TargetType[] {TargetType.DOMAIN, TargetType.CLUSTER,
                TargetType.SERVER, TargetType.DAS, TargetType.CONFIG};
            ConfigContext configContext = getConfigContext();        
            Target target = TargetBuilder.INSTANCE.createTarget(
                DOMAIN_TARGET, validTypes, targetName, 
                configContext);            
            final Config[] configs = target.getConfigs();
            int numConfigs = configs.length;
            String[] result = new String[numConfigs];
            for (int i = 0; i < numConfigs; i++) {
                result[i] = configs[i].getName();
            }
            return result;
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.listConfigurations.Exception", targetName);
        }            
    }
  
    private ServersConfigBean getServersConfigBean()
    {
        return new ServersConfigBean(getConfigContext());
    }
    
    /**
     * Returns the combined runtime status for the servers
     * refering to the config
     * 
     * @param   configName      Name of the config
     *
     * @return  boolean         Restart required status
     */
    public boolean isRestartRequired(String configName) throws InstanceException
    {
        try
        {                       
            boolean restartRequired = false;
            final String[] servers = getServersForConfig(configName);
            ServersConfigBean cmb = getServersConfigBean();  
            RuntimeStatus status = null;
            for (int i = 0; i < servers.length; i++)
            {
                try {
                    status = cmb.getRuntimeStatus(servers[i]);
                } catch (InstanceException ie) {
                    // ignore instance exceptions and assume
                    // server is down and will be restarted.
                    // call node agent api to figured out
                    // if the server is restarted through node agent
                    // or manurally started
                }

                if ((status != null) && (!status.isStopped()) && (status.isRestartNeeded())) {
                    restartRequired = true;
                    break;
                }
            }    
            return restartRequired; 
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.getConfigStatus.Exception", configName);            
        }
    }

    private String[] getServersForConfig(
        String configName) throws ConfigException
    {
        String[] sa = new String[0];        
        Server[] servers = ServerHelper.getServersReferencingConfig(
            getConfigContext(), configName);
        if (servers != null)
        {
            sa = new String[servers.length];
            for (int i = 0; i < sa.length; i++)
            {
                sa[i] = servers[i].getName();
            }
        }
        return sa;
    }
  
}
