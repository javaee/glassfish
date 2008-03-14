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
 * ConfigurationHandlers.java
 *
 * Created on August 12, 2006, 7:04 PM
 *
 */
package com.sun.enterprise.tools.admingui.handlers;


import java.util.Map;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.File;

import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.OptionGroup;
import com.sun.webui.jsf.model.OptionTitle;
  
import javax.faces.context.ExternalContext;
import javax.faces.model.SelectItem;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  


import com.sun.enterprise.tools.admingui.util.AMXUtil; 
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;

import com.sun.appserv.management.config.AvailabilityServiceConfig;
import com.sun.appserv.management.config.EJBContainerAvailabilityConfig;
import com.sun.appserv.management.config.JMSAvailabilityConfig;
import com.sun.appserv.management.config.WebContainerAvailabilityConfig;
import com.sun.appserv.management.config.ConfigConfig; 
import com.sun.appserv.management.config.ConnectorServiceConfig;
import com.sun.appserv.management.config.DiagnosticServiceConfig; 
import com.sun.appserv.management.config.SecurityServiceConfig;
import com.sun.appserv.management.config.JACCProviderConfig;
import com.sun.appserv.management.config.AuditModuleConfig;
import com.sun.appserv.management.config.TransactionServiceConfig;
import com.sun.appserv.management.config.JMSHostConfig;
import com.sun.appserv.management.config.JMSHostConfigKeys;
import com.sun.appserv.management.config.JMSServiceConfig;
import com.sun.appserv.management.config.MonitoringServiceConfig;
import com.sun.appserv.management.config.ModuleMonitoringLevelsConfig;
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.AccessLogConfig;
import com.sun.appserv.management.config.AccessLogConfigKeys;
import com.sun.appserv.management.config.RequestProcessingConfig;
import com.sun.appserv.management.config.RequestProcessingConfigKeys;
import com.sun.appserv.management.config.KeepAliveConfig;
import com.sun.appserv.management.config.KeepAliveConfigKeys;
import com.sun.appserv.management.config.ConnectionPoolConfig;
import com.sun.appserv.management.config.ConnectionPoolConfigKeys;
import com.sun.appserv.management.config.HTTPAccessLogConfig;
import com.sun.appserv.management.config.HTTPProtocolConfig;
import com.sun.appserv.management.config.HTTPProtocolConfigKeys;
import com.sun.appserv.management.config.HTTPFileCacheConfig;
import com.sun.appserv.management.config.HTTPFileCacheConfigKeys;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.ConfigElement;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.WebModuleConfig;
import com.sun.appserv.management.config.AuthRealmConfig;
import com.sun.appserv.management.config.EventConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.LogLevelValues;
import com.sun.appserv.management.config.ManagementRuleConfig;
import com.sun.appserv.management.config.ManagementRulesConfig;
import com.sun.appserv.management.config.MessageSecurityConfig;
import com.sun.appserv.management.config.ORBConfig;
import com.sun.appserv.management.config.ProviderConfig;
import com.sun.appserv.management.config.RequestPolicyConfig;
import com.sun.appserv.management.config.ResponsePolicyConfig;
import com.sun.appserv.management.config.ThreadPoolConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.enterprise.ee.web.sessmgmt.EEPersistenceTypeResolver;



/**
 *
 * @author Nitya Doraisamy
 */
public class ConfigurationHandlers {
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      Diagnostic Service </p>
     *	<p> Output value: "CheckSum"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "VerifyConfig"   -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "InstallLog"     -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "SysInfo"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "AppDeployDesc"  -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "LogLevel"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogEntries"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDiagnosticsSettings",
    input={
        @HandlerInput(name="configName", type=String.class, required=true)
    },
    output={
        @HandlerOutput(name="CheckSum",      type=Boolean.class),
        @HandlerOutput(name="VerifyConfig",  type=Boolean.class),
        @HandlerOutput(name="InstallLog",    type=Boolean.class),
        @HandlerOutput(name="SysInfo",       type=Boolean.class),
        @HandlerOutput(name="AppDeployDesc", type=Boolean.class),
        @HandlerOutput(name="LogLevel",      type=String.class),
        @HandlerOutput(name="LogEntries",    type=String.class)}
    )
        
        public static void getDiagnosticsSettings(HandlerContext handlerCtx) {
        
        String configName = (String) handlerCtx.getInputValue("configName");
        DiagnosticServiceConfig dgService = AMXUtil.getConfig(configName).getDiagnosticServiceConfig();
        boolean checkSum = dgService.getComputeChecksum();
        boolean verifyConfig = dgService.getVerifyConfig();
        boolean installLog = dgService.getCaptureInstallLog();
        boolean sysInfo = dgService.getCaptureSystemInfo();
        boolean appDeplDesc = dgService.getCaptureAppDD();
        String logLevel = dgService.getMinLogLevel();
        String logEntries = dgService.getMaxLogEntries();
        handlerCtx.setOutputValue("CheckSum", checkSum);
        handlerCtx.setOutputValue("VerifyConfig", verifyConfig);
        handlerCtx.setOutputValue("InstallLog", installLog);
        handlerCtx.setOutputValue("SysInfo", sysInfo);
        handlerCtx.setOutputValue("AppDeployDesc", appDeplDesc);
        handlerCtx.setOutputValue("LogLevel", logLevel);
        handlerCtx.setOutputValue("LogEntries", logEntries);
        
    }
    
   /**
     *	<p> This handler saves the values for Diagnostic Service </p>
      *	<p> Output value: "CheckSum"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "VerifyConfig"   -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "InstallLog"     -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "SysInfo"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "AppDeployDesc"  -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "LogLevel"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogEntries"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveDiagnosticsSettings",
    input={
        @HandlerInput(name="configName",      type=String.class, required=true),
        @HandlerInput(name="CheckSum",      type=Boolean.class),
        @HandlerInput(name="VerifyConfig",  type=Boolean.class),
        @HandlerInput(name="InstallLog",    type=Boolean.class),
        @HandlerInput(name="SysInfo",       type=Boolean.class),
        @HandlerInput(name="AppDeployDesc", type=Boolean.class),
        @HandlerInput(name="LogLevel",      type=String.class),
        @HandlerInput(name="LogEntries",    type=String.class)})
        
        
    public static void saveDiagnosticsSettings(HandlerContext handlerCtx) {
        
        String configName = (String) handlerCtx.getInputValue("configName");
        try{
            DiagnosticServiceConfig dgService = AMXUtil.getConfig(configName).getDiagnosticServiceConfig();
            dgService.setComputeChecksum(((Boolean)handlerCtx.getInputValue("CheckSum")).booleanValue());
            dgService.setVerifyConfig(((Boolean)handlerCtx.getInputValue("VerifyConfig")).booleanValue());
            dgService.setCaptureInstallLog(((Boolean)handlerCtx.getInputValue("InstallLog")).booleanValue());
            dgService.setCaptureSystemInfo(((Boolean)handlerCtx.getInputValue("SysInfo")).booleanValue());
            dgService.setCaptureAppDD(((Boolean)handlerCtx.getInputValue("AppDeployDesc")).booleanValue());
            dgService.setMinLogLevel((String)handlerCtx.getInputValue("LogLevel"));
            dgService.setMaxLogEntries((String)handlerCtx.getInputValue("LogEntries"));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
    /**
     *	<p> This handler returns the default value for Connector Service </p>
     *	<p> Output value: "ShutdownTimeout"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getConnectorServiceDefaults",
    output={
        @HandlerOutput(name="ShutdownTimeout",   type=String.class)})
        
        public static void getConnectorServiceDefaults(HandlerContext handlerCtx) {
        String[] params = {"connector-service", null};
        String shutdTimeout = (String)getDefaultAttributeValue(params);
        if(shutdTimeout != null)
            handlerCtx.setOutputValue("ShutdownTimeout", shutdTimeout);
        
    }
      
    
    /**
     *	<p> This handler returns the value for Connector Service </p>
     *	<p> Output value: "ShutdownTimeout"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getConnectorServiceAttributes",
        input={@HandlerInput(name="cName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="ShutdownTimeout",   type=String.class)})
        
        public static void getConnectorServiceAttributes(HandlerContext handlerCtx) {
        
        String configName = (String) handlerCtx.getInputValue("cName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        ConnectorServiceConfig connService = config.getConnectorServiceConfig();
        if(connService != null){
            String shutdTimeout = connService.getShutdownTimeoutInSeconds();
            handlerCtx.setOutputValue("ShutdownTimeout", shutdTimeout);
        }
    }
    
    /**
     *	<p> This handler saves the values for Connector Service </p>
      *	<p> Output value: "ShutdownTimeout"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveConnectorServiceAttributes",
    input={
        @HandlerInput(name="cName", type=String.class, required=true),
        @HandlerInput(name="ShutdownTimeout",    type=String.class)})
        
    public static void saveConnectorServiceAttributes(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("cName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        try{
            ConnectorServiceConfig connService = config.getConnectorServiceConfig();
            if(connService != null){
                connService.setShutdownTimeoutInSeconds((String)handlerCtx.getInputValue("ShutdownTimeout"));
            }else{
                connService = config.createConnectorServiceConfig();
                connService.setShutdownTimeoutInSeconds((String)handlerCtx.getInputValue("ShutdownTimeout"));
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /*
     * Method that obtains the default values from domain
     * @param array containing the name of object whose default value is needed
     * @returns an Object representing the default values
     */
    private static Object getDefaultAttributeValue(String[] params){
        Object defaultValue = null;
        String[] signature = {"java.lang.String", "[Ljava.lang.String;"};
        AttributeList attrList = (AttributeList)JMXUtil.invoke("com.sun.appserv:type=domain,category=config", "getDefaultAttributeValues", params, signature);
        if(attrList != null){
            for(Iterator itr=attrList.iterator(); itr.hasNext();){
                Attribute attr = (Attribute)itr.next();
                defaultValue = attr.getValue();
            }
        }
        return defaultValue;
    }
    
    /**
     *	<p> This handler returns the values for JACC Providers
     *      in Security Page.</p>
     *  <p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jacc" -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJaccs",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },
    output={
        @HandlerOutput(name="Jacc", type=SelectItem[].class)})

        public static void getJaccs(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        Map<String,JACCProviderConfig>jacc = sConfig.getJACCProviderConfigMap();
        String[] jProvider = (String[])jacc.keySet().toArray(new String[jacc.size()]);
        SelectItem[] options = getOptions(jProvider);
	handlerCtx.setOutputValue("Jacc", options);
    }
    
      /**
     *	<p> This handler returns the values for JMS Host
     *      in Security Page.</p>
     *  <p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Hosts" -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJmsHosts",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },
    output={
        @HandlerOutput(name="Hosts", type=SelectItem[].class)})

        public static void getJmsHosts(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        JMSServiceConfig jConfig = config.getJMSServiceConfig();
        Map<String,JMSHostConfig>hosts = jConfig.getJMSHostConfigMap();
        String[] jHosts = (String[])hosts.keySet().toArray(new String[hosts.size()]);
        SelectItem[] options = getOptions(jHosts);
	handlerCtx.setOutputValue("Hosts", options);
    }
    
    
       /**
     *	<p> This handler returns the values for Audit Modules
     *      in Security Page.</p>
     *  <p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AuditModules" -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAuditModules",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },
    output={
        @HandlerOutput(name="AuditModules", type=SelectItem[].class)})

        public static void getAuditModules(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        Map<String,AuditModuleConfig>modules = sConfig.getAuditModuleConfigMap();
        String[] aModules = (String[])modules.keySet().toArray(new String[modules.size()]);
        SelectItem[] options = getOptions(aModules);
        handlerCtx.setOutputValue("AuditModules", options);
    }
    

    
   /**
     *	<p> This handler returns the value for Security Manager in 
     *      Security Settings </p>
     *	<p> Input value: "objectName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "securityManagerEnabled"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getSecurityManager",
   input={
        @HandlerInput(name="objectName", type=String.class, required=true)   },    
    output={
        @HandlerOutput(name="securityManagerEnabled",      type=Boolean.class)})
        
        public static void getSecurityManager(HandlerContext handlerCtx) {
        
        String objectName = (String)handlerCtx.getInputValue("objectName");             
        Boolean status = isSecurityManagerEnabled(objectName);
        handlerCtx.setOutputValue("securityManagerEnabled", status.toString());
    }
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Security Settings Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Audit"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Principal"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Password"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RoleMapping"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Mapped"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Realm"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Module"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jacc"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getSecuritySettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="Audit",      type=Boolean.class),
        @HandlerOutput(name="Principal",  type=String.class),
        @HandlerOutput(name="Password",    type=String.class),
        @HandlerOutput(name="RoleMapping",       type=Boolean.class),
        @HandlerOutput(name="Mapped", type=String.class),
        @HandlerOutput(name="Realm", type=String.class),
        @HandlerOutput(name="Module", type=String.class),
        @HandlerOutput(name="Jacc", type=String.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getSecuritySettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        boolean audit = sConfig.getAuditEnabled();
        String principal = sConfig.getDefaultPrincipal();
        String password = sConfig.getDefaultPrincipalPassword();
        boolean roleMapping = sConfig.getActivateDefaultPrincipalToRoleMapping();
        String mapped = sConfig.getMappedPrincipalClass();
        String realm = sConfig.getDefaultRealm();
        String module = sConfig.getAuditModules();
        String jacc = sConfig.getJACC();
        Map<String, String> props = sConfig.getProperties();
        handlerCtx.setOutputValue("Audit", audit);
        handlerCtx.setOutputValue("Principal", principal);
        handlerCtx.setOutputValue("Password", password);
        handlerCtx.setOutputValue("RoleMapping", roleMapping);
        handlerCtx.setOutputValue("Mapped", mapped);
        handlerCtx.setOutputValue("Realm", realm);
        handlerCtx.setOutputValue("Module", module);
        handlerCtx.setOutputValue("Jacc", jacc);    
        handlerCtx.setOutputValue("Properties", props);
        
    }    
    
/**            
     *	<p> This handler saves the values for Security </p>
      *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
      *	<p> Input value: "Audit"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Principal"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Password"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RoleMapping"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Mapped"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Jaccs"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Modules"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Realms"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SecurityManager"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveSecuritySettings",
    input={
       @HandlerInput(name="ConfigName",      type=String.class, required=true),        
        @HandlerInput(name="Audit",      type=Boolean.class),
        @HandlerInput(name="Principal",  type=String.class),
        @HandlerInput(name="Password",    type=String.class),
        @HandlerInput(name="RoleMapping",       type=Boolean.class),
        @HandlerInput(name="Mapped", type=String.class),
        @HandlerInput(name="Jaccs",      type=String.class),
        @HandlerInput(name="Modules",    type=String.class),
        @HandlerInput(name="Realms",      type=String.class),
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class)})    
    public static void saveSecuritySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
        Map addProps = (Map)handlerCtx.getInputValue("AddProps");
        String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
        for(int i=0; i<remove.length; i++){
            sConfig.removeProperty(remove[i]);
        }
        if(addProps != null ){
            Iterator additer = addProps.keySet().iterator();
            while(additer.hasNext()){
                Object key = additer.next();
                String addvalue = (String)addProps.get(key);
                sConfig.setPropertyValue((String)key, addvalue);
                
            }
        }              
        sConfig.setAuditEnabled(((Boolean)handlerCtx.getInputValue("Audit")).booleanValue());
        sConfig.setDefaultPrincipal(((String)handlerCtx.getInputValue("Principal")));
        sConfig.setDefaultPrincipalPassword(((String)handlerCtx.getInputValue("Password")));
        sConfig.setActivateDefaultPrincipalToRoleMapping(((Boolean)handlerCtx.getInputValue("RoleMapping")).booleanValue());
        sConfig.setMappedPrincipalClass(((String)handlerCtx.getInputValue("Mapped")));
        sConfig.setJACC((String)handlerCtx.getInputValue("Jaccs"));
        sConfig.setAuditModules((String)handlerCtx.getInputValue("Modules"));
        sConfig.setDefaultRealm((String)handlerCtx.getInputValue("Realms"));
        
    }    
    
/**            
     *	<p> This handler saves the value for Security Manager</p>
      *	<p> Input value: "ObjectName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SecurityManager"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    
    @Handler(id="saveSecurityManager",
    input={
       @HandlerInput(name="ObjectName",      type=String.class, required=true),        
        @HandlerInput(name="SecurityManager",      type=String.class, required=true)})    
    public static void saveSecurityManager(HandlerContext handlerCtx) {
        String objectName = (String)handlerCtx.getInputValue("ObjectName");
        String value = (String)handlerCtx.getInputValue("SecurityManager");

        Boolean status = isSecurityManagerEnabled(objectName);
        Boolean userValue = new Boolean(value);
        if (status.equals(userValue)){
            //no need to change
            return;
        }
        ArrayList newOptions = new ArrayList();
        try {
            String[] origOptions = (String[])JMXUtil.getAttribute(new ObjectName(objectName), ATTRIBUTE_NAME);
            if(userValue){
                for(int i=0; i<origOptions.length; i++){
                    newOptions.add(origOptions[i]);
                }
                newOptions.add(JVM_OPTION_SECURITY_MANAGER);
            }else{
                for(int i=0; i<origOptions.length; i++){
                    if (! (origOptions[i].trim().equals(JVM_OPTION_SECURITY_MANAGER) ||
                            origOptions[i].trim().startsWith(JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL))){
                       newOptions.add(origOptions[i]);
                    }
                }
            }
            
        } catch (Exception ex) {
            // the above method will throw an exception when the object doesn't exist to get the attr.
        }
        String[] jvmOptions = (String[])newOptions.toArray(new String[0]);
        //set the new values
        try {
            Attribute attr = new Attribute(ATTRIBUTE_NAME, jvmOptions);
            JMXUtil.setAttribute(objectName, attr);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }    
    
   /**
     *	<p> This handler returns the DEFAULT values for all the attributes in the
     *      Security Settings Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Audit" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Principal" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Password" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RoleMapping" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Mapped" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jaccs" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Modules" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Realms" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SecurityManager" -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getSecurityDefaultAttributes",
	input={
	    @HandlerInput(name="ConfigName", type=String.class, required=true)},      
    output={
        @HandlerOutput(name="Audit",     type=Boolean.class),
        @HandlerOutput(name="Principal", type=String.class),
        @HandlerOutput(name="Password", type=String.class),
        @HandlerOutput(name="RoleMapping", type=Boolean.class),
        @HandlerOutput(name="Mapped", type=String.class),
        @HandlerOutput(name="Jaccs", type=String.class),
        @HandlerOutput(name="Modules", type=String.class),
        @HandlerOutput(name="Realms", type=String.class),
        @HandlerOutput(name="SecurityManager", type=String.class)})     
        public static void getSecurityDefaultAttributes(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        String audit = sConfig.getDefaultValue("AuditEnabled");
        String principal = sConfig.getDefaultValue("DefaultPrincipal");
        String password = sConfig.getDefaultValue("DefaultPrincipalPassword");
        String roleMapping = sConfig.getDefaultValue("ActivateDefaultPrincipalToRoleMapping");
        String mapped = sConfig.getDefaultValue("MappedPrincipalClass");
        String jaccs = sConfig.getDefaultValue("JACC") ;
        String modules = sConfig.getDefaultValue("AuditModules");
        String realms = sConfig.getDefaultValue("DefaultRealm");

        if(audit.equals("true")) {
            handlerCtx.setOutputValue("Audit", true);    
        } else {
            handlerCtx.setOutputValue("Audit", false);
        }
        handlerCtx.setOutputValue("Modules", modules);
        handlerCtx.setOutputValue("Principal", principal);        
        if(roleMapping.equals("true")) {
            handlerCtx.setOutputValue("RoleMapping", true);    
        } else {
            handlerCtx.setOutputValue("RoleMapping", false);
        }        
        handlerCtx.setOutputValue("Password", password);
        handlerCtx.setOutputValue("Mapped", mapped);
        handlerCtx.setOutputValue("Jaccs", jaccs);
        handlerCtx.setOutputValue("Realms", realms);
        
        

    }            
    
   private static Boolean isSecurityManagerEnabled(String objectName){
        String[] jvmOptions = null;
        try {
            jvmOptions = (String[])JMXUtil.getAttribute(new ObjectName(objectName), ATTRIBUTE_NAME);
            if (jvmOptions != null){
                for(int i=0; i< jvmOptions.length; i++){
                    if (jvmOptions[i].trim().equals(JVM_OPTION_SECURITY_MANAGER) ||
                            jvmOptions[i].trim().startsWith(JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL)){
                        return Boolean.TRUE;
                    }
                }
            }
        } catch (Exception ex) {
            // the above method will throw an exception when the object doesn't exist to get the attr.
        }
        return Boolean.FALSE;
    }
   
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Transaction Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "OnRestart"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Retry"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogLocation"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Heuristic"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "KeyPoint"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"  -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getTransactionServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="OnRestart",      type=Boolean.class),
        @HandlerOutput(name="Timeout",  type=String.class),
        @HandlerOutput(name="Retry",    type=String.class),
        @HandlerOutput(name="LogLocation",       type=String.class),
        @HandlerOutput(name="Heuristic", type=String.class),
        @HandlerOutput(name="KeyPoint", type=String.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getTransactionServiceSettings(HandlerContext handlerCtx) {
        


        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        TransactionServiceConfig tConfig = config.getTransactionServiceConfig();
        boolean onrestart = tConfig.getAutomaticRecovery();
        String timeout = tConfig.getTimeoutInSeconds();
        String retry = tConfig.getRetryTimeoutInSeconds();
        String loglocation = tConfig.getTxLogDir();
        String heuristic = tConfig.getHeuristicDecision();
        String keypoint = tConfig.getKeypointInterval();
        Map<String, String> props = tConfig.getProperties();
        handlerCtx.setOutputValue("OnRestart", onrestart);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Retry", retry);
        handlerCtx.setOutputValue("LogLocation", loglocation);
        handlerCtx.setOutputValue("Heuristic", heuristic);
        handlerCtx.setOutputValue("KeyPoint", keypoint);      
        handlerCtx.setOutputValue("Properties", props);
        
    }       
    
/**
     *	<p> This handler saves the values for all the attributes in 
     *      Transaction Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "OnRestart"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Retry"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "LogLocation"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Heuristic"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "KeyPoint"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveTransactionServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),    
        @HandlerInput(name="OnRestart",      type=Boolean.class),
        @HandlerInput(name="Timeout",  type=String.class),
        @HandlerInput(name="Retry",    type=String.class),
        @HandlerInput(name="LogLocation",       type=String.class),
        @HandlerInput(name="Heuristic", type=String.class),
        @HandlerInput(name="KeyPoint", type=String.class),
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class)})
        
        public static void saveTransactionServiceSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        TransactionServiceConfig tConfig = config.getTransactionServiceConfig();
        ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
        Map addProps = (Map)handlerCtx.getInputValue("AddProps");
        String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
        for(int i=0; i<remove.length; i++){
            tConfig.removeProperty(remove[i]);
        }
        if(addProps != null ){
            Iterator additer = addProps.keySet().iterator();
            while(additer.hasNext()){
                Object key = additer.next();
                String addvalue = (String)addProps.get(key);
                tConfig.setPropertyValue((String)key, addvalue);
                
            }
        }              
        tConfig.setAutomaticRecovery(((Boolean)handlerCtx.getInputValue("OnRestart")).booleanValue());
        tConfig.setTimeoutInSeconds(((String)handlerCtx.getInputValue("Timeout")));
        tConfig.setRetryTimeoutInSeconds(((String)handlerCtx.getInputValue("Retry")));
        tConfig.setTxLogDir(((String)handlerCtx.getInputValue("LogLocation")));
        tConfig.setHeuristicDecision(((String)handlerCtx.getInputValue("Heuristic")));
        tConfig.setKeypointInterval(((String)handlerCtx.getInputValue("KeyPoint")));
        
    }           
   
/**
     *	<p> This handler returns the default values for all the attributes in 
     *      Transaction Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "OnRestart"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Retry"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogLocation"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Heuristic"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "KeyPoint"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getTransactionServiceDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="OnRestart",      type=Boolean.class),
        @HandlerOutput(name="Timeout",  type=String.class),
        @HandlerOutput(name="Retry",    type=String.class),
        @HandlerOutput(name="LogLocation",       type=String.class),
        @HandlerOutput(name="Heuristic", type=String.class),
        @HandlerOutput(name="KeyPoint", type=String.class)})
        
        public static void getTransactionServiceDefaultSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        TransactionServiceConfig tConfig = config.getTransactionServiceConfig();
        String onrestart = tConfig.getDefaultValue("AutomaticRecovery");
        String timeout = tConfig.getDefaultValue("TimeoutInSeconds");
        String retry = tConfig.getDefaultValue("RetryTimeoutInSeconds");
        String loglocation = tConfig.getDefaultValue("TxLogDir");
        String heuristic = tConfig.getDefaultValue("HeuristicDecision");
        String keypoint = tConfig.getDefaultValue("KeypointInterval");
        handlerCtx.setOutputValue("OnRestart", onrestart);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Retry", retry);
        handlerCtx.setOutputValue("LogLocation", loglocation);
        handlerCtx.setOutputValue("Heuristic", heuristic);
        handlerCtx.setOutputValue("KeyPoint", keypoint);
        
    }    
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      JMS Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Type"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Arguments"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Reconnect"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Interval"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Attempts"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Host"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Behavior"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Iterations"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Scheme"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Service"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJmsServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="Type",      type=String.class),
        @HandlerOutput(name="Timeout",  type=String.class),
        @HandlerOutput(name="Arguments",    type=String.class),
        @HandlerOutput(name="Reconnect",       type=Boolean.class),
        @HandlerOutput(name="Interval", type=String.class),
        @HandlerOutput(name="Attempts", type=String.class),
        @HandlerOutput(name="Host", type=String.class),
        @HandlerOutput(name="Behavior", type=String.class),
        @HandlerOutput(name="Iterations", type=String.class),
        @HandlerOutput(name="Scheme", type=String.class),
        @HandlerOutput(name="Service", type=String.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getJmsServiceSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        JMSServiceConfig jConfig = config.getJMSServiceConfig();
        String type = jConfig.getType();
        String timeout = jConfig.getInitTimeoutInSeconds();
        String arguments = jConfig.getStartArgs();
        boolean reconnect = jConfig.getReconnectEnabled();
        String interval = jConfig.getReconnectIntervalInSeconds();
        String attempts = jConfig.getReconnectAttempts();
        String host = jConfig.getDefaultJMSHost();
        String behavior = jConfig.getAddressListBehavior();
        String iterations = jConfig.getAddressListIterations();
        String scheme = jConfig.getMQScheme();
        String service = jConfig.getMQService();
        Map<String, String> props = jConfig.getProperties();
        handlerCtx.setOutputValue("Type", type);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Arguments", arguments);
        handlerCtx.setOutputValue("Reconnect", reconnect);
        handlerCtx.setOutputValue("Interval", interval);
        handlerCtx.setOutputValue("Attempts", attempts);
        handlerCtx.setOutputValue("Host", host);
        handlerCtx.setOutputValue("Behavior", behavior);
        handlerCtx.setOutputValue("Iterations", iterations);
        handlerCtx.setOutputValue("Scheme", scheme);
        handlerCtx.setOutputValue("Service", service);      
        handlerCtx.setOutputValue("Properties", props);
        
    }     

/**
     *	<p> This handler returns the default values for all the attributes in 
     *      JMS Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Type"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Arguments"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Reconnect"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Interval"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Attempts"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Host"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Behavior"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Iterations"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Scheme"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Service"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJmsServiceDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="Type",      type=String.class),
        @HandlerOutput(name="Timeout",  type=String.class),
        @HandlerOutput(name="Arguments",    type=String.class),
        @HandlerOutput(name="Reconnect",       type=Boolean.class),
        @HandlerOutput(name="Interval", type=String.class),
        @HandlerOutput(name="Attempts", type=String.class),
        @HandlerOutput(name="Host", type=String.class),
        @HandlerOutput(name="Behavior", type=String.class),
        @HandlerOutput(name="Iterations", type=String.class),
        @HandlerOutput(name="Scheme", type=String.class),
        @HandlerOutput(name="Service", type=String.class)})
        
        public static void getJmsServiceDefaultSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        JMSServiceConfig jConfig = config.getJMSServiceConfig();
        String type = jConfig.getDefaultValue("Type");
        String timeout = jConfig.getDefaultValue("InitTimeoutInSeconds");
        String arguments = jConfig.getDefaultValue("StartArgs");
        String reconnect = jConfig.getDefaultValue("ReconnectEnabled");
        String interval = jConfig.getDefaultValue("ReconnectIntervalInSeconds");
        String attempts = jConfig.getDefaultValue("ReconnectAttempts");
        String host = jConfig.getDefaultValue("DefaultJMSHost");
        String behavior = jConfig.getDefaultValue("AddressListBehavior");
        String iterations = jConfig.getDefaultValue("AddressListIterations");
        String scheme = jConfig.getDefaultValue("MQScheme");
        String service = jConfig.getDefaultValue("MQService");
        handlerCtx.setOutputValue("Type", type);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Arguments", arguments);
        handlerCtx.setOutputValue("Reconnect", reconnect);
        handlerCtx.setOutputValue("Interval", interval);
        handlerCtx.setOutputValue("Attempts", attempts);
        handlerCtx.setOutputValue("Host", host);
        handlerCtx.setOutputValue("Behavior", behavior);
        handlerCtx.setOutputValue("Iterations", iterations);
        handlerCtx.setOutputValue("Scheme", scheme);
        handlerCtx.setOutputValue("Service", service);
        
    }
    
    
/**
     *	<p> This handler saves the values for all the attributes in 
     *      JMS Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Type"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Arguments"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Reconnect"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Interval"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Attempts"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Host"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Behavior"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Iterations"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Scheme"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Service"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveJmsServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),   
        @HandlerInput(name="Type",      type=String.class),
        @HandlerInput(name="Timeout",  type=String.class),
        @HandlerInput(name="Arguments",    type=String.class),
        @HandlerInput(name="Reconnect",       type=Boolean.class),
        @HandlerInput(name="Interval", type=String.class),
        @HandlerInput(name="Attempts", type=String.class),
        @HandlerInput(name="Host", type=String.class),
        @HandlerInput(name="Behavior", type=String.class),
        @HandlerInput(name="Iterations", type=String.class),
        @HandlerInput(name="Scheme", type=String.class),
        @HandlerInput(name="Service", type=String.class),
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class)})
        
        public static void saveJmsServiceSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        JMSServiceConfig jConfig = config.getJMSServiceConfig();
        ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
        Map addProps = (Map)handlerCtx.getInputValue("AddProps");
        String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
        for(int i=0; i<remove.length; i++){
            jConfig.removeProperty(remove[i]);
        }
        if(addProps != null ){
            Iterator additer = addProps.keySet().iterator();
            while(additer.hasNext()){
                Object key = additer.next();
                String addvalue = (String)addProps.get(key);
                jConfig.setPropertyValue((String)key, addvalue);
                
            }
        }              
        jConfig.setType(((String)handlerCtx.getInputValue("Type")));
        jConfig.setInitTimeoutInSeconds(((String)handlerCtx.getInputValue("Timeout")));
        jConfig.setStartArgs(((String)handlerCtx.getInputValue("Arguments")));
        jConfig.setReconnectEnabled(((Boolean)handlerCtx.getInputValue("Reconnect")).booleanValue());
        jConfig.setReconnectIntervalInSeconds(((String)handlerCtx.getInputValue("Interval")));
        jConfig.setReconnectAttempts(((String)handlerCtx.getInputValue("Attempts")));
        jConfig.setDefaultJMSHost(((String)handlerCtx.getInputValue("Host")));
        jConfig.setAddressListBehavior(((String)handlerCtx.getInputValue("Behavior")));
        jConfig.setAddressListIterations(((String)handlerCtx.getInputValue("Iterations")));
        jConfig.setMQScheme(((String)handlerCtx.getInputValue("Scheme")));
        jConfig.setMQService(((String)handlerCtx.getInputValue("Service")));
        
    }     
    
    /**
     *	<p> This handler pings JMS
     */
        @Handler(id="pingJMS",
            input={
                @HandlerInput(name="configName", type=String.class, required=true)})
        public static void pingJMS(HandlerContext handlerCtx) {
        
            String configName = (String) handlerCtx.getInputValue("configName");
            String[] params = {null};
            String[] signatures = {"java.lang.String"};
            try{
                Object result = JMXUtil.invoke( "com.sun.appserv:type=resources,category=config",
                            "JMSPing",
                            params,
                            signatures);
                if (result != null) {
                    GuiUtil.prepareAlert(handlerCtx,"success", GuiUtil.getMessage("msg.JmsPingSucceed"), null);
                }else{
                    GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), "msg.PingError");
                }
            }catch(Exception ex){
		GuiUtil.handleException(handlerCtx, ex);
            }
        }
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Monitoring Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Jvm"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Http"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Transaction"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "JmsConnector"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Orb"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Web"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Ejb"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jdbc"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Pool"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jvm"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMonitoringServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="Jvm",      type=String.class),
        @HandlerOutput(name="Http",  type=String.class),
        @HandlerOutput(name="Transaction",    type=String.class),
        @HandlerOutput(name="JmsConnector",       type=String.class),
        @HandlerOutput(name="Orb", type=String.class),
        @HandlerOutput(name="Web", type=String.class),
        @HandlerOutput(name="Ejb", type=String.class),
        @HandlerOutput(name="Jdbc", type=String.class),
        @HandlerOutput(name="ConPool", type=String.class),
        @HandlerOutput(name="ThreadPool", type=String.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getMonitoringServiceSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        MonitoringServiceConfig mSConfig = config.getMonitoringServiceConfig();
	ModuleMonitoringLevelsConfig mConfig = mSConfig.getModuleMonitoringLevelsConfig();
        String jvm = mConfig.getJVM();
        String http = mConfig.getHTTPService();
        String transaction = mConfig.getTransactionService();
        String jms = mConfig.getJMSService();
        String orb = mConfig.getORB();
        String web = mConfig.getWebContainer();
        String ejb = mConfig.getEJBContainer();
        String jdbc = mConfig.getJDBCConnectionPool();
        String threadPool = mConfig.getThreadPool();
        Map<String, String> props = mConfig.getProperties();
        handlerCtx.setOutputValue("Jvm", jvm);
        handlerCtx.setOutputValue("Http", http);
        handlerCtx.setOutputValue("Transaction", transaction);
        handlerCtx.setOutputValue("JmsConnector", jms);
        handlerCtx.setOutputValue("Orb", orb);
        handlerCtx.setOutputValue("Web", web);
        handlerCtx.setOutputValue("Ejb", ejb);
        handlerCtx.setOutputValue("Jdbc", jdbc);
        handlerCtx.setOutputValue("ThreadPool", threadPool);
        handlerCtx.setOutputValue("Properties", props);
        
    }       
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Monitoring Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Jvm"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Http"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Transaction"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "JmsConnector"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Orb"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Web"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Ejb"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jdbc"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Pool"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMonitoringServiceDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="Jvm",      type=String.class),
        @HandlerOutput(name="Http",  type=String.class),
        @HandlerOutput(name="Transaction",    type=String.class),
        @HandlerOutput(name="JmsConnector",       type=String.class),
        @HandlerOutput(name="Orb", type=String.class),
        @HandlerOutput(name="Web", type=String.class),
        @HandlerOutput(name="Ejb", type=String.class),
        @HandlerOutput(name="Jdbc", type=String.class),
        @HandlerOutput(name="ThreadPool", type=String.class)})
        
        public static void getMonitoringServiceDefaultSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        MonitoringServiceConfig mSConfig = config.getMonitoringServiceConfig();
	ModuleMonitoringLevelsConfig mConfig = mSConfig.getModuleMonitoringLevelsConfig();
        String jvm = mConfig.getDefaultValue("JVM");
        String http = mConfig.getDefaultValue("HTTPService");
        String transaction = mConfig.getDefaultValue("TransactionService");
        String jms = mConfig.getDefaultValue("JMSService");
        String orb = mConfig.getDefaultValue("ORB");
        String web = mConfig.getDefaultValue("WebContainer");
        String ejb = mConfig.getDefaultValue("EJBContainer");
        String jdbc = mConfig.getDefaultValue("JDBCConnectionPool");
        String threadPool = mConfig.getDefaultValue("ThreadPool");
        handlerCtx.setOutputValue("Jvm", jvm);
        handlerCtx.setOutputValue("Http", http);
        handlerCtx.setOutputValue("Transaction", transaction);
        handlerCtx.setOutputValue("JmsConnector", jms);
        handlerCtx.setOutputValue("Orb", orb);
        handlerCtx.setOutputValue("Web", web);
        handlerCtx.setOutputValue("Ejb", ejb);
        handlerCtx.setOutputValue("Jdbc", jdbc);
        handlerCtx.setOutputValue("ThreadPool", threadPool);
        
    }      
    
