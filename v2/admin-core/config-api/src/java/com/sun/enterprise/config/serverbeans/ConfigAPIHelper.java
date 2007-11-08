/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;   

import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.util.IAdminConstants;

import java.util.ArrayList;

public class ConfigAPIHelper extends ReferenceHelperBase implements IAdminConstants {
        
    protected static final StringManager _strMgr=StringManager.getManager(ConfigAPIHelper.class);        
    private static ConfigAPIHelper _theInstance;
    private final static	String[]	ILLEGAL_NAME_STRINGS = {"--"};

    
    public ConfigAPIHelper() {
        super();
    }
    
    protected Server[] getReferencingServers(ConfigContext configContext, String name) 
        throws ConfigException
    {
        return ServerHelper.getServersReferencingConfig(configContext, name); 
    }
    
    protected Cluster[] getReferencingClusters(ConfigContext configContext, String name) 
        throws ConfigException
    {
        return ClusterHelper.getClustersReferencingConfig(configContext, name);        
    }
        
    private synchronized static ConfigAPIHelper getInstance()
    {
        if (_theInstance == null) {
            _theInstance = new ConfigAPIHelper();
        }
        return _theInstance;
    }
    
    public static Domain getDomainConfigBean(ConfigContext configCtxt) 
        throws ConfigException 
    {                                  
        return ServerBeansFactory.getDomainBean(configCtxt);        
    }          

    /**
     * Return all the configurations in the domain. We dont check for null here
     * because we assume there is always at least one configuration in the domain.
     */
    public static Config[] getConfigsInDomain(ConfigContext configContext)
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);          
        return domain.getConfigs().getConfig(); 
    }
    
    /**
     * Return true if the given configName is a configuration.
     */
    public static boolean isAConfig(ConfigContext configContext, String configName) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);           
        final Config config = domain.getConfigs().getConfigByName(configName);
        return (config != null ? true : false);
    }
        
    /**
     * Return the configuration associated with the given configName. An exception
     * is thrown if the configuration does not exist.
     */
    public static Config getConfigByName(ConfigContext configContext, String configName) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);        
        final Config config = domain.getConfigs().getConfigByName(configName);
        if (config == null) {
            throw new ConfigException(_strMgr.getString("noSuchConfig", configName));
        }
        return config;
    }

    /**
     * Returns true if the name is unique across a namespace including:
     * server instances, configurations, clusters, node agents.
     */
    public static boolean isNameUnique(ConfigContext configContext, String name) 
        throws ConfigException 
    {
        final Server[] servers = ServerHelper.getServersInDomain(configContext);
        for (int i = 0; i < servers.length; i++) {
            if (servers[i].getName().equals(name)) {
                return false;
            }
        }
        
        final Config[] configs = getConfigsInDomain(configContext);
        for (int i = 0; i < configs.length; i++) {
            if (configs[i].getName().equals(name)) {
                return false;
            }
        }
        
        final Cluster[] clusters = ClusterHelper.getClustersInDomain(configContext);
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i].getName().equals(name)) {
                return false;
            }
        }
        
        final NodeAgent[] controllers = NodeAgentHelper.getNodeAgentsInDomain(configContext);
        for (int i = 0; i < controllers.length; i++) {
            if (controllers[i].getName().equals(name)) {
                return false;
            }
        }
        return true;
    }       
    
    /**
     * Given a server or cluster name, checks if the name is legal according
	 * to our defined specs.
     */
    public static void checkLegalName(String name) 
        throws ConfigException 
    {
		// we don't want an instance with, say, "--" in the name.
		// this will cause havoc with CLI
		// we throw the Exception so that we can document *what* the
		// exact problem with the name is
		
		for(int i = 0; i < ILLEGAL_NAME_STRINGS.length; i++)
		{
			if(name.indexOf(ILLEGAL_NAME_STRINGS[i]) >= 0)
			{
				String s = _strMgr.getString("illegalName", ILLEGAL_NAME_STRINGS[i]);
				throw new ConfigException(s);
			}
		}
    }       
    
    public static String getStandAloneConfigurationName(String name)
    {
        return name + STANDALONE_CONFIGURATION_SUFFIX;
    }
    
    /**
     * Returns true if the given configuration and cluster or instance name represents
     * a standalone configuration.
     */
    public static boolean isConfigurationNameStandAlone(String configName, String name)
    {
        return configName.equals(getStandAloneConfigurationName(
            name)) ? true : false; 
    }
    
    /**
     * Is the configuration referenced by anyone (i.e. any server instance or cluster
     */
    public static boolean isConfigurationReferenced(ConfigContext configContext, String configName) 
        throws ConfigException
    {
        return getInstance().isReferenced(configContext, configName);
    }
    
    /**
     * Return true if the configuration is referenced by no-one other than the given 
     * server instance.
     */
    public static boolean isConfigurationReferencedByServerOnly(ConfigContext configContext, 
        String configName, String serverName) throws ConfigException        
    {        
        return getInstance().isReferencedByServerOnly(configContext, configName, serverName);
    }
    
    /**
     * Return true if the configuration is referenced by no-one other than the given 
     * cluster.
     */
    public static boolean isConfigurationReferencedByClusterOnly(ConfigContext configContext, 
        String configName, String clusterName) throws ConfigException        
    {                       
        return getInstance().isReferencedByClusterOnly(configContext, configName, clusterName);
    }
    
    /**
     * Find all the servers or clusters associated with the given configuration and return them 
     * as a comma separated list.
     */
    public static String getConfigurationReferenceesAsString(ConfigContext configContext, String configName) 
        throws ConfigException
    {        
       return getInstance().getReferenceesAsString(configContext, configName);
    }    
}
