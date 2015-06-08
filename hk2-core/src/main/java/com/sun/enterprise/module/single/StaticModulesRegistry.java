/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.module.bootstrap.StartupContext;

import java.util.List;
import java.util.Properties;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * Implementation of the modules registry that use a single class loader to load
 * all available classes. There is one virtual module available in the modules
 * registry and that module's class loader is the single class loader used to
 * load all artifacts.
 *
 * @author Jerome Dochez
 */
public class StaticModulesRegistry extends SingleModulesRegistry {
              
    final private StartupContext startupContext; 

    public StaticModulesRegistry(ClassLoader singleCL) {
        super(singleCL);
        startupContext = null;
    }

    public StaticModulesRegistry(ClassLoader singleCL, StartupContext startupContext) {
        super(singleCL);
        this.startupContext = startupContext;
    }

    public StaticModulesRegistry(ClassLoader singleCL, List<ManifestProxy.SeparatorMappings> mappings, StartupContext startupContext) {
        super(singleCL, mappings);
        this.startupContext = startupContext;
    }

    @Override
    public void populateConfig(ServiceLocator serviceLocator) {
        // do nothing...
    }

    @Override
    public ServiceLocator createServiceLocator(String name) throws MultiException {
        ServiceLocator serviceLocator = super.createServiceLocator(name);

        StartupContext sc = startupContext;

        if (startupContext==null) {
            sc = new StartupContext(new Properties());
        }

        DynamicConfigurationService dcs = serviceLocator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        config.bind(BuilderHelper.createConstantDescriptor(sc));
        config.commit();
        
        return serviceLocator;
    }

}
