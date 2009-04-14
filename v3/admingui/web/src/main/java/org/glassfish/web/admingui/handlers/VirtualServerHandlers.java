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

package org.glassfish.admingui.handlers;


import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.ApplicationConfig;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import javax.faces.model.SelectItem;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  


import org.glassfish.admingui.common.util.AMXRoot; 
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.AMXUtil;

import com.sun.appserv.management.config.ConfigConfig; 
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.ConfigElement;
import com.sun.appserv.management.config.VirtualServerConfigKeys;

/**
 *
 * @author anilam
 */
public class VirtualServerHandlers {
    
    
        
   /**
     *	<p> This handler returns the list of specified config elements for populating the table.
     *  <p> Input  value: "type" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getVirtualServerList",
        input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getVirtualServerList(HandlerContext handlerCtx){
        
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        List result = new ArrayList();
        try{
            if (config == null){   //this case should never happen, issue#1966
                handlerCtx.setOutputValue("result", result);
                return;
            }

            Iterator iter = null;
            iter = config.getHTTPServiceConfig().getVirtualServerConfigMap().values().iterator();
                       
            List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
            boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;

            if (iter != null){
                while(iter.hasNext()){
                    ConfigElement configE = (ConfigElement) iter.next();
                    HashMap oneRow = new HashMap();
                    String name=configE.getName();                
                    oneRow.put("name", name);
                    oneRow.put("selected", (hasOrig)? GuiUtil.isSelected(name, selectedList): false);
                    String state = ((VirtualServerConfig)configE).getState();
                    String host = ((VirtualServerConfig)configE).getHosts();
                    oneRow.put("state", (state == null) ? " ": state);
                    oneRow.put("host", (host == null) ? " ": host);
                    result.add(oneRow);
                }
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
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
     *	@param	handlerCtx	The HandlerContext.
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
        
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                getDefaultVirtualServerAttributes(handlerCtx, config);
                return;
            }
            Map<String,VirtualServerConfig>vservers = config.getHTTPServiceConfig().getVirtualServerConfigMap();
            VirtualServerConfig vs = (VirtualServerConfig)vservers.get((String)handlerCtx.getInputValue("Name"));
            
            handlerCtx.setOutputValue("Hosts", vs.getHosts());
            handlerCtx.setOutputValue("StateOption", vs.getState());
            handlerCtx.setOutputValue("Http", vs.getHTTPListeners());
            handlerCtx.setOutputValue("Web", vs.getDefaultWebModule());
            handlerCtx.setOutputValue("LogFile", vs.getLogFile());
            
            Map aMap = AMXUtil.getNonSkipPropertiesMap(vs, vsSkipPropsList);
            handlerCtx.setOutputValue("Properties", aMap);
            handlerCtx.setOutputValue("accessLogBufferSize", AMXUtil.getPropertyValue(vs, "accessLogBufferSize"));
            handlerCtx.setOutputValue("accessLogWriteInterval", AMXUtil.getPropertyValue(vs, "accessLogWriteInterval"));
            handlerCtx.setOutputValue("accesslog", AMXUtil.getPropertyValue(vs, "accesslog"));
            handlerCtx.setOutputValue("docroot", AMXUtil.getPropertyValue(vs, "docroot"));
            String sso = vs.getSsoEnabled();
            Boolean ssoFlag = false;
            if ( GuiUtil.isEmpty(sso))
                ssoFlag = false;
            else
            ssoFlag = (sso.equals("true")) ? true: false;
            
            handlerCtx.setOutputValue("sso", ssoFlag);
            
            String accessLoggingFlag = AMXUtil.getPropertyValue(vs, "accessLoggingEnabled") ;
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
    
        private static void getDefaultVirtualServerAttributes(HandlerContext handlerCtx, ConfigConfig config){
        
      
        Map <String,String> defaultMap = config.getHTTPServiceConfig().getDefaultValues(XTypes.VIRTUAL_SERVER_CONFIG, true);
        Map <String,String> defaultMapFF = config.getHTTPServiceConfig().getDefaultValues(XTypes.VIRTUAL_SERVER_CONFIG, false);
        handlerCtx.setOutputValue("Hosts", defaultMap.get("Hosts"));
        handlerCtx.setOutputValue("StateOption", defaultMap.get("State"));
        handlerCtx.setOutputValue("LogFile", defaultMap.get("LogFile"));
        handlerCtx.setOutputValue("sso", Boolean.FALSE);
        handlerCtx.setOutputValue("docroot", defaultMap.get("DocRoot"));
        
        /* commented out for now until issue# 5811 is resolved.
         * 
        Map<String, VirtualServerConfig> vsMap = config.getHTTPServiceConfig().getVirtualServerConfigMap();
        if (vsMap.size() > 0){
            Object[] vsc = vsMap.values().toArray();
            VirtualServerConfig vs = (VirtualServerConfig) vsc[0];
            handlerCtx.setOutputValue("accesslog", vs.getDefaultValues(XTypes.HTTP_ACCESS_LOG_CONFIG, true).get("LogDirectory"));
        }else 
        */
        {
            //just hard code
            handlerCtx.setOutputValue("accesslog", "${com.sun.aas.instanceRoot}/logs/access" );
        }
        handlerCtx.setOutputValue("sso", Boolean.FALSE);
        handlerCtx.setOutputValue("accessLoggingFlag", "off");
        handlerCtx.setOutputValue("Properties", new HashMap());
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
     *	@param	handlerCtx	The HandlerContext.
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
        
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        Map newProps = (Map)handlerCtx.getInputValue("newProps");
        AMXRoot amxRoot = AMXRoot.getInstance();
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map convertedMap = AMXUtil.convertToPropertiesOptionMap(newProps, null);
                AMXUtil.putOptionalValue((String) handlerCtx.getInputValue("accessLogBufferSize"), convertedMap, "accessLogBufferSize");
                AMXUtil.putOptionalValue((String) handlerCtx.getInputValue("accessLogWriteInterval"), convertedMap, "accessLogWriteInterval");

