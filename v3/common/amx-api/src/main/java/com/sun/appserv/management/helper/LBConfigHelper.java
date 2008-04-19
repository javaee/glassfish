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

package com.sun.appserv.management.helper;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.*;
import com.sun.appserv.management.ext.lb.LoadBalancer;
import com.sun.appserv.management.util.misc.Formatter;
import com.sun.appserv.management.util.misc.StringSourceBase;

import javax.management.MBeanException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for simplifying load balancer administration.
 * @since AppServer 9.0
 */
public final class LBConfigHelper {
    
    /**
     * Public constructor
     */
    public LBConfigHelper(final DomainRoot domainRoot) {
        mDomainConfig = domainRoot.getDomainConfig();
        mDomainRoot = domainRoot;
        mLogger = Logger.getLogger(domainRoot.getMBeanLoggerName());
        resBundle = ResourceBundle.getBundle(this.getClass().getPackage().getName()+".LocalStrings");
        formatter = new Formatter(new StringSourceBase());
    }
    
    /**
     * Returns a filtered LBConfig list given the name of the server reference
     * @return Map of items, keyed by name.
     * @see com.sun.appserv.management.config.LBConfig
     */
    public List<LBConfig> getLBConfigsForServer(String serverName) {
        Map<String,LBConfig> lbconfigs = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        List<LBConfig> list = new ArrayList<LBConfig>();
        for(LBConfig config:lbconfigs.values()){
            Map<String,ServerRefConfig> map = config.getServerRefConfigMap();
            for(String name:map.keySet()){
                if(name.equals(serverName)){
                    list.add(config);
                }
            }
        }
        return list;
    }
    
    /**
     * Returns a filtered LBConfig list given the name of the cluster reference
     * @return Map of items, keyed by name.
     * @see com.sun.appserv.management.config.LBConfig
     */
    public List<LBConfig> getLBConfigsForCluster(String clusterName) {
        Map<String,LBConfig> lbconfigs = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        List<LBConfig> list = new ArrayList<LBConfig>();
        for(LBConfig config:lbconfigs.values()){
            Map<String,ClusterRefConfig> map = config.getClusterRefConfigMap();
            for(String name:map.keySet()){
                if(name.equals(clusterName)){
                    list.add(config);
                }
            }
        }
        return list;
    }
    
    /**
     * Returns a filtered list of server references given the name of the load
     * balancer config.
     * @return Map of items, keyed by name.
     * @see com.sun.appserv.management.config.LBConfig
     */
    public Map<String, ServerRefConfig> getServersInLBConfig(String lbConfigName) {
        Map<String,LBConfig> lbconfigs = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        LBConfig lbconfig = lbconfigs.get(lbConfigName);
        return lbconfig.getServerRefConfigMap();
    }
    
    /**
     * Returns a filtered list of cluster references given the name of the load
     * balancer config.
     * @return Map of items, keyed by name.
     * @see com.sun.appserv.management.config.LBConfig
     */
    public Map<String, ClusterRefConfig> getClustersInLBConfig(String lbConfigName) {
        Map<String,LBConfig> lbconfigs = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        LBConfig lbconfig = lbconfigs.get(lbConfigName);
        return lbconfig.getClusterRefConfigMap();
    }
    
    /**
     * Lists all the standalone server instances and clusters in the domain.
     *
     * @return all the standalone server instances and clusters in the domain
     */
    public String[] listTargets() {
        Set<String> targetSet = new HashSet<String>();
        
        Map<String,ClusterConfig> cConfigMap =
                mDomainConfig.getClustersConfig().getClusterConfigMap();
        
        if (cConfigMap != null) {
            targetSet.addAll( cConfigMap.keySet());
        }
        Map<String,StandaloneServerConfig> ssConfigMap =
                mDomainConfig.getServersConfig().getStandaloneServerConfigMap();
        
        if (ssConfigMap != null) {
            targetSet.addAll( ssConfigMap.keySet());
        }
        
        String[] targetArr = new String[targetSet.size()];
        return (String[]) targetSet.toArray(targetArr);
    }
    
    /**
     * Lists all the standalone server instances and clusters in the load
     * balancer.
     *
     * @param lbName    Name of the load balancer
     *
     * @return all the standalone server instances and clusters in the load
     * balancer
     */
    public String[] listTargets(final String lbName) {
        
        Set<String> targetSet = new HashSet<String>();
        
        Map<String, LoadBalancerConfig> lbMap =
                mDomainConfig.getLoadBalancersConfig().getLoadBalancerConfigMap();
        
        if (lbMap == null) {
            return null;
        }
        
        LoadBalancerConfig lb = (LoadBalancerConfig) lbMap.get(lbName);
        if (lb == null) {
            return null;
        }
        
        String lbConfigName = lb.getLbConfigName();
        Map<String, LBConfig> lbConfigMap = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        if ((lbConfigMap == null) || (lbConfigName == null) ){
            return null;
        }
        LBConfig lbConfig = (LBConfig) lbConfigMap.get(lbConfigName);
        if (lbConfig == null) {
            return null;
        }
        Map<String,ClusterRefConfig> cRefMap =
                lbConfig.getClusterRefConfigMap();
        
        if ( cRefMap != null) {
            targetSet.addAll(cRefMap.keySet());
        }
        Map<String,ServerRefConfig> sRefMap =
                    lbConfig.getServerRefConfigMap();
         
        if ( sRefMap != null) {
            targetSet.addAll(sRefMap.keySet());
        }
        
        String [] targetArr = new String[targetSet.size()];
        
        return (String[]) targetSet.toArray(targetArr);
        
    }
    
