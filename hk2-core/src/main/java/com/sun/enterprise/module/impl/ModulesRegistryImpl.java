/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.common_impl.AbstractModulesRegistryImpl;
import com.sun.enterprise.module.common_impl.ModuleId;
import com.sun.enterprise.module.common_impl.AbstractFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.URL;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author Jerome Dochez
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ModulesRegistryImpl extends AbstractModulesRegistryImpl {
    private ClassLoader parentLoader;

    /*package*/ final List<ModuleLifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<ModuleLifecycleListener>();

    public ModulesRegistryImpl(ModulesRegistry parent) {
        super(parent);
        
    }

    /**
     * Creates a new child {@link ModulesRegistryImpl} in this {@link ModulesRegistryImpl}.
     */
    public ModulesRegistry createChild() {
        return new ModulesRegistryImpl(this);
    }

    protected Module newModule(ModuleDefinition moduleDef) {
        Module m = new ModuleImpl(this, moduleDef);
        for (ModuleLifecycleListener l : getLifecycleListeners()) {
            l.moduleInstalled(m);
        }
        return m;
    }

    protected List<ActiveDescriptor> parseInhabitants(
            Module module, String name, ServiceLocator serviceLocator, List<PopulatorPostProcessor> postProcessors)
            throws IOException {
        return ((ModuleImpl)module).parseInhabitants(name, postProcessors);
    }

    /**
     * Creates and return a new private module implementation giving a name and
     * version constraints. A private module is like any other module except
     * it is not registered to be shared by other potential module users.
     *
     * @param moduleName the module name
     * @param version the desired version
     * @return the new private module or null if cannot be found
     * @throws com.sun.enterprise.module.ResolveError if the module dependencies cannot be resolved
     */
    /*package*/ ModuleImpl newPrivateModuleFor(String moduleName, String version) {
        if(parent!=null) {
            ModuleImpl m = ModulesRegistryImpl.class.cast(parent).newPrivateModuleFor(moduleName,version);
            if(m!=null)     return m;
        }

        ModuleId id = AbstractFactory.getInstance().createModuleId(moduleName, version);
        Module module = modules.get(id);
        if (module!=null) {
            ModuleImpl privateModule =
                    (ModuleImpl)newModule(module.getModuleDefinition());
            privateModule.resolve();
            return privateModule;
        }
        return (ModuleImpl)loadFromRepository(moduleName, version);

    }

    @Override
    public ModuleImpl getProvidingModule(String providerClassName) {
        return ModuleImpl.class.cast(super.getProvidingModule(providerClassName));
    }

    /**
     * Sets the classloader parenting the class loaders created by the modules
     * associated with this registry.
     * @param parent parent class loader
     */
    public void setParentClassLoader(ClassLoader parent) {
        this.parentLoader = parent;
    }

    /**
     * Returns the parent class loader parenting the class loaders created
     * by modules associated with this registry.
     * @return the parent classloader
     */
    public ClassLoader getParentClassLoader() {
        return parentLoader;
    }

    /**
     * Returns a ClassLoader capable of loading classes from a set of modules identified
     * by their module definition and also load new urls.
     *
     * @param parent the parent class loader for the returned class loader instance
     * @param defs module definitions for all modules this classloader should be
     *        capable of loading
     * @param urls urls to be added to the module classloader
     * @return class loader instance
     * @throws com.sun.enterprise.module.ResolveError if one of the provided module
     *         definition cannot be resolved
     */
    public ClassLoader getModulesClassLoader(ClassLoader parent,
                                             Collection<ModuleDefinition> defs,
                                             URL[] urls) throws ResolveError {

        if (parent==null) {
            parent = getParentClassLoader();
        }
        ClassLoaderProxy cl = new ClassLoaderProxy(new URL[0], parent);
        for (ModuleDefinition def : defs) {
            Module module = this.makeModuleFor(def.getName(), def.getVersion());
            cl.addDelegate(module.getClassLoader());
        }
        
        if (urls != null) {
            for (URL url : urls) {
                cl.addURL(url);
            }
        }
        return cl;
    }

    
    /**
     * Returns a ClassLoader capable of loading classes from a set of modules identified
     * by their module definition
     *
     * @param parent the parent class loader for the returned class loader instance
     * @param defs module definitions for all modules this classloader should be
     *        capable of loading classes from
     * @return class loader instance
     * @throws com.sun.enterprise.module.ResolveError if one of the provided module
     *         definition cannot be resolved
     */
    public ClassLoader getModulesClassLoader(ClassLoader parent,
                                             Collection<ModuleDefinition> defs)
        throws ResolveError {
        return getModulesClassLoader(parent, defs, null);
    }
    

    public Module find(Class clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if(cl==null)    return null;
        if (cl instanceof ModuleClassLoader)
            return ((ModuleClassLoader) cl).getOwner();
        return null;
    }

    /**
     * Add a <code>ModuleLifecycleListener</code> to this registry. The listener
     * will be notified for each module startup and shutdown.
     * @param listener the listener implementation
     */
    public void register(ModuleLifecycleListener listener) {
        lifecycleListeners.add(listener);
    }

    /**
     * Removes an <code>ModuleLifecycleListener</code> from this registry.
     * Notification of module startup and shutdown will not be emitted to this
     * listener any longer.
     * @param listener the listener to unregister
     */
    public void unregister(ModuleLifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }

    public List<ModuleLifecycleListener> getLifecycleListeners() {
        return Collections.unmodifiableList(lifecycleListeners);
    }

    /**
     * Detaches all the modules from this registry. The modules are not
     * deconstructed when calling this method.
     */
    public void detachAll() {
        modules.clear();
    }

    /**
     * Shuts down this module's registry, apply housekeeping tasks
     *
     */
    public void shutdown() {
        detachAll();
        for (Repository repo : repositories.values()) {
            try {
                repo.shutdown();
            } catch(Exception e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Error while closing repository " + repo, e);
                // swallows
            }
        }
    }

}
