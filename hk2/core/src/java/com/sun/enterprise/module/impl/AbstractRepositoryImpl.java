package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Repository;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Partial implementation of {@link Repository}
 * that statically enumerates all {@link ModuleDefinition}
 * upfront.
 *
 * @author Kohsuke Kawaguchi
 * @author Jerome Dochez
 */
public abstract class AbstractRepositoryImpl implements Repository {
    private final String name;
    private final URI location;
    private Map<String,ModuleDefinition> moduleDefs;
    /**
     * {@link #moduleDefs}'s values in a read-only list.
     */
    private List<ModuleDefinition> allModules;


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
        return moduleDefs.get(name);
    }

    public List<ModuleDefinition> findAll() {
        return allModules;
    }

    public List<ModuleDefinition> findAll(String name) {
        return Collections.singletonList(moduleDefs.get(name));
    }

    public void initialize() throws IOException {
        moduleDefs = loadModuleDefs();
        allModules = Collections.unmodifiableList(new ArrayList<ModuleDefinition>(moduleDefs.values()));
    }

    /**
     * Called from {@link #initialize()} to load all {@link ModuleDefinition}s.
     */
    protected abstract Map<String, ModuleDefinition> loadModuleDefs() throws IOException;

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
        if (manifest==null || manifest.getMainAttributes().getValue(ManifestConstants.BUNDLE_NAME)==null) {
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

    public Module newModule(ModulesRegistry registry, ModuleDefinition def) {
        return new Module(registry,def);
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