    /**
     * Creates a load balancer element ( and the necessary config)
     *
     *
     * @param loadbalancerName      Name of the load balancer
     * @param autoApplyEnabled      Auto apply enabled or not
     * @param targets               Standalone server instances or clusters
     * @param params                This is optional, loadbalancer
     *                              configuration elemements. The valid
     *                              keys are:
     *     <ul>
     *        <li>{@link LBConfigKeys#RESPONSE_TIMEOUT_IN_SECONDS_KEY}</li>
     *        <li>{@link LBConfigKeys#HTTPS_ROUTING_KEY}</li>
     *        <li>{@link LBConfigKeys#RELOAD_POLL_INTERVAL_IN_SECONDS_KEY}</li>
     *        <li>{@link LBConfigKeys#MONITORING_ENABLED_KEY}</li>
     *        <li>{@link LBConfigKeys#ROUTE_COOKIE_ENABLED_KEY}</li>
     *     </ul>
     *
     * @return {@link LoadBalancerConfig}
     */
    public LoadBalancerConfig createLoadbalancer(String loadbalancerName,
            boolean autoApplyEnabled, String[] targets, Map<String,String> params) {
        
        // first create the lb-config element
        
        if ( loadbalancerName == null ) {
            throw new IllegalArgumentException(
                    "loadbalancerName can not be null");
        }
        
        //iniitialize lb-config name that we are going to use for this lb
        String lbConfigName = loadbalancerName + LB_CONFIG_SUFFIX;
        
        //the following block tries to get a unique lb-config name
        //get all the lb-configs
        Map<String,LBConfig> lbconfigMap = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        if(lbconfigMap != null){
            //keep appending a counter till there is no lb-config by that name
            for(int i=1;lbconfigMap.get(lbConfigName) != null;i++){
                lbConfigName = loadbalancerName + LB_CONFIG_SUFFIX + "_" + i;
            }
        }
        
        //create the lb-config
        LBConfig lbConfig = mDomainConfig.getLBConfigsConfig().createLBConfig(lbConfigName, params);
        
        //get the default values for health-checker
        final Map healthCheckerAttrsMap = mDomainRoot.getDomainConfig().getDefaultValues(HealthCheckerConfig.J2EE_TYPE);
        final String healthCheckerUrl = (String) healthCheckerAttrsMap.get("url");
        final String healthCheckerInterval = (String) healthCheckerAttrsMap.get("interval-in-seconds");
        final String healthCheckerTimeout = (String) healthCheckerAttrsMap.get("timeout-in-seconds");
        
        if ( targets != null) {
            for (int idx =0; idx < targets.length; idx++) {
                String targetName = targets[idx];
                
                if ( isCluster(targetName)) {
                    //create the cluster-ref
                    ClusterRefConfig clusterRefConfig = 
                            lbConfig.createClusterRefConfig(targetName, null);
                    //create the health checker
                    clusterRefConfig.createHealthCheckerConfig(healthCheckerUrl,
                            healthCheckerInterval, healthCheckerTimeout);
                } else if ( isStandaloneServer( targetName)) {
                    //create the server-ref
                    ServerRefConfig serverRefConfig = 
                            lbConfig.createServerRefConfig(targetName, null);
                    //create the health checker
                    serverRefConfig.createHealthCheckerConfig(healthCheckerUrl,
                            healthCheckerInterval, healthCheckerTimeout);
                }
            }
        }
        
        // now create the load-balancer element
        return mDomainConfig.getLoadBalancersConfig().createLoadBalancerConfig(loadbalancerName,
                lbConfigName, autoApplyEnabled, null);
    }
    
    /**
     * This method supports the create-http-lb CLI command. It creates a lb-config, cluster-ref, health-checker by using
     * the given parameters.
     * @param loadbalancername the name for the load-balancer element that will be created
     * @param target cluster-ref or server-ref parameter of lb-config
     * @param options Map of option name and option value. The valid options are
     *          responsetimeout response-timeout-in-seconds attribute of lb-config
     *          httpsrouting https-routing parameter of lb-config
     *          reloadinterval reload-poll-interval-in-seconds parameter of lb-config
     *          monitor monitoring-enabled parameter of lb-config
     *          routecookie route-cookie-enabled parameter of lb-config
     *          lb-policy load balancing policy to be used for the cluster target
     *          lb-policy-module specifies the path to the shared library implementing the user-defined policy
     *          auto-apply-enabled pushes changes from lb config to the physical load balancer
     *          healthcheckerurl url attribute of health-checker
     *          healthcheckerinterval interval-in-seconds parameter of health-checker
     *          healthcheckertimeout timeout-in-seconds parameter of health-checker
     * @param filePath the path to the file where loadbalancer.xml will be exported
     * @return the path to the newly written file
     * @since AS 9.0
     * @throws javax.management.MBeanException exception indicating the original cause of problm
     */
    public final LoadBalancerConfig createLoadBalancer(String loadbalancerName, 
            String target, Map<String,String> options, 
            Map<String,String> properties) throws MBeanException {
        
        if (loadbalancerName == null ) {
            throw new IllegalArgumentException(
                    "loadbalancerName can not be null");
        }
        //check if the load-balancer with the name already exists
        if (mDomainConfig.getLoadBalancersConfig().getLoadBalancerConfigMap().get(loadbalancerName)!=null)
        {
            String msg = formatter.format(
                            resBundle.getString("LoadBalancerConfigExists"),
                                                loadbalancerName);  
            throw new MBeanException(new RuntimeException(msg));
        }
        
        String healthCheckerUrl = options.get(HEALTH_CHECKER_URL);
        String healthCheckerInterval = options.get(HEALTH_CHECKER_INTERVAL);
        String healthCheckerTimeout = options.get(HEALTH_CHECKER_TIMEOUT);
        String lbPolicy = options.get(LB_POLICY);
        String lbPolicyModule = options.get(LB_POLICY_MODULE);
        boolean autoApplyEnabled = Boolean.valueOf(
                    options.get(AUTO_APPLY_ENABLED)).booleanValue();
        boolean enableAllInstances = Boolean.valueOf(
                    options.get(LB_ENABLE_ALL_INSTANCES)).booleanValue();
        boolean enableAllApps = Boolean.valueOf(
                    options.get(LB_ENABLE_ALL_APPLICATIONS)).booleanValue();
        boolean isCluster = isCluster(target);
        Map<String,String> params = getParams(options);
        //iniitialize lb-config name that we are going to use for this lb
        String lbConfigName = loadbalancerName + LB_CONFIG_SUFFIX;
        
        //the following block tries to get a unique lb-config name
        //get all the lb-configs
        Map<String,LBConfig> lbconfigMap = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        if(lbconfigMap != null){
            //keep appending a counter till there is no lb-config by that name
            for(int i=1;lbconfigMap.get(lbConfigName) != null;i++){
                lbConfigName = loadbalancerName + LB_CONFIG_SUFFIX + "_" + i;
            }
        }
        
        if(!isCluster){
            if((lbPolicy!=null) || (lbPolicyModule!=null)){
                //throw exception
                String msg = formatter.format(resBundle.getString("NotCluster"),
                        target);
                throw new MBeanException(new RuntimeException(msg));
            }
        }
        //create the lb-config
        LBConfig lbConfig = mDomainConfig.getLBConfigsConfig().createLBConfig(lbConfigName, params);
        
        if(isCluster){
            
            //create the cluster-ref
            ClusterRefConfig clusterRefConfig = 
                    lbConfig.createClusterRefConfig(target, lbPolicy, 
                        lbPolicyModule);
            //create the health checker
            clusterRefConfig.createHealthCheckerConfig(healthCheckerUrl,
                    healthCheckerInterval, healthCheckerTimeout);
        }else{
            //create the server-ref
            ServerRefConfig serverRefConfig = lbConfig.createServerRefConfig(target,"30",true,true);
            //create the health checker
            serverRefConfig.createHealthCheckerConfig(healthCheckerUrl,
                    healthCheckerInterval, healthCheckerTimeout);
        }
        //lb-enable all the instances in the target
        if (enableAllInstances)
            setServerStatus(target, 0,true, true);
        else
            setServerStatus(target, 0,false, true);
        //lb-enable all applications in the target, dtd's default value is false
        if (enableAllApps)
            enableAllApplications(target);

        // now create the load-balancer element
        return mDomainConfig.getLoadBalancersConfig().createLoadBalancerConfig(loadbalancerName,
                lbConfigName, autoApplyEnabled, properties);
    }
    
