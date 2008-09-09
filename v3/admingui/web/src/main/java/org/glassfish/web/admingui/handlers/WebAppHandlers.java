
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
 * WebAppHandler.java
 *
 * Created on August 10, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author anilam
 */

package org.glassfish.web.admingui.handlers;

import com.sun.appserv.management.config.AppClientModuleConfig;
import com.sun.appserv.management.config.ApplicationConfig;
import com.sun.appserv.management.config.CustomMBeanConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.EJBModuleConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.LifecycleModuleConfig;
import com.sun.appserv.management.config.ObjectTypeValues;
import com.sun.appserv.management.config.RARModuleConfig;
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.WebModuleConfig;

import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.ObjectName;

import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.TargetUtil;


public class WebAppHandlers {
    /** Creates a new instance of ApplicationsHandler */
    public WebAppHandlers() {
    }
    
    /**
     *	<p> This handler returns the values for all the attributes of the Application
     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
     *  <p> Output value: "description" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "enbled" -- Type: <code>java.lang.Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getApplicationInfo",
        input={
            @HandlerInput(name="name", type=String.class, required=true),
            @HandlerInput(name="appType", type=String.class, required=true)},
        output={
            @HandlerOutput(name="location", type=String.class),
            @HandlerOutput(name="libraries", type=String.class),
            @HandlerOutput(name="contextRoot", type=String.class),
            @HandlerOutput(name="vs", type=String.class),
            @HandlerOutput(name="description", type=String.class),
            @HandlerOutput(name="threadPool", type=String.class),
            @HandlerOutput(name="availEnabled", type=Boolean.class),
            @HandlerOutput(name="javaWebStart", type=Boolean.class),
            @HandlerOutput(name="enabledString", type=String.class),
            @HandlerOutput(name="enabled", type=Boolean.class)} )
    public static void getApplicationInfo(HandlerContext handlerCtx) {
        
        String name = (String) handlerCtx.getInputValue("name");
        String appType = (String) handlerCtx.getInputValue("appType");
        AMXRoot amxRoot = AMXRoot.getInstance();

        
        ApplicationConfig appConfig = amxRoot.getApplicationsConfig().getApplicationConfigMap().get(name);
        if (appConfig == null){
            System.out.println("!!!!!! Error: Cannot find application with the name: " + name);
            return;
        }
        handlerCtx.setOutputValue("contextRoot", appConfig.getContextRoot());
	//handlerCtx.setOutputValue("availEnabled", appConfig.getAvailabilityEnabled());
        if(!amxRoot.supportCluster()) {
            //We need this only for PE, so hard code it "server"
            handlerCtx.setOutputValue("vs", TargetUtil.getAssociatedVS(name, "server"));
        }

	handlerCtx.setOutputValue("location", appConfig.getLocation());
	handlerCtx.setOutputValue("description", appConfig.getDescription());
	
        if(amxRoot.isEE())
            handlerCtx.setOutputValue("enabledString", TargetUtil.getEnabledStatus(appConfig, true));
        else
            handlerCtx.setOutputValue("enabled", TargetUtil.isApplicationEnabled(appConfig, "server" ));
        
        if (!"connector".equals(appType)){
            handlerCtx.setOutputValue("libraries", appConfig.getLibraries());
        }
    }
    


    /**
     *	<p> This handler save  the values for all the attributes of the Application
     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveApplicationInfo",
        input={
            @HandlerInput(name="name", type=String.class, required=true),
            @HandlerInput(name="appType", type=String.class, required=true),
            @HandlerInput(name="description", type=String.class),
            @HandlerInput(name="contextRoot", type=String.class),
            @HandlerInput(name="vs", type=String.class),
            @HandlerInput(name="javaWebStart", type=Boolean.class),
            @HandlerInput(name="threadPool", type=String.class),
            @HandlerInput(name="enabled", type=Boolean.class),
            @HandlerInput(name="availEnabled", type=String.class)
        })
    public static void saveApplicationInfo(HandlerContext handlerCtx) {

        String target = "server";   //TODO: Fix for EE
        String name = (String) handlerCtx.getInputValue("name");
        String appType = (String) handlerCtx.getInputValue("appType");
        AMXRoot amxRoot = AMXRoot.getInstance();
        
        try{
	
	    ApplicationConfig appConfig = amxRoot.getApplicationsConfig().getApplicationConfigMap().get(name);
            if (appConfig == null){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.NoSuchApplication"));
                return;
            }
            appConfig.setContextRoot((String)handlerCtx.getInputValue("contextRoot"));
            if (amxRoot.isEE()){
                appConfig.setAvailabilityEnabled((String) handlerCtx.getInputValue("availEnabled"));
            }
            else {
                String vs = (String)handlerCtx.getInputValue("vs");
                //only for PE, so hard-code to 'server'
                TargetUtil.setVirtualServers(name, "server", vs);
            }
            appConfig.setDescription((String)handlerCtx.getInputValue("description"));
            if(! amxRoot.isEE()){
                Boolean enabled = (Boolean) handlerCtx.getInputValue("enabled");
                TargetUtil.setApplicationEnabled(appConfig, "server", enabled); 
            }
        }catch(Exception ex){
            ex.printStackTrace();
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

        /**
     *	<p> This handler returns the list of web applications for populating the table.
     *  <p> Input  value: "name" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDeployedWebInfo",
        input={
            @HandlerInput(name="serverName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getDeployedWebInfo(HandlerContext handlerCtx){
        
        String serverName = (String) handlerCtx.getInputValue("serverName");
        Map<String, ApplicationConfig> webAppsConfig = AMXUtil.getApplicationConfigByType("web");
        List result = new ArrayList();
        for(ApplicationConfig appConfig : webAppsConfig.values()){
            if (ObjectTypeValues.USER.equals(appConfig.getObjectType())){
                HashMap oneRow = new HashMap();
                String protocol = "http" ;
                String enable =  TargetUtil.getEnabledStatus(appConfig, true);
                oneRow.put("name", appConfig.getName());
                oneRow.put("enabled", enable);
                String contextRoot = appConfig.getContextRoot();
                oneRow.put("contextRoot", contextRoot);
                String port = getPortForApplication(appConfig.getName());
                if (port.startsWith("-") ){
                    protocol="https";
                    port = port.substring(1);
                }
                oneRow.put("port", port);
                if(AMXRoot.getInstance().isEE()){
                    if (enable.equals(GuiUtil.getMessage("deploy.allDisabled")) ||
                            enable.equals(GuiUtil.getMessage("deploy.noTarget")))
                        oneRow.put("hasLaunch", false);
                    else
                        oneRow.put("hasLaunch", true);
                }else{
                    oneRow.put("hasLaunch", Boolean.parseBoolean(enable) );
                    String ctxRoot = calContextRoot(contextRoot);
                    oneRow.put("launchLink", protocol+"://"+serverName+":"+ port + ctxRoot);
                }
                oneRow.put("selected", false);
                //List<String> targets = TargetUtil.getDeployedTargets(appConfig, true);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("result", result);
    }

    /**
     *	<p> Returns the app type for displaying in the redeploy page
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAppTypeForDisplay",
        input={
            @HandlerInput(name="appType", type=String.class)},
        output={
            @HandlerOutput(name="displayType", type=String.class)}
     )
    public static void getAppTypeForDisplay(HandlerContext handlerCtx){
        String appType = (String) handlerCtx.getInputValue("appType");
        handlerCtx.setOutputValue("displayType", displayMap.get(appType));
    }
    
    
//    
//    /** 
//     * <p> Handler to set the viewKey which is used to decide if user wants summary or detail view.
//     */
//    @Handler(id="setAppViewKey",
//        input={
//            @HandlerInput(name="key", type=String.class, required=true),
//            @HandlerInput(name="selectedValue", type=String.class)}
//     )
//    public static void setAppViewKey(HandlerContext handlerCtx){
//            String key = (String) handlerCtx.getInputValue("key");
//            String selectedValue = (String) handlerCtx.getInputValue("selectedValue");
//            if (selectedValue.equals("summary"))
//                handlerCtx.getFacesContext().getExternalContext().getSessionMap().put(key, true);
//            else
//                handlerCtx.getFacesContext().getExternalContext().getSessionMap().put(key, false);
//    }
//    

