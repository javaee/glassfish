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

import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.Named;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.deployment.common.DeploymentContextImpl;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.enterprise.v3.admin.AdminAdapter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.event.Events;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.*;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.CompositeHandler;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.internal.data.*;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;
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
    protected GrizzlyService adapter;

    @Inject
    protected ArchiveFactory archiveFactory;

    @Inject
    protected Applications applications;

    @Inject
    protected Domain domain;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    protected ClassLoaderHierarchy clh;

    @Inject
    Events events;

    protected Logger logger = LogDomains.getLogger(AppServerStartup.class, LogDomains.CORE_LOGGER);
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ApplicationLifecycle.class);      
    private static final String IS_COMPOSITE = "isComposite";

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

        for (ArchiveHandler handler : habitat.getAllByContract(ArchiveHandler.class)) {
            if (!(handler instanceof CompositeHandler) && !"DEFAULT".equals(handler.getClass().getAnnotation(Service.class).name())) {
                if (handler.handles(archive)) {
                    return handler;
                }
            }
        }
        return habitat.getComponent(ArchiveHandler.class, "DEFAULT");
    }

    public ApplicationInfo deploy(final ExtendedDeploymentContext context, final ActionReport report) {
        return deploy(null, context, report);
    }

    public ApplicationInfo deploy(Collection<Sniffer> sniffers, final ExtendedDeploymentContext context, final ActionReport report) {

        events.send(new org.glassfish.api.event.EventListener.Event<ExtendedDeploymentContext>(Deployment.DEPLOYMENT_START, context));
        
        ProgressTracker tracker = new ProgressTracker() {
            public void actOn(Logger logger) {
                for (EngineRef module : get("started", EngineRef.class)) {
                    module.stop(context, logger);
                }
                for (EngineRef module : get("loaded", EngineRef.class)) {
                    module.unload(context, report);
                }
                for (EngineRef module : get("prepared", EngineRef.class)) {
                    module.clean(context, logger);
                }
            }
        };
        DeployCommandParameters commandParams = context.getCommandParameters(DeployCommandParameters.class);

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
            context.createClassLoaders(clh, handler);

            final ClassLoader cloader = context.getClassLoader();
            final ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cloader);
                
                // containers that are started are not stopped even if the deployment fail, the main reason
                // is that some container do not support to be restarted.
                LinkedList<EngineInfo> sortedEngineInfos =
                    setupContainerInfos(handler, sniffers, context, report);
                if (sortedEngineInfos ==null || sortedEngineInfos.isEmpty()) {
                    report.failure(logger, localStrings.getLocalString("unknowncontainertype","There is no installed container capable of handling this application {0}",context.getSource()));                    
                    tracker.actOn(logger);
                    return null;
                }

                final String appName = commandParams.name();

                appInfo = appRegistry.get(appName);
                boolean alreadyRegistered = appInfo!=null;
                if (!alreadyRegistered) {

                    // this is a first time deployment as opposed as load following an unload event,
                    // we need to create the application info
                    ModuleInfo moduleInfo = null;
                    try {
                        moduleInfo = prepareModule(sortedEngineInfos, appName, context, report, tracker);
                    } catch(Exception prepareException) {
                        report.failure(logger, "Exception while preparing the app");
                        tracker.actOn(logger);
                        return null;
                    }

                    // the deployer did not take care of populating the application info, this
                    // is not a composite module.
                    appInfo=context.getModuleMetaData(ApplicationInfo.class);
                    if (appInfo==null) {
                        appInfo = new ApplicationInfo(context.getSource(), appName);
                        appInfo.addModule(moduleInfo);

                        for (Object m : context.getModuleMetadata()) {
                            appInfo.addMetaData(m);
                        }

                    }

                    appRegistry.add(appName, appInfo);
                } else {
                    context.addModuleMetaData(appInfo);                    
                }

                // now were falling back into the mainstream loading/starting sequence, at this
                // time the containers are set up, all the modules have been prepared in their
                // associated engines and the application info is created and registered
                appInfo.setLibraries(commandParams.libraries());

                try {
                    appInfo.load(context, report, tracker);
                } catch(Exception loadException) {
                    report.failure(logger, "Exception while loading the app", loadException);
                    tracker.actOn(logger);
                    if (!alreadyRegistered) {
                        appRegistry.remove(appName);    
                    }
                    return null;
                }

                // if enable attribute is set to true
                // we start the application
                if (commandParams.enabled) {
                    appInfo.start(context, report, tracker);
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
            if (appInfo==null) {
                events.send(new org.glassfish.api.event.EventListener.Event<ExtendedDeploymentContext>(Deployment.DEPLOYMENT_FAILURE, context));
            } else {
                events.send(new org.glassfish.api.event.EventListener.Event<ApplicationInfo>(Deployment.DEPLOYMENT_SUCCESS, appInfo));
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

    public LinkedList<EngineInfo> setupContainerInfos(DeploymentContext context, ActionReport report)
        throws Exception {

        return setupContainerInfos(null, null, context, report);
    }

    // set up containers and prepare the sorted ModuleInfos
    public LinkedList<EngineInfo> setupContainerInfos(final ArchiveHandler handler,
            Collection<Sniffer> sniffers, DeploymentContext context,
            ActionReport report) throws Exception {

        if (sniffers==null) {
            ReadableArchive source=context.getSource();
            if (handler instanceof CompositeHandler) {
                source = new CompositeArchive(context.getSource(), (CompositeHandler) handler);
                context.getProps().setProperty(IS_COMPOSITE, "true");
            }
            sniffers = snifferManager.getSniffers(source, context.getClassLoader());

        }

        if (sniffers.size()==0) {
            report.failure(logger,localStrings.getLocalString("deploy.unknownmoduletpe","Module type not recognized"));
            return null;
        }

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
        LinkedList<EngineInfo> sortedEngineInfos, String moduleName,
        DeploymentContext context, ActionReport report,
        ProgressTracker tracker) throws Exception {

        List<EngineRef> addedEngines = new LinkedList<EngineRef>();
        for (EngineInfo engineInfo : sortedEngineInfos) {

            // get the deployer
            Deployer deployer = engineInfo.getDeployer();

            try {
                deployer.prepare(context);

                // construct an incomplete EngineRef which will be later
                // filled in at loading time
                EngineRef engineRef = new EngineRef(engineInfo, adapter, null);
                addedEngines.add(engineRef);
                tracker.add("prepared", EngineRef.class, engineRef);

                tracker.add(Deployer.class, deployer);
            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method", e);
                throw e;
            }
        }
        // I need to create the application info here from the context, or something like this.
        // and return the application info from this method for automatic registration in the caller.
        return new ModuleInfo(moduleName, addedEngines);
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

    protected ApplicationInfo unload(String appName, ExtendedDeploymentContext context, ActionReport report) {

        ApplicationInfo info = appRegistry.get(appName);
        if (info==null) {
            report.failure(context.getLogger(), "Application " + appName + " not registered", null);
            return null;

        }
        info.unload(context, report);
        return info;

    }

    public void undeploy(String appName, ExtendedDeploymentContext context, ActionReport report) {

        if (report.getExtraProperties()!=null) {
            context.getProps().put("ActionReportProperties", report.getExtraProperties());
        }
        
        ApplicationInfo info = unload(appName, context, report);
        try {
            info.clean(context);
        } catch(Exception e) {
            report.failure(context.getLogger(), "Exception while cleaning application artifacts", e);
            return;
        }
        appRegistry.remove(appName);
    }

    // register application information in domain.xml
    public void registerAppInDomainXML(final ApplicationInfo
        applicationInfo, final DeploymentContext context)
        throws TransactionFailure {
        final Properties moduleProps = context.getProps();
        ConfigSupport.apply(new ConfigCode() {
            public Object run(ConfigBeanProxy... params) throws PropertyVetoException, TransactionFailure {

                Applications apps = (Applications) params[0];
                Server servr = (Server) params[1];

                // adding the application element
                Application app = params[0].createChild(Application.class);

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
                applicationInfo.save(app);

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


    // check if the application is registered in domain.xml
    public boolean isRegistered(String appName) {
        return ConfigBeansUtilities.getModule(appName)!=null;
    }

    public ApplicationInfo get(String appName) {
        return appRegistry.get(appName);
    }

    public ExtendedDeploymentContext getContext(Logger logger, File source, OpsParams params)
        throws IOException {

        ReadableArchive archive = null;
        if (source!=null) {
             archive = archiveFactory.openArchive(source);
            if (archive==null) {
                throw new IOException("Invalid archive type : " + source.getAbsolutePath());
            }
        }
        return getContext(logger, archive, params);
    }

    public ExtendedDeploymentContext getContext(Logger logger, ReadableArchive source, OpsParams params) throws IOException {
        if (source != null && !(new File(source.getURI().getSchemeSpecificPart()).isDirectory())) {
            File expansionDir = new File(domain.getApplicationRoot(), 
                params.name());
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
                ArchiveHandler archiveHandler = getArchiveHandler(source);
                Long start = System.currentTimeMillis();
                archiveHandler.expand(source, archiveFactory.createArchive(expansionDir));
                System.out.println("Deployment expansion took " + (System.currentTimeMillis() - start));

                // Close the JAR archive before losing the reference to it or else the JAR remains locked.
                try {
                    source.close();
                } catch(IOException e) {
                    logger.log(Level.SEVERE, localStrings.getLocalString("deploy.errorclosingarchive","Error while closing deployable artifact {0}", source.getURI().getSchemeSpecificPart()),e);
                    throw e;
                }
                source = archiveFactory.openArchive(expansionDir);
            } catch(IOException e) {
                logger.log(Level.SEVERE, localStrings.getLocalString("deploy.errorexpandingjar","Error while expanding archive file"),e);
                throw e;
            }
        }
        return new DeploymentContextImpl(logger, source, params, env);
    }
}

