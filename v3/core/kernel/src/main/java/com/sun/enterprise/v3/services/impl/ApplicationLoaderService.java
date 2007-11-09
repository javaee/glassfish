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
package com.sun.enterprise.v3.services.impl;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.v3.common.HTMLActionReporter;
import com.sun.enterprise.v3.contract.ApplicationMetaDataPersistence;
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.v3.data.ContainerInfo;
import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.enterprise.v3.deployment.DeploymentContextImpl;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.V3Environment;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Startup;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This service is responsible for loading all deployed applications...
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class ApplicationLoaderService extends ApplicationLifecycle
        implements Startup, PreDestroy, PostConstruct {
    
    @Inject
    GrizzlyAdapter adapter;

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    V3Environment env;

    @Inject
    DeploymentService deploymentService;

    @Inject
    ApplicationMetaDataPersistence metaData;

    Map<String, Object> startedContainers=null;

    /**
     * Retuns the lifecyle of the service. 
     * Once the applications are loaded, this service does not need to remain
     * available
     */
    public Startup.Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }
    
    /**
     * Starts the application loader service.
     * 
     * Look at the list of applications installed in our local repository
     * Get a Deployer capable for each application found 
     * Invoke the deployer load() method for each application.
     */
    public void postConstruct() {
        
        assert env!=null;
        
        startedContainers = new HashMap<String, Object>();
        File appsRoot = new File(env.getApplicationStubPath());

        if (appsRoot.exists()) {
            File[] appNames = appsRoot.listFiles();
            for (File appRoot : appNames) {
                if (appRoot.isDirectory()) {

                    processApplication(appRoot, logger);
                }
            }
        }

        // does the user want us to run a particular application
        String defaultParam = env.getStartupContext().getArguments().get("default");
        if (defaultParam!=null) {
            File sourceFile;
            if (defaultParam.equals(".")) {
                sourceFile = new File(System.getProperty("user.dir"));
            } else {
                sourceFile = new File(defaultParam);
            }

            if (sourceFile.exists()) {
                sourceFile = sourceFile.getAbsoluteFile();
                try {
                    ReadableArchive sourceArchive=null;
                    try {
                        sourceArchive = archiveFactory.openArchive(sourceFile);
                        ArchiveHandler handler = getArchiveHandler(sourceArchive);
                        ClassLoader cloader = null;
                        if (handler!=null) {
                            cloader = handler.getClassLoader(null, sourceArchive);
                        }

                        Iterable<Sniffer> appSniffers = getSniffers(sourceArchive, cloader);
                        if (appSniffers!=null) {
                            Properties deploymentProperties = new Properties();
                            deploymentProperties.setProperty(DeployCommand.NAME, sourceFile.getName());
                            DeploymentContextImpl depContext = new DeploymentContextImpl(
                                    logger,
                                    sourceArchive,
                                    deploymentProperties,
                                    env);
                            depContext.setClassLoader(cloader);

                            ActionReport report = new HTMLActionReporter();
                            load(appSniffers, depContext, report);

                        } else {
                            logger.severe("Cannot find the application type for the artifact at : "
                                    + sourceFile.getAbsolutePath());
                            logger.severe("Was the container or sniffer removed ?");
                        }
                    } finally {
                        if (sourceArchive!=null) {
                            sourceArchive.close();
                        }
                    }
                } catch(IOException e) {
                    logger.log(Level.SEVERE, "IOException while opening deployed artifact", e);

                }
            }
        }
        
    }
    

    private void processApplication(File appRoot, final Logger logger) {

        long operationStartTime = Calendar.getInstance().getTimeInMillis();

        Properties props = metaData.load(appRoot.getName());
        if (props==null) {
            return;
        }

        String sniffersType=props.getProperty("Type");
        String source = props.getProperty("Source");
        String appName = props.getProperty("Name");

        if (sniffersType == null) {
            logger.severe("Cannot determine application type at " + appRoot.getAbsolutePath());
            return;
        }
        URI uri = null;
        try {
            uri = new URI(source);
        } catch (URISyntaxException e) {
            logger.severe("Cannot determine original location for application : " + e.getMessage());
        }
        File sourceFile = new File(uri);
        if (sourceFile.exists()) {
            try {
                ReadableArchive archive = null;
                try {

                    archive = archiveFactory.openArchive(sourceFile);
                    Properties deploymentProperties = new Properties();
                    deploymentProperties.setProperty(DeployCommand.NAME, appName);

                    DeploymentContextImpl depContext = new DeploymentContextImpl(
                            logger,
                            archive,
                            deploymentProperties,
                            env);

                    depContext.setProps(props);

                    ActionReport report = new HTMLActionReporter();

                    List<Sniffer> sniffers = new ArrayList<Sniffer>();
                    StringTokenizer st = new StringTokenizer(sniffersType, " ", false);
                    while(st.hasMoreElements()) {
                        String snifferType = st.nextToken();
                        Sniffer sniffer = getSniffer(snifferType);
                        if (sniffer!=null) {
                            sniffers.add(sniffer);
                        } else {
                            logger.severe("Cannot find sniffer for module type : " + snifferType);        
                        }
                    }
                    if (sniffers.isEmpty()) {
                        logger.severe("Cannot find any sniffer for deployed app " + appName);
                        return;
                    }
                    load(sniffers, depContext, report);
                    if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
                        logger.info("Loading " + appName + " Application done is "
                                + (Calendar.getInstance().getTimeInMillis() - operationStartTime) + " ms");
                    } else {
                        logger.severe(report.getMessage());
                    }
                } finally {
                    if (archive!=null) {
                        try {
                            archive.close();
                        } catch(IOException e) {
                            logger.log(Level.FINE, e.getMessage(), e);
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException while opening deployed artifact", e);

            }
            
        } else {
            logger.severe("Application previously deployed is not at its original location any more : " + source);            
        }
    }

    @Override
    protected Deployer getDeployer(ContainerInfo containerInfo) {
        final Deployer deployer = containerInfo.getDeployer();
        assert deployer!=null;

        return new Deployer() {

            /**
             * Prepares the application bits for running in the application server.
             * For certain cases, this is exploding the jar file to a format the
             * ContractProvider instance is expecting, generating non portable artifacts and
             * other application specific tasks.
             * Failure to prepare should throw an exception which will cause the overall
             * deployment to fail.
             *
             * @param context of the deployment
             *                TODO : @return something meaningful
             */
            public void prepare(DeploymentContext context) {
                // nothig to do
            }

            /**
             * Loads a previously prepared application in its execution environment and
             * return a ContractProvider instance that will identify this environment in
             * future communications with the application's container runtime.
             *
             * @param container in which the application will reside
             * @param context   of the deployment
             * @return an ApplicationContainer instance identifying the running application
             */
            @SuppressWarnings("unchecked")
            public ApplicationContainer load(Object container, DeploymentContext context) {
                return deployer.load(container, context);
            }

            /**
             * Unload or stop a previously running application identified with the
             * ContractProvider instance. The container will be stop upon return from this
             * method.
             *
             * @param appContainer instance to be stopped
             * @param context      of the undeployment
             */
            @SuppressWarnings("unchecked")
            public void unload(ApplicationContainer appContainer, DeploymentContext context) {
                deployer.unload(appContainer, context);
            }

            /**
             * Clean any files and artifacts that were created during the execution
             * of the prepare method.
             */
            public void clean(DeploymentContext context) {
                // nothing to do
            }

            public MetaData getMetaData() {
                return deployer.getMetaData();
            }
        };
    }

    
    public String toString() {
        return "Application Loader";
    }
       
    /**
     * Stopped all loaded applications
     */
    public void preDestroy() {


        final Properties props = new Properties();
        final ActionReport dummy = new HTMLActionReporter();
        for (ContainerInfo containerInfo : containerRegistry.getContainers()) {
            final Deployer deployer = getDeployer(containerInfo);
            if (deployer==null) {
                continue;
            }
            Iterable<ApplicationInfo> apps = containerInfo.getApplications();
            for (ApplicationInfo appInfo : apps) {
                props.put(DeployCommand.NAME, appInfo.getName());
                DeploymentContextImpl depContext = new DeploymentContextImpl(
                    logger,appInfo.getSource() , props, env);
                super.unload(appInfo.getName(), depContext, dummy);
            }
        }
    }
}
