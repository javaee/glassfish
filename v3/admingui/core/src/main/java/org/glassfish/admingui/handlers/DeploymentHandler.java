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
 * DeploymentHandler.java
 *
 */

package org.glassfish.admingui.handlers;

import com.sun.appserv.management.config.ApplicationConfig;
import com.sun.appserv.management.ext.runtime.RuntimeMgr;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//TODO-V3
//import com.sun.enterprise.connectors.ConnectorRuntime;

import org.glassfish.deployment.client.DFDeploymentStatus;
import org.glassfish.deployment.client.DeploymentFacility;
import org.glassfish.deployment.client.DFProgressObject;
import org.glassfish.deployment.client.DFDeploymentProperties;


import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;


import com.sun.webui.jsf.model.UploadedFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.glassfish.admingui.common.util.DeployUtil;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;
import org.glassfish.admingui.common.util.TargetUtil;

/**
 *
 */
public class DeploymentHandler {

    //should be the same as in DeploymentProperties in deployment/common
    public static final String KEEP_SESSIONS = "keepSessions";

        /**
     *	<p> This method deploys the uploaded file </p>
     *      to a give directory</p>
     *	<p> Input value: "file" -- Type: <code>com.sun.webui.jsf.model.UploadedFile</code></p>
     *	<p> Input value: "appName" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "appType" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "ctxtRoot" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "VS" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "enabled" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "verifier" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "jws" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "precompileJSP" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "libraries" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "description" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "rmistubs" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "threadpool" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "registryType" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "deploy", input = {
        @HandlerInput(name = "filePath", type = String.class),
        @HandlerInput(name = "origPath", type = String.class),
        @HandlerInput(name = "appName", type = String.class),
        @HandlerInput(name = "ctxtRoot", type = String.class),
        @HandlerInput(name = "VS", type = String.class),
        @HandlerInput(name = "enabled", type = String.class),
        @HandlerInput(name = "precompileJSP", type = String.class),
        @HandlerInput(name = "libraries", type = String.class),
        @HandlerInput(name = "description", type = String.class),
        @HandlerInput(name="targets", type=String[].class )
        })
    public static void deploy(HandlerContext handlerCtx) {

        Properties deploymentProps = new Properties();
        String appName = (String) handlerCtx.getInputValue("appName");
        String origPath = (String) handlerCtx.getInputValue("origPath");
        String filePath = (String) handlerCtx.getInputValue("filePath");
        String ctxtRoot = (String) handlerCtx.getInputValue("ctxtRoot");
        String[] vs = (String[]) handlerCtx.getInputValue("VS");
        String enabled = (String) handlerCtx.getInputValue("enabled");
        
        String libraries = (String) handlerCtx.getInputValue("libraries");
        String precompile = (String) handlerCtx.getInputValue("precompileJSP");
        String desc = (String) handlerCtx.getInputValue("description");
        String[] targets = (String[]) handlerCtx.getInputValue("targets");
        if (targets == null || targets.length == 0 || !AMXRoot.getInstance().isEE()) {
            targets = null;
        }
        if (GuiUtil.isEmpty(origPath)) {
            String mesg = GuiUtil.getMessage("msg.deploy.nullArchiveError");
            GuiUtil.handleError(handlerCtx, mesg);
            return;
        }

        deploymentProps.setProperty(DFDeploymentProperties.NAME, appName != null ? appName : "");
        deploymentProps.setProperty(DFDeploymentProperties.CONTEXT_ROOT, ctxtRoot != null ? ctxtRoot : "");
        deploymentProps.setProperty(DFDeploymentProperties.ENABLED, enabled != null ? enabled : "false");
        deploymentProps.setProperty(DFDeploymentProperties.DEPLOY_OPTION_LIBRARIES, libraries != null ? libraries : "");
        deploymentProps.setProperty(DFDeploymentProperties.DESCRIPTION, desc != null ? desc : "");
        deploymentProps.setProperty(DFDeploymentProperties.PRECOMPILE_JSP, precompile != null ? precompile : "false");
        //do not send VS if user didn't specify, refer to bug#6542276
        if (vs != null && vs.length > 0) {
            if (!GuiUtil.isEmpty(vs[0])) {
                String vsTargets = GuiUtil.arrayToString(vs, ",");
                deploymentProps.setProperty(DFDeploymentProperties.VIRTUAL_SERVERS, vsTargets);
            }
        }
        try {
            DeployUtil.deploy(targets, deploymentProps, filePath, handlerCtx);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This method uploads a file temp directory</p>
     *	<p> Input value: "file" -- Type: <code>com.sun.webui.jsf.model.UploadedFile</code></p>
     *	<p> Output value: "uploadDir" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "uploadFileToTempDir", 
    input = {
        @HandlerInput(name = "file", type = UploadedFile.class)},
    output = {
        @HandlerOutput(name = "origPath", type = String.class),
        @HandlerOutput(name = "uploadedTempFile", type = String.class)
    })
    public static void uploadFileToTempDir(HandlerContext handlerCtx) {
        UploadedFile uploadedFile = (UploadedFile) handlerCtx.getInputValue("file");
        File tmpFile = null;
        String uploadTmpFile = "";
        if (uploadedFile != null) {
            String name = uploadedFile.getOriginalName();
            //see bug# 6498910, for IE, getOriginalName() returns the full path, including the drive.
            //for any other browser, it just returns the file name.
            int lastIndex = name.lastIndexOf("\\");
            if (lastIndex != -1) {
                name = name.substring(lastIndex + 1, name.length());
            }
            int index = name.indexOf(".");
            if (index <= 0) {
                String mesg = GuiUtil.getMessage("msg.deploy.nullArchiveError");
                GuiUtil.handleError(handlerCtx, mesg);
                return;
            }
            String suffix = name.substring(index);
            String prefix = name.substring(0, index);
            handlerCtx.setOutputValue("origPath", prefix);
            try {
                //createTempFile requires min. of 3 char for prefix.
                if (prefix.length() <= 2) {
                    prefix = prefix + new Random().nextInt(100000);
                }
                tmpFile = File.createTempFile(prefix, suffix);
                uploadedFile.write(tmpFile);
                uploadTmpFile = tmpFile.getCanonicalPath();
            } catch (IOException ioex) {
                try {
                    uploadTmpFile = tmpFile.getAbsolutePath();
                } catch (Exception ex) {
                //Handle AbsolutePathException here
                }
            } catch (Exception ex) {
                GuiUtil.handleException(handlerCtx, ex);
            }
        }
        handlerCtx.setOutputValue("uploadedTempFile", uploadTmpFile);
    }
    
    /**
     *  <p> This handler redeploy any application
     */
     @Handler(id="redeploy",
        input={
        @HandlerInput(name="filePath", type=String.class, required=true),
        @HandlerInput(name="origPath", type=String.class, required=true),
        @HandlerInput(name="appName", type=String.class, required=true),
        @HandlerInput(name="keepSessions", type=Boolean.class)})
        
