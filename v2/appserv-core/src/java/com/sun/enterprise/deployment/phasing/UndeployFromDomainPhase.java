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
 * UndeployFromDomainPhase.java
 *
 * Created on June 10, 2003, 1:37 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/UndeployFromDomainPhase.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.deployment.Application;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.sun.enterprise.admin.util.Assert;
import com.sun.enterprise.admin.common.exception.DeploymentException;
import com.sun.enterprise.admin.common.constant.DeploymentConstants;
import com.sun.enterprise.deployment.backend.Deployer;
import com.sun.enterprise.deployment.backend.DeployerFactory;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentEventType;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.util.i18n.StringManager;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This phase is responsible for undeploying a application/module
 * from domain. It uses AppUndeployer, ModuleUnDeplyer[EjbModuleDeployer..etc] to
 * undeploy and also unregisters the mbeans
 * @author  Sandhya E
 */
public class UndeployFromDomainPhase extends DeploymentPhase {
    
    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( UndeployFromDomainPhase.class );
    
    /** 
     * Creates a new instance of Class 
     * @param deploymentCtx DeploymentContext object
     */
    public UndeployFromDomainPhase(DeploymentContext deploymentCtx) 
    {
        this.deploymentCtx = deploymentCtx;
        this.name=UNDEPLOY;
    }
    
    /** 
     * Undeploys the application using App/ModuleUnDeployers
     * unregisters the application for domain.xml
     * @param phaseCtx the DeploymentPhaseCtx object
     * @throws DeploymentPhaseException
     */
    public void runPhase(DeploymentPhaseContext phaseCtx)
    {
        DeploymentStatus status = phaseCtx.getDeploymentStatus();
        
        DeploymentRequest req = phaseCtx.getDeploymentRequest();
        DeploymentTarget target = (DeploymentTarget)req.getTarget();

        // set the descriptor on request so we can get it in Deployers code
        Application app = deploymentCtx.getApplication(req.getName());
        req.setDescriptor(app);
        
        // Clear out the reference to the class loader
        if (app != null) {
            app.setClassLoader(null);
        }

        String type = null;
        Deployer deployer = null;
        try{            
            if(!req.isApplication())
            {         
                type = DeploymentServiceUtils.getModuleTypeString(req.getType());
            }
            deployer = DeployerFactory.getDeployer(req);
            deployer.doRequest();
            
            // create a DeploymentStatus for cleanup stage, it is a 
            // substatus of current (UndeployFromDomainPhase) deployment status
            DeploymentStatus cleanupStatus =
                new DeploymentStatus(status);
            req.setCurrentDeploymentStatus(cleanupStatus);

            deployer.cleanup();

            DeploymentServiceUtils.removeFromConfig(req.getName(), 
                req.getType());
          
            // remove the application from deployment context
            deploymentCtx.removeApplication(req.getName());

            status.setStageStatus(DeploymentStatus.SUCCESS);            

            postPhaseNotify(getPostPhaseEvent(req));
            
        }catch(Throwable t){
            status.setStageStatus(DeploymentStatus.FAILURE);
            status.setStageException(t);
            status.setStageStatusMessage(t.getMessage());

            // Clean up domain.xml so that the system config is clean after the undeploy
            try {
                if (deployer != null) {
                    deployer.removePolicy();
                }
                DeploymentServiceUtils.removeFromConfig(req.getName(), req.getType());
            } catch (Exception eee){}
        }
    }
    
    /**
     * Event that will be broadcasted at the start of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
    protected DeploymentEvent getPrePhaseEvent(DeploymentRequest req) 
    {
        return new DeploymentEvent(DeploymentEventType.PRE_UNDEPLOY, new DeploymentEventInfo(req));
    }
    
    /**
     * Event that will be broadcasted at the end of the phase
     * @param req Deployment request object
     * @return DeploymentEvent
     */
   protected DeploymentEvent getPostPhaseEvent(DeploymentRequest req) 
   {
        return new DeploymentEvent(DeploymentEventType.POST_UNDEPLOY, new DeploymentEventInfo(req));
   }
   
}
