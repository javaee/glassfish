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
 * DeploymentPhase.java
 *
 * Created on April 29, 2003, 12:25 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/DeploymentPhase.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.deployment.backend.DeploymentEventManager;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.admin.common.constant.DeploymentConstants;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This class is a framework for a deployment phase.
 * A deployment phase is a logical part of deployment or undeployment operation
 * Deployment operation has three phases namely J2EEC, Associate, Start
 * Undeployment has UndeployFromDomain,Disassociate,Stop phases.
 * These phases are invoked from DeploymentService. There is no state associated with these 
 * phases, that makes it possible for running phases concurrently.
 * This class also provides methods for notifications, and pre post conditions.
 * @author  Sandhya E
 */
public abstract class DeploymentPhase implements DeploymentConstants{
    
    /** names used for phases **/
    public static final String J2EEC = "J2EEC";
    public static final String ASSOCIATE = "Associate";
    public static final String DISASSOCIATE = "Disassociate";
    public static final String APP_START = "appStart";
    public static final String RA_START = "raStart";
    public static final String APP_STOP = "appStop";
    public static final String RA_STOP = "raStop";
    public static final String UNDEPLOY = "Undeploy";
    public static final String PRE_RES_CREATION = "preResCreation";
    public static final String POST_RES_CREATION = "postResCreation";
    public static final String PRE_RES_DELETION = "preResDeletion";
    public static final String POST_RES_DELETION = "postResDeletion";
    
    /** context object for executing this phase **/
    protected DeploymentContext deploymentCtx;
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( DeploymentPhase.class );
    
    /** name of the phase **/
    String name = null;
    
    /**
     * This method executes the whole phase.
     * @param req DeploymentRequest object
     * @param status the DeploymentStatus object to return feedback
     * @throws DeploymentPhaseException
     */
    public final DeploymentPhaseContext executePhase(DeploymentRequest req, DeploymentStatus status) 
        throws DeploymentPhaseException {
	DeploymentPhaseContext phaseCtx = getPhaseContext();
        phaseCtx.setDeploymentRequest(req);
        phaseCtx.setDeploymentStatus(status);
        prePhase(phaseCtx);     
        if (status.getStatus()>DeploymentStatus.FAILURE)
            runPhase(phaseCtx);
        if (status.getStatus()>DeploymentStatus.FAILURE)
            postPhase(phaseCtx);
	return phaseCtx;
    }
    
    /**
     * This method is called when a successfully executed phase needs 
     * to be rollbacked due to a subsequent failure in the deployment
     * process. All information needed for rollbacking should be 
     * available in the DeploymentPhaseContext instance returned by
     * the executePhase and passed to this method
     *
     * @param phaseCtx the DeploymentPhaseContext instance
     * @throws DeploymentPhaseException
     */
    public void rollback(DeploymentPhaseContext phaseCtx) throws DeploymentPhaseException {
    }
    
    /**
     * Phase specific execution logic will go in this method. Any phase implementing
     * this class will provide its implementation for this method.
     * @param phaseCtx the DeploymentPhaseContext object
     * @throws DeploymentPhaseException
     */
    public abstract void runPhase(DeploymentPhaseContext phaseCtx) throws DeploymentPhaseException;
    
    /**
     * Any prePhase checks can be done here, and also any preparation for actual
     * phase execution can happen here
     * @param phaseCtx the DeploymentPhaseContext object     
     * @throws DeploymentPhaseException
     */
    public void prePhase(DeploymentPhaseContext phaseCtx) throws DeploymentPhaseException{
        //phase specific pre conditions must be checked here
    }
    
    /**
     * Any postPhase checks and postPhase cleanup can happen here
     * @param phaseCtx the DeploymentPhaseContext object     
     * @throws DeploymentPhaseException
     */
    public void postPhase(DeploymentPhaseContext phaseCtx) throws DeploymentPhaseException{
        //phase specific post conditions must be checked here
    }
    
    /**
     * All the listeners registered for this phase will be notified 
     * at the start of the phase.
     */
    public void prePhaseNotify(DeploymentEvent event){
        DeploymentEventManager.notifyDeploymentEvent(event);
    }
    
    /**
     * All listeners registered with DeploymentEventManager will be notified
     * at the end of the phase
     */
    public void postPhaseNotify(DeploymentEvent event) {
        DeploymentEventManager.notifyDeploymentEvent(event);
    }
    
    /**
     * Gets the name of this phase
     * @return name of the phase
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of this phase
     * @param name name of the phase
     */
    /*package*/ void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the deployment target of the specified name
     * @param targetName target that has to be returned, if targetName is null default target 
     *                   is returned
     * @return deploymentTarget DeploymentTarget
     */
    protected DeploymentTarget getTarget(String targetName) throws DeploymentPhaseException{
        try{
            DeploymentTarget target   = getTargetFactory().getTarget(
                                         deploymentCtx.getConfigContext(), targetName);
            return target;
        }catch(IASDeploymentException de){
            String msg = localStrings.getString("enterprise.deployment.phasing.phase.targetnotfound");
            throw new DeploymentPhaseException(getName(), msg, de);
        }
        
    }
     
     /**
      * @return a new @see DeploymentPhaseContext instance to hold information
      * about this phase. Can be used at rollback time to undo a succesful 
      * deployment phase.
      */
      protected DeploymentPhaseContext getPhaseContext() {
	  return new StandardDeploymentPhaseContext();
      }

      private DeploymentTargetFactory getTargetFactory() {
        return DeploymentTargetFactory.getDeploymentTargetFactory();
      }
}