        /**
     *	This method determines the hostname of the given serverInstance
     *	ObjectName to the best of its ability.  It will attempt to obtain the node-agent....
     *
     *	@param	serverInstance	The ObjectName to use to determine the hostname
     */
    protected String getHost(ObjectName serverInstance) {
        
        return "";
        /* TODO-V3
         * 
	// Find the node agent (if there is one)
	String nodeAgentRef = (String)JMXUtil.getAttribute(serverInstance, "node-agent-ref");
	if ((nodeAgentRef == null) || nodeAgentRef.equals("")) {
	    return getDefaultHostName();
	}

	// Get the JMX connector for the node agent
	ObjectName jmxConnector = (ObjectName)JMXUtil.invoke(
		"com.sun.appserv:type=node-agent,name="+nodeAgentRef+
		    ",category=config",
		"getJmxConnector", null, null);
	if (jmxConnector == null) {
	    return getDefaultHostName();
	}
	
	// Try to get the hostname
	// Get "client-hostname" from the properties (use this way instead
	// of getProperty to avoid exception
	AttributeList properties = (AttributeList)JMXUtil.invoke(
		jmxConnector, "getProperties", null, null);
	Attribute att;
	String hostName = null;
	Iterator it = properties.iterator();
	while (it.hasNext()) {
	    att = (Attribute)it.next();
	    if (att.getName().equals("client-hostname")) {
		hostName = (String)att.getValue();
		break;
	    }
	}

	// Get default host name
	if ((hostName == null) || hostName.equals("") || hostName.equals("0.0.0.0")) {
	    return getDefaultHostName();
	}

	// We found the hostname!!
	return hostName;
         */
    }


