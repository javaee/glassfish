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
 * DeploymentTargetFactoryPEImpl.java
 *
 * Created on June 22, 2003, 7:57 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/DeploymentTargetFactoryPEImpl.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.deployment.backend.IASDeploymentException;

import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;

/**
 * Target factory used for PE case. In case target name is domain a GroupDeploymentTarget 
 * that acts as an aggregation of servers in the domain is returned.
 * @author Sandhya E
 *
 * Changed by Sridatta for bug fix: 4932179
 * Target is always ServerDeploymentTarget for PE. There is always only 1 instance
 * in PE. Changing the implementation to reflect this.
 */
//FIXTHIS: Question. Does this really need to be made pluggable? Everywhere else 
//(i.e. config and resource operations) we allow all possible targets in both PE, SE, EE.
//This would clean this up alot. Currently we need pluggability so that we can 
//create a subclass of ServerDeploymentTarget (which attempts to shuttle bits).
public class DeploymentTargetFactoryPEImpl extends DeploymentTargetFactory {        
        
    private static final TargetType[] VALID_DEPLOYMENT_TYPES = new TargetType[] {TargetType.DAS};
    
    /** 
     * Creates a new instance of DeploymentTargetFactoryPEImpl 
     */
    public DeploymentTargetFactoryPEImpl() {}

    /**
     * @return valid deployment target types for this target factory
     */
    public TargetType[] getValidDeploymentTargetTypes() {
        return this.VALID_DEPLOYMENT_TYPES;
    }
    
    /**
     * Returns the Deployment target for the targetName. If targetName is default_target "domain" then a
     * GroupDeploymentTarget representing a collection of all servers in this domain is returned. 
     * This method is called from Association/DisassociationPhase 
     * and Start/StopPhase. For PE, when no target name is specified in the deployment commands
     * association/disassociation/start/stop need to execute for all servers.
     * @param configContext
     * @param targetName name of the target
     * @return DeploymentTarget representing the targetName [ GroupDeploymentTarget is targetName == "domain"]
     * @throws IASDeploymentException
     */
    public DeploymentTarget getTarget(ConfigContext configContext, String domainName, String targetName) throws IASDeploymentException 
    {
        try{
            //parse the given target and ensure that it is a valid server instance. Server instance
            //is the only valid target type for PE.            
            final Target target = TargetBuilder.INSTANCE.createTarget(VALID_DEPLOYMENT_TYPES, 
                targetName, configContext);    
            if (targetName == null) {
                targetName = target.getName();            
            }
            return new ServerDeploymentTarget(configContext , domainName, targetName);
        } catch(Throwable t){            
            throw new IASDeploymentException("Error:" + t.getMessage(), t);
        }
    }    

    /**
     * Returns the default target. Used incase when deployment command doesnt
     * get the target name from user
     * @param configContext config context
     * @return DeploymentTarget
     * @throws IASDeploymentException
     */
    public DeploymentTarget getTarget(ConfigContext configContext, String domainName) throws IASDeploymentException 
    {
        return getTarget(configContext, domainName, null);
    }
    
}