/**
     *	<p> This handler saves the values for all the attributes in 
     *      Monitoring Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Jvm"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Http"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Transaction"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "JmsConnector"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Orb"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Web"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Ejb"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Jdbc"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Pool"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveMonitoringServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),   
        @HandlerInput(name="Jvm",      type=String.class),
        @HandlerInput(name="Http",  type=String.class),
        @HandlerInput(name="Transaction",    type=String.class),
        @HandlerInput(name="JmsConnector",       type=String.class),
        @HandlerInput(name="Orb", type=String.class),
        @HandlerInput(name="Web", type=String.class),
        @HandlerInput(name="Ejb", type=String.class),
        @HandlerInput(name="Jdbc", type=String.class),
        @HandlerInput(name="ThreadPool", type=String.class),
        @HandlerInput(name="ConPool", type=String.class),
        @HandlerInput(name="newProps", type=Map.class)})
        
        public static void saveMonitoringServiceSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        Map newProps = (Map)handlerCtx.getInputValue("newProps");
        MonitoringServiceConfig mSConfig = config.getMonitoringServiceConfig();
	ModuleMonitoringLevelsConfig mConfig = mSConfig.getModuleMonitoringLevelsConfig();
        
        AMXUtil.updateProperties(mConfig, newProps, null);
         
        mConfig.setJVM(((String)handlerCtx.getInputValue("Jvm")));
        mConfig.setHTTPService(((String)handlerCtx.getInputValue("Http")));
        mConfig.setTransactionService(((String)handlerCtx.getInputValue("Transaction")));
        //setting JMXService will also affect connector connection pool, connector-service.
        mConfig.setJMSService( (String)handlerCtx.getInputValue("JmsConnector"));
        mConfig.setORB(((String)handlerCtx.getInputValue("Orb")));
        mConfig.setWebContainer(((String)handlerCtx.getInputValue("Web")));
        mConfig.setEJBContainer(((String)handlerCtx.getInputValue("Ejb")));
        mConfig.setJDBCConnectionPool(((String)handlerCtx.getInputValue("Jdbc")));
        
        mConfig.setThreadPool(((String)handlerCtx.getInputValue("ThreadPool")));
        
    }      

/**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Virtual Server Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Name"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Hosts"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Stategrp"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Http"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Web"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogFile"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getVirtualServerSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true) },        
    output={
        @HandlerOutput(name="Hosts",      type=String.class),
        @HandlerOutput(name="StateOption",  type=String.class),
        @HandlerOutput(name="Http",    type=String.class),
        @HandlerOutput(name="Web",       type=String.class),
        @HandlerOutput(name="Modules",       type=SelectItem[].class),
        @HandlerOutput(name="LogFile", type=String.class),
        @HandlerOutput(name="accesslog", type=String.class),
        @HandlerOutput(name="docroot", type=String.class),
        @HandlerOutput(name="accessLogBufferSize", type=String.class),
        @HandlerOutput(name="accessLogWriteInterval", type=String.class),
        @HandlerOutput(name="accessLoggingFlag", type=String.class),
        @HandlerOutput(name="sso", type=Boolean.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getVirtualServerSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                getDefaultVirtualServerAttributes(handlerCtx);
                return;
            }
            Map<String,VirtualServerConfig>vservers = config.getHTTPServiceConfig().getVirtualServerConfigMap();
            VirtualServerConfig vs = (VirtualServerConfig)vservers.get((String)handlerCtx.getInputValue("Name"));
            
            handlerCtx.setOutputValue("Hosts", vs.getHosts());
            handlerCtx.setOutputValue("StateOption", vs.getState());
            handlerCtx.setOutputValue("Http", vs.getHTTPListeners());
            handlerCtx.setOutputValue("Web", vs.getDefaultWebModule());
            handlerCtx.setOutputValue("LogFile", vs.getLogFile());
            
            handlerCtx.setOutputValue("Properties", AMXUtil.getNonSkipPropertiesMap(vs, vsSkipPropsList));
            Map origProps = vs.getProperties();
            handlerCtx.setOutputValue("accessLogBufferSize", origProps.get("accessLogBufferSize"));
            handlerCtx.setOutputValue("accessLogWriteInterval", origProps.get("accessLogWriteInterval"));
            handlerCtx.setOutputValue("accesslog", origProps.get("accesslog"));
            handlerCtx.setOutputValue("docroot", origProps.get("docroot"));
            String sso = (String) origProps.get("sso-enabled");
            Boolean ssoFlag = false;
            if ( GuiUtil.isEmpty(sso))
                ssoFlag = false;
            else
            ssoFlag = (sso.equals("true")) ? true: false;
            
            handlerCtx.setOutputValue("sso", ssoFlag);
            
            String accessLoggingFlag = (String) origProps.get("accessLoggingEnabled");
            if (GuiUtil.isEmpty(accessLoggingFlag)){
                handlerCtx.setOutputValue("accessLoggingFlag", "off");
            }else
            if ("true".equals(accessLoggingFlag))
                handlerCtx.setOutputValue("accessLoggingFlag", "true");
            else
                handlerCtx.setOutputValue("accessLoggingFlag", "false");
                
            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    } 
    
        private static void getDefaultVirtualServerAttributes(HandlerContext handlerCtx){ 
        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(VirtualServerConfig.J2EE_TYPE);         
        handlerCtx.setOutputValue("Hosts", defaultMap.get("hosts"));
        handlerCtx.setOutputValue("StateOption", defaultMap.get("state"));
        handlerCtx.setOutputValue("Http", defaultMap.get("http-listeners"));
        handlerCtx.setOutputValue("Web", defaultMap.get("default-web-module"));
        handlerCtx.setOutputValue("LogFile", defaultMap.get("log-file"));
        //handlerCtx.setOutputValue("docroot", defaultMap.get("docroot"));
        handlerCtx.setOutputValue("docroot", "${com.sun.aas.instanceRoot}/docroot");
        Map dMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(HTTPAccessLogConfig.J2EE_TYPE);  
        handlerCtx.setOutputValue("accesslog", dMap.get("log-directory"));
        handlerCtx.setOutputValue("sso", Boolean.FALSE);
        handlerCtx.setOutputValue("accessLoggingFlag", "off");
        handlerCtx.setOutputValue("Properties", new HashMap());
    }        
        
      /**
     *	<p> This handler returns the list of web modules including both embedded
     *  ones in ear and web app.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAllWebModules",
       output={
        @HandlerOutput(name="modules", type=List.class)})

        public static void getAllWebModules(HandlerContext handlerCtx) {
        
        Map<String,WebModuleConfig> webs = AMXUtil.getDomainConfig().getWebModuleConfigMap();
        List result = new ArrayList();
        result.add("");
        for(String nm : webs.keySet()){
            result.add(nm);
        }
        
        Map<String,J2EEApplicationConfig> ears = AMXUtil.getDomainConfig().getJ2EEApplicationConfigMap();
        
        try{
            for(String appName : ears.keySet()){
                String[] modules = (String[])JMXUtil.invoke(
                        "com.sun.appserv:type=applications,category=config", 
                        "getModuleComponents",
                        new Object[]{appName}, 
                        new String[]{"java.lang.String"});
                for (int j = 0; j < modules.length; j++) {
                    ObjectName moduleName = new ObjectName(modules[j]);
                    String type = moduleName.getKeyProperty("j2eeType");
                    if (type != null && type.equalsIgnoreCase("WebModule")) {
                        String name = moduleName.getKeyProperty("name");
                        String entry = appName+"#"+name;
                        result.add(entry);
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        handlerCtx.setOutputValue("modules", result);
        
    }    
  
     /**
     *	<p> This handler returns a list of classnames for realms
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getClassnames",
       output={
        @HandlerOutput(name="Classnames", type=SelectItem[].class)})

        public static void getClassnames(HandlerContext handlerCtx) {
        String[] classnames = (String[])JMXUtil.invoke(
                "com.sun.appserv:category=config,config=server-config,type=security-service",
                "getPredefinedAuthRealmClassNames", null, null );
        SelectItem[] options = getOptions(classnames);
        handlerCtx.setOutputValue("Classnames", options);
    }       
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Virtual Server Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Name"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Hosts"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Stategrp"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Http"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Web"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "LogFile"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveVirtualServerSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true),        
        @HandlerInput(name="newProps", type=Map.class),  
        @HandlerInput(name="Hosts",      type=String.class),
        @HandlerInput(name="Http",    type=String.class),
        @HandlerInput(name="Web",       type=String.class),
        @HandlerInput(name="LogFile", type=String.class),
        @HandlerInput(name="StateOption",     type=String.class),
        @HandlerInput(name="docroot",     type=String.class),
        @HandlerInput(name="accesslog",     type=String.class),
        @HandlerInput(name="accessLogBufferSize", type=String.class),
        @HandlerInput(name="accessLogWriteInterval", type=String.class),
        @HandlerInput(name="accessLoggingFlag", type=String.class),
        @HandlerInput(name="sso",     type=Boolean.class)})
        
        public static void saveVirtualServerSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        Map newProps = (Map)handlerCtx.getInputValue("newProps");
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map convertedMap = AMXUtil.convertToPropertiesOptionMap(newProps, null);
                putOptionalValue((String) handlerCtx.getInputValue("accesslog"), convertedMap, "accesslog");
                putOptionalValue((String) handlerCtx.getInputValue("docroot"), convertedMap, "docroot");
                putOptionalValue((String) handlerCtx.getInputValue("accessLogBufferSize"), convertedMap, "accessLogBufferSize");
                putOptionalValue((String) handlerCtx.getInputValue("accessLogWriteInterval"), convertedMap, "accessLogWriteInterval");
                putOptionalValue(""+ handlerCtx.getInputValue("sso"), convertedMap, "sso-enabled");
                String accessLoggingFlag = (String)handlerCtx.getInputValue("accessLoggingFlag");
                if (!accessLoggingFlag.equals("off")){
                    putOptionalValue(accessLoggingFlag, convertedMap, "accessLoggingEnabled");
                }
                
                VirtualServerConfig server = config.getHTTPServiceConfig().createVirtualServerConfig(
                        (String)handlerCtx.getInputValue("Name"), ((String)handlerCtx.getInputValue("Hosts")), convertedMap);
                
                server.setHosts(((String)handlerCtx.getInputValue("Hosts")));
                server.setHTTPListeners(((String)handlerCtx.getInputValue("Http")));
                server.setDefaultWebModule(((String)handlerCtx.getInputValue("Web")));
                server.setLogFile(((String)handlerCtx.getInputValue("LogFile")));
                //server.setState(((String)handlerCtx.getInputValue("StateOption")));
                String tmp = (String)handlerCtx.getInputValue("StateOption");
                server.setState(tmp);
                return;
                
            }
            Map<String,VirtualServerConfig>vservers = config.getHTTPServiceConfig().getVirtualServerConfigMap();
            VirtualServerConfig vs = (VirtualServerConfig)vservers.get((String)handlerCtx.getInputValue("Name"));
            AMXUtil.updateProperties(vs, newProps, vsSkipPropsList);
            
            vs.setHosts(((String)handlerCtx.getInputValue("Hosts")));
            vs.setState(((String)handlerCtx.getInputValue("StateOption")));
            vs.setHTTPListeners(((String)handlerCtx.getInputValue("Http")));
            vs.setDefaultWebModule(((String)handlerCtx.getInputValue("Web")));
            vs.setLogFile(((String)handlerCtx.getInputValue("LogFile")));
            
            AMXUtil.changeProperty(vs, "accesslog", (String)handlerCtx.getInputValue("accesslog"));
            AMXUtil.changeProperty(vs, "accessLogBufferSize", (String)handlerCtx.getInputValue("accessLogBufferSize"));
            AMXUtil.changeProperty(vs, "accessLogWriteInterval", (String)handlerCtx.getInputValue("accessLogWriteInterval"));
            AMXUtil.changeProperty(vs, "docroot", (String)handlerCtx.getInputValue("docroot"));
            AMXUtil.changeProperty(vs, "sso-enabled", ""+handlerCtx.getInputValue("sso"));
            String accessLoggingFlag = (String)handlerCtx.getInputValue("accessLoggingFlag");
            if (accessLoggingFlag.equals("off"))
                accessLoggingFlag=null;
            AMXUtil.changeProperty(vs, "accessLoggingEnabled", accessLoggingFlag);
            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }   
     
    
    static private void putOptionalValue(String value, Map convertedMap, String propName)
    {
       if (GuiUtil.isEmpty(value))
           return;
       convertedMap.put(PropertiesAccess.PROPERTY_PREFIX + propName, value);
    }
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit JaccProviders Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJaccProviderSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true) },        
    output={
        @HandlerOutput(name="PolicyProvider",      type=String.class),
        @HandlerOutput(name="PolicyConfig",  type=String.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getJaccProviderSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                getDefaultJaccProviderAttributes(handlerCtx);
                return;
            }
            Map<String,JACCProviderConfig>jaccProviders = config.getSecurityServiceConfig().getJACCProviderConfigMap();
            JACCProviderConfig jacc = (JACCProviderConfig)jaccProviders.get((String)handlerCtx.getInputValue("Name"));
            Map<String, String> props = jacc.getProperties();
            String jconfig = jacc.getPolicyConfigurationFactoryProvider();
            String policy = jacc.getPolicyProvider();
            handlerCtx.setOutputValue("PolicyConfig", jconfig);
            handlerCtx.setOutputValue("PolicyProvider", policy);
            handlerCtx.setOutputValue("Properties", props);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    } 
    
        private static void getDefaultJaccProviderAttributes(HandlerContext handlerCtx){ 
        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(JACCProviderConfig.J2EE_TYPE);         
        handlerCtx.setOutputValue("PolicyConfig", defaultMap.get("policy-configuration-factory-provider"));
        handlerCtx.setOutputValue("PolicyProvider", defaultMap.get("policy-provider"));
        
    }       
    
/**
     *	<p> This handler saves the values for all the attributes in 
     *      Edit JaccProvider Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveJaccProviderSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true),        
        @HandlerInput(name="AddProps", type=Map.class),  
        @HandlerInput(name="RemoveProps", type=ArrayList.class),
        @HandlerInput(name="PolicyConfig",      type=String.class),
        @HandlerInput(name="PolicyProvider",  type=String.class)})
        
        public static void saveJaccProviderSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map optionalMap = AMXUtil.convertToPropertiesOptionMap((Map)handlerCtx.getInputValue("AddProps"), null);
                config.getSecurityServiceConfig().createJACCProviderConfig(
                        (String)handlerCtx.getInputValue("Name"), ((String)handlerCtx.getInputValue("PolicyProvider")), ((String)handlerCtx.getInputValue("PolicyConfig")), optionalMap);
                return;
            }
            Map<String,JACCProviderConfig>jaccProviders = config.getSecurityServiceConfig().getJACCProviderConfigMap();
            JACCProviderConfig jacc = (JACCProviderConfig)jaccProviders.get((String)handlerCtx.getInputValue("Name"));
            ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
            Map addProps = (Map)handlerCtx.getInputValue("AddProps");
            String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
            for(int i=0; i<remove.length; i++){
                jacc.removeProperty(remove[i]);
            }
            if(addProps != null ){
                Iterator additer = addProps.keySet().iterator();
                while(additer.hasNext()){
                    Object key = additer.next();
                    String addvalue = (String)addProps.get(key);
                    jacc.setPropertyValue((String)key, addvalue);
                    
                }
            }
            jacc.setPolicyConfigurationFactoryProvider(((String)handlerCtx.getInputValue("PolicyConfig")));
            jacc.setPolicyProvider(((String)handlerCtx.getInputValue("PolicyProvider")));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }   
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Audit Modules Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAuditModuleSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true) },        
    output={
        @HandlerOutput(name="Classname",      type=String.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getAuditModuleSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                getDefaultAuditModuleAttributes(handlerCtx);
                return;
            }
            Map<String,AuditModuleConfig>auditModules = config.getSecurityServiceConfig().getAuditModuleConfigMap();
            AuditModuleConfig module = (AuditModuleConfig)auditModules.get((String)handlerCtx.getInputValue("Name"));
            Map<String, String> props = module.getProperties();
            String classname = module.getClassname();
            handlerCtx.setOutputValue("Classname", classname);
            handlerCtx.setOutputValue("Properties", props);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }  
    
        private static void getDefaultAuditModuleAttributes(HandlerContext handlerCtx){ 
        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(AuditModuleConfig.J2EE_TYPE);         
        handlerCtx.setOutputValue("Classname", defaultMap.get("classname"));
        
    }       
    
/**
     *	<p> This handler saves the values for all the attributes in 
     *      Edit Audit Modules Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveAuditModuleSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true),        
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class),
        @HandlerInput(name="Classname",      type=String.class)})
        
        public static void saveAuditModuleSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map optionalMap = AMXUtil.convertToPropertiesOptionMap((Map)handlerCtx.getInputValue("AddProps"), null);
                config.getSecurityServiceConfig().createAuditModuleConfig(
                        (String)handlerCtx.getInputValue("Name"), ((String)handlerCtx.getInputValue("Classname")), true, optionalMap);
                return;
            }
            Map<String,AuditModuleConfig>modules = config.getSecurityServiceConfig().getAuditModuleConfigMap();
            AuditModuleConfig module = (AuditModuleConfig)modules.get((String)handlerCtx.getInputValue("Name"));
            ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
            Map addProps = (Map)handlerCtx.getInputValue("AddProps");
            String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
            for(int i=0; i<remove.length; i++){
                module.removeProperty(remove[i]);
            }
            if(addProps != null ){
                Iterator additer = addProps.keySet().iterator();
                while(additer.hasNext()){
                    Object key = additer.next();
                    String addvalue = (String)addProps.get(key);
                    module.setPropertyValue((String)key, addvalue);
                    
                }
            }
            module.setClassname(((String)handlerCtx.getInputValue("Classname")));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit JaccProviders Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getRealmSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true) },        
    output={
        @HandlerOutput(name="Classname",      type=String.class),
        @HandlerOutput(name="ClassnameInput",      type=String.class),
        @HandlerOutput(name="ClassnameOption",     type=String.class),
        @HandlerOutput(name="predefinedClassname",     type=Boolean.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getRealmSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                getDefaultRealmAttributes(handlerCtx);
                return;
            }
            Map<String,AuthRealmConfig>realms = config.getSecurityServiceConfig().getAuthRealmConfigMap();
            AuthRealmConfig realm = (AuthRealmConfig)realms.get((String)handlerCtx.getInputValue("Name"));
            Map<String, String> props = realm.getProperties();
            String classname = realm.getClassname();
            String[] classnames = (String[])JMXUtil.invoke(
                "com.sun.appserv:category=config,config=server-config,type=security-service",
                "getPredefinedAuthRealmClassNames", null, null );     
            List cn = new ArrayList();
            for(int i=0; i<classnames.length; i++){  
                cn.add(classnames[i]);
             }
            if(cn.contains(classname)) {
                handlerCtx.setOutputValue("Classname", classname);   
                handlerCtx.setOutputValue("ClassnameOption", "predefine");
                handlerCtx.setOutputValue("predefinedClassname", true);
            } else {
                handlerCtx.setOutputValue("ClassnameInput", classname);   
                handlerCtx.setOutputValue("ClassnameOption", "input");
                handlerCtx.setOutputValue("predefinedClassname", false);
            }
            handlerCtx.setOutputValue("Properties", props);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    } 
    
    private static void getDefaultRealmAttributes(HandlerContext handlerCtx){ 
        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(AuthRealmConfig.J2EE_TYPE);         
        handlerCtx.setOutputValue("Classname", defaultMap.get("classname"));
        handlerCtx.setOutputValue("ClassnameOption", "predefine");
        handlerCtx.setOutputValue("predefinedClassname", true);
    }   
      
/**
     *	<p> This handler saves the values for all the attributes in 
     *      Edit Realms Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveRealmSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true),        
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class),
        @HandlerInput(name="Classname",      type=String.class),
        @HandlerInput(name="ClassnameOption",     type=String.class),
        @HandlerInput(name="ClassnameInput",     type=String.class)})
        
        public static void saveRealmSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        String option = (String)handlerCtx.getInputValue("ClassnameOption");
        String classname = null;
        if(option.equals("predefine")){
            classname = (String)handlerCtx.getInputValue("Classname");
        } else {
            classname = (String)handlerCtx.getInputValue("ClassnameInput");            
        }
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map optionalMap = AMXUtil.convertToPropertiesOptionMap((Map)handlerCtx.getInputValue("AddProps"), null);
                config.getSecurityServiceConfig().createAuthRealmConfig(
                        (String)handlerCtx.getInputValue("Name"), classname, optionalMap);
                return;
            }
            Map<String,AuthRealmConfig>realms = config.getSecurityServiceConfig().getAuthRealmConfigMap();
            AuthRealmConfig realm = (AuthRealmConfig)realms.get((String)handlerCtx.getInputValue("Name"));
            ArrayList removeProps = (ArrayList)handlerCtx.getInputValue("RemoveProps");
            Map addProps = (Map)handlerCtx.getInputValue("AddProps");
            String[] remove = (String[])removeProps.toArray(new String[ removeProps.size()]);
            for(int i=0; i<remove.length; i++){
                realm.removeProperty(remove[i]);
            }
            if(addProps != null ){
                Iterator additer = addProps.keySet().iterator();
                while(additer.hasNext()){
                    Object key = additer.next();
                    String addvalue = (String)addProps.get(key);
                    realm.setPropertyValue((String)key, addvalue);
                    
                }
            }
            realm.setClassname(classname);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Physical Destinations Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJmsDestinationSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true) },        
    output={
        @HandlerOutput(name="Type",      type=String.class)})
        
        public static void getJmsDestinationSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                getJmsDestinationDefaults(handlerCtx);
                return;
            }
            String type = (String)JMXUtil.getAttribute(new ObjectName("com.sun.appserv:type=admin-object-resource,category=config,jndi-name="+(String)handlerCtx.getInputValue("Name")), "res-type");
            handlerCtx.setOutputValue("Type", type);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    } 
    
    /**
     *	<p> This handler returns the default value for JMS Physical Destinations </p>
     *	<p> Output value: "Type"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJmsDestinationDefaults",
    output={
        @HandlerOutput(name="Type",   type=String.class)})
        
        public static void getJmsDestinationDefaults(HandlerContext handlerCtx) {
        String[] params = {"res-type", null};
        String type = (String)getDefaultAttributeValue(params);
        if(type != null)
            handlerCtx.setOutputValue("Type", type);
        
    }  
    

/**
     *	<p> This handler saves the values for all the attributes in 
     *      Edit Realms Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="savePhysicalDestinations",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="Name", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true),        
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="Type",      type=String.class)})
        
        public static void savePhysicalDestinations(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            AttributeList list = new AttributeList();
            list.add(new Attribute("destName", (String)handlerCtx.getInputValue("Name")));
            list.add(new Attribute("destType", (String)handlerCtx.getInputValue("Type")));
            
            Properties props = new Properties();
            Map addProps = (Map)handlerCtx.getInputValue("AddProps");
            if(addProps != null ){
                Iterator additer = addProps.keySet().iterator();
                while(additer.hasNext()){
                    Object key = additer.next();
                    String addvalue = (String)addProps.get(key);
                    props.put(key, addvalue);
                    
                }
            }
            
        String[] types = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};
        Object[] params = new Object[]{list, props, null};
        
         Object obj =  JMXUtil.invoke(
            "com.sun.appserv:type=resources,category=config", 
            "createPhysicalDestination", params, types);            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }
    
    /**
     *	<p> This handler returns the values for list of thread pools in 
     *      ORB Page </p>
     *  <p> Input  value: "ConfigName               -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "ThreadPools"     -- Type: <code>SelectItem[].class 
     *      SelectItem[] (castable to Option[])</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getThreadPools",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},
    output={
        @HandlerOutput(name="ThreadPools",   type=SelectItem[].class)})
        
        public static void getThreadPools(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        Map threadPools = config.getThreadPoolConfigMap();
        String[] poolIds = (String[])threadPools.keySet().toArray(new String[threadPools.size()]);
        SelectItem[] options = getModOptions(poolIds);
        
        handlerCtx.setOutputValue("ThreadPools", options);
    }
    
    /**
     *	<p> This handler returns the list of Thread Pools for populating 
     *  <p> the table in Thread Pools top level page
     *  <p> Input  value: "ConfigName"   -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "selectedRows" -- Type: <code> java.util.List</code></p>
     *  <p> Output  value: "Result"      -- Type: <code> java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getThreadPoolsList",
        input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="Result", type=java.util.List.class)}
     )
    public static void getThreadPoolsList(HandlerContext handlerCtx){
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));      
        Iterator iter = config.getThreadPoolConfigMap().values().iterator();
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
       
        List result = new ArrayList();
        if (iter != null){
            while(iter.hasNext()){
                ThreadPoolConfig threadConfig = (ThreadPoolConfig) iter.next();
                HashMap oneRow = new HashMap();
                String name = threadConfig.getThreadPoolId();
                oneRow.put("name", name);
                oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
                String maxSize = threadConfig.getMaxThreadPoolSize();
                String minSize = threadConfig.getMinThreadPoolSize();
                oneRow.put("maxSize", (maxSize == null) ? " ": maxSize);
                oneRow.put("minSize", (minSize == null) ? " ": minSize);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("Result", result);
    }
    
    /**
     *	<p> This handler takes in selected rows, and removes selected Listeners
     *  <p> Input  value: "selectedRows"  -- Type: <code> java.util.List</code></p>
     *  <p> Input  value: "ConfigName"    -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteThreadPools",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName",   type=String.class, required=true)}
    )
    public static void deleteThreadPools(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                config.removeThreadPoolConfig(name);
            }
        }catch(Exception ex){
           GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      ORB Page </p>
     *  <p> Input  value: "cName               -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "ThreadPools"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxMsgSize"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "TotalConns"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "IIOPClient"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getOrbValues",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},
    output={
        @HandlerOutput(name="ThreadPools",     type=String.class), 
        @HandlerOutput(name="MaxMsgSize",      type=String.class),
        @HandlerOutput(name="TotalConns",      type=String.class),
        @HandlerOutput(name="IIOPClient",      type=Boolean.class),
        @HandlerOutput(name="Properties",      type=Map.class)})
        
        public static void getOrbValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        IIOPServiceConfig iiopConfig = config.getIIOPServiceConfig();
        ORBConfig orbConfig = iiopConfig.getORBConfig();
        String threadPoolId = orbConfig.getUseThreadPoolIds();
        String maxMsgSize = orbConfig.getMessageFragmentSize();
        String totalConns = orbConfig.getMaxConnections();
        boolean iiopClient = iiopConfig.getClientAuthenticationRequired();
        Map<String, String> props = orbConfig.getProperties();
        
        handlerCtx.setOutputValue("ThreadPools", threadPoolId);
        handlerCtx.setOutputValue("MaxMsgSize", maxMsgSize);
        handlerCtx.setOutputValue("TotalConns", totalConns);
        handlerCtx.setOutputValue("IIOPClient", iiopClient);
        handlerCtx.setOutputValue("Properties", props);
    }
   
    /**
     *	<p> This handler returns the default values for all the attributes in 
     *      ORB Page </p>
     *	<p> Output value: "ThreadPools"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxMsgSize"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "TotalConns"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "IIOPClient"         -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultOrbValues",
    output={
        @HandlerOutput(name="ThreadPools",      type=String.class), 
        @HandlerOutput(name="MaxMsgSize",       type=String.class),
        @HandlerOutput(name="TotalConns",       type=String.class),
        @HandlerOutput(name="IIOPClient",       type=Boolean.class)})
        
        public static void getDefaultOrbValues(HandlerContext handlerCtx) {
        
        Map<String, String> orbAttrMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(ORBConfig.J2EE_TYPE);
        Map<String, String> iiopAttrMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(IIOPServiceConfig.J2EE_TYPE);
        handlerCtx.setOutputValue("ThreadPools", orbAttrMap.get("use-thread-pool-ids"));
        handlerCtx.setOutputValue("MaxMsgSize", orbAttrMap.get("message-fragment-size"));
        handlerCtx.setOutputValue("TotalConns", orbAttrMap.get("max-connections"));
        handlerCtx.setOutputValue("IIOPClient", Boolean.valueOf(iiopAttrMap.get("client-authentication-required")));
        
    }
    
    /**
     *	<p> This handler saves the values for all the attributes in 
     *      ORB Page </p>
     *  <p> Input value: "ConfigName              -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "ThreadPools"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MaxMsgSize"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "TotalConns"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "IIOPClient"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "AddProps"           -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"        -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveOrbValues",
    input={
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="ThreadPools",     type=String.class), 
        @HandlerInput(name="MaxMsgSize",      type=String.class),
        @HandlerInput(name="TotalConns",      type=String.class),
        @HandlerInput(name="IIOPClient",      type=Boolean.class),
        @HandlerInput(name="AddProps",        type=Map.class),
        @HandlerInput(name="RemoveProps",     type=ArrayList.class)})
        
        public static void saveOrbValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        try{
            IIOPServiceConfig iiopConfig = config.getIIOPServiceConfig();
            ORBConfig orbConfig = iiopConfig.getORBConfig();
            orbConfig.setUseThreadPoolIds((String)handlerCtx.getInputValue("ThreadPools"));
            orbConfig.setMessageFragmentSize((String)handlerCtx.getInputValue("MaxMsgSize"));
            orbConfig.setMaxConnections((String)handlerCtx.getInputValue("TotalConns"));
            iiopConfig.setClientAuthenticationRequired(((Boolean)handlerCtx.getInputValue("IIOPClient")).booleanValue());
            AMXUtil.editProperties(handlerCtx, orbConfig);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Thread Pools Page </p>
     *  <p> Input  value: "ConfigName               -- Type: <code>java.lang.String</code></p>
     *	<p> Input  value: "ThreadPoolId"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MinPoolSize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxPoolSize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "IdleTimeout"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NoWorkQ"            -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getThreadPoolValues",
    input={
        @HandlerInput(name="Edit",       type=Boolean.class, required=true),
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="ThreadPoolId",      type=String.class, required=true) },
    output={
        @HandlerOutput(name="MinPoolSize",      type=String.class),
        @HandlerOutput(name="MaxPoolSize",      type=String.class),
        @HandlerOutput(name="IdleTimeout",      type=String.class),
        @HandlerOutput(name="NoWorkQ",          type=String.class)})
        
        public static void getThreadPoolValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
        if(!edit){
            getDefaultThreadPoolValues(handlerCtx);
            return;
        }
        String threadPoolId = (String)handlerCtx.getInputValue("ThreadPoolId");
        Map threadPoolsMap = config.getThreadPoolConfigMap();
        ThreadPoolConfig threadConfig = (ThreadPoolConfig)threadPoolsMap.get(threadPoolId);
        String minPoolSize = threadConfig.getMinThreadPoolSize();
        String maxPoolSize = threadConfig.getMaxThreadPoolSize();
        String idleTimeout = threadConfig.getIdleThreadTimeoutInSeconds();
        String noWorkQ = threadConfig.getNumWorkQueues();
        
        handlerCtx.setOutputValue("MinPoolSize", minPoolSize);
        handlerCtx.setOutputValue("MaxPoolSize", maxPoolSize);
        handlerCtx.setOutputValue("IdleTimeout", idleTimeout);
        handlerCtx.setOutputValue("NoWorkQ", noWorkQ);
    }
    
    /**
     *	<p> This handler returns the default values for all the attributes in 
     *      Edit Thread Pools Page </p>
     *  <p> Input  value: "ConfigName               -- Type: <code>java.lang.String</code></p>
     *	<p> Input  value: "ThreadPoolId"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MinPoolSize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MaxPoolSize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "IdleTimeout"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NoWorkQ"            -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultThreadPoolValues",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="ThreadPoolId",      type=String.class, required=true) },
    output={
        @HandlerOutput(name="MinPoolSize",      type=String.class),
        @HandlerOutput(name="MaxPoolSize",      type=String.class),
        @HandlerOutput(name="IdleTimeout",      type=String.class),
        @HandlerOutput(name="NoWorkQ",          type=String.class)})
        
        public static void getDefaultThreadPoolValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        Map<String, String> poolsAttrMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(ThreadPoolConfig.J2EE_TYPE);       

        handlerCtx.setOutputValue("MinPoolSize", poolsAttrMap.get("min-thread-pool-size"));
        handlerCtx.setOutputValue("MaxPoolSize", poolsAttrMap.get("max-thread-pool-size"));
        handlerCtx.setOutputValue("IdleTimeout", poolsAttrMap.get("idle-thread-timeout-in-seconds"));
        handlerCtx.setOutputValue("NoWorkQ", poolsAttrMap.get("num-work-queues"));
    }
    
    /**
     *	<p> This handler sets the values for all the attributes in 
     *      Edit Thread Pools Page </p>
     *  <p> Input  value: "ConfigName         -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "ThreadPoolId"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Edit"               -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "MinPoolSize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MaxPoolSize"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "IdleTimeout"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "NoWorkQ"            -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveThreadPoolValues",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="ThreadPoolId",      type=String.class, required=true),
        @HandlerInput(name="Edit",              type=Boolean.class, required=true),
        @HandlerInput(name="MinPoolSize",       type=String.class),
        @HandlerInput(name="MaxPoolSize",       type=String.class),
        @HandlerInput(name="IdleTimeout",       type=String.class),
        @HandlerInput(name="NoWorkQ",           type=String.class)})
        
        public static void saveThreadPoolValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        String threadPoolId = (String)handlerCtx.getInputValue("ThreadPoolId");
        try{
            ThreadPoolConfig threadConfig = null;
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                threadConfig = config.createThreadPoolConfig(threadPoolId, new HashMap());
            } else {
                Map threadPoolsMap = config.getThreadPoolConfigMap();
                threadConfig = (ThreadPoolConfig)threadPoolsMap.get(threadPoolId);
            }
            threadConfig.setMinThreadPoolSize((String)handlerCtx.getInputValue("MinPoolSize"));
            threadConfig.setMaxThreadPoolSize((String)handlerCtx.getInputValue("MaxPoolSize"));
            threadConfig.setIdleThreadTimeoutInSeconds((String)handlerCtx.getInputValue("IdleTimeout"));
            threadConfig.setNumWorkQueues((String)handlerCtx.getInputValue("NoWorkQ"));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Access Log Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Rotation"       -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Output value: "Policy"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Interval"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Suffix"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Format"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAccessLogSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Rotation",      type=Boolean.class),
        @HandlerOutput(name="Policy",  type=String.class),
        @HandlerOutput(name="Interval",    type=String.class),
        @HandlerOutput(name="Suffix",       type=String.class),
        @HandlerOutput(name="Format", type=String.class)})
        
        public static void getAccessLogSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	AccessLogConfig al = config.getHTTPServiceConfig().getAccessLogConfig();
        
        String policy="";
        String interval="";
        String suffix="";
        String format="";
        boolean rotation = true;
        
        if (al != null){
             rotation = al.getRotationEnabled();
             policy = al.getRotationPolicy();
             interval = al.getRotationIntervalInMinutes();
             suffix = al.getRotationSuffix();
             format = al.getFormat();
        }else{
            Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(AccessLogConfig.J2EE_TYPE);
            policy = (String) defaultMap.get("rotation-policy");
            interval = (String)defaultMap.get("rotation-interval-in-minutes");
            suffix = (String)defaultMap.get("rotation-suffix");
            format = (String)defaultMap.get("format");
            String rotationKey = (String) defaultMap.get("rotation-enabled");
            rotation = (rotationKey == null) ? false : Boolean.valueOf(rotationKey);
        }
        handlerCtx.setOutputValue("Rotation", rotation);
        handlerCtx.setOutputValue("Policy", policy);
        handlerCtx.setOutputValue("Interval", interval);
        handlerCtx.setOutputValue("Suffix", suffix);
        handlerCtx.setOutputValue("Format", format);        
        
    }   
    
/**
     *	<p> This handler returns the default values for all the attributes in the
     *      Access Log Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Rotation"       -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Output value: "Policy"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Interval"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Suffix"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Format"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAccessLogDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Rotation",      type=Boolean.class),
        @HandlerOutput(name="Policy",  type=String.class),
        @HandlerOutput(name="Interval",    type=String.class),
        @HandlerOutput(name="Suffix",       type=String.class),
        @HandlerOutput(name="Format", type=String.class)})
        
        public static void getAccessLogDefaultSettings(HandlerContext handlerCtx) {
        
        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(AccessLogConfig.J2EE_TYPE);
        String policy = (String) defaultMap.get("rotation-policy");
        String interval = (String)defaultMap.get("rotation-interval-in-minutes");
        String suffix = (String)defaultMap.get("rotation-suffix");
        String format = (String)defaultMap.get("format");
        String rotationKey = (String) defaultMap.get("rotation-enabled");
        boolean rotation = (rotationKey == null) ? false : Boolean.valueOf(rotationKey);
        
        handlerCtx.setOutputValue("Rotation", rotation);
        handlerCtx.setOutputValue("Policy", policy);
        handlerCtx.setOutputValue("Interval", interval);
        handlerCtx.setOutputValue("Suffix", suffix);
        handlerCtx.setOutputValue("Format", format);        
        
    }   
    
/**
     *	<p> This handler returns the default values for all the attributes in the
     *      Access Log Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Rotation"       -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Input value: "Policy"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Interval"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Suffix"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Format"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveAccessLogSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="Rotation",      type=Boolean.class),
        @HandlerInput(name="Policy",  type=String.class),
        @HandlerInput(name="Interval",    type=String.class),
        @HandlerInput(name="Suffix",       type=String.class),
        @HandlerInput(name="Format", type=String.class)})
        
        public static void saveAccessLogSettings(HandlerContext handlerCtx) {
        
        try{
            ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
            AccessLogConfig al = config.getHTTPServiceConfig().getAccessLogConfig();
            if (al == null){
                al = config.getHTTPServiceConfig().createAccessLogConfig(new HashMap());
            }
            Boolean temp = (Boolean) handlerCtx.getInputValue("Rotation");
            al.setRotationEnabled( (temp == null) ? false : temp);
            al.setRotationPolicy(((String)handlerCtx.getInputValue("Policy")));
            al.setRotationIntervalInMinutes(((String)handlerCtx.getInputValue("Interval")));
            al.setRotationSuffix(((String)handlerCtx.getInputValue("Suffix")));
            al.setFormat(((String)handlerCtx.getInputValue("Format")));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }   
    
/**
     *	<p> This handler returns the values for all the attributes in the
     *      Request Processing Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Initial"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Increment"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Buffer"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getRequestProcessingSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Count",  type=String.class),
        @HandlerOutput(name="Initial",    type=String.class),
        @HandlerOutput(name="Increment",       type=String.class),
        @HandlerOutput(name="Timeout", type=String.class),
        @HandlerOutput(name="Buffer", type=String.class)})
        
        public static void getRequestProcessingSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	RequestProcessingConfig rp = config.getHTTPServiceConfig().getRequestProcessingConfig();
        String count = rp.getThreadCount();
        String initial = rp.getInitialThreadCount();
        String increment = rp.getThreadIncrement();
        String timeout = rp.getRequestTimeoutInSeconds();
        String buffer = rp.getHeaderBufferLengthInBytes();
        handlerCtx.setOutputValue("Count", count);
        handlerCtx.setOutputValue("Initial", initial);
        handlerCtx.setOutputValue("Increment", increment);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Buffer", buffer);        
        
    }   
    

    /**
     *	<p> This handler returns the default values for all the attributes in the
     *      Request Processing Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Initial"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Increment"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Buffer"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getRequestProcessingDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Count",  type=String.class),
        @HandlerOutput(name="Initial",    type=String.class),
        @HandlerOutput(name="Increment",       type=String.class),
        @HandlerOutput(name="Timeout", type=String.class),
        @HandlerOutput(name="Buffer", type=String.class)})
        
        public static void getRequestProcessingDefaultSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	RequestProcessingConfig rp = config.getHTTPServiceConfig().getRequestProcessingConfig();
        String count = rp.getDefaultValue(RequestProcessingConfigKeys.THREAD_COUNT_KEY);
        String initial = rp.getDefaultValue(RequestProcessingConfigKeys.INITIAL_THREAD_COUNT_KEY);
        String increment = rp.getDefaultValue(RequestProcessingConfigKeys.THREAD_INCREMENT_KEY);
        String timeout = rp.getDefaultValue(RequestProcessingConfigKeys.REQUEST_TIMEOUT_IN_SECONDS_KEY);
        String buffer = rp.getDefaultValue(RequestProcessingConfigKeys.HEADER_BUFFER_LENGTH_IN_BYTES_KEY);
        handlerCtx.setOutputValue("Count", count);
        handlerCtx.setOutputValue("Initial", initial);
        handlerCtx.setOutputValue("Increment", increment);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Buffer", buffer);        
        
    }   
    
/**
     *	<p> This handler saves the values for all the attributes in the
     *      Request Processing Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Initial"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Increment"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Timeout"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Buffer"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveRequestProcessingSettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="Count",  type=String.class),
        @HandlerInput(name="Initial",    type=String.class),
        @HandlerInput(name="Increment",       type=String.class),
        @HandlerInput(name="Timeout", type=String.class),
        @HandlerInput(name="Buffer", type=String.class)})
        
        public static void saveRequestProcessingSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            RequestProcessingConfig rp = config.getHTTPServiceConfig().getRequestProcessingConfig();
            rp.setThreadCount(((String)handlerCtx.getInputValue("Count")));
            rp.setInitialThreadCount(((String)handlerCtx.getInputValue("Initial")));
            rp.setThreadIncrement(((String)handlerCtx.getInputValue("Increment")));
            rp.setRequestTimeoutInSeconds(((String)handlerCtx.getInputValue("Timeout")));
            rp.setHeaderBufferLengthInBytes(((String)handlerCtx.getInputValue("Buffer"))); 
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }   
    
/**
     *	<p> This handler returns the values for all the attributes in the
     *      Keep Alive Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Connections"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getKeepAliveSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Count",  type=String.class),
        @HandlerOutput(name="Connections",    type=String.class),
        @HandlerOutput(name="Timeout",       type=String.class)})
        
        public static void getKeepAliveSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	KeepAliveConfig rp = config.getHTTPServiceConfig().getKeepAliveConfig();
        String count = rp.getThreadCount();
        String connections = rp.getMaxConnections();
        String timeout = rp.getTimeoutInSeconds();
        handlerCtx.setOutputValue("Count", count);
        handlerCtx.setOutputValue("Connections", connections);
        handlerCtx.setOutputValue("Timeout", timeout);   
        
    } 
    
/**
     *	<p> This handler returns the default values for all the attributes in the
     *      Keep Alive Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Connections"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Timeout"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getKeepAliveDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Count",  type=String.class),
        @HandlerOutput(name="Connections",    type=String.class),
        @HandlerOutput(name="Timeout",       type=String.class)})
        
        public static void getKeepAliveDefaultSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	KeepAliveConfig rp = config.getHTTPServiceConfig().getKeepAliveConfig();
        String count = rp.getDefaultValue(KeepAliveConfigKeys.THREAD_COUNT_KEY );
        String connections = rp.getDefaultValue(KeepAliveConfigKeys.MAX_CONNECTIONS_KEY );
        String timeout = rp.getDefaultValue(KeepAliveConfigKeys.TIMEOUT_IN_SECONDS_KEY );
        handlerCtx.setOutputValue("Count", count);
        handlerCtx.setOutputValue("Connections", connections);
        handlerCtx.setOutputValue("Timeout", timeout);   
        
    } 
    
/**
     *	<p> This handler saves the values for all the attributes in the
     *      Keep Alive Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Connections"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Timeout"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveKeepAliveSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="Count",  type=String.class),
        @HandlerInput(name="Connections",    type=String.class),
        @HandlerInput(name="Timeout",       type=String.class)})
        
        public static void saveKeepAliveSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	KeepAliveConfig rp = config.getHTTPServiceConfig().getKeepAliveConfig();
        rp.setThreadCount(((String)handlerCtx.getInputValue("Count")));
        rp.setMaxConnections(((String)handlerCtx.getInputValue("Connections")));
        rp.setTimeoutInSeconds(((String)handlerCtx.getInputValue("Timeout")));
        
    } 
    
    
    /**
     *	<p> This handler returns the values of properties in HttpService </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getHttpService",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},
    output={
        @HandlerOutput(name="accessLogBufferSize", type=String.class),
        @HandlerOutput(name="accessLogWriteInterval", type=String.class),
        @HandlerOutput(name="accessLoggingEnabled", type=Boolean.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getHttpService(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        HTTPServiceConfig hConfig = config.getHTTPServiceConfig();
        
        try{
            
            handlerCtx.setOutputValue("Properties", AMXUtil.getNonSkipPropertiesMap(hConfig, httpServiceSkipPropsList));
            Map origProps = hConfig.getProperties();
            handlerCtx.setOutputValue("accessLogBufferSize", origProps.get("accessLogBufferSize"));
            handlerCtx.setOutputValue("accessLogWriteInterval", origProps.get("accessLogWriteInterval"));
            String alog = (String) origProps.get("accessLoggingEnabled");
            Boolean accessLoggingEnabled = true;
            if ( GuiUtil.isEmpty(alog))
                accessLoggingEnabled = true;
            else
            accessLoggingEnabled = (alog.equals("true")) ? true: false;
            
            handlerCtx.setOutputValue("accessLoggingEnabled", accessLoggingEnabled);
                    
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    } 
    
    
    /**
     *	<p> This handler saves the Http Service properties 
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveHttpService",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="newProps", type=Map.class),  
        @HandlerInput(name="accessLogBufferSize", type=String.class),
        @HandlerInput(name="accessLogWriteInterval", type=String.class),
        @HandlerInput(name="accessLoggingEnabled",     type=Boolean.class)})
        
        public static void saveHttpService(HandlerContext handlerCtx) {
        
        
        try{
            ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
            HTTPServiceConfig hConfig = config.getHTTPServiceConfig();
            Map newProps = (Map)handlerCtx.getInputValue("newProps");
        
            AMXUtil.updateProperties(hConfig, newProps, httpServiceSkipPropsList);
            
            AMXUtil.changeProperty(hConfig, "accessLogBufferSize", (String)handlerCtx.getInputValue("accessLogBufferSize"));
            AMXUtil.changeProperty(hConfig, "accessLogWriteInterval", (String)handlerCtx.getInputValue("accessLogWriteInterval"));
            AMXUtil.changeProperty(hConfig, "accessLoggingEnabled", ""+handlerCtx.getInputValue("accessLoggingEnabled"));
            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }   

    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Jms  Hosts Page </p>
     *  <p> Input  value: "JmsHostName"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Host"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Port"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AdminUser"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AdminPwd"       -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJmsHostValues",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="JmsHostName", type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true) },
    output={
        @HandlerOutput(name="Host",        type=String.class),
        @HandlerOutput(name="Port",        type=String.class),
        @HandlerOutput(name="AdminUser",   type=String.class),
        @HandlerOutput(name="AdminPwd",    type=String.class)})
        
        public static void getJmsDestinationValues(HandlerContext handlerCtx) {
        try{
        Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
        if(!edit){
            getDefaultJmsHostAttributes(handlerCtx);
            return;
        }
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        String jmsHostName = (String)handlerCtx.getInputValue("JmsHostName");
        
        Map jmsHostsMap = config.getJMSServiceConfig().getJMSHostConfigMap();
        JMSHostConfig jmsHostConfig = (JMSHostConfig)jmsHostsMap.get(jmsHostName);
        String host = jmsHostConfig.getHost();
        String port = jmsHostConfig.getPort();
        String adminUser = jmsHostConfig.getAdminUserName();
        String adminPwd = jmsHostConfig.getAdminPassword();
        
        handlerCtx.setOutputValue("Host", host);
        handlerCtx.setOutputValue("Port", port);
        handlerCtx.setOutputValue("AdminUser", adminUser);
        handlerCtx.setOutputValue("AdminPwd", adminPwd);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }           
    }
       

        private static void getDefaultJmsHostAttributes(HandlerContext handlerCtx){     
        Map defaultMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(JMSHostConfig.J2EE_TYPE);
             
        handlerCtx.setOutputValue("Host", defaultMap.get("host"));
        handlerCtx.setOutputValue("Port", defaultMap.get("port"));
        handlerCtx.setOutputValue("AdminUser", defaultMap.get("admin-user-name"));
        handlerCtx.setOutputValue("AdminPwd", defaultMap.get("admin-password"));
        
    }    
    
    /**
     *	<p> This handler returns the default values for all the attributes in 
     *      Edit Jms Host Page </p>
     *  <p> Input  value: "ConfigName"     -- Type: <code>java.lang.String</code></p>
     *	<p> Input  value: "JmsHostName"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Host"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Port"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AdminUser"      -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AdminPwd"       -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultJmsHostValues",
    input={
        @HandlerInput(name="ConfigName",       type=String.class, required=true),
        @HandlerInput(name="JmsHostName",      type=String.class, required=true) },
    output={
        @HandlerOutput(name="Host",        type=String.class),
        @HandlerOutput(name="Port",        type=String.class),
        @HandlerOutput(name="AdminUser",   type=String.class),
        @HandlerOutput(name="AdminPwd",    type=String.class)})
        
        public static void getDefaultJmsDestinationValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        String jmsHostName = (String)handlerCtx.getInputValue("JmsHostName");
        Map jmsHostsMap = config.getJMSServiceConfig().getJMSHostConfigMap();
        JMSHostConfig jmsHostConfig = (JMSHostConfig)jmsHostsMap.get(jmsHostName);
        
        String host = jmsHostConfig.getDefaultValue(JMSHostConfigKeys.HOST_KEY);
        String port = jmsHostConfig.getDefaultValue(JMSHostConfigKeys.PORT_KEY);
        String adminUser = jmsHostConfig.getDefaultValue(JMSHostConfigKeys.ADMIN_USER_NAME_KEY);
        String adminPwd = jmsHostConfig.getDefaultValue(JMSHostConfigKeys.ADMIN_PASSWORD_KEY);
        
        handlerCtx.setOutputValue("Host", host);
        handlerCtx.setOutputValue("Port", port);
        handlerCtx.setOutputValue("AdminUser", adminUser);
        handlerCtx.setOutputValue("AdminPwd", adminPwd);
        
    }    
    
     /**
     *	<p> This handler sets the values for all the attributes in 
     *      Edit Jms Host Page </p>
     *  <p> Input value: "ConfigName       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "JmsHostName"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Host"            -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Port"            -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AdminUser"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AdminPwd"        -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveJmsHostValues",
    input={
        @HandlerInput(name="ConfigName",       type=String.class, required=true),
        @HandlerInput(name="JmsHostName",      type=String.class, required=true),
        @HandlerInput(name="Edit", type=Boolean.class, required=true),
        @HandlerInput(name="Host",             type=String.class),
        @HandlerInput(name="Port",             type=String.class),
        @HandlerInput(name="AdminUser",        type=String.class),
        @HandlerInput(name="AdminPwd",         type=String.class)})
        
        public static void saveJmsHostValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        String jmsHostName = (String)handlerCtx.getInputValue("JmsHostName");
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            
            if(!edit){
               /** refer to issue#1385  Can switch back to use AMX after this issue is resolved.

                JMSHostConfig host = config.getJMSServiceConfig().createJMSHostConfig(jmsHostName,null);
                host.setHost((String)handlerCtx.getInputValue("Host"));
                host.setPort((String)handlerCtx.getInputValue("Port"));
                host.setAdminUserName((String)handlerCtx.getInputValue("AdminUser"));
                host.setAdminPassword((String)handlerCtx.getInputValue("AdminPwd"));
                */
                AttributeList attrList = new AttributeList();
                attrList.add(new Attribute("name",jmsHostName));
                attrList.add(new Attribute("host",(String)handlerCtx.getInputValue("Host")));
                attrList.add(new Attribute("port",(String)handlerCtx.getInputValue("Port")));
                attrList.add(new Attribute("admin-user-name",(String)handlerCtx.getInputValue("AdminUser")));
                attrList.add(new Attribute("admin-password",(String)handlerCtx.getInputValue("AdminPwd")));

                Object[] params = {attrList, null, configName};
                String[] types ={"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};
                JMXUtil.invoke("com.sun.appserv:category=config,type=configs", "createJmsHost", params, types);

                return;
                
            }
            Map jmsHostsMap = config.getJMSServiceConfig().getJMSHostConfigMap();
            JMSHostConfig jmsHostConfig = (JMSHostConfig)jmsHostsMap.get(jmsHostName);            
            jmsHostConfig.setHost((String)handlerCtx.getInputValue("Host"));
            jmsHostConfig.setPort((String)handlerCtx.getInputValue("Port"));
            jmsHostConfig.setAdminUserName((String)handlerCtx.getInputValue("AdminUser"));
            jmsHostConfig.setAdminPassword((String)handlerCtx.getInputValue("AdminPwd"));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
