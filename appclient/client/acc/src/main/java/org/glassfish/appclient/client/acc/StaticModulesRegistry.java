/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantsParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of ModulesRegistry for HK2 modules for use from static
 * (non-OSGi) Java programs.
 * <p>
 * This is inspired by the anonymous inner class extension of ModulesRegistryImpl
 * contained in ASMainStatic.
 *
 * @author dochez
 * @author tjquinn
 */
public class StaticModulesRegistry extends ModulesRegistryImpl {
    private final Module[] proxyMod = new Module[1];

    public StaticModulesRegistry() {
        super(null);
    }

    public Module find(Class clazz) {
        Module m = super.find(clazz);
        if (m == null)
            return proxyMod[0];
        return m;
    }

    @Override
    public Collection<Module> getModules() {
        ArrayList<Module> list = new ArrayList<Module>();
        list.add(proxyMod[0]);
        return list;
    }

    @Override
    public Module makeModuleFor(String name, String version) throws ResolveError {
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

        for (ModuleMetadata.InhabitantsDescriptor d : proxyMod[0].getMetadata().getHabitats(name)) {
            inhabitantsParser.parse(d.createScanner(),holder);
        }
    }
}