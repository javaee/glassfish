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
import com.sun.enterprise.v3.data.*;
import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.enterprise.v3.deployment.DeploymentContextImpl;
import com.sun.enterprise.v3.services.impl.GrizzlyAdapter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    ModulesRegistry modulesRegistry;

    @Inject
    GrizzlyAdapter adapter;

    @Inject
    ArchiveFactory archiveFactory;

    protected Logger logger = LogDomains.getLogger(LogDomains.DPL_LOGGER);

    protected Deployer getDeployer(ContainerInfo container) {
        return container.getDeployer();
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
            final String appType = sniffer.getModuleType();

            // start all the containers associated with sniffers.
            ContainerInfo containerInfo = containerRegistry.getContainer(appType);
            if (containerInfo == null) {
                containerInfo = startContainer(sniffer, logger, report);
                if (containerInfo==null) {
                    stopContainers(logger, startedContainers);
                    failure(logger, "Cannot start container associated to application of type : " + sniffer.getModuleType(), null, report);
                    return null;
                }
                startedContainers.add(containerInfo);
            }
        }

        // all containers that have recognized parts of the application being deployed
        // have now been successfully started. Start the deployment process.
        List<Deployer> preparedDeployers = new ArrayList<Deployer>();

        // first we need to run the deployers which may invalidate the class loader
        // we use for deployment. This is true for technologies like JPA where they need
        // to reload previously loaded classes in order to perform bytecode enhancements.
        LinkedList<ContainerInfo> sortedModuleInfos = new LinkedList<ContainerInfo>();
        boolean atLeastOne=false;
        for (Sniffer sniffer : sniffers) {
            final String appType = sniffer.getModuleType();

            // get the container.
            ContainerInfo containerInfo = containerRegistry.getContainer(appType);
            Deployer deployer = getDeployer(containerInfo);
            if (deployer.getMetaData().invalidatesClassLoader()) {
                atLeastOne=true;
                sortedModuleInfos.addFirst(containerInfo);
            } else {
                sortedModuleInfos.addLast(containerInfo);
            }
        }

        for (ContainerInfo containerInfo : sortedModuleInfos) {

            // get the deployer
            Deployer deployer = containerInfo.getDeployer();

            // we might need to flush the class loader used the load the application
            // bits in case we ran all the prepare() methods of the invalidating
            // deployers.
            if (atLeastOne && !deployer.getMetaData().invalidatesClassLoader()) {
                ArchiveHandler handler = getArchiveHandler(context.getSource());
                context.setClassLoader(handler.getClassLoader(null, context.getSource()));
            }

            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(containerInfo.getConnectorCL());
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
        List<ApplicationContainer> loadedContainers = new ArrayList<ApplicationContainer>();
        List<ModuleInfo> modules = new ArrayList<ModuleInfo>();
        for (Sniffer sniffer : sniffers) {
            final String appType = sniffer.getModuleType();

            // get the container.
            ContainerInfo containerInfo = containerRegistry.getContainer(appType);
            Deployer deployer = getDeployer(containerInfo);

            ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(containerInfo.getConnectorCL());
                try {
                    ApplicationContainer appCtr = deployer.load(containerInfo.getContainer(), context);
                    ModuleInfo moduleInfo = new ModuleInfo(containerInfo, appCtr, context.getModuleMetaData(sniffer.getModuleType(), Object.class));
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
                    adapter.registerEndpoint(appAdapter.getContextRoot(), appAdapter, module.getApplicationContainer());
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

    protected ContainerInfo startContainer(Sniffer sniffer, Logger logger, ActionReport report) {

        ContainerStarter starter = new ContainerStarter(modulesRegistry, habitat, logger);
        ContainerInfo containerInfo = starter.startContainer(sniffer);
        if (containerInfo == null) {
            failure(logger, "Cannot start container associated to application of type : " + sniffer.getModuleType(), null, report);
            return null;
        }

        Object container = containerInfo.getContainer();
        Class<? extends Deployer> deployerClass = container.getClass().getAnnotation(Container.class).deployerImpl();
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

        assert containerInfo != null;

        containerRegistry.addContainer(containerInfo);
        return containerInfo;
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

    // Todo : switch info to use inhabitant
    protected void stopContainer(Logger logger, ContainerInfo info) {
      /*try {
            if (info.getDeployer()!=null) {
                componentMgr.releaseComponent(info.getDeployer());
            }
        } catch(ComponentException e) {
            logger.log(Level.SEVERE, "Cannot release deployer : " + info.getDeployer(), e);
        }
        try {
            componentMgr.releaseComponent(info.getContainer());
        } catch(ComponentException e) {
            logger.log(Level.SEVERE, "Cannot release container : " + info.getContainer(), e);
        }*/
    }

    protected void unload(String appName, DeploymentContext context, ActionReport report) {

        ApplicationInfo info = appRegistry.get(appName);
        if (info==null) {
            failure(context.getLogger(), "Application " + appName + " not registered", null, report);
            return;

        }
        for (ModuleInfo moduleInfo : info.getModuleInfos()) {
            unloadModule(moduleInfo, info, context, report);
        }

        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            for (ModuleInfo moduleInfo : info.getModuleInfos()) {
                try {
                    moduleInfo.getContainerInfo().getDeployer().clean(context);
                } catch(Exception e) {
                    failure(context.getLogger(), "Exception while cleaning application artifacts", e, report);
                    return;
                }
                moduleInfo.getContainerInfo().remove(info);
            }
        }
        appRegistry.remove(appName);
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
            adapter.unregisterEndpoint(appAdapter.getContextRoot());
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
        return true;
    }
}
