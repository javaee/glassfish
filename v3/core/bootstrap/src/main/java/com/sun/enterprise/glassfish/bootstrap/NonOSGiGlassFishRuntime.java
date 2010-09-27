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
import org.glassfish.simpleglassfishapi.GlassFish;
import org.glassfish.simpleglassfishapi.GlassFishRuntime;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;
import org.glassfish.simpleglassfishapi.GlassFishException;
import org.glassfish.simpleglassfishapi.GlassFishOptions;

/**
 * The GlassFishRuntime implementation for NonOSGi environments.
 * @author bhavanishankar@dev.java.net
 */
public class NonOSGiGlassFishRuntime extends GlassFishRuntime {

    private Main main;
    private HashMap gfMap = new HashMap<String, GlassFish>();
    private static Logger logger = Util.getLogger();

    public NonOSGiGlassFishRuntime(Main main) {
        this.main = main;
    }


    /**
     * Creates a new GlassFish instance and add it to a Map of instances
     * created by this runtime.
     * @param gfOptions
     * @return
     * @throws Exception
     */
    @Override
    public GlassFish newGlassFish(GlassFishOptions gfOptions) throws GlassFishException {
        // set env props before updating config, because configuration update may actually trigger
        // some code to be executed which may be depending on the environment variable values.
         try {
            setEnv(gfOptions);

            final StartupContext startupContext = new StartupContext(gfOptions.getAllOptions());
            ModulesRegistry modulesRegistry = AbstractFactory.getInstance().createModulesRegistry();
            final Habitat habitat = main.createHabitat(modulesRegistry, startupContext);
            final ModuleStartup gfKernel = main.findStartupService(modulesRegistry, habitat, null, startupContext);
            // create a new GlassFish instance
            GlassFishImpl gfImpl = new GlassFishImpl(gfKernel, habitat, gfOptions.getAllOptions());
            // Add this newly created instance to a Map
            gfMap.put(gfOptions.getInstanceRoot(), gfImpl);
            return gfImpl;
        } catch(Exception e) {
            throw new GlassFishException(e);
        }
    }

    @Override
    protected void disposeGlassFishInstances() {
        for(Object gf : gfMap.values()) {
            ((GlassFish)gf).dispose();
        }
    }

    private void setEnv(GlassFishOptions gfOptions) throws Exception {
        /*
        final String installRootValue = properties.getProperty(Constants.INSTALL_ROOT_PROP_NAME);
        if (installRootValue != null && !installRootValue.isEmpty()) {
        File installRoot = new File(installRootValue);
        System.setProperty(Constants.INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
        final Properties asenv = ASMainHelper.parseAsEnv(installRoot);
        for (String s : asenv.stringPropertyNames()) {
        System.setProperty(s, asenv.getProperty(s));
        }
        System.setProperty(Constants.INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());
        } */
        String instanceRootValue = gfOptions.getInstanceRoot();
        if (instanceRootValue == null) {
            instanceRootValue = createDefaultInstanceRoot();
            gfOptions.setInstanceRoot(instanceRootValue);
            gfOptions.setInstanceRootUri(new File(instanceRootValue).toURI().toString());
        }

        File instanceRoot = new File(instanceRootValue);
        System.setProperty(Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
        System.setProperty(Constants.INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());
        provisionInstanceRoot(instanceRoot, gfOptions.getAllOptions());

        // Copy the configFile to the instanceRoot/config
        copyConfigFile(gfOptions.getConfigFileUri(), instanceRootValue);
    }


   private String createDefaultInstanceRoot() throws Exception {
        String tmpDir = System.getProperty("glassfish.embedded.tmpdir");
        if (tmpDir == null) {
            tmpDir = System.getProperty("user.dir");
        }
        File instanceRoot = File.createTempFile("gfembed", "tmp", new File(tmpDir));
        instanceRoot.delete();
        instanceRoot.mkdir(); // convert the file into a directory.
        return instanceRoot.getAbsolutePath();
    }

    private void provisionInstanceRoot(File instanceRoot, Properties props) {
        new File(instanceRoot, "config").mkdirs();
        new File(instanceRoot, "docroot").mkdirs();
        try {
            // If the regular installation location can be discovered,
            // discover it and copy the necessary configuration files.
            File discoveredInstallRoot = ASMainHelper.findInstallRoot();
            File discoveredInstanceRoot = ASMainHelper.findInstanceRoot(discoveredInstallRoot, props);
            if (!discoveredInstanceRoot.equals(instanceRoot)) {
                copy(discoveredInstanceRoot, instanceRoot, "config", "docroot");
            }
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
    }

    // Copy the subDirs under srcDir to dstDir

    private void copy(final File srcDir, final File dstDir, final String... subDirs) {
        srcDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File path) {
                if (path.isDirectory()) {
                    path.listFiles(this);
                } else {
                    for (String subDir : subDirs) {
                        String srcPath = new File(srcDir, subDir).getPath();
                        if (path.getPath().startsWith(srcPath)) {
                            File dstFile = new File(dstDir, path.getPath().substring(srcDir.getPath().length()));
                            if (!dstFile.exists()) {
                                dstFile.getParentFile().mkdirs();
                                try {
                                    Util.copyFile(path, dstFile);
//                                    logger.info("Copied " + path + " to " + dstFile);
                                } catch (Exception ex) {
                                    logger.warning(ex.getMessage());
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    private void copyConfigFile(String configFileURI, String instanceRoot) throws Exception {
        if (configFileURI != null && instanceRoot != null) {
            URI configFile = URI.create(configFileURI);
            InputStream stream = configFile.toURL().openConnection().getInputStream();
            File domainXml = new File(instanceRoot, "config/domain.xml");
            logger.finer("domainXML uri = " + configFileURI + ", size = " + stream.available());
            if (!domainXml.toURI().equals(configFile)) {
                Util.copy(stream, new FileOutputStream(domainXml), stream.available());
                logger.finer("Created " + domainXml);
            } else {
                logger.finer("Skipped creation of " + domainXml);
            }

        }
    }


}
