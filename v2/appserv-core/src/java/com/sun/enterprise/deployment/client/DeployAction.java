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

package com.sun.enterprise.deployment.client;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentProgressImpl;
import com.sun.appserv.management.deploy.DeploymentSourceImpl;
import com.sun.appserv.management.deploy.DeploymentSupport;
import com.sun.enterprise.deployapi.ProgressObjectImpl;
import com.sun.enterprise.deployapi.SunTarget;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.deploy.shared.Archive;
import com.sun.enterprise.deployment.deploy.shared.MemoryMappedArchive;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.util.FileUploadUtil;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.management.Notification;

public final class DeployAction extends ProgressObjectImpl {

    private DeploymentMgr deplMgr = null;
    private static final int SLEEP_TIME = 100;
    private static final long TIMEOUT_LOOPS = 1000000000 / SLEEP_TIME;

    // if this system property is set, it will use jmx upload mechanism
    // with the specified chunk size
    private static String JMX_UPLOAD_CHUNK_SIZE = "jmx.upload.chunk.size";
    // if this system property is set, it will use jmx upload mechanism
    private static String HTTP_PROXYHOST = "http.proxyHost";

    private final String jmxUploadChunkSizeProp = 
        System.getProperty(JMX_UPLOAD_CHUNK_SIZE);

    private final String httpProxyHostProp = System.getProperty(HTTP_PROXYHOST);

    private static StringManager localStrings = StringManager.getManager(DeployAction.class);
    private static final Logger _logger = 
        LogDomains.getLogger(LogDomains.DPL_LOGGER);
    
    public DeployAction(SunTarget[] targets) {
        super(targets);
    }

    /*
     * Uploads an archive using deploymentMgrMBean
     */
    private Object uploadArchive(Archive module) throws IOException {

        long totalSize = module.getArchiveSize();
        int chunkSize = 32 * 1024;
        if (jmxUploadChunkSizeProp != null && 
            jmxUploadChunkSizeProp.length() > 0) {
            chunkSize = Integer.parseInt(jmxUploadChunkSizeProp);
        }
        Object uploadID = null;
        long remaining = totalSize;
        BufferedInputStream bis = null;
        try {
            String name = getArchiveName(module);
            if (module instanceof MemoryMappedArchive) {
                //i.e. module.getArchiveUri() == null
                byte[] bytes = ((MemoryMappedArchive) module).getByteArray();
                bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
            } else {
                bis = new BufferedInputStream(new FileInputStream(new File(module.getURI().getPath())));
            }
            uploadID = deplMgr.initiateFileUpload(name, totalSize);
            while(remaining != 0) {
                int actual = (remaining < chunkSize) ? (int) remaining : chunkSize;
                byte[] bytes = new byte[actual];
                try {
                    bis.read(bytes);
                } catch (EOFException eof) {
                    break;
                } 
                _logger.log(Level.FINE, "Uploading one chunk...");
                deplMgr.uploadBytes(uploadID, bytes);
                remaining -= actual;
            }
        } finally {
            if(bis!=null) {
                bis.close();
            }
        }
        return uploadID;
    }

    // upload an archive using UploadServlet 
    private String uploadArchiveOverHTTP(ServerConnectionIdentifier serverId, 
        Archive module) throws Exception {
        return FileUploadUtil.uploadToServlet(serverId.getHostName(),
            Integer.toString(serverId.getHostPort()), serverId.getUserName(), 
            serverId.getPassword(), module);
    }

