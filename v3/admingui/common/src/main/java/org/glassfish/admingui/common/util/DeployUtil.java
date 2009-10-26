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

package org.glassfish.admingui.common.util;

import java.io.*;
import java.util.Properties;

import org.glassfish.deployment.client.DFDeploymentStatus;
import org.glassfish.deployment.client.DeploymentFacility;
import org.glassfish.deployment.client.DFProgressObject;
import org.glassfish.deployment.client.DFDeploymentProperties;

import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.net.URI;
import javax.enterprise.deploy.spi.Target;

/**
 *
 * @author anilam
 */

public class DeployUtil {
    // using DeploymentFacility API
     public static void deploy(String[] targets, Properties deploymentProps, String location,  HandlerContext handlerCtx) throws Exception {
            
        deploymentProps.setProperty(DFDeploymentProperties.UPLOAD, "false");
        boolean status = invokeDeploymentFacility(targets, deploymentProps, location, handlerCtx);
        if(status){
            //String mesg = GuiUtil.getMessage("msg.deploySuccess", new Object[] {"", "deployed"});
            //GuiUtil.prepareAlert(handlerCtx, "success", mesg, null);
        }
    }
     
     public static boolean invokeDeploymentFacility(String[] targets, Properties props, String archivePath, HandlerContext handlerCtx) 
     	throws Exception {
     	if(archivePath == null) {
            //Localize this message.
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.deploy.nullArchiveError"));
     	}
        
        if (targets == null){
            String defaultTarget =  (V3AMX.getInstance().isEE()) ? "domain" : "server";
            targets = new String[] {defaultTarget};
        }
        
        File filePath = new File(archivePath);
        URI source = filePath.toURI();
        DeploymentFacility df = GuiUtil.getDeploymentFacility();
        DFProgressObject progressObject = null;
        progressObject = df.deploy(df.createTargets(targets), source, null , props);  //null for deployment plan
        progressObject.waitFor();
        DFDeploymentStatus status = progressObject.getCompletedStatus();
        boolean ret = checkDeployStatus(status, handlerCtx, true);
     	return ret;
     }

     public static boolean checkDeployStatus(DFDeploymentStatus status, HandlerContext handlerCtx, boolean stopProcessing) 
     {
         //TODO-V3 get more msg to user.
        //parse the deployment status and retrieve failure/warning msg
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bos);
        DFDeploymentStatus.parseDeploymentStatus(status, pw);
        byte[] statusBytes = bos.toByteArray();
        String statusString = new String(statusBytes);

         if (status!=null && status.getStatus() == DFDeploymentStatus.Status.FAILURE){
            if (stopProcessing) 
                GuiUtil.handleError(handlerCtx, statusString);
            else
                GuiUtil.prepareAlert(handlerCtx,"error", GuiUtil.getMessage("msg.Error"), statusString);
                
            return false;
         }
         if (status!=null && status.getStatus() == DFDeploymentStatus.Status.WARNING){
            //We may need to log this mesg.
            GuiUtil.prepareAlert(handlerCtx, "warning", GuiUtil.getMessage("deploy.warning"),statusString);
            return false;
         }
         return true;
     }
    
    //Status of app-ref created will be the same as the app itself.
    static public void handleAppRefs(String appName, String[] targetNames, HandlerContext handlerCtx, boolean addFlag, Boolean enableFlag) {
        if (targetNames != null && targetNames.length > 0){
            DeploymentFacility df= GuiUtil.getDeploymentFacility();        
            DFProgressObject progressObject = null;
            Properties dProps = new Properties();

            if (enableFlag != null)
                dProps.setProperty(DFDeploymentProperties.ENABLED, enableFlag.toString());
            
            if (addFlag)
                progressObject = df.createAppRef(df.createTargets(targetNames), appName, dProps);
            else
                progressObject = df.deleteAppRef(df.createTargets(targetNames), appName, dProps);
            DFDeploymentStatus status = df.waitFor(progressObject);
            checkDeployStatus(status, handlerCtx, true);
        }
    }

    static public boolean restartApplication(String appName, HandlerContext handlerCtx){
        //disable application and then enable it.
        if (enableApp(appName, handlerCtx, false)){
            return enableApp(appName, handlerCtx, true);
        }
        return false;
    }


    static public boolean enableApp(String appName, HandlerContext handlerCtx, boolean enable ){
        String[] targetNames = new String[]{"server"};
        DeploymentFacility df = GuiUtil.getDeploymentFacility();
        Target[] targets = df.createTargets(targetNames);
        DFProgressObject  progressObject  = (enable) ? df.enable(targets,appName) : df.disable(targets, appName);
        progressObject.waitFor();
        DFDeploymentStatus status = progressObject.getCompletedStatus();
        boolean ret = checkDeployStatus(status, handlerCtx, false);
        return ret;
    }
 
}
