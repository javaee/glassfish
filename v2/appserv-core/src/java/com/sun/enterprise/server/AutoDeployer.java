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
 * @(#) AutoDeployer.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import java.io.File;

import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentCommand;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.Deployer;
import com.sun.enterprise.deployment.backend.DeployerFactory;
import com.sun.enterprise.deployment.backend.IASDeploymentException;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

/**
 * This class is a listener implementation of the auto deploy callbacks.
 * When auto deploy monitor detects a new archive under the auto deploy 
 * directory, this class handles the callback from the monitor.
 *
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
class AutoDeployer implements MonitorListener { 

    /** logger for this auto deployer */
    static Logger _logger=LogDomains.getLogger(LogDomains.CORE_LOGGER);

    // ---- START OF MonitorListener METHOD -----------------------------------

    /**
     * No-op. 
     *
     * @param    entry    monitored entry with a change in time stamp
     *
     * @return   no-op; returns false
     */
    public boolean reload(MonitorableEntry entry) { 
        return false;
    }

    /**
     * Handles the callbacks from the auto deploy monitor. 
     *
     * @param    entry    entry thats being monitored
     * @param    archive  newly detected archive under the auto deploy directory
     *
     * @return   true if deployed successfully
     */
    public boolean deploy(MonitorableEntry entry, File archive) {

        boolean status = false;

        try {
            DeploymentRequest req = deploy(archive);

            // load/reload deployed application/stand alone module

            // update the config context in server runtime

            // notify admin server if necessary

            status = true;
        } catch (IASDeploymentException de) {
            _logger.log(Level.WARNING, "core.exception", de);
            status = false;
        }

        return status;
    }

    // ---- END OF MonitorListener METHOD -------------------------------------

    /**
     * Deploys the given archive. If it is already been deployed, this 
     * redeploys the application.
     *
     * @param    archive    archive to be deployed
     * @return   deployment request after the deployment; this provides 
     *           information about the type of deployment being executed
     * @throws   IASDeploymentException    if an error while deploying
     */
    private DeploymentRequest deploy(File archive) 
            throws IASDeploymentException {

        // instance environment for this server
        InstanceEnvironment env = 
            ApplicationServer.getServerContext().getInstanceEnvironment();

        // sets the type of the archive being deployed
        DeployableObjectType type = null;
        if ( FileUtils.isEar(archive) ) {
            type = DeployableObjectType.APP;
        } else if ( FileUtils.isJar(archive) ) {
            type = DeployableObjectType.EJB;
        } else if ( FileUtils.isWar(archive) ) {
            type = DeployableObjectType.WEB;
        } else if ( FileUtils.isRar(archive) ) {
            type = DeployableObjectType.CONN;
        }

        // constructs a deploy request with the given type
        DeploymentRequest req = 
            new DeploymentRequest(env, type, DeploymentCommand.DEPLOY);

        // sets the archive that needs to be deployed
        req.setFileSource(archive);

        // application is registered with the name of the 
        // archive without extension
        String fileName  = archive.getName();
        int dotIdx       = fileName.indexOf(fileName);
        String regName   = fileName.substring(0, dotIdx); 
        req.setName(regName);

        // sets the web context root
        if (type.isWEB()) {
            req.setContextRoot(regName);
        }

        // redeploys the app if already deployed
        req.setForced(true);

        // does the actual deployment
        Deployer deployer = DeployerFactory.getDeployer(req);
        deployer.doRequest();

        // any clean up other than cleaner thread invocation
        deployer.cleanup();

        return req;
    }
}

