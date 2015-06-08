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

package com.sun.enterprise.module;

import java.io.PrintStream;
import java.util.List;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface Module {
    /**
     * Returns the module definition for this module instance
     * @return the module definition
     */
    ModuleDefinition getModuleDefinition();

    /**
     * Short-cut for {@code getModuleDefinition().getName()}.
     */
    String getName();

    /**
     * Returns the registry owning this module
     * @return the registry owning the module
     */
    ModulesRegistry getRegistry();

    /**
     * Returns the module's state
     * @return the module's state
     */
    ModuleState getState();

    /**
     * Ensure that this module is {@link ModuleState#RESOLVED resolved}.
     *
     * <p>
     * If the module is already resolved, this method does nothing.
     * Otherwise, iterate over all declared ModuleDependency instances and use the
     * associated <code>ModuleRegistry</code> to resolve it. After successful
     * completion of this method, the module state is
     * {@link ModuleState#RESOLVED}.
     *
     * @throws ResolveError if any of the declared dependency of this module
     * cannot be satisfied
     */
    void resolve() throws ResolveError;

    /**
     * Forces module startup. In most cases, the runtime will take care
     * of starting modules when they are first used. There could be cases where
     * code need to manually start a sub module. Invoking this method will
     * move the module to the {@link ModuleState#READY ModuleState.READY}, the
     * {@link LifecyclePolicy#start Lifecycle.start} method will be invoked.
     */
    void start() throws ResolveError;

    /**
     * Forces module stop. In most cases, the runtime will take care of stopping
     * modules when the last module user released its interest. However, in
     * certain cases, it may be interesting to manually stop the module.
     * Stopping the module means that the module is removed from the registry,
     * the class loader references are released (note : the class loaders will
     * only be released if all instances of any class loaded by them are gc'ed).
     * If a <code>LifecyclePolicy</code> for this module is defined, the
     * {@link LifecyclePolicy#stop(Module) Lifecycle.stop(Module)}
     * method will be called and finally the module state will be
     * returned to {@link ModuleState#NEW ModuleState.NEW}.
     *
     * @return true if unloading was successful
     */
    boolean stop();

    /**
     * Detach this module from its registry. This does not free any of the
     * loaded resources. Only proper release of all references to the public
     * class loader will ensure module being garbage collected.
     * Detached modules are orphan and will be garbage collected if resources
     * are properly disposed.
     */
    void detach();

    /**
     * Trigger manual refresh mechanism, the module will check all its
     * URLs and generate change events if any of them has changed. This
     * will allow the owning registry to force a module upgrade at next
     * module request.
     */
    void refresh();

    /**
     * Gets the metadata of this module.
     */
    ModuleMetadata getMetadata();

    /**
     * Add a new module change listener
     * @param listener the listener
     */
    void addListener(ModuleChangeListener listener);

    /**
     * Unregister a module change listener
     * @param listener the listener to unregister
     */
    void removeListener(ModuleChangeListener listener);

    /**
     * Return the <code>ClassLoader</code>  instance associated with this module.
     * Only designated public interfaces will be loaded and returned by
     * this classloader
     * @return the public <code>ClassLoader</code>
     */
    ClassLoader getClassLoader();

    /**
     * Returns the list of imported modules.
     *
     * <p>
     * This is the module version of {@link ModuleDefinition#getDependencies()},
     * but after fully resolved.
     *
     * <p>
     * To enforce the stable class visibility, once {@link Module} is
     * created, dependencies cannot be changed &mdash; that is, we
     * don't want "a.b.C" to suddenly mean something different once
     * the code starts running.
     *
     * @return
     *      Can be empty but never null. Read-only.
     */
    List<Module> getImports();

    void addImport(Module module);

    /**
     * Create and add a new module to this module's list of
     * imports.
     * @param dependency new module's definition
     */
    Module addImport(ModuleDependency dependency);

    /**
     * Returns true if this module is sharable. A sharable module means that
     * onlu one instance of the module classloader will be used by all users.
     *
     * @return true if this module is sharable.
     */
    boolean isShared();

    /**
     * Returns true if the module is sticky. A sticky module cannot be stopped or
     * unloaded. Once a sticky module is loaded or started, it will stay in the
     * JVM until it exists.
     * @return true is the module is sticky
     */
    boolean isSticky();

    /**
     * Sets the sticky flag.
     * @param sticky true if the module should stick around
     */
    void setSticky(boolean sticky);

    <T> Iterable<Class<? extends T>> getProvidersClass(Class<T> serviceClass);

    Iterable<Class> getProvidersClass(String name);

    /**
     * Returns true if this module has any provider for the given service class.
     */
    boolean hasProvider(Class serviceClass);

    void dumpState(PrintStream writer);

    /**
     * Removes the module from the registry backing store, the module will need
     * be reinstalled to be loaded.
     */
    void uninstall();
}