    public void run() {
        ConnectionSource dasConnection= (ConnectionSource) args[0];
        Archive deployArchive = (Archive) args[1];
        Archive deployPlan = (Archive) args[2];
        Map deployOptions = (Map) args[3];
        SunTarget[] targetList = (SunTarget[]) args[4];
        SunTarget domain = (SunTarget) args[5];
        boolean isLocalConnectionSource = ((Boolean) args[6]).booleanValue();
        ServerConnectionIdentifier serverId = 
            (ServerConnectionIdentifier) args[7];
        Object archiveUploadID = null;
        Object planUploadID = null;
        Map deployedTargets = null;
        Object deployActionID = null;
        boolean isDirectoryDeploy = false;
        boolean isRedeploy = false;

        //Make sure the file permission is correct when deploying a file
        //Note that if using JSR88 deploying from InputStream, the
        //deployArchive.getArchiveUri() would be null, and not directory
        //deploy
        if (deployArchive == null) {
            setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.archive_not_specified"), domain);
            return;                            
        }
        if (deployArchive.getURI() != null) {
            
            File tmpFile = new File(deployArchive.getURI().getPath());
            if(!tmpFile.exists()) {
                setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.archive_not_in_location"), domain);
                return;                
            }
            if(!tmpFile.canRead()) {
                setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.archive_no_read_permission"), domain);
                return;
            }
            if (tmpFile.isDirectory()) {
                isDirectoryDeploy = true;
            }
        }
        
        try {
            // Get the module ID
            this.moduleID = (String)deployOptions.get(DeploymentProperties.DEPLOY_OPTION_NAME_KEY);
            boolean isModuleDeployed = isModuleDeployed(dasConnection, moduleID);
            // for redeploy, force should be true - enforce it here it self
            if(("false".equals(deployOptions.get(DeploymentProperties.DEPLOY_OPTION_FORCE_KEY))) &&
               (isModuleDeployed) ) {
                setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.deploy_error_module_exists"), domain);
                return;
            }

            deplMgr = ProxyFactory.getInstance(dasConnection).getDomainRoot().getDeploymentMgr();
            
            /**
             * If there is only one target and that target is a stand alone server target, set WSDL_TARGET_HINT 
             * in options to enable WSDL generation with the target's host and port. Refer to bug 6157923 for more info
             */
            if( (targetList.length == 1) &&  (TargetType.STAND_ALONE_SERVER.equals(targetList[0].getTargetType())) && !("server".equals(targetList[0].getName())) ) {
                deployOptions.put(DeploymentProperties.WSDL_TARGET_HINT, targetList[0].getName());
            }

            // Do redeploy if force=true and the module is already deployed
            if( ("true".equals(deployOptions.get(DeploymentProperties.DEPLOY_OPTION_FORCE_KEY))) &&
                (isModuleDeployed) ) {
                isRedeploy = true;

                // Get list of all targets on which this module is already deployed
                deployedTargets = DeploymentClientUtils.getDeployedTargetList(dasConnection, moduleID);
              
                // Check if any of the specified targets is not part of the deployed target list
                // If so, it means user has to use create-app-ref and not redeploy; flag error
                if(DeploymentClientUtils.isNewTarget(deployedTargets, targetList)) {
                    setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.useCreateAppRef",
                                        moduleID), domain);                    
                }
                
                // if there is already app ref associated with this app
                if (deployedTargets.size() > 0) {
                    // if it's redeploy to domain, then it's equivalent
                    // to redeploy to all targets 
                    if ((TargetType.DOMAIN.equals(targetList[0].getName()))) {
                        DeploymentFacility deploymentFacility;
                        if(isLocalConnectionSource) {
                            deploymentFacility = DeploymentFacilityFactory.getLocalDeploymentFacility();
                        } else {
                            deploymentFacility = DeploymentFacilityFactory.getDeploymentFacility();
                        }
                        deploymentFacility.connect(
                            targetList[0].getConnectionInfo());
                        Set nameSet = deployedTargets.keySet();
                        String[] targetNames = (String[])nameSet.toArray(
                            new String[nameSet.size()]);
                        Target[] targetList2 = 
                            deploymentFacility.createTargets(targetNames);
                        if (targetList2 == null) {
                            setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.createTargetsFailed"), domain);
                            return;
                        }
                        targetList = new SunTarget[targetList2.length];
                        for (int ii = 0; ii < targetList2.length; ii++) {
                            targetList[ii] = (SunTarget)targetList2[ii];
                        }
                    }
                    // if all targets on which the app is deployed is not 
                    // given, return error
                    else if (!DeploymentClientUtils.isTargetListComplete(
                        deployedTargets, targetList)) {
                        setupForAbnormalExit(
                           localStrings.getString("enterprise.deployment.client.specifyAllTargets", moduleID, "redeploy"),
                           domain);
                        return;
                    }

                    // Stop all apps;
                    Map options = new HashMap();
                    options.putAll(deployOptions);
                    options.put(DeploymentProperties.REDEPLOY, Boolean.toString(isModuleDeployed));
                    RollBackAction undeplRollback = new RollBackAction(RollBackAction.DELETE_APP_REF_OPERATION, 
                                                                moduleID, deployOptions);
                    for(int i=0; i<targetList.length; i++) {
                        options.put(DeploymentProperties.DEPLOY_OPTION_CASCADE_KEY, "true");

                        // We dont rollback for stop failure because the failure may be because of server being down
                        // We just add DeploymentStatus of this phase to the complete DeploymentStatus
                        
                        DeploymentClientUtils.setResourceOptions(
                            options, 
                            DeploymentProperties.RES_UNDEPLOYMENT,
                            targetList[i].getName());
                        DeploymentStatus stat = 
                            DeploymentClientUtils.stopApplication(
                                dasConnection.getExistingMBeanServerConnection(),
                                moduleID, targetList[i], options);
                        if (!checkStatusAndAddStage(targetList[i], null, localStrings.getString("enterprise.deployment.client.redeploy_stop", targetList[i].getName()) , dasConnection, stat)) {
                            return;
                        }
                        
                        // del-app-ref from all targets
                        options.put(DeploymentProperties.DEPLOY_OPTION_CASCADE_KEY, "false");
                        stat = DeploymentClientUtils.deleteApplicationReference(
                                dasConnection.getExistingMBeanServerConnection(),
                                moduleID, targetList[i], options);
                        if(!checkStatusAndAddStage(targetList[i], undeplRollback, localStrings.getString("enterprise.deployment.client.redeploy_remove_ref", targetList[i].getName()), dasConnection, stat)) {
                            return;
                        }
                        undeplRollback.addTarget(targetList[i], RollBackAction.APP_REF_DELETED);
                    }
                }
            }

