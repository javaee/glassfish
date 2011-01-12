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
 * DeploymentHandler.java
 *
 */
package org.glassfish.admingui.common.handlers;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.glassfish.deployment.client.DFDeploymentStatus;
import org.glassfish.deployment.client.DeploymentFacility;
import org.glassfish.deployment.client.DFProgressObject;
import org.glassfish.deployment.client.DFDeploymentProperties;


import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.SyntaxException;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.layout.template.TemplateParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.admingui.common.util.DeployUtil;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.RestUtil;
import org.glassfish.admingui.common.util.TargetUtil;


/**
 *
 */
public class DeploymentHandler {

    @Handler(id = "deploy",
    input = {
        @HandlerInput(name = "filePath", type = String.class),
        @HandlerInput(name = "origPath", type = String.class),
        @HandlerInput(name = "allMaps", type = Map.class),
        @HandlerInput(name = "appType", type = String.class),
        @HandlerInput(name="targets", type=String[].class, required=true ),
        @HandlerInput(name = "propertyList", type = List.class)
    })
    public static void deploy(HandlerContext handlerCtx) {

        String appType = (String) handlerCtx.getInputValue("appType");
        String origPath = (String) handlerCtx.getInputValue("origPath");
        String filePath = (String) handlerCtx.getInputValue("filePath");
        Map allMaps = (Map) handlerCtx.getInputValue("allMaps");
        Map attrMap = new HashMap((Map) allMaps.get(appType));

        if (GuiUtil.isEmpty(origPath)) {
            GuiUtil.getLogger().info("deploy(): origPath is NULL");
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
        String[] targets = (String[])handlerCtx.getInputValue("targets");
        if (targets == null || targets.length==0)
            targets = null;

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
                final String name = oneRow.get("name");
                if (GuiUtil.isEmpty(name)) {
                    continue;
                }
                if (propertyNames.contains(name)) {
                    Logger logger = GuiUtil.getLogger();
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, GuiUtil.getCommonMessage("LOG_IGNORE_DUP_PROP", new Object[]{name}));
                    }
                    continue;
                } else {
                    propertyNames.add(name);
                }
                String value = oneRow.get("value");
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
            DeployUtil.deploy(targets, deploymentProps, filePath, handlerCtx);
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
        @HandlerInput(name = "convertToFalse", type = List.class, required = true),
        @HandlerInput(name = "valueMap", type = Map.class, required = true)
    })
    public static void redeploy(HandlerContext handlerCtx) {
        try {
            String filePath = (String) handlerCtx.getInputValue("filePath");
            String origPath = (String) handlerCtx.getInputValue("origPath");
            Map<String,String> deployMap = (Map) handlerCtx.getInputValue("deployMap");
            Map<String,String> valueMap = (Map) handlerCtx.getInputValue("valueMap");
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
             String ctxRoot = valueMap.get(DFDeploymentProperties.CONTEXT_ROOT);
             if (ctxRoot != null){
                 deploymentProps.setContextRoot(ctxRoot);
             }

             String availabilityEnabled = valueMap.get(DFDeploymentProperties.AVAILABILITY_ENABLED);
             if (availabilityEnabled != null){
                deploymentProps.setAvailabilityEnabled(Boolean.parseBoolean(availabilityEnabled));
             }
             String keepState = deployMap.get("keepState");
             if (keepState != null){
                deploymentProps.setProperty("keepState", keepState);
             }

             deploymentProps.setForce(true);
             deploymentProps.setUpload(false);
             deploymentProps.setName(appName);
             deploymentProps.setVerify(Boolean.parseBoolean(deployMap.get(DFDeploymentProperties.VERIFY)));
             deploymentProps.setPrecompileJSP(Boolean.parseBoolean(deployMap.get(DFDeploymentProperties.PRECOMPILE_JSP)));
             if ("osgi".equals(deployMap.get("type"))){
                 deploymentProps.setProperty("type", "osgi");
             }
             Properties props = new Properties();
             _setProps(deployMap, props, DFDeploymentProperties.DEPLOY_OPTION_JAVA_WEB_START_ENABLED);
             _setProps(deployMap, props, "preserveAppScopedResources");
             deploymentProps.setProperties(props);

             //deploy to the same target
             List<String> targetList = DeployUtil.getApplicationTarget(appName, "application-ref");
             String[] targetArray = null;
             if ( ! targetList.isEmpty()){
                 targetArray = (String[])targetList.toArray(new String[targetList.size()]);
             }
             DeployUtil.invokeDeploymentFacility( targetArray, deploymentProps, filePath, handlerCtx, "redeploy.warning");
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    private static void _setProps(Map deployMap, Properties props, String pName){
        String str = (String) deployMap.get(pName);
        if (str != null){
            props.setProperty(pName, str);
        }
    }

    /**
     *	<p> This handler takes in selected rows, and do the undeployment
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="gf.undeploy",
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

        //List errorList = new ArrayList();
        //List undeployedAppList = new ArrayList();
        List selectedRows = (List) obj;
        DFProgressObject progressObject = null;
        DeploymentFacility df = GuiUtil.getDeploymentFacility();
        //Hard coding to server, fix me for actual targets in EE.
        for (int i = 0; i < selectedRows.size(); i++) {
            boolean domainOnly = false;
            Map oneRow = (Map) selectedRows.get(i);
            String appName = (String) oneRow.get("name");
            List targets = DeployUtil.getApplicationTarget(appName, "application-ref");
            if (targets.isEmpty()){
                targets.add("domain");
                domainOnly = true;
            }

            String[] targetArray = (String[])targets.toArray(new String[targets.size()]);
            progressObject = df.undeploy(df.createTargets(targetArray), appName, dProps);
            progressObject.waitFor();
            DFDeploymentStatus status = progressObject.getCompletedStatus();
            //we DO want it to continue and call the rest handlers, ie navigate(). This will
            //re-generate the table data because there may be some apps thats been undeployed 
            //successfully.  If we stopProcessing, the table data is stale and still shows the
            //app that has been gone.
            if (DeployUtil.checkDeployStatus(status, handlerCtx, false, "undeploy.warning")) {
                if(! domainOnly){
                    removeFromDefaultWebModule(appName, targets);
                }
            }else{
                //errorList.add(appName);
                return;
            }
        }
//        if (errorList.size() > 0){
//            GuiUtil.prepareAlert("error", GuiUtil.getMessage("msg.Error"), GuiUtil.getMessage("msg.deploy.UndeployError") + " " + GuiUtil.listToString(errorList, ","));
//        }
    }


    //For any undeployed applications, we need to ensure that it is no longer specified as the
    //default web module of any VS.
    static private void  removeFromDefaultWebModule(String undeployedAppName, List<String> targets){

        String prefix = GuiUtil.getSessionValue("REST_URL")+"/configs/config/";
        Map attrsMap = new HashMap();
        attrsMap.put("defaultWebModule", "");
        for(String oneTarget:  targets){
            try{
                //find the config ref. by this target
                String endpoint = TargetUtil.getTargetEndpoint(oneTarget);
                String configName = (String) RestUtil.getEntityAttrs(endpoint, "entity").get("configRef");
                String encodedConfigName = URLEncoder.encode(configName, "UTF-8");

                //get all the VS of this config
                String vsEndpoint =  prefix + encodedConfigName + "/http-service/virtual-server";
                Map vsMap = RestUtil.getChildMap( vsEndpoint );

                //for each VS, look at the defaultWebModule
                if (vsMap != null && vsMap.size()>0){
                    List<String> vsList = new ArrayList(vsMap.keySet());
                    for(String oneVs : vsList){
                        String oneEndpoint = vsEndpoint+"/" + oneVs ;
                        String defWebModule = (String) RestUtil.getEntityAttrs( oneEndpoint , "entity").get("defaultWebModule");
                        if (GuiUtil.isEmpty(defWebModule)){
                            continue;
                        }
                        int index = defWebModule.indexOf("#");
                        if (index != -1){
                            defWebModule = defWebModule.substring(0, index);
                        }
                        if (undeployedAppName.equals(defWebModule)){
                            RestUtil.restRequest(oneEndpoint, attrsMap, "POST", null, false);
                        }
                    }
                }
            }catch(Exception ex){

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
    @Handler(id = "gf.changeAppStatus",
    input = {
        @HandlerInput(name = "selectedRows", type = List.class, required = true),
        @HandlerInput(name = "target", type = String.class, defaultValue = "server"),
        @HandlerInput(name = "enabled", type = Boolean.class, required = true)
    })
    public static void changeAppStatus(HandlerContext handlerCtx) {

        List obj = (List) handlerCtx.getInputValue("selectedRows");
        String target = (String) handlerCtx.getInputValue("target");
        boolean enabled = ((Boolean) handlerCtx.getInputValue("enabled")).booleanValue();
        DeploymentFacility df = GuiUtil.getDeploymentFacility();
        //Hard coding to server, fix me for actual targets in EE.
        List selectedRows = (List) obj;
        for (int i = 0; i < selectedRows.size(); i++) {
            Map oneRow = (Map) selectedRows.get(i);
            String appName = (String) oneRow.get("name");
            if (DeployUtil.enableApp(appName, target, handlerCtx, enabled)){
                String msg = GuiUtil.getMessage((enabled) ? "msg.enableSuccessfulPE" : "msg.disableSuccessfulPE");
                GuiUtil.prepareAlert("success", msg, null);
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
            
    @Handler(id = "gf.getDeploymentDescriptorList",
    input = {
        @HandlerInput(name = "appName", type = String.class, required = true),
        @HandlerInput(name = "data", type = List.class, required = true)},
    output = {
        @HandlerOutput(name = "descriptors", type = List.class)
    })
    public static void getDeploymentDescriptorList(HandlerContext handlerCtx) {
        String appName = (String) handlerCtx.getInputValue("appName");
        List<Map<String, Object>> ddList = (List) handlerCtx.getInputValue("data");
        List result = new ArrayList();
        if ((ddList != null) && ddList.size() > 0) {
            for (Map<String, Object> oneDD : ddList) {
                HashMap oneRow = new HashMap();
                Map<String, String> props = (Map) oneDD.get("properties");
                final String mName = props.get(MODULE_NAME_KEY);
                oneRow.put("moduleName", (mName==null)?  "" : mName);
                final String ddPath = props.get(DD_PATH_KEY);
                oneRow.put("ddPath", (ddPath==null)? "" : ddPath);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("descriptors", result);
    }

    @Handler(id = "gf.getDeploymentDescriptor",
    input = {
        @HandlerInput(name = "appName", type = String.class, required = true),
        @HandlerInput(name = "moduleName", type = String.class, required = true),
        @HandlerInput(name = "descriptorName", type = String.class, required = true),
        @HandlerInput(name = "data", type = List.class, required = true)
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
        List<Map<String, Object>> ddList = (List) handlerCtx.getInputValue("data");
        if (ddList.size() > 0) {
            for (Map<String, Object> oneDD : ddList) {
                Map<String, String> props = (Map) oneDD.get("properties");
                if (moduleName.equals(props.get(MODULE_NAME_KEY)) && descriptorName.equals(props.get(DD_PATH_KEY))) {
                    final String ddContent = props.get(DD_CONTENT_KEY);
                    String content = (ddContent==null)? "" : ddContent;
                    handlerCtx.setOutputValue("content", content);
                    handlerCtx.setOutputValue("encoding", getEncoding(content));
                }
            }
        }
    }

    private static String getEncoding(String xmlDoc) {
	String encoding = null;
	TemplateParser parser = new TemplateParser(new ByteArrayInputStream(xmlDoc.getBytes()));
	try {
	    parser.open();
	    encoding = parser.readUntil("encoding", false);
	    if (encoding.endsWith("encoding")) {
		// Read encoding="..."
		parser.readUntil('=', false);
		encoding = (String) parser.getNVP("encoding").getValue();
	    } else {
		// Not found...
		encoding = null;
	    }
	} catch (SyntaxException ex) {
	    encoding = null;
	} catch (IOException ex) {
	    encoding = null;
	}

        if (encoding == null) {
            encoding = "UTF-8";
        }
        return encoding;
    }

    private static final String MODULE_NAME_KEY = "module-name";
    private static final String DD_PATH_KEY = "dd-path";
    private static final String DD_CONTENT_KEY = "dd-content";

}
