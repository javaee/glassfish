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
 * DeploymentHandler.java
 *
 */
package org.glassfish.admingui.common.handlers;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.admin.amx.base.RuntimeRoot;
import org.glassfish.deployment.client.DFDeploymentStatus;
import org.glassfish.deployment.client.DeploymentFacility;
import org.glassfish.deployment.client.DFProgressObject;
import org.glassfish.deployment.client.DFDeploymentProperties;


import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.intf.config.VirtualServer;
import org.glassfish.admingui.common.util.DeployUtil;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;
import org.w3c.dom.Document;

/**
 *
 */
public class DeploymentHandler {

    //should be the same as in DeploymentProperties in deployment/common
    public static final String KEEP_SESSIONS = "keepSessions";

    @Handler(id = "deploy",
    input = {
        @HandlerInput(name = "filePath", type = String.class),
        @HandlerInput(name = "origPath", type = String.class),
        @HandlerInput(name = "allMaps", type = Map.class),
        @HandlerInput(name = "appType", type = String.class),
        @HandlerInput(name = "propertyList", type = List.class)
    })
    public static void deploy(HandlerContext handlerCtx) {

        String appType = (String) handlerCtx.getInputValue("appType");
        String origPath = (String) handlerCtx.getInputValue("origPath");
        String filePath = (String) handlerCtx.getInputValue("filePath");
        Map allMaps = (Map) handlerCtx.getInputValue("allMaps");
        Map attrMap = new HashMap((Map) allMaps.get(appType));

        if (GuiUtil.isEmpty(origPath)) {
            String mesg = GuiUtil.getMessage("msg.deploy.nullArchiveError");
            GuiUtil.handleError(handlerCtx, mesg);
            return;
        }
        try{
            String decodedName = URLDecoder.decode((String)attrMap.get("name"), "UTF-8");
            attrMap.put("name", decodedName);
        }catch(Exception ex){
            //ignore
        }

        DFDeploymentProperties deploymentProps = new DFDeploymentProperties();

        /* Take care some special properties, such as VS  */

        //do not send VS if user didn't specify, refer to bug#6542276
        String[] vs = (String[]) attrMap.get(DFDeploymentProperties.VIRTUAL_SERVERS);
        if (vs != null && vs.length > 0) {
            if (!GuiUtil.isEmpty(vs[0])) {
                String vsTargets = GuiUtil.arrayToString(vs, ",");
                deploymentProps.setProperty(DFDeploymentProperties.VIRTUAL_SERVERS, vsTargets);
            }
        }
        attrMap.remove(DFDeploymentProperties.VIRTUAL_SERVERS);

        //Take care of checkBox
        List<String> convertToFalseList = (List) attrMap.get("convertToFalseList");
        if (convertToFalseList != null) {
            for (String one : convertToFalseList) {
                if (attrMap.get(one) == null) {
                    attrMap.put(one, "false");
                }
            }
            attrMap.remove("convertToFalseList");
        }

        Properties props = new Properties();
        for (Object attr : attrMap.keySet()) {
            String key = (String) attr;
            String prefix = "PROPERTY-";
            String value = (String) attrMap.get(key);
            if (value == null) {
                continue;
            }
            if (key.startsWith(prefix)) {
                if (! value.equals("")){
                    props.setProperty(key.substring(prefix.length()), value);
                }

            } else {
                deploymentProps.setProperty(key, value);
            }
        }



        // include any  additional property that user enters
        List<Map<String, String>> propertyList = (List) handlerCtx.getInputValue("propertyList");
        if (propertyList != null) {
            Set propertyNames = new HashSet();
            for (Map<String, String> oneRow : propertyList) {
                final String name = oneRow.get(ProxyHandlers.PROPERTY_NAME);
                if (GuiUtil.isEmpty(name)) {
                    continue;
                }
                if (propertyNames.contains(name)) {
                    GuiUtil.getLogger().warning("Ignored Duplicate Property Name : " + name);
                    continue;
                } else {
                    propertyNames.add(name);
                }
                String value = oneRow.get(ProxyHandlers.PROPERTY_VALUE);
                if (GuiUtil.isEmpty(value)) {
                    continue;
                }
                props.setProperty(name, value);

            }
        }

        if (props.size() > 0) {
            deploymentProps.setProperties(props);
        }

        try {
            DeployUtil.deploy(null, deploymentProps, filePath, handlerCtx);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }

    }