            // Get a deploy ID
            deployActionID = deplMgr.initDeploy();
            
            // Make a copy of deployOptions and set the ENABLE flag in this copy to true
            // This is so that the enabled flag during deploy-to-domain is always true
            // This might need to be replaced with an efficient logic to look up the current enabled flag
            // using AMX
            Map dupOptions = new HashMap();
            dupOptions.putAll(deployOptions);
            dupOptions.put(DeploymentProperties.DEPLOY_OPTION_ENABLE_KEY, Boolean.TRUE.toString());
            dupOptions.put(DeploymentProperties.REDEPLOY, Boolean.toString(isModuleDeployed));
     
            // if deploy to "domain" or redeploy to "domain" with no 
            // existing application-ref
            if ((TargetType.DOMAIN.equals(targetList[0].getName()))) {
                if (isRedeploy) {
                    DeploymentClientUtils.setResourceOptions(
                        dupOptions, 
                        DeploymentProperties.RES_REDEPLOYMENT,
                        targetList);
                } else {
                    DeploymentClientUtils.setResourceOptions(
                        dupOptions, 
                        DeploymentProperties.RES_DEPLOYMENT,
                        targetList);
                }
            } else {
                DeploymentClientUtils.setResourceOptions(
                    dupOptions, 
                    DeploymentProperties.RES_NO_OP,
                    targetList);
            }
            
            // Now start a fresh deploy in domain
            // upload file only if this not a directory deploy AND it is not a local connection source (not from gui)
            