    /**
     * Deletes a load balancer element ( and the necessary config, if nobody
     * else is using this config)
     */
    public void removeLoadbalancer(String loadbalancerName) {

        //first get the lbConfigName
        final LoadBalancerConfig loadbalancerConfig = 
                mDomainConfig.getLoadBalancersConfig().getLoadBalancerConfigMap().get(loadbalancerName);
        if(loadbalancerConfig == null){
            final String msg = formatter.format(
                resBundle.getString("LoadBalancerConfigNotDefined"),loadbalancerName);
            throw new RuntimeException(msg);            
        }
        final String lbConfigName = loadbalancerConfig .getLbConfigName();
        
        //get the load balancers map
        final Map<String, LoadBalancerConfig> lbMap = mDomainConfig.getLoadBalancersConfig().getLoadBalancerConfigMap();
        if ( lbMap != null) {
            // check to see if any other load-balancer is using lb-config
            for(LoadBalancerConfig lbConfig : lbMap.values()){
                if (!lbConfig.getName().equals(loadbalancerConfig.getName()) &&
                        lbConfig.getLbConfigName().equals(lbConfigName)) {
                    // this load-balancer element is still using it
                    final String msg = formatter.format(resBundle.getString("LbConfigIsInUse"),
                                                    lbConfigName);
                    throw new RuntimeException(msg);            
                }
            }
        }        
        // now remove load-balancer element
        mDomainConfig.getLoadBalancersConfig().removeLoadBalancerConfig(loadbalancerName);
        // no load-balancer element is using this lb-config, remove it
        mDomainConfig.getLBConfigsConfig().removeLBConfig(lbConfigName);
    }
    
    /**
     * This method supports the delete-http-lb-ref CLI command. It removes a 
     * server-ref|cluster-ref by using the given parameters.
     * @param lbName the name of the load-balancer element that exists
     * @param configName the name of the lb-config element that exists
     * @param target cluster-ref or server-ref parameter of lb-config
     * @param force force the removal of the server-ref|cluster-ref
     * @return return the newly created lbref
     * @since AS 9.1
     * @throws javax.management.MBeanException exception indicating the original cause of problm
     */
    public String removeLBRef(String lbName, String configName, String target, 
                boolean force) throws MBeanException {
        
        boolean isCluster = isCluster(target);
        if((configName!=null) && 
            (mDomainConfig.getLBConfigsConfig().getLBConfigMap().get(configName)==null)){
                
            String msg = formatter.format(resBundle.getString("LbConfigNotDefined"),
                                            configName);
            throw new MBeanException(new RuntimeException(msg));            
        }
        else if (lbName!=null){
            LoadBalancerConfig loadBalancerConfig = 
                    mDomainConfig.getLoadBalancersConfig().getLoadBalancerConfigMap().get(lbName);
            if (loadBalancerConfig==null){
                String msg = formatter.format(
                    resBundle.getString("LoadBalancerConfigNotDefined"),lbName);
                throw new MBeanException(new RuntimeException(msg));            
            }
            configName = loadBalancerConfig.getLbConfigName();
        }
        LBConfig lbConfig = mDomainConfig.getLBConfigsConfig().getLBConfigMap().get(configName);
        if(isCluster){
            if (!force){
            //check the lb-enabled flag for all server-refs in this cluster
                Map<String, ServerRefConfig> serverRefConfigMap = 
                    mDomainConfig.getClustersConfig().getClusterConfigMap().get(target).getServerRefConfigMap();
                for (ServerRefConfig serverRefConfig : serverRefConfigMap.values()) {
                    if ( serverRefConfig.getLBEnabled() ){
                        String msg = formatter.format(
                        resBundle.getString("DisableServer"), target);
                        throw new MBeanException(new RuntimeException(msg));            
                    }
                }
            }
            
            //remove the cluster-ref
            lbConfig.removeClusterRefConfig(target);
        } else {
            if (!force && 
                ( lbConfig.getServerRefConfigMap().get(target).getLBEnabled()) ){
                String msg = formatter.format(
                    resBundle.getString("DisableServer"), target);
                throw new MBeanException(new RuntimeException(msg));            
                
            }
            //remove the server-ref
            lbConfig.removeServerRefConfig(target);
        }

        return null;
    }
    
    
    /**
     * Disables load balancing for a server with a quiescing period specififed
     * by the timeout .
     *
     * @param target target server whose load balancing has to be disabled
     * @param timeout quiescing time
     */
    public void disableServer(String target, int timeout) {
        setServerStatus(target,timeout,false, false);
    }
    
    /**
     * Enables a server for load balancing.
     *
     * @param target target server whose load balancing has to be enabled
     */
    public void enableServer(String target) {
        setServerStatus(target, 0, true, false);
    }
    
    /**
     * Disables load balancing for a particular application in a server instance
     * with a quiescing period specififed by the timeout .
     *
     * @param target target server where the application has been deployed
     * @param appName application name.
     * @param timeout quiescing time
     */
    public void disableApplication(String target, String appName, int timeout) {
        setApplicationStatus(target, appName, timeout, false);
    }
    
    /**
     * Enables load balancing for a particular application in a server instance
     *
     * @param target target server where the application has been deployed
     * @param appName application name.
     */
    public void enableApplication(String target, String appName) {
        setApplicationStatus(target, appName, 0, true);
    }
    
    /**
     * This is a convenience method to fetch the stats for server instance(s) 
     * which are either standalone or clustered or both
     * @param targetLoadBalancer    Load Balancer for which stats are to be
     *                              returned
     * @param target                Target cluster name. This is used if
     *                              allTargets (next param) is false.
     * @param allTargets            list Monitors for all targets.
     *
     * @return Map of LoadBalancerServerStats and the fully qualified names 
     * of the servers i.e. clustername.servername or servername
    public Map<String, LoadBalancerServerStats> getInstanceStats(
            final String targetLoadBalancer, final String target, boolean allTargets) {
        
        if ( targetLoadBalancer == null )
            throw new IllegalArgumentException(
                    "Load Balancer Name can not be null");
        if ( !allTargets && target == null )
            throw new IllegalArgumentException(
                    "Specify AllTargets or atleast one target");
        
        Map<String,LoadBalancerServerStats>
                loadBalancerServerStatsMap = new HashMap<String,LoadBalancerServerStats>();
        
        Map<String, LoadBalancerServerMonitor> instanceMonitorMap =
                getInstanceMonitors(targetLoadBalancer, target, allTargets);
        
        for (String serverFQName : instanceMonitorMap.keySet()) {
            LoadBalancerServerMonitor loadBalancerServerMonitor =
                    instanceMonitorMap.get(serverFQName);
            LoadBalancerServerStats loadBalancerServerStats =
                    loadBalancerServerMonitor.getLoadBalancerServerStats();
            loadBalancerServerStatsMap.put(serverFQName, loadBalancerServerStats);
        }
        return loadBalancerServerStatsMap;
    }
     */
    
