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

package org.glassfish.web.admingui.handlers;


import java.util.Map;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.management.Attribute;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.intf.config.VirtualServer;
import org.glassfish.admin.amx.intf.config.grizzly.Http;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListener;
import org.glassfish.admin.amx.intf.config.grizzly.Protocol;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;

/**
 *
 * @author Anissa Lam
 */
public class WebHandlers {


    @Handler(id="getAllHttpPorts",
        input={
            @HandlerInput(name="configName", type=String.class, required=true)},
        output={
    @HandlerOutput(name="ports", type=String.class)}
     )
    public static void getAllHttpPorts(HandlerContext handlerCtx){
        String configName = (String) handlerCtx.getInputValue("configName");
        AMXProxy listeners = V3AMX.getInstance().getConfig(configName).getNetworkConfig().child("network-listeners");
        Map<String, AMXProxy> networkListeners = listeners.childrenMap("network-listener");
        StringBuffer sb = new StringBuffer();
        for(AMXProxy oneListener:  networkListeners.values()){
            sb.append(oneListener.attributesMap().get("Port"));
            sb.append(" ,");
        }
        String ports = (sb.length() == 0) ? "" : sb.substring(0, sb.length()-2);
        handlerCtx.setOutputValue("ports", ports);
    }

    @Handler(id="createNetworkListener",
        input={
            @HandlerInput(name="configName", type=String.class),
            @HandlerInput(name="attrMap", type=Map.class, required=true)})
    public static void createNetworkListener(HandlerContext handlerCtx){
        Map attrMap = (Map) handlerCtx.getInputValue("attrMap");
        String protocolChoice = (String)attrMap.get("protocolChoice");
        String protocolName = "";
        String securityEnabled = attrMap.get("SecurityEnabled") ==null ? "false" : "true";
        // Take care protocol first.
        Map aMap = new HashMap();
        if ("create".equals(protocolChoice)){
            //Setup to create HTTP also
            Map httpAttrs = new HashMap();
            httpAttrs.put("DefaultVirtualServer", attrMap.get("DefaultVirtualServer"));
            aMap.put(Util.deduceType(Http.class), httpAttrs);

            aMap.put("Name",  attrMap.get("newProtocolName"));
            aMap.put("SecurityEnabled",  securityEnabled);
            AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getConfig("server-config").getNetworkConfig().child("protocols");
            AMXProxy pp = amx.createChild("protocol",  aMap);
            protocolName = pp.getName();

        }else{
            protocolName = (String) attrMap.get("existingProtocolName");
            AMXProxy amx = V3AMX.getInstance().getConfig("server-config").getNetworkConfig().child("protocols").childrenMap("protocol").get(protocolName);
            V3AMX.setAttribute(amx.objectName(), new Attribute("SecurityEnabled", securityEnabled ));
        }

        Map nMap = new HashMap();
        putA(nMap,  attrMap, "Name" );
        putA(nMap,  attrMap, "Address");
        putA(nMap,  attrMap, "Port" );
        putA(nMap,  attrMap, "Transport");
        putA(nMap,  attrMap, "ThreadPool");
        putA(nMap,  attrMap, "Enabled" , "false");
        putA(nMap,  attrMap, "JkEnabled", "false");
        nMap.put("Protocol", protocolName);

        AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getConfig("server-config").getNetworkConfig().child("network-listeners");
        amx.createChild("network-listener", nMap);

        //get the virtual server and add this network listener to it.
         Protocol protocol = V3AMX.getInstance().getConfig("server-config").getNetworkConfig().getProtocols().getProtocol().get(protocolName);
         if (protocol.getHttp() != null){
             String vsName = (String) protocol.getHttp().getDefaultVirtualServer();
             changeNetworkListenersInVS(vsName, (String) nMap.get("Name"),  true);
         }
    }


    private static void changeNetworkListenersInVS(String vsName, String listenerName, boolean addFlag){
        //get the virtual server and add this network listener to it.
        if (GuiUtil.isEmpty(vsName) || GuiUtil.isEmpty(listenerName)){
            return;
        }
         VirtualServer vsProxy =  V3AMX.getInstance().getConfig("server-config").getHttpService().getVirtualServer().get(vsName);
         List<String> listeners = GuiUtil.parseStringList(vsProxy.getNetworkListeners(), ",");
         if (addFlag){
             if (! listeners.contains(listenerName)){
                 listeners.add(listenerName);
             }
         }else{
             if (listeners.contains(listenerName)){
                 listeners.remove(listenerName);
             }
         }
         String ll = GuiUtil.listToString(listeners, ",");
         vsProxy.setNetworkListeners(ll);
    }
    

     @Handler(id="findHttpProtocol",
        input={
            @HandlerInput(name="listenerName", type=String.class)},
        output={
            @HandlerOutput(name="httpProtocolName", type=String.class),
            @HandlerOutput(name="sameAsProtocol", type=Boolean.class)}
     )
    public static void findHttpProtocol(HandlerContext handlerCtx){
        String listenerName = (String)handlerCtx.getInputValue("listenerName");
        Map<String, NetworkListener> nls = V3AMX.getInstance().getConfig("server-config").getNetworkConfig().as(NetworkConfig.class).getNetworkListeners().getNetworkListener();
        NetworkListener listener = nls.get(listenerName);
        String http = listener.findHttpProtocol().getName();
        handlerCtx.setOutputValue("httpProtocolName", http);
        handlerCtx.setOutputValue("sameAsProtocol", http.equals(listener.findProtocol().getName()));
     }

