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

package com.sun.enterprise.deployment.client;

import java.util.Map;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import com.sun.enterprise.deployment.deploy.shared.Archive;

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

import com.sun.enterprise.deployment.backend.DeploymentStatus;

/**
 * This interface defines basic deployment related facilities 
 * such as deploying any j2ee modules on a Domain Admin Server
 * or target servers as well as retrieving non portable artifacts
 * for successful runs in a client mode configuration.
 *
 * @author Jerome Dochez
 */
public interface DeploymentFacility {

    final static String STUBS_JARFILENAME = "clientstubs.jar";
    
    /**
     * Connects to a particular instance of the domain adminstration 
     * server using the provided connection information
     */
    public boolean connect(ServerConnectionIdentifier targetDAS);
    
    /** 
     * @return true if we are connected to a domain adminstration 
     * server
     */
    public boolean isConnected();
    
    /** 
     * Disconnects from a domain administration server and releases
     * all associated resouces.
     */
    public boolean disconnect();
        
    /**
     * Initiates a deployment operation on the server, using a source 
     * archive abstraction and an optional deployment plan if the 
     * server specific information is not embedded in the source 
     * archive. The deploymentOptions is a key-value pair map of 
     * deployment options for this operations. Once the deployment 
     * is successful, the targets server instances 
     * 
     * @param source is the j2ee module abstraction (with or without 
     * the server specific artifacts). 
     * @param deploymenPlan is the optional deployment plan is the source 
     * archive is portable.
     * @param the deployment options
     * @return a JESProgressObject to receive deployment events.
     */
    public JESProgressObject deploy(Target[] targets, Archive source, 
        Archive deploymentPlan, Map deploymentOptions);
    
    /**
     * Initiates a undeployment operation on the server 
     * @param module ID for the component to undeploy
     * @return a JESProgress to receive undeployment events
     */
    // FIXME : This will go once admin-cli changes its code
    public JESProgressObject undeploy(Target[] targets, String moduleID);

    public JESProgressObject undeploy(Target[] targets, String moduleID, Map options);
    
    /**
     * Enables a deployed component on the provided list of targets.
     */ 
    public JESProgressObject enable(Target[] targets, String moduleID);

    /**
     * Disables a deployed component on the provided list of targets
     */
    public JESProgressObject disable(Target[] targets, String moduleID);
    
    /**
     * Add an application ref on the selected targets
     */ 
    public JESProgressObject createAppRef(Target[] targets, String moduleID, Map options);

    /**
     * remove the application ref for the provided list of targets.
     */
    public JESProgressObject deleteAppRef(Target[] targets, String moduleID, Map options);    

    /**
     * list all application refs that are present in the provided list of targets
     */
    public TargetModuleID[] listAppRefs(String[] targets) throws IOException;
    
    /**
     * Downloads a particular file from the server repository. 
     * The filePath is a relative path from the root directory of the 
     * deployed component identified with the moduleID parameter. 
     * The resulting downloaded file should be placed in the 
     * location directory keeping the relative path constraint. 
     * 
     * @param location is the root directory where to place the 
     * downloaded file
     * @param moduleID is the moduleID of the deployed component 
     * to download the file from
     * @param moduleURI is the relative path to the file in the repository 
     * or STUBS_JARFILENAME to download the appclient jar file.
     * @return the downloaded local file absolute path.
     */
    public String downloadFile(File location, String moduleID, 
            String moduleURI) throws IOException;
    
    /**
     * Wait for a progress object to be in a completed state 
     * (sucessful or failed) and return the DeploymentStatus for 
     * this progress object.
     * @param the progress object to wait for completion
     * @return the deployment status
     */
    public DeploymentStatus waitFor(JESProgressObject po);
     
    public Target[] createTargets(String[] targets );
    
}
