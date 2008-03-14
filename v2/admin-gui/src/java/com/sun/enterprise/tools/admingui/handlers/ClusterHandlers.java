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
 * ServerHandlers.java
 *
 * Created on July 20, 2006, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.enterprise.tools.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.component.dataprovider.MultipleListDataProvider;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;

import javax.management.ObjectName;

import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;

import com.sun.webui.jsf.component.TableRowGroup;

import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.appserv.management.config.ConfigConfigKeys;
import com.sun.appserv.management.j2ee.StateManageable;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.J2EECluster;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.ee.admin.PortReplacedException;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;


/**
 *
 * @author Administrator
 */
public class ClusterHandlers{

   
    /**
     *	<p> This handler creates clusters and clustered instances
     *	@param	context	The HandlerContext.
     */
    @Handler(id="createCluster",
    input={
        @HandlerInput(name="Name",       type=String.class, required=true),
        @HandlerInput(name="Config",  type=String.class),
        @HandlerInput(name="CopyConfig",     type=String.class),
        @HandlerInput(name="Instances", type=List.class)})
        public static void createCluster(HandlerContext handlerCtx) {
        String clusterName = (String)handlerCtx.getInputValue("Name");
        String configName = (String)handlerCtx.getInputValue("Config");
        String copyFlag = (String)handlerCtx.getInputValue("CopyConfig");
        List<Map> instances = (List)handlerCtx.getInputValue("Instances");
        boolean clusterCreated = false;
        try {
            if (AMXUtil.getConfig(clusterName+"-config") != null){
               GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.cluster.creationError", new 
                        Object[]{clusterName}));
                return;
            }            
            if(instances == null || instances.isEmpty()) {
                createClusterConfig(copyFlag, configName, clusterName);
                clusterCreated = true;
            } else {
                ListIterator li = instances.listIterator();
                while(li.hasNext()) {
                    Map server = (Map)li.next();
                    String instanceName = (String)server.get("name");
                    String nodeAgent = (String)server.get("node");
                    String weight = (String)server.get("weight");
                    if (instanceName != null && (! instanceName.trim().equals(""))) {
                        //Cannot use AMX API b/c a generic exception is thrown warning of port replacements
                        //JMX throws PortReplaceException which we can catch and ignore. AMX throws MBeanException for ALL exceptions
                        //and we can't ignore all exceptions
                        if (nodeAgent != null && (! nodeAgent.trim().equals(""))) {
                            if(!clusterCreated) {
                                createClusterConfig(copyFlag, configName, clusterName);
                                clusterCreated = true;
                            }
                            createInstance(nodeAgent, instanceName, clusterName);
                        } else {
                            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.requiredMsgNodeAgent"));
                        }
                        
                        if(weight != null && (! weight.trim().equals(""))) {
                            AMXUtil.getDomainConfig().getClusteredServerConfigMap().get(instanceName).setLBWeight(weight);
                            
                        }
                    } else {
                        //In case user clicked "New" button and added empty rows to instances table we
                        //still need to create cluster without instances
                        if(!clusterCreated) {
                            createClusterConfig(copyFlag, configName, clusterName);
                            clusterCreated = true;
                        }
                        
                    }
                }
                
            }
        }catch(Exception ex){
            //We want to go back to the table or not depending if cluster is already created.
            if (clusterCreated)
                GuiUtil.prepareException(handlerCtx, ex);
            else
                GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    private static void createClusterConfig(String copyFlag, String configName, String clusterName) {
        boolean configCopied = false;
        if ("copy".equals(copyFlag)){
            configCopied = true;
            HashMap configMap = new HashMap();
            if("default-config".equals(configName)) {
                AMXUtil.getDomainConfig().createConfigConfig(clusterName+"-config", configMap);
                
            } else {
                configMap.put(ConfigConfigKeys.SRC_CONFIG_NAME_KEY, configName);
                AMXUtil.getDomainConfig().createConfigConfig(clusterName+"-config", configMap);
            }
            configName = clusterName + "-config";
            
        }
        AMXUtil.getDomainConfig().createClusterConfig(clusterName, configName, new HashMap());
    }  
    
    /**
     *	<p> This handler creates cluster instance
     *	@param	context	The HandlerContext.
     */
    @Handler(id="createClusterInstance",
    input={
        @HandlerInput(name="InstanceName",       type=String.class, required=true),
        @HandlerInput(name="ClusterName",       type=String.class, required=true),
        @HandlerInput(name="NodeAgent",        type=String.class, required=true)})
        public static void createClusterInstance(HandlerContext handlerCtx) {
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        String clusterName = (String)handlerCtx.getInputValue("ClusterName");
        String nodeAgent = (String)handlerCtx.getInputValue("NodeAgent");
        try {
            createInstance(nodeAgent, instanceName, clusterName);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }    
    
    private static void createInstance(String nodeAgent, String instanceName, String clusterName) {
        String[] signature = {
            "java.lang.String",
            "java.lang.String",
            "java.lang.String",
            "java.lang.String",
            "java.util.Properties"
        };
        Object[] params = {
            nodeAgent,
            instanceName,
            null,
            clusterName,
            null
        };
        try {
            JMXUtil.getMBeanServer().invoke(new ObjectName("com.sun.appserv:type=servers,category=config"), "createServerInstance", params, signature);
        }catch (Exception ex){
            if (ex.getCause() instanceof PortReplacedException){
                // indicates that createInstance succeeded but with a "WARNING indicating that the user provided
                // or default ports were overridden.
                // ignored.
            }else
                throw new RuntimeException(ex);
        }
    }
    
   /**
     *	<p> This handler takes in a HashMap, the name-value pair being the Properties.
     *  It turns each name-value pair to one hashMap, representing one row of table data, 
     *  and returns the list of Map. 
     *
     *  <p> Input value: "Properties" -- Type: <code>java.util.Map</code>/</p>
     *  <p> Output value: "TableList" -- Type: <code>java.util.List</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusterTableList",
    input={
        @HandlerInput(name="Properties", type=Map.class, required=true)},
    output={
        @HandlerOutput(name="TableList", type=List.class)})
        public static void getClusterTableList(HandlerContext handlerCtx) {
        List data = new ArrayList();
        Map<String, Object> props = (Map)handlerCtx.getInputValue("Properties");	
        if(props != null ){
            for(String key : props.keySet()){
                HashMap oneRow = new HashMap();
                Object value = props.get(key);
                String valString = (value==null)? "" : value.toString();
                oneRow.put("name", key);
                oneRow.put("weight", " ");
                oneRow.put("node", " ");
                oneRow.put("selected", false);
                data.add(oneRow);
            }
        }
        List<List<Map<String, Object>>> list = new ArrayList<List<Map<String, Object>>>();
        list.add(data);        
        handlerCtx.setOutputValue("TableList", list);
    }    
    
    
    /**
     *	<p> This handler returns the list of Clusters and config info for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClustersforTable",
        output={
            @HandlerOutput(name="hasCluster", type=String.class),
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getClustersforTable(HandlerContext handlerCtx){
        
        List result = new ArrayList();
        try{
            Map <String, ClusterConfig> clusterMap = AMXUtil.getDomainConfig().getClusterConfigMap();
            for(String key : clusterMap.keySet()){
                HashMap oneRow = new HashMap();
                String name = clusterMap.get(key).getName();
                String config = clusterMap.get(key).getReferencedConfigName();
                Map<String,ClusteredServerConfig> cservers = clusterMap.get(key).getClusteredServerConfigMap();
                oneRow.put("name", (name == null) ? " ": name);
                oneRow.put("config", (config == null) ? " ": config);
                oneRow.put("selected", false);
                result.add(oneRow);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("hasCluster", (result.size()>0) );
    }  

    /**
     *	<p> This handler returns the list of ClusteredInstances for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusteredInstances",
           input={
            @HandlerInput(name="ClusterName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getClusteredInstances(HandlerContext handlerCtx){
        
        String cluster = (String) handlerCtx.getInputValue("ClusterName");
        List result = new ArrayList();
        try{
            if(cluster != null){
                Map<String,ClusteredServerConfig> serverMap = AMXUtil.getDomainConfig().getClusterConfigMap().get(cluster).getClusteredServerConfigMap();
                if(serverMap != null) {
                    for(String key : serverMap.keySet()){
                        HashMap oneRow = new HashMap();
                        String name = serverMap.get(key).getName();
                        oneRow.put("name", name);
                        oneRow.put("image", AMXUtil.getStatusForDisplay(
                                AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name) , true));
                        result.add(oneRow);
                    }
                }
            }
            handlerCtx.setOutputValue("result", result);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
      /**
     *	<p> This handler returns the number of instances stopped and restart required for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusteredInstancesStatus",
           input={
            @HandlerInput(name="Instances", type=List.class, required=true)},
        output={
            @HandlerOutput(name="Stopped", type=String.class),
            @HandlerOutput(name="Stopping", type=String.class),
            @HandlerOutput(name="Failed", type=String.class),
            @HandlerOutput(name="Starting", type=String.class),
            @HandlerOutput(name="Running", type=String.class),
            @HandlerOutput(name="Restart", type=String.class)}
     )
     public static void getClusteredInstancesStatus(HandlerContext handlerCtx){
        
     	List instances = (List) handlerCtx.getInputValue("Instances");
        int stoppedCount = 0;
        int failedCount = 0;
        int stoppingCount = 0;
        int startingCount = 0;
        int runningCount = 0;
        int restartCount = 0;
        if(instances != null){
            List<Map> instanceMap = (List) instances;
            for(Map oneRow : instanceMap){
                String name = (String)oneRow.get("name");
		RuntimeStatus rsts = JMXUtil.getRuntimeStatus(name);
		int statusCode = JMXUtil.getRuntimeStatusCode(rsts);
		switch(statusCode) {
                	case Status.kInstanceRunningCode: {
               			boolean restart = rsts.isRestartNeeded();
               			if (restart){
               				restartCount++;
               			} else {
               				runningCount++;
               			}
				break;
               		} 
               		case Status.kInstanceFailedCode: {
               			failedCount++;
				break;
               		}
               		case Status.kInstanceStartingCode: {
               			startingCount++;
				break;
               		}
               		case Status.kInstanceNotRunningCode: {
               			stoppedCount++;
				break;
               		}
               		case Status.kInstanceStoppingCode: {
               			stoppingCount++;
				break;
               		}
        	}
	}

	}
        handlerCtx.setOutputValue("Stopped", (stoppedCount == 0) ? " ": AMXUtil.getStatusImage(StateManageable.STATE_STOPPED) + "&nbsp;" +stoppedCount+ "&nbsp;" + GuiUtil.getMessage("common.stoppedState")+"<br>");
        handlerCtx.setOutputValue("Stopping", (stoppingCount == 0) ? " ": AMXUtil.getStatusImage(StateManageable.STATE_STOPPING) + "&nbsp;" +stoppingCount+ "&nbsp;" + GuiUtil.getMessage("common.stoppingState")+"<br>");
        handlerCtx.setOutputValue("Starting", (startingCount == 0) ? " ": AMXUtil.getStatusImage(StateManageable.STATE_STARTING) + "&nbsp;" +startingCount+ "&nbsp;" + GuiUtil.getMessage("common.startingState")+"<br>");
        handlerCtx.setOutputValue("Failed", (failedCount == 0) ? " ": AMXUtil.getStatusImage(StateManageable.STATE_FAILED) + "&nbsp;" +failedCount+ "&nbsp;" + GuiUtil.getMessage("common.failedState"));
        handlerCtx.setOutputValue("Running", (runningCount == 0) ? " ": AMXUtil.getStatusImage(StateManageable.STATE_RUNNING) + "&nbsp;" +runningCount+ "&nbsp;" + GuiUtil.getMessage("common.runningState")+"<br>");
        handlerCtx.setOutputValue("Restart", (restartCount == 0) ? " ": GuiUtil.getMessage("common.restartRequiredImage") + "&nbsp;" +restartCount+ "&nbsp;" + GuiUtil.getMessage("common.restartState")+"<br>");
        
    }
    
    /**
     *  <p> This handler starts or stops specified cluster.
     *  @param  context The HandlerContext.
     */
    @Handler(id="clusterAction",
         input={
           @HandlerInput(name="start", type=Boolean.class, required=true),
           @HandlerInput(name="clusterName", type=String.class, required=true)}
     )
     public static void clusterAction(HandlerContext handlerCtx){

        String clusterName = (String) handlerCtx.getInputValue("clusterName");
        Boolean start = (Boolean) handlerCtx.getInputValue("start");
        try{
            JMXUtil.clusterAction(clusterName, start);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

     
   /**
     *	<p> This handler starts all selected clustered server instances.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="selectedClustersAction",
           input={
           @HandlerInput(name="start", type=Boolean.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
     )
     public static void selectedClustersAction(HandlerContext handlerCtx){
        
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        Boolean start = (Boolean) handlerCtx.getInputValue("start");
        
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String clusterName = (String)oneRow.get("name");
                JMXUtil.clusterAction(clusterName, start);
            }
            
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }        
    }
    
    /**
     *	<p> This handler starts the selected instance specified by the name 
     */
    @Handler(id="clusterInstanceAction",
        input={
            @HandlerInput(name="clusterName", type=String.class, required=true),
            @HandlerInput(name="start", type=Boolean.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
        )

    public static void clusterInstanceAction(HandlerContext handlerCtx) {
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        String clusterName = (String) handlerCtx.getInputValue("clusterName");
        Boolean start = (Boolean) handlerCtx.getInputValue("start");
        try {
            //If all the instance of the cluster is selected, just call start cluster
            Map instancesMap = AMXUtil.getDomainConfig().getClusterConfigMap().get(clusterName).getClusteredServerConfigMap();
            if (instancesMap.size() == obj.size()){
                JMXUtil.clusterAction(clusterName, start);
                return;
            }
            
            List<Map> selectedRows = (List) obj;
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                if (start)
                    JMXUtil.startServerInstance(name);
                else
                    JMXUtil.stopServerInstance(name);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }        
    }   
    
      /**
     *	<p> This handler deletes all selected clustered server instances.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteSelectedClusters",
           input={
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
     )
     public static void deleteSelectedClusters(HandlerContext handlerCtx){
        
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                // see if the cluster is running...
                boolean running = true;
                J2EECluster j2eeCluster = AMXUtil.getJ2EEDomain().getJ2EEClusterMap().get(name);
                // try to stop the cluster.
                if(j2eeCluster.getstate() == StateManageable.STATE_RUNNING){
                    j2eeCluster.stop();
                }
                // get the list of instances of the cluster
                Map<String,ClusteredServerConfig> serverMap = AMXUtil.getDomainConfig().getClusterConfigMap().get(name).getClusteredServerConfigMap();
                if(serverMap != null){
                    // delete the instances one by one.  Need to use JMX. AMX API not working correctly.
                    for(String key : serverMap.keySet()){
                        HashMap server = new HashMap();
                        String serverName = serverMap.get(key).getName();
                        JMXUtil.invoke(
                                "com.sun.appserv:type=servers,category=config",
                                "deleteServerInstance",
                                new Object[]{serverName},
                                new String[]{"java.lang.String"});
                    }
                    //Using AMX to remove server is not working.  Need to uncomment once it is working.
                    //AMXUtil.getDomainConfig().getClusterConfigMap().get(name).removeClusteredServerConfig(serverName);
                    
                }
                // delete the cluster itself.
                AMXUtil.getDomainConfig().removeClusterConfig(name);
            }
            
            
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
      /**
     *	<p> This handler deletes all selected clustered server instances.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteSelectedInstances",
           input={
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
     )
     public static void deleteSelectedInstances(HandlerContext handlerCtx){
        
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                // see if the instance is running...
                boolean running = true;
                J2EEServer j2eeServer = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name);
                // try to stop the cluster.
                if(j2eeServer.getstate() == StateManageable.STATE_RUNNING){
                    j2eeServer.stop();
                }
                // remove instance
                AMXUtil.getDomainConfig().removeClusteredServerConfig(name);
            }
            
            
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }  
 
    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Cluster General Page.</p>
     *  <p> Input  value: "ClusterName" -- Type: <code> java.lang.String</code></p>
     *	<p> Output value: "Config" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HbEnabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "HbAddress" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HbPort" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Status" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusterGeneralAttributes",
        input={
            @HandlerInput(name="ClusterName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="HbEnabled", type=Boolean.class),
            @HandlerOutput(name="HbPort", type=String.class),
            @HandlerOutput(name="HbAddress", type=String.class),
            @HandlerOutput(name="Config", type=String.class)})

    public static void getClusterGeneralAttributes(HandlerContext handlerCtx) {
        String clusterName = (String) handlerCtx.getInputValue("ClusterName");
        try{
            ClusterConfig cluster = AMXUtil.getDomainConfig().getClusterConfigMap().get(clusterName);
            String address = cluster.getHeartbeatAddress();
            String port = cluster.getHeartbeatPort();
            boolean hbenabled = cluster.getHeartbeatEnabled();
            String config = cluster.getReferencedConfigName();
            
            handlerCtx.setOutputValue("HbAddress", address);
            handlerCtx.setOutputValue("HbPort", port);
            handlerCtx.setOutputValue("HbEnabled", hbenabled);
            handlerCtx.setOutputValue("Config", config);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
   /**
     *	<p> This handler saves the values for all the attributes in the
     *      Cluster General Page.</p>
      *	<p> Output value: "clusterName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HbEnabled"   -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "HbPort"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HbAddress"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveClusterGeneralAttributes",
    input={
        @HandlerInput(name="clusterName",      type=String.class),
        @HandlerInput(name="HbEnabled",  type=Boolean.class),
        @HandlerInput(name="HbPort",    type=String.class),
        @HandlerInput(name="HbAddress",       type=String.class)})
        
        
    public static void saveClusterGeneralAttributes(HandlerContext handlerCtx) {
        String clusterName = (String) handlerCtx.getInputValue("clusterName");
        try{
            ClusterConfig cluster = AMXUtil.getDomainConfig().getClusterConfigMap().get(clusterName);
            
            cluster.setHeartbeatEnabled(((Boolean)handlerCtx.getInputValue("HbEnabled")).booleanValue());
            cluster.setHeartbeatPort((String)handlerCtx.getInputValue("HbPort"));
            cluster.setHeartbeatAddress((String)handlerCtx.getInputValue("HbAddress"));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }  
    
    /**
     *	<p> This handler returns the number of instances running and stopped for the cluster</p>
     *  <p> Input  value: "ClusterName" -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "NumStopped" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NumRunning" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusteredInstancesStatusCount",
        input={
            @HandlerInput(name="ClusterName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="NumStopped", type=String.class),
            @HandlerOutput(name="NumRunning", type=String.class),
            @HandlerOutput(name="disableEjb", type=Boolean.class),
            @HandlerOutput(name="disableStart", type=Boolean.class),
            @HandlerOutput(name="disableStop", type=Boolean.class)
            })

        public static void getClusteredInstancesStatusCount(HandlerContext handlerCtx) {
        
        String clusterName = (String) handlerCtx.getInputValue("ClusterName");
        
        int stopped = 0;
        int running = 0;
        Map<String,ClusteredServerConfig> serverMap = AMXUtil.getDomainConfig().getClusterConfigMap().get(clusterName).getClusteredServerConfigMap();
        if(serverMap != null) {
            for(String key : serverMap.keySet()){
                String serverName = serverMap.get(key).getName();
		RuntimeStatus rsts = JMXUtil.getRuntimeStatus(serverName);
		int statusCode = JMXUtil.getRuntimeStatusCode(rsts);
                if(statusCode == Status.kInstanceStoppingCode || statusCode == Status.kInstanceNotRunningCode || statusCode == Status.kInstanceFailedCode) {
                    stopped++;
                    
                }
                if(statusCode == Status.kInstanceRunningCode || statusCode == Status.kInstanceStartingCode){
                    running++;
                }
            }
        }
        handlerCtx.setOutputValue("NumStopped", stopped+" "+GuiUtil.getMessage("cluster.numStopped"));
        handlerCtx.setOutputValue("NumRunning", running+" "+GuiUtil.getMessage("cluster.numRunning"));
        handlerCtx.setOutputValue("disableEjb", (stopped > 0) ? false :true);  //refer to bug#6342445
        handlerCtx.setOutputValue("disableStart", (stopped > 0) ? false :true);
        handlerCtx.setOutputValue("disableStop", (running > 0) ? false :true);
    }   
    
   /**
     *	<p> This handler returns the list of Clustered Instances for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getListOfClusteredInstances",
        input={
            @HandlerInput(name="ClusterName", type=String.class, required=true)},    
        output={
            @HandlerOutput(name="result", type=java.util.List.class),
            @HandlerOutput(name="hasLB", type=Boolean.class)}
     )
     public static void getListOfClusteredInstances(HandlerContext handlerCtx){
        String clusterName = (String) handlerCtx.getInputValue("ClusterName");
        try{
            ClusterConfig clusterConfig = AMXUtil.getDomainConfig().getClusterConfigMap().get(clusterName);
            Map<String,ClusteredServerConfig> serverMap = AMXUtil.getDomainConfig().getClusterConfigMap().get(clusterName).getClusteredServerConfigMap();
            List result = new ArrayList();
            if(serverMap != null) {
                for(String key : serverMap.keySet()){
                    HashMap oneRow = new HashMap();
                    String serverName = key;
                    String config = serverMap.get(key).getReferencedConfigName();
                    String node = serverMap.get(key).getReferencedNodeAgentName();
                    String weight = serverMap.get(key).getLBWeight();
                    String state = AMXUtil.getStatusForDisplay(
                            AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(serverName), true);
                    
                    oneRow.put("name", serverName);
                    oneRow.put("selected", false);
                    oneRow.put("config", (config == null) ? " ": config);
                    oneRow.put("node", (node == null) ? " ": node);
                    oneRow.put("weight", (weight == null) ? " ": weight);
                    oneRow.put("status", (state == null) ? " ": state);
                    
                    ServerRefConfig serverRef = clusterConfig.getServerRefConfigMap().get(key);
                    int timeout = serverRef.getDisableTimeoutInMinutes();
                    oneRow.put("timeout", ""+timeout);
                    oneRow.put("lbStatus", ""+serverRef.getLBEnabled());
                    
                    result.add(oneRow);
                }
            }
            
            Map lbMap =  AMXUtil.getLBConfigHelper().getLoadBalancers(clusterName, true);
            handlerCtx.setOutputValue("hasLB", !lbMap.isEmpty());
            handlerCtx.setOutputValue("result", result);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
       /**
     *	<p> This handler saves the weight for the clustered instance 
     *  <p> Input  value: "TableRowGroup" -- Type: <code> TableRowGroup</code></p>
     */
    @Handler(id="saveClusteredInstanceWeight",
        input={
            @HandlerInput(name="TableRowGroup", type=TableRowGroup.class, required=true)}
        )
    
    public static void saveClusteredInstanceWeight(HandlerContext handlerCtx) {
        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("TableRowGroup");
        try{
        MultipleListDataProvider dp = (MultipleListDataProvider)trg.getSourceData();
        List data = dp.getLists();
         ListIterator li = data.listIterator();
         while(li.hasNext()) {
             List inner = (List)li.next();
             ListIterator innerli = inner.listIterator();
             boolean foundError = false;
             while(innerli.hasNext()){
                 Map instance = (Map)innerli.next();
                 String name = (String)instance.get("name");
                 String weight = (String)instance.get("weight");
                 if (weight != null && (! weight.trim().equals(""))) {
                     AMXUtil.getDomainConfig().getClusteredServerConfigMap().get(name).setLBWeight(weight);
                 } else {
                     foundError = true;
                 }
                if (!foundError){
                    GuiUtil.prepareAlert(handlerCtx,"success", GuiUtil.getMessage("msg.saveSuccessful"), null);
                }else{
                    GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.EmptyWeight"), null);
                }                 
             }
         }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }            
    }       
    
    /**
     *	<p> This handler saves the disable timeout of server ref. 
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     */
    @Handler(id="saveServerRefDisableTimeout",
        input={
            @HandlerInput(name="clusterName", type=String.class, required=true),
            @HandlerInput(name="TableRowGroup", type=TableRowGroup.class, required=true)}
        )
    
    public static void saveServerRefDisableTimeout(HandlerContext handlerCtx) {
        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("TableRowGroup");
        String clusterName = (String)handlerCtx.getInputValue("clusterName");
        ClusterConfig clusterConfig = AMXUtil.getDomainConfig().getClusterConfigMap().get(clusterName);
        try{
            MultipleListDataProvider dp = (MultipleListDataProvider)trg.getSourceData();
            List<List<Object>> data = dp.getLists();
            for(List inner : data){
                List<Map> innerMap = inner;
                for(Map oneRow: innerMap){
                    String serverName = (String)oneRow.get("name");
                    ServerRefConfig ref = clusterConfig.getServerRefConfigMap().get(serverName);
                    String timeout = (String)oneRow.get("timeout");
                    if (GuiUtil.isEmpty(timeout)){
                        timeout=ref.getDefaultValue("DisableTimeoutInMinutes");
                    }
                    ref.setDisableTimeoutInMinutes(Integer.parseInt(timeout));
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }            
    }    
    
    /**
     *	<p> This handler sets the lb-enable of server-ref of a cluster.
     *  <p> Input  value: "target" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="changeServerRefLB",
        input={
            @HandlerInput(name="clusterName", type=String.class, required=true),
            @HandlerInput(name="enabled", type=Boolean.class, required=true),
            @HandlerInput(name="selectedRows", type=java.util.List.class)}
     )
    public static void changeServerRefLB(HandlerContext handlerCtx){
        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
        boolean enabled = ((Boolean)handlerCtx.getInputValue("enabled")).booleanValue();
        String clusterName = (String)handlerCtx.getInputValue("clusterName");
        ClusterConfig clusterConfig = AMXUtil.getDomainConfig().getClusterConfigMap().get(clusterName);
        try{
            for(Map oneRow: selectedRows){
                String serverName = (String) oneRow.get("name");
                ServerRefConfig ref = clusterConfig.getServerRefConfigMap().get(serverName);
                ref.setLBEnabled(enabled);
            }
        }catch(Exception ex){
            //TODO: log exception
            GuiUtil.prepareException(handlerCtx, ex);
        }
    }
    
        
    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Clustered Server Instance General Page.</p>
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "httpPorts" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "iiopPorts" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "version" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "configDir" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "debugPort" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusterInstanceGeneralAttributes",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="httpPorts", type=String.class),
            @HandlerOutput(name="iiopPorts", type=String.class),
            @HandlerOutput(name="version", type=String.class),
            @HandlerOutput(name="configDir", type=String.class),
            @HandlerOutput(name="debugPort", type=String.class),
            @HandlerOutput(name="status",     type=String.class),
            @HandlerOutput(name="nodeAgent",     type=String.class),
            @HandlerOutput(name="config",     type=String.class),
            @HandlerOutput(name="running",     type=Boolean.class),
            @HandlerOutput(name="nodeAgentStatus",     type=String.class)})

        public static void getClusterInstanceGeneralAttributes(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        try {
            ClusteredServerConfig server = AMXUtil.getDomainConfig().getClusteredServerConfigMap().get(instanceName);
            J2EEServer j2eeServer = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(instanceName);
            String config = server.getReferencedConfigName();
            String version = j2eeServer.getserverVersion();
            String node = null;
            //nodeagent name and status
            node = server.getReferencedNodeAgentName();
            String sts = NodeAgentHandlers.getNodeAgentStatus(node);
            StandAloneInstanceHandlers.getInstanceGeneralAttributes(handlerCtx, instanceName, config, version, node, sts);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }       
    
    /**
     *	<p> This handler returns the config properties in the
     *      Clustered Server Instance Config Properties Page.</p>
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "result" -- Type: <code>java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusterInstanceConfigProperties",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)})

        public static void getClusterInstanceConfigProperties(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        try {
            ClusteredServerConfig serverConfig = AMXUtil.getDomainConfig().getClusteredServerConfigMap().get(instanceName);
            ConfigConfig defaultConfig = AMXUtil.getDomainConfig().getConfigConfigMap().get("default-config");
            String[] propNames = serverConfig.getSystemPropertyNames();
            List result = new ArrayList();
             for(int i=0; i<propNames.length; i++){                
               HashMap oneRow = new HashMap();
               String name = propNames[i];
               //System.out.println("testing"+propNames[i] +);
               String propValue = serverConfig.getSystemPropertyValue(propNames[i]);
               String defaultValue = (String)defaultConfig.getSystemPropertyValue(propNames[i]);
               oneRow.put("name", propNames[i]);
               oneRow.put("default", defaultValue);
               oneRow.put("override", propValue);
               result.add(oneRow);
            }
            handlerCtx.setOutputValue("result", result);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }           
    
    /**
     *	<p> This method returns the properties for Clustered Instance Config </p>
     *
     *  <p> Output value: "Properties" -- Type: <code>java.util.List</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClusterInstanceProperties",
    input={
        @HandlerInput(name="InstanceName", type=String.class, required=true)},
    output={
        @HandlerOutput(name="Properties", type=Map.class)})
        public static void getClusterInstanceProperties(HandlerContext handlerCtx) {
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        ClusteredServerConfig serverConfig = AMXUtil.getDomainConfig().getClusteredServerConfigMap().get(instanceName);
        Map<String, String> props = serverConfig.getProperties();
        handlerCtx.setOutputValue("Properties", props);
        
    } 
    
/**            
     *	<p> This handler saves the props for Clustered Instance</p>
      *	<p> Input value: "InstanceName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"     -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"     -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveClusterInstanceProperties",
    input={
        @HandlerInput(name="InstanceName",      type=String.class, required=true),        
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class)})    
    public static void saveClusterInstanceProperties(HandlerContext handlerCtx) {
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        ClusteredServerConfig serverConfig = AMXUtil.getDomainConfig().getClusteredServerConfigMap().get(instanceName);
        ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
        Map addProps = (Map)handlerCtx.getInputValue("AddProps");
        String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
        for(int i=0; i<remove.length; i++){
            serverConfig.removeProperty(remove[i]);
        }
        if(addProps != null ){
            Iterator additer = addProps.keySet().iterator();
            while(additer.hasNext()){
                Object key = additer.next();
                String addvalue = (String)addProps.get(key);
                serverConfig.setPropertyValue((String)key, addvalue);
                
            }
        }      
    }
        
       /**
     *	<p> This handler saves the config properties for the clustered instance 
     *  <p> Input  value: "TableRowGroup" -- Type: <code> TableRowGroup</code></p>
     */
    @Handler(id="saveClusteredInstanceConfigProps",
        input={
            @HandlerInput(name="TableRowGroup", type=TableRowGroup.class, required=true),
            @HandlerInput(name="InstanceName",      type=String.class, required=true)}
        )
    
    public static void saveClusteredInstanceConfigProps(HandlerContext handlerCtx) {
        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("TableRowGroup");
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        try{
        MultipleListDataProvider dp = (MultipleListDataProvider)trg.getSourceData();
        List data = dp.getLists();
         ListIterator li = data.listIterator();
         while(li.hasNext()) {
             List inner = (List)li.next();
             ListIterator innerli = inner.listIterator();
             boolean foundError = false;
             while(innerli.hasNext()){
                 Map instance = (Map)innerli.next();
                 String name = (String)instance.get("name");
                 String override = (String)instance.get("override");
                 if (override != null && (!override.trim().equals(""))) {
                     AMXUtil.getDomainConfig().getClusteredServerConfigMap().get(instanceName).setSystemPropertyValue(name, override);
                 } else {
                     foundError = true;
                 }
                if (!foundError){
                    GuiUtil.prepareAlert(handlerCtx,"success", GuiUtil.getMessage("msg.saveSuccessful"), null);
                }else{
                    GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.EmptyOverrideValue"), null);
                }                  
             }
         }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }            
    }            
    
     /**
     *	<p> This handler returns the list of instance for the cluster
     *  if 'state' is not specified, ALl istance will be returned.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getInstancesOfCluster",
           input={
            @HandlerInput(name="clusterName", type=String.class, required=true),
            @HandlerInput(name="state", type=Boolean.class),
            @HandlerInput(name="addEmpty", type=Boolean.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class),
            @HandlerOutput(name="hasResult", type=Boolean.class)}
     )
     public static void getInstancesOfCluster(HandlerContext handlerCtx){
        String cluster = (String) handlerCtx.getInputValue("clusterName");
        Boolean state = (Boolean) handlerCtx.getInputValue("state");
        Boolean addEmpty = (Boolean) handlerCtx.getInputValue("addEmpty");
        
        List result = new ArrayList();
        try{
            Map<String,ClusteredServerConfig> serverMap = AMXUtil.getDomainConfig().getClusterConfigMap().get(cluster).getClusteredServerConfigMap();
            for(String key : serverMap.keySet()){
                String name = serverMap.get(key).getName();
		RuntimeStatus rsts = JMXUtil.getRuntimeStatus(name);
		int statusCode = JMXUtil.getRuntimeStatusCode(rsts);
                if (state == null)
                    result.add(name);
                else{
                    if(statusCode == Status.kInstanceRunningCode){
                        if (state) {
				 result.add(name);
			}
                    }else{
                        if (!state) result.add(name);
                    }
                }
            }
            handlerCtx.setOutputValue("result", result);
            handlerCtx.setOutputValue("hasResult", !result.isEmpty());
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    
    /**
     *	<p> This handler migrates EJB Timers between clustered instances.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="migrateEjbTimer",
    input={
        @HandlerInput(name="source",  type=String.class, required=true),
        @HandlerInput(name="dest",     type=String.class)})
        public static void migrateEjbTimer(HandlerContext handlerCtx) {
        String source = (String)handlerCtx.getInputValue("source");
        String dest = (String)handlerCtx.getInputValue("dest");
        if (dest == null) dest = "";
        
        String objName = "com.sun.appserv:type=ejb-timer-management,category=config";
        String methodName ="migrateTimers";
        Object[] params = new Object[] {source, dest};
        String[] types = new String[] {"java.lang.String", "java.lang.String"};
        try{
            JMXUtil.invoke(objName, methodName, params, types);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
}
