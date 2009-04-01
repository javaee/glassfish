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
package org.glassfish.admingui.handlers;

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

import javax.faces.model.SelectItem;


import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.GuiUtil;

import com.sun.webui.jsf.component.TableRowGroup;

import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.NodeAgentConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.SystemPropertiesAccess;

import com.sun.appserv.management.config.SystemPropertyConfig;
import com.sun.appserv.management.j2ee.J2EEServer;
import java.util.Collection;
import org.glassfish.admingui.common.util.AMXUtil;


/**
 *
 * @author Administrator
 */
public class StandAloneInstanceHandlers{

   /**
     *	<p> This handler returns the list of StandaloneInstances for populating the table.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getStandaloneInstances",
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getStandaloneInstances(HandlerContext handlerCtx){
        
        Iterator iter = AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().values().iterator();
        
        List result = new ArrayList();
        if (iter != null){
            while(iter.hasNext()){
                StandaloneServerConfig server = (StandaloneServerConfig) iter.next();
                HashMap oneRow = new HashMap();
                String name = server.getName();
                String config = server.getConfigRef();
                String node = server.getNodeAgentRef();
                String weight = server.getLBWeight();
                // TODO-V3
                //String status = JMXUtil.getStatusForDisplay("com.sun.appserv:type=server,name="+name+",category=config");
                oneRow.put("name", (name == null) ? " ": name);
                oneRow.put("selected", false);
                oneRow.put("config", (config == null) ? " ": config);
                oneRow.put("node", (node == null) ? " ": node);
                oneRow.put("weight", (weight == null) ? " ": weight);
                // TODO-V3
                oneRow.put("weight", " ");
                //oneRow.put("status", (status == null) ? " ": status);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("result", result);
    }
    
    

    /**
     * <p> This handler returns the node agent names of the domain </p>
     *
     * <p> Output value: "NodeAgents" -- Type: <code>java.util.SelectItem</code>
     * @param  handlerCtx The HandlerContext.
     */
    @Handler(id="getNodeAgentsForDropdown",
        output={
            @HandlerOutput(name="NodeAgents", type=SelectItem[].class)})
    public static void getNodeAgentsForDropdown(HandlerContext handlerCtx) {
        Map<String,NodeAgentConfig> nodeAgentMap = 
            (Map)AMXRoot.getInstance().getDomainConfig().getNodeAgentsConfig().getNodeAgentConfigMap();
	String[] nodeAgents = nodeAgentMap == null ?  null : 		(String[])nodeAgentMap.keySet().toArray(new String[nodeAgentMap.size()]);
	/* TODO-V3
        SelectItem[] agents = ConfigurationHandlers.getOptions(nodeAgents);
         
 	handlerCtx.setOutputValue("NodeAgents", agents);
         */
        handlerCtx.setOutputValue("NodeAgents", " ");
    }

