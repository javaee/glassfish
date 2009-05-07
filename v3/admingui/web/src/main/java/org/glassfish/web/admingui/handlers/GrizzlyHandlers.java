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


import javax.management.ObjectName;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.intf.config.Config;
import org.glassfish.admin.amx.intf.config.ThreadPool;
import org.glassfish.admin.amx.intf.config.ThreadPools;
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

        Config config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
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

        Config config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
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
        Config config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
        Map<String, ThreadPool> pMap = config.getThreadPools().getThreadPool();
        List result = new ArrayList();

        for(ThreadPool tpc : pMap.values()){
            try{
                Map<String, Object> attrs = tpc.attributesMap();
                HashMap oneRow = new HashMap();
                oneRow.put("Name", getA(attrs, "Name"));
                oneRow.put("MinThreadPoolSize", getA(attrs, "MinThreadPoolSize"));
                oneRow.put("MaxThreadPoolSize", getA(attrs, "MaxThreadPoolSize"));
                oneRow.put("MaxQueueSize", getA(attrs, "MaxQueueSize"));
                oneRow.put("IdleThreadTimeoutSeconds", getA(attrs, "IdleThreadTimeoutSeconds"));
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
         Config config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
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
            Config config = V3AMX.getServerConfig((String) handlerCtx.getInputValue("configName"));
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

    @Handler(id="getBeanAttrs",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

        public static void getBeanAttrs(HandlerContext handlerCtx) {
        try{
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            AMXProxy  amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            handlerCtx.setOutputValue("valueMap", amx.attributesMap());
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    @Handler(id="getDefaultBeanAttrs",
    input={
        @HandlerInput(name="parentObjectNameStr",   type=String.class, required=true),
        @HandlerInput(name="childType",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="valueMap",        type=Map.class)})

        public static void getDefaultBeanAttrs(HandlerContext handlerCtx) {
        try{
	    
            boolean calltest=false;

            String parentName = (String) handlerCtx.getInputValue("parentObjectNameStr");
            if (calltest){
		AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentName));
		
		String type = "file-cache";
        System.out.println("type = " + type);
        System.out.println("objectName = " + parentName);
		Map mm = amx.getDefaultValues(type, true);
        System.out.println(mm);
	    }
            String childType = (String) handlerCtx.getInputValue("childType");
            AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentName));
            Map valueMap = amx.getDefaultValues(childType, true);
            handlerCtx.setOutputValue("valueMap", valueMap);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    @Handler(id="saveBeanAttributes",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true),
        @HandlerInput(name="attrs",   type=Map.class),
        @HandlerInput(name="skipAttrs",   type=List.class) } )
        public static void saveBeanAttributes(HandlerContext handlerCtx) {
        try{
            Map attrs = (Map) handlerCtx.getInputValue("attrs");
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            List<String> skipAttrs = (List) handlerCtx.getInputValue("skipAttrs");
            if (skipAttrs != null){
                for(String sk : skipAttrs){
                    if (attrs.keySet().contains(sk)){
                        attrs.remove(sk);
                    }
                }
            }
            V3AMX.setAttributes( objectNameStr, attrs);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    @Handler(id="createBean",
    input={
        @HandlerInput(name="parentObjectNameStr",   type=String.class, required=true),
        @HandlerInput(name="childType",   type=String.class, required=true),
        @HandlerInput(name="attrs",   type=Map.class),
        @HandlerInput(name="skipAttrs",   type=List.class)})
        public static void createBean(HandlerContext handlerCtx) {
        try{
            final String childType = (String) handlerCtx.getInputValue("childType");
            Map attrs = (Map) handlerCtx.getInputValue("attrs");
            String parentObjectNameStr = (String) handlerCtx.getInputValue("parentObjectNameStr");
            AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentObjectNameStr));
            List<String> skipAttrs = (List) handlerCtx.getInputValue("skipAttrs");
            if (skipAttrs != null){
                for(String sk : skipAttrs){
                    if (attrs.keySet().contains(sk)){
                        attrs.remove(sk);
                    }
                }
            }
            System.out.println("========createChild========");
            System.out.println(amx.toString());
            System.out.println("childType = " + childType);
            System.out.println(attrs);
            amx.createChild( childType,attrs);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    @Handler(id="createObjectName",
    input={
        @HandlerInput(name="value",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="result",        type=String.class)})

        public static void createObjectName(HandlerContext handlerCtx) {
        try{
            String value = (String) handlerCtx.getInputValue("value");
            String value1 = value.replaceAll("<", "[");
            String result  = value1.replaceAll(">", "]");
            handlerCtx.setOutputValue("result",result);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    @Handler(id="getChildrenByType",
    input={
        @HandlerInput(name="parentObjectNameStr",   type=String.class, required=true),
        @HandlerInput(name="type",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="result",        type=List.class)})

        public static void getChildrenByType(HandlerContext handlerCtx) {
        try{
            String type = (String) handlerCtx.getInputValue("type");
            String parentObjectNameStr = (String) handlerCtx.getInputValue("parentObjectNameStr");
            List result = new ArrayList();
            AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(parentObjectNameStr));
            Map<String, AMXProxy> childrenMap = amx.childrenMap(type);
            result.addAll(childrenMap.keySet());
            handlerCtx.setOutputValue("result",result);
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
