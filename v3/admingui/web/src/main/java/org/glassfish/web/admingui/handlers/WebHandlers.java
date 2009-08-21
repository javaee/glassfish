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

import java.util.HashMap;
import javax.management.Attribute;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
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
        putA(nMap,  attrMap, "Enabled");
        putA(nMap,  attrMap, "JkEnabled");
        nMap.put("Protocol", protocolName);

        AMXConfigProxy amx = (AMXConfigProxy) V3AMX.getInstance().getConfig("server-config").getNetworkConfig().child("network-listeners");
        amx.createChild("network-listener", nMap);
    }

    
    private static void putA(Map nMap, Map attrMap, String key){
        String val = (String) attrMap.get(key);
        if (! GuiUtil.isEmpty(val)){
            nMap.put(key, val);
        }
    }


}
