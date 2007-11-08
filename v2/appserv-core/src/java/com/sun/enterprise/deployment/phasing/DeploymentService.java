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

package com.sun.enterprise.deployment.phasing;

import java.io.File;
import java.util.Map;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.pluggable.DeploymentFactory;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.management.deploy.DeploymentCallback;

/**
 * Manages the phases and maps deployment operations to deployment phases
 * @author deployment dev team
 */
public abstract class DeploymentService {

    private static DeploymentService deployService;

    /**
     * This is a singleton factory for the DeploymentService implementation.  We should revisit
     * this to make sure this will work in multi-threaded environment, i.e. concurrent deployment.
     */
    public static DeploymentService getDeploymentService(
            ConfigContext configContext) {

        if (deployService != null) {
            return deployService;
        }

        PluggableFeatureFactory featureFactory =
            ApplicationServer.getServerContext().getPluggableFeatureFactory();
        DeploymentFactory dFactory = featureFactory.getDeploymentFactory();
        deployService = dFactory.createDeploymentService(configContext);
        return deployService;
    }

    public static DeploymentService getDeploymentService() {
        ConfigContext configContext = 
            DeploymentServiceUtils.getConfigContext();
        return DeploymentService.getDeploymentService(configContext);
    }


    /**
     * This method deploys application. Prepares the app, stores it in 
     * central repository and registers with config
     * @param req DeploymentRequest object
     */
    public abstract DeploymentStatus deploy(DeploymentRequest req) throws IASDeploymentException;
    
    /**
     * This method deploys application. Prepares the app, stores it in 
     * It constructs the DeploymentRequest using the parameters first.
     */
    public abstract DeploymentStatus deploy(File deployFile, 
        File planFile, String archiveName, String moduleID, 
        DeploymentProperties dProps, DeploymentCallback callback)
        throws IASDeploymentException;

    /**
     * This method undeploys application. Removes the application from 
     * central repository and unregisters the application from config
     * @param req DeploymentRequest object
     */   
    public abstract DeploymentStatus undeploy(DeploymentRequest req) 
        throws IASDeploymentException;
    
    /**
     * This method undeploys application. Removes the application from 
     * It constructs the DeploymentRequest using the parameters first.
     */
    public abstract DeploymentStatus undeploy(String mModuleID,
        Map mParams) throws IASDeploymentException;

    /**
     * This method is used to associate an application to a target.
     * @param req DeploymentRequest object
     */
    public abstract DeploymentStatus associate(DeploymentRequest req) 
        throws IASDeploymentException;

    /**
     * This method is used to associate an application to a target.
     * It constructs the DeploymentRequest using the parameters first.
     */
    public abstract DeploymentStatus associate(String targetName, 
        boolean enabled, String virtualServers, String referenceName) 
        throws IASDeploymentException;
    
    /**
     * This method is used to associate an application to a target.
     * It constructs the DeploymentRequest using the parameters first.
     */
    public abstract DeploymentStatus associate(String targetName, 
        String referenceName, Map options) throws IASDeploymentException;

    /**
     * This method removes references of an application on a particular target
     * @param req DeploymentRequest object
     */	 
    public abstract DeploymentStatus disassociate(DeploymentRequest req)
        throws IASDeploymentException;

   /**
     * This method removes references of an application on a particular target
     * It constructs the DeploymentRequest using the parameters first.
     */  
    public abstract DeploymentStatus disassociate(String targetName, 
        String referenceName) throws IASDeploymentException;

    public abstract DeploymentStatus disassociate(String targetName, 
        String referenceName, Map options) throws IASDeploymentException;

    public abstract DeploymentStatus start(DeploymentRequest req);
    
    public abstract DeploymentStatus stop(DeploymentRequest req)
        throws IASDeploymentException;

    public abstract DeploymentStatus start(String moduleID, String targetName, 
        Map options) throws IASDeploymentException;

    public abstract DeploymentStatus stop(String moduleID, String targetName, 
        Map options) throws IASDeploymentException;

    public abstract boolean quit(String moduleID);

    /**
     * This method finds moduleID by explicitly loading the dd for its 
     * display name.
     * This method is called by the DeployThread if and only if the client
     * did not provide a moduleID.  This is only true when a client is using 
     * JSR88.distribute with InputStream signature.
     * @@ todo: Ideally we do not want to load the deployment descriptor 
     * more than once.  This one is a necessary evil since we need the 
     * moduleID *now* before proceeding with the rest of deployment.  
     * Bigger re-construction is needed if we want to optimize deployment 
     * further for JSR88 using InputStream. 
     * NOTE that we choose to load the dd and use the display name as
     * the moduleID instead of the uploaded file name for backward
     * compatibility and clarity (uploaded file name is not descriptive).
     * @param file the deployed file
     * @return the moduleID derived from this file using the dd's display name
     */
    public abstract String getModuleIDFromDD (File file) throws Exception;
}
