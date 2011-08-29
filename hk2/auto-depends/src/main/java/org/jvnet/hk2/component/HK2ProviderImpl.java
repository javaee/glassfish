/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.component;

import com.sun.hk2.component.ConstructorCreator;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.ScopedInhabitant;
import org.glassfish.hk2.*;
import org.glassfish.hk2.spi.HK2Provider;
import org.jvnet.hk2.annotations.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Default Hk2 Provider based on the habitat.
 *
 * @author Jerome Dochez
 */
public class HK2ProviderImpl implements HK2Provider {

    @Override
    public Services create(Services parent, Class<? extends Module>... moduleTypes) {

        final Habitat habitat = new Habitat(parent, moduleTypes.length>0?getModuleName(moduleTypes[0]):null);
        final DynamicBinderFactory parentBinder = (parent==null?null:parent.bindDynamically());
        final BinderFactoryImpl binderFactory = new BinderFactoryImpl(parentBinder);

        // The trick in the code below is that when users request an instance of Services,
        // we need to run the corresponding Module configuration.
        List<Inhabitant<Services>> inhabitants = new ArrayList<Inhabitant<Services>>();

        for (Class<? extends Module> moduleType : moduleTypes) {

            // We create an inhabitant for the Module service, this will allow us to use normal HK2
            // dependency resolution between modules.
            ConstructorCreator<Module> inhabitant = new ConstructorCreator<Module>(moduleType, habitat, null);
            final ScopedInhabitant<Module> scopedInhabitant = new ScopedInhabitant<Module>(inhabitant, new Scope() {
                @Override
                public ScopeInstance current() {
                     return habitat.singletonScope;
                }
            });

            final String moduleName = getModuleName(moduleType);

            // Here we use a special existing singleton inhabitant for Services instance so we
            // can be notified when the Services instance is requested in order to perform the
            // Module instantiation and configuration
            // At the end, all Services instances are the unique habitat, which is registered
            // multiple times under each Services names (obtained from the Module's Service
            // annotation)
            final Inhabitant<Services> servicesInhabitant = new ExistingSingletonInhabitant<Services>(habitat) {
                    @Override
                    public Services get(Inhabitant onBehalfOf) throws ComponentException {
                        if (!scopedInhabitant.isActive()) {
                            // time to get the module instance finally
                            Module module = scopedInhabitant.get(null);
                            // run its configuration
                            module.configure(binderFactory);
                            // register all the bindings now.
                            // start with the parent
                            if (parentBinder!=null) {
                                parentBinder.commit();
                            }
                            binderFactory.registerIn(habitat);
                        }
                        return super.get(onBehalfOf);
                    }
            };

            // if the Module was registered with a module name, we should register the
            // module instance
            if (moduleName!=null) {
                habitat.addIndex(servicesInhabitant, Module.class.getName(), moduleName);
                habitat.addIndex(servicesInhabitant, Services.class.getName(), moduleName);
            }
            inhabitants.add(servicesInhabitant);
        }

        // all the modules inhabitants are added, this should take care of instantiating
        // in the right order, as well as calling Module.configure at the same time the
        // postConstruct is called
        for (Inhabitant<Services> serviceInhabitant : inhabitants) {
            serviceInhabitant.get();
        }

        return habitat;
    }

    public Services create(Services parent, Module... modules) {

        Habitat habitat = new Habitat(parent, modules.length>0?getModuleName(modules[0].getClass()):null);
        DynamicBinderFactory parentBinder = (parent==null?null:parent.bindDynamically());
        BinderFactoryImpl binderFactory = new BinderFactoryImpl(parentBinder);

        for (Module module : modules) {
            habitat.inject(module);
            module.configure(binderFactory);
            if (parentBinder!=null) {
                parentBinder.commit();
            }
            binderFactory.registerIn(habitat);
        }
        return habitat;
    }


    private String getModuleName(Class<? extends Module> module) {
        Service service = module.getAnnotation(Service.class);
        if (service!=null) {
            return service.name();
        }
        return null;
    }
}
