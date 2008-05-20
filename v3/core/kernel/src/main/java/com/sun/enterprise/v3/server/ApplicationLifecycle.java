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

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.common_impl.Tokenizer;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ContainerInfo;
import org.glassfish.internal.data.ContainerRegistry;
import org.glassfish.internal.data.ModuleInfo;
import com.sun.enterprise.v3.deployment.DeploymentContextImpl;
import com.sun.enterprise.v3.deployment.EnableCommand;
import org.glassfish.api.container.EndpointRegistrationException;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application Loader is providing utitily methods to load applications
 *
 * <p>
 * TODO: this class has a "feature-envy" problem. For better encapsulation and
 * API navigability, much of these methods should be moved to ApplicationInfo/
 * ContainerInfo/ModuleInfo. For example the {@link #startModules} method
 * clearly belongs to {@link ApplicationInfo} - KK.
 *
 * <p>
 * Having admin commands extend from this is also not a very good idea
 * in terms of allowing re-wiring in different environments.
 *
 * <p>
 * For now I'm just making this class reusable on its own.
 *
 * @author Jerome Dochez
 */
@Service
public class ApplicationLifecycle {

    @Inject
    protected SnifferManager snifferManager;

    @Inject
    Habitat habitat;

    @Inject
    ContainerRegistry containerRegistry;

    @Inject
    public ApplicationRegistry appRegistry;

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    protected GrizzlyService adapter;

    @Inject
    protected ArchiveFactory archiveFactory;

    @Inject
    protected Applications applications;

    @Inject
    protected Server server;

    protected Logger logger = LogDomains.getLogger(LogDomains.DPL_LOGGER);

    protected <T extends Container, U extends ApplicationContainer> Deployer<T, U> getDeployer(ContainerInfo<T, U> containerInfo) {
        return containerInfo.getDeployer();
    }


    /**
     * Returns the ArchiveHandler for the passed archive abstraction or null
     * if there are none.
     *
     * @param archive the archive to find the handler for
     * @return the archive handler or null if not found.
     */
    public ArchiveHandler getArchiveHandler(ReadableArchive archive) throws IOException {
        for (ArchiveHandler handler : habitat.getAllByContract(ArchiveHandler.class)) {
            if (handler.handles(archive)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Sets up the parent class loader for the application class loader.
     * Application class loader are under the control of the ArchiveHandler since
     * a special archive file format will require a specific class loader.
     *
     * However GlassFish needs to be able to add capabilities to the application
     * like adding APIs accessibility, this is done through its parent class loader
     * which we create and maintain.
     *
     * @param parent the parent class loader
     * @param context deployment context 
     * @param deployers list of elligible deployers for this deployment.
     * @return class loader capable of loading public APIs identified by the deployers
     * @throws ResolveError if one of the deployer's public API module is not found.
     */
    protected ClassLoader createApplicationParentCL(ClassLoader parent, DeploymentContextImpl context, Collection<Deployer> deployers)
        throws ResolveError {

        final ReadableArchive source = context.getSource();
        // we add of the involved deployers public APIs
        List<ModuleDefinition> defs = new ArrayList<ModuleDefinition>();
        for (Deployer deployer : deployers) {
            final MetaData deployMetadata = deployer.getMetaData();
            if (deployMetadata!=null) {
                ModuleDefinition[] moduleDefs = deployMetadata.getPublicAPIs();
                if (moduleDefs!=null) {
                    defs.addAll(Arrays.asList(moduleDefs));
                }
            }
        }
        // now let's see if the application is requesting any module imports
        Manifest m=null;
        try {
            m = source.getManifest();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot load application's manifest file :", e.getMessage());
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
        }
        if (m!=null) {
            String importedBundles = m.getMainAttributes().getValue(ManifestConstants.BUNDLE_IMPORT_NAME);
            if (importedBundles!=null) {
                for( String token : new Tokenizer(importedBundles,",")) {
                    // will throw ResolveError if not found.
                    Module module = modulesRegistry.makeModuleFor(token, null);
                    if (module!=null) {
                        defs.add(module.getModuleDefinition());
                    }
                }
            }
        }

        // now maybe the deployer's have added extra APIs...
        defs.addAll(context.getPublicAPIs());

        return modulesRegistry.getModulesClassLoader(parent, defs);
    }

    public ApplicationInfo deploy(Iterable<Sniffer> sniffers, final DeploymentContextImpl context, ActionReport report) {

        final ApplicationLifecycle myself = this;

        ProgressTracker tracker = new ProgressTracker() {
            public void actOn(Logger logger) {
                myself.stopModules(get("started", ModuleInfo.class), logger);
                myself.unload(get(ModuleInfo.class), context);
                myself.clean(get(Deployer.class), context);
                stopContainers(get(ContainerInfo.class), logger);
            }
        };

        try {
            LinkedList<ContainerInfo> sortedContainerInfos =
                setupContainerInfos(sniffers, context, report, tracker);
            if (sortedContainerInfos==null || sortedContainerInfos.isEmpty()) {
                report.failure(logger, "There is no installed container capable of handling this application", null);
                tracker.actOn(logger);
                return null;
            }
            ApplicationInfo appInfo = prepare(sortedContainerInfos,
                context, report, tracker);

            appInfo = load(sortedContainerInfos, appInfo,
                context, report, tracker);

            if (appInfo == null) {
                report.failure(logger, "Exception while loading the app", null);
                tracker.actOn(logger);
                return null;
            }

            // if enable attribute is set to true
            // we start the application
            if (Boolean.valueOf(context.getCommandParameters().getProperty(
                ParameterNames.ENABLED))) {
                startModules(appInfo, context, report, tracker);
            }

            return appInfo;

        } catch (Exception e) {
            report.failure(logger, "Exception while deploying the app", e);
            tracker.actOn(logger);
            return null;
        }
    }

    public ApplicationInfo enable(Iterable<Sniffer> sniffers, final DeploymentContextImpl context, ActionReport report) {

        final ApplicationLifecycle myself = this;

        String appName = context.getCommandParameters().getProperty(
            EnableCommand.COMPONENT);

        ApplicationInfo appInfo = appRegistry.get(appName);

        if (appInfo != null) {
            // this is an enable after deploy without server restart
            ProgressTracker tracker = new ProgressTracker() {
                public void actOn(Logger logger) {
                    myself.stopModules(get("started", ModuleInfo.class),
                        logger);
                }
            };

            try {
                return startModules(appInfo, context, report, tracker);
            } catch (Exception e) {
                report.failure(logger, "Exception while enabling the app", e);
                tracker.actOn(logger);
                return null;
            }
        }  else {
            // this is an enable after server restart
            // so we need to do all the steps
            return deploy(sniffers, context, report);
        }
    }

    public void disable(String appName, ActionReport report) {

        ApplicationInfo appInfo = appRegistry.get(appName);

        if (appInfo != null) {
            stopModules(Arrays.asList(appInfo.getModuleInfos()), logger);
        }
    }

    /**
     * Suspends this application.
     *
     * @return true if suspending was successful, false otherwise.
     */
    public boolean suspend(String appName, ActionReport report) {
        boolean isSuccess = true;

        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo != null) {
            isSuccess = suspendModules(Arrays.asList(appInfo.getModuleInfos()),
                                       logger);
        }

        return isSuccess;
    }

    /**
     * Resumes this application.
     *
     * @return true if resumption was successful, false otherwise.
     */
    public boolean resume(String appName, ActionReport report) {
        boolean isSuccess = true;

        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo != null) {
            isSuccess = resumeModules(Arrays.asList(appInfo.getModuleInfos()),
                                      logger);
        }

        return isSuccess;
    }

    // set up containers and prepare the sorted ModuleInfos
    protected LinkedList<ContainerInfo> setupContainerInfos(
            Iterable<Sniffer> sniffers, DeploymentContextImpl context,
            ActionReport report, ProgressTracker tracker) throws Exception {

        //List<ContainerInfo> startedContainers = new ArrayList<ContainerInfo>();
        for (Sniffer sniffer : sniffers) {
            if (sniffer.getContainersNames() == null || sniffer.getContainersNames().length == 0) {
                report.failure(logger, "no container associated with application of type : " + sniffer.getModuleType(), null);
                return null;
            }

            Module snifferModule = modulesRegistry.find(sniffer.getClass());
            if (snifferModule == null) {
                report.failure(logger, "cannot find container module from service implementation " + sniffer.getClass(), null);
                return null;
            }

            // start all the containers associated with sniffers.
            ContainerInfo containerInfo = containerRegistry.getContainer(sniffer.getContainersNames()[0]);
            if (containerInfo == null) {
                // need to synchronize on the registry to not end up starting the same container from
                // different threads.
                synchronized (containerRegistry) {
                    if (containerRegistry.getContainer(sniffer.getContainersNames()[0]) == null) {
                        Collection<ContainerInfo> containersInfo = setupContainer(sniffer, snifferModule, logger, report);
                        if (containersInfo == null || containersInfo.size() == 0) {
                            String msg = "Cannot start container(s) associated to application of type : " + sniffer.getModuleType();
                            report.failure(logger, msg, null);
                            throw new Exception(msg);
                        }
                        tracker.addAll(ContainerInfo.class, containersInfo);
                    }
                }
            }

        }

        // now start all containers, by now, they should be all setup...
        if (!startContainers(tracker.get(ContainerInfo.class), logger, report)) {
            String msg = "Failed to start containers";
            report.failure(logger, msg, null);
            throw new Exception(msg);
        }

        // all containers that have recognized parts of the application being deployed
        // have now been successfully started. Start the deployment process.

        // sort deployers based on medata required and provided by them

        // first spearate them in buckets...
        LinkedList<ContainerInfo> sortedContainerInfos = new LinkedList<ContainerInfo>();
        Map<Class, Deployer> metaDataProvided = new HashMap<Class, Deployer>();
        Map<Class, List<Deployer>> metaDataRequired = new HashMap<Class, List<Deployer>>();
        Map<Deployer, ContainerInfo> containerInfosByDeployers = new HashMap<Deployer, ContainerInfo>();
        for (Sniffer sniffer : sniffers) {
            for (String containerName : sniffer.getContainersNames()) {
                ContainerInfo<?, ?> containerInfo = containerRegistry.getContainer(containerName);
                ClassLoader original = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(containerInfo.getClassLoader());
                    Deployer deployer = getDeployer(containerInfo);
                    containerInfosByDeployers.put(deployer, containerInfo);
                    final MetaData metadata = deployer.getMetaData();
                    Class[] requires = metadata.requires();
                    Class[] provides = metadata.provides();
                    if( (requires == null || requires.length == 0) && (provides == null || provides.length == 0) ) {
                        // the deployer neither requires not provides any metadata. Put it in sortedModuleinfo
                        // they would effectively end up being in the middle of the list (see the sorting below)
                        sortedContainerInfos.add(containerInfo);
                    } else {
                        for (Class metadataType : metadata.requires()) {
                            List<Deployer> requesters = metaDataRequired.get(metadataType);
                            if (requesters == null) {
                                //first requester deployer of this metadataType. Initialize the list
                                requesters = new LinkedList<Deployer>();
                                metaDataRequired.put(metadataType, requesters);
                            }
                            requesters.add(deployer);
                        }
                    }
                    for (Class metadataType : metadata.provides()) {
                        Deployer currentProvidindDeployer = metaDataProvided.get(metadataType);
                        if (currentProvidindDeployer != null) {
                            report.failure(logger, "More than one deployer [" + currentProvidindDeployer + ", " + deployer
                                    + "] provide same metadata : " + metadataType, null);
                        }
                        metaDataProvided.put(metadataType, deployer);
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(original);
                }
            }
        }

        // now sort...
        for (Class required : metaDataRequired.keySet()) {
            if (metaDataProvided.containsKey(required)) {
                Deployer provider = metaDataProvided.get(required);
                // TODO : better sorting job.
                sortedContainerInfos.addFirst(containerInfosByDeployers.get(provider));
                List<Deployer> requesters = metaDataRequired.get(required);
                for (Deployer requester : requesters) {
                    sortedContainerInfos.add(containerInfosByDeployers.get(requester));
                }

            } else {
                report.failure(logger, "Deployer " + metaDataRequired.get(required) + " requires " + required + " but no other deployer provides it", null);
                return null;
            }
        }

        return sortedContainerInfos;
    }

    // prepare phase of the deployment
    public ApplicationInfo prepare(
        LinkedList<ContainerInfo> sortedContainerInfos,
        DeploymentContextImpl context, ActionReport report,
        ProgressTracker tracker) throws Exception {

        List<Deployer> deployers = new ArrayList<Deployer>();
        for (ContainerInfo containerInfo : sortedContainerInfos) {
            Deployer deployer = containerInfo.getDeployer();
            deployers.add(deployer);
        }

        // Ok we now have all we need to create the parent class loader for our application
        // which will be stored in the deployment context.
        ClassLoader parentCL = createApplicationParentCL(null, context, deployers);
        ArchiveHandler handler = getArchiveHandler(context.getSource());
        context.setClassLoader(handler.getClassLoader(parentCL, context.getSource()));

        boolean invalidated = false;
        for (ContainerInfo containerInfo : sortedContainerInfos) {

            // get the deployer
            Deployer<?,?> deployer = containerInfo.getDeployer();


            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(containerInfo.getContainer().getClass().getClassLoader());
                try {
                    for (Class<?> metadata : deployer.getMetaData().provides()) {
                        context.addModuleMetaData(deployer.loadMetaData(metadata, context));
                    }
                } catch(Exception e) {
                    report.failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method", e);
                    throw e;
                }
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        }

        // now re-order to get the invalidator class loaders first...
        deployers.clear();
        for (ContainerInfo containerInfo : sortedContainerInfos) {
            Deployer deployer = containerInfo.getDeployer();
            if (deployer.getMetaData().invalidatesClassLoader()) {
                deployers.add(0, deployer);
            } else {
                deployers.add(deployer);
            }
        }

        for (ContainerInfo containerInfo : sortedContainerInfos) {

            // get the deployer
            Deployer deployer = containerInfo.getDeployer();


            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(containerInfo.getContainer().getClass().getClassLoader());
                try {
                    deployer.prepare(context);
                    if (deployer.getMetaData().invalidatesClassLoader()) {
                        invalidated = true;
                    } else {
                        if (invalidated) {
                            // we might need to flush the class loader used the load the application
                            // bits in case we ran all the prepare() methods of the invalidating
                            // deployers.
                            context.setClassLoader(handler.getClassLoader(parentCL, context.getSource()));
                            // add the class file transformers to the new class loader
                            try {
                                InstrumentableClassLoader icl = InstrumentableClassLoader.class.cast(context.getClassLoader());
                                for (ClassFileTransformer transformer : context.getTransformers()) {
                                    icl.addTransformer(transformer);
                                }
                            } catch (Exception e) {
                                report.failure(logger, "Class loader used for loading application cannot handle bytecode enhancer", e);
                                throw e;

                            }
                            invalidated = false;
                        }
                    }
                    // construct an incomplete ModuleInfo which will be later
                    // filled in at loading time
                    ModuleInfo moduleInfo = new ModuleInfo(containerInfo, null);
                    tracker.add(ModuleInfo.class, moduleInfo);

                    tracker.add(Deployer.class, deployer);
                } catch(Exception e) {
                    report.failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method", e);
                    throw e;
                }
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        }

        for (ModuleDefinition def : context.getPublicAPIs()) {
            Module module = modulesRegistry.makeModuleFor(def.getName(), def.getVersion());
            if (module!=null) {
                logger.severe("TODO : add dependencies on the fly");
            }
        }

        final String appName = context.getCommandParameters().getProperty(
            ParameterNames.NAME);

        ApplicationInfo appInfo = new ApplicationInfo(context.getSource(),
            appName, tracker.get(ModuleInfo.class).toArray(
            new ModuleInfo[tracker.get(ModuleInfo.class).size()]));

        appRegistry.add(appName, appInfo);

        return appInfo;
    }

    public ApplicationInfo load(LinkedList<ContainerInfo> sortedContainerInfos,
        ApplicationInfo appInfo, DeploymentContextImpl context,
        ActionReport report, ProgressTracker tracker) throws Exception {

        List <ModuleInfo> moduleInfos = tracker.get(ModuleInfo.class);

        for (ContainerInfo containerInfo : sortedContainerInfos) {

            // get the container.
            Deployer deployer = containerInfo.getDeployer();

            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(containerInfo.getContainer().getClass().getClassLoader());
                try {
                    ApplicationContainer appCtr = deployer.load(containerInfo.getContainer(), context);
                    if (appCtr==null) {
                        String msg = "Cannot load application in " + containerInfo.getContainer().getName() + " container";
                        report.failure(logger, msg, null);
                        throw new Exception(msg);
                    }

                    if (moduleInfos.isEmpty())  {
                        // if ModuleInfos have not been partially
                        // populated before
                        ModuleInfo moduleInfo = new ModuleInfo(containerInfo,
                            appCtr);
                        tracker.add(ModuleInfo.class, moduleInfo);
                    } else {
                        // fill in the previously partial populated ModuleInfo
                        for (ModuleInfo moduleInfo : moduleInfos) {
                            if (moduleInfo.getContainerInfo().getContainer().getName().equals(containerInfo.getContainer().getName())) {
                                moduleInfo.setApplicationContainer(appCtr);
                                break;
                            }
                        }
                    }
                } catch(Exception e) {
                    report.failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method", e);
                    throw e;
                }
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        }

        if (appInfo == null) {
            String appName = context.getCommandParameters().getProperty(
                ParameterNames.NAME);
            appInfo = new ApplicationInfo(context.getSource(),
                appName, tracker.get(ModuleInfo.class).toArray(
                new ModuleInfo[tracker.get(ModuleInfo.class).size()]));
        }

        return appInfo;
    }

    public ApplicationInfo startModules(
        ApplicationInfo appInfo, DeploymentContextImpl context,
        ActionReport report, ProgressTracker tracker) throws Exception {

        // registers all deployed items.
        for (ModuleInfo module : appInfo.getModuleInfos()) {

            try {
                module.getApplicationContainer().start(
                    context.getClassLoader());
                tracker.add("started", ModuleInfo.class, module);

                // add the endpoint
                try {
                    Adapter appAdapter = Adapter.class.cast(module.getApplicationContainer());
                    adapter.registerEndpoint(appAdapter.getContextRoot(), null, appAdapter, module.getApplicationContainer());
                } catch (ClassCastException e) {
                    // ignore the application may not publish endpoints.
                }

            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + module.getApplicationContainer().getClass() + " start method", e);
                throw e;
            }
        }

        return appInfo;
    }

    protected void stopModules(Iterable<ModuleInfo> modules, Logger logger) {

        for (ModuleInfo module : modules) {
            try {
                module.getApplicationContainer().stop();
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Cannot stop module " +
                        module.getContainerInfo().getSniffer().getModuleType(),e );
            }
        }
    }

    protected boolean suspendModules(Iterable<ModuleInfo> modules,
                                     Logger logger) {

        boolean isSuccess = true;

        for (ModuleInfo module : modules) {
            try {
                module.getApplicationContainer().suspend();
            } catch(Exception e) {
                isSuccess = false;
                logger.log(Level.SEVERE, "Error suspending module " +
                           module.getContainerInfo().getSniffer().getModuleType(),e );
            }
        }

        return isSuccess;
    }

    protected boolean resumeModules(Iterable<ModuleInfo> modules,
                                    Logger logger) {

        boolean isSuccess = true;

        for (ModuleInfo module : modules) {
            try {
                module.getApplicationContainer().resume();
            } catch(Exception e) {
                isSuccess = false;
                logger.log(Level.SEVERE, "Error resuming module " +
                           module.getContainerInfo().getSniffer().getModuleType(),e );
            }
        }

        return isSuccess;
    }

    protected void unload(Iterable<ModuleInfo> modules, DeploymentContext context) {
        for (ModuleInfo module : modules) {
            try {
                module.getContainerInfo().getDeployer().unload(module.getApplicationContainer(), context);
            } catch(Throwable e) {
                logger.log(Level.SEVERE, "Failed to unload from container type : " +
                        module.getContainerInfo().getSniffer().getModuleType(), e);
            }
        }
    }

    protected void clean(Iterable<Deployer> deployers, DeploymentContext context) {
        for (Deployer deployer : deployers) {
            try {
                deployer.clean(context);
            } catch(Throwable e) {
                context.getLogger().log(Level.INFO, "Deployer.clean failed for " + deployer, e);
            }
        }
    }

    protected Collection<ContainerInfo> setupContainer(Sniffer sniffer, Module snifferModule,  Logger logger, ActionReport report) {
        ContainerStarter starter = habitat.getComponent(ContainerStarter.class);
        Collection<ContainerInfo> containersInfo = starter.startContainer(sniffer, snifferModule);
        if (containersInfo == null || containersInfo.size()==0) {
            report.failure(logger, "Cannot start container(s) associated to application of type : " + sniffer.getModuleType(), null);
            return null;
        }
        return containersInfo;
    }

    protected boolean startContainers(Collection<ContainerInfo> containersInfo, Logger logger, ActionReport report) {
        for (ContainerInfo containerInfo : containersInfo) {
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(containerInfo.getClassLoader());
                Container container;
                try {
                    container = containerInfo.getContainer();
                } catch(Exception e) {
                    logger.log(Level.SEVERE, "Cannot start container  " +  containerInfo.getSniffer().getModuleType(),e);
                    return false;
                }
                Class<? extends Deployer> deployerClass = container.getDeployer();
                Deployer deployer;
                try {
                        deployer = habitat.getComponent(deployerClass);
                        containerInfo.setDeployer(deployer);
                } catch (ComponentException e) {
                    report.failure(logger, "Cannot instantiate or inject "+deployerClass, e);
                    stopContainer(logger, containerInfo);
                    return false;
                } catch (ClassCastException e) {
                    stopContainer(logger, containerInfo);
                    report.failure(logger, deployerClass+" does not implement " +
                                        " the org.jvnet.glassfish.api.deployment.Deployer interface", e);
                    return false;
                }
            }  finally {
                Thread.currentThread().setContextClassLoader(original);
            }

        }
        return true;
    }

    protected void stopContainers(Iterable<ContainerInfo> ctrInfos, Logger logger) {
        for (ContainerInfo ctrInfo : ctrInfos) {
            try {
                stopContainer(logger, ctrInfo);
            } catch(Exception e) {
                // this is not a failure per se but we need to document it.
                logger.log(Level.INFO,"Cannot release container " + ctrInfo.getSniffer().getModuleType(), e);
            }
        }
    }

    // Todo : take care of Deployer when unloading...
    protected void stopContainer(Logger logger, ContainerInfo info)
    {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(info.getContainer().getClass().getClassLoader());
            if (info.getDeployer()!=null) {
                Inhabitant i = habitat.getInhabitantByType(info.getDeployer().getClass());
                if (i!=null) {
                    i.release();
                }
            }
            if (info.getContainer()!=null) {
                Inhabitant i = habitat.getInhabitantByType(info.getContainer().getClass());
                if (i!=null) {
                    i.release();
                }
            }
            containerRegistry.removeContainer(info);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Container " + info.getContainer().getName() + " stopped");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    protected ApplicationInfo unload(String appName, DeploymentContext context, ActionReport report) {

        ApplicationInfo info = appRegistry.get(appName);
        if (info==null) {
            report.failure(context.getLogger(), "Application " + appName + " not registered", null);
            return null;

        }
        for (ModuleInfo moduleInfo : info.getModuleInfos()) {
            unloadModule(moduleInfo, info, context, report);
            if (!moduleInfo.getContainerInfo().getApplications().iterator().hasNext()) {
                stopContainer(context.getLogger(), moduleInfo.getContainerInfo());
            }
        }


        return info;

    }

    public void undeploy(String appName, DeploymentContext context, ActionReport report) {

        ApplicationInfo info =unload(appName, context, report);

        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            for (ModuleInfo moduleInfo : info.getModuleInfos()) {
                try {
                    moduleInfo.getContainerInfo().getDeployer().clean(context);
                } catch(Exception e) {
                    report.failure(context.getLogger(), "Exception while cleaning application artifacts", e);
                    return;
                }
            }
        }
        appRegistry.remove(appName);
    }

    protected boolean unloadModule(ModuleInfo module,
                                     ApplicationInfo info,
                                     DeploymentContext context,
                                     ActionReport report) {

        // remove any endpoints if exists.
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            if (module.getApplicationContainer().getClassLoader()!=null) {
                Thread.currentThread().setContextClassLoader(module.getApplicationContainer().getClassLoader());
            } else {
                Thread.currentThread().setContextClassLoader(module.getContainerInfo().getContainer().getClass().getClassLoader());
            }
            
            //@TODO change EndportRegistrationException processing if required
            try {
                final Adapter appAdapter = Adapter.class.cast(module.getApplicationContainer());
                adapter.unregisterEndpoint(appAdapter.getContextRoot(), module.getApplicationContainer());
            } catch (EndpointRegistrationException e) {
                logger.log(Level.WARNING, "Exception during unloading module '" + 
                        module + "'", e);
            } catch(ClassCastException e) {
                // do nothing the application did not have an adapter
            }


            // first stop the application
            try {
                if (!module.getApplicationContainer().stop()) {
                    logger.severe("Cannot stop application " + info.getName() + " in container "
                            + module.getContainerInfo().getSniffer().getModuleType());
                }
            } catch(Exception e) {
                report.failure(context.getLogger(), "Exception while stopping the application", e);
                return false;
            }

            // then remove the application from the container
            Deployer deployer = module.getContainerInfo().getDeployer();
            try {
                deployer.unload(module.getApplicationContainer(), context);
            } catch(Exception e) {
                report.failure(context.getLogger(), "Exception while shutting down application container", e);
                return false;
            }
            module.getContainerInfo().remove(info);
            return true;
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    // register application information in domain.xml
    protected void registerAppInDomainXML(final ApplicationInfo
        applicationInfo, final DeploymentContext context)
        throws TransactionFailure {
        final Properties moduleProps = context.getProps();
        ConfigSupport.apply(new ConfigCode() {
            public Object run(ConfigBeanProxy... params) throws PropertyVetoException, TransactionFailure {

                    Applications apps = (Applications) params[0];
                    Server servr = (Server) params[1];

                    // adding the application element
                    Application app = ConfigSupport.createChildOf(params[0], Application.class);

                    // various attributes
                    app.setName(moduleProps.getProperty(ServerTags.NAME));
                    app.setLocation(moduleProps.getProperty(
                        ServerTags.LOCATION));
                    app.setObjectType(moduleProps.getProperty(
                        ServerTags.OBJECT_TYPE));
                    // always set the enable attribute of application to true
        		    app.setEnabled(String.valueOf(true));
                    if (moduleProps.getProperty(ServerTags.CONTEXT_ROOT) !=
                        null) {
		            app.setContextRoot(moduleProps.getProperty(
                                ServerTags.CONTEXT_ROOT));
                    }
                    if (moduleProps.getProperty(ServerTags.LIBRARIES) !=
                        null) {
		            app.setLibraries(moduleProps.getProperty(
                                ServerTags.LIBRARIES));
                    }
                    app.setDirectoryDeployed(moduleProps.getProperty(
                        ServerTags.DIRECTORY_DEPLOYED));

                    apps.getModules().add(app);

                    // engine element
                    for (ModuleInfo moduleInfo :
                        applicationInfo.getModuleInfos()) {
                        Engine engine = ConfigSupport.createChildOf(app,
                        Engine.class);
                        app.getEngine().add(engine);
                        engine.setSniffer(moduleInfo.getContainerInfo(
                            ).getSniffer().getModuleType());
                    }

                    // property element
                    // trim the properties that have been written as attributes
                    // the rest properties will be written as property element
                    for (Iterator itr = moduleProps.keySet().iterator();
                        itr.hasNext();) {
                        String propName = (String) itr.next();
                        if (!propName.equals(ServerTags.NAME) &&
                            !propName.equals(ServerTags.LOCATION) &&
                            !propName.equals(ServerTags.ENABLED) &&
                            !propName.equals(ServerTags.CONTEXT_ROOT) &&
                            !propName.equals(ServerTags.LIBRARIES) &&
                            !propName.equals(ServerTags.OBJECT_TYPE) &&
                            !propName.equals(ServerTags.VIRTUAL_SERVERS) &&
                            !propName.equals(ServerTags.DIRECTORY_DEPLOYED))
                        {
                            Property prop = ConfigSupport.createChildOf(app,
                                Property.class);
                            app.getProperty().add(prop);
                            prop.setName(propName);
                            prop.setValue(moduleProps.getProperty(propName));
                        }
                    }

                    // adding the application-ref element
                    ApplicationRef appRef = ConfigSupport.createChildOf(
                            params[1], ApplicationRef.class);
                    appRef.setRef(moduleProps.getProperty(ServerTags.NAME));
                    if (moduleProps.getProperty(
                        ServerTags.VIRTUAL_SERVERS) != null) {
                        appRef.setVirtualServers(moduleProps.getProperty(
                            ServerTags.VIRTUAL_SERVERS));
                    }
                    appRef.setEnabled(moduleProps.getProperty(
                        ServerTags.ENABLED));

                    servr.getApplicationRef().add(appRef);
                    
                    return Boolean.TRUE;
                }

        }, applications, server);
    }

    protected void unregisterAppFromDomainXML(final String appName)
        throws TransactionFailure {
        ConfigSupport.apply(new ConfigCode() {
            public Object run(ConfigBeanProxy... params) throws PropertyVetoException, TransactionFailure {
                Applications apps = (Applications) params[0];
                Server servr = (Server) params[1];
                // remove application-ref element
                for (ApplicationRef appRef : servr.getApplicationRef()) {
                    if (appRef.getRef().equals(appName)) {
                        ((Server)params[1]).getApplicationRef().remove(appRef);
                        break;
                    }
                }

                // remove application element
                for (com.sun.enterprise.config.serverbeans.Module module :
                    apps.getModules()) {
                    if (module.getName().equals(appName)) {
                        ((Applications)params[0]).getModules().remove(module);
                        break;
                    }
                }
                return Boolean.TRUE;
            }
        }, applications, server);
    }

    // this is to update the enable attribute in domain.xml with the new value
    protected void setEnableAttributeInDomainXML(final String appName,
        final boolean newEnabledValue) throws Exception {

        // the enable attribute of the application element is always
        // set to true
        // we use the enable attribute of the application-ref to control
        // whether the application should be enabled or not
        ApplicationRef applicationRef = null;

        for (ApplicationRef appRef : server.getApplicationRef()) {
            if (appRef.getRef().equals(appName)) {
                applicationRef = appRef;
                if (appRef.getEnabled().equals(newEnabledValue)) {
                    // no need to set again, return
                    return;
                }
                break;
            }
        }

        if (applicationRef == null) {
            throw new Exception("Application Ref not found for " + appName +
                " in configuration");
        }

        ConfigSupport.apply(new SingleConfigCode<ApplicationRef>() {
            public Object run(ApplicationRef param) throws
                PropertyVetoException, TransactionFailure {
                param.setEnabled(String.valueOf(newEnabledValue));
                return null;
            }
        }, applicationRef);
    }

    // set the neccessary information in DeploymentContext params from
    // domain.xml
    protected Properties populateDeployParamsFromDomainXML(Application app, ApplicationRef appRef) {
        if (app == null || appRef == null) {
            return new Properties();
        }
        Properties deploymentParams = new Properties();
        deploymentParams.setProperty(ParameterNames.NAME, app.getName());
        deploymentParams.setProperty(ParameterNames.LOCATION, app.getLocation());
        deploymentParams.setProperty(ParameterNames.ENABLED, app.getEnabled());
        if (app.getContextRoot() != null) {
            deploymentParams.setProperty(ParameterNames.CONTEXT_ROOT,
                app.getContextRoot());
        }
        if (app.getLibraries() != null) {
            deploymentParams.setProperty(ParameterNames.LIBRARIES,
                app.getLibraries());
        }
        deploymentParams.setProperty(ParameterNames.DIRECTORY_DEPLOYED,
            app.getDirectoryDeployed());

        if (appRef.getVirtualServers() != null) {
            deploymentParams.setProperty(ParameterNames.VIRTUAL_SERVERS,
                appRef.getVirtualServers());
        }

        return deploymentParams;
    }

    // set the neccessary information in DeploymentContext props from
    // domain.xml
    protected Properties populateDeployPropsFromDomainXML(Application app) {
        if (app == null) {
            return new Properties();
        }
        Properties deploymentProps = new Properties();
        for (Property prop : app.getProperty()) {
            deploymentProps.put(prop.getName(), prop.getValue());
        }
        deploymentProps.setProperty(ServerTags.OBJECT_TYPE,
            app.getObjectType());
        return deploymentProps;
    }

    // check if the application is registered in domain.xml
    protected boolean isRegistered(String appName) {
        return ConfigBeansUtilities.getModule(appName)!=null;
    }

    // clean up generated files
    protected void deleteContainerMetaInfo(DeploymentContext context) {

        // need to remove the generated directories...
        // need to remove generated/xml, generated/ejb, generated/jsp

        // remove generated/xml
        File generatedXmlRoot = context.getScratchDir("xml");
        FileUtils.whack(generatedXmlRoot);

        // remove generated/ejb
        File generatedEjbRoot = context.getScratchDir("ejb");
        // recursively delete...
        FileUtils.whack(generatedEjbRoot);

        // remove generated/jsp
        File generatedJspRoot = context.getScratchDir("jsp");
        // recursively delete...
        FileUtils.whack(generatedJspRoot);
    }
}
