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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.server.util.ASClassLoaderUtil;
import com.sun.enterprise.PoolManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.*;
import com.sun.enterprise.resource.MonitorableResourcePool;
import com.sun.enterprise.resource.ResourcePool;
import com.sun.enterprise.server.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.event.ResourceDeployEvent;
import com.sun.enterprise.deployment.runtime.connector.SunConnector;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;

import com.sun.enterprise.server.ApplicationLifecycle;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.InstanceFactory;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.loader.EJBClassPathUtils;

import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorRuntimeException;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.util.Utility;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.Set;
import java.util.Iterator;
import java.lang.Integer;
import java.util.ArrayList;
import java.lang.reflect.Method;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.spi.ManagedConnectionFactory;


/**
 *
 * @author    Srikanth P, Sivakumar Thyagarajan, Aditya Gore, Kshitiz Saxena
 * @version
 */

public class ResourcesUtil {
    
    static final int NO_OF_ALL_CONNECTOR_RESOURCE_TYPE = 4;
    static final int NO_OF_CONNECTOR_RESOURCE_TYPE = 3;
    static final int NO_OF_JDBC_RESOURCE_TYPE = 2;
    
    public static final String RA_CONFIGS="ra_configs";
    public static final String CONNECTION_POOLS ="connection_pools";
    public static final String RESOURCES ="resources";
    
    //The thread local ResourcesUtil is used in two cases
    //1. An event config context is to be used as in case of resource
    //   deploy/undeploy and enable/disable events.
    //2. An admin config context to be used for ConnectorRuntime.getConnection(...)
    //   request
    static ThreadLocal<ResourcesUtil> localResourcesUtil =
            new ThreadLocal<ResourcesUtil>();
    
    static ServerContext sc_ = null;
    
    Resources res = null;
    Domain dom = null;
    ConfigContext configContext_ = null;
    
    static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
    
    static StringManager localStrings =
            StringManager.getManager(ResourcesUtil.class);
    
    public static void setServerContext(ServerContext sc){
        sc_ = sc;
    }
    
    public static ResourcesUtil createInstance(){
        if(localResourcesUtil.get() != null)
            return localResourcesUtil.get();
        if(sc_ == null){
            String message = localStrings.getStringWithDefault(
                    "server_context_is_null",
                    "Server context is null. Hence cannot create instance of ResourcesUtil or DASResourcesUtil.");
            _logger.log(Level.SEVERE, message);
            return null;
        }
        try{
            if (isDAS()) {
                return new DASResourcesUtil();
            } else {
                return new ResourcesUtil();
            }
        }catch(ConfigException ex){
            String message = localStrings.getStringWithDefault(
                    "caught_config_exception",
                    "Caught ConfigException while instantiating ResourcesUtil or DASResourcesUtil : ");
            
            _logger.log(Level.SEVERE,
                   message + ex.getMessage(), ex);
            return null;
        }
    }
    
    protected ResourcesUtil() throws ConfigException{
        this(sc_.getConfigContext());
    }
    
    protected ResourcesUtil(ConfigContext configContext) throws ConfigException {
        try{
            configContext_ = configContext;
            dom = ServerBeansFactory.getDomainBean(configContext_);
            res = dom.getResources();
        }catch(ConfigException ex){
            String message = localStrings.getStringWithDefault(
                    "caught_config_exception",
                    "Caught ConfigException while instantiating ResourcesUtil or DASResourcesUtil : ");
            throw new ConfigException(message, ex);
            
        }
    }
    
    public ConfigBean[][] getConnectorResources() throws ConfigException{
        return getConnectorResources(false);
    }
    
    public ConfigBean[][] getConnectorResources(boolean onlystandAloneRars)
    throws ConfigException{
        
        ConfigBean[][] configBeanArray  = new ConfigBean[NO_OF_ALL_CONNECTOR_RESOURCE_TYPE][];
        Map allConnectorResources = getFilteredAllConnectorResources(onlystandAloneRars, false, null);
        configBeanArray[0] = (ConfigBean[]) allConnectorResources.get(RA_CONFIGS);
        configBeanArray[1] = (ConfigBean[]) allConnectorResources.get(CONNECTION_POOLS);
        configBeanArray[2] = (ConfigBean[]) allConnectorResources.get(RESOURCES);
        configBeanArray[3] = getFilteredAdminObjectResources(onlystandAloneRars, false, null);
        return configBeanArray;
    }
    
    public ConfigBean[][] getStandAloneNonSystemRarConnectorResourcesWithoutRAConfigs()
    throws ConfigException {
        
        boolean onlystandAloneRars = true;
        ConfigBean configBeanArray[][] =
                new ConfigBean[NO_OF_CONNECTOR_RESOURCE_TYPE][];
        Map allConnectorResources = getFilteredAllConnectorResources(onlystandAloneRars, true, null);
        configBeanArray[0] = (ConfigBean[]) allConnectorResources.get(CONNECTION_POOLS);
        configBeanArray[1] = (ConfigBean[]) allConnectorResources.get(RESOURCES);
        configBeanArray[2] = getFilteredAdminObjectResources(onlystandAloneRars, true, null);
        return configBeanArray;
    }
    
    public ConfigBean[][] getAllConnectorResourcesForRar(String rarName)
    throws ConfigException {
        
        ConfigBean configBeanArray[][] =
                new ConfigBean[NO_OF_CONNECTOR_RESOURCE_TYPE][];
        Map allConnectorResources = getFilteredAllConnectorResources(false, false, rarName);
        configBeanArray[0] = (ConfigBean[]) allConnectorResources.get(CONNECTION_POOLS);
        configBeanArray[1] = (ConfigBean[]) allConnectorResources.get(RESOURCES);
        configBeanArray[2] = getFilteredAdminObjectResources(false, false, rarName);
        return configBeanArray;
        
    }
    
    public ConfigBean[] getEnabledAdminObjectResources(String rarName) throws ConfigException {
        return getFilteredAdminObjectResources(false, false, rarName);
    }
    
    public Map getAllConnectorResources() throws ConfigException{
        return getFilteredAllConnectorResources(false, false, null);
    }
    
