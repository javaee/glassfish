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

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.phasing.AssociationPhase;
import com.sun.enterprise.deployment.phasing.DeploymentService;
import com.sun.enterprise.deployment.phasing.DeploymentTarget;
import com.sun.enterprise.deployment.phasing.J2EECPhase;
import com.sun.enterprise.deployment.phasing.PEDeploymentService;
import com.sun.enterprise.deployment.phasing.DeploymentTarget;
import com.sun.enterprise.deployment.phasing.ServerDeploymentTarget;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


/**
 * Manages the phases and maps deployment operations to deployment phases
 * @author  deployment team
 */
public class EEDeploymentService extends PEDeploymentService {

    /** resource bundle */
    private static StringManager localStrings =
          StringManager.getManager( EEDeploymentService.class );

    /** 
      * Creates a new instance of PEDeploymentService 
      * @param configContext config context object
      */
    public EEDeploymentService(ConfigContext configContext) {
        super(configContext);
    }

    /**
     * The following is not turned on unless the following system property is true:
     * "dynamic.reconfiguration.enabled"
     */
    protected List getDeployPhaseListForTarget(DeploymentRequest req) {

        DeploymentTarget target = (DeploymentTarget) req.getTarget();        

        String dynamicReconfigurationEnabled =
            System.getProperty("dynamic.reconfiguration.enabled") == null
            ? "true"
            : (String) System.getProperty("dynamic.reconfiguration.enabled");

        //FIXME. Change the following to Level.FINE once we are more stable
        if ("true".equals(dynamicReconfigurationEnabled)) {
            sLogger.log(Level.INFO, "Dynamic deployment is ON");
        } else {
            sLogger.log(Level.INFO, "Dynamic deployment is OFF");
        }

        //phase list: j2eec, associate, start
        //if dynamic-reconfig is turned on
        //OR
        //if deploying to DAS
        if ("true".equals(dynamicReconfigurationEnabled) 
            || AdminService.getAdminService().isDas()) {
            return super.getDeployPhaseListForTarget(req);
        }
        
        //XXX FIXME should we be aware of the dynamic reconfig here?  or should 
        //the admin event framework take care of not sending the events?
        //Note that we could potentially remove the manipulation of the phase list
        //if the admin event is not sent when dynamic-reconfig is false.  It is not
        //the case right now.

        J2EECPhase j2eec = new J2EECPhase(deploymentContext);
        AssociationPhase associate = new AssociationPhase(deploymentContext);

        //phase list: j2eec, associate
        //if dynamic-reconfig is turned off
        List deployPhaseList = new ArrayList();
        deployPhaseList.add(j2eec);
        deployPhaseList.add(associate);
        return deployPhaseList;
    }
}
