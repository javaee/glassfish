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
import com.sun.enterprise.module.common_impl.AbstractFactory;
import org.glassfish.simpleglassfishapi.Constants;
import org.glassfish.simpleglassfishapi.GlassFishRuntime;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bhavanishankar@dev.java.net
 */

public class NonOSGiGlassFishRuntimeBuilder implements GlassFishRuntime.RuntimeBuilder {

    private static Logger logger = Util.getLogger();
    private static final String JAR_EXT = ".jar";

    public GlassFishRuntime build(Properties properties) throws Exception {
        /* Step 1. Build the classloader. */
        // The classloades should contain installRoot/modules/**/*.jar files.
        String installRoot = ASMainHelper.findInstallRoot().getAbsolutePath();
        List<URL> moduleJarURLs = getModuleJarURLs(installRoot);
        ClassLoader cl = new StaticClassLoader(getClass().getClassLoader(), moduleJarURLs);

        // Step 2. Setup the module subsystem.
        Main main = new EmbeddedMain();
        SingleHK2Factory.initialize(cl);
        ModulesRegistry modulesRegistry = AbstractFactory.getInstance().createModulesRegistry();
        modulesRegistry.setParentClassLoader(cl);

        // Step 3. Create NonOSGIGlassFishRuntime
        GlassFishRuntime glassFishRuntime = new NonOSGiGlassFishRuntime(main);
        logger.logp(Level.INFO, getClass().getName(), "build", "Created GlassFishRuntime {0} " +
                "with Bootstrap Properties {1}", new Object[]{glassFishRuntime, properties});
        return glassFishRuntime;
    }

    public boolean handles(Properties properties) {
        org.glassfish.simpleglassfishapi.Constants.Platform platform =
                Constants.Platform.valueOf(properties.getProperty(
                        Constants.PLATFORM_PROPERTY_KEY, Constants.Platform.Felix.name()));
        switch (platform) {
            case Static:
                return true;
        }
        return false;
    }

    public void destroy() throws Exception {
    }

    private List<URL> getModuleJarURLs(String installRoot) {
        File modulesDir = new File(installRoot, "modules/");
        final File autostartModulesDir = new File(modulesDir, "autostart/");
        final List<URL> moduleJarURLs = new ArrayList<URL>();
        modulesDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory() && !pathname.equals(autostartModulesDir)) {
                    pathname.listFiles(this);
                } else if (pathname.getName().endsWith(JAR_EXT)) {
                    try {
                        moduleJarURLs.add(pathname.toURI().toURL());
                    } catch (Exception ex) {
                        logger.warning(ex.getMessage());
                    }
                }
                return false;
            }
        });
        return moduleJarURLs;
    }

    private class StaticClassLoader extends URLClassLoader {
        public StaticClassLoader(ClassLoader parent, List<URL> moduleJarURLs) {
            super(moduleJarURLs.toArray(new URL[0]), parent);
        }
    }

}
