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
 * ApplicationStopPhase.java
 *
 * Created on May 20, 2003, 3:19 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/ApplicationStopPhase.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;

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
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.server.core.AdminNotificationHelper;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.server.Constants;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Phase that is responsible to send stop events when an application is undeployed or
 * disassociated
 * @author  Sandhya E
 */
public class ApplicationStopPhase extends DeploymentPhase {
    
    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( ApplicationStopPhase.class );
    
    /**
     * Creates a new instance of Class 
     * @param deploymentCtx DeploymentContext object
     */
    public ApplicationStopPhase(DeploymentContext deploymentCtx)
    {
        this.deploymentCtx = deploymentCtx;
        this.name = APP_STOP;
    }
    
    /** 
     * Sends stop events to the required target 
     * @param req DeploymentRequest object
     * @param phaseCtx the DeploymentPhaseContext object     
     */
    public void runPhase(DeploymentPhaseContext phaseCtx)
    {
        String type = null;
        
        DeploymentRequest req = phaseCtx.getDeploymentRequest();

        DeploymentTarget target = (DeploymentTarget)req.getTarget(); 
        DeploymentStatus status = phaseCtx.getDeploymentStatus();

        int loadUnloadAction = Constants.UNLOAD_ALL;

        Application app = DeploymentServiceUtils.getInstanceManager(
               req.getType()).getRegisteredDescriptor(req.getName());

        // store the application object in DeploymentContext before it's 
        // removed from instance manager cache
        deploymentCtx.addApplication(req.getName(), app);
        
        if(!req.isApplication())
        {         
            type = DeploymentServiceUtils.getModuleTypeString(req.getType());
        } else {
            if ( (app != null) && (app.getRarComponentCount() != 0) ) {
                loadUnloadAction = Constants.UNLOAD_REST;
            }
        }
        
        prePhaseNotify(getPrePhaseEvent(req));
  
        boolean success;
        try {
            // send this event to unload non-rar standalone module
            // or to unload the non-rar submodules of embedded rar
            if (! req.isConnectorModule()) {
                success = target.sendStopEvent(req.getActionCode(), req.getName(), type, req.getCascade(), req.isForced(), loadUnloadAction);
            } else {
                status.setStageStatus(DeploymentStatus.SUCCESS);
                return;
            }
        } catch(DeploymentTargetException dte) {
            status.setStageStatus(DeploymentStatus.FAILURE);
            if (dte.getCause()!=null) {
                status.setStageException(dte.getCause());
                status.setStageStatusMessage(dte.getMessage());
            }
            return;
        }
        if (success) {
            status.setStageStatus(DeploymentStatus.SUCCESS);
        } else {
            status.setStageStatus(DeploymentStatus.WARNING);
            status.setStageStatusMessage("Application failed to stop");
        }            
        
        postPhaseNotify(getPostPhaseEvent(req));
        
        // if any exception is thrown. we let the stack unroll, it 
        // will be processed in the DeploymentService.
    }
    
    /**
     * Event that will be broadcasted at the start of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    protected DeploymentEvent getPrePhaseEvent(DeploymentRequest req) 
    {
        return new DeploymentEvent(DeploymentEventType.PRE_APP_STOP, new DeploymentEventInfo(req));
    }
    
    /**
     * Event that will be broadcasted at the end of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    protected DeploymentEvent getPostPhaseEvent(DeploymentRequest req) 
    {
        return new DeploymentEvent(DeploymentEventType.POST_APP_STOP,new DeploymentEventInfo(req) );
    }

}