    /**
     *  <p> This handler redeploy any application
     */
    @Handler(id = "redeploy",
    input = {
        @HandlerInput(name = "filePath", type = String.class, required = true),
        @HandlerInput(name = "origPath", type = String.class, required = true),
        @HandlerInput(name = "deployMap", type = Map.class, required = true),
        @HandlerInput(name = "convertToFalse", type = List.class, required = true)
    })
    public static void redeploy(HandlerContext handlerCtx) {
        try {
            String filePath = (String) handlerCtx.getInputValue("filePath");
            String origPath = (String) handlerCtx.getInputValue("origPath");
            Map<String,String> deployMap = (Map) handlerCtx.getInputValue("deployMap");
            List<String> convertToFalsList = (List<String>) handlerCtx.getInputValue("convertToFalse");
            if (convertToFalsList != null)
            for (String one : convertToFalsList) {
                if (deployMap.get(one) == null) {
                    deployMap.put(one, "false");
                }
            }
            String appName = deployMap.get("appName");
            DFDeploymentProperties deploymentProps = new DFDeploymentProperties();

             //If we are redeploying a web app, we want to preserve context root.
             AMXProxy app = V3AMX.getInstance().getApplication(appName);
             String ctxRoot = (String) app.attributesMap().get("ContextRoot");
             if (ctxRoot != null){
                 deploymentProps.setContextRoot(ctxRoot);
             }
             deploymentProps.setForce(true);
             deploymentProps.setUpload(false);
             deploymentProps.setName(appName);
             deploymentProps.setVerify(Boolean.parseBoolean(deployMap.get("verify")));
             deploymentProps.setPrecompileJSP(Boolean.parseBoolean(deployMap.get("precompilejsp")));
             Properties prop = new Properties();
             prop.setProperty(KEEP_SESSIONS, ""+deployMap.get("keepSessions"));
             deploymentProps.setProperties(prop);
             
             DeployUtil.invokeDeploymentFacility(null, deploymentProps, filePath, handlerCtx);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler takes in selected rows, and do the undeployment
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="undeploy",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true)})
    public static void undeploy(HandlerContext handlerCtx) {

        Object obj = handlerCtx.getInputValue("selectedRows");

        Properties dProps = new Properties();
//        if(appType.equals("connector")) {
//                //Default cascade is true. May be we can issue a warning,
//                //bcz undeploy will fail anyway if cascade is false.
//                dProps.put(DFDeploymentProperties.CASCADE, "true");
//        }

        List errorList = new ArrayList();
        List undeployedAppList = new ArrayList();
        List selectedRows = (List) obj;
        DFProgressObject progressObject = null;
        DeploymentFacility df = GuiUtil.getDeploymentFacility();
        //Hard coding to server, fix me for actual targets in EE.
        String[] targetNames = new String[]{"server"};
        for (int i = 0; i < selectedRows.size(); i++) {
            Map oneRow = (Map) selectedRows.get(i);
            String appName = (String) oneRow.get("name");
            //Undeploy the app here.
//            if(V3AMX.getInstance().isEE()){
//                List<String> refList = TargetUtil.getDeployedTargets(appName, true);
//                if(refList.size() > 0)
//                    targetNames = refList.toArray(new String[refList.size()]);
//                else
//                    targetNames=new String[]{"domain"};
//            }

            progressObject = df.undeploy(df.createTargets(targetNames), appName, dProps);
            progressObject.waitFor();
            DFDeploymentStatus status = progressObject.getCompletedStatus();
            //we DO want it to continue and call the rest handlers, ie navigate(). This will
            //re-generate the table data because there may be some apps thats been undeployed 
            //successfully.  If we stopProcessing, the table data is stale and still shows the
            //app that has been gone.
            if (DeployUtil.checkDeployStatus(status, handlerCtx, false)) {
                undeployedAppList.add(appName);
            }else{
                errorList.add(appName);
            }
        }
        removeFromDefaultWebModule(undeployedAppList);
        if (errorList.size() > 0){
            GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), GuiUtil.getMessage("msg.deploy.UndeployError") + " " + GuiUtil.listToString(errorList, ","));
        }
    }


    //For any undeployed applications, we need to ensure that it is no longer specified as the
    //default web module of any VS.
    static private void  removeFromDefaultWebModule(List<String> undeployedAppList){
        Map<String, VirtualServer> vsMap = V3AMX.getInstance().getConfig("server-config").getHttpService().getVirtualServer();
        for(VirtualServer vs : vsMap.values()){
            String appName = vs.getDefaultWebModule();
            if (GuiUtil.isEmpty(appName)){
                continue;
            }
            int index = appName.indexOf("#");
            if (index != -1){
                appName = appName.substring(0, index);
            }
            if (undeployedAppList.contains(appName)){
                vs.setDefaultWebModule("");
            }
        }
    }
        
    /**
     *	<p> This handler takes in selected rows, and change the status of the app
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
     *  <p> Input  value: "enabled" -- Type: <code>Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "changeAppStatus",
    input = {
        @HandlerInput(name = "selectedRows", type = List.class, required = true),
        @HandlerInput(name = "enabled", type = Boolean.class, required = true)
    })
    public static void changeAppStatus(HandlerContext handlerCtx) {

        List obj = (List) handlerCtx.getInputValue("selectedRows");
        boolean enabled = ((Boolean) handlerCtx.getInputValue("enabled")).booleanValue();
        GuiUtil.getLogger().fine("changeAppStatus:  enabled = " + enabled);
        DeploymentFacility df = GuiUtil.getDeploymentFacility();
        //Hard coding to server, fix me for actual targets in EE.
        List selectedRows = (List) obj;
        for (int i = 0; i < selectedRows.size(); i++) {
            Map oneRow = (Map) selectedRows.get(i);
            String appName = (String) oneRow.get("name");
            if (DeployUtil.enableApp(appName, handlerCtx, enabled)){
                String msg = GuiUtil.getMessage((enabled) ? "msg.enableSuccessfulPE" : "msg.disableSuccessfulPE");
                GuiUtil.prepareAlert(handlerCtx, "success", msg, null);
            }else{
                //stop changing other app status.
                break;
            }
        }
    }

    /**
     *	<p> This method returns the deployment descriptors for a given app. </p>
     *
     *  <p> Output value: "descriptors" -- Type: <code>java.util.List</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getDeploymentDescriptorList",
    input = {
        @HandlerInput(name = "appName", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "descriptors", type = List.class)
    })
    public static void getDeploymentDescriptorList(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        List result = new ArrayList();
        RuntimeRoot runtimeMgr = V3AMX.getInstance().getRuntime();
        List<Map<String, String>> ddList = runtimeMgr.getDeploymentConfigurations(appName);
        if (ddList.size() > 0) {
            for (Map<String, String> oneDD : ddList) {
                HashMap oneRow = new HashMap();
                oneRow.put("moduleName", oneDD.get(RuntimeRoot.MODULE_NAME_KEY));
                oneRow.put("ddPath", oneDD.get(RuntimeRoot.DD_PATH_KEY));
//                    oneRow.put("ddContent", oneDD.get(RuntimeRoot.DD_CONTENT_KEY) );
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("descriptors", result);
    }

    @Handler(id = "getDeploymentDescriptor",
    input = {
        @HandlerInput(name = "appName", type = String.class, required = true),
        @HandlerInput(name = "moduleName", type = String.class, required = true),
        @HandlerInput(name = "descriptorName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "content", type = String.class),
        @HandlerOutput(name = "encoding", type = String.class)
    })
    public static void getDeploymentDesciptor(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        String moduleName = (String) handlerCtx.getInputValue("moduleName");
        if (moduleName == null){
            moduleName = "";   //for application.xml and sun-application.xml  where it is at top leverl, with a module name.
        }
        String descriptorName = (String) handlerCtx.getInputValue("descriptorName");
        RuntimeRoot runtimeMgr = V3AMX.getInstance().getRuntime();
        List<Map<String, String>> ddList = runtimeMgr.getDeploymentConfigurations(appName);
        if (ddList.size() > 0) {
            for (Map<String, String> oneDD : ddList) {
                if (oneDD.get(RuntimeRoot.MODULE_NAME_KEY).equals(moduleName) && oneDD.get(RuntimeRoot.DD_PATH_KEY).equals(descriptorName)) {
                    String content = oneDD.get(RuntimeRoot.DD_CONTENT_KEY);

                    handlerCtx.setOutputValue("content", content);
                    handlerCtx.setOutputValue("encoding", getEncoding(content));
                }
            }
        }
    }

    protected static String getEncoding(String xmlDoc) {
        String encoding = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlDoc.getBytes()));
            encoding = doc.getXmlEncoding();
        } catch (Exception ex) {
            Logger.getLogger(DeploymentHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (encoding == null) {
            encoding = "UTF-8";
        }
        return encoding;
    }
}