    /**
     * <p> This handler returns the config names of the domain </p>
     *
     * <p> Output value: "Configs" -- Type: <code>java.util.SelectItem</code>
     * @param  handlerCtx The HandlerContext.
     */
    @Handler(id="getConfigsForDropdown",
        output={
            @HandlerOutput(name="Configs", type=SelectItem[].class)})
    public static void getConfigsForDropdown(HandlerContext handlerCtx) {
        Map<String,ConfigConfig> configMap = 
            (Map)AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap();
	String[] values = configMap == null ?  null : (String[])configMap.keySet().toArray(new String[configMap.size()]);
        ArrayList newConfigs = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            if(!values[i].equals("server-config")) {
                newConfigs.add(values[i]);
            }
	}
        String[] configNames = (String[])newConfigs.toArray(new String[0]);
        /* TODO-V3
	SelectItem[] configs = ConfigurationHandlers.getOptions(configNames);
 	handlerCtx.setOutputValue("Configs", configs);
         */
        handlerCtx.setOutputValue("Configs", " ");
    }


    /**
     *	<p> This handler creates standalone instances
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="createStandaloneInstance",
    input={
        @HandlerInput(name="Name",       type=String.class, required=true),
        @HandlerInput(name="NodeAgent",        type=String.class, required=true),
        @HandlerInput(name="Config",  type=String.class),
        @HandlerInput(name="CopyConfig",     type=String.class)})
        public static void createStandaloneInstance(HandlerContext handlerCtx) {
        String name = (String)handlerCtx.getInputValue("Name");
        String nodeAgent = (String)handlerCtx.getInputValue("NodeAgent");
        String configName = (String)handlerCtx.getInputValue("Config");
        String copyFlag = (String)handlerCtx.getInputValue("CopyConfig");
        String[] signature = {
            "java.lang.String",
            "java.lang.String",
            "java.lang.String",
            "java.lang.String",
            "java.util.Properties"
        };
        
        boolean configCopied = false;
        /* TODO-V3
        try {
            if ("copy".equals(copyFlag)){
                configCopied = true;
                HashMap configMap = new HashMap();
                if("default-config".equals(configName)) {
                    AMXRoot.getInstance().getConfigsConfig().createConfigConfig(name+"-config", configMap);
                    
                } else {
                    configMap.put(ConfigConfigKeys.SRC_CONFIG_NAME_KEY, configName);
                    AMXRoot.getInstance().getConfigsConfig().createConfigConfig(name+"-config", configMap);
                }
                
            }
            
            if(configCopied)
                configName = name + "-config";
            
                Object[] params = {
                    nodeAgent,
                    name,
                    configName,
                    null,
                    null
                };
                //JMX throws PortReplaceException which we can catch and ignore. AMX throws MBeanException for ALL exceptions
                //and we can't ignore all exceptions 
                // AMXUtil.getServersConfig().createStandaloneServerConfig(name, nodeAgent, configName, new HashMap());
                JMXUtil.getMBeanServer().invoke(new ObjectName("com.sun.appserv:type=servers,category=config"), "createServerInstance", params, signature);
                
        }catch (Exception ex){
            if (ex.getCause() instanceof PortReplacedException){
                // indicates that createInstance succeeded but with a "WARNING indicating that the user provided
                // or default ports were overridden.
                // ignored.
            }else{
                GuiUtil.handleException(handlerCtx, ex);
            }
            
        }
         */
    }

    
     /**
     *	<p> This handler takes in selected rows, and removes selected instance
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="deleteStandaloneInstance",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true)}
    )
    public static void deleteStandaloneInstance(HandlerContext handlerCtx) {

        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                AMXRoot.getInstance().getServersConfig().removeStandaloneServerConfig(name);                
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }   
    
   /**
     *	<p> This handler stops the selected instance specified by the name 
     */
    @Handler(id="stopSelectedInstance",
        input={
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
        )

    public static void stopSelectedInstance(HandlerContext handlerCtx) {
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        /* TODO-V3
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                JMXUtil.stopServerInstance(name);
                //AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(name).stop();              
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }        
         */
    }
    
  /**
     *	<p> This handler starts the selected instance specified by the name 
     */
    @Handler(id="startSelectedInstance",
        input={
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
        )

    public static void startSelectedInstance(HandlerContext handlerCtx) {
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        /* TODO-V3
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                JMXUtil.startServerInstance(name);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }        
         */
    }   
    
    /**
     *	<p> This handler starts the instance specified by the name 
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     */
    @Handler(id="startServerInstance",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true)}
        )
    
    public static void startServerInstance(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        /* TODO-V3
        try{
            JMXUtil.startServerInstance(instanceName);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }         
         */   
    } 
    
   /**
     *	<p> This handler stops the instance specified by the name 
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     */
    @Handler(id="stopServerInstance",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true)}
        )
    
    public static void stopServerInstance(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        /* TODO-V3
        try{
            JMXUtil.stopServerInstance(instanceName);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }         
         */   
    }     
    
  /**
     *	<p> This handler checks to see if server instance is DAS 
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     */
    @Handler(id="isAdminServer",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="isAdminServer", type=Boolean.class)})
    public static void isAdminServer(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        try{
            boolean isAdmin = false;
            if ("server".equals(instanceName)){
                isAdmin = true;
            }
            handlerCtx.setOutputValue("isAdminServer", isAdmin);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }            
    }         
    
       /**
     *	<p> This handler saves the weight for the standalone instance 
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     */
    @Handler(id="saveStandaloneInstanceWeight",
        input={
            @HandlerInput(name="TableRowGroup", type=TableRowGroup.class, required=true)}
        )
    
    public static void saveStandaloneInstanceWeight(HandlerContext handlerCtx) {
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
                     AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().get(name).setLBWeight(weight);
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
     *	<p> This handler returns the values for all the attributes in the
     *      PE Server Instance General Page.</p>
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     *	<p> Output value: "hostName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "httpPorts" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "iiopPorts" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "version" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "configDir" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "debugPort" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getStandaloneInstanceGeneralAttributes",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true),
            @HandlerInput(name="isAdminServer", type=Boolean.class, required=true)},
        output={
            @HandlerOutput(name="httpPorts", type=String.class),
            @HandlerOutput(name="hostName", type=String.class),
            @HandlerOutput(name="iiopPorts", type=String.class),
            @HandlerOutput(name="version", type=String.class),
            @HandlerOutput(name="configDir", type=String.class),
            @HandlerOutput(name="debugPort", type=String.class),
            @HandlerOutput(name="status",     type=String.class),
            @HandlerOutput(name="nodeAgent",     type=String.class),
            @HandlerOutput(name="config",     type=String.class),
            @HandlerOutput(name="running",     type=Boolean.class),
            @HandlerOutput(name="nodeAgentStatus",     type=String.class)})

        public static void getStandaloneInstanceGeneralAttributes(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        Boolean isAdminServer = (Boolean) handlerCtx.getInputValue("isAdminServer");
        try {
            StandaloneServerConfig server = AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().get(instanceName);
            J2EEServer j2eeServer = AMXRoot.getInstance().getJ2EEDomain().getJ2EEServerMap().get(instanceName);
            String config = server.getConfigRef();
            String version = j2eeServer.getserverVersion();
            String sts = null;
            String node = null;
            //nodeagent name and status
            /* TODO-V3
            if(!isAdminServer.booleanValue()) {
                node = server.getReferencedNodeAgentName();
                sts = NodeAgentHandlers.getNodeAgentStatus(node);
            }
             */
            
            
            getInstanceGeneralAttributes(handlerCtx, instanceName, config, version, node, sts);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
       public static void getInstanceGeneralAttributes(HandlerContext handlerCtx, String instanceName,
               String config, String version, String node, String sts){ 
         //http ports
        HTTPServiceConfig service = AMXRoot.getInstance().getConfig(config).getHTTPServiceConfig();
        Map<String,HTTPListenerConfig>listeners = service.getHTTPListenerConfigMap();
        StringBuffer ports = new StringBuffer();
        for(String key : listeners.keySet()){
            String port = listeners.get(key).getPort();
            if (port.startsWith("$")){
                port = resolveToken( (port.substring(2, port.length()-1) ), config, instanceName);
            }
            ports.append(","+port);
        }
        ports.deleteCharAt(0);  //remove the first ','
        handlerCtx.setOutputValue("httpPorts", ports.toString());
        
        
        //iiop ports
        IIOPServiceConfig iiopService = AMXRoot.getInstance().getConfig(config).getIIOPServiceConfig();
        Map<String,IIOPListenerConfig> iiopListeners = iiopService.getIIOPListenerConfigMap();
        StringBuffer iports = new StringBuffer();
        for(String key : iiopListeners.keySet()){
            String iport = iiopListeners.get(key).getPort();
            if (iport.startsWith("$")){
                iport = resolveToken( (iport.substring(2, iport.length()-1) ), config, instanceName);
            }
            iports.append(","+iport);
        }
        iports.deleteCharAt(0);  //remove the first ','
        handlerCtx.setOutputValue("iiopPorts", iports.toString());
        
        Object debugPort = null;
        /* TODO-V3
        //debug port; can't get the runtim info of whether debug is on through AMX
        Object debugPort = JMXUtil.getAttribute("com.sun.appserv:j2eeType=J2EEServer,name="+instanceName+",category=runtime", "debugPort");
         */
        String msg = GuiUtil.getMessage("inst.notEnabled");
        if (debugPort != null) {
            String port = debugPort.toString();
            if (port.equals("0") == false) {
                msg = GuiUtil.getMessage("inst.debugEnabled") + debugPort.toString();
            }
        }         
        String configDir = null;
        /* TODO-V3
        //ConfigDir can't get through AMX
        String configDir = (String)JMXUtil.invoke("com.sun.appserv:type=domain,category=config", "getConfigDir", null, null);
         */
        
        handlerCtx.setOutputValue("configDir", configDir);
        handlerCtx.setOutputValue("nodeAgent", node);
        handlerCtx.setOutputValue("config", config);
        handlerCtx.setOutputValue("version", version);
        //String status = AMXUtil.getStatusForDisplay(
        //                AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(instanceName), true);
        String status = null;
        /* TODO-V3
        String status = JMXUtil.getStatusForDisplay("com.sun.appserv:type=server,name="+instanceName+",category=config");
         */
        handlerCtx.setOutputValue("status", status);       
        handlerCtx.setOutputValue("nodeAgentStatus", sts);
        handlerCtx.setOutputValue("debugPort", msg);
        /* TODO-V3
	RuntimeStatus rsts = JMXUtil.getRuntimeStatus(instanceName);
        int state = JMXUtil.getRuntimeStatusCode(rsts);         
        handlerCtx.setOutputValue("running", (state == Status.kInstanceRunningCode) ? true : false);
         */
        handlerCtx.setOutputValue("running", false);
        /* TODO-V3
	get host name
        */
        handlerCtx.setOutputValue("hostName", " ");
    }     
     
    
    private static String resolveToken(String pn, String configName, String instanceName) {
        //For EE, the instance will have its own override System Properties value instead of using the one from config.
        if (AMXRoot.getInstance().isEE()){
            SystemPropertiesAccess sprops = AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().get(instanceName);
            if (sprops == null){
                sprops = AMXRoot.getInstance().getServersConfig().getClusteredServerConfigMap().get(instanceName);
            }
            if (sprops != null){
                if (sprops.getSystemPropertyConfigMap().containsKey(pn)){
                    return sprops.getSystemPropertyConfigMap().get(pn).getValue();
                }
            }
        }
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
        return config.getSystemPropertyConfigMap().get(pn).getValue();
    }
    
       /**
     *	<p> This handler saves the config props for the standalone instance 
     *  <p> Input  value: "TableRowGroup" -- Type: <code> TableRowGroup</code></p>
     */
    @Handler(id="saveStandaloneInstanceConfigProps",
        input={
            @HandlerInput(name="TableRowGroup", type=TableRowGroup.class, required=true),
            @HandlerInput(name="InstanceName",      type=String.class, required=true)}
        )
    
    public static void saveStandaloneInstanceConfigProps(HandlerContext handlerCtx) {
        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("TableRowGroup");
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        AMXRoot amxRoot = AMXRoot.getInstance();
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
                     amxRoot.getServersConfig().getStandaloneServerConfigMap().get(instanceName).getSystemPropertyConfigMap().get(name).setValue(override);
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
     *	<p> This handler saves the props for Standalone Instances</p>
      *	<p> Input value: "InstanceName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"     -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"     -- Type: <code>java.util.ArrayList</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="saveStandaloneInstanceProperties",
    input={
        @HandlerInput(name="InstanceName",      type=String.class, required=true),        
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class)})    
    public static void saveStandaloneInstanceProperties(HandlerContext handlerCtx) {
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        AMXRoot amxRoot = AMXRoot.getInstance();
        StandaloneServerConfig serverConfig = amxRoot.getServersConfig().getStandaloneServerConfigMap().get(instanceName);
        ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
        Map addProps = (Map)handlerCtx.getInputValue("AddProps");
        String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
        for(int i=0; i<remove.length; i++){
            serverConfig.removePropertyConfig(remove[i]);
        }
        if(addProps != null ){
            Iterator additer = addProps.keySet().iterator();
            while(additer.hasNext()){
                Object key = additer.next();
                String addvalue = (String)addProps.get(key);
                AMXUtil.setPropertyValue(serverConfig,(String)key, addvalue);
                
            }
        }      
    }
    
    /**
     *	<p> This handler returns the values for all the config properties in the
     *      Standalone Instance Config Properties Page.</p>
     *  <p> Input  value: "instanceName" -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "result" -- Type: <code>java.util.List</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getStandaloneInstanceConfigProperties",
        input={
            @HandlerInput(name="instanceName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)})

        public static void getStandaloneInstanceConfigProperties(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        AMXRoot amxRoot = AMXRoot.getInstance();
        try {
            StandaloneServerConfig serverConfig = amxRoot.getServersConfig().getStandaloneServerConfigMap().get(instanceName);
            ConfigConfig defaultConfig = amxRoot.getConfig("default-config");
            Collection<SystemPropertyConfig> systemPropConfigs = serverConfig.getSystemPropertyConfigMap().values();
            List result = new ArrayList();
             for(SystemPropertyConfig spc : systemPropConfigs){
               HashMap oneRow = new HashMap();
               String name = spc.getName();
               oneRow.put("name",name);
               oneRow.put("override", spc.getValue());
               SystemPropertyConfig  defaultProp = defaultConfig.getSystemPropertyConfigMap().get(name);
               oneRow.put("default", (defaultProp == null)? "" : defaultProp.getValue());
               result.add(oneRow);
            }
            handlerCtx.setOutputValue("result", result);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }       
    
   /**
     *	<p> This method returns the properties for Standalone Instances </p>
     *
     *  <p> Output value: "Properties" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getStandaloneInstanceProperties",
    input={
        @HandlerInput(name="InstanceName", type=String.class, required=true)},
    output={
        @HandlerOutput(name="propConfig", type=Map.class)})
        public static void getStandaloneInstanceProperties(HandlerContext handlerCtx) {
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        StandaloneServerConfig serverConfig = AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().get(instanceName);
        handlerCtx.setOutputValue("propConfig", serverConfig.getPropertyConfigMap());
        
    }   
}
