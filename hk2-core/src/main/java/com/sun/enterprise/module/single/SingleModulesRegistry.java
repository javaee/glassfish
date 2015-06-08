/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.module.single;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;

import java.io.IOException;
import java.util.*;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Populator;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.impl.Hk2LoaderPopulatorPostProcessor;
import org.glassfish.hk2.utilities.ClasspathDescriptorFileFinder;

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
        this(singleCL, null);
    }


    public SingleModulesRegistry(ClassLoader singleCL, List<ManifestProxy.SeparatorMappings> mappings) {

        super(null);
        this.singleClassLoader = singleCL;
        setParentClassLoader(singleClassLoader);

        ModuleDefinition moduleDef = null;
        try {
            moduleDef = new ProxyModuleDefinition(singleClassLoader, mappings);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected List<ActiveDescriptor> parseInhabitants(Module module, String name, ServiceLocator serviceLocator, List<PopulatorPostProcessor> postProcessors)
            throws IOException {

        ArrayList<PopulatorPostProcessor> allPostProcessors = new ArrayList<PopulatorPostProcessor>();

        allPostProcessors.add(new Hk2LoaderPopulatorPostProcessor(singleClassLoader));
        if (postProcessors != null) {
          allPostProcessors.addAll(postProcessors);
        }

        DynamicConfigurationService dcs = serviceLocator.getService(DynamicConfigurationService.class);
        Populator populator = dcs.getPopulator();
        
    	List<ActiveDescriptor<?>> retVal = populator.populate(
                new ClasspathDescriptorFileFinder(singleClassLoader, name),
                allPostProcessors.toArray(new PopulatorPostProcessor[allPostProcessors.size()]));
    	
    	return (List<ActiveDescriptor>) ((List) retVal);
    }

}
