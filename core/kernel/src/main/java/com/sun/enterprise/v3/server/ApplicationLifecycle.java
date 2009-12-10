/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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

package com.sun.enterprise.v3.server;

import org.glassfish.deployment.common.ApplicationConfigInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.config.serverbeans.*;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.api.admin.config.ApplicationName;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.deployment.common.DeploymentContextImpl;
import com.sun.enterprise.v3.admin.AdminAdapter;
import com.sun.logging.LogDomains;
import com.sun.hk2.component.*;
import org.glassfish.api.*;
import org.glassfish.api.event.*;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.*;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.CompositeHandler;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.data.*;
import org.glassfish.internal.api.*;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.File;
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
@Scoped(Singleton.class)
public class ApplicationLifecycle implements Deployment {
        

    @Inject
    protected SnifferManagerImpl snifferManager;

    @Inject
    Habitat habitat;

    @Inject
    ContainerRegistry containerRegistry;

    @Inject
    public ApplicationRegistry appRegistry;

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    protected Applications applications;

    @Inject
    protected Domain domain;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    Events events;

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

        // first we try the composite handlers as archive handlers can be fooled with the
        // sub directories and such.
        for (CompositeHandler handler : habitat.getAllByContract(CompositeHandler.class)) {
            if (handler.handles(archive)) {
                return handler;
            }
        }

        // re-order the list so the ConnectorHandler gets picked up last 
        // before the default
        // this will avoid un-necessary annotation scanning in some cases
        LinkedList<ArchiveHandler> handlerList = new LinkedList<ArchiveHandler>();
        for (ArchiveHandler handler : habitat.getAllByContract(ArchiveHandler.class)) {
            if (!(handler instanceof CompositeHandler) && !"DEFAULT".equals(handler.getClass().getAnnotation(Service.class).name())) {
                if ("connector".equals(handler.getClass().getAnnotation(
                    Service.class).name())) {
                    handlerList.addLast(handler);
                } else {
                    handlerList.addFirst(handler);
                }
            }
        }

        for (ArchiveHandler handler : handlerList) {
            if (handler.handles(archive)) {
                return handler;
            }
        }

