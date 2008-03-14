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
 * LoadBalancerHandlers.java
 *
 * Created on December 16, 2006, 8:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.text.DateFormat;

import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.TargetUtil;

import com.sun.appserv.management.ext.lb.LoadBalancer;

import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.HealthCheckerConfig;
import com.sun.appserv.management.config.HealthCheckerConfigCR;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.appserv.management.config.LBConfig;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ClusterRefConfig;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.appserv.management.config.PropertiesAccess;

import com.sun.webui.jsf.component.Field;

/**
 *
 * @author anilam
 */


public class LoadBalancerHandlers {
    
    /** Creates a new instance of LoadBalancerHandlers */
    public LoadBalancerHandlers() {
    }
    
    
    /**
     *	<p> This handler returns the list of Load Balancers for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLoadBalancersList",
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getLoadBalancersList(HandlerContext handlerCtx){
        
        Map <String, LoadBalancerConfig> loadBalancerConfigMap = AMXUtil.getDomainConfig().getLoadBalancerConfigMap();
        List result = new ArrayList();
        for(String key : loadBalancerConfigMap.keySet()){
            try{
                String status = GuiUtil.getMessage("loadBalancer.unknown");
                HashMap oneRow = new HashMap();
                oneRow.put("name", key);
                LoadBalancer lb = AMXUtil.getDomainRoot().getLoadBalancerMap().get(key);
                if( lb != null){
                    status = GuiUtil.getMessage(lb.isApplyChangeRequired()? "loadBalancer.needApply" : "loadBalancer.upToDate");
                }
                oneRow.put("status", status);
                oneRow.put("selected", false);
                result.add(oneRow);
            }catch (Exception ex){
                ex.printStackTrace();
                continue;
            }
        }
        handlerCtx.setOutputValue("result", result);
    }
    
    /**
     *	<p> This handler returns the list of Load Balancers for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLbTargets",
        input={
            @HandlerInput(name="lbName", type=String.class)},
        output={
            @HandlerOutput(name="isCluster", type=Boolean.class),
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getLbTargets(HandlerContext handlerCtx){
        List result = new ArrayList();
        try{
           String lbName = (String)handlerCtx.getInputValue("lbName");
           LoadBalancerConfig loadBalancer = AMXUtil.getDomainConfig().getLoadBalancerConfigMap().get(lbName);
           String lbConfigName = loadBalancer.getLbConfigName();
           if (GuiUtil.isEmpty(lbConfigName)){
               handlerCtx.setOutputValue("result", result);
               return;
           }
           
           LBConfig lbConfig = AMXUtil.getDomainConfig().getLBConfigMap().get(lbConfigName);
           if (lbConfig == null){
               handlerCtx.setOutputValue("result", result);
               return;
           }
           
           Map<String, ServerRefConfig> serverRefMap = lbConfig.getServerRefConfigMap();
           Set<String> targets = serverRefMap.keySet();
           Boolean isCluster = false;
           if (serverRefMap.isEmpty()){
               Map<String, ClusterRefConfig> clusterRefMap = lbConfig.getClusterRefConfigMap();
               if (clusterRefMap.isEmpty()){
                   handlerCtx.setOutputValue("isCluster",isCluster);
                   handlerCtx.setOutputValue("result", result);
                    return;
               }else{
                   isCluster = true;
                   targets = clusterRefMap.keySet();
               }
           }
           handlerCtx.setOutputValue("isCluster",isCluster);
            for(String key : targets){
                HashMap oneRow = new HashMap();
                oneRow.put("name", key);
                if (isCluster){
                    oneRow.put("url", "/cluster/clusterGeneral.jsf?clusterName="+key);
                }else{
                    oneRow.put("url", "/standalone/standaloneInstanceGeneral.jsf?instanceName="+key);
                }
                result.add(oneRow);
            }
        }catch(Exception ex){
            ex.printStackTrace();
            
        }
        handlerCtx.setOutputValue("result", result);
    }
    
    /**
     *	<p> This handler deletes selected Load Balancer and its associated LBConfig.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteSelectedLoadBalancers",
        input={
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
     )
     public static void deleteSelectedLoadBalancers(HandlerContext handlerCtx){
        List<Map> selectedRows= (List) handlerCtx.getInputValue("selectedRows");
        try{
            for(Map oneRow : selectedRows){
                removeLoadbalancer((String)oneRow.get("name"));
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    
    /**
     * Deletes a load balancer element ( and the necessary config, if nobody
     * else is using this config)
     */
    private static void removeLoadbalancer(String loadbalancerName) {

        LoadBalancerConfig loadBalancerConfig = AMXUtil.getDomainConfig().getLoadBalancerConfigMap().get(loadbalancerName);
        if(loadBalancerConfig == null){
            return;
        }
        String lbConfigName = loadBalancerConfig .getLbConfigName();
        
        // now remove load-balancer element
        AMXUtil.getDomainConfig().removeLoadBalancerConfig(loadbalancerName);

        Map<String, LoadBalancerConfig> lbMap =  AMXUtil.getDomainConfig().getLoadBalancerConfigMap();
        if ( lbMap == null || lbMap.isEmpty()) {
            AMXUtil.getDomainConfig().removeLBConfig(lbConfigName);
            return;
        }
        
        // now remove lb-config, if nobody is using it
        for(LoadBalancerConfig lbConfig : lbMap.values()){
            if ( lbConfig.getLbConfigName().equals(lbConfigName)) {
                // this load-balancer element is still using it, just return
                // else continue to check other elements
                return;
            }
        }
        // no load-balancer element is using this lb-config, remove it
        AMXUtil.getDomainConfig().removeLBConfig(lbConfigName);
    }
    
    
                            