/**
     *	<p> This handler returns the values for all the attributes in the
     *      Connection Pool Config Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Queue"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Receive"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Send"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getConnectionPoolSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Count",  type=String.class),
        @HandlerOutput(name="Queue",    type=String.class),
        @HandlerOutput(name="Receive",       type=String.class),
        @HandlerOutput(name="Send",       type=String.class)})
        
        public static void getConnectionPoolSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	ConnectionPoolConfig cp = config.getHTTPServiceConfig().getConnectionPoolConfig();
        String count = cp.getMaxPendingCount();
        String queue = cp.getQueueSizeInBytes();
        String receive = cp.getReceiveBufferSizeInBytes();
        String send = cp.getSendBufferSizeInBytes();
        handlerCtx.setOutputValue("Count", count);
        handlerCtx.setOutputValue("Queue", queue);
        handlerCtx.setOutputValue("Receive", receive);   
        handlerCtx.setOutputValue("Send", send);   
        
    }     
    
    
/**
     *	<p> This handler returns the default values for all the attributes in the
     *      Connection Pool Config Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Queue"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Receive"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Send"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getConnectionPoolDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Count",  type=String.class),
        @HandlerOutput(name="Queue",    type=String.class),
        @HandlerOutput(name="Receive",       type=String.class),
        @HandlerOutput(name="Send",       type=String.class)})
        
        public static void getConnectionPoolDefaultSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	ConnectionPoolConfig cp = config.getHTTPServiceConfig().getConnectionPoolConfig();
        String count = cp.getDefaultValue(ConnectionPoolConfigKeys.MAX_PENDING_COUNT_KEY);
        String queue = cp.getDefaultValue(ConnectionPoolConfigKeys.QUEUE_SIZE_IN_BYTES_KEY);
        String receive = cp.getDefaultValue(ConnectionPoolConfigKeys.RECEIVE_BUFFER_SIZE_IN_BYTES_KEY);
        String send = cp.getDefaultValue(ConnectionPoolConfigKeys.SEND_BUFFER_SIZE_IN_BYTES_KEY);
        handlerCtx.setOutputValue("Count", count);
        handlerCtx.setOutputValue("Queue", queue);
        handlerCtx.setOutputValue("Receive", receive);   
        handlerCtx.setOutputValue("Send", send);   
        
    }     
    
/**
     *	<p> This handler saves the values for all the attributes in the
     *      Connection Pool Config Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Count"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Queue"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Receive"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Send"     -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveConnectionPoolSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="Count",  type=String.class),
        @HandlerInput(name="Queue",    type=String.class),
        @HandlerInput(name="Receive",       type=String.class),
        @HandlerInput(name="Send",       type=String.class)})
        
        public static void saveConnectionPoolSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            ConnectionPoolConfig cp = config.getHTTPServiceConfig().getConnectionPoolConfig();
            cp.setMaxPendingCount((String)handlerCtx.getInputValue("Count"));
            cp.setQueueSizeInBytes((String)handlerCtx.getInputValue("Queue"));
            cp.setReceiveBufferSizeInBytes((String)handlerCtx.getInputValue("Receive"));
            cp.setSendBufferSizeInBytes((String)handlerCtx.getInputValue("Send"));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }     
    
/**
     *	<p> This handler returns the values for all the attributes in the
     *      HTTP Protocol Config Page </p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Version"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DNS"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "SSL"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Forced"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Default"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getHttpProtocolSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Version",  type=String.class),
        @HandlerOutput(name="DNS",      type=Boolean.class),
        @HandlerOutput(name="SSL",      type=Boolean.class),
        @HandlerOutput(name="Forced",   type=String.class),
        @HandlerOutput(name="Default",  type=String.class)})
        
        public static void getHttpProtocolSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	HTTPProtocolConfig hp = config.getHTTPServiceConfig().getHTTPProtocolConfig();
        String version = hp.getVersion();
        boolean dns = hp.getDNSLookupEnabled();
        boolean ssl = hp.getSSLEnabled();
        String forced = hp.getForcedType();
        String defaultResponse = hp.getDefaultType();
        handlerCtx.setOutputValue("Version", version);
        handlerCtx.setOutputValue("DNS", dns);
        handlerCtx.setOutputValue("SSL", ssl);   
        handlerCtx.setOutputValue("Forced", forced);    
        handlerCtx.setOutputValue("Default", defaultResponse);   
        
    }     
    
/**
     *	<p> This handler returns the default values for all the attributes in the
     *      HTTP Protocol Config Page </p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Version"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DNS"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "SSL"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Forced"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Default"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getHttpProtocolDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Version",  type=String.class),
        @HandlerOutput(name="DNS",      type=Boolean.class),
        @HandlerOutput(name="SSL",      type=Boolean.class),
        @HandlerOutput(name="Forced",   type=String.class),
        @HandlerOutput(name="Default",  type=String.class)})
        
        public static void getHttpProtocolDefaultSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	HTTPProtocolConfig hp = config.getHTTPServiceConfig().getHTTPProtocolConfig();
        String version = hp.getDefaultValue(HTTPProtocolConfigKeys.VERSION_KEY);
        String dns = hp.getDefaultValue(HTTPProtocolConfigKeys.DNS_LOOKUP_ENABLED_KEY );
        String ssl = hp.getDefaultValue(HTTPProtocolConfigKeys.SSL_ENABLED_KEY);
        String forced = hp.getDefaultValue(HTTPProtocolConfigKeys.FORCED_TYPE_KEY);
        String defaultResponse = hp.getDefaultValue(HTTPProtocolConfigKeys.DEFAULT_TYPE_KEY);
        handlerCtx.setOutputValue("Version", version);
        if(dns.equals("true")) {
            handlerCtx.setOutputValue("DNS", true);    
        } else {
            handlerCtx.setOutputValue("DNS", false);
        }   
       if(ssl.equals("true")) {
            handlerCtx.setOutputValue("SSL", true);    
        } else {
            handlerCtx.setOutputValue("SSL", false);
        }           
        handlerCtx.setOutputValue("Forced", forced);    
        handlerCtx.setOutputValue("Default", defaultResponse);   
        
    }         
    
/**
     *	<p> This handler saves the values for all the attributes in the
     *      HTTP Protocol Config Page </p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Version"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "DNS"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "SSL"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Forced"    -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Default"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveHttpProtocolSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="Version",  type=String.class),
        @HandlerInput(name="DNS",      type=Boolean.class),
        @HandlerInput(name="SSL",      type=Boolean.class),
        @HandlerInput(name="Forced",   type=String.class),
        @HandlerInput(name="Default",  type=String.class)})
        
        public static void saveHttpProtocolSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            HTTPProtocolConfig hp = config.getHTTPServiceConfig().getHTTPProtocolConfig();
            hp.setVersion(((String)handlerCtx.getInputValue("Version")));
            hp.setDNSLookupEnabled(((Boolean)handlerCtx.getInputValue("DNS")).booleanValue());
            hp.setSSLEnabled(((Boolean)handlerCtx.getInputValue("SSL")).booleanValue());
            hp.setForcedType(((String)handlerCtx.getInputValue("Forced")));
            hp.setDefaultType(((String)handlerCtx.getInputValue("Default")));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }    
    
/**
     *	<p> This handler returns the values for all the attributes in the
     *      HTTP File Caching Config Page </p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Globally"   -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "FileTransmission"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Age"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "FileCount"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HashSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MedLimit"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MedSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SmLimit"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SmSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "FileCaching"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getHttpFileCachingSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Globally",  type=Boolean.class),
        @HandlerOutput(name="FileTransmission",      type=Boolean.class),
        @HandlerOutput(name="Age",          type=String.class),
        @HandlerOutput(name="FileCount",    type=String.class),        			
        @HandlerOutput(name="HashSize",     type=String.class),
        @HandlerOutput(name="MedLimit",     type=String.class),
        @HandlerOutput(name="MedSize",      type=String.class),
        @HandlerOutput(name="SmLimit",      type=String.class),
        @HandlerOutput(name="SmSize",  	    type=String.class),
        @HandlerOutput(name="FileCaching",  type=String.class)})
        
        public static void getHttpFileCachingSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	HTTPFileCacheConfig hp = config.getHTTPServiceConfig().getHTTPFileCacheConfig();
        boolean globally = hp.getGloballyEnabled();
        boolean fileTransmission = hp.getFileTransmissionEnabled();
        String age = hp.getMaxAgeInSeconds();
        String fileCount = hp.getMaxFilesCount();
        String hashSize = hp.getHashInitSize();
        String medLimit = hp.getMediumFileSizeLimitInBytes();
        String medSize = hp.getMediumFileSpaceInBytes();
        String smLimit = hp.getSmallFileSizeLimitInBytes();
        String smSize = hp.getSmallFileSpaceInBytes();
        boolean fileCaching = hp.getFileCachingEnabled();
       if(fileCaching == true) {
            handlerCtx.setOutputValue("FileCaching", "ON");    
        } else {
            handlerCtx.setOutputValue("FileCaching", "OFF");
        }              
        handlerCtx.setOutputValue("Globally", globally);
        handlerCtx.setOutputValue("FileTransmission", fileTransmission);
        handlerCtx.setOutputValue("Age", age);   
        handlerCtx.setOutputValue("FileCount", fileCount);    
        handlerCtx.setOutputValue("HashSize", hashSize);   
        handlerCtx.setOutputValue("MedLimit", medLimit);
        handlerCtx.setOutputValue("MedSize", medSize);
        handlerCtx.setOutputValue("SmLimit", smLimit);   
        handlerCtx.setOutputValue("SmSize", smSize);    
        
    }         
    
/**
     *	<p> This handler returns the default values for all the attributes in the
     *      HTTP File Caching Config Page </p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
    *	<p> Output value: "Globally"   -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "FileTransmission"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Age"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "FileCount"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HashSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MedLimit"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MedSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SmLimit"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SmSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "FileCaching"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getHttpFileCachingDefaultSettings",
  input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},        
    output={
        @HandlerOutput(name="Globally",  type=Boolean.class),
        @HandlerOutput(name="FileTransmission",      type=Boolean.class),
        @HandlerOutput(name="Age",          type=String.class),
        @HandlerOutput(name="FileCount",    type=String.class),        			
        @HandlerOutput(name="HashSize",     type=String.class),
        @HandlerOutput(name="MedLimit",     type=String.class),
        @HandlerOutput(name="MedSize",      type=String.class),
        @HandlerOutput(name="SmLimit",      type=String.class),
        @HandlerOutput(name="SmSize",  	    type=String.class),
        @HandlerOutput(name="FileCaching",  type=String.class)})
        
        public static void getHttpFileCachingDefaultSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
	HTTPFileCacheConfig hp = config.getHTTPServiceConfig().getHTTPFileCacheConfig();
        String globally = hp.getDefaultValue(HTTPFileCacheConfigKeys.GLOBALLY_ENABLED_KEY);
        String fileTransmission = hp.getDefaultValue(HTTPFileCacheConfigKeys.FILE_TRANSMISSION_ENABLED_KEY);
        String age = hp.getDefaultValue(HTTPFileCacheConfigKeys.MAX_AGE_IN_SECONDS_KEY);
        String fileCount = hp.getDefaultValue(HTTPFileCacheConfigKeys.MAX_FILES_COUNT_KEY);
        String hashSize = hp.getDefaultValue(HTTPFileCacheConfigKeys.HASH_INIT_SIZE_KEY);
        String medLimit = hp.getDefaultValue(HTTPFileCacheConfigKeys.MEDIUM_FILE_SIZE_LIMIT_IN_BYTES_KEY);
        String medSize = hp.getDefaultValue(HTTPFileCacheConfigKeys.MEDIUM_FILE_SPACE_IN_BYTES_KEY);
        String smLimit = hp.getDefaultValue(HTTPFileCacheConfigKeys.SMALL_FILE_SIZE_LIMIT_IN_BYTES_KEY);
        String smSize = hp.getDefaultValue(HTTPFileCacheConfigKeys.SMALL_FILE_SPACE_IN_BYTES_KEY);
        String fileCaching = hp.getDefaultValue(HTTPFileCacheConfigKeys.FILE_CACHING_ENABLED_KEY);
        if(globally.equals("true")) {
            handlerCtx.setOutputValue("Globally", true);    
        } else {
            handlerCtx.setOutputValue("Globally", false);
        }   
        if(fileTransmission.equals("true")) {
            handlerCtx.setOutputValue("FileTransmission", true);    
        } else {
            handlerCtx.setOutputValue("FileTransmission", false);
        }        
        if(fileCaching.equals("true")) {
            handlerCtx.setOutputValue("FileCaching", "ON");    
        } else {
            handlerCtx.setOutputValue("FileCaching", "OFF");
        }                
        handlerCtx.setOutputValue("Age", age);   
        handlerCtx.setOutputValue("FileCount", fileCount);    
        handlerCtx.setOutputValue("HashSize", hashSize);   
        handlerCtx.setOutputValue("MedLimit", medLimit);
        handlerCtx.setOutputValue("MedSize", medSize);
        handlerCtx.setOutputValue("SmLimit", smLimit);   
        handlerCtx.setOutputValue("SmSize", smSize);      
        
    }        
    
/**
     *	<p> This handler saves the values for all the attributes in the
     *      HTTP File Caching Config Page </p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Globally"   -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "FileTransmission"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Age"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "FileCount"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HashSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MedLimit"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MedSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SmLimit"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SmSize"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "FileCaching"   -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveHttpFileCachingSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),        
        @HandlerInput(name="Globally",  type=Boolean.class),
        @HandlerInput(name="FileTransmission",      type=Boolean.class),
        @HandlerInput(name="Age",          type=String.class),
        @HandlerInput(name="FileCount",    type=String.class),        			
        @HandlerInput(name="HashSize",     type=String.class),
        @HandlerInput(name="MedLimit",     type=String.class),
        @HandlerInput(name="MedSize",      type=String.class),
        @HandlerInput(name="SmLimit",      type=String.class),
        @HandlerInput(name="SmSize",  	    type=String.class),
        @HandlerInput(name="FileCaching",  type=String.class)})
        
        public static void saveHttpFileCachingSettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            HTTPFileCacheConfig hp = config.getHTTPServiceConfig().getHTTPFileCacheConfig();
            hp.setGloballyEnabled(((Boolean)handlerCtx.getInputValue("Globally")).booleanValue());
            hp.setFileTransmissionEnabled(((Boolean)handlerCtx.getInputValue("FileTransmission")).booleanValue());
            hp.setMaxAgeInSeconds(((String)handlerCtx.getInputValue("Age")));
            hp.setMaxFilesCount(((String)handlerCtx.getInputValue("FileCount")));
            hp.setHashInitSize(((String)handlerCtx.getInputValue("HashSize")));
            hp.setMediumFileSizeLimitInBytes(((String)handlerCtx.getInputValue("MedLimit")));
            hp.setMediumFileSpaceInBytes(((String)handlerCtx.getInputValue("MedSize")));
            hp.setSmallFileSizeLimitInBytes(((String)handlerCtx.getInputValue("SmLimit")));
            hp.setSmallFileSpaceInBytes(((String)handlerCtx.getInputValue("SmSize")));
            String fileCaching = (String)handlerCtx.getInputValue("FileCaching");
            if(fileCaching.equals("ON")) {
                hp.setFileCachingEnabled(true);    
            } else {
                hp.setFileCachingEnabled(false);   
            }             
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }         
    
   /**
     *	<p> This handler returns the list of specified config elements for populating the table.
     *  <p> Input  value: "type" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getConfigList",
        input={
            @HandlerInput(name="type", type=String.class, required=true),
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getConfigList(HandlerContext handlerCtx){
        
        String type = (String) handlerCtx.getInputValue("type");
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        boolean isJms=false;
        boolean isVS=false;
        boolean isRealm=false;
        boolean isJacc=false;
        boolean isAudit=false;
        List result = new ArrayList();
        try{
            if (config == null){   //this case should never happen, issue#1966
                handlerCtx.setOutputValue("result", result);
                return;
            }

            Iterator iter = null;
            if("jmsHost".equals(type)){
                iter = config.getJMSServiceConfig().getJMSHostConfigMap().values().iterator();
                isJms=true;
            }else
            if("virtualServer".equals(type)){
                iter = config.getHTTPServiceConfig().getVirtualServerConfigMap().values().iterator();
                isVS=true;
            }
            if("realm".equals(type)){
                iter = config.getSecurityServiceConfig().getAuthRealmConfigMap().values().iterator();
                isRealm=true;
            }        
            if("jacc".equals(type)){
                iter = config.getSecurityServiceConfig().getJACCProviderConfigMap().values().iterator();
                isJacc=true;
            }    
            if("audit".equals(type)){
                iter = config.getSecurityServiceConfig().getAuditModuleConfigMap().values().iterator();
                isAudit=true;
            }             
            List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
            boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;

            if (iter != null){
                while(iter.hasNext()){
                    ConfigElement configE = (ConfigElement) iter.next();
                    HashMap oneRow = new HashMap();
                    String name=configE.getName();                
                    oneRow.put("name", name);
                    oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
                    if(isVS){
                        String state = ((VirtualServerConfig)configE).getState();
                        String host = ((VirtualServerConfig)configE).getHosts();
                        oneRow.put("state", (state == null) ? " ": state);
                        oneRow.put("host", (host == null) ? " ": host);
                    }else
                    if(isJms){
                        String host = ((JMSHostConfig)configE).getHost();
                        String port = ((JMSHostConfig)configE).getPort();
                        oneRow.put("host", (host == null) ? " ": host);
                        oneRow.put("port", (port == null) ? " ": port);
                    }   
                    if(isRealm){
                        String classname = ((AuthRealmConfig)configE).getClassname();
                        oneRow.put("classname", (classname == null) ? " ": classname);
                    }               
                    if(isJacc){
                        String policy = ((JACCProviderConfig)configE).getPolicyProvider();
                        oneRow.put("policy", (policy == null) ? " ": policy);
                    }      
                    if(isAudit){
                        String classname = ((AuditModuleConfig)configE).getClassname();
                        oneRow.put("classname", (classname == null) ? " ": classname);
                    }                      
                    result.add(oneRow);
                }
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
    }    
    
   /**
     *	<p> This handler returns the list of physical destinations.
     *  <p> Input  value: "type" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getPhysicalDestinations",
        input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getPhysicalDestinations(HandlerContext handlerCtx){
        
        ObjectName[] objectNames = null;
        String [] params = {((String)handlerCtx.getInputValue("ConfigName"))};
        String [] types = {"java.lang.String"};
        List result = new ArrayList();
        try{
            objectNames = (ObjectName[])JMXUtil.invoke("com.sun.appserv:type=resources,category=config", "listPhysicalDestinations", params, types);
            
            if (objectNames == null) {
                handlerCtx.setOutputValue("result", result);
                return; //nothing to load..
            }
            List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
            boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;

            for (int i = 0; i < objectNames.length; i++) {
                // getAttributes for the given objectName...
                HashMap oneRow = new HashMap();
                oneRow.put("name", objectNames[i].getKeyProperty("destName"));
                oneRow.put("type", objectNames[i].getKeyProperty("destType"));
                oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(objectNames[i].getKeyProperty("destName"), selectedList): false);
                result.add(oneRow);
            }

           }catch(Exception ex){
               GuiUtil.handleException(handlerCtx, ex);
           }
           handlerCtx.setOutputValue("result", result);
    }        
    
     /**
     *	<p> This handler takes in selected rows, and removes selected config
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteConfig",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="Type", type=String.class, required=true)}
    )
    public static void deleteConfig(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        String type = (String)handlerCtx.getInputValue("Type");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                if("virtualServer".equals(type)){
                    config.getHTTPServiceConfig().removeVirtualServerConfig(name);
                }else
                if("jmsHost".equals(type)){
                     /* refer to issue#1385  ; can switch back to use AMX after that issue is solved.
                    config.getJMSServiceConfig().removeJMSHostConfig(name);
                     */
                    Object[] params = {name, configName};
                    String[] types = {"java.lang.String", "java.lang.String"};
                    JMXUtil.invoke("com.sun.appserv:category=config,type=configs", "deleteJmsHost", params, types);

                }else
                if("realm".equals(type)){
                    config.getSecurityServiceConfig().removeAuthRealmConfig(name);
                }else
                if("jacc".equals(type)){
                    config.getSecurityServiceConfig().removeJACCProviderConfig(name);
                }else
                if("audit".equals(type)){
                    config.getSecurityServiceConfig().removeAuditModuleConfig(name);
                }else
                if("jmsPhysicalDestination".equals(type)){
                    Object[] params = new Object[]{
                        name,
                        (String)oneRow.get("type"),
                        ((String)handlerCtx.getInputValue("ConfigName"))};
                    String[] types = new String[]{
                        "java.lang.String",
                        "java.lang.String",
                        "java.lang.String"};
                    JMXUtil.invoke("com.sun.appserv:type=resources,category=config", "deletePhysicalDestination", params, types);
                }
                
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }  
    
     /**
     *	<p> This handler takes in selected rows, and removes selected config
     *	@param	context	The HandlerContext.
     */
    @Handler(id="flushJMSDestination",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName", type=String.class, required=true)}
    )
    public static void flushJMSDestination(HandlerContext handlerCtx) {
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                String type = (String)oneRow.get("type");
                    Object[] params = new Object[]{
                        name, 
                        type, 
                        ((String)handlerCtx.getInputValue("ConfigName"))};
                    String[] types = new String[]{
                        "java.lang.String", 
                        "java.lang.String", 
                        "java.lang.String"};
                    JMXUtil.invoke("com.sun.appserv:type=resources,category=config", "flushJMSDestination", params, types);    
            }
        }catch(Exception ex){
           GuiUtil.handleException(handlerCtx, ex);
        }
    }    
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit IIOP Listener Page </p>
     *  <p> Input  value: "Edit                -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input  value: "FromStep2           -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input  value: "ConfigName          -- Type: <code>java.lang.String</code></p>
     *  <p> Input  value: "IiopName            -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "NetwkAddr"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ListenerPort"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Listener"           -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getIiopListenerSettings",
    input={
        @HandlerInput(name="Edit",       type=Boolean.class, required=true),
        @HandlerInput(name="FromStep2",  type=Boolean.class, required=true),
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="IiopName",   type=String.class, required=true) },
    output={
        @HandlerOutput(name="NetwkAddr",       type=String.class), 
        @HandlerOutput(name="ListenerPort",    type=String.class),
        @HandlerOutput(name="Listener",        type=Boolean.class),
        @HandlerOutput(name="security",        type=Boolean.class),
        
        @HandlerOutput(name="Properties",      type=Map.class)})
        
        public static void getIiopListenerSettings(HandlerContext handlerCtx) {
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            Boolean fromStep2 = (Boolean) handlerCtx.getInputValue("FromStep2");
            if(!edit){
                if((fromStep2 == null) || (! fromStep2)){
                    handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("iiopProps", new HashMap());
                    handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("sslProps", null);
                    getDefaultIiopListenerSettings(handlerCtx);
                    handlerCtx.setOutputValue("Properties", new HashMap());
                }else{
                    Map props = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("iiopProps");
                    handlerCtx.setOutputValue("NetwkAddr", props.get("address"));
                    handlerCtx.setOutputValue("ListenerPort", props.get("port"));
                    handlerCtx.setOutputValue("Listener", props.get("listener"));
                    handlerCtx.setOutputValue("security", props.get("security"));
                    handlerCtx.setOutputValue("Properties", props.get("options"));
                }    
                return;
            }
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String iiopListenerName = (String) handlerCtx.getInputValue("IiopName");
            ConfigConfig config = AMXUtil.getConfig(configName);
            IIOPListenerConfig iiopListConfig = config.getIIOPServiceConfig().getIIOPListenerConfigMap().get(iiopListenerName);
            handlerCtx.setOutputValue("NetwkAddr", iiopListConfig.getAddress());
            handlerCtx.setOutputValue("ListenerPort", iiopListConfig.getPort());
            handlerCtx.setOutputValue("Listener", iiopListConfig.getEnabled());
            handlerCtx.setOutputValue("security", iiopListConfig.getSecurityEnabled());
            handlerCtx.setOutputValue("Properties", iiopListConfig.getProperties());
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the default values for all the attributes in 
     *      Edit IIOP Listener Page </p>
     *  <p> Input  value: "ConfigName          -- Type: <code>java.lang.String</code></p>
     *  <p> Input  value: "IiopName            -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "NetwkAddr"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ListenerPort"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Listener"           -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultIiopListenerSettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true) },
    output={
        @HandlerOutput(name="NetwkAddr",       type=String.class), 
        @HandlerOutput(name="ListenerPort",    type=String.class),
        @HandlerOutput(name="Listener",        type=Boolean.class) })
        
        public static void getDefaultIiopListenerSettings(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);      
        Map<String, String> iiopAttrMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(IIOPListenerConfig.J2EE_TYPE);       
        handlerCtx.setOutputValue("NetwkAddr", iiopAttrMap.get("Address"));
        handlerCtx.setOutputValue("ListenerPort", iiopAttrMap.get("port"));
        handlerCtx.setOutputValue("Listener", iiopAttrMap.get("enabled"));
    }
            
    /**
     *	<p> This handler saves the values for all the attributes in 
     *      Edit IIOP Listener Page </p>
     *  <p> Input  value: "ConfigName         -- Type: <code>java.lang.String</code></p>
     *  <p> Input  value: "IiopName           -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "NetwkAddr"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ListenerPort"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Listener"           -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "AddProps"           -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"        -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveIiopListenerSettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="IiopName",   type=String.class, required=true),
        @HandlerInput(name="NetwkAddr",       type=String.class, required=true),
        @HandlerInput(name="Edit",            type=Boolean.class, required=true),
        @HandlerInput(name="ListenerPort",    type=String.class),
        @HandlerInput(name="Listener",        type=Boolean.class),
        @HandlerInput(name="security",        type=Boolean.class),
        @HandlerInput(name="AddProps",        type=Map.class),
        @HandlerInput(name="RemoveProps",     type=ArrayList.class)})
        
        public static void saveIiopListenerSettings(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        String iiopListenerName = (String) handlerCtx.getInputValue("IiopName");
        String address = (String)handlerCtx.getInputValue("NetwkAddr");
        String port = (String)handlerCtx.getInputValue("ListenerPort");
        boolean listener = ((Boolean)handlerCtx.getInputValue("Listener")).booleanValue();
        boolean security = ((Boolean)handlerCtx.getInputValue("security")).booleanValue();
        
        ConfigConfig config = AMXUtil.getConfig(configName);       
        try{
            IIOPListenerConfig iiopListConfig = null;
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map iiopPropsMap = new HashMap();
                iiopPropsMap.put("iiopName", iiopListenerName);
                iiopPropsMap.put("address", address);
                iiopPropsMap.put("options", (Map)handlerCtx.getInputValue("AddProps"));
                iiopPropsMap.put("port", port);
                iiopPropsMap.put("listener", listener);
                iiopPropsMap.put("security", security);
                handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("iiopProps", iiopPropsMap);
                //iiopListConfig = config.getIIOPServiceConfig().createIIOPListenerConfig(iiopListenerName, address, options);
            } else {
                Map iiopMap = config.getIIOPServiceConfig().getIIOPListenerConfigMap();
                iiopListConfig = (IIOPListenerConfig)iiopMap.get(iiopListenerName);            
                iiopListConfig.setAddress(address);
                AMXUtil.editProperties(handlerCtx, iiopListConfig);
                iiopListConfig.setPort(port);
                iiopListConfig.setEnabled(listener); 
                iiopListConfig.setSecurityEnabled(security);
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
     
    /**
     *	<p> This handler returns the list of specified Listener elements for populating 
     *  <p> the table in IIOPListeners page & HTTP Listeners page
     *  <p> Input  value: "Type"         -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "ConfigName"   -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "selectedRows" -- Type: <code> java.util.List</code></p>
     *  <p> Output  value: "Result"      -- Type: <code> java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getListenersList",
        input={
            @HandlerInput(name="Type", type=String.class, required=true),
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="Result", type=java.util.List.class)}
     )
    public static void getListenersList(HandlerContext handlerCtx){
        String type = (String) handlerCtx.getInputValue("Type");
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        List result = new ArrayList();
        boolean isIIOP = false;
        boolean isHTTP = false;
        Iterator iter = null;
        try{
            if("iiopListener".equals(type)){ //NOI18N 
                iter = config.getIIOPServiceConfig().getIIOPListenerConfigMap().values().iterator();
                isIIOP = true;
            }else if("httpListener".equals(type)){ //NOI18N 
                iter = config.getHTTPServiceConfig().getHTTPListenerConfigMap().values().iterator();
                isHTTP = true;
            }

            List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
            boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
       
            if (iter != null){
                while(iter.hasNext()){
                    ConfigElement configE = (ConfigElement) iter.next();
                    HashMap oneRow = new HashMap();
                    String name=configE.getName();                
                    oneRow.put("name", name);
                    oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
                    if(isIIOP){
                        IIOPListenerConfig listConfig = (IIOPListenerConfig)configE; 
                        boolean enabled = listConfig.getEnabled();
                        String ntwkAddress = listConfig.getAddress();
                        String listPort = listConfig.getPort();
                        oneRow.put("enabled", enabled);
                        oneRow.put("ntwkAddress", (ntwkAddress == null) ? " ": ntwkAddress);
                        oneRow.put("listPort", (listPort == null) ? " ": listPort);
                    }else if(isHTTP){
                        HTTPListenerConfig httpConfig = (HTTPListenerConfig)configE; 
                        boolean enabled = httpConfig.getEnabled();
                        String ntwkAddress = httpConfig.getAddress();
                        String listPort = httpConfig.getPort();
                        String virtualServer = httpConfig.getDefaultVirtualServer();
                        oneRow.put("enabled", enabled);
                        oneRow.put("ntwkAddress", (ntwkAddress == null) ? " ": ntwkAddress);
                        oneRow.put("listPort", (listPort == null) ? " ": listPort);
                        oneRow.put("defVirtualServer", (virtualServer == null) ? " ": virtualServer);
                    }   
                    result.add(oneRow);
                }
            }
        } catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("Result", result);
    }
    
    /**
     *	<p> This handler takes in selected rows, and removes selected Listeners
     *  <p> Input  value: "selectedRows"  -- Type: <code> java.util.List</code></p>
     *  <p> Input  value: "ConfigName"    -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "Type"          -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteListeners",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName",   type=String.class, required=true),
        @HandlerInput(name="Type",         type=String.class, required=true)}
    )
    public static void deleteListeners(HandlerContext handlerCtx) {
        String configName = (String)handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        String type = (String)handlerCtx.getInputValue("Type");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                if("iiopListener".equals(type)){  //NOI18N
                    config.getIIOPServiceConfig().removeIIOPListenerConfig(name);
                }else if("httpListener".equals(type)){  //NOI18N
                    //Need to use JMX because we also need to remove the references in Virtual server.
                    //This is specifed as the http-listeners attribute of the virtual server.
                    String[] types = new String[]{ "java.lang.String", "java.lang.String"};
                    Object[] params = new Object[]{name,configName};
                    JMXUtil.invoke( "com.sun.appserv:type=configs,category=config", 
                        "deleteHttpListener", params, types);
                }
            }
        }catch(Exception ex){
           GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for list of thread pools in 
     *      ORB Page </p>
     *  <p> Input  value: "ConfigName               -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "DefaultVirtualServers"   -- Type: <code>SelectItem[].class 
     *      SelectItem[] (castable to Option[])</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultVirtualServers",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},
    output={
        @HandlerOutput(name="DefaultVirtualServers",   type=SelectItem[].class)})
        
        public static void getDefaultVirtualServers(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        Map virtServers = config.getHTTPServiceConfig().getVirtualServerConfigMap();
        String[] virtualServers = (String[])virtServers.keySet().toArray(new String[virtServers.size()]);
        SelectItem[] options = getModOptions(virtualServers);
        
        handlerCtx.setOutputValue("DefaultVirtualServers", options);
    }
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      New/Edit HTTP Listener Page </p>
     *  <p> Input  value: "Edit"               -- Type: <code>java.lang.String</code></p>
     *  <p> Input  value: "FromStep2"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input  value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input  value: "HttpName"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Listener"           -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Output value: "NetwkAddr"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ListenerPort"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DefaultVirtServer"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ServerName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RedirectPort"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Acceptor"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PoweredBy"          -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Blocking"           -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getHttpListenerValues",
    input={
        @HandlerInput(name="Edit",       type=Boolean.class, required=true),
        @HandlerInput(name="FromStep2",  type=Boolean.class, required=true),
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="HttpName",   type=String.class, required=true) },
    output={
        @HandlerOutput(name="Listener",          type=Boolean.class),
        @HandlerOutput(name="security",          type=Boolean.class),
        @HandlerOutput(name="NetwkAddr",         type=String.class), 
        @HandlerOutput(name="ListenerPort",      type=String.class),
        @HandlerOutput(name="DefaultVirtServer", type=String.class),
        @HandlerOutput(name="ServerName",        type=String.class),
        @HandlerOutput(name="RedirectPort",      type=String.class),
        @HandlerOutput(name="Acceptor",          type=String.class),
        @HandlerOutput(name="PoweredBy",         type=Boolean.class),
        @HandlerOutput(name="Blocking",          type=Boolean.class),
        @HandlerOutput(name="Properties",        type=Map.class)})
        
        public static void getHttpListenerValues(HandlerContext handlerCtx) {
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            Boolean fromStep2 = (Boolean) handlerCtx.getInputValue("FromStep2");
            if(!edit){
                if((fromStep2 == null) || (! fromStep2)){
                    handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("httpProps", new HashMap());
                    handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("sslProps", null);
                    Map<String, String> httpAttrMap = AMXUtil.getDomainConfig().getDefaultAttributeValues(HTTPListenerConfig.J2EE_TYPE);
                    handlerCtx.setOutputValue("Listener", httpAttrMap.get("enabled"));
                    handlerCtx.setOutputValue("security", httpAttrMap.get("security-enabled"));
                    handlerCtx.setOutputValue("Acceptor", httpAttrMap.get("acceptor-threads"));
                    handlerCtx.setOutputValue("PoweredBy", httpAttrMap.get("xpowered-by"));
                    handlerCtx.setOutputValue("Blocking", httpAttrMap.get("blocking-enabled"));
                }else{
                    Map props = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("httpProps");
                    handlerCtx.setOutputValue("Listener", props.get("enabled"));
                    handlerCtx.setOutputValue("security", props.get("securityEnabled"));
                    handlerCtx.setOutputValue("NetwkAddr", props.get("address"));
                    handlerCtx.setOutputValue("ListenerPort", props.get("port"));
                    handlerCtx.setOutputValue("DefaultVirtServer", props.get("virtualServer"));
                    handlerCtx.setOutputValue("ServerName", props.get("serverName"));
                    handlerCtx.setOutputValue("RedirectPort", props.get("redirectPort"));
                    handlerCtx.setOutputValue("Acceptor", props.get("acceptor-threads"));
                    handlerCtx.setOutputValue("PoweredBy", props.get("xpowered-by"));
                    handlerCtx.setOutputValue("Blocking", props.get("blocking-enabled"));
                    handlerCtx.setOutputValue("Properties", props.get("options"));
                }
                return;
            }
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String httpListenerName = (String) handlerCtx.getInputValue("HttpName");
            ConfigConfig config = AMXUtil.getConfig(configName);
            HTTPListenerConfig httpListConfig = config.getHTTPServiceConfig().getHTTPListenerConfigMap().get(httpListenerName);
            handlerCtx.setOutputValue("Listener", httpListConfig.getEnabled());
            handlerCtx.setOutputValue("security", httpListConfig.getSecurityEnabled());
            handlerCtx.setOutputValue("NetwkAddr", httpListConfig.getAddress());
            handlerCtx.setOutputValue("ListenerPort", httpListConfig.getPort());
            handlerCtx.setOutputValue("DefaultVirtServer", httpListConfig.getDefaultVirtualServer());
            handlerCtx.setOutputValue("ServerName", httpListConfig.getServerName());
            handlerCtx.setOutputValue("RedirectPort", httpListConfig.getRedirectPort());
            handlerCtx.setOutputValue("Acceptor", httpListConfig.getAcceptorThreads());
            handlerCtx.setOutputValue("PoweredBy", httpListConfig.getXpoweredBy());
            handlerCtx.setOutputValue("Blocking", httpListConfig.getBlockingEnabled());
            Map<String, String> pMap = httpListConfig.getProperties();
            
            //refer to issue#2920; If we want to hide this property, just uncomment the following 2 lines.
            //if (httpListenerName.equals(ADMIN_LISTENER))
            //    pMap.remove(PROXIED_PROTOCOLS);
            
            handlerCtx.setOutputValue("Properties", pMap);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the attributes in 
     *      New/Edit HTTP Listener Page </p>
     *  <p> Input value: "ConfigName         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "HttpName           -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Edit"              -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "NetwkAddr"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ListenerPort"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "DefaultVirtServer" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ServerName"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Listener"          -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "security"          -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "RedirectPort"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Acceptor"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "PoweredBy"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Blocking"          -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "AddProps"          -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"       -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveHttpListenerValues",
    input={
        @HandlerInput(name="ConfigName",        type=String.class, required=true),
        @HandlerInput(name="HttpName",          type=String.class, required=true),
        @HandlerInput(name="Edit",              type=Boolean.class, required=true),
        @HandlerInput(name="NetwkAddr",         type=String.class, required=true),
        @HandlerInput(name="ListenerPort",      type=String.class, required=true),
        @HandlerInput(name="DefaultVirtServer", type=String.class, required=true),
        @HandlerInput(name="ServerName",        type=String.class, required=true),
        @HandlerInput(name="Listener",          type=Boolean.class),
        @HandlerInput(name="security",          type=Boolean.class),
        @HandlerInput(name="RedirectPort",      type=String.class),
        @HandlerInput(name="Acceptor",          type=String.class),
        @HandlerInput(name="PoweredBy",         type=Boolean.class),
        @HandlerInput(name="Blocking",          type=Boolean.class),
        @HandlerInput(name="AddProps",          type=Map.class),
        @HandlerInput(name="RemoveProps",       type=ArrayList.class) })
        
        public static void saveHttpListenerValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        String httpListenerName = (String) handlerCtx.getInputValue("HttpName");
        String listPort = (String)handlerCtx.getInputValue("ListenerPort");
        String address = (String)handlerCtx.getInputValue("NetwkAddr");
        String virtualServer = (String)handlerCtx.getInputValue("DefaultVirtServer");
        String serverName = (String)handlerCtx.getInputValue("ServerName");
        ConfigConfig config = AMXUtil.getConfig(configName);       
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map httpPropsMap = new HashMap();
                httpPropsMap.put("httpName", httpListenerName);
                httpPropsMap.put("address", address);
                httpPropsMap.put("port", listPort);
                httpPropsMap.put("virtualServer", virtualServer);
                httpPropsMap.put("serverName", serverName);
                httpPropsMap.put("options", (Map)handlerCtx.getInputValue("AddProps"));
                httpPropsMap.put("enabled", (Boolean)handlerCtx.getInputValue("Listener"));
                httpPropsMap.put("securityEnabled", (Boolean)handlerCtx.getInputValue("security"));
                httpPropsMap.put("redirectPort", (String)handlerCtx.getInputValue("RedirectPort"));
                httpPropsMap.put("acceptor-threads", (String)handlerCtx.getInputValue("Acceptor"));
                httpPropsMap.put("xpowered-by", (Boolean)handlerCtx.getInputValue("PoweredBy")); 
                httpPropsMap.put("blocking-enabled", (Boolean)handlerCtx.getInputValue("Blocking")); 
                handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("httpProps", httpPropsMap);
                //the actual creation is in step 2 of the wizard.
            } else {
                HTTPListenerConfig httpListConfig = config.getHTTPServiceConfig().getHTTPListenerConfigMap().get(httpListenerName);
                String previousVSName = httpListConfig.getDefaultVirtualServer();
                httpListConfig.setAddress(address);
                httpListConfig.setPort((String)handlerCtx.getInputValue("ListenerPort"));
                httpListConfig.setDefaultVirtualServer(virtualServer);
                httpListConfig.setServerName(serverName);
                httpListConfig.setEnabled((Boolean)handlerCtx.getInputValue("Listener"));
                httpListConfig.setSecurityEnabled((Boolean)handlerCtx.getInputValue("security"));
                httpListConfig.setRedirectPort((String)handlerCtx.getInputValue("RedirectPort"));
                httpListConfig.setAcceptorThreads((String)handlerCtx.getInputValue("Acceptor"));
                httpListConfig.setXpoweredBy((Boolean)handlerCtx.getInputValue("PoweredBy"));
                httpListConfig.setBlockingEnabled((Boolean)handlerCtx.getInputValue("Blocking"));
                AMXUtil.editProperties(handlerCtx, httpListConfig);
                
                //refer to issue #2920
                if (httpListenerName.equals(ADMIN_LISTENER)){
                    if (httpListConfig.getSecurityEnabled()){
                        if (httpListConfig.existsProperty(PROXIED_PROTOCOLS))
                            httpListConfig.setPropertyValue(PROXIED_PROTOCOLS, PROXIED_PROTOCOLS_VALUE);
                         else
                             httpListConfig.createProperty(PROXIED_PROTOCOLS, PROXIED_PROTOCOLS_VALUE);
                    }else{
                        if (httpListConfig.existsProperty(PROXIED_PROTOCOLS))
                            httpListConfig.removeProperty(PROXIED_PROTOCOLS);
                    }
                }
                
                //Also need to change the http-listeners attributes of Virtual Server.
                Map<String,VirtualServerConfig>vservers = config.getHTTPServiceConfig().getVirtualServerConfigMap();
                VirtualServerConfig previousVS = vservers.get(previousVSName);
                VirtualServerConfig newVS = vservers.get(virtualServer);
                String hl = previousVS.getHTTPListeners();
                String[] hlArray = GuiUtil.stringToArray(hl, ",");
                
                //remove from previous VS.
                String tmp = "";
                for(int i=0; i<hlArray.length; i++){
                    if (! hlArray[i].equals(httpListenerName))
                        tmp= (tmp == "")? hlArray[i] : tmp+","+hlArray[i];
                }
                previousVS.setHTTPListeners(tmp);
                
                //add to current VS.
                tmp = newVS.getHTTPListeners();
                if (GuiUtil.isEmpty(tmp))
                    newVS.setHTTPListeners(httpListenerName);
                else{
                    tmp = newVS.getHTTPListeners()+","+httpListenerName;
                    newVS.setHTTPListeners(tmp);
                }
                    
            }
            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the list of Message Security Configuration elements for populating 
     *  <p> the table in Message Security page
     *  <p> Input  value: "ConfigName"   -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "selectedRows" -- Type: <code> java.util.List</code></p>
     *  <p> Output  value: "Result"      -- Type: <code> java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMsgSecurityList",
        input={
            @HandlerInput(name="ConfigName",   type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="Result",  type=List.class),
            @HandlerOutput(name="ShowNew", type=Boolean.class)})
            
    public static void getMsgSecurityList(HandlerContext handlerCtx){
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        
        Iterator iter = config.getSecurityServiceConfig().getMessageSecurityConfigMap().values().iterator();
        
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
       
        List result = new ArrayList();
        if (iter != null){
            while(iter.hasNext()){
                MessageSecurityConfig msgSecurityConfig = (MessageSecurityConfig) iter.next();
                HashMap oneRow = new HashMap();
                String name = msgSecurityConfig.getAuthLayer();
                oneRow.put("name", name);
                oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
                String defaultProv = msgSecurityConfig.getDefaultProvider();
                String defaultClientProv = msgSecurityConfig.getDefaultClientProvider();
                oneRow.put("defaultProv", (defaultProv == null) ? " ": defaultProv);
                oneRow.put("defaultClientProv", (defaultClientProv == null) ? " ": defaultClientProv);
                result.add(oneRow);
            }
        }
        boolean showNew = true;
        if(result.size() == 2){
            showNew = false;
        }
        handlerCtx.setOutputValue("Result", result);
        handlerCtx.setOutputValue("ShowNew", showNew);
    }
    
    /**
     *	<p> This handler takes in selected rows, and removes selected Listeners
     *  <p> Input  value: "selectedRows"  -- Type: <code> java.util.List</code></p>
     *  <p> Input  value: "ConfigName"    -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "Type"          -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteMsgSecurities",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName",   type=String.class, required=true)}
    )
    public static void deleteMsgSecurities(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                config.getSecurityServiceConfig().removeMessageSecurityConfig(name);
            }
        }catch(Exception ex){
           GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      Edit Message Security Configuration Page </p>
     *  <p> Input value: "ConfigName"         -- Type: <code> java.lang.String</code></p>
     *  <p> Input value: "AuthLayer"          -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "DefaultProvider"   -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "DefaultClProvider" -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "Providers"         -- Type: <code> java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getMsgSecurityConfigValues",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="AuthLayer", type=String.class, required=true) },        
    output={
        @HandlerOutput(name="DefaultProvider",    type=String.class),
        @HandlerOutput(name="DefaultClProvider",  type=String.class),
        @HandlerOutput(name="Providers",          type=SelectItem[].class) })
        
        public static void getMsgSecurityConfigValues(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        String authLayerName = (String)handlerCtx.getInputValue("AuthLayer");
        try{
            MessageSecurityConfig msgSecConfig = config.getSecurityServiceConfig().getMessageSecurityConfigMap().get(authLayerName);
            handlerCtx.setOutputValue("DefaultProvider", msgSecConfig.getDefaultProvider());
            handlerCtx.setOutputValue("DefaultClProvider", msgSecConfig.getDefaultClientProvider());
            Map<String,ProviderConfig> providersMap = msgSecConfig.getProviderConfigMap();
            String[] providers = (String[])providersMap.keySet().toArray(new String[providersMap.size()]);
            if(providers != null) {
                SelectItem[] options = getModOptions(providers);
                handlerCtx.setOutputValue("Providers", options);
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler saves the values for all the attributes in 
     *      Edit Message Security Configuration Page </p>
     *  <p> Input value: "ConfigName"         -- Type: <code> java.lang.String</code></p>
     *  <p> Input value: "AuthLayer"          -- Type: <code> java.lang.String</code></p>
     *  <p> Input value: "DefaultProvider"    -- Type: <code> java.lang.String</code></p>
     *  <p> Input value: "DefaultClProvider"  -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveMsgSecurityConfigValues",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true), 
        @HandlerInput(name="AuthLayer", type=String.class, required=true),        
        @HandlerInput(name="DefaultProvider",    type=String.class),
        @HandlerInput(name="DefaultClProvider",  type=String.class) })
        
        public static void saveMsgSecurityConfigValues(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        String authLayerName = (String)handlerCtx.getInputValue("AuthLayer");
        try{
            MessageSecurityConfig msgSecConfig = config.getSecurityServiceConfig().getMessageSecurityConfigMap().get(authLayerName);
            msgSecConfig.setDefaultProvider((String)handlerCtx.getInputValue("DefaultProvider"));
            msgSecConfig.setDefaultClientProvider((String)handlerCtx.getInputValue("DefaultClProvider"));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the list of Providers for populating 
     *  <p> the table in Message Security page's Providers tab
     *  <p> Input  value: "ConfigName"   -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "selectedRows" -- Type: <code> java.util.List</code></p>
     *  <p> Output  value: "Result"      -- Type: <code> java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getProvidersList",
        input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="MsgSecurityName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="Result", type=java.util.List.class)}
     )
    public static void getProvidersList(HandlerContext handlerCtx){
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        String msgSecurityName = (String)handlerCtx.getInputValue("MsgSecurityName");
        Map providerMap = config.getSecurityServiceConfig().getMessageSecurityConfigMap().get(msgSecurityName).getProviderConfigMap();
        Iterator iter = providerMap.values().iterator();
        
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
       
        List result = new ArrayList();
        if (iter != null){
            while(iter.hasNext()){
                ProviderConfig providerConfig = (ProviderConfig) iter.next();
                HashMap oneRow = new HashMap();
                String name = providerConfig.getProviderId();
                oneRow.put("name", name);
                oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
                String providerType = providerConfig.getProviderType();
                String className = providerConfig.getClassName();
                oneRow.put("provType", (providerType == null) ? " ": providerType);
                oneRow.put("className", (className == null) ? " ": className);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("Result", result);
    }
    
    /**
     *	<p> This handler takes in selected rows, and removes selected Listeners
     *  <p> Input  value: "selectedRows"    -- Type: <code> java.util.List</code></p>

     *  <p> Input  value: "ConfigName"      -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "MsgSecurityName" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteProviders",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName",   type=String.class, required=true),
        @HandlerInput(name="MsgSecurityName", type=String.class, required=true)}
    )
    public static void deleteProviders(HandlerContext handlerCtx) {
        String configName = (String)handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXUtil.getConfig(configName);
        String msgSecurityName = (String)handlerCtx.getInputValue("MsgSecurityName");
        MessageSecurityConfig msgConfig = config.getSecurityServiceConfig().getMessageSecurityConfigMap().get(msgSecurityName);
        
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                String currDefProvider = msgConfig.getDefaultClientProvider();
                if(name.equals(currDefProvider)){
                    msgConfig.setDefaultClientProvider(""); //NOI18N
                }
                Object[] params = {name};
                String[] types = {"java.lang.String"};
                String objName = "com.sun.appserv:type=message-security-config,category=config,name=" + msgSecurityName +",config=" + configName;
                JMXUtil.invoke(objName, "removeProviderConfigByProviderId", params, types);
                //msgConfig.removeProviderConfig(name);
            }
            if (File.separatorChar == '\\'){
                //For Window, there is a timing issue that we need to put in some delay.
                //Otherwise, when we redisplay the provider table after deletion, there will be exception thrown
                //since it doesn't recognize that the provider has already been deleted
                Thread.sleep(2000);
            }
        }catch(Exception ex){
           GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      New/Edit Provider Configuration Page </p>
     *  <p> Input value: "Edit"                -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MsgSecurityName"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ProviderId"          -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DefaultProvider"     -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Output value: "ProviderType"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ClassName"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AuthSourceRequest"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AuthRecpRequest"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AuthSourceResponse"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AuthRecpResponse"    -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"         -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getProviderValues",
    input={
        @HandlerInput(name="Edit",            type=Boolean.class, required=true),
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="MsgSecurityName", type=String.class, required=true),
        @HandlerInput(name="ProviderId",      type=String.class, required=true) },
    output={
        @HandlerOutput(name="DefaultProvider",    type=Boolean.class),
        @HandlerOutput(name="ProviderType",       type=String.class),
        @HandlerOutput(name="ClassName",          type=String.class), 
        @HandlerOutput(name="AuthSourceRequest",  type=String.class),
        @HandlerOutput(name="AuthRecpRequest",    type=String.class),
        @HandlerOutput(name="AuthSourceResponse", type=String.class),
        @HandlerOutput(name="AuthRecpResponse",   type=String.class),
        @HandlerOutput(name="Properties",         type=Map.class)})
        
        public static void getProviderValues(HandlerContext handlerCtx) {
        try{
            
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String msgSecurityName = (String) handlerCtx.getInputValue("MsgSecurityName");
            ConfigConfig config = AMXUtil.getConfig(configName);
            MessageSecurityConfig msgConfig = config.getSecurityServiceConfig().getMessageSecurityConfigMap().get(msgSecurityName);
            if(!edit){
                handlerCtx.setOutputValue("DefaultProvider", false);
                return;
            }
            String providerId = (String) handlerCtx.getInputValue("ProviderId");
            ProviderConfig provConfig = msgConfig.getProviderConfigMap().get(providerId);           
            handlerCtx.setOutputValue("ProviderType", provConfig.getProviderType());
            handlerCtx.setOutputValue("ClassName", provConfig.getClassName());
            RequestPolicyConfig requestConfig = provConfig.getRequestPolicyConfig();
            if(requestConfig != null){
                handlerCtx.setOutputValue("AuthSourceRequest", requestConfig.getAuthSource());
                handlerCtx.setOutputValue("AuthRecpRequest", requestConfig.getAuthRecipient());
            }
            ResponsePolicyConfig responseConfig = provConfig.getResponsePolicyConfig();
            if(responseConfig != null){
                handlerCtx.setOutputValue("AuthSourceResponse", responseConfig.getAuthSource());
                handlerCtx.setOutputValue("AuthRecpResponse", responseConfig.getAuthRecipient());
            }
            handlerCtx.setOutputValue("Properties", provConfig.getProperties());
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      New/Edit Provider Configuration Page </p>
     *  <p> Output value: "ProviderTypes"        -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "AuthSources"          -- Type: <code>java.util.Array</code></p>
     *  <p> Output value: "AuthRecepients"       -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getListsforProvider",
    output={
        @HandlerOutput(name="ProviderTypes",       type=SelectItem[].class),
        @HandlerOutput(name="AuthSources",         type=SelectItem[].class),
        @HandlerOutput(name="AuthRecepients",      type=SelectItem[].class)})
        
        public static void getProviderLists(HandlerContext handlerCtx) {
        SelectItem[] types = getOptions(providerTypes);
        SelectItem[] sources = getModOptions(authSource);
        SelectItem[] recepients = getModOptions(authRecipient);
        handlerCtx.setOutputValue("ProviderTypes", types);
        handlerCtx.setOutputValue("AuthSources", sources);
        handlerCtx.setOutputValue("AuthRecepients", recepients);
    }
    
    /**
     *	<p> This handler saves the values for all the attributes in 
     *      New/Edit Provider Configuration Page </p>
     *  <p> Input value: "Edit"                -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MsgSecurityName"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ProviderId"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "DefaultProvider"     -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Input value: "ProviderType"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ClassName"           -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AuthSourceRequest"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AuthRecpRequest"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AuthSourceResponse"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AuthRecpResponse"    -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"            -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"         -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveProviderValues",
    input={
        @HandlerInput(name="Edit",            type=Boolean.class, required=true),
        @HandlerInput(name="ConfigName",      type=String.class, required=true),
        @HandlerInput(name="MsgSecurityName", type=String.class, required=true),
        @HandlerInput(name="ProviderId",      type=String.class, required=true),
        @HandlerInput(name="DefaultProvider",    type=Boolean.class),
        @HandlerInput(name="ProviderType",       type=String.class),
        @HandlerInput(name="ClassName",          type=String.class), 
        @HandlerInput(name="AuthSourceRequest",  type=String.class),
        @HandlerInput(name="AuthRecpRequest",    type=String.class),
        @HandlerInput(name="AuthSourceResponse", type=String.class),
        @HandlerInput(name="AuthRecpResponse",   type=String.class),
        @HandlerInput(name="AddProps",           type=Map.class),
        @HandlerInput(name="RemoveProps",        type=ArrayList.class) })
        
        public static void saveProviderValues(HandlerContext handlerCtx) {
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String msgSecurityName = (String) handlerCtx.getInputValue("MsgSecurityName");
            String providerId = (String) handlerCtx.getInputValue("ProviderId");
            String providerType = (String) handlerCtx.getInputValue("ProviderType");
            String className = (String) handlerCtx.getInputValue("ClassName");
            
            ConfigConfig config = AMXUtil.getConfig(configName);
            MessageSecurityConfig msgConfig = config.getSecurityServiceConfig().getMessageSecurityConfigMap().get(msgSecurityName);
            ProviderConfig provConfig = null;
            if(!edit){
                boolean isDefault = (Boolean)handlerCtx.getInputValue("DefaultProvider");
                String msgObjName = "com.sun.appserv:type=configs,category=config";
                String createOperName = "createMessageSecurityProvider";
                /*paramsForMsgSecProv = {"messageLayer", "providerId", "providerType", "providerClassName", 
                    "requestAuthSource", "requestAuthRecipient", "responseAuthSource", "responseAuthRecipient",
                    "isDefaultProvider", "properties", "targetName"}; */
                String[] types = new String[] {"java.lang.String", "java.lang.String", 
                    "java.lang.String", "java.lang.String", "java.lang.String", 
                    "java.lang.String", "java.lang.String", "java.lang.String", 
                    "boolean", "java.util.Properties", "java.lang.String" };
                Object[] createParams = {msgSecurityName, providerId, providerType, className,
                    null, null, null, null, isDefault, null, configName};
                JMXUtil.invoke(msgObjName, createOperName, createParams, types);
                if (File.separatorChar == '\\'){
                //For Window, there is a timing issue that we need to put in some delay.
                Thread.sleep(2000);
            }
                provConfig = msgConfig.getProviderConfigMap().get(providerId);
                AMXUtil.editProperties(handlerCtx, provConfig);
            }else{
                provConfig = msgConfig.getProviderConfigMap().get(providerId);
                provConfig.setProviderType((String)handlerCtx.getInputValue("ProviderType"));
                provConfig.setClassName((String)handlerCtx.getInputValue("ClassName"));
                AMXUtil.editProperties(handlerCtx, provConfig);
            }
            /*if(!edit){
                provConfig = msgConfig.createProviderConfig(providerId, providerType, className, null);
                boolean isDefault = (Boolean)handlerCtx.getInputValue("DefaultProvider");
                if(isDefault) {
                    msgConfig.setDefaultClientProvider(providerId);
                }
                AMXUtil.editProperties(handlerCtx, provConfig);
            }else{
                provConfig = msgConfig.getProviderConfigMap().get(providerId);
                provConfig.setProviderType((String)handlerCtx.getInputValue("ProviderType"));
                provConfig.setClassName((String)handlerCtx.getInputValue("ClassName"));
                AMXUtil.editProperties(handlerCtx, provConfig);
            }*/
            String objName = "com.sun.appserv:type=provider-config,config=" + configName + ",category=config,message-security-config=" + msgSecurityName + ",name=" + providerId;
            String[] signature = {"javax.management.AttributeList"};
            
            String requestOperation = "createRequestPolicy";
            AttributeList requestAttrList = new AttributeList();
            requestAttrList.add(new Attribute("auth-source", (String)handlerCtx.getInputValue("AuthSourceRequest")));
            requestAttrList.add(new Attribute("auth-recipient", (String)handlerCtx.getInputValue("AuthRecpRequest")));
            
            String responseOperation = "createResponsePolicy";
            AttributeList responseAttrList = new AttributeList();
            responseAttrList.add(new Attribute("auth-source", (String)handlerCtx.getInputValue("AuthSourceResponse")));
            responseAttrList.add(new Attribute("auth-recipient", (String)handlerCtx.getInputValue("AuthRecpResponse")));
                       
            Object[] requestParams = {requestAttrList};
            Object[] responseParams = {responseAttrList};
            JMXUtil.invoke(objName, requestOperation, requestParams, signature);
            JMXUtil.invoke(objName, responseOperation, responseParams, signature);
            
            //AMX API's not working
            /*
            RequestPolicyConfig requestConfig = provConfig.getRequestPolicyConfig();
            ResponsePolicyConfig responseConfig = provConfig.getResponsePolicyConfig();
            if(requestConfig == null){
                requestConfig = provConfig.createRequestPolicyConfig(new HashMap());
            }
            if(responseConfig == null){
                responseConfig = provConfig.createResponsePolicyConfig(new HashMap());
            }
            requestConfig.setAuthSource((String)handlerCtx.getInputValue("AuthSourceRequest"));
            requestConfig.setAuthRecipient((String)handlerCtx.getInputValue("AuthRecpRequest"));
            responseConfig.setAuthSource((String)handlerCtx.getInputValue("AuthSourceResponse"));
            responseConfig.setAuthRecipient((String)handlerCtx.getInputValue("AuthRecpResponse"));
             */
            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the values for all the attributes in 
     *      New Message Security Configuration Page </p>
     *  <p> Input value: "ConfigName"         -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "DefaultProvider"   -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "DefaultClProvider" -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "Providers"         -- Type: <code> java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultMessageAndProviderValues",
    input={
        @HandlerInput(name="ConfigName",          type=String.class)},
    output={
        @HandlerOutput(name="AuthLayers",         type=SelectItem[].class),
        @HandlerOutput(name="DefaultProvider",    type=Boolean.class),
        @HandlerOutput(name="ProviderTypes",       type=SelectItem[].class) })
        
        public static void getDefaultMessageAndProviderValues(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        Map configs = config.getSecurityServiceConfig().getMessageSecurityConfigMap();
        Vector layers = new Vector(Arrays.asList(messageLayers));
        for(Iterator it = configs.values().iterator(); it.hasNext();){
            MessageSecurityConfig msgConfig = (MessageSecurityConfig)it.next();
            String layerName = msgConfig.getAuthLayer();
            if((layerName != null) && (layers.contains(msgConfig.getAuthLayer())) ){
                layers.remove(layerName);
            }
        }
        SelectItem[] authLayers = getOptions((String[])layers.toArray(new String[layers.size()]));
        SelectItem[] types = getOptions(providerTypes);
        handlerCtx.setOutputValue("AuthLayers", authLayers);
        handlerCtx.setOutputValue("DefaultProvider", false);
        handlerCtx.setOutputValue("ProviderTypes", types);
    }
    
    /**
     *	<p> This handler saves the values for all the attributes in 
     *      New Message Security Configuration Page </p>
     *  <p> Input value: "ConfigName"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AuthLayer"           -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "DefaultProvider"     -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Input value: "ProviderType"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ProviderId"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ClassName"           -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"            -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"         -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveMessageAndProviderValues",
    input={
        @HandlerInput(name="ConfigName",       type=String.class, required=true),
        @HandlerInput(name="AuthLayer",        type=String.class, required=true),
        @HandlerInput(name="DefaultProvider",  type=Boolean.class),
        @HandlerInput(name="ProviderType",     type=String.class, required=true),
        @HandlerInput(name="ProviderId",       type=String.class, required=true),
        @HandlerInput(name="ClassName",        type=String.class, required=true),
        @HandlerInput(name="AddProps",         type=Map.class),
        @HandlerInput(name="RemoveProps",      type=ArrayList.class) })
        
        public static void saveMessageAndProviderValues(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            String authLayer = (String) handlerCtx.getInputValue("AuthLayer");
            String providerId = (String) handlerCtx.getInputValue("ProviderId");
            String providerType = (String) handlerCtx.getInputValue("ProviderType");
            String className = (String) handlerCtx.getInputValue("ClassName");
            boolean isDefault = (Boolean) handlerCtx.getInputValue("DefaultProvider");
            
            ConfigConfig config = AMXUtil.getConfig(configName);
            MessageSecurityConfig msgConfig = config.getSecurityServiceConfig().createMessageSecurityConfig(authLayer, 
                    providerId, providerType, className, new HashMap());
            if(isDefault) {
                msgConfig.setDefaultClientProvider(providerId);
            }
            AMXUtil.editProperties(handlerCtx, msgConfig.getProviderConfigMap().get(providerId));
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler takes in selected rows, and removes selected Management Rules
     *  <p> Input  value: "selectedRows"    -- Type: <code> java.util.List</code></p>
     *  <p> Input  value: "ConfigName"      -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="deleteManagementRules",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName",   type=String.class, required=true) }
    )
    public static void deleteManagementRules(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        ManagementRulesConfig mgRulesConfig = config.getManagementRulesConfig();
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                mgRulesConfig.removeManagementRuleConfig(name);
            }
        }catch(Exception ex){
           GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
        /**
     *	<p> This handler takes in selected rows, and removes selected Management Rules
     *  <p> Input  value: "selectedRows"    -- Type: <code> java.util.List</code></p>
     *  <p> Input  value: "ConfigName"      -- Type: <code> java.lang.String</code></p>
     *  <p> Input  value: "Enable"          -- Type: <code> java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="enableDisableManagementRules",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName",   type=String.class, required=true),
        @HandlerInput(name="Enable",       type=Boolean.class, required=true) }
    )
    public static void enableDisableManagementRules(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        boolean enable = ((Boolean)handlerCtx.getInputValue("Enable")).booleanValue();
        ManagementRulesConfig mgRulesConfig = config.getManagementRulesConfig();
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                ManagementRuleConfig ruleConfig = mgRulesConfig.getManagementRuleConfigMap().get(name);
                ruleConfig.setEnabled(enable);
            }
        }catch(Exception ex){
           GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      Availability Service Page </p>
     *	<p> Input value: "ConfigName"           -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "AvailabilityEnabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "StorePoolName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HAStoreName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HAAgentHosts"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HAAgentPort"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HAAgentPassword"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AutoManage"          -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "StoreHealthCheck"    -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "HAStoreHealthCheck"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultAvailabilityServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerOutput(name="StorePoolName",            type=String.class),
        @HandlerOutput(name="HAStoreName",              type=String.class),
        @HandlerOutput(name="HAAgentHosts",             type=String.class),
        @HandlerOutput(name="HAAgentPort",              type=String.class),
        @HandlerOutput(name="HAAgentPassword",          type=String.class),
        @HandlerOutput(name="AutoManage",               type=Boolean.class),
        @HandlerOutput(name="StoreHealthCheck",         type=Boolean.class),
        @HandlerOutput(name="HAStoreHealthCheck",       type=String.class)})
        
        public static void getDefaultAvailabilityServiceSettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        AvailabilityServiceConfig availConfig = config.getAvailabilityServiceConfig();
        boolean availEnabled = Boolean.valueOf(availConfig.getDefaultValue("AvailabilityEnabled")).booleanValue();
        String storePoolName = availConfig.getDefaultValue("StorePoolName");
        String haStoreName = availConfig.getDefaultValue("HAStoreName");
        String haAgentHosts = availConfig.getDefaultValue("HAAgentHosts");
        String haAgentPort = availConfig.getDefaultValue("HAAgentPort");
        String haAgentPwd = availConfig.getDefaultValue("HAAgentPassword");
        boolean autoMgStore = Boolean.valueOf(availConfig.getDefaultValue("AutoManageHAStore")).booleanValue();
        boolean storeHealthChk = Boolean.valueOf(availConfig.getDefaultValue("HAStoreHealthcheckEnabled")).booleanValue();
        String haStoreHealthChk = availConfig.getDefaultValue("HAStoreHealthcheckIntervalSeconds");
        handlerCtx.setOutputValue("AvailabilityEnabled", availEnabled);
        handlerCtx.setOutputValue("StorePoolName", storePoolName);
        handlerCtx.setOutputValue("HAStoreName", haStoreName);
        handlerCtx.setOutputValue("HAAgentHosts", haAgentHosts);
        handlerCtx.setOutputValue("HAAgentPort", haAgentPort);      
        handlerCtx.setOutputValue("HAAgentPassword", haAgentPwd);      
        handlerCtx.setOutputValue("AutoManage", autoMgStore);      
        handlerCtx.setOutputValue("StoreHealthCheck", storeHealthChk);      
        handlerCtx.setOutputValue("HAStoreHealthCheck", haStoreHealthChk);  
    }    
    
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Availability Service Page </p>
     *	<p> Input value: "ConfigName"           -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "AvailabilityEnabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "StorePoolName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HAStoreName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HAAgentHosts"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HAAgentPort"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "HAAgentPassword"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AutoManage"          -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "StoreHealthCheck"    -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "HAStoreHealthCheck"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"          -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAvailabilityServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerOutput(name="StorePoolName",            type=String.class),
        @HandlerOutput(name="HAStoreName",              type=String.class),
        @HandlerOutput(name="HAAgentHosts",             type=String.class),
        @HandlerOutput(name="HAAgentPort",              type=String.class),
        @HandlerOutput(name="HAAgentPassword",          type=String.class),
        @HandlerOutput(name="AutoManage",               type=Boolean.class),
        @HandlerOutput(name="StoreHealthCheck",         type=Boolean.class),
        @HandlerOutput(name="HAStoreHealthCheck",       type=String.class),
        @HandlerOutput(name="Properties",               type=Map.class)})
        
        public static void getAvailabilityServiceSettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        AvailabilityServiceConfig availConfig = config.getAvailabilityServiceConfig();
        boolean availEnabled = availConfig.getAvailabilityEnabled();
        String storePoolName = availConfig.getStorePoolName();
        String haStoreName = availConfig.getHAStoreName();
        String haAgentHosts = availConfig.getHAAgentHosts();
        String haAgentPort = availConfig.getHAAgentPort();
        String haAgentPwd = availConfig.getHAAgentPassword();
        boolean autoMgStore = availConfig.getAutoManageHAStore();
        boolean storeHealthChk = availConfig.getHAStoreHealthcheckEnabled();
        String haStoreHealthChk = availConfig.getHAStoreHealthcheckIntervalSeconds();
        Map<String, String> props = availConfig.getProperties();
        handlerCtx.setOutputValue("AvailabilityEnabled", availEnabled);
        handlerCtx.setOutputValue("StorePoolName", storePoolName);
        handlerCtx.setOutputValue("HAStoreName", haStoreName);
        handlerCtx.setOutputValue("HAAgentHosts", haAgentHosts);
        handlerCtx.setOutputValue("HAAgentPort", haAgentPort);      
        handlerCtx.setOutputValue("HAAgentPassword", haAgentPwd);      
        handlerCtx.setOutputValue("AutoManage", autoMgStore);      
        handlerCtx.setOutputValue("StoreHealthCheck", storeHealthChk);      
        handlerCtx.setOutputValue("HAStoreHealthCheck", haStoreHealthChk);      
        handlerCtx.setOutputValue("Properties", props);
        
    }
    
    /**
     *	<p> This handler saves the values for the attributes in 
     *      Availability Service Page </p>
     *	<p> Input value: "ConfigName"           -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "AvailabilityEnabled"  -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "StorePoolName"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "HAStoreName"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "HAAgentHosts"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "HAAgentPort"          -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "HAAgentPassword"      -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AutoManage"           -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "StoreHealthCheck"     -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "HAStoreHealthCheck"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"             -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"          -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveAvailabilityServiceSettings",
    input={
        @HandlerInput(name="ConfigName",               type=String.class, required=true),   
        @HandlerInput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerInput(name="StorePoolName",            type=String.class),
        @HandlerInput(name="HAStoreName",              type=String.class),
        @HandlerInput(name="HAAgentHosts",             type=String.class),
        @HandlerInput(name="HAAgentPort",              type=String.class),
        @HandlerInput(name="HAAgentPassword",          type=String.class),
        @HandlerInput(name="AutoManage",               type=Boolean.class),
        @HandlerInput(name="StoreHealthCheck",         type=Boolean.class),
        @HandlerInput(name="HAStoreHealthCheck",       type=String.class),
        @HandlerInput(name="AddProps",                 type=Map.class),
        @HandlerInput(name="RemoveProps",              type=ArrayList.class)})
        
        public static void saveAvailabilityServiceSettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            AvailabilityServiceConfig availConfig = config.getAvailabilityServiceConfig();
            availConfig.setAvailabilityEnabled(((Boolean)handlerCtx.getInputValue("AvailabilityEnabled")).booleanValue());
            availConfig.setStorePoolName(((String)handlerCtx.getInputValue("StorePoolName")));
            availConfig.setHAStoreName(((String)handlerCtx.getInputValue("HAStoreName")));
            availConfig.setHAAgentHosts(((String)handlerCtx.getInputValue("HAAgentHosts")));
            availConfig.setHAAgentPort(((String)handlerCtx.getInputValue("HAAgentPort")));
            availConfig.setHAAgentPassword(((String)handlerCtx.getInputValue("HAAgentPassword")));
            availConfig.setAutoManageHAStore(((Boolean)handlerCtx.getInputValue("AutoManage")).booleanValue());
            availConfig.setHAStoreHealthcheckEnabled(((Boolean)handlerCtx.getInputValue("StoreHealthCheck")).booleanValue());
            availConfig.setHAStoreHealthcheckIntervalSeconds(((String)handlerCtx.getInputValue("HAStoreHealthCheck")));
            AMXUtil.editProperties(handlerCtx, availConfig);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      Web Container Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                   -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "PersistenceType"             -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PersistenceFrequency"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PersistenceScope"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SingleSignOn"                -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "HttpSessionStore"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PersistenceHealthCheck"      -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultWebAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerOutput(name="PersistenceType",          type=String.class),
        @HandlerOutput(name="PersistenceFrequency",     type=String.class),
        @HandlerOutput(name="PersistenceScope",         type=String.class),
        @HandlerOutput(name="SingleSignOn",             type=Boolean.class),
        @HandlerOutput(name="HttpSessionStore",         type=String.class),
        @HandlerOutput(name="PersistenceHealthCheck",   type=Boolean.class)})
        
        public static void getDefaultWebAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        WebContainerAvailabilityConfig webConfig = config.getAvailabilityServiceConfig().getWebContainerAvailabilityConfig();
        boolean availEnabled = Boolean.valueOf(webConfig.getDefaultValue("AvailabilityEnabled")).booleanValue();
        String persistenceType = webConfig.getDefaultValue("PersistenceType");         
        String persistenceFreq = webConfig.getDefaultValue("PersistenceFrequency");        
        String persistenceScope = webConfig.getDefaultValue("PersistenceScope");        
        boolean singleSignOn = Boolean.valueOf(webConfig.getDefaultValue("SSOFailoverEnabled")).booleanValue();
        String httpSessionStore = webConfig.getDefaultValue("HTTPSessionStorePoolName");        
        boolean persistenceHealthChk = Boolean.valueOf(webConfig.getDefaultValue("PersistenceStoreHealthCheckEnabled")).booleanValue();
                
        handlerCtx.setOutputValue("AvailabilityEnabled", availEnabled);
        handlerCtx.setOutputValue("PersistenceType", persistenceType);
        handlerCtx.setOutputValue("PersistenceFrequency", persistenceFreq);
        handlerCtx.setOutputValue("PersistenceScope", persistenceScope);
        handlerCtx.setOutputValue("SingleSignOn", singleSignOn);      
        handlerCtx.setOutputValue("HttpSessionStore", httpSessionStore);      
        handlerCtx.setOutputValue("PersistenceHealthCheck", persistenceHealthChk);      
    }    
     
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Web Container Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                   -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "PersistenceType"             -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PersistenceFrequency"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PersistenceScope"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SingleSignOn"                -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "HttpSessionStore"            -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "PersistenceHealthCheck"      -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Properties"          -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerOutput(name="PersistenceType",          type=String.class),
        @HandlerOutput(name="PersistenceFrequency",     type=String.class),
        @HandlerOutput(name="PersistenceScope",         type=String.class),
        @HandlerOutput(name="SingleSignOn",             type=Boolean.class),
        @HandlerOutput(name="HttpSessionStore",         type=String.class),
        @HandlerOutput(name="PersistenceHealthCheck",   type=Boolean.class),
        @HandlerOutput(name="Properties",               type=Map.class)})
        
        public static void getWebAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        WebContainerAvailabilityConfig webConfig = config.getAvailabilityServiceConfig().getWebContainerAvailabilityConfig();
        boolean availEnabled = webConfig.getAvailabilityEnabled();
        String persistenceType = webConfig.getPersistenceType();
        String persistenceFreq = webConfig.getPersistenceFrequency();
        String persistenceScope = webConfig.getPersistenceScope();
        boolean singleSignOn = webConfig.getSSOFailoverEnabled();
        String httpSessionStore = webConfig.getHTTPSessionStorePoolName();
        boolean persistenceHealthChk = webConfig.getPersistenceStoreHealthCheckEnabled();
        Map<String, String> props = webConfig.getProperties();
        handlerCtx.setOutputValue("AvailabilityEnabled", availEnabled);
        handlerCtx.setOutputValue("PersistenceType", persistenceType);
        handlerCtx.setOutputValue("PersistenceFrequency", persistenceFreq);
        handlerCtx.setOutputValue("PersistenceScope", persistenceScope);
        handlerCtx.setOutputValue("SingleSignOn", singleSignOn);      
        handlerCtx.setOutputValue("HttpSessionStore", httpSessionStore);      
        handlerCtx.setOutputValue("PersistenceHealthCheck", persistenceHealthChk);      
        handlerCtx.setOutputValue("Properties", props);       
    }
    
    /**
     *	<p> This handler saves the values for the attributes in 
     *      Web Container Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                  -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "PersistenceType"             -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "PersistenceFrequency"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "PersistenceScope"            -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SingleSignOn"                -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "HttpSessionStore"            -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "PersistenceHealthCheck"      -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "AddProps"                    -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"                 -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveWebAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName",               type=String.class, required=true),   
        @HandlerInput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerInput(name="PersistenceType",          type=String.class),
        @HandlerInput(name="PersistenceFrequency",     type=String.class),
        @HandlerInput(name="PersistenceScope",         type=String.class),
        @HandlerInput(name="SingleSignOn",             type=Boolean.class),
        @HandlerInput(name="HttpSessionStore",         type=String.class),
        @HandlerInput(name="PersistenceHealthCheck",   type=Boolean.class),
        @HandlerInput(name="AddProps",                 type=Map.class),
        @HandlerInput(name="RemoveProps",              type=ArrayList.class)})
        
        public static void saveWebAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            WebContainerAvailabilityConfig webConfig = config.getAvailabilityServiceConfig().getWebContainerAvailabilityConfig();
            webConfig.setAvailabilityEnabled(((Boolean)handlerCtx.getInputValue("AvailabilityEnabled")).booleanValue());
            webConfig.setPersistenceType(((String)handlerCtx.getInputValue("PersistenceType")));
            webConfig.setPersistenceFrequency(((String)handlerCtx.getInputValue("PersistenceFrequency")));
            webConfig.setPersistenceScope(((String)handlerCtx.getInputValue("PersistenceScope")));
            webConfig.setSSOFailoverEnabled(((Boolean)handlerCtx.getInputValue("SingleSignOn")).booleanValue());
            webConfig.setHTTPSessionStorePoolName(((String)handlerCtx.getInputValue("HttpSessionStore")));
            webConfig.setPersistenceStoreHealthCheckEnabled(((Boolean)handlerCtx.getInputValue("PersistenceHealthCheck")));
            AMXUtil.editProperties(handlerCtx, webConfig);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      Web Container Availability Tab of Availability Service page </p>
     *  <p> Output value: "PersistenceTypeList"         -- Type: <code>SelectItem[].class</code></p>
     *  <p> Output value: "PersistenceFrequencyList"    -- Type: <code>SelectItem[].class</code></p>
     *  <p> Output value: "PersistenceScopeList"        -- Type: <code>SelectItem[].class</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getWebAvailabilityLists",
    output={
        @HandlerOutput(name="PersistenceTypeList",      type=SelectItem[].class),
        @HandlerOutput(name="PersistenceFrequencyList", type=SelectItem[].class),
        @HandlerOutput(name="PersistenceScopeList",     type=SelectItem[].class)})
        
        public static void getWebAvailabilityLists(HandlerContext handlerCtx) {
        EEPersistenceTypeResolver resolver = new EEPersistenceTypeResolver();
        List persistenceList = resolver.getWebDefinedPersistenceTypes();
        String[] persistenceTypes = (String[])persistenceList.toArray(new String[persistenceList.size()]);
        handlerCtx.setOutputValue("PersistenceTypeList", getOptions(persistenceTypes));      
        handlerCtx.setOutputValue("PersistenceFrequencyList", getOptions(persistenceFrequency));      
        handlerCtx.setOutputValue("PersistenceScopeList", getOptions(persistenceScope));      
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      Ejb Container Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                   -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "HAPersistenceType"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SFSBPersistenceType"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SFSBStoreName"               -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultEjbAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerOutput(name="HAPersistenceType",        type=String.class),
        @HandlerOutput(name="SFSBPersistenceType",      type=String.class),
        @HandlerOutput(name="SFSBStoreName",            type=String.class)})
        
        public static void getDefaultEjbAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        EJBContainerAvailabilityConfig ejbConfig = config.getAvailabilityServiceConfig().getEJBContainerAvailabilityConfig();
        boolean availEnabled = Boolean.valueOf(ejbConfig.getDefaultValue("AvailabilityEnabled")).booleanValue();
        String haPersistenceType = ejbConfig.getDefaultValue("SFSBHAPersistenceType");         
        String sfsbPersistenceType = ejbConfig.getDefaultValue("SFSBPersistenceType");        
        String sfsbStoreName = ejbConfig.getDefaultValue("SFSBStorePoolName");        
               
        handlerCtx.setOutputValue("AvailabilityEnabled", availEnabled);
        handlerCtx.setOutputValue("HAPersistenceType", haPersistenceType);
        handlerCtx.setOutputValue("SFSBPersistenceType", sfsbPersistenceType);
        handlerCtx.setOutputValue("SFSBStoreName", sfsbStoreName);
    }    
     
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Ejb Container Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                   -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "HAPersistenceType"           -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SFSBPersistenceType"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SFSBStoreName"               -- Type: <code>java.lang.String</code></p>      
     *  <p> Output value: "Properties"          -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getEjbAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerOutput(name="HAPersistenceType",        type=String.class),
        @HandlerOutput(name="SFSBPersistenceType",      type=String.class),
        @HandlerOutput(name="SFSBStoreName",            type=String.class),
        @HandlerOutput(name="Properties",               type=Map.class)})
        
        public static void getEjbAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        EJBContainerAvailabilityConfig ejbConfig = config.getAvailabilityServiceConfig().getEJBContainerAvailabilityConfig();
        boolean availEnabled = ejbConfig.getAvailabilityEnabled();
        String haPersistenceType = ejbConfig.getSFSBHAPersistenceType();
        String sfsbPersistenceType = ejbConfig.getSFSBPersistenceType();
        String sfsbStoreName = ejbConfig.getSFSBStorePoolName();      
        Map<String, String> props = ejbConfig.getProperties();
        handlerCtx.setOutputValue("AvailabilityEnabled", availEnabled);
        handlerCtx.setOutputValue("HAPersistenceType", haPersistenceType);
        handlerCtx.setOutputValue("SFSBPersistenceType", sfsbPersistenceType);
        handlerCtx.setOutputValue("SFSBStoreName", sfsbStoreName);
        handlerCtx.setOutputValue("Properties", props);       
    }
    
    /**
     *	<p> This handler saves the values for the attributes in 
     *      Ejb Container Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                  -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "HAPersistenceType"           -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SFSBPersistenceType"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SFSBStoreName"               -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"                    -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"                 -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveEjbAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName",               type=String.class, required=true),   
        @HandlerInput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerInput(name="HAPersistenceType",          type=String.class),
        @HandlerInput(name="SFSBPersistenceType",     type=String.class),
        @HandlerInput(name="SFSBStoreName",         type=String.class),
        @HandlerInput(name="AddProps",                 type=Map.class),
        @HandlerInput(name="RemoveProps",              type=ArrayList.class)})
        
        public static void saveEjbAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            EJBContainerAvailabilityConfig ejbConfig = config.getAvailabilityServiceConfig().getEJBContainerAvailabilityConfig();
            ejbConfig.setAvailabilityEnabled(((Boolean)handlerCtx.getInputValue("AvailabilityEnabled")).booleanValue());
            ejbConfig.setSFSBHAPersistenceType(((String)handlerCtx.getInputValue("HAPersistenceType")));
            ejbConfig.setSFSBPersistenceType(((String)handlerCtx.getInputValue("SFSBPersistenceType")));
            ejbConfig.setSFSBStorePoolName(((String)handlerCtx.getInputValue("SFSBStoreName")));
            AMXUtil.editProperties(handlerCtx, ejbConfig);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      Ejb Container Availability Tab of Availability Service page </p>
     *  <p> Output value: "PersistenceTypeList"         -- Type: <code>SelectItem[].class</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getEjbAvailabilityLists",
    output={
        @HandlerOutput(name="PersistenceTypeList",      type=SelectItem[].class)})
        public static void getEjbAvailabilityLists(HandlerContext handlerCtx) {
        EEPersistenceTypeResolver resolver = new EEPersistenceTypeResolver();
        List persistenceList = resolver.getEjbDefinedPersistenceTypes();
        String[] persistenceTypes = (String[])persistenceList.toArray(new String[persistenceList.size()]);
        handlerCtx.setOutputValue("PersistenceTypeList", getOptions(persistenceTypes));      
    }
    
    /**
     *	<p> This handler returns the default values for the attributes in 
     *      Jms Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                   -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "StorePoolName"               -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDefaultJmsAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerOutput(name="StorePoolName",            type=String.class)})
        
        public static void getDefaultJmsAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        JMSAvailabilityConfig jmsConfig = config.getAvailabilityServiceConfig().getJMSAvailabilityConfig();
        boolean availEnabled = Boolean.valueOf(jmsConfig.getDefaultValue("AvailabilityEnabled")).booleanValue();
        String storePoolName = jmsConfig.getDefaultValue("MQStorePoolName");         
               
        handlerCtx.setOutputValue("AvailabilityEnabled", availEnabled);
        handlerCtx.setOutputValue("StorePoolName", storePoolName);
    }    
     
    /**
     *	<p> This handler returns the values for the attributes in 
     *      Jms Container Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                   -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "StorePoolName"               -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"          -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJmsAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerOutput(name="StorePoolName",            type=String.class),
        @HandlerOutput(name="Properties",               type=Map.class)})
        
        public static void getJmsAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        JMSAvailabilityConfig jmsConfig = config.getAvailabilityServiceConfig().getJMSAvailabilityConfig();
        boolean availEnabled = jmsConfig.getAvailabilityEnabled();
        String storePoolName = jmsConfig.getMQStorePoolName();
        Map<String, String> props = jmsConfig.getProperties();
        handlerCtx.setOutputValue("AvailabilityEnabled", availEnabled);
        handlerCtx.setOutputValue("StorePoolName", storePoolName);
        handlerCtx.setOutputValue("Properties", props);       
    }
    
    /**
     *	<p> This handler saves the values for the attributes in 
     *      Jms Container Availability Tab of Availability Service page </p>
     *	<p> Input value: "ConfigName"                  -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "AvailabilityEnabled"         -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "StorePoolName"               -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"                    -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"                 -- Type: <code>java.util.ArrayList</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveJmsAvailabilitySettings",
    input={
        @HandlerInput(name="ConfigName",               type=String.class, required=true),   
        @HandlerInput(name="AvailabilityEnabled",      type=Boolean.class),
        @HandlerInput(name="StorePoolName",            type=String.class),
        @HandlerInput(name="AddProps",                 type=Map.class),
        @HandlerInput(name="RemoveProps",              type=ArrayList.class)})
        
        public static void saveJmsAvailabilitySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            JMSAvailabilityConfig jmsConfig = config.getAvailabilityServiceConfig().getJMSAvailabilityConfig();
            jmsConfig.setAvailabilityEnabled(((Boolean)handlerCtx.getInputValue("AvailabilityEnabled")).booleanValue());
            jmsConfig.setMQStorePoolName(((String)handlerCtx.getInputValue("StorePoolName")));
            AMXUtil.editProperties(handlerCtx, jmsConfig);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    public static SelectItem[] getOptions(String[] values){
        if (values == null){
           SelectItem[] options = (SelectItem []) Array.newInstance(SUN_OPTION_CLASS, 0);
           return options;
        }
        SelectItem[] options =
                (SelectItem []) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i =0; i < values.length; i++) {
            SelectItem option = getSunOption(values[i], values[i]);
            options[i] = option;
        }
        return options;
    }
   
     public static Option[] getOptionsArray(String[] values){
        Option[] options =
                (Option []) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i =0; i < values.length; i++) {
            Option option = getOption(values[i], values[i]);
            options[i] = option;
        }
        return options;
    }
     
    public static Option getOption(String value, String label) {
	try {
	    return (Option) SUN_OPTION_CONSTRUCTOR.newInstance(value, label);
	} catch (Exception ex) {
	    return null;
	}
    } 
     
    public static SelectItem[] getOptions(String[] values, String[] labels){
        SelectItem[] options =
                (SelectItem []) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i =0; i < values.length; i++) {
            SelectItem option = getSunOption(values[i], labels[i]);
            options[i] = option;
        }
        return options;
    }
    
    public static SelectItem[] getModOptions(String[] values){
        int size = (values == null)? 1 : values.length +1;
        SelectItem[] options =
	    (SelectItem []) Array.newInstance(SUN_OPTION_CLASS, size);
        options[0] = getSunOption("", "");
	for (int i = 0; i < values.length; i++) {
	    SelectItem option = getSunOption(values[i], values[i]);
	    options[i+1] = option;
	}
        return options;
    }
   
    public static SelectItem getSunOption(String value, String label) {
	try {
	    return (SelectItem) SUN_OPTION_CONSTRUCTOR.newInstance(value, label);
	} catch (Exception ex) {
	    return null;
	}
    }

    private static Class	     SUN_OPTION_CLASS = null;
    private static Constructor SUN_OPTION_CONSTRUCTOR = null;

    static {
	try {
	    SUN_OPTION_CLASS =
		Class.forName("com.sun.webui.jsf.model.Option");
	    SUN_OPTION_CONSTRUCTOR = SUN_OPTION_CLASS.
		getConstructor(new Class[] {Object.class, String.class});
	} catch (Exception ex) {
	    // Ignore exception here, NPE will be thrown when attempting to
	    // use SUN_OPTION_CONSTRUCTOR.
	}
    }
        

    //mbean Attribute Name
    private static final String ATTRIBUTE_NAME   = "jvm-options";
    private static final String JVM_OPTION_SECURITY_MANAGER = "-Djava.security.manager";
    private static final String JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL = "-Djava.security.manager=";    
    private static String[] providerTypes = {"client", "server", "client-server"}; //NOI18N
    private static String[] authSource = {"sender", "content"}; //NOI18N
    private static String[] authRecipient  = {"before-content", "after-content"}; //NOI18N
    private static String[] messageLayers = {"SOAP", "HttpServlet"}; //NOI18N
    private static String[] persistenceFrequency = {"time-based",  "web-method"}; //NOI18N
    private static String[] persistenceScope = {"session",  "modified-session", "modified-attribute"}; //NOI18N
    
    private static List vsSkipPropsList = new ArrayList();
    private static List httpServiceSkipPropsList = new ArrayList();
    
    static {
        vsSkipPropsList.add("accesslog");
        vsSkipPropsList.add("docroot");
        vsSkipPropsList.add("sso-enabled");
        vsSkipPropsList.add("sso-enabled");
        vsSkipPropsList.add("accessLogBufferSize");
        vsSkipPropsList.add("accessLogWriteInterval");
        vsSkipPropsList.add("accessLoggingEnabled");
    
        httpServiceSkipPropsList.add("accessLogBufferSize");
        httpServiceSkipPropsList.add("accessLogWriteInterval");
        httpServiceSkipPropsList.add("accessLoggingEnabled");
    }
    
    
    
    private static final String ADMIN_LISTENER = "admin-listener";
    private static final String PROXIED_PROTOCOLS = "proxiedProtocols";
    private static final String PROXIED_PROTOCOLS_VALUE = "http";
    
}