                convertedMap.put(VirtualServerConfigKeys.HTTP_LISTENERS_KEY,handlerCtx.getInputValue("Http"));
                convertedMap.put(VirtualServerConfigKeys.DEFAULT_WEB_MODULE_KEY,handlerCtx.getInputValue("Web"));
                convertedMap.put(VirtualServerConfigKeys.LOG_FILE_KEY,handlerCtx.getInputValue("LogFile"));
                convertedMap.put(VirtualServerConfigKeys.STATE_KEY,handlerCtx.getInputValue("StateOption"));
                //System.out.println("::::::::::::::::::: in GUI:  createVirtualServerConfig: with Map = " + convertedMap);
                VirtualServerConfig server = config.getHTTPServiceConfig().createVirtualServerConfig(
                        (String)handlerCtx.getInputValue("Name"), ((String)handlerCtx.getInputValue("Hosts")),  convertedMap);
                                
            }
            Map<String,VirtualServerConfig>vservers = config.getHTTPServiceConfig().getVirtualServerConfigMap();
            VirtualServerConfig vs = (VirtualServerConfig)vservers.get((String)handlerCtx.getInputValue("Name"));
            AMXUtil.updateProperties(vs, newProps, vsSkipPropsList);
            
            vs.setHosts(((String)handlerCtx.getInputValue("Hosts")));
            vs.setState(((String)handlerCtx.getInputValue("StateOption")));
            vs.setHTTPListeners(((String)handlerCtx.getInputValue("Http")));
            vs.setDefaultWebModule(((String)handlerCtx.getInputValue("Web")));
            vs.setLogFile(((String)handlerCtx.getInputValue("LogFile")));
            
            AMXUtil.setPropertyValue(vs, "accesslog", (String)handlerCtx.getInputValue("accesslog"));
            AMXUtil.setPropertyValue(vs, "accessLogBufferSize", (String)handlerCtx.getInputValue("accessLogBufferSize"));
            AMXUtil.setPropertyValue(vs, "accessLogWriteInterval", (String)handlerCtx.getInputValue("accessLogWriteInterval"));

            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
    }   

    /**
     *	<p> This handler takes in selected rows, and removes selected config
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="deleteConfig",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="Type", type=String.class, required=true)}
    )
    public static void deleteConfig(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
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
                    //TODO-V3
                    //JMXUtil.invoke("com.sun.appserv:category=config,type=configs", "deleteJmsHost", params, types);

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
                    //TODO-V3
                    //JMXUtil.invoke("com.sun.appserv:type=resources,category=config", "deletePhysicalDestination", params, types);
                }
                
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }  
    
  /**
     *	<p> This handler returns the list of web modules including both embedded
     *  ones in ear and web app.
     *	@param	handlerCtx	The HandlerContext.
     */
    //TODO-V3,  will need to add embedded war module in deployed EAR as well.
    /*
    @Handler(id="getAllWebModules",
       output={
        @HandlerOutput(name="modules", type=List.class)})

        public static void getAllWebModules(HandlerContext handlerCtx) {
        
        Map<String,WebModuleConfig> webs = AMXRoot.getInstance().getApplicationsConfig().getWebModuleConfigMap();
        List result = new ArrayList();
        result.add("");
        for(String nm : webs.keySet()){
            result.add(nm);
        }
        
        Map<String,J2EEApplicationConfig> ears = AMXRoot.getInstance().getApplicationsConfig().getJ2EEApplicationConfigMap();
        
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
     * 
     */
    
     /**
     *	<p> This handler returns the list of web modules including both embedded
     *  ones in ear and web app.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getAllWebModules",
       output={
        @HandlerOutput(name="modules", type=List.class)})

        public static void getAllWebModules(HandlerContext handlerCtx) {
        
        Map<String,ApplicationConfig> webs = AMXRoot.getInstance().getApplicationsConfig().getApplicationConfigMap();
        List result = new ArrayList();
        result.add("");
        for(String nm : webs.keySet()){
            result.add(nm);
        }
        handlerCtx.setOutputValue("modules", result);
    }
      
      
    
    
    
     private static List vsSkipPropsList = new ArrayList();

     static {
        vsSkipPropsList.add("accessLogBufferSize");
        vsSkipPropsList.add("accessLogWriteInterval");
        vsSkipPropsList.add("accessLoggingEnabled");
    
    }


}