    private Map getFilteredAllConnectorResources(
            boolean onlystandAloneRars, boolean onlynonSystemRars, String rarName) throws ConfigException {
        
        HashMap allConnectorResources = new HashMap();
        int noOfConnectorResources = res.sizeConnectorResource();
        
        if(noOfConnectorResources == 0) {
            return allConnectorResources;
        }
        
        HashSet<ConnectorResource> standAloneRarConnectorResourcesVector =
                new HashSet<ConnectorResource>();
        HashSet<ConnectorResource> embeddedRarConnectorResourcesVector  =
                new HashSet<ConnectorResource>();
        HashSet<ConnectorConnectionPool> standAloneRarPoolsVector =
                new HashSet<ConnectorConnectionPool>();
        HashSet<ConnectorConnectionPool> embeddedRarPoolsVector  =
                new HashSet<ConnectorConnectionPool>();
        HashSet<ResourceAdapterConfig> standAloneRarConfigsVector =
                new HashSet<ResourceAdapterConfig>();
        HashSet<ResourceAdapterConfig> embeddedRarConfigsVector =
                new HashSet<ResourceAdapterConfig>();
        
        for(int i=0; i< noOfConnectorResources; ++i) {
            ConnectorResource ccResource = res.getConnectorResource(i);
            if(ccResource == null)
                continue;
            ConnectorConnectionPool ccPool = getConnectorConnectionPoolByName(
                    ccResource.getPoolName());
            if(ccPool == null)
                continue;
            String resourceAdapterName = ccPool.getResourceAdapterName();
            if(resourceAdapterName == null)
                continue;
            if(rarName != null && !rarName.equals(resourceAdapterName))
                continue;
            
            if(!isEnabled(ccResource))
                continue;
            
            if(belongToSystemRar(resourceAdapterName)){
                if(!onlynonSystemRars){
                    standAloneRarPoolsVector.add(ccPool);
                    standAloneRarConnectorResourcesVector.add(ccResource);
                }
            }else{
                ResourceAdapterConfig resAdapterConfig =
                            res.getResourceAdapterConfigByResourceAdapterName(
                            resourceAdapterName);
                if(belongToStandAloneRar(resourceAdapterName)){
                    standAloneRarConfigsVector.add(resAdapterConfig);
                    standAloneRarPoolsVector.add(ccPool);
                    standAloneRarConnectorResourcesVector.add(ccResource);
                }else if(belongToEmbeddedRar(resourceAdapterName)) {
                    embeddedRarConfigsVector.add(resAdapterConfig);
                    embeddedRarPoolsVector.add(ccPool);
                    embeddedRarConnectorResourcesVector.add(ccResource);
                }else{
                    String message = localStrings.getString("no.resource.adapter.found", 
                            resourceAdapterName, ccResource.getJndiName());
                    _logger.warning(message);
                }
            }   
        }
        
        ResourceAdapterConfig[] raConfigs = null;
        ConnectorConnectionPool[] ccPools = null;
        ConnectorResource[] ccResources = null;
        if(onlystandAloneRars){
            raConfigs = new ResourceAdapterConfig[standAloneRarConfigsVector.size()];
            raConfigs = (ResourceAdapterConfig[])standAloneRarConfigsVector.toArray(raConfigs);
            ccPools = new ConnectorConnectionPool[standAloneRarPoolsVector.size()];
            ccPools = (ConnectorConnectionPool[])standAloneRarPoolsVector.toArray(ccPools);
            ccResources = new ConnectorResource[standAloneRarConnectorResourcesVector.size()];
            ccResources = (ConnectorResource[])standAloneRarConnectorResourcesVector.toArray(ccResources);
        } else{
            Vector<ConnectorResource> allConnectorResourcesVector = new Vector<ConnectorResource>();
            Vector<ConnectorConnectionPool> allPoolsVector = new Vector<ConnectorConnectionPool>();
            Vector<ResourceAdapterConfig> allRaConfigs = new Vector<ResourceAdapterConfig>();
            allConnectorResourcesVector.addAll(standAloneRarConnectorResourcesVector);
            allConnectorResourcesVector.addAll(embeddedRarConnectorResourcesVector);
            allPoolsVector.addAll(standAloneRarPoolsVector);
            allPoolsVector.addAll(embeddedRarPoolsVector);
            allRaConfigs.addAll(standAloneRarConfigsVector);
            allRaConfigs.addAll(embeddedRarConfigsVector);
            raConfigs = new ResourceAdapterConfig[allRaConfigs.size()];
            raConfigs = (ResourceAdapterConfig[])allRaConfigs.toArray(raConfigs);
            ccPools = new ConnectorConnectionPool[allPoolsVector.size()];
            ccPools = (ConnectorConnectionPool[])allPoolsVector.toArray(ccPools);
            ccResources = new ConnectorResource[allConnectorResourcesVector.size()];
            ccResources = (ConnectorResource[])allConnectorResourcesVector.toArray(ccResources);
        }
        allConnectorResources.put(RA_CONFIGS, raConfigs);
        allConnectorResources.put(CONNECTION_POOLS, ccPools);
        allConnectorResources.put(RESOURCES, ccResources);
        return allConnectorResources;
    }
    
    private ConfigBean[] getFilteredAdminObjectResources(
            boolean onlystandAloneRars, boolean onlynonSystemRars, String rarName)
            throws ConfigException {
        
        int noOfAdminObjectResources = res.sizeAdminObjectResource();
        if(noOfAdminObjectResources == 0) {
            return null;
        }
        Vector<AdminObjectResource> standAloneRarAdminObjectResourcesVector = 
                new Vector<AdminObjectResource>();
        Vector<AdminObjectResource> embeddedRarAdminObjectResourcesVector = 
                new Vector<AdminObjectResource>();
        for(int i=0; i< noOfAdminObjectResources; ++i) {
            AdminObjectResource adminObjectResource = res.getAdminObjectResource(i);
            if(adminObjectResource == null)
                continue;
            String resourceAdapterName = adminObjectResource.getResAdapter();
            if(resourceAdapterName == null)
                continue;
            if(rarName!= null && !rarName.equals(resourceAdapterName))
                continue;
            // skips the admin resource if it is not referenced by the server
            if(!isEnabled(adminObjectResource))
                continue;
            if(belongToSystemRar(resourceAdapterName)){
                if(!onlynonSystemRars)
                    standAloneRarAdminObjectResourcesVector.add(adminObjectResource);
            }else if(belongToStandAloneRar(resourceAdapterName)){
                standAloneRarAdminObjectResourcesVector.add(adminObjectResource);
            }else if(belongToEmbeddedRar(resourceAdapterName)){
                embeddedRarAdminObjectResourcesVector.add(adminObjectResource);
            }else{
                String message = localStrings.getString("no.resource.adapter.found",
                        resourceAdapterName, adminObjectResource.getJndiName());
                _logger.warning(message);
            }
        }
        
        if(onlystandAloneRars) {
            AdminObjectResource[] standAloneRarAdminObjectResources =
                    new AdminObjectResource[standAloneRarAdminObjectResourcesVector.size()];
            standAloneRarAdminObjectResources =
                    (AdminObjectResource[])standAloneRarAdminObjectResourcesVector.toArray(
                    standAloneRarAdminObjectResources);
            return standAloneRarAdminObjectResources;
        } else {
            Vector<AdminObjectResource> allAdminObjectResourcesVector = 
                    new Vector<AdminObjectResource>();
            allAdminObjectResourcesVector.addAll(
                    standAloneRarAdminObjectResourcesVector);
            allAdminObjectResourcesVector.addAll(
                    embeddedRarAdminObjectResourcesVector);
            AdminObjectResource[] allAdminObjectResources =
                    new AdminObjectResource[allAdminObjectResourcesVector.size()];
            allAdminObjectResources =
                    (AdminObjectResource[])allAdminObjectResourcesVector.toArray(
                    allAdminObjectResources);
            return allAdminObjectResources;
        }
    }
    
    public boolean belongToSystemRar(String resourceAdapterName) {
        Iterator<String> iter = ConnectorRuntime.systemRarNames.iterator();
        while(iter.hasNext()){
            if(resourceAdapterName.equals(iter.next()))
                return true;
        }
        return false;
    }
    
    public boolean belongToStandAloneRar(String resourceAdapterName){
        Applications apps = dom.getApplications();
        ConnectorModule connectorModule = apps.getConnectorModuleByName(resourceAdapterName);
        if(connectorModule == null)
            return false;
        return true;
    }
    
