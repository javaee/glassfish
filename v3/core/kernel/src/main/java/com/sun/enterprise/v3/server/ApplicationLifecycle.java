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
import com.sun.enterprise.module.*;
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

import java.util.*;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;

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
                    defs.addAll(Arrays.asList(moduleDefs));
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
     * @param cloader is a class loader capable of loading classes and resources
     * from the passed archive.
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

    public ApplicationInfo load(Iterable<Sniffer> sniffers, final DeploymentContextImpl context, ActionReport report) {

        final ApplicationLifecycle myself = this;

        ProgressTracker tracker = new ProgressTracker() {
            public void actOn(Logger logger) {
                myself.stopModules(get("started", ModuleInfo.class), logger);
                myself.unload(get(ModuleInfo.class), context);
                myself.clean(get(Deployer.class), context);
                stopContainers(get(ContainerInfo.class), logger);
            }
        };

        //List<ContainerInfo> startedContainers = new ArrayList<ContainerInfo>();
        for (Sniffer sniffer : sniffers) {
            if (sniffer.getContainersNames()==null || sniffer.getContainersNames().length==0) {
                failure(logger, "no container associated with application of type : " + sniffer.getModuleType(), null, report);
                return null;
            }

            String resourceName = sniffer.getClass().getName().replace(".","/")+".class";
            URL resource = sniffer.getClass().getClassLoader().getResource(resourceName);
            if (resource==null) {
                failure(logger, "cannot find container module from service implementation " + sniffer.getClass(), null, report);
                return null;
            }
            String resourceID = resource.toString();
            String manifest = resourceID.substring(0, resourceID.length() - resourceName.length())+ JarFile.MANIFEST_NAME;
            Manifest m = null;

            InputStream is = null;
            try {
                URL manifestURL = new URL(manifest);
                is = manifestURL.openStream();
                if (is!=null) {
                    m = new Manifest(is);
                }
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            } finally {
                if (is!=null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        logger.finer("cannot close manifest file input stream");
                    }
                }
            }
            Module snifferModule=null;
            if (m!=null) {
                String bundleName = m.getMainAttributes().getValue(ManifestConstants.BUNDLE_NAME);
                snifferModule = modulesRegistry.makeModuleFor(bundleName, null);
            }
            if (snifferModule==null) {
                failure(logger, "cannot find container module from service implementation " + sniffer.getClass(), null, report);
                return null;
            }

            // start all the containers associated with sniffers.
            ContainerInfo containerInfo = containerRegistry.getContainer(sniffer.getContainersNames()[0]);
            if (containerInfo == null) {
                Collection<ContainerInfo> containersInfo = startContainer(sniffer, snifferModule, logger, report);
                if (containersInfo==null || containersInfo.size()==0) {
                    tracker.actOn(logger);
                    failure(logger, "Cannot start container(s) associated to application of type : " + sniffer.getModuleType(), null, report);
                    return null;
                }
                tracker.addAll(ContainerInfo.class, containersInfo);
            }

        }

        // all containers that have recognized parts of the application being deployed
        // have now been successfully started. Start the deployment process.
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
                    deployer.prepare(context);
                    tracker.add(Deployer.class, deployer);
                } catch(Exception e) {
                    failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method",e, report);
                    tracker.actOn(logger);
                    return null;
                }
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        }

        // this is the end of the prepare phase, on to the load phase.
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

                        tracker.actOn(logger);
                        return null;
                    }
                    ModuleInfo moduleInfo = new ModuleInfo(containerInfo, appCtr);
                    tracker.add(ModuleInfo.class, moduleInfo);
                } catch(Exception e) {
                    failure(logger, "Exception while invoking " + deployer.getClass() + " prepare method",e, report);
                    tracker.actOn(logger);
                    return null;
                }
            } finally {
                Thread.currentThread().setContextClassLoader(currentCL);
            }
        }

        // registers all deployed items.
        for (ModuleInfo module : tracker.get(ModuleInfo.class)) {

            try {
                module.getApplicationContainer().start();
                tracker.add("started", ModuleInfo.class, module);

                // add the endpoint
                try {
                    Adapter appAdapter = Adapter.class.cast(module.getApplicationContainer());
                    adapter.registerEndpoint(appAdapter.getContextRoot(), null, appAdapter, module.getApplicationContainer());
                } catch (ClassCastException e) {
                    // ignore the application may not publish endpoints.
                }

            } catch(Exception e) {
                failure(logger, "Exception while invoking " + module.getApplicationContainer().getClass() + " start method",e, report);
                tracker.actOn(logger);
                return null;
            }
        }

        final String appName = context.getCommandParameters().getProperty(DeployCommand.NAME);
        ApplicationInfo appInfo = new ApplicationInfo(context.getSource(), appName,
                tracker.get(ModuleInfo.class).toArray(new ModuleInfo[tracker.get(ModuleInfo.class).size()]));

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

    protected Collection<ContainerInfo> startContainer(Sniffer sniffer, Module snifferModule,  Logger logger, ActionReport report) {

        ContainerStarter starter = new ContainerStarter(modulesRegistry, habitat, logger);
        Collection<ContainerInfo> containersInfo = starter.startContainer(sniffer, snifferModule);
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
            Inhabitant i = habitat.getInhabitantByType(info.getDeployer().getClass());
            if (i!=null) {
                i.release();
            }
            i = habitat.getInhabitantByType(info.getContainer().getClass());
            if (i!=null) {
                i.release();
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
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            if (module.getApplicationContainer().getClassLoader()!=null) {
                Thread.currentThread().setContextClassLoader(module.getApplicationContainer().getClassLoader());
            } else {
                Thread.currentThread().setContextClassLoader(module.getContainerInfo().getContainer().getClass().getClassLoader());
            }
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
        } finally {
            Thread.currentThread().setContextClassLoader(original);   
        }
    }
}
