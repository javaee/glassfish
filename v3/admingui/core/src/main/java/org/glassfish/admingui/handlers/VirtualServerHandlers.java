/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.handlers;


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


import org.glassfish.admingui.util.AMXRoot; 
import org.glassfish.admingui.util.GuiUtil;

import com.sun.appserv.management.config.ConfigConfig; 
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.HTTPAccessLogConfig;
import com.sun.appserv.management.config.ConfigElement;
import com.sun.appserv.management.config.PropertiesAccess;


/**
 *
 * @author anilam
 */
public class VirtualServerHandlers {
    
    
        
   /**
     *	<p> This handler returns the list of specified config elements for populating the table.
     *  <p> Input  value: "type" -- Type: <code> java.lang.String</code></p>
     *	@param	context	The HandlerContext.
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
                    oneRow.put("selected", (hasOrig)? ConnectorsHandlers.isSelected(name, selectedList): false);
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
        
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                //TODO-V3 TP2 cannot get default attributes
                //getDefaultVirtualServerAttributes(handlerCtx);
                return;
            }
            Map<String,VirtualServerConfig>vservers = config.getHTTPServiceConfig().getVirtualServerConfigMap();
            VirtualServerConfig vs = (VirtualServerConfig)vservers.get((String)handlerCtx.getInputValue("Name"));
            
            handlerCtx.setOutputValue("Hosts", vs.getHosts());
            handlerCtx.setOutputValue("StateOption", vs.getState());
            handlerCtx.setOutputValue("Http", vs.getHTTPListeners());
            handlerCtx.setOutputValue("Web", "AMX Exception"); //vs.getDefaultWebModule());
            handlerCtx.setOutputValue("LogFile", vs.getLogFile());
            
            Map aMap = AMXRoot.getInstance().getNonSkipPropertiesMap(vs, vsSkipPropsList);
            handlerCtx.setOutputValue("Properties", aMap);
            Map origProps = vs.getProperties();
            handlerCtx.setOutputValue("accessLogBufferSize", origProps.get("accessLogBufferSize"));
            handlerCtx.setOutputValue("accessLogWriteInterval", origProps.get("accessLogWriteInterval"));
            handlerCtx.setOutputValue("accesslog", origProps.get("accesslog"));
            handlerCtx.setOutputValue("docroot", "AMX Exception"); // origProps.get("docroot"));
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
        Map defaultMap = AMXRoot.getInstance().getDomainConfig().getDefaultAttributeValues(VirtualServerConfig.J2EE_TYPE);         
        handlerCtx.setOutputValue("Hosts", defaultMap.get("hosts"));
        handlerCtx.setOutputValue("StateOption", defaultMap.get("state"));
        handlerCtx.setOutputValue("Http", defaultMap.get("http-listeners"));
        handlerCtx.setOutputValue("Web", defaultMap.get("default-web-module"));
        handlerCtx.setOutputValue("LogFile", defaultMap.get("log-file"));
        //handlerCtx.setOutputValue("docroot", defaultMap.get("docroot"));
        handlerCtx.setOutputValue("docroot", "${com.sun.aas.instanceRoot}/docroot");
        Map dMap = AMXRoot.getInstance().getDomainConfig().getDefaultAttributeValues(HTTPAccessLogConfig.J2EE_TYPE);  
        handlerCtx.setOutputValue("accesslog", dMap.get("log-directory"));
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
        
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        Map newProps = (Map)handlerCtx.getInputValue("newProps");
        AMXRoot amxRoot = AMXRoot.getInstance();
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map convertedMap = amxRoot.convertToPropertiesOptionMap(newProps, null);
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
            AMXRoot.getInstance().updateProperties(vs, newProps, vsSkipPropsList);
            
            vs.setHosts(((String)handlerCtx.getInputValue("Hosts")));
            vs.setState(((String)handlerCtx.getInputValue("StateOption")));
            vs.setHTTPListeners(((String)handlerCtx.getInputValue("Http")));
            vs.setDefaultWebModule(((String)handlerCtx.getInputValue("Web")));
            vs.setLogFile(((String)handlerCtx.getInputValue("LogFile")));
            
            amxRoot.changeProperty(vs, "accesslog", (String)handlerCtx.getInputValue("accesslog"));
            amxRoot.changeProperty(vs, "accessLogBufferSize", (String)handlerCtx.getInputValue("accessLogBufferSize"));
            amxRoot.changeProperty(vs, "accessLogWriteInterval", (String)handlerCtx.getInputValue("accessLogWriteInterval"));
            amxRoot.changeProperty(vs, "docroot", (String)handlerCtx.getInputValue("docroot"));
            amxRoot.changeProperty(vs, "sso-enabled", ""+handlerCtx.getInputValue("sso"));
            String accessLoggingFlag = (String)handlerCtx.getInputValue("accessLoggingFlag");
            if (accessLoggingFlag.equals("off"))
                accessLoggingFlag=null;
            amxRoot.changeProperty(vs, "accessLoggingEnabled", accessLoggingFlag);
            
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        
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
     *	@param	context	The HandlerContext.
     */
    //TODO-V3,  will need to add embedded war module in deployed EAR as well.
    /*
    @Handler(id="getAllWebModules",
       output={
        @HandlerOutput(name="modules", type=List.class)})

        public static void getAllWebModules(HandlerContext handlerCtx) {
        
        Map<String,WebModuleConfig> webs = AMXRoot.getInstance().getDomainConfig().getWebModuleConfigMap();
        List result = new ArrayList();
        result.add("");
        for(String nm : webs.keySet()){
            result.add(nm);
        }
        
        Map<String,J2EEApplicationConfig> ears = AMXRoot.getInstance().getDomainConfig().getJ2EEApplicationConfigMap();
        
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
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getAllWebModules",
       output={
        @HandlerOutput(name="modules", type=List.class)})

        public static void getAllWebModules(HandlerContext handlerCtx) {
        
        Map<String,ApplicationConfig> webs = AMXRoot.getInstance().getDomainConfig().getApplicationConfigMap();
        List result = new ArrayList();
        result.add("");
        for(String nm : webs.keySet()){
            result.add(nm);
        }
        handlerCtx.setOutputValue("modules", result);
    }
      
      
    
    static private void putOptionalValue(String value, Map convertedMap, String propName)
    {
       if (GuiUtil.isEmpty(value))
           return;
       convertedMap.put(PropertiesAccess.PROPERTY_PREFIX + propName, value);
    }
    
     private static List vsSkipPropsList = new ArrayList();

     static {
        vsSkipPropsList.add("accesslog");
        vsSkipPropsList.add("docroot");
        vsSkipPropsList.add("sso-enabled");
        vsSkipPropsList.add("sso-enabled");
        vsSkipPropsList.add("accessLogBufferSize");
        vsSkipPropsList.add("accessLogWriteInterval");
        vsSkipPropsList.add("accessLoggingEnabled");
    
    }


}
