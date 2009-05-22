package com.sun.enterprise.module.single;

import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.Holder;

import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Normal modules registry with configuration handling backed up
 * by a single class loader. There is one virtual module available in the modules
 * registry and that module's class loader is the single class loader used to
 * load all artifacts.
 *
 * @author Jerome Dochez
 */
public class SingleModulesRegistry  extends ModulesRegistryImpl {

    final ClassLoader singleClassLoader;
    final Module[] proxyMod = new Module[1];

    public SingleModulesRegistry(ClassLoader singleCL) {
        super(null);
        this.singleClassLoader = singleCL;
        setParentClassLoader(singleClassLoader);

        ModuleDefinition moduleDef = null;
        try {
            moduleDef = new ProxyModuleDefinition(singleClassLoader);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        proxyMod[0] = new ProxyModule(this, moduleDef, singleClassLoader);
        add(moduleDef);
    }

    @Override
    public Module find(Class clazz) {
        Module m = super.find(clazz);
        if (m == null)
            return proxyMod[0];
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
        list.add(proxyMod[0]);
        return list;
    }

    @Override
    public Module makeModuleFor(String name, String version, boolean resolve) throws ResolveError {
        return proxyMod[0];
    }

    @Override
    public void parseInhabitants(Module module, String name, InhabitantsParser inhabitantsParser)
            throws IOException {

        Holder<ClassLoader> holder = new Holder<ClassLoader>() {
            public ClassLoader get() {
                return proxyMod[0].getClassLoader();
            }
        };

        for (ModuleMetadata.InhabitantsDescriptor d : proxyMod[0].getMetadata().getHabitats(name))
            inhabitantsParser.parse(d.createScanner(), holder);
    }

}
