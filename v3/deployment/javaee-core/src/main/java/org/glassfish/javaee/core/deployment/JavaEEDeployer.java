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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.javaee.core.deployment;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.container.Container;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipItem;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.DeploymentPlanArchive;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.ApplicationVisitor;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.DescriptorArchivist;
import org.glassfish.deployment.common.DeploymentProperties;
import com.sun.enterprise.deployment.backend.DeploymentImplConstants;
import com.sun.enterprise.deployment.backend.ClientJarMakerThread;
import org.glassfish.deployment.common.DeploymentException;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.config.serverbeans.ServerTags;
import org.jvnet.hk2.annotations.Inject;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

/**
 * Convenient superclass for JavaEE Deployer implementations.
 *
 */
public abstract class   JavaEEDeployer<T extends Container, U extends ApplicationContainer>
        implements Deployer<T, U> {

    @Inject
    protected ServerEnvironment env;

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
    protected ApplicationRegistry appRegistry;

    @Inject(name="application_deploy", optional=true)
    protected ApplicationVisitor deploymentVisitor=null;

    @Inject(name="application_undeploy", optional=true)
    protected ApplicationVisitor undeploymentVisitor=null;

    private static String CLIENT_JAR_MAKER_CHOICE = System.getProperty(
        DeploymentImplConstants.CLIENT_JAR_MAKER_CHOICE);

    private static final String APPLICATION_TYPE = "Application-Type";

    private static String WRITEOUT_XML = System.getProperty(
        "writeout.xml");

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData() {
        return new MetaData(false, null, null);
    }


    /*
     * Gets the common instance classpath, which is composed of the
     * pathnames of domain_root/lib/classes and
     * domain_root/lib/[*.jar|*.zip] (in this
     * order), separated by the path-separator character.
     * @return The instance classpath
     */
    protected String getCommonClassPath() {
        StringBuffer sb = new StringBuffer();

        File libDir = env.getLibPath();
        String libDirPath = libDir.getAbsolutePath();

        // Append domain_root/lib/classes
        sb.append(libDirPath + File.separator + "classes");
        sb.append(File.pathSeparator);

        // Append domain_root/lib/[*.jar|*.zip]
        String[] files = libDir.list();
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                if (files[i].endsWith(".jar") || files[i].endsWith(".zip")) {
                    sb.append(libDirPath + File.separator + files[i]);
                    sb.append(File.pathSeparator);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @param type type of metadata that this deployer has declared providing.
     * @param dc deployment context
     */
    //TODO dochez, does this need to go ?
    public <V> V loadMetaData(Class<V> type, DeploymentContext dc) {
        if (dc.getModuleMetaData(type)!=null) {
            return dc.getModuleMetaData(type);
        }
        try {
            return (V) parseModuleMetaData(dc);
        } catch (Exception e) {
            dc.getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
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
            validateApplication(dc);
            
            prepareScratchDirs(dc);
            String objectType = getObjectType(dc);
            if (objectType != null) {
                dc.getProps().setProperty(ServerTags.OBJECT_TYPE,
                    objectType);
            }

            generateArtifacts(dc);
            if (Boolean.valueOf(WRITEOUT_XML)) {
                saveAppDescriptor(dc);
            }
            createClientJar(dc);
            return true;
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            throw new RuntimeException(ex.getMessage(),ex);
        }
    }

   /**
     * Loads a previously prepared application in its execution environment and
     * return a ContractProvider instance that will identify this environment in
     * future communications with the application's container runtime.
     * @param container in which the application will reside
     * @param context of the deployment
     * @return an ApplicationContainer instance identifying the running application
     */
    public U load(T container, DeploymentContext context) {
        // reset classloader on DOL object before loading so we have a 
        // valid classloader set on DOL
        Application app = context.getModuleMetaData(Application.class);
        if (app != null) {
            app.setClassLoader(context.getClassLoader()); 
        }    
        return null;
    }

    protected void validateApplication(DeploymentContext dc) {
        Application app = dc.getModuleMetaData(Application.class);
        // we only validate the application once
        if (app != null && !app.isValidated()) {
            app.setClassLoader(dc.getClassLoader());
            app.visit((ApplicationVisitor) new ApplicationValidator());
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
        archivist.setXMLValidation(false);
        archivist.setRuntimeXMLValidation(false);

        archivist.setDefaultBundleDescriptor(
                getDefaultBundleDescriptor());

        // we only expand deployment plan once in the first deployer
        if (dc.getModuleMetaData(Application.class) == null) {
            String deploymentPlan = props.getProperty(
                DeploymentProperties.DEPLOYMENT_PLAN);
            handleDeploymentPlan(deploymentPlan, archivist, sourceArchive);
        }

        Application application = applicationFactory.openArchive(
                name, archivist, sourceArchive, true);

        // this may not be the best location for this but it will suffice.
        if (deploymentVisitor!=null) {
            deploymentVisitor.accept(application);
        }

        return application;
    }

    protected void generateArtifacts(DeploymentContext dc) 
        throws DeploymentException {
    }

    protected void createClientJar(DeploymentContext dc)
        throws DeploymentException {
    }

    protected final void createClientJar(DeploymentContext dc, 
        ZipItem[] clientStubs) throws DeploymentException {
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
        String appName = context.getCommandParameters().getProperty(
            ParameterNames.NAME);
        Application app = getApplicationFromApplicationInfo(appName);
        if (app != null) {
            context.addModuleMetaData(app);
            if (undeploymentVisitor!=null) {
                undeploymentVisitor.accept(app);
            }
        }
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

    // get the object type from the application manifest file if 
    // it is present. Application can be user application or system 
    // appliction.
    protected String getObjectType(DeploymentContext context) {
        try{
            Manifest manifest = context.getSource().getManifest();
            if(manifest==null)  return null;
            Attributes attrs = manifest.getMainAttributes();
            return attrs.getValue(APPLICATION_TYPE);
        }catch(IOException e){
            // by default resource-type will be assigned "user".
            return null;
        }
    }
 
    protected Application getApplicationFromApplicationInfo(
        String appName) {
        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo == null) {
            return null;
        }

        for (ModuleInfo moduleInfo: appInfo.getModuleInfos()) {
            ApplicationContainer appCtr = moduleInfo.getApplicationContainer();
            Object descriptor = appCtr.getDescriptor();
            if (descriptor instanceof BundleDescriptor) {
                return ((BundleDescriptor)descriptor).getApplication();
            }
        }
        return null;
    }

    protected void handleDeploymentPlan(String deploymentPlan, 
        Archivist archivist, ReadableArchive sourceArchive) throws IOException {
        //Note in copying of deployment plan to the portable archive,
        //we should make sure the manifest in the deployment plan jar
        //file does not overwrite the one in the original archive
        if (deploymentPlan != null) {
            DeploymentPlanArchive dpa = new DeploymentPlanArchive();
            dpa.open(new File(deploymentPlan).toURI());
            // need to revisit for ear case
            WritableArchive targetArchive = archiveFactory.createArchive(
                sourceArchive.getURI());
            archivist.copyInto(dpa, targetArchive, false);
        } 
    }

    abstract protected RootDeploymentDescriptor getDefaultBundleDescriptor();
    abstract protected String getModuleType();
}