    /**
     * This is a convenience method to fetch the stats for context roots 
     * for an application.
     * @param targetLoadBalancer    Load Balancer for which stats are to be
     *                              returned
     * @param target                Target cluster name. This is used if
     *                              allTargets (next param) is false.
     * @param allTargets            list Monitors for all targets.
     *
     * @return Map of LoadBalancerContextRootStats and the fully qualified names 
     * of the servers i.e. clustername.servername or servername
    public Map<String, LoadBalancerContextRootStats> getInstanceStats(
            final String targetLoadBalancer, final String contextRoot,
            final String target, boolean allTargets) {
        
        if ( contextRoot == null )
            throw new IllegalArgumentException("ContextRoot can not be null");
        if ( targetLoadBalancer == null )
            throw new IllegalArgumentException(
                    "Load Balancer Name can not be null");
        if ( !allTargets && target == null )
            throw new IllegalArgumentException(
                    "Specify AllTargets or atleast one target");
        
        Map<String,LoadBalancerContextRootStats>
                loadBalancerContextRootStatsMap = new HashMap<String,LoadBalancerContextRootStats>();
        
        Map<String, LoadBalancerServerMonitor> instanceMonitorMap =
                getInstanceMonitors(targetLoadBalancer, target, allTargets);
        
        for (String serverFQName : instanceMonitorMap.keySet()) {
            LoadBalancerServerMonitor loadBalancerServerMonitor =
                    instanceMonitorMap.get(serverFQName);
            Map<String, LoadBalancerApplicationMonitor>
                    loadBalancerApplicationMonitorMap =
                    loadBalancerServerMonitor.getLoadBalancerApplicationMonitorMap();
            for (String appName : loadBalancerApplicationMonitorMap.keySet()) {
                LoadBalancerApplicationMonitor loadBalancerApplicationMonitor =
                        loadBalancerApplicationMonitorMap.get(appName);
                Map<String, LoadBalancerContextRootMonitor>
                        loadBalancerContextRootMonitorMap =
                        loadBalancerApplicationMonitor.getLoadBalancerContextRootMonitorMap();
                LoadBalancerContextRootMonitor loadBalancerContextRootMonitor =
                        loadBalancerContextRootMonitorMap.get(contextRoot);
                loadBalancerContextRootStatsMap.put(
                        serverFQName, (LoadBalancerContextRootStats)loadBalancerContextRootMonitor.getStats());
            }
        }
        return loadBalancerContextRootStatsMap;
    }
     */
    
    /**
     * This method supports the create-http-lb-ref CLI command. It creates a server-ref|cluster-ref, health-checker by using
     * the given parameters.
     * @param lbName the name of the load-balancer element that exists
     * @param configName the name of the lb-config element that exists
     * @param target cluster-ref or server-ref parameter of lb-config
     * @param options Map of option name and option value. The valid options are
     *          healthcheckerurl url attribute of health-checker
     *          healthcheckerinterval interval-in-seconds parameter of health-checker
     *          healthcheckertimeout timeout-in-seconds parameter of health-checker
     * @return return the newly created lbref
     * @since AS 9.1
     * @throws javax.management.MBeanException exception indicating the original cause of problm
     */
    public String createLBRef(String lbName, String configName, 
            String target, Map<String,String> options) throws MBeanException {
        
        String healthCheckerUrl = options.get(HEALTH_CHECKER_URL);
        String healthCheckerInterval = options.get(HEALTH_CHECKER_INTERVAL);
        String healthCheckerTimeout = options.get(HEALTH_CHECKER_TIMEOUT);
        String lbPolicy = options.get(LB_POLICY);
        String lbPolicyModule = options.get(LB_POLICY_MODULE);
        boolean isCluster = isCluster(target);
        boolean enableAllInstances = 
                Boolean.getBoolean(options.get(LB_ENABLE_ALL_INSTANCES));
        boolean enableAllApps = 
                Boolean.getBoolean(options.get(LB_ENABLE_ALL_APPLICATIONS));
        
        if((configName!=null) && 
            (mDomainConfig.getLBConfigsConfig().getLBConfigMap().get(configName)==null)){
                
            String msg = formatter.format(resBundle.getString("LbConfigNotDefined"),
                                            configName);
            throw new MBeanException(new RuntimeException(msg));            
        }
        else if (lbName!=null){
            LoadBalancerConfig loadBalancerConfig = 
                    mDomainConfig.getLoadBalancersConfig().getLoadBalancerConfigMap().get(lbName);
            if (loadBalancerConfig==null){
                String msg = formatter.format(
                    resBundle.getString("LoadBalancerConfigNotDefined"),lbName);
                throw new MBeanException(new RuntimeException(msg));            
            }
            configName = loadBalancerConfig.getLbConfigName();
        }
        LBConfig lbConfig = mDomainConfig.getLBConfigsConfig().getLBConfigMap().get(configName);
        if(!isCluster){
            if((lbPolicy!=null) || (lbPolicyModule!=null)){
                //throw exception
                String msg = formatter.format(resBundle.getString("NotCluster"),
                        target);
                throw new MBeanException(new RuntimeException(msg));
            }
        }
        if(isCluster){
            
            //create the cluster-ref
            ClusterRefConfig clusterRefConfig = 
                lbConfig.createClusterRefConfig(target, lbPolicy, lbPolicyModule);
            //create the health checker
            clusterRefConfig.createHealthCheckerConfig(healthCheckerUrl,
                    healthCheckerInterval, healthCheckerTimeout);
        }else{
            //create the server-ref
            ServerRefConfig serverRefConfig = lbConfig.createServerRefConfig(target,"30",true,true);
            //create the health checker
            serverRefConfig.createHealthCheckerConfig(healthCheckerUrl,
                    healthCheckerInterval, healthCheckerTimeout);
        }
        //lb-enable all the instances in the target
        if (enableAllInstances)
            enableServer(target);
        //lb-enable all applications in the target
        if (enableAllApps)
            enableAllApplications(target);

        return null;
    }
    