            String uploadProp = (String) deployOptions.get(DeploymentProperties.UPLOAD);
            boolean upload = new Boolean((uploadProp != null) ? uploadProp : DeploymentProperties.DEFAULT_UPLOAD).booleanValue();
            if(!isDirectoryDeploy && !isLocalConnectionSource && upload) {
                // upload the archive

                long startTime = System.currentTimeMillis();
                long endTime = startTime;

                // we use jmx upload for following scenarios:
                // 1. for secure connection: https
                // 2. if the JMX_UPLOAD_CHUNK_SIZE system property is set
                // 3. if the HTTP_PROXYHOST system property is set
	        if ( serverId.isSecure() || 
                     (jmxUploadChunkSizeProp != null && 
                      jmxUploadChunkSizeProp.length() > 0) || 
                     (httpProxyHostProp != null && 
                      httpProxyHostProp.length() > 0) ){
                    // using jmx
                    archiveUploadID = uploadArchive(deployArchive);
                    
                    // If there is a plan, upload the plan
                    if (deployPlan != null){
                        if (deployPlan.getURI()!=null) {
                            File f = new File(deployPlan.getURI().getPath());
                            if (f.length()!= 0) {
                                planUploadID = uploadArchive(deployPlan);
                            }
                        }
                    }

                    endTime = System.currentTimeMillis();

                    // Call DeploymentMgr to start deploy
                    deplMgr.startDeploy(deployActionID, archiveUploadID, planUploadID, dupOptions);
                } else {
                    // using http
                    String archivePath = uploadArchiveOverHTTP(serverId, 
                        deployArchive);
                    DeploymentSourceImpl archiveSource = 
                        new DeploymentSourceImpl(archivePath, true, 
                            new String[1], new String[1], new String[1], 
                            new HashMap());

                    String planPath = null;
                    DeploymentSourceImpl planSource = null; 

                    // If there is a plan, upload the plan
                    if (deployPlan != null){
                        if (deployPlan.getURI()!=null){
                            File f = new File(deployPlan.getURI().getPath());
                            if (f.length()!= 0) {
                                planPath = uploadArchiveOverHTTP(serverId, deployPlan);
                                planSource =
                                    new DeploymentSourceImpl(planPath, true, 
                                    new String[1], new String[1], new String[1], 
                                    new HashMap());
                            }
                        }
                    }

                    endTime = System.currentTimeMillis();

                    deplMgr.startDeploy(deployActionID, archiveSource.asMap(), 
                        planSource == null ? null : planSource.asMap(), 
                        dupOptions);
                }
                _logger.log(Level.FINE, 
                    "time in upload: " + (endTime-startTime));
            } else {
                // Directory deploy is supported only on DAS - check that here
                if((isDirectoryDeploy) && (!isDomainLocal(domain))) {
                    setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.domainNotLocal"), 
                        domain);
                    return;                    
                }
                DeploymentSourceImpl archive = new DeploymentSourceImpl(deployArchive.getURI().getPath(), true,
                                                    new String[1], new String[1], new String[1], new HashMap());
                // we do not support deployment plan for directory deployment
                // currently
                deplMgr.startDeploy(deployActionID, archive.asMap(), null, dupOptions);
            }

            // if deployActionID is still null, then there is some failure - report this and die
            if(deployActionID == null) {
                setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.no_deployment_id"), domain);
                return;
            }

            // Wait till deploy is over
            boolean done = false;
            int waitLoopCount = 0;
            com.sun.appserv.management.deploy.DeploymentStatus finalStatusFromMBean = null;
            do {
                Notification[] notifs = deplMgr.takeNotifications(deployActionID);
                for(int i=0; i<notifs.length; i++) {
                    Map notifType = (Map) notifs[i].getUserData();
                    if(notifType.get(deplMgr.NOTIF_DEPLOYMENT_COMPLETED_STATUS_KEY) != null) {
                        finalStatusFromMBean = 
                            DeploymentSupport.mapToDeploymentStatus((Map)deplMgr.getFinalDeploymentStatus(deployActionID));
                        done = true;
                    } else if(notifType.get(deplMgr.NOTIF_DEPLOYMENT_PROGRESS_KEY) != null) {
                        DeploymentProgress prog = DeploymentSupport.mapToDeploymentProgress((Map)notifType.get(deplMgr.NOTIF_DEPLOYMENT_PROGRESS_KEY));
                        String progStr = prog.getDescription() + " : " + prog.getProgressPercent() + "%";
                        fireProgressEvent(StateType.RUNNING, progStr, domain);
                    }
                }
                if(!done) {
                    if(waitLoopCount > TIMEOUT_LOOPS) {
                        setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.deployment_time_out"), domain);
                        return;
                    }
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch(InterruptedException e) {
                        //Swallowing this exception deliberately; we dont want to do anything but wait
                    }
                }
                waitLoopCount++;
            } while(!done);

            DeploymentStatus tmp = DeploymentClientUtils.getDeploymentStatusFromAdminStatus(finalStatusFromMBean);

            if(!checkStatusAndAddStage(
                domain, null, 
                localStrings.getString("enterprise.deployment.client.deploy_in_domain"),
                dasConnection, tmp)) {
                return;
            }

            //Take the one returned from the server
            if (moduleID == null) {
                moduleID = tmp.getProperty(DeploymentStatus.MODULE_ID);
            }

            String key = moduleID + DeploymentStatus.KEY_SEPARATOR + DeploymentStatus.MODULE_TYPE;
            this.moduleType = ModuleType.getModuleType(
                                (new Integer(tmp.getProperty(key))).intValue());

            // Start keeping track of actions to be rolled back
            RollBackAction rollback = new RollBackAction(RollBackAction.DEPLOY_OPERATION, moduleID, deployOptions);

            // Deploy is done; create app ref if target[0] was not a domain
            if(!(TargetType.DOMAIN.equals(targetList[0].getName()))) {
                for(int i=0; i<targetList.length; i++) {
                    
                    // If this is a redeploy, set enable flag of options as per state of the app before redeploy
                    if(deployedTargets != null) {
                        dupOptions.put(DeploymentProperties.DEPLOY_OPTION_ENABLE_KEY, deployedTargets.get(targetList[i].getName()).toString());
                    } else {
                        dupOptions.put(DeploymentProperties.DEPLOY_OPTION_ENABLE_KEY, deployOptions.get(DeploymentProperties.DEPLOY_OPTION_ENABLE_KEY));
                    }
                    DeploymentStatus stat = 
                        DeploymentClientUtils.createApplicationReference(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetList[i], dupOptions);
                    if(!checkStatusAndAddStage(targetList[i], rollback, 
                       localStrings.getString("enterprise.deployment.client.deploy_create_ref", targetList[i].getName()), dasConnection, stat)) {
                        return;
                    }
                    rollback.addTarget(targetList[i], rollback.APP_REF_CREATED);

                    // Start the apps only if enable is true; if this is a redeploy, then check what was the
                    // state before redeploy
                    /*
                      XXX Start the application regardless the value of "enable"
                      Otherwise no DeployEvent would be sent to the listeners on 
                      a remote instance, which would in turn synchronize the app
                      bits.  Note that the synchronization is only called during
                      applicationDeployed, not applicationEnabled.  To make sure
                      the deployment code can work with both the new and the old
                      mbeans, we will call the start for now (as the old mbeans
                      would do).  The backend listeners are already enhanced to
                      make sure the apps are not actually loaded unless the enable
                      attributes are true for both the application and 
                      application-ref elements.
                    */
                    if ((deployedTargets != null) && 
                         (Boolean.FALSE.equals(deployedTargets.get(targetList[i].getName())))) {
                             continue;
                    }
                    
                    // We dont rollback for start failure because start failure may be because of server being down
                    // We just add DeploymentStatus of this phase to the complete DeploymentStatus
                    
                    if (isRedeploy) {
                        DeploymentClientUtils.setResourceOptions(
                            deployOptions, 
                            DeploymentProperties.RES_REDEPLOYMENT,
                            targetList[i].getName());
                    } else {
                        DeploymentClientUtils.setResourceOptions(
                            deployOptions, 
                            DeploymentProperties.RES_DEPLOYMENT,
                            targetList[i].getName());
                    }

                    stat = DeploymentClientUtils.startApplication(
                            dasConnection.getExistingMBeanServerConnection(),
                            moduleID, targetList[i], deployOptions);
                    checkStatusAndAddStage(targetList[i], null, 
                        localStrings.getString("enterprise.deployment.client.deploy_start", targetList[i].getName()), dasConnection, stat, true);
                }
            }

            // Do WSDL publishing only if the caller is not GUI
            if ( !isLocalConnectionSource ) {
                try {
                    DeploymentClientUtils.doWsdlFilePublishing(tmp, dasConnection);
                } catch (Exception wsdlEx) {
                    DeploymentStatus newStatus = new DeploymentStatus();
                    newStatus.setStageStatus(DeploymentStatus.FAILURE);
                    newStatus.setStageStatusMessage(wsdlEx.getMessage());
                    newStatus.setStageException(wsdlEx);
                    checkStatusAndAddStage(domain, rollback, 
                        localStrings.getString("enterprise.deployment.client.deploy_publish_wsdl"), dasConnection, newStatus);
                    String msg =  localStrings.getString("enterprise.deployment.client.deploy_publish_wsdl_exception", wsdlEx.getMessage());
                    setupForAbnormalExit(msg, domain);
                    return;
                }
            }
            
            initializeTargetModuleIDForAllServers(
                    tmp, dasConnection.getMBeanServerConnection(false));

            setupForNormalExit(localStrings.getString("enterprise.deployment.client.deploy_application", moduleID), domain);
        } catch (Throwable ioex) {
            finalDeploymentStatus.setStageException(ioex);
            setupForAbnormalExit(localStrings.getString("enterprise.deployment.client.deploy_application_failed", ioex.getMessage()), domain);
            return;
        }
    }

    private boolean isModuleDeployed(ConnectionSource dasConnection, String moduleID) throws Exception {
        return DeploymentClientUtils.isModuleDeployed(
            dasConnection.getExistingMBeanServerConnection(), moduleID);
    }
    
    private boolean isDomainLocal(SunTarget domain) {
        // host = "localhost", it is local
        if("localhost".equalsIgnoreCase(domain.getHostName())) {
            return true;
        }
        
        // get localhost details and see if the host name or IP address matches with that of the domain
        try {
            InetAddress lh = InetAddress.getLocalHost();        
            if(domain.getHostName().equalsIgnoreCase(lh.getCanonicalHostName())) {
                return true;
            }
            if(domain.getHostName().equalsIgnoreCase(lh.getHostName())) {
                return true;
            }
            if(domain.getHostName().equalsIgnoreCase(lh.getHostAddress())) {
                return true;
            }
        } catch (UnknownHostException ex) {
            return false;
        }
        return false;
    }

    private String getArchiveName(Archive archive) {
        
        if (archive.getURI()==null){
            return null;
        }
        String name = archive.getURI().getPath();
        if (name != null) {
            return name.substring(name.lastIndexOf("/")+1);
        }
        return name;
    }
}
