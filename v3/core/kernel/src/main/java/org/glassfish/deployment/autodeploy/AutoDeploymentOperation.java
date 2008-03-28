/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.deployment.autodeploy;

import com.sun.enterprise.v3.admin.CommandRunner;
import com.sun.enterprise.v3.deployment.DeployCommand;
import java.io.File;
import java.util.Properties;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.deployment.autodeploy.AutoDeployer.AutodeploymentStatus;
import org.glassfish.deployment.common.DeploymentProperties;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;

/**
 * Performs a single auto-deployment operation for a single file.
 * <p>
 * Note - Use the newInstance static method to obtain a fully-injected operation;
 * it is safer and more convenient than using the no-arg constructor and then 
 * invoking init yourself.
 * 
 * @author tjquinn
 */
@Service
@Scoped(PerLookup.class)
public class AutoDeploymentOperation extends AutoOperation {

    /**
     * Creates a fully-injected, ready-to-use AutoDeploymentOperation object.
     * @param habitat
     * @param renameOnSuccess
     * @param file
     * @param enabled
     * @param virtualServer
     * @param forceDeploy
     * @param verify
     * @param preJspCompilation
     * @param target
     * @return the injected, initialized AutoDeploymentOperation
     */
    static AutoDeploymentOperation newInstance(
            Habitat habitat,
            boolean renameOnSuccess,
            File file, 
            boolean enabled,
            String virtualServer,
            boolean forceDeploy,
            boolean verify,
            boolean preJspCompilation,
            String target) {
        
        AutoDeploymentOperation o = 
                (AutoDeploymentOperation) habitat.getComponent(AutoDeploymentOperation.class);
        
        o.init(renameOnSuccess, file, enabled, virtualServer, forceDeploy, verify, preJspCompilation, target);
        return o;
    }  
    
    private boolean renameOnSuccess;
    
    private static final String COMMAND_NAME = "deploy";
    
    @Inject(name=COMMAND_NAME)
    private DeployCommand deployCommand;
    
    @Inject
    private AutodeployRetryManager retryManager;
    
    /**
     * Completes the initialization of the object.
     * @param renameOnSuccess
     * @param file
     * @param enabled
     * @param virtualServer
     * @param forceDeploy
     * @param verify
     * @param preJspCompilation
     * @param target
     * @return the object itself for convenience
     */
    protected AutoDeploymentOperation init (
            boolean renameOnSuccess,
            File file, 
            boolean enabled,
            String virtualServer,
            boolean forceDeploy,
            boolean verify,
            boolean preJspCompilation,
            String target) {
        
        super.init(file, getDeployActionProperties(
                        file, 
                        enabled, 
                        virtualServer, 
                        forceDeploy,
                        verify,
                        preJspCompilation,
                        target),
                        COMMAND_NAME,
                        deployCommand);
        this.renameOnSuccess = renameOnSuccess;
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    protected String getMessageString(AutodeploymentStatus ds, File file) {
        return localStrings.getLocalString(
                ds.deploymentMessageKey, 
                ds.deploymentDefaultMessage, 
                file);
    }

    /**
     * {@inheritDoc}
     */
    protected void markFiles(AutodeploymentStatus ds, File file) {
        /*
         * One reason an auto-deployment may fail is if the auto-deployed
         * file is a directory and the directory's contents were not yet
         * complete when the autodeployer detected a change in the top-level
         * directory file's timestamp.  Retry a failed autodeploy of a
         * directory for the prescribed retry period or until it succeeds.
         */
        if (ds == AutodeploymentStatus.FAILURE) {
            if (file.isDirectory()) {
                try {
                    retryManager.recordFailedDeployment(file);
                } catch (AutoDeploymentException ex) {
                    /*
                     * The retry manager has concluded that this most recent
                     * failure should be the last one. 
                     */
                    markDeployFailed(file);
                }
            } else {
                retryManager.endMonitoring(file);
                markDeployFailed(file);
            }
        } else {
            retryManager.recordSuccessfulDeployment(file);
            if (ds.status) {
                if (renameOnSuccess) {
                    // XXX remove file from retry mgr
                    markDeployed(file);
                }
            } else {
                markDeployFailed(file);
            }
        }
    }
    
    // Methods for creating operation status file(s)
    private void markDeployed(File f) {
        try {
            deleteAllMarks(f);
            getDeployedFile(f).createNewFile();
        } catch (Exception e) { 
            //ignore 
        }
    }
    
    private void markDeployFailed(File f) {
        try {
            deleteAllMarks(f);
            getDeployFailedFile(f).createNewFile();
        } catch (Exception e) { 
            //ignore 
        }
    }
    
    private static Properties getDeployActionProperties(
            File deployablefile,
            boolean enabled,
            String virtualServer,
            boolean forceDeploy,
            boolean verify,
            boolean jspPreCompilation,
            String target){
        
        DeploymentProperties dProps = new DeploymentProperties();
        dProps.setPath(deployablefile.getAbsolutePath());
//        dProps.setUpload(false);
        dProps.setEnabled(enabled);
        if (virtualServer != null) {
            dProps.setVirtualServers(virtualServer);
        }
        dProps.setForce(forceDeploy);
        dProps.setVerify(verify);
        dProps.setPrecompileJSP(jspPreCompilation);
//        dProps.setResourceAction(DeploymentProperties.RES_DEPLOYMENT);
//        dProps.setResourceTargetList(target);
    
        return (Properties)dProps;
    }
}
