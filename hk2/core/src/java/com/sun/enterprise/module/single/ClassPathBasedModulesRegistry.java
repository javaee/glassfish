package com.sun.enterprise.module.single;

import com.sun.enterprise.module.InhabitantsDescriptor;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantsParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Implements a modules registry based on a class-path style of module
 * description using a single class loader (capable of loading the entire
 * class-path)
 *
 * @author Jerome Dochez
 */
public class ClassPathBasedModulesRegistry extends ModulesRegistryImpl {

    final ClassLoader cLoader;
    final List<ModuleDefinition> moduleDefs = new ArrayList<ModuleDefinition>();
    final List<Module> modules = new ArrayList<Module>();


    public ClassPathBasedModulesRegistry(ClassLoader singleCL, String classPath) throws IOException {

        super(null);
        this.cLoader = singleCL;
        setParentClassLoader(cLoader);

        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String classPathElement = st.nextToken();
            File f = new File(classPathElement);
            if (f.exists()) {
                ModuleDefinition md = new DefaultModuleDefinition(f);
                moduleDefs.add(md);
                add(md);
            }
        }

        // now create fake modules
        for (ModuleDefinition md : moduleDefs) {
            // they all use the same class loader since they are not really modules
            // and we don't run in a modular environment
            modules.add(new ProxyModule(this, md, cLoader));
        }
    }

    @Override
    public Module find(Class clazz) {
        Module m = super.find(clazz);
        // all modules can load all classes
        if (m == null)
            return modules.get(0);
        return m;
    }

    @Override
    public Collection<Module> getModules(String moduleName) {
        // I could not care less about the modules names
        return getModules();
    }

    @Override
    public Collection<Module> getModules() {
        ArrayList<Module> list = new ArrayList<Module>();
        list.addAll(modules);
        return list;
    }

    @Override
    public Module makeModuleFor(String name, String version, boolean resolve) throws ResolveError {
        for (int i=0;i<moduleDefs.size();i++) {
            ModuleDefinition md = moduleDefs.get(i);
            if (md.getName().equals(name)) {
                return modules.get(i); 
            }
        }
        return null;
    }

    @Override
    public void parseInhabitants(Module module, String name, InhabitantsParser inhabitantsParser)
            throws IOException {

        Holder<ClassLoader> holder = new Holder<ClassLoader>() {
            public ClassLoader get() {
                return cLoader;
            }
        };

        for (Module m : modules) {
            // each module can have a different way of representing the inhabitant meta-data
            // some may use the inhabitant file, others may rely on introspection 
            for (InhabitantsDescriptor d :m.getMetadata().getHabitats(name))
                inhabitantsParser.parse(d.createScanner(), holder);
        }
    }

}
