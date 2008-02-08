/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.common_impl.AbstractModulesRegistryImpl;
import com.sun.hk2.component.InhabitantsParser;

import java.io.IOException;
import java.util.Collection;
import java.net.URL;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ModulesRegistryImpl extends AbstractModulesRegistryImpl {
    private ClassLoader parentLoader;

    /* package */ ModulesRegistryImpl(AbstractModulesRegistryImpl parent) {
        super(parent);
    }

    /**
     * Creates a new child {@link ModulesRegistryImpl} in this {@link ModulesRegistryImpl}.
     */
    public ModulesRegistry createChild() {
        return new ModulesRegistryImpl(this);
    }

    protected void parseInhabitants(
            Module module, String name, InhabitantsParser inhabitantsParser)
            throws IOException {
        ModuleImpl.class.cast(module).parseInhabitants(name, inhabitantsParser);
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

        if (modules.containsKey(moduleName)) {
            Module module = modules.get(moduleName);
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
     * Returns a ClassLoader capable of loading classes from a set of modules indentified
     * by their module definition
     *
     * @param parent the parent class loader for the returned class loader instance
     * @param defs module definitions for all modules this classloader should be capable of loading
     * classes from
     * @return class loader instance
     * @throws com.sun.enterprise.module.ResolveError if one of the provided module definition cannot be resolved
     */
    public ClassLoader getModulesClassLoader(ClassLoader parent, Collection<ModuleDefinition> defs)
        throws ResolveError {

        if (parent==null) {
            parent = getParentClassLoader();
        }
        ClassLoaderProxy cl = new ClassLoaderProxy(new URL[0], parent);
        for (ModuleDefinition def : defs) {
            ModuleImpl module = ModuleImpl.class.cast(this.makeModuleFor(def.getName(), def.getVersion()));
            cl.addDelegate(module.getClassLoader());
        }
        return cl;
    }
}
