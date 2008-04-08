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

package org.glassfish.admingui.handlers;

import java.util.Properties;
import java.util.Random;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import com.sun.webui.jsf.model.UploadedFile;

import java.io.File;
import java.io.IOException;

import org.glassfish.admingui.util.GuiUtil;
import org.glassfish.admingui.util.AMXRoot;

import static org.glassfish.deployment.client.DFDeploymentProperties.*;

public class FileUploadHandler {
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
    @Handler(id="deploy",
    input={
        @HandlerInput(name="filePath",     type=String.class),
        @HandlerInput(name="origPath",     type=String.class),
	@HandlerInput(name="appName", type=String.class),
	@HandlerInput(name="appType", type=String.class),
	@HandlerInput(name="ctxtRoot", type=String.class),
	@HandlerInput(name="VS", type=String.class),
	@HandlerInput(name="enabled", type=String.class),
        @HandlerInput(name="availability", type=String.class),
	@HandlerInput(name="verifier", type=String.class),
	@HandlerInput(name="jws", type=String.class),
	@HandlerInput(name="precompileJSP", type=String.class),
	@HandlerInput(name="libraries", type=String.class),
        @HandlerInput(name="description", type=String.class),
        @HandlerInput(name="listPageLink", type=String.class),
        //specific to RAR
	@HandlerInput(name="threadpool", type=String.class),
	@HandlerInput(name="registryType", type=String.class),
        @HandlerInput(name="cancelPage", type=String.class),
        @HandlerInput(name="target", type=String.class),

        @HandlerInput(name="targets", type=String[].class, required=true ),
	@HandlerInput(name="rmistubs", type=String.class)},
	output={
		@HandlerOutput(name="nextPage", type=String.class),
		@HandlerOutput(name="rarProps", type=Properties.class)})
    public static void  deploy(HandlerContext handlerCtx) {
        
        Properties deploymentProps = new Properties();
	String appName = (String)handlerCtx.getInputValue("appName");
        String origPath = (String)handlerCtx.getInputValue("origPath");   
        String filePath = (String)handlerCtx.getInputValue("filePath");  
	String ctxtRoot = (String)handlerCtx.getInputValue("ctxtRoot");
	String[] vs = (String[])handlerCtx.getInputValue("VS");
	String enabled = (String)handlerCtx.getInputValue("enabled");
	String verifier = (String)handlerCtx.getInputValue("verifier");
	String libraries = (String)handlerCtx.getInputValue("libraries");
	String precompile = (String)handlerCtx.getInputValue("precompileJSP");
	String desc = (String)handlerCtx.getInputValue("description");
	String rmis = (String)handlerCtx.getInputValue("rmistubs");
	String jws = (String)handlerCtx.getInputValue("jws");
        String availability = (String)handlerCtx.getInputValue("availability");
	String appType = (String)handlerCtx.getInputValue("appType");
        String[] targets = (String[])handlerCtx.getInputValue("targets");
        if (targets == null || targets.length==0 || !AMXRoot.getInstance().isEE())
            targets = null;
        
                
        if(GuiUtil.isEmpty(origPath)) {
            String mesg = GuiUtil.getMessage("msg.deploy.nullArchiveError");
            GuiUtil.handleError(handlerCtx, mesg);
            return;
	}            

        //in V3, doesn't seem to need archiveName.  it actually will give out error
	//deploymentProps.setProperty(ARCHIVE_NAME, origPath);
	deploymentProps.setProperty(NAME, appName != null ? appName : "");
	deploymentProps.setProperty(CONTEXT_ROOT, ctxtRoot != null ? ctxtRoot : "");
	deploymentProps.setProperty(ENABLED, enabled != null ? enabled : "false");
	//deploymentProps.setProperty(VERIFY, verifier != null ? verifier : "false");
	deploymentProps.setProperty(DEPLOY_OPTION_LIBRARIES, libraries != null ? libraries: "");
	//deploymentProps.setProperty(DESCRIPTION, desc != null ? desc : "");
	deploymentProps.setProperty(PRECOMPILE_JSP, precompile != null ? precompile : "false");
	//do not send VS if user didn't specify, refer to bug#6542276
        if (vs != null && vs.length > 0) {
		if(!GuiUtil.isEmpty(vs[0])) {
			String vsTargets = GuiUtil.arrayToString(vs, ",");
			deploymentProps.setProperty(VIRTUAL_SERVERS, vsTargets);
		}
	}
	//deploymentProps.setProperty(GENERATE_RMI_STUBS, rmis != null ? rmis : "false");
	//deploymentProps.setProperty(DEPLOY_OPTION_JAVA_WEB_START_ENABLED, jws != null ? jws : "false");
        //if(AMXRoot.getInstance().isEE())
        //    deploymentProps.setProperty(AVAILABILITY_ENABLED, availability != null ? availability : "false");
	deploymentProps.setProperty("appType", appType != null ? appType : "");
        try{
            DeploymentHandler.deploy(targets, deploymentProps, filePath, handlerCtx);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    
    /**
     *	<p> This method uploads a file temp directory</p>
     *	<p> Input value: "file" -- Type: <code>com.sun.webui.jsf.model.UploadedFile</code></p>
     *	<p> Output value: "uploadDir" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="uploadFileToTempDir",
        input={
            @HandlerInput(name="file", type=UploadedFile.class)},
	output={
            @HandlerOutput(name="origPath", type=String.class),
            @HandlerOutput(name="uploadedTempFile", type=String.class)})
    public static void uploadFileToTempDir(HandlerContext handlerCtx) {
	UploadedFile uploadedFile = (UploadedFile)handlerCtx.getInputValue("file");
	File tmpFile = null;
        String uploadTmpFile = "";
	if(uploadedFile != null) {
            String name = uploadedFile.getOriginalName();
            //see bug# 6498910, for IE, getOriginalName() returns the full path, including the drive.
            //for any other browser, it just returns the file name.
            int lastIndex = name.lastIndexOf("\\");
            if (lastIndex != -1){
                name = name.substring(lastIndex+1, name.length());
            }
			int index = name.indexOf(".");
			if(index <= 0) {
				String mesg = GuiUtil.getMessage("msg.deploy.nullArchiveError");
                GuiUtil.handleError(handlerCtx, mesg);
				return;
			}
            String suffix = name.substring(index);
            String prefix = name.substring(0, index);
            handlerCtx.setOutputValue("origPath", prefix);
            try {
                //createTempFile requires min. of 3 char for prefix.
                if (prefix.length() <=2)
                    prefix= prefix + new Random().nextInt(100000);
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


}
