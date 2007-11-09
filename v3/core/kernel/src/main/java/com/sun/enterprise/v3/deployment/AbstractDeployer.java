/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.deployment;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.jvnet.hk2.annotations.Inject;

import java.io.File;

/**
 * Convenient superclass for Deployer implementations.
 *
 * @author Jerome Dochez
 */
public abstract class AbstractDeployer{

    @Inject
    protected V3Environment env;

    @Inject
    ArchiveFactory archiveFactory;

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData() {
        return new MetaData(false, null);
    }

    /**
     * Prepares the application bits for running in the application server.
     * For certain cases, this is exploding the jar file to a format the
     * ContractProvider instance is expecting, generating non portable artifacts and
     * other application specific tasks.
     * Failure to prepare should throw an exception which will cause the overall
     * deployment to fail.
     *
     * @param dc deployment context
     *                TODO : @return something meaningful
     */
    public void prepare(DeploymentContext dc) {

        /*ReadableArchive sourceArchive = dc.getSource();
        File source = new File(sourceArchive.getURI());
        if (source.isDirectory()) {
            return;
        }
        Properties params = dc.getCommandParameters();
        String moduleName = params.getProperty(DeployCommand.NAME);

        ReadableArchive targetArchive;
        try {
            File target = new File(env.getApplicationRepositoryPath(), moduleName);
            new ModuleExploder().explodeJar(source, target);
            targetArchive = archiveFactory.openArchive(target);
        } catch (IOException e) {
            dc.getLogger().log(Level.SEVERE, "Exception while exploding war file", e);
            return;
        }
        dc.setSource(targetArchive);
        */

    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     */
    public void clean(DeploymentContext dc) {
        String appDir = env.getApplicationRepositoryPath();
        if (dc.getSource().getURI().getSchemeSpecificPart().startsWith(appDir)) {
            // we own this directory, let's remove it
            FileUtils.whack(new File(dc.getSource().getURI()));
        }

    }
}
