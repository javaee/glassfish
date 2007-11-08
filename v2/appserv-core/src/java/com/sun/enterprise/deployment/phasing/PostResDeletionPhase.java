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

import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentEventType;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.util.DeploymentProperties;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.resource.Resource;
import com.sun.enterprise.resource.ResourcesXMLParser;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages the deletion of the resources from 
 * sun-resources.xml that need to deleted prior to resource adapter unloading
 */
public class PostResDeletionPhase extends ResourcePhase {

    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager(PostResDeletionPhase.class);
   
    /** 
     * Creates a new instance of PostResDeletionPhase 
     * @param deploymentCtx context object for the deployment
     */
    public PostResDeletionPhase(DeploymentContext deploymentCtx) 
    {
        this.deploymentCtx = deploymentCtx;        
        this.name = POST_RES_DELETION;
    }

    /**
     * Phase specific execution logic will go in this method.
     * Any phase implementing
     * this class will provide its implementation for this method.
     * @param req Deployment request object
     * @param phaseCtx the DeploymentPhaseContext object
     */
    public void runPhase(DeploymentPhaseContext phaseCtx)
    {
        DeploymentStatus status = phaseCtx.getDeploymentStatus();

        try {
            DeploymentRequest req = phaseCtx.getDeploymentRequest();
                                                                      
            prePhaseNotify(getPrePhaseEvent(req));
            doResourceOperation(req);
            postPhaseNotify(getPostPhaseEvent(req));
                                                                      
            phaseCtx.getDeploymentStatus().setStageStatus(DeploymentStatus.SUCCESS);
        } catch(Exception e) {
            status.setStageStatus(DeploymentStatus.WARNING);
            status.setStageException(e);
            status.setStageStatusMessage(e.getMessage());
        }
    }
                                                                      
    /**
     * Event that will be broadcasted at the start of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    private DeploymentEvent getPrePhaseEvent(DeploymentRequest req)
    {
        return new DeploymentEvent(DeploymentEventType.PRE_RES_DELETE, new DeploymentEventInfo(req));
    }
                                                                      
    /**
                                                                      
     * Event that will be broadcasted at the end of the phase
     * @return DeploymentEvent      */
    private DeploymentEvent getPostPhaseEvent(DeploymentRequest req)    {
        return new DeploymentEvent(DeploymentEventType.POST_RES_DELETE, new DeploymentEventInfo(req));
    }
                                                                      
    // redeployment for resource deletion phase is same as undeployment
    public void handleRedeployment(List<String> targetList,
        List<Resource> resourceList) throws Exception {
        handleUndeployment(targetList, resourceList);
    }
                                                                      
    // no op if it's deployment     
    public void handleDeployment(List<String> targetList,
        List<Resource> resourceList) throws Exception {
    }
                                                                      
    public String getActualAction(String resAction) {
        if (resAction.equals(DeploymentProperties.RES_DEPLOYMENT)) {
            return DeploymentProperties.RES_NO_OP;
        } else {
            return resAction;
        }
    }

    public List<Resource> getRelevantResources(List<Resource> allResources) {
        return ResourcesXMLParser.getNonConnectorResourcesList(allResources, false);
    }
}
