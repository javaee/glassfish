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
 * AssociationPhase.java
 *
 * Created on May 20, 2003, 3:14 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/AssociationPhase.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentEventType;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.deployment.util.DeploymentProperties;

import com.sun.enterprise.util.i18n.StringManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages the association of an application or module with a target
 * @author  Sandhya E
 */
public class AssociationPhase extends DeploymentPhase {

    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( AssociationPhase.class );
   
    /** 
     * Creates a new instance of AssociationPhase 
     * @param deploymentCtx context object for the deployment
     */
    public AssociationPhase(DeploymentContext deploymentCtx) 
    {
        this.deploymentCtx = deploymentCtx;        
        this.name = ASSOCIATE;
    }

    /**
     * 
     */
    public void prePhase(DeploymentPhaseContext phaseCtx)
    {
        DeploymentRequest req = phaseCtx.getDeploymentRequest();
        DeploymentStatus status = phaseCtx.getDeploymentStatus();
        DeploymentTarget target = (DeploymentTarget)req.getTarget();

        try {
            // check context root uniqueness
            String virtualServers = (String)req.getOptionalAttributes().get(ServerTags.VIRTUAL_SERVERS);

            String contextRootInConflict = 
                ApplicationConfigHelper.checkContextRootUniqueness(
                    DeploymentServiceUtils.getConfigContext(), req.getName(), 
                    req.getTarget().getName(), virtualServers);
 
            if (contextRootInConflict != null) {
                throw new IASDeploymentException(localStrings.getString(
                    "duplicate_context_root",
                    contextRootInConflict, req.getName(), 
                    req.getTarget().getName()));
            }

            // only support directory deployment on DAS
            if (DeploymentServiceUtils.isDirectoryDeployed(req.getName(),
                req.getType())) {
                if (target != null && ServerHelper.isAServer(deploymentCtx.getConfigContext(), target.getTarget().getName()) && ServerHelper.isDAS(deploymentCtx.getConfigContext(), target.getTarget().getName())) {
                    return; 
                } else {
                    throw new IASDeploymentException(localStrings.getString(
                        "dir_deploy_not_support"));
                }
            }

            // FIXME: add the context root check here
        } catch(Throwable t){
            status.setStageStatus(DeploymentStatus.FAILURE);
            status.setStageException(t);
            status.setStageStatusMessage(t.getMessage());
        }
    }

    
    /** 
     * Phase specific execution logic will go in this method. Any phase implementing
     * this class will provide its implementation for this method.
     * @param req Deployment request object
     * @param phaseCtx the DeploymentPhaseContext object     
     */
    public void runPhase(DeploymentPhaseContext phaseCtx) 
    {
        try {
            DeploymentRequest req = phaseCtx.getDeploymentRequest();
            DeploymentTarget target = (DeploymentTarget)req.getTarget();
            if(target == null) {
                String msg = localStrings.getString("enterprise.deployment.phasing.association.targetnotspecified" );
                sLogger.log(Level.FINEST, msg);
                phaseCtx.getDeploymentStatus().setStageStatus(DeploymentStatus.WARNING);
                return;
            }
            prePhaseNotify(getPrePhaseEvent(req));
            String virtualServers = (String)req.getOptionalAttributes().get(ServerTags.VIRTUAL_SERVERS);

            /*
             *Add the app ref and send the event unless this is part of a 
             *redeployment sequence and the attributes for the app ref did not change 
             *as a result of the deployment, compared to their original values.
             */
            boolean needToAdd = true;
            DeploymentContext.SavedApplicationRefInfo savedAppRefInfo = null;
            
            if (req.isRedeployInProgress()) {
                /*
                 *Find the saved app ref info.
                 */
                savedAppRefInfo = 
                        deploymentCtx.removeSavedAppRef(req.getName(), target.getName());
                if (savedAppRefInfo != null) {
                    needToAdd = savedAppRefInfo.isChanging();
                }
            }
            sLogger.fine("AssociationPhase for " + req.getName() + " on " + target.getName() + 
                    "; isRedeployInProgress = " + req.isRedeployInProgress() +
                    ", savedAppRefInfo is " + (savedAppRefInfo != null ? savedAppRefInfo.toString() : "<null>" ) + "need to add = " + needToAdd);
            if (needToAdd) {
                sLogger.fine("AssociationPhase adding reference for " + req.getName() + " on " + target.getName());
                target.addAppReference(req.getName(), req.isStartOnDeploy(), virtualServers);
                /*
                 *See if there are saved non-default values for this app ref.
                 */
                if (savedAppRefInfo != null) {
                    /*
                     *Find the newly-created app ref. and assign the saved
                     *values.
                     */
                    ApplicationRef newAppRef = ApplicationReferenceHelper.findCurrentAppRef(
                            deploymentCtx,
                            target.getName(),
                            req.getName());
                    newAppRef.setLbEnabled(savedAppRefInfo.appRef().isLbEnabled());
                    newAppRef.setDisableTimeoutInMinutes(savedAppRefInfo.appRef().getDisableTimeoutInMinutes());
                }
                postPhaseNotify(getPostPhaseEvent(req));
                sendAssociateEvent(req);
            }
            
            phaseCtx.getDeploymentStatus().setStageStatus(DeploymentStatus.SUCCESS);
        } catch(DeploymentTargetException dte) {
            phaseCtx.getDeploymentStatus().setStageStatus(DeploymentStatus.FAILURE);
            phaseCtx.getDeploymentStatus().setStageException(dte.getCause());
            if (dte.getCause()!=null) {
                phaseCtx.getDeploymentStatus().setStageStatusMessage(dte.getCause().getMessage());
            }
        } catch(Throwable t) {
            phaseCtx.getDeploymentStatus().setStageStatus(DeploymentStatus.FAILURE);
            phaseCtx.getDeploymentStatus().setStageException(t);
            phaseCtx.getDeploymentStatus().setStageStatusMessage(t.getMessage());
        }
    }
    

    /**
     * Event that will be broadcasted at the start of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    private DeploymentEvent getPrePhaseEvent(DeploymentRequest req) 
    {
        return new DeploymentEvent(DeploymentEventType.PRE_ASSOCIATE, new DeploymentEventInfo(req));
    }
    
    /**
     * Event that will be broadcasted at the end of the phase
     * @return DeploymentEvent
     */
    private DeploymentEvent getPostPhaseEvent(DeploymentRequest req)
    {
        return new DeploymentEvent(DeploymentEventType.POST_ASSOCIATE, new DeploymentEventInfo(req));
    }


    private void sendAssociateEvent(DeploymentRequest req ) throws 
        DeploymentTargetException {
        try {
            DeploymentTarget target = (DeploymentTarget)req.getTarget();
            
            String moduleType;
            
            if(req.isApplication()) {
                moduleType = null;
            }
            else {
                moduleType = DeploymentServiceUtils.getModuleTypeString(req.getType());
            }
            
            int eventType = com.sun.enterprise.admin.event.BaseDeployEvent.APPLICATION_REFERENCED;
            String appName = req.getName();
            String targetName = target.getTarget().getName();
            
           boolean success = DeploymentServiceUtils.multicastEvent(
                            eventType, 
                            appName, 
                            moduleType, 
                            false, 
                            true, 
                            targetName);
           
           sLogger.log(Level.FINE, "sendAssociateEvent: success=" + success);
        } catch(Throwable t) {
            DeploymentTargetException dte = 
                new DeploymentTargetException(t.getMessage());
            dte.initCause(t);
            throw dte;
        }
    }
}
