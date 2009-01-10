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

import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.Named;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.deployment.common.DeploymentContextImpl;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.enterprise.v3.admin.AdminAdapter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.*;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.internal.data.*;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.deployment.Deployment;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application Loader is providing utitily methods to load applications
 *
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
public class ApplicationLifecycle implements Deployment {

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

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    protected ClassLoaderHierarchy clh;

    protected Logger logger = LogDomains.getLogger(AppServerStartup.class, LogDomains.CORE_LOGGER);
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ApplicationLifecycle.class);      

    protected <T extends Container, U extends ApplicationContainer> Deployer<T, U> getDeployer(EngineInfo<T, U> engineInfo) {
        return engineInfo.getDeployer();
    }


    /**
     * Returns the ArchiveHandler for the passed archive abstraction or null
     * if there are none.
     *
     * @param archive the archive to find the handler for
     * @return the archive handler or null if not found.
     * @throws IOException when an error occur
     */
    public ArchiveHandler getArchiveHandler(ReadableArchive archive) throws IOException {
        for (ArchiveHandler handler : habitat.getAllByContract(ArchiveHandler.class)) {
            if (handler.handles(archive)) {
                return handler;
            }
        }
        return null;
    }

    public ApplicationInfo deploy(Iterable<Sniffer> sniffers, final DeploymentContext dc, final ActionReport report) {

        final ApplicationLifecycle myself = this;
        
        // Todo. dochez, find a better solution
        final DeploymentContextImpl context = DeploymentContextImpl.class.cast(dc);

        ProgressTracker tracker = new ProgressTracker() {
            public void actOn(Logger logger) {
                for (EngineRef module : get("started", EngineRef.class)) {
                    module.stop(context, logger);
                }
                for (EngineRef module : get(EngineRef.class)) {
                    module.unload(null, context, report);
                }
                myself.clean(get(Deployer.class).toArray(new Deployer[0]), context);
            }
        };

        context.setPhase(DeploymentContextImpl.Phase.PREPARE);
        try {
            ArchiveHandler handler = getArchiveHandler(context.getSource());
            context.createClassLoaders(clh, handler);

            // containers that are started are not stopped even if the deployment fail, the main reason
            // is that some container do not support to be restarted.
            LinkedList<EngineInfo> sortedEngineInfos =
                setupContainerInfos(sniffers, context, report);
            if (sortedEngineInfos ==null || sortedEngineInfos.isEmpty()) {
                report.failure(logger, "There is no installed container capable of handling this application", null);
                tracker.actOn(logger);
                return null;
            }
            ApplicationInfo appInfo = prepare(sortedEngineInfos,
                context, report, tracker);

            appInfo = load(sortedEngineInfos, appInfo,
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
                appInfo.start(context, report, tracker);
            }

            return appInfo;

        } catch (Exception e) {
            report.failure(logger, "Exception while deploying the app", e);
            tracker.actOn(logger);
            return null;
        }
    }

    public ApplicationInfo enable(String appName, final DeploymentContextImpl context, final ActionReport report) {

        final ApplicationInfo appInfo = appRegistry.get(appName);

        final Collection<Sniffer> sniffers = snifferManager.getSniffers(context.getSource(),
                context.getClassLoader());

        if (appInfo != null) {
            // this is an enable after deploy without server restart
            ProgressTracker tracker = new ProgressTracker() {
                public void actOn(Logger logger) {
                    for (EngineRef module : get("started", EngineRef.class)) {
                        module.unload(appInfo, context, report);
                    }
                }
            };


            try {
                // construct application classloader and store in the context                 
                ArchiveHandler handler = getArchiveHandler(context.getSource());
                context.createClassLoaders(clh, handler);

                List<EngineInfo> engineInfos = new ArrayList<EngineInfo>();
                for (Sniffer sniffer : sniffers) {
                    engineInfos.add(containerRegistry.getContainer(sniffer.getContainersNames()[0]));
                }
                if (sniffers.size()==0) {
                    report.setMessage(localStrings.getLocalString("unknownmoduletpe","Module type not recognized"));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return null;
                }

                load(engineInfos, appInfo, context, report, tracker);
                appInfo.start(context, report, tracker); 
                return appInfo;

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

    public void disable(String appName, DeploymentContext context, ActionReport report) {

        unload(appName, context, report);
    }

    /**
     * Suspends this application.
     *
     * @param appName the registration application ID
     * @return true if suspending was successful, false otherwise.
     */
    public boolean suspend(String appName) {
        boolean isSuccess = true;

        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo != null) {
            isSuccess = appInfo.suspend(logger);
        }

        return isSuccess;
    }

    /**
     * Resumes this application.
     *
     * @param appName the registration application ID
     * @return true if resumption was successful, false otherwise.
     */
    public boolean resume(String appName) {
        boolean isSuccess = true;

        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo != null) {
            isSuccess = appInfo.resume(logger);
        }

        return isSuccess;
    }

    // set up containers and prepare the sorted ModuleInfos
    public LinkedList<EngineInfo> setupContainerInfos(
            Iterable<Sniffer> sniffers, DeploymentContext context,
            ActionReport report) throws Exception {

        Map<Deployer, EngineInfo> containerInfosByDeployers = new HashMap<Deployer, EngineInfo>();

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
            final String containerName = sniffer.getContainersNames()[0];

            // start all the containers associated with sniffers.
            EngineInfo engineInfo = containerRegistry.getContainer(containerName);
            if (engineInfo == null) {
                // need to synchronize on the registry to not end up starting the same container from
                // different threads.
                Collection<EngineInfo> containersInfo=null;
                synchronized (containerRegistry) {
                    if (containerRegistry.getContainer(containerName) == null) {
                        containersInfo = setupContainer(sniffer, snifferModule, logger, report);
                        if (containersInfo == null || containersInfo.size() == 0) {
                            String msg = "Cannot start container(s) associated to application of type : " + sniffer.getModuleType();
                            report.failure(logger, msg, null);
                            throw new Exception(msg);
                        }
                    }
                }

                // now start all containers, by now, they should be all setup...
                if (!startContainers(containersInfo, logger, report)) {
                    final String msg = "Aborting, Failed to start container " + containerName;
                    report.failure(logger, msg, null);
                    throw new Exception(msg);
                }
            }
            engineInfo = containerRegistry.getContainer(sniffer.getContainersNames()[0]);
            if (engineInfo ==null) {
                final String msg = "Aborting, Failed to start container " + containerName;
                report.failure(logger, msg, null);
                throw new Exception(msg);
            }
            Deployer deployer = getDeployer(engineInfo);
            if (deployer==null) {
                report.failure(logger, "Got a null deployer out of the " + engineInfo.getContainer().getClass() + " container");
                return null;
            }
            containerInfosByDeployers.put(deployer, engineInfo);
        }

        // all containers that have recognized parts of the application being deployed
        // have now been successfully started. Start the deployment process.

        List<ApplicationMetaDataProvider> providers = new LinkedList<ApplicationMetaDataProvider>();
        providers.addAll(habitat.getAllByContract(ApplicationMetaDataProvider.class));

        LinkedList<EngineInfo> sortedEngineInfos = new LinkedList<EngineInfo>();

        Map<Class, ApplicationMetaDataProvider> typeByProvider = new HashMap<Class, ApplicationMetaDataProvider>();
        for (ApplicationMetaDataProvider provider : habitat.getAllByContract(ApplicationMetaDataProvider.class)) {
            if (provider.getMetaData()!=null) {
                for (Class provided : provider.getMetaData().provides()) {
                     typeByProvider.put(provided, provider);
                }
            }
        }

        // check if everything is provided.
        for (ApplicationMetaDataProvider provider : habitat.getAllByContract(ApplicationMetaDataProvider.class)) {
            if (provider.getMetaData()!=null) {
                 for (Class dependency : provider.getMetaData().requires()) {
                     if (!typeByProvider.containsKey(dependency)) {
                         report.failure(logger, "ApplicationMetaDataProvider " + provider + " requires "
                                 + dependency + " but no other ApplicationMetaDataProvider provides it", null);
                         return null;
                     }
                 }
            }
        }

        Map<Class, Deployer> typeByDeployer = new HashMap<Class, Deployer>();
        for (Deployer deployer : containerInfosByDeployers.keySet()) {
            if (deployer.getMetaData()!=null) {
                for (Class provided : deployer.getMetaData().provides()) {
                    typeByDeployer.put(provided, deployer);
                }
            }
        }

        for (Deployer deployer : containerInfosByDeployers.keySet()) {
            if (deployer.getMetaData()!=null) {
                for (Class dependency : deployer.getMetaData().requires()) {
                    if (!typeByDeployer.containsKey(dependency) && !typeByProvider.containsKey(dependency)) {
                        report.failure(logger, "Deployer " + dependency + " requires " + deployer + " but no other deployer provides it", null);
                        return null;
                    }
                }
            }
        }

        // ok everything is satisfied, just a matter of running things in order
        LinkedList<Deployer> orderedDeployers = new LinkedList<Deployer>();
        for (Deployer deployer : containerInfosByDeployers.keySet()) {
            loadDeployer(orderedDeployers, deployer, typeByDeployer, typeByProvider, context);
        }

        // now load metadata from deployers.
        for (Deployer deployer : orderedDeployers) {

            final MetaData metadata = deployer.getMetaData();
            try {
                if (metadata!=null) {
                    if (metadata.provides()==null || metadata.provides().length==0) {
                        deployer.loadMetaData(null, context);
                    } else {
                        for (Class<?> provide : metadata.provides()) {
                            if (context.getModuleMetaData(provide)==null) {
                                context.addModuleMetaData(deployer.loadMetaData(provide, context));
                            }
                        }
                    }
                } else {
                    deployer.loadMetaData(null, context);
                }
            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method", e);
                throw e;
            }
            
            sortedEngineInfos.add(containerInfosByDeployers.get(deployer));
        }

        return sortedEngineInfos;
    }

    private void loadDeployer(LinkedList<Deployer> results, Deployer deployer, Map<Class, Deployer> typeByDeployer,  Map<Class, ApplicationMetaDataProvider> typeByProvider, DeploymentContext dc)
        throws IOException {

        if (results.contains(deployer)) {
            return;
        }
        results.addFirst(deployer);
        if (deployer.getMetaData()!=null) {
            for (Class required : deployer.getMetaData().requires()) {
                if (typeByDeployer.containsKey(required)) {
                    loadDeployer(results,typeByDeployer.get(required), typeByDeployer, typeByProvider, dc);
                } else {
                    ApplicationMetaDataProvider provider = typeByProvider.get(required);
                    if (provider==null) {
                        logger.severe("I don't get it, file a bug, no-one is providing " + required + " yet it passed validation");
                    } else {
                        LinkedList<ApplicationMetaDataProvider> providers = new LinkedList<ApplicationMetaDataProvider>();

                        addRecursively(providers, typeByProvider, provider);
                        for (ApplicationMetaDataProvider p : providers) {
                            dc.addModuleMetaData(p.load(dc));
                        }
                    }
                }
            }
        }                
    }

    private void addRecursively(LinkedList<ApplicationMetaDataProvider> results, Map<Class, ApplicationMetaDataProvider> providers, ApplicationMetaDataProvider provider) {

        results.addFirst(provider);
        for (Class type : provider.getMetaData().requires()) {
            if (providers.containsKey(type)) {
                addRecursively(results, providers, providers.get(type));
            }
        }

    }

    // prepare phase of the deployment
    public ApplicationInfo prepare(
        LinkedList<EngineInfo> sortedEngineInfos,
        DeploymentContextImpl context, ActionReport report,
        ProgressTracker tracker) throws Exception {
        
        for (EngineInfo engineInfo : sortedEngineInfos) {

            // get the deployer
            Deployer deployer = engineInfo.getDeployer();

            try {
                deployer.prepare(context);

                // construct an incomplete EngineRef which will be later
                // filled in at loading time
                EngineRef engineRef = new EngineRef(engineInfo, adapter, null);
                tracker.add(EngineRef.class, engineRef);

                tracker.add(Deployer.class, deployer);
            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method", e);
                throw e;
            }
        }

        final String appName = context.getCommandParameters().getProperty(
            ParameterNames.NAME);

        ApplicationInfo appInfo = new ApplicationInfo(context.getSource(),
            appName, tracker.get(EngineRef.class).toArray(new EngineRef[0]));

        appRegistry.add(appName, appInfo);

        return appInfo;
    }

    public ApplicationInfo load(List<EngineInfo> sortedEngineInfos,
        ApplicationInfo appInfo, DeploymentContextImpl context,
        ActionReport report, ProgressTracker tracker) throws Exception {

        context.setPhase(DeploymentContextImpl.Phase.LOAD);
        EngineRef[] engineRefs = tracker.get(EngineRef.class).toArray(new EngineRef[0]);

        if (!context.getTransformers().isEmpty()) {
            // add the class file transformers to the new class loader
            try {
                InstrumentableClassLoader icl = InstrumentableClassLoader.class.cast(context.getFinalClassLoader());
                for (ClassFileTransformer transformer : context.getTransformers()) {
                    icl.addTransformer(transformer);
                }
            } catch (Exception e) {
                report.failure(logger, "Class loader used for loading application cannot handle bytecode enhancer", e);
                throw e;
            }
        }
        for (EngineInfo engineInfo : sortedEngineInfos) {

            // get the container.
            Deployer deployer = engineInfo.getDeployer();

            ClassLoader currentClassLoader  = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(context.getClassLoader());
                ApplicationContainer appCtr = deployer.load(engineInfo.getContainer(), context);
                if (appCtr==null) {
                    String msg = "Cannot load application in " + engineInfo.getContainer().getName() + " container";
                    report.failure(logger, msg, null);
                    throw new Exception(msg);
                }

                if (engineRefs.length==0)  {
                    // if ModuleInfos have not been partially
                    // populated before
                    EngineRef engineRef = new EngineRef(engineInfo, adapter, appCtr);
                    tracker.add(EngineRef.class, engineRef);
                } else {
                    // fill in the previously partial populated EngineRef
                    for (EngineRef engineRef : engineRefs) {
                        if (engineRef.getContainerInfo().getContainer().getName().equals(engineInfo.getContainer().getName())) {
                            engineRef.setApplicationContainer(appCtr);
                            break;
                        }
                    }
                }
            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method", e);
                throw e;
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }

        if (appInfo == null) {
            String appName = context.getCommandParameters().getProperty(
                ParameterNames.NAME);
            appInfo = new ApplicationInfo(context.getSource(),
                appName, tracker.get(EngineRef.class).toArray(new EngineRef[0]));
        }

        return appInfo;
    }

    protected void clean(Deployer[] deployers, DeploymentContext context) {
        for (Deployer deployer : deployers) {
            try {
                deployer.clean(context);
            } catch(Throwable e) {
                context.getLogger().log(Level.INFO, "Deployer.clean failed for " + deployer, e);
            }
        }
    }

    protected Collection<EngineInfo> setupContainer(Sniffer sniffer, Module snifferModule,  Logger logger, ActionReport report) {
        ContainerStarter starter = habitat.getComponent(ContainerStarter.class);
        Collection<EngineInfo> containersInfo = starter.startContainer(sniffer, snifferModule);
        if (containersInfo == null || containersInfo.size()==0) {
            report.failure(logger, "Cannot start container(s) associated to application of type : " + sniffer.getModuleType(), null);
            return null;
        }
        return containersInfo;
    }

    protected boolean startContainers(Collection<EngineInfo> containersInfo, Logger logger, ActionReport report) {
        for (EngineInfo engineInfo : containersInfo) {
            Container container;
            try {
                container = engineInfo.getContainer();
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Cannot start container  " +  engineInfo.getSniffer().getModuleType(),e);
                return false;
            }
            Class<? extends Deployer> deployerClass = container.getDeployer();
            Deployer deployer;
            try {
                    deployer = habitat.getComponent(deployerClass);
                    engineInfo.setDeployer(deployer);
            } catch (ComponentException e) {
                report.failure(logger, "Cannot instantiate or inject "+deployerClass, e);
                stopContainer(logger, engineInfo);
                return false;
            } catch (ClassCastException e) {
                stopContainer(logger, engineInfo);
                report.failure(logger, deployerClass+" does not implement " +
                                    " the org.jvnet.glassfish.api.deployment.Deployer interface", e);
                return false;
            }
        }
        return true;
    }

    protected void stopContainers(EngineInfo[] ctrInfos, Logger logger) {
        for (EngineInfo ctrInfo : ctrInfos) {
            try {
                stopContainer(logger, ctrInfo);
            } catch(Exception e) {
                // this is not a failure per se but we need to document it.
                logger.log(Level.INFO,"Cannot release container " + ctrInfo.getSniffer().getModuleType(), e);
            }
        }
    }

    // Todo : take care of Deployer when unloading...
    protected void stopContainer(Logger logger, EngineInfo info)
    {
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
    }

    protected ApplicationInfo unload(String appName, DeploymentContext context, ActionReport report) {

        ApplicationInfo info = appRegistry.get(appName);
        if (info==null) {
            report.failure(context.getLogger(), "Application " + appName + " not registered", null);
            return null;

        }
        info.unload(context, report);
        return info;

    }

    public void undeploy(String appName, DeploymentContext context, ActionReport report) {

        if (report.getExtraProperties()!=null) {
            context.getProps().put("ActionReportProperties", report.getExtraProperties());
        }
        
        ApplicationInfo info = unload(appName, context, report);

        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            for (EngineRef engineRef : info.getModuleInfos()) {
                try {
                    engineRef.getContainerInfo().getDeployer().clean(context);
                } catch(Exception e) {
                    report.failure(context.getLogger(), "Exception while cleaning application artifacts", e);
                    return;
                }
            }
        }
        appRegistry.remove(appName);
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

                    if (moduleProps.getProperty(ServerTags.DESCRIPTION) !=null) {
                        app.setDescription(moduleProps.getProperty(
                                ServerTags.DESCRIPTION));
                    }
                    apps.getModules().add(app);

                    // engine element
                    for (EngineRef engineRef :
                        applicationInfo.getModuleInfos()) {
                        applicationInfo.save(app);
                        Engine engine = ConfigSupport.createChildOf(app,
                        Engine.class);
                        app.getEngine().add(engine);
                        engine.setSniffer(engineRef.getContainerInfo(
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
                            !propName.equals(ServerTags.DIRECTORY_DEPLOYED) &&
                            !propName.startsWith(
                                DeploymentProperties.APP_CONFIG))
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
                    } else {
                        // deploy to all virtual-servers, we need to get the list.
                        HttpService httpService = habitat.getComponent(HttpService.class);
                        StringBuilder sb = new StringBuilder();
                        for (VirtualServer s : httpService.getVirtualServer()) {
                            if (s.getId().equals(AdminAdapter.VS_NAME)) {
                                continue;
                            }
                            if (sb.length()>0) {
                                sb.append(',');
                            }
                            sb.append(s.getId());
                        }
                        appRef.setVirtualServers(sb.toString());
                    }
                    appRef.setEnabled(moduleProps.getProperty(
                        ServerTags.ENABLED));

                    List<ApplicationConfig> savedAppConfigs =
                            (List<ApplicationConfig>) moduleProps.get(DeploymentProperties.APP_CONFIG);
                    if (savedAppConfigs != null) {
                        for (ApplicationConfig ac : savedAppConfigs) {
                            app.getApplicationConfigs().add(ac);
                        }
                    }

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
                for (Named module : apps.getModules()) {
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
                if (Boolean.valueOf(appRef.getEnabled()) == newEnabledValue) {
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

        deploymentParams.put("APPLICATION_CONFIG", app);

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
    public void deleteContainerMetaInfo(DeploymentContext context) {

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

