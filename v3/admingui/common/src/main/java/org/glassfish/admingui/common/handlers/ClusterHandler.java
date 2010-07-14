/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * ClusterHandler.java
 *
 * Created on July 1,2010  9:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author anilam
 */
package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;

import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;
import java.util.List;
import org.glassfish.admingui.common.util.GuiUtil;

import org.glassfish.admingui.common.util.RestResponse;



public class ClusterHandler {

    /** Creates a new instance of InstanceHandler */
    public ClusterHandler() {
    }

    @Handler(id = "gf.getInstanceStatus",
        input = {
            @HandlerInput(name = "instanceName", type = String.class, required = true),
            @HandlerInput(name = "listInstanceProps", type = List.class, required = true)
        },
        output = {
            @HandlerOutput(name = "status", type = String.class)
        })
    public static void getInstanceStatus(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");
        List<Map<String, String>> props = (List<Map<String, String>>) handlerCtx.getInputValue("listInstanceProps");
        try{
            for(Map<String, String> oneProp : props){
                if (oneProp.get("name").equals(instanceName)){
                    handlerCtx.setOutputValue("status", oneProp.get("value"));
                    return;
                }
            }
         }catch(Exception ex){
             //Log error ?
             handlerCtx.setOutputValue("status", "UNKNOWN");
         }
     }
    
    @Handler(id = "gf.clusterAction",
        input = {
            @HandlerInput(name = "rows", type = List.class, required = true),
            @HandlerInput(name = "action", type = String.class, required = true),
            @HandlerInput(name = "extraInfo", type = Object.class) })
    public static void clusterAction(HandlerContext handlerCtx) {
        String action = (String) handlerCtx.getInputValue("action");
        List<Map> rows =  (List<Map>) handlerCtx.getInputValue("rows");
        List errorClusters = new ArrayList();
        Map response = null;

        for (Map oneRow : rows) {
            String clusterName = (String) oneRow.get("Name");

            boolean error = false;
            if (action.equals("delete-cluster")){
                //need to delete the clustered instance first
                Map clusterInstanceMap = (Map)handlerCtx.getInputValue("extraInfo");
                List<String> instanceNameList = (List) clusterInstanceMap.get(clusterName);
                for(String instanceName : instanceNameList){
                    response = deleteInstance(instanceName, null);
                    if (response == null){
                        errorClusters.add(clusterName);
                        error = true;
                        break;
                    }
                }
                if (error){
                    continue;
                }
            }
            try{
                response = RestApiHandlers.restRequest( GuiUtil.getSessionValue("REST_URL") + "/clusters/cluster/" + clusterName + "/" + action + ".xml", null, "post" ,null);
            }catch (Exception ex){
                GuiUtil.getLogger().severe("Error in clusterAction ; \nendpoint = " +
                            GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + clusterName + "/" + action + ".xml\n" );
                response = null;
            }
            if (response == null) {
                errorClusters.add(clusterName);
            }
        }
        if (errorClusters.size() > 0){
            String details = GuiUtil.getMessage(CLUSTER_RESOURCE_NAME, "cluster.error."+"action" , new String[]{""+errorClusters});
            GuiUtil.handleError(handlerCtx, details);
        }

     }


    @Handler(id = "gf.instanceAction",
        input = {
            @HandlerInput(name = "rows", type = List.class, required = true),
            @HandlerInput(name = "action", type = String.class, required = true)})
    public static void instanceAction(HandlerContext handlerCtx) {
        String action = (String) handlerCtx.getInputValue("action");
        List<Map> rows =  (List<Map>) handlerCtx.getInputValue("rows");
        List errorInstances = new ArrayList();
        Map response = null;

        for (Map oneRow : rows) {
            String instanceName = (String) oneRow.get("Name");
            if(action.equals("delete-instance")){
                response = deleteInstance(instanceName, (String) oneRow.get("Node"));
            }else{
                Map attrsMap = new HashMap();
                attrsMap.put("id", instanceName);
                try{
                    response = RestApiHandlers.restRequest( GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + instanceName + "/" + action + ".xml", attrsMap, "post" ,null);
                }catch (Exception ex){
                    GuiUtil.getLogger().severe("Error in instanceAction ; \nendpoint = " +
                            GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + instanceName + "/" + action + ".xml\n" +
                            "attrsMap=" + attrsMap);
                    response = null;
                }
            }
            if (response ==null){
                errorInstances.add(instanceName);
            }
        }
        if (errorInstances.size() > 0){
            String details = GuiUtil.getMessage(CLUSTER_RESOURCE_NAME, "instance.error."+"action" , new String[]{""+errorInstances});
            GuiUtil.handleError(handlerCtx, details);
        }
     }


    /*
     * getDeploymentTargets takes in a list of cluster names, and an list of Properties that is returned from the
     * list-instances --standaloneonly=true.  Extract the instance name from this properties list.
     * The result list will include "server",  clusters and standalone instances,  suitable for deployment or create resources.
     *
     */
    @Handler(id = "gf.getDeploymentTargets",
        input = {
            @HandlerInput(name = "clusterList", type = List.class),
            @HandlerInput(name = "listInstanceProps", type = List.class)
        },
        output = {
            @HandlerOutput(name = "result", type = List.class)
        })
    public static void getDeploymentTargets(HandlerContext handlerCtx) {
        List<String> result = new ArrayList();
        result.add("server");
        try{
            List<String> clusterList = (List) handlerCtx.getInputValue("clusterList");
            if (clusterList != null){
                for(String oneCluster : clusterList){
                    result.add(oneCluster);
                }
            }

            List<Map<String, String>> props = (List<Map<String, String>>) handlerCtx.getInputValue("listInstanceProps");
            for(Map<String, String> oneProp : props){
                result.add(oneProp.get("name"));
            }
         }catch(Exception ex){
             GuiUtil.getLogger().severe("getDeploymentTargets failed.");
             //print stacktrace ??
         }
        handlerCtx.setOutputValue("result", result);
     }

    private static Map deleteInstance(String instanceName, String nodeName){
        if (GuiUtil.isEmpty(nodeName)){
            Map iMap = RestApiHandlers.getAttributesMap(GuiUtil.getSessionValue("REST_URL")+"/servers/server/" + instanceName);
            nodeName = (String) iMap.get("Node");
        }
        Map attrsMap = new HashMap();
        attrsMap.put("id", instanceName);
        attrsMap.put("node", nodeName);
        try{
            return  RestApiHandlers.restRequest( GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + instanceName + "/delete-instance.xml", attrsMap, "post" ,null);
        }catch(Exception ex){
            GuiUtil.getLogger().severe("Error in deleteInstance ; \nendpoint = " +
                            GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + instanceName + "/delete-instance.xml\n" +
                            "attrsMap=" + attrsMap);
            return null;
        }
    }

    public static String CLUSTER_RESOURCE_NAME = "org.glassfish.cluster.admingui.Strings";
}
        
 