    public static void redeploy(HandlerContext handlerCtx) {
         try{
             String filePath = (String) handlerCtx.getInputValue("filePath");
             String origPath = (String) handlerCtx.getInputValue("origPath");
             String appName = (String) handlerCtx.getInputValue("appName");
             Boolean keepSessions = (Boolean) handlerCtx.getInputValue("keepSessions");
             DFDeploymentProperties deploymentProps = new DFDeploymentProperties();
             //If we are redeploying a web app, we want to preserve context root.
             ApplicationConfig appConfig = AMXUtil.getApplicationConfigByName(appName);
             if (appConfig != null){
                 deploymentProps.setContextRoot(appConfig.getContextRoot());
             }
             deploymentProps.setForce(true);
             deploymentProps.setUpload(false);
             deploymentProps.setName(appName);
             
             Properties prop = new Properties();
             String ks = (keepSessions==null) ? "false" : keepSessions.toString();
             prop.setProperty(KEEP_SESSIONS, ks);
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
     *	@param	context	The HandlerContext.
     */
    @Handler(id="undeploy",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="appType", type=String.class, required=true)})
        
    public static void undeploy(HandlerContext handlerCtx) {
        
        Object obj = handlerCtx.getInputValue("selectedRows");

        //appType can be one of the following: application,webApp,ejbModule,connector,appClient
        String appType = (String)handlerCtx.getInputValue("appType");
        Properties dProps = new Properties();;

        if(appType.equals("connector")) {
                //Default cascade is true. May be we can issue a warning,
                //bcz undeploy will fail anyway if cascade is false.
                dProps.put(DFDeploymentProperties.CASCADE, "true");
        }
        
        List selectedRows = (List) obj;
        DFProgressObject progressObject = null;
        DeploymentFacility df= GuiUtil.getDeploymentFacility();
        //Hard coding to server, fix me for actual targets in EE.
        String[] targetNames = new String[] {"server"};
        
        for(int i=0; i< selectedRows.size(); i++){
            Map oneRow = (Map) selectedRows.get(i);
            String appName = (String) oneRow.get("name");
            //Undeploy the app here.
            if(AMXRoot.getInstance().isEE()){
                List<String> refList = TargetUtil.getDeployedTargets(appName, true);
                if(refList.size() > 0)
                    targetNames = refList.toArray(new String[refList.size()]);
                else
                    targetNames=new String[]{"domain"};
            }
            progressObject = df.undeploy(df.createTargets(targetNames), appName, dProps);
            
            progressObject.waitFor();
            DFDeploymentStatus status = progressObject.getCompletedStatus();
            //we DO want it to continue and call the rest handlers, ie navigate(). This will
            //re-generate the table data because there may be some apps thats been undeployed 
            //successfully.  If we stopProcessing, the table data is stale and still shows the
            //app that has been gone.
            if( DeployUtil.checkDeployStatus(status, handlerCtx, false)){
                String mesg = GuiUtil.getMessage("msg.deploySuccess", new Object[]{appName, "undeployed"});
                //we need to fix cases where more than 1 app is undeployed before putting this msg.                
                //GuiUtil.prepareAlert(handlerCtx, "success", mesg, null); 
            }
        }
    }
    
    
        
