/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.embed.impl;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import java.io.IOException;

/**
 *
 * @author Byron Nevins
 */
public class EmbeddedModulesRegistryImpl extends ModulesRegistryImpl {

    public EmbeddedModulesRegistryImpl() throws IOException {
        super(null);
        proxyModuleDefinition = new ProxyModuleDefinition(getClass().getClassLoader());
        proxyModule = add(proxyModuleDefinition);
    }

    @Override
    public Module find(Class clazz) {
        Module m = super.find(clazz);

        if (m == null)
            m = proxyModule;
        
        return m;
    }

    private Module                  proxyModule;
    private ProxyModuleDefinition   proxyModuleDefinition;
}