    public boolean belongToEmbeddedRar( String resourceAdapterName) {
        String appName = getAppNameToken(resourceAdapterName);
        if(appName==null)
            return false;
        Applications apps = dom.getApplications();
        J2eeApplication j2eeApp = apps.getJ2eeApplicationByName(appName);
        if(j2eeApp == null)
            return false;
        return true;
    }
    
    
    public ConfigBean[] getResourceAdapterConfigs() throws ConfigException {
        ResourceAdapterConfig[] raConfigBeans = res.getResourceAdapterConfig();
        if(raConfigBeans == null || raConfigBeans.length == 0)
            return null;
        //Only referenced resource adapter configs must be returned
        Vector referencedResourceAdapterConfigs = new Vector();
        for(int i=0; i<raConfigBeans.length; i++){
            if(isRarEnabled(raConfigBeans[i].getResourceAdapterName()))
                referencedResourceAdapterConfigs.add(raConfigBeans[i]);
        }
        ConfigBean[] results = new ConfigBean[referencedResourceAdapterConfigs.size()];
        return (ConfigBean[]) referencedResourceAdapterConfigs.toArray(results);
    }
     
    
    public ConfigBean[] getResourceAdapterConfigs(String rarName)
    throws ConfigException {
        ResourceAdapterConfig[] raConfigBeans = res.getResourceAdapterConfig();
        if(raConfigBeans == null || raConfigBeans.length == 0) {
            return null;
        }
        for(int i=0;raConfigBeans != null && i<raConfigBeans.length;++i) {
            if(raConfigBeans[i].getResourceAdapterName().equals(rarName)){
                return new ConfigBean[]{raConfigBeans[i]};
            }
        }
        return null;
    }
    
    public ConfigBean[] getEmbeddedRarResourceAdapterConfigs(String appName)
    throws ConfigException {
        Applications apps = dom.getApplications();
        J2eeApplication j2eeApp = apps.getJ2eeApplicationByName(appName);
        if(j2eeApp == null || !j2eeApp.isEnabled()) {
            return null;
        }
        
        ResourceAdapterConfig[] raConfigBeans = res.getResourceAdapterConfig();
        
        if(raConfigBeans == null) {
            return null;
        }
        String appNameToken = null;
        Vector tmpVector = new Vector();
        for(int i=0;i<raConfigBeans.length;++i) {
            appNameToken =
                    getAppNameToken(raConfigBeans[i].getResourceAdapterName());
            if(appNameToken != null && appName.equals(appNameToken)) {
                tmpVector.add(raConfigBeans[i]);
            }
        }
        if(tmpVector.size() == 0)
            return null;
        ConfigBean[] result = new ConfigBean[tmpVector.size()];
        return (ConfigBean[])tmpVector.toArray(result);
    }
    
    private String getAppNameToken(String rarName) {
        if(rarName == null) {
            return null;
        }
        int index = rarName.indexOf(
                ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER);
        if(index != -1) {
            return rarName.substring(0,index);
        } else {
            return null;
        }
    }
    
    public static void setEventConfigContext(ConfigContext ctx) throws ConfigException {
        ResourcesUtil resUtil = null;
        if(isDAS())
            resUtil = new DASResourcesUtil(ctx);
        else
            resUtil = new ResourcesUtil(ctx);
        localResourcesUtil.set(resUtil);
    }
    
    public static void resetEventConfigContext() {
        localResourcesUtil.set(null);
    }
    
    public boolean poolBelongsToSystemRar(String poolName) {
        ConnectorConnectionPool ccPool =
                res.getConnectorConnectionPoolByName(poolName);
        if(ccPool != null){
            return belongToSystemRar(ccPool.getResourceAdapterName());
        } else {
            JdbcConnectionPool jdbcPool = 
                    res.getJdbcConnectionPoolByName(poolName);
            if(jdbcPool != null) {
                return true;
            }
        }
        return false;
    }
    
    public boolean resourceBelongsToSystemRar(String resourceName) {
        ConnectorResource connectorResource =
                res.getConnectorResourceByJndiName(resourceName);
        if(connectorResource != null){
            return poolBelongsToSystemRar(connectorResource.getPoolName());
        } else {
            JdbcResource jdbcResource =
                    res.getJdbcResourceByJndiName(resourceName);
            if(jdbcResource != null) {
                return true;
            }
        }
        return false;
    }
    
    public boolean adminObjectBelongsToSystemRar(String adminObject) {
        AdminObjectResource aor =
                res.getAdminObjectResourceByJndiName(adminObject);
        if(aor != null) {
            return belongToSystemRar(aor.getResAdapter());
        }
        return false;
    }
    
