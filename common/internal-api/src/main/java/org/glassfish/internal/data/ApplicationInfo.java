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

package org.glassfish.internal.data;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.component.PreDestroy;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;

/**
 * Information about a running application. Applications are composed of modules.
 * Modules run in an individual container.
 *
 * @author Jerome Dochez
 */
public class ApplicationInfo {

    final static private Logger logger = LogDomains.getLogger(ApplicationInfo.class, LogDomains.CORE_LOGGER);


    final private ModuleInfo[] modules;
    final private String name;
    final private ReadableArchive source;


    /**
     * Creates a new instance of an ApplicationInfo
     *
     * @param source the archive for this application
     * @param name name of the application
     * @param modules the modules that are forming the application
     */
    public ApplicationInfo(ReadableArchive source,
                           String name, ModuleInfo... modules) {
        this.name = name;
        this.source = source;
        this.modules = modules;
    }
    

    
    /**
     * Returns the registration name for this application
     * @return the application registration name
     */
    public String getName() {
        return name;
    }  
    


    /**
     * Returns the directory where the application bits are located
     * @return the application bits directory
     * */
    public ReadableArchive getSource() {
        return source;
    }


    /**
     * Returns the modules of this application
     * @return the modules of this application
     */
    public ModuleInfo[] getModuleInfos() {
        return modules;
    }

    /**
     * Returns the list of sniffers that participated in loaded this
     * application
     *
     * @return array of sniffer that loaded the application's module
     */
    public Iterable<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<Sniffer>();
        for (ModuleInfo module : modules) {
            sniffers.add(module.getContainerInfo().getSniffer());
        }
        return sniffers;
    }

    /*
     * Returns the ModuleInfo for a particular container type
     * @param type the container type
     * @return the module info is this application as a module implemented with
     * the passed container type
     */
    public <T extends Container> ModuleInfo getModuleInfo(Class<T> type) {
        for (ModuleInfo info : modules) {
            T container = null;
            try {
                container = type.cast(info.getContainerInfo().getContainer());
            } catch (Exception e) {
                // ignore, wrong container
            }
            if (container!=null) {
                return info;
            }
        }
        return null;
    }


    public void start(
        DeploymentContext context,
        ActionReport report, ProgressTracker tracker) throws Exception {

        // registers all deployed items.
        for (ModuleInfo module : getModuleInfos()) {

            try {
                if (!module.start( context, tracker)) {
                    report.failure(logger, "Module not started " +  module.getApplicationContainer().toString());
                    throw new Exception( "Module not started " +  module.getApplicationContainer().toString());
                }
            } catch(Exception e) {
                report.failure(logger, "Exception while invoking " + module.getApplicationContainer().getClass() + " start method", e);
                throw e;
            }
        }
    }

    private void unload(ModuleInfo[] modules, ApplicationInfo info,  DeploymentContext context, ActionReport report) {

        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        for (ModuleInfo module : modules) {
            if (module.getApplicationContainer()!=null && module.getApplicationContainer().getClassLoader()!=null) {
                classLoaders.add(module.getApplicationContainer().getClassLoader());
            }
            try {
                module.unload(info, context, report);
            } catch(Throwable e) {
                logger.log(Level.SEVERE, "Failed to unload from container type : " +
                        module.getContainerInfo().getSniffer().getModuleType(), e);
            }
        }
        // all modules have been unloaded, clean the class loaders...
        for (ClassLoader cloader : classLoaders) {
            try {
                PreDestroy.class.cast(cloader).preDestroy();
            } catch (Exception e) {
                // ignore, the class loader does not need to be explicitely stopped.
            }
        }
    }

    public void stop(ApplicationContext context, Logger logger) {

        for (ModuleInfo module : getModuleInfos()) {
            try {
                module.stop(context, logger);
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Cannot stop module " +
                        module.getContainerInfo().getSniffer().getModuleType(),e );
            }
        }
    }

    public void unload(DeploymentContext context, ActionReport report) {

        stop(context, logger);

        unload(getModuleInfos(), this, context, report);

    }

    public boolean suspend(Logger logger) {

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

    public boolean resume(Logger logger) {

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
    
}