    /**
     * Configures the lb-weight attribute of each instance with the corresponding weight. Corresponds to the CLI 
     * command configure-lb-weight
     * @param clusterName name of the cluster
     * @param instanceVsWeights Map of instance name Vs weight
     */
    public void configureLBWeight(String clusterName, Map instanceVsWeights) {
        
        //get ALL clustered <server> elements
        Map<String,ClusterConfig> clusterConfigMap = mDomainConfig.getClustersConfig().getClusterConfigMap();
        //get the cluster config for the given cluster
        ClusterConfig clusterConfig = clusterConfigMap.get(clusterName);
        if(clusterConfig == null)
            throw new IllegalArgumentException(formatter.format(resBundle.getString("InvalidCluster"),clusterName));
        //get the map of clustered server config
        Map<String,ClusteredServerConfig> clusteredServerConfigMap = clusterConfig.getClusteredServerConfigMap();
        //iterate through all the given weights and instances
        for(Object instance:instanceVsWeights.keySet()){
            //get the corresponding clustered server config for this instance
            ClusteredServerConfig clusteredServerConfig = clusteredServerConfigMap.get(instance);
            if(clusteredServerConfig == null)
                throw new IllegalArgumentException(formatter.format(resBundle.getString("InvalidInstance"),instance,clusterName));
            //set the lb-weight
            clusteredServerConfig.setLBWeight( instanceVsWeights.get(instance).toString());
        }
    }

    /**
     * Enables all user applications in the given target
     * @param target The target for which all the applications have to be enabled
     */
    public void enableAllApplications(String target){
        //check if the target is cluster
        if (isCluster(target)) {
            //get the cluster config
            ClusterConfig cRef = mDomainConfig.getClustersConfig().getClusterConfigMap().get(target);
            enableAllApplications(cRef.getDeployedItemRefConfigMap());
        } else {
            //The target must be server, get the server config for this target
            StandaloneServerConfig serverConfig = mDomainConfig.getServersConfig().getStandaloneServerConfigMap().get(target);
            if(serverConfig != null){
                enableAllApplications(serverConfig.getDeployedItemRefConfigMap());
            }
        }
    }

    // PRIVATE METHODS
    /*
     * Sets the lb-enabled flag to true for all the deployed items.
     * Deployed items can be one of the J2EE-App, Web/EJB/RAR/AppClient Modules.
     */
    private void enableAllApplications(final Map<String,DeployedItemRefConfig> deployedItemRefConfigMap) {
        //iterate through all the deployed items
        for (DeployedItemRefConfig deployedItemRefConfig : deployedItemRefConfigMap.values()) {
            
            //Check to see if the deployed item is App or Module (Web, EJB, RAR or Appclient)
            final ApplicationConfig app = 
                    mDomainConfig.getApplicationsConfig().getApplicationConfigMap().get(deployedItemRefConfig.getName());
            if(app != null) {
                //if the type is user, then only set the lb-enabled to true
                if(app.getObjectType().equals(ObjectTypeValues.USER)) {
                    deployedItemRefConfig.setLBEnabled( true );
                }
                continue;
            }
            //Check to see if this is Web module
            final WebModuleConfig web = 
                    mDomainConfig.getApplicationsConfig().getWebModuleConfigMap().get(deployedItemRefConfig.getName());
            if (web != null) {
                //if the type is user, then only set the lb-enabled to true
                if(web.getObjectType().equals(ObjectTypeValues.USER)) {
                    deployedItemRefConfig.setLBEnabled( true);
                }
                continue;
            }
            //Check to see if this is EJB module
            final EJBModuleConfig ejb = 
                    mDomainConfig.getApplicationsConfig().getEJBModuleConfigMap().get(deployedItemRefConfig.getName());
            if (ejb != null) {
                //if the type is user, then only set the lb-enabled to true
                if(ejb.getObjectType().equals(ObjectTypeValues.USER)) {
                    deployedItemRefConfig.setLBEnabled( true);
                }
                continue;
            }
            //Check to see if this is RAR module
            final RARModuleConfig rar = 
                    mDomainConfig.getApplicationsConfig().getRARModuleConfigMap().get(deployedItemRefConfig.getName());
            if (rar != null) {
                //if the type is user, then only set the lb-enabled to true
                if(rar.getObjectType().equals(ObjectTypeValues.USER)) {
                    deployedItemRefConfig.setLBEnabled( true);
                }
                continue;
            }
            //Check to see if this is AppClient module
            final AppClientModuleConfig appClient = 
                    mDomainConfig.getApplicationsConfig().getAppClientModuleConfigMap().get(deployedItemRefConfig.getName());
            if (appClient != null) {
                //if the type is user (There is no API yet), then only set the lb-enabled to true
                //if(appClient.getObjectType().equals(ObjectTypeValues.USER)) {
                    deployedItemRefConfig.setLBEnabled( true);
                //}
                continue;
            }
        }
    }
    
    boolean isCluster(String name) {
        Map<String,ClusterConfig> cConfigMap =
                mDomainConfig.getClustersConfig().getClusterConfigMap();
        
        if (cConfigMap == null) {
            return false;
        }
        ClusterConfig cConfig = (ClusterConfig) cConfigMap.get(name);
        if ( cConfig == null) {
            return false;
        } else {
            return true;
        }
    }
    
