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

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.impl.ModuleImpl;
import com.sun.enterprise.module.impl.ClassLoaderProxy;
import com.sun.enterprise.v3.data.*;
import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.enterprise.v3.deployment.DeploymentContextImpl;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;

/**
 * Application Loader is providing utitily methods to load applications
 *
 * @author Jerome Dochez
 */
abstract public class ApplicationLifecycle {

    @Inject
    protected Sniffer[] sniffers;
    
    @Inject
    protected ArchiveHandler[] archiveHandlers;

    @Inject
    protected Habitat habitat;
    
    @Inject
    protected ContainerRegistry containerRegistry;

    @Inject
    protected ApplicationRegistry appRegistry;

    @Inject
    protected ModulesRegistry modulesRegistry;

    @Inject
    GrizzlyService adapter;

    @Inject
    ArchiveFactory archiveFactory;

    protected Logger logger = LogDomains.getLogger(LogDomains.DPL_LOGGER);

    protected <T extends Container, U extends ApplicationContainer> Deployer<T, U> getDeployer(ContainerInfo<T, U> containerInfo) {
        return containerInfo.getDeployer();
    }

    public Sniffer getSniffer(String appType) {
        assert appType!=null;
        for (Sniffer sniffer : sniffers) {
            if (appType.equalsIgnoreCase(sniffer.getModuleType())) {
                return sniffer;
            }
        }
        return null;
    }