    /**
     * Returns the deffered connector resource config. This can be resource of JMS RA which is lazily 
     * loaded. Or for other connector RA which is not loaded at startup. The connector RA which does 
     * not have any resource or admin object associated with it are not loaded at startup. They are 
     * all lazily loaded.
     */
    protected DeferredResourceConfig getDeferredConnectorResourceConfigs(
            String resourceName) {
        
        if(resourceName == null) {
            return null;
        }
        ConfigBean[] resourcesToload = new ConfigBean[2];
        
        try {
            if(!isReferenced(resourceName)){
                return null;
            }
        } catch (ConfigException e) {
            String message = localStrings.getString(
                    "error.finding.resources.references",
                    resourceName);
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE,message + e.getMessage(), e);
        }
        
        
        ConnectorResource connectorResource =
                res.getConnectorResourceByJndiName(resourceName);
        if(connectorResource == null || !connectorResource.isEnabled()) {
            return null;
        }
        String poolName = connectorResource.getPoolName();
        ConnectorConnectionPool ccPool =
                res.getConnectorConnectionPoolByName(poolName);
        if(ccPool == null) {
            return null;
        }
        String rarName = ccPool.getResourceAdapterName();
        if(rarName != null){
            resourcesToload[0] = ccPool;
            resourcesToload[1] = connectorResource;
            ResourceAdapterConfig[] resourceAdapterConfig =
                    new ResourceAdapterConfig[1];
            resourceAdapterConfig[0] =
                    res.getResourceAdapterConfigByResourceAdapterName(
                    rarName);
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,null,ccPool,
                    connectorResource,null,null,
                    resourceAdapterConfig);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }
    
    /**
     * Returns the deffered connector connection pool config. This can be pool of JMS RA which is lazily 
     * loaded. Or for other connector RA which is not loaded at startup. The connector RA which does 
     * not have any resource or admin object associated with it are not loaded at startup. They are 
     * all lazily loaded.
     */
    protected DeferredResourceConfig getDeferredConnectorPoolConfigs(
            String poolName) {
        
        ConfigBean[] resourcesToload = new ConfigBean[1];
        if(poolName == null) {
            return null;
        }
        
        
        ConnectorConnectionPool ccPool =
                res.getConnectorConnectionPoolByName(poolName);
        if(ccPool == null) {
            return null;
        }
        
        String rarName = ccPool.getResourceAdapterName();
        
        if(rarName != null){
            resourcesToload[0] = ccPool;
            ResourceAdapterConfig[] resourceAdapterConfig =
                    new ResourceAdapterConfig[1];
            resourceAdapterConfig[0] =
                    res.getResourceAdapterConfigByResourceAdapterName(
                    rarName);
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,null,ccPool,
                    null,null,null,resourceAdapterConfig);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }
    
    /**
     * Returns the deffered admin object config. This can be admin object of JMS RA which is lazily 
     * loaded. Or for other connector RA which is not loaded at startup. The connector RA which does 
     * not have any resource or admin object associated with it are not loaded at startup. They are 
     * all lazily loaded.
     */
    protected DeferredResourceConfig getDeferredAdminObjectConfigs(
            String resourceName) {
        
        if(resourceName == null) {
            return null;
        }
        ConfigBean[] resourcesToload = new ConfigBean[1];
        
        try {
            if(!isReferenced(resourceName)){
                return null;
            }
        } catch (ConfigException e) {
            String message = localStrings.getString(
                    "error.finding.resources.references",
                    resourceName);
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE,message + e.getMessage(), e);
        }
        
        AdminObjectResource adminObjectResource =
                res.getAdminObjectResourceByJndiName(resourceName);
        if(adminObjectResource == null || !adminObjectResource.isEnabled()) {
            return null;
        }
        String rarName = adminObjectResource.getResAdapter();
        if(rarName != null){
            resourcesToload[0] = adminObjectResource;
            ResourceAdapterConfig[] resourceAdapterConfig =
                    new ResourceAdapterConfig[1];
            resourceAdapterConfig[0] =
                    res.getResourceAdapterConfigByResourceAdapterName(rarName);
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,adminObjectResource,
                    null,null,null,null,resourceAdapterConfig);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }
    
    protected DeferredResourceConfig getDeferredJdbcResourceConfigs(
            String resourceName) {
        
        ConfigBean[] resourcesToload = new ConfigBean[2];
        if(resourceName == null) {
            return null;
        }
        
        try {
            //__pm does not have a domain.xml entry and hence will not
            //be referenced
            if(!(resourceName.endsWith("__pm"))){
                if(!isReferenced(resourceName)){
                    return null;
                }
            }
        } catch (ConfigException e) {
            String message = localStrings.getString(
                    "error.finding.resources.references",
                    resourceName);
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE,message + e.getMessage(), e);
        }
        
        JdbcResource jdbcResource = res.getJdbcResourceByJndiName(resourceName);
        if(jdbcResource == null || !jdbcResource.isEnabled()) {
            String cmpResourceName =
                    getCorrespondingCmpResourceName(resourceName);
            jdbcResource =res.getJdbcResourceByJndiName(cmpResourceName);
            if(jdbcResource == null) {
                return null;
            }
        }
        JdbcConnectionPool jdbcPool =
                res.getJdbcConnectionPoolByName(jdbcResource.getPoolName());
        if(jdbcPool == null) {
            return null;
        }
        String rarName = getRAForJdbcConnectionPool(jdbcPool);
        if(rarName != null && belongToSystemRar(rarName)) {
            resourcesToload[0] = jdbcPool;
            resourcesToload[1] = jdbcResource;
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,null,null,
                    null,jdbcPool,jdbcResource,null);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }
    
    protected DeferredResourceConfig getDeferredJdbcPoolConfigs(
            String poolName) {
        
        ConfigBean[] resourcesToload = new ConfigBean[1];
        if(poolName == null) {
            return null;
        }
        
        JdbcConnectionPool jdbcPool =
                res.getJdbcConnectionPoolByName(poolName);
        if(jdbcPool == null) {
            return null;
        }
        String rarName = getRAForJdbcConnectionPool(jdbcPool);
        
        if(rarName != null && belongToSystemRar(rarName)) {
            resourcesToload[0] = jdbcPool;
            DeferredResourceConfig resourceConfig =
                    new DeferredResourceConfig(rarName,null,null,
                    null,jdbcPool,null,null);
            resourceConfig.setResourcesToLoad(resourcesToload);
            return resourceConfig;
        }
        return null;
    }
    
    public DeferredResourceConfig getDeferredResourceConfig(String resourceName) {
        DeferredResourceConfig resConfig = getDeferredConnectorResourceConfigs(
                resourceName);
        if(resConfig != null) {
            return resConfig;
        }
        
        resConfig = getDeferredJdbcResourceConfigs(
                resourceName);
        
        if(resConfig != null) {
            return resConfig;
        }
        
        resConfig = getDeferredAdminObjectConfigs(
                resourceName);
        
        return resConfig;
    }
    
    public DeferredResourceConfig getDeferredPoolConfig(String poolName) {
        
        DeferredResourceConfig resConfig = getDeferredConnectorPoolConfigs(
                poolName);
        if(resConfig != null) {
            return resConfig;
        }
        
        if(poolName == null){
            return null;
        }
        
        resConfig = getDeferredJdbcPoolConfigs(poolName);
        
        return resConfig;
    }
    
    protected String getCorrespondingCmpResourceName(String resourceName) {
        
        int index = resourceName.lastIndexOf("__pm");
        if(index != -1) {
            return resourceName.substring(0,index);
        }
        return null;
    }
    
    
    
    public Object[] getConnectorResourcesJndiNames(String poolName) {
        Vector jndiNamesVector = new Vector();
        ConnectorResource[] connectorResource = res.getConnectorResource();
        if (connectorResource == null || connectorResource.length == 0) {
            return null;
        }
        
        for(int i=0; i<connectorResource.length; ++ i) {
            if(connectorResource[i].getPoolName().equals(poolName)) {
                jndiNamesVector.add(connectorResource[i].getJndiName());
            }
        }
        return jndiNamesVector.toArray();
    }
    
    public Object[] getConnectorConnectionPoolNames(String moduleName) {
        Vector poolNamesVector = new Vector();
        ConnectorConnectionPool[] connectorConnectionPool=
                res.getConnectorConnectionPool();
        if (connectorConnectionPool== null
                || connectorConnectionPool.length == 0) {
            return null;
        }
        
        for(int i=0; i<connectorConnectionPool.length; ++ i) {
            if(connectorConnectionPool[i].getResourceAdapterName().equals(
                    moduleName)) {
                poolNamesVector.add(connectorConnectionPool[i].getName());
            }
        }
        return poolNamesVector.toArray();
    }
    
    public ConnectorConnectionPool[] getConnectorConnectionPools() {
        ConnectorConnectionPool[] connectorConnectionPool = null;
        connectorConnectionPool = res.getConnectorConnectionPool();
        if (connectorConnectionPool == null || connectorConnectionPool.length == 0)
            return null;
        return connectorConnectionPool;
    }
    
    public JdbcConnectionPool[] getJdbcConnectionPools() {
        JdbcConnectionPool[] jdbcConnectionPool = res.getJdbcConnectionPool();
        if(jdbcConnectionPool == null || jdbcConnectionPool.length == 0)
            return null;
        return jdbcConnectionPool;
    }
    
    public String[] getdbUserPasswordOfJdbcConnectionPool(
            JdbcConnectionPool jdbcConnectionPool) {
        
        String[] userPassword = new String[2];
        userPassword[0]=null;
        userPassword[1]=null;
        ElementProperty[] elementProperty =
                jdbcConnectionPool.getElementProperty();
        if(elementProperty==null || elementProperty.length == 0) {
            return userPassword;
        }
        
        for (int i=0; i<elementProperty.length;i++) {
            String prop = elementProperty[i].getName().toUpperCase();
            if ("USERNAME".equals( prop ) || "USER".equals( prop ) ) {
                userPassword[0]=elementProperty[i].getValue();
            } else if("PASSWORD".equals( prop ) ) {
                userPassword[1]=elementProperty[i].getValue();
            }
        }
        return userPassword;
    }
    
    public String[] getdbUserPasswordOfConnectorConnectionPool(
            ConnectorConnectionPool connectorConnectionPool) {
        
        String[] userPassword = new String[2];
        userPassword[0]=null;
        userPassword[1]=null;
        ElementProperty[] elementProperty =
                connectorConnectionPool.getElementProperty();
        if(elementProperty != null && elementProperty.length != 0) {
            boolean foundUserPassword = false;
            for (int i=0; i<elementProperty.length;i++) {
                String prop = elementProperty[i].getName().toUpperCase();
                
                if( "USERNAME".equals( prop ) || "USER".equals( prop ) ) {
                    userPassword[0]=elementProperty[i].getValue();
                    foundUserPassword = true;
                } else if("PASSWORD".equals( prop ) ) {
                    userPassword[1]=elementProperty[i].getValue();
                    foundUserPassword = true;
                }
            }
            if(foundUserPassword == true) {
                return userPassword;
            }
        }
        
        String poolName = connectorConnectionPool.getName();
        String rarName = connectorConnectionPool.getResourceAdapterName();
        String connectionDefName =
                connectorConnectionPool.getConnectionDefinitionName();
        ConnectorRegistry connectorRegistry =
                ConnectorRegistry.getInstance();
        ConnectorDescriptor connectorDescriptor =
                connectorRegistry.getDescriptor(rarName);
        ConnectionDefDescriptor cdd =
                connectorDescriptor.getConnectionDefinitionByCFType(
                connectionDefName);
        Set configProps = cdd.getConfigProperties();
        for(Iterator iter = configProps.iterator(); iter.hasNext();) {
            EnvironmentProperty envProp= (EnvironmentProperty)iter.next();
            String prop = envProp.getName().toUpperCase();
            
            if("USER".equals( prop ) || "USERNAME".equals( prop )) {
                
                userPassword[0]=envProp.getValue();
            } else if( "PASSWORD".equals(prop) ) {
                userPassword[1]=envProp.getValue();
            }
            
        }
        
        if ( userPassword[0] != null && ! "".equals(userPassword[0].trim()) ) {
            return userPassword;
        }
        
        //else read the default username and password from the ra.xml
        ManagedConnectionFactory mcf =
                connectorRegistry.getManagedConnectionFactory( poolName );
        userPassword[0] = ConnectionPoolObjectsUtils.getValueFromMCF(
                "UserName", poolName, mcf);
        userPassword[1] = ConnectionPoolObjectsUtils.getValueFromMCF(
                "Password", poolName, mcf);
        
        return userPassword;
    }
    
    public String getResourceType(ConfigBean cb) {
        if(cb instanceof ConnectorConnectionPool) {
            return ResourceDeployEvent.RES_TYPE_CCP;
        } else if(cb instanceof ConnectorResource) {
            return ResourceDeployEvent.RES_TYPE_CR;
        } else if(cb instanceof AdminObjectResource) {
            return ResourceDeployEvent.RES_TYPE_AOR;
        } else if(cb instanceof ResourceAdapterConfig) {
            return ResourceDeployEvent.RES_TYPE_RAC;
        } else if (cb instanceof JdbcConnectionPool) {
            return ResourceDeployEvent.RES_TYPE_JCP;
        } else if (cb instanceof JdbcResource) {
            return ResourceDeployEvent.RES_TYPE_JDBC;
        }
        return null;
    }
    
    
    public String getLocation(String moduleName) {
        
        if(moduleName == null) {
            return null;
        }
        String location  = null;
        ConnectorModule connectorModule =
                dom.getApplications().getConnectorModuleByName(moduleName);
        if(connectorModule != null) {
            location = RelativePathResolver.
                    resolvePath(connectorModule.getLocation());
        }
        return location;
        
    }
    
    public ConfigBean[][] getJdbcResources() throws ConfigException{
        ConfigBean configBeanArray[][] =
                new ConfigBean[NO_OF_JDBC_RESOURCE_TYPE][];
        Map allJdbcResources  = getJdbcResourcesAsMap();
        configBeanArray[0] = (ConfigBean[]) allJdbcResources.get(CONNECTION_POOLS);
        configBeanArray[1] = (ConfigBean[]) allJdbcResources.get(RESOURCES);
        return configBeanArray;
    }
    
    
    public Map getJdbcResourcesAsMap() throws ConfigException{
        
        HashMap allJdbcResources = new HashMap();
        int noOfJdbcResources = res.sizeJdbcResource();
        
        if(noOfJdbcResources == 0) {
            return allJdbcResources;
        }
        
        Vector<JdbcResource> jdbcResourcesVector =
                new Vector<JdbcResource>();
        Vector<JdbcConnectionPool> jdbcPoolsVector =
                new Vector<JdbcConnectionPool>();
        
        for(int i=0; i< noOfJdbcResources; ++i) {
            JdbcResource jdbcResource = res.getJdbcResource(i);
            if(jdbcResource == null)
                continue;
            JdbcConnectionPool jdbcPool = getJdbcConnectionPoolByName(
                    jdbcResource.getPoolName());
            if(jdbcPool == null)
                continue;
            
            if(!isEnabled(jdbcResource))
                continue;
            
            jdbcResourcesVector.add(jdbcResource);
            
            if(!jdbcPoolsVector.contains(jdbcPool))
                jdbcPoolsVector.add(jdbcPool);
        }
        
        if(jdbcResourcesVector.size() == 0)
            return allJdbcResources;
        
        ConfigBean[]jdbcPools = new JdbcConnectionPool[jdbcPoolsVector.size()];
        ConfigBean[]jdbcResources = new JdbcResource[jdbcResourcesVector.size()];
        jdbcPools = (JdbcConnectionPool[]) jdbcPoolsVector.toArray(
                jdbcPools);
        jdbcResources = (JdbcResource[]) jdbcResourcesVector.toArray(
                jdbcResources);
        allJdbcResources.put(CONNECTION_POOLS, jdbcPools);
        allJdbcResources.put(RESOURCES, jdbcResources);
        return allJdbcResources;
    }
    
    
    /**
     * Returns true if the given resource is referenced by this server.
     *
     * @param   resourceName   the name of the resource
     *
     * @return  true if the named resource is used/referred by this server
     *
     * @throws  ConfigException  if an error while parsing domain.xml
     */
    protected boolean isReferenced(String resourceName) throws ConfigException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("isReferenced :: " + resourceName + " - "
                    + ServerHelper.serverReferencesResource(
                    configContext_, sc_.getInstanceName(),
                    resourceName));
        }
        
        return ServerHelper.serverReferencesResource(configContext_,
                sc_.getInstanceName(), resourceName);
    }
    
    
    public boolean isEnabled(ConnectorResource cr) throws ConfigException {
        
        if(cr == null || !cr.isEnabled())
            return false;
        
        if(!isResourceReferenceEnabled(cr.getJndiName()))
            return false;
        
        String poolName = cr.getPoolName();
        ConnectorConnectionPool ccp = res.getConnectorConnectionPoolByName(poolName);
        if (ccp == null) {
            return false;
        }
        return isEnabled(ccp);
    }
    
    public boolean isEnabled(AdminObjectResource aot) throws ConfigException {
        if(aot == null || !aot.isEnabled())
            return false;
        if(!isResourceReferenceEnabled(aot.getJndiName()))
            return false;
        
        String raName = aot.getResAdapter();
        return isRarEnabled(raName);
    }
    
    public boolean isEnabled(ConnectorConnectionPool ccp) throws ConfigException {
        if(ccp == null) {
            return false;
        }
        String raName = ccp.getResourceAdapterName();
        return isRarEnabled(raName);
    }
    
    public boolean isEnabled(JdbcResource jr) throws ConfigException {
        
        if(jr == null || !jr.isEnabled())
            return false;
        
        if(!isResourceReferenceEnabled(jr.getJndiName()))
            return false;
        
        return true;
    }
    
    public boolean isEnabled(ResourceAdapterConfig rac) throws ConfigException {
        
        if(rac == null)
            return false;
        
        return isRarEnabled(rac.getResourceAdapterName());
    }
    
    private boolean isRarEnabled(String raName) throws ConfigException{
        if(raName == null || raName.length() == 0)
            return false;
        ConnectorModule module = dom.getApplications().getConnectorModuleByName(raName);
        if(module != null) {
            if(!module.isEnabled())
                return false;
            return isApplicationReferenceEnabled(raName);
        } else if(belongToSystemRar(raName)) {
            return true;
        } else {
            return belongToEmbeddedRarAndEnabled(raName);
        }
    }
    
    private boolean belongToEmbeddedRarAndEnabled(String resourceAdapterName) throws ConfigException {
        String appName = getAppNameToken(resourceAdapterName);
        if(appName==null)
            return false;
        Applications apps = dom.getApplications();
        J2eeApplication j2eeApp = apps.getJ2eeApplicationByName(appName);
        if(j2eeApp == null || !j2eeApp.isEnabled())
            return false;
        return isApplicationReferenceEnabled(appName);
    }
    
    public boolean belongToRar(String rarName,ConnectorConnectionPool ccp) {
        if(ccp == null || rarName== null) {
            return false;
        }
        if(ccp.getResourceAdapterName().equals(rarName)) {
            return true;
        } else {
            return false;
        }
    }
    public boolean belongToRar(String rarName,ConnectorResource cr) {
        if(cr == null || rarName== null) {
            return false;
        }
        String poolName = cr.getPoolName();
        ConnectorConnectionPool ccp = res.getConnectorConnectionPoolByName(poolName);
        if (ccp == null) {
            return false;
        }
        return belongToRar(rarName, ccp);
    }
    
    public boolean belongToRar(String rarName,AdminObjectResource aor) {
        if(aor == null || rarName== null) {
            return false;
        }
        if(aor.getResAdapter().equals(rarName)) {
            return true;
        } else {
            return false;
        }
    }
    
    
    /*
    public String[] listConnectorModuleNames() {
     
        Applications apps = dom.getApplications();
        if(apps == null)
            return null;
        ConnectorModule[] connectorModules = apps.getConnectorModule();
        if(connectorModules == null)
            return null;
        String[] connectorModuleNames = null;
        connectorModuleNames = new String[connectorModules.length];
        for(int i=0;connectorModules != null && i<connectorModules.length;
        ++i){
            connectorModuleNames[i] = connectorModules[i].getName();
        }
        return connectorModuleNames;
    }
     */
    
    
    /**
     * This method takes in an admin JdbcConnectionPool and returns the RA
     * that it belongs to.
     *
     * @param pool - The pool to check
     * @return The name of the JDBC RA that provides this pool's datasource
     *
     */
    
    public String getRAForJdbcConnectionPool( JdbcConnectionPool pool ) {
        String dsRAName = ConnectorConstants.JDBCDATASOURCE_RA_NAME;
        
        if ( pool.getResType() == null ) {
            return dsRAName;
        }
        
        //check if its XA
        if ( "javax.sql.XADataSource".equals( pool.getResType() ) ) {
            if ( pool.getDatasourceClassname() == null ) {
                return dsRAName;
            }
            try {
                Class dsClass=Utility.loadClass(pool.getDatasourceClassname());
                if (javax.sql.XADataSource.class.isAssignableFrom(dsClass)) {
                    return ConnectorConstants.JDBCXA_RA_NAME;
                }
            } catch( ClassNotFoundException cnfe) {
                return dsRAName;
            }
        }
        
        //check if its CP
        if ("javax.sql.ConnectionPoolDataSource".equals(pool.getResType())) {
            if ( pool.getDatasourceClassname() == null ) {
                return dsRAName;
            }
            try {
                Class dsClass=Utility.loadClass(pool.getDatasourceClassname());
                if(javax.sql.ConnectionPoolDataSource.class.isAssignableFrom(
                        dsClass) ) {
                    return
                            ConnectorConstants.JDBCCONNECTIONPOOLDATASOURCE_RA_NAME;
                }
            } catch( ClassNotFoundException cnfe) {
                return dsRAName;
            }
        }
        //default to __ds
        return dsRAName;
    }
    
    public ConnectorResource[] getAllJmsResources() throws ConfigException {
        ConnectorResource[] cr = (ConnectorResource[]) getFilteredAllConnectorResources(false, false, 
                ConnectorConstants.DEFAULT_JMS_ADAPTER).get(RESOURCES);
        
        return cr;
    }
    
    public AdminObjectResource[] getAllJmsAdminObjects() throws ConfigException {
        AdminObjectResource[] aor = (AdminObjectResource[]) getFilteredAdminObjectResources(false, false,
                ConnectorConstants.DEFAULT_JMS_ADAPTER);
        
        return aor;
    }
    
    /**
     * Gets the shutdown-timeout attribute from domain.xml
     * via the connector server config bean.
     * @return
     */
    public int getShutdownTimeout() throws ConnectorRuntimeException {
        try {
            ConnectorService connectorServiceElement = ServerBeansFactory
                    .getConnectorServiceBean(ApplicationServer.getServerContext().
                    getConfigContext());
            int shutdownTimeout;
            if (connectorServiceElement == null) {
                //Connector service element is not specified in
                //domain.xml and hence going with the default time-out
                shutdownTimeout =
                        ConnectorConstants.DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT;
                _logger.log(Level.FINE, "Shutdown timeout set to "
                        +  shutdownTimeout
                        + "through default");
                return shutdownTimeout;
            } else {
                shutdownTimeout = (new Integer(connectorServiceElement.
                        getShutdownTimeoutInSeconds())).intValue();
                _logger.log(Level.FINE, "Shutdown timeout set to "
                        + shutdownTimeout + " from domain.xml");
                return shutdownTimeout;
            }
        } catch (Exception e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(e.getMessage());
            crex.initCause(e);
            throw crex;
        }
    }
    
    public static boolean isDAS() {
        try {
            return ServerHelper.isDAS( com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getAdminConfigContext(), sc_.getInstanceName() );
        } catch( ConfigException ce ) {
            //better be more restrictive by returning false
            return false;
        }
    }
    
    
    /**
     * Determines if a connector connection pool is referred in a
     * server-instance via resource-refs
     *
     * @param poolName
     * @return boolean true if pool is referred in this server instance, false
     * otherwise
     * @throws ConfigException
     */
    public boolean isPoolReferredInServerInstance(String poolName)
    throws ConfigException {
        ConnectorResource[] connRes = res.getConnectorResource();
        
        for (int i = 0; i < connRes.length; i++) {
            _logger.fine("poolname " + connRes[i].getPoolName()
            + "resource " + connRes[i].getJndiName());
            
            if ((connRes[i].getPoolName().equalsIgnoreCase(poolName))){
                _logger.fine("Connector resource "  + connRes[i].getJndiName() +
                        "refers " + poolName + "in this server instance");
                return true;
            }
        }
        
        _logger.fine("No JDBC resource refers " + poolName
                + "in this server instance");
        return false;
    }
    
    /**
     * Determines if a JDBC connection pool is referred in a
     * server-instance via resource-refs
     *
     * @param jdbcPoolName
     * @return boolean true if pool is referred in this server instance, false
     * otherwise
     * @throws ConfigException
     */
    public boolean isJdbcPoolReferredInServerInstance(String jdbcPoolName)
    throws ConfigException {
        JdbcResource[] jdbcRes = res.getJdbcResource();
        
        for (int i = 0; i < jdbcRes.length; i++) {
            _logger.fine("poolname " + jdbcRes[i].getPoolName()
            + "resource " + jdbcRes[i].getJndiName()
            + " referred " + isReferenced(jdbcRes[i].getJndiName()));
            //Have to check isReferenced here!
            if ((jdbcRes[i].getPoolName().equalsIgnoreCase(jdbcPoolName))
            && isReferenced(jdbcRes[i].getJndiName())){
                _logger.fine("JDBC resource "  + jdbcRes[i].getJndiName() +
                        "refers " + jdbcPoolName + "in this server instance");
                return true;
            }
        }
        _logger.fine("No JDBC resource refers " + jdbcPoolName +
                "in this server instance");
        return false;
    }
    
    /**
     * Gets the list of applications deployed.
     * @param none
     * @return Application[] of deloyed applications.
     */
    
    public Application[] getDeployedApplications() throws ConfigException{
        _logger.log(Level.FINE, "in ResourcesUtil.getApplicationNames()");
        
        // Get list of deployed "Applications"
        Applications apps = dom.getApplications();
        // From Applications, get the J2ee Applications and ejb modules
        J2eeApplication[] j2ee_apps = apps.getJ2eeApplication();
        EjbModule[] ejb_modules = apps.getEjbModule();
        
        // This array would contain the deployment descriptors of all the
        // ejbmodules and aplications.
        ArrayList deployedAppsDescriptorList = new ArrayList();
        
        if ((j2ee_apps.length + ejb_modules.length) == 0)
            return (new Application[] {});
        
        // Get the respective Managers to retrieve Deployment descriptors
        AppsManager appsManager = getAppsManager();
        EjbModulesManager ejbModulesManager = getEjbModulesManager();
        
        // Get Deployment desc for J2EE apps.
        for (int i=0; i<j2ee_apps.length; i++){
            String appName = j2ee_apps[i].getName();
            // Check if the application is referenced by the server instance on
            // which recovery is happening.
            if (!(ServerHelper.serverReferencesApplication(configContext_, sc_.getInstanceName(), appName) ))
                continue;
            
            try{
                Application appDescriptor =
                        appsManager.getAppDescriptor(appName,
                        ASClassLoaderUtil.getSharedClassLoader());
                deployedAppsDescriptorList.add(appDescriptor);
            } catch (Exception e){
                String message = localStrings.getString(
                    "error.getting.application.DD",
                    appName);
                _logger.log(Level.WARNING, message + e.getMessage());
                _logger.log(Level.FINE,message + e.getMessage(), e);
            }
        }
        
        // Get deployment desc for EJB modules.
        for (int i=0; i<ejb_modules.length; i++){
            String modName = ejb_modules[i].getName();
            
            if (!(ServerHelper.serverReferencesApplication(configContext_, sc_.getInstanceName(), modName) ))
                continue;
            
            try{
                Application appDescriptor =
                        ejbModulesManager.getDescriptor(modName,
                        ASClassLoaderUtil.getSharedClassLoader());
                deployedAppsDescriptorList.add(appDescriptor);
            } catch (Exception e){
                String message = localStrings.getString(
                    "error.getting.module.DD",
                    modName);
                _logger.log(Level.WARNING, message + e.getMessage());
                _logger.log(Level.FINE,message + e.getMessage(), e);
            }
        }
        
        return (Application[])(deployedAppsDescriptorList.toArray(new Application[] {}));
        
    }
    
    public ConnectorDescriptor getConnectorDescriptorFromUri(String appName, String raLoc)
    throws ConfigException{
        AppsManager am = getAppsManager();
        Application app = am.getAppDescriptor(appName,
                ASClassLoaderUtil.getSharedClassLoader());
        return app.getRarDescriptorByUri(raLoc);
    }
    
    
    private AppsManager getAppsManager() throws ConfigException{
        InstanceEnvironment iEnv = sc_.getInstanceEnvironment();
        return InstanceFactory.createAppsManager(iEnv, false);
    }
    
    private EjbModulesManager getEjbModulesManager() throws ConfigException{
        InstanceEnvironment iEnv = sc_.getInstanceEnvironment();
        return InstanceFactory.createEjbModuleManager(iEnv, false);
    }
    
    
    /**
     * Gets the deployment location for a J2EE application.
     * @param rarName
     * @return
     */
    public String getApplicationDeployLocation(String appName) {
        J2eeApplication app = dom.getApplications().getJ2eeApplicationByName(appName);
        return RelativePathResolver.resolvePath(app.getLocation());
    }
    
    /**
     * Checks if a resource reference is enabled
     * @since SJSAS 8.1 PE/SE/EE
     */
    private boolean isResourceReferenceEnabled(String resourceName)
    throws ConfigException {
        ResourceRef ref = ServerHelper.getServerByName( configContext_,
                sc_.getInstanceName()).getResourceRefByRef(resourceName);
        if (ref == null) {
            _logger.fine("ResourcesUtil :: isResourceReferenceEnabled null ref");
            if(isADeployEvent())
                return true;
            else
                return false;
        }
        _logger.fine("ResourcesUtil :: isResourceReferenceEnabled ref enabled ?" + ref.isEnabled());
        return ref.isEnabled();
    }
    
    /**
     * Checks if a resource reference is enabled
     * @since SJSAS 9.1 PE/SE/EE
     */
    private boolean isApplicationReferenceEnabled(String appName)
    throws ConfigException {
        ApplicationRef appRef = ServerHelper.getServerByName( configContext_,
                sc_.getInstanceName()).getApplicationRefByRef(appName);
        if (appRef == null) {
            _logger.fine("ResourcesUtil :: isApplicationReferenceEnabled null ref");
            if(isADeployEvent())
                return true;
            else
                return false;
        }
        _logger.fine("ResourcesUtil :: isApplicationReferenceEnabled appRef enabled ?" + appRef.isEnabled());
        return appRef.isEnabled();
    }
    
    /**
     * Checks if a Resource is enabled.
     *
     * Since 8.1 PE/SE/EE, A resource [except resource adapter configs, connector and
     * JDBC connection pools which are global and hence enabled always] is enabled
     * only when the resource is enabled and there exists a resource ref to this
     * resource in this server instance and that resource ref is enabled.
     *
     * Before a resource is loaded or deployed, it is checked to see if it is
     * enabled.
     *
     * @since 8.1 PE/SE/EE
     */
    public boolean isEnabled(ConfigBean res) throws ConfigException{
        _logger.fine("ResourcesUtil :: isEnabled");
        if(res == null)
            return false;
        if(res instanceof JdbcResource)
            return isEnabled((JdbcResource)res);
        else if(res instanceof ConnectorResource)
            return isEnabled((ConnectorResource)res);
        else if(res instanceof AdminObjectResource)
            return isEnabled((AdminObjectResource)res);
        else if(res instanceof ResourceAdapterConfig)
            return isEnabled((ResourceAdapterConfig)res);
        else if(res instanceof ConnectorConnectionPool)
            return isEnabled((ConnectorConnectionPool)res);
        else if(res instanceof JdbcConnectionPool)
            //JDBC RA is system RA and is always enabled
            return true;

        if(!res.isEnabled())
            return false;
        
        Server server = ServerBeansFactory.getServerBean(configContext_);
        //using ServerTags, otherwise have to resort to reflection or multiple instanceof/casts
        ResourceRef resRef = server.getResourceRefByRef(res.getAttributeValue(ServerTags.JNDI_NAME));

        if(resRef == null)
            return false;
        
        return resRef.isEnabled();
    }
    
    
    public com.sun.enterprise.config.serverbeans.JdbcConnectionPool
            getJdbcConnectionPoolByName( String poolName ) {
        return res.getJdbcConnectionPoolByName( poolName );
    }
    
    public com.sun.enterprise.config.serverbeans.ConnectorConnectionPool
            getConnectorConnectionPoolByName( String poolName ) {
        return res.getConnectorConnectionPoolByName( poolName );
    }
    
    public com.sun.enterprise.config.serverbeans.JdbcConnectionPool
            getJDBCPoolForResource(String resourceName) throws ConfigException {
        JdbcResource jr = res.getJdbcResourceByJndiName(resourceName);
        if (isEnabled(jr)) {
            return this.getJdbcConnectionPoolByName(jr.getPoolName());
        }
        return null;
    }
    
    /**
     * Gets a PMF resource on the basis of its jndi name
     *
     * @param jndiName the jndi name of the PMF resource to lookup
     * @param checkReference if true, returns this PMF resource only if it is referenced in
     *                       this server. If false, returns the PMF resource irrespective of
     *                       whether it is referenced or not.
     */
    public PersistenceManagerFactoryResource getPMFResourceByJndiName(
            String jndiName ) {
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ResourceUtil :: looking up pmf resource, jndiName is :"
                    + jndiName );
        }
        PersistenceManagerFactoryResource pmf =
                res.getPersistenceManagerFactoryResourceByJndiName( jndiName );
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("getPMFResourceByJndiName:: looked up pmf resource : "
                    + pmf);
        }
        //does the isReferenced method throw NPE for null value? Better be safe
        if (pmf == null) {
            return null;
        }
        
        try {
            return isReferenced( jndiName ) ? pmf : null;
        }catch( ConfigException ce ) {
            return null;
        }
    }
    
    /**
     * Gets a JDBC resource on the basis of its jndi name
     * @param jndiName the jndi name of the JDBC resource to lookup
     * @param checkReference if true, returns this JDBC resource only if it is referenced in
     *                       this server. If false, returns the JDBC resource irrespective of
     *                       whether it is referenced or not.
     */
    public JdbcResource getJdbcResourceByJndiName( String jndiName) {
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ResourcesUtil :: looking up jdbc resource, jndiName is :"
                    + jndiName );
        }
        
        JdbcResource jdbcRes =
                res.getJdbcResourceByJndiName( jndiName );
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ResourcesUtil :: looked up jdbc resource:" + jdbcRes.getJndiName() );
        }
        //does the isReferenced method throw NPE for null value? Better be safe
        if (jdbcRes == null) {
            return null;
        }
        
        try {
            return isReferenced( jndiName ) ? jdbcRes : null;
        }catch( ConfigException ce ) {
            return null;
        }
    }
    
    /*
     * Get a list of connector connection pools excluding JMS CFs
     */
    public Map<ConnectorConnectionPool, MonitorableResourcePool> getPoolMap() {
        ConnectorConnectionPool[] cp = getConnectorConnectionPools();
        if (cp == null) {
            return null;
        }
        
        Map<ConnectorConnectionPool, MonitorableResourcePool> poolMap = 
            new HashMap<ConnectorConnectionPool, MonitorableResourcePool>();
        Map allPools = Switch.getSwitch().getPoolManager().getPoolTable();

        for( int i = 0 ; i < cp.length; i++ ) {
            if (!(cp[i].getName().equalsIgnoreCase(
                            ConnectorConstants.DEFAULT_JMS_ADAPTER))) {
                ResourcePool p = (ResourcePool) allPools.get( cp[i].getName() );

                if (p != null && (p instanceof MonitorableResourcePool )) {
                    poolMap.put(cp[i], (MonitorableResourcePool)p);
                }
            }
        }
        return poolMap;
    }
    
    /**
     * @param ccp connector connection pool
     * @return RA Name for the given connector connection pool
     */
    public String getRAName(ConnectorConnectionPool ccp) {
        if(ccp == null)
            return null;
        String resourceAdapterName = ccp.getResourceAdapterName();
        int delimIdx = resourceAdapterName.indexOf(ConnectorConstants.
                        EMBEDDEDRAR_NAME_DELIMITER);
        if (delimIdx == -1) {
            return resourceAdapterName;
        } else {
            return resourceAdapterName.substring(delimIdx + 1);
        }
    }

    /**
     * @param ccp connector connection pool
     * @return App Name for the given connector connection pool
     */
    public String getAppName(ConnectorConnectionPool ccp) {
        if(ccp == null)
            return null;
        String resourceAdapterName = ccp.getResourceAdapterName();
        int delimIdx = resourceAdapterName.indexOf(ConnectorConstants.
                        EMBEDDEDRAR_NAME_DELIMITER);
        if (delimIdx == -1) {
            return null;
        } else {
            return resourceAdapterName.substring(0, delimIdx);
        }
    }
    
    
    /**
     * Loads all system RA resources not used till now
     * This method is used when user accesses jndi tree.
     */
    public void loadSystemRAResources(){
        InitialContext ctx;
        
        try {
            ctx = new InitialContext();
        } catch (NamingException ex) {
            String message = localStrings.getString(
                    "error.getting.intial.context");
            _logger.log(Level.SEVERE, message);
            _logger.log(Level.FINE, ex.getMessage(), ex);
            return;
        }
        
        try {
            ConnectorResource[] cr = getAllJmsResources();
            for(int i=0; i<cr.length; i++){
                try {
                    ctx.lookup(cr[i].getJndiName());
                } catch (NamingException ex) {
                    String message = localStrings.getString(
                            "error.looking.up.resource",
                            cr[i].getJndiName());
                    _logger.log(Level.SEVERE, message);
                    _logger.log(Level.FINE, ex.getMessage(), ex);
                }
            }
        } catch (ConfigException ex) {
            String message = localStrings.getString(
                    "error.getting.jms.resources");
            _logger.log(Level.SEVERE, message);
        }
        try {
            AdminObjectResource[] aor = getAllJmsAdminObjects();
            for(int i=0; i<aor.length; i++){
                try {
                    ctx.lookup(aor[i].getJndiName());
                } catch (NamingException ex) {
                    String message = localStrings.getString(
                            "error.looking.up.resource",
                            aor[i].getJndiName());
                    _logger.log(Level.SEVERE, message);
                    _logger.log(Level.FINE, ex.getMessage(), ex);
                }
            }
        } catch (ConfigException ex) {
            String message = localStrings.getString(
                    "error.getting.jms.admin.objects");
            _logger.log(Level.SEVERE, message);
            _logger.log(Level.FINE, ex.getMessage(), ex);
        }
        
        try {
            JdbcResource[] jdbc = (JdbcResource[]) getJdbcResourcesAsMap().get(RESOURCES);
            for(int i=0; i<jdbc.length; i++){
                try {
                    ctx.lookup(jdbc[i].getJndiName());
                } catch (NamingException ex) {
                    String message = localStrings.getString(
                            "error.looking.up.resource",
                            jdbc[i].getJndiName());
                    _logger.log(Level.SEVERE, message);
                    _logger.log(Level.FINE, ex.getMessage(), ex);
                }
            }
        } catch (ConfigException ex) {
            String message = localStrings.getString(
                    "error.getting.jdbc.resources");
            _logger.log(Level.SEVERE, message);
            _logger.log(Level.FINE, ex.getMessage(), ex);
        }
    }
    
    /** 
     * Checks whether call is from a deploy event.
     * Since in case of deploy event, the localResourceUtil will be set, so check is based on that.
     */
    private boolean isADeployEvent(){
        if(localResourcesUtil.get() != null)
            return true;
        return false;
    }
}
