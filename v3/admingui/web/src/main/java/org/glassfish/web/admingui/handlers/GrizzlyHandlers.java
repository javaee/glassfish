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
 * HttpServiceHandlers.java
 *
 *
 */
package org.glassfish.web.admingui.handlers;


import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  


import org.glassfish.admingui.common.util.AMXRoot; 
import org.glassfish.admingui.common.util.GuiUtil;

import com.sun.appserv.management.config.ConfigConfig; 
import com.sun.appserv.management.config.ConfigElement;
import com.sun.appserv.management.config.HTTPListenerConfig;
import java.util.Iterator;

/**
 *
 * @author Anissa Lam
 */
public class GrizzlyHandlers {

    
    @Handler(id="getNetworkListeners",
        input={
            @HandlerInput(name="configName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getNetworkListeners(HandlerContext handlerCtx){
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("configName")));
        List result = new ArrayList();
        try{
            //TODO : need to get from backend.  Just mock up now.
            HashMap oneRow = new HashMap();
            oneRow.put("name", "http-listener-1");
            oneRow.put("protocol", "http-listener-1");
            oneRow.put("selected", false);
            oneRow.put("port", "8080");
            oneRow.put("threadPool", "http-thread-pool");
            oneRow.put("enabled", "true");
            String enable="true";
            String enableURL= (enable.equals("true"))? "/resource/images/enabled.png" : "/resource/images/disabled.png";
            oneRow.put("enableURL", enableURL);
            result.add(oneRow);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
    }

    @Handler(id="getprotocols",
        input={
            @HandlerInput(name="configName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getprotocols(HandlerContext handlerCtx){
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("configName")));
        List result = new ArrayList();
        try{
            //TODO : need to get from backend.  Just mock up now.
            HashMap oneRow = new HashMap();
            oneRow.put("selected", false);
            oneRow.put("name", "http-listener-1");
            oneRow.put("securityEnabled", "false");
            result.add(oneRow);
            oneRow = new HashMap();
            oneRow.put("name", "http-listener-2");
            oneRow.put("selected", false);
            oneRow.put("securityEnabled", "true");
            result.add(oneRow);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
    }



    @Handler(id="getThreadPools",
        input={
            @HandlerInput(name="configName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getThreadPools(HandlerContext handlerCtx){
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("configName")));
        List result = new ArrayList();
        try{
            //TODO : need to get from backend.  Just mock up now.
            HashMap oneRow = new HashMap();
            oneRow.put("selected", false);
            oneRow.put("name", "http-thread-pool");
            oneRow.put("maxSize", "20");
            oneRow.put("minSize", "2");
            result.add(oneRow);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
    }


     @Handler(id="deleteGrizzlyElement",
        input={
            @HandlerInput(name="configName", type=String.class, required=true),
            @HandlerInput(name="type", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class, required=true)}
     )
    public static void deleteGrizzlyElement(HandlerContext handlerCtx){
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("configName")));
    }




    @Handler(id="getTransports",
        input={
            @HandlerInput(name="configName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getTransports(HandlerContext handlerCtx){
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("configName")));
        List result = new ArrayList();
        try{
            //TODO : need to get from backend.  Just mock up now.
            HashMap oneRow = new HashMap();
            oneRow.put("selected", false);
            oneRow.put("name", "tcp");
            oneRow.put("byteBuffer", "HEAP");
            oneRow.put("classname", "");
            result.add(oneRow);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
    }


    @Handler(id="getNetworkListenerAttr",
    input={
        @HandlerInput(name="configName",   type=String.class, required=true) },
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})
        
        public static void getNetworkListenerAttr(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("configName");
            Map valueMap = new HashMap();

            valueMap.put("address", "0.0.0.0");
            valueMap.put("enabled","true");
            valueMap.put("port","");
            valueMap.put("protocol","http-listener-1");
            valueMap.put("threadPool","http-thread-pool-l");
            valueMap.put("transport","tcp");

            handlerCtx.setOutputValue("valueMap", valueMap);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    @Handler(id="getNetworkListenerDefault",
    input={
        @HandlerInput(name="configName",   type=String.class, required=true) },
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})
        
        public static void getNetworkListenerDefault(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("configName");
            Map valueMap = new HashMap();

            valueMap.put("address", "0.0.0.0");
            valueMap.put("enabled","true");
            valueMap.put("port","");
            valueMap.put("protocol","http-listener-1");
            valueMap.put("threadPool","http-thread-pool-l");
            valueMap.put("transport","tcp");

            handlerCtx.setOutputValue("valueMap", valueMap);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }




    @Handler(id="getThreadPoolAttr",
    input={
        @HandlerInput(name="configName",   type=String.class, required=true) },
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

        public static void getThreadPoolAttr(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("configName");
            Map valueMap = new HashMap();

            valueMap.put("timeout","120");
            valueMap.put("maxQueue","-1");
            valueMap.put("maxThread","200");
            valueMap.put("minThread","0");

            handlerCtx.setOutputValue("valueMap", valueMap);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id="getThreadPoolDefault",
    input={
        @HandlerInput(name="configName",   type=String.class, required=true) },
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

        public static void getThreadPoolDefault(HandlerContext handlerCtx) {
        try{
            String configName = (String) handlerCtx.getInputValue("configName");
            Map valueMap = new HashMap();

            valueMap.put("timeout","120");
            valueMap.put("maxQueue","-1");
            valueMap.put("maxThread","200");
            valueMap.put("minThread","0");

            handlerCtx.setOutputValue("valueMap", valueMap);
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
     *  <p> Input value: "newProps"          -- Type: <code>java.util.Map</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
	/*
    @Handler(id="saveHttpListenerValues",
    input={
        @HandlerInput(name="ConfigName",        type=String.class, required=true),
        @HandlerInput(name="HttpName",          type=String.class, required=true),
        @HandlerInput(name="Edit",              type=Boolean.class, required=true),
        @HandlerInput(name="NetwkAddr",         type=String.class, required=true),
        @HandlerInput(name="ListenerPort",      type=String.class, required=true),
        @HandlerInput(name="DefaultVirtServer", type=String.class, required=true),
        @HandlerInput(name="ServerName",        type=String.class, required=true),
        @HandlerInput(name="Listener",          type=String.class),
        @HandlerInput(name="security",          type=String.class),
        @HandlerInput(name="RedirectPort",      type=String.class),
        @HandlerInput(name="Acceptor",          type=String.class),
        @HandlerInput(name="PoweredBy",         type=String.class),
        @HandlerInput(name="Blocking",          type=String.class),
        @HandlerInput(name="newProps",          type=Map.class)})
        
        public static void saveHttpListenerValues(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        String httpListenerName = (String) handlerCtx.getInputValue("HttpName");
        String listPort = (String)handlerCtx.getInputValue("ListenerPort");
        String address = (String)handlerCtx.getInputValue("NetwkAddr");
        String virtualServer = (String)handlerCtx.getInputValue("DefaultVirtServer");
        String serverName = (String)handlerCtx.getInputValue("ServerName");
        ConfigConfig config = AMXRoot.getInstance().getConfig(configName);       
        try{
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            if(!edit){
                Map httpPropsMap = new HashMap();
                httpPropsMap.put("httpName", httpListenerName);
                httpPropsMap.put("address", address);
                httpPropsMap.put("port", listPort);
                httpPropsMap.put("virtualServer", virtualServer);
                httpPropsMap.put("serverName", serverName);
                httpPropsMap.put("options", (Map)handlerCtx.getInputValue("newProps"));
                httpPropsMap.put("enabled", (String)handlerCtx.getInputValue("Listener"));
                httpPropsMap.put("securityEnabled", (String)handlerCtx.getInputValue("security"));
                httpPropsMap.put("redirectPort", (String)handlerCtx.getInputValue("RedirectPort"));
                httpPropsMap.put("acceptor-threads", (String)handlerCtx.getInputValue("Acceptor"));
                httpPropsMap.put("xpowered-by", (String)handlerCtx.getInputValue("PoweredBy")); 
                httpPropsMap.put("blocking-enabled", (String)handlerCtx.getInputValue("Blocking")); 
                handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("httpProps", httpPropsMap);
                //the actual creation is in step 2 of the wizard.
            } else {
                HTTPListenerConfig httpListConfig = config.getHTTPServiceConfig().getHTTPListenerConfigMap().get(httpListenerName);
                String previousVSName = httpListConfig.getDefaultVirtualServer();
                httpListConfig.setAddress(address);
                httpListConfig.setPort((String)handlerCtx.getInputValue("ListenerPort"));
                httpListConfig.setDefaultVirtualServer(virtualServer);
                httpListConfig.setServerName(serverName);
                httpListConfig.setEnabled(""+ handlerCtx.getInputValue("Listener"));
                httpListConfig.setSecurityEnabled((String)handlerCtx.getInputValue("security"));
                httpListConfig.setRedirectPort((String)handlerCtx.getInputValue("RedirectPort"));
                httpListConfig.setAcceptorThreads((String)handlerCtx.getInputValue("Acceptor"));
                httpListConfig.setXpoweredBy((String)handlerCtx.getInputValue("PoweredBy"));
                httpListConfig.setBlockingEnabled((String)handlerCtx.getInputValue("Blocking"));
                AMXUtil.updateProperties( httpListConfig, (Map)handlerCtx.getInputValue("newProps"));
                
                //refer to issue #2920
                if (httpListenerName.equals(ADMIN_LISTENER)){
                    if (Boolean.valueOf(httpListConfig.getSecurityEnabled())){
                        if (httpListConfig.getPropertyConfigMap().get(PROXIED_PROTOCOLS) != null)
                            httpListConfig.getPropertyConfigMap().get(PROXIED_PROTOCOLS).setValue(PROXIED_PROTOCOLS_VALUE);
                         else
                             httpListConfig.createPropertyConfig(PROXIED_PROTOCOLS, PROXIED_PROTOCOLS_VALUE);
                    }else{
                        if (httpListConfig.getPropertyConfigMap().get(PROXIED_PROTOCOLS) != null)
                            httpListConfig.removePropertyConfig(PROXIED_PROTOCOLS);
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
                        tmp= (tmp.equals("") )? hlArray[i] : tmp+","+hlArray[i];
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
    */
    
    /**
     *	<p> This handler returns the values for list of thread pools in 
     *      ORB Page </p>
     *  <p> Input  value: "ConfigName               -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "DefaultVirtualServers"   -- Type: <code>SelectItem[].class 
     *      SelectItem[] (castable to Option[])</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    /*
    @Handler(id="getDefaultVirtualServers",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)},
    output={
        @HandlerOutput(name="DefaultVirtualServers",  type=java.util.List.class)})
        
        public static void getDefaultVirtualServers(HandlerContext handlerCtx) {
            String configName = (String) handlerCtx.getInputValue("ConfigName");
            ConfigConfig config = AMXRoot.getInstance().getConfig(configName);
            Iterator<String> iter = config.getHTTPServiceConfig().getVirtualServerConfigMap().keySet().iterator();
            List options = new ArrayList();
            options.add("");
            while(iter.hasNext()){
                    options.add( iter.next());
                }

            handlerCtx.setOutputValue("DefaultVirtualServers", options);
        }
     */

    
    //mbean Attribute Name
    private static List httpServiceSkipPropsList = new ArrayList();
    
    static {
        httpServiceSkipPropsList.add("accessLogBufferSize");
        httpServiceSkipPropsList.add("accessLogWriteInterval");
        httpServiceSkipPropsList.add("accessLoggingEnabled");
    }
}
