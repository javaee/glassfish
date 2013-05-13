/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;

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
    protected List<ActiveDescriptor> parseInhabitants(Module module, String name, ServiceLocator serviceLocator, List<PopulatorPostProcessor> postProcessors)
            throws IOException {
        return null;
    }

}
