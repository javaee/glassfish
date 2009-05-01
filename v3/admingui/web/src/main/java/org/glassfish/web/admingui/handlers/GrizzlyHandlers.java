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


import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.intf.config.ConfigConfig;
import org.glassfish.admin.amx.intf.config.ConfigsConfig;
import org.glassfish.admin.amx.intf.config.ThreadPoolConfig;
import org.glassfish.admin.amx.intf.config.ThreadPoolsConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListener;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListeners;
import org.glassfish.admin.amx.intf.config.grizzly.Protocol;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.GuiUtil;


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

        ConfigConfig config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
        Map<String, NetworkListener> nls = config.getNetworkConfig().as(NetworkConfig.class).getNetworkListeners().getNetworkListener();
        List result = new ArrayList();
        for(NetworkListener nl : nls.values()){
            try{
                Map<String, Object> attrs = nl.attributesMap();
                HashMap oneRow = new HashMap();
                oneRow.put("Name", getA(attrs, "Name"));
                oneRow.put("Protocol", getA(attrs, "Protocol"));
                oneRow.put("selected", false);
                oneRow.put("Port", getA(attrs, "Port"));
                oneRow.put("ThreadPool", getA(attrs, "ThreadPool"));
                String enabled = getA(attrs, "Enabled");
                oneRow.put("enabled", enabled);
                String enableURL= (enabled.equals("true"))? "/resource/images/enabled.png" : "/resource/images/disabled.png";
                oneRow.put("enableURL", enableURL);
                result.add(oneRow);
            }catch(Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
        }
        handlerCtx.setOutputValue("result", result);
    }

    private static String getA(Map<String, Object> attrs,  String key){
        String res = (String) attrs.get(key);
        if (res == null) res = "";
        return res;
    }

    @Handler(id="getprotocols",
        input={
            @HandlerInput(name="configName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getprotocols(HandlerContext handlerCtx){

        ConfigConfig config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
        Map<String, Protocol> pMap = config.getNetworkConfig().as(NetworkConfig.class).getProtocols().getProtocol();
        List result = new ArrayList();
        for(Protocol protocol : pMap.values()){
            try{
                Map<String, Object> attrs = protocol.attributesMap();
                HashMap oneRow = new HashMap();
                oneRow.put("Name", getA(attrs, "Name"));
                oneRow.put("SecurityEnabled", getA(attrs, "SecurityEnabled"));
                oneRow.put("selected", false);
                result.add(oneRow);
            }catch(Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
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
        ConfigConfig config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
        Map<String, ThreadPoolConfig> pMap = config.getThreadPools().getThreadPool();
        List result = new ArrayList();

        for(ThreadPoolConfig tpc : pMap.values()){
            try{
                Map<String, Object> attrs = tpc.attributesMap();
                HashMap oneRow = new HashMap();
                oneRow.put("Name", getA(attrs, "Name"));
                oneRow.put("MinThreadPoolSize", getA(attrs, "MinThreadPoolSize"));
                oneRow.put("MaxThreadPoolSize", getA(attrs, "MaxThreadPoolSize"));
                oneRow.put("MaxQueueSize", getA(attrs, "MaxQueueSize"));
                oneRow.put("IdleThreadTimeout", getA(attrs, "IdleThreadTimeout"));
                oneRow.put("selected", false);
                result.add(oneRow);
            }catch(Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
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
         ConfigConfig config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
         String type = (String) handlerCtx.getInputValue("type");
         AMXConfigProxy amxP = null;
         if (type.equals("thread-pool")){
             amxP = config.getThreadPools();
         }else
         if (type.equals("network-listener")){
             amxP = config.getNetworkConfig().as(NetworkConfig.class).getNetworkListeners();
         }else
         if (type.equals("protocol")){
             amxP = config.getNetworkConfig().as(NetworkConfig.class).getProtocols();
         }
         if (amxP == null){
             GuiUtil.handleError(handlerCtx, "GUI internal error, Not such Type: " + type);
             return;
         }

         List obj = (List) handlerCtx.getInputValue("selectedRows");
         List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String Name = (String)oneRow.get("Name");
                amxP.removeChild(type, Name);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    @Handler(id="getNetworkListenerAttr",
    input={
        @HandlerInput(name="configName",   type=String.class, required=true),
        @HandlerInput(name="name",   type=String.class),
        @HandlerInput(name="fromDefault",   type=String.class)},
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})
        
        public static void getNetworkListenerAttr(HandlerContext handlerCtx) {
        try{
            String fromDefault = (String) handlerCtx.getInputValue("fromDefault");
            Map attrs = null;
            ConfigConfig config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
            NetworkListeners nls = config.getNetworkConfig().as(NetworkConfig.class).getNetworkListeners();
            if ( GuiUtil.isEmpty(fromDefault) || fromDefault.equals("false")){
                String name = (String) handlerCtx.getInputValue("name");
                NetworkListener nl = nls.getNetworkListener().get(name);
                attrs = nl.attributesMap();
            } else {
                attrs = nls.getDefaultValues("network-listener", true);
            }
            handlerCtx.setOutputValue("valueMap", attrs);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    


    @Handler(id="getThreadPoolAttr",
    input={
        @HandlerInput(name="configName",   type=String.class, required=true),
        @HandlerInput(name="name",   type=String.class),
        @HandlerInput(name="fromDefault",   type=String.class)},
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

        public static void getThreadPoolAttr(HandlerContext handlerCtx) {
        try{
            String fromDefault = (String) handlerCtx.getInputValue("fromDefault");
            Map attrs = null;
            ConfigConfig config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
            ThreadPoolsConfig tps = config.getThreadPools();
            if ( GuiUtil.isEmpty(fromDefault) || fromDefault.equals("false")){
                String name = (String) handlerCtx.getInputValue("name");
                ThreadPoolConfig tp = tps.getThreadPool().get(name);
                attrs = tp.attributesMap();
            } else {
                attrs = tps.getDefaultValues("thread-pool", true);
            }
            handlerCtx.setOutputValue("valueMap", attrs);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id="saveThreadPoolAttr",
    input={
        @HandlerInput(name="configName",   type=String.class, required=true),
        @HandlerInput(name="attrs",   type=Map.class),
        @HandlerInput(name="Edit",   type=Boolean.class)})
        public static void saveThreadPoolAttr(HandlerContext handlerCtx) {
        try{
            Map attrs = (Map) handlerCtx.getInputValue("attrs");
            Boolean edit = (Boolean) handlerCtx.getInputValue("Edit");
            ConfigConfig config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
            ThreadPoolsConfig tps = config.getThreadPools();
            if (edit){
                String name = (String) attrs.get("Name");
                ThreadPoolConfig tpc = tps.getThreadPool().get(name);
                tpc.setClassname(getA(attrs, "Classname"));
                tpc.setIdleThreadTimeoutSeconds(getA(attrs, "IdleThreadTimeout"));
                tpc.setMaxQueueSize(getA(attrs, "MaxQueueSize"));
                tpc.setMinThreadPoolSize(getA(attrs, "MinThreadPoolSize"));
                tpc.setMaxThreadPoolSize(getA(attrs, "MaxThreadPoolSize"));
            }else{
                tps.createChild("thread-pool", attrs);
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    
    //mbean Attribute Name
    private static List httpServiceSkipPropsList = new ArrayList();
    
    static {
        httpServiceSkipPropsList.add("accessLogBufferSize");
        httpServiceSkipPropsList.add("accessLogWriteInterval");
        httpServiceSkipPropsList.add("accessLoggingEnabled");
    }
}