        return habitat.getComponent(ArchiveHandler.class, "DEFAULT");
    }

    public ApplicationInfo deploy(final ExtendedDeploymentContext context) {
        return deploy(null, context);
    }

    public ApplicationInfo deploy(Collection<Sniffer> sniffers, final ExtendedDeploymentContext context) {

        events.send(new Event<DeploymentContext>(Deployment.DEPLOYMENT_START, context));
        final ActionReport report = context.getActionReport();

        final DeployCommandParameters commandParams = context.getCommandParameters(DeployCommandParameters.class);

        final String appName = commandParams.name();
        if (commandParams.origin == OpsParams.Origin.deploy && 
            appRegistry.get(appName) != null) {
            report.setMessage(localStrings.getLocalString("appnamenotunique","Application name {0} is already in use. Please pick a different name.", appName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return null;                
        }

        // if the virtualservers param is not defined, set it to all
        // defined virtual servers minus __asadmin on that target
        if (commandParams.virtualservers == null) {
            commandParams.virtualservers = getVirtualServers(
                commandParams.target);
        }
        
        ProgressTracker tracker = new ProgressTracker() {
            public void actOn(Logger logger) {
                appRegistry.remove(appName);

                for (EngineRef module : get("started", EngineRef.class)) {
                    module.stop(context);
                }
                try {
                    PreDestroy.class.cast(context).preDestroy();
                } catch (Exception e) {

                }                
                for (EngineRef module : get("loaded", EngineRef.class)) {
                    module.unload(context);
                }
                for (EngineRef module : get("prepared", EngineRef.class)) {
                    module.clean(context);
                }
                if (!commandParams.keepfailedstubs) {
                    context.clean();
                }
            }
        };

        context.addTransientAppMetaData(ExtendedDeploymentContext.TRACKER, 
            tracker);
        context.setPhase(DeploymentContextImpl.Phase.PREPARE);
        ApplicationInfo appInfo = null;
        try {
            ArchiveHandler handler = context.getArchiveHandler();
            if (handler == null) {
                handler = getArchiveHandler(context.getSource());
                context.setArchiveHandler(handler);
            }
            if (handler==null) {
                report.setMessage(localStrings.getLocalString("unknownarchivetype","Archive type of {0} was not recognized",context.getSource()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return null;                
            }
            ClassLoaderHierarchy clh = habitat.getByContract(ClassLoaderHierarchy.class);
            context.createDeploymentClassLoader(clh, handler);

            final ClassLoader cloader = context.getClassLoader();
            final ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cloader);
                
                // containers that are started are not stopped even if the deployment fail, the main reason
                // is that some container do not support to be restarted.
                if (sniffers!=null && logger.isLoggable(Level.FINE)) {
                    for (Sniffer sniffer : sniffers) {
                        logger.fine("Before Sorting" + sniffer.getModuleType());
                    }
                }
                List<EngineInfo> sortedEngineInfos =
                    setupContainerInfos(handler, sniffers, context);

                if (logger.isLoggable(Level.FINE)) {
                    for (EngineInfo info : sortedEngineInfos) {
                        logger.fine("After Sorting " + info.getSniffer().getModuleType());
                    }
                }
                if (sortedEngineInfos ==null || sortedEngineInfos.isEmpty()) {
                    report.failure(logger, localStrings.getLocalString("unknowncontainertype","There is no installed container capable of handling this application {0}",context.getSource()));                    
                    tracker.actOn(logger);
                    return null;
                }


                // create a temporary application info to hold metadata
                // so the metadata could be accessed at classloader 
                // construction time through ApplicationInfo
                ApplicationInfo tempAppInfo = new ApplicationInfo(events, 
                    context.getSource(), appName);
                for (Object m : context.getModuleMetadata()) {
                    tempAppInfo.addMetaData(m);
                }
                tempAppInfo.setIsJavaEEApp(sortedEngineInfos);
                appRegistry.add(appName, tempAppInfo);

                context.createApplicationClassLoader(clh, handler);


                    // this is a first time deployment as opposed as load following an unload event,
                    // we need to create the application info
                    // todo : we should come up with a general Composite API solution
                    ModuleInfo moduleInfo = null;
                    try {
                        moduleInfo = prepareModule(sortedEngineInfos, appName, context, tracker);

                    } catch(Exception prepareException) {
                        report.failure(logger, "Exception while preparing the app", prepareException);
                        tracker.actOn(logger);
                        return null;
                    }

                    // the deployer did not take care of populating the application info, this
                    // is not a composite module.
                    appInfo=context.getModuleMetaData(ApplicationInfo.class);
                    if (appInfo==null) {
                        appInfo = new ApplicationInfo(events, context.getSource(), appName);
                        appInfo.addModule(moduleInfo);

                        for (Object m : context.getModuleMetadata()) {
                            moduleInfo.addMetaData(m);
                            appInfo.addMetaData(m);
                        }
                    } else {
                        for (EngineRef ref : moduleInfo.getEngineRefs()) {
                            appInfo.add(ref);
                        }
                    }

                // remove the temp application info from the registry
                // first, then register the real one
                appRegistry.remove(appName);
                appInfo.setIsJavaEEApp(sortedEngineInfos);
                appRegistry.add(appName, appInfo);

                events.send(new Event<DeploymentContext>(Deployment.APPLICATION_PREPARED, context), false);

                // now were falling back into the mainstream loading/starting sequence, at this
                // time the containers are set up, all the modules have been prepared in their
                // associated engines and the application info is created and registered
                 // if enable attribute is set to true
                 // we load and start the application
                if (commandParams.enabled) {
                    Thread.currentThread().setContextClassLoader(context.getFinalClassLoader());
                    appInfo.setLibraries(commandParams.libraries());
                    try {
                        appInfo.load(context, tracker);
                        appInfo.start(context, tracker);
                    } catch(Exception loadException) {
                        report.failure(logger, "Exception while loading the app", loadException);
                        tracker.actOn(logger);
                        return null;
                    }
                }
                return appInfo;
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }

        } catch (Exception e) {
            report.failure(logger, "Exception while deploying the app", e);
            tracker.actOn(logger);
            return null;
        } finally {
            if (report.getActionExitCode()==ActionReport.ExitCode.SUCCESS) {
                events.send(new Event<ApplicationInfo>(Deployment.DEPLOYMENT_SUCCESS, appInfo));
            } else {
                events.send(new Event<DeploymentContext>(Deployment.DEPLOYMENT_FAILURE, context));
            }
        }
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

    public List<EngineInfo> setupContainerInfos(DeploymentContext context)
        throws Exception {

        return setupContainerInfos(null, null, context);
    }

    // set up containers and prepare the sorted ModuleInfos
    public List<EngineInfo> setupContainerInfos(final ArchiveHandler handler,
            Collection<Sniffer> sniffers, DeploymentContext context)
             throws Exception {

        final ActionReport report = context.getActionReport();
        if (sniffers==null) {
            ReadableArchive source=context.getSource();
            if (handler instanceof CompositeHandler) {
                context.getAppProps().setProperty(ServerTags.IS_COMPOSITE, "true");
                sniffers = snifferManager.getCompositeSniffers(context);
            } else {
                sniffers = snifferManager.getSniffers(source, context.getClassLoader());
            }

        }

        if (sniffers.size()==0) {
            report.failure(logger,localStrings.getLocalString("deploy.unknownmoduletpe","Module type not recognized"));
            return null;
        }

        snifferManager.validateSniffers(sniffers, context);

        Map<Deployer, EngineInfo> containerInfosByDeployers = new LinkedHashMap<Deployer, EngineInfo>();

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
                        containersInfo = setupContainer(sniffer, snifferModule, logger, context);
                        if (containersInfo == null || containersInfo.size() == 0) {
                            String msg = "Cannot start container(s) associated to application of type : " + sniffer.getModuleType();
                            report.failure(logger, msg, null);
                            throw new Exception(msg);
                        }
                    }
                }

                // now start all containers, by now, they should be all setup...
                if (!startContainers(containersInfo, logger, context)) {
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
                if (!startContainers(Collections.singleton(engineInfo), logger, context)) {
                    final String msg = "Aborting, Failed to start container " + containerName;
                    report.failure(logger, msg, null);
                    throw new Exception(msg);
                }
                deployer = getDeployer(engineInfo);

                if (deployer == null) {
                     report.failure(logger, "Got a null deployer out of the " + engineInfo.getContainer().getClass() + " container, is it annotated with @Service ?");
                     return null;
                } 
             } 
            containerInfosByDeployers.put(deployer, engineInfo);
        }

        // all containers that have recognized parts of the application being deployed
        // have now been successfully started. Start the deployment process.

        List<ApplicationMetaDataProvider> providers = new LinkedList<ApplicationMetaDataProvider>();
        providers.addAll(habitat.getAllByContract(ApplicationMetaDataProvider.class));

        List<EngineInfo> sortedEngineInfos = new ArrayList<EngineInfo>();

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
                         // at this point, I only log problems, because it maybe that what I am deploying now
                         // will not require this application metadata.
                         logger.warning("ApplicationMetaDataProvider " + provider + " requires "
                                 + dependency + " but no other ApplicationMetaDataProvider provides it");
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
                        Service s = deployer.getClass().getAnnotation(Service.class);
                        String serviceName;
                        if (s!=null && s.name()!=null && s.name().length()>0) {
                            serviceName = s.name();
                        } else {
                            serviceName = deployer.getClass().getSimpleName();
                        }
                        report.failure(logger, serviceName + " deployer requires " + dependency + " but no other deployer provides it", null);
                        return null;
                    }
                }
            }
        }

        // ok everything is satisfied, just a matter of running things in order
        List<Deployer> orderedDeployers = new ArrayList<Deployer>();
        for (Deployer deployer : containerInfosByDeployers.keySet()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Keyed Deployer " + deployer.getClass());   
            }
            loadDeployer(orderedDeployers, deployer, typeByDeployer, typeByProvider, context);
        }

        // now load metadata from deployers.
        for (Deployer deployer : orderedDeployers) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Ordered Deployer " + deployer.getClass());
            }

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

    private void loadDeployer(List<Deployer> results, Deployer deployer, Map<Class, Deployer> typeByDeployer,  Map<Class, ApplicationMetaDataProvider> typeByProvider, DeploymentContext dc)
        throws IOException {

        if (results.contains(deployer)) {
            return;
        }
        results.add(deployer);
        if (deployer.getMetaData()!=null) {
            for (Class required : deployer.getMetaData().requires()) {
                if (dc.getModuleMetaData(required)!=null) {
                    continue;
                }
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

    public ModuleInfo prepareModule(
        List<EngineInfo> sortedEngineInfos, String moduleName,
        DeploymentContext context,
        ProgressTracker tracker) throws Exception {

        ActionReport report = context.getActionReport();
        List<EngineRef> addedEngines = new ArrayList<EngineRef>();
        for (EngineInfo engineInfo : sortedEngineInfos) {

            // get the deployer
            Deployer deployer = engineInfo.getDeployer();

            try {
                deployer.prepare(context);

                // construct an incomplete EngineRef which will be later
                // filled in at loading time
                EngineRef engineRef = new EngineRef(engineInfo, null);
                addedEngines.add(engineRef);
                tracker.add("prepared", EngineRef.class, engineRef);

                tracker.add(Deployer.class, deployer);
            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method", e);
                throw e;
            }
        }
        if (events!=null) {
            events.send(new Event<DeploymentContext>(Deployment.MODULE_PREPARED, context), false);
        }
        // I need to create the application info here from the context, or something like this.
        // and return the application info from this method for automatic registration in the caller.

        // set isComposite property on module props so we know whether to persist 
        // module level properties inside ModuleInfo
        String isComposite = context.getAppProps().getProperty(
            ServerTags.IS_COMPOSITE);
        if (isComposite != null) {
            context.getModuleProps().setProperty(ServerTags.IS_COMPOSITE, isComposite);
        }

        ModuleInfo mi = new ModuleInfo(events, moduleName, addedEngines,
            context.getModuleProps());

        /*
         * Save the application config that is potentially attached to each
         * engine in the corresponding EngineRefs that have already created.
         * 
         * Later, in registerAppInDomainXML, the appInfo is saved, which in
         * turn saves the moduleInfo children and their engineRef children.
         * Saving the engineRef assigns the application config to the Engine
         * which corresponds directly to the <engine> element in the XML.
         * A long way to get this done.
         */

//        Application existingApp = applications.getModule(Application.class, moduleName);
//        if (existingApp != null) {
            ApplicationConfigInfo savedAppConfig = new ApplicationConfigInfo(context.getAppProps());
            for (EngineRef er : mi.getEngineRefs()) {
               ApplicationConfig c = savedAppConfig.get(mi.getName(),
                       er.getContainerInfo().getSniffer().getModuleType());
               if (c != null) {
                   er.setApplicationConfig(c);
               }
            }
//        }
        return mi;
    }

    protected Collection<EngineInfo> setupContainer(Sniffer sniffer, Module snifferModule,  Logger logger, DeploymentContext context) {
        ActionReport report = context.getActionReport();
        ContainerStarter starter = habitat.getComponent(ContainerStarter.class);
        Collection<EngineInfo> containersInfo = starter.startContainer(sniffer, snifferModule);
        if (containersInfo == null || containersInfo.size()==0) {
            report.failure(logger, "Cannot start container(s) associated to application of type : " + sniffer.getModuleType(), null);
            return null;
        }
        return containersInfo;
    }

    protected boolean startContainers(Collection<EngineInfo> containersInfo, Logger logger, DeploymentContext context) {
        ActionReport report = context.getActionReport();
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
                engineInfo.stop(logger);
                return false;
            } catch (ClassCastException e) {
                engineInfo.stop(logger);
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
                ctrInfo.stop(logger);
            } catch(Exception e) {
                // this is not a failure per se but we need to document it.
                logger.log(Level.INFO,"Cannot release container " + ctrInfo.getSniffer().getModuleType(), e);
            }
        }
    }

    protected ApplicationInfo unload(String appName, ExtendedDeploymentContext context) {
        ActionReport report = context.getActionReport();
        ApplicationInfo info = appRegistry.get(appName);
        if (info==null) {
            report.failure(context.getLogger(), "Application " + appName + " not registered", null);
            return null;

        }
        info.stop(context, context.getLogger());
        info.unload(context);
        return info;

    }

    public void undeploy(String appName, ExtendedDeploymentContext context) {

        ActionReport report = context.getActionReport();
        if (report.getExtraProperties()!=null) {
            context.getAppProps().put("ActionReportProperties", report.getExtraProperties());
        }

        ApplicationInfo info = appRegistry.get(appName);
        if (info==null) {
            report.failure(context.getLogger(), "Application " + appName + " not registered", null);
            events.send(new Event(Deployment.UNDEPLOYMENT_FAILURE, context));
            return;

        }

        events.send(new Event<DeploymentContext>(Deployment.UNDEPLOYMENT_VALIDATION, context), false);

        if (report.getActionExitCode()==ActionReport.ExitCode.FAILURE) {
            // if one of the validation listeners sets the action report 
            // status as failure, return
            return;
        }

        events.send(new Event(Deployment.UNDEPLOYMENT_START, info));

        unload(appName, context);
        try {
            info.clean(context);
        } catch(Exception e) {
            report.failure(context.getLogger(), "Exception while cleaning application artifacts", e);
            events.send(new Event(Deployment.UNDEPLOYMENT_FAILURE, context));
            return;
        }
        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            events.send(new Event(Deployment.UNDEPLOYMENT_SUCCESS, context));
        } else {            
            events.send(new Event(Deployment.UNDEPLOYMENT_FAILURE, context));            
        }
        
        appRegistry.remove(appName);
        info = null;
    }

    // register application information in domain.xml
    public void registerAppInDomainXML(final ApplicationInfo
        applicationInfo, final DeploymentContext context)
        throws TransactionFailure {
        final Properties appProps = context.getAppProps();
        final DeployCommandParameters deployParams = context.getCommandParameters(DeployCommandParameters.class);
        ConfigSupport.apply(new ConfigCode() {
            public Object run(ConfigBeanProxy... params) throws PropertyVetoException, TransactionFailure {

                Applications apps = (Applications) params[0];
                Server servr = (Server) params[1];

                // adding the application element
                Application app = params[0].createChild(Application.class);

                // various attributes
                app.setName(deployParams.name);
                if (deployParams.libraries != null) {
                    app.setLibraries(deployParams.libraries);
                }
                if (deployParams.description != null) {
                    app.setDescription(deployParams.description);
                }

                if (appProps.getProperty(ServerTags.CONTEXT_ROOT) != null) {
                    app.setContextRoot(appProps.getProperty(
                        ServerTags.CONTEXT_ROOT));
                }
                if (appProps.getProperty(ServerTags.LOCATION) != null) {
                    app.setLocation(appProps.getProperty(
                        ServerTags.LOCATION));
                    // always set the enable attribute of application to true
                    app.setEnabled(String.valueOf(true));
                } else {
                    // this is not a regular javaee module 
                    app.setEnabled(deployParams.enabled.toString());
                }
                if (appProps.getProperty(ServerTags.OBJECT_TYPE) != null) {
                    app.setObjectType(appProps.getProperty(
                        ServerTags.OBJECT_TYPE));
                }
                if (appProps.getProperty(ServerTags.DIRECTORY_DEPLOYED) 
                    != null) {
                    app.setDirectoryDeployed(appProps.getProperty(
                        ServerTags.DIRECTORY_DEPLOYED));
                }


                apps.getModules().add(app);
             
                if (applicationInfo != null) {
                    applicationInfo.save(app);
                }

                // property element
                // trim the properties that have been written as attributes
                // the rest properties will be written as property element
                for (Iterator itr = appProps.keySet().iterator();
                    itr.hasNext();) {
                    String propName = (String) itr.next();
                    if (!propName.equals(ServerTags.LOCATION) &&
                        !propName.equals(ServerTags.CONTEXT_ROOT) &&
                        !propName.equals(ServerTags.OBJECT_TYPE) &&
                        !propName.equals(ServerTags.DIRECTORY_DEPLOYED) &&
                        !propName.startsWith(
                            DeploymentProperties.APP_CONFIG))
                    {
                        Property prop = app.createChild(Property.class);
                        app.getProperty().add(prop);
                        prop.setName(propName);
                        prop.setValue(appProps.getProperty(propName));
                    }
                }

                // adding the application-ref element
                ApplicationRef appRef = params[1].createChild(ApplicationRef.class);
                appRef.setRef(deployParams.name);
                if (deployParams.virtualservers != null) {
                    appRef.setVirtualServers(deployParams.virtualservers);
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
                appRef.setEnabled(deployParams.enabled.toString());

                servr.getApplicationRef().add(appRef);

                return Boolean.TRUE;
            }

        }, applications, server);
    }

    public void unregisterAppFromDomainXML(final String appName)
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
                for (ApplicationName module : apps.getModules()) {
                    if (module.getName().equals(appName)) {
                        ((Applications)params[0]).getModules().remove(module);
                        break;
                    }
                }
                return Boolean.TRUE;
            }
        }, applications, server);
    }


    // check if the application is registered in domain.xml
    public boolean isRegistered(String appName) {
        return ConfigBeansUtilities.getModule(appName)!=null;
    }

    public ApplicationInfo get(String appName) {
        return appRegistry.get(appName);
    }


    public class DeploymentContextBuidlerImpl implements DeploymentContextBuilder {
        private final Logger logger;
        private final ActionReport report;
        private final OpsParams params;
        private File sFile;
        private ReadableArchive sArchive;
        private ArchiveHandler handler;

        public DeploymentContextBuidlerImpl(Logger logger, OpsParams params, ActionReport report) {
            this.logger = logger;
            this.report = report;
            this.params = params;
        }

        public DeploymentContextBuidlerImpl(DeploymentContextBuilder b) throws IOException {
            this.logger = b.logger();
            this.report = b.report();
            this.params = b.params();
            ReadableArchive archive = getArchive(b);
            source(archive);
            handler = b.archiveHandler();
        }

        public DeploymentContextBuilder source(File source) {
            this.sFile = source;
            return this;
        }

        public File sourceAsFile() {
            return sFile;
        }
        public ReadableArchive sourceAsArchive() {
            return sArchive;
        }

        public ArchiveHandler archiveHandler() {
            return handler;
        }

        public DeploymentContextBuilder source(ReadableArchive archive) {
            this.sArchive = archive;
            return this;
        }

        public DeploymentContextBuilder archiveHandler(ArchiveHandler handler) {
            this.handler = handler;
            return this;
        }

        public ExtendedDeploymentContext build() throws IOException {
            return build(null);
        }
        public Logger logger() { return logger; };
        public ActionReport report() { return report; };
        public OpsParams params() { return params; };

        public ExtendedDeploymentContext build(ExtendedDeploymentContext initialContext) throws IOException {
            return ApplicationLifecycle.this.getContext(initialContext, this);
        }
    }

    public DeploymentContextBuilder getBuilder(Logger logger, OpsParams params, ActionReport report) {
        return new DeploymentContextBuidlerImpl(logger, params, report);
    }


    // cannot put it on the builder itself since the builder is an official API.
    private ReadableArchive getArchive(DeploymentContextBuilder builder) throws IOException {
        ReadableArchive archive = builder.sourceAsArchive();
        if (archive==null && builder.sourceAsFile()==null) {
            throw new IOException("Source archive or file not provided to builder");
        }
        if (archive==null && builder.sourceAsFile()!=null) {
             archive = habitat.getComponent(ArchiveFactory.class).openArchive(builder.sourceAsFile());
            if (archive==null) {
                throw new IOException("Invalid archive type : " + builder.sourceAsFile().getAbsolutePath());
            }
        }
        return archive;        
    }

    private ExtendedDeploymentContext getContext(ExtendedDeploymentContext initial, DeploymentContextBuilder builder) throws IOException {

        DeploymentContextBuilder copy = new DeploymentContextBuidlerImpl(builder);

        ReadableArchive archive = getArchive(copy);
        copy.source(archive);

        if (initial==null) {
            initial = new DeploymentContextImpl(copy, env);
        }

        ArchiveHandler archiveHandler = copy.archiveHandler();
        if (archiveHandler == null) {
            archiveHandler = getArchiveHandler(archive);
        }



        // this is needed for autoundeploy to find the application 
        // with the archive name
        File sourceFile = new File(archive.getURI().getSchemeSpecificPart());
        initial.getAppProps().put(ServerTags.DEFAULT_APP_NAME, 
            DeploymentUtils.getDefaultEEName(sourceFile.getName()));   

        if (!(sourceFile.isDirectory())) {
            // create a temporary deployment context
            File expansionDir = new File(domain.getApplicationRoot(), 
                copy.params().name());
            if (!expansionDir.mkdirs()) {
                /*
                 * On Windows especially a previous directory might have
                 * remainded after an earlier undeployment, for example if
                 * a JAR file in the earlier deployment had been locked.
                 * Warn but do not fail in such a case.
                 */
                logger.fine(localStrings.getLocalString("deploy.cannotcreateexpansiondir", "Error while creating directory for jar expansion: {0}",expansionDir));
            }
            try {
                Long start = System.currentTimeMillis();
                ArchiveFactory archiveFactory = habitat.getComponent(ArchiveFactory.class);
                archiveHandler.expand(archive, archiveFactory.createArchive(expansionDir), initial);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Deployment expansion took " + (System.currentTimeMillis() - start));
                }

                // Close the JAR archive before losing the reference to it or else the JAR remains locked.
                try {
                    archive.close();
                } catch(IOException e) {
                    logger.log(Level.SEVERE, localStrings.getLocalString("deploy.errorclosingarchive","Error while closing deployable artifact {0}", archive.getURI().getSchemeSpecificPart()),e);
                    throw e;
                }
                archive = archiveFactory.openArchive(expansionDir);
                initial.setSource(archive);
            } catch(IOException e) {
                logger.log(Level.SEVERE, localStrings.getLocalString("deploy.errorexpandingjar","Error while expanding archive file"),e);
                throw e;
            }
        }
        initial.setArchiveHandler(archiveHandler);
        return initial;
    }

    /*
     * @return comma-separated list of all defined virtual servers (exclusive
     * of __asadmin)
     */
    private String getVirtualServers(String target) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        Server server = domain.getServerNamed(target);
        if (server != null) {
            Config config = domain.getConfigs().getConfigByName(
                server.getConfigRef());
            if (config != null) {
                HttpService httpService = config.getHttpService();
                if (httpService != null) {
                    List<VirtualServer> hosts = httpService.getVirtualServer();
                    if (hosts != null) {
                        for (VirtualServer host : hosts) {
                            if (("__asadmin").equals(host.getId())) {
                                continue;
                            }
                            if (first) {
                                sb.append(host.getId());
                                first = false;
                            } else {
                                sb.append(",");
                                sb.append(host.getId());
                            }
                        }
                    }
                }
            }
        }
        return sb.toString();
    }
}

