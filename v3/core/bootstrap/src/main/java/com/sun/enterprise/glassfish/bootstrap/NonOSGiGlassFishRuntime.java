/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import org.glassfish.experimentalgfapi.GlassFish;
import org.glassfish.experimentalgfapi.GlassFishRuntime;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.util.Properties;

/**
 * @author bhavanishankar@dev.java.net
 */

public class NonOSGiGlassFishRuntime extends GlassFishRuntime {

    Main main;

    public NonOSGiGlassFishRuntime(Main main) {
        this.main = main;
    }

    @Override
    public GlassFish newGlassFish(Properties properties) throws Exception {
        // set env props before updating config, because configuration update may actually trigger
        // some code to be executed which may be depending on the environment variable values.
        setEnv(properties);
        final StartupContext startupContext = new StartupContext(properties);
        ModulesRegistry modulesRegistry = AbstractFactory.getInstance().createModulesRegistry();
        final Habitat habitat = main.createHabitat(modulesRegistry, startupContext);
        final ModuleStartup gfKernel = main.findStartupService(modulesRegistry, habitat, null, startupContext);
        return new GlassFishImpl(gfKernel, habitat);
    }

    private void setEnv(Properties properties) {
        final String installRootValue = properties.getProperty(Constants.INSTALL_ROOT_PROP_NAME);
        if (installRootValue != null && !installRootValue.isEmpty()) {
            File installRoot = new File(installRootValue);
            System.setProperty(Constants.INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
            final Properties asenv = ASMainHelper.parseAsEnv(installRoot);
            for (String s : asenv.stringPropertyNames()) {
                System.setProperty(s, asenv.getProperty(s));
            }
            System.setProperty(Constants.INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());
        }
        final String instanceRootValue = properties.getProperty(Constants.INSTANCE_ROOT_PROP_NAME);
        if (instanceRootValue != null && !instanceRootValue.isEmpty()) {
            File instanceRoot = new File(instanceRootValue);
            System.setProperty(Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
            System.setProperty(Constants.INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());
        }
    }

}
