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
package com.sun.enterprise.v3.server;

import com.sun.enterprise.v3.common.HTMLActionReporter;
import org.glassfish.internal.data.*;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.deployment.common.DeploymentContextImpl;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Startup;
import org.glassfish.api.event.*;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.*;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

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
public class ApplicationLoaderService implements Startup, PreDestroy, PostConstruct {

    final Logger logger = LogDomains.getLogger(AppServerStartup.class, LogDomains.CORE_LOGGER);

    @Inject
    Deployment deployment;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    SnifferManager snifferManager;

    @Inject
    ContainerRegistry containerRegistry;

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    Events events;

    @Inject
    protected Applications applications;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    ServerEnvironment env;

    private static final String IS_COMPOSITE = "isComposite";
    
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
/*        throw new RuntimeException("Just for fun !");
    }

    public void foo() {
*/
        assert env!=null;
        for (Named m : applications.getModules()) {
            if (m instanceof Application) {
                Application module = (Application) m;
                for (ApplicationRef appRef : server.getApplicationRef()) {
                    if (appRef.getRef().equals(module.getName())) {
                        if (appRef.getEnabled().equals(String.valueOf(
                            Boolean.TRUE))) {
                            // only process the application when the enable
                            // attribute is true
                            processApplication(module, appRef, logger);
                        }
                        break;
                    }
                }
            }
        }

        // does the user want us to run a particular application
        String defaultParam = env.getStartupContext().getArguments().getProperty("default");
        if (defaultParam!=null) {
            File sourceFile;
            if (defaultParam.equals(".")) {
                sourceFile = new File(System.getProperty("user.dir"));
            } else {
                sourceFile = new File(defaultParam);
            }

            Properties deploymentProperties = new Properties();
            deploymentProperties.setProperty(ParameterNames.NAME, sourceFile.getName());
            
            if (sourceFile.exists()) {
                sourceFile = sourceFile.getAbsoluteFile();
                if (!sourceFile.isDirectory()) {
                    deploymentProperties.setProperty(ParameterNames.NAME, sourceFile.getName());

                    // ok we need to explode the directory somwhere and remember to delete it on shutdown
                    try {
                        final File tmpFile = File.createTempFile(sourceFile.getName(),"");
                        final String path = tmpFile.getAbsolutePath();
                        tmpFile.delete();
                        File tmpDir = new File(path);
                        tmpDir.deleteOnExit();
                        events.register(new org.glassfish.api.event.EventListener() {
                            public void event(Event event) {
                                if (event.is(EventTypes.SERVER_SHUTDOWN)) {
                                    if (tmpFile.exists()) {
                                        FileUtils.whack(tmpFile);
                                    }
                                }
                            }
                        });
                        if (tmpDir.mkdirs()) {
                            ReadableArchive sourceArchive=null;
                            sourceArchive = archiveFactory.openArchive(sourceFile);
                            ArchiveHandler handler = deployment.getArchiveHandler(sourceArchive);
                            final String appName = handler.getDefaultApplicationName(sourceArchive);
                            deploymentProperties.setProperty(ParameterNames.NAME, appName);
                            deploymentProperties.setProperty(ParameterNames.CONTEXT_ROOT, appName);
                            handler.expand(sourceArchive, archiveFactory.createArchive(tmpDir));
                            sourceFile = tmpDir;
                            logger.info("Source is not a directory, using temporary location " + tmpDir.getAbsolutePath());
                            logger.warning("Using " + appName + " as context root for application");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }


                }
                try {
                    ReadableArchive sourceArchive=null;
                    try {
                        sourceArchive = archiveFactory.openArchive(sourceFile);

                        DeployCommandParameters parameters = new DeployCommandParameters(sourceFile);
                        parameters.name = sourceFile.getName();
                        parameters.enabled = Boolean.TRUE;

                        DeploymentContextImpl depContext = new DeploymentContextImpl(
                                logger,
                                sourceArchive,
                                parameters,
                                env, true);

                        ActionReport report = new HTMLActionReporter();
                        ApplicationInfo appInfo = deployment.deploy(depContext, report);
                        if (appInfo==null) {

                            logger.severe("Cannot find the application type for the artifact at : "
                                    + sourceFile.getAbsolutePath());
                            logger.severe("Was the container or sniffer removed ?");
                        }
                    } finally {
                        if (sourceArchive!=null) {
                            sourceArchive.close();
                        }
                    }
                } catch(Exception e) {
                    logger.log(Level.SEVERE, "IOException while opening deployed artifact", e);

                }
            }
        }

    }


    public void processApplication(Application app, ApplicationRef appRef, 
        final Logger logger) {

        long operationStartTime = Calendar.getInstance().getTimeInMillis();

        String source = app.getLocation();
        final String appName = app.getName();

        List<String> snifferTypes = new ArrayList<String>();
        for (Module module : app.getModule()) {
            for (Engine engine : module.getEngines()) {
                snifferTypes.add(engine.getSniffer());
            }
        }

        if (snifferTypes.isEmpty()) {
            logger.severe("Cannot determine application type at " + source);
            return;
        }
        URI uri = null;
        try {
            uri = new URI(source);
        } catch (URISyntaxException e) {
            logger.severe("Cannot determine original location for application : " + e.getMessage());
            return;
        }
        File sourceFile = new File(uri);
        if (sourceFile.exists()) {
            try {
                ReadableArchive archive = null;
                try {

                    archive = archiveFactory.openArchive(sourceFile);
                    DeployCommandParameters deploymentParams =
                        app.getDeployParameters(appRef);

                    DeploymentContextImpl depContext = new DeploymentContextImpl(
                            logger,
                            archive,
                            deploymentParams,
                            env,
                            true);


                    depContext.setProps(app.getDeployProperties());

                    ActionReport report = new HTMLActionReporter();

                    List<Sniffer> sniffers = new ArrayList<Sniffer>();
                    if (!Boolean.valueOf(app.getDeployProperties().getProperty
                        (IS_COMPOSITE))) {
                        for (String snifferType : snifferTypes) {
                            Sniffer sniffer = snifferManager.getSniffer(snifferType);
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
                    } else {
                        // todo, this is a cludge to force the reload and reparsing of the
                        // composite application.
                        sniffers=null;
                    }
                    deployment.deploy(sniffers, depContext, report);
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


    public String toString() {
        return "Application Loader";
    }

    /**
     * Stopped all loaded applications
     */
    public void preDestroy() {


        final ActionReport dummy = new HTMLActionReporter();
        // stop all running applications
        for (Application app : applications.getApplications()) {
            ApplicationInfo appInfo = deployment.get(app.getName());
            if (appInfo!=null) {
                UndeployCommandParameters parameters = new UndeployCommandParameters(appInfo.getName());
                DeploymentContextImpl depContext = new DeploymentContextImpl(
                    logger,appInfo.getSource() , parameters, env, false);
                appInfo.unload(depContext, dummy);
                appRegistry.remove(appInfo.getName());
            }
        }
        // stop all the containers
        for (EngineInfo engineInfo : containerRegistry.getContainers()) {
            engineInfo.stop(logger);
        }
    }

}
