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
 * ApplicationStartPhase.java
 *
 * Created on May 20, 2003, 3:16 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/ApplicationStartPhase.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentEventType;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.server.Constants;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This phase is responsible to send the start events to the actual server
 * instance. This phase is executed as part of Deploy, Associate operations
 * @author  Sandhya E
 */
public class ApplicationStartPhase extends DeploymentPhase {

    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( ApplicationStartPhase.class );
    
    /** 
     * Creates a new instance of ApplicationStartPhase 
     * @param deploymentCtx DeploymentContext object
     */
    public ApplicationStartPhase(DeploymentContext deploymentCtx) 
    {
         this.deploymentCtx = deploymentCtx;
         this.name = APP_START;
    }
    
    /** 
     * Phase specific execution logic will go in this method. Any phase implementing
     * this class will provide its implementation for this method.
     * @param req DeploymentRequest object
     * @param phaseCtx the DeploymentPhaseContext object     
     */
    public void runPhase(DeploymentPhaseContext phaseCtx) {
        String type = null;
        
        DeploymentRequest req = phaseCtx.getDeploymentRequest();

        DeploymentStatus status = phaseCtx.getDeploymentStatus();
        
        DeploymentTarget target = (DeploymentTarget)req.getTarget();
        if(target == null) {
            String msg = localStrings.getString("enterprise.deployment.phasing.start.targetnotspecified");
            sLogger.log(Level.FINEST, msg);
            status.setStageStatus(DeploymentStatus.SUCCESS);            
            return;
        }
        
        prePhaseNotify(getPrePhaseEvent(req));
        int actionCode = req.getActionCode();

        int loadUnloadAction = Constants.LOAD_ALL;
        
        if(req.isApplication()) {
            type = null;
            Application app = DeploymentServiceUtils.getInstanceManager(
               DeployableObjectType.APP).getRegisteredDescriptor(req.getName());

            if ( (app != null) && (app.getRarComponentCount() != 0) ) {
                loadUnloadAction = Constants.LOAD_REST;
            }
        }
        else {
            type = DeploymentServiceUtils.getModuleTypeString(req.getType());
        }
        
        boolean success;
        try { 
           // send this event to load non-rar standalone module or application
           // or to load the non-rar submodules of embedded rar
           if (! req.isConnectorModule()) {
               success = target.sendStartEvent(actionCode, req.getName(), type,
                                           req.isForced(), loadUnloadAction);
           } else {
               status.setStageStatus(DeploymentStatus.SUCCESS);
               return;
           }
        } catch(DeploymentTargetException dte) {
            status.setStageStatus(DeploymentStatus.WARNING);
            if (dte.getCause()!=null) {
                status.setStageStatusMessage(dte.getMessage());
            }
            return;
        }
        if (success) {
            status.setStageStatus(DeploymentStatus.SUCCESS);
        } else {
            status.setStageStatus(DeploymentStatus.WARNING);
            status.setStageStatusMessage("Application failed to load");
        }
        postPhaseNotify(getPostPhaseEvent(req));
        
        // if any exception arrise, we let it unroll this stack, it will
        // be processed by DeploymentService
    }
    
    /**
     * Event that will be broadcasted at the start of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    private DeploymentEvent getPrePhaseEvent(DeploymentRequest req) {
        return new DeploymentEvent(DeploymentEventType.PRE_APP_START, new DeploymentEventInfo(req));
    }
    
    /**
     * Event that will be broadcasted at the end of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    private DeploymentEvent getPostPhaseEvent(DeploymentRequest req) {
        return new DeploymentEvent(DeploymentEventType.POST_APP_START, new DeploymentEventInfo(req));
    }
    
}
