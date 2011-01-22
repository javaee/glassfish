/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.module.common_impl;

import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.RepositoryChangeListener;
import com.sun.enterprise.module.ManifestConstants;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Partial implementation of {@link Repository}
 * that statically enumerates all {@link ModuleDefinition}
 * upfront.
 *
 * @author Kohsuke Kawaguchi
 * @author Jerome Dochez
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class AbstractRepositoryImpl implements Repository {
    private final String name;
    private final URI location;
    private Map<ModuleId, ModuleDefinition> moduleDefs;
    private List<URI> libraries;
    protected List<RepositoryChangeListener> listeners;

    public AbstractRepositoryImpl(String name, URI location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public URI getLocation() {
        return location;
    }

    public ModuleDefinition find(String name, String version) {
        return moduleDefs.get(AbstractFactory.getInstance().createModuleId(name, version));
    }

    public List<ModuleDefinition> findAll() {
        return new ArrayList<ModuleDefinition>(moduleDefs.values());
    }

    public List<ModuleDefinition> findAll(String name) {
        List<ModuleDefinition> result = new ArrayList<ModuleDefinition>();
        for (ModuleDefinition md : findAll()) {
            if (name.equals(md.getName())) result.add(md);
        }
        return result;
    }

    public void initialize() throws IOException {
        assert moduleDefs==null;    // TODO: is it allowed to call the initialize method multiple times?
        moduleDefs = new HashMap<ModuleId, ModuleDefinition>();
        libraries = new ArrayList<URI>();
        loadModuleDefs(moduleDefs, libraries);
    }

    /**
     * Called from {@link #initialize()} to load all {@link ModuleDefinition}s and libraries defintions
     */
    protected abstract void loadModuleDefs(Map<ModuleId, ModuleDefinition> moduleDefs,
                                           List<URI> libraries) throws IOException;

    /**
     * Loads a jar file and builds a {@link ModuleDefinition}.
     *
     * <p>
     * The system allows {@link ModuleDefinition}s to be built in any way,
     * but in practice module jars need to be built in a way agnostic
     * to {@link Repository} implementations (so that same module could
     * be used in different {@link Repository}s), so it makes sense
     * to try to stick to the "common" loading scheme.
     *
     * @param jar
     *      Either a jar file or a directory that has the same structure as a jar file. 
     */
    protected ModuleDefinition loadJar(File jar) throws IOException {
        Jar jarFile = Jar.create(jar);
        Manifest manifest = jarFile.getManifest();
        if (manifest==null) {
            // we cannot find a manifest file in the bundle, so we look
            // if there is a manifest file residing outside of the jar
            // file with the same jar file name with a .mf extension,
            // so for foo.jar, we look for foo.mf and if it is there,
            // we use it as the manifest file.
            String simpleName = jarFile.getBaseName();
            File manifestFile = new File(jar.getParentFile(), simpleName+".mf");
            if (manifestFile.exists()) {
                InputStream is=null;
                try {
                    is = new BufferedInputStream(new FileInputStream(manifestFile));
                    manifest = new Manifest(is);
                } finally {
                    if (is!=null) {
                        is.close();
                    }
                }
            }
        }
        if (manifest!=null) {
            Attributes attr = manifest.getMainAttributes();
            String bundleName = attr.getValue(ManifestConstants.BUNDLE_NAME);
            if (bundleName!=null) {
                return newModuleDefinition(jar, attr);
            }
        }

        /* this has undesirable side effect of picking up jar files
           referenced via class-path as modules. so just return null.

        // if we are here, that mean that either there was no co-bundled or
        // external manifest file for this jar file OR the manifest file did
        // not contain the manifest entries for module management
        // in that case, I am just adding the jar file to the repository
        // with no dependency management.
        return new CookedModuleDefinition(jar, null);
         */
        return null;
    }

    protected void add(ModuleDefinition def) {
        moduleDefs.put(AbstractFactory.getInstance().createModuleId(def), def);
    }

    protected void remove(ModuleDefinition def) {
        moduleDefs.remove(AbstractFactory.getInstance().createModuleId(def));
    }

    protected void addLibrary(URI location) {
        libraries.add(location);
    }

    protected void removeLibrary(URI location) {
        libraries.remove(location);
    }

    public void shutdown() throws IOException {
        // nothing to do
    }

    public String toString() {
        StringBuffer s= new StringBuffer();
        for (ModuleDefinition moduleDef : findAll()) {
            s.append(moduleDef.getName()).append(":");
        }
        return s.toString();
    }

    /**
     * Returns the plain jar files installed in this repository. Plain jar files
     * are not modules, they do not have the module's metadata and can only be used
     * when referenced from a module dependency list or when added to a class
     * loader directly
     *
     * @return jar files location stored in this repository.
     */
    public List<URI> getJarLocations() {
        return Collections.unmodifiableList(libraries);
    }

    /**
     * Add a listener to changes happening to this repository. Repository can
     * change during the lifetime of an execution (files added/removed/changed)
     *
     * @param listener implementation listening to this repository changes
     * @return true if the listener was added successfully
     */
    public synchronized boolean addListener(RepositoryChangeListener listener) {
        if (listeners==null) {
            listeners = new ArrayList<RepositoryChangeListener>();
        }
        return listeners.add(listener);
    }

    /**
     * Removes a previously registered listener
     *
     * @param listener the previously registered listener
     * @return true if the listener was successfully unregistered
     */
    public synchronized boolean removeListener(RepositoryChangeListener listener) {
        if (listeners==null) {
            return false;
        }
        return listeners.remove(listener);
    }

    /**
     * Extensibility point for subclasses to create a different instance
     * of {@link ModuleDefinition}.
     *
     * @param jar
     *      The module jar file for which {@link ModuleDefinition} will be created.
     *      Never null.
     */
    protected ModuleDefinition newModuleDefinition(File jar, Attributes attr) throws IOException {
        return new DefaultModuleDefinition(jar, attr);
    }

}