    /**
     *	<p> This handler takes in selected rows, and change the status of the app
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *  <p> Input  value: "appType" -- Type: <code>String</code></p>
     *  <p> Input  value: "enabled" -- Type: <code>Boolean</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="changeAppStatus",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true),
        @HandlerInput(name="appType", type=String.class, required=true),
        @HandlerInput(name="enabled", type=Boolean.class, required=true)})
        
    public static void changeAppStatus(HandlerContext handlerCtx) {
        
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        boolean enabled = ((Boolean)handlerCtx.getInputValue("enabled")).booleanValue();
       
        DeploymentFacility df= GuiUtil.getDeploymentFacility();
        //Hard coding to server, fix me for actual targets in EE.
        String[] targetNames = new String[] {"server"};
        List selectedRows = (List) obj;
        try{
            for(int i=0; i< selectedRows.size(); i++){
                Map oneRow = (Map) selectedRows.get(i);
                String appName = (String) oneRow.get("name");
                
                // In V3, use DF to do disable or enable
                if (enabled)
                    df.enable(df.createTargets(targetNames), appName);
                else
                    df.disable(df.createTargets(targetNames), appName); 
                
                if (AMXRoot.getInstance().isEE()){
                    String msg = GuiUtil.getMessage((enabled)? "msg.enableSuccessful" : "msg.disableSuccessful");
                    GuiUtil.prepareAlert(handlerCtx, "success", msg, null);
                }else{
                    String msg = GuiUtil.getMessage((enabled)? "msg.enableSuccessfulPE" : "msg.disableSuccessfulPE");
                    GuiUtil.prepareAlert(handlerCtx, "success", msg, null);
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This method returns the deployment descriptors for a given app. </p>
     *
     *  <p> Output value: "descriptors" -- Type: <code>java.util.List</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getDescriptors",
    input={
        @HandlerInput(name="appName", type=String.class, required=true),
        @HandlerInput(name="includeSubComponent", type=Boolean.class)},
    output={
        @HandlerOutput(name="descriptors", type=List.class)})
        public static void getDescriptors(HandlerContext handlerCtx) {
            String appName = (String)handlerCtx.getInputValue("appName");
            List list = new ArrayList();
            RuntimeMgr runtimeMgr = AMXRoot.getInstance().getRuntimeMgr();
            Map<String,String> descriptors = runtimeMgr.getDeploymentConfigurations(appName);
            try{
                for(String dd : descriptors.keySet()){ 
                    HashMap map = new HashMap();
                    map.put("name", appName);
                    map.put("moduleName", "");
                    int index = dd.lastIndexOf(File.separator)+1;
                    map.put("descriptor", dd.substring(index));
                    map.put("descriptorPath", dd);
                    list.add(map);
                }
            }catch(Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
            handlerCtx.setOutputValue("descriptors", list);
    }   
    
     

//    /**
//     *	<p> This method returns the resource-adapter properties </p>
//     *
//     *  <p> input value: "adapterProperties" -- Type: <code>java.util.Map</code>/</p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="createResourceAdapterConfig",
//    input={
//        @HandlerInput(name="dProps", type=Properties.class),
//        @HandlerInput(name="AddProps",    type=Map.class)},
//    output={
//        @HandlerOutput(name="nextPage", type=String.class)}
//    )
//    public static void createResourceAdapterConfig(HandlerContext handlerCtx) {
//        Properties dProps = (Properties)handlerCtx.getInputValue("dProps");
//
//        String name = dProps.getProperty("name");
//        ResourceAdapterConfig ra = AMXRoot.getInstance().getResourcesConfig().createResourceAdapterConfig(name, null);
//
//        String threadPool = dProps.getProperty("threadPool");
//        if(!GuiUtil.isEmpty(threadPool))
//            ra.setThreadPoolIDs(threadPool);
//
//        String registry = dProps.getProperty("registry");
//        if (!GuiUtil.isEmpty(registry)){
//            ra.createPropertyConfig(registry, "true");
//        }
//        
//        Map<String,String> addProps = (Map)handlerCtx.getInputValue("AddProps");
//        if(addProps != null ){
//             for(String key: addProps.keySet()){
//                 String value = addProps.get(key);
//                 if (!GuiUtil.isEmpty(value))
//                    ra.createPropertyConfig(key,value);
//             }  
//         }
//        handlerCtx.setOutputValue("nextPage", "applications/connectorModules.jsf");
//    }
//    
//    


    
//    /**
//     *	<p> This handler creates references for the given application/module name 
//     *
//     *  <p> Input value: "name" -- Type: <code>String</code>/</p>
//     *  <p> Input value: "targets" -- Type: <code>String[]</code>/</p>
//     *  <p> Output value: "name" -- Type: <code>String</code>/</p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="createApplicationReferences",
//        input={
//        @HandlerInput(name="name", type=String.class, required=true),
//        @HandlerInput(name="targets", type=String[].class, required=true )})
//    public static void createApplicationReferences(HandlerContext handlerCtx) {
//        String name = (String)handlerCtx.getInputValue("name");
//        String[] selTargets = (String[])handlerCtx.getInputValue("targets");
//        List<String> targets = Arrays.asList(selTargets);
//        List<String> associatedTargets = TargetUtil.getDeployedTargets(name, true);
//        try{
//            List addTargets = new ArrayList();
//            for(String targetName:targets) {
//                if(!(associatedTargets.contains(targetName))) {
//                       addTargets.add(targetName);
//                }
//            }
//            DeployUtil.handleAppRefs(name, (String[])addTargets.toArray(new String[addTargets.size()]), handlerCtx, true, null);
//            
//            //removes the old application references
//            List removeTargets = new ArrayList();
//            for(String targetName:associatedTargets) {
//                if(!(targets.contains(targetName))) {
//                    removeTargets.add(targetName);
//                }
//            }
//            DeployUtil.handleAppRefs(name, (String[])removeTargets.toArray(new String[removeTargets.size()]), handlerCtx, false, null);
//            
//        }catch(Exception ex){
//            GuiUtil.handleException(handlerCtx, ex);
//        }
//    }

    
//    /**
//     *	<p> This method displays the deployment descriptors for a given app. </p>
//     *
//     *  <p> Output value: "descriptors" -- Type: <code>String.class</code>/</p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="descriptorDisplay",
//    input={
//        @HandlerInput(name="filePath", type=String.class),
//        @HandlerInput(name="appName", type=String.class),
//        @HandlerInput(name="pageName", type=String.class)},
//    output={
//        @HandlerOutput(name="descriptor", type=String.class),
//        @HandlerOutput(name="appName", type=String.class),
//        @HandlerOutput(name="pageName", type=String.class)})
//
//        public static void descriptorDisplay(HandlerContext handlerCtx) {
//            String filePath = (String)handlerCtx.getInputValue("filePath");
//            String appName = (String)handlerCtx.getInputValue("appName");
//            String pageName = (String)handlerCtx.getInputValue("pageName");
//            String objectName = "com.sun.appserv:type=applications,category=config";
//            String methodName = "getDeploymentDescriptor";
//            Object[] params = {filePath};
//            String[] types = {"java.lang.String"};
//
//            //TODO-V3
//            //String descriptor=(String)JMXUtil.invoke(objectName, methodName, params, types);
//            String descriptor ="";
//
//            handlerCtx.setOutputValue("descriptor", descriptor);
//            handlerCtx.setOutputValue("appName", appName);
//            handlerCtx.setOutputValue("pageName", pageName);
//        }

    
//    /**
//     *	<p> This method returns the resource-adapter properties </p>
//     *
//     *  <p> Output value: "adapterProperties" -- Type: <code>java.util.List</code>/</p>
//     *	@param	context	The HandlerContext.
//     */
//    @Handler(id="getAdapterProperties",
//    input={
//        @HandlerInput(name="dProps", type=Properties.class)},
//    output={
//        @HandlerOutput(name="properties", type=java.util.Map.class),
//        @HandlerOutput(name="dProps", type=Properties.class)})
//
//        public static void getAdapterProperties(HandlerContext handlerCtx) {
//
//            Properties dProps = (Properties)handlerCtx.getInputValue("dProps");
//            String filePath = dProps.getProperty("filePath");
//            Map props = new HashMap();
//            try{
//                //TODO-V3
//                //props = ConnectorRuntime.getRuntime().getResourceAdapterBeanProperties(filePath);
//                clearValues(props);
//            }catch(Exception ex){
//                //TODO: Log exception,  for now just ignore and return empty properties list
//            }
//            handlerCtx.setOutputValue("properties", props);
//            handlerCtx.setOutputValue("dProps", dProps);
//    }    

//	private static void clearValues(Map map) {
//            //refer to bugid 6212118
//            //The value returned by connector runtime is the type for that property, we want to 
//            //empty it out so that user can fill in the value 
//
//            Set<String> keySet = map.keySet();
//            for(String key: keySet) {
//                    map.put(key, "");
//            }
//	}
}