    /**
     * Returns the ArchiveHandler for the passed archive abstraction or null
     * if there are none.
     *
     * @param archive the archive to find the handler for
     * @return the archive handler or null if not found.
     */
    public ArchiveHandler getArchiveHandler(ReadableArchive archive) {
        for (ArchiveHandler handler : archiveHandlers) {
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
     * @param deployers list of elligible deployers for this deployment.
     * @return class loader capable of loading public APIs identified by the deployers
     * @throws ResolveError if one of the deployer's public API module is not found.
     */
    protected ClassLoader createApplicationParentCL(ClassLoader parent, Collection<Deployer> deployers)
        throws ResolveError {

        List<ModuleDefinition> defs = new ArrayList<ModuleDefinition>();
        for (Deployer deployer : deployers) {
            final MetaData deployMetadata = deployer.getMetaData();
            if (deployMetadata!=null) {
                ModuleDefinition[] moduleDefs = deployMetadata.getPublicAPIs();
                if (moduleDefs!=null) {
                    for (ModuleDefinition def : moduleDefs) {
                        defs.add(def);
                    }
                }
            }
        }
        return modulesRegistry.getModulesClassLoader(parent, defs);
    }

    /**
     * Sets up a parent classloader that will be used to create a temporary application
     * class loader to load classes from the archive before the Deployers are available.
     * Sniffer.handles() method takes a class loader as a parameter and this class loader
     * needs to be able to load any class the sniffer load themselves.
     *
     * @param parent parent class loader for this class loader
     * @param sniffers sniffer instances
     * @return a class loader with visibility on all classes loadable by classloaders.
     */
    protected ClassLoader createSnifferParentCL(ClassLoader parent, Collection<Sniffer> sniffers) {
        // Use the sniffers class loaders as the delegates to the parent class loader.
        // This will allow any class loadable by the sniffer (therefore visible to the sniffer
        // class loader) to be also loadable by the archive's class loader.
        List<ModuleDefinition> snifferDefs = new ArrayList<ModuleDefinition>();
        ClassLoaderProxy cl = new ClassLoaderProxy(new URL[0], parent);
        for (Sniffer sniffer : sniffers) {
            cl.addDelegate(sniffer.getClass().getClassLoader());
        }
        return cl;
        
    }

    /**
     * Returns a collection of sniffers that recognized some parts of the
     * passed archive as components their container handle.
     *
     * If no sniffer recognize the passed archive, an empty collection is
     * returned.
     *
     * @param archive source archive abstraction
     * @return possibly empty collection of sniffers that handle the passed
     * archive.
     */
    public Collection<Sniffer> getSniffers(ReadableArchive archive, ClassLoader cloader) {

        List<Sniffer> appSniffers = new ArrayList<Sniffer>();
        for (Sniffer sniffer : sniffers) {
            if (sniffer.handles(archive, cloader )) {
                appSniffers.add(sniffer);
            }
        }
        return appSniffers;
    }

    public ApplicationInfo load(Iterable<Sniffer> sniffers, DeploymentContextImpl context, ActionReport report) {

        List<ContainerInfo> startedContainers = new ArrayList<ContainerInfo>();
        for (Sniffer sniffer : sniffers) {
            if (sniffer.getContainersNames()==null || sniffer.getContainersNames().length==0) {
                failure(logger, "no container associated with application of type : " + sniffer.getModuleType(), null, report);
                return null;
            }
            // start all the containers associated with sniffers.
            ContainerInfo containerInfo = containerRegistry.getContainer(sniffer.getContainersNames()[0]);
            if (containerInfo == null) {
                Collection<ContainerInfo> containersInfo = startContainer(sniffer, logger, report);
                if (containersInfo==null || containersInfo.size()==0) {
                    stopContainers(logger, startedContainers);
                    failure(logger, "Cannot start container(s) associated to application of type : " + sniffer.getModuleType(), null, report);
                    return null;
                }
                startedContainers.addAll(containersInfo);
                break;
            }

        }

        // all containers that have recognized parts of the application being deployed
        // have now been successfully started. Start the deployment process.
        List<Deployer> preparedDeployers = new ArrayList<Deployer>();
        List<Deployer> deployers = new ArrayList<Deployer>();

        // first we need to run the deployers which may invalidate the class loader
        // we use for deployment. This is true for technologies like JPA where they need
        // to reload previously loaded classes in order to perform bytecode enhancements.
        LinkedList<ContainerInfo> sortedModuleInfos = new LinkedList<ContainerInfo>();
        boolean atLeastOne=false;
        for (Sniffer sniffer : sniffers) {
            for (String containerName : sniffer.getContainersNames()) {
                ContainerInfo containerInfo = containerRegistry.getContainer(containerName);
                Deployer deployer = getDeployer(containerInfo);
                deployers.add(deployer);
                if (deployer.getMetaData().invalidatesClassLoader()) {
                    atLeastOne=true;
                    sortedModuleInfos.addFirst(containerInfo);
                } else {
                    sortedModuleInfos.addLast(containerInfo);
                }
            }
        }

        // Ok we now have all we need to create the parent class loader for our application
        // which will be stored in the deployment context.
        ClassLoader parentCL = createApplicationParentCL(null, deployers);
        ArchiveHandler handler = getArchiveHandler(context.getSource());
        context.setClassLoader(handler.getClassLoader(parentCL, context.getSource()));

        for (ContainerInfo containerInfo : sortedModuleInfos) {

            // get the deployer
            Deployer deployer = containerInfo.getDeployer();

            // we might need to flush the class loader used the load the application
            // bits in case we ran all the prepare() methods of the invalidating
            // deployers.
            if (atLeastOne && !deployer.getMetaData().invalidatesClassLoader()) {
                context.setClassLoader(handler.getClassLoader(parentCL, context.getSource()));
            }

            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(containerInfo.getContainer().getClass().getClassLoader());
                try {
                    preparedDeployers.add(deployer);
                    deployer.prepare(context);
                } catch(Exception e) {
                    failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method",e, report);
                    clean(preparedDeployers, context);
                    stopContainers(logger, startedContainers);
                    return null;
                }
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        }

        // this is the end of the prepare phase, on to the load phase.
        List<ModuleInfo> modules = new ArrayList<ModuleInfo>();
        for (ContainerInfo containerInfo : sortedModuleInfos) {

            // get the container.
            Deployer deployer = containerInfo.getDeployer();

            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(containerInfo.getContainer().getClass().getClassLoader());
                try {
                    ApplicationContainer appCtr = deployer.load(containerInfo.getContainer(), context);
                    if (appCtr==null) {
                        failure(logger, "Cannot load application in " + containerInfo.getContainer().getName() + " container",
                            null, report);

                        unload(modules, context);
                        clean(preparedDeployers, context);
                        stopContainers(logger, startedContainers);
                        return null;
                    }
                    ModuleInfo moduleInfo = new ModuleInfo(containerInfo, appCtr);
                    modules.add(moduleInfo);
                } catch(Exception e) {
                    failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method",e, report);
                    unload(modules, context);
                    clean(preparedDeployers, context);
                    stopContainers(logger, startedContainers);
                    return null;
                }
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        }

        // registers all deployed items.
        List<ModuleInfo> startedModules = new ArrayList<ModuleInfo>();
        for (ModuleInfo module : modules) {

            try {
                module.getApplicationContainer().start();
                startedModules.add(module);

                // add the endpoint
                try {
                    Adapter appAdapter = Adapter.class.cast(module.getApplicationContainer());
                    adapter.registerEndpoint(appAdapter.getContextRoot(), null, appAdapter, module.getApplicationContainer());
                } catch (ClassCastException e) {
                    // ignore the application may not publish endpoints.
                }

            } catch(Exception e) {
                failure(logger, "Exception while invoking " + module.getApplicationContainer().getClass() + " start method",e, report);
                stopModules(startedModules, logger);
                unload(modules, context);
                clean(preparedDeployers, context);
                stopContainers(logger, startedContainers);
                return null;
            }
        }

        final String appName = context.getCommandParameters().getProperty(DeployCommand.NAME);
        ApplicationInfo appInfo = new ApplicationInfo(context.getSource(), appName,
                startedModules.toArray(new ModuleInfo[startedModules.size()]));

        appRegistry.add(appName, appInfo);

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

    protected void unload(Iterable<ModuleInfo> modules, DeploymentContext context) {
        for (ModuleInfo module : modules) {
            try {
                module.getContainerInfo().getDeployer().unload(module.getApplicationContainer(), context);
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Failed to unload from container type : " +
                        module.getContainerInfo().getSniffer().getModuleType(), e);
            }
        }
    }

    protected void clean(Iterable<Deployer> deployers, DeploymentContext context) {
        for (Deployer deployer : deployers) {
            try {
                deployer.clean(context);
            } catch(Exception e) {
                context.getLogger().log(Level.INFO, "Deployer.clean failed for " + deployer, e);
            }
        }
    }

    protected Collection<ContainerInfo> startContainer(Sniffer sniffer, Logger logger, ActionReport report) {

        ContainerStarter starter = new ContainerStarter(modulesRegistry, habitat, logger);
        Collection<ContainerInfo> containersInfo = starter.startContainer(sniffer);
        if (containersInfo == null || containersInfo.size()==0) {
            failure(logger, "Cannot start container(s) associated to application of type : " + sniffer.getModuleType(), null, report);
            return null;
        }

        for (ContainerInfo containerInfo : containersInfo) {
            Container container = containerInfo.getContainer();
            Class<? extends Deployer> deployerClass = container.getDeployer();
            Deployer deployer;
            try {
                deployer = habitat.getComponent(deployerClass);
                containerInfo.setDeployer(deployer);
            } catch (ComponentException e) {
                failure(logger, "Cannot instantiate or inject "+deployerClass, e, report);
                stopContainer(logger, containerInfo);
                return null;
            } catch (ClassCastException e) {
                stopContainer(logger, containerInfo);
                failure(logger, deployerClass+" does not implement " +
                        " the org.jvnet.glassfish.api.deployment.Deployer interface", e, report);
                return null;
            }
        }
        return containersInfo;
    }

    protected void stopContainers(Logger logger, Iterable<ContainerInfo> ctrInfos) {
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
        Inhabitant i = habitat.getInhabitantByType(info.getDeployer().getClass());
        if (i!=null) {
            i.release();
        }
        i = habitat.getInhabitantByType(info.getContainer().getClass());
        if (i!=null) {
            i.release();
        }
        containerRegistry.removeContainer(info);

    }

    protected ApplicationInfo unload(String appName, DeploymentContext context, ActionReport report) {

        ApplicationInfo info = appRegistry.get(appName);
        if (info==null) {
            failure(context.getLogger(), "Application " + appName + " not registered", null, report);
            return null;

        }
        for (ModuleInfo moduleInfo : info.getModuleInfos()) {
            unloadModule(moduleInfo, info, context, report);
            if (!moduleInfo.getContainerInfo().getApplications().iterator().hasNext()) {
                stopContainer(context.getLogger(), moduleInfo.getContainerInfo());
            }
        }


        appRegistry.remove(appName);
        return info;

    }

    protected void undeploy(String appName, DeploymentContext context, ActionReport report) {
        
        ApplicationInfo info =unload(appName, context, report);
        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            for (ModuleInfo moduleInfo : info.getModuleInfos()) {
                try {
                    moduleInfo.getContainerInfo().getDeployer().clean(context);
                } catch(Exception e) {
                    failure(context.getLogger(), "Exception while cleaning application artifacts", e, report);
                    return;
                }
            }
        }
    }
    
    protected void failure(Logger logger, String message, Throwable e, ActionReport report) {

        if (e!=null) {
            logger.log(Level.SEVERE, message ,e);
            report.setMessage(message + " : "+ e.toString());
        } else {
            logger.log(Level.SEVERE, message);            
            report.setMessage(message);            
        }
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    protected boolean unloadModule(ModuleInfo module,
                                     ApplicationInfo info,
                                     DeploymentContext context,
                                     ActionReport report) {
        
        // remove any endpoints if exists.
        try {
            final Adapter appAdapter = Adapter.class.cast(module.getApplicationContainer());
            adapter.unregisterEndpoint(appAdapter.getContextRoot(), module.getApplicationContainer());
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
            failure(context.getLogger(), "Exception while stopping the application", e, report);
            return false;
        }

        // then remove the application from the container
        Deployer deployer = module.getContainerInfo().getDeployer();
        try {
            deployer.unload(module.getApplicationContainer(), context);
        } catch(Exception e) {
            failure(context.getLogger(), "Exception while shutting down application container", e, report);
            return false;
        }
        module.getContainerInfo().remove(info);
        return true;
    }
}