     @Handler(id="addNetworkListenerInfo",
        input={
            @HandlerInput(name="protocolListOfRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=List.class)}
     )
    public static void addNetworkListenerInfo(HandlerContext handlerCtx){
         List<Map> listOfMap = (List<Map>)handlerCtx.getInputValue("protocolListOfRows");
         for(Map oneRow : listOfMap){
             String pName = (String) oneRow.get("Name");
             Protocol protocol = V3AMX.getInstance().getConfig("server-config").getNetworkConfig().getProtocols().getProtocol().get(pName);
             List<NetworkListener> listenerList = findNetworkListeners(protocol);
             List nameList = new ArrayList();
             for(NetworkListener one: listenerList){
                 nameList.add(one.getName());
             }
             oneRow.put("listenerList", nameList);
         }
         handlerCtx.setOutputValue("result", listOfMap);
     }


     /*
      * delete selected Network Listener.  If the protocol of this network listener is not used by any other listener,
      * and its name ends in "-protocol"  which is created by GUI, we will delete this protocol as well.
      */
    @Handler(id="deleteNetworkListeners",
        input={
            @HandlerInput(name = "selectedRows", type = List.class, required = true)})
    public static void deleteNetworkListeners(HandlerContext handlerCtx){

        NetworkConfig nConfig = V3AMX.getInstance().getConfig("server-config").getNetworkConfig().as(NetworkConfig.class);
        Map<String, NetworkListener> nls = nConfig.getNetworkListeners().getNetworkListener();
        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
        try {
            for (Map oneRow : selectedRows) {
                String listenerName = (String) oneRow.get("Name");
                NetworkListener listener = nls.get(listenerName);
                Protocol protocol = listener.findProtocol();
                List listenerList = findNetworkListeners(protocol);
                nConfig.getNetworkListeners().removeChild("network-listener", listenerName);

                //remove the network listener from the VS's attr list.
                if (protocol.getHttp()!= null){
                    changeNetworkListenersInVS(protocol.getHttp().getDefaultVirtualServer(), listenerName, false);
                }

                if (listenerList.size() == 1){
                    //this protocol is used only by this listener, test if this is also created by GUI.
                    if (protocol.getName().equals(listenerName+GuiUtil.getMessage("org.glassfish.web.admingui.Strings", "grizzly.protocolExtension"))){
                        nConfig.getProtocols().removeChild("protocol", protocol.getName());
                    }
                }
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    @Handler(id="updateNetworkListenerInVS",
    input={
        @HandlerInput(name = "previousVSName", type = String.class, required = true),
        @HandlerInput(name = "protocolName", type = String.class, required = true)})
    public static void updateNetworkListenerInVS(HandlerContext handlerCtx){

        String previousVSName = (String) handlerCtx.getInputValue("previousVSName");
        String protocolName = (String) handlerCtx.getInputValue("protocolName");
        Protocol protocol = V3AMX.getInstance().getConfig("server-config").getNetworkConfig().as(NetworkConfig.class).getProtocols().getProtocol().get(protocolName);
        if (protocol.getHttp() == null){
            // shouldn't get to this case.
            return;
        }
        String newVSName = protocol.getHttp().getDefaultVirtualServer();
        if (newVSName.equals(previousVSName)){
            //the VS is not changed. no need to modify.
            return;
        }

        List<NetworkListener> listenerList = findNetworkListeners(protocol);
        for(NetworkListener one: listenerList){
            changeNetworkListenersInVS(previousVSName, one.getName(), false);
            changeNetworkListenersInVS(newVSName, one.getName(), true);
        }
    }

    /*
     * Delete selected protocol.  Any listener that is using this protocol will also be deleted.
     */
    @Handler(id="deleteProtocol",
        input={
            @HandlerInput(name = "selectedRows", type = List.class, required = true)})
    public static void deleteProtocol(HandlerContext handlerCtx){

        NetworkConfig nConfig = V3AMX.getInstance().getConfig("server-config").getNetworkConfig().as(NetworkConfig.class);
        Map<String, Protocol> ps = nConfig.getProtocols().getProtocol();
        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
        try {
            for (Map oneRow : selectedRows) {
                String protocolName = (String) oneRow.get("Name");
                Protocol protocol = ps.get(protocolName);
                List<NetworkListener> listenerList = findNetworkListeners(protocol);
                for(NetworkListener one: listenerList){
                    if (protocol.getHttp()!= null){
                        changeNetworkListenersInVS(protocol.getHttp().getDefaultVirtualServer(), one.getName(), false);
                    }
                    nConfig.getNetworkListeners().removeChild("network-listener", one.getName());
                }
                nConfig.getProtocols().removeChild("protocol", protocolName);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    private static List<NetworkListener> findNetworkListeners(Protocol protocol){
        List result = new ArrayList();
        Map<String, NetworkListener> nMap = V3AMX.getInstance().getConfig("server-config").getNetworkConfig().getNetworkListeners().getNetworkListener();
        String nm = protocol.getName();
        for(NetworkListener one : nMap.values()){
            if (one.findProtocol().getName().equals(nm)){
                result.add(one);
            }
        }
        return result;
    }

    private static void putA(Map nMap, Map attrMap, String key){
        String val = (String) attrMap.get(key);
        if (! GuiUtil.isEmpty(val)){
            nMap.put(key, val);
        }
    }

    private static void putA(Map nMap, Map attrMap, String key, String defaultValue){
        String val = (String) attrMap.get(key);
        if (! GuiUtil.isEmpty(val)){
            nMap.put(key, val);
        }else{
            nMap.put(key, defaultValue);
        }
    }
}
