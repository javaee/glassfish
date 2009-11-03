/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
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
package org.glassfish.internal.data;

import java.beans.PropertyVetoException;
import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URLClassLoader;

import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.ServerTags;
import org.jvnet.hk2.config.types.Property;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.Events;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Each module of an application has an associated module info instance keeping
 * the list of engines in which that module is loaded.
 *
 * @author Jerome Dochez
 */
public class ModuleInfo {

    final static private Logger logger = LogDomains.getLogger(ApplicationInfo.class, LogDomains.CORE_LOGGER);
    
    protected Set<EngineRef> engines = new LinkedHashSet<EngineRef>();
    final protected Map<Class<? extends Object>, Object> metaData = new HashMap<Class<? extends Object>, Object>();

    protected final String name;
    protected final Events events;
    private Properties moduleProps;
    private boolean started=false;
    private ClassLoader moduleClassLoader;
    private Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
    
  
    public ModuleInfo(final Events events, String name, Collection<EngineRef> refs, 
        Properties moduleProps) {
        this.name = name;
        this.events = events;
        for (EngineRef ref : refs) {
            engines.add(ref);
        }
        this.moduleProps = moduleProps;
    }

    public Set<EngineRef> getEngineRefs() {
        Set<EngineRef> copy = new LinkedHashSet<EngineRef>();
        copy.addAll(_getEngineRefs());
        return copy; 
    }

    protected Set<EngineRef> _getEngineRefs() {
        return engines;
    }

    public Set<ClassLoader> getClassLoaders() {
        return classLoaders;
    }

    public void cleanClassLoaders() {
        classLoaders = null; 
        moduleClassLoader = null;
    }

    public void addMetaData(Object o) {
        metaData.put(o.getClass(), o);
    }

    public <T> T getMetaData(Class<T> c) {
        return c.cast(metaData.get(c));
    }

    public String getName() {
        return name;
    }

    public Properties getModuleProps() {
        Properties props =  new Properties();
        props.putAll(moduleProps);
        return props;
    }


