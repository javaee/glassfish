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
package com.sun.enterprise.ee.deployment.phasing;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.deployment.phasing.DeploymentTarget;
import com.sun.enterprise.deployment.phasing.DeploymentTargetFactory;
import com.sun.enterprise.deployment.phasing.DeploymentTargetFactoryPEImpl;
import com.sun.enterprise.deployment.phasing.ApplicationReferenceHelper;

import com.sun.enterprise.deployment.backend.IASDeploymentException;

import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;

/**
 * This class is used to determine the set of servers and groups for a 
 * specified deployment target in EE
 * @author  Sandhya E
 */
public class DeploymentTargetFactoryEEImpl extends DeploymentTargetFactoryPEImpl 
{
    private static final TargetType[] VALID_DEPLOYMENT_TYPES = new TargetType[] {
        TargetType.DOMAIN, TargetType.CLUSTER, TargetType.UNCLUSTERED_SERVER, TargetType.DAS};  
    
    /**
     * Creates a new instance of DeploymentTargetFactoryPEImpl 
     */
    public DeploymentTargetFactoryEEImpl() {
        super();
    }

    /**
     * @return valid deployment target types for this target factory
     */
    public TargetType[] getValidDeploymentTargetTypes() {
        return this.VALID_DEPLOYMENT_TYPES;
    }
    
    /**
     * Returns the Deployment target for the targetName. If targetName is 
     * default_target "domain" then no DeploymentTarget is returned. This 
     * method is called from Association/DisassociationPhase and 
     * Start/StopPhase. For EE, when no target name is specified in the 
     * deployment commands association/disassociation/start/stop need not do 
     * anything. So null is returned in that case
     *
     * @param ctx  config context
     * @param targetName name of the target
     *
     * @return DeploymentTarget representing the targetName 
     *         [ null is targetName == "domain"]
     *
     * @throws IASDeploymentException
     */
    public DeploymentTarget getTarget(ConfigContext configContext, String domainName, 
            String targetName) throws IASDeploymentException {

         try{
            //parse the given target and ensure that it is a valid server instance. 
            final Target target = TargetBuilder.INSTANCE.createTarget(VALID_DEPLOYMENT_TYPES, 
                targetName, configContext);
            final TargetType targetType = target.getType();
            if (targetName == null) {
                targetName = target.getName();
            }
            if (targetType == TargetType.DOMAIN) {
                return new DomainDeploymentTarget(configContext, domainName, targetName);
            } else if (targetType == TargetType.CLUSTER) {
                return new ClusterDeploymentTarget(configContext, domainName, targetName);
            } else if (targetType == TargetType.SERVER) {
                return new EEServerDeploymentTarget(configContext, domainName, targetName);
            } else if (targetType == TargetType.DAS) {
                return new EEServerDeploymentTarget(configContext, domainName, targetName);
            } else {
                throw new IASDeploymentException("Target not found: " + targetName);
            }                                  
        } catch (Throwable t) {
            throw new IASDeploymentException(t);
        }
    }    
}
