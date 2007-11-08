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
 * DisassociationPhase.java
 *
 * Created on May 20, 2003, 3:17 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/DisassociationPhase.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentEventType;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.util.i18n.StringManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This phase is responsible to disassociate an application from a target
 * It uses ServerDeploymentTarget, GroupDeploymentTarget to remove references
 * @author  Sandhya E
 */
class DisassociationPhase extends DeploymentPhase {
    
    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** string manager */
    private static StringManager localStrings =
            StringManager.getManager( DisassociationPhase.class );
    
    /**
     * Creates a new instance of DisassociatePhase
     * @param deploymentCtx DeploymentContext object
     */
    public DisassociationPhase(DeploymentContext deploymentCtx) {
        this.deploymentCtx = deploymentCtx;
        this.name = DISASSOCIATE;
    }
    
    /**
     * This method disassociates the application from the target as specified
     * in request object
     * @param req DeploymentRequest object
     * @param phaseCtx the DeploymentPhaseContext object
     */
    public void runPhase(DeploymentPhaseContext phaseCtx) {
        DeploymentStatus status = phaseCtx.getDeploymentStatus();
        
        try {
            //get the app directory on target's cache deleted
            DeploymentRequest req = phaseCtx.getDeploymentRequest();
            DeploymentTarget target = (DeploymentTarget)req.getTarget();
            
            /*
             *Remove the app ref and send the event unless this is part of a 
             *redeployment sequence of operations and the attributes for the app
             *ref will not change as a result of the deployment, compared to 
             *their current values.
             */
            ApplicationRef ref = ApplicationReferenceHelper.findCurrentAppRef(
                    deploymentCtx, target.getName(), req.getName());
            boolean needToRemoveRef = true;
            DeploymentContext.SavedApplicationRefInfo info = null;
            if (req.isRedeployInProgress()) {
                /*
                 *Save a pointer to the existing app ref for use during the later
                 *disassociate phase.
                 */
                info = deploymentCtx.saveAppRef(
                        req.getName(), target.getName(), ref, req);
                needToRemoveRef = info.isChanging();
            }
            sLogger.fine("DisassociationPhase for " + req.getName() + " on " + target.getName() + 
                    "; isRedeployInProgress = " + req.isRedeployInProgress() +
                    ", savedAppRefInfo is " + (info != null ? info.toString() : "<null>" ) + "need to remove = " + needToRemoveRef);
            if (needToRemoveRef) {
                sLogger.fine("DisassociationPhase removing app ref for " + req.getName() + " on " + target.getName());
                target.removeAppReference(req.getName());
                sendDisassociateEvent(req);
            }
            status.setStageStatus(DeploymentStatus.SUCCESS);
        }catch(Throwable t){
            status.setStageStatus(DeploymentStatus.FAILURE);
            status.setStageException(t);
            status.setStageStatusMessage(t.getMessage());
        }
    }
    
    
    /**
     * Event that will be broadcasted at the start of the phase
     * @param req DeploymentRequest object
     * @return DeploymentEvent
     **/
    private DeploymentEvent getPrePhaseEvent(DeploymentRequest req) {
        return new DeploymentEvent(DeploymentEventType.PRE_DISASSOCIATE, new DeploymentEventInfo(req));
    }
    
    /**
     * Event that will be broadcasted at the end of the phase
     * @param req DeploymentRequest object
     * @return DeploymentEvent
     **/
    private DeploymentEvent getPostPhaseEvent(DeploymentRequest req) {
        return new DeploymentEvent(DeploymentEventType.POST_DISASSOCIATE,new DeploymentEventInfo(req));
    }
    
    private void sendDisassociateEvent(DeploymentRequest req) throws
            Exception {
        DeploymentTarget target = (DeploymentTarget)req.getTarget();
        
        String moduleType;
        
        if(req.isApplication()) {
            moduleType = null;
        } else {
            moduleType = DeploymentServiceUtils.getModuleTypeString(req.getType());
        }
        
        int eventType = com.sun.enterprise.admin.event.BaseDeployEvent.APPLICATION_UNREFERENCED;
        String appName = req.getName();
        String targetName = target.getTarget().getName();
        
        boolean success = DeploymentServiceUtils.multicastEvent(
                eventType,
                appName,
                moduleType,
                false,
                true,
                targetName);
        
        sLogger.log(Level.FINE, "sendDisassociateEvent: success=" + success);
    }
}