    boolean isStandaloneServer(String name) {
        Map<String,StandaloneServerConfig> ssConfigMap =
                mDomainConfig.getServersConfig().getStandaloneServerConfigMap();
        
        if (ssConfigMap == null) {
            return false;
        }
        StandaloneServerConfig ssConfig = (StandaloneServerConfig)
        ssConfigMap.get(name);
        if ( ssConfig == null) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * sets the lb-enabled flag for the target, also sets the disable-timeout-in-minutes
     * if the status flag is false.
     * @param target the cluster or the server for which the lb-enabled has to be set
     * @param timeout The disable-timeout-in-minutes if the server has to be disabled
     * @param status true to enable, false to disable the server
     * @param ignore false throw exception if the previous value is same, true
     * otherwise
     */
    private void setServerStatus(String target, int timeout, boolean status,
    boolean ignore) {
        //timeout has to be specified for disable operation
        if (timeout < 0 && !status) {
            String msg = resBundle.getString("InvalidNumber");
            throw new IllegalArgumentException(msg);
        }
        
        try {
            // disables cluster if target is a cluster
            if (isCluster(target)) {
                //get the cluster config
                ClusterConfig cRef = mDomainConfig.getClustersConfig().getClusterConfigMap().get(target);
                if (cRef == null) {
                    mLogger.log(Level.FINEST," server " + target +
                            " does not exist in any cluster in the domain");
                    String msg = formatter.format(resBundle.getString("ServerNotDefined"),
                            target);
                    throw new MBeanException(new RuntimeException(msg));
                }

                //iterate through all the servers in the cluster
                for(ServerRefConfig sRef : cRef.getServerRefConfigMap().values()){
                    //set lb-enabled flag and the timeout with error checks
                    setLBEnabled(sRef, status, timeout, target, ignore);
                }
            } else { // target is a server
                ServerRefConfig sRef = null;
                boolean foundTarget = false;
                //get all the lb-configs
                Map<String,LBConfig> lbConfigs = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
                //iterate through lb-configs
                for(LBConfig lbConfig : lbConfigs.values()){
                    //get the server-ref in this lb-config
                    Map<String,ServerRefConfig> serverRefs = lbConfig.getServerRefConfigMap();
                    //get the server-ref for this target
                    sRef = serverRefs.get(target);
                    if (sRef == null) {
                        mLogger.log(Level.FINEST," server " + target +
                                " does not exist in " + serverRefs);
                    } else {
                        foundTarget = true;
                        break;
                    }
                }
                // did not find server target
                if (!foundTarget) {
                    //get the server-ref from some some cluster
                    sRef = getServerRefConfigFromCluster(target);
                    if (sRef == null) {
                        mLogger.log(Level.FINEST," server " + target +
                                " does not exist in any cluster in the domain");
                        String msg = formatter.format(resBundle.getString("ServerNotDefined"),
                                            target);
                        throw new MBeanException(new RuntimeException(msg));
                    }
                }
                //set the lb-enabled flag
                setLBEnabled(sRef, status, timeout, target, ignore);
            }
            
        } catch(Exception ce) {
            ce.printStackTrace();
            throw new RuntimeException(ce);
        }
        
    }

    /**
     * sets the lb-enabled flag for the target
     * @param sRef server-ref class
     * @param status the enable status
     * @param timeout disable timeout
     * @param target the target cluster or server
     * @param ignore false throw exception if the previous value is same, true
     * @throws javax.management.MBeanException wrapper for all exceptions
     */
    private void setLBEnabled(final ServerRefConfig sRef, final boolean status, 
            final int timeout, final String target, boolean ignore) throws MBeanException {
        int curTout = sRef.getDisableTimeoutInMinutes();
        //check if it is already in the state desired
        boolean enabled = sRef.getLBEnabled();
        if(!status){
            if ((ignore == false) && (enabled == false) && (curTout == timeout)) {
                        String msg = formatter.format(resBundle.getString("ServerDisabled"),
                                sRef.getRef());
                        throw new MBeanException(new Exception(msg));
            }
            //set the disable timeout in minutes
            sRef.setDisableTimeoutInMinutes(timeout);
            //set the lb-enabled to false
            sRef.setLBEnabled(false);
            //mLogger.log(Level.INFO,formatter.format(resBundle.getString(
            //        "http_lb_admin.ServerDisabled"), target));
        }else{
            if ((ignore == false) && (enabled == true)){
                        String msg = formatter.format(resBundle.getString("ServerEnabled"),
                                sRef.getRef());
                throw new MBeanException(new Exception("ServerEnabled"));
            }
            sRef.setLBEnabled(true);
            //mLogger.log(Level.INFO,formatter.format(resBundle.getString(
            //        "http_lb_admin.ServerEnabled"), target));
        }
    }
    
    /**
     * sets the lb-enabled flag to the value of status for the given target
     * if status is false also sets the disable-timeout-in-minutes for the application
     * @param target server or cluster target
     * @param appName application name
     * @param timeout disable timeout in minutes
     * @param status enable status
     */
    private void setApplicationStatus(String target, String appName,int timeout, boolean status){
        
        //disable timeout is required for disable operation
        if (timeout < 0 && !status) {
            String msg = resBundle.getString("InvalidNumber");
            throw new IllegalArgumentException(msg);
        }
        
        try {
            DeployedItemRefConfig dRef = null;
            // disables cluster if target is a cluster
            if (isCluster(target)) {
                //get the clusterConfig for this cluster
                Map<String,ClusterConfig> clusterConfigs = mDomainConfig.getClustersConfig().getClusterConfigMap();
                ClusterConfig clusterConfig = clusterConfigs.get(target);
                //get the deployed item object corresponding to the given appName
                dRef = clusterConfig.getDeployedItemRefConfigMap().get(appName);
            } else { // target is a server
                //get the standalone serverConfig
                Map<String,StandaloneServerConfig> ssConfigMap =
                        mDomainConfig.getServersConfig().getStandaloneServerConfigMap();
                StandaloneServerConfig ssc = ssConfigMap.get(target);
                if (ssc == null) {
                    //get the clustered server config
                    ClusteredServerConfig s = mDomainConfig.getServersConfig().getClusteredServerConfigMap().get(target);
                    //get the deployed item object corresponding to the given appName
                    dRef = s.getDeployedItemRefConfigMap().get(appName);
                }else{
                    //get the deployed item object corresponding to the given appName
                    dRef = ssc.getDeployedItemRefConfigMap().get(appName);
                }
            }
            if (dRef == null) {
                mLogger.log(Level.FINEST," server " + target +
                        " does not exist in any cluster in the domain");
                String msg = formatter.format(resBundle.getString("AppRefNotDefined"),
                        target);
                throw new MBeanException(new RuntimeException(msg));
            }
            int curTout = Integer.parseInt(
                    dRef.getDisableTimeoutInMinutes());
            //check if the app is already in the state desired
            boolean enabled = dRef.getLBEnabled();
            if(!status){
                if ((enabled == false) && (curTout == timeout)) {
                    String msg = resBundle.getString("AppDisabledOnServer"
                            );
                    throw new MBeanException(new Exception(msg));
                }
                //set the disable timeout
                dRef.setDisableTimeoutInMinutes( "" + timeout );
                //disable the app
                dRef.setLBEnabled(false);
                mLogger.log(Level.INFO,resBundle.getString(
                        "http_lb_admin.ApplicationDisabled"));
            }else{
                if (enabled == true) {
                    String msg = resBundle.getString("AppEnabledOnServer"
                            );
                    throw new MBeanException(new Exception(msg));
                }
                //enable the app
                dRef.setLBEnabled(true);
                mLogger.log(Level.INFO,resBundle.getString(
                        "http_lb_admin.ApplicationEnabled"));
            }
        } catch(Exception ce) {
            throw new RuntimeException(ce);
        }
        
    }
    
    /**
     * returns the server-ref elements corresponding to the target
     * by searching all the cluster-ref elements.
     * @param target target server or cluster
     * @return ServerRefConfig the class that refers to the server-ref obtained by searching through all the clusters
     * @throws javax.management.MBeanException wrapper for all the exceptions
     */
    private ServerRefConfig getServerRefConfigFromCluster(String target)
    throws MBeanException {
        // check if this server is part of cluster, then
        // turn on lb-enable flag in the cluster.
        //get all the lb-configs
        Map<String,LBConfig> lbConfigs = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        //iterate through all the lb-configs
        for(LBConfig lbConfig : lbConfigs.values()){
            //get the cluster-refs in this lb-config
            Map<String,ClusterRefConfig> clusterRefs = lbConfig.getClusterRefConfigMap();
            //iterate through all the cluster-refs
            for(ClusterRefConfig clusterRef :clusterRefs.values()){
                
                //get the cluster name
                String clusterName = clusterRef.getReferencedClusterName();
                
                //get the clusterConfig for this clustername
                Map<String,ClusterConfig> clusterConfigs =  mDomainConfig.getClustersConfig().getClusterConfigMap();
                ClusterConfig config = clusterConfigs.get(clusterName);
                
                //get all the server-refs for this cluster
                Map<String,ServerRefConfig> serverRefConfigs = config.getServerRefConfigMap();
                
                //iterate through the server-refs
                for(ServerRefConfig serverRefConfig : serverRefConfigs.values() ){
                    
                    //if this server-ref matches the given target , return it
                    if(serverRefConfig.getName().equals(target)){
                        return serverRefConfig;
                    }
                    
                }
            }
        }
        return null;
    }

    /**
     * Gets the Maps of LoadBalancerServerMonitor for specified load balancer and
     * specified target(s).
     *
     * @param targetLoadBalancer    Load Balancer for which Monitors are to be
     *                              returned
     * @param target                Target cluster name. This is used if
     *                              allTargets (next param) is false.
     * @param allTargets            list Monitors for all targets.
     *
     * @return Map of LoadBalancerServerMonitors and their names
    public Map<String, LoadBalancerServerMonitor> getInstanceMonitors(
        final String targetLoadBalancer, final String target, boolean allTargets) {

        Map<String,LoadBalancerServerMonitor> 
        loadBalancerServerMonitorMap = new HashMap<String,LoadBalancerServerMonitor>();
    
        MonitoringRoot mRoot = mDomainRoot.getMonitoringRoot();
        Map<String, LoadBalancerMonitor> loadBalancerMonitorMap =
                mRoot.getLoadBalancerMonitorMap();
        LoadBalancerMonitor loadBalancerMonitor =
                loadBalancerMonitorMap.get(targetLoadBalancer);
        
        Map<String, LoadBalancerClusterMonitor> loadBalancerClusterMonitorMap =
                loadBalancerMonitor.getLoadBalancerClusterMonitorMap();
        if (!allTargets) {
            LoadBalancerClusterMonitor loadBalancerClusterMonitor =
                loadBalancerClusterMonitorMap.get(target);
            populateLoadBalancerServerMonitorMap(target,
                loadBalancerServerMonitorMap, loadBalancerClusterMonitor);
        } else {
            for (String clusterName : loadBalancerClusterMonitorMap.keySet()) {
                LoadBalancerClusterMonitor loadBalancerClusterMonitor =
                    loadBalancerClusterMonitorMap.get(clusterName);
                populateLoadBalancerServerMonitorMap(target,
                    loadBalancerServerMonitorMap, loadBalancerClusterMonitor);
            }
        }
        return loadBalancerServerMonitorMap;
    }
     */

    /**
     * Returns the stats for an instance. If the instance is being load 
     * balanced by multiple load balancers the a map of stats keyed by 
     * load balancer name is returned
     *
     * @param serverName    instance name 
     *
     * @return Map of LoadBalancerServerMonitor keyed by load balancer name
    public Map<String, LoadBalancerServerMonitor> 
        getInstanceAggregateStats(String serverName) {
        
        Collection<LoadBalancerConfig> loadBalancers = 
            getLoadBalancers(serverName, false).values();

        Map<String,LoadBalancerServerMonitor> 
            loadBalancerServerMonitorMap = new HashMap<String,LoadBalancerServerMonitor>();

        MonitoringRoot mRoot = mDomainRoot.getMonitoringRoot();
        Map<String, LoadBalancerMonitor> loadBalancerMonitorMap =
            mRoot.getLoadBalancerMonitorMap();
        
        for (LoadBalancerConfig loadBalancer : loadBalancers) {
            String targetLoadBalancer = loadBalancer.getName();
            LoadBalancerMonitor loadBalancerMonitor =
                loadBalancerMonitorMap.get(targetLoadBalancer);
        
            Map<String, LoadBalancerClusterMonitor> loadBalancerClusterMonitorMap =
                loadBalancerMonitor.getLoadBalancerClusterMonitorMap();
            for (String clusterName : loadBalancerClusterMonitorMap.keySet()) {
                LoadBalancerClusterMonitor loadBalancerClusterMonitor =
                    loadBalancerClusterMonitorMap.get(clusterName);
                LoadBalancerServerMonitor loadBalancerServerMonitor =
                    loadBalancerClusterMonitor.getLoadBalancerServerMonitorMap()
                    .get(serverName);
                loadBalancerServerMonitorMap.put(
                    targetLoadBalancer, loadBalancerServerMonitor);
            }
        }
        return loadBalancerServerMonitorMap;
    }
     */
    
    /**
     * Returns the load balancers loadbalancing a target : 
     * standalone instance, clustered instance or a cluster
     *
     * @param targetName    standalone instance name or
     *                      clustered instance name or a cluster name
     * @param isCluster     whether the targetName is a cluster or instance name
     *
     * @return Map of LoadBalancerConfig keyed by load balancer name
     */
    public Map<String,LoadBalancerConfig> getLoadBalancers(
        String targetName, boolean isCluster) {
        Map<String, LBConfig> lbConfigMap = fetchLBConfigs(targetName, isCluster);
        return fetchLoadBalancerConfigs(lbConfigMap);
    }
    
    private Map<String, LBConfig> fetchLBConfigs(
        String targetName, boolean isCluster) {

        Map<String,LBConfig> result = new HashMap<String,LBConfig>();
        Map<String,LBConfig> lbConfigMap = mDomainConfig.getLBConfigsConfig().getLBConfigMap();
        
        if (isCluster) {
            for (String lbConfigName : lbConfigMap.keySet()) {
                LBConfig lbConfig = lbConfigMap.get(lbConfigName);
                Map<String,ClusterRefConfig> lbClusterRefConfigMap =
                    lbConfig.getClusterRefConfigMap();
                for (String clusterRef : lbClusterRefConfigMap.keySet()) {
                    if (clusterRef.equals(targetName)) {
                        result.put(lbConfigName, lbConfig);
                        break;
                    }
                }
            }
        } else if (isStandaloneServer(targetName)) {
         /*its a serverName which means you have to find LBConfigs containing 
         1. standalone server references with the same name 
         2. clustered server references with the same name */
            for (String lbConfigName : lbConfigMap.keySet()) {
                LBConfig lbConfig = lbConfigMap.get(lbConfigName);
                Map<String,ServerRefConfig> lbServerRefConfigMap =
                    lbConfig.getServerRefConfigMap();
                for (String serverRef : lbServerRefConfigMap.keySet()) {
                    if (serverRef.equals(targetName)) {
                        result.put(lbConfigName, lbConfig);
                        break;
                    }
                }
            }
        } else {//we assume that its a clustered instance name
            for (String lbConfigName : lbConfigMap.keySet()) {
                LBConfig lbConfig = lbConfigMap.get(lbConfigName);
                Map<String,ClusterRefConfig> lbClusterRefConfigMap =
                    lbConfig.getClusterRefConfigMap();
                Map<String,ClusterConfig> clusterConfigMap =
                    mDomainConfig.getClustersConfig().getClusterConfigMap();
                Map<String,ClusterConfig> relevantClusterConfigMap = new HashMap<String,ClusterConfig>();
                for (String clusterRef : lbClusterRefConfigMap.keySet()) 
                    relevantClusterConfigMap.put(clusterRef, 
                        clusterConfigMap.get(clusterRef));
                //so now we have the right set of <cluster> elements
                for (String clusterName : relevantClusterConfigMap.keySet()) {
                    ClusterConfig clusterConfig = 
                        relevantClusterConfigMap.get(clusterName);
                    Map<String,ServerRefConfig> clusteredServerRefConfigMap =
                        clusterConfig.getServerRefConfigMap();
                    for (String serverRef : clusteredServerRefConfigMap.keySet()) {
                        if (serverRef.equals(targetName)) {
                            result.put(lbConfigName, lbConfig);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }    
    
    private Map<String,LoadBalancerConfig> fetchLoadBalancerConfigs(
        Map<String, LBConfig> lbConfigMap) {

        Map<String,LoadBalancerConfig> relevantLoadBalancerConfigMap = new HashMap<String,LoadBalancerConfig>();

        for (String lbConfigName : lbConfigMap.keySet()) {
            //collect all load-balancer elements which refer to this lb-config
            Map<String,LoadBalancerConfig> allLoadBalancerConfigMap =
                 mDomainRoot.getDomainConfig().getLoadBalancersConfig().getLoadBalancerConfigMap();

            for (String loadBalancerName : allLoadBalancerConfigMap.keySet()) {
                LoadBalancerConfig loadBalancerConfig = 
                    allLoadBalancerConfigMap.get(loadBalancerName);
                if (loadBalancerConfig.getLbConfigName().equals(lbConfigName))
                    relevantLoadBalancerConfigMap.put(
                        loadBalancerName, loadBalancerConfig);
            }
        }
        return relevantLoadBalancerConfigMap;
    }  
    
    /*
    private void populateLoadBalancerServerMonitorMap(String target,
        Map<String, LoadBalancerServerMonitor> loadBalancerServerMonitorMap,
        LoadBalancerClusterMonitor loadBalancerClusterMonitor) {
        
        Map<String, LoadBalancerServerMonitor> tmpLoadBalancerServerMonitorMap =
                loadBalancerClusterMonitor.getLoadBalancerServerMonitorMap();
        for (String serverName : tmpLoadBalancerServerMonitorMap.keySet()) {
            LoadBalancerServerMonitor loadBalancerServerMonitor =
                    tmpLoadBalancerServerMonitorMap.get(serverName);
            if (isStandaloneServer(serverName))
                loadBalancerServerMonitorMap.put(
                        serverName, loadBalancerServerMonitor);
            else loadBalancerServerMonitorMap.put(
                    loadBalancerClusterMonitor.getName() + "." + target,
                    loadBalancerServerMonitor);
        }
    }
    */
    
    private LoadBalancer createLoadBalancer(final String configName) {
        mDomainConfig.getLoadBalancersConfig().createLoadBalancerConfig(
            configName+LB_SUFFIX, configName, false, null);
        Map<String,LoadBalancer> lbs = mDomainRoot.getLoadBalancerMap();
        LoadBalancer lb = lbs.get(configName+LB_SUFFIX);
        return lb;
    }
    
    /**
     * creates a map of the parameters
     */
    private Map<String,String> getParams(Map<String,String> options) {
        
        Map<String,String> params = new HashMap<String,String>();
        params.put(LBConfigKeys.HTTPS_ROUTING_KEY,options.get(HTTPS_ROUTING));
        params.put(LBConfigKeys.MONITORING_ENABLED_KEY,options.get(MONITOR));
        params.put(LBConfigKeys.RELOAD_POLL_INTERVAL_IN_SECONDS_KEY, options.get(RELOAD_INTERVAL));
        params.put(LBConfigKeys.RESPONSE_TIMEOUT_IN_SECONDS_KEY, options.get(RESPONSE_TIMEOUT));
        params.put(LBConfigKeys.ROUTE_COOKIE_ENABLED_KEY, options.get(ROUTE_COOKIE));
        return params;
    }
    
    /**
     * writes the String lbxml to the file given by filePath
     */
    private String writeToFile(final String lbxml, final String filePath)  {
        FileOutputStream fo = null;
        try{
            fo = new FileOutputStream(filePath);
            fo.write(lbxml.getBytes());
        }catch(IOException ioe){
            ioe.printStackTrace();
        }finally{
            try{
                fo.close();
            }catch(IOException e){}
        }
        
        return filePath; 
    }
    
    // PRIVATE VARIABLES
    private final DomainConfig mDomainConfig;
    
    private final DomainRoot mDomainRoot;
    
    private static final String LB_CONFIG_SUFFIX = "_LB_CONFIG";
    
    private static final String LB_SUFFIX = "-lb-temp";
    
    private final Logger mLogger;
    
    private final ResourceBundle resBundle;
    
    private Formatter formatter;
    
    public static final String RESPONSE_TIMEOUT = "responsetimeout";
    public static final String HTTPS_ROUTING = "httpsrouting";
    public static final String RELOAD_INTERVAL = "reloadinterval";
    public static final String MONITOR = "monitor";
    public static final String ROUTE_COOKIE = "routecookie";
    public static final String HEALTH_CHECKER_URL = "healthcheckerurl";
    public static final String HEALTH_CHECKER_TIMEOUT = "healthcheckertimeout";
    public static final String HEALTH_CHECKER_INTERVAL = "healthcheckerinterval";
    public static final String TARGET = "target";
    public static final String CONFIG = "config";
    public static final String LB_POLICY = "lbpolicy";
    public static final String LB_POLICY_MODULE = "lbpolicymodule";
    public static final String AUTO_APPLY_ENABLED = "autoapplyenabled";
    public static final String LB_ENABLE_ALL_INSTANCES = "lbenableallinstances";
    public static final String LB_ENABLE_ALL_APPLICATIONS = "lbenableallapplications";
    public static final String LB_WEIGHT = "lbweight";
}