    /**
     * Returns the list of sniffers that participated in loaded this
     * application
     *
     * @return array of sniffer that loaded the application's module
     */
    public Collection<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<Sniffer>();
        for (EngineRef engine : _getEngineRefs()) {
            sniffers.add(engine.getContainerInfo().getSniffer());
        }
        return sniffers;
    }

    public void load(ExtendedDeploymentContext context, ProgressTracker tracker) throws Exception {
        ActionReport report = context.getActionReport();
        context.setPhase(ExtendedDeploymentContext.Phase.LOAD);
        moduleClassLoader = context.getClassLoader();

        installTransformers(context);

        Set<EngineRef> filteredEngines = new LinkedHashSet<EngineRef>();

        for (EngineRef engine : _getEngineRefs()) {

            final EngineInfo engineInfo = engine.getContainerInfo();

            // get the container.
            Deployer deployer = engineInfo.getDeployer();

            ClassLoader currentClassLoader  = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(context.getClassLoader());
                ApplicationContainer appCtr = deployer.load(engineInfo.getContainer(), context);
                if (appCtr==null) {
                    String msg = "Cannot load application in " + engineInfo.getContainer().getName() + " container";
                    logger.fine(msg);
                    continue;
                }
                engine.load(context, tracker);
                engine.setApplicationContainer(appCtr);
                filteredEngines.add(engine);

            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + deployer.getClass() + " load method", e);
                throw e;
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }

        engines = filteredEngines;

        if (events!=null) {
            events.send(new Event<ModuleInfo>(Deployment.MODULE_LOADED, this), false);
        }
    }    

    /*
     * Returns the EngineRef for a particular container type
     * @param type the container type
     * @return the module info is this application as a module implemented with
     * the passed container type
     */
    public <T extends Container> EngineRef getEngineRefForContainer(Class<T> type) {
        for (EngineRef engine : _getEngineRefs()) {
            T container = null;
            try {
                container = type.cast(engine.getContainerInfo().getContainer());
            } catch (Exception e) {
                // ignore, wrong container
            }
            if (container!=null) {
                return engine;
            }
        }
        return null;
    }


    public synchronized void start(
        DeploymentContext context,
        ProgressTracker tracker) throws Exception {

        ActionReport report = context.getActionReport();

        if (started)
            return;
        
        // registers all deployed items.
        for (EngineRef engine : _getEngineRefs()) {
            if (context.getLogger().isLoggable(Level.FINE)) {
                context.getLogger().fine("starting " + engine.getContainerInfo().getSniffer().getModuleType());
            }

            ClassLoader currentClassLoader  = 
                Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(context.getClassLoader());
                if (!engine.start( context, tracker)) {
                    report.failure(logger, "Module not started " +  engine.getApplicationContainer().toString());
                    throw new Exception( "Module not started " +  engine.getApplicationContainer().toString());
                }
            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + engine.getApplicationContainer().getClass() + " start method", e);
                throw e;
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }
        started=true;
        if (events!=null) {
            events.send(new Event<ModuleInfo>(Deployment.MODULE_STARTED, this), false);
        }
    }

    public synchronized void stop(ExtendedDeploymentContext context, Logger logger) {

        if (!started)
            return;
        
        for (EngineRef module : _getEngineRefs()) {
            ClassLoader currentClassLoader  = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(moduleClassLoader);
                context.setClassLoader(moduleClassLoader);
                module.stop(context);
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Cannot stop module " +
                        module.getContainerInfo().getSniffer().getModuleType(),e );
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }
        started=false;
        if (events!=null) {
            events.send(new Event<ModuleInfo>(Deployment.MODULE_STOPPED, this), false);
        }
    }

    public void unload(ExtendedDeploymentContext context) {

        for (EngineRef engine : _getEngineRefs()) {
            if (engine.getApplicationContainer()!=null && engine.getApplicationContainer().getClassLoader()!=null) {
                classLoaders.add(engine.getApplicationContainer().getClassLoader());
                ClassLoader currentClassLoader  = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(moduleClassLoader);
                    context.setClassLoader(moduleClassLoader);
                    engine.unload(context);
                } catch(Throwable e) {
                    logger.log(Level.SEVERE, "Failed to unload from container type : " +
                            engine.getContainerInfo().getSniffer().getModuleType(), e);
                } finally {
                    Thread.currentThread().setContextClassLoader(currentClassLoader);
                    context.setClassLoader(null);
                }
            }
        }

        // add the module classloader to the predestroy list if it's not
        // already there
        if (classLoaders != null && moduleClassLoader != null) {
            classLoaders.add(moduleClassLoader);
        }

        if (events!=null) {
            events.send(new Event<ModuleInfo>(Deployment.MODULE_UNLOADED, this), false);
        }

    }

    public void clean(ExtendedDeploymentContext context) throws Exception {
        for (EngineRef ref : _getEngineRefs()) {
            ref.clean(context);
        }
        if (events!=null) {
            events.send(new Event<DeploymentContext>(Deployment.MODULE_CLEANED,context), false);
        }        
        
    }

    public boolean suspend(Logger logger) {

        boolean isSuccess = true;

        for (EngineRef engine : _getEngineRefs()) {
            try {
                engine.getApplicationContainer().suspend();
            } catch(Exception e) {
                isSuccess = false;
                logger.log(Level.SEVERE, "Error suspending module " +
                           engine.getContainerInfo().getSniffer().getModuleType(),e );
            }
        }

        return isSuccess;
    }

    public boolean resume(Logger logger) {

        boolean isSuccess = true;

        for (EngineRef module : _getEngineRefs()) {
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

    /**
     * Saves its state to the configuration. this method must be called within a transaction
     * to the configured module instance.
     *
     * @param module the module being persisted
     */
    public void save(Module module) throws TransactionFailure, PropertyVetoException {

        module.setName(name);

        // write out the module properties only for composite app
        if (Boolean.valueOf(moduleProps.getProperty(
            ServerTags.IS_COMPOSITE))) {
            moduleProps.remove(ServerTags.IS_COMPOSITE);
            for (Iterator itr = moduleProps.keySet().iterator(); 
                itr.hasNext();) {
                String propName = (String) itr.next();
                Property prop = module.createChild(Property.class);
                module.getProperty().add(prop);
                prop.setName(propName);
                prop.setValue(moduleProps.getProperty(propName));
            }
        }

        for (EngineRef ref : _getEngineRefs()) {
            Engine engine = module.createChild(Engine.class);
            module.getEngines().add(engine);
            ref.save(engine);
        }
    }

    private void installTransformers(ExtendedDeploymentContext context) throws Exception{
        ActionReport report = context.getActionReport();
        if (!context.getTransformers().isEmpty()) {
            // add the class file transformers to the new class loader
            try {
                InstrumentableClassLoader icl = InstrumentableClassLoader.class.cast(context.getFinalClassLoader());
                String isComposite = context.getAppProps().getProperty(
                    ServerTags.IS_COMPOSITE);

                if (Boolean.valueOf(isComposite) && 
                    icl instanceof URLClassLoader) {
                    URLClassLoader urlCl = (URLClassLoader)icl;
                    if (this instanceof ApplicationInfo) {
                        // for ear lib PUs, let's install the
                        // tranformers with the EarLibClassLoader
                        icl = InstrumentableClassLoader.class.cast(urlCl.getParent().getParent());
                    } else {
                        // for modules inside the ear, let's install the 
                        // transformers with the EarLibClassLoader in 
                        // addition to installing them to module classloader
                        ClassLoader libCl = urlCl.getParent().getParent();
                        if (!(libCl instanceof URLClassLoader)) {
                            // web module
                            libCl = libCl.getParent();
                        }
                        if (libCl instanceof URLClassLoader) {
                            InstrumentableClassLoader libIcl = InstrumentableClassLoader.class.cast(libCl);
                            for (ClassFileTransformer transformer : context.getTransformers()) {
                                libIcl.addTransformer(transformer);
                            }
                        }
            
                    }
                }
                for (ClassFileTransformer transformer : context.getTransformers()) {
                    icl.addTransformer(transformer);
                }

            } catch (Exception e) {
                report.failure(logger, "Class loader used for loading application cannot handle bytecode enhancer", e);
                throw e;
            }
        }
    }
}