    /**
     *	This method is used as a fallback when no Hostname is provided.
     */
    public static String getDefaultHostName() {
        String defaultHostName = "localhost";
	try {
	    InetAddress host = InetAddress.getLocalHost();
	    defaultHostName = host.getCanonicalHostName();
	} catch(UnknownHostException uhe) {
//	    sLogger.log(Level.FINEST, "mbean.get_local_host_error", uhe);
//	    sLogger.log(Level.INFO, "mbean.use_default_host");
	}
	return defaultHostName;
    }
    
    
    /* returns the port number on which appName could be executed 
     * will try to get a port number that is not secured.  But if it can't find one, a
     * secured port will be returned, prepanded with '-'
     */
    static String getPortForApplication(String appName) {
        
        DeployedItemRefConfig appRef = TargetUtil.getDeployedItemRefObject(appName, "server");
        String vsId = null;
        if (appRef == null) { // no ref found for this application
            vsId = getNonAdminVirtualServer();
        } else {
            vsId = TargetUtil.getAssociatedVS(appName, "server");
            if (vsId == null || vsId.length() ==0) { // no vs found for this application
                vsId = getNonAdminVirtualServer();
            } else {
                if (vsId.indexOf(",") > 0) {
                    vsId = vsId.substring(0, vsId.indexOf(","));
                }
            }
        }
        if (vsId == null)
            return ""; // no vs found for this app..

        String port = null;
        Boolean secure = false;
        try{
            final HTTPServiceConfig httpServiceConfig = AMXRoot.getInstance().getConfig("server-config").getHTTPServiceConfig();
             VirtualServerConfig vsConfig = httpServiceConfig.getVirtualServerConfigMap().get(vsId);
             if (vsConfig != null) {
                String listeners = vsConfig.getHTTPListeners();
                if (!GuiUtil.isEmpty(listeners)) {
                    StringTokenizer tok = new StringTokenizer(listeners, ",");
                    String listener = "";
                    while (tok.hasMoreTokens()) {
                        listener = tok.nextToken();
                        HTTPListenerConfig hConfig = httpServiceConfig.getHTTPListenerConfigMap().get(listener);
                        secure = Boolean.valueOf(hConfig.getSecurityEnabled());
                        port = hConfig.getPort();
                        if (! secure) break;
                    }
                }
            }
            return (secure) ? "-" + port : port;
        }catch(Exception ex){
            //Maybe the vitrual server is not found, maybe there is no http listener
            //this can be the case due to user error during deployment. refer to issue#2807.
            //TODO: use logger
            ex.printStackTrace();
            return "";
        }
        
    }
    
