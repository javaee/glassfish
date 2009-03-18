/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.module.single;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.ExistingSingletonInhabitant;

import java.util.Collection;
import java.util.ArrayList;
import java.io.IOException;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.config.ConfigParser;

/**
 * Implementation of the modules registry that use a single class loader to load
 * all available classes. There is one virtual module available in the modules
 * registry and that module's class loader is the single class loader used to
 * load all artifacts.
 *
 * @author Jerome Dochez
 */
public class StaticModulesRegistry extends ModulesRegistryImpl {

    final ClassLoader singleClassLoader;
    final Module[] proxyMod = new Module[1];

    public StaticModulesRegistry(ClassLoader singleCL) {
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

    @Override
    public Habitat createHabitat(String name, InhabitantsParser parser) throws ComponentException {
        try {
            Habitat habitat = parser.habitat;

            for (final Module module : getModules())
                parseInhabitants(module, name,parser);

            // default modules registry is the one that created the habitat
            habitat.addIndex(new ExistingSingletonInhabitant<ModulesRegistry>(this),
                    ModulesRegistry.class.getName(), null);
            
            return habitat;
        } catch (IOException e) {
            throw new ComponentException("Failed to create a habitat",e);
        }
    }
}
