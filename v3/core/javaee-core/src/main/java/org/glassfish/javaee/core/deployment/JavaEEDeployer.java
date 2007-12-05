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

package org.glassfish.javaee.core.deployment;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.interfaces.DeploymentImplConstants;
import com.sun.enterprise.deployment.backend.ClientJarMakerThread;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.v3.deployment.AbstractDeployer;
import org.jvnet.hk2.annotations.Inject;

import java.io.File;
import java.util.Properties;

/**
 * Convenient superclass for JavaEE Deployer implementations.
 *
 */
public abstract class JavaEEDeployer extends AbstractDeployer {

    @Inject
    protected V3Environment env;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    ArchivistFactory archivistFactory;
                                      
    @Inject
    ApplicationFactory applicationFactory;

    private static String CLIENT_JAR_MAKER_CHOICE = System.getProperty(
        DeploymentImplConstants.CLIENT_JAR_MAKER_CHOICE);

    /**
     * Prepares the application bits for running in the application server.
     * For certain cases, this is exploding the jar file to a format the
     * ContractProvider instance is expecting, generating non portable 
     * artifacts and other application specific tasks.
     * Failure to prepare should throw an exception which will cause the overall
     * deployment to fail.
     *
     * @param dc deployment context
     *                TODO : @return something meaningful
     */
    public void prepare(DeploymentContext dc) {
        try {
            parseModuleMetaData(dc);
            generateArtifacts(dc);
            createClientJar(dc);
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            RuntimeException re = new RuntimeException(ex.getMessage());
            re.initCause(ex);
            throw re;
        }
    }

    protected void parseModuleMetaData(DeploymentContext dc) 
        throws Exception {
        ReadableArchive sourceArchive = dc.getSource();
        ClassLoader cl = dc.getClassLoader();

        Archivist archivist = archivistFactory.getArchivist(
            sourceArchive, cl);
        archivist.setAnnotationProcessingRequested(true);

        archivist.setDefaultBundleDescriptor(
            getDefaultBundleDescriptor());

         Application application = applicationFactory.openArchive(
            archivist, sourceArchive, true);

        archivist.validate(cl);

        dc.addModuleMetaData(getModuleType(), application);
    }

    protected void generateArtifacts(DeploymentContext dc) {
    }

    protected void createClientJar(DeploymentContext dc) {
    }

    protected final void createClientJar(DeploymentContext dc, 
        ZipItem[] clientStubs) throws IASDeploymentException {
        Properties props = dc.getCommandParameters();
        String name = props.getProperty(DeploymentProperties.NAME);
        String clientJarRequested = 
            props.getProperty(DeploymentProperties.CLIENTJARREQUESTED);

        // destination file for the client jar file
        File appDirectory = dc.getScratchDir();

        // upgrade scenario
        if (!FileUtils.safeIsDirectory(dc.getScratchDir())) {
            appDirectory = dc.getSourceDir(); 
        }

        File clientJar = new File(appDirectory, name
            + DeploymentImplConstants.ClientJarSuffix);

        //XXX: do we need to worry about upgrade scenario where the jar 
        // will be stored in source dir

        // now we look if the client jar file is being requested by the client
        // tool
        if (clientJarRequested!=null &&
                Boolean.valueOf(clientJarRequested).booleanValue()) {

            // the client jar file is requested upon deployment,
            // we need to build synchronously
            ClientJarMakerThread.createClientJar(dc, 
                clientJar, clientStubs, CLIENT_JAR_MAKER_CHOICE);

        } else {
            // the client jar file is not requested, we build it asynchronously.
            Thread clientJarThread = new ClientJarMakerThread(dc, 
                clientJar, clientStubs, CLIENT_JAR_MAKER_CHOICE);
            clientJarThread.start();
        }
    }

    abstract protected RootDeploymentDescriptor getDefaultBundleDescriptor();
    abstract protected String getModuleType();
}