    // returns 'first' nonadmin virtual server -
    private static String getNonAdminVirtualServer() {
        
        Map<String, VirtualServerConfig> vsMap = AMXRoot.getInstance().getConfig("server-config").getHTTPServiceConfig().getVirtualServerConfigMap();
        for(String vsName : vsMap.keySet()){
            if (! vsName.equals("__asadmin")){
                return vsName;
            }
        }
        return "";
    }
  
    
    private static String calContextRoot(String contextRoot){
        //If context root is not specified or if the context root is "/", ensure that we don't show two // at the end.
        //refer to issue#2853
        String ctxRoot = "";
        if ((contextRoot == null) || contextRoot.equals("") || contextRoot.equals("/"))
            ctxRoot = "/";
        else
        if (contextRoot.startsWith("/"))
            ctxRoot = contextRoot;
        else
            ctxRoot = "/" + contextRoot;
        return ctxRoot;
    }
    
      static private Map<String, String> displayMap = new HashMap();
      static private Map<String, String> editMap = new HashMap();
      static private Map<String, String> typeMap = new HashMap();
      static{
        editMap.put(J2EEApplicationConfig.J2EE_TYPE, "enterpriseApplicationsEdit.jsf");
        editMap.put(WebModuleConfig.J2EE_TYPE, "webApplicationsEdit.jsf");
        editMap.put(EJBModuleConfig.J2EE_TYPE, "ejbModulesEdit.jsf");
        editMap.put(LifecycleModuleConfig.J2EE_TYPE, "lifecycleModulesEdit.jsf");
        editMap.put(RARModuleConfig.J2EE_TYPE, "connectorModulesEdit.jsf");
        editMap.put(AppClientModuleConfig.J2EE_TYPE, "appclientModulesEdit.jsf");
        editMap.put(CustomMBeanConfig.J2EE_TYPE, "customMBeansEdit.jsf");
        
        typeMap.put(J2EEApplicationConfig.J2EE_TYPE, GuiUtil.getMessage("tree.enterpriseApps"));
        typeMap.put(WebModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.webApps"));
        typeMap.put(EJBModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.ejbModules"));
        typeMap.put(LifecycleModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.lifecycleModules"));
        typeMap.put(RARModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.connectorModules"));
        typeMap.put(AppClientModuleConfig.J2EE_TYPE, GuiUtil.getMessage("tree.appclientModules"));
        typeMap.put(CustomMBeanConfig.J2EE_TYPE, GuiUtil.getMessage("tree.customMBeans"));
        
        displayMap.put("application", GuiUtil.getMessage("deploy.ear"));
        displayMap.put("webApp", GuiUtil.getMessage("deploy.war"));
        displayMap.put("ejbModule", GuiUtil.getMessage("deploy.ejb"));
        displayMap.put("appclient", GuiUtil.getMessage("deploy.appClient"));
        displayMap.put("connector", GuiUtil.getMessage("deploy.rar"));
      }
   
   
}
