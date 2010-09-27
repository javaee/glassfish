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
 * ApplicationHandlers.java
 *
 * Created on August 1, 2010, 2:32 PM
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
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import java.util.Set;
import java.util.TreeSet;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.DeployUtil;
import org.glassfish.admingui.common.util.RestUtil;
import org.glassfish.admingui.common.util.TargetUtil;
import org.glassfish.admingui.common.util.AppUtil;



public class ApplicationHandlers {

    /**
     *	<p> This handler returns the list of applications for populating the table.
     *  <p> Input  value: "serverName" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "gf.getDeployedAppsInfo",
        input = {
            @HandlerInput(name = "appPropsMap", type = Map.class, required=true),
            @HandlerInput(name = "serverName", type = String.class, defaultValue="server"),
            @HandlerInput(name = "filterValue", type = String.class)},
        output = {
            @HandlerOutput(name = "filters", type = java.util.List.class),
            @HandlerOutput(name = "result", type = java.util.List.class)})

    public static void getDeployedAppsInfo(HandlerContext handlerCtx) {
        String serverName = (String) handlerCtx.getInputValue("serverName");
        Map<String, String> appPropsMap = (Map) handlerCtx.getInputValue("appPropsMap");
        String filterValue = (String) handlerCtx.getInputValue("filterValue");
        Set filters = new TreeSet();
        filters.add("");
        if (GuiUtil.isEmpty(filterValue)) {
            filterValue = null;
        }
        List result = new ArrayList();

	if (appPropsMap != null) {
	    for(String oneAppName : appPropsMap.keySet()){
              try{
		String engines = appPropsMap.get(oneAppName);
		if (GuiUtil.isEmpty(engines)){
		    //this is life cycle, do not display in the applications table.
		    continue;
		}
		HashMap oneRow = new HashMap();
		oneRow.put("name", oneAppName);
                oneRow.put("encodedName", URLEncoder.encode(oneAppName, "UTF-8"));
		oneRow.put("selected", false);
		oneRow.put("enableURL", DeployUtil.getTargetEnableInfo(oneAppName, true, true));
		oneRow.put("sniffers", engines);

		List sniffersList = GuiUtil.parseStringList(engines, ",");
		oneRow.put("sniffersList", sniffersList);
		for(int ix=0; ix< sniffersList.size(); ix++)
		    filters.add(sniffersList.get(ix));
		if (filterValue != null){
		    if (! sniffersList.contains(filterValue))
			continue;
		}

		getLaunchInfo(serverName, oneAppName, oneRow);

		result.add(oneRow);
              }catch(Exception ex){
                ex.printStackTrace();
              }
	    }
	}
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("filters", new ArrayList(filters));
    }


    @Handler(id = "getSubComponents",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true),
            @HandlerInput(name = "appType", type = String.class, required = true),
            @HandlerInput(name = "moduleList", type = List.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)})
    public static void getSubComponents(HandlerContext handlerCtx) {
        List result = new ArrayList();
        String appName = (String) handlerCtx.getInputValue("appName");
        String appType = (String) handlerCtx.getInputValue("appType");
        List<String> modules = (List) handlerCtx.getInputValue("moduleList");

        for(String oneModule: modules){
            Map oneRow = new HashMap();
            List<String> snifferList = AppUtil.getSnifferListOfModule(appName, oneModule);

            String moduleName = oneModule;
            oneRow.put("moduleName", moduleName);
            oneRow.put("name", " ----------- ");
            oneRow.put("type", " ----------- ");
            oneRow.put("hasEndpoint", false);
            oneRow.put("hasLaunch", false);
            oneRow.put("hasAppClientLaunch", false);
            oneRow.put("sniffers", snifferList.toString());

            //TODO:  Siraj to add launch link.
//            if (snifferList.contains("web") &&  AppUtil.isApplicationEnabled(appName, "server")){
//            }

//            if (snifferList.contains("appclient")){
//                String jwEnabled = V3AMX.getPropValue(V3AMX.getInstance().getApplication(appName), "javaWebStartEnabled");
//                if (!GuiUtil.isEmpty(jwEnabled) && jwEnabled.equals("true") ){
//                    String appClientLaunch = V3AMX.getInstance().getRuntime().getRelativeJWSURI(appName, moduleName);
//                    oneRow.put("hasAppClientLaunch", !GuiUtil.isEmpty(appClientLaunch));
//                }
//            }
            result.add(oneRow);
            getSubComponentDetail(appName, moduleName, snifferList, result);
        }
        handlerCtx.setOutputValue("result", result);
    }



    private static List<Map> getSubComponentDetail(String appName, String moduleName, List<String> snifferList, List<Map> result){

        Map attrMap = new HashMap();
        attrMap.put("appName", appName);
        attrMap.put("moduleName", moduleName);
        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/applications/application/list-sub-components";
        Map subMap = RestApiHandlers.restRequest(endpoint, attrMap, "GET", null);
        Map data = (Map)subMap.get("data");
        if(data != null){
            Map<String, Object> props = (Map) data.get("properties");
            if (props == null){
                return result;
            }
            for(String cName: props.keySet()){
                Map oneRow = new HashMap();
                oneRow.put("moduleName", moduleName);
                oneRow.put("name", cName);
                oneRow.put("type", props.get(cName));
                oneRow.put("hasLaunch", false);
                oneRow.put("sniffers", "");
                oneRow.put("hasEndpoint", false);
                oneRow.put("hasAppClientLaunch", false);
    //            if (snifferList.contains("webservices")){
    //                if (!getEndpointMap(appName, moduleName, cName, sMap.get(cName)).isEmpty()){
    //                    oneRow.put("hasEndpoint", true );
    //                }
    //            }
                result.add(oneRow);
            }
        }
        return result;
    }


    @Handler(id = "gf.getLifecyclesInfo",
        input = {
            @HandlerInput(name = "children", type = List.class, required=true)},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)})

    public static void getLifecyclesInfo(HandlerContext handlerCtx) {
        List<Map> children = (List) handlerCtx.getInputValue("children");
        List result = new ArrayList();
        String prefix =  GuiUtil.getSessionValue("REST_URL") + "/applications/application/";
        if (children == null){
            handlerCtx.setOutputValue("result", result);
            return;
        }
        for(Map oneChild : children){
            Map oneRow = new HashMap();
            try{
                String name = (String) oneChild.get("message");
                String encodedName = URLEncoder.encode(name, "UTF-8");
                oneRow.put("name", name);
                oneRow.put("encodedName", encodedName);
                oneRow.put("selected", false);
                oneRow.put("loadOrder", RestUtil.getPropValue(prefix+encodedName, "load-order", handlerCtx));
                oneRow.put("enableURL", DeployUtil.getTargetEnableInfo(name, true, true));
                result.add(oneRow);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        handlerCtx.setOutputValue("result", result);
    }



    @Handler(id = "gf.deleteLifecycle",
        input = {
            @HandlerInput(name = "selectedList", type = List.class, required=true),
            @HandlerInput(name = "onlyDASExist", type = boolean.class, required=true)})

    public static void deleteLifecycle(HandlerContext handlerCtx) {
        List<Map> selectedList = (List) handlerCtx.getInputValue("selectedList");
        boolean onlyDASExist = (Boolean) handlerCtx.getInputValue("onlyDASExist");
        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/applications/application/delete-lifecycle-module" ;
        Map attrs = new HashMap();
        if (onlyDASExist){
            attrs.put("target", "server");
        }
        try{
            for(Map oneRow: selectedList){
                String name = (String) oneRow.get("name");
                String encodedName = URLEncoder.encode(name, "UTF-8");
                attrs.put("id", encodedName);
                if (onlyDASExist){
                    RestApiHandlers.restRequest( endpoint, attrs, "POST", handlerCtx);
                } else{
                    //delete all application-ref first
                    List<Map> appRefs = DeployUtil.getRefEndpoints(name, "application-ref");
                    for(Map  oneRef:  appRefs){
                        attrs.put("target", oneRef.get("targetName"));
                        RestApiHandlers.restRequest((String)oneRef.get("endpoint"), attrs, "DELETE", null);
                    }
                    attrs.put("target", "domain");
                    RestApiHandlers.restRequest( endpoint, attrs, "POST", null);
                }
            }
        }catch(Exception ex){
            GuiUtil.prepareException(handlerCtx, ex);
        }
    }


    private static void getLaunchInfo(String serverName, String appName,  Map oneRow) {
        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/applications/application/" + appName + ".json";
        Map map = RestApiHandlers.restRequest(endpoint, null, "GET", null);
        Map data = (Map)map.get("data");
        String contextRoot = "";
        boolean enabled = false;
        if (data != null) {
            Map extraProperties = (Map)data.get("extraProperties");
            if (extraProperties != null) {
                Map entity = (Map)extraProperties.get("entity");
                if (entity != null) {
                    contextRoot = (String) entity.get("contextRoot");
                    enabled = Boolean.parseBoolean((String) entity.get("enabled"));
                }
            }
        }


        oneRow.put("contextRoot", (contextRoot==null)? "" : contextRoot);
        oneRow.put("hasLaunch", false);
        if ( !enabled || GuiUtil.isEmpty(contextRoot)){
            return;
        }

        List<String> targetList = DeployUtil.getApplicationTarget(appName, "application-ref");
        for(String target : targetList) {
            String virtualServers = getVirtualServers(target, appName);
            Map result = getListener(virtualServers, "server-config");


            String vs = (String)result.get("vs");
            if (vs.equals( "server")){
                vs = serverName;   //this is actually the hostName, more readable for user in the launch URL.
            }
            String port = (String)result.get("port");
            String protocol = (String)result.get("protocol");
            oneRow.put("hasLaunch", true);
            oneRow.put("launchLink", protocol + "://" + vs + ":" + port + calContextRoot(contextRoot));
            //return protocol + "://" + vs + ":" + port ;

/*            HashMap  targetMap = new HashMap();
            if (clusters.contains(target)){
        handlerCtx.setOutputValue("configName", RestApiHandlers.getAttributesMap(endpoint).get("configRef"));
                endpoint = "/clusters/cluster/" + target + "/application-ref/" + appName;
                attrs = RestApiHandlers.getAttributesMap(prefix + endpoint);
            }else{
                endpoint = "/servers/server/" + target + "/application-ref/" + appName;
                attrs = RestApiHandlers.getAttributesMap(prefix  + endpoint);
            }
 */
        }

        /*
         * TODO:  application Launch info,  need to port from AMX to REST.
         *
         *
        String launchLink = V3AMXUtil.getLaunchLink(serverName, appName);
        if (! GuiUtil.isEmpty(launchLink)){
            oneRow.put("hasLaunch", true);
            oneRow.put("launchLink", launchLink + calContextRoot(contextRoot));
        }
        */
    }

    private static String getVirtualServers(String target, String appName) {
        List clusters = TargetUtil.getClusters();
        List standalone = TargetUtil.getStandaloneInstances();
        standalone.add("server");
        String ep = (String)GuiUtil.getSessionValue("REST_URL");
        if (clusters.contains(target)){
            ep = ep + "/clusters/cluster/" + target + "/application-ref/" + appName;
        }else{
            ep = ep + "/servers/server/" + target + "/application-ref/" + appName;
        }
        String virtualServers =
                (String)RestApiHandlers.getAttributesMap(ep).get("virtualServers");
        return virtualServers;
    }

    // returns a  http-listener that is linked to a non-admin VS
    /*
    private static Map getListener() {
        Map<String, VirtualServer> vsMap = V3AMX.getServerConfig("server-config").getHttpService().childrenMap(VirtualServer.class);
        return getOneVsWithNetworkListener(new ArrayList(vsMap.keySet()));
    }
    */

    private static Map getListener(String vsIds, String configName) {
        return getOneVsWithNetworkListener(GuiUtil.parseStringList(vsIds, ","), configName);
    }

    private static Map getOneVsWithNetworkListener(List<String> vsList, String configName) {
        Map result = new HashMap();
        if (vsList == null || vsList.size() == 0) {
            return null;
        }
        //Just to ensure we look at "server" first.
        if (vsList.contains("server")){
            vsList.remove("server");
            vsList.add(0, "server");
        }
        boolean found = false;
//        Map<String, VirtualServer> vsMap = V3AMX.getServerConfig("server-config").getHttpService().childrenMap(VirtualServer.class);
        String ep = (String)GuiUtil.getSessionValue("REST_URL");
        ep = ep + "/configs/config/" + configName + "/http-service/virtual-server";
        Map vsInConfig = new HashMap();
        try{
            vsInConfig = RestApiHandlers.getChildMap(ep);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        for (String vsName : vsList) {
            if (vsName.equals("admin-listener")) {
                continue;
            }
            Object vs = vsInConfig.get(vsName);
            if (vs != null) {
                ep = (String)GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + 
                        configName + "/http-service/virtual-server/" + vsName;
                String listener = (String)RestApiHandlers.getAttributesMap(ep).get("networkListeners");
                if (GuiUtil.isEmpty(listener)) {
                    continue;
                } else {
                    List<String> hpList = GuiUtil.parseStringList(listener, ",");
                    for (String one : hpList) {
                        ep = (String)GuiUtil.getSessionValue("REST_URL") +
"/configs/config/server-config/network-config/network-listeners/network-listener/" + one;

                        Map nlAttributes = RestApiHandlers.getAttributesMap(ep);
                        if ("false".equals((String)nlAttributes.get("enabled"))) {
                            continue;
                        }
//                        String security = (String)oneListener.findProtocol().attributesMap().get("SecurityEnabled");
                        String protocol = (String)nlAttributes.get("protocol");
                        ep = (String)GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + 
                                configName + "/network-config/protocols/protocol/" + protocol;
                        String security = (String)RestApiHandlers.getAttributesMap(ep).get("securityEnabled");

                        result.put("port", nlAttributes.get("port"));
                        result.put("vs", vsName);

                        if ("true".equals(security)) {
                            //use this secured port, but try to find one thats not secured.
                            result.put("protocol", "https");
                            //result.put("port", oneListener.resolveAttribute("Port"));
                            found = true;
                            continue;
                        } else {
                            result.put("protocol", "http");
                            //result.put("port", oneListener.resolveAttribute("Port"));
                            return result;
                        }
                    }
                }
            }
        }
        return found ? result : null;
    }


    @Handler(id = "gf.getTargetEndpoint",
        input = {
            @HandlerInput(name = "target", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "endpoint", type = String.class)})
    public static void getTargetEndpoint(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("endpoint", TargetUtil.getTargetEndpoint( (String) handlerCtx.getInputValue("target")));
    }


    //TODO:  whoever that calls gf.getConfigName() should call getTargetEndpoint and then grep the config in jsf.
    @Handler(id = "gf.getConfigName",
        input = {
            @HandlerInput(name = "target", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "configName", type = String.class)})
    public static void getConfigName(HandlerContext handlerCtx) {
        String endpoint = TargetUtil.getTargetEndpoint( (String) handlerCtx.getInputValue("target"));
        handlerCtx.setOutputValue("configName", RestApiHandlers.getAttributesMap(endpoint).get("configRef"));
    }


    @Handler(id = "gf.getApplicationTarget",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)})
    public static void getApplicationTarget(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        handlerCtx.setOutputValue( "result", DeployUtil.getApplicationTarget(appName, "application-ref"));
    }


     @Handler(id = "gf.changeTargetStatus",
        input = {
            @HandlerInput(name = "selectedRows", type = List.class, required = true),
            @HandlerInput(name = "Enabled", type = String.class, required = true),
            @HandlerInput(name = "forLB", type = Boolean.class, required = true)})
    public static void changeTargetStatus(HandlerContext handlerCtx) {
        String Enabled = (String) handlerCtx.getInputValue("Enabled");
        List<Map>  selectedRows = (List) handlerCtx.getInputValue("selectedRows");
        boolean forLB = (Boolean) handlerCtx.getInputValue("forLB");
        String prefix = (String) GuiUtil.getSessionValue("REST_URL");
        for(Map oneRow : selectedRows){
            Map attrs = new HashMap();
            String endpoint = (String) oneRow.get("endpoint");
            if(forLB){
                attrs.put("lbEnabled", Enabled);
                RestApiHandlers.restRequest(prefix+endpoint, attrs, "post", handlerCtx);
            }else{
                DeployUtil.enableApp( (String)oneRow.get("name"), (String) oneRow.get("targetName"), handlerCtx,
                        Boolean.parseBoolean(Enabled));
            }
        }
     }


     @Handler(id="gf.changeAppTargets",
        input={
        @HandlerInput(name="appName", type=String.class, required=true),
        @HandlerInput(name="targets", type=String[].class, required=true),
        @HandlerInput(name="status", type=String.class)})
    public static void changeAppTargets(HandlerContext handlerCtx) {
        String appName = (String)handlerCtx.getInputValue("appName");
        String status = (String)handlerCtx.getInputValue("status");
        String[] selTargets = (String[])handlerCtx.getInputValue("targets");
        List<String> selectedTargets = Arrays.asList(selTargets);

        List clusters = TargetUtil.getClusters();
        List standalone = TargetUtil.getStandaloneInstances();
        String clusterEndpoint = GuiUtil.getSessionValue("REST_URL")+"/clusters/cluster/";
        String serverEndpoint = GuiUtil.getSessionValue("REST_URL")+"/servers/server/";
        standalone.add("server");

        Map attrs = new HashMap();
        attrs.put("id", appName);
        List<String> associatedTargets = DeployUtil.getApplicationTarget(appName, "application-ref");
        for(String newTarget :  selectedTargets){
            String endpoint;
            if (associatedTargets.contains(newTarget)){
                //no need to add or remove.
                associatedTargets.remove(newTarget);
                continue;
            }else{
                if (clusters.contains(newTarget)){
                    endpoint = clusterEndpoint + newTarget + "/application-ref" ;
                }else{
                    endpoint = serverEndpoint + newTarget + "/application-ref" ;
                }
                attrs.put("target", newTarget);
                if (status != null){
                    attrs.put("enabled", status);
                }
                RestApiHandlers.restRequest(endpoint, attrs, "post", handlerCtx);
            }
         }

         for(String oTarget :  associatedTargets){
            String endpoint;
            if (clusters.contains(oTarget)){
                endpoint = clusterEndpoint + oTarget ;
            }else{
                endpoint = serverEndpoint + oTarget ;
            }
            Map attrMap = new HashMap();
            attrMap.put("target", oTarget);
            RestApiHandlers.restRequest(endpoint + "/application-ref/" + appName, attrMap, "delete", handlerCtx);
        }
    }

   @Handler(id = "gf.reloadApplication",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true)
        })
    public static void reloadApplication(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        List<String> associatedTargets = DeployUtil.getApplicationTarget(appName, "application-ref");
        if (DeployUtil.reloadApplication(appName, associatedTargets,  handlerCtx)){
            GuiUtil.prepareAlert("success", GuiUtil.getMessage("org.glassfish.web.admingui.Strings", "restart.successPE"), null);
        }
    }

   @Handler(id = "gf.getTargetEnableInfo",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true),
            @HandlerInput(name = "isApp", type = Boolean.class)
        },
        output = {
            @HandlerOutput(name = "status", type = String.class)})
    public static void getTargetEnableInfo(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        Boolean isApp = (Boolean) handlerCtx.getInputValue("isApp");
        if(isApp == null) {
            isApp = true;
        }
        handlerCtx.setOutputValue("status", DeployUtil.getTargetEnableInfo(appName, false, isApp));
    }

   @Handler(id = "getVsForDeployment",
        input = {
            @HandlerInput(name = "targetConfig", type = String.class, defaultValue="server-config")
        },
        output = {
        @HandlerOutput(name = "result", type = List.class)})
    public static void getVsForDeployment(HandlerContext handlerCtx) {
       String targetConfig = (String) handlerCtx.getInputValue("targetConfig");
        String endpoint = GuiUtil.getSessionValue("REST_URL")+"/configs/config/"+targetConfig+"/http-service/virtual-server";
        List vsList = new ArrayList();
        try{
            vsList = new ArrayList(RestApiHandlers.getChildMap(endpoint).keySet());
            vsList.remove("__asadmin");
       }catch(Exception ex){
           //TODO: error handling.
       }
        handlerCtx.setOutputValue("result", vsList);
   }


    @Handler(id = "gf.getTargetListInfo",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)})
    public static void getTargetListInfo(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        String prefix = (String) GuiUtil.getSessionValue("REST_URL");
        List clusters = TargetUtil.getClusters();
        List standalone = TargetUtil.getStandaloneInstances();
        standalone.add("server");
        List<String> targetList = DeployUtil.getApplicationTarget(appName, "application-ref");
        List result = new ArrayList();
        Map attrs = null;
        String endpoint="";
        for(String oneTarget : targetList){
            HashMap oneRow = new HashMap();
            if (clusters.contains(oneTarget)){
                endpoint = prefix + "/clusters/cluster/" + oneTarget + "/application-ref/" + appName;
                attrs = RestApiHandlers.getAttributesMap(prefix + endpoint);
            }else{
                endpoint = prefix+"/servers/server/" + oneTarget + "/application-ref/" + appName;
                attrs = RestApiHandlers.getAttributesMap(endpoint);
            }
            oneRow.put("name", appName);
            oneRow.put("selected", false);
            oneRow.put("endpoint", endpoint);
            oneRow.put("targetName", oneTarget);
            oneRow.put("enabled", attrs.get("enabled"));
            oneRow.put("lbEnabled", attrs.get("lbEnabled"));
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
    }

    /*
     * This handler is called for populating the application table in the cluster or instance Application tab.
     */
    @Handler(id = "gf.getSingleTargetAppsInfo",
        input = {
            @HandlerInput(name = "appPropsMap", type = Map.class, required=true),
            @HandlerInput(name = "appRefEndpoint", type = String.class, required=true),
            @HandlerInput(name = "target", type = String.class, required=true),
            @HandlerInput(name = "filterValue", type = String.class)},
        output = {
            @HandlerOutput(name = "filters", type = java.util.List.class),
            @HandlerOutput(name = "result", type = java.util.List.class)})

    public static void getSingleTargetAppsInfo(HandlerContext handlerCtx) {
        String appRefEndpoint = (String) handlerCtx.getInputValue("appRefEndpoint");
        String target = (String) handlerCtx.getInputValue("target");
        Map<String, String> appPropsMap = (Map) handlerCtx.getInputValue("appPropsMap");
        String filterValue = (String) handlerCtx.getInputValue("filterValue");
        Set filters = new TreeSet();
        filters.add("");
        if (GuiUtil.isEmpty(filterValue)) {
            filterValue = null;
        }
        List result = new ArrayList();
        String prefix = (String) GuiUtil.getSessionValue("REST_URL");
	if (appPropsMap != null) {
	    for(String oneAppName : appPropsMap.keySet()){
                try{
                    String engines = appPropsMap.get(oneAppName);
                    HashMap oneRow = new HashMap();
                    oneRow.put("name", oneAppName);
                    String encodedName = URLEncoder.encode(oneAppName, "UTF-8");
                    oneRow.put("targetName", target);
                    oneRow.put("selected", false);
                    Map appRefAttrsMap = RestApiHandlers.getAttributesMap(prefix + appRefEndpoint + encodedName);
                    String image = (appRefAttrsMap.get("enabled").equals("true")) ?  "/resource/images/enabled.png" : "/resource/images/disabled.png";
                    oneRow.put("enabled", image);
                    image = (appRefAttrsMap.get("lbEnabled").equals("true")) ?  "/resource/images/enabled.png" : "/resource/images/disabled.png";
                    oneRow.put("lbEnabled",  image);
                    oneRow.put("endpoint", appRefEndpoint+encodedName);
                    oneRow.put("sniffers", engines);
                    List sniffersList = GuiUtil.parseStringList(engines, ",");
                    oneRow.put("sniffersList", sniffersList);
                    for(int ix=0; ix< sniffersList.size(); ix++)
                        filters.add(sniffersList.get(ix));
                    if (filterValue != null){
                        if (! sniffersList.contains(filterValue))
                            continue;
                    }
                    result.add(oneRow);
                }catch(Exception ex){
                    //skip this app.
                }
	    }
	}
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("filters", new ArrayList(filters));
    }


    @Handler(id="getTargetURLList",
        input={
            @HandlerInput(name="AppID", type=String.class, required=true),
            @HandlerInput(name="contextRoot", type=String.class)},
        output={
            @HandlerOutput(name="URLList", type=List.class)})

    public void getTargetURLList(HandlerContext handlerCtx) {
	String id = (String)handlerCtx.getInputValue("AppID");
        String contextRoot = (String)handlerCtx.getInputValue("contextRoot");
        String ctxRoot = calContextRoot(contextRoot);

        List urls = getLaunchInfo2(id);
	Iterator it = urls.iterator();
	String url = null;
        ArrayList list = new ArrayList();
	while (it.hasNext()) {
	    url = (String)it.next();
            HashMap m = new HashMap();
            m.put("url", url + ctxRoot);
            list.add(m);
	}

        handlerCtx.setOutputValue("URLList", list);

    }


    /*
     * Get the application type for the specified appName.
     * If there isComposite property is true, the appType will be returned as 'ear'
     * Otherwise, depends on the sniffer engine
     */
    @Handler(id = "gf.getApplicationType",
        input = {
            @HandlerInput(name = "snifferMap", type = Map.class, required = true)},
        output = {
            @HandlerOutput(name = "appType", type = String.class)})
    public static void getApplicationType(HandlerContext handlerCtx) {
        Map<String,String> snifferMap = (Map) handlerCtx.getInputValue("snifferMap");
        String appType = "ejb";
        if (! GuiUtil.isEmpty(snifferMap.get("web"))){
            appType="war";
        }else
        if (! GuiUtil.isEmpty(snifferMap.get("connector"))){
            appType="rar";
        }else
        if (! GuiUtil.isEmpty(snifferMap.get("appclient"))){
            appType="appclient";
        }
        handlerCtx.setOutputValue("appType", appType);
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
    


/********************/


    private static List getLaunchInfo2(String appName) {
        List<String> targetList = DeployUtil.getApplicationTarget(appName, "application-ref");
        List URLs = new ArrayList();
        for(String target : targetList) {
            String virtualServers = getVirtualServers(target, appName);
            URLs.addAll(getURLs(GuiUtil.parseStringList(virtualServers, ","), "server-config"));
/*
            String vs = (String)result.get("vs");
            if (vs.equals( "server")){
                vs = serverName;   //this is actually the hostName, more readable for user in the launch URL.
            }
 */
        }
        return URLs;
    }


    private static List getURLs(List<String> vsList, String configName) {
        List URLs = new ArrayList();
        Map result = new HashMap();
        if (vsList == null || vsList.size() == 0) {
            return null;
        }
        //Just to ensure we look at "server" first.
        if (vsList.contains("server")){
            vsList.remove("server");
            vsList.add(0, "server");
        }
        boolean found = false;
//        Map<String, VirtualServer> vsMap = V3AMX.getServerConfig("server-config").getHttpService().childrenMap(VirtualServer.class);
        String ep = (String)GuiUtil.getSessionValue("REST_URL");
        ep = ep + "/configs/config/" + configName + "/http-service/virtual-server";
        Map vsInConfig = new HashMap();
        try{
            vsInConfig = RestApiHandlers.getChildMap(ep);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        for (String vsName : vsList) {
            if (vsName.equals("admin-listener")) {
                continue;
            }
            Object vs = vsInConfig.get(vsName);
            if (vs != null) {
                ep = (String)GuiUtil.getSessionValue("REST_URL") + "/configs/config/" +
                        configName + "/http-service/virtual-server/" + vsName;
                String listener = (String)RestApiHandlers.getAttributesMap(ep).get("networkListeners");
                if (GuiUtil.isEmpty(listener)) {
                    continue;
                } else {
                    List<String> hpList = GuiUtil.parseStringList(listener, ",");
                    for (String one : hpList) {
                        ep = (String)GuiUtil.getSessionValue("REST_URL") +
"/configs/config/server-config/network-config/network-listeners/network-listener/" + one;

                        Map nlAttributes = RestApiHandlers.getAttributesMap(ep);
                        if ("false".equals((String)nlAttributes.get("enabled"))) {
                            continue;
                        }
//                        String security = (String)oneListener.findProtocol().attributesMap().get("SecurityEnabled");
                        ep = (String)GuiUtil.getSessionValue("REST_URL") + "/configs/config/" +
                                configName + "/network-config/protocols/protocol/" + (String)nlAttributes.get("protocol");
                        String security = (String)RestApiHandlers.getAttributesMap(ep).get("securityEnabled");

                        String protocol = "http";
                        if ("true".equals(security))
                            protocol = "https";
/*                        URLs.add(protocol + "://" + vsName + ":" +
                                (String)nlAttributes.get("port"));
 *
 */
                        // for now specify localhost, hostname is not available.
                            URLs.add(protocol + "://" + "localhost" + ":" +
                                (String)nlAttributes.get("port"));

                    }
                }
            }
        }
        return URLs;
    }
}
