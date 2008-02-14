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
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.container.Container;
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.DescriptorArchivist;
import org.glassfish.deployment.common.DeploymentProperties;
import com.sun.enterprise.deployment.backend.DeploymentImplConstants;
import com.sun.enterprise.deployment.backend.ClientJarMakerThread;
import org.glassfish.deployment.common.IASDeploymentException;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ModuleDefinition;
import org.jvnet.hk2.annotations.Inject;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

/**
 * Convenient superclass for JavaEE Deployer implementations.
 *
 */
public abstract class JavaEEDeployer<T extends Container, U extends ApplicationContainer>
        implements Deployer<T, U> {

    @Inject
    protected V3Environment env;

    @Inject
    protected ArchiveFactory archiveFactory;

    @Inject
    protected ArchivistFactory archivistFactory;
                                      
    @Inject
    protected ApplicationFactory applicationFactory;

    @Inject
    protected DescriptorArchivist descriptorArchivist;

    @Inject
    protected ApplicationArchivist applicationArchivist;

    @Inject
    protected ModulesRegistry modulesRegistry;

    private static String CLIENT_JAR_MAKER_CHOICE = System.getProperty(
        DeploymentImplConstants.CLIENT_JAR_MAKER_CHOICE);

    private static String WRITEOUT_XML = System.getProperty(
        "writeout.xml");

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData() {
        List<ModuleDefinition> apis = new ArrayList<ModuleDefinition>();
        Module module = modulesRegistry.makeModuleFor("javax.javaee:javaee", "5.0");
        if (module!=null) {
            apis.add(module.getModuleDefinition());
        }
        return new MetaData(false, apis.toArray(new ModuleDefinition[apis.size()]));
    }    

    /**
     * Prepares the application bits for running in the application server.
     * For certain cases, this is generating non portable 
     * artifacts and other application specific tasks.
     * Failure to prepare should throw an exception which will cause the overall
     * deployment to fail.
     *
     * @param dc deployment context
     * @return true if the prepare phase was successful
     *
     */
    public boolean prepare(DeploymentContext dc) {
        try {
            prepareScratchDirs(dc);
            if (parseModuleMetaData(dc)==null) {
                // hopefully the DOL gave a good message of the failure...
                dc.getLogger().severe("Failed to load deployment descriptor, aborting");
                return false;
            }
            generateArtifacts(dc);
            if (Boolean.valueOf(WRITEOUT_XML)) {
                saveAppDescriptor(dc);
            }
            createClientJar(dc);
            return true;
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            RuntimeException re = new RuntimeException(ex.getMessage());
            re.initCause(ex);
            throw re;
        }
    }

    protected Application parseModuleMetaData(DeploymentContext dc)
        throws Exception {

        ReadableArchive sourceArchive = dc.getSource();
        ClassLoader cl = dc.getClassLoader();
        Properties props = dc.getCommandParameters();
        String name = props.getProperty(DeploymentProperties.NAME);

        Archivist archivist = archivistFactory.getArchivist(
                sourceArchive, cl);
        archivist.setAnnotationProcessingRequested(true);

        archivist.setDefaultBundleDescriptor(
                getDefaultBundleDescriptor());

        Application application = applicationFactory.openArchive(
                name, archivist, sourceArchive, true);

        if (application!=null) {
            archivist.validate(cl);
            dc.addModuleMetaData(application);
        }
        return application;
    }

    protected void generateArtifacts(DeploymentContext dc) 
        throws IASDeploymentException {
    }

    protected void createClientJar(DeploymentContext dc)
        throws IASDeploymentException {
    }

    protected final void createClientJar(DeploymentContext dc, 
        ZipItem[] clientStubs) throws IASDeploymentException {
        Properties props = dc.getCommandParameters();
        String name = props.getProperty(DeploymentProperties.NAME);
        String clientJarRequested = 
            props.getProperty(DeploymentProperties.CLIENTJARREQUESTED);

        // destination file for the client jar file
        File appDirectory = dc.getScratchDir("xml");

        // upgrade scenario
        if (!FileUtils.safeIsDirectory(dc.getScratchDir("xml"))) {
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


    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param context deployment context
     */
    public void clean(DeploymentContext context) {
    }
        
    protected void saveAppDescriptor(DeploymentContext context) 
        throws IOException {
        Application application = 
            context.getModuleMetaData(Application.class);
        ReadableArchive archive = archiveFactory.openArchive(
            context.getSourceDir());
        WritableArchive archive2 = archiveFactory.createArchive(
            context.getScratchDir("xml"));
        descriptorArchivist.write(application, archive, archive2);

        // copy the additional webservice elements etc
        applicationArchivist.copyExtraElements(archive, archive2);
    }

    protected void prepareScratchDirs(DeploymentContext context) 
        throws IOException {
        context.getScratchDir("ejb").mkdirs();
        context.getScratchDir("xml").mkdirs();
        context.getScratchDir("jsp").mkdirs();
    }

    abstract protected RootDeploymentDescriptor getDefaultBundleDescriptor();
    abstract protected String getModuleType();
}
