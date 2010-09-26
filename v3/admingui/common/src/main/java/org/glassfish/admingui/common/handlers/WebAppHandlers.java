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
package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.V3AMXUtil;

import javax.management.openmbean.TabularData;
import org.glassfish.admin.amx.intf.config.Property;
import org.glassfish.admin.amx.monitoring.ServerMon;
import org.glassfish.admingui.common.util.DeployUtil;
import org.glassfish.admingui.common.util.RestResponse;



public class WebAppHandlers {

    /** Creates a new instance of ApplicationsHandler */
    public WebAppHandlers() {
    }

    @Handler(id = "getAppclinetLaunchURL",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true),
            @HandlerInput(name = "moduleName", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "url", type = String.class)})
    public static void getAppclinetLaunchURL(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        String moduleName = (String) handlerCtx.getInputValue("moduleName");
        String appClientLaunch = V3AMX.getInstance().getRuntime().getRelativeJWSURI(appName, moduleName);
        if (!GuiUtil.isEmpty(appClientLaunch)){
            String httpLink = V3AMXUtil.getLaunchLink((String)GuiUtil.getSessionValue("serverName"), appName);
            handlerCtx.setOutputValue("url", httpLink+appClientLaunch);
        }
    }



    @Handler(id = "getEndpointInfo",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true),
            @HandlerInput(name = "moduleName", type = String.class, required = true),
            @HandlerInput(name = "subComponentName", type = String.class, required = true),
            @HandlerInput(name = "type", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = Map.class)})
    public static void getEndpointInfo(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        String moduleName = (String) handlerCtx.getInputValue("moduleName");
        String componentName = (String) handlerCtx.getInputValue("subComponentName");
        String type = (String) handlerCtx.getInputValue("type");
        Map result = new HashMap();
        TabularData endpointMap = getEndpointMap ( appName,   moduleName,  componentName,  type);
        if (endpointMap.isEmpty()){
            handlerCtx.setOutputValue("result", result);
        }

        result.put("appName", (String)endpointMap.get(new Object[] {"appName"}).get("value"));
        result.put("endpointName", (String)endpointMap.get(new Object[] {"endpointName"}).get("value"));
        result.put("namespace", (String)endpointMap.get(new Object[] {"implClass"}).get("value"));
        result.put("serviceName", (String)endpointMap.get(new Object[] {"namespace"}).get("value"));
        result.put("portName", (String)endpointMap.get(new Object[] {"portName"}).get("value"));
        result.put("implClass", (String)endpointMap.get(new Object[] {"implClass"}).get("value"));
        result.put("address", (String)endpointMap.get(new Object[] {"address"}).get("value"));
        result.put("wsdl", (String)endpointMap.get(new Object[] {"wsdl"}).get("value"));
        result.put("tester", (String)endpointMap.get(new Object[] {"tester"}).get("value"));
        result.put("implType", (String)endpointMap.get(new Object[] {"implType"}).get("value"));
        result.put("deploymentType", (String)endpointMap.get(new Object[] {"deploymentType"}).get("value"));
        String launchLink = V3AMXUtil.getLaunchLink((String)GuiUtil.getSessionValue("serverName"), appName);
        if (GuiUtil.isEmpty(launchLink)){
            result.put("disableTester", "true");
            result.put("hasWsdlLink", false);
        }else{
            result.put("disableTester", "false");
            result.put("hasWsdlLink", true);
            result.put("testLink", launchLink+result.get("tester"));
            result.put("wsdlLink", launchLink+result.get("wsdl"));
        }
        GuiUtil.getLogger().fine("Endpoint Info for " + appName + "#" + componentName  +" : " + result);
        handlerCtx.setOutputValue("result", result);
    }

    

     private static TabularData getEndpointMap (String appName,  String moduleName, String componentName, String type){
        ServerMon serverMon = V3AMX.getInstance().getDomainRoot().getMonitoringRoot().getServerMon().get("server").as(ServerMon.class);
        AMXConfigProxy webDeployment = serverMon.childrenMap("web-service-mon").get("webservices-deployment").as(AMXConfigProxy.class);
        String[] params = new String[]{ appName, moduleName, componentName};
        String[] sig = new String[]{"java.lang.String", "java.lang.String", "java.lang.String"};
        TabularData endpointMap = null;
        if (type.equalsIgnoreCase("Servlet")){
            endpointMap = (TabularData) webDeployment.invokeOp("getServlet109Endpoint", params, sig);
        }else{
            endpointMap = (TabularData) webDeployment.invokeOp("getEjb109Endpoint", params, sig);
        }
        return endpointMap;
     }



    
    private static String getA(Map<String, Property> attrs, String key) {
        Property val = attrs.get(key);
        return (val == null) ? "" :  val.getValue();
    }

   //This is called when user change the default web module of a VS.
   //Need to ensure this VS is in the application-ref virtual server list. If not add it and restart the app for
   //change to take into effect.  refer to issue#8671
   @Handler(id = "EnsureDefaultWebModule",
        input = {
            @HandlerInput(name = "endpoint", type = String.class, required = true),
            @HandlerInput(name = "vsName", type = String.class, required = true),
            @HandlerInput(name = "instanceList", type=List.class, required=true)
        })
    public static void EnsureDefaultWebModule(HandlerContext handlerCtx) throws Exception {
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        String vsName = (String) handlerCtx.getInputValue("vsName");
        List instanceList = (List) handlerCtx.getInputValue("instanceList");
        Map vsAttrs = RestApiHandlers.getAttributesMap(endpoint+"/" + vsName);
        String webModule= (String) vsAttrs.get("DefaultWebModule");
        if (GuiUtil.isEmpty(webModule))
            return;
        String appName = webModule;
        int index = webModule.indexOf("#");
        if (index != -1){
            appName=webModule.substring(0, index);
        }
        String serverEndPoint = GuiUtil.getSessionValue("REST_URL") + "/servers/server/";
        for (Object serverName : instanceList) {
            String apprefEndpoint = serverEndPoint + serverName + "/application-ref/" + appName;
            Map apprefAttrs = RestApiHandlers.getAttributesMap(apprefEndpoint+"/" + vsName);
            String vsStr = (String) apprefAttrs.get("VirtualServers");
            List vsList = GuiUtil.parseStringList(vsStr, ",");
            if (vsList.contains(vsName)){
                continue;   //the default web module app is already deployed to this vs, no action needed
            }
            //Add to the vs list of this application-ref, then restart the app.
            vsStr=vsStr+","+vsName;
            apprefAttrs.put("VirtualServers", vsStr);
            RestResponse response = RestApiHandlers.sendUpdateRequest(apprefEndpoint, apprefAttrs, null, null, null);
            if (!response.isSuccess()) {
                GuiUtil.getLogger().severe("Update virtual server failed.  parent=" + apprefEndpoint + "; attrsMap =" + apprefAttrs);
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.error.checkLog"));
                return;
            }
            List targets = new ArrayList();
            targets.add("domain");
            DeployUtil.reloadApplication(appName, targets , handlerCtx);
        }
   }

   

   //This handler is called after user deleted one more more VS from the VS table.
   //We need to go through all the application-ref to see if the VS specified still exist.  If it doesn't, we need to
   //remove that from the vs list.
   @Handler(id = "checkVsOfAppRef")
   public static void checkVsOfAppRef(HandlerContext handlerCtx) throws Exception{
       String configUrl = GuiUtil.getSessionValue("REST_URL") + "/configs/config/";
       List configs = new ArrayList(RestApiHandlers.getChildMap(configUrl).keySet());
       ArrayList vsList = new ArrayList();
       for (Object cfgName : configs) {
           String vsUrl = configUrl + cfgName + "/http-service/virtual-server";
           List vsNames = new ArrayList(RestApiHandlers.getChildMap(vsUrl).keySet());
           for (Object str : vsNames) {
               if (!vsList.contains(str))
                   vsList.add(str);
           }
       }
       List servers = new ArrayList(RestApiHandlers.getChildMap(GuiUtil.getSessionValue("REST_URL") + "/servers/server").keySet());
       for (Object svrName : servers) {
           String serverEndpoint = GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + svrName;
           List appRefs = new ArrayList(RestApiHandlers.getChildMap(serverEndpoint + "/application-ref").keySet());
           for (Object appRef : appRefs) {
               String apprefEndpoint = serverEndpoint + "/application-ref/" + appRef;
               Map apprefAttrs = RestApiHandlers.getAttributesMap(apprefEndpoint);
               String vsStr = (String) apprefAttrs.get("VirtualServers");
               List<String> lvsList = GuiUtil.parseStringList(vsStr, ",");
               boolean changed = false;
               String newVS = "";
               for(String oneVs: lvsList ){
                   if (! vsList.contains(oneVs)){
                       changed = true;
                       continue;
                   }
                   newVS = newVS+","+oneVs;
               }
               if (changed){
                   newVS = newVS.substring(1);
                   apprefAttrs.put("VirtualServers", vsStr);
                   RestResponse response = RestApiHandlers.sendUpdateRequest(apprefEndpoint, apprefAttrs, null, null, null);
                   if (!response.isSuccess()) {
                       GuiUtil.getLogger().severe("Update virtual server failed.  parent=" + apprefEndpoint + "; attrsMap =" + apprefAttrs);
                       GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.error.checkLog"));
                       return;
                   }
               }
           }
       }
   }


    private static String calContextRoot(String contextRoot) {
        //If context root is not specified or if the context root is "/", ensure that we don't show two // at the end.
        //refer to issue#2853
        String ctxRoot = "";
        if ((contextRoot == null) || contextRoot.equals("") || contextRoot.equals("/")) {
            ctxRoot = "/";
        } else if (contextRoot.startsWith("/")) {
            ctxRoot = contextRoot;
        } else {
            ctxRoot = "/" + contextRoot;
        }
        return ctxRoot;
    }

}
