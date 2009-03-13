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
package org.glassfish.appclient.server.core;

import org.glassfish.deployment.common.DownloadableArtifacts;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.logging.Logger;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DummyApplication;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

/**
 * AppClient module deployer.
 *
 */
@Service
public class AppClientDeployer
        extends JavaEEDeployer<AppClientContainerStarter, DummyApplication> {

    private static Logger logger = LogDomains.getLogger(AppClientDeployer.class, LogDomains.ACC_LOGGER);

    private static String CLIENT_JAR_MAKER_CHOICE = System.getProperty(
            DeploymentImplConstants.CLIENT_JAR_MAKER_CHOICE);
    
    @Inject
    protected ServerContext sc;
    @Inject
    protected Domain domain;
    @Inject
    protected Habitat habitat;
    @Inject
    private DownloadableArtifacts downloadInfo;

    public AppClientDeployer() {
    }

    protected String getModuleType() {
        return "appclient";
    }

    @Override
    public MetaData getMetaData() {
        return new MetaData(false, null, new Class[]{Application.class});
    }

    @Override
    public DummyApplication load(AppClientContainerStarter containerStarter, DeploymentContext dc) {
        return new DummyApplication();
    }

    public void unload(DummyApplication application, DeploymentContext dc) {
    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param dc deployment context
     */
    @Override
    public void clean(DeploymentContext dc) {
        super.clean(dc);
    //TODO: additional cleanup?
    }

    @Override
    protected void generateArtifacts(DeploymentContext dc)
            throws DeploymentException {

        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        ReadableArchive source = dc.getSource();

//        TODO:  for EAR containing appclient modules, need a top-level ear dir?
        File appScratchDir = new File(dc.getScratchDir("xml"), params.name());
        ArchiveFactory archiveFactory = Globals.getDefaultHabitat().getComponent(ArchiveFactory.class);

//        File clientJar = new File(appScratchDir, params.name()
//            + DeploymentImplConstants.ClientJarSuffix);

        if (!needToGenerateClientJar(dc)) {
            logger.fine("No need to generate client jar for " + source.getName());
            ReadableArchive originalSource = ((ExtendedDeploymentContext) dc).getOriginalSource();
            URI originalSourceURI = originalSource.getURI();
            downloadInfo.addArtifact(params.name(), originalSourceURI, Util.getURIName(originalSourceURI));
            return;
        }

        logger.fine("Need to generate client jar for " + source.getName());

        if (params.generatermistubs) {
            //TODO
        }

//        File target = new File(appScratchDir, source.getURI().getPath());
//            WritableArchive target = archiveFactory.createArchive(clientJar);
//            downloadInfo.addDownloadJars(params.name(), );

    }

    /** Determines whether a client jar needs to be generated.  It can be skipped
     * if both conditions are met:
     * a. the deployment request does not request the deployer to generate stubs, and
     * b. there are no library JARs from an EAR that must be available to the client.

     * There will be no library JARs required if

     * (1) the app client being deployed is stand-alone (that is, not nested inside an EAR), or
     * (2) the current deployment is an EAR but:
     *    (a) the application.xml contains &lt;library-directory/&gt;
     *        (which turns off all library directory handling) or
     *    (b) the library directory (either explicit or default) contains no JARs.
     */
    private boolean needToGenerateClientJar(DeploymentContext dc) {
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        if (params.generatermistubs) {
            return true;
        }

        ApplicationClientDescriptor bundleDesc = dc.getModuleMetaData(ApplicationClientDescriptor.class);
        Application application = bundleDesc.getApplication();
        if (application.isVirtual()) {
            return false;
        }

        String libraryDirectory = application.getLibraryDirectory();
        if (libraryDirectory == null || libraryDirectory.length() == 0) {
            return false;
        }

        //        the search for *.jar should not be recursive
        File libDir = new File(dc.getSourceDir(), libraryDirectory);
        File[] jarsUnder = libDir.listFiles(new FileFilter() {

            public boolean accept(File f) {
                if (f.isFile() && f.getName().endsWith(".jar")) {
                    return true;
                }
                return false;
            }
        });

        if (jarsUnder == null || jarsUnder.length == 0) {
            return false;
        }

        return true;
    }

//    TODO remove createClientJar method
//    private void createClientJar(DeploymentContext dc,
//            ZipItem[] clientStubs) throws DeploymentException {

        //XXX: do we need to worry about upgrade scenario where the jar
        // will be stored in source dir

        // now we look if the client jar file is being requested by the client
        // tool
//        if (params.clientJarRequested) {
//
//            // the client jar file is requested upon deployment,
//            // we need to build synchronously
//            ClientJarMakerThread.createClientJar(dc,
//                clientJar, clientStubs, CLIENT_JAR_MAKER_CHOICE);
//
//        } else {
//            // the client jar file is not requested, we build it asynchronously.
//            Thread clientJarThread = new ClientJarMakerThread(dc,
//                clientJar, clientStubs, CLIENT_JAR_MAKER_CHOICE);
//            clientJarThread.start();
//        }
//    }
}
