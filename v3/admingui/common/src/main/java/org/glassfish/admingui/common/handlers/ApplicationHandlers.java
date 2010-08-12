/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TreeSet;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.DeployUtil;
import org.glassfish.admingui.common.util.TargetUtil;


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

        for(String oneAppName : appPropsMap.keySet()){

            String engines = appPropsMap.get(oneAppName);
            if (GuiUtil.isEmpty(engines)){
                //this is life cycle, do not display in the applications table.
                continue;
            }
            HashMap oneRow = new HashMap();
            oneRow.put("name", oneAppName);
            oneRow.put("selected", false);
            oneRow.put("enableURL", DeployUtil.getTargetEnableInfo(oneAppName, true));
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
        }
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("filters", new ArrayList(filters));
    }



    private static void getLaunchInfo(String serverName, String appName,  Map oneRow) {
        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/applications/application/" + appName + ".json";
        String contextRoot = (String) RestApiHandlers.restRequest(endpoint, null, "GET", null).get("contextRoot");
        if (contextRoot == null){
            contextRoot = "";
        }

        String refEndpoint = GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + serverName + "/application-ref/" + appName + ".json";
        //String status = (String) RestApiHandlers.getAttributesMap(refEndpoint).get("enabled");
        String status = (String) RestApiHandlers.restRequest(endpoint, null, "GET", null).get("enabled");
        boolean enabled = Boolean.parseBoolean(status);

        oneRow.put("contextRoot", contextRoot);
        oneRow.put("hasLaunch", false);
        if ( !enabled || contextRoot.equals("")){
            return;
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
    

    @Handler(id = "gf.getConfigName",
        input = {
            @HandlerInput(name = "target", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "configName", type = String.class)})
    public static void getConfigName(HandlerContext handlerCtx) {
        String target = (String) handlerCtx.getInputValue("target");
        String endpoint = (String)GuiUtil.getSessionValue("REST_URL");
        if (target.equals("server")){
            endpoint = endpoint + "/servers/server/server";
        }else{
            List clusters = TargetUtil.getClusters();
            if (clusters.contains(target)){
                endpoint = endpoint + "/clusters/cluster/" + target;
            }else{
                endpoint = endpoint + "/servers/server/" + target;
            }
        }
        handlerCtx.setOutputValue("configName", RestApiHandlers.getAttributesMap(endpoint).get("ConfigRef"));
    }



    @Handler(id = "gf.getApplicationTarget",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)})
    public static void getApplicationTarget(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        handlerCtx.setOutputValue( "result", DeployUtil.getApplicationTarget(appName));
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
                attrs.put("LbEnabled", Enabled);
            }else{
                attrs.put("Enabled", Enabled);
            }
            RestApiHandlers.restRequest(prefix+endpoint, attrs, "post", handlerCtx);
        }
     }


     @Handler(id="gf.changeAppTargets",
        input={
        @HandlerInput(name="appName", type=String.class, required=true),
        @HandlerInput(name="targets", type=String[].class, required=true )})
    public static void changeAppTargets(HandlerContext handlerCtx) {
        String appName = (String)handlerCtx.getInputValue("appName");
        String[] selTargets = (String[])handlerCtx.getInputValue("targets");
        List<String> selectedTargets = Arrays.asList(selTargets);

        List clusters = TargetUtil.getClusters();
        List standalone = TargetUtil.getStandaloneInstances();
        String clusterEndpoint = GuiUtil.getSessionValue("REST_URL")+"/clusters/cluster/";
        String serverEndpoint = GuiUtil.getSessionValue("REST_URL")+"/servers/server/";
        standalone.add("server");

        Map attrs = new HashMap();
        attrs.put("ref", appName);
        List<String> associatedTargets = DeployUtil.getApplicationTarget(appName);
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
        if (DeployUtil.reloadApplication(appName, handlerCtx)){
            GuiUtil.prepareAlert(handlerCtx, "success", GuiUtil.getMessage("org.glassfish.web.admingui.Strings", "restart.successPE"), null);
        }
    }

   @Handler(id = "gf.getTargetEnableInfo",
        input = {
            @HandlerInput(name = "appName", type = String.class, required = true)
        },
        output = {
            @HandlerOutput(name = "status", type = String.class)})
    public static void getTargetEnableInfo(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        handlerCtx.setOutputValue("status", DeployUtil.getTargetEnableInfo(appName, false));
    }

   @Handler(id = "getVsForDeployment",
        output = {
        @HandlerOutput(name = "result", type = List.class)})
    public static void getVsForDeployment(HandlerContext handlerCtx) {
        String endpoint = GuiUtil.getSessionValue("REST_URL")+"/configs/config/server-config/http-service/virtual-server";
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
        List<String> targetList = DeployUtil.getApplicationTarget(appName);
        List result = new ArrayList();
        Map attrs = null;
        String endpoint="";
        for(String oneTarget : targetList){
            HashMap oneRow = new HashMap();
            if (clusters.contains(oneTarget)){
                endpoint = "/clusters/cluster/" + oneTarget + "/application-ref/" + appName;
                attrs = RestApiHandlers.getAttributesMap(prefix + endpoint);
            }else{
                endpoint = "/servers/server/" + oneTarget + "/application-ref/" + appName;
                attrs = RestApiHandlers.getAttributesMap(prefix  + endpoint);
            }
            oneRow.put("selected", false);
            oneRow.put("endpoint", endpoint);
            oneRow.put("targetName", oneTarget);
            oneRow.put("enabled", attrs.get("Enabled"));
            oneRow.put("lbEnabled", attrs.get("LbEnabled"));
            result.add(oneRow);
        }
        handlerCtx.setOutputValue("result", result);
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
