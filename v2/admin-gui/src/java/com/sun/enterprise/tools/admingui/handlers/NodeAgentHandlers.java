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
 * NodeAgentHandlers.java
 *
 * Created on November 11, 2006, 11:15 AM
 *
 */
package com.sun.enterprise.tools.admingui.handlers;

import com.sun.appserv.management.config.AuthRealmConfig;
import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.appserv.management.config.LogServiceConfig;
import com.sun.appserv.management.config.ModuleLogLevelsConfig;
import com.sun.appserv.management.config.NodeAgentConfig;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.StateManageable;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.io.File;
import javax.management.Attribute;
import javax.management.AttributeList;

import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

/**
 *
 * @author Nitya Doraisamy
 */
public class NodeAgentHandlers {
    
    /**
     *	<p> This handler returns the list of Clusters and config info for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getNodeAgentsList",
        input={
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getNodeAgentsList(HandlerContext handlerCtx){
            List result = new ArrayList();
        try{
            Map <String, NodeAgentConfig> nodeAgentMap = AMXUtil.getDomainConfig().getNodeAgentConfigMap();
            List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
            boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
            for(String key : nodeAgentMap.keySet()){
                HashMap oneRow = new HashMap();
                NodeAgentConfig agentConfig = nodeAgentMap.get(key);
                String name = agentConfig.getName();
                String agentStatus = agentConfig.getPropertyValue("rendezvousOccurred");
                String hostName = GuiUtil.getMessage("nodeAgent.UnknownHost");
                String status = null;
                if(agentStatus.equalsIgnoreCase("true")){
                    hostName = agentConfig.getJMXConnectorConfig().getPropertyValue("client-hostname");
                    status = getStatusString(name);
                }else{
                    status = getWarningString(GuiUtil.getMessage("nodeAgent.awaitingInitialSync"));
                }
                oneRow.put("name", (name == null) ? " ": name);
                oneRow.put("hostName", (hostName == null) ? " ": hostName);
                oneRow.put("status", (status == null) ? " ": status);
                oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
                result.add(oneRow);
            }
        }catch(Exception ex){
            GuiUtil.prepareException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
    }
    
    /**
     *	<p> This handler starts all selected clustered server instances.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteSelectedNodeAgents",
        input={
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
     )
     public static void deleteSelectedNodeAgents(HandlerContext handlerCtx){
        String operName = "deleteNodeAgentConfig";
        String[] signature = {"java.lang.String"};
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                NodeAgentConfig ndAgent = AMXUtil.getDomainConfig().getNodeAgentConfigMap().get(name);
                Object[] params = {name};
                JMXUtil.invoke(NODE_AGENT_OBJNAME, operName, params, signature);
            }
            if (File.separatorChar == '\\'){
                //For Window, there is a timing issue that we need to put in some delay. bug# 6586023
                Thread.sleep(2000);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the number of instances stopped and restart required for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getNodeAgentInstancesStatus",
           input={
            @HandlerInput(name="Instances", type=List.class, required=true)},
        output={
            @HandlerOutput(name="Stopped", type=String.class),
            @HandlerOutput(name="Restart", type=String.class)}
     )
     public static void getNodeAgentInstancesStatus(HandlerContext handlerCtx){
        List instances = (List) handlerCtx.getInputValue("Instances");
        int stopped = 0;
        int restart = 0;
        if(instances != null){
            List<Map> instanceMap = (List) instances;
            for(Map oneRow : instanceMap){
                String name = (String)oneRow.get("name");
                try{
                    J2EEServer j2eeServer = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name);
                    if(j2eeServer.getstate() == StateManageable.STATE_STOPPING || j2eeServer.getstate() == StateManageable.STATE_STOPPED || j2eeServer.getstate() == StateManageable.STATE_FAILED) {
                        stopped++;
                    }
                    if(j2eeServer.getRestartRequired() == true){
                        restart++;
                    }
                }catch(Exception ex){
                    //ignoring exception since attribute might not be found
                }
            }
        }       
        if(stopped == 0) {
            handlerCtx.setOutputValue("Stopped", "--");
        } else {
            handlerCtx.setOutputValue("Stopped", stopped);
        }
        if(restart == 0) {
            handlerCtx.setOutputValue("Restart", "--");
        } else {
            handlerCtx.setOutputValue("Restart", restart);
        }
    }
    
    
    private static Map getNodeAgentsInstances(String nodeAgentName) {
        Map<String, ServerConfig> values = new HashMap();
        Map standAloneMap = AMXUtil.getDomainConfig().getStandaloneServerConfigMap();
        for(Iterator itr = standAloneMap.values().iterator(); itr.hasNext();){
            StandaloneServerConfig serverConfig = (StandaloneServerConfig)itr.next();
            if(nodeAgentName.equals(serverConfig.getReferencedNodeAgentName())){
                values.put(serverConfig.getName(), (ServerConfig)serverConfig);
            }        
        }
        Map clusterMap = AMXUtil.getDomainConfig().getClusteredServerConfigMap();
        for(Iterator itr = clusterMap.values().iterator(); itr.hasNext();){
            ClusteredServerConfig clusterConfig = (ClusteredServerConfig)itr.next();
            if(nodeAgentName.equals(clusterConfig.getReferencedNodeAgentName())){
                values.put(clusterConfig.getName(), (ServerConfig)clusterConfig);
            }        
        }
        return values;
    }
    
    /**
     *	<p> This handler returns the list of ClusteredInstances for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getNodeAgentValues",
           input={
            @HandlerInput(name="NodeAgentName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="HostName",     type=String.class),
            @HandlerOutput(name="Status",       type=String.class),
            @HandlerOutput(name="OnInstances",  type=Boolean.class),
            @HandlerOutput(name="Connected",    type=Boolean.class),
            @HandlerOutput(name="Properties", type=Map.class) }
     )
     public static void getNodeAgentValues(HandlerContext handlerCtx){
        String ndAgentName = (String) handlerCtx.getInputValue("NodeAgentName");
        try{
            boolean connected = false;
            NodeAgentConfig agentConfig = AMXUtil.getDomainConfig().getNodeAgentConfigMap().get(ndAgentName);
            Map<String, String> props = agentConfig.getProperties();
            String agentStatus = agentConfig.getPropertyValue("rendezvousOccurred");
            String hostName = GuiUtil.getMessage("nodeAgent.UnknownHost");
            String status = null;
            if(agentStatus.equalsIgnoreCase("true")){
                hostName = agentConfig.getJMXConnectorConfig().getPropertyValue("client-hostname");
                connected = true;
                status = getStatusString(agentConfig.getName());
            }else{
                status = getWarningString(GuiUtil.getMessage("nodeAgent.awaitingInitialSync"));
            }
            handlerCtx.setOutputValue("HostName", hostName);
            handlerCtx.setOutputValue("Status", status);
            handlerCtx.setOutputValue("OnInstances", agentConfig.getStartServersInStartup());
            handlerCtx.setOutputValue("Connected", connected);
            handlerCtx.setOutputValue("Properties", props);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns the list of ClusteredInstances for populating the table.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveNodeAgentValues",
           input={
            @HandlerInput(name="NodeAgentName", type=String.class, required=true),
            @HandlerInput(name="OnInstances",   type=Boolean.class, required=true),
            @HandlerInput(name="AddProps", type=Map.class),  
            @HandlerInput(name="RemoveProps", type=ArrayList.class) }
     )
     public static void saveNodeAgentValues(HandlerContext handlerCtx){
        try{
            String ndAgentName = (String) handlerCtx.getInputValue("NodeAgentName");
            boolean onInstances = ((Boolean) handlerCtx.getInputValue("OnInstances")).booleanValue();
            NodeAgentConfig agentConfig = AMXUtil.getDomainConfig().getNodeAgentConfigMap().get(ndAgentName);
            ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
            Map addProps = (Map)handlerCtx.getInputValue("AddProps");
            String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
            for(int i=0; i<remove.length; i++){
                agentConfig.removeProperty(remove[i]);
            }
            if(addProps != null ){
                Iterator additer = addProps.keySet().iterator();
                while(additer.hasNext()){
                    Object key = additer.next();
                    String addvalue = (String)addProps.get(key);
                    agentConfig.setPropertyValue((String)key, addvalue);
                    
                }
            }            
            agentConfig.setStartServersInStartup(onInstances);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the list of Thread Pools for populating 
     *  <p> the table in Thread Pools top level page
     *  <p> Input  value: "ConfigName"   -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "selectedRows" -- Type: <code> java.util.List</code></p>
     *  <p> Output  value: "Result"      -- Type: <code> java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getInstancesList",
        input={
            @HandlerInput(name="NodeAgentName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="Result", type=java.util.List.class)}
     )
    public static void getInstancesList(HandlerContext handlerCtx){
        String ndAgentName = (String) handlerCtx.getInputValue("NodeAgentName");
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
       
        List result = new ArrayList();
        if(ndAgentName != null){
            Map<String, ServerConfig> nodesInstances = getNodeAgentsInstances(ndAgentName);
            Map <String, NodeAgentConfig> nodeAgentMap = AMXUtil.getDomainConfig().getNodeAgentConfigMap();
            NodeAgentConfig agentConfig = nodeAgentMap.get(ndAgentName);
            if(nodesInstances != null) {
                for(String key : nodesInstances.keySet()){
                    HashMap oneRow = new HashMap();
                    ServerConfig serverConfig = nodesInstances.get(key);
                    String name = serverConfig.getName();
                    if(serverConfig instanceof ClusteredServerConfig){
                        ClusteredServerConfig conf = (ClusteredServerConfig)serverConfig;
                        String clusterName = EESupportHandlers.getClusterForServer(name);
                        oneRow.put("clusterName", clusterName);
                        oneRow.put("isCluster", true);
                    } else {
                        oneRow.put("clusterName", "");
                        oneRow.put("isCluster", false);
                    }
                    String configuration = serverConfig.getReferencedConfigName();
                    String status = agentConfig.getPropertyValue("rendezvousOccurred");
                    if (status.equalsIgnoreCase("false")) {
                        status = GuiUtil.getMessage("nodeAgent.notRunning");
                    }
                    status = AMXUtil.getStatusForDisplay(
                        AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name), true);
                    oneRow.put("name", name);
                    oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
                    oneRow.put("config", (configuration == null) ? " ": configuration);
                    oneRow.put("status", (status == null) ? " ": status);
                    result.add(oneRow);
                }
            }
        }       
        handlerCtx.setOutputValue("Result", result);
    }
   
    
    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Logger Tab of the Node Agent tab </p>
     *	<p> Input value: "NodeAgentName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "LogFile"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Alarms"             -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "SystemLog"          -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "LogHandler"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogFilter"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RotationLimit"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RotationTimeLimit"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RetainErrorStats"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getNodeAgentLoggerSettings",
    input={
        @HandlerInput(name="NodeAgentName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="LogFile",            type=String.class),
        @HandlerOutput(name="Alarms",             type=Boolean.class),
        @HandlerOutput(name="SystemLog",          type=Boolean.class),
        @HandlerOutput(name="LogHandler",         type=String.class),
        @HandlerOutput(name="LogFilter",          type=String.class),
        @HandlerOutput(name="RotationLimit",      type=String.class),
        @HandlerOutput(name="RotationTimeLimit",  type=String.class),
        @HandlerOutput(name="RetainErrorStats",   type=String.class),
        @HandlerOutput(name="Properties",         type=Map.class) })
        public static void getNodeAgentLoggerSettings(HandlerContext handlerCtx) {
        String ndAgentName = (String) handlerCtx.getInputValue("NodeAgentName");
        Map <String, NodeAgentConfig> nodeAgentMap = AMXUtil.getDomainConfig().getNodeAgentConfigMap();
        NodeAgentConfig agentConfig = nodeAgentMap.get(ndAgentName);
        
        LogServiceConfig lc = agentConfig.getContainee(LogServiceConfig.J2EE_TYPE);
        String logFile = lc.getFile();
        boolean alarms = lc.getAlarms();
        boolean systemLog = lc.getUseSystemLogging();        
        String logHandler = lc.getLogHandler();
        String logFilter = lc.getLogFilter();
        String rotationLimit = lc.getLogRotationLimitInBytes();
        String rotationTimeLimit = lc.getLogRotationTimeLimitInMinutes();
        String retainErrorStats = lc.getRetainErrorStatisticsForHours();
        Map<String, String> props = lc.getProperties();
        handlerCtx.setOutputValue("LogFile", logFile);
        handlerCtx.setOutputValue("Alarms", alarms);
        handlerCtx.setOutputValue("SystemLog", systemLog);
        handlerCtx.setOutputValue("LogHandler", logHandler);
        handlerCtx.setOutputValue("LogFilter", logFilter);
        handlerCtx.setOutputValue("RotationLimit", rotationLimit);
        handlerCtx.setOutputValue("RotationTimeLimit", rotationTimeLimit);
        handlerCtx.setOutputValue("RetainErrorStats", retainErrorStats);
        handlerCtx.setOutputValue("Properties", props);
    }
    
    /**
     *	<p> This handler saves the values for all the attributes in the
     *      Logger Tab of the Node Agent tab </p>
     *	<p> Input value: "NodeAgentName"     -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "LogFile"           -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Alarms"            -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "SystemLog"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "LogHandler"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "LogFilter"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RotationLimit"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RotationTimeLimit" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RetainErrorStats"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"          -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"       -- Type: <code>java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveNodeAgentLoggerSettings",
    input={
        @HandlerInput(name="NodeAgentName",     type=String.class, required=true),
        @HandlerInput(name="LogFile",           type=String.class),
        @HandlerInput(name="Alarms",            type=Boolean.class),
        @HandlerInput(name="SystemLog",         type=Boolean.class),
        @HandlerInput(name="LogHandler",        type=String.class),
        @HandlerInput(name="LogFilter",         type=String.class),
        @HandlerInput(name="RotationLimit",     type=String.class),
        @HandlerInput(name="RotationTimeLimit", type=String.class),
        @HandlerInput(name="RetainErrorStats",  type=String.class),
        @HandlerInput(name="AddProps",          type=Map.class),
        @HandlerInput(name="RemoveProps",       type=ArrayList.class)})
        public static void saveNodeAgentLoggerSettings(HandlerContext handlerCtx) {
        String ndAgentName = (String) handlerCtx.getInputValue("NodeAgentName");
        Map <String, NodeAgentConfig> nodeAgentMap = AMXUtil.getDomainConfig().getNodeAgentConfigMap();
        NodeAgentConfig agentConfig = nodeAgentMap.get(ndAgentName);
        LogServiceConfig lc = agentConfig.getContainee(LogServiceConfig.J2EE_TYPE);
        try{
        AMXUtil.editProperties(handlerCtx, lc);         
        lc.setFile((String)handlerCtx.getInputValue("LogFile"));
        lc.setAlarms(((Boolean)handlerCtx.getInputValue("Alarms")).booleanValue());
        lc.setUseSystemLogging(((Boolean)handlerCtx.getInputValue("SystemLog")).booleanValue());
        lc.setLogHandler((String)handlerCtx.getInputValue("LogHandler"));
        lc.setLogFilter((String)handlerCtx.getInputValue("LogFilter"));
        lc.setLogRotationLimitInBytes((String)handlerCtx.getInputValue("RotationLimit"));
        lc.setLogRotationTimeLimitInMinutes((String)handlerCtx.getInputValue("RotationTimeLimit"));
        lc.setRetainErrorStatisticsForHours((String)handlerCtx.getInputValue("RetainErrorStats"));  
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
     
    
    /**
     *	<p> This handler creates a placeholder for a Node Agent <p>
     *	<p> Input value: "NodeAgentName"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="createNodeAgent",
    input={
        @HandlerInput(name="NodeAgentName",     type=String.class, required=true),        
        @HandlerInput(name="AddProps", type=Map.class)})
    public static void createNodeAgent(HandlerContext handlerCtx){
        try{
            String nodeAgentName = (String) handlerCtx.getInputValue("NodeAgentName");
            Map addProps = (Map)handlerCtx.getInputValue("AddProps");            
            String operName = "createNodeAgentConfig";
            String[] signature = {"java.lang.String"};
            Object[] params = {nodeAgentName};
            JMXUtil.invoke(NODE_AGENT_OBJNAME, operName, params, signature);       
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler adds properties for a Node Agent <p>
     *	<p> Input value: "NodeAgentName"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="addNodeAgentProperties",
    input={
        @HandlerInput(name="NodeAgentName",     type=String.class, required=true),        
        @HandlerInput(name="AddProps", type=Map.class)})
    public static void addNodeAgentProperties(HandlerContext handlerCtx){
        try{
        String nodeAgentName = (String) handlerCtx.getInputValue("NodeAgentName");
        Map addProps = (Map)handlerCtx.getInputValue("AddProps");   
        String[] type = {"javax.management.Attribute"};
           if(addProps != null ){
                Iterator additer = addProps.keySet().iterator();
                while(additer.hasNext()){
                    Object key = additer.next();
                    String addvalue = (String)addProps.get(key);
                    Attribute attr = new Attribute((String)key, addvalue);
                    Object[] params = new Object[]{attr};
                    JMXUtil.invoke("com.sun.appserv:type=node-agent,name="+nodeAgentName+",category=config", "setProperty", params, type);                    
                }
            }             
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }   
    }    
    
    /**
     * <p> This method displays the save successful message when the page refresh.
     * @param context The HandlerContext.
     */
   @Handler(id="getNodeAgentCreatedMsg",
   input={
        @HandlerInput(name="NodeAgentName",     type=String.class, required=true) } )
    public static void prepareSuccessful(HandlerContext handlerCtx){
        Object msgArgs[] = {handlerCtx.getInputValue("NodeAgentName")};
        GuiUtil.prepareAlert(handlerCtx, "information", GuiUtil.getMessage("nodeAgent.nodeAgentCreatedSummary", msgArgs), GuiUtil.getMessage("nodeAgent.nodeAgentCreatedDetail"));
    }
   