    /**
     * This handler creats the LoadBalanceer and the LBConfig
     */
    @Handler(id="createLoadBalancer",
        input={
        @HandlerInput(name="lbName", type=String.class, required=true),
        @HandlerInput(name="autoApply", type=Boolean.class),
        @HandlerInput(name="deviceHost", type=String.class, required=true),
        @HandlerInput(name="devicePort", type=String.class, required=true),
        @HandlerInput(name="proxyHost", type=String.class),
        @HandlerInput(name="proxyPort", type=String.class),
        @HandlerInput(name="enableAllInstances", type=Boolean.class),
        @HandlerInput(name="enableAllApps", type=Boolean.class),
        @HandlerInput(name="targets", type=String[].class)})
    public static void createLoadBalancer(HandlerContext handlerCtx) {
        String lbName = (String)handlerCtx.getInputValue("lbName");
        String[] targets = (String[])handlerCtx.getInputValue("targets");
        Boolean autoApply = (Boolean)handlerCtx.getInputValue("autoApply");
        if (autoApply == null)
            autoApply = false;
        Boolean enableAllInstances = (Boolean)handlerCtx.getInputValue("enableAllInstances");
        if (enableAllInstances == null)
            enableAllInstances = false;
        Boolean enableAllApps = (Boolean)handlerCtx.getInputValue("enableAllApps");
        if (enableAllApps == null)
            enableAllApps = false;
        
        
        String deviceHost = (String)handlerCtx.getInputValue("deviceHost");
        String devicePort = (String)handlerCtx.getInputValue("devicePort");
        String proxyHost = (String)handlerCtx.getInputValue("proxyHost");
        String proxyPort = (String)handlerCtx.getInputValue("proxyPort");
        try{
            LoadBalancerConfig loadBalancerConfig = AMXUtil.getLBConfigHelper().createLoadbalancer(lbName, autoApply.booleanValue(), targets, null);
            loadBalancerConfig.createProperty(LoadBalancerConfig.DEVICE_HOST_PROPERTY, deviceHost);
            loadBalancerConfig.createProperty(LoadBalancerConfig.DEVICE_ADMIN_PORT_PROPERTY, devicePort);
            if(!GuiUtil.isEmpty(proxyHost))
                loadBalancerConfig.createProperty(loadBalancerConfig.SSL_PROXY_HOST_PROPERTY, proxyHost);
            if(!GuiUtil.isEmpty(proxyPort))
                loadBalancerConfig.createProperty(LoadBalancerConfig.SSL_PROXY_PORT_PROPERTY, proxyPort);
            if (targets != null){
                if (enableAllInstances == true){
                    for(int i=0; i< targets.length; i++){
                        AMXUtil.getLBConfigHelper().enableServer(targets[i], true);
                    }
                }
                if (enableAllApps == true){
                    for(int i=0; i< targets.length; i++){
                        AMXUtil.getLBConfigHelper().enableAllApplications(targets[i]);
                    }
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
      
    
    /**
     * This handler returns the information about the load balancer
     */
    @Handler(id="getLoadBalancerInfo",
        input={
            @HandlerInput(name="lbName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="autoApply", type=Boolean.class),
            @HandlerOutput(name="mbean", type=PropertiesAccess.class)}
     )
    public static void getLoadBalancerInfo(HandlerContext handlerCtx){
        String lbName = (String)handlerCtx.getInputValue("lbName");
        LoadBalancerConfig loadBalancerConfig = AMXUtil.getDomainConfig().getLoadBalancerConfigMap().get(lbName);
        if (loadBalancerConfig==null){
           handlerCtx.setOutputValue("applyEnable", false);
           GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchLB", new Object[]{lbName}));
           return;
        }
        handlerCtx.setOutputValue("autoApply", loadBalancerConfig.getAutoApplyEnabled());
        handlerCtx.setOutputValue("mbean", loadBalancerConfig);
    }
    
     /**
     * This handler returns the information about the load balancer
     */
    @Handler(id="saveLoadBalancer",
        input={
            @HandlerInput(name="lbName", type=String.class, required=true),
            @HandlerInput(name="autoApply", type=Boolean.class),
            @HandlerInput(name="AddProps", type=Map.class),
            @HandlerInput(name="RemoveProps", type=ArrayList.class)} )
    public static void saveLoadBalancer(HandlerContext handlerCtx){
        try{
            String lbName = (String)handlerCtx.getInputValue("lbName");
            Boolean autoApply = (Boolean)handlerCtx.getInputValue("autoApply");
            LoadBalancerConfig loadBalancerConfig = AMXUtil.getDomainConfig().getLoadBalancerConfigMap().get(lbName);
            if (loadBalancerConfig==null){
               GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.loadBalancer.NoSuchLB", new Object[]{lbName}));
               return;
            }
            loadBalancerConfig.setAutoApplyEnabled( (autoApply==null)? false : autoApply);
            AMXUtil.editProperties(handlerCtx, loadBalancerConfig);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    
    /**
     * Test the connection of load balancer
     */
    @Handler(id="testLoadBalancerConnection",
        input={
            @HandlerInput(name="lbName", type=String.class, required=true)})
    public static void testLoadBalancerConnection(HandlerContext handlerCtx){
        String lbName = (String)handlerCtx.getInputValue("lbName");
        Map lbMap = AMXUtil.getDomainRoot().getLoadBalancerMap();
        if ( lbMap == null){
                System.out.println("testLoadBalancerConnection(): AMX getLoadBalancerMap() returns null");
                GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.loadBalancer.NoSuchLB", new Object[]{lbName}), null);
                return;
            }
        LoadBalancer loadBalancer = (LoadBalancer) lbMap.get(lbName);
        try {
        if (loadBalancer == null){
            GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.loadBalancer.NoSuchLB", new Object[]{lbName}), null);
            System.out.println("testLoadBalancerConnection(): lbMap.get(" + lbName+ ")returns null");
            System.out.println("loadbalancer map returned from getLoadBalancerMap() : " + lbMap);
        }else{
            Boolean ok = loadBalancer.testConnection();
            if (ok)
                GuiUtil.prepareAlert(handlerCtx, "success", GuiUtil.getMessage("msg.loadBalancer.TestConnectionSuccess"), null);
            else
                GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.loadBalancer.TestConnectionFailed"), null);
        }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     * This handler returns the information about the LB Config
     */
    @Handler(id="getLBConfigInfo",
        input={
            @HandlerInput(name="lbName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="lbConfigName", type=String.class),
            @HandlerOutput(name="advance", type=Map.class),
            @HandlerOutput(name="props", type=Map.class)}
     )
     public static void getLBConfigInfo(HandlerContext handlerCtx){
        String lbName = (String)handlerCtx.getInputValue("lbName");
        LBConfig lbConfig = getLBConfigOfLoadBalancer(lbName);
        if (lbConfig == null){
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchLB", new Object[]{lbName}));
            return;
        }
        String lbConfigName = lbConfig.getName();
        Map advance = new HashMap();
        handlerCtx.setOutputValue("lbConfigName", lbConfigName);
        advance.put("responseTimeoutInSeconds", lbConfig.getResponseTimeoutInSeconds());
        advance.put("reloadPollIntervalInSeconds", lbConfig.getReloadPollIntervalInSeconds());
        advance.put("httpsRouting",lbConfig.getHttpsRouting());
        advance.put("monitoringEnabled",lbConfig.getMonitoringEnabled());
        advance.put("routeCookieEnabled", lbConfig.getRouteCookieEnabled());
        handlerCtx.setOutputValue("advance", advance);
        
        Map<String, String> props = lbConfig.getProperties();
        handlerCtx.setOutputValue("props", props);
    }
    
    
    /**
     * This handler returns the default information about the LB Config
     */
    @Handler(id="getLBConfigDefault",
        input={
            @HandlerInput(name="lbConfigName", type=String.class)},
        output={
            @HandlerOutput(name="advance", type=Map.class)})
            
     public static void getLBConfigDefault(HandlerContext handlerCtx){
        Map advance = new HashMap();
        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(LBConfig.J2EE_TYPE);
        advance.put("responseTimeoutInSeconds", defaultMap.get("response-timeout-in-seconds"));
        advance.put("reloadPollIntervalInSeconds",defaultMap.get("reload-poll-interval-in-seconds"));
        advance.put("httpsRouting", Boolean.parseBoolean((String) defaultMap.get("https-routing")));
        advance.put("monitoringEnabled", Boolean.parseBoolean((String) defaultMap.get("monitoring-enabled")));
        advance.put("routeCookieEnabled", Boolean.parseBoolean((String) defaultMap.get("route-cookie-enabled")));
        handlerCtx.setOutputValue("advance", advance);
    }
    
    /**
     * This handler saves the information about the LB Config
     */
    @Handler(id="saveLBConfig",
    input={
        @HandlerInput(name="lbConfigName", type=String.class, required=true),
        @HandlerInput(name="advance", type=Map.class),
        @HandlerInput(name="AddProps",    type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class)}
    )
    public static void saveLBConfig(HandlerContext handlerCtx){
        String lbConfigName = (String)handlerCtx.getInputValue("lbConfigName");
        Map advance = (Map) handlerCtx.getInputValue("advance");
        try{
            LBConfig lbConfig = AMXUtil.getDomainConfig().getLBConfigMap().get(lbConfigName);
            if (lbConfig == null){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("loadBalancer.noSuchLBError"));
                return;
            }
            lbConfig.setResponseTimeoutInSeconds((String) advance.get("responseTimeoutInSeconds"));
            lbConfig.setReloadPollIntervalInSeconds((String)advance.get("reloadPollIntervalInSeconds"));

            Boolean temp = (Boolean) advance.get("httpsRouting");
            lbConfig.setHttpsRouting( (temp == null) ? false : temp);

            temp = (Boolean) advance.get("monitoringEnabled");
            lbConfig.setMonitoringEnabled( (temp == null) ? false : temp);

            temp = (Boolean)advance.get("routeCookieEnabled");
            lbConfig.setRouteCookieEnabled( (temp==null)? false: temp);

            AMXUtil.editProperties(handlerCtx, lbConfig);
        
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

     /**
     *	<p> This handler returns the list of targets for populating the target table.
     *  <p> Input  value: "appName" -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "appType" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLoadBalancerTargetTableList",
        input={
            @HandlerInput(name="lbName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="isCluster", type=Boolean.class),
            @HandlerOutput(name="isInstance", type=Boolean.class),
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getLoadBalancerTargetTableList(HandlerContext handlerCtx){
        
        String lbName = (String)handlerCtx.getInputValue("lbName");
        LBConfig lbConfig = getLBConfigOfLoadBalancer(lbName);
        if (lbConfig == null){
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchLB", new Object[]{lbName}));
            return;
        }
        String[] targets = AMXUtil.getLBConfigHelper().listTargets(lbName);
        List result = new ArrayList();
        boolean isCluster = true;
        if(targets.length > 0)
            isCluster = TargetUtil.isCluster(targets[0]);  //all targets will either be cluster or instance.
        
        for(int i=0; i<targets.length; i++ ){
            HashMap oneRow = new HashMap();
            String targetName = targets[i];
            oneRow.put("targetName", targetName);
            oneRow.put("lbConfigName", lbConfig.getName());
            oneRow.put("selected", false);
            oneRow.put("appLink", ApplicationHandlers.getNumberLBAppsByTarget(targetName));
            if(isCluster){
                oneRow.put("policy", lbConfig.getClusterRefConfigMap().get(targetName).getLBPolicy());
                oneRow.put("targetLink", "/cluster/clusterGeneral.jsf?clusterName="+targetName);
                oneRow.put("clusterInstanceLink", TargetUtil.getNumberLBInstancesByTarget(targetName));
            }else{
                oneRow.put("lbEnabled", lbConfig.getServerRefConfigMap().get(targetName).getLBEnabled());
                
                oneRow.put("responseTimeoutInMinutes", ""+lbConfig.getServerRefConfigMap().get(targetName).getDisableTimeoutInMinutes());
                oneRow.put("targetLink", "/standalone/standaloneInstanceGeneral.jsf?instanceName="+targetName);
            }
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("isCluster", isCluster);
        handlerCtx.setOutputValue("isInstance", !isCluster);
    }
    
           
     /**
     *	<p> This handler creates references for the given load balancer 
     *
     *  <p> Input value: "name" -- Type: <code>String</code>/</p>
     *  <p> Input value: "targets" -- Type: <code>String[]</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="createLBTargets",
        input={
        @HandlerInput(name="lbName", type=String.class, required=true),
        @HandlerInput(name="targets", type=String[].class, required=true )})
        
    public static void createLBTargets(HandlerContext handlerCtx) {
        String lbName = (String)handlerCtx.getInputValue("lbName");
        String[] targets = (String[])handlerCtx.getInputValue("targets");
        List<String> selTargets = Arrays.asList(targets);
        boolean isCluster = false;
        
        String[] targetsArray = AMXUtil.getLBConfigHelper().listTargets(lbName); 
        List<String> associatedTargets = Arrays.asList(targetsArray);
        
        LoadBalancerConfig lb = AMXUtil.getDomainConfig().getLoadBalancerConfigMap().get(lbName);
        String lbConfigName = lb.getLbConfigName();
        LBConfig lbConfig = AMXUtil.getDomainConfig().getLBConfigMap().get(lbConfigName);
        
        if (selTargets.size()>0){
            isCluster = TargetUtil.isCluster(selTargets.get(0));
            for(String sel: selTargets){
                AMXConfig sc = null;
                if(isCluster)
                    sc = AMXUtil.getDomainConfig().getClusterConfigMap().get(sel);
                else
                    sc = AMXUtil.getDomainConfig().getStandaloneServerConfigMap().get(sel);
                if (sc == null){
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("loadBalancer.targetHelp"));
                    return;
                }
            }
        }
        
        try{
            //be sure to remove the old targets first. otherwise, if changing from all server to all cluster or visa versa 
            //will result in error.
            for(String targetName: associatedTargets) {
                if(!(selTargets.contains(targetName))) {
                    if (TargetUtil.isCluster(targetName))
                        lbConfig.removeClusterRefConfig(targetName);
                    else
                        lbConfig.removeServerRefConfig(targetName);
                }
            }
             
            for(String targetName: selTargets) {
                if(!(associatedTargets.contains(targetName))) {
                    if (isCluster)
                        lbConfig.createClusterRefConfig(targetName, null);
                    else
                        lbConfig.createServerRefConfig(targetName, null);
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     * This handler returns the information about the LB Config
     */
    @Handler(id="getLBHealthAndPolicy",
        input={
            @HandlerInput(name="lbName", type=String.class, required=true),
            @HandlerInput(name="targetName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="lbConfigName", type=String.class),
            @HandlerOutput(name="advance", type=Map.class),
            @HandlerOutput(name="isCluster", type=Boolean.class),
            @HandlerOutput(name="isInstance", type=Boolean.class)}
     )
     public static void getLBHealthAndPolicy(HandlerContext handlerCtx){
        String lbName = (String)handlerCtx.getInputValue("lbName");
        String targetName = (String)handlerCtx.getInputValue("targetName");
        LBConfig lbConfig = getLBConfigOfLoadBalancer(lbName);
        if (lbConfig == null){
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchLB", new Object[]{lbName}));
            return;
        }
        String lbConfigName = lbConfig.getName();
        Map advance = new HashMap();
        Boolean isCluster = TargetUtil.isCluster(targetName);
        handlerCtx.setOutputValue("lbConfigName", lbConfigName);
        handlerCtx.setOutputValue("isCluster", isCluster);
        handlerCtx.setOutputValue("isInstance", !isCluster);
        HealthCheckerConfig hcc = null;
        if(isCluster){
            ClusterRefConfig cref = lbConfig.getClusterRefConfigMap().get(targetName);
            advance.put("lBPolicy", ""+cref.getLBPolicy());
            String tmp = cref.getLBPolicyModule();
            advance.put("lBPolicyModule", (tmp == null) ? "" : tmp);
            hcc = cref.getHealthCheckerConfig();
        }else{
            ServerRefConfig sref = lbConfig.getServerRefConfigMap().get(targetName);
            advance.put("lbEnabled", sref.getLBEnabled());
            advance.put("enabled", sref.getEnabled());
            advance.put("disableTimeoutInMinutes", ""+sref.getDisableTimeoutInMinutes());
            hcc = sref.getHealthCheckerConfig();
        }
        
        advance.put("url", (hcc == null) ? "" : hcc.getURL());
        advance.put("intervalInSeconds", (hcc == null) ? "" : hcc.getIntervalInSeconds());
        advance.put("timeoutInSeconds", (hcc == null) ? "" : hcc.getTimeoutInSeconds());
        handlerCtx.setOutputValue("advance", advance);
    }
    

     /**
     * This handler returns the default information about the LB Config
     */
    @Handler(id="getLBHealthDefault",
        input={
            @HandlerInput(name="lbConfigName", type=String.class)},
        output={
            @HandlerOutput(name="advance", type=Map.class)})
            
     public static void getLBHealthDefault(HandlerContext handlerCtx){
//        Object[] params = {"health-checker", null};
//        String[] types = { "java.lang.String", "[Ljava.lang.String;"};
//        Object attrs = JMXUtil.invoke("com.sun.appserv:category=config,type=domain", "getDefaultAttributesValues",
//                params, types);
        Map advance = new HashMap();
        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(HealthCheckerConfig.J2EE_TYPE);
        Map refMap =  AMXUtil.getDomainConfig().getDefaultAttributeValues(ServerRefConfig.J2EE_TYPE);
        advance.put("lbEnabled", refMap.get("lb-enabled"));
        advance.put("enabled", refMap.get("enabled"));
        advance.put("disableTimeoutInMinutes", refMap.get("disable-timeout-in-minutes"));
        
        advance.put("url", defaultMap.get("url"));
        advance.put("intervalInSeconds",defaultMap.get("interval-in-seconds"));
        advance.put("timeoutInSeconds", defaultMap.get("timeout-in-seconds"));
        handlerCtx.setOutputValue("advance", advance);
    }
    
    /**
     * This handler saves the information about the LB Config
     */
    @Handler(id="saveLBHealthAndPolicy",
    input={
        @HandlerInput(name="lbConfigName", type=String.class, required=true),
        @HandlerInput(name="targetName", type=String.class, required=true),
        @HandlerInput(name="advance", type=Map.class)}
    )
    public static void saveLBHealthAndPolicy(HandlerContext handlerCtx){
        String lbConfigName = (String)handlerCtx.getInputValue("lbConfigName");
        String targetName = (String)handlerCtx.getInputValue("targetName");
        Boolean isCluster = TargetUtil.isCluster(targetName);
        Map advance = (Map) handlerCtx.getInputValue("advance");
        try{
            LBConfig lbConfig = AMXUtil.getDomainConfig().getLBConfigMap().get(lbConfigName);
            if (lbConfig == null){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("loadBalancer.noSuchLBError"));
                return;
            }
            HealthCheckerConfig hcc = null;
            HealthCheckerConfigCR ref = null;
            if(isCluster){
                ClusterRefConfig cref = lbConfig.getClusterRefConfigMap().get(targetName);
                String policy = (String) advance.get("lBPolicy");
                cref.setLBPolicy(policy);
                if ("user-defined".equals(policy))
                    cref.setLBPolicyModule( "" + advance.get("lBPolicyModule"));
                else
                    cref.setLBPolicyModule("");
                hcc = cref.getHealthCheckerConfig();
                ref = cref;
            }else{
                ServerRefConfig sref = lbConfig.getServerRefConfigMap().get(targetName);
                Boolean temp = (Boolean) advance.get("lbEnabled");
                sref.setLBEnabled( (temp == null) ? false : temp);
                sref.setDisableTimeoutInMinutes( Integer.parseInt(""+advance.get("disableTimeoutInMinutes")));
                hcc = sref.getHealthCheckerConfig();
                ref = sref;
            }
            
            String url = (String) advance.get("url");
            String intervalInSeconds = (String) advance.get("intervalInSeconds");
            String timeoutInSeconds = (String) advance.get("timeoutInSeconds");
            
            if (GuiUtil.isEmpty(url) && GuiUtil.isEmpty(intervalInSeconds) && GuiUtil.isEmpty(timeoutInSeconds))
                return;
            
            if (url == null)  url = "";
            if (intervalInSeconds == null)  intervalInSeconds = "";
            if (timeoutInSeconds == null)  timeoutInSeconds = "";
            
            if (hcc != null){
                hcc.setURL(url);
                hcc.setIntervalInSeconds(intervalInSeconds);
                hcc.setTimeoutInSeconds(timeoutInSeconds);
            }else{
                ref.createHealthCheckerConfig(url, intervalInSeconds, timeoutInSeconds);
            }
        
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    
     /**
     *	<p> This handler takes in selected rows, and change the lbEnabled attr.
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "enabled" -- Type: <code>Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="changeLBEnabled",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="lbName", type=String.class, required=true),
        @HandlerInput(name="enabled", type=Boolean.class, required=true)})
        
    public static void changeLBEnabled(HandlerContext handlerCtx) {
        
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        String  lbName = (String) handlerCtx.getInputValue("lbName");
        boolean enabled = ((Boolean)handlerCtx.getInputValue("enabled")).booleanValue();
        
        List selectedRows = (List) obj;
        try{
            LBConfig lbConfig = getLBConfigOfLoadBalancer(lbName);
            if (lbConfig == null){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.noSuchLB", new Object[]{lbName}));
                return;
            }
            for(int i=0; i< selectedRows.size(); i++){
                Map oneRow = (Map) selectedRows.get(i);
                String target = (String) oneRow.get("targetName");
                ServerRefConfig sref = lbConfig.getServerRefConfigMap().get(target);
                if (sref != null)
                    sref.setLBEnabled(enabled);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler enable or disable the policy module text field according to the policy value.
     */
    @Handler(id="setDisableModuleField",
        input={
            @HandlerInput(name="moduleField", type=com.sun.webui.jsf.component.Field.class),
            @HandlerInput(name="policyValue", type=String.class)}
        )
    public static void setDisableModuleField(HandlerContext handlerCtx) {
        String policyValue = (String)handlerCtx.getInputValue("policyValue");
        Field moduleField = (Field)handlerCtx.getInputValue("moduleField");
        if("user-defined".equals(policyValue)){
            moduleField.setDisabled(false);
        }else
            moduleField.setDisabled(true);
    }
    
    /**
     * Change lb-enabled attribute for standalone instance
     */
    @Handler(id="changeInstanceLoadBalancing",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true),
            @HandlerInput(name="enabled", type=Boolean.class, required=true),
            @HandlerInput(name="cluster", type=String.class)
            }
    )
    public static void changeInstanceLoadBalancing(HandlerContext handlerCtx){
        String instanceName = (String)handlerCtx.getInputValue("instanceName");
        String cluster = (String)handlerCtx.getInputValue("cluster");
        Boolean enabled = (Boolean)handlerCtx.getInputValue("enabled");
        try{
            Map<String,LBConfig> lbConfigs = AMXUtil.getDomainConfig().getLBConfigMap();
            //iterate through lb-configs
            for(LBConfig lbConfig : lbConfigs.values()){
                if(cluster != null){
                    ClusterConfig clusterConfig =  AMXUtil.getDomainConfig().getClusterConfigMap().get(cluster);
                    if(clusterConfig != null) {
                        Map<String,ServerRefConfig> serverMap =  clusterConfig.getServerRefConfigMap();
                        ServerRefConfig sRefConfig = serverMap.get(instanceName);
                            if (sRefConfig != null) {
                                sRefConfig.setLBEnabled(enabled);
                            }                        
                    }
                    
                } else {
                    //get the server-ref in this lb-config
                    Map<String,ServerRefConfig> serverRefs = lbConfig.getServerRefConfigMap();
                    //get the server-ref for this target
                    ServerRefConfig sRef = serverRefs.get(instanceName);
                    if (sRef != null) {
                        sRef.setLBEnabled(enabled);
                    }
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
    /**
     * Get the last exported and Apply changes time
     */
    @Handler(id="getLBExportInfo",
        input={
            @HandlerInput(name="lbName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="lbConfigName", type=String.class),
            @HandlerOutput(name="lastApply", type=String.class),
            @HandlerOutput(name="lastExport", type=String.class)}
    )
    public static void getLBExportInfo(HandlerContext handlerCtx){
        String lbName = (String)handlerCtx.getInputValue("lbName");
        LoadBalancer loadBalancer = AMXUtil.getDomainRoot().getLoadBalancerMap().get(lbName);
        LBConfig lBConfig = getLBConfigOfLoadBalancer(lbName);
        if (loadBalancer == null || lBConfig == null ){
            GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.loadBalancer.NoSuchLB", new Object[]{lbName}), null);
        }
        try{
            Date lastApplyDate = loadBalancer.getLastApplied();
            Date lastExportDate = loadBalancer.getLastExported();
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, GuiUtil.getLocale());
            String lastApply = (lastApplyDate == null)? "" : dateFormat.format(lastApplyDate);
            String lastExport = (lastExportDate == null)? "" : dateFormat.format(lastExportDate);
            handlerCtx.setOutputValue("lastApply", lastApply);
            handlerCtx.setOutputValue("lastExport", lastExport);
            handlerCtx.setOutputValue("lbConfigName", lBConfig.getName());
            
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }                
    
    /**
     * Apply LB changes
     */
    @Handler(id="applyLBChanges",
        input={
            @HandlerInput(name="lbName", type=String.class, required=true)}
    )
    public static void applyLBChanges(HandlerContext handlerCtx){
        String lbName = (String)handlerCtx.getInputValue("lbName");
        LoadBalancer loadBalancer = AMXUtil.getDomainRoot().getLoadBalancerMap().get(lbName);
        if (loadBalancer == null){
            GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.loadBalancer.NoSuchLB", new Object[]{lbName}), null);
        }
        try{
            loadBalancer.applyLBChanges();
            GuiUtil.prepareAlert(handlerCtx, "success", GuiUtil.getMessage("msg.ApplyLBSuccessful"), null);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }           
    
    /**
     *	<p> This handler returns the list of LB for the target
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getLBforTarget",
           input={
            @HandlerInput(name="targetName", type=String.class, required=true),
            @HandlerInput(name="isCluster", type=Boolean.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class),
            @HandlerOutput(name="hasLB", type=Boolean.class)}
     )
     public static void getLBforTarget(HandlerContext handlerCtx){
        String targetName = (String) handlerCtx.getInputValue("targetName");
        Boolean isCluster = (Boolean) handlerCtx.getInputValue("isCluster");
        List result = new ArrayList();
        try{
            
            Map<String, LoadBalancerConfig> lbMap =  AMXUtil.getLBConfigHelper().getLoadBalancers(targetName, isCluster);
            for(String lbName : lbMap.keySet()){
                HashMap oneRow = new HashMap();
                oneRow.put("lbName", lbName);
                result.add(oneRow);
            }
            
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("hasLB", !result.isEmpty());
        handlerCtx.setOutputValue("result", result);
    }
    
    private static LBConfig getLBConfigOfLoadBalancer(String lbName){
        LoadBalancerConfig loadBalancerConfig = AMXUtil.getDomainConfig().getLoadBalancerConfigMap().get(lbName);
        if (loadBalancerConfig==null){
            return null;
        }
        String lbConfigName = loadBalancerConfig.getLbConfigName();
        LBConfig lbConfig = AMXUtil.getDomainConfig().getLBConfigMap().get(lbConfigName);
        return lbConfig;
    }
    
}
