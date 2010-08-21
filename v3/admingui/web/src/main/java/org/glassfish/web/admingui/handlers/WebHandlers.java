/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import java.util.HashMap;
import java.util.List;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListener;
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

    @Handler(id="changeNetworkListenersInVS",
    input={
        @HandlerInput(name = "vsAttrs", type = Map.class, required = true),
        @HandlerInput(name = "listenerName", type = String.class, required = true),
        @HandlerInput(name = "addFlag", type = Boolean.class, required = true)},
        output={
            @HandlerOutput(name="result", type=Map.class)})
    public static void changeNetworkListenersInVS(HandlerContext handlerCtx){
        //get the virtual server and add this network listener to it.
        Map vsAttrs = (HashMap) handlerCtx.getInputValue("vsAttrs");
        String listenerName = (String) handlerCtx.getInputValue("listenerName");
        Boolean addFlag = (Boolean) handlerCtx.getInputValue("addFlag");
        String nwListeners = (String)vsAttrs.get("networkListeners");
        List<String> listeners = GuiUtil.parseStringList(nwListeners, ",");
        if (addFlag.equals(Boolean.TRUE)){
            if (! listeners.contains(listenerName)){
                listeners.add(listenerName);
            }
        }else {
            if (listeners.contains(listenerName)){
                listeners.remove(listenerName);
            }
        }
        String ll = GuiUtil.listToString(listeners, ",");
        vsAttrs.put("networkListeners", ll);
        handlerCtx.setOutputValue("result", vsAttrs);
    }
}