    /**
     *	<p> This handler starts/stops all selected server instances of the Node Agent
     *  <p> Input  value: "selectedRows" -- Type: <code> java.util.List</code></p>
     *  <p> Input  value: "start"        -- Type: <code> java.lang.Boolean</code></p>
     */
    @Handler(id="startStopInstances",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="start",        type=Boolean.class, required=true) })
    public static void startServerInstance(HandlerContext handlerCtx) {
        boolean start = ((Boolean) handlerCtx.getInputValue("start")).booleanValue();
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                J2EEServer instance = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name);
                if(start){
                    instance.start(); 
                }else{
                    instance.stop();
                }    
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    } 
     
    public static String getNodeAgentStatus(String nodeAgentName){
        NodeAgentConfig agentConfig = AMXUtil.getDomainConfig().getNodeAgentConfigMap().get(nodeAgentName);
        String agentStatus = agentConfig.getPropertyValue("rendezvousOccurred");
        String status = getWarningString(GuiUtil.getMessage("nodeAgent.awaitingInitialSync"));
        if(agentStatus.equalsIgnoreCase("true")){
            status = getStatusString(nodeAgentName);
        }
        return status;
    }
    
    private static String getStatusString(String nodeAgentName) {
        String status = null;
        try{
            String objName = "com.sun.appserv:type=node-agent,name=" + nodeAgentName + ",category=config";
            RuntimeStatus sts = (RuntimeStatus)JMXUtil.invoke(objName, "getRuntimeStatus", null, null);
            if (sts == null) {
                status = getWarningString(GuiUtil.getMessage("nodeAgent.unknownState"));
            }else if (sts instanceof RuntimeStatus) {
                status = JMXUtil.getStatusForDisplay(objName);
            }
        }catch(Exception ex){ }
        return status;
    }
    
    
    
    private static String getWarningString(String msg){
        return GuiUtil.getMessage("common.warningImage") + "&nbsp;" + msg;
    }
    
    private static String NODE_AGENT_OBJNAME = "com.sun.appserv:type=node-agents,category=config";
}

